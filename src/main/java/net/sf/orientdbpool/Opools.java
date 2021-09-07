package net.sf.orientdbpool;

/**
 * 池
 * @author Dody
 *
 */
public interface Opools extends AutoCloseable {
	
	/**
	 * 获取连接
	 * @return
	 * @throws NoActiveOrientDBServerException 
	 */
	public OrientDBConnection getConnection() throws NoActiveOrientDBServerException;
	/**
	 * 获取只读连接
	 * @return
	 * @throws NoActiveOrientDBServerException 
	 */
	public OrientDBConnection getReadOnlySession() throws NoActiveOrientDBServerException;
	
	/**
	 * 归还连接
	 * @param connection
	 */
	public void returnConnection(OrientDBConnection connection);
	/**
	 * 阻塞获取连接
	 * @param waitTime
	 * @param reTrytimes
	 * @return
	 * @throws NoActiveOrientDBServerException 
	 */
	public OrientDBConnection getConnection(long waitTime,int reTrytimes) throws NoActiveOrientDBServerException,CurrentConnectionDisableException;
	
	/**
	 * 阻塞获取连接
	 * @param waitTime
	 * @param reTrytimes
	 * @return
	 * @throws NoActiveOrientDBServerException 
	 */
	public OrientDBConnection getReadOnlySession(long waitTime,int reTrytimes) throws NoActiveOrientDBServerException,CurrentConnectionDisableException;
	/**
	 * 确认服务不可用
	 * @param node
	 * @throws NoActiveOrientDBServerException 
	 */
	public void disableServer (String node) throws NoActiveOrientDBServerException ;
	
}
