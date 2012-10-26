package inavi.prototyp.manager.location.impl;

import inavi.main.IndoorNavigation;
import inavi.main.TargetSelectionActivity;
import inavi.manager.location.LocationManager;
import inavi.manager.map.MapManager;
import inavi.map.IBuildingComponent;
import inavi.map.IMap;
import inavi.map.ITarget;
import inavi.map.Point;
import inavi.prototyp.gui.InputTargetActivity;
import inavi.prototyp.gui.PositionSettingActivity;
import inavi.prototyp.main.R;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class DefaultLocationManager extends LocationManager {

	private static final int POSITION_SETTING = 1000;
	private static final int TARGET_SELECTION = 1001;
	private IBuildingComponent component;

	public DefaultLocationManager(IndoorNavigation context,
			MapManager mapManager) {
		super(context, mapManager);
	}

	@Override
	public synchronized Point getCurrentPosition() {
		if (component != null)
			return component.getPosition();
		return null;
	}

	public synchronized void setCurrentPosition(IBuildingComponent component) {
		this.component = component;
		context.positionChanged(component);
	}

	@Override
	public IBuildingComponent getCurrentBuildingComponent() {
		return component;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu
				.add(LOCATION_MANAGER_GROUP_ID, R.id.setPositionItem, 0,
						"Position");
		return false;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.setPositionItem) {
			new AlertDialog.Builder(context)
					.setTitle("Select the input Method").setItems(
							new CharSequence[] { "List of Targets", "Map" },
							new Dialog.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int item) {
									if (item == 0) {
										Intent intent = new Intent(context,
												InputTargetActivity.class);
										intent
												.putExtra(
														InputTargetActivity.ONLY_WITH_ONE_COMPONENT,
														true);
										context.startActivityForResult(intent,
												TARGET_SELECTION);
									} else {
										Intent intent = new Intent(context,
												PositionSettingActivity.class);
										context.startActivityForResult(intent,
												POSITION_SETTING);
									}
								}
							}).create().show();
			return true;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.setPositionItem).setEnabled(
				mapManager.getMap() != null);
		return false;
	}

	@Override
	public String getDescription() {
		return "LocationManaget get the Position from User and is to be update by user or RouteManager";
	}

	@Override
	public String getName() {
		return "Default Location Manager";
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == POSITION_SETTING && resultCode == Activity.RESULT_OK) {
			if (data.getExtras() != null
					&& data.getExtras().containsKey(
							TargetSelectionActivity.COMPONENT_ID)) {
				int id = data.getExtras().getInt(
						TargetSelectionActivity.COMPONENT_ID);
				List<IBuildingComponent> list = mapManager
						.getBuildingComponents();
				IBuildingComponent component = null;
				for (IBuildingComponent bc : list) {
					if (bc.getId() == id) {
						component = bc;
						break;
					}
				}
				if (component != null) {
					setCurrentPosition(component);
				}
			}
		} else if (requestCode == TARGET_SELECTION
				&& resultCode == Activity.RESULT_OK) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				String cName = bundle
						.getString(TargetSelectionActivity.CATEGORY_NAME);
				String tName = bundle
						.getString(TargetSelectionActivity.TARGET_NAME);
				ITarget target = mapManager.getTarget(cName, tName);
				if (target != null) {
					setCurrentPosition(target.getBuildingComponents().get(0));
				}
			}
		}
	}
	
	@Override
	public void mapChanged(IMap map) {
		setCurrentPosition(null);
	}
}
