package com.etz.Utils;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonbExceptionMapper implements ExceptionMapper<JsonbException> {

    @Override
    public Response toResponse(JsonbException exception) {
        // Check if the underlying cause is a NumberFormatException (e.g., empty string for a double)
        if (exception.getCause() instanceof NumberFormatException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid input: 'amount' must be a valid number and cannot be empty.")
                    .build();
        }

        // Fallback for other JSON binding errors
        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON request format.").build();
    }
}