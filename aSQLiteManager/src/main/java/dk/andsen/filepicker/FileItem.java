package dk.andsen.filepicker;

//TODO remove unneeded and simplify split into FileHolder, ls2FileHolder and
//FileHolderCompare classes
//TODO problems with filename == null in root mode

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.andsen.utils.Utils;
//import com.ghostsq.commander.adapters.CommanderAdapter;

public class FileItem {
	private boolean _logging = true;
	//private static String TAG = "LsItem";
	// Debian FTP site
	// -rw-r--r-- 1 1176 1176 1062 Sep 04 18:54 README
	// Android FTP server
	// -rw-rw-rw- 1 system system 93578 Sep 26 00:26 Quote Pro 1.2.4.apk
	// Win2K3 IIS
	// -rwxrwxrwx 1 owner group 314800 Feb 10 2008 classic.jar

	// TODO prw-rw---- gps system 2013-03-06 07:26 .gps.interface.pipe.to_gpsd
	// ligger i /data
	//unmatched!!!
  private static Pattern unix = Pattern
  		.compile( "^([\\-bcdlprwxsStT]{9,10}\\s+\\d+\\s+[^\\s]+\\s+[^\\s]+)\\s+(\\d+)\\s+((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{1,2}\\s+(?:\\d{4}|\\d{1,2}:\\d{2}))\\s+(.+)" );
	// inetutils-ftpd:
	// drwx------ 3 user 80 2009-02-15 12:33 .adobe
	private static Pattern inet = Pattern
			.compile("^([\\-bcdlprwxsStT]{9,10}\\s+.+)\\s+(\\d*)\\s+(\\d{4}-\\d{2}-\\d{2}\\s\\d{1,2}:\\d{2})\\s+(.+)");
	// MSDOS style
	// 02-10-08 02:08PM 314800 classic.jar
	private static Pattern msdos = Pattern
			.compile("^(\\d{2,4}-\\d{2}-\\d{2,4}\\s+\\d{1,2}:\\d{2}[AP]M)\\s+(\\d+|<DIR>)\\s+(.+)");
	private static SimpleDateFormat format_date_time = new SimpleDateFormat(
			"MMM d HH:mm", Locale.ENGLISH);
	private static SimpleDateFormat format_date_year = new SimpleDateFormat(
			"MMM d yyyy", Locale.ENGLISH);
	private static SimpleDateFormat format_full_date = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm", Locale.ENGLISH);
	private static SimpleDateFormat format_msdos_date = new SimpleDateFormat(
			"MM-dd-yy  HH:mmaa", Locale.ENGLISH);

	public final static int MODE_WIDTH = 0x0001, NARROW_MODE = 0x0000,
			WIDE_MODE = 0x0001, MODE_DETAILS = 0x0002, SIMPLE_MODE = 0x0000,
			DETAILED_MODE = 0x0002, MODE_FINGERF = 0x0004, SLIM_MODE = 0x0000,
			FAT_MODE = 0x0004, MODE_HIDDEN = 0x0008, SHOW_MODE = 0x0000,
			HIDE_MODE = 0x0008, MODE_SORTING = 0x0030, SORT_NAME = 0x0000,
			SORT_SIZE = 0x0010, SORT_DATE = 0x0020, SORT_EXT = 0x0030,
			MODE_SORT_DIR = 0x0040, SORT_ASC = 0x0000, SORT_DSC = 0x0040,
			MODE_CASE = 0x0080, CASE_SENS = 0x0000, CASE_IGNORE = 0x0080,
			MODE_ATTR = 0x0300, NO_ATTR = 0x0000, SHOW_ATTR = 0x0100,
			ATTR_ONLY = 0x0200, MODE_ROOT = 0x0400, BASIC_MODE = 0x0000,
			ROOT_MODE = 0x0400, MODE_ICONS = 0x3000, TEXT_MODE = 0x0000,
			ICON_MODE = 0x1000, ICON_TINY = 0x2000,
			LIST_STATE = 0x10000, STATE_IDLE = 0x00000, STATE_BUSY = 0x10000,
			SET_TBN_SIZE = 0x01000000, SET_FONT_SIZE = 0x02000000;

	private String name = null;
	private String link_target_name = null;
	private String attr = null;
	private boolean directory = false;
	private boolean link = false;
	private long size = 0;
	private Date date = null;

