package sicXeSimulator;

import java.io.*;

public class ResourceManager {

	private String progName[] = new String[3];//���α׷��� �̸����� 
	private int startADDR[] = new int[3];//���α׷��� �����ּ� ���� 
	private int progLength[] = new int[3];//���α׷��� �������� 

	private File inputfile = null;//
	private File outputfile = null;
	private FileInputStream inputStream = null;
	private FileOutputStream outputStream = null;

	private int[] Reg = new int[10];// a x l b s t f ta pc sw ����

	public final static int MEM_SIZE = 1048576; // 2�� 10�� ����Ʈ�� �޸� ũ�⸦
	// ������(SIC/XE)
	StringBuffer MEM = new StringBuffer(MEM_SIZE);//���� �޸𸮴�

	// �޸� ������ �ʱ�ȭ �ϴ� �޼ҵ�
	public void initializeMemory() {
		for (int i = 0; i < MEM_SIZE; i++) {
			MEM.insert(i, '-');//��� -�� ä���ش�
		}
	}

	// �� �������� ���� �ʱ�ȭ �ϴ� �޼ҵ�
	public void initializeRegister() {
		for (int i = 0; i < 10; i++)
			Reg[i] = 0;//�������� �ʱ�ȭ 
	}

	// ����̽� ���ٿ� ���� �޼ҵ�
	// ����̽��� �� �̸��� ��Ī�Ǵ� ���Ϸ� �����Ѵ�
	// (F1 �̶�� ����̽��� ������ F1 �̶�� ���Ͽ��� ���� �д´�.)
	// �ش� ����̽�(����)�� ��� ������ ���·� ����� �޼ҵ�
	public void initialDevice(String devName) {
		try {
			inputfile = new File(devName);// ������ ���� �� �����ش� 
			outputfile = new File("05.txt");// ������ ���� ���� ������ �����ش� 
			inputStream = new FileInputStream(inputfile);
			outputStream = new FileOutputStream(outputfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// ������ ����̽�(����)�� ���� ���� �޼ҵ�. �Ķ���ʹ� ���� �����ϴ�.
	public void writeDevice(Byte data) {
		try {

			outputStream.write(data);//1����Ʈ�� ��� 
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	// ������ ����̽�(����)���� ���� �д� �޼ҵ�. �Ķ���ʹ� ���� �����ϴ�.
	public byte readDevice() {

		byte[] temp = new byte[1];//1����Ʈ�� �б� ���ؼ� 

		try {
			if ((inputStream.read(temp)) < 0)// ���� 1����Ʈ �о���� ���Ѵٸ� 
				return 0;//0�� ���� 
			return temp[0];//�о�� ���� ���� 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;

	}

	// �޸� ������ ���� ���� �޼ҵ�
	public void setMemory(int locate, String data, int size) {// �޸𸮿� �������� �ּҿ� �� ũ�⸦ �޾Ƽ� 
		MEM.replace(locate * 2, (locate + size) * 2, data);// �޸𸮿� �÷��� 
	}

	// �޸� �������� ���� �о���� �޼ҵ�
	public String getMemory(int locate, int size) {//���ϴ� ��ġ�� ũ�⸦ ����

		return MEM.substring(locate * 2, (locate + size) * 2);//�޸��� �ش���ġ�� ���� �������� 
	}

	// �������Ϳ� ���� �����ϴ� �޼ҵ�. regNum�� �������� ������ ��Ÿ����.
	public void setRegister(int regNum, int value) {//�������� �ѹ��� ������ �� 
		Reg[regNum] = value;

	}

	public void setRegister(char regNum, int value) {

		Reg[Integer.parseInt("" + regNum)] = value;//���� �ѹ��� ĳ�������̶� ��밡�� 

	}

	// �������Ϳ��� ���� �������� �޼ҵ�
	public int getRegister(int regNum) {
		return Reg[regNum];//���ϴ� ���������� ���� ���� 
	}

	public int getRegister(char regNum) {

		return Reg[Integer.parseInt("" + regNum)];//���ϴ� ���������� ���� ���� 
	}

	public void setProgName(String Name, int currentSection) {
		progName[currentSection] = Name;//�ش� ������ ���α׷��� �̸��� ���� 

	}

	public String getProgName(int currentSection) {
		return progName[currentSection];//�ش� ������ ���α׷� �̸��� ���� 
	}

	public void setProgLength(String Length, int currentSection) {
		progLength[currentSection] = Integer.parseInt(Length, 16);//�ش缽���� ���α׷� ���̸� ����

	}

	public int getProgLength(int currentSection) {
		return progLength[currentSection];//�ش缽���� ���α׷� ���̸� ���� 

	}

	public void setStartAdr(int currentSection) {//�ش� ������ �����ּҸ� �����Ѵ�.
		if (currentSection == 0)//0�� ������ �����ּҴ� 0
			startADDR[currentSection] = 0;
		else//0�� ������ �ƴ϶�� 
			startADDR[currentSection] = getStartAdr(currentSection - 1) + getProgLength(currentSection - 1);
		//�� ���� ������ ���� ���� ������ �����ּҰ����� ������
	}

	public int getStartAdr(int currentSection) {

		return startADDR[currentSection];// �ش� ������ �����ּҸ� ����
	}

	public void setMod(int Address, int value, int Flag, String op) {//modification ���ڵ带 ���� �޸� ���� 
		int temp = 0;
		int adrTemp = Address * 2;
		if (Flag % 2 == 1)//Ȧ����� (������� 5�ʵ� �� �������)
			adrTemp++;// �� ���� ��������Ʈ ���� �����ϹǷ� 

		temp = Integer.parseInt(MEM.substring(adrTemp, adrTemp + Flag), 16);//�ּ� ���� ������ ���� �ٲ㼭 ���� 

		if (op.equals("+")) {//�÷��װ� +��� 
			MEM.replace(adrTemp, adrTemp + Flag, String.format("%0" + Flag + "X", temp + value));//���ִ� ���� ������ 
		} else if (op.equals("-")) {//�÷��װ� - ��� 
			MEM.replace(adrTemp, adrTemp + Flag, String.format("%0" + Flag + "X", temp - value));//���ִ� ������ ���� 

		}

	}
}
