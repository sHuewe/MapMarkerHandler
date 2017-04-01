package com.shuewe.markerhandler;

import android.graphics.Point;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Stephan Hüwe on 16.03.2017.
 * Email: shuewe87@gmail.com
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * Class for the Marker Object, which may be holding more than 1 data object.
 */

class MapMarker {

    /**
     * Map to find marker object from given marker. Used to handle click events.
     */
    static Map<Marker, MapMarker> MARKER_MAP = new HashMap<>();

    /**
     * Center of marker.
     */
    private LatLng m_center;

    /**
     * Cursor iterating through all items of object.
     */
    private int m_cursor = -1;

    /**
     * List of all date of one marker.
     */
    private List<I_SortableMapElement> m_elements = new ArrayList<>();

    /**
     * indicates if object was changed and has to be reseted on map.
     */
    private boolean m_isTouched = true;

    /**
     * Google marker which represents this Object on a map.
     */
    private Marker m_marker;

    /**
     * Returns a MapMarker object containing given I_SortableMapElement.
     * Looks if element has to be added to an existing marker object in given list, or if a marker object has to be generated.
     * Edits or adds marker in the given list.
     *
     * @param element    to be added to markerList
     * @param markerList current list of marker
     * @param projection from google map
     * @return the MapMarker object holding the given element
     */
    static MapMarker addElementToMarker(I_SortableMapElement element, List<MapMarker> markerList, Projection projection) {
        for (MapMarker marker : markerList) {
            if (getPixelDistance(element.getLatLng(), marker.m_center, projection) <= MarkerHandler.MIN_PIXEL_DISTANCE) {
                marker.addElement(element);
                marker.refresh();
                return marker;
            }
        }
        MapMarker ret = new MapMarker();
        ret.addElement(element);
        ret.refresh();
        markerList.add(ret);
        return ret;
    }

    /**
     * Handles changed zoom and returns a set of elements which have to shown seperately on the map.
     *
     * @param projection from google map
     * @param marker     List of all MapMarker shown on map. Gets updated
     * @param mode       MarkerHandler.ZOOM_IN or MarkerHandler.ZOOM_OUT
     * @param markerMap  map<I_SortableMapElement,MapMarker> to be updated
     * @return Set of I_SortableMapElement which should now be visible on map
     */
    static Set<I_SortableMapElement> handleZoomChange(Projection projection, List<MapMarker> marker, int mode, Map<I_SortableMapElement, MapMarker> markerMap) {
        if (mode == MarkerHandler.ZOOM_OUT) {
            adjustMarkerZoomOut(projection, marker, markerMap);
            return new HashSet<>();
        }
        return adjustMarkerZoomIn(projection, marker, markerMap);
    }

    /**
     * Adjustes marker when user zooms in.
     *
     * @param projection of google map
     * @param markerList list of all MapMarker to be updated
     * @param markerMap  map<I_SortableMapElement,MapMarker> to be updated
     * @return Set of I_SortableMapElement which should now be visible on map
     */
    private static Set<I_SortableMapElement> adjustMarkerZoomIn(Projection projection, List<MapMarker> markerList, Map<I_SortableMapElement, MapMarker> markerMap) {

        Set<I_SortableMapElement> ret = new HashSet<>();

        for (MapMarker marker : markerList) {
            if (marker.isOnMap()) {
                List<I_SortableMapElement> elementsToRemove = new ArrayList<>();
                for (I_SortableMapElement element : marker.m_elements) {
                    if (getPixelDistance(element.getLatLng(), marker.m_center, projection) > MarkerHandler.MIN_PIXEL_DISTANCE) {
                        markerMap.put(element, null);
                        ret.add(element);
                        elementsToRemove.add(element);
                    }
                }
                for (I_SortableMapElement element : elementsToRemove) {
                    marker.removeElement(element);
                }
                marker.refresh();
            }
        }
        return ret;
    }

    /**
     * Adjustes marker when user zooms out.
     *
     * @param projection of google map
     * @param markerList list of all MapMarker to be updated
     * @param markerMap  map<I_SortableMapElement,MapMarker> to be updated
     */
    private static void adjustMarkerZoomOut(Projection projection, List<MapMarker> markerList, Map<I_SortableMapElement, MapMarker> markerMap) {

        for (int i = 0; i < markerList.size(); i++) {
            if (markerList.get(i).isOnMap()) {
                List<MapMarker> collect = new ArrayList<>();
                for (int j = i + 1; j < markerList.size(); j++) {
                    if (getPixelDistance(markerList.get(i).m_center, markerList.get(j).m_center, projection) <= MarkerHandler.MIN_PIXEL_DISTANCE) {
                        collect.add(markerList.get(j));
                    }
                }
                markerList.get(i).addMarker(collect);
                for (Map.Entry<I_SortableMapElement, MapMarker> entry : markerMap.entrySet()) {
                    if (collect.contains(entry.getValue())) {
                        markerMap.put(entry.getKey(), markerList.get(i));
                    }
                }
            }
        }
    }

