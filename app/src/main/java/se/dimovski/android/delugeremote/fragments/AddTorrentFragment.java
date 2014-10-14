package se.dimovski.android.delugeremote.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import se.dimovski.android.delugeremote.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link se.dimovski.android.delugeremote.fragments.AddTorrentFragment.AddTorrentCallbacks} interface
 * to handle interaction events.
 * Use the {@link AddTorrentFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */

/*
            options["max_connections"] = state.max_connections
            options["max_upload_slots"] = state.max_upload_slots
            options["max_upload_speed"] = state.max_upload_speed
            options["max_download_speed"] = state.max_download_speed
            options["prioritize_first_last_pieces"] = state.prioritize_first_last
            options["sequential_download"] = state.sequential_download
            options["file_priorities"] = state.file_priorities
            storage_mode = state.storage_mode
            options["pre_allocate_storage"] = (storage_mode == "allocate")
            options["download_location"] = state.save_path
            options["auto_managed"] = state.auto_managed
            options["stop_at_ratio"] = state.stop_at_ratio
            options["stop_ratio"] = state.stop_ratio
            options["remove_at_ratio"] = state.remove_at_ratio
            options["move_completed"] = state.move_completed
            options["move_completed_path"] = state.move_completed_path
            options["add_paused"] = state.paused
            options["shared"] = state.shared
            options["super_seeding"] = state.super_seeding
            options["priority"] = state.priority
            options["owner"] = state.owner
            options["name"] = state.name
 */
public class AddTorrentFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_FILEPATH = "filepath";
    private static final String ARG_FILENAME = "filename";

    private EditText mDownloadLocationEdit;

    ViewPager mViewPager;

    Map<String, Object> options = new HashMap<String, Object>();

    // TODO: Rename and change types of parameters
    private String mFilePath;
    private String mFileName;

    private AddTorrentCallbacks mListener;

    public static Bundle createBundle(String filePath, String filename)
    {
        Bundle args = new Bundle();
        args.putString(ARG_FILEPATH, filePath);
        args.putString(ARG_FILENAME, filename);
        return args;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param filePath Parameter 1.
     * @param filename Parameter 2.
     * @return A new instance of fragment AddTorrentFragment.
     */
    public static AddTorrentFragment newInstance(String filePath, String filename)
    {
        AddTorrentFragment fragment = new AddTorrentFragment();
        Bundle args = createBundle(filePath, filename);
        fragment.setArguments(args);
        return fragment;
    }

    public AddTorrentFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mFilePath = getArguments().getString(ARG_FILEPATH);
            mFileName = getArguments().getString(ARG_FILENAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_add_torrent, container, false);

        // Set up the login form.
        TextView text = (TextView) fragmentView.findViewById(R.id.filename);
        text.setText(mFileName);

        mDownloadLocationEdit = (EditText) fragmentView.findViewById(R.id.download_path);

        Button mAddTorrentButton = (Button) fragmentView.findViewById(R.id.add_button);
        mAddTorrentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(mListener != null)
                {
                    putOption(options, "download_location", String.valueOf(mDownloadLocationEdit.getText()));
                    mListener.addTorrentFile(mFilePath, options);
                }
            }
        });

        return fragmentView;
    }

    private void putOption(Map<String, Object> options, String option, String value)
    {
        if(value != null && !value.isEmpty())
        {
            options.put(option, value);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AddTorrentCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AddTorrentCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface AddTorrentCallbacks {
        // TODO: Update argument type and name
        public void addTorrentFile(String filepath, Map<String, Object> options);
    }

}
