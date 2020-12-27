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

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of A_Handler for maps from google.
 */
public class HandlerGoogle extends A_Handler<Projection, GoogleMap, Marker, com.google.android.gms.maps.model.LatLngBounds,Integer> implements GoogleMap.OnMarkerClickListener {



    private Map<A_MapMarker.COLOR, Integer>COLOR_MAP=new HashMap<A_MapMarker.COLOR, Integer>() {{
        put(A_MapMarker.COLOR.BLUE, R.drawable.marker_blue);
        put(A_MapMarker.COLOR.RED, R.drawable.marker_red);
        put(A_MapMarker.COLOR.GREEN, R.drawable.marker_green);
        put(A_MapMarker.COLOR.YELLOW, R.drawable.marker_yellow);
    }};

    @Override
    public boolean onMarkerClick(Marker marker) {
        for(A_Handler h:listener){
            h.handleClick(marker);
        }
        return false;
    }

    @Override
    Map<A_MapMarker.COLOR, Integer> getColorMap() {
        return COLOR_MAP;
    }



    @Override
    protected void updateSingleMarker(GoogleMap map, A_MapMarker marker) {
        marker.updateMarker();
    }

    @Override
    protected void registerClickListener() {
        m_map.setOnMarkerClickListener(this);

    }

    /**
     * Public constructor.
     *
     * @param elements List of I_SortableMapElements to be displayed on map
     * @param metrics  of device
     */
    public HandlerGoogle(List<? extends I_SortableMapElement> elements, DisplayMetrics metrics) {
        super(elements, metrics);
    }

    @Override
    public void removeMarker(Marker marker) {
        if (marker != null) {
            m_markerOnMap.remove(marker);
            marker.remove();
        }
    }

    @Override
    public void showCurrentSortableMarker() {
        if (m_markerMap.containsKey(getSortableElement())) {
            Marker marker = ((MapMarkerGoogle) m_markerMap.get(getSortableElement())).getMarker();
            setSnippet(marker);
            marker.showInfoWindow();
            m_map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            return;
        }
        m_chooseCursor = true;
        m_map.moveCamera(CameraUpdateFactory.newLatLng(new LatLngGoogleWrapper(getSortableElement().getLatLng()).toOtherLatLng()));
    }

    @Override
    protected A_MapMarker getMarkerInstance() {
        return new MapMarkerGoogle(this, m_textGenerator);
    }

    protected boolean isInRegion(LatLngBounds bounds, LatLng place){
        return bounds.contains(new LatLngGoogleWrapper(place).toOtherLatLng());
    }

    protected LatLngBounds getVisibleRegion(Projection projection){
        return projection.getVisibleRegion().latLngBounds;
    }

    /**
     * Sets the snippet and title of a marker.
     *
     * @param marker to set snippet for
     */
    private void setSnippet(Marker marker) {
        marker.setTitle((m_cursor + 1) + "/" + m_elementPositionsOnMap.size());
        marker.setSnippet(getSortableElement().getSortPropertyString() != null ? getSortableElement().getSortPropertyString() : "");
    }
}
