/*
 * my_assembler 함수를 위한 변수 선언 및 매크로를 담고 있는 헤더 파일이다.
 *
 */
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

/*
 * instruction 목록 파일로 부터 정보를 받아와서 생성하는 구조체 변수이다.
 * 라인 별로 하나의 instruction을 저장한다.
 */
struct inst_unit {

	char oper[8];//명령어의 이름 
	char format[8];//포멧
	int opcode;//opcode
	int opernum;//연산자의 개수


};
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
int inst_index;

/*
 * 어셈블리 할 소스코드를 입력받는 테이블이다. 라인 단위로 관리할 수 있다.
 */
char *input_data[MAX_LINES];
static int line_num;//input의 line수

int label_num;

/*
 * 어셈블리 할 소스코드를 토큰단위로 관리하기 위한 구조체 변수이다.
 * operator는 renaming을 허용한다.
 * nixbpe는 8bit 중 하위 6개의 bit를 이용하여 n,i,x,b,p,e를 표시한다.
 */
struct token_unit {
	char *label;
	char *oper;
	char *operand[MAX_OPERAND];
	char *comment;
	char nixbpe;
};

typedef struct token_unit token;
token *token_table[MAX_LINES];
static int token_line;

struct imdata_unit {//immedate Data로 출력하기 위해서 사용
	int ctr;//locctr
	char *label;//label
	char *oper;//operation
	char *operand[MAX_OPERAND];//operand
	int objectcode;//objectcode
	char nixbpe;//nixbpe bits

};

typedef struct imdata_unit imdata;
imdata *imdata_table[MAX_LINES];
static int imdata_line;

struct literal_unit{
	char name[50];
	//char operand;
	int length;//상수의 길이 
	int address[20];//사용한 locctr
	int type;//1=char 2=int
	int many;

};
typedef struct literal_unit literal;
literal literal_table[MAX_LINES];
static int lit_num = 0;

/*
 * 심볼을 관리하는 구조체이다.
 * 심볼 테이블은 심볼 이름, 심볼의 위치로 구성된다.
 */
struct symbol_unit {
	char symbol[10];
	int addr;
	int type;//1은 상대적 2는 절대적
};
static int loc_num;//로케이션 카운터의 수
typedef struct symbol_unit symbol;
symbol sym_table[5][MAX_LINES];
static int sym_line;//각 섹터의 심볼의 수
static int sym_sec;//섹터의 수

int make_locctr(int index);
int direct(int index);//Directive를 찾아서 처리 해줌 
static int locctr[5];

struct ref{
	char name[10];//사용한 심볼의 이름
	int  add;//사용한 주소 위치
	int op;//0은+ 1은-
	int size;//수정할 비트으 비트의수를 기록
};
typedef struct ref refer;
refer ref_table[5][10];//Refrence를 기록
char extref[5][10][10];//정의한 외부참조를 기록 
static int refmany[5];


static char *input_file;
static char *output_file;
int search_ref(char *);
int has_op(char *);
int find_reg(char );
int init_my_assembler(void);
int init_inst_file(char *inst_file);
int init_input_file(char *input_file);
int search_opcode(char *);
int search_literal(char*);
void make_opcode_output();
int token_parsing(int index);
static int assem_pass1(void);
static int assem_pass2(void);
void make_objectcode_output(char *file_name);