	/**
	 * @param ls_string
	 */
	public FileItem(String ls_string) {
		Matcher m = unix.matcher(ls_string);
		if (m.matches()) {
			Utils.logD("Matched! unix:  " + ls_string, _logging);
			try {
				if (ls_string.charAt(0) == 'd')
					directory = true;
				if (ls_string.charAt(0) == 'l')
					link = true;
				name = m.group(4);
				size = Long.parseLong(m.group(2));
				String date_s = m.group(3);
				boolean in_year = date_s.indexOf(':') > 0;
				SimpleDateFormat df = in_year ? format_date_time : format_date_year;
				date = df.parse(date_s);
				if (in_year) {
					Calendar cal = Calendar.getInstance();
					int cur_year = cal.get(Calendar.YEAR) - 1900;
					int cur_month = cal.get(Calendar.MONTH);
					int f_month = date.getMonth();
					if (f_month > cur_month)
						cur_year--;
					date.setYear(cur_year);
				}
				attr = m.group(1);
				// Log.v( TAG, "Item " + name + ", " + attr );
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return;
		}
		m = inet.matcher(ls_string);
		if (m.matches()) {
			Utils.logD("Matched! inet:  " + ls_string, _logging);
			try {
				if (ls_string.charAt(0) == 'd')
					directory = true;
				name = m.group(4);
				if (ls_string.charAt(0) == 'l') { // link
					link = true;
					int arr_pos = name.indexOf(" -> ");
					if (arr_pos > 0) {
						link_target_name = name.substring(arr_pos + 4);
						name = name.substring(0, arr_pos);
					}
				}
				String sz_str = m.group(2);
				size = sz_str != null && sz_str.length() > 0 ? Long.parseLong(sz_str)
						: -1;
				String date_s = m.group(3);
				SimpleDateFormat df = format_full_date;
				date = df.parse(date_s);
				attr = m.group(1);
				if (attr != null)
					attr = attr.trim();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return;
		}
		m = msdos.matcher(ls_string);
		Utils.logD("Matched! msdos  " + ls_string, _logging);
		if (m.matches()) {
			try {
				name = m.group(3);
				if (m.group(2).equals("<DIR>"))
					directory = true;
				else
					size = Long.parseLong(m.group(2));

				String date_s = m.group(1);
				SimpleDateFormat df = format_msdos_date;
				date = df.parse(date_s);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return;
		}
		// TODO do some manual parsing here
		Utils.logE("Unmatched str   " + ls_string, _logging);
		if (ls_string.charAt(0) == 'd')
			directory = true;
		if (ls_string.charAt(0) == 'l')
			link = true;
		attr = ls_string.substring(0, ls_string.indexOf(" "));
		
	}
	
	public FileItem(String fName, long fSize, boolean isDir) {
		name = fName;
		link_target_name = null;
		attr = null;
		directory = isDir;
		link = false;
		size = fSize;
		date = null;
	}

	public FileItem(String fName, long fSize, boolean isDir, Date date) {
		name = fName;
		link_target_name = null;
		attr = null;
		directory = isDir;
		link = false;
		size = fSize;
		this.date = date;
	}

	/**
	 * @return a String with the name of the file
	 */
	public final String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;  
	}

	/**
	 * @return a Date with the dato of file
	 */
	public final Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the size of the file
	 */
	public final long length() {
		return size;
	}
	
	public final void setLength(long size) {
		this.size = size;
	}

	/**
	 * @return return false if file name is null
	 */
	public final boolean isValid() {
		return name != null;
	}

	/**
	 * @return true if is a directory
	 */
	public final boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean isDir) {
		directory = isDir;
	}
	/**
	 * @return return the target of a link
	 */
	public final String getLinkTarget() {
		return link ? link_target_name : null;
	}

	/**
	 * @return a String with the files attributes
	 */
	public final String getAttr() {
		return attr;
	}

	/**
	 * Compare to file names. Used for 
	 * @param o
	 * @return
	 */
	public final int compareTo(FileItem o) {
		if (o.getName() == null || getName() == null)
			return 0;
		return getName().compareTo(o.getName());
	}

//	/**
//	 * @author mh
//	 *
//	 */
//	public class FileItemComparator implements Comparator<FileItem> {
//		int _type;
//		boolean _case_ignore, _ascending;
//
//		public FileItemComparator(int type, boolean case_ignore,
//				boolean ascending) {
//			_type = type;
//			_case_ignore = case_ignore && (type == SORT_EXT || type == SORT_NAME);
//			_ascending = ascending;
//		}
//
//		@Override
//		public int compare(FileItem f1, FileItem f2) {
//			boolean f1IsDir = f1.isDirectory();
//			boolean f2IsDir = f2.isDirectory();
//			if (f1IsDir != f2IsDir)
//				return f1IsDir ? -1 : 1;
//			int ext_cmp = 0;
//			switch (_type) {
//			case SORT_EXT:
//				ext_cmp = _case_ignore ? f1.getName().compareToIgnoreCase(f2.getName()) : f1.compareTo(f2);
//				ext_cmp = _case_ignore ?
//						getFileExt(f1.getName() ).compareToIgnoreCase(
//						getFileExt(f2.getName() ) ) :
//						getFileExt(f1.getName() ).compareTo(getFileExt(
//						f2.getName()));
//				break;
//			case SORT_SIZE:
//				ext_cmp = f1.length() - f2.length() < 0 ? -1 : 1;
//				break;
//			case SORT_DATE:
//				ext_cmp = f1.getDate().compareTo(f2.getDate());
//				break;
//			}
//			if (ext_cmp == 0)
//				ext_cmp = _case_ignore ? f1.getName().compareToIgnoreCase(f2.getName())
//						: f1.compareTo(f2);
//			return _ascending ? ext_cmp : -ext_cmp;
//		}
//	}

	/**
	 * Create a list of LsItem
	 * @param n number of entries in the list
	 * @return the list
	 */
	public static FileItem[] createArray(int n) {
		return new FileItem[n];
	}
}
