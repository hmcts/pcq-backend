package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswers;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass"})
@ExtendWith(OutputCaptureExtension.class)
class UpdatePcqRequestIntegrationTest extends PcqIntegrationTest {

    private static final String RESPONSE_KEY_4 = "response_body";
    private static final String HTTP_ACCEPTED = "202";
    private static final String HTTP_BAD_REQUEST = "400";
    private static final String RESPONSE_ACCEPTED_MSG = "Success";
    private static final String RESPONSE_INVALID_MSG = "Invalid Request";

    private static final String TEST_DUP_PCQ_ID = "UPDATE-DUP-INTEG-TEST";

    @Test
    void updateDobProvidedSuccess(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/UpdateDobProvided.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void updateDobProvidedSuccessOptOutNull(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/UpdateDobProvidedOptOutNull.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void dobProvidedStateDate(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DobProvidedStale.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_ACCEPTED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_ACCEPTED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        assertNotEquals(
            protectedCharacteristicsOptional.get().getDobProvided(),
            answerRequest.getPcqAnswers().getDobProvided(),
            "Dob Provided matching"
        );

        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void updateDobSuccess(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/UpdateDobValid.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void invalidDob(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/invalidDob.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));

        assertEquals(TEST_PCQ_ID, responseBody.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_BAD_REQUEST, responseBody.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_INVALID_MSG, responseBody.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        assertNotEquals(
            protectedCharacteristicsOptional.get().getDateOfBirth(),
            answerRequest.getPcqAnswers().getDob(),
            "Dob not matching"
        );

        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void updateMainLanguage(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/MainLanguage.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 3; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setLanguageMain(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void updateOtherLanguage(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/OtherLanguage.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void testInjectionOtherLanguage(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createMultipleTestRecords();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/OtherLanguageInjection.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
        assertNotEquals(
            protectedCharacteristicsOptional.get().getOtherLanguage(),
            answerRequest.getPcqAnswers().getLanguageOther(),
            "OtherLanguage not matching"
        );
    }

    @Test
    void updateEnglishLanguageLevel(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/LanguageLevel.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 5; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setEnglishLanguageLevel(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void updateSex(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Sex.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 3; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setSex(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void updateGender(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Gender.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 3; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setGenderDifferent(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void updateGenderDifferent(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/GenderOther.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void updateSexuality(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Sexuality.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 5; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setSexuality(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void updateOtherSexuality(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/SexualityOther.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void testInjectionOtherSexuality(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createMultipleTestRecords();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/SexualityOtherInjection.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
        assertNotEquals(
            protectedCharacteristicsOptional.get().getOtherSexuality(),
            answerRequest.getPcqAnswers().getSexualityOther(),
            "OtherSexuality not matching"
        );
    }

    @Test
    void updateMarried(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Married.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 3; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setMarriage(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void updateEthnicity(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Ethnicity.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 19; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setEthnicity(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void updateEthnicityOther(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/EthnicityOther.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void testInjectionOtherEthnicity(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createMultipleTestRecords();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/EthnicityOtherInjection.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
        assertNotEquals(
            protectedCharacteristicsOptional.get().getOtherEthnicity(),
            answerRequest.getPcqAnswers().getEthnicityOther(),
            "OtherEtnicity not matching"
        );
    }

    @Test
    void updateReligion(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Religion.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 9; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setReligion(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void updateReligionOther(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/ReligionOther.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void testInjectionOtherReligion(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createMultipleTestRecords();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/ReligionOtherInjection.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
        assertNotEquals(
            protectedCharacteristicsOptional.get().getOtherReligion(),
            answerRequest.getPcqAnswers().getReligionOther(),
            "OtherReligion not matching"
        );
    }

    @Test
    void updateDisabilityConditions(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DisabilityConditions.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 3; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setDisabilityConditions(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void updateDisabilityImpact(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DisabilityImpact.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 4; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setDisabilityImpact(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void updateDisabilityTypes(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DisabilityTypes.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void updateOtherDisabilityDetails(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DisabilityOtherDetails.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void testInjectionOtherDisability(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createMultipleTestRecords();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DisabilityOtherInjection.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertLogsForKeywords(capturedOutput);

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
        assertNotEquals(
            protectedCharacteristicsOptional.get().getOtherDisabilityDetails(),
            answerRequest.getPcqAnswers().getDisabilityConditionOther(),
            "OtherDisabilityDetails not matching"
        );
    }

    @Test
    void updatePregnancy(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/Pregnancy.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        runAnswerUpdates(answerRequest);
        assertLogsForKeywords(capturedOutput);

        for (int i = 0; i < 3; i++) {
            PcqAnswers answers = answerRequest.getPcqAnswers();
            answers.setPregnancy(i);
            answerRequest.setPcqAnswers(answers);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            runAnswerUpdates(answerRequest);
            assertLogsForKeywords(capturedOutput);
        }
    }

    @Test
    void invalidPregnancy(CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidPregnancy.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));

        assertEquals(TEST_PCQ_ID, responseBody.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_BAD_REQUEST, responseBody.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_INVALID_MSG, responseBody.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        assertNotEquals(
            protectedCharacteristicsOptional.get().getPregnancy(),
            answerRequest.getPcqAnswers().getPregnancy(),
            "Pregnancy not matching"
        );

        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void testSqlInjection(CapturedOutput capturedOutput) {
        // Create a record first.
        createTestRecord();

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID
                + "'; DROP TABLE protected_characteristics;",getEncryptionKey());

        assertTrue(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertEquals(protectedCharacteristicsOptional.get().getPcqId(), TEST_PCQ_ID, "PCQ Id not matching");

        assertLogsForKeywords(capturedOutput);
    }

    @Test
    void updateMainLanguageEnglish(CapturedOutput capturedOutput) throws IOException {
        String fileName = "JsonTestFiles/UpdateMainLanguageEnglish.json";
        String errorMessage = "Main Language English not matching";
        assertLanguageUpdated(fileName, errorMessage,capturedOutput);
    }

    @Test
    void updateMainLanguageWelsh(CapturedOutput capturedOutput) throws IOException {
        String fileName = "JsonTestFiles/UpdateMainLanguageWelsh.json";
        String errorMessage = "Main Language Welsh not matching";
        assertLanguageUpdated(fileName, errorMessage,capturedOutput);
    }

    //Test English or welsh option as well for backward compatibility
    @Test
    void updateMainLanguageEnglishOrWelsh(CapturedOutput capturedOutput) throws IOException {
        String fileName = "JsonTestFiles/UpdateMainLanguageEnglishOrWelsh.json";
        String errorMessage = "Main Language English or Welsh not matching";
        assertLanguageUpdated(fileName, errorMessage,capturedOutput);
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

    private PcqAnswerRequest createAnswerRequestForTest(String pcqId) {
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

    private void assertLogsForKeywords(CapturedOutput capture) {
        assertTrue(capture.getAll().contains("Co-Relation Id : " + CO_RELATION_ID_FOR_TEST),
                   "Co-Relation Id was not logged in log files.");
    }

    private void assertLanguageUpdated(String fileName, String errorMessage,
               CapturedOutput capturedOutput) throws IOException {
        // Create a record first.
        createTestRecord();

        String jsonStringRequest = jsonStringFromFile(fileName);
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID, getEncryptionKey());

        assertFalse(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);
        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        assertEquals(
            protectedCharacteristicsOptional.get().getMainLanguage(),
            answerRequest.getPcqAnswers().getLanguageMain(),
            errorMessage
        );
        assertLogsForKeywords(capturedOutput);
    }

}
