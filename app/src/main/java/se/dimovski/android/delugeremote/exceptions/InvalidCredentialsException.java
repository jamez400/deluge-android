package se.dimovski.android.delugeremote.exceptions;

/**
 * Created by nihilist on 2014-09-28.
 */
public class InvalidCredentialsException extends Exception {

    public enum ErrorType {
        UsernameNotFound,
        PasswordIncorrect
    }

    private ErrorType errorType;
    private String errorMessage;

    public InvalidCredentialsException(deluge.message.Error error)
    {
        errorMessage = error.exceptionMsg;

        if(error.exceptionMsg.contains("Username"))
        {
            errorType = ErrorType.UsernameNotFound;
        }
        else
        {
            errorType = ErrorType.PasswordIncorrect;
        }
    }

    public boolean incorrectUsername()
    {
        return errorType == ErrorType.UsernameNotFound;
    }
    public boolean incorrectPassword()
    {
        return errorType == ErrorType.PasswordIncorrect;
    }

    public ErrorType getErrorType()
    {
        return errorType;
    }

    public String getMessage()
    {
        return errorMessage;
    }
}
