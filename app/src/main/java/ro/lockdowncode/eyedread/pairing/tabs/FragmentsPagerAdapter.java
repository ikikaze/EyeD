package ro.lockdowncode.eyedread.pairing.tabs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

import ro.lockdowncode.eyedread.DesktopConnection;

/**
 * Created by Adi Neag on 13.07.2018.
 */

public class FragmentsPagerAdapter extends SmartFragmentStatePagerAdapter {

    public static TabActive tabActive;
    public static TabSaved tabSaved;

    public FragmentsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                tabActive = new TabActive();
                return tabActive;
            case 1:
                tabSaved = new TabSaved();
                return tabSaved;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    private String[] tabTitles = {
            "Active", "Salvate"
    };

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    public void notifyData(List<DesktopConnection> myList) {
        tabActive.notifyData(myList);
    }
}
