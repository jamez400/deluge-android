package se.dimovski.android.delugeremote.data.filters;

import java.util.Comparator;

import deluge.net.TorrentField;
import se.dimovski.android.delugeremote.data.TorrentInfo;

/**
 * Created by nihilist on 2014-10-04.
 */
public class OrderBy
{
    public static enum SortOrder {
        ASC, DESC
    };

    public static Comparator<TorrentInfo> eta()
    {
        return eta(SortOrder.ASC);
    }

    public static Comparator<TorrentInfo> eta(SortOrder order)
    {
        return integerFieldSorter(TorrentField.ETA, order(order));
    }

    public static Comparator<TorrentInfo> progress()
    {
        return progress(SortOrder.ASC);
    }

    public static Comparator<TorrentInfo> progress(SortOrder order)
    {
        return floatFieldSorter(TorrentField.PROGRESS, order(order));
    }

    public static Comparator<TorrentInfo> timeAdded()
    {
        return timeAdded(SortOrder.ASC);
    }

    public static Comparator<TorrentInfo> timeAdded(SortOrder order)
    {
        return floatFieldSorter(TorrentField.TIME_ADDED, order(order));
    }

    public static Comparator<TorrentInfo> fileSize()
    {
        return fileSize(SortOrder.ASC);
    }

    public static Comparator<TorrentInfo> fileSize(SortOrder order)
    {
        return integerFieldSorter(TorrentField.TOTAL_SIZE, order(order));
    }

    private static Comparator<TorrentInfo> booleanFieldSorter(final TorrentField field, final boolean ascending)
    {
        return new Comparator<TorrentInfo>()
        {
            @Override
            public int compare(TorrentInfo torrentInfo, TorrentInfo torrentInfo2)
            {
                if(!torrentInfo.hasValue(field) || !torrentInfo.hasValue(field))
                    return 0;
                Boolean fieldValue = torrentInfo.getBool(field);
                Boolean fieldValue2 = torrentInfo2.getBool(field);

                if(ascending)
                {
                    return fieldValue.compareTo(fieldValue2);
                }
                return fieldValue2.compareTo(fieldValue);

            }
        };
    }

    private static Comparator<TorrentInfo> integerFieldSorter(final TorrentField field, final boolean ascending)
    {
        return new Comparator<TorrentInfo>()
        {
            @Override
            public int compare(TorrentInfo torrentInfo, TorrentInfo torrentInfo2)
            {
                if(!torrentInfo.hasValue(field) || !torrentInfo.hasValue(field))
                    return 0;
                Long fieldValue = torrentInfo.getNumber(field).longValue();
                Long fieldValue2 = torrentInfo2.getNumber(field).longValue();

                if(ascending)
                {
                    return fieldValue.compareTo(fieldValue2);
                }
                return fieldValue2.compareTo(fieldValue);

            }
        };
    }

    private static Comparator<TorrentInfo> floatFieldSorter(final TorrentField field, final boolean ascending)
    {
        return new Comparator<TorrentInfo>()
        {
            @Override
            public int compare(TorrentInfo torrentInfo, TorrentInfo torrentInfo2)
            {
                if(!torrentInfo.hasValue(field) || !torrentInfo2.hasValue(field))
                    return 0;
                Float fieldValue = torrentInfo.getFloat(field);
                Float fieldValue2 = torrentInfo2.getFloat(field);

                if(ascending)
                {
                    return fieldValue.compareTo(fieldValue2);
                }
                return fieldValue2.compareTo(fieldValue);

            }
        };
    }

    private static boolean order(SortOrder order)
    {
        if (order == SortOrder.ASC)
            return true;
        return false;
    }

}
