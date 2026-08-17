package com.athena.config;

import com.athena.common.aspect.DictSerializerModifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class DictModule {

    @Bean
    public JacksonModule dictJacksonModule() {
        SimpleModule module = new SimpleModule("DictModule");
        module.setSerializerModifier(new DictSerializerModifier());
        return module;
    }
}