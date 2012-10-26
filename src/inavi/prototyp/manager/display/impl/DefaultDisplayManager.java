package inavi.prototyp.manager.display.impl;

import inavi.main.IndoorNavigation;
import inavi.manager.display.DisplayManager;
import inavi.manager.location.LocationManager;
import inavi.manager.map.MapManager;
import inavi.manager.route.RouteManager;
import inavi.map.IBuildingComponent;
import inavi.map.IFloor;
import inavi.map.IMap;
import inavi.map.Point;
import inavi.prototyp.main.R;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

public class DefaultDisplayManager extends DisplayManager {

	private static final float MAX_ZOOM = 3;
	private static final float MIN_ZOOM = 1;
	private static final float ZOOM_STEP = 0.5f;

	private float zoom;
	private DirectionSensor directionSensor;
	private List<IBuildingComponent> route;

	public DefaultDisplayManager(IndoorNavigation context,
			RouteManager routeManager, LocationManager locationManager,
			MapManager mapManager) {
		super(context, routeManager, locationManager, mapManager);
		zoom = MIN_ZOOM;

		directionSensor = new DirectionSensor(context);
		directionSensor.start();
	}

	@Override
	public void draw(Canvas canvas) {

		if (mapManager == null)
			return;
		IFloor floor = null;
		if (locationManager != null) {
			Point position = locationManager.getCurrentPosition();
			if (position != null) {
				floor = mapManager.getFloor(position.getFloor());
			} else {
				floor = mapManager.getFirstFloor();
			}
		} else {
			floor = mapManager.getFirstFloor();
		}
		if (floor != null) {
			Bitmap bitmap = floor.getBitmap();
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);

			int offsetX = 0, offsetY = 0;
			int cW = canvas.getClipBounds().width();
			int cH = canvas.getClipBounds().height();
			float scaleX = (float) cW / (float) bitmap.getWidth() * zoom;
			float scaleY = (float) cH / (float) bitmap.getHeight() * zoom;
			int w = (int) (cW / scaleX);
			int h = (int) (cH / scaleY);

			if (locationManager != null) {
				Point p = locationManager.getCurrentPosition();
				if (p != null) {
					offsetX = (int) (p.getX() - w / 2);
					offsetY = (int) (p.getY() - h / 2);
				}

				if (offsetX < 0) {
					offsetX = 0;
				} else if (offsetX + w > bitmap.getWidth()) {
					offsetX = bitmap.getWidth() - w;
				}
				if (offsetY < 0) {
					offsetY = 0;
				} else if (offsetY + h > bitmap.getHeight()) {
					offsetY = bitmap.getHeight() - h;
				}

				Bitmap newBitmap = bitmap.copy(Config.RGB_565, true);
				Canvas newCanvas = new Canvas(newBitmap);
				drawPath(newCanvas);
				drawPosition(newCanvas);

				canvas.drawBitmap(newBitmap, new Rect(offsetX, offsetY, offsetX
						+ w, offsetY + h), canvas.getClipBounds(), paint);
			}
			drawFloorNumber(canvas, floor.getFloorNumber());
			drawDirectionArrow(canvas);
		}
	}

	private void drawDirectionArrow(Canvas canvas) {

		Paint paint = new Paint();
		paint.setColor(context.getResources().getColor(R.color.display_info));
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setAlpha(190);
		int width = canvas.getClipBounds().width();
		canvas.drawRect(new Rect(width - 55, 5, width - 5, 55), paint);

		IBuildingComponent from = locationManager.getCurrentBuildingComponent();
		if (from == null || route == null || route.size() < 2)
			return;

		float pivot = getDirection(from.getPosition(), route.get(1)
				.getPosition());
		if (pivot == -1)
			return;
		pivot = (pivot + mapManager.getMap().getNorth()) % 360;
		directionSensor.setPivot(pivot);
		// float orientation = pivot;
		float orientation = directionSensor.getOrientation();
		if (orientation != DirectionSensor.INVALID_VALUE) {
			if (orientation < -20 || orientation > 20)
				paint.setColor(context.getResources().getColor(
						R.color.arrow_wrong));
			paint.setStrokeWidth(3);
			float offsetX = width - 55;
			float offsetY = 5;
			Path path = new Path();
			path.moveTo(offsetX + 10, offsetY + 20);
			path.rLineTo(15, -15);
			path.rLineTo(15, 15);
			path.rLineTo(-10, 0);
			path.rLineTo(0, 20);
			path.rLineTo(-10, 0);
			path.rLineTo(0, -20);
			path.close();
			canvas.rotate(orientation, width - 30, 30);
			paint.setStyle(Style.FILL);
			paint.setAntiAlias(true);
			canvas.drawPath(path, paint);
			canvas.rotate(-orientation, width - 30, 30);
		}
	}

	private void drawFloorNumber(Canvas canvas, int number) {
		Paint paint = new Paint();
		paint.setColor(context.getResources().getColor(R.color.display_info));
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(2);
		paint.setAlpha(190);
		paint.setTextAlign(Align.LEFT);
		paint.setTextSize(50);
		paint.setTextScaleX(1.8f);
		canvas.drawRect(new Rect(5, 5, 55, 55), paint);
		paint.setStyle(Style.FILL_AND_STROKE);
		canvas.drawText(String.valueOf(number), 8, 47, paint);
	}

	private void drawPosition(Canvas canvas) {
		Point position = locationManager.getCurrentPosition();
		if (position != null) {
			Paint paint = new Paint();
			paint.setColor(context.getResources().getColor(R.color.position));
			paint.setStyle(Style.FILL);
			paint.setAntiAlias(true);
			canvas.drawCircle(position.getX(), position.getY(), 5, paint);
		}
	}

	private void drawPath(Canvas canvas) {
		route = routeManager.getCurrentRoute();
		if (route != null && !route.isEmpty()) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(context.getResources().getColor(R.color.path));
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(2);
			Path path = new Path();
			Point point = route.get(0).getPosition();
			path.moveTo(point.getX(), point.getY());
			paint.setStyle(Style.FILL_AND_STROKE);
			canvas.drawCircle(point.getX(), point.getY(), 5, paint);
			for (int i = 1; i < route.size(); i++) {
				point = route.get(i).getPosition();
				if (point.getFloor() == locationManager.getCurrentPosition()
						.getFloor()) {
					paint.setStyle(Style.FILL_AND_STROKE);
					path.lineTo(point.getX(), point.getY());
					paint.setStyle(Style.FILL_AND_STROKE);
					if (i == route.size() - 1) {
						paint.setColor(context.getResources().getColor(
								R.color.target));
					}
					canvas.drawCircle(point.getX(), point.getY(), 5, paint);
				} else {
					break;
				}
			}
			paint.setColor(context.getResources().getColor(R.color.path));
			paint.setStyle(Style.STROKE);
			canvas.drawPath(path, paint);
		}
	}

	@Override
	public String getDescription() {
		return "DisplayManager shows the map, the position and current route";
	}

	@Override
	public String getName() {
		return "Default Display Mananger";
	}

	private float getDirection(Point from, Point to) {
		if (from == null || to == null)
			return -1;
		if (from.getFloor() != to.getFloor())
			return -1;
		float w = to.getX() - from.getX();
		float h = to.getY() - from.getY();
		if (w == 0 && h == 0) {
			return -1;
		}
		float angle = Math.round(Math.toDegrees(Math.atan2(h, w)));
		angle = angle + 90;
		if (angle < 0)
			angle += 360;
		return angle;
	}

	@Override
	public void onDestroy() {
		directionSensor.stop();
	}

	public void zoomIn() {
		if (zoom < MAX_ZOOM) {
			zoom += ZOOM_STEP;
		}
	}

	public void zoomOut() {
		if (zoom > MIN_ZOOM) {
			zoom -= ZOOM_STEP;
		}
	}

	@Override
	public void mapChanged(IMap map) {
		super.mapChanged(map);
		zoom = MIN_ZOOM;
	}
}
