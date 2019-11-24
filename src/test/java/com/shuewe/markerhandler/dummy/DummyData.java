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

import com.shuewe.markerhandler.I_SortableMapElement;
import com.shuewe.markerhandler.LatLng;

import java.util.Objects;

/**
 * Dummy implementation of I_SortableMapElement
 */
public class DummyData implements I_SortableMapElement {

    /**
     * LatLng value.
     */
    private LatLng m_latLng;

    /**
     * Public constructor.
     *
     * @param latLng LatLng
     */
    public DummyData(LatLng latLng) {
        m_latLng = latLng;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof DummyData)) {
            return false;
        }
        DummyData d = (DummyData) o;
        return ((DummyData) o).getLatLng().equals(m_latLng);
    }

    @Override
    public LatLng getLatLng() {
        return m_latLng;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_latLng.latitude, m_latLng.longitude);
    }
}
