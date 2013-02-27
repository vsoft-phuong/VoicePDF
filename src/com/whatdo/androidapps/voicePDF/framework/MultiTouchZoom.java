package com.whatdo.androidapps.voicePDF.framework;

import com.whatdo.androidapps.voicePDF.PDFViewerActivity;

import android.content.Context;
import android.view.MotionEvent;

public class MultiTouchZoom {
	private Context mContext;
    private boolean resetLastPointAfterZoom;
    private float lastZoomDistance;

    public MultiTouchZoom(Context context) {
    	mContext = context;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if ((ev.getAction() & MotionEvent.ACTION_POINTER_DOWN) == MotionEvent.ACTION_POINTER_DOWN) {
            lastZoomDistance = getZoomDistance(ev);
            return true;
        }
        if ((ev.getAction() & MotionEvent.ACTION_POINTER_UP) == MotionEvent.ACTION_POINTER_UP) {
            lastZoomDistance = 0;
            resetLastPointAfterZoom = true;
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE && lastZoomDistance != 0) {
            float zoomDistance = getZoomDistance(ev);
            ((PDFViewerActivity)mContext).setZoom(((PDFViewerActivity)mContext).getZoom() * zoomDistance / lastZoomDistance);
            lastZoomDistance = zoomDistance;
            return true;
        }
        return false;
    }

    private float getZoomDistance(MotionEvent ev) {
        return (float) Math.sqrt(Math.pow(ev.getX(0) - ev.getX(1), 2) + Math.pow(ev.getY(0) - ev.getY(1), 2));
    }

    public boolean isResetLastPointAfterZoom() {
        return resetLastPointAfterZoom;
    }

    public void setResetLastPointAfterZoom(boolean resetLastPointAfterZoom) {
        this.resetLastPointAfterZoom = resetLastPointAfterZoom;
    }
}
