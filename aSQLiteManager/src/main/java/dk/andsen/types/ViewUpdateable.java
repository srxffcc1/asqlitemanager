package dk.andsen.types;

public class ViewUpdateable {
  private boolean editable;
  private boolean insertable;
  private boolean deleteable;
  
	/**
	 * @return true if you can edit records from the view
	 */
	public boolean isUpdateable() {
		return editable;
	}
	public void setUpdateable(boolean editable) {
		this.editable = editable;
	}
	/**
	 * @return true if you can insert records to the view 
	 */
	public boolean isInsertable() {
		return insertable;
	}
	public void setInsertable(boolean insertable) {
		this.insertable = insertable;
	}
	/**
	 * @return true if you can delete records from the view
	 */
	public boolean isDeleteable() {
		return deleteable;
	}
	public void setDeleteable(boolean deleteable) {
		this.deleteable = deleteable;
	}
  
}
