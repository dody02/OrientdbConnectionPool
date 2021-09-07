package net.sf.orientdbpool;


import com.orientechnologies.orient.core.db.ODatabaseSession;
import net.sf.orientdbpool.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 原官方的客户端自动切换无法满足需求。
 * 这里分开只读可写两个连接池
 * 基于OrientDB集群配置，可将OreintDB中读写节点进行分离。这里的连接池与其对应
 *
 * @author Dody
 */
public class ReadWriteProxyOrientDBPool extends TimerTask implements  Opools {

    private static final Logger LOG = LoggerFactory.getLogger(ReadWriteProxyOrientDBPool.class);

    private  OrientDBConnectConfig config;

    private volatile int readOnlyCount;

    private volatile int writeCount;
    /**
     * host,role
     */
    private String currentServer;
    /**
     * host,role
     */
    private String currentReadOnlyServer;

    private ConcurrentHashMap<String, ConcurrentLinkedDeque<OrientDBConnection>> allconnections = new ConcurrentHashMap<String, ConcurrentLinkedDeque<OrientDBConnection>>();

    private ReadWriteProxyPoolFactory factory;

    private Timer timer = new Timer();


//    RedisUtil redisUtil;

    public ReadWriteProxyOrientDBPool(ReadWriteProxyPoolFactory factory,  OrientDBConnectConfig config) {
        this.factory = factory;
        this.config = config;
        timer.schedule(this, config.getCheckTime(), config.getCheckTime());
    }

    /**
     * 获取可写连接
     * @throws NoActiveOrientDBServerException 
     */
    @Override
    public OrientDBConnection getConnection() throws NoActiveOrientDBServerException {
        OrientDBConnection rs = null ;
        try {
            rs = getWriteableConnection();
            if (rs != null) {
                rs.getSession().activateOnCurrentThread();
            }
            return rs;
        } catch (Exception e) {
            if (StringUtils.isEmpty(rs.getNodeName())) {
                this.disableServer(rs.getNodeName());
            }
            e.printStackTrace();
        }
        return rs ;
    }

    /**
     * @param waitTime
     * @param reTrytimes
     * @return
     * @throws NoActiveOrientDBServerException 
     */
    @Override
    public OrientDBConnection getConnection(long waitTime, int reTrytimes) throws NoActiveOrientDBServerException ,CurrentConnectionDisableException{
        OrientDBConnection rs = getWriteableConnection();
        if (rs == null) {
            rs = retryGetConnection(false, waitTime, reTrytimes);
        } else {
            rs.getSession().activateOnCurrentThread();
        }

        return rs;
    }


    /**
     * @return
     * @throws NoActiveOrientDBServerException 
     */
    @Override
    public OrientDBConnection getReadOnlySession() throws NoActiveOrientDBServerException {
        OrientDBConnection rs = getActiveReadOnlyConnection();
        if (rs != null) {
            rs.getSession().activateOnCurrentThread();
        }
        return rs;
    }

    /**
     * @return
     * @throws NoActiveOrientDBServerException 
     */
    @Override
    public OrientDBConnection getReadOnlySession(long waitTime, int reTrytimes) throws NoActiveOrientDBServerException,CurrentConnectionDisableException {
        OrientDBConnection rs = getActiveReadOnlyConnection();
        if (rs == null) {
            rs = retryGetConnection(true, waitTime, reTrytimes);
        } else {
            rs.getSession().activateOnCurrentThread();
        }
        return rs;
    }

    /**
     * 获取活跃的只读连接
     *
     * @return
     * @throws NoActiveOrientDBServerException 
     */
    private OrientDBConnection getActiveReadOnlyConnection() throws NoActiveOrientDBServerException {
        OrientDBConnection rs = null;
        synchronized (allconnections){
            if (allconnections.get(currentReadOnlyServer).isEmpty()) {
            if (this.readOnlyCount < config.getMaxReadOnlySize()) {
                try {
					rs = factory.createConnection(this, currentReadOnlyServer, config.getDatabase(), config.getUsername(),
					        config.getPasswd(), true);

				} catch (Exception e) {
					// 如果创建失败，则切换
					this.disableServer(currentReadOnlyServer);

				}
            }
        } else {
//            LOG.info("get connection from :"+currentReadOnlyServer);
            rs = allconnections.get(currentReadOnlyServer).pop();
        }
        }

        return rs;
    }


    @Override
    public void close() throws Exception {
        timer.cancel();
        if (allconnections.size() > 0) {
            Collection<ConcurrentLinkedDeque<OrientDBConnection>> connections = allconnections.values();
            for (ConcurrentLinkedDeque<OrientDBConnection> tmpQueue : connections) {
                while (!tmpQueue.isEmpty()) {
                    try {
                        tmpQueue.pop().getSession().close();
                    } catch (Exception e) {
                        LOG.error("close session exception",e);
                    }
                }
            }
        }
        allconnections.clear();
    }

    @Override
    public void returnConnection(OrientDBConnection connection) {
        // TODO 将连接存放回池
        if (allconnections.get(connection.getNodeName()) != null) {
        	allconnections.get(connection.getNodeName()).add(connection);
//        	LOG.info("Idle connection count of "+connection.getNodeName()+":"+allconnections.get(connection.getNodeName()).size());
        } else {
            LOG.info("connection.getNodeName():"+connection.getNodeName()+" has not return");
        }

    }

