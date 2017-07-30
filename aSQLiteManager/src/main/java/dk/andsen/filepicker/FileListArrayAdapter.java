package dk.andsen.filepicker;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import dk.andsen.asqlitemanager.R;
import dk.andsen.utils.Utils;

public class FileListArrayAdapter extends ArrayAdapter<String> {
	private final Activity activity;
	private final String[] filetypes;
	private final List<FileItem> ls;
	private boolean _logging = true;
	/**
	 * Build a list of files, directories based on the list of filenames.
	 * Directories are marked by the ic_folder, normal files by ic_document
	 * and file types from the list of file types with ic_app 
	 * @param activity
	 * @param names List of files directories ending with /
	 * @param filtetypes String[] of file types to mark with ic_app
	 */
	public FileListArrayAdapter(Activity activity, List<String> names, List<FileItem> ls, String[] filtetypes) {
		super(activity, R.layout.fp_row_layout, names);
		//Utils.logD("names.size " + names.size(), _logging);
		//Utils.logD("ls.size " + ls.size(), _logging);
		// names 3 - ls 2
		this.activity = activity;
		this.filetypes = filtetypes;
		this.ls = ls;
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public ImageView imageView;
		public TextView textViewName;
		public TextView textViewDate;
		public TextView textViewSize;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ViewHolder will buffer the assess to the individual fields of the row
		// layout
		ViewHolder holder;
		// Recycle existing view if passed as parameter
		// This will save memory and time on Android
		// This only works if the base layout for all classes are the same
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.fp_row_layout, null, true);
			holder = new ViewHolder();
			holder.textViewName = (TextView) rowView.findViewById(R.id.label);
			holder.textViewDate = (TextView) rowView.findViewById(R.id.date);
			holder.textViewSize = (TextView) rowView.findViewById(R.id.size);
			holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}
		holder.textViewName.setText(ls.get(position).getName());
		if (ls.get(position).getDate() != null) {
			holder.textViewDate.setText(ls.get(position).getDate().toLocaleString());
		} else {
			holder.textViewDate.setText("");
		}
		// Change the icon according to type of file - folder, sqlite or other 
		boolean isDir = ls.get(position).isDirectory();
		if (isDir) {
			// It is a directory
			holder.textViewSize.setText("");
			if (ls.get(position).getName().equals(".."))
				holder.imageView.setImageResource(R.drawable.ic_folder_up);
			else {
				holder.imageView.setImageResource(R.drawable.ic_folder);
			}
		} else if (ls.get(position).getLinkTarget() != null) {
			Utils.logD("Link " + ls.get(position).getLinkTarget(), _logging);
			holder.textViewSize.setText("");
			holder.textViewName.setText(ls.get(position).getName() + "-> " + ls.get(position).getLinkTarget());
			holder.imageView.setImageResource(R.drawable.ic_file);
		} else {
			// It is a file
			holder.textViewSize.setText(""+ls.get(position).length());
			// Here all known file types are taken care of 
			if (fileType(ls.get(position).getName(), filetypes)) {
				if (ls.get(position).getName().toLowerCase(Locale.US).endsWith(".sqlite") ||
						ls.get(position).getName().toLowerCase(Locale.US).endsWith(".db"))
					holder.imageView.setImageResource(R.drawable.ic_database);
				else if (ls.get(position).getName().trim().toLowerCase(Locale.US).endsWith (".sql"))
					holder.imageView.setImageResource(R.drawable.ic_sql);
				else if (ls.get(position).getName().trim().toLowerCase(Locale.US).endsWith(".csv"))
					holder.imageView.setImageResource(R.drawable.ic_csv);
			} else
				holder.imageView.setImageResource(R.drawable.ic_file);
		}
		return rowView;
	}
	
	/**
	 * Return true if the fileName ends with one of the types in fileTypes
	 * @param fileName
	 * @param fileTypes
	 * @return
	 */
	private boolean fileType(String fileName, String[] fileTypes) {
		if (fileTypes == null)
			return false;
		if (fileName == null)
			return false;
		//toastMsg("new line: " + nl);
		if (fileName.equals(".."))
			return false;
		String file = fileName;  //
		for (int i = 0; i < fileTypes.length; i++) {
			if (file.endsWith(fileTypes[i]))
				return true;
		}
		return false;
	}
}