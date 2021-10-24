package Chat;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.net.*;
import game.*;

public class Server {
	static main_frm frm = new main_frm();
	static database db = new database();
	static Create_Server CServer;
	public static void main(String[] args) {
		Server server = new Server();
		
		System.out.println(System.getProperty("path", ""));
	}

}

class game_severPan extends JPanel{
	static JButton create_btn = new JButton("開啟伺服器");
	static JButton close_btn = new JButton("關閉伺服器");
	static JLabel port_lab = new JLabel("(預設為7778)port:");
	static JTextField port_text = new JTextField("7778");
	private GameServer gameserver;
	game_severPan(){
		setBorder(BorderFactory.createTitledBorder("遊戲伺服器操作"));
		setLayout(new FlowLayout());
		setBounds(5, 450, 480, 80);
		close_btn.setEnabled(false);
		create_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int port=-1;
				try {
					port = Integer.parseInt(port_text.getText());
				} catch(Exception e1){
					JOptionPane.showMessageDialog(null, port_text.getText() +"是一個不合法的port");
				}
				if(port > 65535)
					JOptionPane.showMessageDialog(null, port_text.getText() +"是一個不合法的port");
				
				if(port > 0 && port <= 65535){
					
					create_btn.setEnabled(false);
					port_text.setEnabled(false);
					close_btn.setEnabled(true);
					gameserver = new GameServer(port);
					boolean success = gameserver.creat();
					if(success){
						messagePan.append_serverarea("遊戲伺服器已開啟。");
					} 
					
				}
			}
		});
		close_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				create_btn.setEnabled(true);
				port_text.setEnabled(true);
				close_btn.setEnabled(false);
				gameserver.close();
				messagePan.append_serverarea("遊戲伺服器已關閉。");
				//server_command.shutdown_immediate();
			}
		});
		port_text.setToolTipText("預設為7778");
		add(port_lab);
		add(port_text);
		add(create_btn);
		add(close_btn);
	}
}

class main_frm extends JFrame{
	static createPan createpan = new createPan();
	static messagePan messagepan = new messagePan();
	static game_severPan gamepan = new game_severPan();
	main_frm(){
		setTitle("伺服器端");
		setSize(500, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(this);
		setResizable(false);
		setVisible(true);
		setLayout(null);
		add(createpan);
		add(messagepan);
		add(gamepan);
	}
}
class createPan extends JPanel{
	static JButton create_btn = new JButton("創建伺服器");
	static JButton close_btn = new JButton("關閉伺服器");
	static JLabel port_lab = new JLabel("(預設為7777)port:");
	static JTextField port_text = new JTextField("7777");
	createPan(){
		setBorder(BorderFactory.createTitledBorder("伺服器操作"));
		setLayout(new FlowLayout());
		setBounds(5, 5, 480, 80);
		close_btn.setEnabled(false);
		create_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int port=-1;
				try {
					port = Integer.parseInt(port_text.getText());
				} catch(Exception e1){
					JOptionPane.showMessageDialog(null, port_text.getText() +"是一個不合法的port");
				}
				if(port > 65535)
					JOptionPane.showMessageDialog(null, port_text.getText() +"是一個不合法的port");
				
				if(port > 0 && port <= 65535){
					Server.db.setAllAccount_state_offline();
					Server.CServer = new Create_Server(port);
					Server.CServer.creat();
					
				}
			}
		});
		close_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				server_command.shutdown_immediate();
			}
		});
		port_text.setToolTipText("預設為7777");
		add(port_lab);
		add(port_text);
		add(create_btn);
		add(close_btn);
	}
	
	static void open() {
		create_btn.setEnabled(false);
		port_text.setEnabled(false);
		close_btn.setEnabled(true);
	}
}
class messagePan extends JPanel{
	static JTextArea message_area = new JTextArea();
	static JTextArea server_area = new JTextArea();
	private JTextField send_text = new JTextField();
	private JButton send_btn = new JButton("傳送");
	private Tabpan tabpan = new Tabpan();
	
