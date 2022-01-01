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

public class SharingApps_CliEventHandler_ex2 extends SharingApps_CliEventHandler {
	private CMClientStub m_clientStub;
	public SharingApps_CliEventHandler_ex2(CMClientStub stub)
	{
		super(stub);
		this.my_program_name = "facto"; // 프로그램 이름 등록
	}
	
	public String my_program(String msg) { //팩토리얼 계산기
		int num=0, result=1;
		String re_msg = "";
		StringTokenizer strtoken_bf = new StringTokenizer(msg);

		while(strtoken_bf.hasMoreTokens()) {
			
			re_msg = re_msg+strtoken_bf.nextToken();			
		}
		
		StringTokenizer strtoken = new StringTokenizer(re_msg, ","); // 숫자 양식이어야 함.
		
		num = Integer.parseInt(strtoken.nextToken());
		
		for(int i = num; i>=1; i--) {
			result = result * i;
		}
		
		return Integer.toString(result);  
	}
}