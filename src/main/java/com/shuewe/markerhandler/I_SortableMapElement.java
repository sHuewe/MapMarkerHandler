/**
 * Created by Stephan Hüwe on 16.03.2017.
 * Email: shuewe87@gmail.com
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 */

package com.shuewe.markerhandler;


/**
 * Interface which has to be implemented for elements displayed on the map.
 */

public interface I_SortableMapElement {

    /**
     * The (optional) Id of the element
     *
     * @return the Id String, default is null
     */
    default String getId() {
        return null;
    }

    /**
     * Gets the LatLng object corresponding to the element.
     *
     * @return com.shuewe.markerhandler.LatLng instance
     */
    LatLng getLatLng();

    /**
     * Gets the string corresponding to an optional sorting property.
     *
     * @return string for sorted property, default is null
     */
    default String getSortPropertyString() {
        return null;
    }
}
