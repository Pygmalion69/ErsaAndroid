package eu.sergehelfrich.ersaandroid.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import eu.sergehelfrich.ersaandroid.entity.Reading;

/**
 * Created by helfrich on 25/02/2018.
 */

public class ChartViewModel extends ViewModel {

    private MutableLiveData<List<Reading>> mReadings;

    public MutableLiveData<List<Reading>> getReadings() {
        if (mReadings == null) {
            mReadings = new MutableLiveData<>();
        }
        return mReadings;
    }

}
