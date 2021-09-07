# Orientdb Connection Pool 作为OrientDB的数据库连接池，提供连接池工具。
相对于OreintDB自带的接口，这个工具多了几项功能； 包括：
1. 针对集群，连接池可以配置区分只读和可写两种连接。
2. 当集群数据库节点有不可用的时候，连接池可以自动切换到集群中可用的节点上。
3. 简化连接操作。
  
# 例如：
import net.sf.orientdbpool.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Test {

    private  static Logger LOG = LoggerFactory.getLogger(Test.class);

    public static void main(String[] arg) throws Exception {

        //数据库名称
        String dataBase = "zb";
        //数据库用户名
        String userName = "user";
        //数据库密码
        String passwd = "passwd";
        //最大只读连接
        int maxReadOnly = 20;
        //最小只读连接
        int minReadOnly = 1;
        //最大可写连接
        int maxWrite = 20;
        //最小可写连接
        int minWrite = 1;

        //只读Orient服务器
        List<String> readdbs = Arrays.asList(new String[]{"remote:localhost:2424","remote:10.0.2.130:2424"});
        //可写Orient服务器
        List<String> writedbs = Arrays.asList(new String[]{"remote:localhost:2424"});


        /**
         *  step1. 构建配置信息类
         */
        OrientDBConnectConfig config = new OrientDBConnectConfig();
        config.setDatabase(dataBase);
        config.setUsername(userName);
        config.setPasswd(passwd);
        config.setMaxReadOnlySize(maxReadOnly);
        config.setMinReadOnlySize(minReadOnly);
        config.setMaxSize(maxWrite);
        config.setMinSize(minWrite);
        config.setReadConnections(readdbs);
        config.setWriteConnections(writedbs);

        /**
         *  step2. 构建数据库连接
         */
        OrientDBPoolsFactory fa = new ReadWriteProxyPoolFactory();
        Opools pools = (ReadWriteProxyOrientDBPool) fa.buildODBPool(config);

        LOG.info("*********************************************************************** Orientdb Config ****************************************************************************************");
        LOG.info("************* userName:" + config.getUsername());
        LOG.info("************* passwd:" + config.getPasswd());
        LOG.info("************* readdbs:" + readdbs);
        LOG.info("************* writedbs:" + writedbs);
        LOG.info("************* IdleConnection:" + config.getIdleConnection());
        LOG.info("************* dataBase:" + dataBase);
        LOG.info("************* maxReadOnly:" + maxReadOnly);
        LOG.info("************* minReadOnly:" + minReadOnly);
        LOG.info("************* maxWrite:" + maxWrite);
        LOG.info("************* minWrite:" + minWrite);

        /**
         * step3. 获取连接，进行数据库操作
         */
        OrientDBConnection con = pools.getConnection();
        con.execute("select * from m  ");
        /**
         * 归还连接
         */
        con.close();

    }
}
