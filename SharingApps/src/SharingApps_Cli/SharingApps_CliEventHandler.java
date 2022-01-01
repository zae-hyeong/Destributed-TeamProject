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
	
	public void sendmsg(String strInput) // strInput�� ������ �Է��� ���α׷� �̸�
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
	
	private void processDummyEvent(CMEvent cme) // ��� ���.
	{
		CMDummyEvent due = (CMDummyEvent) cme;
		String str = due.getDummyInfo();
		String eventcheck = null;
		
		if(str.length() >= 3) {
			eventcheck = str.substring(0, 3);
		}
		System.out.println(eventcheck);
		if(eventcheck != null) { // SharingApps protocol�� �̿�
			switch(eventcheck) {
				case "501":
					showPrograms(str.substring(3));
					break;
				case "505" : //���α׷� ���� ��û -> ���� �Է�
					inputArgs(str.substring(3));
					break;
				case "502" : //���α׷� ���� ��û
					runApp(due, str.substring(3));
					break;
				case "503" : //������ �������� ��� ���
					printResult(str.substring(3));
					break;
				default :
					System.out.println("�ش� ���α׷��� ��Ͽ� �����ϴ�.");
					break;
			}
		} 
		return;
	}
	private void showPrograms(String msg) {
		System.out.println(msg);
	}
	private void inputArgs(String args_info) {
		System.out.println("������ ���ÿ� ���� �Է��Ͻÿ�.");
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
	    	  System.out.println("���α׷� ���� ����");
	      }
	   }
	private void printResult(String result) {
	      System.out.println("result: " + result);
	   }
}