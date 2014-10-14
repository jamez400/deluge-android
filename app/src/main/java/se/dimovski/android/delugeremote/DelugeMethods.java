package se.dimovski.android.delugeremote;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import deluge.message.Response;
import deluge.rpc.Deluge;
import se.dimovski.android.delugeremote.exceptions.ConnectionFailedException;
import se.dimovski.android.delugeremote.exceptions.InvalidCredentialsException;

/**
 * Created by nihilist on 2014-09-29.
 */
public class DelugeMethods
{
    private static Deluge mCurrentConnection = null;
    private static String mHostName = null;

    public static Deluge getCurrentHost()
    {
        return mCurrentConnection;
    }

    public static Deluge connect(String host, String username, String password) throws ConnectionFailedException, InvalidCredentialsException
    {
        if(mHostName != null && mHostName.equals(host))
        {
            return mCurrentConnection;
        }

        try
        {
            mCurrentConnection = Deluge.connect(host);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ConnectionFailedException();
        }

        try
        {
            Response resp = mCurrentConnection.login(username, password).get(5, TimeUnit.SECONDS);
            if(resp.hasError())
            {
                throw new InvalidCredentialsException(resp.getError());
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        catch (TimeoutException e)
        {
            e.printStackTrace();
        }

        return mCurrentConnection;
    }
}
