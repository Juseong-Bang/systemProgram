# systemProgram

설계/구현 아이디어
-클래스는 크게 4가지로 나눠서 구현된다. UI 시뮬레이터, SIC/XE 시뮬레이터, 로더, 리소스 매니저.  
- Visual 시뮬레이터: 사용자가 현재 상황을 파악하기 쉽게 한눈에 들어오는 UI를 보여줌으로써 실제 동작을 바로바로 이해하는데 도움을 준다. 리소스 매니저에 접근해서 레지스터의 값이나 메모리 값을 화면에 보여준다. 
- SIC/XE 시뮬레이터: 로더를 통해서 메모리에 올라간 프로세스를 실행한다. 실제 오브젝트 코드가 실행되는 방식대로 프로그램을 진행시켜준다. 실제 동작이 이 클래스에서 동작한다.
- Loader: 오브젝트 파일을 읽어서 메모리에 올려주는 클래스이다. 레코드를 판독하고 메모리에 지정된 주소 또는 상대주소를 계산해서 올려준다. M 레코드의 경우 정보를 저장해 두었다가 메모리에 올라간 정보를 수정함으로써 절대주소의 경우도 고려해준다.
- ModRec: M레코드를 위해 만든 클래스이다. 심볼의 이름, 주소, 플래그 (+인지 -인지), 수정할 필드 수를 저장하고 사용하기 위해서 만들었다. ArrayList에 한 클래스 씩 저장해서 Table처럼 사용한다
-Resource Manager: 리소스 즉, 메모리와 레지스터를 접근해서 읽고 쓰는 클래스이다. 메모리에 접근은 모두 이 클래스를 통해서 동작한다. 원하는 주소와 데이터 길이를 입력해주면 적절하게 동작한다. 위의 세 클래스에서 모두 접근한다.
-Sic Main: Visual 시뮬레이터를 실행시켜주는 클래스이다.
소스 코드
-public class VisualSimulator extends JFrame : Jframe을 상속 받아 화면을 보여줄 수 있다. 생성자에서 각종 패널과 버튼 등을 초기화해주고 리스너를 달아준다.
-public void initialize(File objFile, ResourceManager rMgr): 시뮬레이터를 동작 시키기 위한 세팅을 수행한다. sic 시뮬레이터를 통해 로더를 수행시키고, 로드 된 값들을 읽어 보여주어 스텝을 진행할 수 있는 상태로 만들어 놓는다. 다이얼로그를 띄어 읽을 파일을 지정해주는 기능도 있다. 
-public void oneStep() : SiC 시뮬레이터에게 한 스텝 명령을 하도록 실행시키는 함수이다. 한 스텝 실행 후 레지스터와 TextArea의 값을 보여주는 함수를 실행한다.
-public void allStep(): 위의 oneStep함수를 반복 실행해준다 코드가 종료되면 레지스터와 TextArea의 값을 보여주는 함수를 실행한다. TextArea의 경우 값이 많으므로 반복 호출보다는 temp에 값을 저장하며 모든 연산이 끝난 후 저장된 값을 TextArea에 보여주는 형식으로 동작한다.
- public void update(): 위의 step 함수들이 화면에 레지스터와 각종 값들을 보여줄 때 사용하는 함수이다. 대부분 리소스 매니저에서 값을 가져와서 보여준다.
- public class SicLoader: 오브젝트 코드를 읽어 메모리에 로드한다.  오브젝트 코드의 각 헤더(H, T, M 등)를 읽어 메모리에 적절히 올리는 동작을 수행한다. 
- public void load(File objFile, ResourceManager manager) : 처음 메모리에 프로그램을 올릴 때 사용되는 초기화 함수이다. 지정된 오브젝트 코드가 쓰여진 파일에서 한 줄씩 읽으며 레코드를 구분해준다.
-한 줄로 읽은 레코드들은 맨 앞 글자로 구분이 가능하므로 substring을 이용해서 지정된 필드값에 따라서 잘라서 토큰처럼 사용한다. 
H레코드는 프로그램 이름과 길이 시작주소를 구해서 리소스 매니저에 저장해준다. 섹션마다 정보가 다르므로 섹션에 해당하는 변수를 같이 저장해서 혼동되지 않도록 한다.  뿐만 아니라 프로그램의 이름은 다른 섹션에서 심볼처럼 사용되므로 외부 정의 테이블에도 주소와 이름을 기록해 둔다. 
D레코드는 외부 정의에서 사용될 심볼의 이름과 주소를 가지므로 12필드씩 잘라서 주소와 이름을 정의 테이블에 같이 기록해 둔다.
T레코드는 메모리에 올라갈 오브젝트 코드를 가지고 있으므로 그 길이와 시작주소와 데이터를 로더에게 전달함으로 프로그램을 메모리에 올려준다.
M레코드는 외부정의가 사용된 주소 플래그 필드 넓이 이름을 가지고 있으므로 각 레코드가 메모리에 올라간 뒤 D 레코드의 내용을 참조해서 메모리의 값에 다시 계산해서 넣어준다. 

