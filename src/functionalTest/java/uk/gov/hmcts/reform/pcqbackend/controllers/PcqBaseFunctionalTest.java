package uk.gov.hmcts.reform.pcqbackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.pcqbackend.client.PcqBackEndServiceClient;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@ComponentScan("uk.gov.hmcts.reform.pcqbackend")
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
@SuppressWarnings({"PMD.AbstractClassWithoutAnyMethod", "PMD.AbstractClassWithoutAbstractMethod"})
public abstract class PcqBaseFunctionalTest {

    @Value("${targetInstance}")
    protected String pcqBackEndApiUrl;

    protected PcqBackEndServiceClient pcqBackEndServiceClient;

    protected RequestSpecification bearerToken;

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.defaultParser = Parser.JSON;

        /* SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);*/

        pcqBackEndServiceClient = new PcqBackEndServiceClient(pcqBackEndApiUrl);
    }


    /**
     * Obtains a JSON String from a JSON file in the classpath (Resources directory).
     * @param fileName - The name of the Json file from classpath.
     * @return - JSON String from the file.
     * @throws IOException - If there is any issue when reading from the file.
     */
    protected String jsonStringFromFile(String fileName) throws IOException {
        File resource = new ClassPathResource(fileName).getFile();
        return new String(Files.readAllBytes(resource.toPath()));
    }

    protected PcqAnswerRequest jsonObjectFromString(String jsonString) throws IOException {
        return new ObjectMapper().readValue(jsonString, PcqAnswerRequest.class);
    }

    protected void checkAssertionsOnResponse(ProtectedCharacteristics protectedCharacteristics,
                                                 PcqAnswerRequest answerRequest) {
        assertEquals("PCQId not matching", protectedCharacteristics.getPcqId(), answerRequest.getPcqId());
        assertEquals("CaseId not matching", protectedCharacteristics.getCaseId(),
                     answerRequest.getCaseId());
        assertEquals("PartyId not matching", protectedCharacteristics.getPartyId(),
                     answerRequest.getPartyId());
        assertEquals("Channel not matching", protectedCharacteristics.getChannel().intValue(),
                     answerRequest.getChannel());
        assertEquals("ServiceId not matching", protectedCharacteristics.getServiceId(),
                     answerRequest.getServiceId());
        assertEquals("Actor not matching", protectedCharacteristics.getActor(),
                     answerRequest.getActor());
        assertEquals("VersionNumber not matching", protectedCharacteristics.getVersionNumber().intValue(),
                     answerRequest.getVersionNo());
        assertEquals("DobProvided not matching", protectedCharacteristics.getDobProvided(),
                     answerRequest.getPcqAnswers().getDobProvided());

    }

    protected String generateUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}
