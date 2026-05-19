package com.bractus.notesservice.config;

import org.springframework.context.annotation.Configuration;

/**
 * AppConfig holds application-level configuration for the Notes microservice.
 *
 * Currently serves as a placeholder for future configuration beans
 * (e.g. CORS settings, custom serializers, connection pool tuning).
 *
 * Keeping this class here follows the standard layered folder structure:
 *   controller / service / repository / model / dto / config / exception
 */
@Configuration
public class AppConfig {
    // Future beans go here — e.g. Jackson ObjectMapper customization,
    // CORS configuration, or MongoDB auditing setup.
}
