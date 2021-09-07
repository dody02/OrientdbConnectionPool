package net.sf.orientdbpool.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.orientechnologies.orient.core.sql.executor.OResult;

public class OResult2Json {

    public static JSONObject getJsonObject(OResult row) {

        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        return JSONObject.parseObject(row.toJSON(), Feature.IgnoreAutoType);
    }
}
