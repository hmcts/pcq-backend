package uk.gov.hmcts.reform.pcqbackend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswers;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Configuration
@Slf4j
public abstract class PcqIntegrationTest extends SpringBootIntegrationTest {

    public static final String CO_RELATION_ID_FOR_TEST = "INTEG-TEST-PCQ";
    public static final String RESPONSE_KEY_1 = "pcqId";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_CREATED = "201";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";

    public static final String NOT_FOUND_MSG = "Record Not found";
    public static final String PCQ_NOT_VALID_MSG = "PCQId not valid";
    public static final String TEST_PCQ_ID = "UPDATE-INTEG-TEST";
    public static final String STATUS_CODE_INVALID_MSG = "Response Status Code not valid";
    public static final String STATUS_INVALID_MSG = "Response Status not valid";
    public static final String PCQ_VALID_MSG = "PCQId valid";

    @Autowired
    protected ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    @Autowired
    protected Environment environment;

    protected PcqBackEndClient pcqBackEndClient;

    @Before
    public void setUpClient() {
        pcqBackEndClient = new PcqBackEndClient(port);
    }

    @After
    public void cleanupTestData() {
        protectedCharacteristicsRepository.deleteAll();
    }

    @SuppressWarnings("PMD.ConfusingTernary")
    protected void checkAssertionsOnResponse(ProtectedCharacteristics protectedCharacteristics,
                                          PcqAnswerRequest answerRequest) {
        assertEquals(protectedCharacteristics.getPcqId(), answerRequest.getPcqId(), "PCQId not matching");
        assertEquals(protectedCharacteristics.getCaseId(), answerRequest.getCaseId(), "CaseId not matching");
        assertEquals(protectedCharacteristics.getDcnNumber(), answerRequest.getDcnNumber(), "DCN Number not matching");
        assertEquals(protectedCharacteristics.getFormId(), answerRequest.getFormId(), "Form Id not matching");
        //assertEquals("PartyId not matching", answerRequest.getPartyId(),
        //             ConversionUtil.decrypt(protectedCharacteristics.getPartyId(), environment
        //                 .getProperty("security.db.backend-encryption-key")));
        assertEquals(answerRequest.getPartyId(), protectedCharacteristics.getPartyId(), "PartyId not matching");
        assertEquals(protectedCharacteristics.getChannel().intValue(),
                     answerRequest.getChannel(), "Channel not matching");
        assertEquals(protectedCharacteristics.getServiceId(),
                     answerRequest.getServiceId(), "ServiceId not matching");
        assertEquals(protectedCharacteristics.getActor(),
                     answerRequest.getActor(), "Actor not matching");
        assertEquals(protectedCharacteristics.getVersionNumber().intValue(),
                     answerRequest.getVersionNo(), "VersionNumber not matching");

        PcqAnswers pcqAnswers = answerRequest.getPcqAnswers();
        assertEquals(protectedCharacteristics.getDobProvided(), pcqAnswers.getDobProvided(),
                     "DobProvided not matching");
        if (protectedCharacteristics.getDateOfBirth() != null) {
            assertEquals(
                PcqUtils.convertDateToString(protectedCharacteristics.getDateOfBirth()),
                pcqAnswers.getDob(),
                "Dob not matching"
            );
        }
        assertEquals(protectedCharacteristics.getMainLanguage(),
                     pcqAnswers.getLanguageMain(), "LanguageMain not matching");
        assertEquals(protectedCharacteristics.getOtherLanguage(),
                     pcqAnswers.getLanguageOther(), "OtherLanguage not matching");
        assertEquals(protectedCharacteristics.getEnglishLanguageLevel(),
                     pcqAnswers.getEnglishLanguageLevel(), "EnglishLanguageLevel not matching");
        assertEquals(protectedCharacteristics.getSex(),
                     pcqAnswers.getSex(), "Sex not matching");
        assertEquals(protectedCharacteristics.getGenderDifferent(),
                      pcqAnswers.getGenderDifferent(), "Gender Different not matching");
        assertEquals(protectedCharacteristics.getOtherGender(),
                     pcqAnswers.getGenderOther(), "Other Gender not matching");
        assertEquals(protectedCharacteristics.getSexuality(),
                     pcqAnswers.getSexuality(), "Sexuality not matching");
        assertEquals(protectedCharacteristics.getOtherSexuality(),
                     pcqAnswers.getSexualityOther(), "Sexuality Other not matching");
        assertEquals(protectedCharacteristics.getMarriage(),
                     pcqAnswers.getMarriage(), "Marriage not matching");
        assertEquals(protectedCharacteristics.getEthnicity(),
                     pcqAnswers.getEthnicity(), "Ethnicity not matching");
        assertEquals(protectedCharacteristics.getOtherEthnicity(),
                     pcqAnswers.getEthnicityOther(), "Other Ethnicity not matching");
        assertEquals(protectedCharacteristics.getReligion(),
                     pcqAnswers.getReligion(), "Religion not matching");
        assertEquals(protectedCharacteristics.getOtherReligion(),
                     pcqAnswers.getReligionOther(), "Religion Other not matching");
        assertEquals(protectedCharacteristics.getDisabilityConditions(),
                     pcqAnswers.getDisabilityConditions(), "Disability Conditions not matching");
        assertEquals(protectedCharacteristics.getDisabilityImpact(),
                     pcqAnswers.getDisabilityImpact(), "Disability Impact not matching");
        assertEquals(protectedCharacteristics.getDisabilityVision(),
                     pcqAnswers.getDisabilityVision(), "Disability Vision not matching");
        assertEquals(protectedCharacteristics.getDisabilityHearing(),
                     pcqAnswers.getDisabilityHearing(), "Disability Hearing not matching");
        assertEquals(protectedCharacteristics.getDisabilityMobility(),
                     pcqAnswers.getDisabilityMobility(), "Disability Mobility not matching");
        assertEquals(protectedCharacteristics.getDisabilityDexterity(),
                     pcqAnswers.getDisabilityDexterity(), "Disability Dexterity not matching");
        assertEquals(protectedCharacteristics.getDisabilityLearning(),
                     pcqAnswers.getDisabilityLearning(), "Disability Learning not matching");
        assertEquals(protectedCharacteristics.getDisabilityMemory(),
                     pcqAnswers.getDisabilityMemory(), "Disability Memory not matching");
        assertEquals(protectedCharacteristics.getDisabilityMentalHealth(),
                     pcqAnswers.getDisabilityMentalHealth(), "Disability Mental Health not matching");
        assertEquals(protectedCharacteristics.getDisabilityStamina(),
                     pcqAnswers.getDisabilityStamina(), "Disability Stamina not matching");
        assertEquals(protectedCharacteristics.getDisabilitySocial(),
                     pcqAnswers.getDisabilitySocial(), "Disability Social not matching");
        assertEquals(protectedCharacteristics.getDisabilityOther(),
                     pcqAnswers.getDisabilityOther(), "Disability Other not matching");
        assertEquals(protectedCharacteristics.getOtherDisabilityDetails(),
                     pcqAnswers.getDisabilityConditionOther(), "Disability Other Details not matching");
        assertEquals(protectedCharacteristics.getDisabilityNone(),
                     pcqAnswers.getDisabilityNone(), "Disability None not matching");
        assertEquals(protectedCharacteristics.getPregnancy(),
                     pcqAnswers.getPregnancy(), "Pregnancy not matching");
    }

    protected void runAnswerUpdates(PcqAnswerRequest answerRequest) {
        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
    }

    protected String getEncryptionKey() {
        return environment.getProperty("security.db.backend-encryption-key");
    }

    protected String updateCompletedDate(String completedDateStr) {
        Timestamp completedTime = PcqUtils.getTimeFromString(completedDateStr);
        Date currentDate = new Date();
        completedTime.setTime(currentDate.getTime());
        return PcqUtils.convertTimeStampToString(completedTime);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonMapFromString(String jsonString) throws IOException {
        return new ObjectMapper().readValue(jsonString, Map.class);
    }

}
