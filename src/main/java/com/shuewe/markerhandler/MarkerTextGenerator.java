package com.shuewe.markerhandler;

import java.util.List;

public interface MarkerTextGenerator {

    MarkerTextGenerator NULL_GENERATOR= new MarkerTextGenerator() {
        @Override
        public String getMarkerTitle(List<I_SortableMapElement> elements, int count) {
            return null;
        }

        @Override
        public String getMarkerDescription(List<I_SortableMapElement> elements, int count) {
            return null;
        }
    };


    String getMarkerTitle(List<I_SortableMapElement> elements,int count);
    String getMarkerDescription(List<I_SortableMapElement> elements,int count);
}
