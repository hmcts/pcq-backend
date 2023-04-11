package uk.gov.hmcts.reform.pcqbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidRequestException;
import uk.gov.hmcts.reform.pcqbackend.exceptions.SchemaValidationException;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;
import uk.gov.hmcts.reform.pcqbackend.utils.ConversionUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Getter
@SuppressWarnings({"PMD.TooManyMethods"})
public class SubmitAnswersService extends BaseService {

    private static final String BAD_REQUEST_ERROR_MSG_KEY = "api-error-messages.bad_request";
    private static final int PAPER_CHANNEL = 2;
    private static final String OPTOUT_YES = "Y";
    private static final String INFO_LOG_MSG = "Co-Relation Id : {}, Channel : {}, Service : {} - submitAnswers API, ";

    ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    @Autowired
    public SubmitAnswersService(ProtectedCharacteristicsRepository protectedCharacteristicsRepository,
                                Environment environment) {
        super(environment);
        this.protectedCharacteristicsRepository = protectedCharacteristicsRepository;


    }

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveMethodLength",
        "PMD.UnusedLocalVariable","PMD.AvoidThrowingRawExceptionTypes"})
    @Transactional
    public ResponseEntity<Object> processPcqAnswers(@Nullable List<String> headers, PcqAnswerRequest answerRequest) {
        String pcqId = answerRequest.getPcqId();
        String coRelationId = "";
        try {

            //Step 1. Check the request contains the required header content.
            coRelationId = validateAndReturnCorrelationId(headers);
            log.info("Co-Relation Id : {} - submitAnswers API call invoked.", coRelationId);

            //Step 2. Perform the validations
            performValidations(answerRequest);

            //Step 3. Check whether record exists in database for the pcqId.
            Optional<ProtectedCharacteristics> protectedCharacteristics;
            if (pcqId != null &&  ! pcqId.equals("")) {
                protectedCharacteristics = protectedCharacteristicsRepository
                    .findByPcqId(answerRequest.getPcqId(), getEncryptionKey());
            } else {
                throw new Exception("PCQ Id is blank or null.");
            }

            ProtectedCharacteristics createCharacteristics = ConversionUtil.convertJsonToDomain(answerRequest);
            if (protectedCharacteristics.isEmpty()) {

                // Create the new PCQ Answers record.
                protectedCharacteristicsRepository.saveProtectedCharacteristicsWithEncryption(
                    createCharacteristics,getEncryptionKey());
                log.info(INFO_LOG_MSG
                             + "Protected Characteristic Questions Record submitted for creation.", coRelationId,
                         createCharacteristics.getChannel(), createCharacteristics.getServiceId());

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
                    createCharacteristics.getOptOut(),
                    createCharacteristics.getPcqId(),
                    createCharacteristics.getCompletedDate());

                if (resultCount == 0) {
                    log.error("Co-Relation Id : {} - submitAnswers API, Completed Date is in the past.", coRelationId);
                    return PcqUtils.generateResponseEntity(pcqId, HttpStatus.ACCEPTED,
                                                           environment.getProperty(
                                                                     "api-error-messages.accepted"));
                } else {
                    log.info(INFO_LOG_MSG
                                 + "Protected Characteristic Record submitted for Update.", coRelationId,
                             createCharacteristics.getChannel(), createCharacteristics.getServiceId());
                }
            }
        } catch (InvalidRequestException ive) {
            return handleInvalidRequestException(pcqId, ive);
        } catch (SchemaValidationException sve) {
            return handleSchemaValidationException(pcqId, coRelationId, sve);
        } catch (Exception ioe) {
            return handleInternalErrors(pcqId, coRelationId, ioe);
        }

        return PcqUtils.generateResponseEntity(pcqId, HttpStatus.CREATED,
                                      environment.getProperty("api-error-messages.created"));
    }

    public ProtectedCharacteristics getProtectedCharacteristicsById(String pcqId) {
        log.info("getAnswer API invoked");
        Optional<ProtectedCharacteristics> protectedCharacteristics = protectedCharacteristicsRepository
            .findByPcqId(pcqId, getEncryptionKey());

        return protectedCharacteristics.orElse(null);

    }

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveMethodLength",
        "PMD.UnusedLocalVariable"})
    @Transactional
    public ResponseEntity<Object> processOptOut(@Nullable List<String> headers, PcqAnswerRequest answerRequest) {
        String pcqId = answerRequest.getPcqId();
        String coRelationId = "";
        try {

            //Step 1. Check the request contains the required header content.
            coRelationId = validateAndReturnCorrelationId(headers);
            log.info("Co-Relation Id : {} - submitAnswers API call with OptOut invoked.", coRelationId);

            //Step 2. Perform the validations
            performValidations(answerRequest);

            //Step 3. Check whether record exists in database for the pcqId.
            Optional<ProtectedCharacteristics> protectedCharacteristics = protectedCharacteristicsRepository
                .findByPcqId(answerRequest.getPcqId(),getEncryptionKey());

            if (protectedCharacteristics.isEmpty()) {
                ProtectedCharacteristics createCharacteristics = ConversionUtil.convertJsonToDomain(answerRequest);

                // Create the new PCQ Answers record with optOut as false.
                protectedCharacteristicsRepository.saveProtectedCharacteristicsWithEncryption(
                    createCharacteristics,getEncryptionKey());
                log.info(INFO_LOG_MSG
                             + "Protected Char Questions Record submitted for creation with optOut true.",
                     coRelationId,
                         createCharacteristics.getChannel(), createCharacteristics.getServiceId());

            } else {
                // Update the PCQ Record
                java.sql.Timestamp completedDateNow = PcqUtils.getTimeFromString(answerRequest.getCompletedDate());
                int resultCount = protectedCharacteristicsRepository.updateCharacteristics(
                                                                    null,
                                                                    null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   completedDateNow,
                                                                   true,
                                                                   answerRequest.getPcqId(),
                                                                   completedDateNow
                );

                if (resultCount == 0) {
                    log.error("Co-Relation Id : {} - submitAnswers API, Completed Date is in the past.", coRelationId);
                    return PcqUtils.generateResponseEntity(pcqId, HttpStatus.ACCEPTED,
                                                           environment.getProperty(
                                                               "api-error-messages.accepted")
                    );

                } else {
                    log.info(INFO_LOG_MSG
                                 + "Protected Char Record submitted for Update with optOut true.", coRelationId,
                             answerRequest.getChannel(), answerRequest.getServiceId());
                    return PcqUtils.generateResponseEntity(pcqId, HttpStatus.OK,
                                                           environment.getProperty("api-error-messages.updated"));
                }
            }
        } catch (InvalidRequestException ive) {
            return handleInvalidRequestException(pcqId, ive);
        } catch (SchemaValidationException sve) {
            return handleSchemaValidationException(pcqId, coRelationId, sve);
        } catch (Exception ioe) {
            return handleInternalErrors(pcqId, coRelationId, ioe);
        }

        return PcqUtils.generateResponseEntity(pcqId, HttpStatus.CREATED,
                                               environment.getProperty("api-error-messages.created"));
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
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
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

    private void validateDcnNumber(String dcnNumber) throws InvalidRequestException {
        if (ObjectUtils.isEmpty(dcnNumber)) {
            throw new InvalidRequestException("DCN Number is missing", HttpStatus.BAD_REQUEST);
        }
        // Check whether a record already exists.
        List<ProtectedCharacteristics> protectedCharacteristics = protectedCharacteristicsRepository
            .findByDcnNumber(dcnNumber,getEncryptionKey());
        if (!protectedCharacteristics.isEmpty()) {
            throw new InvalidRequestException("Record already exists for Dcn Number- " + dcnNumber,
                                              HttpStatus.CONFLICT);
        }
    }

    private String validateAndReturnCorrelationId(List<String> headers) throws InvalidRequestException {
        String coRelationId = ConversionUtil.validateRequestHeader(headers);
        coRelationId = coRelationId.replaceAll("[\n|\r|\t]", "_");

        return coRelationId;
    }

    private void performValidations(PcqAnswerRequest answerRequest) throws IOException, SchemaValidationException,
        InvalidRequestException {

        //Step 1. Validate the request body against the JSON Schema.
        validateRequestAgainstSchema(answerRequest, environment
            .getProperty("api-schema-file.submitanswer-schema"));

        //Step 2. Validate the version number of the request matches the back-end version.
        validateVersionNumber(answerRequest.getVersionNo());

        //Step 3. For paper channel, validate the DCN number.
        if (PAPER_CHANNEL == answerRequest.getChannel()
            && (answerRequest.getOptOut() == null || !answerRequest.getOptOut().equalsIgnoreCase(OPTOUT_YES))) {
            validateDcnNumber(answerRequest.getDcnNumber());
        }
    }

    private ResponseEntity<Object> handleInvalidRequestException(String pcqId, InvalidRequestException ive) {
        log.error(ive.getMessage());
        return PcqUtils.generateResponseEntity(pcqId, ive.getErrorCode(),
                                                     environment.getProperty(BAD_REQUEST_ERROR_MSG_KEY));
    }

    private ResponseEntity<Object> handleSchemaValidationException(String pcqId, String coRelationId,
                                                                   SchemaValidationException sve) {
        log.error(
            "Co-Relation Id : {} - submitAnswers API failed schema validations. "
                + "Detailed error message as follows \n {}",
            coRelationId,
            sve.getFormattedError()
        );
        return PcqUtils.generateResponseEntity(pcqId, HttpStatus.BAD_REQUEST,
                                                     environment.getProperty(BAD_REQUEST_ERROR_MSG_KEY));
    }

    private ResponseEntity<Object> handleInternalErrors(String pcqId, String coRelationId, Exception except) {

        log.error("Co-Relation Id : {} - submitAnswers API call failed "
                      + "due to error - {}", coRelationId, except.getMessage(), except);
        return PcqUtils.generateResponseEntity(pcqId, HttpStatus.INTERNAL_SERVER_ERROR,
                                                     environment.getProperty("api-error-messages.internal_error"));
    }

}
