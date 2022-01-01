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

//public class SharingApps_CliEventHandler implements CMAppEventHandler {
public abstract class SharingApps_CliEventHandler implements CMAppEventHandler {
	private CMClientStub m_clientStub;
	public String my_program_name = "default";
	
	public SharingApps_CliEventHandler(CMClientStub stub)
	{
		m_clientStub = stub;
	}
	
	@Override
	public void processEvent(CMEvent cme) {
		// TODO Auto-generated method stub
		switch(cme.getType())
		{
			case CMInfo.CM_DUMMY_EVENT:
				processDummyEvent(cme);
				break;
			default:
				return;
		}
		
	}
	
	public void sendmsg(String strInput) // strInput은 유저가 입력한 프로그램 이름
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		
		CMDummyEvent me = new CMDummyEvent();
		me.setHandlerSession(myself.getCurrentSession());
		me.setHandlerGroup(myself.getCurrentGroup());
		me.setDummyInfo(strInput);
		m_clientStub.send(me, "SERVER");
		me = null;
	}
	
	public abstract String my_program(String msg);
	
	private void processDummyEvent(CMEvent cme) // 통신 방법.
	{
		CMDummyEvent due = (CMDummyEvent) cme;
		String str = due.getDummyInfo();
		String eventcheck = null;
		
		if(str.length() >= 3) {
			eventcheck = str.substring(0, 3);
		}
		System.out.println(eventcheck);
		if(eventcheck != null) { // SharingApps protocol로 이용
			switch(eventcheck) {
				case "501":
					showPrograms(str.substring(3));
					break;
				case "505" : //프로그램 인자 요청 -> 인자 입력
					inputArgs(str.substring(3));
					break;
				case "502" : //프로그램 실행 요청
					runApp(due, str.substring(3));
					break;
				case "503" : //서버가 전달해준 결과 출력
					printResult(str.substring(3));
					break;
				default :
					System.out.println("해당 프로그램이 목록에 없습니다.");
					break;
			}
		} 
		return;
	}
	private void showPrograms(String msg) {
		System.out.println(msg);
	}
	private void inputArgs(String args_info) {
		System.out.println("다음의 예시와 같이 입력하시오.");
		System.out.println(args_info);
	}
	private void runApp(CMDummyEvent due, String name_args_user) {
	      StringTokenizer strtoken = new StringTokenizer(name_args_user, "#");
	      String progName = strtoken.nextToken();
	      String args = strtoken.nextToken();
	      String user = strtoken.nextToken();
	      String result = null;
	      if(progName.equals(my_program_name)) {
	            result = my_program(args);
	            sendmsg("403" + result + "#" + user);
	      }
	      else {
	    	  System.out.println("프로그램 실행 오류");
	      }
	   }
	private void printResult(String result) {
	      System.out.println("result: " + result);
	   }
}