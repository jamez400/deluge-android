package se.dimovski.android.delugeremote.activities;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import deluge.api.DelugeFuture;
import deluge.api.ResponseCallback;
import deluge.api.response.IntegerResponse;
import deluge.api.response.Response;
import deluge.api.response.TorrentsStatusResponse;
import deluge.impl.DelugeClient;
import deluge.impl.DelugeSession;
import deluge.impl.net.TorrentField;
import se.dimovski.android.delugeremote.DelugeMethods;
import se.dimovski.android.delugeremote.ProgressUpdater;
import se.dimovski.android.delugeremote.R;
import se.dimovski.android.delugeremote.Server;
import se.dimovski.android.delugeremote.Settings;
import se.dimovski.android.delugeremote.data.filters.Filters;
import se.dimovski.android.delugeremote.data.filters.OrderBy;
import se.dimovski.android.delugeremote.exceptions.ConnectionFailedException;
import se.dimovski.android.delugeremote.exceptions.InvalidCredentialsException;
import se.dimovski.android.delugeremote.fragments.NavigationDrawerFragment;
import se.dimovski.android.delugeremote.fragments.ServerConnectDialog;
import se.dimovski.android.delugeremote.fragments.TorrentListFragment;
import se.dimovski.android.delugeremote.data.TorrentInfo;

import static se.dimovski.android.delugeremote.data.filters.OrderBy.SortOrder.*;


