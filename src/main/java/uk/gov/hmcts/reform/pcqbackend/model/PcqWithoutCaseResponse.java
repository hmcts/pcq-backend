package uk.gov.hmcts.reform.pcqbackend.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This is the object representing the REST response for the PCQWithoutACase API.
 */
public class PcqWithoutCaseResponse implements Serializable {

    public static final long serialVersionUID = 432973322;

    private Integer[] pcqId;

    private String responseStatus;

    private String responseStatusCode;

    public Integer[] getPcqId() {
        return Arrays.copyOf(pcqId, pcqId.length);
    }

    @SuppressWarnings("PMD.UseVarargs")
    public void setPcqId(Integer[] pcqId) {
        if (pcqId == null) {
            this.pcqId = new Integer[0];
        } else {
            this.pcqId = Arrays.copyOf(pcqId, pcqId.length);
        }
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(String responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }
}
