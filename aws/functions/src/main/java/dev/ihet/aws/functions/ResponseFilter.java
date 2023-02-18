package dev.ihet.aws.functions;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.CorsHeaders;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class ResponseFilter implements ContainerResponseFilter {

    private static final Logger log = Logger.getLogger(ResponseFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        var origin = requestContext.getHeaders().getFirst(CorsHeaders.ORIGIN);
        if (origin != null) {
            log.info("Setting http-header: " + CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN + "=" + origin);
            responseContext.getHeaders().putSingle(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        }else {
            log.info("No http-header: " + CorsHeaders.ORIGIN + " present in present");
        }
    }
}
