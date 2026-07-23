package ec.ups.edu.proyectofinal.registrations.controller;

import ec.ups.edu.proyectofinal.registrations.dto.RegistrationResponse;
import ec.ups.edu.proyectofinal.registrations.dto.RegistrationStatusRequest;
import ec.ups.edu.proyectofinal.registrations.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/api/events/{eventId}/registrations")
    public ResponseEntity<RegistrationResponse> registerToEvent(@PathVariable Long eventId, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationService.registerToEvent(eventId, principal.getName()));
    }

    @GetMapping("/api/registrations/me")
    public Page<RegistrationResponse> listMine(
            Principal principal,
            @PageableDefault(size = 20, sort = "registeredAt") Pageable pageable
    ) {
        return registrationService.listMine(principal.getName(), pageable);
    }

    @PatchMapping("/api/registrations/{registrationId}/cancel")
    public RegistrationResponse cancelMine(@PathVariable Long registrationId, Principal principal) {
        return registrationService.cancelMine(registrationId, principal.getName());
    }

    @GetMapping("/api/events/{eventId}/registrations")
    public Page<RegistrationResponse> listForEvent(
            @PathVariable Long eventId,
            @PageableDefault(size = 20, sort = "registeredAt") Pageable pageable
    ) {
        return registrationService.listForEvent(eventId, pageable);
    }

    @PatchMapping("/api/events/{eventId}/registrations/{registrationId}/status")
    public RegistrationResponse updateStatus(
            @PathVariable Long eventId,
            @PathVariable Long registrationId,
            @Valid @RequestBody RegistrationStatusRequest request
    ) {
        return registrationService.updateStatus(eventId, registrationId, request.status());
    }
}
