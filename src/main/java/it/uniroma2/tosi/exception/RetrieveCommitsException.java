package it.uniroma2.tosi.exception;

public class RetrieveCommitsException extends Exception{

    private final String message;

    public RetrieveCommitsException(String message){
        this.message=message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
