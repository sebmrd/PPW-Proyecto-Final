package ec.ups.edu.proyectofinal.registrations.controller;

import ec.ups.edu.proyectofinal.registrations.dto.SessionRequest;
import ec.ups.edu.proyectofinal.registrations.dto.SessionResponse;
import ec.ups.edu.proyectofinal.registrations.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public List<SessionResponse> listSessions(@PathVariable Long eventId) {
        return sessionService.listByEvent(eventId);
    }

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @PathVariable Long eventId,
            @Valid @RequestBody SessionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.createSession(eventId, request));
    }

    @PutMapping("/{sessionId}")
    public SessionResponse updateSession(
            @PathVariable Long eventId,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionRequest request
    ) {
        return sessionService.updateSession(eventId, sessionId, request);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long eventId, @PathVariable Long sessionId) {
        sessionService.deleteSession(eventId, sessionId);
        return ResponseEntity.noContent().build();
    }
}
