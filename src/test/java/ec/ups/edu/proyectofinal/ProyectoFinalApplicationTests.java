package ec.ups.edu.proyectofinal;

import ec.ups.edu.proyectofinal.events.repository.EventRepository;
import ec.ups.edu.proyectofinal.events.repository.CategoryRepository;
import ec.ups.edu.proyectofinal.registrations.repository.RegistrationRepository;
import ec.ups.edu.proyectofinal.registrations.repository.SessionRepository;
import ec.ups.edu.proyectofinal.users.repository.RefreshTokenRepository;
import ec.ups.edu.proyectofinal.users.repository.RoleRepository;
import ec.ups.edu.proyectofinal.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"cors.allowed-origins=http://localhost:3000",
		"spring.autoconfigure.exclude="
				+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
				+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
				+ "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
				+ "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration,"
				+ "org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration"
})
class ProyectoFinalApplicationTests {

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private RoleRepository roleRepository;

	@MockitoBean
	private RefreshTokenRepository refreshTokenRepository;

	@MockitoBean
	private EventRepository eventRepository;

	@MockitoBean
	private CategoryRepository categoryRepository;

	@MockitoBean
	private RegistrationRepository registrationRepository;

	@MockitoBean
	private SessionRepository sessionRepository;

	@MockitoBean
	private StringRedisTemplate stringRedisTemplate;

	@Test
	void contextLoads() {
	}

}
