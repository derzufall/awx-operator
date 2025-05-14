package de.wolkenzentrale.operator.awx.interfaces.client;

import de.wolkenzentrale.operator.awx.interfaces.awx.client.AwxClient;
import de.wolkenzentrale.operator.awx.interfaces.dto.ProjectListResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

@SpringBootTest
@ActiveProfiles("test")
class AwxClientIntegrationTest {

    @MockBean
    private AwxClient awxClient;

    @Test
    void contextLoads() {
        // Setup mock response
        ProjectListResponse mockResponse = new ProjectListResponse();
        mockResponse.setCount(0);
        mockResponse.setResults(Collections.emptyList());
        
        Mockito.when(awxClient.listProjects()).thenReturn(Mono.just(mockResponse));
        
        // Verify the client is properly wired and returns expected response
        StepVerifier.create(awxClient.listProjects())
                .expectNextMatches(response -> 
                    response != null && 
                    response.getResults() != null &&
                    response.getCount() == 0)
                .verifyComplete();
    }
} 