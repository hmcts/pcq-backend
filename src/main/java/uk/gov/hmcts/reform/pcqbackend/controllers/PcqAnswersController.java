package uk.gov.hmcts.reform.pcqbackend.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.service.SubmitAnswersService;



/**
 * Controller for the PCQ Back-End API calls.
 */
@RestController
@RequestMapping(path = "/pcq/backend")
@Slf4j
@Api(tags = "PCQ BackEnd - API for PCQ database operations.", value = "This is the Protected Characterstics "
    + "Back-End API that will save user's answers to the database, fetch PCQ Ids that don't have an associated "
    + "case record and add case information to a PCQ record in the database. "
    + "The API will be invoked by two components - PCQ front-end and the Consolidation service.")
public class PcqAnswersController {


    @Autowired
    private SubmitAnswersService submitAnswersService;

    @Autowired
    private Environment environment;

    @PostMapping(path = "/submitAnswers", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals"})
    @ApiOperation(tags = "Save PCQ answers", value = "Add and update PCQ answers to the database.",
        notes = "This API will create a new record in the database for the given PCQId where none exists "
        + "and will update an existing record with the answers as submitted by the users")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Successfully saved to database."),
        @ApiResponse(code = 202, message = "Request valid but stale."),
        @ApiResponse(code = 400, message = "Request failed schema validation."),
        @ApiResponse(code = 403, message = "Version number mismatch."),
        @ApiResponse(code = 500, message = "General/Un-recoverable error."),
        @ApiResponse(code = 503, message = "Database down/un-available.")
    })
    @ResponseBody
    public ResponseEntity<Object> submitAnswers(@RequestHeader HttpHeaders headers,
                                                @RequestBody PcqAnswerRequest answerRequest) {

        return submitAnswersService.processPcqAnswers(headers.get(environment.getProperty(
            "api-required-header-keys.co-relationid")), answerRequest);

    }




}
