package uk.gov.hmcts.reform.pcqbackend.repository;

import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;

public interface ProtectedCharacteristicsRepositoryCustom {

    //void persist(Object entity);

    void saveProtectedCharacteristicsWithEncryption(
        ProtectedCharacteristics pc,String encryptionKey);

}
