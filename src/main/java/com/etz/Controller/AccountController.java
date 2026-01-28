package com.etz.Controller;

import com.etz.DTO.Request.DepositRequest;
import com.etz.DTO.Request.TransferRequest;
import com.etz.Entity.Account;
import com.etz.Entity.Transaction;
import com.etz.Service.AccountService;
import com.etz.Utils.JwtUtil;
import com.etz.Utils.Validation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;


@Produces("application/json")
@Consumes("application/json")
@Path("account")

public class AccountController {

    private final AccountService accountService;

    @Inject
    public AccountController(AccountService accountService) {

        this.accountService = accountService;
    }

    @POST
    @Path("create/savings")
    public Response createSavingsAccount(@HeaderParam("Authorization") String authHeader, Account account) {
        //1.Perform JWT Validation
        Response authCheck = Validation.jwtValidation(authHeader);
        if (authCheck != null) {
            return authCheck;
        }

        try {
            //Perform input validation
            if (!Validation.isValid("username", account.getAccountName())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid account name")
                        .build();
            }
            if (!Validation.isValid("amount", String.valueOf(account.getBalance()))) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid balance")
                        .build();
            }
            if (!Validation.isValid("pin", account.getPin())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid PIN")
                        .build();
            }

            //1.Extract token from header and get userId
            String token = authHeader.substring(7);
            int userId = JwtUtil.getUserIdFromToken(token);

            //3.Set Savings account specific values
            account.setUserId(userId);
            account.setAccountType(Account.AccountType.SAVINGS); // Set Enum here
            account.setAccountNumber(account.generateSAccountNumber()); /// Generate Savings Account Number

            double initialbalance = account.getBalance();
            if (!(initialbalance <= 50)) {

                //4.Create account in the database
                accountService.createAccount(account);

                return Response.status(Response.Status.CREATED)
                        .entity("Account created successfully")
                        .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Insufficient Funds. You need a minimum of $50 to open an account")
                        .build();

            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Account creation failed")
                    .build();
        }
    }


