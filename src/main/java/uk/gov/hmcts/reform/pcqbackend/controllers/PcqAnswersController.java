package uk.gov.hmcts.reform.pcqbackend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcq.commons.model.SubmitResponse;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
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
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PCQ BackEnd - API for PCQ database operations.", description = "This is the Protected Characteristics "
    + "Back-End API that will save user's answers to the database. "
    + "The API will be invoked by the PCQ front-end service.")
public class PcqAnswersController {

    private static final String OPT_OUT_FLAG = "Y";
    private static final String TRUE = "true";

    private final SubmitAnswersService submitAnswersService;

    private final DeleteService deleteService;

    private final Environment environment;

    @PostMapping(path = "/submitAnswers", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals"})
    @Operation(tags = "POST end-points", summary = "Add and update PCQ answers to the database.",
        description = "This API will create a new record in the database for the given PCQId where none exists "
            + "and will update an existing record with the answers as submitted by the users")

    @ApiResponse(
        responseCode = "200",
        description = "Operation completed successfully.",
        content = {@Content(schema = @Schema(implementation = SubmitResponse.class))})
    @ApiResponse(
        responseCode = "201", description = "Successfully saved to database.",
        content = {@Content(schema = @Schema(implementation = SubmitResponse.class))})
    @ApiResponse(
        responseCode = "202", description = "Request valid but stale.",
        content = {@Content(schema = @Schema(implementation = SubmitResponse.class))})
    @ApiResponse(
        responseCode = "400",
        description = "Request failed schema validation.",
        content = @Content)
    @ApiResponse(
        responseCode = "403",
        description = "Version number mismatch.",
        content = @Content)
    @ApiResponse(
        responseCode = "500",
        description = "General/Un-recoverable error.",
        content = @Content)
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

    @Operation(
        tags = "GET end-points", summary = "Get PCQ answer from the database.",
        description = "This API will return a record from the PCQ database for the given PCQId. "
            + "It is intended to be called from the test api for testing purposes."
    )

    @ApiResponse(
        responseCode = "200",
        description = "Details of the pcq answer record",
        content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PcqAnswerResponse.class))})
    @ApiResponse(
        responseCode = "400",
        description = "An invalid id was provided")
    @ApiResponse(
        responseCode = "404",
        description = "No pcq answer record was found with the given id")
    @GetMapping(
        path = "/getAnswer/{pcqId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
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

    @Operation(hidden = true,
        tags = "DELETE end-points", summary = "Delete PCQ Record from the database.",
        description = "This API will delete a record from the PCQ database for the given PCQId. "
            + "It is intended to be called from the test api for testing purposes."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Pcq record has been deleted")
    @ApiResponse(
        responseCode = "400",
        description = "An invalid id was provided")
    @ApiResponse(
        responseCode = "404",
        description = "No pcq answer record was found with the given id")
    @DeleteMapping(
        path = "/deletePcqRecord/{pcqId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> deletePcqRecord(@PathVariable("pcqId") @NotBlank String pcqId) {
        if (environment.getProperty("security.db.allow_delete_record") != null
            && TRUE.equals(environment.getProperty("security.db.allow_delete_record"))) {
            return deleteService.deletePcqRecord(pcqId);
        }
        return PcqUtils.generateResponseEntity(pcqId, HttpStatus.UNAUTHORIZED,
                                               environment.getProperty("api-error-messages.bad_request"));
    }
}
