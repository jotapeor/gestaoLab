package com.main.gestaolabback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {
    @Bean
    public Clock clock(AppConfig config) {
        return Clock.system(ZoneId.of(config.getFusoHorario()));
    }
}
