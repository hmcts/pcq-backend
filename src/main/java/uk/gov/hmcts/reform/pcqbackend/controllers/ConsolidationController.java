package uk.gov.hmcts.reform.pcqbackend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pcq.commons.model.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcq.commons.model.SubmitResponse;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidRequestException;
import uk.gov.hmcts.reform.pcqbackend.service.ConsolidationService;
import uk.gov.hmcts.reform.pcqbackend.utils.ConversionUtil;

import java.util.List;

/**
 * Controller for the Consolidation service API calls.
 */
@RestController
@RequestMapping(path = "/pcq/backend/consolidation")
@AllArgsConstructor
@Slf4j
@Tag(name = "PCQ BackEnd - API for consolidation service operations.",
     description = "This is the Protected Characteristics "
    + "Back-End API that will serve the consolidation service to fetch PCQ Ids that don't have an associated "
    + "case record and add case information to a PCQ record in the database. "
    + "The API will be invoked by the Consolidation service.")
public class ConsolidationController {

    private static final String CO_RELATIONID_PROPERTY_NAME = "api-required-header-keys.co-relationid";
    private static final String ACCEPTED_ERROR_MESSAGE_PROPERTY_NAME = "api-error-messages.accepted";
    private static final String BAD_REQUEST_ERROR_MESSAGE_PROPERTY_NAME = "api-error-messages.bad_request";
    private static final String INTERNAL_ERROR_MESSAGE_PROPERTY_NAME = "api-error-messages.internal_error";

    @Autowired
    private Environment environment;

    @Autowired
    private ConsolidationService consolidationService;

    @Operation(
        tags = "PUT end-points", summary = "Add case information on a PCQ answers record.",
        description = "This API will be invoked by the Consolidation process "
            + "to to update the case information ( case id )"
            + " on the PCQ answers record. "
    )
    @ApiResponse(responseCode = "200", description = "Request executed successfully. "
        + "Case Id successfully added to the PCQ Answers record.",
        content = { @Content(schema = @Schema(implementation = SubmitResponse.class))})
    @ApiResponse(responseCode = "400",
        description = "The supplied input parameters are not in the acceptable format. The user"
        + " will be returned a standard error message.",
        content = { @Content(schema = @Schema(implementation = SubmitResponse.class))})
    @ApiResponse(responseCode = "500", description = "General/Un-recoverable error.",
        content = { @Content(schema = @Schema(implementation = SubmitResponse.class))})
    @PutMapping(
        path = "/addCaseForPCQ/{pcqId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SubmitResponse> addCaseForPcq(@RequestHeader HttpHeaders headers,
                                                        @PathVariable("pcqId") @NotBlank String pcqId,
                                                        @NotBlank String caseId) {

        try {
            return consolidationService.updateCaseId(headers.get(environment.getProperty(
                CO_RELATIONID_PROPERTY_NAME)),pcqId, caseId);
        } catch (Exception e) {
            log.error("addCaseForPCQ API call failed due to error - {}", e.getMessage(), e);
            return PcqUtils.generateSubmitResponseEntity(pcqId, HttpStatus.INTERNAL_SERVER_ERROR,
                                                         environment.getProperty(INTERNAL_ERROR_MESSAGE_PROPERTY_NAME));
        }

    }

    @Operation(
        tags = "GET end-points", summary = "Get list of PCQ Record that don't have associated case information.",
        description = "This API will be invoked by the Consolidation process to get a list of PCQ records that don’t "
            + "have an associated case. Any PCQ answer records which are over 90 days old will not be "
            + "returned in the list. The PCQ Answer response will contain the PCQ Id, Service Id and Actor only."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Request executed successfully. Response will contain the multiple/single PCQ Record(s)/ "
            + "empty array")
    @ApiResponse(responseCode = "400", description = "Missing co-relation Id information in the header.")
    @ApiResponse(
        responseCode = "500",
        description = "Any general application/database un-recoverable error")
    @GetMapping(
        path = "/pcqRecordWithoutCase",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PcqRecordWithoutCaseResponse> getPcqRecordWithoutCase(@RequestHeader HttpHeaders headers) {

        try {
            List<ProtectedCharacteristics> protectedCharacteristicsList = consolidationService.getPcqsWithoutCase(
                headers.get(environment.getProperty(CO_RELATIONID_PROPERTY_NAME)));

            return ConversionUtil.generatePcqRecordWithoutCaseResponse(protectedCharacteristicsList, HttpStatus.OK,
                                                                 environment.getProperty(
                                                                     ACCEPTED_ERROR_MESSAGE_PROPERTY_NAME));

        } catch (InvalidRequestException ive) {
            log.error("getPcqRecordWithoutCase API call failed due to error - {}", ive.getMessage(), ive);
            return ConversionUtil.generatePcqRecordWithoutCaseResponse(null, HttpStatus.BAD_REQUEST,
                                                                 environment.getProperty(
                                                                     BAD_REQUEST_ERROR_MESSAGE_PROPERTY_NAME));
        } catch (Exception e) {
            log.error("getPcqRecordWithoutCase API call failed due to error - {}", e.getMessage(), e);
            return ConversionUtil.generatePcqRecordWithoutCaseResponse(null, HttpStatus.INTERNAL_SERVER_ERROR,
                                                                 environment.getProperty(
                                                                     INTERNAL_ERROR_MESSAGE_PROPERTY_NAME));
        }

    }

}
