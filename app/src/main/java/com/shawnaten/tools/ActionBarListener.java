package com.shawnaten.tools;

import android.app.ActionBar;
import android.support.v4.app.FragmentManager;

public class ActionBarListener implements ActionBar.OnNavigationListener {
    private FragmentManager fm;
    private String[] fragments;

    public ActionBarListener(FragmentManager fm, String[] fragments) {
        this.fm = fm;
        this.fragments = fragments;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        /*
        Fragment toDetach, toAttach;
        toDetach = fm.findFragmentById(R.placeId.main_fragment);
        toAttach = fm.findFragmentByTag(fragments[itemPosition]);
        fm.beginTransaction()
                .detach(toDetach)
                .attach(toAttach)
                .commit();
        */
        return true;
    }

}
