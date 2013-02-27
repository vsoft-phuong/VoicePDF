package com.whatdo.androidapps.voicePDF.views;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

public class PageViewControls extends LinearLayout
{
	PageControls pc;
	
    public PageViewControls(Context context)
    {
        super(context);
        show();
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.BOTTOM);
        pc = new PageControls(context);
        addView(pc);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public void redraw() {
    	pc.redraw();
    }
    
    public void toggleZoomControls() {
        if (getVisibility() == View.VISIBLE) 
        	hide();
        else 
        	show();
    }
    
    private void show() {
    	fade(View.VISIBLE, getWidth(), 0.0f);
    }
    
    private void hide() {
    	fade(View.GONE, 0.0f, getWidth());
    }

    private void fade(int visibility, float startDelta, float endDelta)
    {
        Animation anim = new TranslateAnimation(0,0, startDelta, endDelta);
        anim.setDuration(500);
        startAnimation(anim);
        setVisibility(visibility);
    }
}
