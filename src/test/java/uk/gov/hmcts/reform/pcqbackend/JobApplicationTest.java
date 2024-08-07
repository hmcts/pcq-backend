package uk.gov.hmcts.reform.pcqbackend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.pcqbackend.service.PcqDisposerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class JobApplicationTest {

    @Mock
    PcqDisposerService pcqDisposerService;

    @Mock
    Environment environment;

    @InjectMocks
    JobApplication jobApplication;

    private static final String DISPOSER_ENABLED = "disposerEnabled";
    private static final String PCQ_DISPOSER_JOB_PROPERTY = "PCQ_DISPOSER_JOB";

    @Test
    void shouldCallDisposePcq() {
        // given
        ReflectionTestUtils.setField(jobApplication, DISPOSER_ENABLED, true);
        when(environment.getProperty(PCQ_DISPOSER_JOB_PROPERTY)).thenReturn("true");

        // when
        jobApplication.run(null);

        // then
        verify(pcqDisposerService, times(1)).disposePcq();
    }

    @Test
    void shouldNotCallDisposePcqIfNotJob() {
        // given
        ReflectionTestUtils.setField(jobApplication, DISPOSER_ENABLED, true);
        when(environment.getProperty(PCQ_DISPOSER_JOB_PROPERTY)).thenReturn("");

        // when
        jobApplication.run(null);

        // then
        verify(pcqDisposerService, times(0)).disposePcq();
    }

    @Test
    void shouldNotCallDisposePcqIfDisposerDisabled(CapturedOutput output) {
        // given
        ReflectionTestUtils.setField(jobApplication, DISPOSER_ENABLED, false);
        when(environment.getProperty(PCQ_DISPOSER_JOB_PROPERTY)).thenReturn("true");

        // when
        jobApplication.run(null);

        // then
        verify(pcqDisposerService, times(0)).disposePcq();
        assertThat(output).contains("PCQ disposer is disabled, not running.");
    }

    @Test
    void shouldCatchExceptionIfThrowException() {

        // given
        ReflectionTestUtils.setField(jobApplication, DISPOSER_ENABLED, true);
        when(environment.getProperty(PCQ_DISPOSER_JOB_PROPERTY)).thenReturn("true");
        Mockito.doThrow(new IllegalArgumentException("Exception from PCQ Disposer service"))
            .when(pcqDisposerService).disposePcq();

        // when
        jobApplication.run(null);

        // then
        verify(pcqDisposerService, times(1)).disposePcq();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> pcqDisposerService.disposePcq());
        assertThat(exception).hasMessageContaining("Exception from PCQ Disposer service");
    }
}
