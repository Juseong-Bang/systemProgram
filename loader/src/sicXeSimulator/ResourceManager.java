package sicXeSimulator;

import java.io.*;

public class ResourceManager {

	private String progName[] = new String[3];//프로그램의 이름저장 
	private int startADDR[] = new int[3];//프로그램의 시작주소 저장 
	private int progLength[] = new int[3];//프로그램의 길이저장 

	private File inputfile = null;//
	private File outputfile = null;
	private FileInputStream inputStream = null;
	private FileOutputStream outputStream = null;

	private int[] Reg = new int[10];// a x l b s t f ta pc sw 순서

	public final static int MEM_SIZE = 1048576; // 2의 10승 바이트의 메모리 크기를
	// 가진다(SIC/XE)
	StringBuffer MEM = new StringBuffer(MEM_SIZE);//가상 메모리다

	// 메모리 영역을 초기화 하는 메소드
	public void initializeMemory() {
		for (int i = 0; i < MEM_SIZE; i++) {
			MEM.insert(i, '-');//모두 -로 채워준다
		}
	}

	// 각 레지스터 값을 초기화 하는 메소드
	public void initializeRegister() {
		for (int i = 0; i < 10; i++)
			Reg[i] = 0;//레지스터 초기화 
	}

	// 디바이스 접근에 대한 메소드
	// 디바이스는 각 이름과 매칭되는 파일로 가정한다
	// (F1 이라는 디바이스를 읽으면 F1 이라는 파일에서 값을 읽는다.)
	// 해당 디바이스(파일)를 사용 가능한 상태로 만드는 메소드
	public void initialDevice(String devName) {
		try {
			inputfile = new File(devName);// 복사할 파일 을 열어준다 
			outputfile = new File("05.txt");// 복사한 값을 써줄 파일을 열어준다 
			inputStream = new FileInputStream(inputfile);
			outputStream = new FileOutputStream(outputfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// 선택한 디바이스(파일)에 값을 쓰는 메소드. 파라미터는 변경 가능하다.
	public void writeDevice(Byte data) {
		try {

			outputStream.write(data);//1바이트를 기록 
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	// 선택한 디바이스(파일)에서 값을 읽는 메소드. 파라미터는 변경 가능하다.
	public byte readDevice() {

		byte[] temp = new byte[1];//1바이트씩 읽기 위해서 

		try {
			if ((inputStream.read(temp)) < 0)// 만약 1바이트 읽어오지 못한다면 
				return 0;//0을 리턴 
			return temp[0];//읽어온 값을 리턴 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;

	}

	// 메모리 영역에 값을 쓰는 메소드
	public void setMemory(int locate, String data, int size) {// 메모리에 쓰기위해 주소와 값 크기를 받아서 
		MEM.replace(locate * 2, (locate + size) * 2, data);// 메모리에 올려줌 
	}

	// 메모리 영역에서 값을 읽어오는 메소드
	public String getMemory(int locate, int size) {//원하는 위치와 크기를 전달

		return MEM.substring(locate * 2, (locate + size) * 2);//메모리의 해당위치의 값을 리턴해줌 
	}

	// 레지스터에 값을 세팅하는 메소드. regNum은 레지스터 종류를 나타낸다.
	public void setRegister(int regNum, int value) {//레지스터 넘버와 설정할 값 
		Reg[regNum] = value;

	}

	public void setRegister(char regNum, int value) {

		Reg[Integer.parseInt("" + regNum)] = value;//만약 넘버가 캐릭터형이라도 사용가능 

	}

	// 레지스터에서 값을 가져오는 메소드
	public int getRegister(int regNum) {
		return Reg[regNum];//원하는 레지스터의 값을 리턴 
	}

	public int getRegister(char regNum) {

		return Reg[Integer.parseInt("" + regNum)];//원하는 레지스터의 값을 리턴 
	}

	public void setProgName(String Name, int currentSection) {
		progName[currentSection] = Name;//해당 섹션의 프로그램의 이름을 저장 

	}

	public String getProgName(int currentSection) {
		return progName[currentSection];//해당 섹션의 프로그램 이름을 리턴 
	}

	public void setProgLength(String Length, int currentSection) {
		progLength[currentSection] = Integer.parseInt(Length, 16);//해당섹션의 프로그램 길이를 저장

	}

	public int getProgLength(int currentSection) {
		return progLength[currentSection];//해당섹션의 프로그램 길이를 리턴 

	}

	public void setStartAdr(int currentSection) {//해당 섹션의 시작주소를 설정한다.
		if (currentSection == 0)//0번 섹션의 시작주소는 0
			startADDR[currentSection] = 0;
		else//0번 섹션이 아니라면 
			startADDR[currentSection] = getStartAdr(currentSection - 1) + getProgLength(currentSection - 1);
		//이 이전 섹션의 끝이 다음 섹션의 시작주소값으로 설정됨
	}

	public int getStartAdr(int currentSection) {

		return startADDR[currentSection];// 해당 섹션의 시작주소를 리턴
	}

	public void setMod(int Address, int value, int Flag, String op) {//modification 레코드를 위한 메모리 접근 
		int temp = 0;
		int adrTemp = Address * 2;
		if (Flag % 2 == 1)//홀수라면 (예를들어 5필드 폭 같은경우)
			adrTemp++;// 그 다음 하프바이트 부터 수정하므로 

		temp = Integer.parseInt(MEM.substring(adrTemp, adrTemp + Flag), 16);//주소 값을 숫자형 으로 바꿔서 저장 

		if (op.equals("+")) {//플래그가 +라면 
			MEM.replace(adrTemp, adrTemp + Flag, String.format("%0" + Flag + "X", temp + value));//써있는 값에 더해줌 
		} else if (op.equals("-")) {//플래그가 - 라면 
			MEM.replace(adrTemp, adrTemp + Flag, String.format("%0" + Flag + "X", temp - value));//써있던 값에서 빼줌 

		}

	}
}
