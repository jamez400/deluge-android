package se.dimovski.android.delugeremote.exceptions;

import deluge.api.DelugeException;

/**
 * Created by nihilist on 2014-09-28.
 */
public class InvalidCredentialsException extends Exception {

    public enum ErrorType {
        UsernameNotFound,
        PasswordIncorrect
    }

    private final DelugeException mCause;

    public InvalidCredentialsException(DelugeException cause)
    {
        mCause = cause;
    }

    // TODO: fix differentiating
    public boolean incorrectUsername()
    {
        return ErrorType.UsernameNotFound == ErrorType.UsernameNotFound;
    }

    // TODO: fix differentiating
    public boolean incorrectPassword()
    {
        return ErrorType.PasswordIncorrect == ErrorType.PasswordIncorrect;
    }

    public String getMessage()
    {
        return mCause.getMessage();
    }
}
