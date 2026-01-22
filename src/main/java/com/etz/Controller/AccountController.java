package com.etz.Controller;

import com.etz.DTO.Request.DepositRequest;
import com.etz.Entity.Account;
import com.etz.Entity.Transaction;
import com.etz.Service.AccountService;
import com.etz.Utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
        try {
            //1.Extract token from header
            String token = authHeader.substring(7);

            //2.Get userId from the token
            int userId = JwtUtil.getUserIdFromToken(token);

            //3.Set Savings account specific values
            account.setUserId(userId);
            account.setAccountType(Account.AccountType.SAVINGS); // Set Enum here
            account.setAccountNumber(account.generateSAccountNumber()); /// Generate Savings Account Number

            double initialbalance = account.getBalance();
            if (!(initialbalance <= 50)) {

                //4.Create account in the database
                accountService.createAccount(account);

                return Response.status(Response.Status.CREATED).entity("Account created successfully").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Insufficient Funds. You need a minimum of $50 to open an account").build();

            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Account creation failed").build();
        }
    }

    @POST
    @Path("create/current")
    public Response createCurrentAccount(@HeaderParam("Authorization") String authHeader, Account account) {
        try {
            //Extract token from header
            String token = authHeader.substring(7);

            //Get userId from the token
            int userId = JwtUtil.getUserIdFromToken(token);

            //Set Current account specific values
            account.setUserId(userId);
            account.setAccountType(Account.AccountType.CURRENT);
            account.setAccountNumber(account.generateCAccountNumber()); //Generate Current Account Number

            //Create account in the database
            accountService.createAccount(account);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Response.status(Response.Status.CREATED).entity("Account created successfully").build();
    }

    @POST
    @Path("deposit")
    public Response deposit(@HeaderParam("Authorization") String authHeader, DepositRequest depositRequest) {

        //1.Perform JWT Checks
        try {
            if (!authHeader.startsWith("Bearer ") || authHeader == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Missing or invalid Authorization header").build();
            }
        } catch (NullPointerException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Missing or invalid Authorization header").build();
        }

        //2.Extract token from header and perform validation
        String token = authHeader.substring(7);
        try {
            JwtUtil.validateToken(token);
        } catch (ExpiredJwtException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Token has expired. Please login again").build();
        } catch (JwtException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build();
        }

        //3.Get UserID from token and get all Accounts associated with userID
        int userID = JwtUtil.getUserIdFromToken(token);
        List<Account> accounts = accountService.listAccountsByUserId(userID);
        for(Account account :accounts){
            if(account.getAccountNumber().equals(depositRequest.getAccountNumber())){

                //Deposit into account
                accountService.deposit(depositRequest.getAccountNumber(), depositRequest.getPin(), depositRequest.getAmount());
                return Response.ok("Deposit successful").build();
            }
        }
                return Response.status(Response.Status.NOT_FOUND).entity("Account does not exist or does not belong to you").build();

        }

    @POST
    @Path("withdraw")
    public Response withdraw(@HeaderParam("Authorization") String authHeader, DepositRequest depositRequest) {

        //1.Perform JWT Checks
        try {
            if (!authHeader.startsWith("Bearer ") || authHeader == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Missing or invalid Authorization header").build();
            }
        } catch (NullPointerException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Missing or invalid Authorization header").build();
        }

        //2.Extract token from header and perform validation
        String token = authHeader.substring(7);
        try {
            JwtUtil.validateToken(token);
        } catch (ExpiredJwtException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Token has expired. Please login again").build();
        } catch (JwtException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build();
        }


        //3.Get UserID from token and get all Accounts associated with userID
        int userID = JwtUtil.getUserIdFromToken(token);
        List<Account> accounts = accountService.listAccountsByUserId(userID);

        for(Account account :accounts){
            if(account.getAccountNumber().equals(depositRequest.getAccountNumber())){

                //Implement withdrawal for savings account
                if(account.getAccountType().equals(Account.AccountType.SAVINGS)){
                    //Enforce a minimum balance of 50 after withdrawal
                    if(accountService.getBalance(depositRequest.getAccountNumber())>=depositRequest.getAmount()+50){
                        try {
                            accountService.withdraw(depositRequest.getAccountNumber(), depositRequest.getPin(), depositRequest.getAmount());
                            return Response.ok("Withdrawal successful.Current balance is "+ accountService.getBalance(depositRequest.getAccountNumber())).build();
                        } catch (RuntimeException e) {
                            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
                        }
                    }
                    else{
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Insufficient Funds. Must maintain a minimum balance of $50 after withdrawal").build();
                    }
                }

                //Implement Withdrawal for current account
                //Overdraft limit of -500 for current account
                if(account.getAccountType().equals(Account.AccountType.CURRENT)){
                    if(accountService.getBalance(depositRequest.getAccountNumber())>=depositRequest.getAmount()-500){
                        try {
                            accountService.withdraw(depositRequest.getAccountNumber(), depositRequest.getPin(), depositRequest.getAmount());
                            return Response.ok("Withdrawal successful.Current balance is "+ accountService.getBalance(depositRequest.getAccountNumber())).build();
                        } catch (RuntimeException e) {
                            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
                        }
                    }
                    else{
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Insufficient Funds.Overdraft limit is $-500").build();
                    }
                }

                }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Account does not exist or does not belong to you").build();

    }

    @GET
    @Path("all")
    public List<Account> listallAccounts() {
        return accountService.listallAccounts();
    }

    @GET
    @Path("{id}")
    public Response getAccountById(@PathParam("id") int userId) {

        List<Account> accounts = accountService.listAccountsByUserId(userId);
        if (accounts == null || accounts.isEmpty() || userId <= 0) {
            return Response.status(Response.Status.NOT_FOUND).entity("No accounts associated with User").build();
        }
        return Response.status(Response.Status.FOUND).entity(accounts).build();
    }

    @GET
    @Path("balance/{accountNumber}")
    public Response getBalance(@HeaderParam("Authorization") String authHeader, @PathParam("accountNumber") String accountNumber) {

        //1.Perform JWT Checks
        try {
            if (!authHeader.startsWith("Bearer ") || authHeader == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Missing or invalid Authorization header").build();
            }
        } catch (NullPointerException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Missing or invalid Authorization header").build();
        }

        //2.Extract token from header and perform validation
        String token = authHeader.substring(7);
        try {
            JwtUtil.validateToken(token);
        } catch (ExpiredJwtException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Token has expired. Please login again").build();
        } catch (JwtException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build();
        }

        //3.Get userID from JWT
       int userID=JwtUtil.getUserIdFromToken(token);

        //4.List all accounts from UserID
        List<Account> accounts = accountService.listAccountsByUserId(userID);

        //5.Check if the List contains the account(Path Param)
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                double balance = accountService.getBalance(accountNumber);
                return Response.ok(balance).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Account does not exist or does not belong to you").build();
    }


    @GET
    @Path("statement/{accountNumber}")
    public Response getStatement(@PathParam("accountNumber") String accountNumber) {

        List<Transaction> transactions = accountService.getStatement(accountNumber);
        if (transactions == null || transactions.isEmpty() || accountNumber.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("No accounts associated with User").build();
        }
        return Response.status(Response.Status.FOUND).entity(transactions).build();
    }


    @GET
    @Path("allstatement/{accountNumber}")
    public Response getAllStatement(@PathParam("accountNumber") String accountNumber) {
        List<Transaction> transactions = accountService.getAllStatement(accountNumber);

        if (transactions == null || transactions.isEmpty() || accountNumber.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("No accounts associated with User").build();
        }
        return Response.status(Response.Status.FOUND).entity(transactions).build();

    }


}