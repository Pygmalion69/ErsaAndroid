package eu.sergehelfrich.ersaandroid;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.sergehelfrich.ersaandroid.adapter.Updatable;
import eu.sergehelfrich.ersaandroid.adapter.UpdatableFragmentStatePagerAdapter;
import eu.sergehelfrich.ersaandroid.entity.Reading;
import eu.sergehelfrich.ersaandroid.viewmodel.DashboardViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DashboardViewModel mViewModel;
    private DashboardAdapter mAdapter;

    private static List<String> origins = new ArrayList<>();
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new DashboardAdapter(getSupportFragmentManager());

        ViewPager pager = findViewById(R.id.pager);
        pager.setAdapter(mAdapter);


        mViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);

        final Observer<Integer> originObserver = numberOfOrigins -> {
            if (mViewModel.getReadings() != null && mViewModel.getReadings().getValue() != null) {
                origins.clear();
                for (Reading reading : mViewModel.getReadings().getValue()) {
                    origins.add(reading.getOrigin());
                }
                mAdapter.notifyDataSetChanged();
            }
        };

        mViewModel.getNumberOfOrigins().observe(this, originObserver);

        //TODO:
        String url = "http://services.nitri.de:8080/ersa/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final ApiEndpointInterface apiService =
                retrofit.create(ApiEndpointInterface.class);

        mHandler = new Handler();

        Runnable fetchRunnable = new Runnable() {
            @Override
            public void run() {
                Call<List<Reading>> call = apiService.getLatestReadings();
                call.enqueue(new Callback<List<Reading>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Reading>> call, @NonNull Response<List<Reading>> response) {
                        Log.d(TAG, response.toString());
                        if (response.body() != null && mViewModel != null) {
                            mViewModel.getReadings().postValue(response.body());
                            mViewModel.getNumberOfOrigins().postValue(response.body().size());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Reading>> call, @NonNull Throwable t) {
                        t.printStackTrace();
                    }
                });
                if (mHandler != null) {
                    mHandler.postDelayed(this, 30000);
                }
            }
        };

        mHandler.post(fetchRunnable);

    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onPause();
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
        public int getItemPosition(Object object) {
            return POSITION_NONE;
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

    public interface ApiEndpointInterface {

        @GET("latest")
        Call<List<Reading>> getLatestReadings();
    }


}
