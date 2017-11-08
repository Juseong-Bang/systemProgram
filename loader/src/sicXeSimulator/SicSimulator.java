package sicXeSimulator;

import java.io.File;

public class SicSimulator {
	private SicLoader sicLdr = new SicLoader();;
	private ResourceManager rMgr;
	String logText = "";
	String InstText = "";

	// 시뮬레이터를 동작시키기 위한 세팅을 수행한다.
	// 메모리 작업 등 실질적인 초기화 작업을 수행한다.
	public void initialize(File objFile, ResourceManager manager) {
		rMgr = manager;
		rMgr.initializeMemory();
		rMgr.initializeRegister();
		sicLdr.load(objFile, rMgr);

	}

	// 하나의 명령어만 수행한다. 해당 명령어가 수행되고 난 값의 변화를
	// 보여주고, 다음 명령어를 포인팅한다.
	// 실질적인 동작을 수행하는 메소드
	public boolean oneStep() {
		int pc = rMgr.getRegister(8);//pc값을 증가시키기 전에 불러와서 저장해준다
		int op = getOpcode(rMgr.getMemory(pc, 1));//opcode를 저장해준다
		int target = 0;
		int nixbpe = 0;
		switch (saerchOpFmt(op)) {
			case 3 ://3형식
				if ((1 & getNixpbe(rMgr.getMemory(pc, 2))) == 1) {//e비트가 1 = 4형식
					InstText = rMgr.getMemory(pc, 4) + "\n";//명령어창에 띄어주기 위해서 
					nixbpe = getNixpbe(rMgr.getMemory(pc, 2));//nixbpe를 저장
					target = Integer.parseInt(rMgr.getMemory(pc + 1, 3), 16) & 0xFFFFF;//4형식이므로 20 비트를 저장 

				} else {
					InstText = rMgr.getMemory(pc, 3) + "\n";
					target = Integer.parseInt(rMgr.getMemory(pc + 1, 2), 16) & 0xFFF;//3형식 이므로 12비트를 저장 
					nixbpe = getNixpbe(rMgr.getMemory(pc, 2));	

				}
				break;
			case 2 :
				InstText = rMgr.getMemory(pc, 2) + "\n";
				break;
			case 1 :
				InstText = rMgr.getMemory(pc, 1) + "\n";
				break;
			default :
				break;
		}

		action(op, nixbpe, target);//opcode와 nixbpe 그리고 target의 주소를 넘겨준다.

		if (rMgr.getRegister(8) == 0) {//만약 주소가 0이면 프로그램의 끝이므로 false를 리턴 

			return false;
		}
		return true;
	}

	//한 명령어의 토큰들을 가지고 실제 메모리와 레지스터에 접근하며 프로세싱을 하는 함수이다.	

