package uk.gov.hmcts.reform.pcqbackend.repository;

import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;

public interface ProtectedCharacteristicsRepositoryCustom {

    void saveProtectedCharacteristicsWithEncryption(
        ProtectedCharacteristics pc,String encryptionKey);

}
