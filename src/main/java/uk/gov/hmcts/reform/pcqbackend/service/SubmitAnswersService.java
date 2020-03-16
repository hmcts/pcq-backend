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
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswers;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SubmitAnswersService {

    @Autowired
    private Environment environment;

    @Autowired
    private ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals"})
    @Transactional
    public ResponseEntity<Object> processPcqAnswers(List<String> headers, PcqAnswerRequest answerRequest) {
        int pcqId = answerRequest.getPcqId();
        String coRelationId = "";
        try {

            //Step 1. Check the request contains the required header content.
            coRelationId = validateRequestHeader(headers);
            coRelationId = coRelationId.replaceAll("[\n|\r|\t]", "_");
            log.info("Co-Relation Id : {} - submitAnswers API call invoked.", coRelationId);

            //Step 2. Validate the request body against the JSON Schema.
            validateRequestAgainstSchema(answerRequest, environment
                .getProperty("api-schema-file.submitanswer-schema"));

            //Step 3. Validate the version number of the request matches the back-end version.
            validateVersionNumber(answerRequest.getVersionNo());

            //Step 4. Check whether record exists in database for the pcqId.
            Optional<ProtectedCharacteristics> protectedCharacteristics = protectedCharacteristicsRepository.
                findById(answerRequest.getPcqId());

            if (protectedCharacteristics.isEmpty())
            {
                // Create the new PCQ Answers record.
                ProtectedCharacteristics createCharacteristics = convertJsonToDomain(answerRequest);
                protectedCharacteristicsRepository.save(createCharacteristics);

                log.info("Co-Relation Id : {} - submitAnswers API, Protected Characterstic Record created.", coRelationId);

            } else {
                // Update the PCQ Record.
                ProtectedCharacteristics updateChars = convertJsonToDomain(answerRequest);
                int resultCount = protectedCharacteristicsRepository.updateCharacteristics(updateChars.getDobProvided(),
                                                                       updateChars.getDateOfBirth(),
                                                                       updateChars.getMainLanguage(),
                                                                       updateChars.getOtherLanguage(),
                                                                       updateChars.getEnglishLanguageLevel(),
                                                                       updateChars.getSex(),
                                                                       updateChars.getGenderDifferent(),
                                                                       updateChars.getOtherGender(),
                                                                       updateChars.getSexuality(),
                                                                       updateChars.getOtherSexuality(),
                                                                       updateChars.getMarriage(),
                                                                       updateChars.getEthnicity(),
                                                                       updateChars.getOtherEthnicity(),
                                                                       updateChars.getReligion(),
                                                                       updateChars.getOtherReligion(),
                                                                       updateChars.getDisabilityConditions(),
                                                                       updateChars.getDisabilityImpact(),
                                                                       updateChars.getDisabilityVision(),
                                                                       updateChars.getDisabilityHearing(),
                                                                       updateChars.getDisabilityMobility(),
                                                                       updateChars.getDisabilityDexterity(),
                                                                       updateChars.getDisabilityLearning(),
                                                                       updateChars.getDisabilityMemory(),
                                                                       updateChars.getDisabilityMentalHealth(),
                                                                       updateChars.getDisabilityStamina(),
                                                                       updateChars.getDisabilitySocial(),
                                                                       updateChars.getDisabilityOther(),
                                                                       updateChars.getOtherDisabilityDetails(),
                                                                       updateChars.getDisabilityNone(),
                                                                       updateChars.getPregnancy(),
                                                                       updateChars.getPcqId(),
                                                                       updateChars.getCompletedDate());

                if (resultCount == 0) {
                    log.error("Co-Relation Id : {} - submitAnswers API, Completed Date is in the past.", coRelationId);
                    return generateResponseEntity(pcqId, HttpStatus.ACCEPTED,
                                                  environment.getProperty("api-error-messages.accepted"));
                } else {
                    log.info("Co-Relation Id : {} - submitAnswers API, Protected Characterstic Record saved.",
                             coRelationId);
                }
            }

        } catch (InvalidRequestException ive) {
            log.error(ive.getMessage());
            return generateResponseEntity(pcqId, ive.getErrorCode(),
                                          environment.getProperty("api-error-messages.bad_request"));
        } catch (SchemaValidationException sve) {
            log.error(
                "Co-Relation Id : {} - submitAnswers API failed schema validations. "
                    + "Detailed error message as follows \n {}",
                coRelationId,
                sve.getFormattedError()
            );
            return generateResponseEntity(pcqId, HttpStatus.BAD_REQUEST,
                                          environment.getProperty("api-error-messages.bad_request"));
        } catch (IOException ioe) {
            log.error("Co-Relation Id : {} - submitAnswers API call failed "
                          + "due to error - {}", coRelationId, ioe.getMessage());
            return generateResponseEntity(pcqId, HttpStatus.INTERNAL_SERVER_ERROR,
                                          environment.getProperty("api-error-messages.internal_error"));
        } catch (Exception e) {
            log.error("Co-Relation Id : {} - submitAnswers API call failed "
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

    private String validateRequestHeader(List<String> requestHeaders) throws InvalidRequestException {

        // Validate that the request contains the required Header values.
        if (requestHeaders == null || requestHeaders.isEmpty()) {
            throw new InvalidRequestException("Invalid Request. Expecting required header - Co-Relation Id -"
                                                  + " in the request.", HttpStatus.BAD_REQUEST);
        }

        return requestHeaders.get(0);

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

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    private ProtectedCharacteristics convertJsonToDomain(PcqAnswerRequest pcqAnswerRequest) {
        ProtectedCharacteristics protectedCharacterstics = new ProtectedCharacteristics();
        protectedCharacterstics.setPcqId(pcqAnswerRequest.getPcqId());
        protectedCharacterstics.setActor(pcqAnswerRequest.getActor());
        protectedCharacterstics.setCaseId(pcqAnswerRequest.getCaseId());
        protectedCharacterstics.setChannel(pcqAnswerRequest.getChannel());
        protectedCharacterstics.setCompletedDate(getTimeFromString(pcqAnswerRequest.getCompletedDate()));
        protectedCharacterstics.setPartyId(pcqAnswerRequest.getPartyId());
        protectedCharacterstics.setServiceId(pcqAnswerRequest.getServiceId());
        protectedCharacterstics.setVersionNumber(pcqAnswerRequest.getVersionNo());

        PcqAnswers pcqAnswers = pcqAnswerRequest.getPcqAnswers();
        if (pcqAnswers != null) {
            protectedCharacterstics.setDobProvided(pcqAnswers.getDobProvided());
            if(pcqAnswers.getDob() != null) {
                protectedCharacterstics.setDateOfBirth(getDateFromString(pcqAnswers.getDob()));
            }
            protectedCharacterstics.setMainLanguage(pcqAnswers.getLanguageMain());
            protectedCharacterstics.setOtherLanguage(pcqAnswers.getLanguageOther());
            protectedCharacterstics.setEnglishLanguageLevel(pcqAnswers.getEnglishLanguageLevel());
            protectedCharacterstics.setSex(pcqAnswers.getSex());
            protectedCharacterstics.setGenderDifferent(pcqAnswers.getGenderDifferent());
            protectedCharacterstics.setOtherGender(pcqAnswers.getGenderOther());
            protectedCharacterstics.setSexuality(pcqAnswers.getSexuality());
            protectedCharacterstics.setOtherSexuality(pcqAnswers.getSexualityOther());
            protectedCharacterstics.setMarriage(pcqAnswers.getMarriage());
            protectedCharacterstics.setEthnicity(pcqAnswers.getEthnicity());
            protectedCharacterstics.setOtherEthnicity(pcqAnswers.getEthnicityOther());
            protectedCharacterstics.setReligion(pcqAnswers.getReligion());
            protectedCharacterstics.setOtherReligion(pcqAnswers.getReligionOther());
            protectedCharacterstics.setDisabilityConditions(pcqAnswers.getDisabilityConditions());
            protectedCharacterstics.setDisabilityImpact(pcqAnswers.getDisabilityImpact());
            protectedCharacterstics.setDisabilityVision(pcqAnswers.getDisabilityVision());
            protectedCharacterstics.setDisabilityHearing(pcqAnswers.getDisabilityHearing());
            protectedCharacterstics.setDisabilityMobility(pcqAnswers.getDisabilityMobility());
            protectedCharacterstics.setDisabilityDexterity(pcqAnswers.getDisabilityDexterity());
            protectedCharacterstics.setDisabilityLearning(pcqAnswers.getDisabilityLearning());
            protectedCharacterstics.setDisabilityMemory(pcqAnswers.getDisabilityMemory());
            protectedCharacterstics.setDisabilityMentalHealth(pcqAnswers.getDisabilityMentalHealth());
            protectedCharacterstics.setDisabilityStamina(pcqAnswers.getDisabilityStamina());
            protectedCharacterstics.setDisabilitySocial(pcqAnswers.getDisabilitySocial());
            protectedCharacterstics.setDisabilityOther(pcqAnswers.getDisabilityOther());
            protectedCharacterstics.setOtherDisabilityDetails(pcqAnswers.getDisabilityConditionOther());
            protectedCharacterstics.setDisabilityNone(pcqAnswers.getDisabilityNone());
            protectedCharacterstics.setPregnancy(pcqAnswers.getPregnancy());

        }

        return protectedCharacterstics;
    }

    private Timestamp getTimeFromString(String timeStampStr) {
        String pattern = "yyyy-MM-ddTHH:mm:ss.SSSZ";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(timeStampStr));

        return Timestamp.valueOf(localDateTime);
    }

    private Date getDateFromString(String dateStr) {
        String pattern = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDate = LocalDate.from(formatter.parse(dateStr));
        return Date.valueOf(localDate);
    }
}
