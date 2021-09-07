package net.sf.orientdbpool;

/**
 * 连接池构建
 * @author Dody
 *
 */
public interface OrientDBPoolsFactory {

	public  Opools buildODBPool ( OrientDBConnectConfig config) throws Exception;
}
