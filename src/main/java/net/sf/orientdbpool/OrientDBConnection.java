package net.sf.orientdbpool;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dody
 */
public class OrientDBConnection {


    private static final Logger LOG = LoggerFactory.getLogger(OrientDBConnection.class);


    private String nodeName;

    private OrientDB orientdb;

    private ODatabaseSession session;

    private boolean isActive = false;

    private boolean isMaster = false;

    private boolean isWriteAble = true;

    private  ReadWriteProxyOrientDBPool pool;


    public OrientDBConnection( Opools pool, String node, OrientDB db, ODatabaseSession session) {
        this.nodeName = node;
        this.orientdb = db;
        this.session = session;
        this.pool = ( ReadWriteProxyOrientDBPool) pool;
    }

    /**
     * 关闭
     */
    public void close() {
        pool.returnConnection(this);
    }

    /**
     * 查询操作
     *
     * @param iCommand
     * @param iArgs
     * @return
     * @throws CurrentConnectionDisableException
     * @throws NoActiveOrientDBServerException 
     */
    public OResultSet query(String iCommand, Object... iArgs) throws CurrentConnectionDisableException, NoActiveOrientDBServerException {
        try {
//            LOG.info("query --> " + session.getURL());
            return session.query(iCommand, iArgs);

        } catch (Exception e) {
            LOG.error(session.getURL() + " Exception !",e);
            //TODO 判断异常是否属于服务器不可用
            pool.disableServer(this.nodeName);
            LOG.info("disableServer --> " + this.nodeName);
            throw new CurrentConnectionDisableException();
        }
    }

    /**
     * 执行
     *
     * @param iCommand
     * @param iArgs
     * @return
     * @throws CurrentConnectionDisableException
     * @throws NoActiveOrientDBServerException 
     */
    public OResultSet execute(String iCommand, Object... iArgs) throws OCommandSQLParsingException,CurrentConnectionDisableException, NoActiveOrientDBServerException {
        try {
//            LOG.info("execute --> " + session.getURL());
            return session.execute("sql", iCommand, iArgs);
        } catch (OCommandSQLParsingException e) {
            LOG.error("OCommandSQLParsingException Exception !\n " + iCommand ,e);
            return null ;
        }catch (Exception e) {
            LOG.error(session.getURL() + " Exception !\n " + iCommand ,e);
            //TODO 判断异常是否属于服务器不可用
            pool.disableServer(this.nodeName);
            LOG.info("disableServer --> " + this.nodeName);
            throw new CurrentConnectionDisableException();
        }
    }

    /**
     * 开启事务操作
     *
     * @return
     * @throws CurrentConnectionDisableException
     * @throws NoActiveOrientDBServerException 
     */
    public void begin() throws CurrentConnectionDisableException, NoActiveOrientDBServerException {
        try {
            session.begin();

        } catch (Exception e) {
            LOG.error(session.getURL() + " Exception !");
            //TODO 判断异常是否属于服务器不可用
            pool.disableServer(this.nodeName);
            LOG.info("disableServer --> " + this.nodeName);
            throw new CurrentConnectionDisableException();
        }
    }

    /**
     * 事务提交操作
     *
     * @throws CurrentConnectionDisableException
     * @throws NoActiveOrientDBServerException 
     */
    public void commit() throws CurrentConnectionDisableException, NoActiveOrientDBServerException {
        try {
            session.commit();

        } catch (Exception e) {
            LOG.error(session.getURL() + " Exception !",e);
            //TODO 判断异常是否属于服务器不可用
            pool.disableServer(this.nodeName);
            LOG.info("disableServer --> " + this.nodeName);
            throw new CurrentConnectionDisableException();
        }
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String name) {
        this.nodeName = name;
    }

    public OrientDB getOrientdb() {
        return orientdb;
    }

    public void setOrientdb(OrientDB orientdb) {
        this.orientdb = orientdb;
    }

    public ODatabaseSession getSession() {
        return session;
    }

    public void setSession(ODatabaseSession session) {
        this.session = session;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    public boolean isWriteAble() {
        return isWriteAble;
    }

    public void setWriteAble(boolean isWriteAble) {
        this.isWriteAble = isWriteAble;
    }


}
