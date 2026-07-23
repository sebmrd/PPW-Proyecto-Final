package ec.ups.edu.proyectofinal.users.controller;

import ec.ups.edu.proyectofinal.users.dto.UserStatusRequest;
import ec.ups.edu.proyectofinal.users.dto.UserSummaryResponse;
import ec.ups.edu.proyectofinal.users.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserAdminService userAdminService;

    public UserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public Page<UserSummaryResponse> listUsers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "email") Pageable pageable
    ) {
        return userAdminService.listUsers(status, q, pageable);
    }

    @PatchMapping("/{id}/status")
    public UserSummaryResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        return userAdminService.updateStatus(id, request.status());
    }
}
