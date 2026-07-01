package uk.gov.hmcts.reform.pcqbackend.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepositoryCustom;

public class ProtectedCharacteristicsRepositoryImpl implements ProtectedCharacteristicsRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String INSERT_PCQ_ENCRYPTED =
        "INSERT INTO protected_characteristics "
            + "(pcq_Id, DCN_NUMBER, FORM_ID,CASE_ID, PARTY_ID, CHANNEL, COMPLETED_DATE, SERVICE_ID, ACTOR, "
            + " VERSION_NUMBER, DOB_PROVIDED, DOB, LANGUAGE_MAIN, LANGUAGE_OTHER, ENGLISH_LANGUAGE_LEVEL, "
            + " SEX,GENDER_DIFFERENT, GENDER_OTHER,SEXUALITY,SEXUALITY_OTHER,MARRIAGE ,ETHNICITY, "
            + " ETHNICITY_OTHER,RELIGION,RELIGION_OTHER, DISABILITY_CONDITIONS,DISABILITY_IMPACT,"
            + " DISABILITY_VISION,DISABILITY_HEARING,DISABILITY_MOBILITY,DISABILITY_DEXTERITY, "
            + " DISABILITY_LEARNING,DISABILITY_MEMORY,DISABILITY_MENTAL_HEALTH,DISABILITY_STAMINA, "
            + " DISABILITY_SOCIAL,DISABILITY_OTHER, DISABILITY_CONDITION_OTHER, DISABILITY_NONE,PREGNANCY,OPT_OUT, "
            + " LAST_UPDATED_TIMESTAMP)"
            + " VALUES (:pcqId, :dcnNumber, :formId, :caseId, "
            + " encode(pgp_sym_encrypt(cast(:partyId as text), cast(:encryptionKey as text)), 'base64'), "
            + " :channel, cast(cast(:completedDate as text) as timestamp), "
            + " :serviceId, :actor, :versionNumber, "
            + " :dobProvided ,cast(cast(:dateOfBirth as text) as timestamp), "
            + " :mainLanguage, :otherLanguage, "
            + " :englishLanguageLevel, "
            + " :sex, :genderDifferent, "
            + " :otherGender, :sexuality, :otherSexuality, "
            + " :marriage, :ethnicity, "
            + " :otherEthnicity, :religion, "
            + " :otherReligion, :disabilityConditions, "
            + " :disabilityImpact, :disabilityVision, "
            + " :disabilityHearing, "
            + " :disabilityMobility, "
            + " :disabilityDexterity, "
            + " :disabilityLearning, "
            + " :disabilityMemory, "
            + " :disabilityMentalHealth, "
            + " :disabilityStamina,:disabilitySocial, "
            + " :disabilityOther,:otherDisabilityDetails, "
            + " :disabilityNone, :pregnancy, "
            + " cast(cast(:optOut as text) as boolean), "
            + " :lastUpdatedTimestamp) "
            + " RETURNING pcq_Id";

    @Override
    @Transactional
    public void saveProtectedCharacteristicsWithEncryption(
        ProtectedCharacteristics pc,
        String encryptionKey) {
        Query insertQuery = entityManager.createNativeQuery(INSERT_PCQ_ENCRYPTED);

        insertQuery.setParameter("pcqId", pc.getPcqId())
            .setParameter("dcnNumber", pc.getDcnNumber())
            .setParameter("formId", pc.getFormId())
            .setParameter("caseId", pc.getCaseId())
            .setParameter("partyId", pc.getPartyId())
            .setParameter("encryptionKey", encryptionKey)
            .setParameter("channel", pc.getChannel())
            .setParameter("completedDate", pc.getCompletedDate())
            .setParameter("serviceId", pc.getServiceId())
            .setParameter("actor", pc.getActor())
            .setParameter("dobProvided", pc.getDobProvided())
            .setParameter("versionNumber", pc.getVersionNumber())
            .setParameter("dateOfBirth", pc.getDateOfBirth())
            .setParameter("mainLanguage", pc.getMainLanguage())
            .setParameter("otherLanguage", pc.getOtherLanguage())
            .setParameter("englishLanguageLevel", pc.getEnglishLanguageLevel())
            .setParameter("sex", pc.getSex())
            .setParameter("genderDifferent", pc.getGenderDifferent())
            .setParameter("otherGender", pc.getOtherGender())
            .setParameter("sexuality", pc.getSexuality())
            .setParameter("otherSexuality", pc.getOtherSexuality())
            .setParameter("marriage", pc.getMarriage())
            .setParameter("ethnicity", pc.getEthnicity())
            .setParameter("otherEthnicity", pc.getOtherEthnicity())
            .setParameter("religion", pc.getReligion())
            .setParameter("otherReligion", pc.getOtherReligion())
            .setParameter("disabilityConditions", pc.getDisabilityConditions())
            .setParameter("disabilityImpact", pc.getDisabilityImpact())
            .setParameter("disabilityVision", pc.getDisabilityVision())
            .setParameter("disabilityHearing", pc.getDisabilityHearing())
            .setParameter("disabilityMobility", pc.getDisabilityMobility())
            .setParameter("disabilityDexterity", pc.getDisabilityDexterity())
            .setParameter("disabilityLearning", pc.getDisabilityLearning())
            .setParameter("disabilityMemory", pc.getDisabilityMemory())
            .setParameter("disabilityMentalHealth", pc.getDisabilityMentalHealth())
            .setParameter("disabilityStamina", pc.getDisabilityStamina())
            .setParameter("disabilitySocial", pc.getDisabilitySocial())
            .setParameter("disabilityOther", pc.getDisabilityOther())
            .setParameter("otherDisabilityDetails", pc.getOtherDisabilityDetails())
            .setParameter("disabilityNone", pc.getDisabilityNone())
            .setParameter("pregnancy", pc.getPregnancy())
            .setParameter("optOut", pc.getOptOut())
            .setParameter("lastUpdatedTimestamp", pc.getLastUpdatedTimestamp());

        insertQuery.getSingleResult();
    }

}
