package se.dimovski.android.delugeremote.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by nihilist on 2014-10-06.
 */
public class FragmentPageAdapter extends FragmentPagerAdapter
{
    List<Fragment> mFragments;
    List<String> mTitles;

    public FragmentPageAdapter(FragmentManager fm, List<Fragment> fragments, String[] names)
    {
        super(fm);
        mFragments = fragments;
        mTitles = Arrays.asList(names);

    }

    @Override
    public Fragment getItem(int i)
    {
        return mFragments.get(i);
    }

    @Override
    public int getCount()
    {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(mTitles == null || position >= mTitles.size())
            return "UNTITLED";
        return mTitles.get(position);
    }
}
