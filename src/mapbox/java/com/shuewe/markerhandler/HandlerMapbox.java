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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of A_Handler for mapbox
 */
public class HandlerMapbox extends A_Handler<Projection, SymbolManager, Symbol> {

    /**
     * The default color.
     */
    private String m_defaultColor;
    /**
     * Map instance, only needed for the sortable elements option (to move camera), has to be set by setter if needed
     */
    private MapboxMap m_map;
    /**
     * The currently active marker
     */
    private Symbol m_marked;
    /**
     * Optional textView to show info about marker.
     */
    private TextView m_textView;

    /**
     * Public constructor.
     *
     * @param elements List of I_SortableMapElements to be displayed on map
     * @param metrics  of device
     */
    public HandlerMapbox(List<? extends I_SortableMapElement> elements, DisplayMetrics metrics) {
        super(elements, metrics);
    }

    /**
     * Activates given marker.
     *
     * @param symbol to be activated
     * @param map    SymbolManager
     */
    public void activateMarker(Symbol symbol, SymbolManager map) {
        activateMarker(symbol, map, false);
    }

    /**
     * Activates given marker.
     *
     * @param symbol                to be activated
     * @param map                   SymbolManager
     * @param infoFromSingleElement indicates if info should come from I_SortableMapElement or from whole marker
     */
    public void activateMarker(Symbol symbol, SymbolManager map, boolean infoFromSingleElement) {
        Log.d("Marker Icon Color", symbol.getIconColor());
        if (m_marked != null && !symbol.equals(m_marked)) { //Reset old marked symbol
            A_MapMarker marker = m_markerOnMap.get(m_marked);
            m_markerOnMap.remove(m_marked);
            m_marked.setIconColor(m_defaultColor);
            m_markerOnMap.put(m_marked, marker);
            map.update(m_marked);
        }
        if (!symbol.equals(m_marked)) { //Set new marked symbol
            m_defaultColor = symbol.getIconColor();
            A_MapMarker marker = m_markerOnMap.get(symbol);
            m_markerOnMap.remove(symbol);
            symbol.setIconColor(Color.RED);
            m_markerOnMap.put(symbol, marker);
            m_marked = symbol;
            map.update(symbol);
        }
        if (m_textView != null) {
            String text;
            if (infoFromSingleElement) {
                text = getSortableElement().getSortPropertyString();
                text = text != null ? text : "";
            } else {
                text = m_markerOnMap.get(symbol).getInfoText();
            }
            m_textView.setText(text);
            m_textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void removeMarker(Symbol marker, SymbolManager map) {
        if (marker != null) {
            m_markerOnMap.remove(marker);
            map.delete(marker);
        }
    }

    /**
     * Sets an info TextView (optional).
     *
     * @param view to be used for showing info of selected marker
     */
    public void setInfoView(TextView view) {
        m_textView = view;
    }

    /**
     * Sets the MapboxMap instance.
     * Only needed, if iteration over sorted list is used (because it needs to move the camera)
     *
     * @param map MapboxMap instance
     */
    public void setMapboxMap(MapboxMap map) {
        m_map = map;
    }

    @Override
    public void showCurrentSortableMarker(SymbolManager map) {
        //Check if element is on map
        if (m_markerMap.containsKey(getSortableElement())) {
            //Yes, it is..
            Symbol s = ((MapMarkerMapbox) m_markerMap.get(getSortableElement())).getMarker();
            if (m_map == null) {
                return;
            }
            CameraPosition position = new CameraPosition.Builder().target(s.getLatLng()).build();
            m_map.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            activateMarker(s, map, true);
            return;
        }
        //Element is not on map -> move to element position
        m_chooseCursor = true; //Flag indicates, that this method has to be called again to activate marker
        CameraPosition position = new CameraPosition.Builder().target(new LatLngMapboxWrapper(getSortableElement().getLatLng()).toOtherLatLng()).build();
        m_map.animateCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    @Override
    public void updateMarkerOnMap(final Context context, final SymbolManager map, final Projection projection, final float zoom) {
        if (m_isBusy) {
            setQueue(projection, zoom);
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long t = System.currentTimeMillis();
                        updateMap(projection, zoom);
                        Log.d("MarkerHandler", "updateMap: " + (System.currentTimeMillis() - t));
                        t = System.currentTimeMillis();
                        drawOnMap(map);
                        Log.d("MarkerHandler", "drawOnMap: " + (System.currentTimeMillis() - t));
                    }

                });
            }
        });
        thread.start();
    }

    @Override
    protected A_MapMarker getMarkerInstance() {
        return new MapMarkerMapbox(this);
    }

    @Override
    protected Map<I_SortableMapElement, Boolean> getVisibleElements(com.mapbox.mapboxsdk.maps.Projection projection) {
        Map<I_SortableMapElement, Boolean> ret = new HashMap<>();
        com.mapbox.mapboxsdk.geometry.LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        for (I_SortableMapElement element : m_elements) {
            if (bounds.contains(new LatLngMapboxWrapper(element.getLatLng()).toOtherLatLng())) {
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
