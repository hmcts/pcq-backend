package uk.gov.hmcts.reform.pcqbackend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidRequestException;
import uk.gov.hmcts.reform.pcqbackend.exceptions.SchemaValidationException;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for the PCQ Back-End API calls.
 */
@RestController
@RequestMapping(path = "/pcq/backend")
public class PcqAnswersController {

    Logger logger = LoggerFactory.getLogger(PcqAnswersController.class);


    @Autowired
    private Environment environment;

    @PostMapping(path = "/submitAnswers", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals"})
    public ResponseEntity<Object> submitAnswers(@RequestHeader HttpHeaders headers,
                                                @RequestBody PcqAnswerRequest answerRequest) {

        int pcqId = answerRequest.getPcqId();
        String coRelationId = "";
        try {

            //Step 1. Check the request contains the required header content.
            coRelationId = validateRequestHeader(headers);
            coRelationId = coRelationId.replaceAll("[\n|\r|\t]", "_");
            logger.info("Co-Relation Id : {} - submitAnswers API call invoked.", coRelationId);

            //Step 2. Validate the request body against the JSON Schema.
            validateRequestAgainstSchema(answerRequest, environment
                .getProperty("api-schema-file.submitanswer-schema"));

            //Step 3. Validate the version number of the request matches the back-end version.
            validateVersionNumber(answerRequest.getVersionNo());

        } catch (InvalidRequestException ive) {
            logger.error(ive.getMessage());
            return generateResponseEntity(pcqId, ive.getErrorCode(),
                                          environment.getProperty("api-error-messages.bad_request"));
        } catch (SchemaValidationException sve) {
            logger.error(
                "Co-Relation Id : {} - submitAnswers API failed schema validations. "
                    + "Detailed error message as follows \n {}",
                coRelationId,
                sve.getFormattedError()
            );
            return generateResponseEntity(pcqId, HttpStatus.BAD_REQUEST,
                                          environment.getProperty("api-error-messages.bad_request"));
        } catch (IOException ioe) {
            logger.error("Co-Relation Id : {} - submitAnswers API call failed "
                        + "due to error - {}", coRelationId, ioe.getMessage());
            return generateResponseEntity(pcqId, HttpStatus.INTERNAL_SERVER_ERROR,
                                          environment.getProperty("api-error-messages.internal_error"));
        } catch (Exception e) {
            logger.error("Co-Relation Id : {} - submitAnswers API call failed "
                             + "due to error - {}", coRelationId, e.getMessage(), e);
            return generateResponseEntity(pcqId, HttpStatus.INTERNAL_SERVER_ERROR,
                                          environment.getProperty("api-error-messages.internal_error"));
        }

        return generateResponseEntity(pcqId, HttpStatus.CREATED,
                                      environment.getProperty("api-error-messages.created"));
    }

    private ResponseEntity<Object> generateResponseEntity(int pcqId, HttpStatus code, String message) {

        Map<String, Object> responseMap = new ConcurrentHashMap<>();
        responseMap.put("pcqId", Integer.valueOf(pcqId));
        responseMap.put("responseStatus", message);
        responseMap.put("responseStatusCode", String.valueOf(code.value()));

        return new ResponseEntity<>(responseMap, code);

    }

    private String validateRequestHeader(HttpHeaders requestHeader) throws InvalidRequestException {

        // Validate that the request contains the required Header values.
        String headerKey =  environment.getProperty("api-required-header-keys.co-relationid");
        if (!requestHeader.containsKey(headerKey)) {
            throw new InvalidRequestException("Invalid Request. Expecting required header - Co-Relation Id -"
                                                  + " in the request.", HttpStatus.BAD_REQUEST);
        }

        return requestHeader.get(headerKey).get(0);

    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private void validateRequestAgainstSchema(Object requestObject, String schemaFileName) throws IOException,
        SchemaValidationException {


        //Convert the requestObject to a JSON String.
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(requestObject);

        //Use the json string to form a JSONNode.
        JsonNode jsonNode = mapper.readTree(jsonString);

        //Generate the JSON Schema object from the schema file in the classpath.
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance();
        InputStream inputStream = new ClassPathResource(schemaFileName).getInputStream();
        try {
            JsonSchema jsonSchema = jsonSchemaFactory.getSchema(inputStream);

            //Now validate the json against the schema
            Set<ValidationMessage> errorSet = jsonSchema.validate(jsonNode);

            if (errorSet != null && !errorSet.isEmpty()) {

                StringBuilder strBuilder = new StringBuilder();

                //noinspection RedundantExplicitVariableType
                for (ValidationMessage validationMessage : errorSet) {
                    strBuilder.append(validationMessage.getMessage());
                    strBuilder.append(" ; ");
                }

                throw new SchemaValidationException("Request does not conform to JSON Schema.", strBuilder.toString());
            }

        } finally {
            //Close the stream at the end.
            inputStream.close();
        }

    }

    private void validateVersionNumber(int requestVersionNumber) throws InvalidRequestException {
        if (requestVersionNumber != Integer.valueOf(environment.getProperty("api-version-number"))) {
            throw new InvalidRequestException("Version number mis-match", HttpStatus.FORBIDDEN);
        }
    }


}
