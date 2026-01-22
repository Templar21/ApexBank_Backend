package com.etz.Service;

import com.etz.Entity.Account;
import com.etz.Entity.Transaction;

import java.util.List;

public interface AccountService {

    void createAccount(Account account);

    void deposit(String accountNumber, String pin, double amount);

    void withdraw(String accountNumber, String pin, double amount);

    double getBalance(String accountNumber);

    List<Account> listallAccounts();

    List<Account> listAccountsByUserId(int userId);

    List<Transaction> getStatement(String accountNumber);

}