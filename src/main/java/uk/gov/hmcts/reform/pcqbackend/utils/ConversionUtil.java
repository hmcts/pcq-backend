package uk.gov.hmcts.reform.pcqbackend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.HtmlUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidRequestException;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswers;
import uk.gov.hmcts.reform.pcqbackend.model.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqbackend.model.SubmitResponse;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class ConversionUtil {

    private static final String COMPLETED_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final String DOB_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private ConversionUtil() {

    }

    public static PcqAnswerResponse getPcqResponseFromDomain(ProtectedCharacteristics protectedCharacteristics) {
        PcqAnswerResponse answerResponse = new PcqAnswerResponse();

        answerResponse.setPcqId(protectedCharacteristics.getPcqId());
        answerResponse.setCaseId(protectedCharacteristics.getCaseId());
        answerResponse.setPartyId(protectedCharacteristics.getPartyId());
        answerResponse.setChannel(protectedCharacteristics.getChannel());
        if (protectedCharacteristics.getCompletedDate() != null) {
            answerResponse.setCompletedDate(convertTimeStampToString(protectedCharacteristics.getCompletedDate()));
        }
        answerResponse.setServiceId(protectedCharacteristics.getServiceId());
        answerResponse.setActor(protectedCharacteristics.getActor());
        answerResponse.setVersionNo(protectedCharacteristics.getVersionNumber());

        PcqAnswers pcqAnswers = new PcqAnswers();

        pcqAnswers.setDobProvided(protectedCharacteristics.getDobProvided());
        if (protectedCharacteristics.getDateOfBirth() != null) {
            pcqAnswers.setDob(convertDateToString(protectedCharacteristics.getDateOfBirth()));
        }
        pcqAnswers.setLanguageMain(protectedCharacteristics.getMainLanguage());
        pcqAnswers.setLanguageOther(protectedCharacteristics.getOtherLanguage());
        pcqAnswers.setEnglishLanguageLevel(protectedCharacteristics.getEnglishLanguageLevel());
        pcqAnswers.setSex(protectedCharacteristics.getSex());
        pcqAnswers.setGenderDifferent(protectedCharacteristics.getGenderDifferent());
        pcqAnswers.setGenderOther(protectedCharacteristics.getOtherGender());
        pcqAnswers.setSexuality(protectedCharacteristics.getSexuality());
        pcqAnswers.setSexualityOther(protectedCharacteristics.getOtherSexuality());
        pcqAnswers.setMarriage(protectedCharacteristics.getMarriage());
        pcqAnswers.setEthnicity(protectedCharacteristics.getEthnicity());
        pcqAnswers.setEthnicityOther(protectedCharacteristics.getOtherEthnicity());
        pcqAnswers.setReligion(protectedCharacteristics.getReligion());
        pcqAnswers.setReligionOther(protectedCharacteristics.getOtherReligion());
        pcqAnswers.setDisabilityConditions(protectedCharacteristics.getDisabilityConditions());
        pcqAnswers.setDisabilityImpact(protectedCharacteristics.getDisabilityImpact());
        pcqAnswers.setDisabilityVision(protectedCharacteristics.getDisabilityVision());
        pcqAnswers.setDisabilityHearing(protectedCharacteristics.getDisabilityHearing());
        pcqAnswers.setDisabilityMobility(protectedCharacteristics.getDisabilityMobility());
        pcqAnswers.setDisabilityDexterity(protectedCharacteristics.getDisabilityDexterity());
        pcqAnswers.setDisabilityLearning(protectedCharacteristics.getDisabilityLearning());
        pcqAnswers.setDisabilityMemory(protectedCharacteristics.getDisabilityMemory());
        pcqAnswers.setDisabilityMentalHealth(protectedCharacteristics.getDisabilityMentalHealth());
        pcqAnswers.setDisabilityStamina(protectedCharacteristics.getDisabilityStamina());
        pcqAnswers.setDisabilitySocial(protectedCharacteristics.getDisabilitySocial());
        pcqAnswers.setDisabilityOther(protectedCharacteristics.getDisabilityOther());
        pcqAnswers.setDisabilityConditionOther(protectedCharacteristics.getOtherDisabilityDetails());
        pcqAnswers.setDisabilityNone(protectedCharacteristics.getDisabilityNone());
        pcqAnswers.setPregnancy(protectedCharacteristics.getPregnancy());

        answerResponse.setPcqAnswers(pcqAnswers);


        return answerResponse;
    }

    public static String convertTimeStampToString(Timestamp timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(COMPLETED_DATE_FORMAT, Locale.UK);
        return dateFormat.format(timestamp);
    }

    public static String convertDateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DOB_FORMAT, Locale.UK);
        return dateFormat.format(date);
    }

    public static ResponseEntity<Object> generateResponseEntity(String pcqId, HttpStatus code, String message) {

        Map<String, Object> responseMap = new ConcurrentHashMap<>();
        responseMap.put("pcqId", HtmlUtils.htmlEscape(pcqId));
        responseMap.put("responseStatus", message);
        responseMap.put("responseStatusCode", String.valueOf(code.value()));

        return new ResponseEntity<>(responseMap, code);

    }

    public static ProtectedCharacteristics convertJsonToDomain(PcqAnswerRequest pcqAnswerRequest) {
        ProtectedCharacteristics protectedCharacteristics = new ProtectedCharacteristics();
        protectedCharacteristics.setPcqId(pcqAnswerRequest.getPcqId());
        protectedCharacteristics.setActor(pcqAnswerRequest.getActor());
        protectedCharacteristics.setCaseId(pcqAnswerRequest.getCaseId());
        protectedCharacteristics.setChannel(pcqAnswerRequest.getChannel());
        protectedCharacteristics.setCompletedDate(getTimeFromString(pcqAnswerRequest.getCompletedDate()));
        protectedCharacteristics.setPartyId(pcqAnswerRequest.getPartyId());
        protectedCharacteristics.setServiceId(pcqAnswerRequest.getServiceId());
        protectedCharacteristics.setVersionNumber(pcqAnswerRequest.getVersionNo());

        PcqAnswers pcqAnswers = pcqAnswerRequest.getPcqAnswers();
        if (pcqAnswers != null) {
            protectedCharacteristics.setDobProvided(pcqAnswers.getDobProvided());
            if (pcqAnswers.getDob() != null) {
                protectedCharacteristics.setDateOfBirth(getDateFromString(pcqAnswers.getDob()));
            }
            protectedCharacteristics.setMainLanguage(pcqAnswers.getLanguageMain());
            protectedCharacteristics.setOtherLanguage(pcqAnswers.getLanguageOther());
            protectedCharacteristics.setEnglishLanguageLevel(pcqAnswers.getEnglishLanguageLevel());
            protectedCharacteristics.setSex(pcqAnswers.getSex());
            protectedCharacteristics.setGenderDifferent(pcqAnswers.getGenderDifferent());
            protectedCharacteristics.setOtherGender(pcqAnswers.getGenderOther());
            protectedCharacteristics.setSexuality(pcqAnswers.getSexuality());
            protectedCharacteristics.setOtherSexuality(pcqAnswers.getSexualityOther());
            protectedCharacteristics.setMarriage(pcqAnswers.getMarriage());
            protectedCharacteristics.setEthnicity(pcqAnswers.getEthnicity());
            protectedCharacteristics.setOtherEthnicity(pcqAnswers.getEthnicityOther());
            protectedCharacteristics.setReligion(pcqAnswers.getReligion());
            protectedCharacteristics.setOtherReligion(pcqAnswers.getReligionOther());
            protectedCharacteristics.setDisabilityConditions(pcqAnswers.getDisabilityConditions());
            protectedCharacteristics.setDisabilityImpact(pcqAnswers.getDisabilityImpact());
            protectedCharacteristics.setDisabilityVision(pcqAnswers.getDisabilityVision());
            protectedCharacteristics.setDisabilityHearing(pcqAnswers.getDisabilityHearing());
            protectedCharacteristics.setDisabilityMobility(pcqAnswers.getDisabilityMobility());
            protectedCharacteristics.setDisabilityDexterity(pcqAnswers.getDisabilityDexterity());
            protectedCharacteristics.setDisabilityLearning(pcqAnswers.getDisabilityLearning());
            protectedCharacteristics.setDisabilityMemory(pcqAnswers.getDisabilityMemory());
            protectedCharacteristics.setDisabilityMentalHealth(pcqAnswers.getDisabilityMentalHealth());
            protectedCharacteristics.setDisabilityStamina(pcqAnswers.getDisabilityStamina());
            protectedCharacteristics.setDisabilitySocial(pcqAnswers.getDisabilitySocial());
            protectedCharacteristics.setDisabilityOther(pcqAnswers.getDisabilityOther());
            protectedCharacteristics.setOtherDisabilityDetails(pcqAnswers.getDisabilityConditionOther());
            protectedCharacteristics.setDisabilityNone(pcqAnswers.getDisabilityNone());
            protectedCharacteristics.setPregnancy(pcqAnswers.getPregnancy());

        }

        return protectedCharacteristics;
    }

    public static Timestamp getTimeFromString(String timeStampStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(COMPLETED_DATE_FORMAT);
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(timeStampStr));

        return Timestamp.valueOf(localDateTime);
    }

    public static Date getDateFromString(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DOB_FORMAT);
        LocalDate localDate = LocalDate.from(formatter.parse(dateStr));
        return Date.valueOf(localDate);
    }

    public static Timestamp getDateTimeInPast(long numberOfDays) {
        LocalDateTime currentDateTime = LocalDateTime.now(Clock.systemUTC());

        return Timestamp.valueOf(currentDateTime.minusDays(numberOfDays));
    }

    public static ResponseEntity<SubmitResponse> generateSubmitResponseEntity(String pcqId, HttpStatus code,
                                                                        String message) {

        SubmitResponse submitResponse = new SubmitResponse();
        submitResponse.setPcqId(pcqId);
        submitResponse.setResponseStatus(message);
        submitResponse.setResponseStatusCode(String.valueOf(code.value()));

        return new ResponseEntity<>(submitResponse, code);

    }

    public static ResponseEntity<PcqWithoutCaseResponse> generatePcqWithoutCaseResponse(List<ProtectedCharacteristics>
                                                                                            pcqIds, HttpStatus code,
                                                                                        String message) {
        PcqWithoutCaseResponse pcqWithoutCaseResponse = new PcqWithoutCaseResponse();
        if (pcqIds == null) {
            pcqWithoutCaseResponse.setResponseStatus(message);
            pcqWithoutCaseResponse.setResponseStatusCode(String.valueOf(code.value()));

            return new ResponseEntity<>(pcqWithoutCaseResponse, code);
        }

        List<String> pcqIdArray = new ArrayList<>(pcqIds.size());
        for (ProtectedCharacteristics protectedCharacteristics : pcqIds) {
            pcqIdArray.add(protectedCharacteristics.getPcqId());
        }
        pcqWithoutCaseResponse.setPcqId(pcqIdArray.toArray(new String[0]));
        pcqWithoutCaseResponse.setResponseStatus(message);
        pcqWithoutCaseResponse.setResponseStatusCode(String.valueOf(code.value()));

        return new ResponseEntity<>(pcqWithoutCaseResponse, code);
    }

    public static String validateRequestHeader(List<String> requestHeaders) throws InvalidRequestException {

        // Validate that the request contains the required Header values.
        if (requestHeaders == null || requestHeaders.isEmpty()) {
            throw new InvalidRequestException("Invalid Request. Expecting required header - Co-Relation Id -"
                                                  + " in the request.", HttpStatus.BAD_REQUEST);
        }

        return requestHeaders.get(0);

    }

    /*public static String encryptWithKey(String message, String encryptionKey) {
        try {

            return Base64.getEncoder().encodeToString(ByteArrayHandler.encrypt(
                message.getBytes(StandardCharsets.UTF_8),
                encryptionKey.toCharArray(),
                null,
                SymmetricKeyAlgorithmTags.AES_128,
                false
            ));

        } catch (Exception e) {
            throw new IllegalStateException("Error Encrypting : " + e.getMessage(), e);
        }
    }

    public static String decrypt(String pgpArmoredMsg, String symmetricKey) {
        try {
            return new String(ByteArrayHandler.decrypt(Base64.getDecoder().decode(pgpArmoredMsg),
                                                       symmetricKey.toCharArray()));
        } catch (Exception e) {
            throw new IllegalStateException("Error Decrypting : " + e.getMessage(), e);
        }
    }*/

}
