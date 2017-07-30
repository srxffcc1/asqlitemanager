package dk.andsen.filepicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import dk.andsen.utils.Utils;

public class FPSettings {
	private static boolean _logging = true;
	
	public static String getRecentDirs(Context cont) {
		SharedPreferences settings = cont.getSharedPreferences("FilePicker",
				Context.MODE_PRIVATE);
		return settings.getString("FavoritDirs", null);
	}
	
	public static void saveRecentDirs(Context cont, String recent) {
		SharedPreferences settings = cont.getSharedPreferences("FilePicker",
				Context.MODE_PRIVATE);
		 Editor ed = settings.edit();
		 Utils.logD("New FavoritDirs: " + recent, _logging);
		 ed.putString("FavoritDirs", recent);
		 ed.commit();
	}

	public static String getRecentRootDirs(Context cont) {
		SharedPreferences settings = cont.getSharedPreferences("FilePicker",
				Context.MODE_PRIVATE);
		return settings.getString("FavoritRootDirs", null);
	}
	
	public static void saveRecentRootDirs(Context cont, String recent) {
		SharedPreferences settings = cont.getSharedPreferences("FilePicker",
				Context.MODE_PRIVATE);
		 Editor ed = settings.edit();
		 Utils.logD("New FavoritDirs: " + recent, _logging);
		 ed.putString("FavoritRootDirs", recent);
		 ed.commit();
	}
	
	public static boolean getSuWorking(Context cont) {
		SharedPreferences settings = cont.getSharedPreferences("FilePicker",
				Context.MODE_PRIVATE);
		return settings.getBoolean("SuWorking", false);
	}

	public static void saveSuWorking(Context cont, boolean suWorking) {
		SharedPreferences settings = cont.getSharedPreferences("FilePicker",
				Context.MODE_PRIVATE);
		 Editor ed = settings.edit();
		 Utils.logD("New SuWorking: " + suWorking, _logging);
		 ed.putBoolean("SuWorking", suWorking);
		 ed.commit();
	}
	
}
