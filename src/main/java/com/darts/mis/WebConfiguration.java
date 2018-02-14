package com.darts.mis;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ServletComponentScan("com.darts.mis.web")
public class WebConfiguration {
}
