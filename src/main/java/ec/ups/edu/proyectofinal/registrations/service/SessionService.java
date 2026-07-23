package ec.ups.edu.proyectofinal.registrations.service;

import ec.ups.edu.proyectofinal.events.entity.Event;
import ec.ups.edu.proyectofinal.events.repository.EventRepository;
import ec.ups.edu.proyectofinal.registrations.dto.SessionRequest;
import ec.ups.edu.proyectofinal.registrations.dto.SessionResponse;
import ec.ups.edu.proyectofinal.registrations.entity.Session;
import ec.ups.edu.proyectofinal.registrations.repository.SessionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final EventRepository eventRepository;

    public SessionService(SessionRepository sessionRepository, EventRepository eventRepository) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Evento no encontrado");
        }
        return sessionRepository.findByEvent_IdOrderByStartAtAsc(eventId).stream()
                .map(SessionResponse::from)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @resourceAuthorizationService.isEventOrganizer(#eventId, authentication.name))")
    public SessionResponse createSession(Long eventId, SessionRequest request) {
        validateRequest(request);
        Event event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        Session session = new Session();
        applyRequest(session, request);
        session.setEvent(event);
        session.setCreatedAt(Instant.now());
        session.setUpdatedAt(Instant.now());

        return SessionResponse.from(sessionRepository.save(session));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @resourceAuthorizationService.isEventOrganizer(#eventId, authentication.name))")
    public SessionResponse updateSession(Long eventId, Long sessionId, SessionRequest request) {
        validateRequest(request);
        Session session = sessionRepository.findByIdAndEvent_Id(sessionId, eventId)
                .orElseThrow(() -> new RuntimeException("Sesion no encontrada"));

        applyRequest(session, request);
        session.setUpdatedAt(Instant.now());

        return SessionResponse.from(sessionRepository.save(session));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @resourceAuthorizationService.isEventOrganizer(#eventId, authentication.name))")
    public void deleteSession(Long eventId, Long sessionId) {
        Session session = sessionRepository.findByIdAndEvent_Id(sessionId, eventId)
                .orElseThrow(() -> new RuntimeException("Sesion no encontrada"));
        sessionRepository.delete(session);
    }

    private void applyRequest(Session session, SessionRequest request) {
        session.setTitle(request.title().trim());
        session.setDescription(blankToNull(request.description()));
        session.setStartAt(request.startAt());
        session.setEndAt(request.endAt());
        session.setLocation(blankToNull(request.location()));
        session.setVirtualUrl(blankToNull(request.virtualUrl()));
    }

    private void validateRequest(SessionRequest request) {
        if (!request.startAt().isBefore(request.endAt())) {
            throw new RuntimeException("Las fechas de la sesion no tienen un orden valido");
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
