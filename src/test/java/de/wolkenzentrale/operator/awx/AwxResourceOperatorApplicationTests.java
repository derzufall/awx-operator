package de.wolkenzentrale.operator.awx;

import de.wolkenzentrale.operator.awx.config.TestKubernetesConfig;
import de.wolkenzentrale.operator.awx.config.TestOpenTelemetryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Explicitly activate test profile to exclude production configurations
@Import({TestOpenTelemetryConfig.class, TestKubernetesConfig.class})
class AwxResourceOperatorApplicationTests {

    @Test
    void contextLoads() {
        // This test will fail if the application context cannot be loaded
        // 🎉 With proper test configurations and test profile, this should now pass!
    }
} 