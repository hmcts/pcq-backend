package uk.gov.hmcts.reform.pcqbackend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthorisedServices {

    private final List<String> authorisedServicesList;

    public AuthorisedServices(@Value("#{'${authorised.services}'.split(',\\s*')}") List<String> services) {
        this.authorisedServicesList = List.copyOf(services);
    }

    public boolean hasService(String serviceName) {
        return authorisedServicesList.contains(serviceName);
    }
}
