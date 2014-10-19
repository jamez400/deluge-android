package se.dimovski.android.delugeremote.data.filters;

import deluge.impl.net.TorrentField;
import se.dimovski.android.delugeremote.data.TorrentInfo;

/**
 * Created by nihilist on 2014-10-04.
 */
public class Filters
{
    public static Predicate<TorrentInfo> isFinished()
    {
        return booleanFieldFilter(TorrentField.IS_FINISHED, true);
    }

    public static Predicate<TorrentInfo> notFinished()
    {
        return booleanFieldFilter(TorrentField.IS_FINISHED, false);
    }

    public static Predicate<TorrentInfo> isPaused()
    {
        return booleanFieldFilter(TorrentField.PAUSED, true);
    }

    public static Predicate<TorrentInfo> notPaused()
    {
        return booleanFieldFilter(TorrentField.PAUSED, false);
    }

    public static Predicate<TorrentInfo> unfiltered()
    {
        return new Predicate<TorrentInfo>() {
            @Override
            public boolean apply(TorrentInfo type)
            {
                return true;
            }
        };
    }

    private static Predicate<TorrentInfo> booleanFieldFilter(final TorrentField field, final boolean finished)
    {
        return new Predicate<TorrentInfo>() {
            @Override
            public boolean apply(TorrentInfo type)
            {
                if(!type.hasValue(field))
                    return false;
                return type.getBool(field) == finished;
            }
        };
    }

}
