package com.whatdo.androidapps.voicePDF.views;

import com.whatdo.androidapps.voicePDF.PDFViewerActivity;
import com.whatdo.androidapps.voicePDF.framework.MultiTouchZoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.ImageView;
import android.widget.Scroller;

public class PDFImageView extends ImageView
{ 
	private Bitmap mBi;
	private Context mContext;
    private final Scroller scroller;
    private float lastX;
    private float lastY;
    private VelocityTracker velocityTracker;
    private RectF viewBounds;
    private long lastDownEventTime;
    private static final int DOUBLE_TAP_TIME = 500;
    private MultiTouchZoom multiTouchZoom;
    private int screenWidth, screenHeight;
	
    public PDFImageView(Context context) {
        super(context);
        mContext = context;
        scroller = new Scroller(getContext());
        setFocusable(true);
        setFocusableInTouchMode(true);
		multiTouchZoom = new MultiTouchZoom(context);
		viewBounds = new RectF(0,0,0,0);
    }

    public void updateImage(Bitmap bi) {
		mBi = bi;
	    setImageBitmap(mBi);
		invalidate();
	}
    
	@Override
	protected boolean setFrame (int left, int top, int right, int bottom) {
		if (mBi == null)
			return super.setFrame(0,0,getWidth(),getHeight());
		left = 0;
		top = 0;
		right = mBi.getWidth();
		bottom = mBi.getHeight();
		return super.setFrame(left, top, right, bottom);
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        if (multiTouchZoom != null) {
            if (multiTouchZoom.onTouchEvent(ev)) {
                return true;
            }

            if (multiTouchZoom.isResetLastPointAfterZoom()) {
                setLastPosition(ev);
                multiTouchZoom.setResetLastPointAfterZoom(false);
            }
        }

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopScroller();
                setLastPosition(ev);
                if (ev.getEventTime() - lastDownEventTime < DOUBLE_TAP_TIME) {
                	((PDFViewerActivity)mContext).toggleControls();
                } else {
                    lastDownEventTime = ev.getEventTime();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                scrollBy((int) (lastX - ev.getX()), (int) (lastY - ev.getY()));
                setLastPosition(ev);
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                scroller.fling(getScrollX(), getScrollY(), (int) -velocityTracker.getXVelocity(), (int) -velocityTracker.getYVelocity(), getLeftLimit(), getRightLimit(), getTopLimit(), getBottomLimit());
                velocityTracker.recycle();
                velocityTracker = null;
                break;
        }
        return true;
    }
	
	private void setLastPosition(MotionEvent ev) {
        lastX = ev.getX();
        lastY = ev.getY();
    }
	
	private int getTopLimit() {
        return 0;
    }

    private int getLeftLimit() {
        return 0;
    }

    private int getBottomLimit() {
        return ((PDFViewerActivity)mContext).getPageHeight();
    }

    private int getRightLimit() {
    	return ((PDFViewerActivity)mContext).getPageWidth();
    }
	
	@Override
    public void scrollTo(int x, int y) {
		screenWidth = ((PDFViewerActivity)mContext).getScreenWidth();
		screenHeight = ((PDFViewerActivity)mContext).getScreenHeight();
		viewBounds.set(x, y, screenWidth+x, screenHeight+y);
		if (viewBounds.right >= getRightLimit()) {
			if (getRightLimit() <= screenWidth)
				x = getLeftLimit();
			else
				x = getRightLimit()-screenWidth;
		}
		else if (viewBounds.left <= getLeftLimit()) {
			x = getLeftLimit();
		}
		
		if (viewBounds.bottom >= getBottomLimit()) {
			if (getBottomLimit() <= screenHeight)
				y = getTopLimit();
			else
				y = getBottomLimit()-screenHeight;
		}
		else if (viewBounds.top <= getTopLimit()) {
			y = getTopLimit();
		}
		super.scrollTo(x, y);
    }
	
	@Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
        }
    }
	
	@Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float scrollScaleRatio = getScrollScaleRatio();
        invalidateScroll(scrollScaleRatio);
    }
	
	private void invalidateScroll(float ratio) {
        stopScroller();
        scrollTo((int) (getScrollX() * ratio), (int) (getScrollY() * ratio));
    }
	
	private float getScrollScaleRatio() {
        final float v = ((PDFViewerActivity)mContext).getPageWidth();
		if (v > 0.001)
			return v / getWidth();
		else 
			return 0;
    }
	
	private void stopScroller() {
        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }
    }
}
