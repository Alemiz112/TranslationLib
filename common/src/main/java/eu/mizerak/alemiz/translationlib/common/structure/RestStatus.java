package eu.mizerak.alemiz.translationlib.common.structure;

import lombok.Data;

@Data
public class RestStatus {
    public static final RestStatus OK = create("ok");

    private final String message;
    private final Status status;
    private final String error;

    public static RestStatus create(Throwable throwable) {
        return new RestStatus(throwable.getClass().getSimpleName(), Status.ERROR, throwable.getMessage());
    }

    public static RestStatus create(Status status, String message, String error) {
        return new RestStatus(message, status, error);
    }

    public static RestStatus create(Status status, String message) {
        return new RestStatus(message, status, null);
    }

    public static RestStatus create(String message) {
        return new RestStatus(message, Status.SUCCESS, null);
    }

    public boolean isSuccess() {
        return this.status == Status.SUCCESS;
    }

    public enum Status {
        SUCCESS,
        ERROR;
    }
}
