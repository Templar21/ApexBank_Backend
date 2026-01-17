package com.etz.Controller;


import com.etz.DTO.RegisterRequest;
import com.etz.DTO.RegisterResponse;
import com.etz.Service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("user")
@Produces("application/json")
@Consumes("application/json")
public class UserController {

    @Inject
    private UserService userService;


    @POST
    @Path("register")
    public Response registerUser(RegisterRequest registerRequest){
        try {
            String token = userService.register(registerRequest);
            if (token != null) {
                RegisterResponse response = new RegisterResponse("User registered successfully", token);
                return Response.status(Response.Status.CREATED).entity(response).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Registration failed").build();
            }
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }




    }

