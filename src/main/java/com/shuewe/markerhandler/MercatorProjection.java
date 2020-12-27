package com.shuewe.markerhandler;

public class MercatorProjection {

    public MercatorProjection center() {
        Coordinates nw = getCoordinates(new LatLng(m_northDeg,m_westDeg));
        Coordinates se = getCoordinates(new LatLng(m_southDeg,m_eastDeg));

        double yDist=se.getY()-nw.getY();
        double xDist=se.getX()-nw.getX();

        if(xDist > yDist){
            //increase y
            double toBeExtended = xDist -yDist;
            double ratio = toBeExtended / yDist;
            m_northDeg = m_northDeg + (m_northDeg-m_southDeg) * ratio/2;
            m_southDeg = m_southDeg - (m_northDeg-m_southDeg) * ratio/2;
        }else{
            double toBeExtended = yDist -xDist;
            double ratio = toBeExtended / xDist;
            double longDist = m_eastDeg-m_westDeg;
            if(longDist<0){
                longDist = (180-m_westDeg) + m_eastDeg+180;
            }
            m_eastDeg = m_eastDeg + longDist * ratio/2;
            m_westDeg = m_westDeg - longDist * ratio/2;
        }
        //Correct Datumsgrenze
        if(m_eastDeg < -180){
            m_eastDeg= 180 + (m_eastDeg+180);
        }
        if(m_westDeg < -180){
            m_westDeg = 180 +(m_westDeg+180);
        }
        if(m_eastDeg > 180){
            m_eastDeg= -180 + (m_eastDeg-180);
        }
        if(m_westDeg > 180){
            m_westDeg = -180 +(m_westDeg-180);
        }
        m_north=Math.toRadians(m_northDeg);
        m_west=Math.toRadians(m_westDeg);
        m_south=Math.toRadians(m_southDeg);
        m_east=Math.toRadians(m_eastDeg);
        return this;
    }

    public LatLngBounds getVisibleRegion() {
        return new LatLngBounds(new LatLng(m_northDeg,m_westDeg),new LatLng(m_southDeg,m_eastDeg));
    }

    public class Coordinates{
        private double m_x,m_y;

        private Coordinates(double x,double y){
            m_x=x;
            m_y=y;
        }

        public double getX(){
            return m_x;
        }

        public double getY(){
            return m_y;
        }
    }

    private double m_west,m_east;
    private double m_north,m_south;

    private double m_westDeg,m_eastDeg;
    private double m_northDeg,m_southDeg;

    private double m_projectionWidth, m_projectionHeight;

    private static double getMercatorY(double lat){
        return Math.log(Math.tan(lat/2+Math.PI/4));
    }

    public MercatorProjection(double width, double height){
        m_projectionHeight=height;
        m_projectionWidth=width;
    }

    public MercatorProjection withNorthWestBorder(LatLng latLng){
        m_north=Math.toRadians(latLng.latitude);
        m_west=Math.toRadians(latLng.longitude);
        m_northDeg=latLng.latitude;
        m_westDeg=latLng.longitude;
        return this;
    }
    public MercatorProjection withEastSouthBorder(LatLng latLng){
        m_south=Math.toRadians(latLng.latitude);
        m_east=Math.toRadians(latLng.longitude);
        m_southDeg=latLng.latitude;
        m_eastDeg=latLng.longitude;
        return this;
    }

    public Coordinates getCoordinates(LatLng latLng){
        double yMin=getMercatorY(m_south);
        double yMax=getMercatorY(m_north);
        double distLong=m_east-m_west;
        if(distLong<0){
            distLong = Math.toRadians((180-m_westDeg) + m_eastDeg+180);
        }
        double xFactor=m_projectionWidth/distLong;
        double yFactor=m_projectionHeight/(yMax-yMin);
        if (xFactor != yFactor){
            xFactor= Math.min(xFactor,yFactor);
            yFactor=xFactor;
        }
        double x = (Math.toRadians(latLng.longitude) - m_west) * xFactor;
        if(m_east<m_west) {
            if(latLng.longitude <0){
                //Rechts von Datumsgrenze
                x= (Math.PI-m_west +  Math.toRadians(180 + latLng.longitude)) * xFactor;
            }
        }
        double y = (yMax-getMercatorY(Math.toRadians(latLng.latitude)))*yFactor;
        return new Coordinates(x,y);
    }

}
