/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * This class contains the preference functionalities.
 *
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import dk.andsen.asqlitemanager.R;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {
   // Option names and default values
   private static final String OPT_PAGESIZE = "PageSize";
   private static final String OPT_PAGESIZE_DEF = "20";
   private static final String OPT_SAVESQL = "SaveSQL";
   private static final boolean OPT_SAVESQL_DEF = false;
   private static final String OPT_FILENO = "RecentFiles";
   private static final String OPT_FILENO_DEF = "5";
   private static final String OPT_FONTSIZE = "FontSize";
   private static final String OPT_FONTSIZE_DEF = "12";
   private static final String OPT_FK2LIST = "FKList";
   private static final boolean OPT_FK2LIST_DEF = false;
   private static final String OPT_FKON = "EnableForeignKeys";
   private static final boolean OPT_FKON_DEF = false;
   private static final String OPT_LOGGING = "Logging";
   private static final boolean OPT_LOGGING_DEF = false;
   private static final String OPT_VERTICAL = "MainVertical";
   private static final boolean OPT_VERTICAL_DEF = false;
   private static final String OPT_PAUSE = "Pause";
   private static final String OPT_PAUSE_DEF = "500";
   private static final String OPT_SULOCATION = "SuShell";
   private static final String OPT_SULOCATION_DEF = null;
   private static final String OPT_TESTROOT = "TestRoot";
   private static final boolean OPT_TESTROOT_DEF = false;
   private static final String OPT_MAX_WIDTH = "MaxWidth";
   private static final String OPT_MAX_WIDTH_DEF = "0";
   private static final String OPT_QEDIT_MAX_LINES = "QEMaxLines";
   private static final String OPT_QEDIT_MAX_LINES_DEF = "5";
   private static final String OPT_QEDIT_MIN_LINES = "QEMinLines";
   private static final String OPT_QEDIT_MIN_LINES_DEF = "2";
   private static final String OPT_SPATIALITE = "Spatialite";
   private static final boolean OPT_SPATIALITE_DEF = true;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings);
   }

		public static int getQEMaxLines(Context context) {
		   return validPositiveIntegerOrNumber(PreferenceManager.getDefaultSharedPreferences(context)
		         .getString(OPT_QEDIT_MAX_LINES, OPT_QEDIT_MAX_LINES_DEF), 5);
		}

   public static int getQEMinLines(Context context) {
		   return validPositiveIntegerOrNumber(PreferenceManager.getDefaultSharedPreferences(context)
		         .getString(OPT_QEDIT_MIN_LINES, OPT_QEDIT_MIN_LINES_DEF), 1);
		}

   /**
    * Return the numbers of records to retrieve when paging data
    * @param context
    * @return page size
    */
		public static int getPageSize(Context context) {
		   return validPositiveIntegerOrNumber(PreferenceManager.getDefaultSharedPreferences(context)
		         .getString(OPT_PAGESIZE, OPT_PAGESIZE_DEF), 0);
		}

  /**
   * Return the numbers of records to retrieve when paging data
   * @param context
   * @return page size
   */
	 public static int getFontSize(Context context) {
	    return validPositiveIntegerOrNumber(PreferenceManager.getDefaultSharedPreferences(context)
	          .getString(OPT_FONTSIZE, OPT_FONTSIZE_DEF), 0);
	 }

  /**
   * Return true if executed statements are stored in database
   * @param context
   * @return
   */
  public static boolean getSaveSQL(Context context) {
  	return PreferenceManager.getDefaultSharedPreferences(context)
  		.getBoolean(OPT_SAVESQL, OPT_SAVESQL_DEF);
  }

	public static int getNoOfFiles(Context context) {
    return validPositiveIntegerOrNumber(PreferenceManager.getDefaultSharedPreferences(context)
      	.getString(OPT_FILENO, OPT_FILENO_DEF), 0);
	}
	
	public static int getMaxWidth(Context context) {
		return validPositiveIntegerOrNumber(PreferenceManager.getDefaultSharedPreferences(context)
      	.getString(OPT_MAX_WIDTH, OPT_MAX_WIDTH_DEF), 0);
	}
	
  public static boolean getFKList(Context context) {
  	return PreferenceManager.getDefaultSharedPreferences(context)
  		.getBoolean(OPT_FK2LIST, OPT_FK2LIST_DEF);
  }

  public static boolean getEnableFK(Context context) {
  	return PreferenceManager.getDefaultSharedPreferences(context)
  		.getBoolean(OPT_FKON, OPT_FKON_DEF);
  }

  public static boolean getLogging(Context context) {
  	return PreferenceManager.getDefaultSharedPreferences(context)
  		.getBoolean(OPT_LOGGING, OPT_LOGGING_DEF);
  }

  public static boolean getMainVertical(Context context) {
  	return PreferenceManager.getDefaultSharedPreferences(context)
  		.getBoolean(OPT_VERTICAL, OPT_VERTICAL_DEF);
  }

  public static int getPause(Context context) {
  	return validPositiveIntegerOrNumber(PreferenceManager.getDefaultSharedPreferences(context)
  		.getString(OPT_PAUSE, OPT_PAUSE_DEF), 0);
  }

  public static String getSuLocation(Context context) {
  	return PreferenceManager.getDefaultSharedPreferences(context)
  	.getString(OPT_SULOCATION, OPT_SULOCATION_DEF);
  }

  public static boolean getTestRoot(Context context) {
  	return PreferenceManager.getDefaultSharedPreferences(context)
  		.getBoolean(OPT_TESTROOT, OPT_TESTROOT_DEF);
  }
  
  public static int getDefaultView(Context context) {
  	return new Integer(PreferenceManager.getDefaultSharedPreferences(context)
    .getString("DefaultView", "1")).intValue();
  }

  public static boolean getUseSpatialite(Context context) {
  	return PreferenceManager.getDefaultSharedPreferences(context)
  		.getBoolean(OPT_SPATIALITE, OPT_SPATIALITE_DEF);
  }
 
  /**
   * convert a Sting to a int >= 0 all negative and invalid numbers are treated as zero
   * @param strVal
   * @return
   */
  private static int validPositiveIntegerOrNumber(String strVal, int number) {
		if (strVal.trim().equals(""))
			strVal = "" + number;
		Integer i = number;
		try {
			i = new Integer(strVal).intValue();
			if (i<0)
				i = 0;
		} catch (Exception e) {
			
		}
  	return i.intValue();
  }
}