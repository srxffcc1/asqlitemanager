package dk.andsen.csv;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import dk.andsen.asqlitemanager.Database;
import dk.andsen.asqlitemanager.Prefs;
import dk.andsen.asqlitemanager.R;
import dk.andsen.asqlitemanager.aSQLiteManager;
import dk.andsen.types.Field;
import dk.andsen.utils.Utils;

/**
 * @author andsen
 * This form is a field selector for CSV export
 *
 */
public class CsvExport extends Activity implements OnClickListener {
	private boolean _logging;
	private String _table;
	private Database _db;
	private Field[] fields;
	private LinearLayout _ll;
	private Button btnOk;
	private Button btnCansel;
	private Context _cont;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_cont = this;
		_logging = Prefs.getLogging(_cont);
		Utils.logD("CsvExport onCreate", _logging);
		setContentView(R.layout.csv_export);
		Bundle extras = getIntent().getExtras();
		if(extras !=null) {
			_table = extras.getString("TABLE");
			//_sourceType = extras.getInt("type");
			_db = aSQLiteManager.database;
			setUpUi();
		} else {
			// Missing arguments
		}
	}
	
	/**
	 * Set up user interface 
	 */
	private void setUpUi() {
		Utils.logD("CsvExport setUpUi", _logging);
		_ll = (LinearLayout) findViewById(R.id.csv_ex_fields);
		btnCansel = (Button) findViewById(R.id.csv_ex_cansel);
		btnCansel.setOnClickListener(this);
		btnOk = (Button) findViewById(R.id.csv_ex_ok);
		btnOk.setOnClickListener(this);
		fields = _db.getFields(_table);
		Utils.logD("CsvExport fields.length" + fields.length, _logging);
		for (Field field: fields) {
			Utils.logD("CsvExport adding field " + field.getFieldName(), _logging);
			LinearLayout ll = new LinearLayout(_cont);
			ll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			CheckBox cb = new CheckBox(_cont);
			cb.setText(field.getFieldName());
			cb.setChecked(true);
			_ll.addView(cb);
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.csv_ex_cansel) {
			this.finish();
		} else if (v.getId() == R.id.csv_ex_ok) {
	  	//TODO Run in thread
			List<String> fields = new ArrayList<String>();
	  	String exportFile = null;
	  	for (int i = 0; i < _ll.getChildCount(); i++) {
		  	CheckBox cb = (CheckBox)_ll.getChildAt(i);
		  	if (cb.isChecked()) {
		  		fields.add(cb.getText().toString());
		  	}
	  	}
	  	if (fields.size() > 0) {
		  	String exportRes = _db.csvExport(_cont, _table, fields, exportFile);
		  	if (exportRes == null) {
		  		// all went well
		  		this.finish();
		  	} else {
		  		Utils.showMessage(getString(R.string.Message).toString(), exportRes, _cont);
		  	}
	  	} else {
	  		Utils.showMessage(getText(R.string.Error).toString(),
	  				getText(R.string.NoFieldsSelected).toString(), _cont);
	  	}
		}
	}
}