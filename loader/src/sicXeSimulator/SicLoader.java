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

	// �����ڵ带 �о� �޸𸮿� �ε��Ѵ�.
	// �����ڵ��� �� ���(H, T, M ��)�� �о� ������ �����Ѵ�.
	public void load(File objFile, ResourceManager manager) {
		rMgr = manager;

		String line;

		try {

			objCode = new BufferedReader(new FileReader(objFile));//������Ʈ �ڵ� ������ ���� 

			while ((line = objCode.readLine()) != null) {//���پ� ���� 

				if (line.equals(""))//�� ���� �̶�� ���� ���� ���� 
					continue;

				switch (line.charAt(0)) {//ù��°��¥�� ���� ���ڵ尡 �޶��� 
					case 'H' ://��� ���ڵ�
						rMgr.setProgName(line.substring(1, 7), currentSection);//2~6��° �ڸ��� ���α׷��̸� 
						rMgr.setProgLength(line.substring(13, 19), currentSection);//14~18��°�� ���α׷��� ���� 
						rMgr.setStartAdr(currentSection);//�����ּҸ� ���� 

						addDef(rMgr.getProgName(currentSection), rMgr.getStartAdr(currentSection));//���̺� ���α׷��� ���� �ּҿ� �̸� ��� 

						break;
					case 'D' ://DEF ���ڵ� 

						for (int i = 0; i < line.length() / 12; i++) {//12���ھ� ��� ���� 

							addDef(line.substring(i * 12 + 1, i * 12 + 7), rMgr.getStartAdr(currentSection) + Integer.parseInt(line.substring(i * 12 + 7, i * 12 + 13), 16));//�ܺ� ���ǿ� �������� �̸��� �ּ� ��� 

						}
						break;

					case 'T' ://TEXT ���ڵ� 
						int tRcdLen = Integer.parseInt(line.substring(7, 9), 16);//�� ���ڵ��� ���� ���� 

						rMgr.setMemory(Integer.parseInt(line.substring(1, 7), 16) + rMgr.getStartAdr(currentSection), line.substring(9), tRcdLen);//�޸𸮿� ���ڵ��� ���̸�ŭ �ø� 
						//1~7������ �޸𸮿� �ø�  �ּҸ� ,9���� ���ʹ� objectcode�� ������ �����Ƿ� �ּҿ� ���ڵ带 �ø� 
						break;

					case 'M' ://Modification ���ڵ� 

						modTable.add(new ModRec(line.substring(10, 16), rMgr.getStartAdr(currentSection) + Integer.parseInt(line.substring(1, 7), 16), line.substring(9, 10), Integer.parseInt(line.substring(7, 9), 16)));
						modify_record++;//M���ڵ尡 �������Ŵ� ��� ��ġ���� � �ɺ����� ������,�����÷��׸� �������ش� (���߿� �޸𸮿� ����� ���ؼ� )
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

		for (int index = 0; index < modify_record; index++) {//M���ڵ��� ������ŭ ����� ������ ���� �޸𸮿� ������ 
			int get = getDefAdr(modTable.get(index).getName());
			rMgr.setMod(modTable.get(index).getAdr(), defAdr[get], modTable.get(index).getFlag(), modTable.get(index).getOp());

		}

	}

	

	private void addDef(String name, int adr) {// D���ڵ��� ���� �������ִ� �Լ��̴� 
		defVal[define_record] = name;//�ɺ��� �̸��� 
		defAdr[define_record] = adr;//�ּҸ� �������ش�
		define_record++;

	}

	private int getDefAdr(String name) {
		for (int index = 0; index < define_record; index++) {
			if (defVal[index].equals(name))//����� ���ڵ�κ��� � �ɺ����� ã�Ƽ� ����ϱ����� �ּҰ��� �������ش�

				return index;
		}
		return -1;//������ -1

	}

	public class ModRec {//Modefication ���ڵ� ���ٸ��� ����Ǵ� Ŭ�����̴� 
		private String name, op;
		private int adr, flag;

		public ModRec(String Name, int Address, String Op, int Flag) {
			name = Name;//�ɺ��� �̸� 
			adr = Address;//����� �ּ� 
			op = Op;//������ + - 
			flag = Flag;//���� ��Ʈ �� 

		}
		//������ ������ ���� �����ϰ� ���ִ� �Լ��̴�.
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
