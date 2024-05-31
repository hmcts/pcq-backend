package uk.gov.hmcts.reform.pcqbackend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pcq.commons.model.SasTokenResponse;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidAuthenticationException;
import uk.gov.hmcts.reform.pcqbackend.security.AuthorisedServices;
import uk.gov.hmcts.reform.pcqbackend.service.AuthService;
import uk.gov.hmcts.reform.pcqbackend.service.SasTokenService;


/**
 * Controller for the Sas Token service API calls.
 */
@RestController
@RequestMapping(path = "/pcq/backend/token")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "PCQ BackEnd - API for SAS token service operations.",
    description = "This is the Protected Characteristics "
    + "Back-End API that will serve authentication tasks for other services and components to generate a Service SAS "
    + "token for the PCQ Storage Account container 'pcq' - this will provoide Write, List and Create access."
    + "The API will be invoked by the bulk-scan-processor service.")
public class SasTokenController {

    private final AuthService authService;

    private final SasTokenService sasTokenService;

    private final AuthorisedServices authorisedServices;

    @Operation(
        tags = "GET end-points",
        summary = "Endpoint for BulkScan to generate the PCQ Storage SAS token.",
        description = "This API will be invoked by the Bulk Scan Processor to "
                + "generate an Azure Service SAS token to allow"
                + "the upload of paper PCQ envelops to the PCQ Blob Storage 'pcq' container."
    )
    @ApiResponse(
        responseCode = "200", description = "Successfully generated Storage Account SAS token for BulkScan.",
        content = { @Content(schema = @Schema(implementation = SasTokenResponse.class))})
    @ApiResponse(responseCode = "401", description = "ServiceAuthorization header invalid or expired.")
    @ApiResponse(responseCode = "404", description = "Service or path not found.")
    @ApiResponse(responseCode = "500", description = "Server error, unable to generate SAS token.")
    @GetMapping(
        path = "/bulkscan",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SasTokenResponse> generateBulkScanSasToken(
        @RequestHeader(name = "ServiceAuthorization", required = false) String serviceAuthHeader
    ) {
        String serviceName = authService.authenticate(serviceAuthHeader);
        if (!authorisedServices.hasService(serviceName)) {
            log.info("Service {} has NOT been authorised!", serviceName);
            throw new InvalidAuthenticationException("Unable to authenticate service request.");
        }
        SasTokenResponse sasTokenResponse = new SasTokenResponse(sasTokenService.generateSasToken("bulkscan"));
        return ResponseEntity.ok(sasTokenResponse);
    }

}
