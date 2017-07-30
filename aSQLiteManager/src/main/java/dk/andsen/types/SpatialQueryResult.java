/**
 * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
 * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
 *
 * @author andsen
 *
 */
package dk.andsen.types;

import java.util.List;

public class SpatialQueryResult {
	public String[] columnNames;
	public List<String[]> Data;

	public String[] getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	public List<String[]> getData() {
		return Data;
	}
	public void setData(List<String[]> data) {
		Data = data;
	}
}
