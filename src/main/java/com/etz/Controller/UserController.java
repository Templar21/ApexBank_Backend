package com.etz.Controller;


import com.etz.DTO.Request.LoginRequest;
import com.etz.DTO.Request.RegisterRequest;
import com.etz.DTO.Response.AuthResponse;
import com.etz.Exception.AuthException;
import com.etz.Service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("user")
@Produces("application/json")
@Consumes("application/json")
public class UserController {

    @Inject
    private  UserService userService;

    public UserController() {

    }

    @POST
    @Path("register")
    public Response registerUser(RegisterRequest registerRequest){
        try {
            String token = userService.register(registerRequest);
            if (token != null) {
                AuthResponse response = new AuthResponse("User registered successfully", token);
                return Response.status(Response.Status.CREATED).entity(response).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Registration failed").build();
            }
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @POST
    @Path("login")
    public Response loginUser(LoginRequest loginRequest) {
        try {

            String token = userService.login(loginRequest);
            if (token != null) {
                AuthResponse response = new AuthResponse("Login successful", token);
                return Response.ok(response).build();
            }
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An unexpected error occurred")
                    .build();
        }
        return null;
    }




    }

