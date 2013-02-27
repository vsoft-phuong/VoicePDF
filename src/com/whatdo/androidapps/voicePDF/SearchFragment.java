package com.whatdo.androidapps.voicePDF;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.whatdo.androidapps.voicePDF.R;

public class SearchFragment extends SherlockFragment {
	
	public static SearchFragment newInstance() {
		SearchFragment fragment = new SearchFragment();
        return fragment;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.search, null);
        String text = "Search not available yet";
		
        ((TextView)view.findViewById(R.id.text)).setText(text);
        
		return view;
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//TODO Search Fragment
	}
	
}
