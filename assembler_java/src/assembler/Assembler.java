package assembler;

import java.io.*;
import java.util.ArrayList;

public class Assembler {
	static Assembler myAssembler = new Assembler();
	static ArrayList<InstData> instTable = new ArrayList<InstData>();//inst.txt에 저장된 정보를 저장한다
	static ArrayList<TokenData> tokenTable = new ArrayList<TokenData>();//input.txt에 저장된 정보를 저장한다
	static ArrayList<SymbolData> symTable = new ArrayList<SymbolData>();//심볼테이블
	static ArrayList<LitralData> litTable = new ArrayList<LitralData>();//리터럴 테이블
	static ArrayList<refrenceData> refTable = new ArrayList<refrenceData>();//참초에 대한 정보를 저장한 테이블 
	
	static ArrayList<refrenceData> extref = new ArrayList<refrenceData>();//외부 참조를 기록 
	static ArrayList<refrenceData> extdef = new ArrayList<refrenceData>();//외부 정의를 기록

	int sect = 0;
	int locctr = 0;
	int[] sectSize = new int[10];// 각섹터의길이를저장

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

	private int init_my_assembler() {// 어셈블러에 필요한 데이터 파일들을 초기화하는 부분
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

				String[] values = line.split("\t", 4);// tab을 구분으로 데이터를 저장하였으므로
				instTable.add(new InstData(values[0], values[1], Integer.parseInt(values[2], 16),
						Integer.parseInt(values[3], 10)));// 세번쨰 맴버인 opcode과 네번째
															// 맴버인 연산자의 갯수는
															// 각각 16진수, 10진수로
															// 저장되어있으므로

			}
			instBuffer.close();// instData에 입력이 끝났으므로 닫는다
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
					String[] values = line.split("\t", 4);// 탭 구분으로 개행하였으므로

