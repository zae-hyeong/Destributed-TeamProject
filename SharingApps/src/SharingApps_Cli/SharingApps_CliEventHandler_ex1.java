package SharingApps_Cli;

import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import SA_Info.CP_Info;

public class SharingApps_CliEventHandler_ex1 extends SharingApps_CliEventHandler{
	public SharingApps_CliEventHandler_ex1(CMClientStub stub)
	{
		super(stub);
		this.my_program_name ="calc"; // ���α׷� �̸� ���
	}
	
	public String my_program(String msg) // program 1  int �� ���� �ΰ��� ��Ģ���� ����.
	{
		int num1, num2, result = 0;
	    String oper;
	    String re_msg = "";
	    StringTokenizer strtoken_bf = new StringTokenizer(msg);

	    while(strtoken_bf.hasMoreTokens()) {
	    	re_msg = re_msg+strtoken_bf.nextToken();         
	    }
	      
	    StringTokenizer strtoken = new StringTokenizer(re_msg, "+|-|/|*", true); // ���� ������ ���� ����̾�� ��.
	      
	    num1 = Integer.parseInt(strtoken.nextToken());
	    oper = strtoken.nextToken();
	    num2 = Integer.parseInt(strtoken.nextToken());
	      
	    switch(oper) {
	    case "+":
	       result = num1+num2;
	       break;
	    case "-":
	       result = num1-num2;
	       break;
	    case "*":
	       result = num1*num2;
	       break;
	    case "/":
	       result = num1/num2;
	       break;
	    default:
	       System.out.println("���ڿ� ����");
	    }
	      
	    return Integer.toString(result);
	 }
	

}