	private void action(int opcode, int nixbpe, int tarAdr) {
		int position = rMgr.getRegister(8);// 명령어의 길이를 반영하기 전의 pc 값 

		int format = 0;

		if ((nixbpe & 1) != 1)
			if ((tarAdr & 0x800) == 0x800)//만약 3바이트의 맨 처음 비트가 1이면 
				tarAdr |= 0xff000;// 20비트의 음수로 확장 

		if ((nixbpe & 1) == 1)// 4형식이면  
		{
			format = 4;
			rMgr.setRegister(8, position + 4);//pc값 증가
		} else if (saerchOpFmt(opcode) == 3) {

			if ((nixbpe & 48) == 48)// ni 비트가 둘다 켜져있으면
			{

				tarAdr = (tarAdr + position + 3) & 0xfffff;// 20 비트 이상은 자름 
				format = 3;

				rMgr.setRegister(8, position + 3);//pc값 증가
			} else if ((nixbpe & 32) == 32) // indirect @
			{
				tarAdr = (tarAdr + position + 3) & 0xfffff;// 3바이트 이상은 자름 
				tarAdr = Integer.parseInt(rMgr.getMemory(tarAdr, 3), 16);//인다이렉트 이므로 2중 참조
				rMgr.setRegister(8, position + 3);//pc값 증가
				format = 5;

			} else if ((nixbpe & 16) == 16) {// immadiate #
				rMgr.setRegister(8, position + 3);//pc값 증가

				format = 6;
			}
		} else if (saerchOpFmt(opcode) == 2) {//2형식 
			rMgr.setRegister(8, position + 2);//pc값 증가
			format = 2;
		} else if (saerchOpFmt(opcode) == 1) {//1형식 
			rMgr.setRegister(8, position + 1);//pc값 증가
			format = 1;
		}

		if ((nixbpe & 8) == 8) {//x bit가 켜져있다면 

			tarAdr += rMgr.getRegister(1);//주소값에 x 레지스터의 값을 더함 
		}
		rMgr.setRegister(7, tarAdr);//ta 레지스터의 타겟의 주소값 넣어줌 
		switch (opcode) {
			case (0xb4) : {// clear r를 초기화
				logText = "CLEAR\n";//로그에 기록 
				rMgr.setRegister(rMgr.getMemory(position + 1, 1).charAt(0), 0);// r1레지스터의 값을  0으로

				break;
			}
			case (0x28) : {// COMP A와 m을 비교
				logText = "COMP\n";//로그에 기록 
				int target = Integer.parseInt(rMgr.getMemory(tarAdr, 3), 16);//메모리에서 3바이트 가져옴 

				if (format == 6)
					target = tarAdr;//만약 직접 참조면 주소가 타켓으 값임 

				if (rMgr.getRegister(0) == target)//
					rMgr.setRegister(9, 2);// m과 A가  같으면 sw 레지스터를 1로 바꿈
				else if (rMgr.getRegister(0) < target)
					rMgr.setRegister(9, 4);//A<m 이면 sw를 4로 
				else
					rMgr.setRegister(9, 1);//A>m 이면 sw를 1로 

				break;
			}
			case (0xa0) : {// COMPR
				logText = "COMPR\n";
				String temp = rMgr.getMemory(position + 1, 1);//opcode 바로 뒤에 레지스터 번호가 쓰여있으므로 

				if (rMgr.getRegister(temp.charAt(0)) == rMgr.getRegister(temp.charAt(1)))// r1과r2레지스터의값이같으면
					rMgr.setRegister(9, 2);// sw 레지스터를 2로 바꿈
				else if (rMgr.getRegister(temp.charAt(0)) < rMgr.getRegister(temp.charAt(1)))//r1<r1 면 
					rMgr.setRegister(9, 4);// sw 레지스터를 4로 바꿈
				else
					rMgr.setRegister(9, 1);//r1>r2면 sw를 1로 설정
				break;
			}
			case (0x3c) : {// J
				logText = "J\n";//로그에 기록 
				rMgr.setRegister(8, tarAdr);// 타겟 주소로 pc값을 변경 
				break;
			}
			case (0x30) : {// JEQ
				logText = "JEQ\n";
				if (rMgr.getRegister(9) == 2)//sw레지스터 참조시 같으면 
					rMgr.setRegister(8, tarAdr);// 타겟 주소로 pc값을 변경 
				break;
			}
			case (0x38) : {//JLT
				logText = "JLT\n";
				if (rMgr.getRegister(9) == 4) { //만약 sw플래그가 < 라면 
					rMgr.setRegister(8, tarAdr);// 타겟 주소로 pc값을 변경 
				}
				break;
			}
			case (0x48) : {//Jsub

				logText = "JSUB\n";

				if ((nixbpe & 1) == 1)
					rMgr.setRegister(2, position + 4);//pc의 값을 L에 저장함
				else
					rMgr.setRegister(2, position + 3);//pc의 값을 L에 저장함

				rMgr.setRegister(8, tarAdr);//target의 값을 pc로 설정
				break;
			}
			case (0x00) : {//LDA

				int target = Integer.parseInt(rMgr.getMemory(tarAdr, 3), 16);//타겟 주소에서 3바이트 가져옴 

				logText = "LDA\n";

				if ((format == 6))//immidiate 라면 
					target = tarAdr;//주소가 값이므로

				rMgr.setRegister(0, target);//a에 저장 
				break;
			}
			case (0x50) : {//LDCH

				logText = "LDCH\n";

				int target = Integer.parseInt(rMgr.getMemory(tarAdr, 1), 16);//타겟 주소에서 1바이트 가져옴 

				rMgr.setRegister(0, (byte) target);//하위 1바이트를 A에 저장
				break;
			}
			case (0x74) : {//LDT
				logText = "LDT\n";
				int target = Integer.parseInt(rMgr.getMemory(tarAdr, 3), 16);//타겟 주소에서 3바이트 가져옴 

				if (format == 6)//immidiate 라면 
					target = tarAdr;

				rMgr.setRegister(5, target);//T레지슽터에 값 저장 
				break;
			}
			case (0xd8) : {//RD
				logText = "RD\n";

				rMgr.setRegister(0, rMgr.readDevice());//디바이스로 부터 1바이트 읽어서 A에 저장 
				break;
			}
			case (0x4c) : {//RSUB

				logText = "RSUB\n";
				rMgr.setRegister(8, rMgr.getRegister(2));//L에 저장된 값으로 pc 변경 
				break;
			}
			case (0x0c) : {//STA

				logText = "STA\n";
				rMgr.setMemory(tarAdr, String.format("%06X", rMgr.getRegister(0)), 3);
				//A레지스터에서 가져온 3바이트 값을 타겟 메모리 주소에 써줌 

				break;
			}
			case (0x54) : {//STCH

				logText = "STCH\n";
				String temp = String.format("%02X", rMgr.getRegister(0));
				//메모리에서 1바이트 가져옴 
				rMgr.setMemory(tarAdr, temp, 1);
				//A레지스터에서 가져온 1바이트 값을 타겟 메모리 주소에 써줌 
				break;
			}
			case (0x14) : {//STL

				logText = "STL\n";
				rMgr.setMemory(tarAdr, String.format("%06X", rMgr.getRegister(2)), 3);
				//L레지스터에서 가져온 3바이트 값을 타겟 메모리 주소에 써줌 
				break;
			}
			case (0x10) : {//STX

				logText = "STX\n";

				rMgr.setMemory(tarAdr, String.format("%06X", rMgr.getRegister(1)), 3);
				//X레지스터에서 가져온 3바이트 값을 타겟 메모리 주소에 써줌 

				break;
			}
			case (0xe0) : {//TD
				logText = "TD\n";
				rMgr.setRegister(9, 4);
				//디바이스 테스트후 SW를 =로 바꿔줌 
				break;
			}
			case (0xb8) : {// TIXR X와 r1을 비교 후 X++
				logText = "TIXR\n";//로그 기록 

				rMgr.setRegister(1, rMgr.getRegister(1) + 1);// x 레지스터 값을 1 증가

				int TempReg = Integer.parseInt(rMgr.getMemory(position + 1, 1).substring(0, 1));
				//레지스터 넘버를 가져옴 
				if (rMgr.getRegister(1) == rMgr.getRegister(TempReg))// r1과x레지스터의값을비교
				{
					rMgr.setRegister(9, 2);//  같으면 sw레지스터를 2로 설정
				} else if (rMgr.getRegister(1) < rMgr.getRegister(TempReg)) {
					rMgr.setRegister(9, 4);// X<r1 라면  sw레지스터를 4로 설정
				} else {
					rMgr.setRegister(9, 1);// X>r1이면  sw레지스터를 1으로 설정
				}

				break;
			}
			case (0xdc) : {//WD
				logText = "WD\n";
				rMgr.writeDevice((byte) (rMgr.getRegister(0)));//A레지스터의 값을 파일에 써줌 
				break;
			}

			default :
				break;
		}

	}
	public int getOpcode(String input) {// inst의 상위 1바이트를 입력으로 받음
		return Integer.parseInt(input, 16) & 0xFc;//하위 2비트를 0으로 만들어서 리턴 
	}

	public int getNixpbe(String input) {// inst의 상위 2바이트를 입력으로 받음
		int temp = Integer.parseInt(input, 16) >> 4;//하위 4비트를 자르고 
		return temp & 0x3F;//6비트만 잘라서  리턴 
	}
	public int saerchOpFmt(int opcode) {//opcode의 형식을 리턴 (편의상 2형식 레코드 이와는 모두 3형식으로 값을 반환해줫다)
		switch (opcode) {
			case 0xb4 :// CLEAR
				return 2;
			case 0xa0 :// COMPR
				return 2;
			case 0xB8 :// TIXR
				return 2;
			default :
				return 3;
		}
	}
}
