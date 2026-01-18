package com.etz.Service;

import com.etz.Entity.Account;
import com.etz.Utils.DatabaseConnection;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ApplicationScoped
public class AccountServiceImpl implements AccountService {

    @Override
    public void createSavingsAccount(Account account) {
        // user_id is the foreign key linking to the users table
        String sql = "INSERT INTO accounts (account_number, balance, account_type, user_id, pin, created_at,account_name) VALUES (?, ?, ?, ?, ?, ?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getAccountNumber());
            stmt.setDouble(2, account.getBalance());
            stmt.setString(3, account.getAccountType().name());
            stmt.setInt(4, account.getUserId());         // Pass the Foreign Key
            stmt.setString(5, account.getPin());         // Ensure Account entity has getPin()
            stmt.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setString(7,account.getAccountName());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating account: " + e.getMessage());
        }
    }




    @Override
    public void deposit() {

    }

    @Override
    public void withdraw() {

    }

    @Override
    public void transfer() {

    }

    @Override
    public void getBalance() {

    }

    @Override
    public void getAllAccounts() {

    }

    @Override
    public void getAccountById() {

    }

    @Override
    public void deleteAccount() {

    }

    @Override
    public void updateAccount() {

    }
}