	messagePan(){
		setBorder(BorderFactory.createTitledBorder("訊息"));
		setLayout(null);
		setBounds(5, 100, 480, 350);
		message_area.setEditable(false);
		server_area.setEditable(false);
		send_text.setBounds(10, 290, 380, 30);
		send_btn.setBounds(400, 290, 75, 30);
		add(tabpan);
		add(send_text);
		add(send_btn);
		send_btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(!send_text.getText().equals("")){
					send_Process(send_text.getText());
					send_text.setText("");
				}
			}
		});
	}
	void send_Process(String data){
		switch(data.charAt(0)){
			case '!':
				data = DelPrefix(data);
				String command_data[] = data.split(" ");
				comannd_Process(command_data);
				break;
			default :
				server_command.notice(data);
				break;
		}
	}
	void comannd_Process(String command[]){
		try {
			switch(command[0]){
				case "shutdown":
					server_command.shutdown(Integer.parseInt(command[1]));
					break;
				default :
					messagePan.append_serverarea("目前無此指令。");
					break;
			}
		} catch(ArrayIndexOutOfBoundsException e){
			messagePan.append_serverarea("你所下的參數錯誤。");
		}
	}
	String DelPrefix(String data){
		return data.substring(1,data.length());
	}
	class Tabpan extends JPanel{
		private JScrollPane message_scroll = new JScrollPane(message_area);
		private JScrollPane server_scroll = new JScrollPane(server_area);
		private JTabbedPane tp = new JTabbedPane();
		
		Tabpan(){
			setLayout(null);
			setBounds(10, 20, 450, 250);
			message_scroll.setBounds(10, 20, 450, 250);
			server_scroll.setBounds(10, 20, 450, 250);
			tp.setBounds(0, 0, 450, 250);
			tp.add("伺服器", server_scroll);
			tp.add("聊天室", message_scroll);
			add(tp);
		}
	}
	static void append_serverarea(String str){
		server_area.append(str + "\r\n");
		server_area.setCaretPosition(server_area.getDocument().getLength()); 
	}
	static void append_mesagearea(String str){
		message_area.append(str + "\r\n");
		message_area.setCaretPosition(message_area.getDocument().getLength()); 
	}
}