    @Override
    public void disableServer(String node) throws NoActiveOrientDBServerException {
        // TODO 清除不可用服务，标志可用的服务
        synchronized (this) {
            if (node.equals(this.getCurrentReadOnlyServer())) {
                this.setCurrentReadOnlyServer(null);
                List<String> items = config.getReadConnections();
                if ( items.size() <=1 ) {
                	throw new NoActiveOrientDBServerException("Only One Node in Configure ,No Active OrientDB Node can Use!!!");
                }
                	
                for (int i = 0; i < items.size(); i++) {
                    if (!items.get(i).equals(node)) {// 取另一个备用地址
                        this.currentReadOnlyServer = items.get(i);
                        this.readOnlyCount = this.allconnections.get(currentReadOnlyServer).size();
                        LOG.info("failover to Node:"+currentReadOnlyServer+",current read only connection count:"+readOnlyCount);
                        break;
                    }
                }
            }

            if (node.equals(this.getCurrentServer())) {
                this.setCurrentServer(null);
                List<String> items = config.getWriteConnections();
                if ( items.size() <=1 ) {
                	throw new NoActiveOrientDBServerException("Only One Node in Configure ,No Active OrientDB Node can Use!!!");
                }
                for (int i = 0; i < items.size(); i++) {
                    if (!items.get(i).equals(node)) {// 取另一个备用地址
                        this.currentServer = items.get(i);
                        this.writeCount = this.allconnections.get(currentServer).size();
                        LOG.info("failover to Node:"+currentServer+",current read only connection count:"+writeCount);
                        break;
                    }
                }
            }

            allconnections.get(node).clear();
//            sendRedis(node);
        }

    }

//    /**
//     *  发送到redis缓存
//     *  由web端获取并发送邮件
//     * @param node
//     */
//    private void sendRedis( String node){
//        if( StringUtils.isNotEmpty(node)){
//            redisUtil.set(Constant.CACHE_KEY_DISABLESERVER_SENDMAIL,node);    
//        }
//    }

    public String getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(String currentServer) {
        this.currentServer = currentServer;
    }

    public String getCurrentReadOnlyServer() {
        return currentReadOnlyServer;
    }

    public void setCurrentReadOnlyServer(String currentReadOnlyServer) {
        this.currentReadOnlyServer = currentReadOnlyServer;
    }

    public void addAllconnections(String host, ConcurrentLinkedDeque<OrientDBConnection> connections) {
        this.allconnections.put(host, connections);
    }

    public synchronized void appendReadOnlyCount() {
        this.readOnlyCount++;
        LOG.info("current readOnly connection count :"+readOnlyCount);
    }

    public synchronized void appendWriteCount() {
        this.writeCount++;
        LOG.info("current writeable connection count :"+writeCount);
    }

    /**
     * get active readonly connections count
     *
     * @return
     */
    public int getReadOnlyCount() {
        return readOnlyCount;
    }

    /**
     * get active connections count
     *
     * @return
     */
    public int getWriteCount() {
        return writeCount;
    }

    @Override
    public void run() {
        // Check the active Connection count
        while (allconnections.get(currentServer).size() > config.getIdleConnection()) {
//            allconnections.get(currentServer).pop().getSession().close();
            ODatabaseSession session = allconnections.get(currentServer).pop().getSession();
            session.activateOnCurrentThread();
            session.close();
            synchronized (this) {
                readOnlyCount--;
            }
            LOG.info("current connection count:"+readOnlyCount);
        }
        while (allconnections.get(currentReadOnlyServer).size() > config.getIdleConnection()) {
            allconnections.get(currentReadOnlyServer).pop().getSession().close();
            synchronized (this) {
                    writeCount--;
            }
            LOG.info("current connection count:"+writeCount);
        }
    }

    /**
     * 获取可写连接
     *
     * @param
     * @return
     * @throws NoActiveOrientDBServerException 
     */
    private OrientDBConnection getWriteableConnection() throws NoActiveOrientDBServerException {
        OrientDBConnection rs = null;
        if (allconnections.get(currentServer).isEmpty()) {
            if (this.writeCount < config.getMaxSize()) {
            	try {
            		  rs = factory.createConnection(this, currentServer, config.getDatabase(), config.getUsername(),
                              config.getPasswd(), false);
            	}catch (Exception e ) {
            		//不可用,切换
            		this.disableServer(currentServer);
            	}
              
            }
        } else {
            rs = allconnections.get(currentServer).pop();
        }
        return rs;
    }

    /**
     * Retry Get Connection
     *
     * @param isReadOnly 是否只读连接
     * @param waitTime
     * @param reTrytimes
     * @param
     * @return
     */
    private OrientDBConnection retryGetConnection(boolean isReadOnly, long waitTime, int reTrytimes) throws CurrentConnectionDisableException {
        OrientDBConnection rs = null;

        while (reTrytimes > 0) {
            LOG.info("not active connection, wait " + waitTime + "milliseconds  and retry " + reTrytimes + " times");
            try {
                Thread.sleep(waitTime);
                if (isReadOnly) {
                    rs = allconnections.get(currentReadOnlyServer).pop(); //只读连接
                } else {
                    rs = allconnections.get(currentServer).pop(); //可写连接
                }
                if (rs != null) {
                    LOG.info("get a activeConnection,and return to awake the business process ");
                    break;
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                reTrytimes--;
            }
        }
        if (rs != null) {
            rs.getSession().activateOnCurrentThread();
        } else {
            throw new CurrentConnectionDisableException();
        }
        return rs;
    }


}
