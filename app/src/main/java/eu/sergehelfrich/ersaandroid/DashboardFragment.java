package eu.sergehelfrich.ersaandroid;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import de.nitri.gauge.Gauge;
import eu.sergehelfrich.ersa.Dew;
import eu.sergehelfrich.ersa.Scale;
import eu.sergehelfrich.ersa.Temperature;
import eu.sergehelfrich.ersa.solver.SolverException;
import eu.sergehelfrich.ersaandroid.adapter.Updatable;
import eu.sergehelfrich.ersaandroid.entity.Reading;
import eu.sergehelfrich.ersaandroid.viewmodel.DashboardViewModel;

public class DashboardFragment extends Fragment implements Updatable {

    private static final String ARG_ORIGIN = "origin";

    private String mOrigin;

    private DashboardViewModel mViewModel;

    private TextView mTvOrigin;
    private TextView mTvDateTime;

    private Gauge mGaugeTemperature;
    private Gauge mGaugeHumidity;
    private Gauge mGaugeDewPoint;

    private Dew mDew;
    private Temperature mTemperature;
    private double mRelativeHumidity;
    private Temperature mDewPoint;
    private boolean mViewCreated;
    private Reading mReading;

    public DashboardFragment() {
        // Required empty public constructor
    }


    static DashboardFragment newInstance(String origin) {
        DashboardFragment f = new DashboardFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(ARG_ORIGIN, origin);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOrigin = getArguments().getString(ARG_ORIGIN);
        }

        mDew = new Dew();
        mTemperature = new Temperature(Scale.CELSIUS);
        mDewPoint = new Temperature(Scale.CELSIUS);

        mViewModel = ViewModelProviders.of(getActivity()).get(DashboardViewModel.class);

        final Observer<List<Reading>> readingsObserver = new Observer<List<Reading>>() {
            @Override
            public void onChanged(@Nullable final List<Reading> readings) {
                if (readings != null) {
                    for (Reading reading : readings) {
                        if (mOrigin.equals(reading.getOrigin())) {
                            mReading = reading;
                            break;
                        }
                    }
                    update();
                }
            }
        };

        mViewModel.getReadings().observe(getActivity(), readingsObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mTvOrigin = view.findViewById(R.id.tvOrigin);
        mTvDateTime = view.findViewById(R.id.tvDateTime);
        mGaugeTemperature = view.findViewById(R.id.gaugeTemperature);
        mGaugeHumidity = view.findViewById(R.id.gaugeHumidity);
        mGaugeDewPoint = view.findViewById(R.id.gaugeDewPoint);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewCreated = true;
        if (mViewModel.getReadings() != null && mViewModel.getReadings().getValue() != null) {
            for (Reading reading : mViewModel.getReadings().getValue()) {
                if (mOrigin.equals(reading.getOrigin())) {
                    mReading = reading;
                    break;
                }
            }
            update();
        }
    }

    @Override
    public void update() {

        if (mViewCreated) {
            mTemperature.setTemperature(mReading.getTemperature());
            mRelativeHumidity = mReading.getHumidity();
            double kelvin = mTemperature.getKelvin();
            try {
                mDewPoint.setKelvin(mDew.dewPoint(mRelativeHumidity, kelvin));
            } catch (SolverException e) {
                e.printStackTrace();
            }
            mTvOrigin.setText(mOrigin);
            mTvDateTime.setText((new Date(1000 * mReading.getTimestamp()).toString()));
            mGaugeTemperature.moveToValue(mReading.getTemperature().floatValue());
            mGaugeHumidity.moveToValue(mReading.getHumidity().floatValue());
            mGaugeDewPoint.moveToValue((float) mDewPoint.getTemperature());
        }

    }

}
