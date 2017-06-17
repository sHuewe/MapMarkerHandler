# MapMarkerHandler

This library allows to show a large amount of markers on a google map in Android. To allow this, the library sums up markers which would be positioned to near to each other on the map.
The [Geo Picture Map App](https://play.google.com/store/apps/details?id=com.shuewe.picturemap) demonstrates a possible usage of the MapMarkerHandler.

## How to use the library

You find compiled aar files to all releases in the [release folder](release). The [current version](release/mapMarkerHandler_v1.1.aar) belongs to the v1.1 tag of the repository. If you want to edit the library, you can compile this repository and add it as dependency to your Android project.

### Prepare data to be handeled by the library

The class of the data objects have to implement the interface `I_SortableMapElement`. The interface only consists of two methods. `getLatLng()` has to deliver the LatLng object which 
should be displayed for the object. `getSortPropertyString()` allows to pass a string which can be used if the passed List of data-object was sorted. In this case it is possible to iterate 
through the passed list and the String from this method gets used for the snippet of the marker. A simple example for a picture object, which gets sorted by date, could look like this:

```java
public class PictureData implements I_SortableMapElement{

...

@Override
public LatLng getLatLng(){
	return new LatLng(m_lat,m_lng)
}

@Override
public String getSortPropertyString(){
	return "Pictures taken on 2017/04/01";
}

...

}
```

### Use the library

First you have to pass a List of type `List<? extends I_SortableMapElement>` to the library. If you want to use functions related to the sorting function, this List has to be sorted by yourself (before passing it to the library).
The initialization for our picture-example (with `pictures`is a List of `PictureData`) is done by 
```java
MarkerHandler handler=	new MarkerHandler(pictures, getResources().getDisplayMetrics());
handler.prepareSortedElements();//Only needed if sorted property should be used.
				//For very large datasets (>5000), you should do this in a seperate thread. 
				//Make sure, that this is finished before using sorting functions.
```
To draw markers to a map, you have to call 
```java
handler.updateMarkerOnMap(MyActivity.this,map,projection,zoom);
```
To handle camera changes following code works as OnCamerIdleListener:
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
### Iterate through the data

You can iterate through all elements stored under a certain marker on the map or iterate through your passed elements according to your passed sorting.

#### Iterate through elements of a marker
This is useful to react on click events on the markers. Following code demonstrates it
```java
@Override
public boolean onMarkerClick(Marker arg0) {
	m_markerHandler.moveMarkerCursor(arg0);
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
