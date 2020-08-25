package uk.gov.hmcts.reform.pcqbackend.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pcqbackend.model.SasTokenResponse;
import uk.gov.hmcts.reform.pcqbackend.security.AuthorisedServices;
import uk.gov.hmcts.reform.pcqbackend.service.AuthService;
import uk.gov.hmcts.reform.pcqbackend.service.SasTokenService;


/**
 * Controller for the Sas Token service API calls.
 */
@RestController
@RequestMapping(path = "/pcq/backend/token")
@Slf4j
@Api(tags = "PCQ BackEnd - API for SAS token service operations.", value = "This is the Protected Characteristics "
    + "Back-End API that will serve authentication tasks for other services and components to generate a Service SAS "
    + "token for the PCQ Storage Account container 'pcq' - this will provoide Write, List and Create access."
    + "The API will be invoked by the bulk-scan-processor service.")
public class SasTokenController {

    @Autowired
    private AuthService authService;

    @Autowired
    SasTokenService sasTokenService;

    @Autowired
    private AuthorisedServices authorisedServices;

    @ApiOperation(
        tags = "Get SAS Token for BulkScan", value = "Endpoint for BulkScan to generate the PCQ Storage SAS token.",
        notes = "This API will be invoked by the Bulk Scan Processor to generate an Azure Service SAS token to allow"
                + "the upload of paper PCQ envelops to the PCQ Blob Storage 'pcq' container."
    )
    @ApiResponses({
        @ApiResponse(
            code = 200, message = "Successfully generated Storage Account SAS token for BulkScan.",
            response = SasTokenResponse.class
        ),
        @ApiResponse(code = 401, message = "Request ServiceAuthorization token expired."),
        @ApiResponse(code = 403, message = "Request failed autentication."),
        @ApiResponse(code = 404, message = "Service not found."),
        @ApiResponse(code = 422, message = "The request was malformed.")
    })
    @GetMapping(
        path = "/bulkscan",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<SasTokenResponse> getBulkScanSasToken(
        @RequestHeader(name = "ServiceAuthorization", required = false) String serviceAuthHeader
    ) {
        String serviceName = authService.authenticate(serviceAuthHeader);

        log.info("ServiceAuthorization Service name: {}", serviceName);

        if (authorisedServices.hasService(serviceName)) {
            log.info("Service {} has been authorised!", serviceName);

            SasTokenResponse sasTokenResponse = new SasTokenResponse(sasTokenService.generateSasToken("bulkscan"));
            log.info("Generated SAS Token = {}", sasTokenResponse.getSasToken());
            return ResponseEntity.ok(sasTokenResponse);

        } else {
            log.info("Service {} has NOT been authorised!", serviceName);
        }

        return null;
    }

}
