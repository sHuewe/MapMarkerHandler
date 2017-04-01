package com.shuewe.markerhandler;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Stephan Hüwe on 16.03.2017.
 * Email: shuewe87@gmail.com
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * Interface which has to be implemented for elements displayed on the map.
 */

public interface I_SortableMapElement {

    /**Returns a LatLng object for the object*/
    LatLng getLatLng();

    /**Returns a String corresponding to a sorting property*/
    String getSortPropertyString();
}
