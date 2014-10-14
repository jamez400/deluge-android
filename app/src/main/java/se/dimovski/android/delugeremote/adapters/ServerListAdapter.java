package se.dimovski.android.delugeremote.adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.f2prateek.progressbutton.ProgressButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import deluge.net.TorrentField;
import se.dimovski.android.delugeremote.R;
import se.dimovski.android.delugeremote.Server;
import se.dimovski.android.delugeremote.Settings;
import se.dimovski.android.delugeremote.data.TorrentInfo;
import se.dimovski.android.delugeremote.data.filters.Filters;
import se.dimovski.android.delugeremote.data.filters.OrderBy;
import se.dimovski.android.delugeremote.data.filters.Predicate;

/**
 * Created by nihilist on 2014-09-30.
 */
public class ServerListAdapter extends BaseAdapter
{
    private Context mContext;
    private Settings mSettings;
    private Server[] mServers;

    public ServerListAdapter(Context context, Settings settings)
    {
        super();
        mContext = context;
        mSettings = settings;
    }

    private void update()
    {
        Set<Server> servers = mSettings.getServers();
        if(mServers == null || mServers.length != servers.size())
        {
            mServers = servers.toArray(new Server[0]);
        }
    }

    @Override
    public int getCount()
    {
        update();
        return mSettings.getServerNames().size();
    }

    @Override
    public Server getItem(int i)
    {
        update();
        return mServers[i];
    }

    @Override
    public long getItemId(int i)
    {
        return -1;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        View v = view;
        if (v == null)
        {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.server_list_item, viewGroup, false);
        }

        Server server = getItem(i);

        TextView nameView = (TextView) v.findViewById(R.id.server_item_name);
        if (nameView != null && server != null)
        {
            String name = server.name.trim();
            if(name.isEmpty())
            {
                name = server.hostname;
            }
            nameView.setText(name);
        }

        return v;
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    @Override
    public boolean isEnabled(int arg0)
    {
        return true;
    }
}