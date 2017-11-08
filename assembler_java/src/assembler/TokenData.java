package assembler;


public class TokenData {

	int ctr;// locctr
	String label;// label
	String oper;// operation
	String[] operand;// operand
	int objectcode;// objectcode
	char nixbpe;// nixbpe bits
	String comment;// comment
	int section;// section
	int opcode;

	public TokenData(String label, String operation, String[] operand, String comment) {
		addSet(label, operation, operand, comment);
	}

	public int addSet(String la, String operation, String[] inputop, String inputcomment) {

		label = la;// label

		operand = inputop;
		comment = inputcomment;

		setoperation(operation);
		return 0;
	}

	public void print() {

		if (ctr != -1)
			System.out.printf("%04X", ctr);

		System.out.print("\t" + label + "\t" + oper + "\t");

		if (operand.length > 0) {
			System.out.printf("%s", operand[0]);
			for (int i = 1; i < operand.length; i++)
				System.out.printf(",%s", operand[i]);
		}

		if ((objectcode > 0) || (oper.equals("WORD")))
			System.out.printf("\t\t\t\t%X", objectcode);
		System.out.println("");

	}

	private void setoperation(String operation) {
		String temp = operation;
		if (temp.startsWith("+")) {
			temp = operation.substring(1);
			setNixbpe(1);// e bit¸¦ ÄÑÁÜ
		}
		oper = temp;

	}

	public void setlocctr(int inputLocctr, int sect) {
		ctr = inputLocctr;
		section = sect;
		return;
	}

	public int getlocctr() {
		return ctr;
	}

	public String getOpreatioin() {

		return oper;

	}

	public int getOperandNum() {
		return operand.length;
	}

	public String getIndexedOpernd(int index) {

		if (index < operand.length)
			return operand[index];
		else
			// System.out.println("wrong index");
			return null;

	}

	public String getLabel() {

		return label;

	}

	public void setNixbpe(int set) {
		nixbpe += set;

	}

	public int getNixbpe() {
		return (int) nixbpe;
	}

	public void setopcode(int input) {
		opcode = input;
	}

	public int getopcode() {
		return opcode;

	}

	public void setObjectcode(int input) {
		objectcode += input;

	}

	public int getObjectcode() {
		return objectcode;

	}
}