public class ModRec: Modefication 레코드 한줄마다 저장되는 클래스이다. 심볼의 이름 사용된 주소, 플래그, 필드 너비 등을 저장해준다. 
public class SicSimulator: 메모리에 올라간 프로세스가 동작을 하도록 메모리를 접근해서 연산을 해주는 클래스이다. 실질적인 동작을 한다.
public void initialize(File objFile, ResourceManager manager): 시뮬레이터를 동작 시키기 위한 세팅을 수행한다. 메모리 작업 등 실질적인 초기화 작업을 수행한다.
public boolean oneStep(): 메모리와 레지스터의 접근을 통해 하나의 명령어만 수행한다. 해당 명령어가 수행되고 난 값의 변화를 보여주고, 다음 명령어를 포인팅한다.
-동작은 변수에 pc값을 저장해 둔 뒤 메모리에서 2바이트를 참조해서 opcode와 3/4형식 이라면 nixbpe를 계산해서 저장해 둔다. 그리고 명령어의 포맷에 따라서 만약 e비트가 켜져 있다면 뒤의 20비트를 타겟 주소로 설정하고 아니라면 12비트를 타겟 주소로 설정한다. 포맷에 따라서 Instruction을 보여주는 area에 나타낼 메모리 값을 포맷만큼 읽어 저장해준다.
이후 동작은 action 함수에 opcode ,nixpbe, 타겟 주소를 넘겨주며 실행된다. Action 함수 이후에 pc값이 0이라면 모든 동작이 끝난 것 이므로 false를 리턴 해준다.
private void action(int opcode, int nixbpe, int tarAdr) : 한 명령어의 토큰들을 가지고 실제 메모리와 레지스터에 접근하며 프로세싱을 하는 함수이다.
-명령어의 위치가 있는 값을 position에 저장하고 format을 보기 쉽게 int형 변수에 저장해준다. Nixbpe를 보고 4형식이라면 PC 레지스터를 4 증가시키고 포맷에 4를 저장해준다. 그리고 3형식중 n,I 비트가 모두 켜져 있다면 pc relative 이므로 타겟 주소에 pc값을 더해준 뒤 3바이트로 잘라준다. 이때 상위 비트가 켜져 있다면 주소 값 저장과정에서 잘렸으므로 20비트 음수로 확장해준다.  N 비트만 켜져 있다면 타겟 주소를 3바이트 참조한 값을 다시 타겟 주소로 설정해준 뒤 포맷을 5로 설정하고 pc값을 3 증가 시킨다. i비트만 켜 있다면 포맷을 6으로 설정한 뒤 pc값을 3 증가시켜준다. 그리고 만약 x비트가 켜져 있다면 타겟 주소의 값에 x레지스터의 값을 더해준다. 
- switch 문의 opcode에 따라서 동작이 다른데 요약해서 기술하자면 다음과 같다. 
명령어가 만약 메모리 값을 참조한다면 메모리에 타겟 주소로 명령어마다 필요한 바이트 수만큼 불러온다. 레지스터를 접근한다면 레지스터 넘버를 통해 리소스 매니저의 getRegister함수를 이용해서 가져오고 setRegister를 통해 저장해준다. 만약 비교하는 동작이 있다면 비교하는 두 값 중 앞의 값이 크다면 1 같다면 2 뒤의 값이 크다면 4를 SW 레지스터에 저장해준다. 만약 immidate라면 포맷이 6인지 확인하고 타겟 주소 값 자체를 사용하는 명령어 라면 타겟 주소를 직접 사용한다. 만약 점프 관련 명령어라면 PC 레지스터의 값을 L레지스터나 타겟 주소 값으로 변경해준다. 분기 관련 명령어는 SW 레지스터의 값을 참조하여 저장된 값이 1, 2, 4에 따라서 동작해준다. 만약 RD나 WD의 경우 리소스 매니저의 readDevice 와 writedevice 함수를 통해서 한 바이트씩 읽어온다. 그리고 명령어의 성공 여부에 따라서 SW레지스터에 플래그를 설정해준다.
위의 모든 동작은 교제의 APPENDIX를 참조해서 구현하였다. 동작시에 로그 값을 저장하는 스트링에 현재 사용되는 명령어를 써준다.
-public int getOpcode(String input): inst의 상위 1바이트를 입력으로 받아서 하위 2비트를 0으로 만들어서 리턴 한다.
-public int getNixpbe(String input): inst의 상위 2바이트를 입력으로 받아서 하위 4비트를 자르고 6비트만 잘라서 리턴 해준다.
-public int saerchOpFmt(int opcode): opcode의 형식을 리턴 해준다. 편의상 2형식 레코드 이와는 모두 3형식으로 값을 반환해주도록 만들었다.
public class ResourceManager: 메모리에 접근하며 메소드에서 요청한 주소에 대한 연산과 참조를 제공하는 함수이다. 다음과 같은 자료구조를 사용한다.
private String progName[] = new String[3];//프로그램의 이름저장 
	private int startADDR[] = new int[3];//프로그램의 시작주소 저장 
	private int progLength[] = new int[3];//프로그램의 길이저장 
	private int[] Reg = new int[10];// a x l b s t f ta pc sw 순서
	StringBuffer MEM = new StringBuffer(MEM_SIZE);//가상 메모리를 위해서 쓰는 변수이다.
