package assembler;

import java.io.*;
import java.util.ArrayList;

public class Assembler {
	static Assembler myAssembler = new Assembler();
	static ArrayList<InstData> instTable = new ArrayList<InstData>();//inst.txt�� ����� ������ �����Ѵ�
	static ArrayList<TokenData> tokenTable = new ArrayList<TokenData>();//input.txt�� ����� ������ �����Ѵ�
	static ArrayList<SymbolData> symTable = new ArrayList<SymbolData>();//�ɺ����̺�
	static ArrayList<LitralData> litTable = new ArrayList<LitralData>();//���ͷ� ���̺�
	static ArrayList<refrenceData> refTable = new ArrayList<refrenceData>();//���ʿ� ���� ������ ������ ���̺� 
	
	static ArrayList<refrenceData> extref = new ArrayList<refrenceData>();//�ܺ� ������ ��� 
	static ArrayList<refrenceData> extdef = new ArrayList<refrenceData>();//�ܺ� ���Ǹ� ���

	int sect = 0;
	int locctr = 0;
	int[] sectSize = new int[10];// �������Ǳ��̸�����

	public static void main(String[] args) {

		if (myAssembler.init_my_assembler() < 0) {
			System.out.println("init_my_assembler: program init failed.\n");
			return;
		}

		if (myAssembler.assem_pass1() < 0) {
			System.out.println("assem_pass1: pass1 failed.  \n");
			return;
		}

		if (myAssembler.assem_pass2() < 0) {
			System.out.println(" assem_pass2: pass2 failed. \n");
			return;
		}

		myAssembler.make_objectcode_output("output_116.txt");

		return;

	}

	private int init_my_assembler() {// ������� �ʿ��� ������ ���ϵ��� �ʱ�ȭ�ϴ� �κ�
		int result;

		if ((result = myAssembler.init_inst_file("instset.txt")) < 0)
			return -1;
		if ((result = myAssembler.init_input_file("input.txt")) < 0)
			return -1;
		return result;
	}

	private int init_inst_file(String string) {
		try {
			BufferedReader instBuffer = new BufferedReader(new FileReader(new File(string)));
			String line = null;

			while ((line = instBuffer.readLine()) != null) {

				String[] values = line.split("\t", 4);// tab�� �������� �����͸� �����Ͽ����Ƿ�
				instTable.add(new InstData(values[0], values[1], Integer.parseInt(values[2], 16),
						Integer.parseInt(values[3], 10)));// ������ �ɹ��� opcode�� �׹�°
															// �ɹ��� �������� ������
															// ���� 16����, 10������
															// ����Ǿ������Ƿ�

			}
			instBuffer.close();// instData�� �Է��� �������Ƿ� �ݴ´�
		} catch (Exception ex) {
			System.out.println("instset add err\n");
			return -1;
		}
		return 0;
	}

	private int init_input_file(String string) {
		try {
			BufferedReader inputBuffer = new BufferedReader(new FileReader(new File(string)));
			String line = null;

			while ((line = inputBuffer.readLine()) != null) {
				if (line.charAt(0) != '.') {
					String[] values = line.split("\t", 4);// �� �������� �����Ͽ����Ƿ�

					String[] operand = values[2].split(",", 3);// ����° �ɹ� �ǿ����ڴ�
																// �Ǵٽ� �޸��� ��������
																// �ѹ��� �����ش�
					tokenTable.add(new TokenData(values[0], values[1], operand, values[3]));// tokenData��
																							// ������
																							// ������
																							// �������ش�
				}
			}
			inputBuffer.close();// input�� ��ū���� ������ ������ �������Ƿ� �ݴ´�
		} catch (Exception ex) {
			System.out.println("error at init input data\n");
			return -1;
		}
		return 0;
	}

	private int assem_pass1() {// �н� 1������ ������ token�� �̿��Ͽ� �� ���͸��� �����̼� ī���͸� �Է����ִ�
								// �Լ��̴�

		for (int i = 0; i < tokenTable.size(); i++) {
			i += myAssembler.makelocctr(i);

		}
		sectSize[sect] = locctr;// ������ ������ ���̸� ����
		sect = 0;
		return 0;
	}

