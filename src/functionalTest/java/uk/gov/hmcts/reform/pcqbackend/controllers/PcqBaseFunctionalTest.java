package uk.gov.hmcts.reform.pcqbackend.controllers;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.client.PcqBackEndServiceClient;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ComponentScan("uk.gov.hmcts.reform.pcqbackend")
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
@SuppressWarnings({"PMD.AbstractClassWithoutAnyMethod", "PMD.AbstractClassWithoutAbstractMethod"})
public abstract class PcqBaseFunctionalTest {

    @Value("${targetInstance}")
    protected String pcqBackEndApiUrl;

    @Value("${jwt_test_secret}")
    protected String jwtSecretKey;

    @Value("${api-config-params.number_of_days_limit}")
    protected String daysLimit;

    protected PcqBackEndServiceClient pcqBackEndServiceClient;

    protected RequestSpecification bearerToken;

    protected List<PcqAnswerRequest> clearTestPcqAnswers = new ArrayList<>();

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.defaultParser = Parser.JSON;

        /* SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);*/

        pcqBackEndServiceClient = new PcqBackEndServiceClient(pcqBackEndApiUrl, jwtSecretKey);
    }

    @After
    public void afterTest() {
        for (PcqAnswerRequest pcqAnswerRequest : clearTestPcqAnswers) {
            /* Changing Status code as per code at the moment ,
            this need to change when we have correct method to delete answer Record*/
            String pcqId = pcqAnswerRequest.getPcqId();
            pcqBackEndServiceClient.deleteAnswersRecord(pcqId, HttpStatus.OK);
        }
    }

    @SuppressWarnings({"unchecked", "PMD.ConfusingTernary"})
    protected void checkAssertionsOnResponse(Map<String, Object> responseMap,
                                             PcqAnswerRequest answerRequest) {
        assertEquals("PCQId not matching", responseMap.get("pcqId"), answerRequest.getPcqId());
        assertEquals("DCN Number not matching", responseMap.get("dcnNumber"), answerRequest.getDcnNumber());
        assertEquals("Form Id not matching", responseMap.get("formId"), answerRequest.getFormId());
        assertEquals("CaseId not matching", responseMap.get("ccdCaseId"),
                     answerRequest.getCaseId());
        assertEquals("PartyId not matching", responseMap.get("partyId"),
                     answerRequest.getPartyId());
        assertEquals("Channel not matching", ((Integer)responseMap.get("channel")).intValue(),
                     answerRequest.getChannel());
        assertEquals("ServiceId not matching", responseMap.get("serviceId"),
                     answerRequest.getServiceId());
        assertEquals("Actor not matching", responseMap.get("actor"),
                     answerRequest.getActor());
        assertEquals("VersionNumber not matching", ((Integer)responseMap.get("versionNo")).intValue(),
                     answerRequest.getVersionNo());

        LinkedHashMap<String, Object> answers = (LinkedHashMap<String, Object>) responseMap.get("pcqAnswers");

        assertEquals("DobProvided not matching", (Integer)answers.get("dob_provided"),
                     answerRequest.getPcqAnswers().getDobProvided());
        if (answers.get("dob") != null) {
            assertEquals("Dob not matching", answers.get("dob"), answerRequest.getPcqAnswers().getDob());
        } else {
            assertEquals("Dob not matching", null, answerRequest.getPcqAnswers().getDob());
        }
        assertEquals("LanguageMain not matching", (Integer) answers.get("language_main"),
                     answerRequest.getPcqAnswers().getLanguageMain());
        assertEquals("OtherLanguage not matching", answers.get("language_other"),
                     answerRequest.getPcqAnswers().getLanguageOther());
        assertEquals("EnglishLanguageLevel not matching", (Integer) answers.get("english_language_level"),
                     answerRequest.getPcqAnswers().getEnglishLanguageLevel());
        assertEquals("Sex not matching", (Integer)answers.get("sex"),
                     answerRequest.getPcqAnswers().getSex());
        assertEquals("Gender Different not matching", (Integer)answers.get("gender_different"),
                     answerRequest.getPcqAnswers().getGenderDifferent());
        assertEquals("Other Gender not matching", answers.get("gender_other"),
                     answerRequest.getPcqAnswers().getGenderOther());
        assertEquals("Sexuality not matching", (Integer)answers.get("sexuality"),
                     answerRequest.getPcqAnswers().getSexuality());
        assertEquals("Sexuality Other not matching", answers.get("sexuality_other"),
                     answerRequest.getPcqAnswers().getSexualityOther());
        assertEquals("Marriage not matching", (Integer)answers.get("marriage"),
                     answerRequest.getPcqAnswers().getMarriage());
        assertEquals("Ethnicity not matching", (Integer)answers.get("ethnicity"),
                     answerRequest.getPcqAnswers().getEthnicity());
        assertEquals("Other Ethnicity not matching", answers.get("ethnicity_other"),
                     answerRequest.getPcqAnswers().getEthnicityOther());
        assertEquals("Religion not matching", (Integer)answers.get("religion"),
                     answerRequest.getPcqAnswers().getReligion());
        assertEquals("Religion Other not matching", answers.get("religion_other"),
                     answerRequest.getPcqAnswers().getReligionOther());
        assertEquals("Disability Conditions not matching", (Integer)answers.get("disability_conditions"),
                     answerRequest.getPcqAnswers().getDisabilityConditions());
        assertEquals("Disability Impact not matching", (Integer)answers.get("disability_impact"),
                     answerRequest.getPcqAnswers().getDisabilityImpact());
        assertEquals("Disability Vision not matching", (Integer)answers.get("disability_vision"),
                     answerRequest.getPcqAnswers().getDisabilityVision());
        assertEquals("Disability Hearing not matching", (Integer)answers.get("disability_hearing"),
                     answerRequest.getPcqAnswers().getDisabilityHearing());
        assertEquals("Disability Mobility not matching", (Integer)answers.get("disability_mobility"),
                     answerRequest.getPcqAnswers().getDisabilityMobility());
        assertEquals("Disability Dexterity not matching", (Integer)answers.get("disability_dexterity"),
                     answerRequest.getPcqAnswers().getDisabilityDexterity());
        assertEquals("Disability Learning not matching", (Integer)answers.get("disability_learning"),
                     answerRequest.getPcqAnswers().getDisabilityLearning());
        assertEquals("Disability Memory not matching", (Integer)answers.get("disability_memory"),
                     answerRequest.getPcqAnswers().getDisabilityMemory());
        assertEquals("Disability Mental Health not matching", (Integer)answers.get("disability_mental_health"),
                     answerRequest.getPcqAnswers().getDisabilityMentalHealth());
        assertEquals("Disability Stamina not matching", (Integer)answers.get("disability_stamina"),
                     answerRequest.getPcqAnswers().getDisabilityStamina());
        assertEquals("Disability Social not matching", (Integer)answers.get("disability_social"),
                     answerRequest.getPcqAnswers().getDisabilitySocial());
        assertEquals("Disability Other not matching", (Integer)answers.get("disability_other"),
                     answerRequest.getPcqAnswers().getDisabilityOther());
        assertEquals("Disability Other Details not matching", answers.get("disability_other_details"),
                     answerRequest.getPcqAnswers().getDisabilityConditionOther());
        assertEquals("Disability None not matching", (Integer)answers.get("disability_none"),
                     answerRequest.getPcqAnswers().getDisabilityNone());
        assertEquals("Pregnancy not matching", (Integer)answers.get("pregnancy"),
                     answerRequest.getPcqAnswers().getPregnancy());

    }

    protected String generateUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    protected String updateCompletedDate(String completedDateStr) {
        Timestamp completedTime = PcqUtils.getTimeFromString(completedDateStr);
        Date currentDate = new Date();
        completedTime.setTime(currentDate.getTime());
        return PcqUtils.convertTimeStampToString(completedTime);
    }

    @SuppressWarnings({"unchecked"})
    protected void checkOptOutOnResponse(Map<String, Object> responseMap) {
        LinkedHashMap<String, Object> answers = (LinkedHashMap<String, Object>) responseMap.get("pcqAnswers");
        assertTrue("Opt-out should be true", (Boolean)answers.get("opt_out"));
    }

}
