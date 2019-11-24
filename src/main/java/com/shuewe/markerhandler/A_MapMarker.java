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

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for Markers which represent one or multiple elements of type I_SortableMapElement
 *
 * @param <T> The marker class created on the map
 * @param <S> The object, to add the marker of type T to, eg. GoogleMap (google) or Symbol (mapbox)
 * @param <U> Projection class
 */
public abstract class A_MapMarker<T, S, U> {

    /**
     * Center of marker.
     */
    protected LatLng m_center;

    /**
     * Cursor iterating through all items of object.
     */
    protected int m_cursor = -1;

    /**
     * A marker handler instance.
     */
    protected A_Handler m_handler;

    /**
     * indicates if object was changed and has to be reseted on map.
     */
    protected boolean m_isTouched = true;

    /**
     * Google marker which represents this Object on a map.
     */
    protected T m_marker;

    /**
     * Custom info text.
     */
    private String m_customInfoText = null;

    /**
     * List of all date of one marker.
     */
    private List<I_SortableMapElement> m_elements = new ArrayList<>();

    /**
     * Info title.
     */
    private String m_infoTitle = null;

    /**
     * Public constructor.
     *
     * @param handler marker handler instance
     */
    public A_MapMarker(A_Handler handler) {
        m_handler = handler;
    }

    /**
     * Gets the list of elements represented by this marker.
     *
     * @return list of elements
     */
    public List<I_SortableMapElement> getElements() {
        return m_elements;
    }

    /**
     * Gets the info text of the marker.
     *
     * @return the text corresponding to current cursor.
     */
    public String getInfoText() {
        return m_customInfoText != null ? m_customInfoText : (m_cursor + 1) + "/" + getElements().size();
    }

    /**
     * Sets the info text to override default behavior of getInfoText
     *
     * @param infoText to be displayed as info text
     */
    public void setInfoText(String infoText) {
        m_customInfoText = infoText;
    }

    /**
     * Gets the info title.
     *
     * @return the title
     */
    public String getInfoTitle() {
        return m_infoTitle != null ? m_infoTitle : "Pictures";
    }

    /**
     * Sets the title to override default behavior of getInfoTitle
     *
     * @param title to be set
     */
    public void setInfoTitle(String title) {
        m_infoTitle = title;
    }

    /**
     * Gets an instance of A_MapMarker.
     *
     * @return A_MapMarker
     */
    public abstract A_MapMarker getInstance();

    /**
     * Return google marker object.
     *
     * @return google marker
     */
    public T getMarker() {
        return m_marker;
    }

    /**
     * Handles changed zoom and returns a set of elements which have to shown seperately on the map.
     *
     * @param projection from google map
     * @param marker     List of all A_MapMarker shown on map. Gets updated
     * @param mode       A_Handler.ZOOM_IN or A_Handler.ZOOM_OUT
     * @param markerMap  map<I_SortableMapElement,A_MapMarker> to be updated
     * @return Set of I_SortableMapElement which should now be visible on map
     */
    public Set<I_SortableMapElement> handleZoomChange(U projection, List<A_MapMarker> marker, int mode, Map<I_SortableMapElement, A_MapMarker> markerMap) {
        if (mode == A_Handler.ZOOM_OUT) {
            adjustMarkerZoomOut(projection, marker, markerMap);
            return new HashSet<>();
        }
        return adjustMarkerZoomIn(projection, marker, markerMap);
    }

    /**
     * Adds this instance to given map as marker with color c.
     *
     * @param map to add this instance to
     * @param c   colot of the marker
     * @return marker instance created on the map
     */
    public abstract T setMarker(S map, COLOR c);

    /**
     * Adds this instance to given map as marker with default color.
     *
     * @param map to add this instance to
     * @return marker instance created on the map
     */
    public T setMarker(S map) {
        return setMarker(map, COLOR.BLUE);
    }

    /**
     * Adds an element
     *
     * @param element to be added
     */
    protected void addElement(I_SortableMapElement element) {
        m_elements.add(element);
        m_isTouched = true;
    }

