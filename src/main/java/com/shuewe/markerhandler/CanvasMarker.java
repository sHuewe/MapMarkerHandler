package com.shuewe.markerhandler;

import android.graphics.Canvas;
import android.graphics.Paint;

public class CanvasMarker extends A_MapMarker<String, Canvas,MercatorProjection,Integer> {
    /**
     * Public constructor.
     *
     * @param handler       marker handler instance
     * @param textGenerator
     */
    public CanvasMarker(A_Handler handler, MarkerTextGenerator textGenerator) {
        super(handler, textGenerator);
    }

    @Override
    public A_MapMarker getInstance() {
        return new CanvasMarker(m_handler,m_textGenerator);
    }

    @Override
    public String setMarker(Canvas map, COLOR c) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(((HandlerCanvas)m_handler).getColorMap().get(c));
        MercatorProjection.Coordinates coor = ((HandlerCanvas) m_handler).getProjection().getCoordinates(m_center);
        map.drawCircle((float)coor.getX(),(float)coor.getY(), (float) ((HandlerCanvas)m_handler).getPointRadius(),p);
        return null;
    }

    @Override
    protected double getPixelDistance(LatLng pos, MercatorProjection projection) {
        MercatorProjection.Coordinates coorPoint = projection.getCoordinates(pos);
        MercatorProjection.Coordinates coorCenter = projection.getCoordinates(m_center);
        return Math.sqrt((coorPoint.getX() - coorCenter.getX()) * (coorPoint.getX() - coorCenter.getX()) + (coorPoint.getY() - coorCenter.getY()) * (coorPoint.getY() - coorCenter.getY()) );
    }

    @Override
    protected void updateMarker() {

    }
}
