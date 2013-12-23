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
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
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
	
	private static final int MENU_MAP_FLASH = Menu.FIRST + 1;
	
	private static final String FLASH_ON = Parameters.FLASH_MODE_TORCH;
	private static final String FLASH_OFF = Parameters.FLASH_MODE_OFF;
	
	private static final String LOG_TAG = "securityMAP";
	
	private MapView mapView;
	private MapPOIItem poiItem;
	private MapReverseGeoCoder reverseGeoCoder = null;
	private String centerAddress;
	private Camera camera;

	private boolean isFlashOn=false;
	
	private final String API_KEY="9ceae170ed20454fa4010dc4d4acb1ddf99f2113";	// �� APIŰ
	private final String DATA_KEY="b3bc0127812b7aabcc7e73900d1d69a87d194c41";	// �ּ� �˻��� �� �ʿ��� APIŰ
	private final String TITLE="��������";
	
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
        
        mapView.setMapType(MapView.MapType.Standard);
        
        linearLayout.addView(mapView);
        
        setContentView(linearLayout);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_MAP_FLASH, Menu.NONE, "�÷���");
		
		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		final int itemId = item.getItemId();
		
		switch (itemId) {
		case MENU_MAP_FLASH:
		{
			String[] flashMenuItems = {"�÷��� �ѱ�", "�÷��� ����"};

			Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("�÷���");
			dialog.setItems(flashMenuItems, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0: // Standard
					{
						synchronized(this) {
							Parameters p = camera.getParameters();
							p.setFlashMode(Parameters.FLASH_MODE_TORCH);
							camera.setParameters(p);
							camera.startPreview();
						}
					}
						break;
					case 1: // Satellite
					{
						synchronized(this) {
							Parameters p = camera.getParameters();
							p.setFlashMode(Parameters.FLASH_MODE_OFF);
							camera.setParameters(p);
							camera.stopPreview();
						}
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
	
	// �� ���� ����� ����Ǵ� �κ�
	public void onMapViewInitialized(MapView mapView) { 
		Log.i(LOG_TAG, "MapView had loaded. Now, MapView APIs could be called safely"); 
		
		// ���� �ʱ�ȭ
		camera = Camera.open();
		
		// ���ø����̼��� ó�� ���� �� ������ ���� ��ġ�� �̵����ش�
		mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
		
		// ���� ��ġ�� �޾ƿ´�
		reverseGeoCoder = new MapReverseGeoCoder(DATA_KEY, mapView.getMapCenterPoint(), MapActivity.this, MapActivity.this);
		reverseGeoCoder.startFindingAddress();
	} 
	
	@Override
	public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapCenterPoint) {
		MapPoint.GeoCoordinate mapPointGeo = mapCenterPoint.getMapPointGeoCoord();
		Log.i(LOG_TAG, String.format("MapView onMapViewCenterPointMoved (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
	}

	// ���� �ι� ������ �� ���� �� ����Ǵ� �κ�
	@Override
	public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
		
		MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("DaumMapLibrarySample");
		alertDialog.setMessage(String.format("Double-Tap on (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
		alertDialog.setPositiveButton("OK", null);
		alertDialog.show();
	}

	// ������ ��� ������ �� ǥ�õǴ� �κ�
	@Override
	public void onMapViewLongPressed(final MapView mapView, MapPoint mapPoint) {

		final MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();

		reverseGeoCoder = new MapReverseGeoCoder(DATA_KEY, mapView.getMapCenterPoint(), MapActivity.this, MapActivity.this);
		reverseGeoCoder.startFindingAddress();
		
		String[] mapTypeMenuItems = {"���� �߰��ϱ�"};
		
		Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(centerAddress);
		dialog.setItems(mapTypeMenuItems, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: // ��ġ �߰�
				{
					poiItem = new MapPOIItem();
					poiItem.setItemName(centerAddress);
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

	// ������ �ѹ��� �� ���� �� ǥ�õǴ� �κ�
	@Override
	public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
		MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
		Log.i(LOG_TAG, String.format("MapView onMapViewSingleTapped (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
	}

	// ������ �� ������ �����Ͽ��� �� ǥ�õǴ� �κ�
	@Override
	public void onMapViewZoomLevelChanged(MapView mapView, int zoomLevel) {
		Log.i(LOG_TAG, String.format("MapView onMapViewZoomLevelChanged (%d)", zoomLevel));
	}
	
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
	
	// ��Ŀ ������ �����Ͽ��� �� ����Ǵ� �κ�
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

	// ��Ŀ�� �̵��Ͽ��� �� ǥ�õǴ� �κ�
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

	// ������ �ּҸ� ã���� �� ����Ǵ� �κ�
	@Override
	public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder rGeoCoder, String addressString) {
		
		centerAddress = addressString;
		reverseGeoCoder = null;
	}
	
	// �ּҸ� ã�� ������ �� ����Ǵ� �κ�
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