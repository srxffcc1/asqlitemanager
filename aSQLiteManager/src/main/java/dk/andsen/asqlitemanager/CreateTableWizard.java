package dk.andsen.asqlitemanager;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.andsen.asqlitemanager.R;
import dk.andsen.utils.Utils;

public class CreateTableWizard extends Activity implements OnClickListener {

	Button newTabNewField;
	Button newTabCancel;
	Button newTabOk;
	EditText newTabTabName;
	private boolean _logging;
	private Context _cont;
	private LinearLayout newTabSV;
	ArrayList<String[]> _fieldDefs = new ArrayList<String[]>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_cont = this;
		Utils.logD("onCreate", _logging);
		_logging = Prefs.getLogging(this);
		setContentView(R.layout.tablewizard);
		// fldList contains the list of fields
		newTabNewField = (Button) this.findViewById(R.id.tabWizAddField);
		newTabNewField.setOnClickListener(this);
		newTabCancel = (Button) this.findViewById(R.id.tabWizCancel);
		newTabCancel.setOnClickListener(this);
		newTabOk = (Button) this.findViewById(R.id.tabWizOK);
		newTabOk.setOnClickListener(this);
		newTabSV = (LinearLayout) this.findViewById(R.id.tabWizSV);
		newTabTabName = (EditText) this.findViewById(R.id.tabWizTabName);
		this.setTitle(getText(R.string.CreateTable));
		if (savedInstanceState != null) {
			for (int i = 0; i < savedInstanceState.getInt("Fields"); i++) {
				String[] fieldDef = savedInstanceState.getStringArray("Field"+i);
				_fieldDefs.add(fieldDef);
				adFieldToUI(fieldDef);
			}
		}
	}

	public void onClick(View v) {
		if (v.getId() == View.NO_ID) {
			// The list of fields clicked
			String sid = (String) ((TextView) v).getHint();
			Utils.logD("Field clicked " + sid, _logging);
			Intent i = new Intent(this, CreateTableWizField.class);
			try {
				int id = new Integer(sid).intValue();
				i.putExtra("FieldNo", _fieldDefs.get(id));
				startActivityForResult(i, 55);
			} catch (Exception e) {
				Utils.logE("Error in CreateTableWizField", _logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of how to generate it", _cont);
			}
		} else if (v.getId() == newTabNewField.getId()) {
			Utils.logD("CreateTableWizard newTabNewField", _logging);
			Intent i = new Intent(this, CreateTableWizField.class);
			try {
				startActivityForResult(i, 55);
			} catch (Exception e) {
				Utils.logE("Error in CreateTableWizField", _logging);
				e.printStackTrace();
				Utils.showException("Plase report this error with descriptions of how to generate it", _cont);
			}
		} else if (v.getId() == newTabOk.getId()) {
			Utils.logD("CreateTableWizard OK", _logging);
						if (_fieldDefs.size() == 0 || newTabTabName.getText().toString().trim().equals("")) {
				Utils.showMessage(getText(R.string.Error).toString(),
						getText(R.string.MustEnterTableNameAndOneField).toString(), _cont);
			} else {
				if (buildAndExecuteSQL())
					finish();
			}
		} else if (v.getId() == newTabCancel.getId()) {
			Utils.logD("CreateTableWizard Cancel", _logging);
			finish();
		}
	}

	/**
	 * Build the sql needed to create the table and execute it
	 */
	private boolean buildAndExecuteSQL() {
		Utils.logD("Building CREATE TABLE SQL", _logging);
		String sql = "CREATE TABLE [" + newTabTabName.getText().toString() + "] (";
		for (int i = 0; i < _fieldDefs.size(); i++) {
			sql += "\n" + field2SQL(_fieldDefs.get(i));
			// More fields? add "," field separator
			if (_fieldDefs.size() - 1 > i)
				sql += ", ";
		}
		sql += ")";
		return aSQLiteManager.database.executeStatement(sql, _cont);
	}

	/**
	 * Build the sql needed for the field
	 * @param field a String[] with the field definitions
	 * @return the sql defining the field
	 */
	private String field2SQL(String[] field) {
		String sql = "";
		boolean strickt = false;
		boolean fNotNull = false;
		boolean fUniqeu = false;
		boolean fPK = false;
		boolean fDescr = false;
		boolean fAutoInc = false;
		String fName = field[0];
		String fType = field[1];
		if (fType.endsWith("(strict)")) {
			strickt = true;
			fType = fType.substring(0, fType.indexOf("(strict)"));
		}
		if (field[2].equals("true"))
			fNotNull = true;
		if (field[3].equals("true"))
			fUniqeu = true;
		if (field[4].equals("true"))
			fPK = true;
		if (field[5].equals("true"))
			fDescr = true;
		if (field[6].equals("true"))
			fAutoInc = true;
		sql += "[" + fName + "] " + fType;
		if (fPK) {
			sql += " PRIMARY KEY";
			if (fDescr)
				sql += " DESC";
			if (fAutoInc)
				sql += " AUTOINCREMENT";
		}
		if (fNotNull)
			sql += " NOT NULL";
		if (fUniqeu)
			sql += " UNIQUE";
		if (strickt) {
			if (fType.startsWith("INTEGER")) {
				sql += " check(typeof(" + fName
						+ ") = 'integer')";
			} else if (fType.startsWith("REAL")) {
				sql += " check(typeof(" + fName
						+ ") = 'real' " + "or typeof("
						+ fName + ") = 'integer')";
			} else if (fType.startsWith("TEXT")) {
				sql += " check(typeof(" + fName
						+ ") = 'text')";
			} else {
				// Ups
			}
		}
		String fDef = field[7];
		if (fDef.trim().length() > 0) {
			sql += " DEFAULT " + fDef;
		}
		String fFKTable = field[8];
		String fFKField = field[9];
		if (!fFKTable.equals("")) {
			sql += " REFERENCES " + fFKTable + " (" + fFKField + ")";
		}
		return sql;
	}

	/** 
	 * If the CreateTableWizField return a new field add it to the list of fields
	 * and to the UI
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case 1:
			Utils.logD("Field return from CreateTableWizField", _logging);
			String[] fieldDef = data.getStringArrayExtra("Field");
			if (fieldDef != null) {
				Utils.logD("fName " + fieldDef[0], _logging);
//				Utils.logD("fType " + fieldDef[1], _logging);
//				Utils.logD("fNotNull " + fieldDef[2], _logging);
//				Utils.logD("fUnique " + fieldDef[3], _logging);
//				Utils.logD("fPK " + fieldDef[4], _logging);
//				Utils.logD("fDesc " + fieldDef[5], _logging);
//				Utils.logD("fAutoInc " + fieldDef[6], _logging);
//				Utils.logD("fDef " + fieldDef[7], _logging);
//				Utils.logD("fFKTab " + fieldDef[8], _logging);
//				Utils.logD("fFKField " + fieldDef[9], _logging); 
				int edFieldNo = data.getIntExtra("EditField", -1);
				if (edFieldNo > -1) {
					Utils.logD("A editted field returned (no) " + edFieldNo, _logging);
					_fieldDefs.set(edFieldNo, fieldDef);
					rebuildList();
				} else {
					Utils.logD("New field recieved", _logging);
					_fieldDefs.add(fieldDef);
					rebuildList();
					//adFieldToUI(fieldDef);
				}
			}
			break;	
		default:
			Utils.logD("Unexpected return code from CreateTableWizField", _logging);
			break;
		}
	}

	/**
	 * Rebuild the list of fields with the updated fields 
	 */
	private void rebuildList() {
		newTabSV.removeAllViews();
		for (String[] fieldDef: _fieldDefs) {
			adFieldToUI(fieldDef);
		}
	}

	/**
	 * Add a new field to the list of fields
	 * @param fieldDef a String[] with the field definitions
	 */
	private void adFieldToUI(String[] fieldDef) {
		Utils.logD("Adding Field: " + fieldDef[0], _logging);
		LinearLayout ll = new LinearLayout(_cont);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		TextView tw = new TextView(_cont);
		//String[] field = _fieldDefs.get(_fieldDefs.size() - 1);
		String fieldDescr = fieldDef[0] + " " + fieldDef[1];
		fieldDescr = field2SQL(fieldDef);
		tw.setText(fieldDescr);
		tw.setHint("" + (_fieldDefs.size() - 1));
		tw.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		tw.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String sid = (String) ((TextView) v).getHint();
				int id = new Integer(sid).intValue();
				Utils.logD("Field clicked " + sid, _logging);
				Intent i = new Intent(_cont, CreateTableWizField.class);
				try {
					i.putExtra("Field", _fieldDefs.get(id));
					i.putExtra("FieldNo", id);
					startActivityForResult(i, 55);
				} catch (Exception e) {
					Utils.logE("Error in CreateTableWizField", _logging);
					e.printStackTrace();
					Utils.showException("Plase report this error with descriptions of how to generate it", _cont);
				}
			}
		});
		tw.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				if (v.getId() == View.NO_ID) {
					// The list of fields clicked
					String sid = (String) ((TextView) v).getHint();
					Utils.logD("Field longClicked " + sid, _logging);
					try {
						int id = new Integer(sid).intValue();
						removeField(id);
					} catch (Exception e) {
						Utils.logE("Error in CreateTableWizField", _logging);
						e.printStackTrace();
						Utils.showException(e.getMessage(), _cont);
					}
				}
				return true;
			}
		});
		ll.addView(tw);
		ll.setPadding(5, 5, 5, 5);
		newTabSV.addView(ll);
	}

	/**
	 * Remove a field specified by its number
	 * @param id
	 */
	protected void removeField(final int id) {
		String msg = getText(R.string.DeleteField) + _fieldDefs.get(id)[0] +"?";
		final Builder yesNoDialog = new AlertDialog.Builder(_cont);
		yesNoDialog.setTitle(getText(R.string.DeleteField) + "?");
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
				_fieldDefs.remove(id);
				rebuildList();
			}});
		yesNoDialog.show();
	}

	@Override
  protected void onSaveInstanceState(Bundle saveState) {
      super.onSaveInstanceState(saveState);
      Utils.logD("onSaveInstanceState", _logging);
      //save the created fields
      saveState.putInt("Fields", _fieldDefs.size());
      for (int i = 0; i < _fieldDefs.size(); i++) {
      	Utils.logD("Saving field: " + i, _logging);
      	saveState.putStringArray("Field" +i, _fieldDefs.get(i));
      }
  }
}