package uk.gov.hmcts.reform.pcqbackend.util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@Slf4j
public class LeaderContext {
    private Context context;

    @EventListener
    public void handleEvent(OnGrantedEvent event) {
        log.info("Leader election event received: {}", event);
        context = event.getContext();
    }

    @SuppressWarnings("PMD.NullAssignment")
    @EventListener
    public void handleEvent(OnRevokedEvent event) {
        context = null;
    }

    public boolean isLeader() {
        return context != null;
    }

}
