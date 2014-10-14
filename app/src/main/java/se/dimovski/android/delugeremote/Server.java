package se.dimovski.android.delugeremote;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by nihilist on 2014-10-11.
 */
public class Server
{
    final public String name;
    final public String hostname;
    final public String username;
    final public String password;

    public Server()
    {
        this("localhost", "", "");
    }
    public Server(String name, String hostname, String username, String password)
    {
        this.name = name;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    public Server(String hostname, String username, String password)
    {
        this(hostname, hostname, username, password);
    }

    public Server(Set<String> set)
    {
        String name = "";
        String hostname = "";
        String username = "";
        String password = "";

        for(String item : set)
        {
            char token = item.charAt(0);
            String original = item.substring(1);
            if(token == 'N')
                name = original;
            else if(token == 'H')
                hostname = original;
            else if(token == 'U')
                username = original;
            else if(token == 'P')
                password = original;
        }

        this.name = name;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    public Set<String> getAsSet()
    {
        Set<String> set = new LinkedHashSet<String>();
        set.add('N'+name);
        set.add('H'+hostname);
        set.add('U'+username);
        set.add('P'+password);
        return set;
    }
}
