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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for marker handler objects.
 * Provides functions for clustering elements and estimation which objects are currently visible.
 *
 * @param <T> Projection like parameter.
 * @param <U> Object which allows the creation of an marker on the map, e.g. the GoogleMap class
 * @param <V> Marker object, e.g. com.google.android.gms.maps.model.Marker (google) or com.mapbox.mapboxsdk.plugins.annotation.Symbol (mapbox)
 */
public abstract class A_Handler<T, U, V> {

    /**
     * Default min distance.
     */
    protected static float MIN_MM_DISTANCE_DEFAULT = 2;
    /**
     * Min pixel distance for marker.
     */
    protected static int MIN_PIXEL_DISTANCE;
    /**
     * Zoom out.
     */
    protected static int ZOOM_IN = 2;
    /**
     * Zoom in.
     */
    protected static int ZOOM_OUT = 1;
    /**
     * Map to find marker object from given marker. Used to handle click events.
     */
    public Map<Object, A_MapMarker> m_markerOnMap = new HashMap<>();
    /**
     * Map to find marker object from given marker. Used to handle click events.
     */
    public Map<String, Object> m_nameMap = new HashMap<>();
    /**
     * Indicates if cursor should be used to select sortable element
     */
    protected boolean m_chooseCursor = false;
    /**
     * The activity instance.
     */
    protected Activity m_context;
    /**
     * cursor for the sortable property.
     */
    protected int m_cursor = 0;
    /**
     * positions in m_elements of objects which have a valid LatLng property.
     */
    protected List<Integer> m_elementPositionsOnMap;
    /**
     * all element data.
     */
    protected List<? extends I_SortableMapElement> m_elements;
    /**
     * visible elements.
     */
    protected List<I_SortableMapElement> m_elements_vis;
    /**
     * indicates if the markers are updated at the moment.
     */
    protected volatile boolean m_isBusy;
    /**
     * Map instance.
     */
    protected U m_map;
    /**
     * current zoom level.
     */
    protected float m_mapZoom;
    /**
     * All currently set marker objects.
     */
    protected List<A_MapMarker> m_marker;
    /**
     * Map containing element data and related marker Object.
     */
    protected Map<I_SortableMapElement, A_MapMarker> m_markerMap;
    /**
     * metrics of display.
     */
    protected DisplayMetrics m_metrics;
    /**
     * indicates if changes are pending.
     */
    protected boolean m_queuePending = false;
    /**
     * queued projection.
     */
    protected T m_queueProjection;
    /**
     * queued zoom.
     */
    protected float m_queueZoom;
    /**
     * indicates if marker handler gets started.
     */
    protected boolean m_startCase;

    /**
     * Public constructor.
     *
     * @param elements List of I_SortableMapElements to be displayed on map
     * @param metrics  of device
     */
    public A_Handler(List<? extends I_SortableMapElement> elements, DisplayMetrics metrics) {
        m_metrics = metrics;
        m_elements = elements;
        m_elements_vis = new ArrayList<>();
        m_marker = new ArrayList<>();
        m_markerMap = new HashMap<>();
        m_cursor = 0;
        m_startCase = true;
        Log.d("MapMarklerHandler", elements.size() + " Objects found");
        setMarkerSpacing(TypedValue.COMPLEX_UNIT_MM, A_Handler.MIN_MM_DISTANCE_DEFAULT);
    }

