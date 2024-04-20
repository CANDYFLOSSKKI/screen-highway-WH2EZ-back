package com.wut.screenwebsx.Config;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

@Configuration
public class DockingInterfaceConfig {
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @Inherited
    @Documented
    public @interface Docking {
        String value() default "";
    }
}