    @POST
    @Path("create/current")
    public Response createCurrentAccount(@HeaderParam("Authorization") String authHeader, Account account) {
        //1.Perform JWT Validation
        Response authCheck = Validation.jwtValidation(authHeader);
        if (authCheck != null) {
            return authCheck;
        }

        try {
            //Perform input validation
            if (!Validation.isValid("username", account.getAccountName())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid account name")
                        .build();
            }
            if (!Validation.isValid("amount", String.valueOf(account.getBalance()))) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid balance")
                        .build();
            }
            if (!Validation.isValid("pin", account.getPin())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid PIN")
                        .build();
            }

            //Extract token from the header and get userId
            String token = authHeader.substring(7);
            int userId = JwtUtil.getUserIdFromToken(token);

            //Set Current account specific values
            account.setUserId(userId);
            account.setAccountType(Account.AccountType.CURRENT);
            account.setAccountNumber(account.generateCAccountNumber()); //Generate Current Account Number

            //Create an account in the database
            accountService.createAccount(account);

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Account creation failed")
                    .build();
        }
        return Response.status(Response.Status.CREATED)
                .entity("Account created successfully")
                .build();
    }


    @POST
    @Path("deposit")
    public Response deposit(@HeaderParam("Authorization") String authHeader, DepositRequest depositRequest) {
        //1.Perform JWT Validation
        Response authCheck = Validation.jwtValidation(authHeader);
        if (authCheck != null) {
            return authCheck;
        }

        // 2. Perform input validation for the fields even before it gets to the DB
        if (!Validation.isValid("accountNumber", depositRequest.getAccountNumber())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid account number")
                    .build();
        } else if (!Validation.isValid("pin", depositRequest.getPin())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid PIN")
                    .build();
        } else if (!Validation.isValid("amount", String.valueOf(depositRequest.getAmount()))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid amount")
                    .build();
        }

        //3.Get UserID from token and get all Accounts associated with userID
        String token = authHeader.substring(7);
        int userID = JwtUtil.getUserIdFromToken(token);
        List<Account> accounts = accountService.listAccountsByUserId(userID);

        //4.Check if the account exists and belongs to the user
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(depositRequest.getAccountNumber())) {
                try {
                    //5.Deposit into account
                    accountService.deposit(depositRequest.getAccountNumber(), depositRequest.getPin(), depositRequest.getAmount());
                    return Response.ok("Deposit successful.Your current balance is $" + accountService.getBalance(depositRequest.getAccountNumber()))
                            .build();
                } catch (RuntimeException e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(e.getMessage())
                            .build();
                } catch (Exception e) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(e.getMessage())
                            .build();
                }
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Account does not exist or does not belong to you")
                .build();
    }


    @POST
    @Path("withdraw")
    public Response withdraw(@HeaderParam("Authorization") String authHeader, DepositRequest depositRequest) {
        //1.Perform JWT Validation
        Response authCheck = Validation.jwtValidation(authHeader);
        if (authCheck != null) {
            return authCheck;
        }

        // 2. Perform input validation for the fields even before it gets to the DB
        if (!Validation.isValid("accountNumber", depositRequest.getAccountNumber())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid account number")
                    .build();
        } else if (!Validation.isValid("pin", depositRequest.getPin())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid PIN")
                    .build();
        } else if (!Validation.isValid("amount", String.valueOf(depositRequest.getAmount()))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid amount")
                    .build();
        }

        //3.Get UserID from token and get all Accounts associated with userID
        String token = authHeader.substring(7);
        int userID = JwtUtil.getUserIdFromToken(token);
        List<Account> accounts = accountService.listAccountsByUserId(userID);

        //4.Check if the account exists and belongs to the user
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(depositRequest.getAccountNumber())) {

                //Implement withdrawal for savings account
                if (account.getAccountType().equals(Account.AccountType.SAVINGS)) {
                    //Enforce a minimum balance of 50 after withdrawal
                    if (accountService.getBalance(depositRequest.getAccountNumber()) >= depositRequest.getAmount() + 50) {
                        try {
                            accountService.withdraw(depositRequest.getAccountNumber(), depositRequest.getPin(), depositRequest.getAmount());
                            return Response.ok("Withdrawal successful.Current balance is " + accountService.getBalance(depositRequest.getAccountNumber()))
                                    .build();
                        } catch (RuntimeException e) {
                            return Response.status(Response.Status.BAD_REQUEST)
                                    .entity(e.getMessage())
                                    .build();
                        }
                    } else {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity("Insufficient Funds. Must maintain a minimum balance of $50 after withdrawal")
                                .build();
                    }
                }

                //Implement Withdrawal for current account
                //Overdraft limit of -500 for current account
                if (account.getAccountType().equals(Account.AccountType.CURRENT)) {
                    if (accountService.getBalance(depositRequest.getAccountNumber()) >= depositRequest.getAmount() - 500) {
                        try {
                            accountService.withdraw(depositRequest.getAccountNumber(), depositRequest.getPin(), depositRequest.getAmount());
                            return Response.ok("Withdrawal successful.Current balance is " + accountService.getBalance(depositRequest.getAccountNumber()))
                                    .build();
                        } catch (RuntimeException e) {
                            return Response.status(Response.Status.BAD_REQUEST)
                                    .entity(e.getMessage())
                                    .build();
                        }
                    } else {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity("Insufficient Funds.Overdraft limit is $-500")
                                .build();
                    }
                }

            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Account does not exist or does not belong to you")
                .build();
    }


    @POST
    @Path("transfer/internal")
    public Response internalTransfer(@HeaderParam("Authorization") String authHeader, TransferRequest transferRequest) {
        //1.Perform JWT Validation
        Response authCheck = Validation.jwtValidation(authHeader);
        if (authCheck != null) {
            return authCheck;
        }

        // 2. Validations
        if (!Validation.isValid("accountNumber", transferRequest.getSourceAccount())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Your account number is invalid")
                    .build();
        } else if (!Validation.isValid("accountNumber", transferRequest.getDestinationAccount())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Receiver's account number is invalid")
                    .build();
        } else if (!Validation.isValid("pin", transferRequest.getPin())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid PIN")
                    .build();
        } else if (!Validation.isValid("amount", String.valueOf(transferRequest.getAmount()))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid amount")
                    .build();
        }

        //2.Get userID from JWT
        String token = authHeader.substring(7);
        int userID = JwtUtil.getUserIdFromToken(token);

        //4.List all accounts from UserID
        List<Account> accounts = accountService.listAccountsByUserId(userID);

        for (Account account : accounts) {
            if (account.getAccountNumber().equals(transferRequest.getSourceAccount())) {
                try {
                    //Debit the source
                    accountService.debit(transferRequest.getSourceAccount(), transferRequest.getPin(), transferRequest.getAmount());
                } catch (Exception e) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(e.getMessage())
                            .build();
                }

                //Check if the destination account exists
                if (!accountService.accountExistsByAccountNumber(transferRequest.getDestinationAccount())) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Receiver's account does not exist")
                            .build();
                }
                //Credit the destination
                accountService.credit(transferRequest.getDestinationAccount(), transferRequest.getAmount());
            }
            return Response.status(Response.Status.OK)
                    .entity("Transfer of $" + transferRequest.getAmount() + " from " + transferRequest.getSourceAccount() + " to " + transferRequest.getDestinationAccount() + " successfully processed.Your current balance is " + accountService.getBalance(transferRequest.getSourceAccount()))
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Account not found or does not belong to user")
                .build();

    }


    @GET
    @Path("all") //I will later implement an admin role to be able to acess this
    public List<Account> listallAccounts() {
        return accountService.listallAccounts();
    }


    @GET
    @Path("me")
    public Response getAccountById(@HeaderParam("Authorization") String authHeader) {

        //Perform JWT checks and validation
        Response authCheck = Validation.jwtValidation(authHeader);
        if (authCheck != null) {
            return authCheck;
        }

        int userId = JwtUtil.getUserIdFromToken(authHeader.substring(7));
        List<Account> accounts = accountService.listAccountsByUserId(userId);

        if (accounts == null || accounts.isEmpty() || userId <= 0) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No accounts associated with User")
                    .build();
        }
        return Response.status(Response.Status.FOUND)
                .entity(accounts)
                .build();
    }


    @GET
    @Path("balance/{accountNumber}")
    public Response getBalance(@HeaderParam("Authorization") String authHeader, @PathParam("accountNumber") String accountNumber) {

        //1.Perform JWT Checks
        Response authCheck = Validation.jwtValidation(authHeader);
        if (authCheck != null) {
            return authCheck;
        }

        //Account Number validation
        if (!Validation.isValid("accountNumber", accountNumber)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid account number")
                    .build();
        }

        //2.Get userID from JWT
        String token = authHeader.substring(7);
        int userID = JwtUtil.getUserIdFromToken(token);

        //4.List all accounts from UserID
        List<Account> accounts = accountService.listAccountsByUserId(userID);

        //5.Check if the List contains the account(Path Param)
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                double balance = accountService.getBalance(accountNumber);
                return Response.status(Response.Status.FOUND)
                        .entity("Your current balance is $" + balance)
                        .build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Account does not exist or does not belong to you")
                .build();
    }


    @GET
    @Path("statement/{accountNumber}")
    public Response getStatement(@HeaderParam("Authorization") String authHeader, @PathParam("accountNumber") String accountNumber) {
        //1.Perform JWT Checks
        Response authCheck = Validation.jwtValidation(authHeader);
        if (authCheck != null) {
            return authCheck;
        }

        //Account Number validation
        if (!Validation.isValid("accountNumber", accountNumber)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid account number")
                    .build();
        }

        //2.Get userID from JWT
        String token = authHeader.substring(7);
        int userID = JwtUtil.getUserIdFromToken(token);

        //4.List all accounts from UserID
        List<Account> accounts = accountService.listAccountsByUserId(userID);

        //5.Check if the user is found in the List and then proceed to show the transactions
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                List<Transaction> transactions = accountService.getStatement(accountNumber);
                if (transactions == null || transactions.isEmpty() || accountNumber.isEmpty()) {

                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("No Transactions found for this account")
                            .build();
                }
                return Response.status(Response.Status.OK)
                        .entity(transactions)
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Account not found or account does not belong to you")
                        .build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Error.Try again")
                .build();
    }


    @GET
    @Path("allstatement/{accountNumber}")
    public Response getAllStatement(@HeaderParam("Authorization") String authHeader, @PathParam("accountNumber") String accountNumber) {
        //1.Perform JWT Checks
        Response authCheck = Validation.jwtValidation(authHeader);
        if (authCheck != null) {
            return authCheck;
        }

        //3.Get userID from JWT
        String token = authHeader.substring(7);
        int userID = JwtUtil.getUserIdFromToken(token);

        //4.List all accounts from UserID
        List<Account> accounts = accountService.listAccountsByUserId(userID);

        //5.Check if the user is found in the List and then proceed to show the transactions
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                List<Transaction> transactions = accountService.getAllStatement(accountNumber);
                if (transactions == null || transactions.isEmpty() || accountNumber.isEmpty()) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("No Transactions found for this account")
                            .build();
                }
                return Response.status(Response.Status.FOUND)
                        .entity(transactions)
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Account not found or account does not belong to you")
                        .build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Error.Try again")
                .build();

    }


}