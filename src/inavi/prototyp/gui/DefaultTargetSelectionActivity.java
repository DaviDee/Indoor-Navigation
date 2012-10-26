package inavi.prototyp.gui;

import inavi.main.TargetSelectionActivity;
import inavi.map.IBuildingComponent;
import inavi.map.ITarget;
import inavi.map.ITargetCategory;
import inavi.prototyp.map.impl.DefaultComponent;
import inavi.prototyp.map.impl.DefaultTarget;
import inavi.prototyp.map.impl.DefaultTargetCategory;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class DefaultTargetSelectionActivity extends TargetSelectionActivity {

	private static final int POSITION_SETTING = 0;
	private static final int TARGET_SELECTION = 1;
	private ITarget target;
	private ITargetCategory category;
	private IBuildingComponent component;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		layout.setGravity(Gravity.CENTER_VERTICAL);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		Button button = new Button(this);
		button.setText("List of Targets");
		button.setLayoutParams(params);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showTargetSelection();
			}
		});
		layout.addView(button);
		
		button = new Button(this);
		button.setText("Map");
		button.setLayoutParams(params);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPositionSettig();
			}
		});
		layout.addView(button);
		
		setContentView(layout);
	}

	private void showPositionSettig() {
		Intent intent = new Intent(context, PositionSettingActivity.class);
		this.startActivityForResult(intent, POSITION_SETTING);
	}

	private void showTargetSelection() {
		Intent intent = new Intent(context, InputTargetActivity.class);
		this.startActivityForResult(intent, TARGET_SELECTION);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TARGET_SELECTION && resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				String cName = bundle.getString(CATEGORY_NAME);
				String tName = bundle.getString(TARGET_NAME);
				category = new DefaultTargetCategory(cName, null);
				target = new DefaultTarget(tName, null);
			}
		} else if (requestCode == POSITION_SETTING && resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				component = new DefaultComponent(bundle.getInt(COMPONENT_ID),
						0, 0, 0);
			}
		}
		finish();
	}

	@Override
	public void onBackPressed() {
		target = null;
		category = null;
		super.onBackPressed();
	}

	@Override
	protected ITarget getTarget() {
		return target;
	}

	@Override
	protected ITargetCategory getTargetCategory() {
		return category;
	}

	@Override
	protected IBuildingComponent getBuildingComponent() {
		return component;
	}

}
