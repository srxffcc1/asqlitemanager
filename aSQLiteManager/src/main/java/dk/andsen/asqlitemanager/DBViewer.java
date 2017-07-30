/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * Show tables, views, and index from the current database
 * 
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import dk.andsen.filepicker.FilePicker;
import dk.andsen.types.Types;
import dk.andsen.utils.MyDBArrayAdapter;
import dk.andsen.utils.Recently;
import dk.andsen.utils.Utils;

public class DBViewer extends Activity implements OnClickListener {
	private String _dbPath;
	//private Database database = null;
//	private String[] tables;
//	private String[] views;
	private String[] indexes;
	private ListView list;
	private LinearLayout query;
	private String[] toList;
	private Context _cont;
	private boolean _update = false;
	private final int MENU_EXPORT = 0;
	private final int MENU_RESTORE = 1;
	private final int MENU_SQL = 2;
	private final int MENU_INFO = 3;
	private final int MENU_CREATETABLE = 4;
	private final int FILEPICKER_SQL = 1;
	protected boolean editingDatabase;
	protected String databasePath;
	private int _dialogClicked;
	private boolean _logging = false;
	private boolean newFeatures = true;
	private boolean _showTip = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dbviewer);
		_logging = Prefs.getLogging(this);
		TextView tvDB = (TextView)this.findViewById(R.id.DatabaseToView);
		Button bTab = (Button) this.findViewById(R.id.Tables);
		Button bVie = (Button) this.findViewById(R.id.Views);
		Button bInd = (Button) this.findViewById(R.id.Index);
		Button bQue = (Button) this.findViewById(R.id.Query);
		query = (LinearLayout) this.findViewById(R.id.QueryFrame);
		query.setVisibility(View.GONE);
		bTab.setOnClickListener(this);
		bVie.setOnClickListener(this);
		bInd.setOnClickListener(this);
		bQue.setOnClickListener(this);
		_cont = this;
		_logging = Prefs.getLogging(this);
		Utils.logD("DBViewer onCreate", _logging);
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			_dbPath = extras.getString("db");
			tvDB.setText(getText(R.string.Database) + ": " + _dbPath);
			Utils.logD("Opening database " + _dbPath, _logging);
			if ((new File(_dbPath).canRead())) {
				// it is a readable file no root access needed
				//TODO use database from sSQLiteManager!!
				//database = new Database(_dbPath, _cont);
				aSQLiteManager.database = new Database(_dbPath, _cont);
				//_SQLiteDb = SQLiteDatabase.openDatabase(_dbPath, null, SQLiteDatabase.OPEN_READWRITE);
				if (!aSQLiteManager.database.isDatabase) {
					Utils.logD("User has opened something that is not a database!", _logging);
					Utils.showMessage(getText(R.string.Error).toString(),
							_dbPath + " " + getText(R.string.IsNotADatabase).toString(), _cont);
				} else {
					// it is a database and it is opened
					// Test if database is working and not corrupt
					try {
						aSQLiteManager.database.getTables();
						// Store recently opened files
						if (Prefs.getEnableFK(_cont)) {
							aSQLiteManager.database.FKOn();
						}
						int noOfFiles = Prefs.getNoOfFiles(_cont);
						SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
						String files = settings.getString("Recently", null);
						files = Recently.updateList(files, _dbPath, noOfFiles);
						Editor edt = settings.edit();
						edt.putString("Recently", files);
						edt.commit();
						indexes = aSQLiteManager.database.getIndex();
						list = (ListView) findViewById(R.id.LVList);
						buildList("Tables");
					} catch (Exception e) {
						Utils.showException(e.getLocalizedMessage(), _cont);
					}
				}
			} else {
				// it is not a readable file root access needed
				//TODO implement aShels way of opening system databases
				Utils.showMessage(getText(R.string.Error).toString(), "No editing of system databases yet", _cont);
				//openRootFile(_dbPath);
			}
		}
		Utils.logD("Show Tip	" + 3, _logging);
		if (savedInstanceState != null) {
			Utils.logD("savedInstance true", _logging);
			if (savedInstanceState.getBoolean("showTip")) {
				Utils.logD("showHint true", _logging);
				showTip(getText(R.string.Tip3), 3);
			}
		} else {
			showTip(getText(R.string.Tip3), 3);
		}
	}
	
	@Override
	protected void onDestroy() {
		Utils.logD("DBViewer onDestroy", _logging);
		if (aSQLiteManager.database != null)
			aSQLiteManager.database.close();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Utils.logD("DBViewer onPause", _logging);
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Utils.logD("DBViewer onRestart", _logging);
		if (aSQLiteManager.database == null)
			aSQLiteManager.database = new Database(_dbPath, _cont);
	}

	@Override
  protected void onSaveInstanceState(Bundle saveState) {
      super.onSaveInstanceState(saveState);
      Utils.logD("onSaveInstanceState", _logging);
      saveState.putBoolean("showTip", _showTip );
  }
	
	/**
	 * Build / rebuild the lists with tables, views and indexes
	 * @param type
	 */
  //TODO change type to private static final int DISPMODE_INDEX = 0 ...
	// and change to case
	private void buildList(final String type) {  
		if (type.equals("Clear"))
			toList = new String [] {};
		else if (type.equals("Index"))
			toList = aSQLiteManager.database.getIndex();
		else if (type.equals("Views")) 
			toList = aSQLiteManager.database.getViews();
		else 
			toList = aSQLiteManager.database.getTables();
		int recs = toList.length;
		List<String> ls = new ArrayList<String>();
		for (int i = 0; i < recs; i++) {
			ls.add(toList[i]);
		}
		MyDBArrayAdapter mlist = new MyDBArrayAdapter(this, ls);
		if (list != null) {
			list.setAdapter(mlist); 
			list.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position,
						long id) {
					// Do something with the table / view / index clicked on
					selectRecord(type, position);
				}
			});
			list.setOnItemLongClickListener(new OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> parent, View v,
						int position, long arg3) {
					Utils.logD("Long click on list", _logging);
					dropSelected(type, position);
					return false;
				}
			});
		} else {
			Utils.showMessage(_cont.getText(R.string.Error).toString(),
					_cont.getText(R.string.StrangeErr).toString(), _cont);
		}
	}

	private void dropSelected(final String type, int position) {
		String name;
		final String sql;
		String msg = "";
		name = toList[position];
		//Utils.logD("Handle: " + type + " " + name);
		if (type.equals("Index")) {
			if (indexes[position].startsWith("sqlite_autoindex_")) {
				sql = "";
				msg = getText(R.string.CannotDeleteAutoIndex) + " " + name;
			}
			else {
				sql = "drop index [" + name + "]";
				msg = getText(R.string.DeleteIndex) + " " + name +"?";
			}
		}
		else if (type.equals("Views")) {
			sql = "drop view [" + name + "]";
			msg = getText(R.string.DeleteView) + " "  + name +"?";
		}
		else if (type.equals("Tables")){
			if (name.equalsIgnoreCase("sqlite_master")
					|| name.equalsIgnoreCase("sqlite_sequence")
					|| name.equalsIgnoreCase("android_metadata")) {
				sql = "";
				msg = getText(R.string.CannotDeleteSysTable) + name;
			} else {
				sql = "drop table [" + name + "]";
				msg = getText(R.string.DeleteTable) + " "  + name +"?";
			}
		}
		else {
			sql = "";
			msg = "This is not happening ;-)";
		}
		Utils.logD(msg, _logging);
		if (sql.equals("")) {
			Utils.showMessage(getText(R.string.Error).toString(), msg, _cont);
		} else {
			final Builder yesNoDialog = new AlertDialog.Builder(_cont);
			yesNoDialog.setTitle(getText(R.string.DropItem));
			yesNoDialog.setMessage(msg);
			yesNoDialog.setNegativeButton(getText(R.string.No),
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing
				}});
			yesNoDialog.setPositiveButton(getText(R.string.Yes),
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					// Delete it
					aSQLiteManager.database.executeStatement(sql, _cont);
					buildList(type);
				}});
			yesNoDialog.show();
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
		Utils.logD("Show Tip	" + tipNo, _logging);
		//final boolean logging = Prefs.getLogging(_cont);
		Utils.logD("TipNo " + tipNo, _logging);
		SharedPreferences prefs = _cont.getSharedPreferences(
				"dk.andsen.asqlitemanager_tips", Context.MODE_PRIVATE);
		boolean showTip = prefs.getBoolean("TipNo" + tipNo, true);
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
					SharedPreferences prefs = _cont.getSharedPreferences(
							"dk.andsen.asqlitemanager_tips", Context.MODE_PRIVATE);
					Editor edt = prefs.edit();
					Utils.logD("Show again " + _remember.isChecked(), _logging);
					edt.putBoolean("TipNo" + tipNo, _remember.isChecked());
					edt.commit();
					_showTip = false;
					dial.dismiss();
				}
			});
			_showTip = true;
			dial.show();
		}
	}

	/**
	 * Handle the the item clicked on
	 * @param type the type of list
	 * @param position Number of item in the list
	 */
	protected void selectRecord(String type, int position) {
		String name;
		name = toList[position];
		//Utils.logD("Handle: " + type + " " + name);
		if (type.equals("Index")) {
			String indexDef = "";
			if (indexes[position].startsWith("sqlite_autoindex_"))  //2.5 null pointer ex. here
				indexDef = (String) this.getText(R.string.AutoIndex);
			else
				indexDef = aSQLiteManager.database.getIndexDef(indexes[position]);
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(indexDef);
			Utils.showMessage(this.getString(R.string.Message), indexDef, _cont);
			Utils.toastMsg(_cont, this.getString(R.string.IndexDefCopied));
		}
		else if (type.equals("Views")) {
			Intent i = new Intent(this, TableViewer.class);
			i.putExtra("db", _dbPath);
			i.putExtra("Table", name);
			i.putExtra("type", Types.VIEW);
			try {
				startActivity(i);
			} catch (Exception e) {
				Utils.logE("Error in TableViewer showing a view)", _logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of hov to generate it", _cont);
			}
		}
		else if (type.equals("Tables")){
			Intent i = new Intent(this, TableViewer.class);
			i.putExtra("db", _dbPath);
			i.putExtra("Table", name);
			i.putExtra("type", Types.TABLE);
			try {
				startActivity(i);
			} catch (Exception e) {
				Utils.logE("Error in TableViewer showing a table)", _logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of hov to generate it", _cont);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		if (aSQLiteManager.database == null || !aSQLiteManager.database.isDatabase) {
			Utils.logD("User trying to do things with something that is not a database!", _logging);
			Utils.showMessage(getText(R.string.Error).toString(),
					_dbPath + " " + getText(R.string.IsNotADatabase).toString(), _cont);
			return;
		}
		Utils.logD("DBViewer OnCLick", _logging);
		int key = v.getId();
		if (key == R.id.Tables) {
			buildList("Tables");
		} else if (key == R.id.Views) {
			buildList("Views");
		} else if (key == R.id.Index) {
			buildList("Index");
		} else if (key == R.id.Query) {
			_update = true;
			Intent i = null;
			i = new Intent(this, QueryViewer.class);
			i.putExtra("db", _dbPath);
			try {
				startActivity(i);
			} catch (Exception e) {
				Utils.logE("Error in QueryViewer", _logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of how to generate it", _cont);
			}
		} 
	}
	
	/* (non-Javadoc)
	 * Update the lists to ensure new tables (created in query mode) and indexes
	 * are retrieved
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	public void onWindowFocusChanged(boolean hasFocus) {
		Utils.logD("DBViewer onWindowFocusChanged: " + hasFocus, _logging);
		if(hasFocus & _update) {
			_update = false;
//			tables = _db.getTables();
//			views = _db.getViews();
			indexes = aSQLiteManager.database.getIndex();
			buildList("Tables");
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_EXPORT, 0, getText(R.string.Export));
		menu.add(0, MENU_RESTORE, 0, getText(R.string.Restore));
		// Open files with SQL scripts, execute one or all commands 		
		menu.add(0, MENU_SQL, 0, getText(R.string.OpenSQL));
		menu.add(0, MENU_INFO, 0, getText(R.string.DBInfo));
		if (newFeatures )
			menu.add(0, MENU_CREATETABLE, 0, getText(R.string.CreateTable));
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!aSQLiteManager.database.isDatabase) {
			Utils.logD("User trying to do things with something that is not a database!", _logging);
			Utils.showMessage(getText(R.string.Error).toString(),
					_dbPath + " " + getText(R.string.IsNotADatabase).toString(), _cont);
			return false;  
		}
		switch (item.getItemId()) {
		case MENU_EXPORT:
			_dialogClicked = MENU_EXPORT; 
			showDialog(MENU_EXPORT);
			break;
		case MENU_RESTORE:
			_dialogClicked = MENU_RESTORE;
			showDialog(MENU_RESTORE);
			break;
		case MENU_SQL:
			selectSQLFile();
			break;
		case MENU_INFO:
			String versionStr = aSQLiteManager.database.getVersionInfo();
			Utils.showMessage(getText(R.string.DatabaseInfo).toString(), versionStr, _cont);
			break;
		case MENU_CREATETABLE:
			Intent i = new Intent(this, CreateTableWizard.class);
			try {
				_update = true;
				startActivity(i);
			} catch (Exception e) {
				Utils.logE("Error in CreateTableWizard", _logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of how to generate it", _cont);
			}
			_update = true;
			break;
		}
		return false;
	}
	
//	/**
//	 * Open a create table wizard where the user can define the table
//	 * by adding fields
//	 * 
//	 */
//	private void createTableWizard() {
//		_inWizard = true;
//		Button newTabNewField;
//		Button newTabCancel;
//		Button newTabOk;
//		final EditText newTabTabName;
//		// fldList contains the list of fields
//		final List<String> fldList = new ArrayList<String>();
//		// fkList contains the list of foreign keys
//		final List<String> fkList = new ArrayList<String>();
//		final LinearLayout newTabSV;
//		final Dialog createTab = new Dialog(_cont);
//		createTab.setContentView(R.layout.create_table);
//		createTab.setTitle(getText(R.string.CreateTable));
//		newTabNewField = (Button) createTab.findViewById(R.id.newTabAddField);
//		newTabCancel = (Button) createTab.findViewById(R.id.newTabCancel);
//		newTabOk = (Button) createTab.findViewById(R.id.newTabOK);
//		newTabSV = (LinearLayout) createTab.findViewById(R.id.newTabSV);
//		newTabTabName = (EditText) createTab.findViewById(R.id.newTabTabName);
//		createTab.setTitle(getText(R.string.CreateTable));
//		newTabNewField.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				newField();
//			}
//			private void newField() {
//				Button newFieldCancel;
//				Button newFieldOk;
//				final EditText fName;
//				final EditText fDef;
//				final CheckBox fNotNull;
//				final CheckBox fPK;
//				final CheckBox fUnique;
//				final CheckBox fAutoInc;
//				final CheckBox fDesc;
//				final EditText fFKTab;
//				final EditText fFKFie;
//				final Spinner fSPType;
//				final Dialog createField = new Dialog(_cont);
//				// data types to be selectable from create field
//				final String[] type = { 
//						"INTEGER",
//						"REAL",
//						"TEXT",
//						"BLOB",
//						"DATE",
//						"TIMESTAMP",
//						"TIME",
//						"INTEGER (strict)",
//						"REAL (strict)",
//						"TEXT (strict)"
//						};
//				ArrayAdapter<String> adapterType = new ArrayAdapter<String>(_cont,
//						android.R.layout.simple_spinner_item, type);
//				createField.setContentView(R.layout.create_field);
//				createField.setTitle(getText(R.string.CreateField));
//				newFieldCancel = (Button) createField.findViewById(R.id.newFieldCancel);
//				newFieldOk = (Button) createField.findViewById(R.id.newFieldOK);
//				fName = (EditText) createField.findViewById(R.id.newFldName);
//				fNotNull = (CheckBox) createField.findViewById(R.id.newFldNull);
//				fPK = (CheckBox) createField.findViewById(R.id.newFldPK);
//				fUnique = (CheckBox) createField.findViewById(R.id.newFldUnique);
//				fAutoInc = (CheckBox) createField.findViewById(R.id.newFldAutoInc);
//				fDesc = (CheckBox) createField.findViewById(R.id.newFldDesc);
//				fDef = (EditText) createField.findViewById(R.id.newFldDef);
//				fFKTab = (EditText) createField.findViewById(R.id.newFldFKTab);
//				fFKFie = (EditText) createField.findViewById(R.id.newFldFKFie);
//				fSPType = (Spinner) createField.findViewById(R.id.newFldSpType);
//				adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//				fSPType.setAdapter(adapterType);
//				newFieldCancel.setOnClickListener(new OnClickListener() {
//					public void onClick(View v) {
//						createField.dismiss();
//					}
//				});
//				fPK.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//						Utils.logD("Turning autoinc on / off", _logging);
//						if (isChecked) {
//							//Only turn AutoInc if field is INTEGER
//							int iType = fSPType.getSelectedItemPosition();
//							String stype = type[iType];
//							if (stype.startsWith("INTEGER"))
//								fAutoInc.setEnabled(true);
//							fDesc.setEnabled(true);
//						} else {
//							fAutoInc.setEnabled(false);
//							fDesc.setEnabled(false);
//						}
//					}
//				});
//				// OK clicked on new field
//				newFieldOk.setOnClickListener(new OnClickListener() {
//					public void onClick(View v) {
//						int iType = fSPType.getSelectedItemPosition();
//						String stype = type[iType];
//						Utils.logD("Field type = " + stype, _logging);
//						//Check for name and type not null, FK field and table both set or both unset
//						boolean fkCheck = true;
//						if ((fFKFie.getText().toString().trim().equals("") && !fFKTab.getText().toString().trim().equals(""))
//								|| (!fFKFie.getText().toString().trim().equals("") && fFKTab.getText().toString().trim().equals(""))) {
//							fkCheck = false;
//						}
//						if (!fName.getEditableText().toString().trim().equals("")  && (fkCheck) 
//								&& (!(fAutoInc.isChecked() && fDesc.isChecked()))) {
//							boolean forceType = false;
//							// Build the sql for the field
//							String fld = "[";
//							String fk = "";
//							fld += fName.getEditableText().toString();
//							// shod it use forced types?
//							if (stype.endsWith("(strict)")) {
//								forceType = true;
//								fld += "] " + stype.substring(0, stype.indexOf(" "));
//							} else {
//								fld += "] " + stype;
//							}
//							if (fPK.isChecked()) {
//								fld += " PRIMARY KEY";
//								// Sort descending?
//								if (fDesc.isChecked()) {
//									fld += " DESC";
//								} else {
//									fld += " ASC";
//								}
//								// Add order here ASC / DESC
//								if (fAutoInc.isChecked()) {
//									fld += " AUTOINCREMENT";
//								}
//							}
//							if (fNotNull.isChecked()) 
//								fld += " NOT NULL";
//							if (fUnique.isChecked()) 
//								fld += " UNIQUE";
//							// Handle forced type for INTEGER, REAL and TEXT fields
//							if (forceType) {
//								if (stype.startsWith("INTEGER")) {
//									fld += " check(typeof(" + fName.getEditableText().toString() +") = 'integer')";
//								} else if (stype.startsWith("REAL")) {
//									fld += " check(typeof(" + fName.getEditableText().toString() +") = 'real' " +
//											"or typeof(" + fName.getEditableText().toString() +") = 'integer')";
//								} else if (stype.startsWith("TEXT")) {
//									fld += " typeof(" + fName.getEditableText().toString() +") = 'text')";
//								} else {
//									//Ups
//								}
//							}
//							if (!fDef.getEditableText().toString().equals("")) {
//								fld += " DEFAULT " + fDef.getEditableText().toString();
//							}
//							if (!fFKFie.getEditableText().toString().trim().equals("") &&
//									!fFKTab.getEditableText().toString().trim().equals("")) {
//								//Foreign key constraints
//								fk += " FOREIGN KEY(["
//									+ fName.getEditableText().toString() + "]) REFERENCES ["
//									+ fFKTab.getEditableText().toString() + "]([" 
//									+ fFKFie.getEditableText().toString() + "])";
//								Utils.logD("FK " + fk , _logging);
//							}
//							// Create a LiniearLayout with the new field definition
//							LinearLayout ll = new LinearLayout(_cont);
//							ll.setOrientation(LinearLayout.HORIZONTAL);
//							TextView tw = new TextView(_cont);
//							tw.setText(fld);
//							ll.addView(tw);
//							ll.setPadding(5, 5, 5, 5);
//							// Add it to the LinearLayout
//							newTabSV.addView(ll);
//							// also save it in the field List
//							fldList.add(fld);
//							// If a foreign key is defined save that too
//							if (!fk.trim().equals("")) {
//								LinearLayout llfk = new LinearLayout(_cont);
//								llfk.setOrientation(LinearLayout.HORIZONTAL);
//								TextView twfk = new TextView(_cont);
//								twfk.setText(fk);
//								llfk.addView(twfk);
//								llfk.setPadding(5, 5, 5, 5);
//								newTabSV.addView(llfk);
//							}
//							// And to the foreign key List
//							if (!fk.trim().equals(""))
//								fkList.add(fk);
//							createField.dismiss();
//						} else {
//							String msg = "";
//							if (fName.getEditableText().toString().trim().equals("")) {
//								Utils.logD("No field name", _logging);
//								msg = getText(R.string.MustEnterFieldName).toString();
//							}
//							if ((fAutoInc.isChecked() && fDesc.isChecked())) {
//								Utils.logD("DESC & AutoInc", _logging);
//								getText(R.string.DescAutoIncError).toString();
//								msg += "\n" + getText(R.string.DescAutoIncError).toString();
//							}
//							if (!fkCheck) {
//								Utils.logD("FK check fail", _logging);
//								getText(R.string.FKDefError).toString();
//								msg += "\n" + getText(R.string.FKDefError).toString();
//							}
//							Utils.showMessage(getText(R.string.Error).toString(),
//									msg, _cont);
//						}
//					}
//				});
//				createField.show();
//			}
//		});
//		newTabCancel.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				_inWizard = false;
//				createTab.dismiss();
//			}
//		});
//		newTabOk.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				// build create table SQL if enough informations
//				// field must be specified
//				if (!(newTabTabName.getEditableText().toString().equals(""))
//						&& (fldList.size() > 0)) {
//					String sql = "create table ["
//						+ newTabTabName.getEditableText().toString()
//						+ "] (";
//					Iterator<String> it = fldList.iterator();
//					while (it.hasNext()) {
//						sql += it.next();
//						if (it.hasNext())
//							 sql += ", ";
//					}
//					if (fkList.size() > 0) {
//						sql += " ,";
//						it = fkList.iterator();
//						while (it.hasNext()) {
//							sql += it.next();
//							if (it.hasNext())
//								 sql += ", ";
//							else
//								sql += ")";
//						}
//					} else
//						sql += ")";
//					//Utils.showMessage("SQL", sql, _cont);
//					//Execute sql
//					Utils.logD("Executing " + sql, _logging);
//					database.executeStatement(sql, _cont);
//					_inWizard = false;
//					createTab.dismiss();
//					//Refresh list of tables
//					buildList("Tables");   
//				} else {
//					// not enough inf.
//					Utils.showMessage(getText(R.string.Error).toString(),
//							getText(R.string.MustEnterTableNameAndOneField).toString(), _cont);
//				}
//			}
//		}); 
//		createTab.show();
//	}

	protected Dialog onCreateDialog(int id) 
	{
		switch (id) {
		case MENU_EXPORT:
			//Utils.logD("Creating MENU_EXPORT");
			Dialog export = new AlertDialog.Builder(this)
					.setTitle(getText(R.string.Export))
					.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler())
					.setNegativeButton(getText(R.string.Cancel), null)
					.create();
			return export;
		case MENU_RESTORE:
			//Utils.logD("Creating MENU_RESTORE");
			Dialog restore = new AlertDialog.Builder(this)
					.setTitle(getText(R.string.Restore))
					.setMessage(getString(R.string.Patience))
					.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler())
					.setNegativeButton(getText(R.string.Cancel), null)
					.create();
			return restore;
		case MENU_SQL:
			Dialog sql = new AlertDialog.Builder(this).setTitle(getText(R.string.OpenSQL))
			.setPositiveButton(getText(R.string.OK), new DialogButtonClickHandler())
			.setNegativeButton(getText(R.string.Cancel), null)
			.create();
			return sql;
			
		}
		return null;
	}

	/**
	 * Click handler for the Export and Restore menus  
	 * @author Andsen
	 */
	public class DialogButtonClickHandler implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int clicked) {
			Utils.logD("Dialog: " + dialog.getClass().getName(), _logging);
			switch (clicked) {
			// OK button clicked
			case DialogInterface.BUTTON_POSITIVE:
				//Utils.logD("OK pressed");
				// Find the menu from which the OK button was clicked
				switch (_dialogClicked) {
				case MENU_EXPORT:
					aSQLiteManager.database.exportDatabase();
					Utils.toastMsg(_cont, getString(R.string.DataBaseExported));
					break;
				case MENU_RESTORE:
					aSQLiteManager.database.restoreDatabase();
					Utils.toastMsg(_cont, getString(R.string.DataBaseRestored));
					break;
				case MENU_SQL:
					selectSQLFile();
					break;
				}
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				finish();
				break;
			}
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Utils.logD("requestCode = " + requestCode, _logging);
		// requestCode 1 = from file picker
		if (requestCode == FILEPICKER_SQL && data != null) {
			String sqlFile = data.getStringExtra("RESULT");
			Utils.logD("SQL file selected " + sqlFile, _logging);
			File file = new File(sqlFile);
			String oPath = sqlFile.substring(0, file.getAbsolutePath().lastIndexOf("/")); 
			if (file.getName().toLowerCase(Locale.US).endsWith("sql")) {
				AppSettings.saveString(_cont, "RecentOpenSQLPath", oPath);
				openSQL(file);
			} else {
				Utils.showMessage("Error", "Not a SQL file", _cont);
			}
		} else if (resultCode == RESULT_CANCELED) {
			// no file selected
		}
	}

	private void selectSQLFile() {
		Utils.logD("Open SQL file", _logging);
		String extStore = AppSettings.getString(_cont, "RecentOpenSQLPath");
		if (extStore == null)
			extStore = Environment.getExternalStorageDirectory().toString();
		Utils.logD("Calling Filepicker", _logging);
		Intent i = new Intent(_cont, FilePicker.class);
		i.putExtra("StartDir", extStore);
		i.putExtra("UseRoot", false);
		i.putExtra("GetDir", false);
		i.putExtra("UseBB", false);
		i.putExtra("OpenFile", false);
		String[] filetypes = { ".sql"};
		i.putExtra("FileTypes", filetypes);
		i.putExtra("SQLtype", true);
		i.putExtra("dbPath", _dbPath);
		Utils.logD("Find the SQL file to open", _logging);
		startActivityForResult(i, FILEPICKER_SQL);
	}
	
	/**
	 * Open a sql file in the script viewer / runner
	 * @param file Path to the file
	 */
	private void openSQL(File file) {
		Utils.logD("SQL file", _logging);
		// Look in last location
		final SharedPreferences settings = getSharedPreferences("aSQLiteManager",
				MODE_PRIVATE);
		settings.getString("RecentOpenSQLPath", _dbPath);
		Intent iSqlViewer = new Intent(_cont, SQLViewer.class);
		iSqlViewer.putExtra("script", "" + file.getAbsolutePath());
		iSqlViewer.putExtra("db", _dbPath);
		startActivity(iSqlViewer);
	}
}