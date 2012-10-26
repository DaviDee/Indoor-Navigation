package inavi.prototyp.manager.map.impl;

import inavi.main.IndoorNavigation;
import inavi.manager.map.MapManager;
import inavi.map.IBuildingComponent;
import inavi.map.IFloor;
import inavi.map.IMap;
import inavi.map.ITarget;
import inavi.map.ITargetCategory;
import inavi.map.Point;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Dialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

public class DefaultMapManager extends MapManager {

	private static final int LOAD_ITEM_ID = 0;
	private IMap map;
	private static MapManager instance;

	public DefaultMapManager(IndoorNavigation context) {
		super(context);
		instance = this;
	}

	public static MapManager getInstance() {
		return instance;
	}

	public List<IBuildingComponent> getBuildingComponents() {
		if (map != null) {
			List<IFloor> floors = map.getFloors();
			if (floors != null && !floors.isEmpty()) {
				List<IBuildingComponent> components = new ArrayList<IBuildingComponent>();
				for (IFloor floor : floors)
					components.addAll(floor.getBuildingComponents());
				return components;
			}
		}
		return null;
	}

	public List<IFloor> getFloors() {
		if (map != null)
			return map.getFloors();
		return null;
	}

	public int getFloorsCount() {
		List<IFloor> floors = map.getFloors();
		if (floors != null)
			return floors.size();
		return 0;
	}

	public IMap getMap() {
		return map;
	}

	public List<IBuildingComponent> getStairs() {
		List<IBuildingComponent> components = getBuildingComponents();
		if (components != null) {
			List<IBuildingComponent> result = new LinkedList<IBuildingComponent>();
			for (IBuildingComponent c : components) {
				if (c.getNeighbors() != null) {
					for (IBuildingComponent n : c.getNeighbors()) {
						if (n.getFloorNumber() != c.getFloorNumber()) {
							result.add(c);
							break;
						}
					}
				}
			}
			return result;
		}
		return null;
	}

	public List<ITarget> getTargets() {
		if (map != null) {
			List<ITargetCategory> categories = map.getCategories();
			if (categories != null && !categories.isEmpty()) {
				List<ITarget> targets = new ArrayList<ITarget>();
				for (ITargetCategory category : categories) {
					targets.addAll(category.getTargets());
				}
			}
		}
		return null;
	}

	public void loadMap(String path) {
		map = DefaultMapParser.parse(path);
		context.mapChanged(map);
	}

	@Override
	public IFloor getFloor(int number) {
		if (map != null) {
			List<IFloor> floors = map.getFloors();
			if (floors != null && !floors.isEmpty()) {
				for (IFloor floor : floors)
					if (floor.getFloorNumber() == number)
						return floor;
			}
		}
		return null;
	}

	@Override
	public List<ITargetCategory> getTargetCategories() {
		if (map != null)
			return map.getCategories();
		return null;
	}

	@Override
	public IBuildingComponent getBuildingComponentAt(Point p) {
		IFloor floor = getFloor(p.getFloor());
		if (floor != null)
			return floor.getBuildingComponentAt(p.getX(), p.getY());
		return null;
	}

	@Override
	public IBuildingComponent getBuildingComponentAt(Point p, float tolerance) {
		IFloor floor = getFloor(p.getFloor());
		if (floor != null)
			return floor.getBuildingComponentAt(p.getX(), p.getY(), tolerance);
		return null;
	}

	@Override
	public String getDescription() {
		return "Manager creates Map from XML-file on the SD-Card";
	}

	@Override
	public String getName() {
		return "Default Map Manager";
	}

	@Override
	public String[][] getAvailableMaps() {
		File dir = new File("/sdcard");
		if (dir.exists() && dir.isDirectory()) {
			String files[] = dir.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".xml");
				}
			});
			String result[][] = new String[2][files.length];
			for (int i = 0; i < files.length; i++) {
				result[0][i] = files[i];
				result[1][i] = dir.getPath() + "/" + files[i];
			}
			return result;
		}
		return null;
	}

	@Override
	public IBuildingComponent getBuildingComponent(int id) {
		List<IBuildingComponent> list = getBuildingComponents();
		if (list != null) {
			for (IBuildingComponent b : list) {
				if (b.getId() == id)
					return b;
			}
		}
		return null;
	}

	@Override
	public IFloor getFirstFloor() {
		List<IFloor> floors = getFloors();
		if (floors != null && !floors.isEmpty()) {
			IFloor min = floors.get(0);
			for (int i = 1; i < floors.size(); i++) {
				IFloor f = floors.get(i);
				if (f.getFloorNumber() < min.getFloorNumber())
					min = f;
			}
			return min;
		}
		return null;
	}

	@Override
	public ITarget getTarget(String categoryName, String targetName) {
		if (categoryName != null && targetName != null) {
			List<ITargetCategory> categories = getTargetCategories();
			if (categories != null) {
				for (ITargetCategory c : categories) {
					if (c.getName().equals(categoryName)) {
						List<ITarget> targets = c.getTargets();
						if (targets != null) {
							for (ITarget t : targets) {
								if (t.getName().equals(targetName)) {
									return t;
								}
							}
						}
						break;
					}
				}
			}
		}
		return null;
	}

	public void showMapSelectionDialog() {
		final Dialog dialog = new Dialog(context);
		dialog.setTitle("Load Map");
		LinearLayout layout = new LinearLayout(context);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		layout.setLayoutParams(params);
		ListView list = new ListView(context);
		list.setLayoutParams(params);
		final String[][] maps = getAvailableMaps();
		if (maps != null) {
			list.setAdapter(new ArrayAdapter<String>(context,
					android.R.layout.simple_list_item_1, maps[0]));
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					dialog.dismiss();
					loadMap(maps[1][position]);
				}
			});
		}
		layout.addView(list);
		dialog.setContentView(layout);
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(MAP_MANAGER_GROUP_ID, LOAD_ITEM_ID, 0, "Load Map");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getGroupId() == MAP_MANAGER_GROUP_ID
				&& item.getItemId() == LOAD_ITEM_ID) {
			showMapSelectionDialog();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
}
