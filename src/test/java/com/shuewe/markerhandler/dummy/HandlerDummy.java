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

import android.content.Context;
import android.util.DisplayMetrics;

import com.shuewe.markerhandler.A_Handler;
import com.shuewe.markerhandler.A_MapMarker;
import com.shuewe.markerhandler.I_SortableMapElement;
import com.shuewe.markerhandler.LatLng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dummy implementation of A_Handler for test cases
 */
public class HandlerDummy extends A_Handler<Object, Object, Object> {

    /**
     * Factor used to calculate dummy pixel distance. Larger factor -> larger pixel distance
     */
    public static float PIXEL_DISTANCE_FACTOR = 1;
    /**
     * Elements in current simulated view, has to be set by setter method.
     */
    private Set<I_SortableMapElement> m_elements_inBounds;

    /**
     * Public constructor.
     *
     * @param elements List of I_SortableMapElements to be displayed on map
     * @param metrics  of device
     */
    public HandlerDummy(List<? extends I_SortableMapElement> elements, DisplayMetrics metrics) {
        super(elements, metrics);
    }

    @Override
    public void removeMarker(Object marker, Object map) {

    }

    /**
     * We have to override this method to avoid a null pointer exception because of metrics=null.
     */
    @Override
    public void setMarkerSpacing(int typedValueUnit, float min_distance) {
        A_Handler.MIN_PIXEL_DISTANCE = 5;
    }

    /**
     * Setter for simulating currently visible elements.
     *
     * @param elements which should be visible now
     */
    public void setVisibleElements(Set<I_SortableMapElement> elements) {
        m_elements_inBounds = elements;
    }

    @Override
    public void showCurrentSortableMarker(Object map) {

    }

    @Override
    public void updateMarkerOnMap(final Context context, final Object map, final Object projection, final float zoom) {

        updateMap(projection, zoom);
        drawOnMap(map);

    }

    @Override
    protected A_MapMarker getMarkerInstance() {
        return new MarkerDummy(this);
    }

    @Override
    protected Map<I_SortableMapElement, Boolean> getVisibleElements(Object projection) {
        Map<I_SortableMapElement, Boolean> ret = new HashMap<>();
        for (I_SortableMapElement element : m_elements) {
            if (m_elements_inBounds.contains(element)) {
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
