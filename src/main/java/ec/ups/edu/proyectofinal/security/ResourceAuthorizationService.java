package ec.ups.edu.proyectofinal.security;

import ec.ups.edu.proyectofinal.events.repository.EventRepository;
import ec.ups.edu.proyectofinal.registrations.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ResourceAuthorizationService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public ResourceAuthorizationService(EventRepository eventRepository, RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    public boolean isEventOrganizer(Long eventId, String email) {
        return eventId != null
                && email != null
                && eventRepository.existsByIdAndOrganizer_Email(eventId, normalizeEmail(email));
    }

    public boolean isRegistrationParticipant(Long registrationId, String email) {
        return registrationId != null
                && email != null
                && registrationRepository.existsByIdAndParticipant_Email(registrationId, normalizeEmail(email));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
