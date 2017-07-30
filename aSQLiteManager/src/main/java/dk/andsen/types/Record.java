package dk.andsen.types;

public class Record {
	private AField[] fields;
	private String[] fieldNames;

	public String[] getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(String[] fieldNames) {
		this.fieldNames = fieldNames;
	}

	public void setFields(AField[] fields) {
		this.fields = fields;
	}

	public AField[] getFields() {
		return fields;
	}
	
}
