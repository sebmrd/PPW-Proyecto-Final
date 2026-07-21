package ec.ups.edu.proyectofinal.registrations.service;

import ec.ups.edu.proyectofinal.events.entity.Event;
import ec.ups.edu.proyectofinal.events.repository.EventRepository;
import ec.ups.edu.proyectofinal.registrations.entity.Registration;
import ec.ups.edu.proyectofinal.registrations.repository.RegistrationRepository;
import ec.ups.edu.proyectofinal.users.entity.User;
import ec.ups.edu.proyectofinal.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RegistrationService(RegistrationRepository registrationRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // Regla: Registrar la inscripción y actualizar la disponibilidad dentro de una transacción.
    @Transactional
public Registration registerToEvent(Long eventId, String userEmail) {
    Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

    User participant = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // Ajuste: El estado ahora es String ("FINISHED" en lugar de Enum)
    if ("FINISHED".equals(event.getStatus())) {
        throw new RuntimeException("No se puede inscribir: El evento ya finalizó");
    }

    // Ajuste: El nombre del campo cambió a availableCapacity
    if (event.getAvailableCapacity() <= 0) {
        throw new RuntimeException("No se puede inscribir: El evento no tiene cupos disponibles");
    }

    if (registrationRepository.existsByParticipantAndEvent(participant, event)) {
        throw new RuntimeException("El participante ya se encuentra inscrito en este evento");
    }

    // Ajuste: Actualizar el nuevo nombre de campo
    event.setAvailableCapacity(event.getAvailableCapacity() - 1);
    eventRepository.save(event);

    Registration registration = new Registration();
    registration.setEvent(event);
    registration.setParticipant(participant);
    registration.setStatus("CONFIRMED"); // Mantenemos el String

    return registrationRepository.save(registration);
}
}
