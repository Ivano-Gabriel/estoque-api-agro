package com.lojaagro.estoque_api.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelogioConfig {

    @Bean
    public Clock businessClock(
            @Value("${app.business-time-zone:America/Maceio}") String timeZone) {
        try {
            return Clock.system(ZoneId.of(timeZone));
        } catch (Exception exception) {
            throw new IllegalStateException("BUSINESS_TIME_ZONE invalido: " + timeZone, exception);
        }
    }
}