class Create_Server implements Runnable{
	Thread waitconnect_thread = new Thread(this);
	private int port = 7777;
	private ServerSocket ss;
	static all_client AllClineThread = new all_client();
	Create_Server(){}
	Create_Server(int port){
		this.port = port;
	}
	void close(){
		try {
			Server.db.setAllAccount_state_offline();
			AllClineThread.removeAll();
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void creat(){
		try {
			ss = new ServerSocket(port);
			String str = "伺服器已開啟。";
			messagePan.append_serverarea(str);
			waitconnect_thread.start();
			createPan.open();
		} catch (IOException ex){
			String str = "port:" + this.port + "已被占用。";
			messagePan.append_serverarea(str);
		}
	}
	
	public void run() {
		try {
			while(true){
				Socket cs = ss.accept();
				String str = "一個連線來自於:" + cs.getInetAddress().getHostAddress();
				messagePan.append_serverarea(str);
				AllClineThread.addClientThread(new clientIOProcess(cs));
			}
		} catch(Exception e){
			
		}
		
	}
}
class clientIOProcess implements Runnable{
	private DataInputStream in;
	private DataOutputStream out;
	private int id=-1;
	private String name="";
	private String sex="";
	private int admin = -1;
	Socket cs;
	clientIOProcess(Socket cs){
		try {
			this.cs = cs;
			in = new DataInputStream(cs.getInputStream());
			out = new DataOutputStream(cs.getOutputStream());
			this.sendCommand("is_connect");
		} catch (IOException e) {
		}
		Thread IO_thread = new Thread(this);
		IO_thread.start();
	}
	void OutStream(String str){
		try {
			out.writeUTF(str);
		} catch (IOException e) {
			
		}
	}
	void sendCommand(String str){
		try {
			out.writeUTF("server " + str);
		} catch (IOException e) {
			
		}
	}
	void sendMessage(String str){
		try {
			out.writeUTF("message " + str);
		} catch (IOException e) {
			
		}
	}
	public void run() {
		try {
			while(true){
				String in_data = in.readUTF();
				String prefix[] = in_data.split(" ");
				switch(prefix[0]){
					case "message":
						in_data = DelPrefix(in_data,prefix[0]);
						sendAllMessage_normal(name + ":" + in_data);
						messagePan.append_mesagearea(name + ":" + in_data);
						break;
					case "command":
						in_data = DelPrefix(in_data,prefix[0]);
						String command_str[] = in_data.split(" ");
						command(command_str);
						break;
					case "error":
						in_data = DelPrefix(in_data,prefix[0]);
						messagePan.append_serverarea("傳送錯誤前置碼。");
						break;
					case "server":
						break;
					default:
						messagePan.append_serverarea("一個來自"+ name +"錯誤的串流");
						break;
				}
			}
		} catch (Exception e){
			String str = name + " 使用者離線了。";
			messagePan.append_serverarea(str);
			Create_Server.AllClineThread.sendAllcommand_server("removeOnlineList " + name);
			try {
				out.close();
				in.close();
				Server.db.setAccount_state(id, 0);
				Create_Server.AllClineThread.removeClientThread(this);
			} catch (IOException e1) {
				
			}
		}
		
	}
	String DelPrefix(String str,String prefix){
		return str.substring(prefix.length()+1,str.length());
	}
	void command(String command[]){
		switch(command[0]){
			case "setName":
				setName(command[1]);
				break;
			case "setSex":
				setSex(command[1]);
				break;
			case "setAdmin":
				setAdmin(Integer.parseInt(command[1]));
				break;
			case "setId":
				setId(Integer.parseInt(command[1]));
				break;
			case "loginMessage":
				String str = name + " 已進入聊天室。";
				sendAllMessage_normal(str);
				messagePan.append_mesagearea(str);
				Server.db.setAccount_state(id, 1);
				Create_Server.AllClineThread.sendAllcommand_server("addOnlineList " + name);
				sendOnlineUser_List();
				break;
			default:
				messagePan.append_serverarea("來自客戶端命令錯誤:" + command[1]);
				break;
		}
	}
	void setName(String name){
		this.name = name;
	}
	void setSex(String sex){
		this.sex = sex;
	}
	void setAdmin(int admin){
		this.admin = admin;
	}
	void setId(int id){
		this.id = id;
	}
	int getId(){
		return this.id;
	}
	String getName(){
		return this.name;
	}
	void closeIO(){
		try {
			out.close();
			in.close();
			cs.close();
		} catch (IOException e){
			
		}
	}
	void sendAllMessage_normal(String message){
		for(int i=0;i<Create_Server.AllClineThread.getClinetSize();i++){
			clientIOProcess c = (clientIOProcess) Create_Server.AllClineThread.getelement(i);
			if(c.name == this.name)
				continue;
			c.sendMessage(message);
		}
	}
	void sendOnlineUser_List(){
		String str = "addOnlineList ";
		String name[] = Create_Server.AllClineThread.getAllUserName();
		for(int i=0;i<name.length;i++){
			if(name[i] == this.name)
				continue;
			sendCommand(str + name[i]);
		}
		
	}
}

class all_client{
	private Vector v;
	all_client(){
		v = new Vector();
	}
	public void addClientThread(clientIOProcess cs) {
		v.addElement(cs);
	}
	public void removeClientThread(clientIOProcess cs) {
		v.removeElement(cs);
	}
	public int getClinetSize() {
		return v.size();
	}
	public void sendAllMessage_server(String message){
		for(int i=0;i<v.size();i++){
			clientIOProcess c = (clientIOProcess) v.elementAt(i);
			c.sendMessage(message);
		}
	}
	public void sendAllcommand_server(String command){
		for(int i=0;i<v.size();i++){
			clientIOProcess c = (clientIOProcess) v.elementAt(i);
			c.sendCommand(command);
		}
	}
	public Object getelement(int i){
		return v.elementAt(i);
	}
	public void removeAll(){
		for(int i=0;i<v.size();i++){
			clientIOProcess c = (clientIOProcess) v.elementAt(i);
			c.closeIO();
		}
		v.removeAllElements();
	}
	public String[] getAllUserName(){
		String name[]=new String[v.size()];
		for(int i=0;i<v.size();i++){
			clientIOProcess c = (clientIOProcess) v.elementAt(i);
			name[i] = c.getName();
		}
		return name;
	}
}