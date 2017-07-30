/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * The class contains the filter builder
 *
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import dk.andsen.asqlitemanager.R;
import dk.andsen.utils.Utils;

public class FilterBuilder  extends Activity implements OnClickListener  {
	private Context _cont;
	private boolean _logging;
	private String _sql = "";
	private String _table = "";
	private EditText etValue;
	private EditText etSQL;
	private Spinner spField;
	private Spinner spQualifier;
	private Button btnAdd;
	private Button btnOk;
	private Button btnClear;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_cont = this;
		_logging = Prefs.getLogging(_cont);
		Utils.logD("FilterBuilder onCreate", _logging);
		if (aSQLiteManager.database== null) {
			//prevent not null
			//aSQLiteManager.database = new Database(_dbPath, _cont);
		}
		setContentView(R.layout.filter_wizard);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			_table = extras.getString("TABLE");
			_sql = extras.getString("FILTER");
			if (_sql == null)
				_sql = "";
			// we are editing an existing filter
			Utils.logD("Filter loaded " + _sql, _logging);
			Utils.logD("Table loaded " + _table, _logging);
		}
		setUpUI();
	}

	public void onClick(View btn) {
		if (btn.getId() == R.id.FilterButtonOk) {
			// filter finished return it to TableViewer
			Intent in = new Intent();
			Utils.logD("Filter SQL " + etSQL.getEditableText().toString(), _logging);
			in.putExtra("FILTER", etSQL.getEditableText().toString());
	    setResult(2,in);
	    finish();
		} else if (btn.getId()== btnAdd.getId()) {
			// add a new filter
			boolean isNumber = isNumeric(etValue.getEditableText().toString());
			if(!_sql.trim().equals(""))
				_sql += "\nand ";
			_sql += "(" + spField.getSelectedItem().toString();
			_sql += " " + spQualifier.getSelectedItem().toString();
			if (spQualifier.getSelectedItem().toString().equals("like"))
				_sql += " '" + etValue.getEditableText().toString() + "')";
			else if (spQualifier.getSelectedItem().toString().equals("in"))
				_sql += " (" + etValue.getEditableText().toString() + "))";
			else {
				// Quote all non numbers
				String arg = etValue.getEditableText().toString();
				if (!isNumber)
					arg = "'" + arg + "'";
				_sql += " " + arg + ")";
			}
			etSQL.setText(_sql);
		} else if (btn.getId() == R.id.FilterButtonClear) {
			// clear existing filter
			_sql = "";
			etSQL.setText("");
		}
	}
	
	/**
	 * Bind all the user interface components
	 */
	private void setUpUI() {
		etValue = (EditText) findViewById(R.id.FilterETValue);
		etSQL = (EditText) findViewById(R.id.FilterQuery);
		etSQL.setText(_sql);
		spField = (Spinner) findViewById(R.id.FilterSPField);
		String[] items = aSQLiteManager.database.getFieldsNames(_table);
		ArrayAdapter<String> adField = new ArrayAdapter<String>(this,
        android.R.layout.simple_spinner_item, items);
		adField.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spField.setAdapter(adField);
		spQualifier = (Spinner) findViewById(R.id.FilterSPQualifier);
		String[] qualifiers = new String[] {"=", "!=", "<", ">", "like","in"};
		ArrayAdapter<String> adQualifiers = new ArrayAdapter<String>(this,
        android.R.layout.simple_spinner_item, qualifiers);
		adQualifiers.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spQualifier.setAdapter(adQualifiers);
		btnAdd = (Button) findViewById(R.id.FilterBTNAdd);
		btnAdd.setOnClickListener(this);
		btnOk  = (Button) findViewById(R.id.FilterButtonOk);
		btnOk.setOnClickListener(this);
		btnClear = (Button) findViewById(R.id.FilterButtonClear);
		btnClear.setOnClickListener(this);
	}
	
	/**
	 * Check if a String is a string representation of a number
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    @SuppressWarnings("unused")
			double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}	
}