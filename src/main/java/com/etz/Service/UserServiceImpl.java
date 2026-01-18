package com.etz.Service;

import com.etz.DTO.Request.RegisterRequest;
import com.etz.Entity.User;
import com.etz.Utils.DatabaseConnection;
import com.etz.Utils.JwtUtil;
import com.etz.Utils.PasswordUtil;
import com.etz.Utils.Validation;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    @Override
    public String register(RegisterRequest registerRequest) {

        // Input validation - Check for false and throw exception
        if (!Validation.isValid("username", registerRequest.getUsername())) {
            throw new RuntimeException("Invalid username format");
        }

        if (!Validation.isValid("email", registerRequest.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }

        if (!Validation.isValid("password", registerRequest.getPassword())) {
            throw new RuntimeException("Password does not meet security requirements");
        }

        if (!Validation.isValid("phone_number", registerRequest.getPhone_number())) {
            throw new RuntimeException("Invalid phone number. Must be 10 digits.");
        }


        String hashedPassword = PasswordUtil.hashPassword(registerRequest.getPassword());
        String sql = "INSERT INTO user (username,phone_number,email,password) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, registerRequest.getUsername());
                stmt.setString(2, registerRequest.getPhone_number());
                stmt.setString(3, registerRequest.getEmail());
                stmt.setString(4, hashedPassword);

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                // Return a JWT if registration is successful
                return
                        JwtUtil.generateToken(registerRequest.getUsername());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new RuntimeException("Email,Phone or email already exists. Please login instead.");
            }
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void login(User user) {
        // To be implemented
    }
}