package edu.goldenhammer.model;

import java.util.List;

/**
 * Created by seanjib on 3/2/2017.
 */
public class Map {
    private List<Track> tracks;
    private List<City> cities;

    public Map(List<Track> tracks, List<City> cities) {
        this.tracks = tracks;
        this.cities = cities;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }
}
