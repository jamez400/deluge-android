package se.dimovski.android.delugeremote.adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.f2prateek.progressbutton.ProgressButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import deluge.impl.net.TorrentField;
import se.dimovski.android.delugeremote.R;
import se.dimovski.android.delugeremote.data.TorrentInfo;
import se.dimovski.android.delugeremote.data.filters.Filters;
import se.dimovski.android.delugeremote.data.filters.OrderBy;
import se.dimovski.android.delugeremote.data.filters.Predicate;

/**
 * Created by nihilist on 2014-09-30.
 */
public class TorrentDataSetAdapter extends BaseAdapter
{
    final private Handler mHandler = new Handler();

    private Context mContext;

    private Predicate<TorrentInfo> mFilter = Filters.notFinished();
    private Comparator<TorrentInfo> mOrderBy = OrderBy.timeAdded();

    private boolean mDataSetInvalid;
    private Map<String, Map<String, Object>> mDataSet;
    private List<TorrentInfo> mFilteredList;

    public TorrentDataSetAdapter(Context context, int resource)
    {
        super();
        mContext = context;

        mDataSet = new ConcurrentHashMap<String, Map<String, Object>>();
        mFilteredList = new ArrayList<TorrentInfo>();
        mDataSetInvalid = true;
    }

    public void filter(Predicate<TorrentInfo> predicate)
    {
        mFilter = predicate;
        mDataSetInvalid = true;
        updateDataSet();
    }

    public void orderBy(Comparator<TorrentInfo> comparator)
    {
        mOrderBy = comparator;
        mDataSetInvalid = true;
        updateDataSet();
    }

    private volatile boolean isQueued = false;
    private void updateDataSetDelayed(long milis)
    {
        if(!isQueued)
        {
            isQueued = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    updateDataSet();
                    isQueued = false;
                }
            }, milis);
        }
    }

    public List<TorrentInfo> getListByFilter(Predicate<TorrentInfo> filter)
    {
        List<TorrentInfo> list =  new ArrayList<TorrentInfo>();

        for(Map.Entry<String, Map<String, Object>> entry : mDataSet.entrySet())
        {
            TorrentInfo torrent = new TorrentInfo(entry.getKey(), entry.getValue());
            if (filter.apply(torrent))
            {
                list.add(torrent);
            }
        }
        return list;
    }

    private void updateDataSet()
    {
        List<TorrentInfo> list =  getListByFilter(mFilter);
        Collections.sort(list, mOrderBy);

        mFilteredList = list;
        mDataSetInvalid = false;

        notifyDataSetChanged();
    }

    public TorrentInfo getItem(String id)
    {
        return new TorrentInfo(id, mDataSet.get(id));
    }

    @Override
    public int getCount()
    {
        //checkDataSet();
        return mFilteredList.size();
    }

    @Override
    public TorrentInfo getItem(int i)
    {
        //checkDataSet();
        return mFilteredList.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;

        if (v == null)
        {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.torrent_list_item, parent, false);
        }

        TorrentInfo item = getItem(position);

        if (item != null)
        {
            putString(v, R.id.torrent_name, item.toString());
            if(item.get(TorrentField.TIME_ADDED) == null)
            {
                return v;
            }

            String eta = "";
            if(item.getBool(TorrentField.PAUSED) || item.getBool(TorrentField.IS_SEED))
            {
                eta = item.getString(TorrentField.STATE);
            }
            else
            {
                Integer sec = item.getInteger(TorrentField.ETA);
                if(sec == 0)
                {
                    eta = "-";
                }
                else if(TimeUnit.SECONDS.toHours(sec) > 0)
                {
                    eta = getTime(TimeUnit.SECONDS.toHours(sec), "h");
                }
                else if(TimeUnit.SECONDS.toMinutes(sec) > 0)
                {
                    eta = getTime(TimeUnit.SECONDS.toHours(sec), "min");
                }
                else
                {
                    eta = sec.toString() + " sec";
                }
            }
            putString(v, R.id.torrent_eta, eta);

            String peers = item.getString(TorrentField.NUM_PEERS) + " (" +item.getString(TorrentField.TOTAL_PEERS) + ")";
            if(item.getBool(TorrentField.IS_SEED))
            {
                peers = item.getString(TorrentField.NUM_SEEDS) + " (" +item.getString(TorrentField.TOTAL_SEEDS) + ")";
            }
            putString(v, R.id.peers, peers);

            Number bytes = item.getNumber(TorrentField.TOTAL_SIZE);
            putString(v, R.id.file_size, formatBytes(bytes.longValue()));

            String rate = "0 KB/s";
            if(item.getBool(TorrentField.IS_FINISHED))
            {
                rate = formatBytes(item.getInteger(TorrentField.UPLOAD_PAYLOAD_RATE).longValue())+"/s";
            }
            else
            {
                rate = formatBytes(item.getInteger(TorrentField.DOWNLOAD_PAYLOAD_RATE).longValue())+"/s";
            }
            putString(v, R.id.data_rate, rate);

            float progress = item.getFloat(TorrentField.PROGRESS);
            ProgressButton progressButton1 = (ProgressButton) v.findViewById(R.id.icon);
            progressButton1.setProgressAndMax((int)progress, 100);

            int color = mContext.getResources().getColor(R.color.deluge_progress);
            int circleColor = mContext.getResources().getColor(R.color.white);
            if(item.getBool(TorrentField.PAUSED))
            {
                color = mContext.getResources().getColor(R.color.deluge_progress_paused);
                circleColor = mContext.getResources().getColor(R.color.deluge_progress_paused);
            }
            else if(progress >= 100)
            {
                color = mContext.getResources().getColor(R.color.deluge_progress_complete);
                circleColor = mContext.getResources().getColor(R.color.deluge_progress_complete);
            }
            progressButton1.setProgressColor(color);
            progressButton1.setCircleColor(circleColor);
        }
        return v;
    }

    private String getTime(Long value, String unit)
    {
        return value.toString()+" "+unit;
    }

    private String formatBytes(Long bytes)
    {
        return bytesToLower((float)bytes)+" "+bytesSizeExt(bytes);
    }

    private String bytesToLower(Float value)
    {
        if(value < 1024)
        {
            String output = String.format("%.1f", value);
            output = output.replaceAll(".0", "");
            return output;
        }
        return bytesToLower(value / 1024.0f);
    }

    private String bytesSizeExt(Long bytes)
    {
        if(bytes < 1024)
            return "B";
        else if(bytes < Math.pow(1024, 2))
            return "KB";
        else if(bytes < Math.pow(1024, 3))
            return "MB";
        else if(bytes < Math.pow(1024, 4))
            return "GB";
        else if(bytes < Math.pow(1024, 5))
            return "TB";
        else if(bytes < Math.pow(1024, 6))
            return "PB";
        else if(bytes < Math.pow(1024, 7))
            return "EB";

        return "-";
    }

    private void updateProgressButton(ProgressButton progressButton, SeekBar progressSeekBar)
    {
        progressButton.setProgress(progressSeekBar.getProgress());
    }

    private void putString(View v, int id, String str)
    {
        TextView view = (TextView) v.findViewById(id);
        if (view != null) {
            view.setText(str);
        }
    }

    public void updateAdapterContent(final Map<String, Map<String, Object>> dataset)
    {
        // Queue work on mHandler to change the data on the main thread.
        mHandler.post(new Runnable() {
            @Override
            public void run()
            {
                mDataSet.putAll(dataset);
                mDataSetInvalid = true;

                updateDataSetDelayed(1000);
            }
        });
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