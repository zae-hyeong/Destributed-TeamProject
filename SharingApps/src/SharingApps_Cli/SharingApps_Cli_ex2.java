package SharingApps_Cli;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;


import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;

import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;


public class SharingApps_Cli_ex2 {
	private	CMClientStub m_clientStub;
	private SharingApps_CliEventHandler m_eventHandler;
	private boolean m_bRun;
	private Scanner m_scan = null;
	
	public SharingApps_Cli_ex2()
	{
		m_clientStub = new CMClientStub();
		m_eventHandler = new SharingApps_CliEventHandler_ex2(m_clientStub);
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
	    strNewServerAddress = "172.30.1.42";
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
	public void enrollApps(String program_send)  // �� ��� ����. ( 1. ��� )
	{	
		System.out.println("������ App�� ������ ������Դϴ�...");
		sendmsg("401"+ program_send); // ��űԾ�: �����  401�� ����.
		
	}
	public void showProgReq() {
		sendmsg("404"); // ���α׷� ��� �����ֱ� ��������. 404
	}
	public void reqProg(String prog_name) {  // prog_name�� ���α׷� �̸�
		sendmsg("405"+prog_name);
	}

	// Ŭ���̾�Ʈ�� ���α׷� ����
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
				System.out.println("=====������ ������ ��ȣ ����=====");
				System.out.println("1. ���α׷� ��� ���");
				System.out.println("2. ���α׷� ����");
				System.out.println("3. Ŭ���̾�Ʈ �� ����");
				strInput = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			switch(strInput) {
			case "1" : 
				System.out.println("���α׷� ����� ����մϴ�.");
				showProgReq();
				break;
			case "2" : 
				System.out.println("����� ���α׷��� �̸��� �Է��� �ּ���>>");
				prognum = sc.nextLine();
				reqProg(prognum);
				argsInput = sc.nextLine();
			    sendmsg("402" + prognum + "#" + argsInput);
				break;
			case "3" : 
				System.out.println("Ŭ���̾�Ʈ ���񽺸� �����մϴ�. �����մϴ�.");
				testLogoutDS();
				m_bRun = false;
				break;
			default : 
				System.out.println("�߸��� �Է��Դϴ�. �ٽ� �Է����ּ���");
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
		SharingApps_Cli_ex2 client = new SharingApps_Cli_ex2();
		CMClientStub cmStub = client.getClientStub();
		cmStub.setAppEventHandler(client.getClientEventHandler());
		
		client.StartCM(); // ���� ����
		client.testLoginDS(); // �α���
		client.enrollApps("facto#App for factorial#1# a \nExpected result: a!(a is int num.)");
		System.out.println("Client App Starts");
		
		client.choose(); // �ݺ��� ���ư�
			
		System.out.println("Client ����.");

	}
}