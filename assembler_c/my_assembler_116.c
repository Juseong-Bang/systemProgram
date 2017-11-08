/*
 * 화일명 : my_assembler.c
 * 설  명 : 이 프로그램은 SIC/XE 머신을 위한 간단한 Assembler 프로그램의 메인루틴으로,
 * 입력된 파일의 코드 중, 명령어에 해당하는 OPCODE를 찾아 출력한다.
 *
 */

/*
 *
 * 프로그램의 헤더를 정의한다.
 *
 */
#define _CRT_SECURE_NO_WARNINGS
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <math.h>

#include "my_assembler_116.h"

/* ----------------------------------------------------------------------------------
 * 설명 : 사용자로 부터 어셈블리 파일을 받아서 명령어의 OPCODE를 찾아 출력한다.
 * 매계 : 실행 파일, 어셈블리 파일
 * 반환 : 성공 = 0, 실패 = < 0
 * 주의 : 현재 어셈블리 프로그램의 리스트 파일을 생성하는 루틴은 만들지 않았다.
 *		   또한 중간파일을 생성하지 않는다.
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[])
{
	if (init_my_assembler() < 0)
	{
		printf("init_my_assembler: program init failed.\n");
		return -1;
	}




	if (assem_pass1() < 0){
		printf("assem_pass1: pass1 failed.  \n");
		return -1;
	}
	if (assem_pass2() < 0){
		printf(" assem_pass2: pass2 failed.  \n");
		return -1;
	}
	make_opcode_output();//화면에 출력해주는 함수
	make_objectcode_output("output_116.txt");

	return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다.
 * 매계 : 없음
 * 반환 : 정상종료 = 0 , 에러 발생 = -1
 * 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기
 *		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
 *		   구현하였다.
 * ----------------------------------------------------------------------------------
 */
int init_my_assembler(void)
{
	int result;

	if ((result = init_inst_file("instset.txt")) < 0)
		return -1;
	if ((result = init_input_file("input.txt")) < 0)
		return -1;
	return result;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을
 *        생성하는 함수이다.
 * 매계 : 기계어 목록 파일
 * 반환 : 정상종료 = 0 , 에러 < 0
 * 주의 : 기계어 목록파일 형식은 자유롭게 구현한다. 예시는 다음과 같다.
 *
 *	===============================================================================
 *		   | 이름 | 형식 | 기계어 코드 | 오퍼랜드의 갯수 | NULL|
 *	===============================================================================
 *
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char *inst_file)
{
	FILE * file;
	int i = 0;
	char  buf[MAX_INST];

	*inst_table = (inst*)malloc(sizeof(inst)*MAX_INST);
	if ((file = fopen(inst_file, "r")) == NULL)
	{
		printf("fopen err at instruction set\n");
		return -1;
	}


	while (fgets(buf, MAX_INST, file))//buf로 명령어 집합을 받아 한줄씩 저장한다
	{
		inst_table[i] = (inst*)malloc(sizeof(inst));
		char a[10];
		char b[10];
		int c = 0;
		int d = 0;

		sscanf(buf, "%s%s%x%d", a, b, &c, &d);//버퍼를 각각의 데이터 형에 따라서 나눈다 
		strcpy(inst_table[i]->oper, a);//operation 이름과  
		strcpy(inst_table[i]->format, b);//포멧을 할당하고 
		inst_table[i]->opcode = c;//opcode와 
		inst_table[i++]->opernum = d;//연산자의 개수도 넣는다 
	}

	inst_index = i;//명령어의 갯수도 입력한다.

	return 0;

}

/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 할 소스코드를 읽어오는 함수이다.
 * 매계 : 어셈블리할 소스파일명
 * 반환 : 정상종료 = 0 , 에러 < 0
 * 주의 :
 *
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char *input_file)
{
	FILE * file;
	int i = 0;
	char  buf[MAX_INST];
	if ((file = fopen(input_file, "r")) == NULL)//input file을 열어서
	{
		printf("fopen err at input file\n");
		return -1;
	}
	while (fgets(buf, MAX_INST, file))//오류가 안난다면 버퍼에 한줄씩 담는다.
	{
		if (buf[0] == '.')//.으로 시작하는 주석문은 건너띈다 
			continue;
		input_data[i] = malloc(sizeof(buf));//input_data에 한줄씩 옮겨 저장한다 
		strcpy(input_data[i++], buf);
		line_num++;//라인의 수를 기록한다 최대 5000 라인
	}
	return 0;
}


/* ----------------------------------------------------------------------------------
 * 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다.
 *        패스 1로 부터 호출된다.
 * 매계 : 소스코드의 라인번호
 * 반환 : 정상종료 = 0 , 에러 < 0
 * 주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다.
 * ----------------------------------------------------------------------------------
 */
