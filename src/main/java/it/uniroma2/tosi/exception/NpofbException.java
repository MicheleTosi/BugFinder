package it.uniroma2.tosi.exception;

public class NpofbException extends Exception{

    private final String message;

    public NpofbException(String message){
        this.message=message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
