package uk.gov.hmcts.reform.pcqbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Repository()
public interface ProtectedCharacteristicsRepository extends JpaRepository<ProtectedCharacteristics, String> {

    @SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.UseObjectForClearerAPI"})
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
        + "p.disabilityNone = ?29, p.pregnancy = ?30, p.completedDate = ?31 WHERE p.pcqId = ?32 "
        + "and p.completedDate < ?33")
    int updateCharacteristics(Integer dobProvided, Date dateOfBirth, Integer mainLanguage, String otherLanguage,
                              Integer englishLanguageLevel, Integer sex, Integer genderDifferent, String otherGender,
                              Integer sexuality, String otherSexuality, Integer marriage, Integer ethnicity,
                              String otherEthnicity, Integer religion, String otherReligion,
                              Integer disabilityConditions, Integer disabilityImpact, Integer disabilityVision,
                              Integer disabilityHearing, Integer disabilityMobility, Integer disabilityDexterity,
                              Integer disabilityLearning, Integer disabilityMemory, Integer disabilityMentalHealth,
                              Integer disabilityStamina, Integer disabilitySocial, Integer disabilityOther,
                              String otherDisabilityDetails, Integer disabilityNone, Integer pregnancy,
                              Timestamp completedDateNew, String pcqId, Timestamp completedDate);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE protected_characteristics p SET p.caseId = ?1 WHERE p.pcqId = ?2")
    int updateCase(String caseId, String pcqId);

    List<ProtectedCharacteristics> findByCaseIdIsNullAndCompletedDateGreaterThan(Timestamp completedDate);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM protected_characteristics p WHERE p.pcqId = ?1")
    int deletePcqRecord(String pcqId);

    List<ProtectedCharacteristics> findByDcnNumber(String dcnNumber);
}
