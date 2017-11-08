package assembler;

public class SymbolData {

	String symbol;//�ɺ��� ��� 
	int addr;//�ּҸ� ���� 
	int type;// 1�� ����� 2�� ������
	int section;//���� ���� 

	public SymbolData(String inputSym, int inputAdd, int inputSect) {
		symbol = inputSym;
		addr = inputAdd;
		section = inputSect;

	}

	public void setType(int typ) {

		type = typ;
	}

	public int getType() {

		return type;
	}

	public String getSymbol() {
		return symbol;

	}

	public void setAdd(int input) {
		addr = input;
	}

	public int getAdd() {
		return addr;
	}

	public int getSect() {
		return section;
	}
}
