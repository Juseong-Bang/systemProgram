package assembler;

public class refrenceData {
	String name;// ����� �ɺ��� �̸�
	int add;// ����� �ּ� ��ġ
	int op;// 0��+ 1��-
	int size;// ������ ��Ʈ�� ��Ʈ�Ǽ��� ���
	int sect;//���͸� ��� 

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
