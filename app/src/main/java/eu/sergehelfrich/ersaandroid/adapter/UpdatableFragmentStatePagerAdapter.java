package eu.sergehelfrich.ersaandroid.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.WeakHashMap;

/**
 * @see <a href="https://stackoverflow.com/a/32639894/959505"></a>
 */
public abstract class UpdatableFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    private WeakHashMap<Integer, Fragment> mFragments;

    public UpdatableFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new WeakHashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment item = getFragmentItem(position);
        mFragments.put(position, item);
        return item;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        Integer key = position;
        if (mFragments.containsKey(key)) {
            mFragments.remove(key);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        for (Integer position : mFragments.keySet()) {
            //Make sure we only update fragments that should be seen
            if (position != null && mFragments.get(position) != null && position < getCount()) {
                updateFragmentItem(position, mFragments.get(position));
            }
        }
    }

    @Override
    public int getItemPosition(Object object) {
        //If the object is a fragment, check to see if we have it in the hashmap
        if (object instanceof Fragment) {
            int position = findFragmentPositionHashMap((Fragment) object);
            //If fragment found in the hashmap check if it should be shown
            if (position >= 0) {
                //Return POSITION_NONE if it shouldn't be displayed
                return (position >= getCount() ? POSITION_NONE : position);
            }
        }

        return super.getItemPosition(object);
    }

    /**
     * Find the position of a fragment in the hashmap if it is being viewed
     *
     * @param object the Fragment we want to check for
     * @return the position if found else -1
     */
    private int findFragmentPositionHashMap(Fragment object) {
        for (Integer position : mFragments.keySet()) {
            if (position != null &&
                    mFragments.get(position) != null &&
                    mFragments.get(position) == object) {
                return position;
            }
        }

        return -1;
    }

    public abstract Fragment getFragmentItem(int position);

    public abstract void updateFragmentItem(int position, Fragment fragment);
}