	private int makelocctr(int index) {// �����̼� ī���͸� �Է����ش�

		int p = 0;
		if (tokenTable.get(index).getOpreatioin().equals("CSECT")) {// ���� ���ͷ�
																	// �Ѿ�� ��ɾ�
																	// �̹Ƿ�
			sectSize[sect] = locctr;// ������ ���̸� ����
			sect++;// ���� ���ͷ� �Ѿ
			tokenTable.get(index).setlocctr(-1, sect);// �����̼� ī���͸� ������� �����Ƿ� -1��
														// ����
			locctr = 0;// �����̼� ī���͸� ���� �Űܾ� �ϹǷ� 0���� �ʱ�ȭ
			return 0;
		}

		if (!tokenTable.get(index).getIndexedOpernd(0).equals(""))// �ǿ����ڰ� ����ִٸ�
																	// �Ѿ
			if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '=')// �ǿ����ڰ�
																			// ���ͷ��ϰ��
			{
				if ((p = myAssembler.search_literal(tokenTable.get(index).getIndexedOpernd(0))) != -1)// ���ͷ�Ǯ���̹��ִ¸��ͷ��̶��

					litTable.get(p).inputAddress(tokenTable.get(index).getlocctr());// �ּҸ�����

				else {// ���ͷ� Ǯ�� �ִ´�
					if (tokenTable.get(index).getIndexedOpernd(0).charAt(1) == 'C') {// Ÿ����
																						// ����
						litTable.add(new LitralData(tokenTable.get(index).getIndexedOpernd(0), 1));// ĳ�������̹Ƿ�
																									// Ÿ����
																									// 1
					} else if (tokenTable.get(index).getIndexedOpernd(0).charAt(1) == 'X') {
						litTable.add(new LitralData(tokenTable.get(index).getIndexedOpernd(0), 2));// �������̹Ƿ�
																									// Ÿ����
																									// 2

					} else {// ����
						return -1;
					}
				}
			}
		p = 0;
		if (!tokenTable.get(index).getLabel().equals("") && !tokenTable.get(index).getLabel().equals("*")) {// ���̺���
																											// ������
			symTable.add(new SymbolData(tokenTable.get(index).getLabel(), locctr, sect));// �ɺ���
																							// �����
			symTable.get(symTable.size() - 1).setType(1);
			// �ɺ����̺� ��,�ּ�,���͸� �־���
		}

		tokenTable.get(index).setlocctr(locctr, sect);// �����̼� ī���͸� �ϰ������� ���� (�ؿ���
														// ��뿡 ���� �ٽ� ���� ��������)

