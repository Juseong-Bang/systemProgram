package sicXeSimulator;

import java.io.File;

public class SicSimulator {
	private SicLoader sicLdr = new SicLoader();;
	private ResourceManager rMgr;
	String logText = "";
	String InstText = "";

	// �ùķ����͸� ���۽�Ű�� ���� ������ �����Ѵ�.
	// �޸� �۾� �� �������� �ʱ�ȭ �۾��� �����Ѵ�.
	public void initialize(File objFile, ResourceManager manager) {
		rMgr = manager;
		rMgr.initializeMemory();
		rMgr.initializeRegister();
		sicLdr.load(objFile, rMgr);

	}

	// �ϳ��� ��ɾ �����Ѵ�. �ش� ��ɾ ����ǰ� �� ���� ��ȭ��
	// �����ְ�, ���� ��ɾ �������Ѵ�.
	// �������� ������ �����ϴ� �޼ҵ�
	public boolean oneStep() {
		int pc = rMgr.getRegister(8);//pc���� ������Ű�� ���� �ҷ��ͼ� �������ش�
		int op = getOpcode(rMgr.getMemory(pc, 1));//opcode�� �������ش�
		int target = 0;
		int nixbpe = 0;
		switch (saerchOpFmt(op)) {
			case 3 ://3����
				if ((1 & getNixpbe(rMgr.getMemory(pc, 2))) == 1) {//e��Ʈ�� 1 = 4����
					InstText = rMgr.getMemory(pc, 4) + "\n";//��ɾ�â�� ����ֱ� ���ؼ� 
					nixbpe = getNixpbe(rMgr.getMemory(pc, 2));//nixbpe�� ����
					target = Integer.parseInt(rMgr.getMemory(pc + 1, 3), 16) & 0xFFFFF;//4�����̹Ƿ� 20 ��Ʈ�� ���� 

				} else {
					InstText = rMgr.getMemory(pc, 3) + "\n";
					target = Integer.parseInt(rMgr.getMemory(pc + 1, 2), 16) & 0xFFF;//3���� �̹Ƿ� 12��Ʈ�� ���� 
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

		action(op, nixbpe, target);//opcode�� nixbpe �׸��� target�� �ּҸ� �Ѱ��ش�.

		if (rMgr.getRegister(8) == 0) {//���� �ּҰ� 0�̸� ���α׷��� ���̹Ƿ� false�� ���� 

			return false;
		}
		return true;
	}

	//�� ��ɾ��� ��ū���� ������ ���� �޸𸮿� �������Ϳ� �����ϸ� ���μ����� �ϴ� �Լ��̴�.	

	private void action(int opcode, int nixbpe, int tarAdr) {
		int position = rMgr.getRegister(8);// ��ɾ��� ���̸� �ݿ��ϱ� ���� pc �� 

		int format = 0;

		if ((nixbpe & 1) != 1)
			if ((tarAdr & 0x800) == 0x800)//���� 3����Ʈ�� �� ó�� ��Ʈ�� 1�̸� 
				tarAdr |= 0xff000;// 20��Ʈ�� ������ Ȯ�� 

		if ((nixbpe & 1) == 1)// 4�����̸�  
		{
			format = 4;
			rMgr.setRegister(8, position + 4);//pc�� ����
		} else if (saerchOpFmt(opcode) == 3) {

			if ((nixbpe & 48) == 48)// ni ��Ʈ�� �Ѵ� ����������
			{

				tarAdr = (tarAdr + position + 3) & 0xfffff;// 20 ��Ʈ �̻��� �ڸ� 
				format = 3;

				rMgr.setRegister(8, position + 3);//pc�� ����
			} else if ((nixbpe & 32) == 32) // indirect @
			{
				tarAdr = (tarAdr + position + 3) & 0xfffff;// 3����Ʈ �̻��� �ڸ� 
				tarAdr = Integer.parseInt(rMgr.getMemory(tarAdr, 3), 16);//�δ��̷�Ʈ �̹Ƿ� 2�� ����
				rMgr.setRegister(8, position + 3);//pc�� ����
				format = 5;

			} else if ((nixbpe & 16) == 16) {// immadiate #
				rMgr.setRegister(8, position + 3);//pc�� ����

				format = 6;
			}
		} else if (saerchOpFmt(opcode) == 2) {//2���� 
			rMgr.setRegister(8, position + 2);//pc�� ����
			format = 2;
		} else if (saerchOpFmt(opcode) == 1) {//1���� 
			rMgr.setRegister(8, position + 1);//pc�� ����
			format = 1;
		}

		if ((nixbpe & 8) == 8) {//x bit�� �����ִٸ� 

			tarAdr += rMgr.getRegister(1);//�ּҰ��� x ���������� ���� ���� 
		}
		rMgr.setRegister(7, tarAdr);//ta ���������� Ÿ���� �ּҰ� �־��� 
		switch (opcode) {
			case (0xb4) : {// clear r�� �ʱ�ȭ
				logText = "CLEAR\n";//�α׿� ��� 
				rMgr.setRegister(rMgr.getMemory(position + 1, 1).charAt(0), 0);// r1���������� ����  0����

				break;
			}
			case (0x28) : {// COMP A�� m�� ��
				logText = "COMP\n";//�α׿� ��� 
				int target = Integer.parseInt(rMgr.getMemory(tarAdr, 3), 16);//�޸𸮿��� 3����Ʈ ������ 

				if (format == 6)
					target = tarAdr;//���� ���� ������ �ּҰ� Ÿ���� ���� 

				if (rMgr.getRegister(0) == target)//
					rMgr.setRegister(9, 2);// m�� A��  ������ sw �������͸� 1�� �ٲ�
				else if (rMgr.getRegister(0) < target)
					rMgr.setRegister(9, 4);//A<m �̸� sw�� 4�� 
				else
					rMgr.setRegister(9, 1);//A>m �̸� sw�� 1�� 

				break;
			}
			case (0xa0) : {// COMPR
				logText = "COMPR\n";
				String temp = rMgr.getMemory(position + 1, 1);//opcode �ٷ� �ڿ� �������� ��ȣ�� ���������Ƿ� 

				if (rMgr.getRegister(temp.charAt(0)) == rMgr.getRegister(temp.charAt(1)))// r1��r2���������ǰ��̰�����
					rMgr.setRegister(9, 2);// sw �������͸� 2�� �ٲ�
				else if (rMgr.getRegister(temp.charAt(0)) < rMgr.getRegister(temp.charAt(1)))//r1<r1 �� 
					rMgr.setRegister(9, 4);// sw �������͸� 4�� �ٲ�
				else
					rMgr.setRegister(9, 1);//r1>r2�� sw�� 1�� ����
				break;
			}
			case (0x3c) : {// J
				logText = "J\n";//�α׿� ��� 
				rMgr.setRegister(8, tarAdr);// Ÿ�� �ּҷ� pc���� ���� 
				break;
			}
			case (0x30) : {// JEQ
				logText = "JEQ\n";
				if (rMgr.getRegister(9) == 2)//sw�������� ������ ������ 
					rMgr.setRegister(8, tarAdr);// Ÿ�� �ּҷ� pc���� ���� 
				break;
			}
			case (0x38) : {//JLT
				logText = "JLT\n";
				if (rMgr.getRegister(9) == 4) { //���� sw�÷��װ� < ��� 
					rMgr.setRegister(8, tarAdr);// Ÿ�� �ּҷ� pc���� ���� 
				}
				break;
			}
			case (0x48) : {//Jsub

				logText = "JSUB\n";

				if ((nixbpe & 1) == 1)
					rMgr.setRegister(2, position + 4);//pc�� ���� L�� ������
				else
					rMgr.setRegister(2, position + 3);//pc�� ���� L�� ������

				rMgr.setRegister(8, tarAdr);//target�� ���� pc�� ����
				break;
			}
			case (0x00) : {//LDA

				int target = Integer.parseInt(rMgr.getMemory(tarAdr, 3), 16);//Ÿ�� �ּҿ��� 3����Ʈ ������ 

				logText = "LDA\n";

				if ((format == 6))//immidiate ��� 
					target = tarAdr;//�ּҰ� ���̹Ƿ�

				rMgr.setRegister(0, target);//a�� ���� 
				break;
			}
			case (0x50) : {//LDCH

				logText = "LDCH\n";

				int target = Integer.parseInt(rMgr.getMemory(tarAdr, 1), 16);//Ÿ�� �ּҿ��� 1����Ʈ ������ 

				rMgr.setRegister(0, (byte) target);//���� 1����Ʈ�� A�� ����
				break;
			}
			case (0x74) : {//LDT
				logText = "LDT\n";
				int target = Integer.parseInt(rMgr.getMemory(tarAdr, 3), 16);//Ÿ�� �ּҿ��� 3����Ʈ ������ 

				if (format == 6)//immidiate ��� 
					target = tarAdr;

				rMgr.setRegister(5, target);//T�������Ϳ� �� ���� 
				break;
			}
			case (0xd8) : {//RD
				logText = "RD\n";

				rMgr.setRegister(0, rMgr.readDevice());//����̽��� ���� 1����Ʈ �о A�� ���� 
				break;
			}
			case (0x4c) : {//RSUB

				logText = "RSUB\n";
				rMgr.setRegister(8, rMgr.getRegister(2));//L�� ����� ������ pc ���� 
				break;
			}
			case (0x0c) : {//STA

				logText = "STA\n";
				rMgr.setMemory(tarAdr, String.format("%06X", rMgr.getRegister(0)), 3);
				//A�������Ϳ��� ������ 3����Ʈ ���� Ÿ�� �޸� �ּҿ� ���� 

				break;
			}
			case (0x54) : {//STCH

				logText = "STCH\n";
				String temp = String.format("%02X", rMgr.getRegister(0));
				//�޸𸮿��� 1����Ʈ ������ 
				rMgr.setMemory(tarAdr, temp, 1);
				//A�������Ϳ��� ������ 1����Ʈ ���� Ÿ�� �޸� �ּҿ� ���� 
				break;
			}
			case (0x14) : {//STL

				logText = "STL\n";
				rMgr.setMemory(tarAdr, String.format("%06X", rMgr.getRegister(2)), 3);
				//L�������Ϳ��� ������ 3����Ʈ ���� Ÿ�� �޸� �ּҿ� ���� 
				break;
			}
			case (0x10) : {//STX

				logText = "STX\n";

				rMgr.setMemory(tarAdr, String.format("%06X", rMgr.getRegister(1)), 3);
				//X�������Ϳ��� ������ 3����Ʈ ���� Ÿ�� �޸� �ּҿ� ���� 

				break;
			}
			case (0xe0) : {//TD
				logText = "TD\n";
				rMgr.setRegister(9, 4);
				//����̽� �׽�Ʈ�� SW�� =�� �ٲ��� 
				break;
			}
			case (0xb8) : {// TIXR X�� r1�� �� �� X++
				logText = "TIXR\n";//�α� ��� 

				rMgr.setRegister(1, rMgr.getRegister(1) + 1);// x �������� ���� 1 ����

				int TempReg = Integer.parseInt(rMgr.getMemory(position + 1, 1).substring(0, 1));
				//�������� �ѹ��� ������ 
				if (rMgr.getRegister(1) == rMgr.getRegister(TempReg))// r1��x���������ǰ�����
				{
					rMgr.setRegister(9, 2);//  ������ sw�������͸� 2�� ����
				} else if (rMgr.getRegister(1) < rMgr.getRegister(TempReg)) {
					rMgr.setRegister(9, 4);// X<r1 ���  sw�������͸� 4�� ����
				} else {
					rMgr.setRegister(9, 1);// X>r1�̸�  sw�������͸� 1���� ����
				}

				break;
			}
			case (0xdc) : {//WD
				logText = "WD\n";
				rMgr.writeDevice((byte) (rMgr.getRegister(0)));//A���������� ���� ���Ͽ� ���� 
				break;
			}

			default :
				break;
		}

	}
	public int getOpcode(String input) {// inst�� ���� 1����Ʈ�� �Է����� ����
		return Integer.parseInt(input, 16) & 0xFc;//���� 2��Ʈ�� 0���� ���� ���� 
	}

	public int getNixpbe(String input) {// inst�� ���� 2����Ʈ�� �Է����� ����
		int temp = Integer.parseInt(input, 16) >> 4;//���� 4��Ʈ�� �ڸ��� 
		return temp & 0x3F;//6��Ʈ�� �߶�  ���� 
	}
	public int saerchOpFmt(int opcode) {//opcode�� ������ ���� (���ǻ� 2���� ���ڵ� �̿ʹ� ��� 3�������� ���� ��ȯ�آZ��)
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
