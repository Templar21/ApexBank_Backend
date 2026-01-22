package com.etz.Service;

import com.etz.Entity.Account;
import com.etz.Entity.Transaction;
import com.etz.Utils.DatabaseConnection;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@ApplicationScoped
public class AccountServiceImpl implements AccountService {

    @Override
    public void withdraw(String accountNumber, String pin, double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Withdrawal amount must be greater than zero");
        }

        String getAccountSql = "SELECT account_id, pin, balance FROM accounts WHERE account_number = ?";

        String updateBalanceSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";

        String logTransactionSql = "INSERT INTO transactions (account_id, transaction_type, transaction_amount,transaction_status, transaction_date, balance_after_transaction) VALUES (?, 'WITHDRAWAL', ?,'SUCCESSFUL', ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start Transaction

            try {
                int accountId = -1;
                String dbPin = null;
                double currentBalance = 0.0;

                // Step A: Find the accountId for the given accountNumber
                try (PreparedStatement getStmt = conn.prepareStatement(getAccountSql)) {
                    getStmt.setString(1, accountNumber);

                    try (ResultSet rs = getStmt.executeQuery()) {
                        if (rs.next()) {
                            accountId = rs.getInt("account_id");
                            dbPin = rs.getString("pin");
                            currentBalance = rs.getDouble("balance");
                        } else {
                            throw new SQLException("Account not found: " + accountNumber);
                        }
                    }
                }

                // Step B: Verify PIN
                if (dbPin == null || !dbPin.equals(pin)) {
                    throw new RuntimeException("Invalid PIN");
                }

                // Step C: Update the balance
                try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceSql)) {
                    updateStmt.setDouble(1, amount);
                    updateStmt.setString(2, accountNumber);
                    updateStmt.executeUpdate();
                }

                // Step D: Log the transaction using the retrieved accountId
                try (PreparedStatement logStmt = conn.prepareStatement(logTransactionSql)) {
                    logStmt.setInt(1, accountId);
                    logStmt.setDouble(2, amount);
                    logStmt.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                    logStmt.setDouble(4, currentBalance - amount);
                    logStmt.executeUpdate();
                }

                conn.commit(); // Success: Commit all changes
            } catch (SQLException | RuntimeException e) {
                conn.rollback(); // Failure: Roll back changes
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Withdrawal failed: " + e.getMessage());
        }


    }

    @Override
    public double getBalance(String accountNumber) {
        String sql ="SELECT balance FROM accounts WHERE account_number = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setString(1, accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account();
                    account.setBalance(rs.getDouble("balance"));
                    return account.getBalance();
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public void createAccount(Account account) {
        // user_id is the foreign key linking to the users table
        String sql = "INSERT INTO accounts (account_number, balance, account_type, user_id, pin, created_at,account_name) VALUES (?, ?, ?, ?, ?, ?,?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getAccountNumber());
            stmt.setDouble(2, account.getBalance());
            stmt.setString(3, account.getAccountType().name());
            stmt.setInt(4, account.getUserId());         // Pass the Foreign Key
            stmt.setString(5, account.getPin());         // Ensure Account entity has getPin()
            stmt.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setString(7, account.getAccountName());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating account: " + e.getMessage());
        }
    }

    @Override
    public void deposit(String accountNumber, String pin, double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Deposit amount must be greater than zero");
        }

        // 1. Query to get accountId and pin
        String getAccountSql = "SELECT account_id, pin, balance FROM accounts WHERE account_number = ?";

        // 2. Query to update balance
        String updateBalanceSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";

        // 3. Query to log transaction using account_id
        String logTransactionSql = "INSERT INTO transactions (account_id, transaction_type, transaction_amount,transaction_status, transaction_date, balance_after_transaction) VALUES (?, 'DEPOSIT', ?,'SUCCESSFUL', ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start Transaction

            try {
                int accountId = -1;
                String dbPin = null;
                double currentBalance = 0.0;

                // Step A: Find the accountId for the given accountNumber
                try (PreparedStatement getStmt = conn.prepareStatement(getAccountSql)) {
                    getStmt.setString(1, accountNumber);

                    try (ResultSet rs = getStmt.executeQuery()) {
                        if (rs.next()) {
                            accountId = rs.getInt("account_id");
                            dbPin = rs.getString("pin");
                            currentBalance = rs.getDouble("balance");
                        } else {
                            throw new SQLException("Account not found: " + accountNumber);
                        }
                    }
                }

                // Step B: Verify PIN
                if (dbPin == null || !dbPin.equals(pin)) {
                    throw new RuntimeException("Invalid PIN");
                }

                // Step C: Update the balance
                try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceSql)) {
                    updateStmt.setDouble(1, amount);
                    updateStmt.setString(2, accountNumber);
                    updateStmt.executeUpdate();
                }

                // Step D: Log the transaction using the retrieved accountId
                try (PreparedStatement logStmt = conn.prepareStatement(logTransactionSql)) {
                    logStmt.setInt(1, accountId);
                    logStmt.setDouble(2, amount);
                    logStmt.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                    logStmt.setDouble(4, currentBalance + amount);
                    logStmt.executeUpdate();
                }

                conn.commit(); // Success: Commit all changes
            } catch (SQLException | RuntimeException e) {
                conn.rollback(); // Failure: Roll back changes
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Deposit failed: " + e.getMessage());
        }
    }

    @Override
    public List<Account> listallAccounts() {
        String sql = "SELECT * FROM accounts";

        List<Account> accounts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {

                    Account account = new Account();

                    account.setAccountId(rs.getInt("account_id"));
                    account.setAccountNumber(rs.getString("account_number"));
                    account.setBalance(rs.getDouble("balance"));
                    // Use toUpperCase to handle case sensitivity
                    account.setAccountType(Account.AccountType.valueOf(rs.getString("account_type").toUpperCase()));
                    account.setUserId(rs.getInt("user_id"));
                    account.setPin(rs.getString("pin"));
                    account.setCreatedAt(rs.getString("created_at"));
                    account.setAccountName(rs.getString("account_name"));

                    accounts.add(account);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accounts;
    }


    @Override
    public List<Account> listAccountsByUserId(int userId) {

        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

                    Account account = new Account();
                    account.setAccountId(rs.getInt("account_id"));
                    account.setAccountNumber(rs.getString("account_number"));
                    account.setBalance(rs.getDouble("balance"));
                    try {
                        // Restore the account type mapping, making it robust against case issues.
                        account.setAccountType(Account.AccountType.valueOf(rs.getString("account_type").toUpperCase()));
                    } catch (Exception e) {
                        System.err.println("[ERROR] Could not parse account_type: " + rs.getString("account_type"));
                    }
                    account.setUserId(rs.getInt("user_id"));
                    account.setPin(rs.getString("pin"));
                    account.setCreatedAt(rs.getString("created_at"));
                    account.setAccountName(rs.getString("account_name"));
                    accounts.add(account);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // This should print a stack trace to the console
            throw new RuntimeException(e);
        }

        return accounts;
    }

    @Override
    public List<Transaction> getStatement(String accountNumber) {
        String sql="SELECT a.account_number, t.transaction_id,t.account_id, t.transaction_amount, t.balance_after_transaction, t.transaction_type, t.transaction_date FROM accounts a JOIN transactions t ON a.account_id = t.account_id WHERE a.account_number = ? ORDER BY t.transaction_date DESC LIMIT 5";

        List<Transaction> transactions =  new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql))
        {
            pstmt.setString(1, accountNumber);

            try (ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setTransactionId(rs.getInt("transaction_id"));
                    transaction.setAccountId(rs.getInt("account_id"));
                    transaction.setAccountNumber(rs.getString("account_number")); // Set account number
                    transaction.setTransactionType(rs.getString("transaction_type"));
                    transaction.setAmount(rs.getDouble("transaction_amount"));
                    transaction.setTransactionDate(rs.getString("transaction_date"));
                    transaction.setBalanceAfterTransaction(rs.getDouble("balance_after_transaction"));
                    transactions.add(transaction);
                }
            }

        }
     catch (SQLException e) {
                throw new RuntimeException(e);
        }
    return transactions;
    }

    @Override
    public List<Transaction> getAllStatement(String accountNumber){
        String sql="SELECT a.account_number, t.transaction_id,t.account_id, t.transaction_amount, t.balance_after_transaction, t.transaction_type, t.transaction_date FROM accounts a JOIN transactions t ON a.account_id = t.account_id WHERE a.account_number = ? ";
        List<Transaction> transactions =  new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)){
            pstmt.setString(1, accountNumber);

            try (ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {
                    Transaction transaction = new Transaction();

                    transaction.setTransactionId(rs.getInt("transaction_id"));
                    transaction.setAccountId(rs.getInt("account_id"));
                    transaction.setAccountNumber(rs.getString("account_number"));
                    transaction.setTransactionType(rs.getString("transaction_type"));
                    transaction.setAmount(rs.getDouble("transaction_amount"));
                    transaction.setTransactionDate(rs.getString("transaction_date"));
                    transaction.setBalanceAfterTransaction(rs.getDouble("balance_after_transaction"));
                    transactions.add(transaction);
                }

                }
            }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return transactions;
    }




}
