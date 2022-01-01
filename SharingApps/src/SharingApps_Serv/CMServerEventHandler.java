
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

// CM 이 정한 규칙에 따라서 이벤트 핸들러 정의 -> 인터페이스 구현을 통해서 규칙을 따름
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
	public void sendmsg(String strInput, String user) // strInput은 유저가 입력한 프로그램 이름
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
	public CP_Info findProg(String progName) {   //이름으로 앱을 찾는 보조 메소드
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
	
	
	public void processEvent(CMEvent cme) { // 발생한 CMEvent를 넘겨준다.
		// TODO Auto-generated method stub
		switch(cme.getType()) // 발생한 이벤트의 타입을 알아낸다.
		// CMInfo에서 이벤트 타입을 알 수 있다.
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

		System.out.println("수신받은 이벤트 : "+eventcheck);
		
		if(eventcheck != null) { // SharingApps protocol로 이용
			switch(eventcheck) {
				case "401" : //앱 저장 요청 -> 앱 저장
					storeAppInfo(due.getSender(), str.substring(3));
					break;
				case "402" : //앱 실행 요청 -> 필요 인자 요청
					startApp(due, str.substring(3));
					break;
				case "403" : //사용자의 앱 결과를 받아서 요청자에게 전달
					sendResult(str.substring(3));
					break;
				case "404" : //앱 리스트 요청 -> 앱 리스트 메시지 전달 
					sendApplist(due.getSender());
					break;
				case "405": // 프로그램 실행 요청을 받아서 인자 입력을 요청함
		            requestArgs(due, str.substring(3));
		            break;
				default :
					System.out.println("해당 프로그램이 목록에 없습니다.");
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
	       sendmsg("해당 프로그램이 없습니다.\n", due.getSender());
	       return;
	    }
	    sendmsg("505"+prog.arg_info, due.getSender());
	}

	private void processSessionEvent(CMEvent cme)
	{	
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		CMSessionEvent se = (CMSessionEvent) cme; // CMSessionEvent를 받았으므로 타입 캐스팅
		switch(se.getID()) // event 패키지의 CMSessionEvent에서 Event Id를 확인 가능
		{
		case CMSessionEvent.LOGIN: // ID가 로그인이라면 
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
