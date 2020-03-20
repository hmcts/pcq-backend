package uk.gov.hmcts.reform.pcqbackend.model;

import java.io.Serializable;

/**
 * This is the object representing the REST request for the SubmitAnswers API.
 */
public class PcqAnswerRequest implements Serializable {

    public static final long serialVersionUID = 4328743L;

    private String pcqId;

    private String caseId;

    private String partyId;

    private int channel;

    private String completedDate;

    private int serviceId;

    private int actor;

    private int versionNo;

    private PcqAnswers pcqAnswers;

    public PcqAnswerRequest() {
        // Intentionally left blank.
    }

    public PcqAnswerRequest(String pcqId) {

        this.pcqId = pcqId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getActor() {
        return actor;
    }

    public void setActor(int actor) {
        this.actor = actor;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    public PcqAnswers getPcqAnswers() {
        return pcqAnswers;
    }

    public void setPcqAnswers(PcqAnswers pcqAnswers) {
        this.pcqAnswers = pcqAnswers;
    }

    public String getPcqId() {
        return pcqId;
    }

    public void setPcqId(String pcqId) {
        this.pcqId = pcqId;
    }
}