					String[] operand = values[2].split(",", 3);// 세번째 맴버 피연산자는
																// 또다시 콤마를 기준으로
																// 한번더 나눠준다
					tokenTable.add(new TokenData(values[0], values[1], operand, values[3]));// tokenData에
																							// 각각의
																							// 값들을
																							// 저장해준다
				}
			}
			inputBuffer.close();// input를 토큰으로 나누어 저장이 끝났으므로 닫는다
		} catch (Exception ex) {
			System.out.println("error at init input data\n");
			return -1;
		}
		return 0;
	}

	private int assem_pass1() {// 패스 1에서는 각각의 token을 이용하여 각 섹터마다 로케이션 카운터를 입력해주는
								// 함수이다

		for (int i = 0; i < tokenTable.size(); i++) {
			i += myAssembler.makelocctr(i);

		}
		sectSize[sect] = locctr;// 마지막 섹터의 길이를 저장
		sect = 0;
		return 0;
	}

	private int makelocctr(int index) {// 로케이션 카운터를 입력해준다

		int p = 0;
		if (tokenTable.get(index).getOpreatioin().equals("CSECT")) {// 다음 섹터로
																	// 넘어가는 명령어
																	// 이므로
			sectSize[sect] = locctr;// 섹터의 길이를 저장
			sect++;// 다음 섹터로 넘어감
			tokenTable.get(index).setlocctr(-1, sect);// 로케이션 카운터를 사용하지 않으므로 -1로
														// 저장
			locctr = 0;// 로케이션 카운터를 새로 매겨야 하므로 0으로 초기화
			return 0;
		}

		if (!tokenTable.get(index).getIndexedOpernd(0).equals(""))// 피연산자가 비어있다면
																	// 넘어감
			if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '=')// 피연산자가
																			// 리터럴일경우
			{
				if ((p = myAssembler.search_literal(tokenTable.get(index).getIndexedOpernd(0))) != -1)// 리터럴풀에이미있는리터럴이라면

					litTable.get(p).inputAddress(tokenTable.get(index).getlocctr());// 주소만저장

				else {// 리터럴 풀에 넣는다
					if (tokenTable.get(index).getIndexedOpernd(0).charAt(1) == 'C') {// 타입을
																						// 구분
						litTable.add(new LitralData(tokenTable.get(index).getIndexedOpernd(0), 1));// 캐릭터형이므로
																									// 타입은
																									// 1
					} else if (tokenTable.get(index).getIndexedOpernd(0).charAt(1) == 'X') {
						litTable.add(new LitralData(tokenTable.get(index).getIndexedOpernd(0), 2));// 숫자형이므로
																									// 타입은
																									// 2

					} else {// 에러
						return -1;
					}
				}
			}
		p = 0;
		if (!tokenTable.get(index).getLabel().equals("") && !tokenTable.get(index).getLabel().equals("*")) {// 레이블이
																											// 있으면
			symTable.add(new SymbolData(tokenTable.get(index).getLabel(), locctr, sect));// 심볼로
																							// 등록함
			symTable.get(symTable.size() - 1).setType(1);
			// 심볼테이블에 값,주소,섹터를 넣어줌
		}

		tokenTable.get(index).setlocctr(locctr, sect);// 로케이션 카운터를 일괄적으로 저장 (밑에서
														// 사용에 따라서 다시 값을 세팅해줌)

		if ((p = myAssembler.search_opcode(tokenTable.get(index).getOpreatioin())) != -1) {// 명령어가
																							// optable에
																							// 있는경우

			if (!tokenTable.get(index).getIndexedOpernd(0).equals("")) {
				if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '#')// 피연산자
																				// 앞에
																				// #
					tokenTable.get(index).setNixbpe(16);// immediate
				if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '@')// 피연산자
																				// 앞에
																				// @
					tokenTable.get(index).setNixbpe(32);// indirect

			}
			tokenTable.get(index).setopcode(instTable.get(p).getOpcode());// 받아온
																			// opcode저장
			if ((tokenTable.get(index).getNixbpe() & 1) == 1)// 4형식 명령어 nixbpe
																// 에서 e가 1
			{
				locctr += 4;// 4형식은 4바이트를 사용하므로
				return 0;
			} else if (instTable.get(p).getFormat().equals("3/4")) {// 3형식 명령어
				locctr += 3;// 3형식은 3바이트
				return 0;
			} else if (instTable.get(p).getFormat().equals("2"))// 2형식 명령어
			{
				locctr += 2;// 2바이트
				return 0;
			} else if (instTable.get(p).getFormat().equals("1"))// 1형식 명령어
			{
				locctr += 1;// 1바이트
				return 0;
			}
		} else {
			tokenTable.get(index).setopcode(-1);// 이외의 명령어는 opcode를 -1로 초기화 (밑에서
												// 연산자에 따라서 값을 다시 저장해준다)
			if (tokenTable.get(index).getOpreatioin().equals("START")) {
				tokenTable.get(index).setlocctr(0, sect);
				locctr = 0;// START라면 값들을 초기화
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("END")) {
				// ltorg기능 추가 (같은 기능을 하므로 )
				int litsize;
				tokenTable.get(index).setlocctr(-1, sect);
				if ((litsize = litTable.size()) > 0) {// 리터럴에 있는 값만 큼 새로 추가
					for (int n = 0; n < litsize; n++) {

						String[] temp = { "" };
						tokenTable.add(index + n + 1, new TokenData("*", litTable.get(n).getName(), temp, ""));
						tokenTable.get(index + n + 1).setlocctr(locctr, sect);
						tokenTable.get(index + n + 1).setopcode(-1);
						// 새로만들어지는 토큰에 값들을 저장해줌 (레이블, 연산자, 주소,섹터)
						symTable.add(new SymbolData(litTable.get(n).getName(), locctr, sect));
						symTable.get(symTable.size() - 1).setType(1);// 심볼에 등록
						locctr += litTable.get(n).getLength();
					}

				}
				litTable.clear();// 기록하였으므로 리터럴 테이블은 초기화해준다
				return litsize;// 등록한 리터럴의 갯수만큼 넘어가기 위하여
			} else if (tokenTable.get(index).getOpreatioin().equals("BYTE")) {

				if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'C') {
					locctr += tokenTable.get(index).getIndexedOpernd(0).split("'")[1].length();// 문자타입이므로
																								// 길이만큼이
																								// 한
																								// 바이트
				} else if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'X') {
					locctr += tokenTable.get(index).getIndexedOpernd(0).split("'")[1].length() / 2;// 숫자타입이므로
																									// 한
																									// 문자가
																									// 하프
																									// 바이트
				} else {// 에러
					return -1;
				}
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("WORD")) {
				locctr += 3;// 한 word는 3바이트 이므로
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("RESB")) {
				locctr += Integer.parseInt(tokenTable.get(index).getIndexedOpernd(0));// 피연산자로
																						// 할당하고
																						// 싶은
																						// 만큼의
																						// 바이트수가
																						// 기록되므로
																						// 그만큼
																						// 로케이션
																						// 카운터를
																						// 증가시킴
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("RESW")) {
				locctr += Integer.parseInt(tokenTable.get(index).getIndexedOpernd(0)) * 3;
				// 피연산자로 할당하고 싶은 만큼의 워드수가 기록되므로 그 바이트 만큼 로케이션 카운터를 증가시킴
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("EXTDEF")) {
				// 여기에 외부정의 관련
				tokenTable.get(index).setlocctr(-1, sect);
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("EXTREF")) {
				// 여기에 외부참조 관련
				tokenTable.get(index).setlocctr(-1, sect);
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("EQU")) {
				tokenTable.get(index).setlocctr(-1, sect);
				int h, sym1, sym2;
				if ((h = myAssembler.has_op(tokenTable.get(index).getIndexedOpernd(0))) != 0) {// 연산자를
																								// 가지고
																								// 있다면

					String temp[] = tokenTable.get(index).getIndexedOpernd(0).split("[+|-]", 2);// +-로
																								// 나눔

					if (((sym1 = myAssembler.search_symbloc(temp[0], sect)) > 0)
							&& ((sym2 = myAssembler.search_symbloc(temp[1], sect)) > 0)) {// 둘다
																							// 심볼이면

						if (h > 0) {// 연산자가 +
							if ((symTable.get(sym1).getType() == 2) && (symTable.get(sym2).getType() == 2))// 둘다
																											// 절대값이면
							{
								symTable.get(symTable.size() - 1).setType(2);// 둘다
																				// 절대값이면
																				// 결과도
																				// 절대값
							} else {// 아니라면
								symTable.get(symTable.size() - 1).setType(1);// 하나라도
																				// 상대값이면
																				// 상대값

							}
							symTable.get(symTable.size() - 1)
									.setAdd(symTable.get(sym1).getAdd() + symTable.get(sym2).getAdd());// 결과를
																										// 심볼의
																										// 주소에
																										// 저장
						} else if (h < 0)// 연산자가 -
						{
							if (symTable.get(sym1).getType() == symTable.get(sym2).getType())// 두개의
																								// 타입이
																								// 같다면
							{
								symTable.get(symTable.size() - 1).setType(2);// 무조건
																				// 절대값
							} else {// 아니라면
								symTable.get(symTable.size() - 1).setType(1);// 하나라도
																				// 상대값이면
																				// 상대값

							}
							symTable.get(symTable.size() - 1)

									.setAdd(symTable.get(sym1).getAdd() - symTable.get(sym2).getAdd());
						}
						tokenTable.get(index).setlocctr(symTable.get(symTable.size() - 1).getAdd(), sect);// 방금
																											// 심볼의
																											// 주소를
																											// 그
																											// 토큰테이블의
																											// 로케이션
																											// 카운터
																											// 값으로
																											// 저장
					} else {
						return -1;// 두개다 심볼이 아닌경우 비정상 기록
					}
				}

				else if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '*')// 만약
																					// 연산자가
																					// *(현재
																					// 주소값을
																					// 저장
																					// )
				{
					symTable.get(symTable.size() - 1).setAdd(locctr);// 현재 심볼의
																		// 주소를
																		// 로케이션
																		// 카운터로
																		// 저장
					symTable.get(symTable.size() - 1).setType(1);// 타입은 상대값
																	// (주소이므로 )
					tokenTable.get(index).setlocctr(symTable.get(symTable.size() - 1).getAdd(), sect); // 심볼의
																										// 주소를
																										// 그
																										// 토큰테이블의
																										// 로케이션
																										// 카운터
																										// 값으로
																										// 저장

				}
				return 0;
			} else if (tokenTable.get(index).getOpreatioin().equals("LTORG")) {
				// ltorg기능 추가
				int litsize;
				tokenTable.get(index).setlocctr(-1, sect);
				if ((litsize = litTable.size()) > 0) {// 리터럴에 있는 값만 큼 새로 추가
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

		for (int i = 0; i < symTable.size(); i++) {// 전체 심볼의 수를 전부 탐색한뒤
			if (sect == symTable.get(i).getSect())// 같은 섹터인 경우에만 검색가능
				if (tem.equals(symTable.get(i).getSymbol()))// 만약 있는 심볼이라면
					return i;// 심볼의 인덱스를 리턴한다.
		}
		return -1;// 없다면 -1
	}

	private int has_op(String str) {

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '+')
				return i; // +연산자의 위치
			if (str.charAt(i) == '-')
				return -i;// -연산자의 위치
		}
		return 0;
	}

	private int search_opcode(String opreatioin) {
		for (int i = 0; i < instTable.size(); i++) {
			if (instTable.get(i).getOperation().equals(opreatioin))// instset에
																	// 있는 연산자인경우
																	// 그 주소를 리턴
				return i;
		}

		return -1;
	}

	private int search_literal(String string) {

		for (int i = 0; i < litTable.size(); i++) {
			if (litTable.get(i).getName().equals(string))// 리터럴 테이블 전체를 검색해서 찾으면
															// 주소를 리턴
				return i;
		}

		return -1;

	}

	private int search_ref(String str, int sect) {

		String tem = str;

		for (int i = 0; i < extref.size(); i++) {// 전체 심볼의 수를 전부 탐색한뒤
			if (extref.get(i).getSect() == sect)// 해당 섹터만 검색
				if (tem.equals(extref.get(i).getName()))// 만약 있는 심볼이라면
					return i;// 심볼의 인덱스를 리턴한다.
		}

		return -1;
	}

	private int assem_pass2() {// object코드를 만들어주는 함수를 실행

		for (int i = 0; i < tokenTable.size(); i++) {

			myAssembler.make_objectcode(i);
			tokenTable.get(i).print();// 만들어진 object 코드를 화면에 출력해줌
		}
		return 0;
	}

	private int make_objectcode(int index) {

		int p = 0, s = 0;
		if (tokenTable.get(index).getOpreatioin().equals("CSECT")) {// 다음 섹터로
																	// 넘어가는 경우
			sect++;
			return 0;
		}

		if ((p = myAssembler.search_opcode(tokenTable.get(index).getOpreatioin())) != -1) {// 만약
																							// opcode가
																							// 있는
																							// 동작이라면
			if ((tokenTable.get(index).getNixbpe() & 1) == 1) {// 4형식
				if (tokenTable.get(index).getIndexedOpernd(1) != null)
					if (tokenTable.get(index).getIndexedOpernd(1).equals("X"))// nixbpe중에
																				// x비트
																				// 체크
					{
						tokenTable.get(index).setNixbpe(8);
					}
				if ((tokenTable.get(index).getNixbpe() & 48) == 0)// n이나 i가
																	// 켜저있는게 아니면
					tokenTable.get(index).setNixbpe(48);// ni bit 킴
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getopcode() * 0x1000000);// opcode
																									// 자릿수
																									// 맞춤
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getNixbpe() * 0x100000);//// nixbpe
																									//// 자릿수
																									//// 맞춤
				if (!tokenTable.get(index).getIndexedOpernd(0).equals(""))
					if ((s = myAssembler.search_symbloc(tokenTable.get(index).getIndexedOpernd(0), sect)) != -1)// 심볼이
																												// 있다면
					{
						tokenTable.get(index).setObjectcode(symTable.get(s).getAdd());
					} else if (myAssembler.search_ref(tokenTable.get(index).getIndexedOpernd(0), sect) != -1) {// 만약
																												// 참조한
																												// 변수를
																												// 사용했다면
						refTable.add(new refrenceData(tokenTable.get(index).getIndexedOpernd(0),
								tokenTable.get(index).getlocctr() + 1, sect));
						refTable.get(refTable.size() - 1).setSize(5);// 4형식에서
																		// 사용했으므로
																		// 수정할
																		// 길이는 5

					} else if (((tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '#')
							&& ((tokenTable.get(index).getNixbpe() & 16) == 16))) {// 심볼이
																					// 없고
																					// i
																					// bit가
																					// 1일떄

						String temp = tokenTable.get(index).getIndexedOpernd(0).substring(1);// #을
																								// 뺀
																								// 타겟
																								// 밸류를
						tokenTable.get(index).setObjectcode(Integer.parseInt(temp));// objectcode에
																					// 넣는다.
					}
			} else if (instTable.get(p).getFormat().equals("3/4"))// 3형식
			{
				if (tokenTable.get(index).getIndexedOpernd(1) != null)
					if (tokenTable.get(index).getIndexedOpernd(1).equals("X"))// nixbpe중에
																				// x비트
																				// 체크
					{
						tokenTable.get(index).setNixbpe(8);
					}
				if (!tokenTable.get(index).getIndexedOpernd(0).equals(""))
					if ((s = myAssembler.search_symbloc(tokenTable.get(index).getIndexedOpernd(0), sect)) != -1)// 심볼이
																												// 있다면
					{
						if (Math.abs(symTable.get(s).getAdd() - (tokenTable.get(index).getlocctr() + 3)) < 0x1000) {
							tokenTable.get(index).setObjectcode(
									(symTable.get(s).getAdd() - (tokenTable.get(index).getlocctr() + 3)) & 0xfff);// 음수일경우
																													// 3byte만
																													// 쓰기위해
						} else {// 심볼이 있으나 pc relative를 못씀
							return -1;
						}
						tokenTable.get(index).setNixbpe(2);// pc ralative
					} else if (((tokenTable.get(index).getIndexedOpernd(0).charAt(0) == '#')
							&& ((tokenTable.get(index).getNixbpe() & 16) == 16))) {// 심볼이
																					// 없고
																					// i
																					// bit가
																					// 1일떄
						String temp = tokenTable.get(index).getIndexedOpernd(0).substring(1);// #을
																								// 뺀
																								// 타겟
																								// 밸류를
						tokenTable.get(index).setObjectcode(Integer.parseInt(temp));// objectcode에
																					// 넣는다.
					}
				if ((tokenTable.get(index).getNixbpe() & 48) == 0)// n이나 i가 아니면
					tokenTable.get(index).setNixbpe(48);// ni bit 킴
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getopcode() * 0x10000);// opcode
																									// 자릿수
																									// 맞춤
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getNixbpe() * 0x1000);//// nixbpe
																								//// 자릿수
																								//// 맞춤
			}

			else if (instTable.get(p).getFormat().equals("2")) {// 2형식
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getopcode() * 0x100);// opcode
																								// 자릿수
																								// 맞춤
				tokenTable.get(index)
						.setObjectcode(myAssembler.find_reg(tokenTable.get(index).getIndexedOpernd(0).charAt(0)) * 16);// 2형식의
																														// 첫번쩨
																														// 레지스터
				if (tokenTable.get(index).getIndexedOpernd(1) != null)
					tokenTable.get(index)
							.setObjectcode(myAssembler.find_reg(tokenTable.get(index).getIndexedOpernd(1).charAt(0)));// 2형식의
																														// 두번째
																														// 레지스터
			} else if (instTable.get(p).getFormat().equals("1"))
				tokenTable.get(index).setObjectcode(tokenTable.get(index).getopcode());// opcode
																						// 자릿수
																						// 맞춤
		} else {
			if (tokenTable.get(index).getOpreatioin().equals("BYTE")) {// 여기에
				String temp = tokenTable.get(index).getIndexedOpernd(0).split("'")[1];

				if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'C') {// 타입이
																					// 캐릭터인
																					// 경우
					tokenTable.get(index).setObjectcode(temp.charAt(0));
					for (int i = 1; i < temp.length(); i++) {
						tokenTable.get(index).objectcode *= 0x100;
						tokenTable.get(index).setObjectcode(temp.charAt(i));
					} // 두글자씩 옆으로 밀면서 기록
				} else if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'X') {
					tokenTable.get(index).setObjectcode(Integer.parseInt(temp, 16));// 숫자인경우
																					// 16진수로
																					// 바꿔서
																					// 기록
				} else {// 에러
					return -1;
				}
			} else if (tokenTable.get(index).getOpreatioin().equals("WORD")) {
				// 여기에 WORD 관련

				int PorM;
				if ((PorM = myAssembler.has_op(tokenTable.get(index).getIndexedOpernd(0))) != 0) {
					String temp[] = tokenTable.get(index).getIndexedOpernd(0).split("[+|-]", 2);

					if ((myAssembler.search_ref(temp[0], sect) > 0) && (myAssembler.search_ref(temp[1], sect) > 0)) {

						if (PorM > 0) {// 연산자가 +
							refTable.add(new refrenceData(temp[0], tokenTable.get(index).getlocctr(), sect));
							refTable.get(refTable.size() - 1).setSize(6);// 값를
																			// 기록해야하므로
																			// 3바이트

							refTable.add(new refrenceData(temp[1], tokenTable.get(index).getlocctr(), sect));
							refTable.get(refTable.size() - 1).setSize(6);// 값를
																			// 기록해야하므로
																			// 3바이트
						} else if (PorM < 0)// 연산자가 -
						{
							refTable.add(new refrenceData(temp[0], tokenTable.get(index).getlocctr(), sect));
							refTable.get(refTable.size() - 1).setSize(6);// 값를
																			// 기록해야하므로
																			// 3바이트

							refTable.add(new refrenceData(temp[1], tokenTable.get(index).getlocctr(), sect));
							refTable.get(refTable.size() - 1).setSize(6);// 값를
																			// 기록해야하므로
																			// 3바이트
							refTable.get(refTable.size() - 1).setOp(1);// 값을 음수로
																		// 사용했으므로
																		// 연산자는
																		// -(=타입이
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
							sect));// 외부에서 가져가 사용할 변수를 알려주므로 심볼의 주소를 검색해 같이 기록
				}
				return 0;
			}

			else if (tokenTable.get(index).getOpreatioin().equals("EXTREF")) {

				for (int i = 0; i < tokenTable.get(index).getOperandNum(); i++)
					extref.add(new refrenceData(tokenTable.get(index).getIndexedOpernd(i), -1, sect));
				// 외부에서 가져와서 사용될 변수를 알려주므로 이름을 기록해둠
				return 0;
			} else if (tokenTable.get(index).getLabel().equals("*")) {// 리터럴 일경우
				String temp = tokenTable.get(index).getOpreatioin().split("'")[1];

				if (tokenTable.get(index).getOpreatioin().charAt(1) == 'C') {// 타입이
																				// 캐릭터
					tokenTable.get(index).setObjectcode(temp.charAt(0));
					for (int i = 1; i < temp.length(); i++) {
						tokenTable.get(index).objectcode *= 0x100;
						tokenTable.get(index).setObjectcode(temp.charAt(i));
					} // 두글자씩 밀면서 기록 objectcode에 기록
				} else if (tokenTable.get(index).getOpreatioin().charAt(1) == 'X') {// 숫자인
																					// 경우
					tokenTable.get(index).setObjectcode(Integer.parseInt(temp, 16));// 16진법으로
																					// 기록
				} else {// 에러
					return -1;
				}
				return 0;
			}

		}
		return 0;

	}

	public int find_reg(char r) {// 레지스터 넘버를 리턴해줌

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
				if ((j = myAssembler.search_opcode(tokenTable.get(index).getOpreatioin())) != -1)// 만약연산자가instructionset에있다면
				{

					if (instTable.get(j).getFormat().equals("2")) {
						buf = String.format("%04X", tokenTable.get(index).getObjectcode());// opcode를16진법으로
						record = 4;
					}
					if (instTable.get(j).getFormat().equals("1")) {
						buf = String.format("%02X", tokenTable.get(index).getObjectcode());// opcode를16진법으로
						record = 2;
					}
					if ((tokenTable.get(index).getNixbpe() & 1) == 1) {
						buf = String.format("%08X", tokenTable.get(index).getObjectcode());// opcode를16진법으로
						record = 8;
					} else if (instTable.get(j).getFormat().equals("3/4")) {
						buf = String.format("%06X", tokenTable.get(index).getObjectcode());// opcode를16진법으로
						record = 6;
					}
				} else if (tokenTable.get(index).getOpreatioin().equals("START")
						|| tokenTable.get(index).getOpreatioin().equals("CSECT"))// 새로운H레코드가오는위치
				{
					if (tokenTable.get(index).getOpreatioin().equals("CSECT"))// 첫레코드헤더가아니면
					{
						sect++;
						adr = 0;// 시작주소 초기화
					}
					int r = 0;
					writeBuffer.write(String.format("H%s", tokenTable.get(index).getLabel()));
					for (r = tokenTable.get(index).getLabel().length(); r < 6; r++)
						writeBuffer.write(" ");
					writeBuffer.write(String.format("%012X%n", sectSize[sect]));
					continue;
				} else if (tokenTable.get(index).getOpreatioin().equals("EXTDEF"))// 외부정의레코드가있으면기록한다
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
				} else if (tokenTable.get(index).getOpreatioin().equals("EXTREF"))// 외부참조레코드가있으면기록한다.
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

					if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'C')// 타입구분
						record = k * 2;
					else if (tokenTable.get(index).getIndexedOpernd(0).charAt(0) == 'X')
						record = k;

					switch (record)// 필드의 수가 다르기 때문에 자릿수를  구분해서 출력해준다
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
				} else if (tokenTable.get(index).getLabel().equals("*"))// 만약
																		// 리터럴이온다면
				{
					int k = tokenTable.get(index).getOpreatioin().split("'")[1].length();

					if (tokenTable.get(index).getOpreatioin().charAt(1) == 'C')// 타입구분
						record = k * 2;
					else if (tokenTable.get(index).getOpreatioin().charAt(1) == 'X')
						record = k;

					switch (record)// 필드의 수가 다르기 때문에 자릿수를 구분해서 출력해준다
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
				} else {// object code 출력에 관련없는 명령어 라인이라면 무시
					buf = "";
					record = 0;
				}

				if ((sum + record) > 59)// 10~69를 넘는경우 다음 T레코드로 넘어감
				{
					writeBuffer.write(String.format("T%06X%02X", adr, sum / 2));
					writeBuffer.write(String.format("%s%n", temp)); // temp에저장해두었던레코드
																	// 기록

					sum = record;
					temp = buf;
					adr = tokenTable.get(index).getlocctr();
					continue;
				} else if (index == tokenTable.size() - 1)// 마지막 줄이거나
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
							if (refTable.get(v).getOp() == 0)// 연산의 종류를 파악한다
								writeBuffer.write("+");// 만약 주소값만큼 더해야한다면
							else
								writeBuffer.write("-");// 빼야하는경우라면
							writeBuffer.write(String.format("%s%n", refTable.get(v).getName()));// 참조한위치의주소
						}
					}
					writeBuffer.write(String.format("E000000%n"));
					continue;
				} else if (tokenTable.get(index + 1).getOpreatioin().equals("LTORG")
						|| tokenTable.get(index + 1).getOpreatioin().equals("CSECT")) {// 줄을바꿔야하는경우
					sum += record;
					temp += buf;
					writeBuffer.write(String.format("T%06X%02X", adr, sum / 2));
					writeBuffer.write(String.format("%s%n", temp));// temp에저장해두었던레코드
																	// 기록
					sum = 0;
					record = 0;
					temp = "";
					buf = "";
					adr = tokenTable.get(index + 2).getlocctr();
					if (tokenTable.get(index + 1).getOpreatioin().equals("CSECT"))// 다음섹션으로가는경우라면
					{
						for (int v = 0; v < refTable.size(); v++) {// M 레코드 출력
							if (sect == refTable.get(v).getSect()) {
								writeBuffer.write(String.format("M%06X", refTable.get(v).getAddr()));
								if (refTable.get(v).getSize() == 5)
									writeBuffer.write("05");
								else
									writeBuffer.write("06");
								if (refTable.get(v).getOp() == 0)// 연산의 종류를 파악한다
									writeBuffer.write("+");// 만약 주소값만큼 더해야한다면
								else
									writeBuffer.write("-");// 빼야하는경우라면
								writeBuffer.write(String.format("%s%n", refTable.get(v).getName()));// 참조한위치의주소
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
