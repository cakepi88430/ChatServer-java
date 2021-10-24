package Chat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class server_command {
	public static void notice(String message){
		String str = "[系統公告] " + message;
		Create_Server.AllClineThread.sendAllMessage_server(str);
		messagePan.append_mesagearea(str);
	}
	public static void shutdown(int time){
		String str = "[系統公告] 伺服器即將在" + time + "秒關閉。";
		Create_Server.AllClineThread.sendAllMessage_server(str);
		messagePan.append_mesagearea(str);
		new countdown(time);
		
	}
	public static void shutdown_immediate(){
		Server.frm.createpan.create_btn.setEnabled(true);
		Server.frm.createpan.port_text.setEnabled(true);
		Server.frm.createpan.close_btn.setEnabled(false);
		Server.CServer.close();
		String str = "伺服器已關閉。";
		messagePan.append_serverarea(str);
	}
}
class countdown extends Thread {
	private int time=0;
	countdown(int time){
		this.time = time;
		start();
	}
	public void run() { 
        for(int i=0;i<time;i++){
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        server_command.shutdown_immediate();
	}
} 

