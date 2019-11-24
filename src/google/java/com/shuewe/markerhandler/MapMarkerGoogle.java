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

import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of A_MapMarker for google maps.
 */

public class MapMarkerGoogle extends A_MapMarker<Marker,GoogleMap,Projection> {

    /**Map tp define A_Marker.COLOR values for marker from google. */
    Map<COLOR, BitmapDescriptor> COLOR_MAP= new HashMap<COLOR, BitmapDescriptor>() {{
        put(COLOR.BLUE, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        put(COLOR.RED, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        put(COLOR.GREEN, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        put(COLOR.YELLOW, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
    }};

    /**
     * Public constructor.
     *
     * @param handler instance of A_Handler
     */
    public MapMarkerGoogle(A_Handler handler) {
        super(handler);
    }

    @Override
    public A_MapMarker getInstance(){
        return new MapMarkerGoogle(m_handler);
    }

    @Override
    protected double getPixelDistance(LatLng pos2, Projection projection) {
        Point p1 = projection.toScreenLocation(new LatLngGoogleWrapper(this.m_center).toOtherLatLng());
        Point p2 = projection.toScreenLocation(new LatLngGoogleWrapper(pos2).toOtherLatLng());
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    @Override
    public Marker setMarker(GoogleMap map,COLOR c) {
        m_isTouched = false;
        if (m_marker != null) {
            m_handler.m_markerOnMap.remove(m_marker);
            m_marker.remove();
        }
        m_marker = map.addMarker(new MarkerOptions()
                .position(new LatLngGoogleWrapper(m_center).toOtherLatLng())
                .title(getInfoTitle())
                .snippet(getInfoText())
                .icon(COLOR_MAP.get(c)));

        m_handler.m_markerOnMap.put(m_marker, this);
        return m_marker;
    }

    @Override
    protected void updateMarker() {
        if (m_marker != null) {
            m_marker.setTitle(getInfoTitle());
            m_marker.setSnippet(getInfoText());
        }
    }
}