    /**
     * Adds a single element with a given Id to the map.
     *
     * @param element  the element to show on map (provides a LatLng and should provide an Id)
     * @param infoText the infoText corresponding to the element
     * @param c        the Color of the map. Instance of A_MapMarker.COLOR
     * @return the created instance of A_MapMarker
     */
    public A_MapMarker addElementWithId(I_SortableMapElement element, String infoText, A_MapMarker.COLOR c) {
        if (m_nameMap.containsKey(element.getId())) {
            removeElementById(element.getId());
        }
        A_MapMarker marker = getMarkerInstance();
        marker.addElement(element);
        marker.refresh();
        marker.setInfoText(infoText);
        m_nameMap.put(element.getId(), marker);
        marker.setMarker(m_map, c);
        return marker;
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
    public int getCursor() {
        return m_cursor;
    }

    /**
     * Gets the current element from a google marker.
     * If multiple elements are strored under a marker, they can be iterated through moveMarkerCursor
     *
     * @param marker google marker for which an element should be returned
     * @return I_SortableMapElement
     */
    public I_SortableMapElement getElementFromMarker(V marker) {
        return m_markerOnMap.get(marker).getCurrentElement();
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
     * Moves the cursor of a MapMarkerGoogle object.
     *
     * @param marker google marker
     */
    public void moveMarkerCursor(V marker) {
        m_markerOnMap.get(marker).moveCursor();
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
     * Removes an element with given Id from the map.
     * See addElementWithId ad counterpart to this method.
     *
     * @param id to be removed
     */
    public void removeElementById(String id) {
        A_MapMarker marker = (A_MapMarker) m_nameMap.get(id);
        if (marker == null) {
            Log.d("Marker Handler", "unable to delete marker id:" + id);
            Log.d("Marker Handler", "Available ids:" + m_nameMap.keySet().toString());
            return;
        }
        removeMarker((V) marker.getMarker());
        m_nameMap.remove(id);
    }

    /**
     * Removes the marker from the map
     *
     * @param marker to be removed
     */
    public abstract void removeMarker(V marker);

    /**
     * Sets the minimal marker spacing on the map.
     *
     * @param typedValueUnit int of TypedValue, indicating which unit is meant
     * @param min_distance   min distance to be set
     */
    public void setMarkerSpacing(int typedValueUnit, float min_distance) {
        A_Handler.MIN_PIXEL_DISTANCE = (int) TypedValue.applyDimension(typedValueUnit, min_distance, m_metrics);
    }

    /**
     * Sets cursor for sortable elements to given element.
     *
     * @param cursor to set cursor to
     */
    public void setSortedElements(int cursor) {
        m_cursor = cursor;
    }

    /**
     * Changes the view to the current sortable element. The current sortable element can be changed by moveCursorPrevSortable and
     * moveCursorNextSortable
     */
    public abstract void showCurrentSortableMarker();

    /**
     * Updates the marker on a map.
     *
     * @param projection of google map
     * @param zoom       of google map
     */
    public void updateMarkerOnMap(final T projection, final float zoom) {
        if (m_isBusy) {
            setQueue(projection, zoom);
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long t = System.currentTimeMillis();
                updateMap(projection, zoom);
                Log.d("MarkerHandler", "updateMap: " + (System.currentTimeMillis() - t));
                ((Activity) m_context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long t = System.currentTimeMillis();
                        drawOnMap();
                        Log.d("MarkerHandler", "drawOnMap: " + (System.currentTimeMillis() - t));
                    }

                });
            }
        });
        thread.start();
    }

    /**
     * Draws the marker on the map.
     */
    protected void drawOnMap() {
        String debugName = "OSM draw";
        Log.d(debugName, "start");
        Log.d(debugName, "Currently #Marker:" + m_marker.size());

        List<A_MapMarker> markerToDelete = new ArrayList<>();
        Iterator<A_MapMarker> iterator = m_marker.iterator();
        while (iterator.hasNext()) {
            A_MapMarker marker = iterator.next();
            if (marker.isOnMap()) {
                Log.d(debugName, "keep Marker");
                if (marker.isTouched()) {
                    Log.d(debugName, "set new Position");
                    marker.setMarker(m_map);
                }
            } else {
                Log.d(debugName, "not on map, remove");
                removeMarker((V) marker.getMarker());
                markerToDelete.add(marker);
            }
        }
        for (A_MapMarker markerDel : markerToDelete) {
            m_marker.remove(markerDel);
        }
        Log.d(debugName, "update finished");
        Log.d(debugName, "Currently #Marker:" + m_marker.size());
        if (m_chooseCursor) {
            showCurrentSortableMarker();
            m_chooseCursor = false;
        }
    }

    /**
     * Gets the instance of the corresponding marker class.
     *
     * @return A_MapMarker instance
     */
    protected abstract A_MapMarker getMarkerInstance();

    /**
     * Estimates which elements are currently visible and compares this to the currently shown markers.
     *
     * @param projection of a google map
     * @return a map with changed elements and a boolean indicating if element was added(=true) or removed(=false)
     */
    protected abstract Map<I_SortableMapElement, Boolean> getVisibleElements(T projection);

    /**
     * Initializes the handler.
     *
     * @param context Activity context
     * @param map     instance
     */
    protected void init(Activity context, U map) {
        m_context = context;
        m_map = map;
    }

    /**
     * Gets the map object of the handler
     *
     * @return U MapObject
     */
    public U getMapObject() {
        return m_map;
    }

    /**
     * Sets a projection and zoom value to the queue. Values in the queue get read and used when the currently running
     * marker update process is ready.
     *
     * @param projection of google map
     * @param zoom       of google map
     */
    protected void setQueue(T projection, float zoom) {
        m_queueProjection = projection;
        m_queueZoom = zoom;
        m_queuePending = true;
    }

    /**
     * Starts the update process of the map.
     * This process needs some time, don't run it on UI thread!
     * When thread is ready, call drawOnMap on the UI thread to show results!
     *
     * @param projection of google map
     * @param zoom       of google map
     */
    protected void updateMap(T projection, float zoom) {
        m_queueProjection = null;
        m_isBusy = true;
        long t = System.currentTimeMillis();
        Set<I_SortableMapElement> elementsToAdd = updateVisibleElements(projection, m_markerMap, m_elements_vis);
        Log.d("MarkerHandler", "updateVisibleElements: " + (System.currentTimeMillis() - t));
        if (m_startCase) {
            m_mapZoom = zoom;
        }
        t = System.currentTimeMillis();
        if (m_mapZoom != zoom) {
            int mode = m_mapZoom > zoom ? ZOOM_OUT : ZOOM_IN;
            m_mapZoom = zoom;
            //m_marker: no items get added or removed, just changed. some items may have no pictures any more -> isOnMap = false
            elementsToAdd.addAll(getMarkerInstance().handleZoomChange(projection, m_marker, mode, m_markerMap)); //changes m_markerMap and m_marker
        }
        Log.d("MarkerHandler", "handleZoomChange: " + (System.currentTimeMillis() - t));

        t = System.currentTimeMillis();
        updateMarker(elementsToAdd, projection);
        Log.d("MarkerHandler", "updateMarker: " + (System.currentTimeMillis() - t) + " for " + elementsToAdd.size() + " new Elements");
        m_startCase = false;

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
     * @param projection    of google map
     */
    private void updateMarker(Set<I_SortableMapElement> elementsToAdd, T projection) {
        for (I_SortableMapElement element : elementsToAdd) {
            m_markerMap.put(element, getMarkerInstance().addElementToMarker(element, m_marker, projection));
        }
    }

    /**
     * Updates the visible elements. Removes non visible elements and returns new elements which have to be sorted to markers.
     *
     * @param projection   of google map
     * @param markerMap    to be updated
     * @param elements_vis list of elements which are visible
     * @return a set of I_SortableMapElement which have to be added to the map
     */
    private Set<I_SortableMapElement> updateVisibleElements(T projection, Map<I_SortableMapElement, A_MapMarker> markerMap, List<I_SortableMapElement> elements_vis) {
        Set<I_SortableMapElement> ret = new HashSet<>();
        Map<I_SortableMapElement, Boolean> changedPictures = getVisibleElements(projection);
        for (I_SortableMapElement element : changedPictures.keySet()) {
            if (changedPictures.get(element).equals(true)) {
                markerMap.put(element, null); //new element
                ret.add(element);
                elements_vis.add(element);
            } else {
                A_MapMarker marker = markerMap.get(element);
                marker.removeElement(element);
                elements_vis.remove(element);
                markerMap.remove(element); //element not visible any more
            }
        }
        return ret;
    }
}