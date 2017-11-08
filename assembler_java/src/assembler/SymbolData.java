package assembler;

public class SymbolData {

	String symbol;//심볼을 기록 
	int addr;//주소를 저장 
	int type;// 1은 상대적 2는 절대적
	int section;//사용된 섹터 

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