public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        TorrentListFragment.TorrentListCallbacks, ServerConnectDialog.LoginCallbacks
{
    private Settings settings;

    TorrentListFragment mTorrentListFragment;

    ProgressUpdater mProgressUpdater;
    AlarmManager am;
    PendingIntent pi;
    BroadcastReceiver br;

    private DelugeFuture<TorrentsStatusResponse> mUpdateTorrentDetailsCallback = new DelugeFuture<TorrentsStatusResponse>() {
        @Override
        public void onResponse(TorrentsStatusResponse response)
        {
            mTorrentListFragment.updateTorrentInfo(response.getReturnValue());
            invalidateOptionsMenu();
        }
    };


    @Override
    public void onTorrentClicked(String torrentId)
    {
        // Switch to torrent details?
        invalidateOptionsMenu();
    }

    @Override
    public boolean onLogin(String name, String host, String username, String password) throws ConnectionFailedException, InvalidCredentialsException
    {
        DelugeSession deluge = DelugeMethods.connect(host, username, password);

        Server server = new Server(name, host, username, password);
        settings.saveServerInfo(server);
        settings.setDefaultServer(server.name);

        Set<String> torrentIds = settings.getCachedTorrents(server);

        if(!torrentIds.isEmpty())
        {
            loadTorrentDetails(torrentIds);
        }
        getTorrentIdNames();

        // Complete go to default fragment
        if(getCurrentFocus() != null)
        {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        onNavigationDrawerItemSelected(0);
        return true;
    }

    private void getTorrentIdNames()
    {
        Log.d("deluge", "Initial GET");

        DelugeFuture<TorrentsStatusResponse> req = DelugeMethods.getCurrentHost().getTorrentsStatus(null, new TorrentField[] { TorrentField.PAUSED });
        req.then(new DelugeFuture<TorrentsStatusResponse>() {
            @Override
            public void onResponse(TorrentsStatusResponse response)
            {
                Set<String> torrentIds = response.getReturnValue().keySet();
                Log.d("deluge", "Total number of torrents: " + torrentIds.size());
                loadTorrentDetails(torrentIds);
            }
        });
    }
    private void loadTorrentDetails(Set<String> ids)
    {
        mTorrentListFragment.setTorrentIds(ids);
        settings.setCachedTorrents(settings.getDefaultServer(), ids);

        invalidateOptionsMenu();
        mProgressUpdater.start();

        pollDownloads();
        setupPollingAlarm(250);
    }

    private void doRequest(String[] torrentIds)
    {
        Map<Object, Object> filter = new HashMap<Object, Object>();
        filter.put("id", torrentIds);
        DelugeMethods.getCurrentHost().getTorrentsStatus(filter, TorrentInfo.FIELDS).then(mUpdateTorrentDetailsCallback);
    }

    private void pollTorrents()
    {
        List<String> ids = new ArrayList<String>();
        for(String torrentId : mTorrentListFragment.getTorrentIds())
        {
            ids.add(torrentId);
            if(ids.size() >= 25)
            {
                doRequest(ids.toArray(new String[0]));
                ids.clear();
            }
        }
        if(!ids.isEmpty())
        {
            doRequest(ids.toArray(new String[0]));
        }
    }

    private void pollDownloads()
    {
        List<TorrentInfo> downloading = mTorrentListFragment.getListByFilter(Filters.notFinished());
        downloading.addAll(mTorrentListFragment.getListByVisibility());

        List<String> ids = new ArrayList<String>();
        for(TorrentInfo torrent : downloading)
        {
            ids.add(torrent.id);
        }
        doRequest(ids.toArray(new String[0]));

        String[] refreshTorrents = mTorrentListFragment.getTorrentIdsFromQueue(10).toArray(new String[0]);
        doRequest(refreshTorrents);
    }

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = new Settings(this);


        ProgressBar progressBar = (ProgressBar) findViewById(R.id.activity_bar);

        mProgressUpdater = new ProgressUpdater(progressBar){
            @Override
            public float getProgress()
            {
                float progress = mTorrentListFragment.getProgress();
                return mTorrentListFragment.getProgress();
            }

            @Override
            public void onComplete()
            {
                setupPollingAlarm(1000);
            }
        };

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Server server = settings.getDefaultServer();
        if(server == null)
        {
            openLoginFragment();
        }
        else
        {
            try
            {
                onLogin(server.name, server.hostname, server.username, server.password);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                openLoginFragment();
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        // update the main content by replacing fragments
        mTorrentListFragment = TorrentListFragment.newInstance();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mTorrentListFragment)
                .commit();

        ActionBar actionBar = getActionBar();
        switch (position+1)
        {
            case 1:
                actionBar.setSubtitle( getString(R.string.title_section1) );
                mTorrentListFragment.filter(Filters.notFinished());
                break;
            case 2:
                actionBar.setSubtitle( getString(R.string.title_section2) );
                mTorrentListFragment.filter(Filters.notPaused());
                break;
            case 3:
                actionBar.setSubtitle( getString(R.string.title_section3) );
                mTorrentListFragment.filter(Filters.unfiltered());
                break;
            default:
                actionBar.setSubtitle("");
                break;
        }
    }

    @Override
    public void onAddServerClicked()
    {
        openLoginFragment();
    }

    public void restoreActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.ic_launcher);
    }

    public void torrentActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setIcon(R.drawable.abc_ic_cab_done_holo_dark);
        //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //actionBar.setCustomView(R.layout.actionbar_torrent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (!mNavigationDrawerFragment.isDrawerOpen())
        {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.

            if(mTorrentListFragment.getSelectedTorrents().isEmpty())
            {
                getMenuInflater().inflate(R.menu.main, menu);
                restoreActionBar();
            }
            else
            {
                getMenuInflater().inflate(R.menu.torrent, menu);

                MenuItem pauseResume = menu.findItem(R.id.action_pause_resume);
                if(isAnySelectedNotPaused())
                {
                    pauseResume.setIcon(R.drawable.ic_action_pause);
                }
                else
                {
                    pauseResume.setIcon(R.drawable.ic_action_resume);
                }
                torrentActionBar();
            }

            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(orderByActions(id))
            return true;

        if(torrentActions(id))
            return true;

        if(id == R.id.action_settings)
        {
            return true;
        }
        else if(id == R.id.action_server_settings)
        {
            openLoginFragment();
            return true;
        }
        else if(id == R.id.action_search)
        {
            Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean orderByActions(int id)
    {
        if(id == R.id.orderByTimeAdded)
        {
            mTorrentListFragment.orderBy(OrderBy.timeAdded(DESC));
            return true;
        }
        else if(id == R.id.orderByProgress)
        {
            mTorrentListFragment.orderBy(OrderBy.progress());
            return true;
        }
        else if(id == R.id.orderByFileSize)
        {
            mTorrentListFragment.orderBy(OrderBy.fileSize(DESC));
            return true;
        }

        return false;
    }

    private boolean torrentActions(int id)
    {
        Set<String> selected = mTorrentListFragment.getSelectedTorrents();

        if(id == R.id.action_pause_resume)
        {
            List<String> torrents = new ArrayList<String>();
            torrents.addAll(selected);
            DelugeFuture<IntegerResponse> resp; // TODO: update paused status on response
            if(isAnySelectedNotPaused())
            {
                resp = DelugeMethods.getCurrentHost().pauseTorrent(torrents);
            }
            else
            {
                resp = DelugeMethods.getCurrentHost().resumeTorrent(torrents);
            }
            try
            {
                Response res = resp.get(3, TimeUnit.SECONDS);
                doRequest(torrents.toArray(new String[0]));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if(id == R.id.action_remove)
        {
            Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
            pollTorrents();
            return true;
        }
        else if(id == android.R.id.home)
        {
            if(mTorrentListFragment != null && !selected.isEmpty())
            {
                mTorrentListFragment.clearSelection();
                restoreActionBar();
                return true;
            }
        }

        return false;
    }

    private boolean isAnySelectedNotPaused()
    {
        for(String torrentId : mTorrentListFragment.getSelectedTorrents())
        {
            TorrentInfo info = mTorrentListFragment.getTorrent(torrentId);
            if(info != null && !info.isTrue(TorrentField.PAUSED) || !info.isTrue(TorrentField.IS_SEED))
            {
                return true;
            }
        }
        return false;
    }

    private void openLoginFragment()
    {
        DialogFragment dialog = new ServerConnectDialog();
        dialog.show(getFragmentManager(), "ServerDialogFragment");
    }

    public void setupPollingAlarm(int timeDelay)
    {

        if(am == null)
        {
            am = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
        }
        if(pi == null)
        {
            pi = PendingIntent.getBroadcast(this, 0, new Intent("se.dimovski.android.delugeremote.poll"), 0);
        }
        if(br == null)
        {
            br = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context c, Intent i)
                {
                    pollDownloads();
                }
            };

            registerReceiver(br, new IntentFilter("se.dimovski.android.delugeremote.poll"));
        }

        am.cancel(pi);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), timeDelay, pi);
    }
}
