package eu.sergehelfrich.ersaandroid.entity;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

/**
 * Created by helfrich on 25/02/2018.
 */

@Entity(primaryKeys = {"id", "origin", "timestamp"})
public class Reading {

    public Reading(@NonNull Long id, @NonNull String origin, @NonNull Long timestamp, Double temperature, Double humidity) {
        this.id = id;
        this.origin = origin;
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    private @NonNull Long id;

    private @NonNull String origin;

    private @NonNull Long timestamp;

    private Double temperature;

    private Double humidity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

}
