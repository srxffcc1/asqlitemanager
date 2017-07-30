package dk.andsen.asqlitemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import dk.andsen.utils.Utils;

public class AppSettings {
	private static boolean _logging = true;
	
	/**
	 * Save a configuration String in a aSQLiteManager.xml
	 * @param cont
	 * @param id The values id
	 * @param valThe value
	 */
	public static void saveString(Context cont, String id, String val) {
		Utils.logD("Storing " + id + ": " + val, _logging);
		SharedPreferences settings = cont.getSharedPreferences("aSQLiteManager",
				Context.MODE_PRIVATE);
		Editor ed = settings.edit();
		ed.putString(id, val);
		ed.commit();
	}
	
	public static String getString(Context cont, String id) {
		SharedPreferences settings = cont.getSharedPreferences("aSQLiteManager",
				Context.MODE_PRIVATE);
		Utils.logD("Retriving " + id + " = " + settings.getString(id, null), _logging);
		return settings.getString(id, null);
	}

	public static void saveInt(Context cont, String id, int val) {
		Utils.logD("Storing " + id + ": " + val, _logging);
		SharedPreferences settings = cont.getSharedPreferences("aSQLiteManager",
				Context.MODE_PRIVATE);
		Editor ed = settings.edit();
		ed.putInt(id, val);
		ed.commit();
	}

	public static int getInt(Context cont, String id, int defaultVal) {
		SharedPreferences settings = cont.getSharedPreferences("aSQLiteManager",
				Context.MODE_PRIVATE);
		Utils.logD("Retriving " + id + " = " + settings.getInt(id, defaultVal), _logging);
		return settings.getInt(id, defaultVal);
	}

	public static void saveBoolean(Context cont, String id, boolean val) {
		Utils.logD("Storing " + id + ": " + val, _logging);
		SharedPreferences settings = cont.getSharedPreferences("aSQLiteManager",
				Context.MODE_PRIVATE);
		Editor ed = settings.edit();
		ed.putBoolean(id, val);
		ed.commit();
	}

	public static boolean getBoolean(Context cont, String id, boolean defaultVal) {
		SharedPreferences settings = cont.getSharedPreferences("aSQLiteManager",
				Context.MODE_PRIVATE);
		Utils.logD("Retriving " + id + " = " + settings.getBoolean(id, defaultVal), _logging);
		return settings.getBoolean(id, defaultVal);
	}

	/**
	 * Test if a hint should be shown
	 * @param cont
	 * @param no The number of the hint
	 * @return Return true if a hint should be shown
	 */
	public static boolean showHint(Context cont, int no) {
		SharedPreferences prefs = cont.getSharedPreferences(
				"dk.andsen.asqlitemanager_tips", Context.MODE_PRIVATE);
		return prefs.getBoolean("TipNo" + no, true);		
	}
	
	/**
	 * Save if a hint should be shown on next run
	 * @param cont
	 * @param no The hints number
	 * @param status True if it should be shown at next run
	 */
	public static void setHint(Context cont, int no, boolean status) {
		SharedPreferences prefs = cont.getSharedPreferences(
				"dk.andsen.asqlitemanager_tips", Context.MODE_PRIVATE);
		Editor edt = prefs.edit();
		Utils.logD("Show again " + status, _logging);
		edt.putBoolean("TipNo" + no, status);
		edt.commit();
	}
	
	public static boolean showWelcome(Context cont, String welcomeId) {
		final SharedPreferences settings = 
				cont.getSharedPreferences("aSQLiteManager", Context.MODE_PRIVATE);
		return settings.getBoolean(welcomeId, true);
	}
	
	public static void setWelcomeMsg(Context cont, String welcomeId, boolean status) {
		final SharedPreferences settings = 
				cont.getSharedPreferences("aSQLiteManager", Context.MODE_PRIVATE);
		android.content.SharedPreferences.Editor edt = settings.edit();
		edt.putBoolean(welcomeId, status);
		edt.commit();
	}
	
	public static String getRecentFile(Context cont) {
		SharedPreferences settings = cont.getSharedPreferences("aSQLiteManager",
				Context.MODE_PRIVATE);
		return settings.getString("Recently", null);
	}
	
	public static void saveRecentFiles(Context cont, String recent) {
		SharedPreferences settings = cont.getSharedPreferences("aSQLiteManager",
				Context.MODE_PRIVATE);
		 Editor ed = settings.edit();
		 Utils.logD("New recent: " + recent, _logging);
		 ed.putString("Recently", recent);
		 ed.commit();
	}
	
	/**
	 * This is normally changed from the configuration but must also be set to false
	 *  before editing root databases
	 * @param cont
	 * @param saveSQL
	 */
	public static void saveSaveSQL(Context cont, boolean saveSQL) {
		SharedPreferences settings = cont.getSharedPreferences("dk.andsen.asqlitemanagerd_preferences",
				Context.MODE_PRIVATE);
		 Editor ed = settings.edit();
		 Utils.logD("SaveSQL set to: " + saveSQL, _logging);
		 ed.putBoolean("SaveSQL", saveSQL);
		 ed.commit();
	}
}
