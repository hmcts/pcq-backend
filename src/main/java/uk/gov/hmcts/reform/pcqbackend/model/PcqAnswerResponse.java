package uk.gov.hmcts.reform.pcqbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings({"PMD.TooManyFields"})
public class PcqAnswerResponse implements Serializable {

    public static final long serialVersionUID = 1328743L;

    @JsonProperty("pcq_id")
    private int pcqId;

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("party_id")
    private String partyId;

    @JsonProperty("channel")
    private Integer channel;

    @JsonProperty("completed_date")
    private String completedDate;

    @JsonProperty("service_id")
    private Integer serviceId;

    @JsonProperty("actor")
    private Integer actor;

    @JsonProperty("version_number")
    private Integer versionNo;

    @JsonProperty("dob_provided")
    private Integer dobProvided;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("language_main")
    private Integer languageMain;

    @JsonProperty("language_other")
    private String languageOther;

    @JsonProperty("english_language_level")
    private Integer englishLanguageLevel;

    @JsonProperty("sex")
    private Integer sex;

    @JsonProperty("gender_different")
    private Integer genderDifferent;

    @JsonProperty("gender_other")
    private String genderOther;

    @JsonProperty("sexuality")
    private Integer sexuality;

    @JsonProperty("sexuality_other")
    private String sexualityOther;

    @JsonProperty("marriage")
    private Integer marriage;

    @JsonProperty("ethnicity")
    private Integer ethnicity;

    @JsonProperty("ethnicity_other")
    private String ethnicityOther;

    @JsonProperty("religion")
    private Integer religion;

    @JsonProperty("religion_other")
    private String religionOther;

    @JsonProperty("disability_conditions")
    private Integer disabilityConditions;

    @JsonProperty("disability_impact")
    private Integer disabilityImpact;

    @JsonProperty("disability_vision")
    private Integer disabilityVision;

    @JsonProperty("disability_hearing")
    private Integer disabilityHearing;

    @JsonProperty("disability_mobility")
    private Integer disabilityMobility;

    @JsonProperty("disability_dexterity")
    private Integer disabilityDexterity;

    @JsonProperty("disability_learning")
    private Integer disabilityLearning;

    @JsonProperty("disability_memory")
    private Integer disabilityMemory;

    @JsonProperty("disability_mental_health")
    private Integer disabilityMentalHealth;

    @JsonProperty("disability_stamina")
    private Integer disabilityStamina;

    @JsonProperty("disability_social")
    private Integer disabilitySocial;

    @JsonProperty("disability_other")
    private Integer disabilityOther;

    @JsonProperty("disability_other_details")
    private String disabilityConditionOther;

    @JsonProperty("disability_none")
    private Integer disabilityNone;

    @JsonProperty("pregnancy")
    private Integer pregnancy;

}