    /**
     * Returns a A_MapMarker object containing given I_SortableMapElement.
     * Looks if element has to be added to an existing marker object in given list, or if a marker object has to be generated.
     * Edits or adds marker in the given list.
     *
     * @param element    to be added to markerList
     * @param markerList current list of marker
     * @param projection from google map
     * @return the A_MapMarker object holding the given element
     */
    protected A_MapMarker addElementToMarker(I_SortableMapElement element, List<A_MapMarker> markerList, U projection) {
        for (A_MapMarker marker : markerList) {
            if (!marker.isOnMap()) {
                continue;
            }
            if (marker.getPixelDistance(element.getLatLng(), projection) <= A_Handler.MIN_PIXEL_DISTANCE) {
                marker.addElement(element);
                marker.refresh();
                return marker;
            }
        }
        A_MapMarker ret = getInstance();
        ret.addElement(element);
        ret.refresh();
        markerList.add(ret);
        return ret;
    }

    /**
     * Calculates the distance in pixels of two LatLng positions on a map.
     *
     * @param pos        LatLng from second position
     * @param projection from google map
     * @return the distance in pixel
     */
    protected abstract double getPixelDistance(LatLng pos, U projection);

    /**
     * Updates m_center
     */
    protected void refresh() {
        if (m_isTouched) {
            m_center = calcCenter();
        }
    }

    /**
     * Updates an existing marker.
     */
    protected abstract void updateMarker();

    /**
     * Returns current element.
     *
     * @return I_SortableMapElement from cursor
     */
    I_SortableMapElement getCurrentElement() {
        return m_elements.get(m_cursor);
    }

    /**
     * Indicates if A_MapMarker is on map.
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
     * Adds list of I_SortableMapElement to the marker.
     *
     * @param elements to be added
     */
    private void addElements(List<I_SortableMapElement> elements) {
        m_elements.addAll(elements);
        m_isTouched = true;
    }

    /**
     * Adds a list of A_MapMarker to this A_MapMarker.
     *
     * @param markerList to be added
     */
    private void addMarker(List<A_MapMarker> markerList) {
        for (A_MapMarker marker : markerList) {
            addElements(marker.getElements());
            marker.removeAll();
        }
        m_isTouched = true;
    }

    /**
     * Adjustes marker when user zooms in.
     *
     * @param projection object
     * @param markerList list of all A_MapMarker to be updated
     * @param markerMap  map<I_SortableMapElement,A_MapMarker> to be updated
     * @return Set of I_SortableMapElement which should now be visible on map
     */
    private Set<I_SortableMapElement> adjustMarkerZoomIn(U projection, List<A_MapMarker> markerList, Map<I_SortableMapElement, A_MapMarker> markerMap) {

        Set<I_SortableMapElement> ret = new HashSet<>();
        Log.d("Zoom", "in");
        for (A_MapMarker marker : markerList) {
            if (marker.isOnMap()) {
                List<I_SortableMapElement> elementsToRemove = new ArrayList<>();
                List<I_SortableMapElement> elements = marker.getElements();
                for (I_SortableMapElement element : elements) {
                    if (marker.getPixelDistance(element.getLatLng(), projection) > A_Handler.MIN_PIXEL_DISTANCE) {
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
     * @param projection object
     * @param markerList list of all A_MapMarker to be updated
     * @param markerMap  map<I_SortableMapElement,A_MapMarker> to be updated
     */
    private void adjustMarkerZoomOut(U projection, List<A_MapMarker> markerList, Map<I_SortableMapElement, A_MapMarker> markerMap) {
        Log.d("Zoom", "out");
        for (int i = 0; i < markerList.size(); i++) {
            if (markerList.get(i).isOnMap()) {
                List<A_MapMarker> collect = new ArrayList<>();
                for (int j = i + 1; j < markerList.size(); j++) {
                    if (markerList.get(i).getPixelDistance(markerList.get(j).m_center, projection) <= A_Handler.MIN_PIXEL_DISTANCE) {
                        collect.add(markerList.get(j));
                    }
                }
                markerList.get(i).addMarker(collect);
                for (Map.Entry<I_SortableMapElement, A_MapMarker> entry : markerMap.entrySet()) {
                    if (collect.contains(entry.getValue())) {
                        markerMap.put(entry.getKey(), markerList.get(i));
                    }
                }
            }
        }
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
     * Removes all elements.
     */
    private void removeAll() {
        m_elements.clear();
        m_isTouched = true;
    }

    /**
     * Available colors.
     */
    public enum COLOR {
        BLUE, GREEN, RED, YELLOW;
    }

}
