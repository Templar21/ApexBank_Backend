package com.etz.Controller;

import com.etz.Entity.Account;
import com.etz.Service.AccountService;
import com.etz.Utils.JwtUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;


@Produces("application/json")
@Consumes("application/json")
@Path("account")
public class AccountController {


    private AccountService accountService;

    @Inject
    public AccountController(AccountService accountService) {

        this.accountService = accountService;
    }

    @POST
    @Path("create/savings")
    public Response createSavingsAccount(@HeaderParam("Authorization") String authHeader, Account account){
        try {
            //1.Extract token from header
            String token = authHeader.substring(7);

            //2.Get userId from the token
            int userId = JwtUtil.getUserIdFromToken(token);

            //3.Set Savings account specific values
            account.setUserId(userId);
            account.setAccountType(Account.AccountType.savings); // Set Enum here
            account.setAccountNumber(account.generateSAccountNumber()); //// Generate Savings Account Number

            double initialbalance = account.getBalance();
            if(!(initialbalance <=50)){

                //4.Create account in the database
                accountService.createSavingsAccount(account);

                return Response.status(Response.Status.CREATED)
                        .entity("Account created successfully")
                        .build();
            }
            else{
                throw new RuntimeException("Account balance is too low");
            }
            }


        catch (Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Account creation failed").build();
        }
    }

}
