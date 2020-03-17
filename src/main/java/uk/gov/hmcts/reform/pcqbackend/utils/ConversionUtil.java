package uk.gov.hmcts.reform.pcqbackend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswers;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class ConversionUtil {

    private static final String COMPLETED_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final String DOB_FORMAT = "yyyy-MM-dd";

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
        answerResponse.setDobProvided(protectedCharacteristics.getDobProvided());
        if (protectedCharacteristics.getDateOfBirth() != null) {
            answerResponse.setDob(convertDateToString(protectedCharacteristics.getDateOfBirth()));
        }
        answerResponse.setLanguageMain(protectedCharacteristics.getMainLanguage());
        answerResponse.setLanguageOther(protectedCharacteristics.getOtherLanguage());
        answerResponse.setEnglishLanguageLevel(protectedCharacteristics.getEnglishLanguageLevel());
        answerResponse.setSex(protectedCharacteristics.getSex());
        answerResponse.setGenderDifferent(protectedCharacteristics.getGenderDifferent());
        answerResponse.setGenderOther(protectedCharacteristics.getOtherGender());
        answerResponse.setSexuality(protectedCharacteristics.getSexuality());
        answerResponse.setSexualityOther(protectedCharacteristics.getOtherSexuality());
        answerResponse.setMarriage(protectedCharacteristics.getMarriage());
        answerResponse.setEthnicity(protectedCharacteristics.getEthnicity());
        answerResponse.setEthnicityOther(protectedCharacteristics.getOtherEthnicity());
        answerResponse.setReligion(protectedCharacteristics.getReligion());
        answerResponse.setReligionOther(protectedCharacteristics.getOtherReligion());
        answerResponse.setDisabilityConditions(protectedCharacteristics.getDisabilityConditions());
        answerResponse.setDisabilityImpact(protectedCharacteristics.getDisabilityImpact());
        answerResponse.setDisabilityVision(protectedCharacteristics.getDisabilityVision());
        answerResponse.setDisabilityHearing(protectedCharacteristics.getDisabilityHearing());
        answerResponse.setDisabilityMobility(protectedCharacteristics.getDisabilityMobility());
        answerResponse.setDisabilityDexterity(protectedCharacteristics.getDisabilityDexterity());
        answerResponse.setDisabilityLearning(protectedCharacteristics.getDisabilityLearning());
        answerResponse.setDisabilityMemory(protectedCharacteristics.getDisabilityMemory());
        answerResponse.setDisabilityMentalHealth(protectedCharacteristics.getDisabilityMentalHealth());
        answerResponse.setDisabilityStamina(protectedCharacteristics.getDisabilityStamina());
        answerResponse.setDisabilitySocial(protectedCharacteristics.getDisabilitySocial());
        answerResponse.setDisabilityOther(protectedCharacteristics.getDisabilityOther());
        answerResponse.setDisabilityConditionOther(protectedCharacteristics.getOtherDisabilityDetails());
        answerResponse.setDisabilityNone(protectedCharacteristics.getDisabilityNone());
        answerResponse.setPregnancy(protectedCharacteristics.getPregnancy());


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

    public static ResponseEntity<Object> generateResponseEntity(int pcqId, HttpStatus code, String message) {

        Map<String, Object> responseMap = new ConcurrentHashMap<>();
        responseMap.put("pcqId", pcqId);
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
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(timeStampStr));

        return Timestamp.valueOf(localDateTime);
    }

    public static Date getDateFromString(String dateStr) {
        String pattern = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDate = LocalDate.from(formatter.parse(dateStr));
        return Date.valueOf(localDate);
    }

}
