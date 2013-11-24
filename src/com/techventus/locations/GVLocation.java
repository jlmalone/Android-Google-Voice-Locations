package com.techventus.locations;

/**
 * Created with IntelliJ IDEA.
 * User: Joseph
 * Date: 21.11.13
 * Time: 22:31
 * To change this template use File | Settings | File Templates.
 */
public class GVLocation {

    private long lat;
    private long lon;
    private int radius;
    private String name;

    public GVLocation(){}

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getLat() {
        return lat;
    }

    public void setLat(long lat) {
        this.lat = lat;
    }

    public long getLon() {
        return lon;
    }

    public void setLon(long lon) {
        this.lon = lon;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "GVLocation{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", radius=" + radius +
                ", name='" + name + '\'' +
                '}';
    }
}
