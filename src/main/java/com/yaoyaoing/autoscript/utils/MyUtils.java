package com.yaoyaoing.autoscript.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyUtils {

    public final static String date_time_day_str = "yyyy-MM-dd";
    public final static String date_time_sec_str = "yyyy-MM-dd HH:mm:ss";
    public final static String date_time_min_str = "yyyy-MM-dd HH:mm";
    public final static String date_number = "yyyyMMddHHmmss";

    public final static String output_placeholder_left_char = ">>";
    public final static String output_placeholder_right_char = "<<";


    public static String currentTime() {
        return currentTime(date_time_sec_str);
    }

    public static String currentTime(String format) {
        Calendar calendar = Calendar.getInstance();
        return DateUtil.format(calendar.getTime(), format);
    }


    public static long secmillis(String time) {
        Date clear = DateUtil.parse(time);
        return clear.getTime() / 1000;
    }


    public static long secMillis() {
        return System.currentTimeMillis() / 1000;
    }

    public static long secMillis(int add) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, add);
        long sec = calendar.getTimeInMillis() / 1000;
        return sec;
    }

    /**
     * 时间戳 转 日期
     *
     * @param timeStamp
     * @param format
     * @return
     */
    public static String timeStamp2Date(Object timeStamp, String format) {
        if (timeStamp == null || timeStamp.toString().isEmpty() || timeStamp.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String date = dateFormat.format(new Long(timeStamp + "000"));
        return date;
    }

    public final static String output_placeholder(boolean left) {
        return output_placeholder_left(15, left);
    }

    public final static String output_placeholder_left(int size, boolean left) {
        StringBuilder sbr = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (left) {
                sbr.append(output_placeholder_left_char);
            } else {
                sbr.append(output_placeholder_right_char);
            }
        }
        return sbr.toString();
    }


    public static boolean isMobile(String mobile) {
        String regex = "(\\+\\d+)?1[3458]\\d{9}$";
        return Pattern.matches(regex, mobile);
    }


    public static String hidePhone(String phone) {
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    public static String formatPhone(String phone) {
        String regex = "(1\\w{2})(\\w{4})(\\w{4})";
        return phone.replaceAll(regex, "$1 $2 $3");
    }


    public static String formatBankCard(String idCard) {
        String regex = "^(\\w+)(\\w{4})$";
        return idCard.replaceAll(regex, "**** **** ****$2");
    }


    /**
     * 移除特殊字符
     *
     * @param text
     * @return
     */
    public static String clearSpecialChars(String text) {
        String regs = "([^\\u4e00-\\u9fa5\\w\\(\\)（）])+?";
        Pattern pattern = Pattern.compile(regs);

        Matcher matcher = pattern.matcher(text);

        return matcher.replaceAll("");
    }

    /**
     * 验证 是否是含有 数字和字母
     *
     * @param str
     * @return
     */
    public static boolean isLetterDigit(String str) {
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }


    /**
     * 生成随机手机号
     *
     * @param start
     * @param end
     * @return
     */
    private static int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    public static String generateTel() {
        String[] telFirst = "134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");
        int index = getNum(0, telFirst.length - 1);
        String first = telFirst[index];
        String second = String.valueOf(getNum(1, 888) + 10000).substring(1);
        String third = String.valueOf(getNum(1, 9100) + 10000).substring(1);
        return first + second + third;
    }


    public static List<String> getPhone(String content) {

        String regex = "^[1](([3][0-9])|([4][5-9])|([5][0-3,5-9])|([6][5,6])|([7][0-8])|([8][0-9])|([9][1,8,9]))" +
                "[0-9]{8}$";
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?<!\\d)(?:(?:1[3456789]\\d{9})|(?:861[3456789]\\d{9}))(?!\\d)");
        Matcher matcher = pattern.matcher(content);
        StringBuffer bf = new StringBuffer(64);
        while (matcher.find()) {
            bf.append(matcher.group()).append(",");
        }
        int len = bf.length();
        if (len > 0) {
            bf.deleteCharAt(len - 1);
            String phone = bf.toString();
            String[] phoneSplit = phone.split(",");
            for (String telephone : phoneSplit) {
                boolean isTruePhone = Pattern.matches(regex, telephone);
                if (isTruePhone) {
                    result.add(telephone);
                }
            }
        }
        return result;
    }


    public static String getRandomJianHan(int len) {
        String randomName = "";
        for (int i = 0; i < len; i++) {
            String str = null;
            int hightPos, lowPos; // 定义高低位
            Random random = new Random();
            hightPos = (176 + Math.abs(random.nextInt(39))); // 获取高位值
            lowPos = (161 + Math.abs(random.nextInt(93))); // 获取低位值
            byte[] b = new byte[2];
            b[0] = (new Integer(hightPos).byteValue());
            b[1] = (new Integer(lowPos).byteValue());
            try {
                str = new String(b, "GBK"); // 转成中文
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            randomName += str;
        }
        return randomName;
    }


    /**
     * 比较一个时间与当前时间的相差多少秒
     *
     * @param dataStr 需要比较的时间，格式：yyyy-MM-dd HH:mm:ss
     * @return -1 比较失败，相差的秒数
     * @see
     */
    public final static long compareTime(String dataStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 转换成功的Date对象
            Date dt = sdf.parse(dataStr);
            // 这就是距离1970年1月1日0点0分0秒的毫秒数
            Long time = dt.getTime();
            // 与上面的相同,获取系统当前时间毫秒数
            Long time2 = System.currentTimeMillis();

            long s = (time2 - time) / 1000;
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * datetime
     * 当前日志+时间戳后2位+4位随机数
     *
     * @return 产生订单号
     */
    public final static String getOrderNum() {
        return getOrderNum(null);
    }

    public final static String getOrderNum(String prefix) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
        String time = simpleDateFormat.format(calendar.getTime()).toString();
        time += String.valueOf(System.currentTimeMillis()).substring(10, 12) + RandomUtil.randomNumbers(4);
        return StringUtils.isNotEmpty(prefix) ? prefix + time : time;
    }


    /**
     * taskid_hashcode
     * qrcode_hashcode
     * 使用关键字hashcode 和随机7位数生成编号
     *
     * @param prefix 单号前缀
     * @param key    关键字
     * @return 18503392100
     */
    public final static String getOrderNumHashCode(String prefix, String key, String dateformat) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        String code = MyUtils.currentTime(dateformat);
        code += String.valueOf(Math.abs(key.hashCode()));
        String no = code + RandomUtil.randomNumbers(5);
        return StringUtils.isNotEmpty(prefix) ? prefix + no : no;
    }


    /**
     * 获取当前网络ip
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ipAddress = inet.getHostAddress();
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        // "***.***.***.***".length()
        if (ipAddress != null && ipAddress.length() > 15) {
            // = 15
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }


    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }


    /**
     * 截取数字小数点，不四舍五入
     *
     * @param arg
     * @param decimal
     * @return
     */
    public static double keepDecimal(Object arg, int decimal) {
        String temp = arg.toString();
        if (temp.contains(".")) {
            int inx = temp.indexOf(".") + 1;
            if (temp.length() - inx > decimal) {
                temp = temp.substring(0, inx + decimal);
            }
        }
        return Double.valueOf(temp);
    }

    public static double keep2Decimal(double number) {
        DecimalFormat format = new DecimalFormat("0.##");
        //未保留小数的舍弃规则，RoundingMode.FLOOR表示直接舍弃。
        format.setRoundingMode(RoundingMode.FLOOR);
        return Double.parseDouble(format.format(number));
    }


    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    public static boolean isNotNull(Object... args) {
        boolean pass = true;
        for (Object arg : args) {
            if (arg == null) {
                pass = false;
                break;
            }
        }
        return pass;
    }

    /**
     * 随机获取动漫头像
     *
     * @return
     */
    public static String genHeadImg() {
        int random = RandomUtil.randomInt(1, 3);
        switch (random) {
            case 1:
                return genHeadImg1();
            case 2:
                return genHeadImg2();
        }
        return null;
    }

    public static String genHeadImg2() {
        HttpResponse res = HttpRequest.get("https://api.66mz8.com/api/rand.pic" +
                ".php?type=%E5%8A%A8%E6%BC%AB&return=json").execute();
        if (StringUtils.isNotEmpty(res.body())) {
            if (JSONUtil.isJson(res.body())) {
                JSONObject headRes = JSON.parseObject(res.body());
                if ("200".equals(headRes.getString("code"))) {
                    return headRes.getString("imgurl");
                }
            }
            return null;
        }
        return null;
    }


    public static String genHeadImg1() {
        HttpResponse res = HttpRequest.get("https://api.uomg.com/api/rand.avatar?format=json").execute();
        if (StringUtils.isNotEmpty(res.body())) {
            if (JSONUtil.isJson(res.body())) {
                JSONObject headRes = JSON.parseObject(res.body());
                if ("1".equals(headRes.getString("code"))) {
                    return headRes.getString("imgurl");
                }
            }
            return null;
        }
        return null;
    }
}
