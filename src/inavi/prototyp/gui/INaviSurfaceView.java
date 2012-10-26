package inavi.prototyp.gui;

import inavi.manager.display.DisplayManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class INaviSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {

	private INaviSurfaceThread thread;
	private DisplayManager displayManager;

	public INaviSurfaceView(Context context) {
		super(context);
		init();
	}

	public INaviSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public INaviSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		getHolder().addCallback(this);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		thread = new INaviSurfaceThread(getHolder());
		thread.setRunning(true);
		thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	private class INaviSurfaceThread extends Thread {
		private SurfaceHolder surfaceHolder;
		private boolean running;

		public INaviSurfaceThread(SurfaceHolder surfaceHolder) {
			this.surfaceHolder = surfaceHolder;
		}

		public void setRunning(boolean run) {
			running = run;
		}

		public void onDraw(Canvas canvas) {
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			canvas.drawRect(canvas.getClipBounds(), paint);
			if (displayManager != null) {
				displayManager.draw(canvas);
			} else {
				canvas.drawRect(new Rect(0, 0, canvas.getWidth() - 1, canvas
						.getHeight() - 1), new Paint());
				canvas.drawText("No Image", canvas.getWidth() / 2, canvas
						.getHeight() / 2, new Paint());
			}
		}

		@Override
		public void run() {
			Canvas c;
			while (running) {
				c = null;
				try {
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						onDraw(c);
					}
				} finally {
					if (c != null) {
						surfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}
	}

	public void setDisplayManager(DisplayManager displayManager) {
		this.displayManager = displayManager;
	}
}
