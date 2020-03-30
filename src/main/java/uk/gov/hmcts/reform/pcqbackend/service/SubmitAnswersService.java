package uk.gov.hmcts.reform.pcqbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidRequestException;
import uk.gov.hmcts.reform.pcqbackend.exceptions.SchemaValidationException;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;
import uk.gov.hmcts.reform.pcqbackend.utils.ConversionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;


@Slf4j
@Service
public class SubmitAnswersService {

    Environment environment;

    ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    @Autowired
    public SubmitAnswersService(ProtectedCharacteristicsRepository protectedCharacteristicsRepository,
                                Environment environment) {
        this.protectedCharacteristicsRepository = protectedCharacteristicsRepository;
        this.environment = environment;
    }

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveMethodLength"})
    @Transactional
    public ResponseEntity<Object> processPcqAnswers(List<String> headers, PcqAnswerRequest answerRequest) {
        String pcqId = answerRequest.getPcqId();
        String coRelationId = "";
        try {

            //Step 1. Check the request contains the required header content.
            coRelationId = ConversionUtil.validateRequestHeader(headers);
            coRelationId = coRelationId.replaceAll("[\n|\r|\t]", "_");
            log.info("Co-Relation Id : {} - submitAnswers API call invoked.", coRelationId);

            //Step 2. Validate the request body against the JSON Schema.
            validateRequestAgainstSchema(answerRequest, environment
                .getProperty("api-schema-file.submitanswer-schema"));

            //Step 3. Validate the version number of the request matches the back-end version.
            validateVersionNumber(answerRequest.getVersionNo());

            //Step 4. Check whether record exists in database for the pcqId.
            Optional<ProtectedCharacteristics> protectedCharacteristics = protectedCharacteristicsRepository
                .findById(answerRequest.getPcqId());

            ProtectedCharacteristics createCharacteristics = ConversionUtil.convertJsonToDomain(answerRequest);
            if (protectedCharacteristics.isEmpty()) {
                // Create the new PCQ Answers record.
                protectedCharacteristicsRepository.save(createCharacteristics);

                log.info("Co-Relation Id : {} - submitAnswers API, Protected Characteristic Record submitted "
                             + "for creation.", coRelationId);

            } else {
                // Update the PCQ Record.
                int resultCount = protectedCharacteristicsRepository.updateCharacteristics(
                    createCharacteristics.getDobProvided(),
                    createCharacteristics.getDateOfBirth(),
                    createCharacteristics.getMainLanguage(),
                    createCharacteristics.getOtherLanguage(),
                    createCharacteristics.getEnglishLanguageLevel(),
                    createCharacteristics.getSex(),
                    createCharacteristics.getGenderDifferent(),
                    createCharacteristics.getOtherGender(),
                    createCharacteristics.getSexuality(),
                    createCharacteristics.getOtherSexuality(),
                    createCharacteristics.getMarriage(),
                    createCharacteristics.getEthnicity(),
                    createCharacteristics.getOtherEthnicity(),
                    createCharacteristics.getReligion(),
                    createCharacteristics.getOtherReligion(),
                    createCharacteristics.getDisabilityConditions(),
                    createCharacteristics.getDisabilityImpact(),
                    createCharacteristics.getDisabilityVision(),
                    createCharacteristics.getDisabilityHearing(),
                    createCharacteristics.getDisabilityMobility(),
                    createCharacteristics.getDisabilityDexterity(),
                    createCharacteristics.getDisabilityLearning(),
                    createCharacteristics.getDisabilityMemory(),
                    createCharacteristics.getDisabilityMentalHealth(),
                    createCharacteristics.getDisabilityStamina(),
                    createCharacteristics.getDisabilitySocial(),
                    createCharacteristics.getDisabilityOther(),
                    createCharacteristics.getOtherDisabilityDetails(),
                    createCharacteristics.getDisabilityNone(),
                    createCharacteristics.getPregnancy(),
                    createCharacteristics.getCompletedDate(),
                    createCharacteristics.getPcqId(),
                    createCharacteristics.getCompletedDate());

                if (resultCount == 0) {
                    log.error("Co-Relation Id : {} - submitAnswers API, Completed Date is in the past.", coRelationId);
                    return ConversionUtil.generateResponseEntity(pcqId, HttpStatus.ACCEPTED,
                                                                 environment.getProperty(
                                                                     "api-error-messages.accepted"));
                } else {
                    log.info("Co-Relation Id : {} - submitAnswers API, Protected Characteristic Record "
                                 + "submitted for Update.", coRelationId);
                }
            }

        } catch (InvalidRequestException ive) {
            log.error(ive.getMessage());
            return ConversionUtil.generateResponseEntity(pcqId, ive.getErrorCode(),
                                          environment.getProperty("api-error-messages.bad_request"));
        } catch (SchemaValidationException sve) {
            log.error(
                "Co-Relation Id : {} - submitAnswers API failed schema validations. "
                    + "Detailed error message as follows \n {}",
                coRelationId,
                sve.getFormattedError()
            );
            return ConversionUtil.generateResponseEntity(pcqId, HttpStatus.BAD_REQUEST,
                                          environment.getProperty("api-error-messages.bad_request"));
        } catch (IOException ioe) {
            log.error("Co-Relation Id : {} - submitAnswers API call failed "
                          + "due to error - {}", coRelationId, ioe.getMessage());
            return ConversionUtil.generateResponseEntity(pcqId, HttpStatus.INTERNAL_SERVER_ERROR,
                                          environment.getProperty("api-error-messages.internal_error"));
        } catch (Exception e) {
            log.error("Co-Relation Id : {} - submitAnswers API call failed "
                          + "due to error - {}", coRelationId, e.getMessage(), e);
            return ConversionUtil.generateResponseEntity(pcqId, HttpStatus.INTERNAL_SERVER_ERROR,
                                          environment.getProperty("api-error-messages.internal_error"));
        }

        return ConversionUtil.generateResponseEntity(pcqId, HttpStatus.CREATED,
                                      environment.getProperty("api-error-messages.created"));
    }

    public ProtectedCharacteristics getProtectedCharacteristicsById(String pcqId) {
        log.info("getAnswer API invoked");
        Optional<ProtectedCharacteristics> protectedCharacteristics = protectedCharacteristicsRepository
            .findById(pcqId);

        return protectedCharacteristics.orElse(null);

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
        try (InputStream inputStream = new ClassPathResource(schemaFileName).getInputStream()) {
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

        }

    }

    private void validateVersionNumber(int requestVersionNumber) throws InvalidRequestException {
        if (requestVersionNumber != Integer.parseInt(Objects.requireNonNull(environment.getProperty(
            "api-version-number")))) {
            throw new InvalidRequestException("Version number mis-match", HttpStatus.FORBIDDEN);
        }
    }

}
