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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class MapMarkerGoogle extends A_MapMarker<Marker, GoogleMap, Projection, Integer> {

    private double m_scale=0.3;

    /**
     * Public constructor.
     *
     * @param handler instance of A_Handler
     */
    public MapMarkerGoogle(A_Handler handler, MarkerTextGenerator textGenerator) {
        super(handler,textGenerator);
    }


    @Override
    public A_MapMarker getInstance() {
        return new MapMarkerGoogle(m_handler,m_textGenerator);
    }

    @Override
    protected double getPixelDistance(LatLng pos2, Projection projection) {
        Point p1 = projection.toScreenLocation(new LatLngGoogleWrapper(this.m_center).toOtherLatLng());
        Point p2 = projection.toScreenLocation(new LatLngGoogleWrapper(pos2).toOtherLatLng());
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    @Override
    public Marker setMarker(GoogleMap map, COLOR c) {
        m_isTouched = false;
        if (m_marker != null) {
            m_handler.m_markerOnMap.remove(m_marker);
            m_marker.remove();
        }
        MarkerOptions options=new MarkerOptions().position(new LatLngGoogleWrapper(m_center).toOtherLatLng()).icon(getScaledIcon(c,m_scale));
        setColor(c);
        if(m_textGenerator.getMarkerTitle(getElements(),m_cursor)!=null){
            options=options.title(m_textGenerator.getMarkerTitle(getElements(),m_cursor)).snippet(m_textGenerator.getMarkerDescription(getElements(),m_cursor));
        }
        m_marker = map.addMarker(options);
        m_handler.m_markerOnMap.put(m_marker, this);
        return m_marker;
    }

    private BitmapDescriptor getScaledIcon(COLOR c, double scale) {
        Bitmap b = BitmapFactory.decodeResource(m_handler.getContext().getResources(), (Integer) m_handler.getColorMap().get(c));
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
    }

    @Override
    protected void updateMarker() {
        if (m_marker != null && m_textGenerator.getMarkerTitle(getElements(),m_cursor)!=null) {
            m_marker.setTitle(m_textGenerator.getMarkerTitle(getElements(),m_cursor));
            m_marker.setSnippet(m_textGenerator.getMarkerDescription(getElements(),m_cursor));
        }
        m_marker.setIcon(getScaledIcon(getColor(),m_scale));
    }
}
