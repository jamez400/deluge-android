package se.dimovski.android.delugeremote.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import deluge.message.Response;
import deluge.rpc.Deluge;
import se.dimovski.android.delugeremote.DelugeMethods;
import se.dimovski.android.delugeremote.R;
import se.dimovski.android.delugeremote.Server;
import se.dimovski.android.delugeremote.Settings;
import se.dimovski.android.delugeremote.exceptions.ConnectionFailedException;
import se.dimovski.android.delugeremote.exceptions.InvalidCredentialsException;
import se.dimovski.android.delugeremote.fragments.AddTorrentFragment;
import se.dimovski.android.delugeremote.fragments.ServerConnectDialog;
import se.dimovski.android.delugeremote.fragments.TorrentFilesFragment;
import se.dimovski.android.delugeremote.adapters.FragmentPageAdapter;

/**
 * Created by nihilist on 2014-10-06.
 */
public class AddTorrent extends Activity implements ServerConnectDialog.LoginCallbacks, AddTorrentFragment.AddTorrentCallbacks, TorrentFilesFragment.TorrentFilesCallbacks
{

    private Deluge deluge = null;

    FragmentPageAdapter mSwipeAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_view_pager);

        File file = null;
        if(getIntent() != null)
        {
            Uri uri = getIntent().getData();
            if(uri != null)
            {
                file = new File(uri.getPath());
            }
        }

        if(file != null)
        {
            List<Fragment> mFragments = new ArrayList<Fragment>();
            mFragments.add(AddTorrentFragment.newInstance(file.getPath(), file.getName()));
            mFragments.add(TorrentFilesFragment.newInstance("",""));
            mSwipeAdapter = new FragmentPageAdapter(getFragmentManager(), mFragments, new String[] {"Options", "Files"});

            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSwipeAdapter);
        }

    }

    @Override
    public boolean onLogin(String name, String host, String username, String password) throws ConnectionFailedException, InvalidCredentialsException
    {
        if(deluge == null)
        {
            deluge = DelugeMethods.connect(host, username, password);
        }

        Settings settings = new Settings(this);
        settings.saveServerInfo(new Server(name, host, username, password));

        // Complete go to default fragment
        if(getCurrentFocus() != null)
        {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        return true;
    }


    @Override
    public void addTorrentFile(String filepath, Map<String, Object> options)
    {
        Settings settings = new Settings(this);
        try
        {
            if(deluge == null)
            {
                Server server = settings.getDefaultServer();
                deluge = DelugeMethods.connect(server.hostname, server.username, server.password);
            }
        }
        catch (Exception e)
        {
            DialogFragment dialog = new ServerConnectDialog();
            dialog.show(getFragmentManager(), "ServerDialogFragment");
        }

        try
        {
            File torrent = new File(filepath);
            Future<Response> resp = deluge.addTorrentFile(torrent.getName(), fileToEncodedString(torrent), options);
            resp.get(3, TimeUnit.SECONDS);
            this.finish();
        }
        catch (Throwable e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String fileToEncodedString(File torrent)
    {
        String data = null;
        byte[] torrentData = convertFileToByteArray(torrent);
        if(torrentData != null)
        {
            data = Base64.encodeToString(torrentData, Base64.NO_WRAP);
        }
        return data;
    }

    public static byte[] convertFileToByteArray(File file)
    {
        byte[] byteArray = null;
        try
        {
            FileInputStream fis = new java.io.FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024*8];
            int bytesRead =0;

            while ((bytesRead = fis.read(b)) != -1)
            {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return byteArray;
    }

    @Override
    public void torrentFilesUpdated(Uri uri)
    {
    }
}