int token_parsing(int index)
{

	token_table[index] = (token*)calloc(sizeof(token), sizeof(token));
	int i = 0, j = 0;
	char buf[100];
	if (input_data[index][j] == '\t'){//만약 index 라는 라인이 tab으로 시작하면 (= 레이블이 없으면)
		token_table[index]->label = "\t\0";//레이블값에 탭만 넣어준다 
		j++;
	}
	else{
		while (input_data[index][j] != '\t')//레이블이 있다면 
		{
			buf[i++] = input_data[index][j++];//버퍼에 하나씩 저장한후 
		}
		buf[i] = '\0';//뒤에 널문자를 붙인후 
		token_table[index]->label = malloc(sizeof(buf));
		strcpy(token_table[index]->label, buf);//토큰 테이블에 저장한다.
	}
	j++;
	i = 0;

	while (input_data[index][j] != '\t')//명령어 위치의 값을 받기 시작한다 
	{
		if (input_data[index][j] == '+')
		{

			token_table[index]->nixbpe += 1;//4형식 표기
			j++;
			continue;
		}
		buf[i++] = input_data[index][j++];
	}
	buf[i] = '\0';//다 받으면 뒤에 널문자를 붙이고 
	token_table[index]->oper = malloc(sizeof(buf));
	strcpy(token_table[index]->oper, buf);//operation 에 저장한다 

	i = 0;
	j++;
	int t = 0;
	if (input_data[index][j] == '\t')
	{
		token_table[index]->operand[0] = "\t\0";//만약 연산자가 없다면 첫번쨰 연산자 위치에 탭을 저장한다 .
		j++;
	}
	else{
		while (input_data[index][j] != '\t')
		{
			if (input_data[index][j] == ',')
			{
				buf[i] = '\0';
				i = 0;
				j++;
				token_table[index]->operand[t] = malloc(sizeof(buf));
				strcpy(token_table[index]->operand[t++], buf);
				continue;
			}
			buf[i++] = input_data[index][j++];
		}
		buf[i] = '\0';
		token_table[index]->operand[t] = malloc(sizeof(buf));
		strcpy(token_table[index]->operand[t], buf);//있다면 옮긴후 뒤에 널문자를 붙여서 저장한다
	}

	j++;
	i = 0;

	i = search_opcode(token_table[index]->oper);
	i = 0;
	while (input_data[index][j] != '\0')//comment가 있다면 
	{
		buf[i++] = input_data[index][j++];
	}
	buf[i] = '\0';
	token_table[index]->comment = malloc(sizeof(buf));
	strcpy(token_table[index]->comment, buf);//comment도 뒤에 널문자를 붙여 저장한다. 

	j++;
	i = 0;
	if (token_table[index]->operand[0][0] == '#')//Immediate addressing
		token_table[index]->nixbpe += 16;
	if (token_table[index]->operand[0][0] == '@')//Indirect addressing
		token_table[index]->nixbpe += 32;

	return 0;


}
/* ----------------------------------------------------------------------------------
* 설명 : 입력 문자열이 리터럴인지 알려주는 함수이다.
* 매계 : 토큰 단위로 구분된 문자열
* 반환 : 정상종료 = 리터럴 테이블 인덱스, 에러 < 0
* 주의 :
*
* ----------------------------------------------------------------------------------
*/

