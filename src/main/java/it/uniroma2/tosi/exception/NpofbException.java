package it.uniroma2.tosi.exception;

public class NpofbException extends Exception{

    private String message;
    public NpofbException(){}

    public NpofbException(String message){
        this.message=message;
    }

}
