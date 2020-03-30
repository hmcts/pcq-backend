package uk.gov.hmcts.reform.pcqbackend.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidRequestException;
import uk.gov.hmcts.reform.pcqbackend.model.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqbackend.model.SubmitResponse;
import uk.gov.hmcts.reform.pcqbackend.service.ConsolidationService;
import uk.gov.hmcts.reform.pcqbackend.utils.ConversionUtil;

import java.util.List;
import javax.validation.constraints.NotBlank;


/**
 * Controller for the Consolidation service API calls.
 */
@RestController
@RequestMapping(path = "/pcq/backend/consolidation")
@AllArgsConstructor
@Slf4j
@Api(tags = "PCQ BackEnd - API for consolidation service operations.", value = "This is the Protected Characteristics "
    + "Back-End API that will serve the consolidation service to fetch PCQ Ids that don't have an associated "
    + "case record and add case information to a PCQ record in the database. "
    + "The API will be invoked by the Consolidation service.")
public class ConsolidationController {

    @Autowired
    private Environment environment;

    @Autowired
    private ConsolidationService consolidationService;

    @ApiOperation(
        tags = "Get PCQ Ids", value = "Get list of PCQ Ids that don't have associated case information.",
        notes = "This API will be invoked by the Consolidation process to get a list of PCQ records that don’t "
                + "have an associated case. Any PCQ answer records which are over 90 days old will not be "
                + "returned in the list."
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Request executed successfully. Response will contain the multiple/single PCQ Id(s)/ "
                + "empty array",
            response = PcqWithoutCaseResponse.class
        ),
        @ApiResponse(code = 400, message = "Missing co-relation Id information in the header."),
        @ApiResponse(
            code = 500,
            message = "Any general application/database un-recoverable error"
        )
    })
    @GetMapping(
        path = "/pcqWithoutCase",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<PcqWithoutCaseResponse> getPcqIdsWithoutCase(@RequestHeader HttpHeaders headers) {

        try {

            List<ProtectedCharacteristics> protectedCharacteristicsList = consolidationService.getPcqsWithoutCase
                (headers.get(environment.getProperty("api-required-header-keys.co-relationid")));

            return ConversionUtil.generatePcqWithoutCaseResponse(protectedCharacteristicsList, HttpStatus.OK,
                                                                 environment.getProperty(
                                                                     "api-error-messages.accepted"));

        } catch (InvalidRequestException ive) {
            log.error("getPcqIdsWithoutCase API call failed due to error - {}", ive.getMessage(), ive);
            return ConversionUtil.generatePcqWithoutCaseResponse(null, HttpStatus.BAD_REQUEST,
                                                                 environment.getProperty(
                                                                     "api-error-messages.bad_request"));
        } catch (Exception e) {
            log.error("getPcqIdsWithoutCase API call failed due to error - {}", e.getMessage(), e);
            return ConversionUtil.generatePcqWithoutCaseResponse(null, HttpStatus.INTERNAL_SERVER_ERROR,
                                                               environment.getProperty(
                                                                   "api-error-messages.internal_error"));
        }

    }

    @ApiOperation(
        tags = "Add Case for a PCQ", value = "Add case information on a PCQ answers record.",
        notes = "This API will be invoked by the Consolidation process to to update the case information ( case id )"
            + " on the PCQ answers record. "
    )
    @ApiResponses({
        @ApiResponse(code = 200, message = "Request executed successfully. Case Id successfully added to the PCQ "
            + "Answers record.", response = SubmitResponse.class),
        @ApiResponse(code = 400, message = "The supplied input parameters are not in the acceptable format. The user"
            + " will be returned a standard error message.", response = SubmitResponse.class),
        @ApiResponse(code = 500, message = "General/Un-recoverable error.", response = SubmitResponse.class)
    })
    @PutMapping(
        path = "/addCaseForPCQ/{pcqId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<SubmitResponse> addCaseForPcq(@RequestHeader HttpHeaders headers,
                                                        @PathVariable("pcqId") @NotBlank String pcqId,
                                                        @NotBlank String caseId) {

        try {
            return consolidationService.updateCaseId(headers.get(environment.getProperty(
                "api-required-header-keys.co-relationid")),pcqId, caseId);
        } catch (Exception e) {
            log.error("addCaseForPCQ API call failed due to error - {}", e.getMessage(), e);
            return ConversionUtil.generateSubmitResponseEntity(pcqId, HttpStatus.INTERNAL_SERVER_ERROR,
                                                         environment.getProperty("api-error-messages.internal_error"));
        }

    }

}
