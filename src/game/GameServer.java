package game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class GameServer implements Runnable {
	static final int QUEUE_SIZE = 10;
	static String[] waitqueue = new String[QUEUE_SIZE]; 
	static int rear=0,front=0;
	Thread waitconnect_thread = new Thread(this);
	private int port = 7778;
	private ServerSocket ss;
	static all_player allplayer = new all_player();
	static all_GameRoom allgameroom = new all_GameRoom();
	
	public GameServer(int port){
		this.port = port;
	}
	public void close(){
		try {
			allplayer.closeAll();
			allgameroom.closeAll();
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean creat(){
		try {
			resetQueue();
			ss = new ServerSocket(port);
			waitconnect_thread.start();
			return true;
		} catch (IOException ex){
			return false;
		}
	}
	void resetQueue(){
		rear=0;
		front=0;
	}
	
	public void run() {
		try {
			while(true){
				Socket cs = ss.accept();
				allplayer.addto_V(new playerIOProcess(cs));
				
			}
		} catch(Exception e){
			
		}
		
	}
	static void addq(String name){
		rear = (rear + 1) % QUEUE_SIZE;
		waitqueue[rear] = name;
	}
	static String delq(){
	     front = (front + 1) % QUEUE_SIZE;
	     return waitqueue[front];
	}
}

class playerIOProcess implements Runnable{
	private String name = "";
	private DataInputStream in;
	private DataOutputStream out;
	private boolean wait = true;
	Socket cs;
	
	playerIOProcess(Socket cs){
		try {
			this.cs = cs;
			in = new DataInputStream(cs.getInputStream());
			out = new DataOutputStream(cs.getOutputStream());
		} catch (IOException e) {
		}
		Thread IO_thread = new Thread(this);
		IO_thread.start();
	}
	public void run() {
		try {
			while(true){
				String in_data = in.readUTF();
				String prefix[] = in_data.split(" ");
				switch(prefix[0]){
					case "message":
						
						break;
					case "command":
						in_data = DelPrefix(in_data,prefix[0]);
						String command_str[] = in_data.split(" ");
						command(command_str);
						break;
					case "error":
						
						break;
					default:
						
						break;
				}
			}
		} catch (Exception e){
			try {
				out.close();
				in.close();
			} catch (IOException e1) {
				
			}
		}
		
	}
	void creatGameroom(String str[]){
		GameServer.allgameroom.addto_V(new GameRoom(str));
	}
	void addGameroom(GameRoom gm){
		gm.add(name);
	}
	boolean search_has_wait(){
		if(Math.abs(GameServer.front-GameServer.rear) >= 2)
			return true;
		return false;
			
	}
	
	String DelPrefix(String str,String prefix){
		return str.substring(prefix.length()+1,str.length());
	}
	void command(String command[]){
		switch(command[0]){
			case "setName":
				setName(command[1]);
				GameServer.addq(command[1]);
				break;
			case "SearchGame":
				if(search_has_wait() == true){
					String[] str = new String[2];
					for(int i=0;i<2;i++){
						str[i] = GameServer.delq();
					}
					creatGameroom(str);
				}
				break;
			case "cancel":
				setCancel();
				break;
			case "closeroom":
				closeRoom(Integer.parseInt(command[1]));
				break;
			case "closeroomerr":
				closeRoom_err(Integer.parseInt(command[1]),name);
				break;
			default:
				break;
		}
	}
	void setName(String name){
		this.name = name;
	}
	void setCancel(){
		String qname = GameServer.delq();
		if(!qname.equals(this.name)){
			GameServer.waitqueue[GameServer.front] = qname;
		}
	}
	String getName(){
		return this.name;
	}
	void sendCommand(String command,String data){
		String str = "gameserver "+ command +" " + data;
		try {
			out.writeUTF(str);
		} catch (IOException e) {
			
		}
	}
	void close(){
		try {
			out.close();
			in.close();
			cs.close();
		} catch (IOException e) {
		}
		
	}
	GameRoom getRoominfo(int roomid){
		GameRoom gm = null;
		for(int i=0;i<GameServer.allgameroom.getSize();i++){
			gm = (GameRoom) GameServer.allgameroom.getElement(i);
			if(gm.getRoomId() == roomid){
				break;
			}
		}
		return gm;
	}
	void closeRoom(int roomid){
		GameRoom gm = getRoominfo(roomid);
		GameServer.allgameroom.removeto_V(gm);
	}
	void closeRoom_err(int roomid,String name){
		GameRoom gm = getRoominfo(roomid);
		String[] name_arr = gm.getName();
		for(int i=0;i<name_arr.length;i++){
			if(!name_arr[i].equals(name)){
				name = name_arr[i];
				break;
			}
		}
		
		playerIOProcess pio = null;
		for(int i=0;i<GameServer.allplayer.getClinetSize();i++){
			pio = (playerIOProcess) GameServer.allplayer.getElement(i);
			if(pio.getName().equals(name))
				break;
		}
		pio.sendCommand("closeroomerr", "");
		closeRoom(roomid);
	}
}
class all_player{
	
	private Vector v;
	all_player(){
		v = new Vector();
	}
	public void addto_V(playerIOProcess cs) {
		v.addElement(cs);
	}
	public void removeto_V(playerIOProcess cs) {
		v.removeElement(cs);
	}
	public void removeAll_V() {
		v.removeAllElements();
	}
	public int getClinetSize() {
		return v.size();
	}
	public Object getElement(int i){
		return v.elementAt(i);
	}
	void closeAll(){
		for(int i=0;i<v.size();i++){
			playerIOProcess pio = (playerIOProcess) v.elementAt(i);
			pio.close();
		}
		removeAll_V();
	}
}
class GameRoom {
	static int room_number = 0;
	private int roomID = 0;
	private final int maxnumber = 2; 
	private int number = 0;
	private String[] name = new String[2];
	GameRoom(String[] name) {
		for(int i=0;i<name.length;i++){
			this.name[i] = name[i];
			//System.out.println("1" +this.name[i]);
		}
		this.roomID = ++room_number;
		go();
	}
	
	void add(String name){
		this.name[number++] = name;
	}
	int getRoomId(){
		return this.roomID;
	}
	
	void go(){
		for(int i=0;i<GameServer.allplayer.getClinetSize();i++){
			GameServer.allplayer.getElement(i);
			playerIOProcess p = (playerIOProcess) GameServer.allplayer.getElement(i);
			if(p.getName() == name[0] || p.getName() == name[1]){
				p.sendCommand("opengame", " ");
				p.sendCommand("setroomid", String.valueOf(roomID));
			}
		}
	}
	String[] getName(){
		return name;
	}
}
class all_GameRoom {
	private Vector v;
	all_GameRoom(){
		v = new Vector();
	}
	
	public void addto_V(GameRoom gm) {
		v.addElement(gm);
	}
	public void removeto_V(GameRoom gm) {
		v.removeElement(gm);
	}
	public void removeAll_V() {
		v.removeAllElements();
	}
	public int getSize() {
		return v.size();
	}
	public Object getElement(int i){
		return v.elementAt(i);
	}
	public boolean getVisEmpty() {
		return v.isEmpty();
	}
	void closeAll(){
		v.removeAllElements();
	}
	
}