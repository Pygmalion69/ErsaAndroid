package eu.sergehelfrich.ersaandroid.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import eu.sergehelfrich.ersaandroid.entity.Reading;
import eu.sergehelfrich.ersaandroid.repo.ReadingRepository;

/**
 * Created by helfrich on 25/02/2018.
 */

public class ChartViewModel extends ViewModel {

    private ReadingRepository mRepository;
    private LiveData<List<Reading>> mReadings;

    public void setRepository(ReadingRepository repository) {
        mRepository = repository;
    }

    public LiveData<List<Reading>> getReadings(String origin, long min, long max) {
        mReadings = mRepository.loadReadings(origin, min, max);
        return mReadings;
    }

    public LiveData<List<Reading>> getReadings() {
        return mReadings;
    }

}
