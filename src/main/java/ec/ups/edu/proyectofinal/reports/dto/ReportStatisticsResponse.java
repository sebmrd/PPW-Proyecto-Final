package ec.ups.edu.proyectofinal.reports.dto;

import java.time.Instant;

public record ReportStatisticsResponse(
        Long eventId,
        Instant from,
        Instant to,
        Instant generatedAt,
        long totalEvents,
        long publishedEvents,
        long totalRegistrations,
        long pendingRegistrations,
        long confirmedRegistrations,
        long cancelledRegistrations,
        long rejectedRegistrations
) {
}
