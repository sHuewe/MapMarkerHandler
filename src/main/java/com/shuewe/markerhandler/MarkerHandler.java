package com.shuewe.markerhandler;


import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * Class for the MarkerHanlder, which is visible from outside the package.
 */

public class MarkerHandler {

    /**Min pixel distance for marker.*/
    static int MIN_PIXEL_DISTANCE;

    /**Zoom in.*/
    static int ZOOM_OUT = 1;

    /**Zoom out.*/
    static int ZOOM_IN = 2;

    /**Default min distance.*/
    private static float MIN_MM_DISTANCE_DEFAULT = 8;

    /**Indicates if cursor should be used to select sortable element*/
    private boolean m_chooseCursor = false;

    /**cursor for the sortable property. */
    private int m_cursor = 0;

    /**positions in m_elements of objects which have a valid LatLng property.*/
    private List<Integer> m_elementPositionsOnMap;

    /**all element data.*/
    private List<? extends I_SortableMapElement> m_elements;

    /**visible elements.*/
    private List<I_SortableMapElement> m_elements_vis;

    /**indicates if the markers are updated at the moment.*/
    private volatile boolean m_isBusy;

    /**current zoom level.*/
    private float m_mapZoom;

    /**All currently set marker objects.*/
    private List<MapMarker> m_marker;

    /**Map containing element data and related marker Object.*/
    private Map<I_SortableMapElement, MapMarker> m_markerMap;

    /**metrics of display.*/
    private DisplayMetrics m_metrics;

    /**indicates if changes are pending.*/
    private boolean m_queuePending = false;

    /**queued projection. */
    private Projection m_queueProjection;

    /**queued zoom.*/
    private float m_queueZoom;

    /**indicates if marker handler gets started.*/
    private boolean m_startCase;

    /**
     * Public constructor.
     *
     * @param elements List of I_SortableMapElements to be displayed on map
     * @param metrics of device
     */
    public MarkerHandler(List<? extends I_SortableMapElement> elements, DisplayMetrics metrics) {
        m_metrics=metrics;
        m_elements = elements;
        m_elements_vis = new ArrayList<>();
        m_marker = new ArrayList<>();
        m_markerMap = new HashMap<>();
        m_cursor = 0;
        m_startCase=true;
        setMarkerSpacing(TypedValue.COMPLEX_UNIT_MM, MIN_MM_DISTANCE_DEFAULT);
    }

    /**
     * indicates if the sortable cursor has a next element.
     *
     * @return true, if a next sortable element is available
     */
    public boolean cursorHasNextSortable() {
        return m_cursor < (m_elementPositionsOnMap.size() - 1);
    }

    /**
     * indicates if the sortable cursor has a previous element.
     *
     * @return true, if a previous sortable element is available
     */
    public boolean cursorHasPrevSortable() {
        return m_cursor > 0;
    }

    /**
     * Returns the cursor for sortable elements.
     *
     * @return int cursor
     */
    public int getCursor(){
        return m_cursor;
    }

    /**
     * Gets current sortable element.
     *
     * @return current element
     */
    public I_SortableMapElement getSortableElement() {
        return m_elements.get(m_elementPositionsOnMap.get(m_cursor));
    }

    /**
     * Gets the current element from a google marker.
     * If multiple elements are strored under a marker, they can be iterated through moveMarkerCursor
     *
     * @param marker google marker for which an element should be returned
     * @return I_SortableMapElement
     */
    public I_SortableMapElement getElementFromMarker(Marker marker) {
        return MapMarker.MARKER_MAP.get(marker).getCurrentElement();
    }

    /**
     * indicates if marker are updating now.
     *
     * @return true if update process is running, false if not
     */
    public boolean isBusy() {
        return m_isBusy;
    }

    /**
     * Moves the sortable cursor to the next item.
     *
     * @return true if it was successful, false if not
     */
    public boolean moveCursorNextSortable() {
        if (m_cursor < (m_elementPositionsOnMap.size() - 1)) {
            m_cursor++;
            return true;
        }
        return false;
    }

    /**
     * Moves the sortable cursor to the previous item.
     *
     * @return true if it was successful, false if not
     */
    public boolean moveCursorPrevSortable() {
        if (m_cursor > 0) {
            m_cursor += -1;
            return true;
        }
        return false;
    }

    /**
     * Moves the cursor of a MapMarker object.
     *
     * @param marker google marker
     */
    public void moveMarkerCursor(Marker marker) {
        MapMarker.MARKER_MAP.get(marker).moveCursor();
    }

    /**
     * prepares the sorted property to be shown.
     */
    public void prepareSortedElements() {
        m_elementPositionsOnMap = new ArrayList<>();
        for (int i = 0; i < m_elements.size(); i++) {
            if (!m_elements.get(i).getLatLng().equals(new LatLng(0, 0))) {
                m_elementPositionsOnMap.add(0, i);
            }
        }
    }

    /**
     * Sets cursor for sortable elements to given element.
     *
     * @param cursor to set cursor to
     */
    public void setSortedElements(int cursor){
       m_cursor=cursor;
    }

    /**
     * Sets the minimal marker spacing on the map.
     *
     * @param typedValueUnit int of TypedValue, indicating which unit is meant
     * @param min_distance min distance to be set
     */
    public void setMarkerSpacing(int typedValueUnit, float min_distance){
        MIN_PIXEL_DISTANCE = (int) TypedValue.applyDimension(typedValueUnit, min_distance, m_metrics);
    }

