package com.etz.Entity;

public class Account {
    private int accountId;
    private String accountName;
    private int userId;
    private String accountNumber;
    private double balance;
    private AccountType accountType;
    private String createdAt;
    private String pin;

    public Account() {
    }

    public Account(int accountId, int userId, String accountNumber, double balance,AccountType accountType, String createdAt,String pin,String accountName) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.createdAt = createdAt;
        this.pin = pin;
        this.accountName = accountName;
    }

    public int getAccountId() {
        return accountId;
    }



    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public String getPin() {
   return pin;
    }

    public void setPin(String pin) {
    this.pin = pin;
    }

    // Generate Current Account Number
    public String generateCAccountNumber() {
        String prefix = "CUR-";
        int min = 100000;
        int max = 999999;
        int randomNum = min + (int) (Math.random() * ((max - min) + 1));
        return prefix + randomNum;
    }

    // Generate Savings Account Number
    public String generateSAccountNumber() {
        String prefix = "SAV-";
        int min = 100000;
        int max = 999999;
        int randomNum = min + (int) (Math.random() * ((max - min) + 1));
        return prefix + randomNum;
    }


    // Enum for Transaction Types
    public enum AccountType {
        savings, current
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}
