package com.whatdo.androidapps.voicePDF.framework;

import java.io.File;
import java.util.ArrayList;

import com.whatdo.androidapps.voicePDF.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageTextAdapter extends BaseAdapter {
	private static ArrayList<File> browseList;
	private LayoutInflater inflater;
	private File temp;
	
	public ImageTextAdapter(Context context, ArrayList<File> files) {
		browseList = files;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return browseList.size();
	}

	@Override
	public Object getItem(int pos) {
		return browseList.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View view, ViewGroup parent) {
		if (view == null) {
			view = inflater.inflate(R.layout.browse_row, null);
			TextView filename = (TextView) view.findViewById(R.id.filename);
			ImageView thumbnail_image = (ImageView) view.findViewById(R.id.thumbnail_image);
		   
			temp = browseList.get(pos);

			filename.setText(temp.getName());
			if (temp.isDirectory()) {
				thumbnail_image.setImageResource(R.drawable.folder);
			}
			else {
				  thumbnail_image.setImageResource(R.drawable.pdf);
			}
			view.setTag(temp.getPath());
		}
		else { //reuse view
			TextView filename = (TextView) view.findViewById(R.id.filename);
			ImageView thumbnail_image = (ImageView) view.findViewById(R.id.thumbnail_image);
		   
			temp = browseList.get(pos);

			filename.setText(temp.getName());
			if (temp.isDirectory()) {
				thumbnail_image.setImageResource(R.drawable.folder);
			}
			else {
				  thumbnail_image.setImageResource(R.drawable.pdf);
			}
			view.setTag(temp.getPath());
		}
		return view;
	}
}
