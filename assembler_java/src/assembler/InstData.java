package assembler;

public class InstData {

	String oper;// 명령어의 이름
	String format;// 포멧
	int opcode;// opcode
	int opernum;// 연산자의 개수
	static int inst_index;// 명령어 집합의 수

	public InstData(String operation, String format, int opcode, int opernum) {
		addSet(operation, format, opcode, opernum);
	}

	public int addSet(String op, String fo, int code, int num) {

		oper = op;// 명령어의 이름
		format = fo;// 포멧
		opcode = code;// opcode
		opernum = num;// 연산자의 개수

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
