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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import dk.andsen.asqlitemanager.Database;
import dk.andsen.asqlitemanager.Prefs;
import dk.andsen.asqlitemanager.R;
import dk.andsen.asqlitemanager.aSQLiteManager;
import dk.andsen.types.Field;
import dk.andsen.utils.Utils;

/**
 * @author andsen
 * This form is a field selector for CSV import
 *
 */
public class CsvImport extends Activity implements OnClickListener {
	private boolean _logging;
	private String _table;
	private Database _db;
	private Field[] fields;
	private LinearLayout _ll;
	private Button btnOk;
	private Button btnCansel;
	private Button btnReread;
	private EditText etLinesToSkip;
	private TextView tvFirstLines;
	private Context _cont;
	private List<String> firstLines;
	private String _fileName;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_cont = this;
		_logging = Prefs.getLogging(_cont);
		Utils.logD("CsvImport onCreate", _logging);
		setContentView(R.layout.csv_import);
		Bundle extras = getIntent().getExtras();
		if(extras !=null) {
			_table = extras.getString("TABLE");
			_fileName = extras.getString("FILENAME");
			_db = aSQLiteManager.database;
			setUpUi();
		} else {
			// Missing arguments
			//TODO Message and finish()
			//this.finish();
		}
	}
	
	/**
	 * Set up user interface 
	 */
	private void setUpUi() {
		Utils.logD("CsvImport setUpUi", _logging);
		_ll = (LinearLayout) findViewById(R.id.csv_imp_fields);
		btnCansel = (Button) findViewById(R.id.csv_imp_cansel);
		btnCansel.setOnClickListener(this);
		btnOk = (Button) findViewById(R.id.csv_imp_ok);
		btnOk.setOnClickListener(this);
		btnReread = (Button) findViewById(R.id.csv_imp_reread);
		btnReread.setOnClickListener(this);
		//TODO do not wrap lines here!!
		tvFirstLines = (TextView) findViewById(R.id.csv_first_lines);
		etLinesToSkip = (EditText) findViewById(R.id.cvs_imp_skip);
		fields = _db.getFields(_table);
		Utils.logD("CsvImport fields.length" + fields.length, _logging);
		readFirstLines();
		int counter = 1;
		for (Field field: fields) {
			Utils.logD("CsvImport adding field " + field.getFieldName(), _logging);
			LinearLayout ll = new LinearLayout(_cont);
			ll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			CheckBox cb = new CheckBox(_cont);
			EditText et = new EditText(_cont);
			et.setText(""+counter++);
			cb.setChecked(true);
			cb.setText(field.getFieldName());
			cb.setChecked(true);
			ll.addView(cb);
			ll.addView(et);
			_ll.addView(ll);
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.csv_imp_cansel) {
			// Operation cancelled
			this.finish();
		} else if (v.getId() == R.id.csv_imp_ok) {
			// Import now
			int linesToSkip = new Integer(etLinesToSkip.getText().toString());
			List<String> fields = new ArrayList<String>();
			List<Integer> cols = new ArrayList<Integer>();
  		String importFile = _fileName;
	  	for (int i = 0; i < _ll.getChildCount(); i++) {
	  		LinearLayout ll = (LinearLayout)_ll.getChildAt(i);
		  	CheckBox cb = (CheckBox) ll.getChildAt(0);
		  	EditText et = (EditText) ll.getChildAt(1);
		  	if (cb.isChecked()) {
		  		// Here we make two lists on with the fields to import into and on with the
		  		// corresponding column numbers
		  		fields.add(cb.getText().toString());
		  		cols.add(new Integer(et.getText().toString()));
		  	}
	  	}
	  	if (fields.size() > 0) {
	    	String importRes = _db.cvsImport(_cont, _table, importFile, linesToSkip, fields, cols);
	    	if (importRes == null) {
	    		// all went well
	    	} else {
	    		Utils.showMessage(getString(R.string.Message).toString(), importRes, _cont);
	    	}
	  	} else {
	  		Utils.showMessage(getText(R.string.Error).toString(),
	  				getText(R.string.NoFieldsSelected).toString(), _cont);
	  	}
		} else if (v.getId() == R.id.csv_imp_reread) {
			//Reread the top of the csv file
			readFirstLines();
		}
	}

	/**
	 * Read the top of the CSV file and show it on the screen
	 */
	private void readFirstLines() {
		try {
			int linesToSkip = new Integer(etLinesToSkip.getText().toString());
			// _db._dbPath + "." +_table + ".csv"
			firstLines = CSVUtils.readFirstLines(_fileName, 5, linesToSkip);
			Utils.logD("Importing " + _db._dbPath + "." +_table + ".csv", _logging);
			if (firstLines != null) {
				String text = "";
				for (String line: firstLines) {
					if (text.equals(""))
						text += line;
					else
						text += "\n" + line;
				}
				tvFirstLines.setText(text);
			}
		} catch (Exception e) {
			Utils.showMessage(getText(R.string.Error).toString(), e.getLocalizedMessage(), _cont);
			e.printStackTrace();
		}
		
	}
}