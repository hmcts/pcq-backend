package uk.gov.hmcts.reform.pcqbackend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.HtmlUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswers;
import uk.gov.hmcts.reform.pcq.commons.model.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcq.commons.model.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidRequestException;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils.convertDateToString;
import static uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils.convertTimeStampToString;
import static uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils.getDateFromString;
import static uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils.getTimeFromString;

@Slf4j
public final class ConversionUtil {

    private ConversionUtil() {

    }

    public static PcqAnswerResponse getPcqResponseFromDomain(ProtectedCharacteristics protectedCharacteristics) {
        PcqAnswerResponse answerResponse = new PcqAnswerResponse();

        answerResponse.setPcqId(protectedCharacteristics.getPcqId());
        answerResponse.setDcnNumber(protectedCharacteristics.getDcnNumber());
        answerResponse.setFormId(protectedCharacteristics.getFormId());
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
        pcqAnswers.setOptOut(protectedCharacteristics.getOptOut());
        answerResponse.setPcqAnswers(pcqAnswers);


        return answerResponse;
    }

    public static ProtectedCharacteristics convertJsonToDomain(PcqAnswerRequest pcqAnswerRequest) {
        ProtectedCharacteristics protectedCharacteristics = new ProtectedCharacteristics();
        protectedCharacteristics.setPcqId(pcqAnswerRequest.getPcqId());
        if (pcqAnswerRequest.getDcnNumber() == null) {
            protectedCharacteristics.setDcnNumber(pcqAnswerRequest.getDcnNumber());
        } else {
            protectedCharacteristics.setDcnNumber(HtmlUtils.htmlEscape(pcqAnswerRequest.getDcnNumber()));
        }
        if (pcqAnswerRequest.getFormId() == null) {
            protectedCharacteristics.setFormId(pcqAnswerRequest.getFormId());
        } else {
            protectedCharacteristics.setFormId(HtmlUtils.htmlEscape(pcqAnswerRequest.getFormId()));
        }
        protectedCharacteristics.setActor(pcqAnswerRequest.getActor());
        protectedCharacteristics.setCaseId(pcqAnswerRequest.getCaseId());
        protectedCharacteristics.setChannel(pcqAnswerRequest.getChannel());
        protectedCharacteristics.setCompletedDate(getTimeFromString(pcqAnswerRequest.getCompletedDate()));
        protectedCharacteristics.setPartyId(pcqAnswerRequest.getPartyId());
        protectedCharacteristics.setServiceId(pcqAnswerRequest.getServiceId());
        protectedCharacteristics.setVersionNumber(pcqAnswerRequest.getVersionNo());
        protectedCharacteristics.setOptOut("Y".equals(pcqAnswerRequest.getOptOut()) ? true : false);

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

    public static ResponseEntity<PcqRecordWithoutCaseResponse> generatePcqRecordWithoutCaseResponse(
        List<ProtectedCharacteristics> pcqIds, HttpStatus code, String message) {
        PcqRecordWithoutCaseResponse pcqRecordWithoutCaseResponse = new PcqRecordWithoutCaseResponse();
        if (pcqIds == null) {
            pcqRecordWithoutCaseResponse.setResponseStatus(message);
            pcqRecordWithoutCaseResponse.setResponseStatusCode(String.valueOf(code.value()));

            return new ResponseEntity<>(pcqRecordWithoutCaseResponse, code);
        }

        List<PcqAnswerResponse> pcqRecordArray = new ArrayList<>(pcqIds.size());
        for (ProtectedCharacteristics protectedCharacteristics : pcqIds) {
            pcqRecordArray.add(createPcqRecordForConsolidationService(protectedCharacteristics));
        }
        pcqRecordWithoutCaseResponse.setPcqRecord(pcqRecordArray.toArray(new PcqAnswerResponse[0]));
        pcqRecordWithoutCaseResponse.setResponseStatus(message);
        pcqRecordWithoutCaseResponse.setResponseStatusCode(String.valueOf(code.value()));

        return new ResponseEntity<>(pcqRecordWithoutCaseResponse, code);
    }

    public static String validateRequestHeader(List<String> requestHeaders) throws InvalidRequestException {

        // Validate that the request contains the required Header values.
        if (requestHeaders == null || requestHeaders.isEmpty()) {
            throw new InvalidRequestException("Invalid Request. Expecting required header - Co-Relation Id -"
                                                  + " in the request.", HttpStatus.BAD_REQUEST);
        }

        return requestHeaders.get(0);

    }

    private static PcqAnswerResponse createPcqRecordForConsolidationService(ProtectedCharacteristics pcqDbRecord) {
        PcqAnswerResponse answerResponse = new PcqAnswerResponse();
        answerResponse.setPcqId(pcqDbRecord.getPcqId());
        answerResponse.setServiceId(pcqDbRecord.getServiceId());
        answerResponse.setActor(pcqDbRecord.getActor());
        answerResponse.setDcnNumber(pcqDbRecord.getDcnNumber());
        return answerResponse;
    }

}
