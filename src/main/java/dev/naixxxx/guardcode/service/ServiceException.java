package dev.naixxxx.guardcode.service;

public class ServiceException extends RuntimeException {
    private final int httpStatus;
    public ServiceException(int httpStatus, String message) { super(message); this.httpStatus = httpStatus; }
    public int httpStatus() { return httpStatus; }
}
