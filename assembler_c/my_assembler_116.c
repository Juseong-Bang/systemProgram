/*
 * ȭ�ϸ� : my_assembler.c
 * ��  �� : �� ���α׷��� SIC/XE �ӽ��� ���� ������ Assembler ���α׷��� ���η�ƾ����,
 * �Էµ� ������ �ڵ� ��, ��ɾ �ش��ϴ� OPCODE�� ã�� ����Ѵ�.
 *
 */

/*
 *
 * ���α׷��� ����� �����Ѵ�.
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
 * ���� : ����ڷ� ���� ����� ������ �޾Ƽ� ��ɾ��� OPCODE�� ã�� ����Ѵ�.
 * �Ű� : ���� ����, ����� ����
 * ��ȯ : ���� = 0, ���� = < 0
 * ���� : ���� ����� ���α׷��� ����Ʈ ������ �����ϴ� ��ƾ�� ������ �ʾҴ�.
 *		   ���� �߰������� �������� �ʴ´�.
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
	make_opcode_output();//ȭ�鿡 ������ִ� �Լ�
	make_objectcode_output("output_116.txt");

	return 0;
}

/* ----------------------------------------------------------------------------------
 * ���� : ���α׷� �ʱ�ȭ�� ���� �ڷᱸ�� ���� �� ������ �д� �Լ��̴�.
 * �Ű� : ����
 * ��ȯ : �������� = 0 , ���� �߻� = -1
 * ���� : ������ ��ɾ� ���̺��� ���ο� �������� �ʰ� ������ �����ϰ� �ϱ�
 *		   ���ؼ� ���� ������ �����Ͽ� ���α׷� �ʱ�ȭ�� ���� ������ �о� �� �� �ֵ���
 *		   �����Ͽ���.
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
 * ���� : �ӽ��� ���� ��� �ڵ��� ������ �о� ���� ��� ���̺�(inst_table)��
 *        �����ϴ� �Լ��̴�.
 * �Ű� : ���� ��� ����
 * ��ȯ : �������� = 0 , ���� < 0
 * ���� : ���� ������� ������ �����Ӱ� �����Ѵ�. ���ô� ������ ����.
 *
 *	===============================================================================
 *		   | �̸� | ���� | ���� �ڵ� | ���۷����� ���� | NULL|
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


	while (fgets(buf, MAX_INST, file))//buf�� ��ɾ� ������ �޾� ���پ� �����Ѵ�
	{
		inst_table[i] = (inst*)malloc(sizeof(inst));
		char a[10];
		char b[10];
		int c = 0;
		int d = 0;

		sscanf(buf, "%s%s%x%d", a, b, &c, &d);//���۸� ������ ������ ���� ���� ������ 
		strcpy(inst_table[i]->oper, a);//operation �̸���  
		strcpy(inst_table[i]->format, b);//������ �Ҵ��ϰ� 
		inst_table[i]->opcode = c;//opcode�� 
		inst_table[i++]->opernum = d;//�������� ������ �ִ´� 
	}

	inst_index = i;//��ɾ��� ������ �Է��Ѵ�.

	return 0;

}

/* ----------------------------------------------------------------------------------
 * ���� : ����� �� �ҽ��ڵ带 �о���� �Լ��̴�.
 * �Ű� : ������� �ҽ����ϸ�
 * ��ȯ : �������� = 0 , ���� < 0
 * ���� :
 *
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char *input_file)
{
	FILE * file;
	int i = 0;
	char  buf[MAX_INST];
	if ((file = fopen(input_file, "r")) == NULL)//input file�� ���
	{
		printf("fopen err at input file\n");
		return -1;
	}
	while (fgets(buf, MAX_INST, file))//������ �ȳ��ٸ� ���ۿ� ���پ� ��´�.
	{
		if (buf[0] == '.')//.���� �����ϴ� �ּ����� �ǳʶ�� 
			continue;
		input_data[i] = malloc(sizeof(buf));//input_data�� ���پ� �Ű� �����Ѵ� 
		strcpy(input_data[i++], buf);
		line_num++;//������ ���� ����Ѵ� �ִ� 5000 ����
	}
	return 0;
}


/* ----------------------------------------------------------------------------------
 * ���� : �ҽ� �ڵ带 �о�� ��ū������ �м��ϰ� ��ū ���̺��� �ۼ��ϴ� �Լ��̴�.
 *        �н� 1�� ���� ȣ��ȴ�.
 * �Ű� : �ҽ��ڵ��� ���ι�ȣ
 * ��ȯ : �������� = 0 , ���� < 0
 * ���� : my_assembler ���α׷������� ���δ����� ��ū �� ������Ʈ ������ �ϰ� �ִ�.
 * ----------------------------------------------------------------------------------
 */
