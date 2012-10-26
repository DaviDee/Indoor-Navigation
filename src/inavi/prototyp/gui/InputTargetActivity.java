package inavi.prototyp.gui;

import inavi.main.TargetSelectionActivity;
import inavi.manager.map.MapManager;
import inavi.map.ITarget;
import inavi.map.ITargetCategory;
import inavi.prototyp.main.R;
import inavi.prototyp.manager.map.impl.DefaultMapManager;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class InputTargetActivity extends Activity {

	public final static String ONLY_WITH_ONE_COMPONENT = "onlyWithOneComponent";

	private MapManager mapManager;
	private String categoryName;

	private boolean onlySingle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Context context = this;

		mapManager = DefaultMapManager.getInstance();

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			onlySingle = bundle.getBoolean(ONLY_WITH_ONE_COMPONENT);
		}

		setContentView(R.layout.target_selection);
		Spinner categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
		final AutoCompleteTextView targetTextView = (AutoCompleteTextView) findViewById(R.id.targetTextView);
		Button okButton = (Button) findViewById(R.id.targetSelectionOKButton);

		okButton.setOnClickListener(new OnClickListener() {

			private ITarget target;

			@Override
			public void onClick(View v) {
				if (categoryName == null) {
					Toast.makeText(context, "Invalid Parameter",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (targetTextView.getText() != null) {
					String text = targetTextView.getText().toString();
					target = mapManager.getTarget(categoryName, text);
					if (target == null) {
						Toast.makeText(context, "Invalid Parameter",
								Toast.LENGTH_SHORT).show();
						return;
					}
				} else {
					Toast.makeText(context, "Invalid Parameter",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra(TargetSelectionActivity.CATEGORY_NAME,
						categoryName);
				intent.putExtra(TargetSelectionActivity.TARGET_NAME, target
						.getName());
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		final List<ITargetCategory> categories = mapManager
				.getTargetCategories();
		if (categories == null)
			return;
		List<String> names = new LinkedList<String>();
		for (ITargetCategory category : categories)
			names.add(category.getName());

		categorySpinner.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, names));
		categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View view,
					int position, long id) {
				ITargetCategory category = categories.get(position);
				categoryName = category.getName();
				List<ITarget> targets = category.getTargets();
				if (targets != null) {
					List<String> names = new LinkedList<String>();
					for (ITarget target : targets) {
						if (!onlySingle
								|| target.getBuildingComponents().size() == 1)
							names.add(target.getName());
					}

					targetTextView
							.setAdapter(new ArrayAdapter<String>(
									context,
									android.R.layout.simple_dropdown_item_1line,
									names));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}
}