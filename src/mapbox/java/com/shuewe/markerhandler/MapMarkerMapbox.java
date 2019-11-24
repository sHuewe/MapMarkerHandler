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
public class MapMarkerMapbox extends A_MapMarker<Symbol, SymbolManager, Projection> {

    /**
     * Default marker id.
     */
    public static final String ID_MARKER_DEFAULT = "markerhandler_marker";
    /**
     * Map to bind A_MapMarker.COLOR values to color values suitable for mapbox.
     */
    Map<COLOR, String> COLOR_MAP = new HashMap<COLOR, String>() {{
        put(COLOR.BLUE, ColorUtils.colorToRgbaString(Color.BLUE));
        put(COLOR.RED, ColorUtils.colorToRgbaString(Color.RED));
        put(COLOR.GREEN, ColorUtils.colorToRgbaString(Color.GREEN));
        put(COLOR.YELLOW, ColorUtils.colorToRgbaString(Color.YELLOW));
    }};
    /**
     * Custom set marker ID.
     */
    private String m_markerId = null;

    /**
     * Public constructor.
     *
     * @param handler A_Handler instance
     */
    public MapMarkerMapbox(A_Handler handler) {
        super(handler);
    }

    @Override
    public A_MapMarker getInstance() {
        return new MapMarkerMapbox(m_handler);
    }

    @Override
    public Symbol setMarker(SymbolManager map, COLOR c) {
        m_isTouched = false;
        if (m_marker != null) {
            m_handler.m_markerOnMap.remove(m_marker);
            map.delete(m_marker);
        }
        m_marker = map.create(new SymbolOptions()
                .withLatLng(new com.mapbox.mapboxsdk.geometry.LatLng(m_center.latitude, m_center.longitude))
                .withIconSize(1.2f)
                .withIconImage(m_markerId != null ? m_markerId : ID_MARKER_DEFAULT)
                .withIconColor(COLOR_MAP.get(c)));
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
        PointF p1 = projection.toScreenLocation(new LatLngMapboxWrapper(m_center).toOtherLatLng());
        PointF p2 = projection.toScreenLocation(new LatLngMapboxWrapper(pos2).toOtherLatLng());
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    @Override
    protected void updateMarker() {
    }
}
