package sicXeSimulator;

import java.io.*;
import java.util.ArrayList;

public class SicLoader {
	private BufferedReader objCode;
	private int currentSection = 0;
	private int define_record = 0;
	private int modify_record = 0;

	private String[] defVal = new String[10];
	private int[] defAdr = new int[30];

	private ArrayList<ModRec> modTable = new ArrayList<ModRec>();

	private ResourceManager rMgr;

	// 목적코드를 읽어 메모리에 로드한다.
	// 목적코드의 각 헤더(H, T, M 등)를 읽어 동작을 수행한다.
	public void load(File objFile, ResourceManager manager) {
		rMgr = manager;

		String line;

		try {

			objCode = new BufferedReader(new FileReader(objFile));//오브젝트 코드 파일을 읽음 

			while ((line = objCode.readLine()) != null) {//한줄씩 읽음 

				if (line.equals(""))//빈 라인 이라면 다음 라인 읽음 
					continue;

				switch (line.charAt(0)) {//첫번째글짜에 따라서 레코드가 달라짐 
					case 'H' ://헤더 레코드
						rMgr.setProgName(line.substring(1, 7), currentSection);//2~6번째 자리는 프로그램이름 
						rMgr.setProgLength(line.substring(13, 19), currentSection);//14~18번째는 프로그램의 길이 
						rMgr.setStartAdr(currentSection);//시작주소를 구함 

						addDef(rMgr.getProgName(currentSection), rMgr.getStartAdr(currentSection));//테이블에 프로그램의 시작 주소와 이름 기록 

						break;
					case 'D' ://DEF 레코드 

						for (int i = 0; i < line.length() / 12; i++) {//12글자씩 끊어서 읽음 

							addDef(line.substring(i * 12 + 1, i * 12 + 7), rMgr.getStartAdr(currentSection) + Integer.parseInt(line.substring(i * 12 + 7, i * 12 + 13), 16));//외부 정의에 쓰기위해 이름과 주소 기록 

						}
						break;

					case 'T' ://TEXT 레코드 
						int tRcdLen = Integer.parseInt(line.substring(7, 9), 16);//한 레코드의 길이 저장 

						rMgr.setMemory(Integer.parseInt(line.substring(1, 7), 16) + rMgr.getStartAdr(currentSection), line.substring(9), tRcdLen);//메모리에 레코드의 길이만큼 올림 
						//1~7까지는 메모리에 올릴  주소를 ,9다음 부터는 objectcode를 가지고 있으므로 주소에 레코드를 올림 
						break;

					case 'M' ://Modification 레코드 

						modTable.add(new ModRec(line.substring(10, 16), rMgr.getStartAdr(currentSection) + Integer.parseInt(line.substring(1, 7), 16), line.substring(9, 10), Integer.parseInt(line.substring(7, 9), 16)));
						modify_record++;//M레코드가 있을때매다 어느 위치인지 어떤 심볼인지 연산자,길이플래그를 저장해준다 (나중에 메모리에 덮어쓰기 위해서 )
						break;
					case 'E' :
						currentSection++;

						break;
					default :
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int index = 0; index < modify_record; index++) {//M레코드의 갯수만큼 저장된 정보에 따라서 메모리에 덮어씌운다 
			int get = getDefAdr(modTable.get(index).getName());
			rMgr.setMod(modTable.get(index).getAdr(), defAdr[get], modTable.get(index).getFlag(), modTable.get(index).getOp());

		}

	}

	

	private void addDef(String name, int adr) {// D레코드의 값을 저장해주는 함수이다 
		defVal[define_record] = name;//심볼의 이름과 
		defAdr[define_record] = adr;//주소를 저장해준다
		define_record++;

	}

	private int getDefAdr(String name) {
		for (int index = 0; index < define_record; index++) {
			if (defVal[index].equals(name))//저장된 레코드로부터 어떤 심볼인지 찾아서 사용하기위한 주소값을 리턴해준다

				return index;
		}
		return -1;//없으면 -1

	}

	public class ModRec {//Modefication 레코드 한줄마다 저장되는 클래스이다 
		private String name, op;
		private int adr, flag;

		public ModRec(String Name, int Address, String Op, int Flag) {
			name = Name;//심볼의 이름 
			adr = Address;//덮어씌울 주소 
			op = Op;//연산자 + - 
			flag = Flag;//사용된 비트 폭 

		}
		//각각의 변수를 설정 참조하게 해주는 함수이다.
		public String getName() {
			return name;
		}

		public int getAdr() {
			return adr;
		}

		public String getOp() {
			return op;
		}

		public int getFlag() {
			return flag;
		}
	}

}
