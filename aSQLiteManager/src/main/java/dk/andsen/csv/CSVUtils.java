package dk.andsen.csv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CSVUtils {
	/**
	 * Write a line of CSV items to w
	 * @param w a Writer
	 * @param values a List of Strings with the CSV items
	 * @throws Exception
	 */
	public static void writeLine(Writer w, List<String> values) throws Exception {
		boolean firstVal = true;
		for (String val : values) {
			if (!firstVal) {
				w.write(",");
			}
			// This writes all in quotation marks. Not very good but knowing the SQLite handling
			// of data types as good as it gets right now
			w.write("\"");
			if (val == null)
			val = "null";
			for (int i = 0; i < val.length(); i++) {
				char ch = val.charAt(i);
				if (ch == '\"') { // is it a quote add an extra
					w.write("\""); 
				}
				w.write(ch);
			}
			w.write("\"");
			firstVal = false;
		}
		w.write("\n");
	}

	/**
	 * Converts a line of CSV items to a String List
	 * @param line a String holding a number of CSV items
	 * @return a List of Strings with the CSV items
	 * @throws Exception
	 */
	public static List<String> parseLine(String line) throws Exception {
		if (line == null) {
			return null;
		}
		Vector<String> store = new Vector<String>();
		StringBuffer curVal = new StringBuffer();
		boolean inquotes = false;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (inquotes) {
				if (ch == '\"') {
					inquotes = false;
				} else {
					curVal.append(ch);
				}
			} else {
				if (ch == '\"') {
					inquotes = true;
					if (curVal.length() > 0) {
						// if this is the second quote in a value, add a quote
						// this is for the double quote in the middle of a value
						curVal.append('\"');
					}
				} else if (ch == ',') {
					if (curVal.toString().equals("null"))
						store.add(null);
					else
						store.add(curVal.toString());
					curVal = new StringBuffer();
				} else {
					curVal.append(ch);
				}
			}
		}
		if (curVal.toString().equals("null"))
			store.add(null);
		else
			store.add(curVal.toString());
		return store;
	}
	
	/**
	 * Read up to noLines from a file
	 * @param csvFile
	 * @param noLines
	 * @return
	 * @throws Exception
	 */
	public static List<String> readFirstLines(String csvFile, int noLines, int linesToSkip) throws Exception {
		List<String> lines = new ArrayList<String>();
		String line;
		FileInputStream fis = new FileInputStream(csvFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		int i = 0;
		while ((line = br.readLine()) != null && (++i <= noLines + linesToSkip)) {
			if (i > linesToSkip)
				lines.add(line);
		}
		br.close();
		fis.close();
		return lines;
	}
}