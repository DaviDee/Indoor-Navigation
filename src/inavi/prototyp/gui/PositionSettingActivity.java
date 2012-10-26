package inavi.prototyp.gui;

import inavi.main.TargetSelectionActivity;
import inavi.manager.map.MapManager;
import inavi.map.IBuildingComponent;
import inavi.map.IFloor;
import inavi.map.Point;
import inavi.prototyp.main.R;
import inavi.prototyp.manager.map.impl.DefaultMapManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

public class PositionSettingActivity extends Activity {

	private MapManager mapManager;
	private IFloor floor;
	private Bitmap bitmap;
	private IBuildingComponent component;
	private float scaleX, scaleY;
	private ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(params);
		imageView = new ImageView(this) {
			@Override
			public void draw(Canvas canvas) {
				// TODO Auto-generated method stub
				super.draw(canvas);

				if (floor == null)
					return;
				drawFloorNumber(canvas, floor.getFloorNumber());

				if (bitmap == null)
					return;
				scaleX = ((float) canvas.getClipBounds().width())
						/ ((float) bitmap.getWidth());
				scaleY = ((float) canvas.getClipBounds().height())
						/ ((float) bitmap.getHeight());

				if (component == null)
					return;

				Point p = component.getPosition();
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				paint.setColor(this.getResources().getColor(R.color.position));
				paint.setStyle(Style.FILL_AND_STROKE);

				canvas.drawCircle(p.getX() * scaleX, p.getY() * scaleY, 5,
						paint);

			}
		};
		imageView.setScaleType(ScaleType.FIT_XY);
		imageView.setLayoutParams(params);
		layout.addView(imageView);
		setContentView(layout);

		imageView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					component = floor.getBuildingComponentAt((float) event
							.getX()
							/ scaleX, (float) event.getY() / scaleY);
					imageView.invalidate();
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					float x1 = event.getHistoricalX(0);
					float x2 = event.getX();
					float diff = x2 - x1;
					if (diff > 75 && floor != null) {
						IFloor f = mapManager
								.getFloor(floor.getFloorNumber() - 1);
						if (f != null) {
							selectFloor(f);
						}
					} else if (diff < -75 && floor != null) {
						IFloor f = mapManager
								.getFloor(floor.getFloorNumber() + 1);
						if (f != null) {
							selectFloor(f);
						}
					}
				}
				return true;
			}
		});

		mapManager = DefaultMapManager.getInstance();
		floor = mapManager.getFirstFloor();

		if (floor != null) {
			selectFloor(floor);
		}

	}

	private void selectFloor(IFloor f) {
		floor = f;
		bitmap = floor.getBitmap();
		component = null;
		imageView.setImageBitmap(bitmap);
		imageView.invalidate();
	}

	private void drawFloorNumber(Canvas canvas, int number) {
		Paint paint = new Paint();
		paint.setColor(this.getResources().getColor(R.color.display_info));
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setAlpha(190);
		paint.setTextAlign(Align.LEFT);
		paint.setTextSize(50);
		paint.setTextScaleX(1.8f);
		canvas.drawRect(new Rect(5, 5, 55, 55), paint);
		paint.setStyle(Style.FILL_AND_STROKE);
		canvas.drawText(String.valueOf(number), 8, 47, paint);
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.position_target_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.OKMenuItem).setEnabled(component != null);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.OKMenuItem) {
			Intent intent = new Intent();
			intent.putExtra(TargetSelectionActivity.COMPONENT_ID, component
					.getId());
			setResult(RESULT_OK, intent);
			finish();
			return true;
		} else if (item.getItemId() == R.id.CancelMenuItem) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
