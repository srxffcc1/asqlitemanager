/*
 * Part of Andsen's file picker / directory picker / file opener with root support 
 *
 * The main class of the root file picker
 *
 * @author andsen
 */
package dk.andsen.filepicker;

/*
 * TODO too many updates of list (after sort dialog but before sorting are activated)
 */
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import dk.andsen.asqlitemanager.AppSettings;
import dk.andsen.asqlitemanager.R;
import dk.andsen.utils.Utils;

public class FilePicker extends ListActivity {
	public final static String NOTIFY_RECEIVER_HASH = "hash",
			NOTIFY_ITEMS_TO_RECEIVE = "itms";
	public int GET_DIR = 1;
	public int GET_FILE = 2;
	private static final int MENU_SORTING = 1;
	private static final int MENU_EXIT = 2;
	private static final int MENU_AD_TO_FAVORITES = 4;
	private static final int MENU_FAVORITES = 5;
	private boolean get_dir = false;
	private boolean open_file = false;
	private static List<String> item = null;
	private static List<FileItem> ls;	
	private static TextView tvPath;
	private static String _path;
	private String _startPath;
	private String root = "/";
	private static ProgressDialog _pd;
	private static Activity _act;
	private static String[] _fileTypes;
	private static boolean _logging = true;
	private Button btnGetDir;
	private ListView _lv;
	// change to ignore case during sort
	private boolean caseIgnore = false;
	// change to sort ascending
	private boolean asending = true;
	// Change this to change what files are sorted by
	private int sortBy = FileItemComparator.SORT_NAME;
	// The files to show
	protected String fileFilter = "";
	private String _recentDirs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_act = this;
		setContentView(R.layout.filepicker);
		//final ListView listview = (ListView) findViewById(R.id.list);
		tvPath = (TextView) findViewById(R.id.path);
		btnGetDir = (Button)findViewById(R.id.SelectFolder);
		btnGetDir.setVisibility(View.GONE);
		_lv = getListView();
		_lv.requestFocus();
		getListView().setFastScrollEnabled(true);
		//Add SectionIndexer 
		Bundle extras = getIntent().getExtras();
		if(extras !=null) {
			_path = extras.getString("StartDir");
			_startPath = _path;
			get_dir = extras.getBoolean("GetDir");
			open_file  = extras.getBoolean("OpenFile");
			_fileTypes = extras.getStringArray("FileTypes");
			tvPath.setText(_path);
			if (get_dir) {
				btnGetDir.setVisibility(View.VISIBLE);
				btnGetDir.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Intent in = new Intent();
						in.putExtra("RESULT", endWithSlash(_path));
			      setResult(1,in);
			      finish();
					}
				});
			}
			// Load sorting from configurations
			sortBy = AppSettings.getInt(_act, "SortBy", FileItem.SORT_NAME);
			asending = AppSettings.getBoolean(_act, "Asending", true);
			caseIgnore = AppSettings.getBoolean(_act, "CaseIgnore", false);
			fileFilter = (AppSettings.getString(_act, "FileFilter") == null ? "" : AppSettings.getString(_act, "FileFilter"));
			if (fileFilter.trim().length() > 0)
				Utils.redToastMsg(_act, getText(R.string.Filter).toString() + " = " + fileFilter);
			refreshList(_path);
		} else {
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);        
			finish();			
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Utils.logD("position "  + position, _logging);
		FileItem clickedItem = ls.get(position);
		// if it is a directory show its content
		if (clickedItem.isDirectory()) {
			String tPath = _path; 
			if (clickedItem.getName().equals("..")) {
				tPath = _path.substring(0, _path.lastIndexOf("/"));
				if (tPath.trim().length() == 0)
					tPath = "/";
			} else {
				tPath = endWithSlash(tPath) + clickedItem.getName();
			}
			File f = new File(tPath);
			if (!f.canRead()) {
				// a root directory entered
				Utils.showMessage(getText(R.string.Error).toString(),
						getText(R.string.RootWarning).toString(), _act);
			} else {
				_path = tPath;
				refreshList(_path);
			}
		} else {
			// A file is clicked
			if (open_file) {
				Utils.logD("File: " + _path + "/" + clickedItem.getName(), _logging);
				Uri fileURI = Uri.fromFile(new File(endWithSlash(_path) + "/" + clickedItem.getName()));
				File f = new File(endWithSlash(_path) + "/" + clickedItem.getName());
				// test if it a root dir
				if (!f.canRead()) {
					// It is a root catalogue cannot read that
					Utils.showMessage(getText(R.string.Error).toString(),
							getText(R.string.CannotReadSystemDirs).toString(), _act);
				} else {
					// It is not a root catalogue
					openDatabase(fileURI);
				}
			} else  {
				Intent in = new Intent();
				in.putExtra("RESULT", endWithSlash(_path) + clickedItem.getName());
				setResult(RESULT_OK, in);
				finish();
			}
		}
	}

	/**
	 * Open a database in catalogue that allows reading
	 * @param fileURI
	 */
	private void openDatabase(Uri fileURI) {
		String newDBPath = fileURI.getPath().substring(0, fileURI.getPath().lastIndexOf("/"));
		Utils.logD("Storing RecentOpenDBPath " + newDBPath, _logging);
		AppSettings.saveString(_act, "RecentOpenDBPath", newDBPath);
		Utils.logD("Other file " + fileURI.getPath(), _logging);
		Intent iDBViewer = new Intent(this, dk.andsen.asqlitemanager.DBViewer.class);
		iDBViewer.putExtra("db", "" + fileURI.getPath());
		startActivity(iDBViewer);
//		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
//		intent.setDataAndType(fileURI, "application/x-sqlite3");
//		try {
//			startActivity(intent); 
//		} catch (ActivityNotFoundException e) {
//			Toast.makeText(this, "No app to open this file ;-(", Toast.LENGTH_SHORT).show();
//		};
	}

	/**
	 * Return a String that ends with a "/"
	 * @param str The String you want to make sure ends with /
	 * @return a String which is the param str where a / is added if it didn't end with / 
	 */
	private String endWithSlash(String str) {
		if (str.endsWith("/"))
			return str;
		else
			return str + "/";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SORTING, 0, getText(R.string.Sorting)).setIcon(  
				android.R.drawable.ic_menu_sort_by_size);
		menu.add(0, MENU_AD_TO_FAVORITES, 2, "Ad to favoites");
		menu.add(0, MENU_FAVORITES, 3, "Favoites");
		menu.add(0, MENU_EXIT, 4, getText(R.string.Exit));
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SORTING:
			Utils.logD("Sorting", _logging);
			sortingDialog();
			return true;
		case MENU_EXIT:
			this.finish();
			return true;
		case MENU_FAVORITES:
			Utils.logD("Opening recent dir list", _logging);
			_recentDirs = FPSettings.getRecentDirs(_act); 
			if (_recentDirs == null) {
				Utils.showMessage(getText(R.string.Error).toString(),
						getText(R.string.NoFavorites).toString(), _act);
			} else {
				String[] resentlyDirs = _recentDirs.split(";");
				AlertDialog dial = new AlertDialog.Builder(this)
						.setTitle(getText(R.string.Favorites))
						.setSingleChoiceItems(resentlyDirs, 0, new ResentFileOnClickHandler())
						.create();
				dial.show();
			}
			return true;
		case MENU_AD_TO_FAVORITES:
			int noOfRecent = 10; //Prefs.getNoOfFiles(_act);
