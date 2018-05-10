package eu.sergehelfrich.ersaandroid.api;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import eu.sergehelfrich.ersaandroid.da.ReadingDao;
import eu.sergehelfrich.ersaandroid.entity.Reading;

@Database(entities = {Reading.class}, version = 1, exportSchema = false)
public abstract class ReadingDatabase extends RoomDatabase {

    private static ReadingDatabase instance;

    public abstract ReadingDao readingDao();

    public static synchronized ReadingDatabase getDatabase(Context context) {
        if (instance == null) {
            instance =
                    Room.databaseBuilder(context.getApplicationContext(), ReadingDatabase.class, "reading-database")
                            .build();
        }
        return instance;
    }

}
