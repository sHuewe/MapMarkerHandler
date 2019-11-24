/**
 * Created by Stephan Hüwe on 18.11.2019.
 * Email: shuewe87@gmail.com
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package com.shuewe.markerhandler.dummy;

import com.shuewe.markerhandler.A_Handler;
import com.shuewe.markerhandler.A_MapMarker;
import com.shuewe.markerhandler.LatLng;

/**
 * Dummy implementation of A_Marker
 */
public class MarkerDummy extends A_MapMarker<Object, Object, Object> {

    /**
     * Public constructor.
     *
     * @param handler A_Handler instance
     */
    public MarkerDummy(A_Handler handler) {
        super(handler);
    }

    @Override
    public A_MapMarker getInstance() {
        return new MarkerDummy(m_handler);
    }

    @Override
    public Object setMarker(Object map, COLOR c) {
        return null;
    }

    @Override
    protected double getPixelDistance(LatLng pos, Object projection) {
        return Math.sqrt((m_center.latitude - pos.latitude) * (m_center.latitude - pos.latitude) + (m_center.longitude - pos.longitude) * (m_center.longitude - pos.longitude)) * HandlerDummy.PIXEL_DISTANCE_FACTOR;
    }

    @Override
    protected void updateMarker() {

    }
}
