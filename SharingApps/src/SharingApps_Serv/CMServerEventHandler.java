
package SharingApps_Serv;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEventField;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEvent;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventCONNECT;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventDISCONNECT;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBCOMP;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBLISH;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREC;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREL;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventSUBSCRIBE;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventUNSUBSCRIBE;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;
import SA_Info.CP_Info;

// CM �� ���� ��Ģ�� ���� �̺�Ʈ �ڵ鷯 ���� -> �������̽� ������ ���ؼ� ��Ģ�� ����
public class CMServerEventHandler implements CMAppEventHandler {
	private CMServerStub m_serverStub;
	private int m_nCheckCount;	// for internal forwarding simulation
	private boolean m_bDistFileProc;	// for distributed file processing
	private ArrayList<CP_Info> programs = new ArrayList<CP_Info>();
	private int nums_of_programs =0;
	
	public CMServerEventHandler(CMServerStub serverStub)
	{
		m_serverStub = serverStub;
		m_nCheckCount = 0;
		m_bDistFileProc = false;
	}
	public void sendmsg(String strInput, String user) // strInput�� ������ �Է��� ���α׷� �̸�
	{
		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		      
		CMDummyEvent me = new CMDummyEvent();
		me.setHandlerSession(myself.getCurrentSession());
		me.setHandlerGroup(myself.getCurrentGroup());
		me.setDummyInfo(strInput);
		m_serverStub.send(me, user);
		me = null;
	}
	public CP_Info findProg(String progName) {   //�̸����� ���� ã�� ���� �޼ҵ�
	    int i = 0;
	    while(i < nums_of_programs) {
	        if(progName.equals(programs.get(i).toString()))
	           break;
	        i++;
	       }
	    if(i == nums_of_programs) {
	       return null;
	    }
	    return programs.get(i);
	}
	
	
	public void processEvent(CMEvent cme) { // �߻��� CMEvent�� �Ѱ��ش�.
		// TODO Auto-generated method stub
		switch(cme.getType()) // �߻��� �̺�Ʈ�� Ÿ���� �˾Ƴ���.
		// CMInfo���� �̺�Ʈ Ÿ���� �� �� �ִ�.
		{
		case CMInfo.CM_DUMMY_EVENT:
			processDummyEvent(cme);
			break;
		case CMInfo.CM_SESSION_EVENT:
			processSessionEvent(cme);
			break;
		default:
			return;
		}
	}
	
	private void processDummyEvent(CMEvent cme)
	   {
	      CMDummyEvent due = (CMDummyEvent) cme;
	      String str = due.getDummyInfo();
	      String eventcheck = null;
	      eventcheck = str.substring(0, 3);

		System.out.println("���Ź��� �̺�Ʈ : "+eventcheck);
		
		if(eventcheck != null) { // SharingApps protocol�� �̿�
			switch(eventcheck) {
				case "401" : //�� ���� ��û -> �� ����
					storeAppInfo(due.getSender(), str.substring(3));
					break;
				case "402" : //�� ���� ��û -> �ʿ� ���� ��û
					startApp(due, str.substring(3));
					break;
				case "403" : //������� �� ����� �޾Ƽ� ��û�ڿ��� ����
					sendResult(str.substring(3));
					break;
				case "404" : //�� ����Ʈ ��û -> �� ����Ʈ �޽��� ���� 
					sendApplist(due.getSender());
					break;
				case "405": // ���α׷� ���� ��û�� �޾Ƽ� ���� �Է��� ��û��
		            requestArgs(due, str.substring(3));
		            break;
				default :
					System.out.println("�ش� ���α׷��� ��Ͽ� �����ϴ�.");
					break;
			}
		}
		return;
	}
	public void storeAppInfo(String user_ID, String str) { 
		StringTokenizer strtoken = new StringTokenizer(str, "#");
	    String[] Infos = new String[5];
        
		Infos[0] = user_ID;  //user_id
        Infos[1] = strtoken.nextToken();  //prog_name
        Infos[2] = strtoken.nextToken();  //prog_info
        Infos[3] = strtoken.nextToken();  // arg_num
        Infos[4] = strtoken.nextToken();  //arg_info
        programs.add(new CP_Info(Infos[0], Infos[1], Infos[2], Integer.parseInt(Infos[3]), Infos[4]));
        this.nums_of_programs++;
	}
	public void sendApplist(String recvUser) {
		int nums = programs.size();
		int i;
		
		for(i=0; i<nums; i++)
			sendmsg("501"+Character.forDigit(i+1, 10)+". "+programs.get(i).prog_name+" : "+programs.get(i).prog_info, recvUser);
	}
	public void startApp(CMDummyEvent due, String name_args) {
	      StringTokenizer strtoken = new StringTokenizer(name_args, "#");
	      String progName = strtoken.nextToken();
	      String args = strtoken.nextToken();
	      CP_Info prog = findProg(progName);
	      sendmsg("502" + progName + "#" + args + "#" +due.getSender(), prog.user_id);
	}
	public void sendResult(String result_user) {
	      StringTokenizer strtoken = new StringTokenizer(result_user, "#");
	      String result = strtoken.nextToken();
	      String user = strtoken.nextToken();
	      sendmsg("503"+ result, user);
	}
	public void requestArgs(CMDummyEvent due, String progName) {
		System.out.println("1");
	    CP_Info prog = findProg(progName);
	    System.out.println("2");
	    if( prog == null) {
	       sendmsg("�ش� ���α׷��� �����ϴ�.\n", due.getSender());
	       return;
	    }
	    sendmsg("505"+prog.arg_info, due.getSender());
	}

	private void processSessionEvent(CMEvent cme)
	{	
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		CMSessionEvent se = (CMSessionEvent) cme; // CMSessionEvent�� �޾����Ƿ� Ÿ�� ĳ����
		switch(se.getID()) // event ��Ű���� CMSessionEvent���� Event Id�� Ȯ�� ����
		{
		case CMSessionEvent.LOGIN: // ID�� �α����̶�� 
			System.out.println("["+se.getUserName()+"] requests login.");
			break;
		case CMSessionEvent.LOGOUT:
			System.out.println("["+se.getUserName()+"] logs out.");
			deleteApps(se);
			break;
		default :
			return;
		}
	}
	private void deleteApps(CMSessionEvent se) {
		int i;
		int num = nums_of_programs;
		String user_id = se.getSender();
		String target = null;
		for(i=0; i < num; i++) {
			target = programs.get(i).user_id;
			if(target.equals(user_id)) {
				programs.remove(i);
				nums_of_programs--;
				num = nums_of_programs;
				i = 0;
			}
		}
	}
	
}