    /**
     * Calculates the distance in pixels of two LatLng positions on a map.
     *
     * @param pos1       LatLng from first position
     * @param pos2       LatLng from second position
     * @param projection from google map
     * @return the distance in pixel
     */
    private static double getPixelDistance(LatLng pos1, LatLng pos2, Projection projection) {
        Point p1 = projection.toScreenLocation(pos1);
        Point p2 = projection.toScreenLocation(pos2);
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    /**
     * Returns current element.
     *
     * @return I_SortableMapElement from cursor
     */
    I_SortableMapElement getCurrentElement() {
        return m_elements.get(m_cursor);
    }

    /**
     * Return google marker object.
     *
     * @return google marker
     */
    Marker getGoogleMarker() {
        return m_marker;
    }

    /**
     * Indicates if MapMarker is on map.
     *
     * @return true if marker should be shown, false if it is supposed to be removed from list
     */
    boolean isOnMap() {
        return !m_elements.isEmpty();
    }

    /**
     * Indicates if marker has changed.
     *
     * @return true, if marker has changed and has to be updated on map
     */
    boolean isTouched() {
        return m_isTouched;
    }

    /**
     * Moves the cursor.
     */
    void moveCursor() {
        Log.d("moveMarkerCursor", m_cursor + " " + m_elements.size());
        if (m_cursor < (m_elements.size() - 1)) {
            m_cursor++;
            updateMarker();
            return;
        }
        m_cursor = 0;
        updateMarker();
    }

    /**
     * Removes given element.
     *
     * @param element to be removed
     */
    void removeElement(I_SortableMapElement element) {
        m_isTouched = true;
        m_elements.remove(element);
    }

    /**
     * Sets the Google marker corresponging to this object.
     *
     * @param map instance from google map
     * @return google marker object representing this object
     */
    Marker setMarker(GoogleMap map) {
        m_isTouched = false;
        if (m_marker != null) {
            MARKER_MAP.remove(m_marker);
            m_marker.remove();
        }
        m_marker = map.addMarker(new MarkerOptions().position(m_center).title("Pictures").snippet("1/" + m_elements.size()));

        MARKER_MAP.put(m_marker, this);
        return m_marker;
    }

    /**
     * Adds an element
     *
     * @param element to be added
     */
    private void addElement(I_SortableMapElement element) {
        m_elements.add(element);
        m_isTouched = true;
    }

    /**
     * Adds list of I_SortableMapElement to the marker.
     *
     * @param elements to be added
     */
    private void addElements(List<I_SortableMapElement> elements) {
        m_elements.addAll(elements);
        m_isTouched = true;
    }

    /**
     * Adds a list of MapMarker to this MapMarker.
     *
     * @param markerList to be added
     */
    private void addMarker(List<MapMarker> markerList) {
        for (MapMarker marker : markerList) {
            addElements(marker.m_elements);
            marker.removeAll();
        }
        m_isTouched = true;
    }

    /**
     * Estimates the center.
     *
     * @return LatLng of center
     */
    private LatLng calcCenter() {

        double x = 0, y = 0, z = 0;
        for (I_SortableMapElement element : m_elements) {
            LatLng latLng = element.getLatLng();
            double kuglat = Math.toRadians(90 - latLng.latitude);
            double kuglng = Math.toRadians(latLng.longitude);
            x += Math.sin(kuglat) * Math.cos(kuglng);
            y += Math.sin(kuglat) * Math.sin(kuglng);
            z += Math.cos(kuglat);
        }

        x = x / m_elements.size();
        y = y / m_elements.size();
        z = z / m_elements.size();

        double centerlat = 90 - Math.toDegrees(Math.acos(z / Math.sqrt(x * x + y * y + z * z)));
        double centerlng = Math.toDegrees(Math.atan2(y, x));

        return new LatLng(centerlat, centerlng);
    }

    /**
     * Updates m_center
     */
    private void refresh() {
        if (m_isTouched) {
            m_center = calcCenter();
        }
    }

    /**
     * Removes all elements.
     */
    private void removeAll() {
        m_elements.clear();
        m_isTouched = true;
    }

    /**
     * Updates the marker snippet and title.
     */
    private void updateMarker() {
        if (m_marker != null) {
            m_marker.setTitle("Pictures");
            m_marker.setSnippet((m_cursor + 1) + "/" + m_elements.size());
        }
    }
}
