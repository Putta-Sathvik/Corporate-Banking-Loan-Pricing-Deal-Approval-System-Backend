package com.banking_system.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.banking_system.repository.UserRepository;

class BootstrapAdminSeederTest {

	@Test
	void doesNothingWhenDisabled() {
		BootstrapAdminProperties props = new BootstrapAdminProperties(false, "admin@bank.com", "secret");
		UserRepository repo = org.mockito.Mockito.mock(UserRepository.class);
		PasswordEncoder encoder = org.mockito.Mockito.mock(PasswordEncoder.class);
		BootstrapAdminSeeder seeder = new BootstrapAdminSeeder(props, repo, encoder);

		seeder.run(org.mockito.Mockito.mock(ApplicationArguments.class));

		verify(repo, never()).save(any());
	}

	@Test
	void createsAdminWhenEnabledAndMissing() {
		BootstrapAdminProperties props = new BootstrapAdminProperties(true, "admin@bank.com", "secret");
		UserRepository repo = org.mockito.Mockito.mock(UserRepository.class);
		PasswordEncoder encoder = org.mockito.Mockito.mock(PasswordEncoder.class);
		when(repo.existsByEmail("admin@bank.com")).thenReturn(false);
		when(encoder.encode("secret")).thenReturn("hashed");
		BootstrapAdminSeeder seeder = new BootstrapAdminSeeder(props, repo, encoder);

		seeder.run(org.mockito.Mockito.mock(ApplicationArguments.class));

		verify(repo).save(any());
	}
}
