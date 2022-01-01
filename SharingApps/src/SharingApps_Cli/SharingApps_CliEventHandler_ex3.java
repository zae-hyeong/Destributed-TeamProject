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

public class SharingApps_CliEventHandler_ex3 extends SharingApps_CliEventHandler{
	private CMClientStub m_clientStub;	
	
	public SharingApps_CliEventHandler_ex3(CMClientStub stub)
	{
		super(stub);
		this.my_program_name = "MtoN_Plus";
	}
	// client가 가진 프로그램 작성.
	public String my_program(String msg) { //m~n까지의 합 계산기
		int m=0, n=0, result=0;
		String re_msg = "";
		StringTokenizer strtoken_bf = new StringTokenizer(msg);

		while(strtoken_bf.hasMoreTokens()) {
			
			re_msg = re_msg+strtoken_bf.nextToken();			
		}

		StringTokenizer strtoken = new StringTokenizer(re_msg, ",");
		
		m = Integer.parseInt(strtoken.nextToken());
		n = Integer.parseInt(strtoken.nextToken());
		
		for(int i = m; i<=n; i++) {
			result = result + i;
		}
		
		return Integer.toString(result);
	}
	// 클라이언트가 가진 프로그램 끝.
}