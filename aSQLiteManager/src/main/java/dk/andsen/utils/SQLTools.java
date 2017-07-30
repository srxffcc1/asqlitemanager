package dk.andsen.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import dk.andsen.asqlitemanager.Prefs;

public class SQLTools {
	private static boolean _logging; 

	/**
	 * @param cont
	 * @param scriptFile
	 * @return
	 */
	public static List<String> parseSQLFile(Context cont, String scriptFile) {
		_logging = Prefs.getLogging(cont);
		Utils.logD("Parsing SQL file to List of statemenst", _logging);
		FileReader _f;
		BufferedReader _in;
		final List<String> mylist = new ArrayList<String>();
    try {
    	String nl = "\n";
			_f = new FileReader(scriptFile);
			_in = new BufferedReader(_f);
			Utils.logD("Importing from; " + scriptFile, _logging);
			String line = "";
			String nline;
			boolean inTrigger = false;
			// put each statement in the list
			while ((nline = _in.readLine()) != null) {
				line += nline;
				// starting to read a trigger definition?
				if (nline.trim().toUpperCase(Locale.US).startsWith("CREATE TRIGGER")) {
					inTrigger = true;
				}
				// if more of statement coming append newline
				if (!(line.endsWith(";") || line.equals("")) || inTrigger)
					line += nl;
	      if(line.startsWith("--")) {
	        // It a comment just empty line
					mylist.add(line);
	      	line = "";
	      } else if((nline.trim().endsWith(";") && !inTrigger) || 
	      		(inTrigger && nline.trim().toUpperCase(Locale.US).endsWith("END;"))) {
	        // If line ends with ";" or "end;" if it is a trigger we have a statement
	      	// ready to execute
					inTrigger = false;
	      	line = line.substring(0, line.length() - 1);
	      	//Utils.logD("SQL: " + line, logging);
					mylist.add(line);
	      	line = "";
	      }
			}
	    _in.close();
	    _f.close();
    }  catch (Exception e) {
    	Utils.logE("Exception! " + e.getLocalizedMessage(), _logging);
    	Utils.showException(e.getLocalizedMessage(), cont);
    	return null;
    }
		Utils.logD("All lines read", _logging);
		return mylist;
	}
}
