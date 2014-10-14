package se.dimovski.android.delugeremote;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by nihilist on 2014-10-11.
 */
public abstract class ProgressUpdater implements Runnable
{
    Handler mHandler = new Handler();
    final private ProgressBar mProgressBar;
    private float mProgress = 0;

    public ProgressUpdater(ProgressBar view)
    {
        mProgressBar = view;
        mProgressBar.setMax(10000);
    }

    public abstract float getProgress();

    public abstract void onComplete();

    @Override
    public void run()
    {
        mProgressBar.setVisibility(View.VISIBLE);

        mProgress = getProgress();
        mProgressBar.setProgress((int)(mProgress * 10000));

        if (mProgress < 1.0f)
        {
            mHandler.postDelayed(this, 500);
        }
        else
        {
            mProgressBar.setVisibility(View.GONE);
            onComplete();
        }
    }

    public void start()
    {
        mHandler.post(this);
    }
}
