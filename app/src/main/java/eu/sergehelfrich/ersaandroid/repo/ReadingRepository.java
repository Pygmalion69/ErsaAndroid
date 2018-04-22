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

public class ReadingRepository {

    private final ReadingDao dao;
    private final ErsaApi api;

    public ReadingRepository(ReadingDao dao, ErsaApi api) {
        this.dao = dao;
        this.api = api;
    }

    public LiveData<List<Reading>> loadReadings(String origin, long min, long max) {

        refresh(origin, min, max);
        LiveData<List<Reading>> readings = dao.loadRange(origin, min, max);
        return readings;
    }

    @WorkerThread
    private void refresh(String origin, long min, long max) {

        if (api != null) {
            Call<List<Reading>> call = api.getRange(origin, min, max);
            call.enqueue(new Callback<List<Reading>>() {
                @Override
                public void onResponse(@NonNull Call<List<Reading>> call, @NonNull Response<List<Reading>> response) {
                    List<Reading> body = response.body();
                    if (body != null) {
                        Reading[] array = new Reading[body.size()];
                        if (dao != null)
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    dao.insertReadings(body.toArray(array));
                                }
                            }).start();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<List<Reading>> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        }

    }
}
