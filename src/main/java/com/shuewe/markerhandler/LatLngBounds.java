package com.shuewe.markerhandler;

public class LatLngBounds {
    private LatLng m_northWest;
    private LatLng m_southEast;


    public LatLngBounds(){

    }

    public LatLngBounds(LatLng northWest,LatLng southEast){
        m_northWest=northWest;
        m_southEast=southEast;
    }

    public void addPadding() {
        //TODO Datumsgrenze!
        double longDist = m_southEast.longitude-m_northWest.longitude;
        if(longDist<0){
            longDist = (180-m_northWest.longitude) + m_southEast.longitude+180;
        }
        double latDist= Math.abs(m_northWest.latitude-m_southEast.latitude);

        m_northWest.setLat(m_northWest.latitude+latDist/10);
        m_southEast.setLat(m_southEast.latitude-latDist/10);


        m_northWest.setLng(m_northWest.longitude-longDist/10);
        m_southEast.setLng(m_southEast.longitude+longDist/10);

        if(m_southEast.longitude < -180){
            m_southEast.setLng(180-m_southEast.longitude+180);
        }
        if(m_northWest.longitude < -180){
            m_northWest.setLng(180-m_northWest.longitude+180);
        }
        if(m_southEast.longitude > 180){
            m_southEast.setLng(-180+(m_southEast.longitude-180));
        }
        if(m_northWest.longitude > 180){
            m_northWest.setLng(-180+(m_northWest.longitude-180));
        }
    }

    public boolean contains(LatLng point){
        if(m_northWest==null || m_southEast==null){
            return false;
        }
        if(m_northWest.longitude > m_southEast.longitude){
            //Datumsgrenze
            if(point.longitude > 0){
                //Left side (to Asia)
                if(point.longitude < m_northWest.longitude){
                    return false;
                }
            }else{
                //Right side (to America)
                if(point.longitude > m_southEast.longitude){
                    return false;
                }
            }
        }else{
            if(point.longitude < m_northWest.longitude || point.longitude > m_southEast.longitude){
                return false;
            }
        }
        if(point.latitude > m_northWest.latitude || point.latitude < m_southEast.latitude){
            return false;
        }
        return true;

    }

    public LatLng getNorthWest() {
        return m_northWest;
    }

    public LatLng getSouthEast(){
        return m_southEast;
    }

    public void include(LatLng point){
        if(m_northWest == null || m_southEast == null){
            m_northWest=new LatLng(point.latitude,point.longitude);
            m_southEast=new LatLng(point.latitude,point.longitude);
        }
        if(contains(point)){
            return;
        }
        if(point.latitude > m_northWest.latitude){
            m_northWest.setLat(point.latitude);
        }
        if(point.latitude < m_southEast.latitude){
            m_southEast.setLat(point.latitude);
        }
        //Lat is corrected, check if long has to be corrected too
        if(contains(point)){
            return;
        }
        double changedLongEast =  Math.abs(m_southEast.longitude-point.longitude);
        double changedLongWest =  Math.abs(m_northWest.longitude-point.longitude);
        boolean overDateLimit=false;

        if(changedLongEast > 180){
            changedLongEast = 360 -changedLongEast;
            overDateLimit=true;
        }
        if(changedLongWest > 180){
            changedLongWest = 360 - changedLongWest;
            overDateLimit=true;
        }


        if(changedLongEast == changedLongWest){
            //TODO Datumsgrenze!
            if(overDateLimit){
                if(point.longitude > m_southEast.longitude){
                    //East
                    m_northWest.setLng(point.longitude);
                }else{
                    //West
                    m_southEast.setLng(point.longitude);
                }
            }else{
                if(point.longitude > m_southEast.longitude){
                    //East
                    m_southEast.setLng(point.longitude);
                }else{
                    //West
                    m_northWest.setLng(point.longitude);
                }
            }

            return;
        }
        if(changedLongEast > changedLongWest){
            m_northWest.setLng(point.longitude);
        }else{
            m_southEast.setLng(point.longitude);
        }
    }
}