-public void initializeMemory(): 메모리 영역을 초기화 하는 메소드이다. 전부다 -로 채워준다.
-public void initializeRegister(): 각 레지스터 값을 초기화 하는 메소드이다.
public void writeDevice(Byte data): 한 바이트를 파일에 써주는 함수이다.
public byte readDevice(): 한 바이트를 파일에서 읽어 오는 함수이다. 만약 파일의 끝에 도달했다면 0을 리턴 한다.
public void setMemory(int locate, String data, int size): 메모리에 쓰기위해 주소와 값 크기를 받아서 메모리에 올려준다. 
public String getMemory(int locate, int size): 메모리 영역에서 값을 읽어오는 메소드 이다. 원하는 위치와 크기를 전달하면 메모리의 해당위치의 값을 리턴 해준다.
public void setRegister(int regNum, int value): 레지스터에 값을 세팅하는 메소드. regNum은 레지스터 종류를 나타낸다.
public void setRegister(char regNum, int value) 만약 넘버가 캐릭터 형이라도 사용 가능하다.	
public int getRegister(int regNum) 레지스터에서 값을 가져오는 메소드이다. 원하는 레지스터의 값을 리턴 해준다. 
public int getRegister(char regNum) 만약 넘버가 캐릭터 형이라도 사용 가능하다.	
public void setProgName(String Name, int currentSection) 해당 섹션의 프로그램의 이름을 저장한다.	
public String getProgName(int currentSection) 해당 섹션의 프로그램 이름을 리턴 한다. 
public void setProgLength(String Length, int currentSection): 해당 섹션의 프로그램 길이를 저장
public int getProgLength(int currentSection): 해당 섹션의 프로그램 길이를 리턴 한다.
public void setStartAdr(int currentSection): 해당 섹션의 프로그램 길이를 설정한다.
0번 섹션의 시작주소는 0으로 설정하고 다음 섹션은 이 이전 섹션의 끝이 다음 섹션의 시작 주소 값으로 설정된다. 
public int getStartAdr(int currentSection) 해당 섹션의 시작주소를 리턴 해준다.
public void setMod(int Address, int value, int Flag, String op) modification 레코드를 위한 메모리 접근을 제공한다. 만약 flag가 홀수라면 주소를 1증가시키고 메모리의 해당 address의 값을 value만큼 불러와서 op가 +인지 -인지 구분한 뒤 같이 계산해서 다시 해당 주소에 저장해준다.

