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
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
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
 * @param <W> LatLng Bounds
 * @param <X> Color specify class (String for mapbox, Integer id for others)
 */
public abstract class A_Handler<T, U, V, W, X> {

    static final String LOG_NAME="MapMarkerHandler";

    public interface OnChangedMapListener{
        void handleChangedMap();
    }

    protected static List<A_Handler> listener= new ArrayList<A_Handler>();

    /**
     * Default min distance.
     */
    protected static float MIN_MM_DISTANCE_DEFAULT = 2;
    /**
     * Min pixel distance for marker.
     */
    private int m_minPixelDistance;
    /**
     * Zoom out.
     */
    protected static int ZOOM_IN = 2;
    /**
     * Zoom in.
     */
    protected static int ZOOM_OUT = 1;
    /**
     * The currently active marker
     */
    protected A_MapMarker m_marked;

    private double m_markerFactor=1;

    public double getMarkerSizeFactor(){
        return m_markerFactor;
    }

    public void setMarkerSizeFactor(double factor){
        m_markerFactor=factor;
    }

    /**
     * The default color.
     */
    protected A_MapMarker.COLOR m_defaultColor=A_MapMarker.COLOR.BLUE;

    private W m_queueBounds;

    private List<OnChangedMapListener> m_mapChangedListener=new ArrayList<OnChangedMapListener>();

    /**
     * The active color.
     */
    protected A_MapMarker.COLOR m_activeColor=A_MapMarker.COLOR.RED;
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
    protected Context m_context;
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
    protected Set<I_SortableMapElement> m_elements_notOnMap=new HashSet<I_SortableMapElement>();
    /**
     * indicates if the markers are updated at the moment.
     */
    protected volatile boolean m_isBusy;
    /**
     * Map instance.
     */
    protected U m_map;

    private T m_projection;
    /**
     * current zoom level.
     */
    protected float m_mapZoom;

    protected OnMarkerClickListener m_markerClickListener=null;

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

    public A_MapMarker.COLOR getActiveColor(){
        return m_activeColor;
    }

    public Context getContext(){
        return m_context;
    }

    public  A_MapMarker.COLOR getDefaultColor(){
        return m_defaultColor;
    }

    public void setOnMarkerClickListener(OnMarkerClickListener l){
        m_markerClickListener=l;
        listener.add(this);
        registerClickListener();
    }

    public void setSelectedMarker(A_MapMarker marker){
        m_marked=marker;
    }

    protected boolean handleClick(V marker){
        if(marker==null || !m_markerOnMap.containsKey(marker)){
            resetMarkedMarker();
            return false;
        }
        toggleMarker(m_markerOnMap.get(marker));
        m_markerClickListener.onMarkerClick(m_markerOnMap.get(marker));
        return false;
    }

    public void setDefaultColor(A_MapMarker.COLOR c){
        this.m_defaultColor=c;
    }

    public void setActiveColor(A_MapMarker.COLOR c){
        this.m_activeColor=c;
    }

    public T getProjection(){
        return m_projection;
    }

    public void setProjection(T projection){
        m_projection=projection;
    }

    private void resetMarkedMarker(){
        if(m_marked==null){
            return;
        }
        Log.i(LOG_NAME, "reset old marker");// set old marked symbol
        if(m_markerOnMap.containsKey(m_marked.m_marker)) {
            m_markerOnMap.remove(m_marked.m_marker);
            m_marked.setColor(m_defaultColor);
            updateSingleMarker(m_map, m_marked);
            m_markerOnMap.put(m_marked.m_marker, m_marked);
        }
        m_marked=null;
    }

    public void toggleMarker(A_MapMarker marker){
        Log.i(LOG_NAME,"Try to activate marker: "+marker.getID().toString());
        String markedId="null";
        if(m_marked!=null){
            markedId=m_marked.getID().toString();
        }
        Log.i(LOG_NAME, "Current marker:"+ markedId);
        if (m_marked != null && !marker.equals(m_marked)) { //Re
            resetMarkedMarker();
        }
        if (!marker.getID().equals(markedId)) { //Set new marked symbol
            Log.i(LOG_NAME, "Set new marked marker");
            m_defaultColor = marker.getColor();//symbol.getIconColor();
            m_markerOnMap.remove(marker.m_marker);
            marker.setColor(m_activeColor);
            m_marked = marker;
            updateSingleMarker(m_map,m_marked);
            m_markerOnMap.put(marker.m_marker, marker);
            //m_map.update(symbol);
        }
    }

