package uk.gov.hmcts.reform.pcqbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;

import java.sql.Date;
import java.sql.Timestamp;

@Repository()
public interface ProtectedCharacteristicsRepository extends JpaRepository<ProtectedCharacteristics, Integer> {

    @SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.UseObjectForClearerAPI"})
    @Modifying(clearAutomatically = true)
    @Query(value = "update Protected_Characteristics p set p.dob_provided = ?, p.dob = ?, "
        + "p.language_main = ?, p.language_other = ?, p.english_language_level = ?, "
        + "p.sex = ?, p.gender_different = ?, p.gender_other = ?, p.sexuality = ?, "
        + "p.sexuality_other = ?, p.marriage = ?, p.ethnicity, p.ethnicity_other = ?, "
        + "p.religion = ?, p.religion_other = ?, p.disability_conditions = ?, "
        + "p.disability_impact = ?, p.disability_vision = ?, p.disability_hearing = ?, "
        + "p.disability_mobility = ?, p.disability_dexterity = ?, p.disability_learning = ?, "
        + "p.disability_memory = ?, p.disability_mental_health = ?, p.disability_stamina = ?, "
        + "p.disability_social = ?, p.disability_other = ?, p.disability_condition_other = ?, "
        + "p.disability_none = ?, p.pregnancy = ? where p.pcq_id = ? and p.completed_date < ?", nativeQuery = true)
    int updateCharacteristics(Integer dobProvided, Date dateOfBirth, Integer mainLanguage, String otherLanguage,
                              Integer englishLanguageLevel, Integer sex, Integer genderDifferent, String otherGender,
                              Integer sexuality, String otherSexuality, Integer marriage, Integer ethnicity,
                              String otherEthnicity, Integer religion, String otherReligion,
                              Integer disabilityConditions, Integer disabilityImpact, Integer disabilityVision,
                              Integer disabilityHearing, Integer disabilityMobility, Integer disabilityDexterity,
                              Integer disabilityLearning, Integer disabilityMemory, Integer disabilityMentalHealth,
                              Integer disabilityStamina, Integer disabilitySocial, Integer disabilityOther,
                              String otherDisabilityDetails, Integer disabilityNone, Integer pregnancy,
                              Integer pcqId, Timestamp completedDate);
}
