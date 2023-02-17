package dev.ihet.aws.functions;

import org.jboss.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class FallbackExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger log = Logger.getLogger(FallbackExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        log.error("An unexpected error was caught", exception);
        return Response.serverError()
                .type(MediaType.APPLICATION_JSON)
                .entity(ResourceResponse.error("An unexpected error occurred, pleaser check the logs for details"))
                .build();
    }
}
