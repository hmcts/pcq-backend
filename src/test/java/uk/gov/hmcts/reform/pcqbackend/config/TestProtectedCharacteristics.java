package uk.gov.hmcts.reform.pcqbackend.config;


import org.hibernate.annotations.ColumnTransformer;

@SuppressWarnings("PMD.UnusedPrivateField")
public class TestProtectedCharacteristics {

    @ColumnTransformer(
        read =  "test(decode(party_id, 'base64'), '${encryption.key}')",
        write = "encode(test(?, '${encryption.key}'), 'base64')"
    )
    private String partyId;

}
