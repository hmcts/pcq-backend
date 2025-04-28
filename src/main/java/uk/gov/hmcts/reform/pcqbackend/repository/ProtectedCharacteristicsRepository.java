package uk.gov.hmcts.reform.pcqbackend.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository()
public interface ProtectedCharacteristicsRepository extends JpaRepository<ProtectedCharacteristics, String>,
    ProtectedCharacteristicsRepositoryCustom {

    @SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.UseObjectForClearerAPI", "squid:S107"})
    @Modifying(clearAutomatically = true)
    @Query("UPDATE protected_characteristics p SET p.dobProvided = ?1, p.dateOfBirth = ?2, "
        + "p.mainLanguage = ?3, p.otherLanguage = ?4, p.englishLanguageLevel = ?5, "
        + "p.sex = ?6, p.genderDifferent = ?7, p.otherGender = ?8, p.sexuality = ?9, "
        + "p.otherSexuality = ?10, p.marriage = ?11, p.ethnicity = ?12, p.otherEthnicity = ?13, "
        + "p.religion = ?14, p.otherReligion = ?15, p.disabilityConditions = ?16, "
        + "p.disabilityImpact = ?17, p.disabilityVision = ?18, p.disabilityHearing = ?19, "
        + "p.disabilityMobility = ?20, p.disabilityDexterity = ?21, p.disabilityLearning = ?22, "
        + "p.disabilityMemory = ?23, p.disabilityMentalHealth = ?24, p.disabilityStamina = ?25, "
        + "p.disabilitySocial = ?26, p.disabilityOther = ?27, p.otherDisabilityDetails = ?28, "
        + "p.disabilityNone = ?29, p.pregnancy = ?30, p.completedDate = ?31, p.optOut = ?32, "
        + "p.lastUpdatedTimestamp = ?31 "
        + "WHERE p.pcqId = ?33 and p.completedDate < ?34")
    int updateCharacteristics(Integer dobProvided, Date dateOfBirth, Integer mainLanguage, String otherLanguage,
                              Integer englishLanguageLevel, Integer sex, Integer genderDifferent, String otherGender,
                              Integer sexuality, String otherSexuality, Integer marriage, Integer ethnicity,
                              String otherEthnicity, Integer religion, String otherReligion,
                              Integer disabilityConditions, Integer disabilityImpact, Integer disabilityVision,
                              Integer disabilityHearing, Integer disabilityMobility, Integer disabilityDexterity,
                              Integer disabilityLearning, Integer disabilityMemory, Integer disabilityMentalHealth,
                              Integer disabilityStamina, Integer disabilitySocial, Integer disabilityOther,
                              String otherDisabilityDetails, Integer disabilityNone, Integer pregnancy,
                              Timestamp completedDateNew, Boolean optOut, String pcqId, Timestamp completedDate);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE protected_characteristics p "
        + "SET p.lastUpdatedTimestamp = CURRENT_TIMESTAMP, p.caseId = ?1 "
        + "WHERE p.pcqId = ?2")
    int updateCase(String caseId, String pcqId);


    @Query(value = "SELECT pc.pcq_id, pc.DCN_NUMBER, pc.FORM_ID, pc.CASE_ID, "
        + "pgp_sym_decrypt(decode(pc.party_Id, 'base64'), cast(:encryptionKey as text)) as party_id, "
        + "pc.CHANNEL, pc.COMPLETED_DATE, pc.SERVICE_ID, pc.ACTOR, "
        + "pc.VERSION_NUMBER, pc.DOB_PROVIDED, pc.DOB, pc.LANGUAGE_MAIN ,"
        + "pc.LANGUAGE_OTHER, pc.ENGLISH_LANGUAGE_LEVEL, pc.SEX, pc.GENDER_DIFFERENT, "
        + "pc.GENDER_OTHER, pc.SEXUALITY, pc.SEXUALITY_OTHER, pc.MARRIAGE, "
        + "pc.ETHNICITY, pc.ETHNICITY_OTHER, pc.RELIGION, pc.RELIGION_OTHER, "
        + "pc.DISABILITY_CONDITIONS, pc.DISABILITY_IMPACT, pc.DISABILITY_VISION, "
        + "pc.DISABILITY_HEARING, pc.DISABILITY_MOBILITY, pc.DISABILITY_DEXTERITY, "
        + "pc.DISABILITY_LEARNING, pc.DISABILITY_MEMORY, pc.DISABILITY_MENTAL_HEALTH, "
        + "pc.DISABILITY_STAMINA, pc.DISABILITY_SOCIAL, pc.DISABILITY_OTHER, "
        + "pc.DISABILITY_CONDITION_OTHER, "
        + "pc.DISABILITY_NONE, pc.PREGNANCY, pc.OPT_OUT, pc.LAST_UPDATED_TIMESTAMP "
        + "FROM protected_characteristics pc "
        + "WHERE pc.case_id IS NULL AND  pc.COMPLETED_DATE > :completedDate "
        + "AND  pc.COMPLETED_DATE < :lessThanDate",
        nativeQuery = true)
    List<ProtectedCharacteristics> findByCaseIdIsNullAndCompletedDateGreaterThanAndLessThan(Timestamp completedDate,
                    Timestamp lessThanDate,String encryptionKey);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM protected_characteristics p WHERE p.pcqId = ?1")
    int deletePcqRecord(String pcqId);

    @Query(value = "SELECT pc.pcq_id, pc.DCN_NUMBER, pc.FORM_ID, pc.CASE_ID, "
        + "pgp_sym_decrypt(decode(pc.party_Id, 'base64'), cast(:encryptionKey as text)) as party_id, "
        + "pc.CHANNEL, pc.COMPLETED_DATE, pc.SERVICE_ID, pc.ACTOR, "
        + "pc.VERSION_NUMBER, pc.DOB_PROVIDED, pc.DOB, pc.LANGUAGE_MAIN ,"
        + "pc.LANGUAGE_OTHER, pc.ENGLISH_LANGUAGE_LEVEL, pc.SEX, pc.GENDER_DIFFERENT, "
        + "pc.GENDER_OTHER, pc.SEXUALITY, pc.SEXUALITY_OTHER, pc.MARRIAGE, "
        + "pc.ETHNICITY, pc.ETHNICITY_OTHER, pc.RELIGION, pc.RELIGION_OTHER, "
        + "pc.DISABILITY_CONDITIONS, pc.DISABILITY_IMPACT, pc.DISABILITY_VISION, "
        + "pc.DISABILITY_HEARING, pc.DISABILITY_MOBILITY, pc.DISABILITY_DEXTERITY, "
        + "pc.DISABILITY_LEARNING, pc.DISABILITY_MEMORY, pc.DISABILITY_MENTAL_HEALTH, "
        + "pc.DISABILITY_STAMINA, pc.DISABILITY_SOCIAL, pc.DISABILITY_OTHER, "
        + "pc.DISABILITY_CONDITION_OTHER, "
        + "pc.DISABILITY_NONE, pc.PREGNANCY, pc.OPT_OUT, pc.LAST_UPDATED_TIMESTAMP "
        + "FROM protected_characteristics pc "
        + "WHERE pc.DCN_NUMBER= :dcnNumber ",
        nativeQuery = true)
    List<ProtectedCharacteristics> findByDcnNumber(String dcnNumber, String encryptionKey);


    @Query(value = "SELECT pc.pcq_id, pc.DCN_NUMBER, pc.FORM_ID, pc.CASE_ID, "
        + "pgp_sym_decrypt(decode(pc.party_Id, 'base64'), cast(:encryptionKey as text)) as party_id , "
        + "pc.CHANNEL, pc.COMPLETED_DATE, pc.SERVICE_ID, pc.ACTOR, "
        + "pc.VERSION_NUMBER, pc.DOB_PROVIDED, pc.DOB, pc.LANGUAGE_MAIN ,"
        + "pc.LANGUAGE_OTHER, pc.ENGLISH_LANGUAGE_LEVEL, pc.SEX, pc.GENDER_DIFFERENT, "
        + "pc.GENDER_OTHER, pc.SEXUALITY, pc.SEXUALITY_OTHER, pc.MARRIAGE, "
        + "pc.ETHNICITY, pc.ETHNICITY_OTHER, pc.RELIGION, pc.RELIGION_OTHER, "
        + "pc.DISABILITY_CONDITIONS, pc.DISABILITY_IMPACT, pc.DISABILITY_VISION, "
        + "pc.DISABILITY_HEARING, pc.DISABILITY_MOBILITY, pc.DISABILITY_DEXTERITY, "
        + "pc.DISABILITY_LEARNING, pc.DISABILITY_MEMORY, pc.DISABILITY_MENTAL_HEALTH, "
        + "pc.DISABILITY_STAMINA, pc.DISABILITY_SOCIAL, pc.DISABILITY_OTHER, "
        + "pc.DISABILITY_CONDITION_OTHER, "
        + "pc.DISABILITY_NONE, pc.PREGNANCY, pc.OPT_OUT, pc.LAST_UPDATED_TIMESTAMP "
        + "FROM protected_characteristics pc "
        + "WHERE pc.pcq_id= :pcqId ",
        nativeQuery = true)
    Optional<ProtectedCharacteristics> findByPcqId(String pcqId, String encryptionKey);


    @Query(value = "SELECT pc.pcq_id FROM protected_characteristics pc WHERE pc.case_id IS NOT NULL "
        + "AND pc.last_updated_timestamp < :lastUpdatedTimestamp "
        + "ORDER BY pc.last_updated_timestamp ASC LIMIT :rateLimit", nativeQuery = true)
    List<String> findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(
        @Param("lastUpdatedTimestamp") Timestamp lastUpdatedTimestamp,
        @Param("rateLimit") int rateLimit
    );

    @Query(value = "SELECT pc.pcq_id FROM protected_characteristics pc WHERE pc.case_id IS NULL "
        + "AND pc.last_updated_timestamp < :lastUpdatedTimestamp "
        + "ORDER BY pc.last_updated_timestamp ASC LIMIT :rateLimit", nativeQuery = true)
    List<String> findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
        @Param("lastUpdatedTimestamp") Timestamp lastUpdatedTimestamp,
        @Param("rateLimit") int rateLimit
    );

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM protected_characteristics pc "
        + "WHERE pc.pcq_id IN (SELECT pcq_id FROM protected_characteristics "
        + "WHERE case_id IS NOT NULL AND last_updated_timestamp < :lastUpdatedTimestamp "
        + "ORDER BY pc.last_updated_timestamp ASC LIMIT :rateLimit)",
        nativeQuery = true)
    void deleteInBulkByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(
        @Param("lastUpdatedTimestamp") Timestamp lastUpdatedTimestamp,
        @Param("rateLimit") int rateLimit
    );

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM protected_characteristics pc "
        + "WHERE pc.pcq_id IN (SELECT pcq_id FROM protected_characteristics "
        + "WHERE case_id IS NULL AND last_updated_timestamp < :lastUpdatedTimestamp "
        + "ORDER BY pc.last_updated_timestamp ASC LIMIT :rateLimit)",
        nativeQuery = true)
    void deleteInBulkByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
        @Param("lastUpdatedTimestamp") Timestamp lastUpdatedTimestamp,
        @Param("rateLimit") int rateLimit
    );

}
