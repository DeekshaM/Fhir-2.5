package ca.uhn.fhir.security;

public class AiravataSecurityException extends Exception {
    public AiravataSecurityException(String message) {
        super(message);
    }

    public AiravataSecurityException() {
        super();
    }
}
