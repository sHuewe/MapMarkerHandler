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

import android.util.DisplayMetrics;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of A_Handler for maps from google.
 */
public class HandlerGoogle extends A_Handler<Projection, GoogleMap, Marker> {

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
    public void removeMarker(Marker marker, GoogleMap map) {
        if (marker != null) {
            m_markerOnMap.remove(marker);
            marker.remove();
        }
    }

    @Override
    public void showCurrentSortableMarker(GoogleMap map) {
        if (m_markerMap.containsKey(getSortableElement())) {
            Marker marker = ((MapMarkerGoogle) m_markerMap.get(getSortableElement())).getMarker();
            setSnippet(marker);
            marker.showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            return;
        }
        m_chooseCursor = true;
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLngGoogleWrapper(getSortableElement().getLatLng()).toOtherLatLng()));
    }

    @Override
    protected A_MapMarker getMarkerInstance() {
        return new MapMarkerGoogle(this);
    }

    @Override
    protected Map<I_SortableMapElement, Boolean> getVisibleElements(Projection projection) {
        {
            Map<I_SortableMapElement, Boolean> ret = new HashMap<>();
            LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
            for (I_SortableMapElement element : m_elements) {
                if (bounds.contains(new LatLngGoogleWrapper(element.getLatLng()).toOtherLatLng())) {
                    if (!m_elements_vis.contains(element) & !element.getLatLng().equals(new LatLng(0, 0))) {
                        ret.put(element, true);
                    }
                } else {
                    if (m_elements_vis.contains(element)) {
                        ret.put(element, false);
                    }
                }
            }
            return ret;
        }
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
