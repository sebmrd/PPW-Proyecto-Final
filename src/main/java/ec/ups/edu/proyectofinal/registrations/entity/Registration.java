package ec.ups.edu.proyectofinal.registrations.entity;

import ec.ups.edu.proyectofinal.events.entity.Event;
import ec.ups.edu.proyectofinal.users.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "registrations")
@Getter @Setter
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;

    @Column(nullable = false)
    private Instant registrationDate = Instant.now();

    @Column(nullable = false)
    private String status; // Ej: CONFIRMED, CANCELLED
}
