package uk.gov.hmcts.reform.pcqbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        when(pcqRepository.findAllByCaseIdNullAndLastUpdatedTimestampBefore(any(Timestamp.class)))
            .thenReturn(List.of(new ProtectedCharacteristics()));

        pcqDisposerService.disposePcq();

        verify(pcqRepository, times(1))
            .findAllByCaseIdNotNullAndLastUpdatedTimestampBefore(any(Timestamp.class));
        verify(pcqRepository, times(1))
            .findAllByCaseIdNullAndLastUpdatedTimestampBefore(any(Timestamp.class));
        verifyNoMoreInteractions(pcqRepository);
    }

    @Test
    void disposePcqShouldUseDaysInQueries() {
        ReflectionTestUtils.setField(pcqDisposerService, "dryRun", false);
        ReflectionTestUtils.setField(pcqDisposerService, "keepWithCase", 7);
        ReflectionTestUtils.setField(pcqDisposerService, "keepNoCase", 14);

        when(pcqRepository.findAllByCaseIdNullAndLastUpdatedTimestampBefore(any(Timestamp.class)))
            .thenReturn(List.of(new ProtectedCharacteristics()));

        pcqDisposerService.disposePcq();

        ArgumentCaptor<Timestamp> timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);

        verify(pcqRepository, times(1))
            .findAllByCaseIdNotNullAndLastUpdatedTimestampBefore(timestampCaptor.capture());
        verify(pcqRepository, times(1))
            .findAllByCaseIdNullAndLastUpdatedTimestampBefore(timestampCaptor.capture());

        verify(pcqRepository, times(1))
            .deleteInBulkByCaseIdNotNullAndLastUpdatedTimestampBefore(timestampCaptor.capture());
        verify(pcqRepository, times(1))
            .deleteInBulkByCaseIdNullAndLastUpdatedTimestampBefore(timestampCaptor.capture());
        verifyNoMoreInteractions(pcqRepository);

        List<Timestamp> timestamps = timestampCaptor.getAllValues();

        Timestamp weekAgo = Timestamp.from(Instant.now().minus(7, ChronoUnit.DAYS));
        Timestamp twoWeeksAgo = Timestamp.from(Instant.now().minus(14, ChronoUnit.DAYS));

        // somewhere time gets converted into UTC, and it mismatches by an hour + some millis
        // in our case one hour doesn't matter as we need to delete based on days
        long delta = HOURS.toMillis(1) + 1000;
        assertThat(timestamps.get(0)).isCloseTo(weekAgo, delta);
        assertThat(timestamps.get(1)).isCloseTo(twoWeeksAgo, delta);
        assertThat(timestamps.get(2)).isCloseTo(weekAgo, delta);
        assertThat(timestamps.get(3)).isCloseTo(twoWeeksAgo, delta);
    }

    @Test
    void disposePcqShouldCallDelete() {
        ReflectionTestUtils.setField(pcqDisposerService, "dryRun", false);
        when(pcqRepository.findAllByCaseIdNullAndLastUpdatedTimestampBefore(any(Timestamp.class)))
            .thenReturn(List.of(new ProtectedCharacteristics()));

        pcqDisposerService.disposePcq();

        verify(pcqRepository, times(1))
            .findAllByCaseIdNotNullAndLastUpdatedTimestampBefore(any(Timestamp.class));
        verify(pcqRepository, times(1))
            .findAllByCaseIdNullAndLastUpdatedTimestampBefore(any(Timestamp.class));

        verify(pcqRepository, times(1))
            .deleteInBulkByCaseIdNullAndLastUpdatedTimestampBefore(any(Timestamp.class));
        verify(pcqRepository, times(1))
            .deleteInBulkByCaseIdNotNullAndLastUpdatedTimestampBefore(any(Timestamp.class));

        verifyNoMoreInteractions(pcqRepository);
    }
}
