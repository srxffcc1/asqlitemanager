/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * This is not used. Intended for scrolling instead of paging
 *
 * @author andsen
 *
 */
package dk.andsen.asqlitemanager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class MyCursorAdapter extends CursorAdapter {
	private int _mColField1;
	private int _mColField2;
	private int[] _fieldId;
	private List<String> _fieldNames;

	public MyCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		_fieldNames = new ArrayList<String>();
		_fieldId = new int[c.getColumnCount()];
		for (int i = 0; i < c.getColumnCount(); i++) {
			_fieldNames.add(c.getColumnName(i));
		}
		_mColField1 = c.getColumnIndex("Name of field");
		_mColField2 = c.getColumnIndex("Name of field");
	}

	@Override
	public void bindView(View view, Context context, Cursor c) {
	// show text value
		for (int i = 0; i < c.getColumnCount(); i ++) {
			TextView tv = (TextView) view.findViewById(_fieldId[i]);
			tv.setText(c.getString(i));
		}
	}

	private View buildView() {
		// This should build the view holding the record
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = buildView();
		return v;
	}
}