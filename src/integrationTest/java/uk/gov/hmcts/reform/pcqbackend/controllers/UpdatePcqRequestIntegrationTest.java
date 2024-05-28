package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
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
public class UpdatePcqRequestIntegrationTest extends PcqIntegrationTest {

    public static final String RESPONSE_KEY_4 = "response_body";
    public static final String HTTP_ACCEPTED = "202";
    public static final String HTTP_BAD_REQUEST = "400";
    public static final String RESPONSE_ACCEPTED_MSG = "Success";
    public static final String RESPONSE_INVALID_MSG = "Invalid Request";

    private static final String TEST_DUP_PCQ_ID = "UPDATE-DUP-INTEG-TEST";

    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();

    @Test
    public void updateDobProvidedSuccess() throws IOException {
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
        assertLogsForKeywords();
    }

    @Test
    public void updateDobProvidedSuccessOptOutNull() throws IOException {
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
        assertLogsForKeywords();
    }

    @Test
    public void dobProvidedStateDate() throws IOException {
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

        assertLogsForKeywords();
    }

    @Test
    public void updateDobSuccess() throws IOException {
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
        assertLogsForKeywords();
    }

    @Test
    public void invalidDob() throws IOException {
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

        assertLogsForKeywords();
    }

    @Test
    public void updateMainLanguage() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void updateOtherLanguage() throws IOException {
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
        assertLogsForKeywords();
    }

    @Test
    public void testInjectionOtherLanguage() throws IOException {
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
        assertLogsForKeywords();

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
        assertNotEquals(
            protectedCharacteristicsOptional.get().getOtherLanguage(),
            answerRequest.getPcqAnswers().getLanguageOther(),
            "OtherLanguage not matching"
        );
    }

    @Test
    public void updateEnglishLanguageLevel() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void updateSex() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void updateGender() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void updateGenderDifferent() throws IOException {
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
        assertLogsForKeywords();
    }

    @Test
    public void updateSexuality() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void updateOtherSexuality() throws IOException {
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
        assertLogsForKeywords();
    }

    @Test
    public void testInjectionOtherSexuality() throws IOException {
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
        assertLogsForKeywords();

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
        assertNotEquals(
            protectedCharacteristicsOptional.get().getOtherSexuality(),
            answerRequest.getPcqAnswers().getSexualityOther(),
            "OtherSexuality not matching"
        );
    }

    @Test
    public void updateMarried() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void updateEthnicity() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void updateEthnicityOther() throws IOException {
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
        assertLogsForKeywords();
    }

    @Test
    public void testInjectionOtherEthnicity() throws IOException {
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
        assertLogsForKeywords();

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
        assertNotEquals(
            protectedCharacteristicsOptional.get().getOtherEthnicity(),
            answerRequest.getPcqAnswers().getEthnicityOther(),
            "OtherEtnicity not matching"
        );
    }

    @Test
    public void updateReligion() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void updateReligionOther() throws IOException {
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
        assertLogsForKeywords();
    }

    @Test
    public void testInjectionOtherReligion() throws IOException {
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
        assertLogsForKeywords();

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
        assertNotEquals(
            protectedCharacteristicsOptional.get().getOtherReligion(),
            answerRequest.getPcqAnswers().getReligionOther(),
            "OtherReligion not matching"
        );
    }

    @Test
    public void updateDisabilityConditions() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void updateDisabilityImpact() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void updateDisabilityTypes() throws IOException {
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
        assertLogsForKeywords();
    }

    @Test
    public void updateOtherDisabilityDetails() throws IOException {
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
        assertLogsForKeywords();
    }

    @Test
    public void testInjectionOtherDisability() throws IOException {
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
        assertLogsForKeywords();

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());
        assertNotEquals(
            protectedCharacteristicsOptional.get().getOtherDisabilityDetails(),
            answerRequest.getPcqAnswers().getDisabilityConditionOther(),
            "OtherDisabilityDetails not matching"
        );
    }

    @Test
    public void updatePregnancy() throws IOException {
        // Create a record first.
        createTestRecord();

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
    }

    @Test
    public void invalidPregnancy() throws IOException {
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

        assertLogsForKeywords();
    }

    @Test
    public void testSqlInjection() {
        // Create a record first.
        createTestRecord();

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID
                + "'; DROP TABLE protected_characteristics;",getEncryptionKey());

        assertTrue(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);

        protectedCharacteristicsOptional = protectedCharacteristicsRepository
            .findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertEquals(protectedCharacteristicsOptional.get().getPcqId(), TEST_PCQ_ID, "PCQ Id not matching");

        assertLogsForKeywords();
    }

    @Test
    public void updateMainLanguageEnglish() throws IOException {
        String fileName = "JsonTestFiles/UpdateMainLanguageEnglish.json";
        String errorMessage = "Main Language English not matching";
        assertLanguageUpdated(fileName, errorMessage);
    }

    @Test
    public void updateMainLanguageWelsh() throws IOException {
        String fileName = "JsonTestFiles/UpdateMainLanguageWelsh.json";
        String errorMessage = "Main Language Welsh not matching";
        assertLanguageUpdated(fileName, errorMessage);
    }

    //Test English or welsh option as well for backward compatibility
    @Test
    public void updateMainLanguageEnglishOrWelsh() throws IOException {
        String fileName = "JsonTestFiles/UpdateMainLanguageEnglishOrWelsh.json";
        String errorMessage = "Main Language English or Welsh not matching";
        assertLanguageUpdated(fileName, errorMessage);
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

    private void assertLanguageUpdated(String fileName, String errorMessage) throws IOException {
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
        assertLogsForKeywords();
    }

}
