package com.techventus.locations;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("unchecked")
public class HelloItemizedOverlay extends ItemizedOverlay {
	
	String TAG = "TECHVENTUS - HelloItemizedOverlay";
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private LocationMap origin;
	private GeoPoint geopoint;
	
	private Paint paint1, paint2;
	private float radius = 1000;
	
	
	public HelloItemizedOverlay(Drawable defaultMarker,LocationMap locationMap) {
		super(boundCenterBottom(defaultMarker));
		origin =locationMap;
		radius = origin.radius;
		
		paint1 = new Paint();
	    paint1.setARGB(128, 0, 0, 255);
	    paint1.setStrokeWidth(2);
	    paint1.setStrokeCap(Paint.Cap.ROUND);
	    paint1.setAntiAlias(true);
	    paint1.setDither(false);
	    paint1.setStyle(Paint.Style.STROKE);

	    paint2 = new Paint();
	    paint2.setARGB(64, 0, 0, 255);
		
	}
	
	public void addOverlay(OverlayItem overlay) {
		//mOverlays.set(0, overlay);
	    mOverlays.add(overlay);
	    geopoint = overlay.getPoint();
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}
	
	@Override
	public int size() {
	
		return mOverlays.size();
	}
	
	@Override
	public boolean onTap(int index){
	//	Toast.makeText(origin,mOverlays.get(index).getSnippet(),Toast.LENGTH_SHORT).show();
		return(true);
		
	}
	

	
	@Override
	public boolean onTap(GeoPoint p,MapView mapView){
		//Toast.makeText(origin,"ITEM OVERLAY TAPPED",Toast.LENGTH_SHORT).show();
		//mOverlays.remove(mOverlays.size()-1);
		if(mOverlays.size()<1){
			mOverlays.add(new OverlayItem(p, "SnipTitle","SnipX"));
		}else{
			mOverlays.set(0, new OverlayItem(p, "SnipTitle", "SnipX"));
		}
		
		
		geopoint = p;
		origin.point = geopoint;
		populate();
//		Toast.makeText(origin,""+p.getLatitudeE6()+","+p.getLongitudeE6(),Toast.LENGTH_SHORT);
		
		//this.addOverlay(new OverlayItem(p, "SnipTitle", "Snip"+hio.size()+1));
		return true;
	}
	
	public GeoPoint getGeoPoint(){
		return geopoint;
		
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
	    Point pt = mapView.getProjection().toPixels(geopoint, null);
	    float projectedRadius = mapView.getProjection().metersToEquatorPixels(radius);

	    canvas.drawCircle(pt.x, pt.y, projectedRadius, paint2);
	    canvas.drawCircle(pt.x, pt.y, projectedRadius, paint1);

	}

	
	
}