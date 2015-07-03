package ru.stuff.cfg;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * Created by mark on 20.01.15.
 */
public class SecurityInit extends AbstractSecurityWebApplicationInitializer {
    public SecurityInit() {
        super(AppWebSecurityConfig.class);
    }
}
