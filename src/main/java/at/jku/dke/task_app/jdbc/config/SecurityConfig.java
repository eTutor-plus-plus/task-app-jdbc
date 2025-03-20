package at.jku.dke.task_app.jdbc.config;

import at.jku.dke.etutor.task_app.config.BaseSecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * The application security configuration.
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {
    /**
     * Creates a new instance of class {@link SecurityConfig}.
     */
    public SecurityConfig() {
    }
}
