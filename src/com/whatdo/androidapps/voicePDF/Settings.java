package com.whatdo.androidapps.voicePDF;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
	public static final String PREFS_NAME = "SETTINGS"; //settings stored in SharedPreferences

	public static final File[] recentFiles = new File[8];
	static String[] paths = new String[8];
	
	//defaults
	public static final String DEFAULT_FILE_PATH_HOLDER = "---com.whatdo---";
	
	public static String recent1; //recently opened files
	public static String recent2;
	public static String recent3;
	public static String recent4;
	public static String recent5;
	public static String recent6;
	public static String recent7;
	public static String recent8;
	
	public static void loadRecentFiles(Context context) {
		//Get SharedPreferences
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		paths[0] = settings.getString("recent1", DEFAULT_FILE_PATH_HOLDER);
		paths[1] = settings.getString("recent2", DEFAULT_FILE_PATH_HOLDER);
		paths[2] = settings.getString("recent3", DEFAULT_FILE_PATH_HOLDER);
		paths[3] = settings.getString("recent4", DEFAULT_FILE_PATH_HOLDER);
		paths[4] = settings.getString("recent5", DEFAULT_FILE_PATH_HOLDER);
		paths[5] = settings.getString("recent6", DEFAULT_FILE_PATH_HOLDER);
		paths[6] = settings.getString("recent7", DEFAULT_FILE_PATH_HOLDER);
		paths[7] = settings.getString("recent8", DEFAULT_FILE_PATH_HOLDER);
		
		//check if file's exist and populate recentFilesPaths[] accordingly
		int ind = 0, i=0;
		for (; i<paths.length;i++) {
			if (!paths[i].equals(DEFAULT_FILE_PATH_HOLDER)) {
				recentFiles[ind] = new File(paths[i]);
				if (recentFiles[ind].isFile())
					ind++;
			}
		}
		for (i=ind; i<recentFiles.length;i++) {
			recentFiles[i] = null;
		}
	}
	
	public static void addRecentFiles(Context context, String path) {
		//scan for duplicate
		int dupPos = -1;
		for (int i = 0; i<paths.length; i++) {
			if (paths[i].equals(path)) {
				dupPos = i; //duplicate's location
				break;
			}
			else if (paths[i].equals(DEFAULT_FILE_PATH_HOLDER)) {
				dupPos = i; //end of current list
				break;
			}
		}
		if (dupPos == paths.length) //reached end of loop without breaking
			dupPos--;
		
		//add most recent to the top
		for (;dupPos>0; dupPos--) {
			paths[dupPos] = paths[dupPos-1];
		}
		paths[dupPos] = path;
		
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("recent1", paths[0]);
		editor.putString("recent2", paths[1]);
		editor.putString("recent3", paths[2]);
		editor.putString("recent4", paths[3]);
		editor.putString("recent5", paths[4]);
		editor.putString("recent6", paths[5]);
		editor.putString("recent7", paths[6]);
		editor.putString("recent8", paths[7]);
		editor.commit();
	}
}
