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
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of A_Handler for mapbox
 */
public class HandlerMapbox extends A_Handler<Projection, SymbolManager, Symbol, LatLngBounds> implements OnSymbolClickListener {


    /**
     * Map instance, only needed for the sortable elements option (to move camera), has to be set by setter if needed
     */
    private MapboxMap m_mapIntance;

    /**
     * Optional textView to show info about marker.
     */
    private TextView m_textView;

    @Override
    public void onAnnotationClick(Symbol symbol) {
        handleClick(symbol);
    }


    @Override
    protected void registerClickListener() {
        m_map.addClickListener(this);
    }

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
     */
    public void activateMarker(Symbol symbol) {
        activateMarker(symbol, false);
    }

    /**
     * Activates given marker.
     *
     * @param symbol                to be activated
     * @param infoFromSingleElement indicates if info should come from I_SortableMapElement or from whole marker
     */
    public void activateMarker(Symbol symbol, boolean infoFromSingleElement) {
        Log.d("Marker Icon Color", symbol.getIconColor());
        toggleMarker(m_markerOnMap.get(symbol));
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
    protected void updateSingleMarker(SymbolManager map,A_MapMarker marker){
        map.update(((MapMarkerMapbox)marker).m_marker);
    }


    /**
     * Init method for mapbox
     *
     * @param context       Activity context
     * @param symbolManager to set markers
     * @param mapInstance   instance of the map
     */
    public void init(Activity context, SymbolManager symbolManager, MapboxMap mapInstance) {
        init(context, symbolManager);
        m_mapIntance = mapInstance;
    }

    @Override
    public void removeMarker(Symbol marker) {
        if (marker != null) {
            m_markerOnMap.remove(marker);
            m_map.delete(marker);
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

    @Override
    public void showCurrentSortableMarker() {
        //Check if element is on map
        if (m_markerMap.containsKey(getSortableElement())) {
            //Yes, it is..
            Symbol s = ((MapMarkerMapbox) m_markerMap.get(getSortableElement())).getMarker();
            if (m_mapIntance == null) {
                return;
            }
            CameraPosition position = new CameraPosition.Builder().target(s.getLatLng()).build();
            m_mapIntance.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            activateMarker(s, true);
            return;
        }
        //Element is not on map -> move to element position
        m_chooseCursor = true; //Flag indicates, that this method has to be called again to activate marker
        CameraPosition position = new CameraPosition.Builder().target(new LatLngMapboxWrapper(getSortableElement().getLatLng()).toOtherLatLng()).build();
        m_mapIntance.animateCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    @Override
    public void updateMarkerOnMap(final Projection projection, final float zoom) {
        if (m_isBusy) {
            setQueue(projection, zoom);
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                ((Activity) m_context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long t = System.currentTimeMillis();
                        updateMap(projection, zoom);
                        Log.d("MarkerHandler", "updateMap: " + (System.currentTimeMillis() - t));
                        t = System.currentTimeMillis();
                        drawOnMap();
                        Log.d("MarkerHandler", "drawOnMap: " + (System.currentTimeMillis() - t));
                    }

                });
            }
        });
        thread.start();
    }

    @Override
    protected A_MapMarker getMarkerInstance() {
        return new MapMarkerMapbox(this, m_textGenerator);
    }

    @Override
    protected boolean isInRegion(LatLngBounds bounds, LatLng place) {
        return bounds.contains(new LatLngMapboxWrapper(place).toOtherLatLng());
    }

    @Override
    protected LatLngBounds getVisibleRegion(Projection projection) {
        return projection.getVisibleRegion().latLngBounds;
    }


}
