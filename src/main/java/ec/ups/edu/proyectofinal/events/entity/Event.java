package ec.ups.edu.proyectofinal.events.entity;

import ec.ups.edu.proyectofinal.users.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "events")
@Getter @Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String modality; // Ej: PRESENCIAL, VIRTUAL, HIBRIDO

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer availableSpots; // Para controlar los cupos rápidamente

    @Column(nullable = false)
    private Instant startDate; // Instant almacena en UTC por defecto

    @Column(nullable = false)
    private Instant endDate;

    @Column(nullable = false)
    private String status; // Ej: DRAFT, PUBLISHED, FINISHED

    // Regla de negocio: No eliminar físicamente un evento publicado
    @Column(nullable = false)
    private Boolean isDeleted = false; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;
}
