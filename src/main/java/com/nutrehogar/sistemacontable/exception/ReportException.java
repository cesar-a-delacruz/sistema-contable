package com.nutrehogar.sistemacontable.exception;

public class ReportException extends AppException {
    public ReportException() {
        super();
    }

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportException(Throwable cause) {
        super(cause);
    }
}
