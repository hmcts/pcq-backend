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
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswers;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.client.PcqBackEndServiceClient;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(responseMap.get("pcqId"), answerRequest.getPcqId(), "PCQId not matching");
        assertEquals(responseMap.get("dcnNumber"), answerRequest.getDcnNumber(), "DCN Number not matching");
        assertEquals(responseMap.get("formId"), answerRequest.getFormId(), "Form Id not matching");
        assertEquals(responseMap.get("ccdCaseId"), answerRequest.getCaseId(), "CaseId not matching");
        assertEquals(responseMap.get("partyId"), answerRequest.getPartyId(), "PartyId not matching");
        assertEquals(((Integer)responseMap.get("channel")).intValue(),
                     answerRequest.getChannel(), "Channel not matching");
        assertEquals(responseMap.get("serviceId"), answerRequest.getServiceId(), "ServiceId not matching");
        assertEquals(responseMap.get("actor"), answerRequest.getActor(), "Actor not matching");
        assertEquals(((Integer)responseMap.get("versionNo")).intValue(),
                     answerRequest.getVersionNo(), "VersionNumber not matching");

        LinkedHashMap<String, Object> answers = (LinkedHashMap<String, Object>) responseMap.get("pcqAnswers");

        PcqAnswers pcqAnswers = answerRequest.getPcqAnswers();
        assertEquals((Integer)answers.get("dob_provided"), pcqAnswers.getDobProvided(), "DobProvided not matching");
        if (answers.get("dob") != null) {
            assertEquals(answers.get("dob"), pcqAnswers.getDob(), "Dob not matching");
        }
        assertEquals(
            (Integer) answers.get("language_main"),
            pcqAnswers.getLanguageMain(),
            "LanguageMain not matching"
        );
        assertEquals(answers.get("language_other"),
                     pcqAnswers.getLanguageOther(), "OtherLanguage not matching");
        assertEquals((Integer) answers.get("english_language_level"),
                     pcqAnswers.getEnglishLanguageLevel(), "EnglishLanguageLevel not matching");
        assertEquals((Integer)answers.get("sex"), pcqAnswers.getSex(), "Sex not matching");
        assertEquals((Integer)answers.get("gender_different"),
                     pcqAnswers.getGenderDifferent(), "Gender Different not matching");
        assertEquals(answers.get("gender_other"),
                     pcqAnswers.getGenderOther(), "Other Gender not matching");
        assertEquals((Integer)answers.get("sexuality"),
                     pcqAnswers.getSexuality(), "Sexuality not matching");
        assertEquals(answers.get("sexuality_other"),
                     pcqAnswers.getSexualityOther(), "Sexuality Other not matching");
        assertEquals((Integer)answers.get("marriage"),
                     pcqAnswers.getMarriage(), "Marriage not matching");
        assertEquals((Integer)answers.get("ethnicity"),
                     pcqAnswers.getEthnicity(), "Ethnicity not matching");
        assertEquals(answers.get("ethnicity_other"),
                     pcqAnswers.getEthnicityOther(), "Other Ethnicity not matching");
        assertEquals((Integer)answers.get("religion"),
                     pcqAnswers.getReligion(), "Religion not matching");
        assertEquals(answers.get("religion_other"),
                     pcqAnswers.getReligionOther(), "Religion Other not matching");
        assertEquals((Integer)answers.get("disability_conditions"),
                     pcqAnswers.getDisabilityConditions(), "Disability Conditions not matching");
        assertEquals((Integer)answers.get("disability_impact"),
                     pcqAnswers.getDisabilityImpact(), "Disability Impact not matching");
        assertEquals((Integer)answers.get("disability_vision"),
                     pcqAnswers.getDisabilityVision(), "Disability Vision not matching");
        assertEquals((Integer)answers.get("disability_hearing"),
                     pcqAnswers.getDisabilityHearing(), "Disability Hearing not matching");
        assertEquals((Integer)answers.get("disability_mobility"),
                     pcqAnswers.getDisabilityMobility(), "Disability Mobility not matching");
        assertEquals((Integer)answers.get("disability_dexterity"),
                     pcqAnswers.getDisabilityDexterity(), "Disability Dexterity not matching");
        assertEquals((Integer)answers.get("disability_learning"),
                     pcqAnswers.getDisabilityLearning(), "Disability Learning not matching");
        assertEquals((Integer)answers.get("disability_memory"),
                     pcqAnswers.getDisabilityMemory(), "Disability Memory not matching");
        assertEquals((Integer)answers.get("disability_mental_health"),
                     pcqAnswers.getDisabilityMentalHealth(), "Disability Mental Health not matching");
        assertEquals((Integer)answers.get("disability_stamina"),
                     pcqAnswers.getDisabilityStamina(), "Disability Stamina not matching");
        assertEquals((Integer)answers.get("disability_social"),
                     pcqAnswers.getDisabilitySocial(), "Disability Social not matching");
        assertEquals((Integer)answers.get("disability_other"),
                     pcqAnswers.getDisabilityOther(), "Disability Other not matching");
        assertEquals(
            answers.get("disability_other_details"),
            pcqAnswers.getDisabilityConditionOther(),
            "Disability Other Details not matching"
        );
        assertEquals((Integer)answers.get("disability_none"),
                     pcqAnswers.getDisabilityNone(), "Disability None not matching");
        assertEquals((Integer)answers.get("pregnancy"),
                     pcqAnswers.getPregnancy(), "Pregnancy not matching");

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
        assertTrue((Boolean)answers.get("opt_out"), "Opt-out should be true");
    }

}
