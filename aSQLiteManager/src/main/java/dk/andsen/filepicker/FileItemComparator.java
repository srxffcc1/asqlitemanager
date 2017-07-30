package dk.andsen.filepicker;

import java.util.Comparator;

public class FileItemComparator implements Comparator<FileItem> {

	public final static int SORT_NAME = 0, SORT_DATE = 2, 
			SORT_SIZE = 3, SORT_EXT = 1;

	int _type;
	boolean _case_ignore, _ascending;

	public FileItemComparator(int type, boolean case_ignore,
			boolean ascending) {
		_type = type;
		_case_ignore = case_ignore && (type == SORT_EXT || type == SORT_NAME);
		_ascending = ascending;
	}

	public int compare(FileItem f1, FileItem f2) {
		boolean f1IsDir = f1.isDirectory();
		boolean f2IsDir = f2.isDirectory();
		if (f1IsDir != f2IsDir)
			return f1IsDir ? -1 : 1;
		int ext_cmp = 0;
		switch (_type) {
		case SORT_EXT:
			ext_cmp = _case_ignore ? f1.getName().compareToIgnoreCase(f2.getName()) : f1.compareTo(f2);
			ext_cmp = _case_ignore ?
					getFileExt(f1.getName() ).compareToIgnoreCase(
					getFileExt(f2.getName() ) ) :
					getFileExt(f1.getName() ).compareTo(getFileExt(
					f2.getName()));
			break;
		case SORT_SIZE:
			ext_cmp = f1.length() - f2.length() < 0 ? -1 : 1;
			break;
		case SORT_DATE:
			ext_cmp = f1.getDate().compareTo(f2.getDate());
			break;
		}
		if (ext_cmp == 0)
			ext_cmp = _case_ignore ? f1.getName().compareToIgnoreCase(f2.getName())
					: f1.compareTo(f2);
		return _ascending ? ext_cmp : -ext_cmp;
	}
	
  /**
   * @param file_name
   * @return the extension of a files name
   */
  private String getFileExt( String file_name ) {
    if( file_name == null )
        return "";
    int dot = file_name.lastIndexOf( "." );
    return dot >= 0 ? file_name.substring( dot ) : "";
  }
}
