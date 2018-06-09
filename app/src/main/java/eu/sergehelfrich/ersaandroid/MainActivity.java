package eu.sergehelfrich.ersaandroid;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.sergehelfrich.ersaandroid.adapter.Updatable;
import eu.sergehelfrich.ersaandroid.adapter.UpdatableFragmentStatePagerAdapter;
import eu.sergehelfrich.ersaandroid.api.ErsaApi;
import eu.sergehelfrich.ersaandroid.api.ReadingDatabase;
import eu.sergehelfrich.ersaandroid.da.ReadingDao;
import eu.sergehelfrich.ersaandroid.entity.LatestReadingResult;
import eu.sergehelfrich.ersaandroid.entity.Reading;
import eu.sergehelfrich.ersaandroid.repo.DashboardReadingRepository;
import eu.sergehelfrich.ersaandroid.viewmodel.DashboardViewModel;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SelectOriginDialogFragment.OnCloseListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY_CURRENT_ITEM = "current_item";

    static final String PREFS = "preferences";
    static final String PREF_EXCLUDED_ORIGINS = "excluded_origins";

    private Set<String> mExcludedOrigins;

    private DashboardAdapter mAdapter;

    private List<String> mAllOrigins = new ArrayList<>();
    private List<String> mOrigins = new ArrayList<>();

    private Handler mHandler;
    private ViewPager mPager;
    private static int mCurrentItem;
    private DashboardReadingRepository mReadingRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getInt(KEY_CURRENT_ITEM);
        }

        SharedPreferences preferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        mExcludedOrigins = new HashSet<>(preferences.getStringSet(PREF_EXCLUDED_ORIGINS, new HashSet<>()));

        mAdapter = new DashboardAdapter(getSupportFragmentManager(), mOrigins);

        mPager = findViewById(R.id.pager);
        mPager.setSaveFromParentEnabled(false);
        mPager.setAdapter(mAdapter);

        DashboardViewModel dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);

        //TODO:
        String url = "http://services.nitri.de:8080/ersa/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ErsaApi api = retrofit.create(ErsaApi.class);
        ReadingDao dao = ReadingDatabase.getDatabase(this).readingDao();
        mReadingRepository = new DashboardReadingRepository(dao, api);

        dashboardViewModel.setRepository(mReadingRepository);

        Comparator<String> originComparator = (s1, s2) -> s1.compareToIgnoreCase(s2);

        Observer<List<LatestReadingResult>> readingResultObserver = readings -> {
            if (readings != null) {
                Log.d(TAG, "Readings: " + readings.size());
                mAllOrigins.clear();
                for (Reading reading : readings) {
                    mAllOrigins.add(reading.getOrigin());
                }
                invalidateOptionsMenu();
                Collections.sort(mAllOrigins, originComparator);
                filterOrigins();
            }
        };

        dashboardViewModel.getReadings().observe(this, readingResultObserver);

    }

    private void filterOrigins() {
        ArrayList<String> prevOrigins = new ArrayList<>(mOrigins);
        mOrigins.clear();
        for (String origin : mAllOrigins) {
            if (!mExcludedOrigins.contains(origin)) {
                mOrigins.add(origin);
            }
        }
        if (mOrigins.equals(prevOrigins)) {
            mAdapter.notifyDataSetChanged();
        } else {
            resetPagerAdapter();
        }
    }

    private void resetPagerAdapter() {
        mPager.setAdapter(null);
        mPager.setAdapter(mAdapter);
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mReadingRepository.truncateDb();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mCurrentItem = mPager.getCurrentItem();
        outState.putInt(KEY_CURRENT_ITEM, mCurrentItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        mHandler = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem selectOriginItem = menu.findItem(R.id.action_select_origin);
        if (mAllOrigins == null || mAllOrigins.size() == 0) {
            selectOriginItem.setEnabled(false);
        } else {
            selectOriginItem.setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_origin:
                SelectOriginDialogFragment selectOriginDialogFragment = new SelectOriginDialogFragment();
                selectOriginDialogFragment.setOrigins(mAllOrigins, mExcludedOrigins);
                selectOriginDialogFragment.show(getSupportFragmentManager(), "select_origin");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void dialogClosed() {
        filterOrigins();

    }

    public static class DashboardAdapter extends UpdatableFragmentStatePagerAdapter {

        private List<String> mOrigins;

        DashboardAdapter(FragmentManager fm, List<String> origins) {
            super(fm);
            mOrigins = origins;
        }

        @Override
        public int getCount() {
            return mOrigins.size();
        }


        @Override
        public Fragment getFragmentItem(int position) {
            return DashboardFragment.newInstance(mOrigins.get(position));
        }

        @Override
        public void updateFragmentItem(int position, Fragment fragment) {
            if (fragment instanceof Updatable) {
                ((Updatable) fragment).update();
            }
        }
    }

}
