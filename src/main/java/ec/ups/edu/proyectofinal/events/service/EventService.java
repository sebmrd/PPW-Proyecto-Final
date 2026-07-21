package ec.ups.edu.proyectofinal.events.service;

import ec.ups.edu.proyectofinal.events.entity.Event;
import ec.ups.edu.proyectofinal.events.repository.EventRepository;
import ec.ups.edu.proyectofinal.registrations.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public EventService(EventRepository eventRepository, RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        boolean hasRegistrations = registrationRepository.existsByEvent(event);

        // Ajuste: La propiedad en la entidad ahora se llama 'deleted' y es Boolean
        if ("PUBLISHED".equals(event.getStatus()) && hasRegistrations) {
            event.setDeleted(true); 
            eventRepository.save(event);
        } else {
            eventRepository.delete(event);
        }
    }
}
