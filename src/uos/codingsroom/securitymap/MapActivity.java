package uos.codingsroom.securitymap;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MapActivity extends Activity implements
	MapView.OpenAPIKeyAuthenticationResultListener,
	MapView.MapViewEventListener,
	MapView.CurrentLocationEventListener,
	MapView.POIItemEventListener,
	MapReverseGeoCoder.ReverseGeoCodingResultListener {
	
	private static final int MENU_MAP_TYPE = Menu.FIRST + 1;
	private static final int MENU_MAP_MOVE = Menu.FIRST + 2;
	private static final int MENU_LOCATION_TRACKING = Menu.FIRST + 3;
	private static final int MENU_MAP_OVERLAY = Menu.FIRST + 4;
	
	private static final String LOG_TAG = "DaumMapLibrarySample";
	
	private MapView mapView;
	private MapPOIItem poiItem;
	private MapReverseGeoCoder reverseGeoCoder = null;

	private final String API_KEY="9ceae170ed20454fa4010dc4d4acb1ddf99f2113";
	private final String DATA_KEY="1fbb384aa99b21afd8457926d076ce810df72716";
	private final String TITLE="안전지도";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(TITLE);
        
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        
        MapView.setMapTilePersistentCacheEnabled(true);
        
        mapView = new MapView(this);

        mapView.setDaumMapApiKey(API_KEY);
        mapView.setOpenAPIKeyAuthenticationResultListener(this);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationEventListener(this);
        mapView.setPOIItemEventListener(this);
        
        //mapView.setMapType(MapView.MapType.Hybrid);
        mapView.setMapType(MapView.MapType.Standard);
        
        linearLayout.addView(mapView);
        
        setContentView(linearLayout);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_MAP_TYPE, Menu.NONE, "지도 종류");
		menu.add(0, MENU_MAP_OVERLAY, Menu.NONE, "Overlay");
		
		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		final int itemId = item.getItemId();
		
		switch (itemId) {
		case MENU_MAP_TYPE:
		{
			String[] mapTypeMenuItems = {"일반", "위성", "하이브리드"};

			Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Map Type");
			dialog.setItems(mapTypeMenuItems, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0: // Standard
					{
						mapView.setMapType(MapView.MapType.Standard);
					}
						break;
					case 1: // Satellite
					{
						mapView.setMapType(MapView.MapType.Satellite);
					}
						break;
					case 2: // Hybrid
					{
						mapView.setMapType(MapView.MapType.Hybrid);
					}
						break;
					}
				}

			});
			dialog.show();
		}
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// net.daum.mf.map.api.MapView.OpenAPIKeyAuthenticationResultListener

	@Override
	public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int resultCode, String resultMessage) {
		Log.i(LOG_TAG,	String.format("Open API Key Authentication Result : code=%d, message=%s", resultCode, resultMessage));	
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// net.daum.mf.map.api.MapView.MapViewEventListener
	
	public void onMapViewInitialized(MapView mapView) { 
		Log.i(LOG_TAG, "MapView had loaded. Now, MapView APIs could be called safely"); 
		// 어플리케이션을 처음 실행 시 지도를 현재 위치로 이동해준다
		mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
	} 
	
	@Override
	public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapCenterPoint) {
		MapPoint.GeoCoordinate mapPointGeo = mapCenterPoint.getMapPointGeoCoord();
		Log.i(LOG_TAG, String.format("MapView onMapViewCenterPointMoved (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
	}

	@Override
	public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
		
		MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("DaumMapLibrarySample");
		alertDialog.setMessage(String.format("Double-Tap on (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
		alertDialog.setPositiveButton("OK", null);
		alertDialog.show();
	}

	@Override
	public void onMapViewLongPressed(final MapView mapView, MapPoint mapPoint) {

		final MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
		
		String[] mapTypeMenuItems = {"정보 추가하기"};
		
		Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(TITLE);
		dialog.setItems(mapTypeMenuItems, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: // 위치 추가
				{
					poiItem = new MapPOIItem();
					poiItem.setItemName("City on a Hill");
					poiItem.setMapPoint(MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude,mapPointGeo.longitude));
					poiItem.setMarkerType(MapPOIItem.MarkerType.BluePin);
					poiItem.setShowAnimationType(MapPOIItem.ShowAnimationType.DropFromHeaven);
					mapView.addPOIItem(poiItem);
				}
					break;
				}
			}

		});
		dialog.show();
	}

	@Override
	public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
		MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
		Log.i(LOG_TAG, String.format("MapView onMapViewSingleTapped (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
	}

	@Override
	public void onMapViewZoomLevelChanged(MapView mapView, int zoomLevel) {
		Log.i(LOG_TAG, String.format("MapView onMapViewZoomLevelChanged (%d)", zoomLevel));
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// net.daum.mf.map.api.MapView.CurrentLocationEventListener
	
	@Override
	public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
		MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
		Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
	}

	@Override
	public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float headingAngle) {
		Log.i(LOG_TAG, String.format("MapView onCurrentLocationDeviceHeadingUpdate: device heading = %f degrees", headingAngle));
	}
	
	@Override
	public void onCurrentLocationUpdateFailed(MapView mapView) {
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("DaumMapLibrarySample");
		alertDialog.setMessage("Current Location Update Failed!");
		alertDialog.setPositiveButton("OK", null);
		alertDialog.show();
	}
	
	@Override
	public void onCurrentLocationUpdateCancelled(MapView mapView) {
		Log.i(LOG_TAG, "MapView onCurrentLocationUpdateCancelled");
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// net.daum.mf.map.api.MapView.POIItemEventListener
	
	@Override
	public void onPOIItemSelected(MapView mapView, MapPOIItem poiItem) {
		Log.i(LOG_TAG, String.format("MapPOIItem(%s) is selected", poiItem.getItemName()));
	}
	
	@Override
	public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem poiItem) {

		String alertMessage = null;
		
		if (poiItem == this.poiItem) {
			
			alertMessage = "Touched the callout-balloon of item1";
			
		} else if (poiItem.getTag() == 153) {
			
			String addressForThisItem = MapReverseGeoCoder.findAddressForMapPoint("DAUM_LOCAL_DEMO_APIKEY", poiItem.getMapPoint());
			alertMessage = String.format("Touched the callout-balloon of item2 (address : %s)", addressForThisItem);
			
		} else if ((poiItem.getUserObject() instanceof String) &&  poiItem.getUserObject().equals("item3")) {
			
			/*Intent intent = new Intent(this, MapPOIDetailActivity.class);
			intent.putExtra("POIName", poiItem.getItemName());
			startActivity(intent); */
			return;
			
		} else if (poiItem.getTag() == 276) {
			
			alertMessage = "Touched the callout-balloon of item4";
		}

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("DaumMapLibrarySample");
		alertDialog.setMessage(alertMessage);
		alertDialog.setPositiveButton("OK", null);
		alertDialog.show();
	}

	@Override
	public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem poiItem, MapPoint newMapPoint) {
		
		MapPoint.GeoCoordinate newMapPointGeo = newMapPoint.getMapPointGeoCoord();
		String alertMessage = String.format("Draggable MapPOIItem(%s) has moved to new point (%f,%f)", poiItem.getItemName(), newMapPointGeo.latitude, newMapPointGeo.longitude);
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("DaumMapLibrarySample");
		alertDialog.setMessage(alertMessage);
		alertDialog.setPositiveButton("OK", null);
		alertDialog.show();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// net.daum.mf.map.api.MapReverseGeoCoder.ReverseGeoCodingResultListener
	
	@Override
	public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder rGeoCoder, String addressString) {
		
		String alertMessage = String.format("Center Point Address = [%s]", addressString);
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("DaumMapLibrarySample");
		alertDialog.setMessage(alertMessage);
		alertDialog.setPositiveButton("OK", null);
		alertDialog.show();
		
		reverseGeoCoder = null;
	}
	
	@Override
	public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder rGeoCoder) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("DaumMapLibrarySample");
		alertDialog.setMessage("Reverse Geo-Coding Failed");
		alertDialog.setPositiveButton("OK", null);
		alertDialog.show();
		
		reverseGeoCoder = null;
	}

}