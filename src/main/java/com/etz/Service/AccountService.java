package com.etz.Service;

import com.etz.Entity.Account;

public interface AccountService {

    void createSavingsAccount(Account account);

    void deposit();

    void withdraw();

    void transfer();

    void getBalance();

    void getAllAccounts();

    void getAccountById();

    void deleteAccount();

    void updateAccount();
}
