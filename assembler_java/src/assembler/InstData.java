package assembler;

public class InstData {

	String oper;// ��ɾ��� �̸�
	String format;// ����
	int opcode;// opcode
	int opernum;// �������� ����
	static int inst_index;// ��ɾ� ������ ��

	public InstData(String operation, String format, int opcode, int opernum) {
		addSet(operation, format, opcode, opernum);
	}

	public int addSet(String op, String fo, int code, int num) {

		oper = op;// ��ɾ��� �̸�
		format = fo;// ����
		opcode = code;// opcode
		opernum = num;// �������� ����

		inst_index++;
		return 0;
	}

	public String getOperation() {

		return oper;
	}

	public String getFormat() {
		return format;
	}

	public int getOpcode() {
		return opcode;
	}

	public void print() {
		System.out.println("operand:" + oper + " format:" + format + " opcode:" + opcode + " num:" + opernum + " \n");
	}
}
