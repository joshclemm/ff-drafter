package drafter.comms;

import java.io.Serializable;

public class CellUpdate implements Serializable {

	private static final long serialVersionUID = 2744267983730079562L;

	private int row;
	private int col;
	private Object value;
	private int modelIndex;

	public CellUpdate(int row, int col, Object value) {
		super();
		this.row = row;
		this.col = col;
		this.value = value;
	}

	public CellUpdate(int modelIndex, int firstRow, int column, Object valueAt) {
		this(firstRow,column,valueAt);
		this.modelIndex = modelIndex;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public void setModelIndex(int modelIndex) {
		this.modelIndex = modelIndex;
	}

	public int getModelIndex() {
		return modelIndex;
	}
}
