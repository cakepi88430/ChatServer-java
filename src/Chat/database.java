package Chat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class database {
	static String DB_host = "127.0.0.1";
	static String DB_name = "chat_java";
	static String DB_user = "root";
	static String DB_passwd = "";
	static final String DB_URL = "jdbc:mysql://" + DB_host +"/" + DB_name + "?useUnicode=true&characterEncoding=utf-8";
	private Connection connect = null;
	private Statement state = null;
	private ResultSet result = null;
	database(){
		loadSetting();
		try {
			connect = DriverManager.getConnection(DB_URL,DB_user,DB_passwd);
			//System.out.println("MySQL connect status OK!!");
			state = connect.createStatement();
		} catch (SQLException e){
			System.out.println("MySQL connect status fail!!");
		}
	}
	
	
	int update(String sql){
		try {
			int resc = state.executeUpdate(sql);
			return resc;
		} catch (SQLException e) {
			System.out.println("錯誤");
		}
		return 0; 
	}
	void setAccount_state(int id,int state){
		//state 0=offline,1=online
		String update_sql = "UPDATE `accounts` SET state = '" + state + "' WHERE id = '" + id + "'";
		int res = update(update_sql);
	}
	void setAllAccount_state_offline(){
		//state 0=offline,1=online
		String update_sql = "UPDATE `accounts` SET state = '0' WHERE state = '1' ";
		int res = update(update_sql);
	}
	ResultSet getSQL_Data(String sql){
		try {
			result = state.executeQuery(sql);
			return result;
		} catch (SQLException e) {
			
		}
		return null;
	}
	public static void loadSetting() {
		DB_host = ServerProperties.getProperty("DB_host", DB_host);
		DB_name = ServerProperties.getProperty("DB_name", DB_name);
		DB_user = ServerProperties.getProperty("DB_user", DB_user);
		DB_passwd = ServerProperties.getProperty("DB_passwd", DB_passwd);
	}
}
