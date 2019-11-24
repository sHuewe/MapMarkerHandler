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
 * Abstract wrapper for LatLng Objects.
 *
 * @param <T> class for LatLng object to wrap from, e.g. com.google.android.gms.maps.model.LatLng
 */
public abstract class A_LatLngWrapper<T> {

    /**
     * source LatLng object.
     */
    protected T m_latLng_source;

    /**
     * target LatLng object of this package.
     */
    protected LatLng m_latLng_target;

    /**
     * Public constructor to parse LatLng from other packages.
     *
     * @param latLng of type T
     */
    public A_LatLngWrapper(T latLng) {
        m_latLng_source = latLng;
    }

    /**
     * Public constructor to convert this package LatLng to T.
     *
     * @param latLng object of type com.shuewe.markerhandler.LatLng
     */
    public A_LatLngWrapper(LatLng latLng) {
        m_latLng_target = latLng;
    }

    /**
     * Creates LatLng of type com.shuewe.markerhandler.LatLng
     *
     * @return LatLng
     */
    public abstract LatLng toLatLng();

    /**
     * Create LatLng object from other type than this package.
     *
     * @return LatLng e.g. from google or mapbpox
     */
    public abstract T toOtherLatLng();
}
