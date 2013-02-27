package com.whatdo.androidapps.voicePDF;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.whatdo.androidapps.voicePDF.R;

public class RecentFilesFragment extends SherlockFragment {
	
	public static RecentFilesFragment newInstance() {
		RecentFilesFragment fragment = new RecentFilesFragment();
        return fragment;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//set up listview and adapter
		View view = inflater.inflate(R.layout.recent_files, null);
		ListView recentFilesListView = (ListView)(view.findViewById(R.id.recent_files_list));
		ArrayAdapter<String> mRecentFilesArrayAdapter = new ArrayAdapter<String>(getActivity(), 
																R.layout.recent_files_row);
		recentFilesListView.setAdapter(mRecentFilesArrayAdapter);
		recentFilesListView.setOnItemClickListener(mDeviceClickListener);
		
		//populate listview
		if (Settings.recentFiles[0] == null) {
			view.findViewById(R.id.empty_recent_files).setVisibility(View.VISIBLE);
		}
		else {
			for (int i=0; i<Settings.recentFiles.length; i++) {
				if (Settings.recentFiles[i] != null)
					mRecentFilesArrayAdapter.add(Settings.recentFiles[i].getName());
			}
		}
        
		return view;
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {
        	Settings.addRecentFiles(getActivity(), Settings.recentFiles[pos].getPath());
        	((MainActivity)getActivity()).showPDF(Settings.recentFiles[pos]);
        }
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Settings.loadRecentFiles(getActivity());
	}
	
}
