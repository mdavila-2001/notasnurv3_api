package com.universidad_nur.notasnurv3_api.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin; 
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/role/{roleName}")
    public ResponseEntity<List<Users>> getByRole(@PathVariable String roleName) {
        List<Users> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Users user) {
        userService.createUser(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Users> update(@PathVariable UUID id, @RequestBody Users user) {
        Users updatedUser = userService.updateUser(id, user); 
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Users> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        return ResponseEntity.ok(userService.updateStatus(id, newStatus));
    }
}