package eu.sergehelfrich.ersaandroid;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.sergehelfrich.ersa.Dew;
import eu.sergehelfrich.ersa.Scale;
import eu.sergehelfrich.ersa.Temperature;
import eu.sergehelfrich.ersa.solver.SolverException;
import eu.sergehelfrich.ersaandroid.entity.Reading;
import eu.sergehelfrich.ersaandroid.view.ChartValueMarkerView;
import eu.sergehelfrich.ersaandroid.viewmodel.ChartViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ChartActivity extends AppCompatActivity {

    private Retrofit mRetrofit;
    private ApiEndpointInterface mApiService;
    private String mOrigin;
    private ChartViewModel mChartViewModel;
    private Observer<List<Reading>> mChartReadingsObserver;

    private ProgressBar mProgress;
    private TextView mTvStatus;
    private LineChart mChart;

    Temperature mTemperature = new Temperature(20, Scale.CELSIUS);
    Temperature mDewPoint = new Temperature(Scale.CELSIUS);
    private Dew mDew = new Dew();

    private static final String TAG = ChartActivity.class.getSimpleName();

    private static final int UI_STATE_LOADING = 1;
    private static final int UI_STATE_CHART = 2;
    private static final int UI_STATE_STATUS = 3;

    protected Typeface mTfRegular;
    protected Typeface mTfLight;
    private Long mBaseTimeStamp;

    int mColorTemperature ;
    int mColorHumidity;
    int mColorDewPoint;
    private TextView mTvDegC;
    private TextView mTvPercent;
    private YAxis leftAxis;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        setTitle(R.string.chart_activity_title);

        mProgress = findViewById(R.id.progressBar);
        mTvStatus = findViewById(R.id.textViewStatus);
        mChart = findViewById(R.id.chart);
        mTvDegC = findViewById(R.id.textViewDegC);
        mTvPercent = findViewById(R.id.textViewPercent);

        mColorTemperature = ResourcesCompat.getColor(getResources(), R.color.colorTemperature, null);
        mColorHumidity = ResourcesCompat.getColor(getResources(), R.color.colorHumidity, null);
        mColorDewPoint = ResourcesCompat.getColor(getResources(), R.color.colorDewPoint, null);

        mOrigin = getIntent().getStringExtra(DashboardFragment.ARG_ORIGIN);

        //TODO:
        String url = "http://services.nitri.de:8080/ersa/";

        mRetrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiService =
                mRetrofit.create(ApiEndpointInterface.class);

        mChartViewModel = ViewModelProviders.of(this).get(ChartViewModel.class);

        mChartReadingsObserver = new Observer<List<Reading>>() {

            @Override
            public void onChanged(@Nullable List<Reading> readings) {
                setData();
            }
        };

        mChartViewModel.getReadings().observe(this, mChartReadingsObserver);

        mTfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

        Legend l = mChart.getLegend();
        l.setEnabled(true);

        mChart.getDescription().setEnabled(false);

        ChartValueMarkerView mv = new ChartValueMarkerView(this, R.layout.chart_value_marker_view);
        mv.setChartView(mChart);
        mChart.setMarker(mv);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTfLight);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour
        xAxis.setLabelCount(4);
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @SuppressLint("SimpleDateFormat")
            private SimpleDateFormat mFormat = new SimpleDateFormat("dd-MM HH:mm");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long timeStamp = mBaseTimeStamp + (long) value;
                return mFormat.format(new Date(timeStamp * 1000));
            }
        });

        leftAxis = mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        rightAxis.setTypeface(mTfLight);
        rightAxis.setTextColor(ColorTemplate.getHoloBlue());
        rightAxis.setDrawGridLines(false);
        rightAxis.setGranularityEnabled(true);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(100f);
        rightAxis.setYOffset(-9f);
        rightAxis.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));

        Date nowDate = new Date();
        long now = nowDate.getTime() / 1000;
        long start = now - (3600 * 24);

        fetchRange(start, now);

    }

    public void fetchRange(long min, long max) {
        Call<List<Reading>> call = mApiService.getRange(mOrigin, min, max);
        call.enqueue(new Callback<List<Reading>>() {
            @Override
            public void onResponse(@NonNull Call<List<Reading>> call, @NonNull Response<List<Reading>> response) {
                mChartViewModel.getReadings().postValue(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Reading>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void setData() {
        ArrayList<Entry> temperatureValues = new ArrayList<>();
        ArrayList<Entry> humidityValues = new ArrayList<>();
        ArrayList<Entry> dewPointValues = new ArrayList<>();
        List<Reading> readings = mChartViewModel.getReadings().getValue();
        if (readings != null  && readings.size() > 0) {

            mBaseTimeStamp = readings.get(0).getTimestamp();

            float maxTemperature = readings.get(0).getTemperature().floatValue();
            float minTemperature = maxTemperature;

            for (Reading reading :readings) {
                mTemperature.setTemperature(reading.getTemperature().floatValue());
                float deltaTime = reading.getTimestamp() - mBaseTimeStamp;
                float temperature = (float) mTemperature.getTemperature();
                if (temperature > maxTemperature) {
                    maxTemperature = temperature;
                }
                if (temperature < minTemperature) {
                    minTemperature = temperature;
                }
                temperatureValues.add(new Entry(deltaTime, temperature));
                double relativeHumidity = reading.getHumidity();
                humidityValues.add(new Entry(deltaTime, (float) relativeHumidity));
                try {
                    mDewPoint.setKelvin(mDew.dewPoint(relativeHumidity, mTemperature.getKelvin()));
                } catch (SolverException e) {
                    e.printStackTrace();
                }
                float dewPoint = (float) mDewPoint.getTemperature();
                if (dewPoint > maxTemperature) {
                    maxTemperature = dewPoint;
                }
                if (dewPoint < minTemperature) {
                    minTemperature = dewPoint;
                }
                dewPointValues.add(new Entry(deltaTime, dewPoint));
            }

            float margin = maxTemperature * .2f;
            leftAxis.setAxisMinimum(minTemperature - margin);
            leftAxis.setAxisMaximum(maxTemperature + margin);

            LineDataSet temperatureDataSet = new LineDataSet(temperatureValues, getString(R.string.qtyTemperature));
            temperatureDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            temperatureDataSet.setColor(mColorTemperature);
            temperatureDataSet.setCircleColor(mColorTemperature);
            LineDataSet humidityDataSet = new LineDataSet(humidityValues, getString(R.string.qtyRelativeHumidity));
            humidityDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
            humidityDataSet.setColor(mColorHumidity);
            humidityDataSet.setCircleColor(mColorHumidity);
            LineDataSet dewPointDataSet = new LineDataSet(dewPointValues, getString(R.string.qtyDewpoint));
            dewPointDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dewPointDataSet.setColor(mColorDewPoint);
            dewPointDataSet.setCircleColor(mColorDewPoint);

            LineData data = new LineData(temperatureDataSet, humidityDataSet, dewPointDataSet);
            mChart.setData(data);
            mChart.invalidate();
            setUiState(UI_STATE_CHART);

        }
    }

    private void setUiState(int uiState) {
        switch (uiState) {
            case UI_STATE_LOADING:
                mProgress.setVisibility(View.VISIBLE);
                mChart.setVisibility(View.GONE);
                mTvDegC.setVisibility(View.GONE);
                mTvPercent.setVisibility(View.GONE);
                mTvStatus.setVisibility(View.GONE);
                break;
            case UI_STATE_CHART:
                mProgress.setVisibility(View.GONE);
                mChart.setVisibility(View.VISIBLE);
                mTvDegC.setVisibility(View.VISIBLE);
                mTvPercent.setVisibility(View.VISIBLE);
                mTvStatus.setVisibility(View.GONE);
                break;
            case UI_STATE_STATUS:
                mProgress.setVisibility(View.GONE);
                mChart.setVisibility(View.GONE);
                mTvDegC.setVisibility(View.GONE);
                mTvPercent.setVisibility(View.GONE);
                mTvStatus.setVisibility(View.VISIBLE);
                break;

        }
    }


    public interface ApiEndpointInterface {

        @GET("range")
        Call<List<Reading>> getRange(@Query("origin") String origin, @Query("minTime") long min, @Query("maxTime") long max);
    }
}
