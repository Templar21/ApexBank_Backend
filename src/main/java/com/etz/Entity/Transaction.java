package com.etz.Entity;

public class Transaction {
    private int transactionId;
    private int accountId;
    private String accountNumber; // Added accountNumber field
    private String transactionType;
    private double amount;
    private String transactionDate;
    private double balanceAfterTransaction;

    public Transaction() {
    }

    public Transaction(int transactionId, int accountId, String accountNumber, String transactionType, double amount, String transactionDate, double balanceAfterTransaction) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public void setBalanceAfterTransaction(double balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", accountId=" + accountId +
                ", accountNumber='" + accountNumber + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", amount=" + amount +
                ", transactionDate='" + transactionDate + '\'' +
                ", balanceAfterTransaction=" + balanceAfterTransaction +
                '}';
    }
}