    abstract Map<A_MapMarker.COLOR,X> getColorMap();

    public void addColor(A_MapMarker.COLOR c, X draw){
            getColorMap().put(c,draw);

    }

    protected abstract void updateSingleMarker(U map,A_MapMarker m_marked);

    /**
     * Registers current click listener, and handle to have only current listener attached (if needed)
     */
    protected abstract void registerClickListener();

    protected MarkerTextGenerator m_textGenerator=new MarkerTextGenerator() {
        @Override
        public String getMarkerTitle(List<I_SortableMapElement> elements, int count) {
            if(elements.size()==1){
                return elements.get(0).getId();
            }else{
                return String.format(m_context.getResources().getString(R.string.mmh_elements),String.valueOf(elements.size()));
            }
        }

        @Override
        public String getMarkerDescription(List<I_SortableMapElement> elements, int count) {
            if(elements.size()==1){
                return elements.get(0).getSortPropertyString();
            }else{
                return (count+1)+"/"+elements.size();
            }
        }
    };

    public void setMarkerTextGenerator(MarkerTextGenerator textGenerator){
        m_textGenerator=textGenerator;
    }

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
        m_marker = new ArrayList<>();
        m_markerMap = new HashMap<>();
        m_elements_notOnMap.addAll(elements);
        m_cursor = 0;
        m_startCase = true;
        Log.d(LOG_NAME, elements.size() + " Objects found");
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
        m_nameMap.put(element.getId(), marker);
        marker.setMarker(m_map, c);
        Log.i(LOG_NAME,"Added elememnt");
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
            Log.d(LOG_NAME, "unable to delete marker id:" + id);
            Log.d(LOG_NAME, "Available ids:" + m_nameMap.keySet().toString());
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
        m_minPixelDistance = (int) TypedValue.applyDimension(typedValueUnit, min_distance, m_metrics);
    }

    public void setMarkerSpacing(int min_distance_pixel){
        m_minPixelDistance = min_distance_pixel;
    }

    public int getMinPixelDistance(){
        return m_minPixelDistance;
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

    public void addOnMapChangedListener(OnChangedMapListener l){
        m_mapChangedListener.add(l);
    }

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
        m_projection=projection;
        W bounds = getVisibleRegion(projection);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long t = System.currentTimeMillis();
                Log.i(LOG_NAME, "start calculation");
                updateMap(projection,bounds, zoom);
                Log.i(LOG_NAME, "Calculation ready, send to ui thread: " + (System.currentTimeMillis() - t));
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        long t = System.currentTimeMillis();
                        drawOnMap();
                        Log.i(LOG_NAME, "Draw ready: " + (System.currentTimeMillis() - t));
                        for(OnChangedMapListener listener:m_mapChangedListener){
                            listener.handleChangedMap();
                        }
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
        Log.d(LOG_NAME, "start");
        Log.d(LOG_NAME, "Currently #Marker:" + m_marker.size());

        List<A_MapMarker> markerToDelete = new ArrayList<>();
        Iterator<A_MapMarker> iterator = m_marker.iterator();
        while (iterator.hasNext()) {
            A_MapMarker marker = iterator.next();
            if (marker.isOnMap()) {
                Log.d(LOG_NAME, "keep Marker");
                if (marker.isTouched()) {
                    Log.d(LOG_NAME, "set new Position");
                    marker.setMarker(m_map);
                }
            } else {
                Log.d(LOG_NAME, "not on map, remove");
                removeMarker((V) marker.getMarker());
                markerToDelete.add(marker);
            }
        }
        for (A_MapMarker markerDel : markerToDelete) {
            m_marker.remove(markerDel);
        }
        Log.d(LOG_NAME, "update finished");
        Log.d(LOG_NAME, "Currently #Marker:" + m_marker.size());
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
     * Initializes the handler.
     *
     * @param context Activity context
     * @param map     instance
     */
    public void init(Context context, U map) {
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
        m_queueBounds= getVisibleRegion(projection);
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
    protected void updateMap(T projection, W bounds, float zoom) {
        m_queueProjection = null;
        m_queueBounds=null;
        m_isBusy = true;
        long t = System.currentTimeMillis();
        Set<I_SortableMapElement> elementsToAdd = updateVisibleElements(projection,bounds, m_markerMap);
        Log.i(LOG_NAME, "updateVisibleElements: " + (System.currentTimeMillis() - t));
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
        Log.i(LOG_NAME, "handleZoomChange: " + (System.currentTimeMillis() - t));

        t = System.currentTimeMillis();
        updateMarker(elementsToAdd, projection);
        Log.i(LOG_NAME, "updateMarker: " + (System.currentTimeMillis() - t) + " for " + elementsToAdd.size() + " new Elements");
        m_startCase = false;

        //Is there another update waiting?
        if (m_queuePending) {
            processQueue();
        }
        m_isBusy = false;
    }

    protected void processQueue(){
        m_queuePending= false;
        updateMap(m_queueProjection,m_queueBounds, m_queueZoom);
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
     * @return a set of I_SortableMapElement which have to be added to the map
     */
    private Set<I_SortableMapElement> updateVisibleElements(T projection,W bounds, Map<I_SortableMapElement, A_MapMarker> markerMap) {
        //This method is slow...
        Set<I_SortableMapElement> ret = new HashSet<>();
        long t = System.currentTimeMillis();
        Map<I_SortableMapElement, Boolean> changedPictures = getVisibleElements(bounds);
        Log.i(LOG_NAME,"Detect visible elements in "+(System.currentTimeMillis()-t)+" ms, found "+changedPictures.size()+" Elements have changed");
        for (I_SortableMapElement element : changedPictures.keySet()) {
            if (changedPictures.get(element).equals(true)) {
                markerMap.put(element, null); //new element
                ret.add(element);
                m_elements_notOnMap.remove(element);
            } else {
                A_MapMarker marker = markerMap.get(element);
                marker.removeElement(element);
                m_elements_notOnMap.add(element);
                markerMap.remove(element); //element not visible any more
            }
        }
        return ret;
    }
    protected abstract boolean isInRegion(W bounds, LatLng place);

    protected abstract W getVisibleRegion(T projection);

    /**
     * Estimates which elements are currently visible and compares this to the currently shown markers.
     *
     * @param bounds of a google map
     * @return a map with changed elements and a boolean indicating if element was added(=true) or removed(=false)
     */
    private Map<I_SortableMapElement, Boolean> getVisibleElements(W bounds) {

        Map<I_SortableMapElement, Boolean> ret = new HashMap<>();

        for(I_SortableMapElement element:m_elements_notOnMap){
            if(isInRegion(bounds,element.getLatLng())){
                ret.put(element,true);
            }
        }
        for(A_MapMarker marker:m_marker){
            if(Double.isNaN(marker.m_center.latitude) || Double.isNaN(marker.m_center.longitude)){
                Log.w(LOG_NAME,"Marker has Nan value..");
                continue;
            }
            if(!isInRegion(bounds,marker.m_center)){
                List<I_SortableMapElement> markerElements=marker.getElements();
                for(I_SortableMapElement element: markerElements){
                    if(!isInRegion(bounds,element.getLatLng())){
                        ret.put(element,false);
                    }
                }
            }
        }
        return ret;

/*            Map<I_SortableMapElement, Boolean> ret = new HashMap<>();
            LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
            Map<A_MapMarker,Boolean> checkedMarker= new HashMap<A_MapMarker,Boolean>();
            for (I_SortableMapElement element : m_elements) {
                boolean isContained = false;
                A_MapMarker marker = m_markerMap.get(element);
                if (marker != null) {
                    //It is displayed in a marker currently
                    if (!checkedMarker.containsKey(marker)) {
                        checkedMarker.put(marker, Boolean.valueOf(bounds.contains(new LatLngGoogleWrapper(marker.m_center).toOtherLatLng())));
                    }
                    isContained = checkedMarker.get(marker).booleanValue();
                    if (isContained) {
                        continue;
                    }
                    //Not contained -> remove
                    ret.put(element, false);
                    continue;
                } else {
                    //Elements, which are not represented as marker
                    isContained = bounds.contains(new LatLngGoogleWrapper(element.getLatLng()).toOtherLatLng());
                    if (isContained) {
                        if (!element.getLatLng().equals(new LatLng(0, 0))) {
                            ret.put(element, true);
                        }
                    }
                }
            }
            return ret;*/

    }
}