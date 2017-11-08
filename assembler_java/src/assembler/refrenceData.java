package assembler;

public class refrenceData {
	String name;// 사용한 심볼의 이름
	int add;// 사용한 주소 위치
	int op;// 0은+ 1은-
	int size;// 수정할 비트으 비트의수를 기록
	int sect;//섹터를 기록 

	public refrenceData(String inputName, int inputAdd, int inputSect) {

		name = inputName;
		add = inputAdd;
		sect = inputSect;
	}

	public void setOp(int input) {
		op = input;

	}

	public int getOp() {
		return op;

	}

	public String getName() {
		return name;

	}

	public void setName(String input) {
		name = input;

	}

	public int getSize() {
		return size;

	}

	public void setSize(int input) {
		size = input;

	}

	public int getAddr() {
		return add;

	}

	public void setAddr(int input) {
		add = input;

	}

	public int getSect() {
		
		return sect;
	}

}
