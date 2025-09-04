package com.example.role.based.service.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role-based")
public class RoleBasedController {
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminData() {
        return "Admin access granted! This is restricted data.";
    }

    // Endpoint خاص بالـUser فقط
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String getUserData() {
        return "User access granted! This is a public data for users.";
    }

    // Endpoint متاح لـAdmin و User
    @GetMapping("/both")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getBothData() {
        return "Access granted for both Admin and User.";
    }

    // Endpoint عام (Public) لا يتطلب مصادقة
    @GetMapping("/public")
    public String getPublicInfo() {
        return "This is public data, no authentication needed.";
    }
}
