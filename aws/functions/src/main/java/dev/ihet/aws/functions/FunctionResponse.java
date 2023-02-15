package dev.ihet.aws.functions;

public class FunctionResponse {

    public final String message;

    public final Integer errorCode;

    public FunctionResponse(String message, Integer errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
