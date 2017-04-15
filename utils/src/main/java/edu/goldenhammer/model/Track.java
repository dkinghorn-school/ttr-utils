package edu.goldenhammer.model;

/**
 * Created by seanjib on 3/2/2017.
 */
public class Track {
    private City city1;
    private City city2;
    private int length;
    private Color color;
    private int owner;
    private double location1x;
    private double location1y;
    private double location2x;
    private double location2y;
    private int route_number;
    private boolean secondTrack;

    public Track(City city1, City city2, int length, Color color, int owner, double location1x, double location1y, double location2x, double location2y, int route_number, boolean secondTrack) {
        this.city1 = city1;
        this.city2 = city2;
        this.length = length;
        this.color = color;
        this.owner = owner;
        this.location1x = location1x;
        this.location1y = location1y;
        this.location2x = location2x;
        this.location2y = location2y;
        this.route_number = route_number;
        this.secondTrack = secondTrack;
    }

    public boolean isSecondTrack() {
        return secondTrack;
    }

    public void setSecondTrack(boolean secondTrack) {
        this.secondTrack = secondTrack;
    }

    public int getRoute_number() {
        return route_number;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public City getCity1() {
        return city1;
    }

    public City getCity2() {
        return city2;
    }

    public int getLength() {
        return length;
    }

    public Color getColor() {
        return color;
    }

    public int getOwner() {
        return owner;
    }

    public double getLocation1x() {
        return location1x;
    }

    public double getLocation1y() {
        return location1y;
    }

    public double getLocation2x() {
        return location2x;
    }

    public double getLocation2y() {
        return location2y;
    }

    public boolean hasCity(City city) {
        if(city1.equals(city) || city2.equals(city)) {
            return true;
        }
        return false;
    }
}