    /**
     * Changes the view to the current sortable element. The current sortable element can be changed by moveCursorPrevSortable and
     * moveCursorNextSortable
     *
     * @param map google map
     */
    public void showCurrentSortableMarker(GoogleMap map) {
        if (m_markerMap.containsKey(getSortableElement())) {
            Marker marker = m_markerMap.get(getSortableElement()).getGoogleMarker();
            setSnippet(marker);
            marker.showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            return;
        }
        m_chooseCursor = true;
        map.moveCamera(CameraUpdateFactory.newLatLng(getSortableElement().getLatLng()));
    }

    /**
     * Updates the marker on a map.
     *
     * @param context of the Activity
     * @param map to be updated
     * @param projection of google map
     * @param zoom of google map
     */
    public void updateMarkerOnMap(final Context context,final GoogleMap map, final Projection projection, final float zoom){
        if(m_isBusy){
            setQueue(projection,zoom);
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateMap(projection,zoom);
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawOnMap(map);
                    }
                });
            }
        });
        thread.start();
    }

    /**
     * Draws the marker on the map.
     *
     * @param map instance of google map
     */
    private void drawOnMap(GoogleMap map) {
        List<MapMarker> markerToDelete = new ArrayList<>();
        Iterator<MapMarker> iterator = m_marker.iterator();
        while (iterator.hasNext()) {
            MapMarker marker = iterator.next();
            if (marker.isOnMap()) {
                if (marker.isTouched()) {
                    marker.setMarker(map);
                }
            } else {
                MapMarker.MARKER_MAP.remove(marker.getGoogleMarker());
                Marker gMarker = marker.getGoogleMarker();
                if (gMarker != null) {
                    gMarker.remove();
                }
                markerToDelete.add(marker);
            }
        }
        for (MapMarker markerDel : markerToDelete) {
            m_marker.remove(markerDel);
        }
        if (m_chooseCursor) {
            setSnippet(m_markerMap.get(getSortableElement()).getGoogleMarker());
            m_markerMap.get(getSortableElement()).getGoogleMarker().showInfoWindow();
            m_chooseCursor = false;
        }
    }

    /**
     * Estimates which elements are currently visible and compares this to the currently shown markers.
     *
     * @param projection of a google map
     * @return a map with changed elements and a boolean indicating if element was added(=true) or removed(=false)
     */
    private Map<I_SortableMapElement, Boolean> getVisibleElements(Projection projection) {
        Map<I_SortableMapElement, Boolean> ret = new HashMap<>();
        LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        for (I_SortableMapElement element : m_elements) {
            if (bounds.contains(element.getLatLng())) {
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

    /**
     * Sets a projection and zoom value to the queue. Values in the queue get read and used when the currently running
     * marker update process is ready.
     *
     * @param projection of google map
     * @param zoom of google map
     */
    private void setQueue(Projection projection, float zoom) {
        m_queueProjection = projection;
        m_queueZoom = zoom;
        m_queuePending = true;
    }

    /**
     * Sets the snippet and title of a marker.
     *
     * @param marker to set snippet for
     */
    private void setSnippet(Marker marker) {
        marker.setTitle((m_cursor + 1) + "/" + m_elementPositionsOnMap.size());
        marker.setSnippet(getSortableElement().getSortPropertyString());
    }

    /**
     * Starts the update process of the map.
     * This process needs some time, don't run it on UI thread!
     * When thread is ready, call drawOnMap on the UI thread to show results!
     *
     * @param projection of google map
     * @param zoom of google map
     */
    private void updateMap(Projection projection, float zoom) {
        m_queueProjection = null;
        m_isBusy = true;
        Set<I_SortableMapElement> elementsToAdd = updateVisibleElements(projection, m_markerMap, m_elements_vis);
        if(m_startCase){
            m_mapZoom=zoom;
        }else {
            if (m_mapZoom != zoom) {
                int mode = m_mapZoom > zoom ? ZOOM_OUT : ZOOM_IN;
                m_mapZoom = zoom;
                //m_marker: no items get added or removed, just changed. some items may have no pictures any more -> isOnMap = false
                elementsToAdd.addAll(MapMarker.handleZoomChange(projection, m_marker, mode, m_markerMap)); //changes m_markerMap and m_marker
            }
        }
        updateMarker(elementsToAdd, projection);
        m_startCase=false;

        //Is there another update waiting?
        if (m_queuePending) {
            m_queuePending = false;
            updateMap(m_queueProjection, m_queueZoom);
        }
        m_isBusy = false;
    }

    /**
     * Updates the marker list.
     *
     * @param elementsToAdd to map
     * @param projection of google map
     */
    private void updateMarker(Set<I_SortableMapElement> elementsToAdd, Projection projection) {
        for (I_SortableMapElement element : elementsToAdd) {
            m_markerMap.put(element, MapMarker.addElementToMarker(element, m_marker, projection));
        }
    }

    /**
     * Updates the visible elements.
     *
     * @param projection of google map
     * @param markerMap to be updated
     * @param elements_vis list of elements which are visible
     * @return a set of I_SortableMapElement
     */
    private Set<I_SortableMapElement> updateVisibleElements(Projection projection, Map<I_SortableMapElement, MapMarker> markerMap, List<I_SortableMapElement> elements_vis) {
       Set<I_SortableMapElement> ret= new HashSet<>();
        Map<I_SortableMapElement, Boolean> changedPictures = getVisibleElements(projection);
        for (I_SortableMapElement element : changedPictures.keySet()) {
            if (changedPictures.get(element).equals(true)) {
                markerMap.put(element, null); //new element
                ret.add(element);
                elements_vis.add(element);
            } else {
                MapMarker marker = markerMap.get(element);
                marker.removeElement(element);
                elements_vis.remove(element);
                markerMap.remove(element); //element not visible any more
            }
        }
        return ret;
    }
}
