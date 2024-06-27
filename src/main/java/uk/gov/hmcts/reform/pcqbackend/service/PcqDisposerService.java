package uk.gov.hmcts.reform.pcqbackend.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PcqDisposerService {

    @Value("${disposer.dry-run:true}")
    private boolean dryRun;

    @Value("${disposer.keep-with-case:92}")
    private int keepWithCase;

    @Value("${disposer.keep-no-case:183}")
    private int keepNoCase;

    private final ProtectedCharacteristicsRepository pcqRepository;

    public void disposePcq() {
        log.info("Starting PCQ disposer, dry run: {}", dryRun);

        Timestamp caseCutoffTimestamp = PcqUtils.getDateTimeInPast(keepWithCase);
        Timestamp noCaseCutoffTimestamp = PcqUtils.getDateTimeInPast(keepNoCase);

        log.info("Disposing PCQs with last updated timestamp before {} where case id is present", caseCutoffTimestamp);
        log.info(
            "Disposing PCQs with last updated timestamp before {} where case id is absent",
            noCaseCutoffTimestamp
        );

        List<ProtectedCharacteristics> pcqList = pcqRepository
            .findAllByCaseIdNotNullAndLastUpdatedTimestampBefore(caseCutoffTimestamp);

        List<ProtectedCharacteristics> pcqListNoCaseIds = pcqRepository
            .findAllByCaseIdNullAndLastUpdatedTimestampBefore(noCaseCutoffTimestamp);

        log.info("PCQs # with case id present: {}", pcqList.size());
        log.info("PCQs # with no case ids present: {}", pcqListNoCaseIds.size());

        pcqList.addAll(pcqListNoCaseIds);

        List<String> pcqIds = pcqList.stream().map(ProtectedCharacteristics::getPcqId).toList();

        List<List<String>> splitLists = Lists.partition(pcqIds, 100);
        splitLists.forEach(split -> log.info("DELETABLE PCQ IDS: {}", split));

        if (!dryRun && !pcqList.isEmpty()) {
            log.info("Deleting old PCQs for real... number to delete {}", pcqList.size());
            pcqRepository.deleteInBulkByCaseIdNotNullAndLastUpdatedTimestampBefore(caseCutoffTimestamp);
            pcqRepository.deleteInBulkByCaseIdNullAndLastUpdatedTimestampBefore(noCaseCutoffTimestamp);
        }

        log.info("PCQ disposer completed");
    }
}
