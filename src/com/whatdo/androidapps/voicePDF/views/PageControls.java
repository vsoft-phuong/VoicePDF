package com.whatdo.androidapps.voicePDF.views;

import com.whatdo.androidapps.voicePDF.PDFViewerActivity;
import com.whatdo.androidapps.voicePDF.R;
import com.whatdo.androidapps.voicePDF.framework.OverlapTester;
import com.whatdo.androidapps.voicePDF.framework.Rectangle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class PageControls extends View {
	private final float MARGIN_PADDING = 20;
	
	private Context context;
	private final Bitmap left_arrow_bmp;
	private final Bitmap right_arrow_bmp;
	private final Bitmap no_left_arrow_bmp;
	private final Bitmap no_right_arrow_bmp;
	private final Bitmap zoom_in_bmp;
	private final Bitmap zoom_out_bmp;
	private final Bitmap no_zoom_in_bmp;
	private final Bitmap no_zoom_out_bmp;
	private final Bitmap listen_on_bmp;
	private final Bitmap listen_off_bmp;
	private final Bitmap background_bmp;

	private final Rectangle listen_bounds;
	private final Rectangle zoom_in_bounds;
	private final Rectangle zoom_out_bounds;
	private final Rectangle prev_bounds;
	private final Rectangle next_bounds;
	 
	private boolean zoomOutable = true, zoomInable = true;
	private boolean prevable = false, nextable = true;
	private boolean isListening = true;
	
	private int currPage, numPages;
	private StringBuffer sb;
	
    public PageControls(Context context)
    {
        super(context);
        this.context = context;
        left_arrow_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.left_arrow);
        right_arrow_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.right_arrow);
        no_left_arrow_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_left_arrow);
        no_right_arrow_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_right_arrow);
        zoom_in_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.zoom_in);
        zoom_out_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.zoom_out);
        no_zoom_in_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_zoom_in);
        no_zoom_out_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_zoom_out);
        listen_on_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.listen_on);
        listen_off_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.listen_off);
        background_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.menu_bg);
        
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    	
        float offset = MARGIN_PADDING;
        listen_bounds = new Rectangle(MARGIN_PADDING,0,listen_on_bmp.getWidth(),listen_on_bmp.getHeight());
        offset += listen_on_bmp.getWidth();
        zoom_out_bounds = new Rectangle(offset,0,zoom_out_bmp.getWidth(),zoom_out_bmp.getHeight());
        offset += zoom_out_bmp.getWidth();
        zoom_in_bounds = new Rectangle(offset,0,zoom_in_bmp.getWidth(),zoom_in_bmp.getHeight());
        offset += zoom_in_bmp.getWidth()+MARGIN_PADDING;
        prev_bounds = new Rectangle(offset,0,left_arrow_bmp.getWidth(),left_arrow_bmp.getHeight());
        offset += left_arrow_bmp.getWidth();
        next_bounds = new Rectangle(offset,0,right_arrow_bmp.getWidth(),right_arrow_bmp.getHeight());   
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), background_bmp.getHeight());
    
    }
    
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        
        currPage = ((PDFViewerActivity)context).getPageNum();
        numPages = ((PDFViewerActivity)context).getNumPages();
        
        nextable = (currPage == numPages)?false:true;
		prevable = (currPage == 1)?false:true;
		zoomInable = (((PDFViewerActivity)context).getZoom() == PDFViewerActivity.MAX_ZOOM)?false:true;
		zoomOutable = (((PDFViewerActivity)context).getZoom() == PDFViewerActivity.MIN_ZOOM)?false:true;
		
        final Paint paint = new Paint();
        float offset = MARGIN_PADDING;
    	canvas.drawBitmap(background_bmp, new Rect(0, 0, background_bmp.getWidth(), background_bmp.getHeight()), new Rect(0, 0, getWidth(), getHeight()), paint); 
        float height_offset = getHeight() - zoom_out_bmp.getHeight();
        if(isListening)
         	canvas.drawBitmap(listen_on_bmp, offset, height_offset, paint); 
        else
        	canvas.drawBitmap(listen_off_bmp, offset, height_offset, paint); 
        offset += listen_off_bmp.getWidth();
        if (zoomOutable)
        	canvas.drawBitmap(zoom_out_bmp, offset, height_offset, paint); 
        else
        	canvas.drawBitmap(no_zoom_out_bmp, offset, height_offset, paint); 
        offset += zoom_out_bmp.getWidth();
        if (zoomInable)
        	canvas.drawBitmap(zoom_in_bmp, offset, height_offset, paint);
        else
        	canvas.drawBitmap(no_zoom_in_bmp, offset, height_offset, paint); 
        offset += zoom_in_bmp.getWidth()+MARGIN_PADDING;
        if (prevable)
        	canvas.drawBitmap(left_arrow_bmp, offset, height_offset, paint);
        else
        	canvas.drawBitmap(no_left_arrow_bmp, offset, height_offset, paint);
        offset += left_arrow_bmp.getWidth();
        if (nextable)
        	canvas.drawBitmap(right_arrow_bmp, offset, height_offset, paint); 
        else
        	canvas.drawBitmap(no_right_arrow_bmp, offset, height_offset, paint); 
        offset += right_arrow_bmp.getWidth()+MARGIN_PADDING;
        sb = new StringBuffer();
        sb.append(currPage).append("/").append(numPages);
        paint.setColor(Color.BLACK); paint.setStyle(Paint.Style.FILL); paint.setTextSize(36);
    	canvas.drawText(sb.toString(), offset, getHeight()-zoom_out_bmp.getHeight()/2+paint.getTextSize()/2, paint); 
    }
    
    public void redraw() {
    	invalidate();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        switch (ev.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	        	if (OverlapTester.pointInRectangle(listen_bounds, ev.getX(), ev.getY())) {
	        		isListening = !isListening;
	        		((PDFViewerActivity)context).toggleListen();
	        		invalidate();
	        	}
	        	else if (OverlapTester.pointInRectangle(zoom_out_bounds, ev.getX(), ev.getY())) {
	        		if (zoomOutable) {
		        		((PDFViewerActivity)context).zoomOut();
	        			invalidate();
	        		}
	        	}
	        	else if (OverlapTester.pointInRectangle(zoom_in_bounds, ev.getX(), ev.getY())) {
	        		if (zoomInable) {
		        		((PDFViewerActivity)context).zoomIn();
	        			invalidate();
	        		}
	        	}
	        	else if (OverlapTester.pointInRectangle(prev_bounds, ev.getX(), ev.getY())) {
	        		if (prevable) {
	        			((PDFViewerActivity)context).prevPage();
	        			invalidate();
	        		}
	        	}
	        	else if (OverlapTester.pointInRectangle(next_bounds, ev.getX(), ev.getY())) {
	        		if (nextable) {
	        			((PDFViewerActivity)context).nextPage();
	        			invalidate();
	        		}
	        	}
	            break;
	    }
	    return true;
    }
}
