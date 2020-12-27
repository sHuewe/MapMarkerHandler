/**
 * Created by Stephan Hüwe on 18.11.2019.
 * Email: shuewe87@gmail.com
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package com.shuewe.markerhandler;

/**
 * LatLng object.
 */
public class LatLng {

    /**
     * The latitude value.
     */
    public double latitude;

    /**
     * The longitude value.
     */
    public double longitude;

    /**
     * The public constructor.
     *
     * @param lat latitude
     * @param lng longitude
     */
    public LatLng(double lat, double lng) {
        this.latitude = lat;
        this.longitude = lng;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof LatLng) {
            LatLng latLng = (LatLng) o;
            return latLng.latitude == this.latitude && latLng.longitude == this.longitude;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Double.valueOf(latitude).hashCode();
        hash = 31 * hash + Double.valueOf(longitude).hashCode();
        return hash;
    }

    public void setLat(double latitude) {
        this.latitude=latitude;
    }

    public void setLng(double lng){
        this.longitude=lng;
    }

    @Override
    public String toString() {
        return "lat:" + this.latitude + " lng:" + this.longitude;
    }
}
