package uk.gov.hmcts.reform.pcqbackend.service;

import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@WithTags({@WithTag("testType:Integration")})
public class PcqDisposerServiceIntegrationTest extends PcqIntegrationTest {

    public static final String CASE_ID = "9dd003e0-8e63-42d2-ac1e-d2be4bf956d9";

    private static final int KEEP_NO_CASE = 183;

    @Autowired
    PcqDisposerService pcqDisposerService;

    @Autowired
    ProtectedCharacteristicsRepository pcqRepository;

    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();

    @Before
    public void setUp() {
        insertPcq(Instant.now(), CASE_ID);
        insertPcq(Instant.now().minus(KEEP_NO_CASE * 3L, ChronoUnit.DAYS), CASE_ID);
        insertPcq(Instant.now().minus(KEEP_NO_CASE * 3L, ChronoUnit.DAYS), null);
    }

    @Test
    public void testDisposePcqLogsCollectedPcqs() {
        ReflectionTestUtils.setField(pcqDisposerService, "dryRun", true);

        pcqDisposerService.disposePcq();

        List<ProtectedCharacteristics> pcqList = pcqRepository.findAll();
        assertThat(pcqList).hasSize(3);

        String logMessages = capture.getAll();
        assertThat(logMessages)
            .withFailMessage("Expected to find \"DELETABLE PCQ IDS: \" in log message, but didn't find")
            .contains("DELETABLE PCQ IDS: [");

        String notExpected = "Deleting old PCQs";
        assertThat(logMessages)
            .withFailMessage("Didn't expect to find \"" + notExpected + "\" in log message")
            .doesNotContain(notExpected);
    }

    @Test
    public void testDisposePcqLogsDeletesPcqs() {
        ReflectionTestUtils.setField(pcqDisposerService, "dryRun", false);

        pcqDisposerService.disposePcq();

        List<ProtectedCharacteristics> pcqList = pcqRepository.findAll();
        assertThat(pcqList).hasSize(1);

        String logMessages = capture.getAll();
        assertThat(logMessages)
            .withFailMessage("Expected to find \"DELETABLE PCQ IDS: \" in the logs, but didn't find")
            .contains("DELETABLE PCQ IDS: [");

        String expected = "Deleting old PCQs for real... number to delete 2";
        assertThat(logMessages)
            .withFailMessage("Expect to find \"" + expected + "\" in the logs")
            .contains(expected);
    }

    public ProtectedCharacteristics insertPcq(Instant timestamp, String caseId) {
        ProtectedCharacteristics pcq = new ProtectedCharacteristics();
        String uuid = UUID.randomUUID().toString();
        pcq.setPcqId(uuid);
        pcq.setCaseId(caseId);
        pcq.setChannel(1);
        pcq.setServiceId("TEST-SERVICE");
        pcq.setActor("APPLICANT");
        pcq.setVersionNumber(1);
        pcq.setCompletedDate(Timestamp.from(timestamp));
        pcq.setLastUpdatedTimestamp(Timestamp.from(timestamp));
        pcqRepository.saveAndFlush(pcq);
        return pcq;
    }

}
