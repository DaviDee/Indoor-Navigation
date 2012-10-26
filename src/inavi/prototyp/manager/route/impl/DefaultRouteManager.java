package inavi.prototyp.manager.route.impl;

import inavi.main.IndoorNavigation;
import inavi.manager.location.LocationManager;
import inavi.manager.map.MapManager;
import inavi.manager.route.RouteManager;
import inavi.map.IBuildingComponent;
import inavi.map.IMap;
import inavi.map.ITarget;
import inavi.prototyp.gui.RoutePrefsActivity;
import inavi.prototyp.manager.location.impl.DefaultLocationManager;
import inavi.prototyp.map.impl.DefaultTarget;

import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DefaultRouteManager extends RouteManager {
	private static final int PREFS_ITEM_ID = 0;
	private DefaultRoute router;
	private ITarget target;
	private List<IBuildingComponent> path;

	public DefaultRouteManager(IndoorNavigation context,
			LocationManager locationManager, MapManager mapManager) {
		super(context, locationManager, mapManager);
		this.router = new DefaultRoute();
	}

	public List<IBuildingComponent> getShortestPath(IBuildingComponent from,
			IBuildingComponent to) {
		return router.getShortestPath(mapManager.getMap(), from, to);
	}

	public synchronized void step() {
		if (path != null && !path.isEmpty()) {
			path.remove(0);
			((DefaultLocationManager) locationManager).setCurrentPosition(path
					.get(0));
			if (path.size() > 1) {
				int floor = path.get(1).getFloorNumber();
				if (floor != path.get(0).getFloorNumber()) {
					Toast.makeText(context,
							"Gehen Sie in die " + floor + "-te Etage",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(context, "Sie haben Ihr Ziel erreicht",
						Toast.LENGTH_SHORT).show();
				stopRouting();
			}
		}
	}

	@Override
	public synchronized List<IBuildingComponent> getCurrentRoute() {
		if(path == null)
			return null;
		//create a list copy to avoid a parallel threads access
		return new LinkedList<IBuildingComponent>(path);
	}

	@Override
	public synchronized ITarget getTarget() {
		return target;
	}

	@Override
	public synchronized void startRouting(ITarget target) {
		stopRouting();
		if (target != null) {
			this.target = target;
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			String alg = prefs.getString("route_algorithm", "minimal");
			int diff = Integer.valueOf(prefs.getString("floor_diff", "2"));
			router.setDiff(diff);
			router.setShortestPath(alg.equals("shortest"));

			calculatePath();
			context.targetChanged(target);
		}
	}

	private synchronized void calculatePath() {
		if (locationManager != null && mapManager != null) {
			IBuildingComponent from = locationManager
					.getCurrentBuildingComponent();
			if (from != null) {
				path = router
						.getShortestPath(mapManager.getMap(), from, target);
			}
		}
	}

	@Override
	public synchronized void stopRouting() {
		target = null;
		path = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(ROUTE_MANAGER_GROUP_ID, PREFS_ITEM_ID, 0, "Route Settings");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getGroupId() == ROUTE_MANAGER_GROUP_ID
				&& item.getItemId() == PREFS_ITEM_ID) {
			Intent intent = new Intent(context, RoutePrefsActivity.class);
			context.startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "RouteManager uses optimized Dijkstra-Algorithm for searching of the shortest path and paths with minimal points number";
	}

	@Override
	public String getName() {
		return "Default Route Manager";
	}

	@Override
	public IBuildingComponent getTargetComponent() {
		if (path != null && !path.isEmpty()) {
			return path.get(path.size() - 1);
		}
		return null;
	}

	@Override
	public void startRouting(IBuildingComponent component) {
		List<IBuildingComponent> list = new LinkedList<IBuildingComponent>();
		list.add(component);
		startRouting(new DefaultTarget("", list));
	}

	@Override
	public void mapChanged(IMap map) {
		stopRouting();
	}

	@Override
	public void positionChanged(IBuildingComponent component) {
		if (target != null && path != null && path.size() > 0
				&& path.get(0).getId() != component.getId()) {
			startRouting(target);
		}
	}

}
