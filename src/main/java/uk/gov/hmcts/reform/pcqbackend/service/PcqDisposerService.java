package uk.gov.hmcts.reform.pcqbackend.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PcqDisposerService {

    @Value("${disposer.dry-run:true}")
    private boolean dryRun;

    @Value("${disposer.rateLimit:1000}")
    private int rateLimit;

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

        // Convert to mutable lists
        List<String> pcqListWithCaseIds = new ArrayList<>(pcqRepository
            .findAllPcqIdsByCaseIdNotNullAndLastUpdatedTimestampBeforeWithLimit(caseCutoffTimestamp,rateLimit));

        List<String> pcqListNoCaseIds = new ArrayList<>(pcqRepository
            .findAllPcqIdsByCaseIdNullAndLastUpdatedTimestampBeforeWithLimit(noCaseCutoffTimestamp, rateLimit));

        log.info("PCQs # with case id present: {}", pcqListWithCaseIds.size());
        log.info("PCQs # with no case ids present: {}", pcqListNoCaseIds.size());

        pcqListWithCaseIds.addAll(pcqListNoCaseIds);

        List<List<String>> splitLists = Lists.partition(pcqListWithCaseIds, 100);
        splitLists.forEach(split -> log.info("DELETABLE PCQ IDS: {}", split));

        if (!dryRun && !pcqListWithCaseIds.isEmpty()) {
            for (List<String> batch : splitLists) {
                pcqRepository.deleteByPcqIds(batch);  // updated repository metho
            }
        }

        log.info("PCQ disposer completed");
    }
}
