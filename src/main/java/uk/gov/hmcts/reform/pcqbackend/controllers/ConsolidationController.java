package uk.gov.hmcts.reform.pcqbackend.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.pcqbackend.model.PcqWithoutCaseResponse;


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
    public ResponseEntity<PcqWithoutCaseResponse> getAnswersByPcqId() {

        return ResponseEntity
            .status(200)
            .body(new PcqWithoutCaseResponse());
            //.body(ConversionUtil.getPcqResponseFromDomain(protectedCharacteristics));

    }

}
