package com.banking_system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record BootstrapAdminProperties(
		boolean enabled,
		String email,
		String password
) {
}
