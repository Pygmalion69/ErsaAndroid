package eu.sergehelfrich.ersaandroid;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY_CURRENT_ITEM = "current_item";

    private DashboardAdapter mAdapter;

    private static List<String> origins = new ArrayList<>();
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

        mAdapter = new DashboardAdapter(getSupportFragmentManager());

        mPager = findViewById(R.id.pager);
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

        Observer<List<LatestReadingResult>> readingResultObserver = readings -> {
            if (readings != null) {
            Log.d(TAG, "Readings: " + readings.size());
                origins.clear();
                for (Reading reading : readings) {
                    origins.add(reading.getOrigin());
                }
                mAdapter.notifyDataSetChanged();
        }};

        dashboardViewModel.getReadings().observe(this, readingResultObserver);


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

    public static class DashboardAdapter extends UpdatableFragmentStatePagerAdapter {
        DashboardAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return origins.size();
        }


        @Override
        public Fragment getFragmentItem(int position) {
            return DashboardFragment.newInstance(origins.get(position));

        }

        @Override
        public void updateFragmentItem(int position, Fragment fragment) {
            if (fragment instanceof Updatable) {
                ((Updatable) fragment).update();
            }
        }
    }

}
