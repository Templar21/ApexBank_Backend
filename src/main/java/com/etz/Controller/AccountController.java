package com.etz.Controller;

import com.etz.DTO.Request.DepositRequest;
import com.etz.Entity.Account;
import com.etz.Service.AccountService;
import com.etz.Utils.JwtUtil;
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
            account.setAccountNumber(account.generateSAccountNumber()); //// Generate Savings Account Number

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
            String token=authHeader.substring(7);

            //Get userId from the token
            int userId=JwtUtil.getUserIdFromToken(token);

            //Set Current account specific values
            account.setUserId(userId);
            account.setAccountType(Account.AccountType.CURRENT);
            account.setAccountNumber(account.generateCAccountNumber()); //Generate Current Account Number

            //Create account in the database
            accountService.createAccount(account);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Response.status(Response.Status.CREATED)
                .entity("Account created successfully")
                .build();
    }


    @POST
    @Path("deposit")
    public Response deposit(@HeaderParam("Authorization") String authHeader, DepositRequest depositRequest) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Missing or invalid Authorization header").build();
            }

            //1.Extract token from header
            String token = authHeader.substring(7);

            //2.Get userId from the token (This validates the token)
            JwtUtil.getUserIdFromToken(token);

            accountService.deposit(depositRequest.getAccountNumber(), depositRequest.getPin(), depositRequest.getAmount());
            return Response.ok("Deposit successful").build();

        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build();
        }
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
        if (accounts == null || accounts.isEmpty() || userId<=0) {
            return Response.status(Response.Status.NOT_FOUND).entity("No accounts associated with User").build();
        }
        return Response.status(Response.Status.FOUND).entity(accounts).build();
}

}