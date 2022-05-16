package uk.gov.hmcts.reform.pcqbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import javax.transaction.Transactional;

@Slf4j
@Service
public class DeleteService {
    protected static final String NOT_FOUND_ERROR_MSG_KEY = "api-error-messages.not_found";
    Environment environment;

    ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    @Autowired
    public DeleteService(ProtectedCharacteristicsRepository protectedCharacteristicsRepository,
                                Environment environment) {
        this.protectedCharacteristicsRepository = protectedCharacteristicsRepository;
        this.environment = environment;
    }

    @Transactional
    public ResponseEntity<Object> deletePcqRecord(String pcqId) {
        log.info("deletePcqRecord API invoked PcqId {}", pcqId);
        int resultCount = protectedCharacteristicsRepository.deletePcqRecord(HtmlUtils.htmlEscape(pcqId));
        if (resultCount == 0) {
            log.error(
                "PCQ Id : {} - Delete PCQ Record API, PCQ record does not exist.",
                pcqId
            );
            return PcqUtils.generateResponseEntity(pcqId, HttpStatus.NOT_FOUND,
                                                   environment.getProperty(
                                                       NOT_FOUND_ERROR_MSG_KEY)
            );
        } else {
            log.info("PCQ IDd : {} - Protected Characteristic Record has been deleted.", pcqId

            );
            return PcqUtils.generateResponseEntity(pcqId, HttpStatus.OK,
                                                   environment.getProperty(
                                                       "api-error-messages.deleted")
            );
        }
    }
}
