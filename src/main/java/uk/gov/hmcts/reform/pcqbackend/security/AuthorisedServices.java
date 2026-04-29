package uk.gov.hmcts.reform.pcqbackend.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthorisedServices {

    @Value("#{'${authorised.services}'.split(',\\s*')}")
    private List<String> authorisedServicesList;

    @PostConstruct
    public void init() {
        authorisedServicesList = List.copyOf(authorisedServicesList);
    }

    public boolean hasService(String serviceName) {
        return authorisedServicesList.contains(serviceName);
    }
}
