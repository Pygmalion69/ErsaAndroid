package eu.sergehelfrich.ersaandroid.da;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import eu.sergehelfrich.ersaandroid.entity.Reading;

@Dao
public interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertReadings(Reading... readings);

    @Query("SELECT * FROM Reading WHERE origin = :origin AND timestamp BETWEEN :minTime AND :maxTime")
    public LiveData<List<Reading>> loadRange(String origin, long minTime, long maxTime);

    @Query("DELETE FROM Reading where id NOT IN (SELECT id from Reading ORDER BY id DESC LIMIT 20000)")
    public void truncate();

    @Query("DELETE FROM Reading")
    public void deleteAll();

}
