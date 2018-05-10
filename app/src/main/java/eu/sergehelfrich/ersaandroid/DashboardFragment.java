package eu.sergehelfrich.ersaandroid;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import de.nitri.gauge.Gauge;
import eu.sergehelfrich.ersa.Dew;
import eu.sergehelfrich.ersa.Scale;
import eu.sergehelfrich.ersa.Temperature;
import eu.sergehelfrich.ersa.solver.SolverException;
import eu.sergehelfrich.ersaandroid.adapter.Updatable;
import eu.sergehelfrich.ersaandroid.entity.LatestReadingResult;
import eu.sergehelfrich.ersaandroid.entity.Reading;
import eu.sergehelfrich.ersaandroid.viewmodel.DashboardViewModel;

public class DashboardFragment extends Fragment implements Updatable {

    public static final String ARG_ORIGIN = "origin";

    private String mOrigin;

    private DashboardViewModel mViewModel;

    private ScrollView mDashboardView;
    private ProgressBar mProgress;

    private TextView mTvOrigin;
    private TextView mTvDateTime;

    private Gauge mGaugeTemperature;
    private Gauge mGaugeHumidity;
    private Gauge mGaugeDewPoint;

    private Dew mDew;
    private Temperature mTemperature;
    private Temperature mDewPoint;
    private boolean mViewCreated;
    private Reading mReading;
    private Context mContext;
    private Double mRelativeHumidity;
    private boolean mInitialUpdate = true;

    Observer<List<LatestReadingResult>> mReadingsObserver;

    public DashboardFragment() {
        // Required empty public constructor
    }


    static DashboardFragment newInstance(String origin) {
        DashboardFragment f = new DashboardFragment();

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

        setHasOptionsMenu(true);

        mDew = new Dew();
        mTemperature = new Temperature(Scale.CELSIUS);
        mDewPoint = new Temperature(Scale.CELSIUS);

        mViewModel = ViewModelProviders.of(getActivity()).get(DashboardViewModel.class);

        mReadingsObserver = readings -> {
            if (readings != null) {
                for (LatestReadingResult reading : readings) {
                    if (mOrigin.equals(reading.getOrigin())) {
                        mReading = reading;
                        break;
                    }
                }
                update();
            }
        };

        mViewModel.getReadings().observe(getActivity(), mReadingsObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mDashboardView = view.findViewById(R.id.dashboard_scroll_view);
        mProgress = view.findViewById(R.id.progress);
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
    }

    @Override
    public void onResume() {
        super.onResume();

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

        if (mViewCreated && mReading != null) {
            mTemperature.setTemperature(mReading.getTemperature());
            mRelativeHumidity = mReading.getHumidity();
            dewPoint(mTemperature.getKelvin());
            mTvOrigin.setText(mOrigin);
            mTvDateTime.setText((new Date(1000 * mReading.getTimestamp()).toString()));
            if (mInitialUpdate) {
                mInitialUpdate = false;
                mGaugeTemperature.setValue(mReading.getTemperature().floatValue());
                mGaugeHumidity.setValue(mReading.getHumidity().floatValue());
                mGaugeDewPoint.setValue((float) mDewPoint.getTemperature());
            } else {
                mGaugeTemperature.moveToValue(mReading.getTemperature().floatValue());
                mGaugeHumidity.moveToValue(mReading.getHumidity().floatValue());
                mGaugeDewPoint.moveToValue((float) mDewPoint.getTemperature());
            }
            setVisibility();
        }

    }

    private void dewPoint(double kelvin) {
        try {
            mDewPoint.setKelvin(mDew.dewPoint(mRelativeHumidity, kelvin));
        } catch (SolverException e) {
            e.printStackTrace();
        }
    }

    private void setVisibility() {
        mProgress.setVisibility(View.GONE);
        mDashboardView.setVisibility(View.VISIBLE);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_dashboard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_chart:
                Intent chartIntent = new Intent(mContext, ChartActivity.class);
                chartIntent.putExtra(ARG_ORIGIN, mOrigin);
                startActivity(chartIntent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onAttach(Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mContext = null;
        mViewModel.getReadings().removeObserver(mReadingsObserver);
        super.onDetach();
    }
}
