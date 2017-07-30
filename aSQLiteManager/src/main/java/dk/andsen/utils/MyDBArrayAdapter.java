package dk.andsen.utils;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import dk.andsen.asqlitemanager.Prefs;
import dk.andsen.asqlitemanager.R;

public class MyDBArrayAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private final List<String> names;
	private final float fontSize;

	/**
	 * @param context
	 * @param names List of items
	 */
	public MyDBArrayAdapter(Activity context, List<String> names) {
		super(context, R.layout.row, names);
		this.context = context;
		this.names = names;
		fontSize = Prefs.getFontSize(context);
		
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public TextView textView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		// Recycle existing view if passed as parameter
		// This will save memory and time on Android
		// This only works if the base layout for all classes are the same
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.row, null, true);
			holder = new ViewHolder();
			holder.textView = (TextView) rowView.findViewById(R.id.rowtext);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}
		holder.textView.setText(names.get(position));
		holder.textView.setTextSize(fontSize);
		return rowView;
	}
}