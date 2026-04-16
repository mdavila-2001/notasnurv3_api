package com.universidad_nur.notasnurv3_api.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public List<Users> getUsersByRole(String roleName) {
        try {
            Role roleEnum = Role.valueOf(roleName.toUpperCase());
            return userRepository.findByRole(roleEnum);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("El rol proporcionado no es válido: " + roleName);
        }
    }
    @Transactional
    public void createUser(Users user) {

        if (user.getEmail() == null || user.getName() == null || user.getLastName() == null) {
            throw new RuntimeException("Nombre, Apellido y Email son obligatorios");
        }

        String roleStr = (user.getRole() != null) ? user.getRole().name() : Role.STUDENT.name();

        userRepository.createNewUser(
            user.getCi() != null ? user.getCi() : "",
            user.getName(),
            user.getMiddleName() != null ? user.getMiddleName() : "",
            user.getLastName(),
            user.getMotherLastName() != null ? user.getMotherLastName() : "",
            user.getEmail(),
            user.getPassword() != null ? user.getPassword() : "123456",
            roleStr
        );
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }
    @Transactional
    public Users updateStatus(UUID id, String status) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setStatus(status);
        return userRepository.save(user);
    }

    @Transactional
    public Users updateUser(UUID id, Users details) {
        Users user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setName(details.getName());
        user.setMiddleName(details.getMiddleName()); 
        user.setLastName(details.getLastName());
        user.setMotherLastName(details.getMotherLastName()); 
        user.setEmail(details.getEmail());
        user.setCi(details.getCi());
    
        user.setFacultad(details.getFacultad());
        
        return userRepository.save(user);
    }
}