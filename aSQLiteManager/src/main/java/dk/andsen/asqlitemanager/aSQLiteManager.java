/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * The main class of the aSQLiteManager
 *
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import dk.andsen.filepicker.FilePicker;
import dk.andsen.utils.Utils;

public class aSQLiteManager extends Activity implements OnClickListener {
	/**
	 * True to enable functions under test
	 */
	private static final int MENU_OPT = 1;
	private static final int MENU_HLP = 2;
	private static final int MENU_RESET = 3;
	private final String WelcomeId = "ShowWelcome3.6";
	private final String vers = "3.6";
	private Context _cont;
	private String _recentFiles;
	private boolean _logging = false;
	private boolean loadSettings = false;
	private boolean _showWelcome = false;
	private boolean _showTip = false;
	private Dialog newDatabaseDialog;

	public static Database database = null;
	//public jsqlite.Database spatialDatabase;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_logging = Prefs.getLogging(this);
		Utils.logD("onCreate", _logging);
		_cont = this;
		if (Prefs.getMainVertical(this))
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.main);
		Button open = (Button) this.findViewById(R.id.Open);
		open.setOnClickListener(this);
		Button about = (Button) this.findViewById(R.id.About);
		about.setOnClickListener(this);
		Button newDatabase = (Button) this.findViewById(R.id.NewDB);
		newDatabase.setOnClickListener(this);
		Button recently = (Button) this.findViewById(R.id.Recently);
		recently.setOnClickListener(this);
		TextView tv = (TextView) this.findViewById(R.id.Version);
		tv.setText(getText(R.string.Version) + " " + getText(R.string.VersionNo));
		AppSettings.saveString(_cont, vers, vers);
		if (savedInstanceState != null) {
			Utils.logD("savedInstance true", _logging);
			if (savedInstanceState.getBoolean("showHint")) {
				Utils.logD("showHint true", _logging);
				showWelcome();
			}
		} else
			showWelcome();
		if (savedInstanceState != null) {
			Utils.logD("savedInstance true", _logging);
			if (savedInstanceState.getBoolean("showTip") && !_showWelcome) {
				Utils.logD("showHint true", _logging);
				showTip(getText(R.string.Tip1), 1);
			}
		} else
			if (!_showWelcome)
				showTip(getText(R.string.Tip1), 1);
		AppRater.app_launched(_cont);
		// if aSQLiteManager is started from other app with a name of a database
		// open it
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String _dbPath = extras.getString("Database");
			Intent i = new Intent(_cont, DBViewer.class);
			i.putExtra("db", _dbPath);
			startActivity(i);
		} else {

		}
	}

	/**
	 * Show a tip if not disabled
	 * @param tip
	 *          a CharSequence with the tip
	 * @param tipNo
	 *          a int with the tip number
	 */
	private void showTip(CharSequence tip, final int tipNo) {
		Utils.logD("Show Tip	" + 1, _logging);
		final boolean logging = Prefs.getLogging(_cont);
		Utils.logD("TipNo " + tipNo, logging);
		boolean showTip = AppSettings.showHint(_cont, tipNo); 
		if (showTip) {
			final Dialog dial = new Dialog(_cont);
			dial.setContentView(R.layout.tip);
			dial.setTitle(R.string.Tip);
			Button _btOK = (Button) dial.findViewById(R.id.OK);
			TextView tvTip = (TextView) dial.findViewById(R.id.TextViewTip);
			tvTip.setText(tip);
			_btOK.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					CheckBox _remember = (CheckBox) dial.findViewById(R.id.ShowTipAgain);
					_remember.setText(R.string.ShowTipAgain);
					Utils.logD("Show again " + _remember.isChecked(), logging);
					AppSettings.setHint(_cont, tipNo, _remember.isChecked());
					_showTip = false;
					dial.dismiss();
				}
			});
			_showTip = true;
			dial.show();
		}
	}

	/**
	 * Show the welcome screen if not turned off
	 */
	private void showWelcome() {
		if (AppSettings.showWelcome(_cont, WelcomeId)) {
			_showWelcome = true;
			final Dialog dial = new Dialog(this);
			dial.setContentView(R.layout.new_welcome);
			dial.setTitle(R.string.Welcome);
			Button _btOK = (Button) dial.findViewById(R.id.OK);
			_btOK.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					CheckBox _remember = (CheckBox) dial.findViewById(R.id.ShowAtStartUp);
					AppSettings.setWelcomeMsg(_cont, WelcomeId, _remember.isChecked());
					dial.dismiss();
					_showWelcome = false;
				}
			});
			dial.show();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		Utils.logD("OnClick", _logging);
		int key = v.getId();
		if (key == R.id.Open) {
			Utils.logD("Open", _logging);
			Intent i = null;
			String oPath = AppSettings.getString(_cont, "RecentOpenDBPath");
			Utils.logD("RecentOpenDBPath = " + oPath, _logging);
			if (oPath == null)
				oPath = Environment.getExternalStorageDirectory().toString();
			if (false) {
				//RootFilePicker here
			} else {
				Utils.logD("Calling Filepicker", _logging);
				i = new Intent(this, FilePicker.class);
			}
			i.putExtra("StartDir", oPath);
			i.putExtra("UseRoot", false);
			i.putExtra("GetDir", false);
			i.putExtra("UseBB", false);
			i.putExtra("OpenFile", true);
			String[] filetypes = {".sqlite", ".db"};
			i.putExtra("FileTypes", filetypes);
			try {
				startActivity(i);
			} catch (Exception e) {
				Utils.logE("Error in file picker", _logging);
				e.printStackTrace();
				Utils.showException(
						"Plase report this: " + e.getLocalizedMessage(),
						_cont);
			}
		} else if (key == R.id.About) {
			showAboutDialog();
		} else if (key == R.id.NewDB) {
			Utils.logD("Create new database", _logging);
			newDatabase();
		} else if (key == R.id.Recently) {
			// Retrieve recently opened files
			_recentFiles = AppSettings.getRecentFile(_cont); 
			if (_recentFiles == null) {
				Utils.showMessage(getText(R.string.Error).toString(),
						getText(R.string.NoRecentFiles).toString(), _cont);
			} else {
				// Special handling for databases in Dropbox (cut part of path)
				String recentTest = _recentFiles.replaceAll(
						"/mnt/sdcard/Android/data/com.dropbox.android/files/scratch",
						"[Dropbox]");
				String[] resently = recentTest.split(";");
				AlertDialog dial = new AlertDialog.Builder(this)
						.setTitle(getString(R.string.Recently))
						.setSingleChoiceItems(resently, 0, new ResentFileOnClickHandler())
						.create();
				dial.show();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle saveState) {
		super.onSaveInstanceState(saveState);
		Utils.logD("onSaveInstanceState", _logging);
		saveState.putBoolean("showHint", _showWelcome);
		saveState.putBoolean("showTip", _showTip);
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		//TODO Works but problems with screen refresh
		Utils.logD("Focus changed: " + hasFocus, _logging);
		if (hasFocus) {
			if (loadSettings) {
				_logging = Prefs.getLogging(this);
			}
		}
	}

	/**
	 * Open a the database clicked on from the recently opened file menu
	 */
	public class ResentFileOnClickHandler implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			String[] files = AppSettings.getRecentFile(_cont).split(";");
			String database = files[which];
			Utils.logD("Resent database " + database, _logging);
			// Utils.toastMsg(_cont, database);
			dialog.dismiss();
			//Test if database exists!
			 File f = new File(database);
			 if (f.exists()) {
					Intent i = new Intent(_cont, DBViewer.class);
					i.putExtra("db", database);
					try {
						startActivity(i);
					} catch (Exception e) {
						Utils.logE("Error in DBViewer", _logging);
						e.printStackTrace();
						Utils.showException(
								"Plase report this error with descriptions of how to generate it",
								_cont);
					}
			 } else {
				 Utils.logD("Resently database no longer found", _logging);
				 String recent = AppSettings.getRecentFile(_cont);
				 recent = recent.replace(";" + database + ";", ";"); //TODO what about ";"
				 Utils.showMessage(getText(R.string.Error).toString(),
						 getText(R.string.NoSuchDatabase).toString(), _cont);
			 }
		}
	}

	/**
	 * Display the about dialog
	 */
	private void showAboutDialog() {
		Dialog dial = new Dialog(this);
		dial.setTitle(getText(R.string.About) + " " + getText(R.string.hello));
		dial.setContentView(R.layout.about);
		dial.show();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Utils.logD("Main-activity got result from sub-activity", _logging);
		if (resultCode == Activity.RESULT_CANCELED) {
			Utils.logD(
							"WidgetActivity was cancelled or encountered an error. resultcode == result_cancelled",
							_logging);
			Utils.logD("WidgetActivity was cancelled - data =" + data, _logging);
		} else
			switch (requestCode) {
			case 1:
				String msg = data.getStringExtra("returnedData");
				Utils.showMessage("Returned file", msg, _cont);
				break;
			case 2:
				if (newDatabaseDialog != null) {
					String folderName = Utils.addSlashIfNotEnding(data
							.getStringExtra("RESULT"));
					final TextView newFolder = (TextView) newDatabaseDialog
							.findViewById(R.id.newFolder);
					newFolder.setText(folderName);
				}
				break;
			}
		Utils.logD("Main-activity got result from sub-activity", _logging);
	}

	/**
	 * Create a new empty database
	 */
	private void newDatabase() {
		//Get last new database location
		String lastPathToNewDB = AppSettings.getString(_cont, "RecentNewDBPath"); 
		// check for valid path
		if (!Utils.isPathAValidDirectory(lastPathToNewDB))
			lastPathToNewDB = null;
		Utils.logD("Loaded pathToNewDB: " + lastPathToNewDB, _logging);
		final String pathToNewDB;
		if (lastPathToNewDB != null && !lastPathToNewDB.equals(""))
			pathToNewDB = lastPathToNewDB;
		else 
			pathToNewDB = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/";
		//TODO path must be available!
		newDatabaseDialog = new Dialog(this);
		newDatabaseDialog.setContentView(R.layout.new_database);
		newDatabaseDialog.setTitle(getText(R.string.NewDBSDCard));
		final EditText edNewDB = (EditText) newDatabaseDialog
				.findViewById(R.id.databaseName);
		edNewDB.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_NORMAL);
		// TODO Change to filename only!!!
		edNewDB.setHint(getText(R.string.NewDBPath));
		final TextView newFolder = (TextView) newDatabaseDialog
				.findViewById(R.id.newFolder);
		newFolder.setText(pathToNewDB);
		final Button newFolderSelectButton = (Button) newDatabaseDialog
				.findViewById(R.id.newFolderSelectButton);
		newFolderSelectButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(_cont, FilePicker.class);
				i.putExtra("StartDir", pathToNewDB);
				i.putExtra("UseRoot", false);
				i.putExtra("GetDir", true);
				i.putExtra("UseBB", false);
				i.putExtra("OpenFile", false);
				try {
					startActivityForResult(i, 2);
				} catch (Exception e) {
					Utils.logE("Error in file picker", _logging);
					e.printStackTrace();
					Utils.showException(
							"Plase report this error with descriptions of how to generate it",
							_cont);
				}
			}
		});
		TextView tvMessage = (TextView) newDatabaseDialog
				.findViewById(R.id.newMessage);
		tvMessage.setText(getText(R.string.Database));
		newDatabaseDialog.show();
		final Button btnMOK = (Button) newDatabaseDialog.findViewById(R.id.btnMOK);
		btnMOK.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean error = false;
				String path;
				if (v == btnMOK) {
					if (Utils.isSDAvailable()) {
						String folderName = Utils.addSlashIfNotEnding(newFolder.getText()
								.toString());
						String fileName = edNewDB.getEditableText().toString();
						path = folderName + fileName;
						if (fileName.trim().equals("")) {
							Utils.showMessage((String) getText(R.string.Error),
									(String) getText(R.string.NoFileName), _cont);
						} else {
							if (!path.endsWith(".sqlite"))
								path += ".sqlite";
							try {
								// check to see if it exists
								File f = new File(path);
								if (f.exists()) {
									error = true;
									Utils.showMessage(getString(R.string.Error), path + " "
											+ getString(R.string.DatabaseExists), _cont);
								} else {
									String newDbPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf("/"));
									AppSettings.saveString(_cont, "RecentNewDBPath", newDbPath);
									SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path,
											null);
									db.close();
								}
							} catch (Exception e) {
								error = true;
								Utils.showMessage(getString(R.string.Error),
										getString(R.string.CouldNotCreate) + " " + path, _cont);
								e.printStackTrace();
							}
							// Ask before??
							if (!error) {
								Intent i = new Intent(_cont, DBViewer.class);
								i.putExtra("db", path);
								newDatabaseDialog.dismiss();
								newDatabaseDialog = null;
								try {
									startActivity(i);
								} catch (Exception e) {
									Utils.logE("Error in DBViewer", _logging);
									e.printStackTrace();
									Utils.showException(
											"Plase report this error with descriptions of how to generate it",
											_cont);
								}
							}
						}
					}
					Utils.logD("Path: " + edNewDB.getText().toString(), _logging);
				}
			}
		});
	}

	/*
	 * Creates the menu items
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_OPT, 0, R.string.Option).setIcon(
				R.drawable.ic_menu_preferences);
		menu.add(0, MENU_HLP, 0, R.string.Help).setIcon(R.drawable.ic_menu_help);
		menu.add(0, MENU_RESET, 0, R.string.Reset).setIcon(
				R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	/*
	 * (non-Javadoc) Handles item selections
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_OPT:
			startActivity(new Intent(this, Prefs.class));
			loadSettings = true;
			return true;
		case MENU_HLP:
			Intent i = new Intent(this, Help.class);
			startActivity(i);
			return true;
		case MENU_RESET:
			// Reset all settings to default
			resetAllPreferences();
			return false;
		}
		return false;
	}

	/**
	 * Clear all preferences. Preferences, different choices, recently opened and
	 * tip history
	 */
	private void resetAllPreferences() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {
            //Yes button clicked
						Utils.logD("Deleting preferences", _logging);
			  		// Clear different choices, recently opened, number of times used, day of
			  		// first use
			  		SharedPreferences settings = getSharedPreferences("aSQLiteManager",
			  				MODE_PRIVATE);
			  		SharedPreferences.Editor editor = settings.edit();
			  		editor.clear();
			  		editor.commit();
			  		// Clear preferences
			  		settings = getSharedPreferences("dk.andsen.asqlitemanager_preferences",
			  				MODE_PRIVATE);
			  		editor = settings.edit();
			  		editor.clear();
			  		editor.commit();
			  		// Clear tip history
			  		settings = _cont.getSharedPreferences("dk.andsen.asqlitemanager_tips",
			  				MODE_PRIVATE);
			  		editor = settings.edit();
			  		editor.clear();
			  		editor.commit();
		    	}
				})
		    .setNegativeButton("No", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		      	//No button clicked - do nothing
	      	}
	    	})
		    .show();
	}
}