package net.sf.orientdbpool.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * Created by luoliang on 2019/1/10.
 */
public class StringUtils {

    private static final Logger LOG = LoggerFactory.getLogger(StringUtils.class);

    private static final String hexDigIts[] = {"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};

    /**
     * 字符串是否为空
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    /**
     * 字符串是否不为空
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !"".equals(str.trim());
    }

    /**
     * 字符串截取函数
     * @param str       原始字符串
     * @param start     截取位置，下标从0开始
     * @return
     */
    public static String substring(String str, int start) {
        if(str == null) {
            return null;
        } else {
            if(start < 0) {
                start += str.length();
            }
            if(start < 0) {
                start = 0;
            }
            return start > str.length()?"":str.substring(start);
        }
    }

    /**
     * 字符串截取函数
     * @param str       原始函数
     * @param start     截取位置，小标从0开始
     * @param end       结束位置，截取后不包含此位置字符
     * @return
     */
    public static String substring(String str, int start, int end) {
        if(str == null) {
            return null;
        } else {
            if(end < 0) {
                end += str.length();
            }
            if(start < 0) {
                start += str.length();
            }
            if(end > str.length()) {
                end = str.length();
            }

            if(start > end) {
                return "";
            } else {
                if(start < 0) {
                    start = 0;
                }

                if(end < 0) {
                    end = 0;
                }
                return str.substring(start, end);
            }
        }
    }

    /**
     * 字符串加密
     * @param origin    原始字符串
     * @return
     */
    public static String MD5Encode(String origin){
        return MD5Encode(origin,"utf-8");
    }

    /**
     * 字符串加密
     * @param origin        原始字符串
     * @param charsetname   字符串编码
     * @return
     */
    public static String MD5Encode(String origin, String charsetname){
        String resultString = null;
        try{
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if(null == charsetname || "".equals(charsetname)){
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            }else{
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
            }
        }catch (Exception e){
        }
        return resultString;
    }

    public static String byteArrayToHexString(byte b[]){
        StringBuffer resultSb = new StringBuffer();
        for(int i = 0; i < b.length; i++){
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    public static String byteToHexString(byte b){
        int n = b;
        if(n < 0){
            n += 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigIts[d1] + hexDigIts[d2];
    }

    /**
     * 字符串是否相等
     * @param cs1
     * @param cs2
     * @return
     */
    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        return cs1 == cs2?true:(cs1 != null && cs2 != null?(cs1.length() != cs2.length()?false:(cs1 instanceof String && cs2 instanceof String?cs1.equals(cs2):regionMatches(cs1, false, 0, cs2, 0, cs1.length()))):false);
    }

    private static boolean regionMatches(CharSequence cs, boolean ignoreCase, int thisStart, CharSequence substring, int start, int length) {
        if(cs instanceof String && substring instanceof String) {
            return ((String)cs).regionMatches(ignoreCase, thisStart, (String)substring, start, length);
        } else {
            int index1 = thisStart;
            int index2 = start;
            int tmpLen = length;
            int srcLen = cs.length() - thisStart;
            int otherLen = substring.length() - start;
            if(thisStart >= 0 && start >= 0 && length >= 0) {
                if(srcLen >= length && otherLen >= length) {
                    while(tmpLen-- > 0) {
                        char c1 = cs.charAt(index1++);
                        char c2 = substring.charAt(index2++);
                        if(c1 != c2) {
                            if(!ignoreCase) {
                                return false;
                            }

                            if(Character.toUpperCase(c1) != Character.toUpperCase(c2) && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                                return false;
                            }
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    static Pattern p = Pattern.compile("\\s*|\t|\r|\n");

    /**
     *  去除字符串中含有换行符以及空格
     * @param value 原始字符串
     * @return
     */
    public static String cleanNewLineAndSpaces(String value) {
        String rs = value ;
        if(  !isEmpty(value) ){
            Matcher m = p.matcher(value);
            rs = m.replaceAll("");
        }
        return rs;
    }



    public static boolean containsIgnoreCase(String str, String searchStr) {
        if(str != null && searchStr != null) {
            int len = searchStr.length();
            int max = str.length() - len;

            for(int i = 0; i <= max; ++i) {
                if(str.regionMatches(true, i, searchStr, 0, len)) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    /**
     *  对单引号双引号进行处理
     * @param text
     * @return
     */
    public  static String handleSingleorDoubleQuotes(String text){
        if( StringUtils.isNotEmpty(text)){
             String[] fbsArr = { "\'", "\""};
             for (String key : fbsArr) {
             if (text.contains(key)) {
                text = text.replace(key, "\\" + key);
             }
             }
        }
        return text;
    }


    public  static String urlEncode(String text){
        if( StringUtils.isNotEmpty(text)){
            try {
                return URLEncoder.encode(text,"utf-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error("urlEncode error",e);
            }
        }
        return text;
    }


    public  static String urlDecode(String text){
        if( StringUtils.isNotEmpty(text)){
            try {
                return URLDecoder.decode(text,"utf-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error("urlEncode error",e);
            }
        }
        return text;
    }
}
