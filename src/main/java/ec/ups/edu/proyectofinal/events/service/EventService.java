package ec.ups.edu.proyectofinal.events.service;

import ec.ups.edu.proyectofinal.events.dto.EventRequest;
import ec.ups.edu.proyectofinal.events.dto.EventResponse;
import ec.ups.edu.proyectofinal.events.entity.Category;
import ec.ups.edu.proyectofinal.events.entity.Event;
import ec.ups.edu.proyectofinal.events.repository.CategoryRepository;
import ec.ups.edu.proyectofinal.events.repository.EventRepository;
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
public class EventService {

    private static final Set<String> MODALITIES = Set.of("PRESENTIAL", "VIRTUAL", "HYBRID");
    private static final Set<String> STATUSES = Set.of("DRAFT", "PUBLISHED", "FINISHED", "CANCELLED");

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public EventService(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> listEvents(String status, Long categoryId, String query, Pageable pageable) {
        return eventRepository.search(normalizeOptional(status), categoryId, normalizeSearchQuery(query), pageable)
                .map(EventResponse::from);
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Long eventId) {
        Event event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        return EventResponse.from(event);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public EventResponse createEvent(EventRequest request, String organizerEmail) {
        validateEventRequest(request);

        User organizer = userRepository.findByEmail(normalizeEmail(organizerEmail))
                .orElseThrow(() -> new RuntimeException("Organizador no encontrado"));
        Category category = categoryRepository.findByIdAndActiveTrue(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada o inactiva"));

        Instant now = Instant.now();
        Event event = new Event();
        applyRequest(event, request, category);
        event.setOrganizer(organizer);
        event.setStatus("DRAFT");
        event.setDeleted(false);
        event.setAvailableCapacity(request.capacity());
        event.setCreatedAt(now);
        event.setUpdatedAt(now);

        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @resourceAuthorizationService.isEventOrganizer(#eventId, authentication.name))")
    public EventResponse updateEvent(Long eventId, EventRequest request) {
        validateEventRequest(request);

        Event event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        Category category = categoryRepository.findByIdAndActiveTrue(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada o inactiva"));

        int usedCapacity = event.getCapacity() - event.getAvailableCapacity();
        if (request.capacity() < usedCapacity) {
            throw new RuntimeException("La capacidad no puede ser menor que las inscripciones confirmadas");
        }

        applyRequest(event, request, category);
        event.setAvailableCapacity(request.capacity() - usedCapacity);
        event.setUpdatedAt(Instant.now());

        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @resourceAuthorizationService.isEventOrganizer(#eventId, authentication.name))")
    public EventResponse updateStatus(Long eventId, String status) {
        Event event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        event.setStatus(normalizeStatus(status));
        event.setUpdatedAt(Instant.now());
        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @resourceAuthorizationService.isEventOrganizer(#eventId, authentication.name))")
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        boolean hasRegistrations = registrationRepository.existsByEvent(event);
        if ("PUBLISHED".equals(event.getStatus()) && hasRegistrations) {
            event.setDeleted(true);
            event.setUpdatedAt(Instant.now());
            eventRepository.save(event);
        } else {
            eventRepository.delete(event);
        }
    }

    private void applyRequest(Event event, EventRequest request, Category category) {
        event.setTitle(request.title().trim());
        event.setDescription(request.description().trim());
        event.setModality(normalizeModality(request.modality()));
        event.setLocation(blankToNull(request.location()));
        event.setVirtualUrl(blankToNull(request.virtualUrl()));
        event.setCapacity(request.capacity());
        event.setRegistrationStartAt(request.registrationStartAt());
        event.setRegistrationEndAt(request.registrationEndAt());
        event.setStartAt(request.startAt());
        event.setEndAt(request.endAt());
        event.setCategory(category);
    }

    private void validateEventRequest(EventRequest request) {
        String modality = normalizeModality(request.modality());
        if (!MODALITIES.contains(modality)) {
            throw new RuntimeException("Modalidad invalida");
        }

        boolean hasLocation = blankToNull(request.location()) != null;
        boolean hasVirtualUrl = blankToNull(request.virtualUrl()) != null;
        if ("PRESENTIAL".equals(modality) && (!hasLocation || hasVirtualUrl)) {
            throw new RuntimeException("Un evento presencial requiere ubicacion y no debe tener URL virtual");
        }
        if ("VIRTUAL".equals(modality) && (hasLocation || !hasVirtualUrl)) {
            throw new RuntimeException("Un evento virtual requiere URL virtual y no debe tener ubicacion");
        }
        if ("HYBRID".equals(modality) && (!hasLocation || !hasVirtualUrl)) {
            throw new RuntimeException("Un evento hibrido requiere ubicacion y URL virtual");
        }

        if (!request.registrationStartAt().isBefore(request.registrationEndAt())
                || request.registrationEndAt().isAfter(request.startAt())
                || !request.startAt().isBefore(request.endAt())) {
            throw new RuntimeException("Las fechas del evento no tienen un orden valido");
        }
    }

    private String normalizeStatus(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) {
            throw new RuntimeException("Estado de evento invalido");
        }
        return normalized;
    }

    private String normalizeModality(String modality) {
        return modality.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeSearchQuery(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? "" : normalized;
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
