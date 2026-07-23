package ec.ups.edu.proyectofinal.security;

import ec.ups.edu.proyectofinal.events.repository.EventRepository;
import ec.ups.edu.proyectofinal.registrations.repository.RegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceAuthorizationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private ResourceAuthorizationService authorizationService;

    @Test
    void allowsOnlyTheOrganizerThatOwnsTheEvent() {
        when(eventRepository.existsByIdAndOrganizer_Email(10L, "organizer@academic.test"))
                .thenReturn(true);

        boolean allowed = authorizationService.isEventOrganizer(10L, " Organizer@Academic.Test ");

        assertThat(allowed).isTrue();
        verify(eventRepository).existsByIdAndOrganizer_Email(10L, "organizer@academic.test");
    }

    @Test
    void rejectsMissingEventOrEmail() {
        assertThat(authorizationService.isEventOrganizer(null, "organizer@academic.test")).isFalse();
        assertThat(authorizationService.isRegistrationParticipant(1L, null)).isFalse();

        verifyNoInteractions(eventRepository, registrationRepository);
    }

    @Test
    void allowsOnlyTheParticipantThatOwnsTheRegistration() {
        when(registrationRepository.existsByIdAndParticipant_Email(25L, "participant@academic.test"))
                .thenReturn(true);

        boolean allowed = authorizationService.isRegistrationParticipant(25L, "Participant@Academic.Test");

        assertThat(allowed).isTrue();
        verify(registrationRepository).existsByIdAndParticipant_Email(25L, "participant@academic.test");
    }
}
