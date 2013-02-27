package com.whatdo.androidapps.voicePDF;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.whatdo.androidapps.voicePDF.R;

public class MainActivity extends SherlockFragmentActivity {
	
	private static final String[] CONTENT = new String[] { "Recent", "Browse"};

	FragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    private FragmentManager mFragmentManager;
    private SherlockFragment mBrowseFragment;
    int browseCount = 0;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.simple_tabs);
        
        mFragmentManager = getSupportFragmentManager();
        mAdapter = new FragmentAdapter(mFragmentManager);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TabPageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
    }
	
	class FragmentAdapter extends FragmentPagerAdapter {	
		private final class BrowseListener implements BrowseFragmentListener {
		    public void onFragmentRefresh(File path) {
		    	browseCount++; //one level down
		        mFragmentManager.beginTransaction().remove(mBrowseFragment).commit();
		        mBrowseFragment = BrowseFragment.newInstance(listener, path);
		        dir = path;
		        notifyDataSetChanged();
		    }
		}
		BrowseListener listener = new BrowseListener();

	    private int mCount = CONTENT.length;
	    private File dir;
	    
	    public FragmentAdapter(FragmentManager fm) {
	        super(fm);
	    }

	    @Override
	    public Fragment getItem(int position) {
	    	switch (position) {
	    		case 0:
	    			return RecentFilesFragment.newInstance();
	    		case 1:
	    		default:
	    			if (mBrowseFragment == null) 
	    				mBrowseFragment = BrowseFragment.newInstance(listener, Environment.getExternalStorageDirectory());
	    			return mBrowseFragment;
	    	}
	    }

	    @Override
	    public int getCount() {
	        return mCount;
	    }
	    
	    @Override
	    public int getItemPosition(Object object) {
	        if (object instanceof BrowseFragment)
	            return POSITION_NONE;
	        return POSITION_UNCHANGED;
	    }
	    
	    @Override
	    public CharSequence getPageTitle(int position) {
	    	return CONTENT[position];
	    }
	    
	    private void browseFragmentBack() {
	    	browseCount--; //up one level
	    	File temp = new File(dir.getParent());
    		mFragmentManager.beginTransaction().remove(mBrowseFragment).commit();
	    	mBrowseFragment = BrowseFragment.newInstance(listener, temp);
	    	dir = temp;
	        notifyDataSetChanged();
	    }
	}

	public interface BrowseFragmentListener {
	    void onFragmentRefresh(File path);
	}
	
	@Override
	public void onBackPressed() {
		if (mPager.getCurrentItem() == 1 && browseCount>0) {
			mAdapter.browseFragmentBack();
	    } else {
	        super.onBackPressed();
	    }
	}
	
	void showPDF(File file) {
		if (!file.isFile()) {
			Toast.makeText(this, "ERROR: File unavailalbe", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(this, PDFViewerActivity.class);
	    intent.putExtra(PDFViewerActivity.EXTRA_PDFFILENAME, file.getPath());
	    startActivity(intent);
	    finish();
	}
}
