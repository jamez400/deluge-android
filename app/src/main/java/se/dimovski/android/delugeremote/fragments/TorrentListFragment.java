package se.dimovski.android.delugeremote.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;


import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import se.dimovski.android.delugeremote.R;
import se.dimovski.android.delugeremote.adapters.TorrentDataSetAdapter;
import se.dimovski.android.delugeremote.data.TorrentInfo;
import se.dimovski.android.delugeremote.data.filters.Predicate;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link TorrentListCallbacks}
 * interface.
 */
public class TorrentListFragment extends Fragment implements AbsListView.OnItemClickListener
{
    private TorrentListCallbacks mListener;
    private Set<String> mSelectedTorrents = new HashSet<String>();
    private static Set<String> mAllTorrents = new HashSet<String>();
    private static Queue<String> mAllTorrentsQueue = new ConcurrentLinkedQueue<String>();
    private static Set<String> mFetchedTorrents = new HashSet<String>();

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private static TorrentDataSetAdapter mAdapter;

    public static TorrentListFragment newInstance()
    {
        return new TorrentListFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TorrentListFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(mAdapter == null)
        {
            mAdapter = new TorrentDataSetAdapter(getActivity(), R.layout.torrent_list_item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_torrentlist, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (TorrentListCallbacks) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement TorrentListCallbacks");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (null != mListener)
        {
            view.setSelected(true);

            String torrentId = mAdapter.getItem(position).id;
            if(mSelectedTorrents.remove(torrentId) == false)
            {
                mSelectedTorrents.add(torrentId);
            }
            mListener.onTorrentClicked(torrentId);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText)
    {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView)
        {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public void clearSelection()
    {
        mListView.clearChoices();
        mListView.invalidateViews();
        mSelectedTorrents.clear();
    }


    public List<TorrentInfo> getListByFilter(Predicate<TorrentInfo> predicate)
    {
        if(mAdapter != null)
            return mAdapter.getListByFilter(predicate);
        return Collections.EMPTY_LIST;
    }

    public void filter(Predicate<TorrentInfo> predicate)
    {
        if(mAdapter != null)
            mAdapter.filter(predicate);
    }

    public void orderBy(Comparator<TorrentInfo> comparator)
    {
        if(mAdapter != null)
            mAdapter.orderBy(comparator);
    }

    public void updateTorrentInfo(Map<String, Map<String, Object>> data)
    {
        if(data != null && mAdapter != null)
        {
            mFetchedTorrents.addAll(data.keySet());
            mAdapter.updateAdapterContent(data);
        }
    }

    public float getProgress()
    {
        return mFetchedTorrents.size()/(float)mAllTorrents.size();
    }

    public Set<String> getSelectedTorrents()
    {
        return mSelectedTorrents;
    }

    public void setTorrentIds(Set<String> torrentIds)
    {
        mAllTorrents = torrentIds;
        mAllTorrentsQueue.clear();
        mAllTorrentsQueue.addAll(mAllTorrents);
    }

    public Set<String> getTorrentIdsFromQueue(int limit)
    {
        Set<String> ids = new HashSet<String>(limit);
        for(int i=0; i<limit; i++)
        {
            String id = mAllTorrentsQueue.poll();
            mAllTorrentsQueue.offer(id);
            ids.add(id);
        }
        return ids;
    }

    public Set<String> getTorrentIds()
    {
        return mAllTorrents;
    }

    public TorrentInfo getTorrent(String torrentId)
    {
        return mAdapter.getItem(torrentId);
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface TorrentListCallbacks
    {
        public void onTorrentClicked(String torrentId);
    }
}
