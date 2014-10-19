package se.dimovski.android.delugeremote.data;

import java.util.Map;

import deluge.impl.net.TorrentField;


/**
 * Created by nihilist on 2014-09-27.
 */
public class TorrentInfo
{
    public static TorrentField[] FIELDS = new TorrentField[] {
            TorrentField.NAME,
            TorrentField.TIME_ADDED,
            TorrentField.ETA,
            TorrentField.IS_FINISHED,
            TorrentField.IS_SEED,
            TorrentField.NUM_PEERS,
            TorrentField.NUM_SEEDS,
            TorrentField.PAUSED,
            TorrentField.PROGRESS,
            TorrentField.TOTAL_PEERS,
            TorrentField.TOTAL_SEEDS,
            TorrentField.DOWNLOAD_PAYLOAD_RATE,
            TorrentField.UPLOAD_PAYLOAD_RATE,
            TorrentField.TOTAL_SIZE,
            TorrentField.IS_AUTO_MANAGED,
            TorrentField.STATE
    };

    public final String id;
    private Map<String, Object> data;

    public TorrentInfo(String id, Map<String, Object> data)
    {
        this.id = id;
        this.data = data;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        TorrentInfo that = (TorrentInfo) o;

        if (data != null ? !data.equals(that.data) : that.data != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }


    public boolean hasValue(TorrentField field)
    {
        return data.get(field.toString()) != null;
    }

    public Object get(TorrentField field)
    {
        if(!hasValue(field)) return null;
        return data.get(field.toString());
    }

    public String getString(TorrentField field)
    {
        if(!hasValue(field)) return null;
        return String.valueOf(get(field));
    }

    public Boolean getBool(TorrentField field)
    {
        if(!hasValue(field)) return null;
        return (Boolean)get(field);
    }

    public boolean isTrue(TorrentField field)
    {
        if(hasValue(field))
        {
            return getBool(field);
        }
        return false;
    }

    public Integer getInteger(TorrentField field)
    {
        if(!hasValue(field)) return null;
        return (Integer)get(field);
    }

    public Number getNumber(TorrentField field)
    {
        if(!hasValue(field)) return null;
        return (Number)get(field);
    }

    public Long getLong(TorrentField field)
    {
        if(!hasValue(field)) return null;
        return (Long)get(field);
    }

    public Float getFloat(TorrentField field)
    {
        if(!hasValue(field)) return null;
        return (Float)get(field);
    }

    @Override
    public String toString()
    {
        return get(TorrentField.NAME).toString();
    }
}
