package uk.gov.hmcts.reform.pcqbackend.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveParameterList"})
class ProtectedCharacteristicsRepositoryImplTest {

    private static final String ENCRYPTION_KEY = "ThisIsATestKeyForEncryption";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtectedCharacteristicsRepositoryImplTest.class);

    private static final String NA = "N/A";

    @Autowired
    @Qualifier("protectedCharacteristicsRepositoryImpl")
    private ProtectedCharacteristicsRepositoryCustom protectedCharacteristicsRepositoryCustom;

    @Autowired
    private ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

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
    void shouldFindByPcqId() {
        final Optional<ProtectedCharacteristics> pc = protectedCharacteristicsRepository
            .findByPcqId("1", ENCRYPTION_KEY);

        assertThat(pc).isPresent();
        assertThat(pc.get().getPcqId()).isEqualTo("1");
    }

    @Test
    void shouldFindByDcnNumber() {
        final List<ProtectedCharacteristics> pc = protectedCharacteristicsRepository
            .findByDcnNumber("1", ENCRYPTION_KEY);

        assertThat(pc).isNotEmpty();
        assertThat(pc.get(0).getDcnNumber()).isEqualTo("1");
    }

    @Test
    void shouldUpdateCharacteristics() {
        String id = "1";
        String dob = "2000-01-01";
        ProtectedCharacteristics npc = getProtectedCharacteristics(id, dob);

        // Modify one of the fields
        npc.setMainLanguage(2);

        // Call the method under test
        protectedCharacteristicsRepository.updateCharacteristics(
            npc.getDobProvided(), npc.getDateOfBirth(), npc.getMainLanguage(), npc.getOtherLanguage(),
            npc.getEnglishLanguageLevel(), npc.getSex(), npc.getGenderDifferent(),
            npc.getOtherGender(),
            npc.getSexuality(), npc.getOtherSexuality(), npc.getMarriage(),
            npc.getEthnicity(),
            npc.getOtherEthnicity(), npc.getReligion(), npc.getOtherReligion(),
            npc.getDisabilityConditions(), npc.getDisabilityImpact(), npc.getDisabilityVision(),
            npc.getDisabilityHearing(), npc.getDisabilityMobility(), npc.getDisabilityDexterity(),
            npc.getDisabilityLearning(), npc.getDisabilityMemory(), npc.getDisabilityMentalHealth(),
            npc.getDisabilityStamina(), npc.getDisabilitySocial(), npc.getDisabilityOther(),
            npc.getOtherDisabilityDetails(), npc.getDisabilityNone(), npc.getPregnancy(),
            npc.getLastUpdatedTimestamp(), npc.getOptOut(), npc.getPcqId(), npc.getCompletedDate()
        );

        // get record
        final Optional<ProtectedCharacteristics> pc = protectedCharacteristicsRepository
            .findByPcqId("1", ENCRYPTION_KEY);

        assertThat(pc).isPresent();
        assertThat(pc.get().getMainLanguage()).isEqualTo(2);
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
    void shouldFindByCaseIdIsNullAndCompletedDateGreaterThan() {
        String priorTimestamp = "2023-06-13 00:00:00";
        protectedCharacteristicsRepository.updateCase(null, "1");

        Timestamp completedDate = Timestamp.valueOf(priorTimestamp);
        Timestamp currentDate = new Timestamp(System.currentTimeMillis());

        // get record
        final List<ProtectedCharacteristics> pc = protectedCharacteristicsRepository
            .findByCaseIdIsNullAndCompletedDateGreaterThanAndLessThan(completedDate,currentDate, ENCRYPTION_KEY);

        assertThat(pc).isNotEmpty();
        assertThat(pc.get(0).getPcqId()).isEqualTo("1");
    }

    @Test
    void shouldFindByCaseIdIsNullAndCompletedDateGreaterThanLessThan() {
        String priorTimestamp = "2023-06-13 00:00:00";
        String lessThanTimeStamp = "2023-12-14 00:00:00";
        ProtectedCharacteristics protectedCharacteristics = getProtectedCharacteristics(
            String.valueOf(11),
            "1990-09-" + String.format("%02d", 11));
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = sdf.parse("2023-11-30");
            long time = date.getTime();
            protectedCharacteristics.setCompletedDate(new Timestamp(time));
            protectedCharacteristics.setCaseId(null);
        } catch (Exception e) {
            LOGGER.error("shouldFindByCaseIdIsNullAndCompletedDateGreaterThanLessThan", e);
        }
        protectedCharacteristicsRepositoryCustom
            .saveProtectedCharacteristicsWithEncryption(protectedCharacteristics, ENCRYPTION_KEY);
        Timestamp completedDate = Timestamp.valueOf(priorTimestamp);
        Timestamp lessThanDate = Timestamp.valueOf(lessThanTimeStamp);

        // get record
        final List<ProtectedCharacteristics> pc = protectedCharacteristicsRepository
            .findByCaseIdIsNullAndCompletedDateGreaterThanAndLessThan(completedDate,lessThanDate, ENCRYPTION_KEY);

        assertThat(pc).isNotEmpty();
        assertThat(pc.get(0).getPcqId()).isEqualTo("11");
    }

    @Test
    void shouldDeleteRecord() {
        protectedCharacteristicsRepository.deletePcqRecord("1");

        final Optional<ProtectedCharacteristics> pc = protectedCharacteristicsRepository
            .findByPcqId("1", ENCRYPTION_KEY);

        assertThat(pc).isNotPresent();
    }

    @Test
    void shouldFindAllByCaseIdNotNullAndLastUpdatedTimestampBefore() {
        Timestamp lastUpdatedTimestamp = new Timestamp(System.currentTimeMillis());

        // get record
        final List<String> pc = protectedCharacteristicsRepository
            .findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(lastUpdatedTimestamp, 1000);

        assertThat(pc).isNotEmpty();
    }

    @Test
    void shouldFindAllByCaseIdNullAndLastUpdatedTimestampBefore() {
        protectedCharacteristicsRepository.updateCase(null, "1");

        Timestamp lastUpdatedTimestamp = new Timestamp(System.currentTimeMillis());

        // get record
        final List<String> pc = protectedCharacteristicsRepository
            .findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(lastUpdatedTimestamp,1000);

        assertThat(pc).isNotEmpty();
        assertThat(pc.get(0)).isEqualTo("1");
    }

    @Test
    void shouldDeleteInBulkByCaseIdNotNullAndLastUpdatedTimestampBefore() {
        Timestamp lastUpdatedTimestamp = new Timestamp(System.currentTimeMillis());

        protectedCharacteristicsRepository
            .deleteInBulkByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(lastUpdatedTimestamp,1000);

        // get record
        final List<String> pc = protectedCharacteristicsRepository
            .findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(lastUpdatedTimestamp,1000);

        assertThat(pc).isEmpty();
    }

    @Test
    void shouldDeleteInBulkByCaseIdNullAndLastUpdatedTimestampBefore() {
        protectedCharacteristicsRepository.updateCase(null, "1");

        Timestamp lastUpdatedTimestamp = new Timestamp(System.currentTimeMillis());

        protectedCharacteristicsRepository.deleteInBulkByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
            lastUpdatedTimestamp,1000);

        // get record
        final List<String> pc = protectedCharacteristicsRepository
            .findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(lastUpdatedTimestamp,1000);

        assertThat(pc).isNotEmpty();
        assertThat(pc.get(0)).isEqualTo("2");
    }

    @Test
    void shouldFindAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit() {
        Timestamp lastUpdatedTimestamp = new Timestamp(System.currentTimeMillis());
        int rateLimit = 2;

        // When
        List<String> result = protectedCharacteristicsRepository
            .findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(lastUpdatedTimestamp, rateLimit);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(rateLimit);
        assertThat(result).containsExactlyInAnyOrder("1","2"); // Adjust based on test data
    }


    @Test
    void shouldFindAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit() {
        protectedCharacteristicsRepository.updateCase(null, "10");
        // Given
        Timestamp lastUpdatedTimestamp = new Timestamp(System.currentTimeMillis());
        int rateLimit = 1;

        // When
        List<String> result = protectedCharacteristicsRepository
            .findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
            lastUpdatedTimestamp, rateLimit
        );

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isLessThanOrEqualTo(rateLimit);
        assertThat(result).containsExactlyInAnyOrder("10"); // Adjust based on test data
    }

    @Test
    void shouldDeleteInBulkByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit() {
        // Given
        Timestamp lastUpdatedTimestamp = new Timestamp(System.currentTimeMillis());
        int rateLimit = 2;

        // Verify initial data
        List<String> initialRecords = protectedCharacteristicsRepository
            .findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(
            lastUpdatedTimestamp, rateLimit
        );
        assertThat(initialRecords).isNotEmpty();

        // When
        protectedCharacteristicsRepository
            .deleteInBulkByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(lastUpdatedTimestamp, rateLimit);

        // Then
        List<String> remainingRecords = protectedCharacteristicsRepository
            .findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(
            lastUpdatedTimestamp, rateLimit
        );
        assertThat(remainingRecords).containsExactlyInAnyOrder("3", "4");
    }

    @Test
    void shouldDeleteInBulkByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit() {
        // Given
        Timestamp lastUpdatedTimestamp = new Timestamp(System.currentTimeMillis());
        int rateLimit = 2;
        protectedCharacteristicsRepository.updateCase(null, "8");
        protectedCharacteristicsRepository.updateCase(null, "9");
        protectedCharacteristicsRepository.updateCase(null, "10");
        // Verify initial data
        List<String> initialRecords = protectedCharacteristicsRepository
            .findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
            lastUpdatedTimestamp, rateLimit
        );
        assertThat(initialRecords).isNotEmpty();

        // When
        protectedCharacteristicsRepository
            .deleteInBulkByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(lastUpdatedTimestamp, rateLimit);

        // Then
        List<String> remainingRecords = protectedCharacteristicsRepository
            .findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(
            lastUpdatedTimestamp, rateLimit
        );
        assertThat(remainingRecords).containsExactlyInAnyOrder("10");
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
