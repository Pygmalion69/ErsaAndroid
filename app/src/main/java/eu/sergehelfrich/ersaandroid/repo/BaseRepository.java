package eu.sergehelfrich.ersaandroid.repo;

import eu.sergehelfrich.ersaandroid.da.ReadingDao;

public abstract class BaseRepository {

    ReadingDao mDao = null;

    public void truncateDb() {
        new Thread(() -> {
            if (mDao != null) mDao.truncate();
        }).start();
    }

}