int search_literal(char* lit){

	for (int i = 0; i < lit_num; i++)
	{
		if (strcmp(lit, literal_table[i].name) == 0)
			return i;
	}

	return -1;

}
/* ----------------------------------------------------------------------------------
* 설명 : 입력 문자열이 심볼인지 알려주는 함수이다.
* 매계 : 토큰 단위로 구분된 문자열
* 반환 : 정상종료 = 심볼 테이블 인덱스, 에러 < 0
* 주의 :
*
* ----------------------------------------------------------------------------------
*/
int search_symbloc(char *str)
{
	int i = 0;
	char *tem = str;
	if ((tem[0] == '#') || (tem[0] == '@'))
		tem++;

	for (i; i < sym_line; i++){//전체 심볼의 수를 전부 탐색한뒤
		if (strcmp(tem, sym_table[sym_sec][i].symbol) == 0)//만약 있는 심볼이라면 
			return i;//심볼의 인덱스를 리턴한다.
	}		return -1;//없다면 -1
}
/* ----------------------------------------------------------------------------------
* 설명 : 패스1에서 얻은 정보를 통해 오브젝트 코드를 만들어주는 함수이다. 각종 디렉티브와 
*어드레싱 모드들이 이 함수에서 판단되어 사용된다
* 매계 : 바꿔줄 라인 넘버
* 반환 :  에러 < 0
*
* ----------------------------------------------------------------------------------
*/
int make_objectcode(int imdata_line){
	int p = 0, s = 0, t = 0;

	if ((p = search_opcode(imdata_table[imdata_line]->oper)) != -1){//만약 opcode가 있는 동작이라면 
		if ((imdata_table[imdata_line]->nixbpe & 1) == 1){//4형식
			if (imdata_table[imdata_line]->operand[1] != NULL)
			if (strcmp(imdata_table[imdata_line]->operand[1], "X") == 0)//nixbpe중에 x비트 체크
			{
				imdata_table[imdata_line]->nixbpe += 8;
			}
			if ((imdata_table[imdata_line]->nixbpe & 48) == 0)//n이나 i가 아니면 
				imdata_table[imdata_line]->nixbpe += 48;//ni bit 킴
			imdata_table[imdata_line]->objectcode += inst_table[p]->opcode * 0x1000000;//opcode 자릿수 맞춤
			imdata_table[imdata_line]->objectcode += imdata_table[imdata_line]->nixbpe * 0x100000;//nixbpe 자릿수 맞춤
			if ((s = search_symbloc(imdata_table[imdata_line]->operand[0])) != -1)//심볼이 있다면
			{
				imdata_table[imdata_line]->objectcode += sym_table[sym_sec][s].addr;//심볼의 주소를 더함
			}
			else if (search_ref(imdata_table[imdata_line]->operand[0]) != -1){//만약 참조한 변수를 사용했다면 
				ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr + 1;
				ref_table[sym_sec][refmany[sym_sec]].size = 5;//5 halfbyte 크기만큼 레코드 수정
				strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, imdata_table[imdata_line]->operand[0]);
			}
			else if (((imdata_table[imdata_line]->operand[0][0] == '#') && ((imdata_table[imdata_line]->nixbpe & 16) == 16))){//심볼이 없고 i bit가 1일떄 
				char *temp = imdata_table[imdata_line]->operand[0] + 1;//#을 뺀 타겟 밸류를 
				imdata_table[imdata_line]->objectcode += atoi(temp);//objectcode에 넣는다.
			}
		}
		else if ((strcmp(inst_table[p]->format, "3/4")) == 0)//3형식
		{
			if (imdata_table[imdata_line]->operand[1] != NULL)
			if (strcmp(imdata_table[imdata_line]->operand[1], "X") == 0)//nixbpe중에 x비트 체크
			{
				imdata_table[imdata_line]->nixbpe += 8;//xbit 체크 
			}
			if ((s = search_symbloc(imdata_table[imdata_line]->operand[0])) != -1)//심볼이 있다면 
			{
				if (abs(sym_table[sym_sec][s].addr - (imdata_table[imdata_line]->ctr + 3)) < 0x1000){
					imdata_table[imdata_line]->objectcode += ((sym_table[sym_sec][s].addr - (imdata_table[imdata_line]->ctr + 3))) & 0xfff;//음수일경우 3byte만 쓰기위해 
				}
				else{//심볼이 있으나 pc relative를 못씀 
					return -1;
				}
				imdata_table[imdata_line]->nixbpe += 2;//pc ralative
				//imdata_table[imdata_line]->objectcode += sym_table[sym_sec][s].addr;//심볼의 주소를 더함(상대주소로 바꿔야함)
			}
			else if (((imdata_table[imdata_line]->operand[0][0] == '#') && ((imdata_table[imdata_line]->nixbpe & 16) == 16))){//심볼이 없고 i bit가 1일떄 
				char *temp = imdata_table[imdata_line]->operand[0] + 1;//#을 뺀 타겟 밸류를 
				imdata_table[imdata_line]->objectcode += atoi(temp);//objectcode에 넣는다.
			}
			if ((imdata_table[imdata_line]->nixbpe & 48) == 0)//n이나 i가 아니면 
				imdata_table[imdata_line]->nixbpe += 48;//ni bit 킴
			imdata_table[imdata_line]->objectcode += inst_table[p]->opcode * 0x10000;//opcode 자릿수 맞춤
			imdata_table[imdata_line]->objectcode += imdata_table[imdata_line]->nixbpe * 0x1000;//nixbpe 자릿수 맞춤
		}

		else if ((strcmp(inst_table[p]->format, "2")) == 0){//2형식
			imdata_table[imdata_line]->objectcode += inst_table[p]->opcode * 0x100;//opcode 자릿수 맞춤
			imdata_table[imdata_line]->objectcode += find_reg(imdata_table[imdata_line]->operand[0][0]) * 16;
			if (imdata_table[imdata_line]->operand[1] != NULL)
				imdata_table[imdata_line]->objectcode += find_reg(imdata_table[imdata_line]->operand[1][0]);
		}
		else if ((strcmp(inst_table[p]->format, "1")) == 0)
			imdata_table[imdata_line]->objectcode += inst_table[p]->opcode;//opcode 자릿수 맞춤
	}
	else{
		if (strcmp(imdata_table[imdata_line]->oper, "BYTE") == 0){//여기에 BYTE 관련
			int input_type = 0;
			int i = 0;

			char buf[30];
			if (imdata_table[imdata_line]->operand[0][0] == 'C')
			{
				input_type = 1;//문자타입
			}
			else if (imdata_table[imdata_line]->operand[0][0] == 'X')
			{
				input_type = 2;//숫자타입
			}
			else{//에러
				return -1;
			}

			while ((imdata_table[imdata_line]->operand[0][i + 2]) != '\'')
			{
				buf[i] = imdata_table[imdata_line]->operand[0][i + 2];
				i++;
			}
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "EQU") == 0){
			char te1[10], te2[10];
			int t = 0;
			int h = has_op(imdata_table[imdata_line]->operand[0]);
			if (h != 0){//+나 -를 가지고 있으면
				{
					while (imdata_table[imdata_line]->operand[0][t] != '\0'){
						if (t < abs(h))
							te1[t] = imdata_table[imdata_line]->operand[0][t];
						if (t == abs(h))
							te1[t] = '\0';
						if (t>abs(h))
							te2[t - abs(h) - 1] = imdata_table[imdata_line]->operand[0][t];
						t++;
					}
					te2[t - abs(h) - 1] = '\0';
					if ((search_ref(te1) != -1) && (search_ref(te2) != -1))//두 심볼 모두 테이블에 있으면
					{

						if (h > 0){//연산자가 +
							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6; //6 halfbyte 크기만큼 레코드 수정
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te1);

							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte 크기만큼 레코드 수정
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te2);

						}if (h < 0)//연산자가 -
						{
							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte 크기만큼 레코드 수정
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te1);

							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte 크기만큼 레코드 수정
							strcpy(ref_table[sym_sec][refmany[sym_sec]].name, te2);
							ref_table[sym_sec][refmany[sym_sec]++].op = 1;//연산자 타입 -
						}
					}
					else{
						imdata_line++;
						return -1;
					}
				}
			}
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "WORD") == 0){
			//여기에 WORD 관련
			char te1[10], te2[10];
			int t = 0;
			int h = has_op(imdata_table[imdata_line]->operand[0]);
			if (h != 0){//+나 -를 가지고 있으면
				{
					while (imdata_table[imdata_line]->operand[0][t] != '\0'){
						if (t < abs(h))
							te1[t] = imdata_table[imdata_line]->operand[0][t];
						if (t == abs(h))
							te1[t] = '\0';
						if (t>abs(h))
							te2[t - abs(h) - 1] = imdata_table[imdata_line]->operand[0][t];
						t++;
					}
					te2[t - abs(h) - 1] = '\0';
					if ((search_ref(te1) != -1) && (search_ref(te2) != -1))//두 심볼 모두 테이블에 있으면
					{

						if (h > 0){//연산자가 +
							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6; //6 halfbyte 크기만큼 레코드 수정
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te1);

							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte 크기만큼 레코드 수정
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te2);

						}if (h < 0)//연산자가 -
						{
							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte 크기만큼 레코드 수정
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te1);

							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte 크기만큼 레코드 수정
							strcpy(ref_table[sym_sec][refmany[sym_sec]].name, te2);
							ref_table[sym_sec][refmany[sym_sec]++].op = 1;//연산자 타입 -
						}
					}
					else{
						imdata_line++;
						return -1;
					}
				}
			}
			else
			{
				imdata_table[imdata_line]->objectcode += atoi(imdata_table[imdata_line]->operand[0]);
			}
			imdata_line++;
			return 0;
		}

		else if (strcmp(imdata_table[imdata_line]->oper, "CSECT") == 0){
			sym_sec++;
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "EXTDEF") == 0){
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "EQU") == 0){

			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "EXTREF") == 0){
			for (int k = 0; k < 3; k++){
				if (imdata_table[imdata_line]->operand[k] != NULL)
					strcpy(extref[sym_sec][k], imdata_table[imdata_line]->operand[k]);//EXTREF에서 정의한 외부참조를 기록함
				else
					break;
			}
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->label, "*") == 0){
			int input_type = 0;
			int i = 0;

			char buf[30];
			if (imdata_table[imdata_line]->oper[1] == 'C')
			{
				input_type = 1;//문자타입
			}
			else if (imdata_table[imdata_line]->oper[1] == 'X')
			{
				input_type = 2;//숫자타입
			}
			else{//에러
				return -1;
			}

			while ((imdata_table[imdata_line]->oper[i + 3]) != '\'')
			{
				buf[i] = imdata_table[imdata_line]->oper[i + 3];
				i++;
			}
			buf[i] = '\0';

			if (input_type == 1)
			{
				int u = 0;
				imdata_table[imdata_line]->objectcode += buf[u];
				for (u = 1; u < (int)strlen(buf); u++)
				{
					imdata_table[imdata_line]->objectcode *= 0x100;
					imdata_table[imdata_line]->objectcode += buf[u];

				}
			}
			else if (input_type == 2)
			{
				//	locctr[loc_num] += i / 2;
				imdata_table[imdata_line]->objectcode += strtol(buf, NULL, 16);
			}
			imdata_line++;
			return 0;
		}
	}

	return 0;
}
/* ----------------------------------------------------------------------------------
* 설명 :EXREF에서 외부 참조로 정의한 변수인지 찾아주는 함수이다.
* 매계 : 토큰 단위로 구분된 문자열
* 반환 : 정상종료 =인덱스, 에러 < 0
* 주의 :
*
* ----------------------------------------------------------------------------------
*/
int search_ref(char *str){
	int i = 0;
	char *tem = str;
	for (i; i < 10; i++){//전체 심볼의 수를 전부 탐색한뒤
		if (extref[sym_sec][i] != NULL)
		{
			if (strcmp(tem, extref[sym_sec][i]) == 0)//만약 있는 심볼이라면 
				return i;//심볼의 인덱스를 리턴한다.
		}
		else
			return -1;
	}//없다면 -1
	return -1;
}
/* ----------------------------------------------------------------------------------
* 설명 :레지스터의 넘버를  찾아주는 함수이다.
* 매계 :한 문자
* 반환 : 정상종료 =레지스터 넘버 인덱스
* ----------------------------------------------------------------------------------
*/
int find_reg(char r){

	switch (r){
	case'A':
		return 0;
	case'X':
		return 1;
	case'L':
		return 2;
	case'B':
		return 3;
	case'S':
		return 4;
	case'T':
		return 5;
	case'F':
		return 6;
	case'P':
		return 8;
	case '\t':
		return 0;



	}
	return -1;
}
/* ----------------------------------------------------------------------------------
 * 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다.
 * 매계 : 토큰 단위로 구분된 문자열
 * 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0
 * 주의 :
 *
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str)
{

	for (int i = 0; i < inst_index; i++){//전체 명령어의 수를 전부 탐색한뒤
		if (strcmp(str, inst_table[i]->oper) == 0)//만약 있는 명령어라면 
			return i;//명령어의 인덱스를 리턴한다.
	}		return -1;//없다면 -1
}

/* ----------------------------------------------------------------------------------
* 설명 :중간파일을 화면에 띄어주는 함수 
* 매계 : 없움
* 반환 : 없음
* -----------------------------------------------------------------------------------
*/
void make_opcode_output()
{

	int j = 0, p = 0, i = 0;
	FILE * file;
	if ((file = stdout) == NULL)//기록을 할 파일을 연다 
	{
		printf("fopen err at output file\n");
	}
	else{//파일이 오류가 없이 잘열리면 
		for (i = 0; i < imdata_line; i++)
		{

			if (imdata_table[i]->ctr == -1)
				fprintf(file, "\t\t");
			else
				fprintf(file, "%04X\t", imdata_table[i]->ctr);
			fprintf(file, "%s\t%s\t", imdata_table[i]->label, imdata_table[i]->oper);
			p = 0;
			while (imdata_table[i]->operand[p] != NULL)
			{
				if (p != 0)
					fprintf(file, ",");

				fprintf(file, "%s", imdata_table[i]->operand[p++]);//레이블 명령어 연산자 순서로 파일포인터에  입력한다
			}
			fprintf(file, "\t");

			if ((j = search_opcode(imdata_table[i]->oper)) != -1)//만약 연산자가 instruction set에 있다면 
			{
				if (strcmp(inst_table[j]->format, "2") == 0)
					fprintf(file, "%04X\n", imdata_table[i]->objectcode);// opcode를 16진법으로 출력하고 개행한다.
				if (strcmp(inst_table[j]->format, "1") == 0)
					fprintf(file, "%02X\n", imdata_table[i]->objectcode);// opcode를 16진법으로 출력하고 개행한다.
				if ((imdata_table[i]->nixbpe & 1) == 1)
					fprintf(file, "%08X\n", imdata_table[i]->objectcode);// opcode를 16진법으로 출력하고 개행한다.
				else if (strcmp(inst_table[j]->format, "3/4") == 0)
					fprintf(file, "%06X\n", imdata_table[i]->objectcode);// opcode를 16진법으로 출력하고 개행한다.
			}
			else if (strcmp(imdata_table[i]->oper, "WORD") == 0)
			{
				fprintf(file, "%06X\n", imdata_table[i]->objectcode);
			}
			else if (strcmp(imdata_table[i]->oper, "BYTE") == 0)
			{
				fprintf(file, "%X\n", imdata_table[i]->objectcode);

			}
			else if (strcmp(imdata_table[i]->label, "*") == 0)
			{
				fprintf(file, "%X\n", imdata_table[i]->objectcode);

			}
			else if (strcmp(imdata_table[i]->oper, "CSECT") == 0)
			{
				fprintf(file, "\t\n");
				sym_sec++;
			}
			else
				fprintf(file, "\t\n");//없다면 탭과 개행을 출력한다
		}
	}
	sym_sec = 0;
}

