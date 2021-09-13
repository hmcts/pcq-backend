package uk.gov.hmcts.reform.pcqbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidRequestException;
import uk.gov.hmcts.reform.pcq.commons.model.SubmitResponse;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;
import uk.gov.hmcts.reform.pcqbackend.utils.ConversionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.transaction.Transactional;



@Slf4j
@Service
public class ConsolidationService {

    Environment environment;

    ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    @Autowired
    public ConsolidationService(ProtectedCharacteristicsRepository protectedCharacteristicsRepository,
                                Environment environment) {
        this.protectedCharacteristicsRepository = protectedCharacteristicsRepository;
        this.environment = environment;
    }

    public List<ProtectedCharacteristics> getPcqsWithoutCase(List<String> headers) throws InvalidRequestException {

        String coRelationId = ConversionUtil.validateRequestHeader(headers);
        log.info("Co-Relation Id : {} - getPcqsWithoutCase service invoked", coRelationId);

        List<ProtectedCharacteristics> returnList = protectedCharacteristicsRepository
            .findByCaseIdIsNullAndOptOutFalseAndCompletedDateGreaterThan(
                PcqUtils.getDateTimeInPast(Long.parseLong(Objects.requireNonNull(environment.getProperty(
                "api-config-params.number_of_days_limit")))));

        if (returnList == null) {
            return new ArrayList<>();
        }

        return returnList;
    }

    @Transactional
    public ResponseEntity<SubmitResponse> updateCaseId(List<String> headers, String pcqId, String caseId) {

        try {

            String coRelationId = ConversionUtil.validateRequestHeader(headers);
            log.info("Co-Relation Id : {} - updateCaseId service invoked", coRelationId);

            String tmpPcqId = HtmlUtils.htmlEscape(pcqId);
            int updateCount = protectedCharacteristicsRepository.updateCase(HtmlUtils.htmlEscape(caseId), tmpPcqId);
            if (updateCount == 0) {
                //No Records updated. Generate an 400 error message.
                log.error("Co-Relation Id : {} - updateCaseId - No records found for the supplied pcqId", coRelationId);
                return PcqUtils.generateSubmitResponseEntity(tmpPcqId, HttpStatus.BAD_REQUEST,
                                                                   environment.getProperty(
                                                                       "api-error-messages.bad_request")
                );
            }

            return PcqUtils.generateSubmitResponseEntity(tmpPcqId, HttpStatus.OK, environment.getProperty(
                "api-error-messages.updated"));
        } catch (InvalidRequestException ive) {
            log.error("updateCaseId API call failed due to error - {}", ive.getMessage(), ive);
            return PcqUtils.generateSubmitResponseEntity(pcqId, HttpStatus.BAD_REQUEST,
                                                               environment.getProperty(
                                                                   "api-error-messages.bad_request")
            );
        }
    }

}
