package eu.sergehelfrich.ersaandroid.api;

import java.util.List;

import eu.sergehelfrich.ersaandroid.entity.Reading;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ErsaApi {

    @GET("latest")
    Call<List<Reading>> getLatestReadings();

    @GET("range")
    Call<List<Reading>> getRange(@Query("origin") String origin, @Query("minTime") long min, @Query("maxTime") long max);
}
