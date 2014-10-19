package se.dimovski.android.delugeremote;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import deluge.api.response.Response;
import deluge.impl.DelugeClient;
import deluge.impl.DelugeSession;
import se.dimovski.android.delugeremote.exceptions.ConnectionFailedException;
import se.dimovski.android.delugeremote.exceptions.InvalidCredentialsException;

/**
 * Created by nihilist on 2014-09-29.
 */
public class DelugeMethods
{
    private static DelugeSession mCurrentConnection = null;
    private static String mHostName = null;

    public static DelugeSession getCurrentHost()
    {
        return mCurrentConnection;
    }

    public static DelugeSession connect(String host, String username, String password) throws ConnectionFailedException, InvalidCredentialsException
    {
        if(mHostName != null && mHostName.equals(host))
        {
            return mCurrentConnection;
        }

        try
        {
            mCurrentConnection = DelugeClient.getSession(host);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ConnectionFailedException();
        }

        try
        {
            mCurrentConnection.login(username, password).get(5, TimeUnit.SECONDS);
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