//			SharedPreferences settings = getSharedPreferences("aShell", MODE_PRIVATE);
//			String files = settings.getString("FavoritDirs", null);
//			files = RecentlyDirs.updateList(files, _path, noOfFiles);
//			Editor edt = settings.edit();
//			edt.putString("FavoritDirs", files);
//			edt.commit();
			String files = FPSettings.getRecentDirs(_act);
			files = RecentlyDirs.updateList(files, _path, noOfRecent);
			FPSettings.saveRecentDirs(_act, files);
			return true;
		}
		return false;
	}
	
	/**
	 * Change to a recent directory
	 */
	public class ResentFileOnClickHandler implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Utils.logD("Favorites taped " + "", _logging);
			String[] dirs = FPSettings.getRecentDirs(_act).split(";");
			String dir = dirs[which];
			Utils.logD("Resent dir " + dir, _logging);
			_path = dir;
			tvPath.setText(dir);
			refreshList(dir);
			dialog.dismiss();
		}
	}
	
	private void sortingDialog() {
		// Set up sorting here
		final Dialog sortingDialog = new Dialog(this);
		sortingDialog.setContentView(R.layout.file_picker_sorting);
		sortingDialog.setTitle(R.string.Sorting);
		final CheckBox cbAscending = (CheckBox) sortingDialog.findViewById(R.id.cbAscending);
		cbAscending.setChecked(asending);
		final CheckBox cbIgnoreCase = (CheckBox) sortingDialog.findViewById(R.id.cbIgnoreCase);
		cbIgnoreCase.setChecked(caseIgnore);
		final RadioGroup orderByGroup = (RadioGroup) sortingDialog.findViewById(R.id.radioSortBy);
		final RadioButton byName = (RadioButton) sortingDialog.findViewById(R.id.radioName);
		final int byNameId = byName.getId();
		final RadioButton byExt = (RadioButton) sortingDialog.findViewById(R.id.radioExt); 
		final int byExtId = byExt.getId();
		final RadioButton byDate = (RadioButton) sortingDialog.findViewById(R.id.radioDate); 
		final int byDateId = byDate.getId();
		final RadioButton bySize = (RadioButton) sortingDialog.findViewById(R.id.radioSize); 
		final int bySizeId = bySize.getId();
		if (sortBy == FileItemComparator.SORT_NAME)
			orderByGroup.check(byNameId);
		if (sortBy == FileItemComparator.SORT_EXT)
			orderByGroup.check(byExtId);
		if (sortBy == FileItemComparator.SORT_DATE)
			orderByGroup.check(byDateId);
		if (sortBy == FileItemComparator.SORT_SIZE)
			orderByGroup.check(bySizeId);
		final EditText etFilter = (EditText) sortingDialog.findViewById(R.id.edFileFilter);
		etFilter.setText(fileFilter);
		Button btnOk = (Button) sortingDialog.findViewById(R.id.btnSOK);
		btnOk.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				asending = cbAscending.isChecked();
				caseIgnore = cbIgnoreCase.isChecked();
				int selectedId = orderByGroup.getCheckedRadioButtonId();
				sortBy = orderByGroup.getCheckedRadioButtonId();
				Utils.logD("Sort by " + sortBy, _logging);
				if (selectedId == byNameId) {
					sortBy = FileItemComparator.SORT_NAME;
				} else if (selectedId == byExtId) {
					sortBy = FileItemComparator.SORT_EXT;
				} else if(selectedId == byDateId) {
					sortBy = FileItemComparator.SORT_DATE;
				} else if (selectedId == bySizeId) {
					sortBy = FileItemComparator.SORT_SIZE;
				}
				//if (!etFilter.getText().toString().trim().equals("")) {
				fileFilter  = etFilter.getText().toString().trim();
				//}
				AppSettings.saveInt(_act, "SortBy", sortBy);
				AppSettings.saveBoolean(_act, "Asending", asending);
				AppSettings.saveBoolean(_act, "CaseIgnore", caseIgnore);
				AppSettings.saveString(_act, "FileFilter", fileFilter);
				refreshList(_path);
				sortingDialog.dismiss();
			}
		});
		sortingDialog.show();
		//Utils.showMessage("Sorting", "Sorting", android.R.drawable.ic_menu_sort_by_size, "OK", _act);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (item != null && item.size() > 0) {
			updateList();
		}
		super.onWindowFocusChanged(hasFocus);
	}

	/**
	 * Update the list on the screen
	 */
	private void updateList() {
		FileListArrayAdapter mlist = new FileListArrayAdapter(_act , item, ls, _fileTypes);
		setListAdapter(mlist);
		_lv.requestFocus();
		_pd.dismiss();
	}
	
	/**
	 * Refresh the data list
	 * @param path Path to the catalogue to display 
	 */
	private void refreshList(String path) {
		_pd = ProgressDialog.show(this, getString(R.string.Working),
				null, true, false);
		getDir(path);
		updateList();
	}

	/**
	 * Read the files from the directory identified by path
	 * @param path the path to read
	 */
	private void getDir(String path) {
		tvPath.setText(_path);   //getText(R.string.Path) + " " + 
		item = new ArrayList<String>();
		ls  = new ArrayList<FileItem>();
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				String[] fileTypes = fileFilter.split(";");
				//No filter accept all files
				if (fileFilter.equals(""))
					return true;
				// Accept all directories
				if (f.isDirectory())
					return true;
				// Accept all files with an extension specified in the filter
				for(String ext: fileTypes) {
					if (f.getName().endsWith(ext)) {
						return true;
					}
				}
				return false;
			}
		};
		File f = new File(_path);
		File[] files = f.listFiles(filter);
		//Utils.logD("Files found = " + files.length, _logging);
		if (files != null) {
			//Utils.logD("Files " + files.length, _logging);
			for (int i = 0; i < files.length; i++) {
				item.add(files[i].getName());
				Date date = new Date(files[i].lastModified());
				// TODO should identify links
				ls.add(new FileItem(files[i].getName() , files[i].length(), files[i].isDirectory(), date));
			}
			// Do not sort empty directories
			if(ls.size() > 0) {
				FileItemComparator cmp = new FileItemComparator(sortBy , caseIgnore, asending);
				Collections.sort(ls, cmp);
			}
		}
		// Add .. if not in root catalogue
		if (!_path.equals(root )) {
			if (f.getParentFile() != null) {
				ls.add(0, new FileItem("..", -1, true));
				item.add(0, "..");
			}
		}
	}

	/* 
	 * If in root folder exit else move one directory up
	 * TODO when first back is hit from / show message "Back one more to exit"
	 * Then exit if next action is back   
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
    	Utils.logD("Back pressed", _logging);
    	if (_path.equalsIgnoreCase(_startPath)) {
        return super.onKeyDown(keyCode, event);
    	}
    	if (!_path.equals("/")) {
  			_path = _path.substring(0, _path.lastIndexOf("/"));
  			if (_path.trim().length() == 0)
  				_path = "/";
  			tvPath.setText(_path);
  			refreshList(_path);
      	return false;
    	} 
    }
    return super.onKeyDown(keyCode, event);
	}
}