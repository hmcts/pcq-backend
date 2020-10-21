package uk.gov.hmcts.reform.pcqbackend.repository.impl;

import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ProtectedCharacteristicsRepositoryImpl implements ProtectedCharacteristicsRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void persist(Object entity) {
        entityManager.persist(entity);
    }

}
