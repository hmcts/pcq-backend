package uk.gov.hmcts.reform.pcqbackend.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity(name = "protected_characteristics")
@NoArgsConstructor
@Getter
@Setter
@SuppressWarnings({"PMD.TooManyFields"})
public class ProtectedCharacteristics implements Serializable {

    public static final long serialVersionUID = 5428747L;

    @Id
    private String pcqId;

    @Column(name = "CASE_ID")
    private String caseId;

    @Column(name = "PARTY_ID")
    private String partyId;

    @Column(name = "CHANNEL")
    private Integer channel;

    @Column(name = "COMPLETED_DATE")
    private Timestamp completedDate;

    @Column(name = "SERVICE_ID")
    private Integer serviceId;

    @Column(name = "ACTOR")
    private Integer actor;

    @Column(name = "VERSION_NUMBER")
    private Integer versionNumber;

    @Column(name = "DOB_PROVIDED")
    private Integer dobProvided;

    @Column(name = "DOB")
    private Date dateOfBirth;

    @Column(name = "LANGUAGE_MAIN")
    private Integer mainLanguage;

    @Column(name = "LANGUAGE_OTHER")
    private String otherLanguage;

    @Column(name = "ENGLISH_LANGUAGE_LEVEL")
    private Integer englishLanguageLevel;

    @Column(name = "SEX")
    private Integer sex;

    @Column(name = "GENDER_DIFFERENT")
    private Integer genderDifferent;

    @Column(name = "GENDER_OTHER")
    private String otherGender;

    @Column(name = "SEXUALITY")
    private Integer sexuality;

    @Column(name = "SEXUALITY_OTHER")
    private String otherSexuality;

    @Column(name = "MARRIAGE")
    private Integer marriage;

    @Column(name = "ETHNICITY")
    private Integer ethnicity;

    @Column(name = "ETHNICITY_OTHER")
    private String otherEthnicity;

    @Column(name = "RELIGION")
    private Integer religion;

    @Column(name = "RELIGION_OTHER")
    private String otherReligion;

    @Column(name = "DISABILITY_CONDITIONS")
    private Integer disabilityConditions;

    @Column(name = "DISABILITY_IMPACT")
    private Integer disabilityImpact;

    @Column(name = "DISABILITY_VISION")
    private Integer disabilityVision;

    @Column(name = "DISABILITY_HEARING")
    private Integer disabilityHearing;

    @Column(name = "DISABILITY_MOBILITY")
    private Integer disabilityMobility;

    @Column(name = "DISABILITY_DEXTERITY")
    private Integer disabilityDexterity;

    @Column(name = "DISABILITY_LEARNING")
    private Integer disabilityLearning;

    @Column(name = "DISABILITY_MEMORY")
    private Integer disabilityMemory;

    @Column(name = "DISABILITY_MENTAL_HEALTH")
    private Integer disabilityMentalHealth;

    @Column(name = "DISABILITY_STAMINA")
    private Integer disabilityStamina;

    @Column(name = "DISABILITY_SOCIAL")
    private Integer disabilitySocial;

    @Column(name = "DISABILITY_OTHER")
    private Integer disabilityOther;

    @Column(name = "DISABILITY_CONDITION_OTHER")
    private String otherDisabilityDetails;

    @Column(name = "DISABILITY_NONE")
    private Integer disabilityNone;

    @Column(name = "PREGNANCY")
    private Integer pregnancy;

}
