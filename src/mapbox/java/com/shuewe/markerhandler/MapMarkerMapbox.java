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

import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;

import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of A_MapMarker for mapbox
 */
public class MapMarkerMapbox extends A_MapMarker<Symbol, SymbolManager, Projection, String> {


    /**
     * Custom set marker ID.
     */
    private String m_markerId = null;

    /**
     * Public constructor.
     *
     * @param handler A_Handler instance
     */
    public MapMarkerMapbox(A_Handler handler, MarkerTextGenerator textGenerator) {
        super(handler,textGenerator);
        m_markerId=((HandlerMapbox)handler).getMarkerID();
    }


    @Override
    public A_MapMarker getInstance() {
        return new MapMarkerMapbox(m_handler,m_textGenerator);
    }

    @Override
    public Symbol setMarker(SymbolManager map, COLOR c) {
        m_isTouched = false;
        if (m_marker != null) {
            m_handler.m_markerOnMap.remove(m_marker);
            map.delete(m_marker);
        }
        setColor(c);
        m_marker = map.create(new SymbolOptions()
                .withLatLng(new com.mapbox.mapboxsdk.geometry.LatLng(m_center.latitude, m_center.longitude))
                .withIconSize((float) (0.35f*m_handler.getMarkerSizeFactor()))
                .withIconOffset(new Float[]{0f,-30f})
                .withIconImage(m_markerId)
                .withIconColor((String)m_handler.getColorMap().get(c)));
        m_handler.m_markerOnMap.put(m_marker, this);
        return m_marker;
    }

    /**
     * Sets the marker ID.
     * <p>
     * Must match to a marker ID in the mapbox mapstyle!
     * Only needed if ID should be different to "markerhandler_marker" (default value)
     *
     * @param markerId
     */
    public void setMarkerID(String markerId) {
        m_markerId = markerId;
    }

    @Override
    protected double getPixelDistance(LatLng pos2, Projection projection) {
        Log.d("p1", m_center.toString());
        Log.d("p2", pos2.toString());
        MercatorProjection p = ((HandlerMapbox)m_handler).getMercatorProjection();
        MercatorProjection.Coordinates c1 = p.getCoordinates(m_center);
        MercatorProjection.Coordinates c2 = p.getCoordinates(pos2);
        return Math.sqrt((c1.getX()-c2.getX())*(c1.getX()-c2.getX())+(c1.getY()-c2.getY())*(c1.getY()-c2.getY()));
    }

    @Override
    protected void updateMarker() {
        m_marker.setIconColor((String)m_handler.getColorMap().get(getColor()));

    }
}
