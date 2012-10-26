package inavi.prototyp.main;

import inavi.main.IndoorNavigation;
import inavi.manager.display.DisplayManager;
import inavi.manager.location.LocationManager;
import inavi.manager.map.MapManager;
import inavi.manager.route.RouteManager;
import inavi.prototyp.gui.DefaultTargetSelectionActivity;
import inavi.prototyp.gui.HelpActivity;
import inavi.prototyp.gui.INaviSurfaceView;
import inavi.prototyp.manager.display.impl.DefaultDisplayManager;
import inavi.prototyp.manager.location.impl.DefaultLocationManager;
import inavi.prototyp.manager.map.impl.DefaultMapManager;
import inavi.prototyp.manager.route.impl.DefaultRouteManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class IndoorNavigationPrototyp extends IndoorNavigation {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((INaviSurfaceView) findViewById(R.id.surfaceView))
				.setDisplayManager(displayManager);
		collectToolBarViews();
	}

	private void collectToolBarViews() {
		int alpha = 130;

		ImageButton step = (ImageButton) findViewById(R.id.stepButton);
		step.getBackground().setAlpha(alpha);
		step.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((DefaultRouteManager) routeManager).step();
			}
		});

		ImageButton zoomIn = (ImageButton) findViewById(R.id.zoomInButton);
		zoomIn.getBackground().setAlpha(alpha);
		zoomIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((DefaultDisplayManager) displayManager).zoomIn();
			}
		});
		ImageButton zoomOut = (ImageButton) findViewById(R.id.zoomOutButton);
		zoomOut.getBackground().setAlpha(alpha);
		zoomOut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((DefaultDisplayManager) displayManager).zoomOut();
			}
		});

	}

	@Override
	protected DisplayManager getDisplayManager(MapManager manager,
			RouteManager routeManager, LocationManager locationManager) {
		return new DefaultDisplayManager(this, routeManager, locationManager,
				mapManager);
	}

	@Override
	protected LocationManager getLocationManager(MapManager mapManager) {
		return new DefaultLocationManager(this, mapManager);
	}

	@Override
	protected MapManager getMapManager() {
		return new DefaultMapManager(this);
	}

	@Override
	protected RouteManager getRouteManager(MapManager mapManager,
			LocationManager locationManager) {
		return new DefaultRouteManager(this, locationManager, mapManager);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.inavi_main_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.helpItem){
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Drawable getStartRoutingIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getStartRoutingTitle() {
		return "Start Routing";
	}

	@Override
	protected Drawable getStopRoutingIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getStopRoutingTitle() {
		return "Stop Routing";
	}

	@Override
	protected Class getTargetSelectionActivityClass() {
		return DefaultTargetSelectionActivity.class;
	}
	
}