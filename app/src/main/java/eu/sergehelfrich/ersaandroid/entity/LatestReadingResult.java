package eu.sergehelfrich.ersaandroid.entity;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

/**
 * Created by helfrich on 05/05/2018.
 */

@Entity
public class LatestReadingResult extends Reading {

    private Long maxtimestamp; // query alias needed by Room

    public LatestReadingResult(@NonNull Long id, @NonNull String origin, @NonNull Long timestamp, Double temperature, Double humidity) {
        super(id, origin, timestamp, temperature, humidity);
    }

    public Long getMaxtimestamp() {
        return maxtimestamp;
    }

    public void setMaxtimestamp(Long maxtimestamp) {
        this.maxtimestamp = maxtimestamp;
    }

    public Reading getReading() {
        return new Reading(getId(), getOrigin(), getTimestamp(), getTemperature(), getHumidity());
    }

}
