package com.ryanremer.empty_live_wallpaper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;

public class MyWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new MyWallpaperEngine();
    }

    private class MyWallpaperEngine extends Engine {
        private final Handler handler = new Handler();
        private final Runnable drawRunner = this::handleDraw;

        // Some values for the default Live Wallpaper
        private final Paint paint = new Paint();
        private final ArrayList<Point> points = new ArrayList<>();

        public MyWallpaperEngine() {
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(10f);

            points.add(new Point(20,20));
        }

        /**
         * This is the main draw function for the live wallpaper use functions of the [Canvas]
         * to change what is rendered on the live wallpaper.
         *
         * @param canvas The [Canvas] for the wallpaper
         */
        private void draw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);
            for (Point point : points) {
                canvas.drawCircle(point.x, point.y, 200f, paint);
            }
        }


        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);

//            An example of how you can use touch events to change the wallpaper
            points.add(new Point((int)event.getX(), (int)event.getY()));
            if (points.size() > 50) {
                points.remove(0);
            }

            runDraw();
        }

        /*
         *  Below this are helper functions to help run [draw] safely
         */

        /**
         * Queues up the draw function for the [Handler], use this in functions outside of the
         * [draw] function to update the wallpaper
         */
        private void runDraw() {
            handler.removeCallbacks(drawRunner);
            handler.post(drawRunner);
        }

        /**
         * Removes the draw function from the [Handler], use this in functions outside of the
         * [draw] function to cancel drawing operations (like when the wallpaper isn't visible)
         */
        private void cancelDraw() {
            handler.removeCallbacks(drawRunner);
            handler.post(drawRunner);
        }

        /**
         * This function calls the [draw] function safely
         */
        private void handleDraw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    draw(canvas);
                }
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }

            handler.removeCallbacks(drawRunner);
        }

        /*
         *  Below this are overrides made to the Wallpaper service to enable the
         *  [draw] helper functions.
         */

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            runDraw();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                runDraw();
            } else {
                cancelDraw();
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            cancelDraw();
        }
    }
}
