package dev.ihet.aws.functions;

import java.util.List;
import java.util.Objects;

public class ResourceResponse {

    public final String message;

    public final boolean error;

    public final List<String> validationErrors;

    private ResourceResponse(String message, boolean error, List<String> validationErrors) {
        this.message = message;
        this.error = error;
        this.validationErrors = validationErrors;
    }

    public static ResourceResponse ok() {
        return new ResourceResponse("ok", false, null);
    }

    public static ResourceResponse error(String message) {
        return new ResourceResponse(Objects.requireNonNull(message), true, null);
    }

    public static ResourceResponse validationError(List<String> validationErrors) {
        return new ResourceResponse("Request invalid", true, validationErrors);
    }
}
