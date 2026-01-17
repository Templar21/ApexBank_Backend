package com.etz.Service;

import com.etz.DTO.RegisterRequest;
import com.etz.Entity.User;
import com.etz.Utils.DatabaseConnection;
import com.etz.Utils.JwtUtil;
import com.etz.Utils.PasswordUtil;
import com.etz.Utils.Validation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserServiceImpl implements UserService {

    @Override
    public String register(RegisterRequest registerRequest) {

        //Input validation
       Validation.isValid("username", registerRequest.getUsername());
       Validation.isValid("email", registerRequest.getEmail());
       Validation.isValid("phone_number", registerRequest.getPhone_number());
       Validation.isValid("password", registerRequest.getPassword());

        String hashedPassword = PasswordUtil.hashPassword(registerRequest.getPassword());
        String sql = "INSERT INTO users (username,phone_number,email,password) VALUES (?, ?, ?, ?)";

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
                throw new RuntimeException("Username or Email already exists");
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