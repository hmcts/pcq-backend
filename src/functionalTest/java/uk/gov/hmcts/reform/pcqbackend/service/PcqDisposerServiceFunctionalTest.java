package uk.gov.hmcts.reform.pcqbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@ActiveProfiles("functional")
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class PcqDisposerServiceFunctionalTest {

    public static final String CASE_ID = "9dd003e0-8e63-42d2-ac1e-d2be4bf956d9";

    private List<String> createdPcqs;

    @Autowired
    PcqDisposerService pcqDisposerService;

    @Autowired
    ProtectedCharacteristicsRepository pcqRepository;

    @BeforeEach
    public void setUp() {
        setDisposerServiceValue("keepWithCase", 3652);
        setDisposerServiceValue("keepNoCase", 4383);

        Instant twentyYearsAgo = Instant.now().minus(7000, ChronoUnit.DAYS);
        Instant elevenYearsAgo = Instant.now().minus(4000, ChronoUnit.DAYS);

        createdPcqs = List.of(
            insertPcq(twentyYearsAgo, CASE_ID).getPcqId(),
            insertPcq(twentyYearsAgo, null).getPcqId(),
            insertPcq(elevenYearsAgo, CASE_ID).getPcqId(),
            insertPcq(elevenYearsAgo, null).getPcqId(),
            insertPcq(Instant.now(), CASE_ID).getPcqId(),
            insertPcq(Instant.now(), null).getPcqId()
        );
    }

    @AfterEach
    public void tearDown() {
        pcqRepository.deleteAllById(createdPcqs);
    }

    @Test
    void testDisposePcqLogsCollectedPcqs(CapturedOutput output) {
        setDisposerServiceValue("disposerEnabled", true);
        setDisposerServiceValue("dryRun", true);

        pcqDisposerService.disposePcq();

        List<ProtectedCharacteristics> pcqList = pcqRepository.findAllById(createdPcqs);
        assertThat(pcqList).hasSize(6);
        assertLogMessagesContain(output.getAll(), null, "Deleting old PCQs");
    }

    @Test
    void testDisposePcqLogsDeletesPcqs(CapturedOutput capture) {
        setDisposerServiceValue("disposerEnabled", true);
        setDisposerServiceValue("dryRun", false);

        pcqDisposerService.disposePcq();

        List<ProtectedCharacteristics> pcqList = pcqRepository.findAllById(createdPcqs);
        assertThat(pcqList).hasSize(3);
        assertLogMessagesContain(capture.getAll(), "Deleting old PCQs for real... number to delete 3", null);
    }

    @Test
    void testDisposePcqDoesNotRunIfDisabled(CapturedOutput capture) {
        setDisposerServiceValue("disposerEnabled", false);

        pcqDisposerService.disposePcq();
        List<ProtectedCharacteristics> pcqList = pcqRepository.findAllById(createdPcqs);

        assertThat(pcqList).hasSize(6);

        String logMessages = capture.getAll();
        String expectedMsg = "PCQ disposer is disabled, not running.";
        assertThat(logMessages)
            .withFailMessage("Expected to find + \"" + expectedMsg + "\" in the logs, but didn't find")
            .contains(expectedMsg)
            .withFailMessage("Didn't expect to find \"PCQ disposer completed\" in the logs, but found")
            .doesNotContain("PCQ disposer completed");
    }

    private void assertLogMessagesContain(String logMessages, String expectedMsg, String notExpectedMsg) {
        String expected1 = createdPcqs.get(0);
        String expected2 = createdPcqs.get(1);
        String expected3 = createdPcqs.get(2);

        String notExpected1 = createdPcqs.get(3);
        String notExpected2 = createdPcqs.get(4);
        String notExpected3 = createdPcqs.get(5);

        assertThat(logMessages)
            .withFailMessage("Expected to find \"PCQ id: " + expected1 + "\" in log messages, but didn't find")
            .contains("PCQ id: " + expected1)
            .withFailMessage("Expected to find \"PCQ id: " + expected2 + "\" in log messages, but didn't find")
            .contains("PCQ id: " + expected2)
            .withFailMessage("Expected to find \"PCQ id: " + expected3 + "\" in log messages, but didn't find")
            .contains("PCQ id: " + expected3)
            .withFailMessage("Didn't expect to find PCQ id " + notExpected1 + " in the logs but found")
            .doesNotContain(notExpected1)
            .withFailMessage("Didn't expect to find PCQ id " + notExpected2 + " in the logs but found")
            .doesNotContain(notExpected2)
            .withFailMessage("Didn't expect to find PCQ id " + notExpected3 + " in the logs but found")
            .doesNotContain(notExpected3);

        if (expectedMsg != null) {
            assertThat(logMessages)
                .withFailMessage("Expected to find \"" + expectedMsg + "\" in the logs, but didn't find")
                .contains(expectedMsg);
        }

        if (notExpectedMsg != null) {
            assertThat(logMessages)
                .withFailMessage("Didn't expect to find \"" + notExpectedMsg + "\" in the logs, but found")
                .doesNotContain(notExpectedMsg);
        }
    }

    private void setDisposerServiceValue(String flag, Object value) {
        log.error("PCQ Disposer service - {}", pcqDisposerService);
        ReflectionTestUtils.setField(pcqDisposerService, flag, value);
    }

    public ProtectedCharacteristics insertPcq(Instant completedDate, String caseId) {
        ProtectedCharacteristics pcq = new ProtectedCharacteristics();
        String uuid = UUID.randomUUID().toString();
        pcq.setPcqId(uuid);
        pcq.setCaseId(caseId);
        pcq.setChannel(1);
        pcq.setServiceId("TEST-SERVICE");
        pcq.setActor("APPLICANT");
        pcq.setVersionNumber(1);
        pcq.setCompletedDate(Timestamp.from(completedDate));
        pcq.setLastUpdatedTimestamp(Timestamp.from(completedDate));
        pcqRepository.saveAndFlush(pcq);
        return pcq;
    }

}
