package ec.ups.edu.proyectofinal.config;

import ec.ups.edu.proyectofinal.exception.RateLimitExceededException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitingService {

    private final StringRedisTemplate redisTemplate;

    public RateLimitingService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkRateLimit(String key, int limit, long windowInSeconds, String operationName) {
        try {
            // La rúbrica pide incremento atómico
            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1) {
                // Si es la primera solicitud, le ponemos el Tiempo de Vida (TTL)
                redisTemplate.expire(key, Duration.ofSeconds(windowInSeconds));
            }

            if (count != null && count > limit) {
                Long expire = redisTemplate.getExpire(key);
                long retryAfter = (expire != null && expire > 0) ? expire : windowInSeconds;
                throw new RateLimitExceededException("Demasiadas solicitudes para la operación: " + operationName, retryAfter);
            }
        } catch (DataAccessException e) {
            // Keep the API usable if Redis is unavailable in local development.
        }
    }
}
