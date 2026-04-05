package com.viettelsoftware.intern_management;

import com.viettelsoftware.intern_management.shared.generation.domain.ExcludeFromGeneratedCodeCoverage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.viettelsoftware", exclude = {UserDetailsServiceAutoConfiguration.class})
@ExcludeFromGeneratedCodeCoverage(reason = "Not testing logs")
public class InternManagementApplication {

	public static void main(String[] args) {
		Environment env = SpringApplication.run(InternManagementApplication.class, args).getEnvironment();

		if (log.isInfoEnabled()) {
			log.info(ApplicationStartupTraces.of(env));
		}
	}

}
