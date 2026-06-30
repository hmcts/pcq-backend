package uk.gov.hmcts.reform.pcqbackend;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.times;

class ApplicationTest {

    @Test
    void testMainMethod() {
        try (MockedStatic<SpringApplication> mockedStatic = Mockito.mockStatic(SpringApplication.class)) {
            mockedStatic.when(() -> SpringApplication.run(Application.class)).thenReturn(null);
            Application.main(new String[]{});
            mockedStatic.verify(() -> SpringApplication.run(Application.class), times(1));
        }
    }
}
