package ec.ups.edu.proyectofinal.registrations.service;

import ec.ups.edu.proyectofinal.events.entity.Event;
import ec.ups.edu.proyectofinal.events.repository.EventRepository;
import ec.ups.edu.proyectofinal.registrations.dto.RegistrationResponse;
import ec.ups.edu.proyectofinal.registrations.entity.Registration;
import ec.ups.edu.proyectofinal.registrations.repository.RegistrationRepository;
import ec.ups.edu.proyectofinal.users.entity.User;
import ec.ups.edu.proyectofinal.users.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@Service
public class RegistrationService {

    private static final Set<String> MANAGED_STATUSES = Set.of("CONFIRMED", "REJECTED", "CANCELLED");

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RegistrationService(
            RegistrationRepository registrationRepository,
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('PARTICIPANT') and #userEmail == authentication.name")
    public RegistrationResponse registerToEvent(Long eventId, String userEmail) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        User participant = userRepository.findByEmail(normalizeEmail(userEmail))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validateCanRegister(event);

        if (registrationRepository.existsByParticipantAndEvent(participant, event)) {
            throw new RuntimeException("El participante ya se encuentra inscrito en este evento");
        }

        Instant now = Instant.now();
        event.setAvailableCapacity(event.getAvailableCapacity() - 1);
        eventRepository.save(event);

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setParticipant(participant);
        registration.setStatus("CONFIRMED");
        registration.setRegisteredAt(now);
        registration.setStatusUpdatedAt(now);
        registration.setConfirmedAt(now);

        return RegistrationResponse.from(registrationRepository.save(registration));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PARTICIPANT') and #userEmail == authentication.name")
    public Page<RegistrationResponse> listMine(String userEmail, Pageable pageable) {
        return registrationRepository.findByParticipant_EmailOrderByRegisteredAtDesc(normalizeEmail(userEmail), pageable)
                .map(RegistrationResponse::from);
    }

    @Transactional
    @PreAuthorize("hasRole('PARTICIPANT') and @resourceAuthorizationService.isRegistrationParticipant(#registrationId, authentication.name)")
    public RegistrationResponse cancelMine(Long registrationId, String userEmail) {
        Registration registration = registrationRepository.findByIdAndParticipant_Email(registrationId, normalizeEmail(userEmail))
                .orElseThrow(() -> new RuntimeException("Inscripcion no encontrada"));

        if ("CANCELLED".equals(registration.getStatus())) {
            throw new RuntimeException("La inscripcion ya se encuentra cancelada");
        }

        Instant now = Instant.now();
        if ("CONFIRMED".equals(registration.getStatus())) {
            Event event = eventRepository.findByIdForUpdate(registration.getEvent().getId())
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
            event.setAvailableCapacity(event.getAvailableCapacity() + 1);
            eventRepository.save(event);
            registration.setEvent(event);
        }

        registration.setStatus("CANCELLED");
        registration.setCancelledAt(now);
        registration.setStatusUpdatedAt(now);

        return RegistrationResponse.from(registrationRepository.save(registration));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @resourceAuthorizationService.isEventOrganizer(#eventId, authentication.name))")
    public Page<RegistrationResponse> listForEvent(Long eventId, Pageable pageable) {
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Evento no encontrado");
        }
        return registrationRepository.findByEvent_Id(eventId, pageable)
                .map(RegistrationResponse::from);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @resourceAuthorizationService.isEventOrganizer(#eventId, authentication.name))")
    public RegistrationResponse updateStatus(Long eventId, Long registrationId, String status) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Inscripcion no encontrada"));

        if (!registration.getEvent().getId().equals(eventId)) {
            throw new RuntimeException("La inscripcion no pertenece al evento indicado");
        }

        String newStatus = normalizeManagedStatus(status);
        String currentStatus = registration.getStatus();
        if (newStatus.equals(currentStatus)) {
            return RegistrationResponse.from(registration);
        }

        Instant now = Instant.now();
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if ("CONFIRMED".equals(newStatus) && !"CONFIRMED".equals(currentStatus)) {
            if (event.getAvailableCapacity() <= 0) {
                throw new RuntimeException("No existen cupos disponibles para confirmar la inscripcion");
            }
            event.setAvailableCapacity(event.getAvailableCapacity() - 1);
            registration.setConfirmedAt(now);
            registration.setCancelledAt(null);
        } else if (!"CONFIRMED".equals(newStatus) && "CONFIRMED".equals(currentStatus)) {
            event.setAvailableCapacity(event.getAvailableCapacity() + 1);
            registration.setConfirmedAt(null);
        }

        if ("CANCELLED".equals(newStatus)) {
            registration.setCancelledAt(now);
        } else if (!"CONFIRMED".equals(newStatus)) {
            registration.setCancelledAt(null);
        }

        registration.setEvent(event);
        registration.setStatus(newStatus);
        registration.setStatusUpdatedAt(now);
        eventRepository.save(event);

        return RegistrationResponse.from(registrationRepository.save(registration));
    }

    private void validateCanRegister(Event event) {
        Instant now = Instant.now();
        if (!"PUBLISHED".equals(event.getStatus())) {
            throw new RuntimeException("Solo se permiten inscripciones en eventos publicados");
        }
        if (now.isBefore(event.getRegistrationStartAt()) || now.isAfter(event.getRegistrationEndAt())) {
            throw new RuntimeException("El periodo de inscripcion no se encuentra abierto");
        }
        if (event.getAvailableCapacity() <= 0) {
            throw new RuntimeException("No se puede inscribir: el evento no tiene cupos disponibles");
        }
    }

    private String normalizeManagedStatus(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!MANAGED_STATUSES.contains(normalized)) {
            throw new RuntimeException("Estado de inscripcion invalido");
        }
        return normalized;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
