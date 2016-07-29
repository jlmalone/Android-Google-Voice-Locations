package com.techventus.locations;

/**
 * Created with IntelliJ IDEA.
 * User: Joseph
 * Date: 21.11.13
 * Time: 22:46
 * To change this template use File | Settings | File Templates.
 */
public class LocationPhoneEnablePreference2
{
    GVLocation gvLocation;
    String phoneName;
    int phoneEnabled;


    public LocationPhoneEnablePreference2()
    {

    }

    public GVLocation getGvLocation()
    {
        return gvLocation;
    }

    public void setGvLocation(GVLocation gvLocation)
    {
        this.gvLocation = gvLocation;
    }

    public String getPhoneName()
    {
        return phoneName;
    }

    public void setPhoneName(String phoneName)
    {
        this.phoneName = phoneName;
    }

    public int getPhoneEnabled()
    {
        return phoneEnabled;
    }

    public void setPhoneEnabled(int phoneEnabled)
    {
        this.phoneEnabled = phoneEnabled;
    }

    @Override
    public String toString()
    {
        return "LocationPhoneEnablePreference2{" +
                "gvLocation=" + gvLocation +
                ", phoneName='" + phoneName + '\'' +
                ", phoneEnabled='" + phoneEnabled + '\'' +
                '}';
    }
}
