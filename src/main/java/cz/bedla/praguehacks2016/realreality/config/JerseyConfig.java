package cz.bedla.praguehacks2016.realreality.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import cz.bedla.praguehacks2016.realreality.endpoint.Rest;

@Component
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(Rest.class);
    }
}
