package eu.sergehelfrich.ersaandroid.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import eu.sergehelfrich.ersaandroid.entity.LatestReadingResult;
import eu.sergehelfrich.ersaandroid.repo.DashboardReadingRepository;

/**
 * Created by helfrich on 25/02/2018.
 */

public class DashboardViewModel extends ViewModel {

    private DashboardReadingRepository mRepository;

    private LiveData<List<LatestReadingResult>> mReadings;

    public void setRepository(DashboardReadingRepository repository) {
        mRepository = repository;
    }

    public LiveData<List<LatestReadingResult>> getReadings() {
        mReadings = mRepository.loadLatestReadings();
        return mReadings;
    }

}