int token_parsing(int index)
{

	token_table[index] = (token*)calloc(sizeof(token), sizeof(token));
	int i = 0, j = 0;
	char buf[100];
	if (input_data[index][j] == '\t'){//���� index ��� ������ tab���� �����ϸ� (= ���̺��� ������)
		token_table[index]->label = "\t\0";//���̺��� �Ǹ� �־��ش� 
		j++;
	}
	else{
		while (input_data[index][j] != '\t')//���̺��� �ִٸ� 
		{
			buf[i++] = input_data[index][j++];//���ۿ� �ϳ��� �������� 
		}
		buf[i] = '\0';//�ڿ� �ι��ڸ� ������ 
		token_table[index]->label = malloc(sizeof(buf));
		strcpy(token_table[index]->label, buf);//��ū ���̺� �����Ѵ�.
	}
	j++;
	i = 0;

	while (input_data[index][j] != '\t')//��ɾ� ��ġ�� ���� �ޱ� �����Ѵ� 
	{
		if (input_data[index][j] == '+')
		{

			token_table[index]->nixbpe += 1;//4���� ǥ��
			j++;
			continue;
		}
		buf[i++] = input_data[index][j++];
	}
	buf[i] = '\0';//�� ������ �ڿ� �ι��ڸ� ���̰� 
	token_table[index]->oper = malloc(sizeof(buf));
	strcpy(token_table[index]->oper, buf);//operation �� �����Ѵ� 

	i = 0;
	j++;
	int t = 0;
	if (input_data[index][j] == '\t')
	{
		token_table[index]->operand[0] = "\t\0";//���� �����ڰ� ���ٸ� ù���� ������ ��ġ�� ���� �����Ѵ� .
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
		strcpy(token_table[index]->operand[t], buf);//�ִٸ� �ű��� �ڿ� �ι��ڸ� �ٿ��� �����Ѵ�
	}

	j++;
	i = 0;

	i = search_opcode(token_table[index]->oper);
	i = 0;
	while (input_data[index][j] != '\0')//comment�� �ִٸ� 
	{
		buf[i++] = input_data[index][j++];
	}
	buf[i] = '\0';
	token_table[index]->comment = malloc(sizeof(buf));
	strcpy(token_table[index]->comment, buf);//comment�� �ڿ� �ι��ڸ� �ٿ� �����Ѵ�. 

	j++;
	i = 0;
	if (token_table[index]->operand[0][0] == '#')//Immediate addressing
		token_table[index]->nixbpe += 16;
	if (token_table[index]->operand[0][0] == '@')//Indirect addressing
		token_table[index]->nixbpe += 32;

	return 0;


}
/* ----------------------------------------------------------------------------------
* ���� : �Է� ���ڿ��� ���ͷ����� �˷��ִ� �Լ��̴�.
* �Ű� : ��ū ������ ���е� ���ڿ�
* ��ȯ : �������� = ���ͷ� ���̺� �ε���, ���� < 0
* ���� :
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
* ���� : �Է� ���ڿ��� �ɺ����� �˷��ִ� �Լ��̴�.
* �Ű� : ��ū ������ ���е� ���ڿ�
* ��ȯ : �������� = �ɺ� ���̺� �ε���, ���� < 0
* ���� :
*
* ----------------------------------------------------------------------------------
*/
int search_symbloc(char *str)
{
	int i = 0;
	char *tem = str;
	if ((tem[0] == '#') || (tem[0] == '@'))
		tem++;

	for (i; i < sym_line; i++){//��ü �ɺ��� ���� ���� Ž���ѵ�
		if (strcmp(tem, sym_table[sym_sec][i].symbol) == 0)//���� �ִ� �ɺ��̶�� 
			return i;//�ɺ��� �ε����� �����Ѵ�.
	}		return -1;//���ٸ� -1
}
/* ----------------------------------------------------------------------------------
* ���� : �н�1���� ���� ������ ���� ������Ʈ �ڵ带 ������ִ� �Լ��̴�. ���� ��Ƽ��� 
*��巹�� ������ �� �Լ����� �ǴܵǾ� ���ȴ�
* �Ű� : �ٲ��� ���� �ѹ�
* ��ȯ :  ���� < 0
*
* ----------------------------------------------------------------------------------
*/
int make_objectcode(int imdata_line){
	int p = 0, s = 0, t = 0;

	if ((p = search_opcode(imdata_table[imdata_line]->oper)) != -1){//���� opcode�� �ִ� �����̶�� 
		if ((imdata_table[imdata_line]->nixbpe & 1) == 1){//4����
			if (imdata_table[imdata_line]->operand[1] != NULL)
			if (strcmp(imdata_table[imdata_line]->operand[1], "X") == 0)//nixbpe�߿� x��Ʈ üũ
			{
				imdata_table[imdata_line]->nixbpe += 8;
			}
			if ((imdata_table[imdata_line]->nixbpe & 48) == 0)//n�̳� i�� �ƴϸ� 
				imdata_table[imdata_line]->nixbpe += 48;//ni bit Ŵ
			imdata_table[imdata_line]->objectcode += inst_table[p]->opcode * 0x1000000;//opcode �ڸ��� ����
			imdata_table[imdata_line]->objectcode += imdata_table[imdata_line]->nixbpe * 0x100000;//nixbpe �ڸ��� ����
			if ((s = search_symbloc(imdata_table[imdata_line]->operand[0])) != -1)//�ɺ��� �ִٸ�
			{
				imdata_table[imdata_line]->objectcode += sym_table[sym_sec][s].addr;//�ɺ��� �ּҸ� ����
			}
			else if (search_ref(imdata_table[imdata_line]->operand[0]) != -1){//���� ������ ������ ����ߴٸ� 
				ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr + 1;
				ref_table[sym_sec][refmany[sym_sec]].size = 5;//5 halfbyte ũ�⸸ŭ ���ڵ� ����
				strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, imdata_table[imdata_line]->operand[0]);
			}
			else if (((imdata_table[imdata_line]->operand[0][0] == '#') && ((imdata_table[imdata_line]->nixbpe & 16) == 16))){//�ɺ��� ���� i bit�� 1�ϋ� 
				char *temp = imdata_table[imdata_line]->operand[0] + 1;//#�� �� Ÿ�� ����� 
				imdata_table[imdata_line]->objectcode += atoi(temp);//objectcode�� �ִ´�.
			}
		}
		else if ((strcmp(inst_table[p]->format, "3/4")) == 0)//3����
		{
			if (imdata_table[imdata_line]->operand[1] != NULL)
			if (strcmp(imdata_table[imdata_line]->operand[1], "X") == 0)//nixbpe�߿� x��Ʈ üũ
			{
				imdata_table[imdata_line]->nixbpe += 8;//xbit üũ 
			}
			if ((s = search_symbloc(imdata_table[imdata_line]->operand[0])) != -1)//�ɺ��� �ִٸ� 
			{
				if (abs(sym_table[sym_sec][s].addr - (imdata_table[imdata_line]->ctr + 3)) < 0x1000){
					imdata_table[imdata_line]->objectcode += ((sym_table[sym_sec][s].addr - (imdata_table[imdata_line]->ctr + 3))) & 0xfff;//�����ϰ�� 3byte�� �������� 
				}
				else{//�ɺ��� ������ pc relative�� ���� 
					return -1;
				}
				imdata_table[imdata_line]->nixbpe += 2;//pc ralative
				//imdata_table[imdata_line]->objectcode += sym_table[sym_sec][s].addr;//�ɺ��� �ּҸ� ����(����ּҷ� �ٲ����)
			}
			else if (((imdata_table[imdata_line]->operand[0][0] == '#') && ((imdata_table[imdata_line]->nixbpe & 16) == 16))){//�ɺ��� ���� i bit�� 1�ϋ� 
				char *temp = imdata_table[imdata_line]->operand[0] + 1;//#�� �� Ÿ�� ����� 
				imdata_table[imdata_line]->objectcode += atoi(temp);//objectcode�� �ִ´�.
			}
			if ((imdata_table[imdata_line]->nixbpe & 48) == 0)//n�̳� i�� �ƴϸ� 
				imdata_table[imdata_line]->nixbpe += 48;//ni bit Ŵ
			imdata_table[imdata_line]->objectcode += inst_table[p]->opcode * 0x10000;//opcode �ڸ��� ����
			imdata_table[imdata_line]->objectcode += imdata_table[imdata_line]->nixbpe * 0x1000;//nixbpe �ڸ��� ����
		}

		else if ((strcmp(inst_table[p]->format, "2")) == 0){//2����
			imdata_table[imdata_line]->objectcode += inst_table[p]->opcode * 0x100;//opcode �ڸ��� ����
			imdata_table[imdata_line]->objectcode += find_reg(imdata_table[imdata_line]->operand[0][0]) * 16;
			if (imdata_table[imdata_line]->operand[1] != NULL)
				imdata_table[imdata_line]->objectcode += find_reg(imdata_table[imdata_line]->operand[1][0]);
		}
		else if ((strcmp(inst_table[p]->format, "1")) == 0)
			imdata_table[imdata_line]->objectcode += inst_table[p]->opcode;//opcode �ڸ��� ����
	}
	else{
		if (strcmp(imdata_table[imdata_line]->oper, "BYTE") == 0){//���⿡ BYTE ����
			int input_type = 0;
			int i = 0;

			char buf[30];
			if (imdata_table[imdata_line]->operand[0][0] == 'C')
			{
				input_type = 1;//����Ÿ��
			}
			else if (imdata_table[imdata_line]->operand[0][0] == 'X')
			{
				input_type = 2;//����Ÿ��
			}
			else{//����
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
			if (h != 0){//+�� -�� ������ ������
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
					if ((search_ref(te1) != -1) && (search_ref(te2) != -1))//�� �ɺ� ��� ���̺� ������
					{

						if (h > 0){//�����ڰ� +
							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6; //6 halfbyte ũ�⸸ŭ ���ڵ� ����
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te1);

							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte ũ�⸸ŭ ���ڵ� ����
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te2);

						}if (h < 0)//�����ڰ� -
						{
							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte ũ�⸸ŭ ���ڵ� ����
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te1);

							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte ũ�⸸ŭ ���ڵ� ����
							strcpy(ref_table[sym_sec][refmany[sym_sec]].name, te2);
							ref_table[sym_sec][refmany[sym_sec]++].op = 1;//������ Ÿ�� -
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
			//���⿡ WORD ����
			char te1[10], te2[10];
			int t = 0;
			int h = has_op(imdata_table[imdata_line]->operand[0]);
			if (h != 0){//+�� -�� ������ ������
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
					if ((search_ref(te1) != -1) && (search_ref(te2) != -1))//�� �ɺ� ��� ���̺� ������
					{

						if (h > 0){//�����ڰ� +
							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6; //6 halfbyte ũ�⸸ŭ ���ڵ� ����
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te1);

							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte ũ�⸸ŭ ���ڵ� ����
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te2);

						}if (h < 0)//�����ڰ� -
						{
							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte ũ�⸸ŭ ���ڵ� ����
							strcpy(ref_table[sym_sec][refmany[sym_sec]++].name, te1);

							ref_table[sym_sec][refmany[sym_sec]].add = imdata_table[imdata_line]->ctr;
							ref_table[sym_sec][refmany[sym_sec]].size = 6;//6 halfbyte ũ�⸸ŭ ���ڵ� ����
							strcpy(ref_table[sym_sec][refmany[sym_sec]].name, te2);
							ref_table[sym_sec][refmany[sym_sec]++].op = 1;//������ Ÿ�� -
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
					strcpy(extref[sym_sec][k], imdata_table[imdata_line]->operand[k]);//EXTREF���� ������ �ܺ������� �����
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
				input_type = 1;//����Ÿ��
			}
			else if (imdata_table[imdata_line]->oper[1] == 'X')
			{
				input_type = 2;//����Ÿ��
			}
			else{//����
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
* ���� :EXREF���� �ܺ� ������ ������ �������� ã���ִ� �Լ��̴�.
* �Ű� : ��ū ������ ���е� ���ڿ�
* ��ȯ : �������� =�ε���, ���� < 0
* ���� :
*
* ----------------------------------------------------------------------------------
*/
int search_ref(char *str){
	int i = 0;
	char *tem = str;
	for (i; i < 10; i++){//��ü �ɺ��� ���� ���� Ž���ѵ�
		if (extref[sym_sec][i] != NULL)
		{
			if (strcmp(tem, extref[sym_sec][i]) == 0)//���� �ִ� �ɺ��̶�� 
				return i;//�ɺ��� �ε����� �����Ѵ�.
		}
		else
			return -1;
	}//���ٸ� -1
	return -1;
}
/* ----------------------------------------------------------------------------------
* ���� :���������� �ѹ���  ã���ִ� �Լ��̴�.
* �Ű� :�� ����
* ��ȯ : �������� =�������� �ѹ� �ε���
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
 * ���� : �Է� ���ڿ��� ���� �ڵ������� �˻��ϴ� �Լ��̴�.
 * �Ű� : ��ū ������ ���е� ���ڿ�
 * ��ȯ : �������� = ���� ���̺� �ε���, ���� < 0
 * ���� :
 *
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str)
{

	for (int i = 0; i < inst_index; i++){//��ü ��ɾ��� ���� ���� Ž���ѵ�
		if (strcmp(str, inst_table[i]->oper) == 0)//���� �ִ� ��ɾ��� 
			return i;//��ɾ��� �ε����� �����Ѵ�.
	}		return -1;//���ٸ� -1
}

/* ----------------------------------------------------------------------------------
* ���� :�߰������� ȭ�鿡 ����ִ� �Լ� 
* �Ű� : ����
* ��ȯ : ����
* -----------------------------------------------------------------------------------
*/
void make_opcode_output()
{

	int j = 0, p = 0, i = 0;
	FILE * file;
	if ((file = stdout) == NULL)//����� �� ������ ���� 
	{
		printf("fopen err at output file\n");
	}
	else{//������ ������ ���� �߿����� 
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

				fprintf(file, "%s", imdata_table[i]->operand[p++]);//���̺� ��ɾ� ������ ������ ���������Ϳ�  �Է��Ѵ�
			}
			fprintf(file, "\t");

			if ((j = search_opcode(imdata_table[i]->oper)) != -1)//���� �����ڰ� instruction set�� �ִٸ� 
			{
				if (strcmp(inst_table[j]->format, "2") == 0)
					fprintf(file, "%04X\n", imdata_table[i]->objectcode);// opcode�� 16�������� ����ϰ� �����Ѵ�.
				if (strcmp(inst_table[j]->format, "1") == 0)
					fprintf(file, "%02X\n", imdata_table[i]->objectcode);// opcode�� 16�������� ����ϰ� �����Ѵ�.
				if ((imdata_table[i]->nixbpe & 1) == 1)
					fprintf(file, "%08X\n", imdata_table[i]->objectcode);// opcode�� 16�������� ����ϰ� �����Ѵ�.
				else if (strcmp(inst_table[j]->format, "3/4") == 0)
					fprintf(file, "%06X\n", imdata_table[i]->objectcode);// opcode�� 16�������� ����ϰ� �����Ѵ�.
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
				fprintf(file, "\t\n");//���ٸ� �ǰ� ������ ����Ѵ�
		}
	}
	sym_sec = 0;
}

/* ----------------------------------------------------------------------------------
* ���� :��ū ���̺��� �ʿ��� ���븸 �߰� ���Ϸ� �Ű� ���� ����
* �Ű� : �Ű� ���� ����
* ��ȯ : ����<0 
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
	strcpy(imdata_table[imdata_line]->label, token_table[index]->label);//�߰����Ͽ� �� ���̺�
	strcpy(imdata_table[imdata_line]->oper, token_table[index]->oper);//�߰����Ͽ� �� ������
	imdata_table[imdata_line]->nixbpe = token_table[index]->nixbpe;//nixbpe bits
	imdata_table[imdata_line]->ctr = locctr[loc_num];//�߰����Ͽ� �� locctr


	return 0;
}
/* ----------------------------------------------------------------------------------
* ���� :�߰� ���Ͽ��� �����̼� ī���͸� ���� ����, ���ͷ��� ���ͷ��ǿ� �ִ� ������ �����Ѵ�
* �Ű� :�߰� ����
* ��ȯ : ����<0
* -----------------------------------------------------------------------------------
*/
int make_locctr(int index){
	int p = 0;

	if (imdata_table[imdata_line]->operand[0][0] == '=')
	{
		if (search_literal(imdata_table[imdata_line]->operand[0]) != -1)//���ͷ�Ǯ�� �̹� �ִ� ���ͷ��̶�� 
			literal_table[lit_num].address[literal_table[lit_num].many++] = imdata_table[imdata_line]->ctr;//����� ��ġ�� �Է�
		else{//���ͷ� Ǯ�� �ִ´�
			char temp[50];
			if (imdata_table[imdata_line]->operand[0][1] == 'C')
			{
				literal_table[lit_num].type = 1;//���ͷ� Ÿ���� ����
			}
			else if (imdata_table[imdata_line]->operand[0][1] == 'X')
			{
				literal_table[lit_num].type = 2;//���ͷ� Ÿ���� ���� 
			}
			else{//����
				return -1;
			}

			while ((imdata_table[imdata_line]->operand[0][p + 3]) != '\'')
			{
				temp[p++] = imdata_table[imdata_line]->operand[0][p + 3];

			}
			temp[p] = '\0';
			//strcpy(literal_table[lit_num].name, temp);//literal�� 

			strcpy(literal_table[lit_num].name, imdata_table[imdata_line]->operand[0]);//literal�� ���� ���̺� ����
			literal_table[lit_num].address[literal_table[lit_num].many++] = imdata_table[imdata_line]->ctr;//����� ��ġ�� ���� 

			if (literal_table[lit_num].type == 1)
				literal_table[lit_num].length = p;//���ڴ� �ѱ��ڰ� 1����Ʈ
			else
				literal_table[lit_num].length = p / 2;//���ڴ� �ѱ��ڰ� half����Ʈ (16�����̱⋚����)

			lit_num++;
		}
	}
	p = 0;
	if (strcmp(imdata_table[imdata_line]->label, "\t\0") != 0){
		if (strcmp(imdata_table[imdata_line]->oper, "CSECT") != 0){
			strcpy(sym_table[sym_sec][sym_line].symbol, imdata_table[imdata_line]->label);
			sym_table[sym_sec][sym_line].addr = imdata_table[imdata_line]->ctr;//���̺��� ������ �ɺ����̺� �ִ´� 
			sym_table[sym_sec][sym_line++].type = 1;//Ÿ�� ����(��밪)
		}
	}
	if ((p = search_opcode(imdata_table[imdata_line]->oper)) != -1)//��ɾ optable�� �ִ°�� 
	{
		if (((imdata_table[imdata_line]->nixbpe) & 1) == 1){//4���� ��ɾ� nixbpe ���� e�� 1
			locctr[loc_num] += 4;
			imdata_line++;
			return 0;
		}
		else if (strcmp(inst_table[p]->format, "3/4") == 0){//3���� ��ɾ�
			locctr[loc_num] += 3;
			imdata_line++;
			return 0;
		}
		else if (atoi(inst_table[p]->format) == 2)//2���� ��ɾ�
		{
			locctr[loc_num] += 2;
			imdata_line++;
			return 0;
		}
		else if (atoi(inst_table[p]->format) == 1)//1���� ��ɾ�
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
			//ltorg��� �߰� 

			imdata_table[imdata_line]->ctr = -1;
			if (lit_num > 0){
				for (int n = 0; n < lit_num; n++)
				{
					imdata_table[imdata_line + 1] = (imdata*)calloc(sizeof(imdata), sizeof(imdata));
					imdata_table[imdata_line + 1]->label = (char*)malloc(3);
					strcpy(imdata_table[imdata_line + 1 + n]->label, "*\0");//���̺� *�Է�
					imdata_table[imdata_line + 1]->ctr = locctr[loc_num];//locctr�Է�
					imdata_table[imdata_line + 1]->oper = (char*)malloc(strlen(literal_table[n].name));
					strcpy(imdata_table[imdata_line + 1]->oper, literal_table[n].name);//�����ڿ� ���ͷ� �Է�

					strcpy(sym_table[sym_sec][sym_line].symbol, literal_table[n].name);//�ɺ� ���̺� �̸� ���
					sym_table[sym_sec][sym_line].type = 1;//Ÿ�� ����(��밪)
					sym_table[sym_sec][sym_line++].addr = locctr[loc_num];//�ɺ��� �ּ�

					locctr[loc_num] += literal_table[n].length;
					imdata_line++;

				}

			}
			lit_num = 0;
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "BYTE") == 0){//���⿡ BYTE ����

			int input_type = 0;
			int i = 0;

			char buf[30];
			if (imdata_table[imdata_line]->operand[0][0] == 'C')
			{
				input_type = 1;//����Ÿ��
			}
			else if (imdata_table[imdata_line]->operand[0][0] == 'X')
			{
				input_type = 2;//����Ÿ��
			}
			else{//����
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
			//���⿡ WORD ����
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
			//���⿡ �ܺ����� ���� 
			imdata_table[imdata_line]->ctr = -1;
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "EXTREF") == 0){
			//���⿡ �ܺ����� ���� 
			imdata_table[imdata_line]->ctr = -1;
			imdata_line++;

			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "EQU") == 0){
			strcpy(sym_table[sym_sec][sym_line].symbol, imdata_table[imdata_line]->label);
			char te1[10], te2[10];
			int t = 0;
			int h = has_op(imdata_table[imdata_line]->operand[0]);
			if (h != 0){//+�� -�� ������ ������
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
					if ((search_symbloc(te1) > 0) && (search_symbloc(te2) > 0))//�� �ɺ� ��� ���̺� ������
					{

						if (h > 0){//�����ڰ� +
							if ((sym_table[sym_sec][search_symbloc(te1)].type == 2) && (sym_table[sym_sec][search_symbloc(te2)].type == 2))//�ΰ��� ���밪�� ��쿡�� 
							{
								sym_table[sym_sec][sym_line].type = 2;//���밪�̹Ƿ�
							}
							else{//�ƴ϶�� 
								sym_table[sym_sec][sym_line].type = 1;//��밪�̹Ƿ�

							}
							sym_table[sym_sec][sym_line].addr = sym_table[sym_sec][search_symbloc(te1)].addr + sym_table[sym_sec][search_symbloc(te2)].addr;
						}if (h < 0)//�����ڰ� -
						{
							if (sym_table[sym_sec][search_symbloc(te1)].type == sym_table[sym_sec][search_symbloc(te2)].type)//�ΰ��� Ÿ���� ������ 
							{
								sym_table[sym_sec][sym_line].type = 2;//���밪�̹Ƿ� 
							}
							else
							{
								sym_table[sym_sec][sym_line].type = 1;//��밪�̹Ƿ� 
							}
							sym_table[sym_sec][sym_line].addr = sym_table[sym_sec][search_symbloc(te1)].addr - sym_table[sym_sec][search_symbloc(te2)].addr;
						}
						imdata_table[imdata_line]->ctr = sym_table[sym_sec][sym_line].addr;
						sym_line++;
					}
					else{
						imdata_line++;
						return -1;//�ƴѰ�� ������ ��� 
					}
				}
			}
			else if (imdata_table[imdata_line]->operand[0][0] == '*')
			{
				sym_table[sym_sec][sym_line].type = 1;
				sym_table[sym_sec][sym_line].addr = imdata_table[imdata_line]->ctr;
				sym_line++;
			}
			//���⿡ EQU ���� 
			imdata_line++;
			return 0;
		}
		else if (strcmp(imdata_table[imdata_line]->oper, "LTORG") == 0){
			//���⿡ LTORG ���� 

			imdata_table[imdata_line]->ctr = -1;
			if (lit_num > 0){
				for (int n = 0; n < lit_num; n++)
				{
					imdata_table[imdata_line + 1] = (imdata*)calloc(sizeof(imdata), sizeof(imdata));
					imdata_table[imdata_line + 1]->label = (char*)malloc(3);
					strcpy(imdata_table[imdata_line + 1 + n]->label, "*\0");//���̺� *�Է�
					imdata_table[imdata_line + 1]->ctr = locctr[loc_num];//locctr�Է�
					imdata_table[imdata_line + 1]->oper = (char*)malloc(strlen(literal_table[n].name));
					strcpy(imdata_table[imdata_line + 1]->oper, literal_table[n].name);//�����ڿ� ���ͷ� �Է�

					strcpy(sym_table[sym_sec][sym_line].symbol, literal_table[n].name);//�ɺ� ���̺� �̸� ���
					sym_table[sym_sec][sym_line].type = 1;//Ÿ�� ����(��밪)
					sym_table[sym_sec][sym_line++].addr = locctr[loc_num];//�ɺ��� �ּ�

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
* ���� :�Է��� ���ڿ��� + �Ǵ� -�������� ��ġ�� ��뿩�θ� �˷��ִ� �Լ�
* �Ű� : ���ڿ�
* ��ȯ : ������ ���� = 0 , +�������� ��ġ>0 ,-�������� ��ġ <0
*
* -----------------------------------------------------------------------------------
*/
int has_op(char *str){
	int i = 0;
	while (str[i] != '\0'){
		if (str[i] == '+')
			return i; //+�������� ��ġ
		if (str[i] == '-')
			return -i;//-�������� ��ġ
		i++;
	}
	return 0;
}

/* ----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �н�1������ �����ϴ� �Լ��̴�.
*		   �н�1������..
*		   1. ���α׷� �ҽ��� ��ĵ�Ͽ� �ش��ϴ� ��ū������ �и��Ͽ� ���α׷� ���κ� ��ū
*		   ���̺��� �����Ѵ�.
*
* �Ű� : ����
* ��ȯ : ���� ���� = 0 , ���� = < 0
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
* ���� : ����� �ڵ带 ���� �ڵ�� �ٲٱ� ���� �н�2 ������ �����ϴ� �Լ��̴�.
*		   �н� 2������ ���α׷��� ����� �ٲٴ� �۾��� ���� ������ ����ȴ�.
*		   ������ ���� �۾��� ����Ǿ� ����.
*		   1. ������ �ش� ����� ��ɾ ����� �ٲٴ� �۾��� �����Ѵ�.
* �Ű� : ����
* ��ȯ : �������� = 0, �����߻� = < 0
* ���� :
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
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ object code (������Ʈ 1��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
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
	if ((file = fopen(file_name, "w")) == NULL)//����� �� ������ ���� 
	{
		printf("fopen err at output file\n");
	}
	else{//������ ������ ���� �߿����� 
		for (i = 0; i < imdata_line; i++)
		{
			if ((j = search_opcode(imdata_table[i]->oper)) != -1)//���� �����ڰ� instruction set�� �ִٸ� 
			{

				if (strcmp(inst_table[j]->format, "2") == 0){
					sprintf(buf, "%04X", imdata_table[i]->objectcode);// opcode�� 16�������� �Է��Ѵ�.
					record = 4;
				}
				if (strcmp(inst_table[j]->format, "1") == 0){
					sprintf(buf, "%02X", imdata_table[i]->objectcode);// opcode�� 16��������  �Է��Ѵ�.
					record = 2;
				}
				if ((imdata_table[i]->nixbpe & 1) == 1){
					sprintf(buf, "%08X", imdata_table[i]->objectcode);// opcode�� 16��������  �Է��Ѵ�.
					record = 8;
				}
				else if (strcmp(inst_table[j]->format, "3/4") == 0){
					sprintf(buf, "%06X", imdata_table[i]->objectcode);// opcode�� 16�������� �Է��Ѵ�.
					record = 6;
				}
			}
			else if ((strcmp(imdata_table[i]->oper, "START") == 0) || (strcmp(imdata_table[i]->oper, "CSECT") == 0))//���ο� H ���ڵ尡 ������ġ 
			{
				if (strcmp(imdata_table[i]->oper, "CSECT") == 0)//ù ���ڵ� ����� �ƴϸ� 
				{
					sym_sec++;
					loc_num++;
					adr = 0;//�����ּ� �ʱ�ȭ 
				}
				int r = 0;
				fprintf(file, "H%s", imdata_table[i]->label);
				for (r = strlen(imdata_table[i]->label); r < 6; r++)
					fprintf(file, " ");
				fprintf(file, "%012X\n", locctr[loc_num]);//���ڵ带 ��� 
				continue;

			}
			else if (strcmp(imdata_table[i]->oper, "EXTDEF") == 0)//�ܺ����� ���ڵ尡 ������ ����Ѵ�
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
			else if (strcmp(imdata_table[i]->oper, "EXTREF") == 0)//�ܺ� ���� ���ڵ尡 ������ ����Ѵ�.
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

				if (imdata_table[i]->operand[0][0] == 'C')//Ÿ�� ���� 
					record = (k - 2) * 2;
				else if (imdata_table[i]->operand[0][0] == 'X')
					record = (k - 2);

				switch (record)//�ʵ��� ���� �ٸ��� ������ �����ؼ� ������ش�
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
			else if (strcmp(imdata_table[i]->label, "*") == 0)//���� ���ͷ��� �´ٸ� 
			{
				int k = 3;
				while (imdata_table[i]->oper[k] != '\'')
				{
					k++;
				}

				if (imdata_table[i]->oper[1] == 'C')//���ͷ��� Ÿ�� ���� 
					record = (k - 3) * 2;
				else if (imdata_table[i]->oper[1] == 'X')
					record = (k - 3);

				switch (record)//���ͷ� ���� �ʵ��� ���� �ٸ��� ������ �����ؼ� ������ش�
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
			else{//object code ��¿� ���þ��� ��ɾ� �����̶�� ���� 
				strcpy(buf, "");
				record = 0;
			}

			if ((sum + record) > 59)//10~69�� �Ѵ°�� ���� T���ڵ�� �Ѿ 
			{
				fprintf(file, "T%06X%02X", adr, sum / 2);
				fprintf(file, "%s\n", temp);//temp�� �����صξ��� ���ڵ� ��� 
				sum = record;
				strcpy(temp, buf);
				adr = imdata_table[i]->ctr;
				continue;
			}
			else if (i == (imdata_line - 1))//������ ���̰ų� 
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
					fprintf(file, "M%06X", ref_table[sym_sec][v].add);//M ���ڵ� ��� 
					if (ref_table[sym_sec][v].size == 5)//
						fprintf(file, "05");
					else
						fprintf(file, "06");
					if (ref_table[sym_sec][v].op == 0)//������ ������ �ľ��Ѵ�
						fprintf(file, "+");//���� �ּҰ���ŭ ���ؾ��Ѵٸ�
					else
						fprintf(file, "-");//�����ϴ°����
					fprintf(file, "%s\n", ref_table[sym_sec][v].name);//��������ġ�� �ּ�
				}
				fprintf(file, "E000000");
				continue;
			}
			else if ((strcmp(imdata_table[i + 1]->oper, "LTORG") == 0) || (strcmp(imdata_table[i + 1]->oper, "CSECT") == 0)){//���� �ٲ���ϴ°�� 
				sum += record;
				strcat(temp, buf);
				fprintf(file, "T%06X%02X", adr, sum / 2);
				fprintf(file, "%s\n", temp);//temp�� �����صξ��� ���ڵ� ��� 
				strcpy(buf, "");
				record = 0;
				strcpy(temp, "");
				sum = 0;
				adr = imdata_table[i + 2]->ctr;
				if ((strcmp(imdata_table[i + 1]->oper, "CSECT") == 0))//���� �������� ���°���� 
				{
					for (int v = 0; v < refmany[sym_sec]; v++){//M ���ڵ� ���
						fprintf(file, "M%06X", ref_table[sym_sec][v].add);
						if (ref_table[sym_sec][v].size == 5)
							fprintf(file, "05");
						else
							fprintf(file, "06");
						if (ref_table[sym_sec][v].op == 0)//������ ������ �ľ��Ѵ�
							fprintf(file, "+");//���� �ּҰ���ŭ ���ؾ��Ѵٸ� 
						else
							fprintf(file, "-");//�����ϴ°���� 
						fprintf(file, "%s\n", ref_table[sym_sec][v].name);//��������ġ�� �ּ�
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
