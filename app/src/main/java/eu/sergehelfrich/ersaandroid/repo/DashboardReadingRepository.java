package eu.sergehelfrich.ersaandroid.repo;

import android.arch.lifecycle.LiveData;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.List;

import eu.sergehelfrich.ersaandroid.api.ErsaApi;
import eu.sergehelfrich.ersaandroid.da.ReadingDao;
import eu.sergehelfrich.ersaandroid.entity.LatestReadingResult;
import eu.sergehelfrich.ersaandroid.entity.Reading;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardReadingRepository extends BaseRepository {

    private final String TAG = DashboardReadingRepository.class.getSimpleName();

    private final ReadingDao mDao;
    private final ErsaApi mApi;

    public DashboardReadingRepository(ReadingDao dao, ErsaApi api) {
        mDao = dao;
        mApi = api;

        Handler handler = new Handler();

        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {

                refresh();
                handler.postDelayed(this, 30000);
            }
        };

        handler.post(updateRunnable);
    }

    public LiveData<List<LatestReadingResult>> loadLatestReadings() {
        refresh();
        return mDao.loadLatest();
    }

    @WorkerThread
    private void refresh() {

        if (mApi != null) {
            Call<List<Reading>> call = mApi.getLatestReadings();
            call.enqueue(new Callback<List<Reading>>() {
                @Override
                public void onResponse(@NonNull Call<List<Reading>> call, @NonNull Response<List<Reading>> response) {
                    Log.d(TAG, response.toString());
                    if (response.body() != null) {
                        insertReadings(response.body());
                    }

                }

                @Override
                public void onFailure(@NonNull Call<List<Reading>> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    private void insertReadings(List<Reading> readings) {
        if (readings != null) {
            Reading[] array = new Reading[readings.size()];
            if (mDao != null)
                new Thread(() -> mDao.insertReadings(readings.toArray(array))).start();
        }
    }

}