/* ----------------------------------------------------------------------------------
* 설명 :토큰 테이블에서 필요한 내용만 중간 파일로 옮겨 적는 과정
* 매계 : 옮겨 적을 라인
* 반환 : 오류<0 
* -----------------------------------------------------------------------------------
*/
int make_copy(int index){

	//	literal_table[index] = (literal*)malloc(sizeof(literal));
	imdata_table[imdata_line] = (imdata*)calloc(sizeof(imdata), sizeof(imdata));

	imdata_table[imdata_line]->label = (char*)malloc(sizeof(token_table[index]->label));
	imdata_table[imdata_line]->oper = (char*)malloc(sizeof(token_table[index]->oper));

	for (int m = 0; m < 3; m++){
		if (token_table[index]->operand[m] == NULL)
			continue;
		imdata_table[imdata_line]->operand[m] = (char*)malloc(sizeof(token_table[index]->operand[m]));
		strcpy(imdata_table[imdata_line]->operand[m], token_table[index]->operand[m]);
	}
	strcpy(imdata_table[imdata_line]->label, token_table[index]->label);//중간파일에 쓸 레이블
	strcpy(imdata_table[imdata_line]->oper, token_table[index]->oper);//중간파일에 쓸 연산자
	imdata_table[imdata_line]->nixbpe = token_table[index]->nixbpe;//nixbpe bits
	imdata_table[imdata_line]->ctr = locctr[loc_num];//중간파일에 쓸 locctr


	return 0;
}
/* ----------------------------------------------------------------------------------
* 설명 :중간 파일에서 로케이션 카운터를 적는 과정, 리터럴을 리터럴탭에 넣는 과정도 포함한다
* 매계 :중간 파일
* 반환 : 오류<0
* -----------------------------------------------------------------------------------
*/
int make_locctr(int index){
	int p = 0;

	if (imdata_table[imdata_line]->operand[0][0] == '=')
	{
		if (search_literal(imdata_table[imdata_line]->operand[0]) != -1)//리터럴풀에 이미 있는 리터럴이라면 
			literal_table[lit_num].address[literal_table[lit_num].many++] = imdata_table[imdata_line]->ctr;//사용한 위치만 입력
		else{//리터럴 풀에 넣는다
			char temp[50];
			if (imdata_table[imdata_line]->operand[0][1] == 'C')
			{
				literal_table[lit_num].type = 1;//리터럴 타입이 문자
			}
			else if (imdata_table[imdata_line]->operand[0][1] == 'X')
			{
				literal_table[lit_num].type = 2;//리터럴 타입이 숫자 
			}
			else{//에러
				return -1;
			}

			while ((imdata_table[imdata_line]->operand[0][p + 3]) != '\'')
			{
				temp[p++] = imdata_table[imdata_line]->operand[0][p + 3];

			}
			temp[p] = '\0';
			//strcpy(literal_table[lit_num].name, temp);//literal을 

			strcpy(literal_table[lit_num].name, imdata_table[imdata_line]->operand[0]);//literal의 값을 테이블에 저장
			literal_table[lit_num].address[literal_table[lit_num].many++] = imdata_table[imdata_line]->ctr;//사용한 위치를 저장 

			if (literal_table[lit_num].type == 1)
				literal_table[lit_num].length = p;//문자는 한글자가 1바이트
			else
				literal_table[lit_num].length = p / 2;//숫자는 한글자가 half바이트 (16진법이기떄문에)

			lit_num++;
		}
	}
	p = 0;
	if (strcmp(imdata_table[imdata_line]->label, "\t\0") != 0){
		if (strcmp(imdata_table[imdata_line]->oper, "CSECT") != 0){
			strcpy(sym_table[sym_sec][sym_line].symbol, imdata_table[imdata_line]->label);
			sym_table[sym_sec][sym_line].addr = imdata_table[imdata_line]->ctr;//레이블이 있으면 심볼테이블에 넣는다 
			sym_table[sym_sec][sym_line++].type = 1;//타입 설정(상대값)
		}
	}
	if ((p = search_opcode(imdata_table[imdata_line]->oper)) != -1)//명령어가 optable에 있는경우 
	{
		if (((imdata_table[imdata_line]->nixbpe) & 1) == 1){//4형식 명령어 nixbpe 에서 e가 1
			locctr[loc_num] += 4;
			imdata_line++;
			return 0;
		}
		else if (strcmp(inst_table[p]->format, "3/4") == 0){//3형식 명령어
			locctr[loc_num] += 3;
			imdata_line++;
			return 0;
		}
		else if (atoi(inst_table[p]->format) == 2)//2형식 명령어
		{
			locctr[loc_num] += 2;
			imdata_line++;
			return 0;
		}
		else if (atoi(inst_table[p]->format) == 1)//1형식 명령어
		{
			locctr[loc_num] += 1;
			imdata_line++;
			return 0;
		}
	}
	else{
		if (strcmp(imdata_table[imdata_line]->oper, "START") == 0){
			imdata_table[imdata_line]->ctr = 0;
			locctr[loc_num] = 0;
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "END") == 0){
			//ltorg기능 추가 

			imdata_table[imdata_line]->ctr = -1;
			if (lit_num > 0){
				for (int n = 0; n < lit_num; n++)
				{
					imdata_table[imdata_line + 1] = (imdata*)calloc(sizeof(imdata), sizeof(imdata));
					imdata_table[imdata_line + 1]->label = (char*)malloc(3);
					strcpy(imdata_table[imdata_line + 1 + n]->label, "*\0");//레이블에 *입력
					imdata_table[imdata_line + 1]->ctr = locctr[loc_num];//locctr입력
					imdata_table[imdata_line + 1]->oper = (char*)malloc(strlen(literal_table[n].name));
					strcpy(imdata_table[imdata_line + 1]->oper, literal_table[n].name);//연산자에 리터럴 입력

					strcpy(sym_table[sym_sec][sym_line].symbol, literal_table[n].name);//심볼 테이블에 이름 등록
					sym_table[sym_sec][sym_line].type = 1;//타입 설정(상대값)
					sym_table[sym_sec][sym_line++].addr = locctr[loc_num];//심볼의 주소

					locctr[loc_num] += literal_table[n].length;
					imdata_line++;

				}

			}
			lit_num = 0;
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "BYTE") == 0){//여기에 BYTE 관련

			int input_type = 0;
			int i = 0;

			char buf[30];
			if (imdata_table[imdata_line]->operand[0][0] == 'C')
			{
				input_type = 1;//문자타입
			}
			else if (imdata_table[imdata_line]->operand[0][0] == 'X')
			{
				input_type = 2;//숫자타입
			}
			else{//에러
				return -1;
			}

			while ((imdata_table[imdata_line]->operand[0][i + 2]) != '\'')
			{
				buf[i] = imdata_table[imdata_line]->operand[0][i + 2];
				i++;
			}
			buf[i] = '\0';

			if (input_type == 1)
			{
				locctr[loc_num] += i;
				int u = 0;
				imdata_table[imdata_line]->objectcode += buf[u];
				for (u = 1; u < (int)strlen(buf); u++)
				{
					imdata_table[imdata_line]->objectcode *= 0x100;
					imdata_table[imdata_line]->objectcode += buf[u];

				}
			}
			else if (input_type == 2)
			{
				locctr[loc_num] += i / 2;
				imdata_table[imdata_line]->objectcode += strtol(buf, NULL, 16);
			}
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "WORD") == 0){
			//여기에 WORD 관련
			locctr[loc_num] += 3;
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "RESB") == 0){
			locctr[loc_num] += atoi(imdata_table[imdata_line]->operand[0]);
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "RESW") == 0){
			locctr[loc_num] += atoi(imdata_table[imdata_line]->operand[0]) * 3;
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "CSECT") == 0){
			imdata_table[imdata_line]->ctr = -1;
			locctr[++loc_num] = 0;
			//	refmany[sym_sec] = 0;
			sym_sec++;
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "EXTDEF") == 0){
			//여기에 외부정의 관련 
			imdata_table[imdata_line]->ctr = -1;
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "EXTREF") == 0){
			//여기에 외부참조 관련 
			imdata_table[imdata_line]->ctr = -1;
			imdata_line++;

			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "EQU") == 0){
			strcpy(sym_table[sym_sec][sym_line].symbol, imdata_table[imdata_line]->label);
			char te1[10], te2[10];
			int t = 0;
			int h = has_op(imdata_table[imdata_line]->operand[0]);
			if (h != 0){//+나 -를 가지고 있으면
				{
					while (imdata_table[imdata_line]->operand[0][t] != '\0'){
						if (t < abs(h))
							te1[t] = imdata_table[imdata_line]->operand[0][t];
						if (t == abs(h))
							te1[t] = '\0';
						if (t>abs(h))
							te2[t - abs(h) - 1] = imdata_table[imdata_line]->operand[0][t];
						t++;
					}
					te2[t - abs(h) - 1] = '\0';
					if ((search_symbloc(te1) > 0) && (search_symbloc(te2) > 0))//두 심볼 모두 테이블에 있으면
					{

						if (h > 0){//연산자가 +
							if ((sym_table[sym_sec][search_symbloc(te1)].type == 2) && (sym_table[sym_sec][search_symbloc(te2)].type == 2))//두개다 절대값일 경우에만 
							{
								sym_table[sym_sec][sym_line].type = 2;//절대값이므로
							}
							else{//아니라면 
								sym_table[sym_sec][sym_line].type = 1;//상대값이므로

							}
							sym_table[sym_sec][sym_line].addr = sym_table[sym_sec][search_symbloc(te1)].addr + sym_table[sym_sec][search_symbloc(te2)].addr;
						}if (h < 0)//연산자가 -
						{
							if (sym_table[sym_sec][search_symbloc(te1)].type == sym_table[sym_sec][search_symbloc(te2)].type)//두개의 타입이 같으면 
							{
								sym_table[sym_sec][sym_line].type = 2;//절대값이므로 
							}
							else
							{
								sym_table[sym_sec][sym_line].type = 1;//상대값이므로 
							}
							sym_table[sym_sec][sym_line].addr = sym_table[sym_sec][search_symbloc(te1)].addr - sym_table[sym_sec][search_symbloc(te2)].addr;
						}
						imdata_table[imdata_line]->ctr = sym_table[sym_sec][sym_line].addr;
						sym_line++;
					}
					else{
						imdata_line++;
						return -1;//아닌경울 비정상 기록 
					}
				}
			}
			else if (imdata_table[imdata_line]->operand[0][0] == '*')
			{
				sym_table[sym_sec][sym_line].type = 1;
				sym_table[sym_sec][sym_line].addr = imdata_table[imdata_line]->ctr;
				sym_line++;
			}
			//여기에 EQU 관련 
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "LTORG") == 0){
			//여기에 LTORG 관련 

			imdata_table[imdata_line]->ctr = -1;
			if (lit_num > 0){
				for (int n = 0; n < lit_num; n++)
				{
					imdata_table[imdata_line + 1] = (imdata*)calloc(sizeof(imdata), sizeof(imdata));
					imdata_table[imdata_line + 1]->label = (char*)malloc(3);
					strcpy(imdata_table[imdata_line + 1 + n]->label, "*\0");//레이블에 *입력
					imdata_table[imdata_line + 1]->ctr = locctr[loc_num];//locctr입력
					imdata_table[imdata_line + 1]->oper = (char*)malloc(strlen(literal_table[n].name));
					strcpy(imdata_table[imdata_line + 1]->oper, literal_table[n].name);//연산자에 리터럴 입력

					strcpy(sym_table[sym_sec][sym_line].symbol, literal_table[n].name);//심볼 테이블에 이름 등록
					sym_table[sym_sec][sym_line].type = 1;//타입 설정(상대값)
					sym_table[sym_sec][sym_line++].addr = locctr[loc_num];//심볼의 주소

					locctr[loc_num] += literal_table[n].length;
					imdata_line++;

				}

			}
			lit_num = 0;
			imdata_line++;
			return 0;
		}


	}
	return -1;
}
/* ----------------------------------------------------------------------------------
* 설명 :입력한 문자열에 + 또는 -연산자의 위치와 사용여부를 알려주는 함수
* 매계 : 문자열
* 반환 : 비정상 종료 = 0 , +연산자의 위치>0 ,-연산자의 위치 <0
*
* -----------------------------------------------------------------------------------
*/
int has_op(char *str){
	int i = 0;
	while (str[i] != '\0'){
		if (str[i] == '+')
			return i; //+연산자의 위치
		if (str[i] == '-')
			return -i;//-연산자의 위치
		i++;
	}
	return 0;
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
*		   패스1에서는..
*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
*		   테이블을 생성한다.
*
* 매계 : 없음
* 반환 : 정상 종료 = 0 , 에러 = < 0
* 
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
	*(token_table) = (token*)malloc(sizeof(token)*inst_index);
	for (int n = 0; n < line_num; n++)
	if (token_parsing(n))
		return -1;
	*(imdata_table) = (imdata*)malloc(sizeof(imdata)*MAX_LINES);
	for (int n = 0; n < line_num; n++)
	{
		if (make_copy(n))
			return -1;
		if (make_locctr(n))
			return -1;

	}
	sym_sec = 0;
	loc_num = 0;
	return 0;
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
*		   다음과 같은 작업이 수행되어 진다.
*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
* 매계 : 없음
* 반환 : 정상종료 = 0, 에러발생 = < 0
* 주의 :
* -----------------------------------------------------------------------------------
*/
static int assem_pass2(void)
{

	for (int n = 0; n < imdata_line; n++)
		make_objectcode(n);

	sym_sec = 0;
	loc_num = 0;
	return 0;
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char *file_name)
{
	int j = 0, p = 0, i = 0;
	int record = 0;
	int sum = 0;
	int adr = 0;
	FILE * file;

	char temp[80] = "";
	char  buf[MAX_INST] = "";
	if ((file = fopen(file_name, "w")) == NULL)//기록을 할 파일을 연다 
	{
		printf("fopen err at output file\n");
	}
	else{//파일이 오류가 없이 잘열리면 
		for (i = 0; i < imdata_line; i++)
		{
			if ((j = search_opcode(imdata_table[i]->oper)) != -1)//만약 연산자가 instruction set에 있다면 
			{

				if (strcmp(inst_table[j]->format, "2") == 0){
					sprintf(buf, "%04X", imdata_table[i]->objectcode);// opcode를 16진법으로 입력한다.
					record = 4;
				}
				if (strcmp(inst_table[j]->format, "1") == 0){
					sprintf(buf, "%02X", imdata_table[i]->objectcode);// opcode를 16진법으로  입력한다.
					record = 2;
				}
				if ((imdata_table[i]->nixbpe & 1) == 1){
					sprintf(buf, "%08X", imdata_table[i]->objectcode);// opcode를 16진법으로  입력한다.
					record = 8;
				}
				else if (strcmp(inst_table[j]->format, "3/4") == 0){
					sprintf(buf, "%06X", imdata_table[i]->objectcode);// opcode를 16진법으로 입력한다.
					record = 6;
				}
			}
			else if ((strcmp(imdata_table[i]->oper, "START") == 0) || (strcmp(imdata_table[i]->oper, "CSECT") == 0))//새로운 H 레코드가 오는위치 
			{
				if (strcmp(imdata_table[i]->oper, "CSECT") == 0)//첫 레코드 헤더가 아니면 
				{
					sym_sec++;
					loc_num++;
					adr = 0;//시작주소 초기화 
				}
				int r = 0;
				fprintf(file, "H%s", imdata_table[i]->label);
				for (r = strlen(imdata_table[i]->label); r < 6; r++)
					fprintf(file, " ");
				fprintf(file, "%012X\n", locctr[loc_num]);//레코드를 출력 
				continue;

			}
			else if (strcmp(imdata_table[i]->oper, "EXTDEF") == 0)//외부정의 레코드가 있으면 기록한다
			{
				fprintf(file, "D");
				for (int k = 0; k < 3; k++)
				{
					if (imdata_table[i]->operand[k] != '\0')
						fprintf(file, "%6s%06X", imdata_table[i]->operand[k], sym_table[sym_sec][search_symbloc(imdata_table[i]->operand[k])].addr);
					else
						break;
				}
				fprintf(file, "\n");
			}
			else if (strcmp(imdata_table[i]->oper, "EXTREF") == 0)//외부 참조 레코드가 있으면 기록한다.
			{
				fprintf(file, "R");
				for (int k = 0; k < 3; k++)
				{
					if (imdata_table[i]->operand[k] != '\0')
						fprintf(file, "%-6s", imdata_table[i]->operand[k]);
					else
						break;
				}
				fprintf(file, "\n");
			}
			else if (strcmp(imdata_table[i]->oper, "WORD") == 0)
			{
				sprintf(buf, "%06X", imdata_table[i]->objectcode);
				record = 6;
			}
			else if (strcmp(imdata_table[i]->oper, "BYTE") == 0)
			{
				int k = 2;
				while (imdata_table[i]->operand[0][k] != '\'')
				{
					k++;
				}

				if (imdata_table[i]->operand[0][0] == 'C')//타입 구분 
					record = (k - 2) * 2;
				else if (imdata_table[i]->operand[0][0] == 'X')
					record = (k - 2);

				switch (record)//필드의 수가 다르기 때문에 구분해서 출력해준다
				{
				case 1:
					sprintf(buf, "%01X", imdata_table[i]->objectcode);
					break;
				case 2:
					sprintf(buf, "%02X", imdata_table[i]->objectcode);
					break;
				case 3:
					sprintf(buf, "%03X", imdata_table[i]->objectcode);
					break;
				case 4:
					sprintf(buf, "%04X", imdata_table[i]->objectcode);
					break;
				case 5:
					sprintf(buf, "%05X", imdata_table[i]->objectcode);
					break;
				case 6:
					sprintf(buf, "%06X", imdata_table[i]->objectcode);
					break;
				case 7:
					sprintf(buf, "%07X", imdata_table[i]->objectcode);
					break;
				case 8:
					sprintf(buf, "%08X", imdata_table[i]->objectcode);
					break;
				}
			}
			else if (strcmp(imdata_table[i]->label, "*") == 0)//만약 리터럴이 온다면 
			{
				int k = 3;
				while (imdata_table[i]->oper[k] != '\'')
				{
					k++;
				}

				if (imdata_table[i]->oper[1] == 'C')//리터럴의 타입 구분 
					record = (k - 3) * 2;
				else if (imdata_table[i]->oper[1] == 'X')
					record = (k - 3);

				switch (record)//리터럴 마다 필드의 수가 다르기 때문에 구분해서 출력해준다
				{
				case 1:
					sprintf(buf, "%01X", imdata_table[i]->objectcode);
					break;
				case 2:
					sprintf(buf, "%02X", imdata_table[i]->objectcode);
					break;
				case 3:
					sprintf(buf, "%03X", imdata_table[i]->objectcode);
					break;
				case 4:
					sprintf(buf, "%04X", imdata_table[i]->objectcode);
					break;
				case 5:
					sprintf(buf, "%05X", imdata_table[i]->objectcode);
					break;
				case 6:
					sprintf(buf, "%06X", imdata_table[i]->objectcode);
					break;
				case 7:
					sprintf(buf, "%07X", imdata_table[i]->objectcode);
					break;
				case 8:
					sprintf(buf, "%08X", imdata_table[i]->objectcode);
					break;
				}
			}
			else{//object code 출력에 관련없는 명령어 라인이라면 무시 
				strcpy(buf, "");
				record = 0;
			}

			if ((sum + record) > 59)//10~69를 넘는경우 다음 T레코드로 넘어감 
			{
				fprintf(file, "T%06X%02X", adr, sum / 2);
				fprintf(file, "%s\n", temp);//temp에 저장해두었던 레코드 기록 
				sum = record;
				strcpy(temp, buf);
				adr = imdata_table[i]->ctr;
				continue;
			}
			else if (i == (imdata_line - 1))//마지막 줄이거나 
			{
				sum += record;
				strcat(temp, buf);
				fprintf(file, "T%06X%02X", adr, sum / 2);
				fprintf(file, "%s\n", temp);
				strcpy(buf, "");
				record = 0;
				strcpy(temp, "");
				sum = 0;
				for (int v = 0; v < refmany[sym_sec]; v++){
					fprintf(file, "M%06X", ref_table[sym_sec][v].add);//M 레코드 출력 
					if (ref_table[sym_sec][v].size == 5)//
						fprintf(file, "05");
					else
						fprintf(file, "06");
					if (ref_table[sym_sec][v].op == 0)//연산의 종류를 파악한다
						fprintf(file, "+");//만약 주소값만큼 더해야한다면
					else
						fprintf(file, "-");//빼야하는경우라면
					fprintf(file, "%s\n", ref_table[sym_sec][v].name);//참조한위치의 주소
				}
				fprintf(file, "E000000");
				continue;
			}
			else if ((strcmp(imdata_table[i + 1]->oper, "LTORG") == 0) || (strcmp(imdata_table[i + 1]->oper, "CSECT") == 0)){//줄을 바꿔야하는경우 
				sum += record;
				strcat(temp, buf);
				fprintf(file, "T%06X%02X", adr, sum / 2);
				fprintf(file, "%s\n", temp);//temp에 저장해두었던 레코드 기록 
				strcpy(buf, "");
				record = 0;
				strcpy(temp, "");
				sum = 0;
				adr = imdata_table[i + 2]->ctr;
				if ((strcmp(imdata_table[i + 1]->oper, "CSECT") == 0))//다음 섹션으로 가는경우라면 
				{
					for (int v = 0; v < refmany[sym_sec]; v++){//M 레코드 출력
						fprintf(file, "M%06X", ref_table[sym_sec][v].add);
						if (ref_table[sym_sec][v].size == 5)
							fprintf(file, "05");
						else
							fprintf(file, "06");
						if (ref_table[sym_sec][v].op == 0)//연산의 종류를 파악한다
							fprintf(file, "+");//만약 주소값만큼 더해야한다면 
						else
							fprintf(file, "-");//빼야하는경우라면 
						fprintf(file, "%s\n", ref_table[sym_sec][v].name);//참조한위치의 주소
					}
					fprintf(file, "E000000\n\n");
				}
				continue;
			}
			if (record != 0)
			{
				sum += record;
				strcat(temp, buf);

			}

		}

	}

}
