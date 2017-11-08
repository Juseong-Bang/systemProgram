/* 
 * my_assembler �Լ��� ���� ���� ���� �� ��ũ�θ� ��� �ִ� ��� �����̴�. 
 * 
 */
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

/* 
 * instruction ��� ���Ϸ� ���� ������ �޾ƿͼ� �����ϴ� ����ü �����̴�.
 * ������ ������ instruction set�� ��Ŀ� ���� ���� �����ϵ�
 * ���� ���� �ϳ��� instruction�� �����Ѵ�.
 */
struct inst_unit {
	
	char *oper;//���ɾ��� �̸� 
	char *format;//����
	int opcode;//opcode
	int opernum;//�������� ����
	
		
		/* add your code here */
};
typedef struct inst_unit inst;
inst inst_table[MAX_INST];
int inst_index;

/*
 * ������� �� �ҽ��ڵ带 �Է¹޴� ���̺��̴�. ���� ������ ������ �� �ִ�.
 */
char *input_data[MAX_LINES];
static int line_num;

int label_num; 

/* 
 * ������� �� �ҽ��ڵ带 ��ū������ �����ϱ� ���� ����ü �����̴�.
 * operator�� renaming�� ����Ѵ�.
 * nixbpe�� 8bit �� ���� 6���� bit�� �̿��Ͽ� n,i,x,b,p,e�� ǥ���Ѵ�.
 */
struct token_unit {
	char *label;//���̺�
	char *oper; //���ɾ�
	char *operand[MAX_OPERAND];//�ǿ����� 
	char *comment;//�ڸ�Ʈ
	//char nixbpe; // ���� ������Ʈ���� ���ȴ�.

};

typedef struct token_unit token; 
token token_table[MAX_LINES]; 
static int token_line;

/*
 * �ɺ��� �����ϴ� ����ü�̴�.
 * �ɺ� ���̺��� �ɺ� �̸�, �ɺ��� ��ġ�� �����ȴ�.
 */
struct symbol_unit {
	char symbol[10];
	int addr;
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES];

static int locctr;
//--------------

static char *input_file;
static char *output_file;
int init_my_assembler(void);
int init_inst_file(char *inst_file);
int init_input_file(char *input_file);
int search_opcode(char *str);
void make_opcode_output(char *file_name);
int token_parsing(int index);
/* ���� ������Ʈ���� ����ϰ� �Ǵ� �Լ�*/
static int assem_pass1(void);
static int assem_pass2(void);
void make_objectcode_output(char *file_name);