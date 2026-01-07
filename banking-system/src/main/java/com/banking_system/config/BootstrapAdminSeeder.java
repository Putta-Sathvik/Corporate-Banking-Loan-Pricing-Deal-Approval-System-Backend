package com.banking_system.config;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.banking_system.model.User;
import com.banking_system.model.UserRole;
import com.banking_system.repository.UserRepository;

@Component
public class BootstrapAdminSeeder implements ApplicationRunner {
	private static final Logger log = LoggerFactory.getLogger(BootstrapAdminSeeder.class);

	private final BootstrapAdminProperties props;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public BootstrapAdminSeeder(
			BootstrapAdminProperties props,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.props = props;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!props.enabled()) {
			return;
		}
		if (!StringUtils.hasText(props.email()) || !StringUtils.hasText(props.password())) {
			log.warn("Bootstrap admin enabled but email/password not set; skipping");
			return;
		}

		String email = props.email().trim().toLowerCase(java.util.Locale.ROOT);
		if (userRepository.existsByEmail(email)) {
			log.info("Bootstrap admin already exists: {}", email);
			return;
		}

		Instant now = Instant.now();
		User admin = new User(
				null,
				email,
				passwordEncoder.encode(props.password()),
				UserRole.ADMIN,
				true,
				now,
				now);
		userRepository.save(admin);
		log.info("Bootstrapped ADMIN user: {}", email);
	}
}
