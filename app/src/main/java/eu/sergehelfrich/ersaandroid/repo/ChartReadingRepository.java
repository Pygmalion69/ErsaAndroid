package eu.sergehelfrich.ersaandroid.repo;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.List;

import eu.sergehelfrich.ersaandroid.api.ErsaApi;
import eu.sergehelfrich.ersaandroid.da.ReadingDao;
import eu.sergehelfrich.ersaandroid.entity.Reading;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChartReadingRepository extends BaseRepository {

    private final String TAG = ChartReadingRepository.class.getSimpleName();

    private final ErsaApi mApi;

    public ChartReadingRepository(ReadingDao dao, ErsaApi api) {
        mDao = dao;
        mApi = api;
    }

    public LiveData<List<Reading>> loadReadings(String origin, long min, long max) {

        refresh(origin, min, max);
        return mDao.loadRange(origin, min, max);
    }

    @WorkerThread
    private void refresh(String origin, long min, long max) {

        if (mApi != null) {
            Call<List<Reading>> call = mApi.getRange(origin, min, max);
            call.enqueue(new Callback<List<Reading>>() {
                @Override
                public void onResponse(@NonNull Call<List<Reading>> call, @NonNull Response<List<Reading>> response) {
                    insertReadings(response.body());
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
