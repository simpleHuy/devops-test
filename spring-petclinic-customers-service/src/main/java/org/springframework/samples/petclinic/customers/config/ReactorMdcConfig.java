package org.springframework.samples.petclinic.customers.config;
import reactor.core.publisher.Hooks;

import jakarta.annotation.PostConstruct;

public class ReactorMdcConfig {
    @PostConstruct
        public void enableReactorMdc() {
            Hooks.enableAutomaticContextPropagation();
        }
}
