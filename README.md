# MapMarkerHandler

This library allows to show a large amount of markers on maps in Android. To allow this, the library sums up markers which would be positioned to near to each other on the map.
The [Geo Picture Map App](https://play.google.com/store/apps/details?id=com.shuewe.picturemap) demonstrates a possible usage of the MapMarkerHandler.
Currently the library supports maps from google and from mapbox via following flavors:
 * google
 * mapbox
 * googleAndMapbox.
 
By the use of this library it is very easy to switch between google an mapbox as map providers.

## Setup / Preparation

You find compiled aar files to all releases in the [release folder](release). The current version belongs to the v2.0 tag of the repository. If you want to edit the library, you can compile this repository and add it as dependency to your Android project.
* [google](release/mapMarkerHandler_google_v2.0.aar)
* [mapbox](release/mapMarkerHandler_mapbox_v2.0.aar)
* [googleAndMapbox](release/mapMarkerHandler_googleandmapbox_v2.0.aar)

### Prepare data to be handled by the library

The class of the data objects have to implement the interface `I_SortableMapElement`. The interface only consists of two methods. `getLatLng()` has to deliver the LatLng object which 
should be displayed for the object. `getSortPropertyString()` allows to pass a string which can be used if the passed List of data-object was sorted. In this case it is possible to iterate 
through the passed list and the String from this method gets used for the snippet of the marker. Further, you can implement getId(), if you want to control single elements by ID.
 
 A simple example for a picture object, which gets sorted by date, could look like this:

```java
public class PictureData implements I_SortableMapElement{

    private double m_lat,m_lng;
    private String m_id,m_sortString;

    public PictureData(double lat,double lng, String id,String sortString){
        m_lat=lat;
        m_lng=lng;
        m_id=id;
        m_sortString=sortString;
    }

    @Override
    public LatLng getLatLng(){
	    return new LatLng(m_lat,m_lng);
    }

    @Override
    public String getSortPropertyString(){
	    return m_sortString; 
    }

    @Override
    public String getId(){
        return m_id;
    }
}
```

## Cluster Data

### Init
First you have to pass a List of type `List<? extends I_SortableMapElement>` to the library. If you want to use functions related to the sorting function, this List has to be sorted by yourself (before passing it to the library).
The initialization for our picture-example (with `pictures`is a List of `PictureData`) is done by 

#### Google
```java
A_Handler handler=	new HandlerGoogle(pictures, getResources().getDisplayMetrics());
handler.prepareSortedElements();//Only needed if sorted property should be used.
				//For very large datasets (>5000), you should do this in a seperate thread. 
				//Make sure, that this is finished before using sorting functions.
```
#### Mapbox
```java
A_Handler handler=	new HandlerMapbox(pictures, getResources().getDisplayMetrics());
handler.prepareSortedElements();//Only needed if sorted property should be used.
				//For very large datasets (>5000), you should do this in a seperate thread. 
				//Make sure, that this is finished before using sorting functions.
```

### Draw markers on map
#### Google
```java
public void onMapReady(GoogleMap map) {
     final float zoom = map.getCameraPosition().zoom;
     final Projection projection = map.getProjection();
     handler.updateMarkerOnMap(MyActivity.this, map, projection, zoom);
}
```
#### Mapbox
```java
private MapView mapView = findViewById(R.id.myMapboxView);
mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {

                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        ((HandlerMapbox)handler).setMapboxMap(mapboxMap);
                        ((HandlerMapbox)handler).setInfoView((TextView) findViewById(R.id.infoText));
                        style.addImage(MapMarkerMapbox.ID_MARKER_DEFAULT,
                                        BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.marker)), //You have to add a suitable drawable for markers to your project
                                        true);
                        GeoJsonOptions geoJsonOptions = new GeoJsonOptions().withTolerance(0.4f);
                        symbolManager = new SymbolManager(mapView,mapboxMap,style,null,geoJsonOptions);
                        symbolManager.setIconAllowOverlap(true);
                        final float zoom = (float)mapboxMap.getCameraPosition().zoom;
                        final Projection projection = mapboxMap.getProjection();
                        handler.updateMarkerOnMap(MyActivity.this, symbolManager, projection, zoom);
                    }
                });
            }
});
```

### React on CameraIdle events:

#### Google
```java
new GoogleMap.OnCameraIdleListener() {
	@Override
	public void onCameraIdle() {
		float zoom = map.getCameraPosition().zoom;
		Projection projection=map.getProjection();
		handler.updateMarkerOnMap(MyActivity.this,map,projection,zoom);
	}
}
```
#### Mapbox
```java
mapboxMap.addOnCameraIdleListener(new MapboxMap.OnCameraIdleListener() {
    @Override
    public void onCameraIdle() {
        final float zoom = (float)mapboxMap.getCameraPosition().zoom;
        final Projection projection = mapboxMap.getProjection();
        handler.updateMarkerOnMap(MyActivity.this, symbolManager, projection, zoom);
    }
});
```
### Iterate through the data

You can iterate through all elements stored under a certain marker on the map or iterate through your passed elements according to your passed sorting.

#### Iterate through elements of a marker
This is useful to react on click events on the markers. Following code demonstrates it
```java
@Override
public boolean onMarkerClick(Marker arg0) {
	handler.moveMarkerCursor(arg0);
	doSomethingWithThePicture(handler.getElementFromMarker(arg0));
	return true;
}
```

#### Iterate through sorted elements
In our example this can be used to show the markers chronologicaly like offered by the [Geo Picture Map App](https://play.google.com/store/apps/details?id=com.shuewe.picturemap). Following code demonstrates it.
```java
private void doSthWithNextPicture() {
	handler.moveCursorNextSortable();
	doSomethingWithThePicture(handler.getSortableElement());
}
```
## Handling of single markers per Id
The simplest way to use this library, is by adding single (not clustered) markers by their Id. For doing this the init code from above can be simplified to the following:
 #### Google
 ```java
 A_Handler handler=	new HandlerGoogle(Collections.<I_SortableMapElement>emptyList(), getResources().getDisplayMetrics());
 ```
 #### Mapbox
 ```java
 A_Handler handler=	new HandlerMapbox(Collections.<I_SortableMapElement>emptyList(), getResources().getDisplayMetrics());
 ```
 For adding or removing markers to the map, you need to get Instances of `GoogleMap map` (for google), resp. `SymbolManager symbolManager` (for mapbox) like shown [here](#google-1) and [here](#mapbox-1).
 Then you can add a blue marker for 
  ```java
  PictureData pic = new PictureData(40,20,"HOME","My home");
  ```
  by this code:
  #### Google
  ```java
    handler.addElementWithId(pic,map,"My home",A_MapMarker.COLOR.BLUE);
  ```
  #### Mapbox
  ```java
    handler.addElementWithId(pic,symbolManager,"My home",A_MapMarker.COLOR.BLUE);
  ```
  To remove the marker, use this:
  #### Google
  ```java
    handler.removeElementById(pic.getId(),map);
  ```
   #### Mapbox
   ```java
    handler.removeElementById(pic.getId(),symbolManager);
   ```
