package ec.ups.edu.proyectofinal.events.controller;

import ec.ups.edu.proyectofinal.events.dto.EventRequest;
import ec.ups.edu.proyectofinal.events.dto.EventResponse;
import ec.ups.edu.proyectofinal.events.dto.EventStatusRequest;
import ec.ups.edu.proyectofinal.events.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public Page<EventResponse> listEvents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "startAt") Pageable pageable
    ) {
        return eventService.listEvents(status, categoryId, q, pageable);
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable Long id) {
        return eventService.getEvent(id);
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(request, principal.getName()));
    }

    @PutMapping("/{id}")
    public EventResponse updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return eventService.updateEvent(id, request);
    }

    @PatchMapping("/{id}/status")
    public EventResponse updateStatus(@PathVariable Long id, @Valid @RequestBody EventStatusRequest request) {
        return eventService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
