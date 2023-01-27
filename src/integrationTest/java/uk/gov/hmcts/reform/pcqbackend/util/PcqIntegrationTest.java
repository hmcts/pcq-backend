package uk.gov.hmcts.reform.pcqbackend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
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
        assertEquals("PCQId not matching", protectedCharacteristics.getPcqId(), answerRequest.getPcqId());
        assertEquals("CaseId not matching", protectedCharacteristics.getCaseId(),
                     answerRequest.getCaseId());
        assertEquals("DCN Number not matching", protectedCharacteristics.getDcnNumber(),
                     answerRequest.getDcnNumber());
        assertEquals("Form Id not matching", protectedCharacteristics.getFormId(),
                     answerRequest.getFormId());
        //assertEquals("PartyId not matching", answerRequest.getPartyId(),
        //             ConversionUtil.decrypt(protectedCharacteristics.getPartyId(), environment
        //                 .getProperty("security.db.backend-encryption-key")));
        assertEquals("PartyId not matching", answerRequest.getPartyId(),
                     protectedCharacteristics.getPartyId());
        assertEquals("Channel not matching", protectedCharacteristics.getChannel().intValue(),
                     answerRequest.getChannel());
        assertEquals("ServiceId not matching", protectedCharacteristics.getServiceId(),
                     answerRequest.getServiceId());
        assertEquals("Actor not matching", protectedCharacteristics.getActor(),
                     answerRequest.getActor());
        assertEquals("VersionNumber not matching", protectedCharacteristics.getVersionNumber().intValue(),
                     answerRequest.getVersionNo());
        assertEquals("DobProvided not matching", protectedCharacteristics.getDobProvided(),
                     answerRequest.getPcqAnswers().getDobProvided());
        if (protectedCharacteristics.getDateOfBirth() != null) {
            assertEquals("Dob not matching", PcqUtils.convertDateToString(protectedCharacteristics
                                                                                    .getDateOfBirth()),
                         answerRequest.getPcqAnswers().getDob()
            );
        } else {
            assertEquals("Dob not matching", null, answerRequest.getPcqAnswers().getDob());
        }
        assertEquals("LanguageMain not matching", protectedCharacteristics.getMainLanguage(),
                     answerRequest.getPcqAnswers().getLanguageMain());
        assertEquals("OtherLanguage not matching", protectedCharacteristics.getOtherLanguage(),
                     answerRequest.getPcqAnswers().getLanguageOther());
        assertEquals("EnglishLanguageLevel not matching", protectedCharacteristics.getEnglishLanguageLevel(),
                     answerRequest.getPcqAnswers().getEnglishLanguageLevel());
        assertEquals("Sex not matching", protectedCharacteristics.getSex(),
                     answerRequest.getPcqAnswers().getSex());
        assertEquals("Gender Different not matching", protectedCharacteristics.getGenderDifferent(),
                      answerRequest.getPcqAnswers().getGenderDifferent());
        assertEquals("Other Gender not matching", protectedCharacteristics.getOtherGender(),
                     answerRequest.getPcqAnswers().getGenderOther());
        assertEquals("Sexuality not matching", protectedCharacteristics.getSexuality(),
                     answerRequest.getPcqAnswers().getSexuality());
        assertEquals("Sexuality Other not matching", protectedCharacteristics.getOtherSexuality(),
                     answerRequest.getPcqAnswers().getSexualityOther());
        assertEquals("Marriage not matching", protectedCharacteristics.getMarriage(),
                     answerRequest.getPcqAnswers().getMarriage());
        assertEquals("Ethnicity not matching", protectedCharacteristics.getEthnicity(),
                     answerRequest.getPcqAnswers().getEthnicity());
        assertEquals("Other Ethnicity not matching", protectedCharacteristics.getOtherEthnicity(),
                     answerRequest.getPcqAnswers().getEthnicityOther());
        assertEquals("Religion not matching", protectedCharacteristics.getReligion(),
                     answerRequest.getPcqAnswers().getReligion());
        assertEquals("Religion Other not matching", protectedCharacteristics.getOtherReligion(),
                     answerRequest.getPcqAnswers().getReligionOther());
        assertEquals("Disability Conditions not matching", protectedCharacteristics.getDisabilityConditions(),
                     answerRequest.getPcqAnswers().getDisabilityConditions());
        assertEquals("Disability Impact not matching", protectedCharacteristics.getDisabilityImpact(),
                     answerRequest.getPcqAnswers().getDisabilityImpact());
        assertEquals("Disability Vision not matching", protectedCharacteristics.getDisabilityVision(),
                     answerRequest.getPcqAnswers().getDisabilityVision());
        assertEquals("Disability Hearing not matching", protectedCharacteristics.getDisabilityHearing(),
                     answerRequest.getPcqAnswers().getDisabilityHearing());
        assertEquals("Disability Mobility not matching", protectedCharacteristics.getDisabilityMobility(),
                     answerRequest.getPcqAnswers().getDisabilityMobility());
        assertEquals("Disability Dexterity not matching", protectedCharacteristics.getDisabilityDexterity(),
                     answerRequest.getPcqAnswers().getDisabilityDexterity());
        assertEquals("Disability Learning not matching", protectedCharacteristics.getDisabilityLearning(),
                     answerRequest.getPcqAnswers().getDisabilityLearning());
        assertEquals("Disability Memory not matching", protectedCharacteristics.getDisabilityMemory(),
                     answerRequest.getPcqAnswers().getDisabilityMemory());
        assertEquals("Disability Mental Health not matching", protectedCharacteristics.getDisabilityMentalHealth(),
                     answerRequest.getPcqAnswers().getDisabilityMentalHealth());
        assertEquals("Disability Stamina not matching", protectedCharacteristics.getDisabilityStamina(),
                     answerRequest.getPcqAnswers().getDisabilityStamina());
        assertEquals("Disability Social not matching", protectedCharacteristics.getDisabilitySocial(),
                     answerRequest.getPcqAnswers().getDisabilitySocial());
        assertEquals("Disability Other not matching", protectedCharacteristics.getDisabilityOther(),
                     answerRequest.getPcqAnswers().getDisabilityOther());
        assertEquals("Disability Other Details not matching", protectedCharacteristics.getOtherDisabilityDetails(),
                     answerRequest.getPcqAnswers().getDisabilityConditionOther());
        assertEquals("Disability None not matching", protectedCharacteristics.getDisabilityNone(),
                     answerRequest.getPcqAnswers().getDisabilityNone());
        assertEquals("Pregnancy not matching", protectedCharacteristics.getPregnancy(),
                     answerRequest.getPcqAnswers().getPregnancy());
    }

    protected void runAnswerUpdates(PcqAnswerRequest answerRequest) {
        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
        assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
        assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                     response.get(RESPONSE_KEY_3));

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
    }

    protected String getEncryptionKey() {
        String encryptionKey = environment.getProperty("security.db.backend-encryption-key");
        log.info("EncryptionKey " + encryptionKey);
        return environment.getProperty("security.db.backend-encryption-key");
    }

    protected String updateCompletedDate(String completedDateStr) {
        Timestamp completedTime = PcqUtils.getTimeFromString(completedDateStr);
        Calendar calendar = Calendar.getInstance();
        completedTime.setTime(calendar.getTimeInMillis());
        return PcqUtils.convertTimeStampToString(completedTime);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonMapFromString(String jsonString) throws IOException {
        return new ObjectMapper().readValue(jsonString, Map.class);
    }

}
