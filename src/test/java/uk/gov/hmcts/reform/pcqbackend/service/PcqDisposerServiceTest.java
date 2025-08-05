package uk.gov.hmcts.reform.pcqbackend.service;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PcqDisposerServiceTest {

    @Mock
    ProtectedCharacteristicsRepository pcqRepository;

    @InjectMocks
    private PcqDisposerService pcqDisposerService;

    @Test
    void disposePcqInDryRunModeShouldNotCallDelete() {
        ReflectionTestUtils.setField(pcqDisposerService, "dryRun", true);
        when(pcqRepository.findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
            any(Timestamp.class), any(Integer.class))).thenReturn(List.of());

        pcqDisposerService.disposePcq();

        verify(pcqRepository, times(1))
            .findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(
                any(Timestamp.class), any(Integer.class));
        verify(pcqRepository, times(1))
            .findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
                any(Timestamp.class), any(Integer.class));
        verifyNoMoreInteractions(pcqRepository);
    }

    @Test
    void disposePcqShouldUseDaysInQueries() {
        ReflectionTestUtils.setField(pcqDisposerService, "dryRun", false);
        ReflectionTestUtils.setField(pcqDisposerService, "keepWithCase", 7);
        ReflectionTestUtils.setField(pcqDisposerService, "keepNoCase", 14);
        ReflectionTestUtils.setField(pcqDisposerService, "rateLimit", 1000);

        // Mocking pcqIds
        List<String> pcqIdsWithCase = List.of("pcqId1", "pcqId2");
        List<String> pcqIdsNoCase = List.of("pcqId3", "pcqId4");

        when(pcqRepository.findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(
            any(Timestamp.class), any(Integer.class))).thenReturn(pcqIdsWithCase);

        when(pcqRepository.findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
            any(Timestamp.class), any(Integer.class))).thenReturn(pcqIdsNoCase);

        pcqDisposerService.disposePcq();

        ArgumentCaptor<Timestamp> timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);

        verify(pcqRepository, times(1))
            .findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(
                timestampCaptor.capture(), any(Integer.class));
        verify(pcqRepository, times(1))
            .findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
                timestampCaptor.capture(), any(Integer.class));

        verify(pcqRepository, times(1)).deleteByPcqIds(anyList());

        verifyNoMoreInteractions(pcqRepository);

        List<Timestamp> timestamps = timestampCaptor.getAllValues();

        Timestamp weekAgo = Timestamp.from(Instant.now().minus(7, ChronoUnit.DAYS));
        Timestamp twoWeeksAgo = Timestamp.from(Instant.now().minus(14, ChronoUnit.DAYS));

        // somewhere time gets converted into UTC, and it mismatches by an hour + some millis
        // in our case one hour doesn't matter as we need to delete based on days
        long delta = HOURS.toMillis(1) + 1000;
        assertThat(timestamps.get(0)).isCloseTo(weekAgo, delta);
        assertThat(timestamps.get(1)).isCloseTo(twoWeeksAgo, delta);
    }

    @Test
    void disposePcqShouldCallDelete() {
        ReflectionTestUtils.setField(pcqDisposerService, "dryRun", false);
        ReflectionTestUtils.setField(pcqDisposerService, "rateLimit", 1000);
        // Mocking pcqIds
        List<String> pcqIdsWithCase = List.of("pcqId1", "pcqId2");
        List<String> pcqIdsNoCase = List.of("pcqId3", "pcqId4");
        when(pcqRepository.findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(
            any(Timestamp.class), any(Integer.class))).thenReturn(pcqIdsWithCase);

        when(pcqRepository.findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
            any(Timestamp.class), any(Integer.class))).thenReturn(pcqIdsNoCase);

        pcqDisposerService.disposePcq();

        verify(pcqRepository, times(1))
            .findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(
                any(Timestamp.class), any(Integer.class));
        verify(pcqRepository, times(1))
            .findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
                any(Timestamp.class), any(Integer.class));

        verify(pcqRepository, times(1)).deleteByPcqIds(anyList());

        verifyNoMoreInteractions(pcqRepository);
    }

    @Test
    void shouldLogErrorWhenDeletionFails() {
        final LogCaptor logCaptor = LogCaptor.forClass(PcqDisposerService.class);
        ReflectionTestUtils.setField(pcqDisposerService, "dryRun", false);
        List<String> pcqIds = List.of("pcqId1", "pcqId2");
        List<String> pcqId2s = List.of("pcqId3", "pcqId4");
        when(pcqRepository.findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(
            any(Timestamp.class), any(Integer.class))).thenReturn(pcqIds);
        when(pcqRepository.findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
            any(Timestamp.class), any(Integer.class))).thenReturn(pcqId2s);

        doThrow(new RuntimeException("Database error"))
            .when(pcqRepository).deleteByPcqIds(anyList());

        pcqDisposerService.disposePcq();
        verify(pcqRepository, times(1)).deleteByPcqIds(anyList());
        verifyNoMoreInteractions(pcqRepository);
        assertThat(logCaptor.getErrorLogs())
            .anyMatch(log -> log.contains("Error executing PCQ Disposer service"));
        assertThat(logCaptor.getErrorLogs())
            .anyMatch(log -> log.contains("Failed to delete batch of PCQs"));
    }
}
