package SharingApps_Cli;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

import SA_Info.CP_Info;
import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMGroupInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;


public class SharingApps_Cli {
	private String program_for_send = "calc#App for calculation#1#a + b(a, b is num.)"; // 사용자 앱 등록을 위한 입력. 함수이름 #앱설명#인자수#인자설명
	private	CMClientStub m_clientStub;
	private SharingApps_CliEventHandler m_eventHandler;
	private boolean m_bRun;
	private Scanner m_scan = null;
	
	public SharingApps_Cli()
	{
		m_clientStub = new CMClientStub();
		m_eventHandler = new SharingApps_CliEventHandler_ex1(m_clientStub);
		m_bRun = true;
	}
	
	public CMClientStub getClientStub()
	{
		return m_clientStub;
	}
	public SharingApps_CliEventHandler getClientEventHandler()
	{
		return m_eventHandler;
	}
	   public void StartCM()
	   {
	      // get current server info from the server configuration file
	      String strCurServerAddress = null;
	      int nCurServerPort = -1;
	      String strNewServerAddress = null;
	      String strNewServerPort = null;
	      
	      strCurServerAddress = m_clientStub.getServerAddress();
	      nCurServerPort = m_clientStub.getServerPort();
	      
	      // ask the user if he/she would like to change the server info
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	      System.out.println("========== start CM");
	      System.out.println("current server address: "+strCurServerAddress);
	      System.out.println("current server port: "+nCurServerPort);
	      
	      System.out.print("new server address (enter for current value): ");
	      //strNewServerAddress = br.readLine().trim();
	      strNewServerAddress = "172.30.1.20";
	      System.out.print("new server port (enter for current value): ");
	      //strNewServerPort = br.readLine().trim();
	      strNewServerPort = "7777";
	      // update the server info if the user would like to do
	      if(!strNewServerAddress.isEmpty() && !strNewServerAddress.equals(strCurServerAddress))
	         m_clientStub.setServerAddress(strNewServerAddress);
	      if(!strNewServerPort.isEmpty() && Integer.parseInt(strNewServerPort) != nCurServerPort)
	         m_clientStub.setServerPort(Integer.parseInt(strNewServerPort));

	      boolean bRet = m_clientStub.startCM();
	      if(!bRet)
	      {
	         System.err.println("CM initialization error!");
	         return;
	      } 
	   }


	public void testLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		boolean bRequestResult = false;
		Console console = System.console();
		if(console == null)
		System.out.println("====== login to default server");
		System.out.print("user name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strUserName = br.readLine();
			if(console == null)
			{
				System.out.print("password: ");
				strPassword = br.readLine();
			}
			else
				strPassword = new String(console.readPassword("password: "));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
		if(bRequestResult)
			System.out.println("successfully sent the login request.");
		else
			System.err.println("failed the login request!");
		System.out.println("======");
	}
	public void testLogoutDS()
	{
		boolean bRequestResult = false;
		System.out.println("====== logout from default server");
		bRequestResult = m_clientStub.logoutCM();
		if(bRequestResult)
			System.out.println("successfully sent the logout request.");
		else
			System.err.println("failed the logout request!");
		System.out.println("======");
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
	public void enrollApps(String program_send)  // 앱 등록 과정. ( 1. 등록 )
	{
		String program_for_send2 = "facto#App for factorial#1# a!(a is int num.)"; // 사용자 앱 등록을 위한 입력. 함수이름 #앱설명#인자수#인자설명
		String program_for_send3 = "MtoN_Plus#App for MtoN Plus#2# m+(m+1)+(m+2)+...+n (m, n is num.)"; // 사용자 앱 등록을 위한 입력. 함수이름 #앱설명#인자수#인자설명		System.out.println("유저의 App을 서버에 등록중입니다...")
		sendmsg("401"+ program_send); // 통신규약: 등록은  401로 정함.
		sendmsg("401"+ program_for_send2); // 통신규약: 등록은  401로 정함.
		sendmsg("401"+ program_for_send3); // 통신규약: 등록은  401로 정함.
		
		/* 이건 program 배열이였을때 경우. 지금은 프로그램 1개로 가정~
		for( String str : program_array) {
			sendmsg("401#"+str); // 통신규약: 등록은  401로 정함.
		}*/
		
	}
	public void showProgReq() {
		sendmsg("404"); // 프로그램 목록 보여주기 프로토콜. 404
	}
	public void reqProg(String prog_name) {  // prog_name은 프로그램 이름
		sendmsg("405"+prog_name);
	}

	// 클라이언트가 프로그램 선택
	public void choose() 
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		m_scan = new Scanner(System.in);
		Scanner sc = new Scanner(System.in);
		String strInput = null;
		String argsInput = null;
		String prognum = null;
		boolean m_bRun = true;
		
		while(m_bRun) {
			try {
				System.out.println("=====실행할 서비스의 번호 선택=====");
				System.out.println("1. 프로그램 목록 출력");
				System.out.println("2. 프로그램 실행");
				System.out.println("3. 클라이언트 앱 종료");
				strInput = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			switch(strInput) {
			case "1" : 
				System.out.println("프로그램 목록을 출력합니다.");
				showProgReq();
				break;
			case "2" : 
				System.out.println("사용할 프로그램의 이름을 입력해 주세요>>");
				prognum = sc.nextLine();
				reqProg(prognum);
				argsInput = sc.nextLine();
			    sendmsg("402" + prognum + "#" + argsInput);
				break;
			case "3" : 
				System.out.println("클라이언트 서비스를 종료합니다. 감사합니다.");
				testLogoutDS();
				m_bRun = false;
				break;
			default : 
				System.out.println("잘못된 입력입니다. 다시 입력해주세요");
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_scan.close();
	}

	public static void main(String[] args)
	{
		SharingApps_Cli client = new SharingApps_Cli();
		CMClientStub cmStub = client.getClientStub();
		cmStub.setAppEventHandler(client.getClientEventHandler());
		
		client.StartCM(); // 서버 연결
		client.testLoginDS(); // 로그인
		client.enrollApps("calc#App for calculation#1#a + b(a, b is num.)");  // 앱 등록  401 
		
		System.out.println("Client App Starts");
		
		client.choose(); // 반복문 돌아감
			
		System.out.println("Client 종료.");

	}
}