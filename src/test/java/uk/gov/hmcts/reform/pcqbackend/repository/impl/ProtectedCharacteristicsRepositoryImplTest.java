package uk.gov.hmcts.reform.pcqbackend.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepositoryCustom;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.liquibase.enabled=false",
    "spring.flyway.enabled=true"
})
class ProtectedCharacteristicsRepositoryImplTest {

    private static final String ENCRYPTION_KEY = "ThisIsATestKeyForEncryption";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtectedCharacteristicsRepositoryImplTest.class);

    private static final String NA = "N/A";

    @Autowired
    @Qualifier("protectedCharacteristicsRepositoryImpl")
    private ProtectedCharacteristicsRepositoryCustom protectedCharacteristicsRepositoryCustom;

    @Autowired
    private ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        for (int i = 1; i <= 10; i++) {
            protectedCharacteristicsRepositoryCustom
                .saveProtectedCharacteristicsWithEncryption(getProtectedCharacteristics(
                    String.valueOf(i),
                    "1990-09-" + String.format("%02d", i)
                ), ENCRYPTION_KEY);
        }
    }

    @Test
    void shouldSearchByPcqId() {
        final Optional<ProtectedCharacteristics> pc = protectedCharacteristicsRepository
            .findByPcqId("1", ENCRYPTION_KEY);

        assertThat(pc).isPresent();
        assertThat(pc.get().getPcqId()).isEqualTo("1");
    }

    @Test
    void shouldSearchByDcnNumber() {
        final List<ProtectedCharacteristics> pc = protectedCharacteristicsRepository
            .findByDcnNumber("1", ENCRYPTION_KEY);

        assertThat(pc).isNotEmpty();
        assertThat(pc.get(0).getDcnNumber()).isEqualTo("1");
    }

    @Test
    void shouldUpdateCase() {
        protectedCharacteristicsRepository.updateCase("2", "1");

        final Optional<ProtectedCharacteristics> pc = protectedCharacteristicsRepository
            .findByPcqId("1", ENCRYPTION_KEY);

        assertThat(pc).isPresent();
        assertThat(pc.get().getPcqId()).isEqualTo("1");
        assertThat(pc.get().getCaseId()).isEqualTo("2");
    }

    @Test
    void shouldDeleteRecord() {
        protectedCharacteristicsRepository.deletePcqRecord("1");

        final Optional<ProtectedCharacteristics> pc = protectedCharacteristicsRepository
            .findByPcqId("1", ENCRYPTION_KEY);

        assertThat(pc).isNotPresent();
    }

    private ProtectedCharacteristics getProtectedCharacteristics(final String id, final String dob) {
        final ProtectedCharacteristics pc = new ProtectedCharacteristics();
        pc.setPcqId(id);
        pc.setDcnNumber(id);
        pc.setFormId(id);
        pc.setCaseId(id);
        pc.setPartyId(id);
        pc.setChannel(1);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date dateOfBirth = sdf.parse(dob);
            pc.setDateOfBirth(new java.sql.Date(dateOfBirth.getTime()));  // Correctly convert to java.sql.Date
        } catch (Exception e) {
            LOGGER.error("getProtectedCharacteristics", e);
        }
        pc.setCompletedDate(new Timestamp(System.currentTimeMillis()));
        pc.setServiceId(id);
        pc.setActor(id);
        pc.setVersionNumber(1);
        pc.setDobProvided(1);
        pc.setMainLanguage(1);
        pc.setOtherLanguage("Welsh");
        pc.setEnglishLanguageLevel(2);
        pc.setSex(1);
        pc.setGenderDifferent(0);
        pc.setOtherGender(NA);
        pc.setSexuality(1);
        pc.setOtherSexuality(NA);
        pc.setMarriage(1);
        pc.setEthnicity(1);
        pc.setOtherEthnicity(NA);
        pc.setReligion(1);
        pc.setOtherReligion(NA);
        pc.setDisabilityConditions(0);
        pc.setDisabilityImpact(0);
        pc.setDisabilityVision(0);
        pc.setDisabilityHearing(0);
        pc.setDisabilityMobility(0);
        pc.setDisabilityDexterity(0);
        pc.setDisabilityLearning(0);
        pc.setDisabilityMemory(0);
        pc.setDisabilityMentalHealth(0);
        pc.setDisabilityStamina(0);
        pc.setDisabilitySocial(0);
        pc.setDisabilityOther(0);
        pc.setOtherDisabilityDetails(NA);
        pc.setDisabilityNone(1);
        pc.setPregnancy(0);
        pc.setOptOut(false);
        pc.setLastUpdatedTimestamp(new Timestamp(System.currentTimeMillis()));
        return pc;
    }
}
