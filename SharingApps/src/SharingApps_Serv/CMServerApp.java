package SharingApps_Serv;
import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSUserAccessSimulator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.JOptionPane;

public class CMServerApp { // 내가 만든 서버 App의 이름이 CMServerApp임
   private CMServerStub m_serverStub; 
   private CMServerEventHandler m_eventHandler;
   private boolean m_bRun;
   private CMSNSUserAccessSimulator m_uaSim;
   private Scanner m_scan = null;
   
   public CMServerApp()
   {
      m_serverStub = new CMServerStub(); // 스텁 객체 생성
      m_eventHandler = new CMServerEventHandler(m_serverStub); // 이벤트 핸들러 생성 
      m_bRun = true;
      m_uaSim = new CMSNSUserAccessSimulator();
   }
   
   public CMServerStub getServerStub()
   {
      return m_serverStub;
   }
   
   public CMServerEventHandler getServerEventHandler()
   {
      return m_eventHandler;
   }
   
   ///////////////////////////////////////////////////////////////
   // test methods
   public void startTest()
   {
      System.out.println("Server application starts.");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      m_scan = new Scanner(System.in);
      String strInput = null;
      int nCommand = -1;
      while(m_bRun)
      {
         System.out.println("Type \"0\" for menu.");
         System.out.print("> ");
         try {
            strInput = br.readLine();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            continue;
         }
         
         try {
            nCommand = Integer.parseInt(strInput);
         } catch (NumberFormatException e) {
            System.out.println("Incorrect command number!");
            continue;
         }
         
         switch(nCommand)
         {
         case 0:
            printAllMenus();
            break;
         case 100:
            startCM();
            break;
         case 999:
            terminateCM();
            return;
         case 1: // print session information
            printSessionInfo();
            break;
         case 2: // print selected group information
            printGroupInfo();
            break;
         default:
            System.err.println("Unknown command.");
            break;
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
   
   public void printAllMenus()
   {
      System.out.print("---------------------------------- Help\n");
      System.out.print("0: show all menus\n");
      System.out.print("---------------------------------- Start/Stop\n");
      System.out.print("100: strat CM, 999: terminate CM\n");
      System.out.print("---------------------------------- Information\n");
      System.out.print("1: show session information, 2: show group information\n");
   }
   
   public void startCM()
   {
      // get current server info from the server configuration file
      String strSavedServerAddress = null;
      String strCurServerAddress = null;
      int nSavedServerPort = -1;
      String strNewServerAddress = null;
      String strNewServerPort = null;
      int nNewServerPort = -1;
      
      strSavedServerAddress = m_serverStub.getServerAddress();
      strCurServerAddress = CMCommManager.getLocalIP();
      nSavedServerPort = m_serverStub.getServerPort();
      
      // ask the user if he/she would like to change the server info
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("========== start CM");
      System.out.println("detected server address: "+strCurServerAddress);
      System.out.println("saved server port: "+nSavedServerPort);
      
      try {
         System.out.print("new server address (enter for detected value): ");
         strNewServerAddress = br.readLine().trim();
         if(strNewServerAddress.isEmpty()) strNewServerAddress = strCurServerAddress;

         System.out.print("new server port (enter for saved value): ");
         strNewServerPort = br.readLine().trim();
         try {
            if(strNewServerPort.isEmpty()) 
               nNewServerPort = nSavedServerPort;
            else
               nNewServerPort = Integer.parseInt(strNewServerPort);            
         } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
         }
         
         // update the server info if the user would like to do
         if(!strNewServerAddress.equals(strSavedServerAddress))
            m_serverStub.setServerAddress(strNewServerAddress);
         if(nNewServerPort != nSavedServerPort)
            m_serverStub.setServerPort(Integer.parseInt(strNewServerPort));
         
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      
      boolean bRet = m_serverStub.startCM();
      if(!bRet)
      {
         System.err.println("CM initialization error!");
         return;
      }
      startTest();
   }
   
   public void terminateCM()
   {
      m_serverStub.terminateCM();
      m_bRun = false;
   }
   
   public void printSessionInfo()
   {
      System.out.println("------------------------------------------------------");
      System.out.format("%-20s%-20s%-10s%-10s%n", "session name", "session addr", "port", "#users");
      System.out.println("------------------------------------------------------");
      
      CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
      Iterator<CMSession> iter = interInfo.getSessionList().iterator();
      while(iter.hasNext())
      {
         CMSession session = iter.next();
         System.out.format("%-20s%-20s%-10d%-10d%n", session.getSessionName(), session.getAddress()
               , session.getPort(), session.getSessionUsers().getMemberNum());
      }
      return;
   }
   
   public void printGroupInfo()
   {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String strSessionName = null;
      
      System.out.println("====== print group information");
      System.out.print("Session name: ");
      try {
         strSessionName = br.readLine();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
      CMSession session = interInfo.findSession(strSessionName);
      if(session == null)
      {
         System.out.println("Session("+strSessionName+") not found.");
         return;
      }
      
      System.out.println("------------------------------------------------------------------");
      System.out.format("%-20s%-20s%-10s%-10s%n", "group name", "multicast addr", "port", "#users");
      System.out.println("------------------------------------------------------------------");

      Iterator<CMGroup> iter = session.getGroupList().iterator();
      while(iter.hasNext())
      {
         CMGroup gInfo = iter.next();
         System.out.format("%-20s%-20s%-10d%-10d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
               , gInfo.getGroupPort(), gInfo.getGroupUsers().getMemberNum());
      }

      System.out.println("======");
      return;
   }
   
   
   public static void main(String[] args) {
      // TODO Auto-generated method stub
      CMServerApp server = new CMServerApp();
      CMServerStub cmStub = server.getServerStub(); // 만든 CM stub 객체를 가져온다
      // 서버의 이벤트 핸들러를 CM에 등록하여 CM이 자신이 수신한 이벤트를 처리하고난 뒤 이벤트를 다시 어디로 전달할지 알게된다.
      // 이벤트 핸들러르 정의해야한다. -> CMServerEventHandler.java
      cmStub.setAppEventHandler(server.getServerEventHandler());  
      server.startCM(); 
      // CM을 시작한다. => CM의 초기화가 끝났고 필요로 하는 스레드를 띄어서 내 App 하부에서 독립적으로 실행된다. 

      System.out.println("Server application is terminated.");
   }

}