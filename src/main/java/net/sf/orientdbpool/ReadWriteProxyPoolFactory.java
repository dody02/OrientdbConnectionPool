package net.sf.orientdbpool;

import com.orientechnologies.orient.client.remote.OStorageRemote;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 构建连接池的工厂类
 * 可通过此类，构建Orient读写连接池
 */
public class ReadWriteProxyPoolFactory implements  OrientDBPoolsFactory {

	
    private static final Logger LOG = LoggerFactory.getLogger(ReadWriteProxyPoolFactory.class);
    
	@Override
	public  Opools buildODBPool( OrientDBConnectConfig config) throws Exception {
		
		ReadWriteProxyOrientDBPool readWriteProxyPool = new ReadWriteProxyOrientDBPool(this,config);
		List<String>readConn = config.getReadConnections();
		for (int i =0 ; i < readConn.size(); i ++) {
			ConcurrentLinkedDeque< OrientDBConnection> ReadOnlyconnections = new ConcurrentLinkedDeque< OrientDBConnection>();

			String host = readConn.get(i);
			if (i == 0 ) {
				ReadOnlyconnections.add(createConnection(readWriteProxyPool,host,config.getDatabase(),config.getUsername(),config.getPasswd(), true));
				readWriteProxyPool.setCurrentReadOnlyServer(host);
			}else {
				ReadOnlyconnections.add(createConnection(readWriteProxyPool,host,config.getDatabase(),config.getUsername(),config.getPasswd(),true));
			}
			readWriteProxyPool.addAllconnections(host, ReadOnlyconnections);
		}
		
		
		List<String> writeConn = config.getWriteConnections();
		for (int i = 0 ; i < writeConn.size(); i ++) {
			ConcurrentLinkedDeque< OrientDBConnection> writeConnections = new ConcurrentLinkedDeque< OrientDBConnection>();
			String host = writeConn.get(i);
			if (i == 0 ) {
				writeConnections.add(createConnection(readWriteProxyPool,host,config.getDatabase(),config.getUsername(),config.getPasswd(), false));
				readWriteProxyPool.setCurrentServer(host);
			} else {
				writeConnections.add(createConnection(readWriteProxyPool,host,config.getDatabase(),config.getUsername(),config.getPasswd(),false));
			}
			readWriteProxyPool.addAllconnections(host, writeConnections);
		}
		
		
		return readWriteProxyPool;
	}
	
	/**
	 * 
	 * @param readWriteProxyPool
	 * @param url
	 * @param database
	 * @param username
	 * @param passwd
	 * @param isReadOnly
	 * @return
	 */
	public  OrientDBConnection createConnection ( ReadWriteProxyOrientDBPool readWriteProxyPool, String url, String database, String username, String passwd, boolean isReadOnly) throws Exception{
		 OrientDBConnection rs  = null;
		try {
			OrientDB orientDB = new OrientDB(url, OrientDBConfig.defaultConfig());
			OrientDBConfig config  = OrientDBConfig.builder().
					addConfig(OGlobalConfiguration.COMMAND_CACHE_ENABLED, false).
					addConfig(OGlobalConfiguration.NETWORK_SOCKET_RETRY, 0).
					addConfig(OGlobalConfiguration.NETWORK_SOCKET_RETRY_DELAY, 0).
					addConfig(OGlobalConfiguration.CLIENT_CONNECTION_STRATEGY, OStorageRemote.CONNECTION_STRATEGY.STICKY).
					build();
			ODatabaseSession  session =  orientDB.open(database, username, passwd, config);

			rs = new  OrientDBConnection(readWriteProxyPool,url,orientDB,session);
			
			if(isReadOnly) {
				readWriteProxyPool.appendReadOnlyCount();
			}else {
				readWriteProxyPool.appendWriteCount();
			}
		}catch(Exception e) {
			LOG.error("create New Connection Exception ",e);
			throw e;
		}
		
		
		return rs;
	}
}
