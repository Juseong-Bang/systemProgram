package assembler;

import java.util.ArrayList;

public class LitralData {
	String name;//리터럴의 이름 
	ArrayList<Integer> address = new ArrayList<Integer>();//사용한 리터럴의 주소들을 기록 
	int type;// 1=char 2=int
	int length;//리터럴의 길이 

	public LitralData(String inputname) {

		name = inputname;
	}

	public LitralData(String inputname, int inType) {

		name = inputname;
		type = inType;
	}

	public void inputAddress(int add) {

		address.add(add);
	}

	public void setType(int inputType) {
		type = inputType;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return name.split("'")[1];
	}

	public int getLength() {
		if (type == 1)
			return name.split("'")[1].length();
		else
			return name.split("'")[1].length() / 2;
	}

	public int getType() {
		return type;
	}
}
