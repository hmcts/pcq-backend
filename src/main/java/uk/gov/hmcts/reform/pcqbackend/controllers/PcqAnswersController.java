package uk.gov.hmcts.reform.pcqbackend.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcq.commons.model.SubmitResponse;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.exceptions.DataNotFoundException;
import uk.gov.hmcts.reform.pcqbackend.service.DeleteService;
import uk.gov.hmcts.reform.pcqbackend.service.SubmitAnswersService;
import uk.gov.hmcts.reform.pcqbackend.utils.ConversionUtil;


/**
 * Controller for the PCQ Back-End API calls.
 */
@RestController
@RequestMapping(path = "/pcq/backend")
@AllArgsConstructor
@Slf4j
@Api(tags = "PCQ BackEnd - API for PCQ database operations.", value = "This is the Protected Characteristics "
    + "Back-End API that will save user's answers to the database. "
    + "The API will be invoked by the PCQ front-end service.")
public class PcqAnswersController {

    private static final String OPT_OUT_FLAG = "Y";
    private static final String TRUE = "true";

    @Autowired
    private SubmitAnswersService submitAnswersService;

    @Autowired
    private DeleteService deleteService;

    @Autowired
    private Environment environment;

    @PostMapping(path = "/submitAnswers", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals"})
    @ApiOperation(tags = "POST end-points", value = "Add and update PCQ answers to the database.",
        notes = "This API will create a new record in the database for the given PCQId where none exists "
        + "and will update an existing record with the answers as submitted by the users")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Operation completed successfully.", response = SubmitResponse.class),
        @ApiResponse(code = 201, message = "Successfully saved to database.", response = SubmitResponse.class),
        @ApiResponse(code = 202, message = "Request valid but stale.", response = SubmitResponse.class),
        @ApiResponse(code = 400, message = "Request failed schema validation.", response = SubmitResponse.class),
        @ApiResponse(code = 403, message = "Version number mismatch.", response = SubmitResponse.class),
        @ApiResponse(code = 500, message = "General/Un-recoverable error.", response = SubmitResponse.class)
    })
    @ResponseBody
    public ResponseEntity<Object> submitAnswers(@RequestHeader HttpHeaders headers,
                                                @RequestBody PcqAnswerRequest answerRequest) {

        try {
            if (answerRequest.getOptOut() != null && OPT_OUT_FLAG.equals(answerRequest.getOptOut())) {
                return submitAnswersService.processOptOut(headers.get(
                    environment.getProperty("api-required-header-keys.co-relationid")), answerRequest);
            }
            return submitAnswersService.processPcqAnswers(headers.get(
                environment.getProperty("api-required-header-keys.co-relationid")), answerRequest);
        } catch (Exception e) {
            log.error("submitAnswers API call failed due to error - {}", e.getMessage(), e);
            return PcqUtils.generateResponseEntity(answerRequest.getPcqId(), HttpStatus.INTERNAL_SERVER_ERROR,
                                                   environment.getProperty("api-error-messages.internal_error"));
        }

    }

    @ApiOperation(
        tags = "GET end-points", value = "Get PCQ answer from the database.",
        notes = "This API will return a record from the PCQ database for the given PCQId. "
            + "It is intended to be called from the test api for testing purposes."
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Details of the pcq answer record",
            response = PcqAnswerResponse.class
        ),
        @ApiResponse(
            code = 400,
            message = "An invalid id was provided"
        ),
        @ApiResponse(
            code = 404,
            message = "No pcq answer record was found with the given id"
        )
    })
    @GetMapping(
        path = "/getAnswer/{pcqId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<PcqAnswerResponse> getAnswersByPcqId(@PathVariable("pcqId") @NotBlank String pcqId) {

        ProtectedCharacteristics protectedCharacteristics = submitAnswersService
            .getProtectedCharacteristicsById(pcqId);

        if (protectedCharacteristics == null) {
            throw new DataNotFoundException();
        }

        return ResponseEntity
            .status(200)
            .body(ConversionUtil.getPcqResponseFromDomain(protectedCharacteristics));

    }

    @ApiIgnore
    @ApiOperation(
        tags = "DELETE end-points", value = "Delete PCQ Record from the database.",
        notes = "This API will delete a record from the PCQ database for the given PCQId. "
            + "It is intended to be called from the test api for testing purposes."
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Pcq record has been deleted"
        ),
        @ApiResponse(
            code = 400,
            message = "An invalid id was provided"
        ),
        @ApiResponse(
            code = 404,
            message = "No pcq answer record was found with the given id"
        )
    })
    @DeleteMapping(
        path = "/deletePcqRecord/{pcqId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<Object> deletePcqRecord(@PathVariable("pcqId") @NotBlank String pcqId) {
        if (environment.getProperty("security.db.allow_delete_record") != null
            && TRUE.equals(environment.getProperty("security.db.allow_delete_record"))) {
            return deleteService.deletePcqRecord(pcqId);
        }
        return PcqUtils.generateResponseEntity(pcqId, HttpStatus.UNAUTHORIZED,
                                               environment.getProperty("api-error-messages.bad_request"));
    }
}
