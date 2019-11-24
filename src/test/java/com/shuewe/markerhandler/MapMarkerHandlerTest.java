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

import com.shuewe.markerhandler.dummy.DummyData;
import com.shuewe.markerhandler.dummy.HandlerDummy;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Class with some test cases for the abstract classes.
 */
public class MapMarkerHandlerTest {

    /**
     * Tests the estimation of visible elements on map. setVisibleElements is a helper method in the dummy implementation to set info which are normally given by the projection.
     */
    @Test
    public void testVisibleElements() {
        List<DummyData> elements = new ArrayList<DummyData>();
        elements.add(new DummyData(new LatLng(40, 20)));
        elements.add(new DummyData(new LatLng(44, 21)));
        elements.add(new DummyData(new LatLng(20, 2)));
        elements.add(new DummyData(new LatLng(15, 4)));
        A_Handler handler = new HandlerDummy(elements, null);
        HandlerDummy.PIXEL_DISTANCE_FACTOR = 1;
        ((HandlerDummy) handler).setVisibleElements(new HashSet<>(elements.subList(1, 2)));
        handler.updateMarkerOnMap(null, null, null, 0);
        assertTrue(handler.m_elements_vis.size() == 1);
        ((HandlerDummy) handler).setVisibleElements(new HashSet<>(elements.subList(0, 2)));
        handler.updateMarkerOnMap(null, null, null, 0);
        assertTrue(handler.m_elements_vis.size() == 2);
        ((HandlerDummy) handler).setVisibleElements(new HashSet<>(elements.subList(0, 3)));
        handler.updateMarkerOnMap(null, null, null, 0);
        assertTrue(handler.m_elements_vis.size() == 3);
        System.out.print(handler.m_marker.size());
    }

    /**
     * Tests the clustering functions. For the dummy implementation the distance of two coordinates in pixel is simplified by
     * sqrt((lat1-lat2)*(lat1-lat2) + (lng1-lng2)*(lng1-lng2))*factor. Zooming is simulated by changing 'factor' (HandlerDummy.PIXEL_DISTANCE_FACTOR).
     */
    @Test
    public void testZooming() {
        List<DummyData> elements = new ArrayList<DummyData>();
        elements.add(new DummyData(new LatLng(40, 20)));
        elements.add(new DummyData(new LatLng(42, 21)));
        elements.add(new DummyData(new LatLng(44, 21)));
        elements.add(new DummyData(new LatLng(20, 2)));
        elements.add(new DummyData(new LatLng(15, 4)));
        A_Handler handler = new HandlerDummy(elements, null);

        //First three elements should be represented as one marker
        HandlerDummy.PIXEL_DISTANCE_FACTOR = 1;
        ((HandlerDummy) handler).setVisibleElements(new HashSet<>(elements)); //All elements are on the map
        handler.updateMarkerOnMap(null, null, null, 5);
        assertTrue(handler.m_marker.size() == 3); //Checks the amount of markers on the map
        assertEquals(handler.m_elements_vis.size(), elements.size()); //Checks if all elements are still represented by markers on the map

        //Zoom in. Factor gets increased, so all elements have a higher simulated distance and all should be displayed separately.
        HandlerDummy.PIXEL_DISTANCE_FACTOR = 10;
        handler.updateMarkerOnMap(null, null, null, 6); //Have to change zoom, because otherwise zoom is not checked again
        assertTrue(handler.m_marker.size() == 5);
        assertEquals(handler.m_elements_vis.size(), elements.size());

        //Zoom back to initial situation
        HandlerDummy.PIXEL_DISTANCE_FACTOR = 1;
        handler.updateMarkerOnMap(null, null, null, 5);
        assertTrue(handler.m_marker.size() == 3);
        assertEquals(handler.m_elements_vis.size(), elements.size());

        //Zoom out. Factor gets decreased, so all elements seems to be close to each other and should be displayed as one marker
        HandlerDummy.PIXEL_DISTANCE_FACTOR = 0.01f;
        handler.updateMarkerOnMap(null, null, null, 4); //Have to change zoom, because otherwise zoom is not checked again
        assertEquals(1, handler.m_marker.size());
        assertEquals(handler.m_elements_vis.size(), elements.size());

        System.out.print(handler.m_marker.size());
    }
}
