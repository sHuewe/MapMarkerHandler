package com.shuewe.markerhandler;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HandlerCanvas extends A_Handler<MercatorProjection, Canvas,String,LatLngBounds,Integer> {

    private double m_pointRadius=10;

    /**
     * Map to bind A_MapMarker.COLOR values to color values suitable for mapbox.
     */
    Map<A_MapMarker.COLOR, Integer> COLOR_MAP = new HashMap<A_MapMarker.COLOR, Integer>() {{
        put(A_MapMarker.COLOR.BLUE, Color.BLUE);
        put(A_MapMarker.COLOR.RED, Color.RED);
        put(A_MapMarker.COLOR.GREEN, Color.GREEN);
        put(A_MapMarker.COLOR.YELLOW,Color.YELLOW);
    }};
    /**
     * Public constructor.
     *
     * @param elements List of I_SortableMapElements to be displayed on map
     * @param metrics  of device
     */
    public HandlerCanvas(List<? extends I_SortableMapElement> elements, DisplayMetrics metrics) {
        super(elements, metrics);
    }

    public void setPointRadius(double radius){
        m_pointRadius=radius;
    }

    public double getPointRadius(){
        return m_pointRadius;
    }



    @Override
    Map<A_MapMarker.COLOR, Integer> getColorMap() {
        return COLOR_MAP;
    }

    @Override
    protected void updateSingleMarker(Canvas map, A_MapMarker m_marked) {
        return;
    }

    @Override
    protected void registerClickListener() {
        return;
    }

    @Override
    public void removeMarker(String marker) {
        return;
    }

    @Override
    public void showCurrentSortableMarker() {
        return;
    }

    @Override
    protected A_MapMarker getMarkerInstance() {
        return new CanvasMarker(this,m_textGenerator);
    }

    @Override
    protected boolean isInRegion(LatLngBounds bounds, LatLng place) {
        return bounds.contains(place);
    }

    @Override
    protected LatLngBounds getVisibleRegion(MercatorProjection projection) {
        return projection.getVisibleRegion();
    }
}
