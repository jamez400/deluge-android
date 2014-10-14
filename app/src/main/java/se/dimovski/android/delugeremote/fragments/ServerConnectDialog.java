package se.dimovski.android.delugeremote.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import se.dimovski.android.delugeremote.R;
import se.dimovski.android.delugeremote.Server;
import se.dimovski.android.delugeremote.Settings;
import se.dimovski.android.delugeremote.exceptions.ConnectionFailedException;
import se.dimovski.android.delugeremote.exceptions.InvalidCredentialsException;

/**
 * Created by nihilist on 2014-10-11.
 */
public class ServerConnectDialog extends DialogFragment
{

    private EditText mServerName;
    private EditText mHostnameView;
    private EditText mUserView;
    private EditText mPasswordView;

    private LoginCallbacks mListener;

    public View setupView(View v)
    {
        // Set up the login form.
        mServerName = (EditText) v.findViewById(R.id.name);
        mHostnameView = (EditText) v.findViewById(R.id.hostname);
        mUserView = (EditText) v.findViewById(R.id.username);
        mPasswordView = (EditText) v.findViewById(R.id.password);

        Settings settings = new Settings(getActivity());
        Server defaultServer = settings.getDefaultServer();

        if(defaultServer != null)
        {
            if(!defaultServer.hostname.isEmpty())
            {
                mHostnameView.setText(defaultServer.hostname);

                if(!defaultServer.name.isEmpty() && !defaultServer.hostname.equals(defaultServer.name))
                {
                    mServerName.setText(defaultServer.name);
                }

                if(!defaultServer.username.isEmpty())
                {
                    mUserView.setText(defaultServer.username);
                }

                if(!defaultServer.password.isEmpty())
                {
                    mPasswordView.setText(defaultServer.password);
                }
            }

        }

        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = setupView(inflater.inflate(R.layout.login, null));

        builder.setView(v)
                // Add action buttons
                .setPositiveButton("Connect", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        attemptConnect();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ServerConnectDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public void attemptConnect()
    {
        // Reset errors.
        mHostnameView.setError(null);
        mUserView.setError(null);
        mPasswordView.setError(null);


        String name = getFieldText(mServerName);
        String hostname = getFieldText(mHostnameView);
        String username = getFieldText(mUserView);
        String password = getFieldText(mPasswordView);

        boolean incomplete = hostname.isEmpty() || username.isEmpty() || password.isEmpty();

        if (mListener != null && !incomplete)
        {
            try
            {
                mListener.onLogin(name, hostname, username, password);
            }
            catch (ConnectionFailedException e)
            {
                mHostnameView.requestFocus();
                Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_SHORT).show();
            }
            catch (InvalidCredentialsException e)
            {
                if(e.incorrectUsername())
                {
                    mUserView.requestFocus();
                    Toast.makeText(getActivity(), "Unknown user", Toast.LENGTH_SHORT).show();
                }
                else if(e.incorrectPassword())
                {
                    mPasswordView.requestFocus();
                    Toast.makeText(getActivity(), "Incorrect password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getActivity(), "Unknown Error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getFieldText(EditText field)
    {
        String value = field.getText().toString();
        if (TextUtils.isEmpty(value))
        {
            field.setError(getString(R.string.error_field_required));
            field.requestFocus();
        }
        return value;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (LoginCallbacks) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement LoginCallbacks");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    public interface LoginCallbacks
    {
        /**
         * Attempt to login
         *
         * @return True, if connection was successful
         */
        public boolean onLogin(String name, String host, String username, String password) throws ConnectionFailedException, InvalidCredentialsException;
    }
}
