package se.dimovski.android.delugeremote;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by nihilist on 2014-09-28.
 */
public class Settings
{
    private SharedPreferences prefs;

    public Settings(Activity activity)
    {
        prefs = activity.getSharedPreferences ("DelugeServerSettings1",Context.MODE_PRIVATE);
    }

    public Set<String> getServerNames()
    {
        return prefs.getStringSet("servers", new LinkedHashSet<String>());
    }

    public Set<Server> getServers()
    {
        Set<Server> set = new HashSet<Server>();
        for(String name : getServerNames())
        {
            set.add(getServerInfo(name));
        }
        return set;
    }

    public Server getDefaultServer()
    {
        String serverName = prefs.getString("default_server", "");
        Server server = getServerInfo(serverName);
        if(server == null)
            server = new Server();
        return server;
    }

    public void setDefaultServer(String name)
    {
        prefs.edit().putString("default_server", name).commit();
    }

    public Server getServerInfo(String name)
    {
        Set<String> serverInfo = prefs.getStringSet(name, null);
        if(serverInfo != null && serverInfo.size() == 4)
        {
            return new Server(serverInfo);
        }
        return null;
    }

    public void saveServerInfo(Server server)
    {
        Set<String> servers = getServerNames();
        servers.add(server.name);
        prefs.edit().putStringSet("servers", servers).commit();

        prefs.edit().putStringSet(server.name, server.getAsSet()).commit();
    }

    public Set<String> getCachedTorrents(Server server)
    {
        return prefs.getStringSet("torrent_cache_"+server.hostname, Collections.EMPTY_SET);
    }

    public void setCachedTorrents(Server server, Set<String> ids)
    {
        prefs.edit().putStringSet("torrent_cache_"+server.hostname, ids).commit();
    }
}
