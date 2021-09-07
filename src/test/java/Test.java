import com.orientechnologies.orient.core.sql.executor.OResultSet;
import net.sf.orientdbpool.*;
import net.sf.orientdbpool.utils.OResult2Json;
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
        String userName = "root";
        //数据库密码
        String passwd = "asdf";
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
        OResultSet rs = con.execute("select * from member  ");
        if (rs.hasNext()){
            System.out.println(OResult2Json.getJsonObject(rs.next()));
        }
        /**
         * 归还连接
         */
        con.close();
        LOG.info("OK");

    }
}
