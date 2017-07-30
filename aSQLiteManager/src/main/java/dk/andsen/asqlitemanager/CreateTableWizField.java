package dk.andsen.asqlitemanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.CompoundButton.OnCheckedChangeListener;
import dk.andsen.asqlitemanager.R;
import dk.andsen.utils.Utils;

public class CreateTableWizField extends Activity implements OnClickListener {

	private EditText fName;
	private EditText fDef;
	private CheckBox fNotNull;
	private CheckBox fPK;
	private CheckBox fUnique;
	private CheckBox fAutoInc;
	private CheckBox fDesc;
	private EditText fFKTab;
	private EditText fFKField;
	private Spinner fSPType;
	private Button newFieldCancel;
	private Button newFieldOk;
	private Context _cont;
	private boolean _logging = false;
	private boolean _editing = false;
	private String[] _edField;
	private int _edFieldNo = 0;

	final String[] type = { 
			"INTEGER",
			"REAL",
			"TEXT",
			"BLOB",
			"DATE",
			"TIMESTAMP",
			"TIME",
			"INTEGER (strict)",
			"REAL (strict)",
			"TEXT (strict)"
			};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_cont = this;
		_logging = Prefs.getLogging(_cont);
		Utils.logD("onCreate", _logging);
		setContentView(R.layout.tablewizfield);
		setUpUI();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			// we are editing an existing field
			Utils.logD("We are editing!", _logging);
			_editing  = true;
			_edField = extras.getStringArray("Field"); 
			_edFieldNo  = extras.getInt("FieldNo");
			dataToUI();
			Utils.logD("Editing a field: " + _edFieldNo, _logging);
			Utils.logD("Field: " + _edField[0], _logging);
			Utils.logD("Type: " + _edField[1], _logging);
			Utils.logD("NotNull: " + _edField[2], _logging);
			Utils.logD("Unique: " + _edField[3], _logging);
			Utils.logD("PK: " + _edField[4], _logging);
			Utils.logD("Desc: " + _edField[5], _logging);
			Utils.logD("AutoInc: " + _edField[6], _logging);
			Utils.logD("Def: " + _edField[7], _logging);
			Utils.logD("FKTable: " + _edField[8], _logging);
			Utils.logD("FKField: " + _edField[9], _logging);
			

		} 
	}
		
	private void dataToUI() {
		fName.setText(_edField[0]);
		int i = 0;
		Utils.logD("Looking for " + _edField[1], _logging);
		for (String aType: type) {
			if (aType.equals(_edField[1])) {
				Utils.logD("Found " + _edField[1], _logging);
				fSPType.setSelection(i);
				break;
			}
			i++;
		}
		if (_edField[2].equals("true"))
			fNotNull.setChecked(true);
		if (_edField[3].equals("true"))
			fUnique.setChecked(true);
		if (_edField[4].equals("true"))
			fPK.setChecked(true);
		if (_edField[5].equals("true"))
			fDesc.setChecked(true);
		if (_edField[6].equals("true"))
			fAutoInc.setChecked(true);
		fDef.setText(_edField[7]);
		fFKTab.setText(_edField[8]);
		fFKField.setText(_edField[9]);
	}

	private void setUpUI() {
		fName = (EditText) findViewById(R.id.tabWizFieldName);
		fDef = (EditText) findViewById(R.id.tabWizFieldDef);
		fFKTab = (EditText) findViewById(R.id.tabWizFieldFKTab);
		fFKField = (EditText) findViewById(R.id.tabWizFieldFKFie);
		fNotNull = (CheckBox) findViewById(R.id.tabWizFieldNull);
		fPK = (CheckBox) findViewById(R.id.tabWizFieldPK);
		fPK.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					//Only turn AutoInc if field is INTEGER
					String stype = fSPType.getSelectedItem().toString();
					if (stype.startsWith("INTEGER"))
						fAutoInc.setEnabled(true);
					fDesc.setEnabled(true);
				} else {
					fAutoInc.setChecked(false);
					fAutoInc.setEnabled(false);
					fDesc.setChecked(false);
					fDesc.setEnabled(false);
				}
			}
		});
		fUnique = (CheckBox) findViewById(R.id.tabWizFieldUnique);
		fAutoInc = (CheckBox) findViewById(R.id.tabWizFieldAutoInc);
		fDesc = (CheckBox) findViewById(R.id.tabWizFieldDesc);
		fSPType = (Spinner) findViewById(R.id.tabWizFieldType);
		newFieldCancel = (Button) findViewById(R.id.tabWizFieldCancel); 
		newFieldCancel.setOnClickListener(this);
		newFieldOk = (Button) findViewById(R.id.tabWizFieldOK);
		newFieldOk.setOnClickListener(this);
		ArrayAdapter<String> adapterType = new ArrayAdapter<String>(_cont,
				android.R.layout.simple_spinner_item, type);
		adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fSPType.setAdapter(adapterType);
	}

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tabWizFieldOK) {
			String msg = "";
			if (fName.getEditableText().toString().trim().equals("")) {
				Utils.logD("No field name", _logging);
				msg = getText(R.string.MustEnterFieldName).toString();
			}
			if ((fAutoInc.isChecked() && fDesc.isChecked())) {
				Utils.logD("DESC & AutoInc", _logging);
				getText(R.string.DescAutoIncError).toString();
				if (msg.length() > 0)
					msg += "\n";
				msg += getText(R.string.DescAutoIncError).toString();
			}
			if (fFKTab.getText().toString().trim().equals("") != 
					fFKField.getText().toString().trim().equals("")) {
				Utils.logD("FK check fail", _logging);
				getText(R.string.FKDefError).toString();
				if (msg.length() > 0)
					msg += "\n";
				msg += getText(R.string.FKDefError).toString();
			}
			if (msg.length() > 0) {
				Utils.showMessage(getText(R.string.Error).toString(),
						msg, _cont);
			} else {
				// save all field data and return to create table wizard
				Intent in = new Intent();
				String[] field = new String[10];
				field[0] = fName.getText().toString();
				field[1] = fSPType.getSelectedItem().toString();
				field[2] = "" + fNotNull.isChecked();
				field[3] = "" + fUnique.isChecked();
				field[4] = "" + fPK.isChecked();
				field[5] = "" + fDesc.isChecked();
				field[6] = "" + fAutoInc.isChecked();
				field[7] = fDef.getText().toString();
				field[8] = fFKTab.getText().toString();
				field[9] = fFKField.getText().toString(); 
				in.putExtra("Field", field);
				if (_editing)
					in.putExtra("EditField", _edFieldNo);
	      setResult(1,in);
	      finish();
			}
		} else if (id == R.id.tabWizFieldCancel) {
			finish();
		}
