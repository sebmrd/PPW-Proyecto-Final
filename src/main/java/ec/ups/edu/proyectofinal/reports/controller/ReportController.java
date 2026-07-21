package ec.ups.edu.proyectofinal.reports.controller;

import ec.ups.edu.proyectofinal.reports.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports/events/{eventId}/registrations.pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> getRegistrationsPdf(@PathVariable Long eventId) {
        byte[] pdfBytes = reportService.generateRegistrationsPdf(eventId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "registrations-event-" + eventId + ".pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/reports/events/{eventId}/registrations.xlsx")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> getRegistrationsExcel(@PathVariable Long eventId) {
        byte[] excelBytes = reportService.generateRegistrationsExcel(eventId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "registrations-event-" + eventId + ".xlsx");
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/registrations/{id}/certificate.pdf")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<byte[]> getCertificatePdf(@PathVariable Long id) {
        byte[] pdfBytes = reportService.generateCertificatePdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate-" + id + ".pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
