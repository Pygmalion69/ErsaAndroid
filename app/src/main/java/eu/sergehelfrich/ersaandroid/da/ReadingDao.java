package eu.sergehelfrich.ersaandroid.da;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import eu.sergehelfrich.ersaandroid.entity.LatestReadingResult;
import eu.sergehelfrich.ersaandroid.entity.Reading;

@Dao
public interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReadings(Reading... readings);

    @Query("SELECT COUNT(*) FROM Reading INNER JOIN(SELECT origin, MAX(timestamp) AS maxtimestamp FROM Reading GROUP BY origin) mts ON Reading.origin = mts.origin AND timestamp = maxtimestamp")
    int loadNumberOfOrigins();

    @Query("SELECT * FROM Reading INNER JOIN(SELECT origin, MAX(timestamp) AS maxtimestamp FROM Reading GROUP BY origin) mts ON Reading.origin = mts.origin AND timestamp = maxtimestamp")
    LiveData<List<LatestReadingResult>> loadLatest();

    @Query("SELECT * FROM Reading")
    LiveData<List<Reading>> loadAll();

    @Query("SELECT * FROM Reading WHERE origin = :origin AND timestamp BETWEEN :minTime AND :maxTime")
    LiveData<List<Reading>> loadRange(String origin, long minTime, long maxTime);

    @Query("DELETE FROM Reading where id NOT IN (SELECT id from Reading ORDER BY id DESC LIMIT 20000)")
    void truncate();

    @Query("DELETE FROM Reading")
    void deleteAll();

}