//		switch (id) {
//			case R.id.tabWizFieldOK: 
//			String msg = "";
//			if (fName.getEditableText().toString().trim().equals("")) {
//				Utils.logD("No field name", _logging);
//				msg = getText(R.string.MustEnterFieldName).toString();
//			}
//			if ((fAutoInc.isChecked() && fDesc.isChecked())) {
//				Utils.logD("DESC & AutoInc", _logging);
//				getText(R.string.DescAutoIncError).toString();
//				if (msg.length() > 0)
//					msg += "\n";
//				msg += getText(R.string.DescAutoIncError).toString();
//			}
//			if (fFKTab.getText().toString().trim().equals("") != 
//					fFKField.getText().toString().trim().equals("")) {
//				Utils.logD("FK check fail", _logging);
//				getText(R.string.FKDefError).toString();
//				if (msg.length() > 0)
//					msg += "\n";
//				msg += getText(R.string.FKDefError).toString();
//			}
//			if (msg.length() > 0) {
//				Utils.showMessage(getText(R.string.Error).toString(),
//						msg, _cont);
//			} else {
//				// save all field data and return to create table wizard
//				Intent in = new Intent();
//				String[] field = new String[10];
//				field[0] = fName.getText().toString();
//				field[1] = fSPType.getSelectedItem().toString();
//				field[2] = "" + fNotNull.isChecked();
//				field[3] = "" + fUnique.isChecked();
//				field[4] = "" + fPK.isChecked();
//				field[5] = "" + fDesc.isChecked();
//				field[6] = "" + fAutoInc.isChecked();
//				field[7] = fDef.getText().toString();
//				field[8] = fFKTab.getText().toString();
//				field[9] = fFKField.getText().toString(); 
//				in.putExtra("Field", field);
//				if (_editing)
//					in.putExtra("EditField", _edFieldNo);
//	      setResult(1,in);
//	      finish();
//			}
//			break;
//		case R.id.tabWizFieldCancel:
//			finish();
//			break;
//		}
	}
}
