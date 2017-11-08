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

#include "my_assembler.h"

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
		if(init_my_assembler()< 0)
		{
				printf("init_my_assembler: 프로그램 초기화에 실패 했습니다.\n"); 
				return -1 ; 
		}

		for(int i=0;i<line_num;i++)
				if(token_parsing(i)<0)//라인의 수만큼 파싱을 진행한다.
						printf("parsing err\n");//만약 토큰 파싱 함수가 음수면 파싱이 에러난것

		make_opcode_output("output.txt");


		/* 
		 * 추후 프로젝트 1에서 사용되는 부분
		 *
		 if(assem_pass1() < 0 ){
		 printf("assem_pass1: 패스1 과정에서 실패하였습니다.  \n") ; 
		 return -1 ; 
		 }
		 if(assem_pass2() < 0 ){
		 printf(" assem_pass2: 패스2 과정에서 실패하였습니다.  \n") ; 
		 return -1 ; 
		 }

		 make_objectcode_output("output") ; 
		 */
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
		int result ; 

		if((result = init_inst_file("instset.txt")) < 0 )
				return -1 ;
		if((result = init_input_file("input.txt")) < 0 )
				return -1 ; 
		return result ; //각각의 초기화가 실패하면 리턴 -1;
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
		int i=0;
		char  buf[MAX_INST];
		if((file=fopen(inst_file,"r"))==NULL)
		{
				printf("fopen err at instruction set\n");
				return -1;
		}
		while(fgets(buf,MAX_INST,file))//buf로 명령어 집합을 받아 한줄씩 저장한다
		{
				char a[10];
				char b[10];
				int c=0;
				int d=0;
				sscanf(buf,"%s%s%x%d",a,b,&c,&d);//버퍼를 각각의 데이터 형에 따라서 나눈다 
				inst_table[i].oper=malloc(sizeof(a));
				strcpy(inst_table[i].oper,a);//operation 이름과  
				inst_table[i].format=malloc(sizeof(b));
				strcpy(inst_table[i].format,b);//포멧을 할당하고 
				inst_table[i].opcode=c;//opcode와 
				inst_table[i].opernum=d;//연산자의 개수도 넣는다 
				i++;
		}

		inst_index=i;//명령어의 갯수도 입력한다.

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
		int i=0;
		char  buf[MAX_INST];
		if((file=fopen(input_file,"r"))==NULL)//input file을 열어서
		{	printf("fopen err at input file\n"); 
				return -1;
		}
		while(fgets(buf,MAX_INST,file))//오류가 안난다면 버퍼에 한줄씩 담는다.
		{
				if(buf[0]=='.')//.으로 시작하는 주석문은 건너띈다 
						continue;
				input_data[i]=malloc(sizeof(buf));//input_data에 한줄씩 옮겨 저장한다 
				strcpy(input_data[i++],buf);
				line_num++;//라인의 수를 기록한다 최대 5000 라인
		}return 0;
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
		int i=0,j=0;
		char buf[100];
		if(input_data[index][j]=='\t'){//만약 index 라는 라인이 tab으로 시작하면 (= 레이블이 없으면)
				token_table[index].label="\t\0";//레이블값에 탭만 넣어준다 
				j++;
		}else{
				while(input_data[index][j]!='\t')//레이블이 있다면 
				{
						buf[i++]=input_data[index][j++];//버퍼에 하나씩 저장한후 
				}
				buf[i]='\0';//뒤에 널문자를 붙인후 
				token_table[index].label=malloc(sizeof(buf));
				strcpy(token_table[index].label,buf);//토큰 테이블에 저장한다.
		}
		j++;
		i=0;

		while(input_data[index][j]!='\t')//명령어 위치의 값을 받기 시작한다 
		{
				buf[i++]=input_data[index][j++];
		}
		buf[i]='\0';//다 받으면 뒤에 널문자를 붙이고 
		token_table[index].oper=malloc(sizeof(buf));
		strcpy(token_table[index].oper,buf);//operation 에 저장한다 

		j++;
		i=0;

		if(input_data[index][j]=='\t')
		{
				token_table[index].operand[0]="\t\0";//만약 연산자가 없다면 첫번쨰 연산자 위치에 탭을 저장한다 .
				j++;
		}else{
				while(input_data[index][j]!='\t')
				{
						buf[i++]=input_data[index][j++];
				}
				buf[i]='\0';
				token_table[index].operand[0]=malloc(sizeof(buf));
				strcpy(token_table[index].operand[0],buf);//있다면 옮긴후 뒤에 널문자를 붙여서 저장한다
		}

		j++;
		i = 0;

		while (input_data[index][j] != '\0')//comment가 있다면 
		{
			buf[i++] = input_data[index][j++];
		}
		buf[i] = '\0';
		token_table[index].comment = malloc(sizeof(buf));
		strcpy(token_table[index].comment, buf);//comment도 뒤에 널문자를 붙여 저장한다. 

		j++;
		i = 0;


		return 0;

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
		for(int i=0 ;i<inst_index;i++){//전체 명령어의 수를 전부 탐색한뒤
				if(strcmp(str,inst_table[i].oper)==0)//만약 있는 명령어라면 
						return i;//명령어의 인덱스를 리턴한다.
		}		return -1;//없다면 -1


}

/* ----------------------------------------------------------------------------------
 * 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
 *        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 4번) 이다.
 * 매계 : 생성할 오브젝트 파일명
 * 반환 : 없음
 * 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
 *        화면에 출력해준다.
 *        또한 과제 4번에서만 쓰이는 함수이므로 이후의 프로젝트에서는 사용되지 않는다.
 * -----------------------------------------------------------------------------------
 */
void make_opcode_output(char *file_name)
{

		int j, i=0;
		FILE * file;
		char  buf[MAX_INST];
		if((file=fopen(file_name,"w"))==NULL)//기록을 할 파일을 연다 
		{	printf("fopen err at output file\n");
		}else{//파일이 오류가 없이 잘열리면 
				for(i=0;i<line_num;i++)
				{			fprintf(file,"%s\t%s\t%s\t",token_table[i].label,token_table[i].oper,token_table[i].operand[0]);//레이블 명령어 연산자 순서로 파일포인터에  입력한다
						if((j=search_opcode(token_table[i].oper))!=-1)//만약 연산자가 instruction set에 있다면 
								fprintf(file,"%x\n",inst_table[j].opcode);// opcode를 16진법으로 출력하고 개행한다.
						else
								fprintf(file,"\t\n");//없다면 탭과 개행을 출력한다
				}
		}
}







/* --------------------------------------------------------------------------------*
 * ------------------------- 추후 프로젝트에서 사용할 함수 --------------------------*
 * --------------------------------------------------------------------------------*/


/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
 *		   패스1에서는..
 *		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
 *		   테이블을 생성한다.
 *
 * 매계 : 없음
 * 반환 : 정상 종료 = 0 , 에러 = < 0
 * 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
 *	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
 *
 * -----------------------------------------------------------------------------------
 */
static int assem_pass1(void)
{
		/* add your code here */

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

		/* add your code here */

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
		/* add your code here */

}
