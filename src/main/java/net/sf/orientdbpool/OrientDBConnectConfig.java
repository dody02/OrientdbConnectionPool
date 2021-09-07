package net.sf.orientdbpool;

import java.util.List;

/**
 * OrientDB配置信息
 * @author Dody
 *
 */
public class OrientDBConnectConfig {
	
	public static final String READONLYCONNECTION = "read";
	
	public static final String READWRITECONNECTION = "write";

	
	private String database;
	
	private String username;
	
	private String passwd;
	
	
	private int minReadOnlySize;
	
	private int maxReadOnlySize;
	
	private int minSize;
	
	private int maxSize;

	private List<String> readConnections;

	private List<String> writeConnections;
	
	
	private long checkTime = 30000;
	private int idleConnection = 1;

	public List<String> getReadConnections() {
		return readConnections;
	}

	public void setReadConnections(List<String> readConnections) {
		this.readConnections = readConnections;
	}

	public List<String> getWriteConnections() {
		return writeConnections;
	}

	public void setWriteConnections(List<String> writeConnections) {
		this.writeConnections = writeConnections;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public int getMinReadOnlySize() {
		return minReadOnlySize;
	}

	public void setMinReadOnlySize(int minReadOnlySize) {
		this.minReadOnlySize = minReadOnlySize;
	}

	public int getMaxReadOnlySize() {
		return maxReadOnlySize;
	}

	public void setMaxReadOnlySize(int maxReadOnlySize) {
		this.maxReadOnlySize = maxReadOnlySize;
	}

	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public int getIdleConnection() {
		return idleConnection;
	}
	
	public void setIdleConnection(int idleConnection) {
		this.idleConnection = idleConnection;
	}
	
	public long getCheckTime() {
		return checkTime;
	}
	
	public void setCheckTime(long checkTime) {
		this.checkTime = checkTime;
	}
	
}
