package com.whatdo.androidapps.voicePDF;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.whatdo.androidapps.voicePDF.R;
import com.whatdo.androidapps.voicePDF.MainActivity.BrowseFragmentListener;
import com.whatdo.androidapps.voicePDF.framework.ImageTextAdapter;

public class BrowseFragment extends SherlockFragment {
	static BrowseFragmentListener listener;
	static File dir;
	ArrayList<File> filesList;
	
	
	public static BrowseFragment newInstance(BrowseFragmentListener listener, File path) {
		BrowseFragment fragment = new BrowseFragment();
		dir = path;
		BrowseFragment.listener = listener;
        return fragment;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//set up and populate listview
		View view = inflater.inflate(R.layout.browse, null);
		ListView browseListView = (ListView)(view.findViewById(R.id.browse_list));
		browseListView.setAdapter(new ImageTextAdapter(getActivity(), filesList));
		browseListView.setOnItemClickListener(mDeviceClickListener);
		
		if (filesList.isEmpty()) {
			view.findViewById(R.id.empty_browse).setVisibility(View.VISIBLE);
		}
		
		return view;
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            String path = (String) v.getTag();
            File temp = new File(path);
            if (temp.isDirectory()) {
            	listener.onFragmentRefresh(temp);
            }
            else {
            	Settings.addRecentFiles(getActivity(), path);
            	((MainActivity)getActivity()).showPDF(temp);
            }
        }
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		filesList = new ArrayList<File>();
		File[] files = dir.listFiles();
		File temp; String name;
		for (int i=0; i<files.length; i++) {
			temp = files[i];
			if (!temp.isHidden()) {
				if (temp.isDirectory()) {
					filesList.add(files[i]);
				}
				else if (temp.isFile()) {
					name = temp.getName();
					if (name.substring(name.length()-4).equals(".pdf"))
						filesList.add(files[i]);
				}
			}
		}

		Collections.sort(filesList, new Comparator<File>() {
			public int compare(File one, File two) {
				return one.getName().compareTo(two.getName());
			}
		});
	}
}
