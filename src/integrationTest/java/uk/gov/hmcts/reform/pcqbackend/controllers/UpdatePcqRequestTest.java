package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.OutputCaptureRule;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswers;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
@SuppressWarnings({"PMD.TooManyMethods"})
public class UpdatePcqRequestTest extends PcqIntegrationTest {

    public static final String RESPONSE_KEY_4 = "response_body";
    public static final String HTTP_ACCEPTED = "202";
    public static final String HTTP_BAD_REQUEST = "400";
    public static final String RESPONSE_ACCEPTED_MSG = "Success";
    public static final String RESPONSE_INVALID_MSG = "Invalid Request";

    private static final String TEST_DUP_PCQ_ID = "UPDATE-DUP-INTEG-TEST";
    private static final String IO_EXCEPTION_MSG = "IOException while executing test";


    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();

    @Test
    public void updateDobProvidedSuccess() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/UpdateDobProvided.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateDobProvidedSuccessOptOutNull() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/UpdateDobProvidedOptOutNull.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void dobProvidedStateDate() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DobProvidedStale.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_ACCEPTED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_ACCEPTED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            assertNotEquals("DobProvided matching", protectedCharacteristicsOptional.get().getDobProvided(),
                         answerRequest.getPcqAnswers().getDobProvided());

            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateDobSuccess() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/UpdateDobValid.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void invalidDob() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/invalidDob.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));

            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, responseBody.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_BAD_REQUEST, responseBody.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_INVALID_MSG,
                         responseBody.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            assertNotEquals("Dob not matching", protectedCharacteristicsOptional
                .get().getDateOfBirth(), answerRequest.getPcqAnswers().getDob());

            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateMainLanguage() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/MainLanguage.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 3; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setLanguageMain(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateOtherLanguage() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/OtherLanguage.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void testInjectionOtherLanguage() {
        // Create an record first.
        createMultipleTestRecords();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/OtherLanguageInjection.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();

            protectedCharacteristicsOptional = protectedCharacteristicsRepository
                .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
            assertNotEquals("OtherLanguage not matching", protectedCharacteristicsOptional
                                .get().getOtherLanguage(),
                         answerRequest.getPcqAnswers().getLanguageOther());


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateEnglishLanguageLevel() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/LanguageLevel.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 5; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setEnglishLanguageLevel(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateSex() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Sex.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 3; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setSex(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateGender() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Gender.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 3; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setGenderDifferent(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateGenderDifferent() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/GenderOther.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateSexuality() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Sexuality.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 5; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setSexuality(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateOtherSexuality() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/SexualityOther.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void testInjectionOtherSexuality() {
        // Create an record first.
        createMultipleTestRecords();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/SexualityOtherInjection.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();

            protectedCharacteristicsOptional = protectedCharacteristicsRepository
                .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
            assertNotEquals("OtherSexuality not matching", protectedCharacteristicsOptional
                                .get().getOtherSexuality(),
                            answerRequest.getPcqAnswers().getSexualityOther());


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateMarried() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Married.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 3; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setMarriage(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateEthnicity() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Ethnicity.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 19; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setEthnicity(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateEthnicityOther() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/EthnicityOther.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void testInjectionOtherEthnicity() {
        // Create an record first.
        createMultipleTestRecords();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/EthnicityOtherInjection.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();

            protectedCharacteristicsOptional = protectedCharacteristicsRepository
                .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
            assertNotEquals("OtherEtnicity not matching", protectedCharacteristicsOptional
                                .get().getOtherEthnicity(),
                            answerRequest.getPcqAnswers().getEthnicityOther());


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateReligion() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Religion.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 9; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setReligion(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateReligionOther() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/ReligionOther.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void testInjectionOtherReligion() {
        // Create an record first.
        createMultipleTestRecords();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/ReligionOtherInjection.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();

            protectedCharacteristicsOptional = protectedCharacteristicsRepository
                .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
            assertNotEquals("OtherReligion not matching", protectedCharacteristicsOptional
                                .get().getOtherReligion(),
                            answerRequest.getPcqAnswers().getReligionOther());


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateDisabilityConditions() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DisabilityConditions.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 3; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setDisabilityConditions(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateDisabilityImpact() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DisabilityImpact.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 4; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setDisabilityImpact(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateDisabilityTypes() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DisabilityTypes.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updateOtherDisabilityDetails() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DisabilityOtherDetails.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void testInjectionOtherDisability() {
        // Create an record first.
        createMultipleTestRecords();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DisabilityOtherInjection.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            assertLogsForKeywords();

            protectedCharacteristicsOptional = protectedCharacteristicsRepository
                .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
            assertNotEquals("OtherDisabilityDetails not matching", protectedCharacteristicsOptional
                                .get().getOtherDisabilityDetails(),
                            answerRequest.getPcqAnswers().getDisabilityConditionOther());


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void updatePregnancy() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Pregnancy.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords();

            for (int i = 0; i < 3; i++) {
                PcqAnswers answers = answerRequest.getPcqAnswers();
                answers.setPregnancy(i);
                answerRequest.setPcqAnswers(answers);
                answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

                runAnswerUpdates(answerRequest);
                assertLogsForKeywords();
            }


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void invalidPregnancy() {
        // Create an record first.
        createTestRecord();

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidPregnancy.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));

            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, responseBody.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_BAD_REQUEST, responseBody.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_INVALID_MSG,
                         responseBody.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
            assertNotEquals("Pregnancy not matching", protectedCharacteristicsOptional
                .get().getPregnancy(), answerRequest.getPcqAnswers().getPregnancy());

            assertLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void testSqlInjection() {
        // Create an record first.
        createTestRecord();

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID
                + "'; DROP TABLE protected_characteristics;",getEncryptionKey());

        assertTrue(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_PCQ_ID,getEncryptionKey());
        assertNotNull("Answer record not found", protectedCharacteristicsOptional.get());
        assertEquals("PCQ Id not matching", protectedCharacteristicsOptional.get().getPcqId(), TEST_PCQ_ID);

        assertLogsForKeywords();


    }


    private void createTestRecord() {
        PcqAnswerRequest testRequest = createAnswerRequestForTest(TEST_PCQ_ID);
        pcqBackEndClient.createPcqAnswer(testRequest);
    }

    private void createMultipleTestRecords() {
        PcqAnswerRequest testRequest = createAnswerRequestForTest(TEST_PCQ_ID);
        pcqBackEndClient.createPcqAnswer(testRequest);

        testRequest = createAnswerRequestForTest(TEST_DUP_PCQ_ID);
        pcqBackEndClient.createPcqAnswer(testRequest);
    }

    public PcqAnswerRequest createAnswerRequestForTest(String pcqId) {
        PcqAnswerRequest answerRequest = new PcqAnswerRequest();
        answerRequest.setPcqId(pcqId);
        answerRequest.setCaseId("CCD-Case-2");
        answerRequest.setPartyId("23");
        answerRequest.setChannel(1);
        answerRequest.setServiceId("PROBATE");
        answerRequest.setCompletedDate("2020-03-05T09:13:45.000Z");
        answerRequest.setActor("RESPONDENT");
        answerRequest.setVersionNo(1);
        PcqAnswers answers = new PcqAnswers();
        answers.setDobProvided(1);

        answerRequest.setPcqAnswers(answers);

        return answerRequest;
    }

    private void assertLogsForKeywords() {
        assertTrue(capture.getAll().contains("Co-Relation Id : " + CO_RELATION_ID_FOR_TEST),
                   "Co-Relation Id was not logged in log files.");
    }

}
