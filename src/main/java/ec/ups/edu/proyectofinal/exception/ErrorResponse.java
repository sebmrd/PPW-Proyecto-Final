package ec.ups.edu.proyectofinal.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String internalCode;
    private String message;
    private String path;
}