		if ((p = myAssembler.search_opcode(tokenTable.get(index).getOpreatioin())) != -1) {// ��ɾ
																							// optable��
																							// �ִ°��

			if (!tokenTable.get(index).getIndexedOpernd(0).equals("")) {
				if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '#')// �ǿ�����
																				// �տ�
																				// #
					tokenTable.get(index).setNixbpe(16);// immediate
				if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '@')// �ǿ�����
																				// �տ�
																				// @
					tokenTable.get(index).setNixbpe(32);// indirect

			}
			tokenTable.get(index).setopcode(instTable.get(p).getOpcode());// �޾ƿ�
																			// opcode����
			if ((tokenTable.get(index).getNixbpe() & 1) == 1)// 4���� ��ɾ� nixbpe
																// ���� e�� 1
			{
				locctr += 4;// 4������ 4����Ʈ�� ����ϹǷ�
				return 0;
			} else if (instTable.get(p).getFormat().equals("3/4")) {// 3���� ��ɾ�
				locctr += 3;// 3������ 3����Ʈ
				return 0;
			} else if (instTable.get(p).getFormat().equals("2"))// 2���� ��ɾ�
			{
				locctr += 2;// 2����Ʈ
				return 0;
			} else if (instTable.get(p).getFormat().equals("1"))// 1���� ��ɾ�
			{
				locctr += 1;// 1����Ʈ
				return 0;
			}
		} else {
			tokenTable.get(index).setopcode(-1);// �̿��� ��ɾ�� opcode�� -1�� �ʱ�ȭ (�ؿ���
												// �����ڿ� ���� ���� �ٽ� �������ش�)
			if (tokenTable.get(index).getOpreatioin().equals("START")) {
				tokenTable.get(index).setlocctr(0, sect);
				locctr = 0;// START��� ������ �ʱ�ȭ
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("END")) {
				// ltorg��� �߰� (���� ����� �ϹǷ� )
				int litsize;
				tokenTable.get(index).setlocctr(-1, sect);
				if ((litsize = litTable.size()) > 0) {// ���ͷ��� �ִ� ���� ŭ ���� �߰�
					for (int n = 0; n < litsize; n++) {

						String[] temp = { "" };
						tokenTable.add(index + n + 1, new TokenData("*", litTable.get(n).getName(), temp, ""));
						tokenTable.get(index + n + 1).setlocctr(locctr, sect);
						tokenTable.get(index + n + 1).setopcode(-1);
						// ���θ�������� ��ū�� ������ �������� (���̺�, ������, �ּ�,����)
						symTable.add(new SymbolData(litTable.get(n).getName(), locctr, sect));
						symTable.get(symTable.size() - 1).setType(1);// �ɺ��� ���
						locctr += litTable.get(n).getLength();
					}

				}
				litTable.clear();// ����Ͽ����Ƿ� ���ͷ� ���̺��� �ʱ�ȭ���ش�
				return litsize;// ����� ���ͷ��� ������ŭ �Ѿ�� ���Ͽ�
			} else if (tokenTable.get(index).getOpreatioin().equals("BYTE")) {

				if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'C') {
					locctr += tokenTable.get(index).getIndexedOpernd(0).split("'")[1].length();// ����Ÿ���̹Ƿ�
																								// ���̸�ŭ��
																								// ��
																								// ����Ʈ
				} else if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'X') {
					locctr += tokenTable.get(index).getIndexedOpernd(0).split("'")[1].length() / 2;// ����Ÿ���̹Ƿ�
																									// ��
																									// ���ڰ�
																									// ����
																									// ����Ʈ
				} else {// ����
					return -1;
				}
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("WORD")) {
				locctr += 3;// �� word�� 3����Ʈ �̹Ƿ�
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("RESB")) {
				locctr += Integer.parseInt(tokenTable.get(index).getIndexedOpernd(0));// �ǿ����ڷ�
																						// �Ҵ��ϰ�
																						// ����
																						// ��ŭ��
																						// ����Ʈ����
																						// ��ϵǹǷ�
																						// �׸�ŭ
																						// �����̼�
																						// ī���͸�
																						// ������Ŵ
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("RESW")) {
				locctr += Integer.parseInt(tokenTable.get(index).getIndexedOpernd(0)) * 3;
				// �ǿ����ڷ� �Ҵ��ϰ� ���� ��ŭ�� ������� ��ϵǹǷ� �� ����Ʈ ��ŭ �����̼� ī���͸� ������Ŵ
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("EXTDEF")) {
				// ���⿡ �ܺ����� ����
				tokenTable.get(index).setlocctr(-1, sect);
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("EXTREF")) {
				// ���⿡ �ܺ����� ����
				tokenTable.get(index).setlocctr(-1, sect);
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("EQU")) {
				tokenTable.get(index).setlocctr(-1, sect);
				int h, sym1, sym2;
				if ((h = myAssembler.has_op(tokenTable.get(index).getIndexedOpernd(0))) != 0) {// �����ڸ�
																								// ������
																								// �ִٸ�

					String temp[] = tokenTable.get(index).getIndexedOpernd(0).split("[+|-]", 2);// +-��
																								// ����

					if (((sym1 = myAssembler.search_symbloc(temp[0], sect)) > 0)
							&& ((sym2 = myAssembler.search_symbloc(temp[1], sect)) > 0)) {// �Ѵ�
																							// �ɺ��̸�

						if (h > 0) {// �����ڰ� +
							if ((symTable.get(sym1).getType() == 2) && (symTable.get(sym2).getType() == 2))// �Ѵ�
																											// ���밪�̸�
							{
								symTable.get(symTable.size() - 1).setType(2);// �Ѵ�
																				// ���밪�̸�
																				// �����
																				// ���밪
							} else {// �ƴ϶��
								symTable.get(symTable.size() - 1).setType(1);// �ϳ���
																				// ��밪�̸�
																				// ��밪

							}
							symTable.get(symTable.size() - 1)
									.setAdd(symTable.get(sym1).getAdd() + symTable.get(sym2).getAdd());// �����
																										// �ɺ���
																										// �ּҿ�
																										// ����
						} else if (h < 0)// �����ڰ� -
						{
							if (symTable.get(sym1).getType() == symTable.get(sym2).getType())// �ΰ���
																								// Ÿ����
																								// ���ٸ�
							{
								symTable.get(symTable.size() - 1).setType(2);// ������
																				// ���밪
							} else {// �ƴ϶��
								symTable.get(symTable.size() - 1).setType(1);// �ϳ���
																				// ��밪�̸�
																				// ��밪

							}
							symTable.get(symTable.size() - 1)

									.setAdd(symTable.get(sym1).getAdd() - symTable.get(sym2).getAdd());
						}
						tokenTable.get(index).setlocctr(symTable.get(symTable.size() - 1).getAdd(), sect);// ���
																											// �ɺ���
																											// �ּҸ�
																											// ��
																											// ��ū���̺���
																											// �����̼�
																											// ī����
																											// ������
																											// ����
					} else {
						return -1;// �ΰ��� �ɺ��� �ƴѰ�� ������ ���
					}
				}

				else if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '*')// ����
																					// �����ڰ�
																					// *(����
																					// �ּҰ���
																					// ����
																					// )
				{
					symTable.get(symTable.size() - 1).setAdd(locctr);// ���� �ɺ���
																		// �ּҸ�
																		// �����̼�
																		// ī���ͷ�
																		// ����
					symTable.get(symTable.size() - 1).setType(1);// Ÿ���� ��밪
																	// (�ּ��̹Ƿ� )
					tokenTable.get(index).setlocctr(symTable.get(symTable.size() - 1).getAdd(), sect); // �ɺ���
																										// �ּҸ�
																										// ��
																										// ��ū���̺���
																										// �����̼�
																										// ī����
																										// ������
																										// ����

				}
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("LTORG")) {
				// ltorg��� �߰�
				int litsize;
				tokenTable.get(index).setlocctr(-1, sect);
				if ((litsize = litTable.size()) > 0) {// ���ͷ��� �ִ� ���� ŭ ���� �߰�
					for (int n = 0; n < litsize; n++) {

						String[] temp = { "" };
						tokenTable.add(index + n + 1, new TokenData("*", litTable.get(n).getName(), temp, ""));
						tokenTable.get(index + n + 1).setlocctr(locctr, sect);
						tokenTable.get(index + n + 1).setopcode(-1);

						symTable.add(new SymbolData(litTable.get(n).getName(), locctr, sect));
						symTable.get(symTable.size() - 1).setType(1);
						locctr += litTable.get(n).getLength();
					}

				}
				litTable.clear();
				return litsize;
			}

		}
		return -1;
	}

	public int search_symbloc(String str, int sect) {
		String tem = str;

		if ((tem.charAt(0) == '#') || (tem.charAt(0) == '@'))
			tem = tem.substring(1);

		for (int i = 0; i < symTable.size(); i++) {// ��ü �ɺ��� ���� ���� Ž���ѵ�
			if (sect == symTable.get(i).getSect())// ���� ������ ��쿡�� �˻�����
				if (tem.equals(symTable.get(i).getSymbol()))// ���� �ִ� �ɺ��̶��
					return i;// �ɺ��� �ε����� �����Ѵ�.
		}
		return -1;// ���ٸ� -1
	}

	private int has_op(String str) {

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '+')
				return i; // +�������� ��ġ
			if (str.charAt(i) == '-')
				return -i;// -�������� ��ġ
		}
		return 0;
	}

	private int search_opcode(String opreatioin) {
		for (int i = 0; i < instTable.size(); i++) {
			if (instTable.get(i).getOperation().equals(opreatioin))// instset��
																	// �ִ� �������ΰ��
																	// �� �ּҸ� ����
				return i;
		}

		return -1;
	}

	private int search_literal(String string) {

		for (int i = 0; i < litTable.size(); i++) {
			if (litTable.get(i).getName().equals(string))// ���ͷ� ���̺� ��ü�� �˻��ؼ� ã����
															// �ּҸ� ����
				return i;
		}

		return -1;

	}

	private int search_ref(String str, int sect) {

		String tem = str;

		for (int i = 0; i < extref.size(); i++) {// ��ü �ɺ��� ���� ���� Ž���ѵ�
			if (extref.get(i).getSect() == sect)// �ش� ���͸� �˻�
				if (tem.equals(extref.get(i).getName()))// ���� �ִ� �ɺ��̶��
					return i;// �ɺ��� �ε����� �����Ѵ�.
		}

		return -1;
	}

	private int assem_pass2() {// object�ڵ带 ������ִ� �Լ��� ����

		for (int i = 0; i < tokenTable.size(); i++) {

			myAssembler.make_objectcode(i);
			tokenTable.get(i).print();// ������� object �ڵ带 ȭ�鿡 �������
		}
		return 0;
	}

	private int make_objectcode(int index) {

		int p = 0, s = 0;
		if (tokenTable.get(index).getOpreatioin().equals("CSECT")) {// ���� ���ͷ�
																	// �Ѿ�� ���
			sect++;
			return 0;
		}

		if ((p = myAssembler.search_opcode(tokenTable.get(index).getOpreatioin())) != -1) {// ����
																							// opcode��
																							// �ִ�
																							// �����̶��
			if ((tokenTable.get(index).getNixbpe() & 1) == 1) {// 4����
				if (tokenTable.get(index).getIndexedOpernd(1) != null)
					if (tokenTable.get(index).getIndexedOpernd(1).equals("X"))// nixbpe�߿�
																				// x��Ʈ
																				// üũ
					{
						tokenTable.get(index).setNixbpe(8);
					}
				if ((tokenTable.get(index).getNixbpe() & 48) == 0)// n�̳� i��
																	// �����ִ°� �ƴϸ�
					tokenTable.get(index).setNixbpe(48);// ni bit Ŵ
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getopcode() * 0x1000000);// opcode
																									// �ڸ���
																									// ����
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getNixbpe() * 0x100000);//// nixbpe
																									//// �ڸ���
																									//// ����
				if (!tokenTable.get(index).getIndexedOpernd(0).equals(""))
					if ((s = myAssembler.search_symbloc(tokenTable.get(index).getIndexedOpernd(0), sect)) != -1)// �ɺ���
																												// �ִٸ�
					{
						tokenTable.get(index).setObjectcode(symTable.get(s).getAdd());
					} else if (myAssembler.search_ref(tokenTable.get(index).getIndexedOpernd(0), sect) != -1) {// ����
																												// ������
																												// ������
																												// ����ߴٸ�
						refTable.add(new refrenceData(tokenTable.get(index).getIndexedOpernd(0),
								tokenTable.get(index).getlocctr() + 1, sect));
						refTable.get(refTable.size() - 1).setSize(5);// 4���Ŀ���
																		// ��������Ƿ�
																		// ������
																		// ���̴� 5

					} else if (((tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '#')
							&& ((tokenTable.get(index).getNixbpe() & 16) == 16))) {// �ɺ���
																					// ����
																					// i
																					// bit��
																					// 1�ϋ�

						String temp = tokenTable.get(index).getIndexedOpernd(0).substring(1);// #��
																								// ��
																								// Ÿ��
																								// �����
						tokenTable.get(index).setObjectcode(Integer.parseInt(temp));// objectcode��
																					// �ִ´�.
					}
			} else if (instTable.get(p).getFormat().equals("3/4"))// 3����
			{
				if (tokenTable.get(index).getIndexedOpernd(1) != null)
					if (tokenTable.get(index).getIndexedOpernd(1).equals("X"))// nixbpe�߿�
																				// x��Ʈ
																				// üũ
					{
						tokenTable.get(index).setNixbpe(8);
					}
				if (!tokenTable.get(index).getIndexedOpernd(0).equals(""))
					if ((s = myAssembler.search_symbloc(tokenTable.get(index).getIndexedOpernd(0), sect)) != -1)// �ɺ���
																												// �ִٸ�
					{
						if (Math.abs(symTable.get(s).getAdd() - (tokenTable.get(index).getlocctr() + 3)) < 0x1000) {
							tokenTable.get(index).setObjectcode(
									(symTable.get(s).getAdd() - (tokenTable.get(index).getlocctr() + 3)) & 0xfff);// �����ϰ��
																													// 3byte��
																													// ��������
						} else {// �ɺ��� ������ pc relative�� ����
							return -1;
						}
						tokenTable.get(index).setNixbpe(2);// pc ralative
					} else if (((tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '#')
							&& ((tokenTable.get(index).getNixbpe() & 16) == 16))) {// �ɺ���
																					// ����
																					// i
																					// bit��
																					// 1�ϋ�
						String temp = tokenTable.get(index).getIndexedOpernd(0).substring(1);// #��
																								// ��
																								// Ÿ��
																								// �����
						tokenTable.get(index).setObjectcode(Integer.parseInt(temp));// objectcode��
																					// �ִ´�.
					}
				if ((tokenTable.get(index).getNixbpe() & 48) == 0)// n�̳� i�� �ƴϸ�
					tokenTable.get(index).setNixbpe(48);// ni bit Ŵ
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getopcode() * 0x10000);// opcode
																									// �ڸ���
																									// ����
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getNixbpe() * 0x1000);//// nixbpe
																								//// �ڸ���
																								//// ����
			}

			else if (instTable.get(p).getFormat().equals("2")) {// 2����
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getopcode() * 0x100);// opcode
																								// �ڸ���
																								// ����
				tokenTable.get(index)
						.setObjectcode(myAssembler.find_reg(tokenTable.get(index).getIndexedOpernd(0).charAt(0)) * 16);// 2������
																														// ù����
																														// ��������
				if (tokenTable.get(index).getIndexedOpernd(1) != null)
					tokenTable.get(index)
							.setObjectcode(myAssembler.find_reg(tokenTable.get(index).getIndexedOpernd(1).charAt(0)));// 2������
																														// �ι�°
																														// ��������
			} else if (instTable.get(p).getFormat().equals("1"))
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getopcode());// opcode
																						// �ڸ���
																						// ����
		} else {
			if (tokenTable.get(index).getOpreatioin().equals("BYTE")) {// ���⿡
				String temp = tokenTable.get(index).getIndexedOpernd(0).split("'")[1];

				if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'C') {// Ÿ����
																					// ĳ������
																					// ���
					tokenTable.get(index).setObjectcode(temp.charAt(0));
					for (int i = 1; i < temp.length(); i++) {
						tokenTable.get(index).objectcode *= 0x100;
						tokenTable.get(index).setObjectcode(temp.charAt(i));
					} // �α��ھ� ������ �и鼭 ���
				} else if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'X') {
					tokenTable.get(index).setObjectcode(Integer.parseInt(temp, 16));// �����ΰ��
																					// 16������
																					// �ٲ㼭
																					// ���
				} else {// ����
					return -1;
				}
			} else if (tokenTable.get(index).getOpreatioin().equals("WORD")) {
				// ���⿡ WORD ����

				int PorM;
				if ((PorM = myAssembler.has_op(tokenTable.get(index).getIndexedOpernd(0))) != 0) {
					String temp[] = tokenTable.get(index).getIndexedOpernd(0).split("[+|-]", 2);

					if ((myAssembler.search_ref(temp[0], sect) > 0) && (myAssembler.search_ref(temp[1], sect) > 0)) {

						if (PorM > 0) {// �����ڰ� +
							refTable.add(new refrenceData(temp[0], tokenTable.get(index).getlocctr(), sect));
							refTable.get(refTable.size() - 1).setSize(6);// ����
																			// ����ؾ��ϹǷ�
																			// 3����Ʈ

							refTable.add(new refrenceData(temp[1], tokenTable.get(index).getlocctr(), sect));
							refTable.get(refTable.size() - 1).setSize(6);// ����
																			// ����ؾ��ϹǷ�
																			// 3����Ʈ
						} else if (PorM < 0)// �����ڰ� -
						{
							refTable.add(new refrenceData(temp[0], tokenTable.get(index).getlocctr(), sect));
							refTable.get(refTable.size() - 1).setSize(6);// ����
																			// ����ؾ��ϹǷ�
																			// 3����Ʈ

							refTable.add(new refrenceData(temp[1], tokenTable.get(index).getlocctr(), sect));
							refTable.get(refTable.size() - 1).setSize(6);// ����
																			// ����ؾ��ϹǷ�
																			// 3����Ʈ
							refTable.get(refTable.size() - 1).setOp(1);// ���� ������
																		// ��������Ƿ�
																		// �����ڴ�
																		// -(=Ÿ����
																		// 1)
						}
					} else
						return 0;
				} else {
					tokenTable.get(index).setObjectcode(Integer.parseInt(tokenTable.get(index).getIndexedOpernd(0)));
				}

				return 0;
			}

			else if (tokenTable.get(index).getOpreatioin().equals("EXTDEF")) {

				for (int i = 0; i < tokenTable.get(index).getOperandNum(); i++) {
					int k = myAssembler.search_symbloc(tokenTable.get(index).getIndexedOpernd(i), sect);
					extdef.add(new refrenceData(tokenTable.get(index).getIndexedOpernd(i), symTable.get(k).getAdd(),
							sect));// �ܺο��� ������ ����� ������ �˷��ֹǷ� �ɺ��� �ּҸ� �˻��� ���� ���
				}
				return 0;
			}

			else if (tokenTable.get(index).getOpreatioin().equals("EXTREF")) {

				for (int i = 0; i < tokenTable.get(index).getOperandNum(); i++)
					extref.add(new refrenceData(tokenTable.get(index).getIndexedOpernd(i), -1, sect));
				// �ܺο��� �����ͼ� ���� ������ �˷��ֹǷ� �̸��� ����ص�
				return 0;
			} else if (tokenTable.get(index).getLabel().equals("*")) {// ���ͷ� �ϰ��
				String temp = tokenTable.get(index).getOpreatioin().split("'")[1];

				if (tokenTable.get(index).getOpreatioin().charAt(1) == 'C') {// Ÿ����
																				// ĳ����
					tokenTable.get(index).setObjectcode(temp.charAt(0));
					for (int i = 1; i < temp.length(); i++) {
						tokenTable.get(index).objectcode *= 0x100;
						tokenTable.get(index).setObjectcode(temp.charAt(i));
					} // �α��ھ� �и鼭 ��� objectcode�� ���
				} else if (tokenTable.get(index).getOpreatioin().charAt(1) == 'X') {// ������
																					// ���
					tokenTable.get(index).setObjectcode(Integer.parseInt(temp, 16));// 16��������
																					// ���
				} else {// ����
					return -1;
				}
				return 0;
			}

		}
		return 0;

	}

	public int find_reg(char r) {// �������� �ѹ��� ��������

		switch (r) {
		case 'A':
			return 0;
		case 'X':
			return 1;
		case 'L':
			return 2;
		case 'B':
			return 3;
		case 'S':
			return 4;
		case 'T':
			return 5;
		case 'F':
			return 6;
		case 'P':
			return 8;
		default:
			return 0;
		}
	}

	private void make_objectcode_output(String string) {
		try {
			BufferedWriter writeBuffer = new BufferedWriter(new FileWriter(new File(string)));
			int record = 0;
			int sum = 0;
			int adr = 0;
			sect = 0;
			String buf = "";
			String temp = "";

			for (int index = 0; index < tokenTable.size(); index++) {

				int j = 0;
				if ((j = myAssembler.search_opcode(tokenTable.get(index).getOpreatioin())) != -1)// ���࿬���ڰ�instructionset���ִٸ�
				{

					if (instTable.get(j).getFormat().equals("2")) {
						buf = String.format("%04X", tokenTable.get(index).getObjectcode());// opcode��16��������
						record = 4;
					}
					if (instTable.get(j).getFormat().equals("1")) {
						buf = String.format("%02X", tokenTable.get(index).getObjectcode());// opcode��16��������
						record = 2;
					}
					if ((tokenTable.get(index).getNixbpe() & 1) == 1) {
						buf = String.format("%08X", tokenTable.get(index).getObjectcode());// opcode��16��������
						record = 8;
					} else if (instTable.get(j).getFormat().equals("3/4")) {
						buf = String.format("%06X", tokenTable.get(index).getObjectcode());// opcode��16��������
						record = 6;
					}
				} else if (tokenTable.get(index).getOpreatioin().equals("START")
						|| tokenTable.get(index).getOpreatioin().equals("CSECT"))// ���ο�H���ڵ尡������ġ
				{
					if (tokenTable.get(index).getOpreatioin().equals("CSECT"))// ù���ڵ�������ƴϸ�
					{
						sect++;
						adr = 0;// �����ּ� �ʱ�ȭ
					}
					int r = 0;
					writeBuffer.write(String.format("H%s", tokenTable.get(index).getLabel()));
					for (r = tokenTable.get(index).getLabel().length(); r < 6; r++)
						writeBuffer.write(" ");
					writeBuffer.write(String.format("%012X%n", sectSize[sect]));
					continue;
				} else if (tokenTable.get(index).getOpreatioin().equals("EXTDEF"))// �ܺ����Ƿ��ڵ尡���������Ѵ�
				{
					writeBuffer.write("D");
					for (int k = 0; k < 3; k++) {
						if (tokenTable.get(index).getIndexedOpernd(k) != null)
							writeBuffer.write(String.format("%6s%06X", tokenTable.get(index).getIndexedOpernd(k),
									symTable.get(
											myAssembler.search_symbloc(tokenTable.get(index).getIndexedOpernd(k), sect))
											.getAdd()));
						else
							break;
					}
					writeBuffer.write(String.format("%n"));
				} else if (tokenTable.get(index).getOpreatioin().equals("EXTREF"))// �ܺ��������ڵ尡���������Ѵ�.
				{
					writeBuffer.write("R");
					for (int k = 0; k < 3; k++) {
						if (tokenTable.get(index).getIndexedOpernd(k) != null)
							writeBuffer.write(String.format("%-6s", tokenTable.get(index).getIndexedOpernd(k)));
						else
							break;
					}
					writeBuffer.write(String.format("%n"));
				} else if (tokenTable.get(index).getOpreatioin().equals("WORD")) {
					buf = String.format("%06X", tokenTable.get(index).getObjectcode());
					record = 6;
				} else if (tokenTable.get(index).getOpreatioin().equals("BYTE")) {
					int k = tokenTable.get(index).getIndexedOpernd(0).split("'")[1].length();

					if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'C')// Ÿ�Ա���
						record = k * 2;
					else if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'X')
						record = k;

					switch (record)// �ʵ��� ���� �ٸ��� ������ �ڸ�����  �����ؼ� ������ش�
					{
					case 1:
						buf = String.format("%01X", tokenTable.get(index).getObjectcode());
						break;
					case 2:
						buf = String.format("%02X", tokenTable.get(index).getObjectcode());
						break;
					case 3:
						buf = String.format("%03X", tokenTable.get(index).getObjectcode());
						break;
					case 4:
						buf = String.format("%04X", tokenTable.get(index).getObjectcode());
						break;
					case 5:
						buf = String.format("%05X", tokenTable.get(index).getObjectcode());
						break;
					case 6:
						buf = String.format("%06X", tokenTable.get(index).getObjectcode());
						break;
					case 7:
						buf = String.format("%07X", tokenTable.get(index).getObjectcode());
						break;
					case 8:
						buf = String.format("%08X", tokenTable.get(index).getObjectcode());
						break;
					}
				} else if (tokenTable.get(index).getLabel().equals("*"))// ����
																		// ���ͷ��̿´ٸ�
				{
					int k = tokenTable.get(index).getOpreatioin().split("'")[1].length();

					if (tokenTable.get(index).getOpreatioin().charAt(1) == 'C')// Ÿ�Ա���
						record = k * 2;
					else if (tokenTable.get(index).getOpreatioin().charAt(1) == 'X')
						record = k;

					switch (record)// �ʵ��� ���� �ٸ��� ������ �ڸ����� �����ؼ� ������ش�
					{
					case 1:
						buf = String.format("%01X", tokenTable.get(index).getObjectcode());
						break;
					case 2:
						buf = String.format("%02X", tokenTable.get(index).getObjectcode());
						break;
					case 3:
						buf = String.format("%03X", tokenTable.get(index).getObjectcode());
						break;
					case 4:
						buf = String.format("%04X", tokenTable.get(index).getObjectcode());
						break;
					case 5:
						buf = String.format("%05X", tokenTable.get(index).getObjectcode());
						break;
					case 6:
						buf = String.format("%06X", tokenTable.get(index).getObjectcode());
						break;
					case 7:
						buf = String.format("%07X", tokenTable.get(index).getObjectcode());
						break;
					case 8:
						buf = String.format("%08X", tokenTable.get(index).getObjectcode());
						break;
					}
				} else {// object code ��¿� ���þ��� ��ɾ� �����̶�� ����
					buf = "";
					record = 0;
				}

				if ((sum + record) > 59)// 10~69�� �Ѵ°�� ���� T���ڵ�� �Ѿ
				{
					writeBuffer.write(String.format("T%06X%02X", adr, sum / 2));
					writeBuffer.write(String.format("%s%n", temp)); // temp�������صξ������ڵ�
																	// ���

					sum = record;
					temp = buf;
					adr = tokenTable.get(index).getlocctr();
					continue;
				} else if (index == tokenTable.size() - 1)// ������ ���̰ų�
				{
					sum += record;
					temp += buf;
					writeBuffer.write(String.format("T%06X%02X%s%n", adr, sum / 2, temp));

					buf = "";
					temp = "";

					record = 0;
					sum = 0;

					for (int v = 0; v < refTable.size(); v++) {
						if (sect == refTable.get(v).getSect()) {
							writeBuffer.write(String.format("M%06X", refTable.get(v).getAddr()));
							if (refTable.get(v).getSize() == 5)//
								writeBuffer.write("05");
							else
								writeBuffer.write("06");
							if (refTable.get(v).getOp() == 0)// ������ ������ �ľ��Ѵ�
								writeBuffer.write("+");// ���� �ּҰ���ŭ ���ؾ��Ѵٸ�
							else
								writeBuffer.write("-");// �����ϴ°����
							writeBuffer.write(String.format("%s%n", refTable.get(v).getName()));// ��������ġ���ּ�
						}
					}
					writeBuffer.write(String.format("E000000%n"));
					continue;
				} else if (tokenTable.get(index + 1).getOpreatioin().equals("LTORG")
						|| tokenTable.get(index + 1).getOpreatioin().equals("CSECT")) {// �����ٲ���ϴ°��
					sum += record;
					temp += buf;
					writeBuffer.write(String.format("T%06X%02X", adr, sum / 2));
					writeBuffer.write(String.format("%s%n", temp));// temp�������صξ������ڵ�
																	// ���
					sum = 0;
					record = 0;
					temp = "";
					buf = "";
					adr = tokenTable.get(index + 2).getlocctr();
					if (tokenTable.get(index + 1).getOpreatioin().equals("CSECT"))// �����������ΰ��°����
					{
						for (int v = 0; v < refTable.size(); v++) {// M ���ڵ� ���
							if (sect == refTable.get(v).getSect()) {
								writeBuffer.write(String.format("M%06X", refTable.get(v).getAddr()));
								if (refTable.get(v).getSize() == 5)
									writeBuffer.write("05");
								else
									writeBuffer.write("06");
								if (refTable.get(v).getOp() == 0)// ������ ������ �ľ��Ѵ�
									writeBuffer.write("+");// ���� �ּҰ���ŭ ���ؾ��Ѵٸ�
								else
									writeBuffer.write("-");// �����ϴ°����
								writeBuffer.write(String.format("%s%n", refTable.get(v).getName()));// ��������ġ���ּ�
							}
						}
						writeBuffer.write(String.format("E000000%n%n"));
					}
					continue;
				}
				if (record != 0) {
					sum += record;
					temp += buf;

				}
			}
			writeBuffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
