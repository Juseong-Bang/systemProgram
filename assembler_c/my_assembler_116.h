/*
 * my_assembler �Լ��� ���� ���� ���� �� ��ũ�θ� ��� �ִ� ��� �����̴�.
 *
 */
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

/*
 * instruction ��� ���Ϸ� ���� ������ �޾ƿͼ� �����ϴ� ����ü �����̴�.
 * ���� ���� �ϳ��� instruction�� �����Ѵ�.
 */
struct inst_unit {

	char oper[8];//��ɾ��� �̸� 
	char format[8];//����
	int opcode;//opcode
	int opernum;//�������� ����


};
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
int inst_index;

/*
 * ����� �� �ҽ��ڵ带 �Է¹޴� ���̺��̴�. ���� ������ ������ �� �ִ�.
 */
char *input_data[MAX_LINES];
static int line_num;//input�� line��

int label_num;

/*
 * ����� �� �ҽ��ڵ带 ��ū������ �����ϱ� ���� ����ü �����̴�.
 * operator�� renaming�� ����Ѵ�.
 * nixbpe�� 8bit �� ���� 6���� bit�� �̿��Ͽ� n,i,x,b,p,e�� ǥ���Ѵ�.
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

struct imdata_unit {//immedate Data�� ����ϱ� ���ؼ� ���
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
	int length;//����� ���� 
	int address[20];//����� locctr
	int type;//1=char 2=int
	int many;

};
typedef struct literal_unit literal;
literal literal_table[MAX_LINES];
static int lit_num = 0;

/*
 * �ɺ��� �����ϴ� ����ü�̴�.
 * �ɺ� ���̺��� �ɺ� �̸�, �ɺ��� ��ġ�� �����ȴ�.
 */
struct symbol_unit {
	char symbol[10];
	int addr;
	int type;//1�� ����� 2�� ������
};
static int loc_num;//�����̼� ī������ ��
typedef struct symbol_unit symbol;
symbol sym_table[5][MAX_LINES];
static int sym_line;//�� ������ �ɺ��� ��
static int sym_sec;//������ ��

int make_locctr(int index);
int direct(int index);//Directive�� ã�Ƽ� ó�� ���� 
static int locctr[5];

struct ref{
	char name[10];//����� �ɺ��� �̸�
	int  add;//����� �ּ� ��ġ
	int op;//0��+ 1��-
	int size;//������ ��Ʈ�� ��Ʈ�Ǽ��� ���
};
typedef struct ref refer;
refer ref_table[5][10];//Refrence�� ���
char extref[5][10][10];//������ �ܺ������� ��� 
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