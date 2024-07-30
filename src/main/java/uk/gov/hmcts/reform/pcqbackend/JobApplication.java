package uk.gov.hmcts.reform.pcqbackend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pcqbackend.service.PcqDisposerService;


@Component
@Slf4j
@RequiredArgsConstructor
public class JobApplication implements ApplicationRunner {

    @Value("${disposer.enabled:false}")
    private boolean disposerEnabled;

    private final PcqDisposerService pcqDisposerService;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String job = environment.getProperty("PCQ_DISPOSER_JOB");
            if ("true".equals(job)) {
                if (!disposerEnabled) {
                    log.info("PCQ disposer is disabled, not running.");
                    return;
                }
                pcqDisposerService.disposePcq();
            }
        } catch (Exception e) {
            //To check this error on traces and raise alert
            log.error("Error executing PCQ Disposer service : " + e);
            //To see the stackTrace of Exception
            log.error("Error executing PCQ Disposer service", e);
        }
    }
}

