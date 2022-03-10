package com.allinpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main {
    private static CloseableHttpClient httpClient=null;
    public static ArrayList<String> filter(String response){
        //SimplePropertyPreFilter 过滤器
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        filter.getIncludes().add("userApikey");
        JSONObject jsonObject = JSONObject.parseObject(response);
        /*String str = JSON.toJSONString(jsonObject, filter);*/
        String userApikey=jsonObject.getString("userApikey");
        String flagCode=jsonObject.getString("flagCode");
        System.out.println(userApikey);
        return new ArrayList<>(Arrays.asList(userApikey,flagCode));
        //返回数组列表
    }
//    public static void SendTemplateMsg(String sensorName,String isLine,String value,String updateDateTime) throws WxErrorException {
//        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
//                .toUser("o9YEK69-1hBUw1SzsGG0JNpVL9BA")
//                .templateId("c01ahO7ioMFQZdpYwMn0y2tlCJ3e0A_ryDBmnr18fAQ")
//                .url("")
//                .build();
//        templateMessage.addData(new WxMpTemplateData("first",sensorName));
//        templateMessage.addData(new WxMpTemplateData("keyword1",updateDateTime));
//        templateMessage.addData(new WxMpTemplateData("keyword2",value));
//        templateMessage.addData(new WxMpTemplateData("keyword3",isLine));
//        templateMessage.addData(new WxMpTemplateData("remark", ""));
//        wxService.getTemplateMsgService().sendTemplateMsg(templateMessage);
//    }
    public static void filter2(String response)  {
        JSONObject object=JSONObject.parseObject(response);
        JSONArray arr=object.getJSONArray("deviceList");
        JSONObject object1=(JSONObject)arr.get(0);
        JSONArray jsonArray=object1.getJSONArray("sensorList");
        Iterator<Object> iterator=jsonArray.iterator();
        //List<JSONObject> list=new ArrayList<JSONObject>();
        String token=getToken();
        while (iterator.hasNext()){
            JSONObject jsonObject=(JSONObject) iterator.next();
            String sensorName=jsonObject.getString("sensorName");
            String isLine=jsonObject.getString("isLine");
            String updateDateTime= jsonObject.getString("updateDateTime");
            String value=jsonObject.getString("value");
            System.out.println(sensorName+"状态"+isLine+"传感器数值"+value+"更新时间"+updateDateTime);
//            SendTemplateMsg(sensorName,isLine,updateDateTime,value);
            SendWeChatMsg(token,sensorName,isLine,updateDateTime,value);
        }
        /*for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object2=(JSONObject)jsonArray.get(i);
            System.out.println(object2);
        }*/


        /*System.out.println(jsonArray);
        System.out.println(object1);
        System.out.println(arr.get(0).getClass());
        System.out.println(jsonArray.getClass());*/
    }
    public static String doPost(String url, JSONObject json){
        try{
            if (httpClient==null){
                httpClient= HttpClientBuilder.create().build();

            }
            HttpPost post=new HttpPost(url);
            StringEntity s=new StringEntity(json.toString());
            //发送json数据需要设置contentType
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");
            //设置请求参数
            post.setEntity(s);
            HttpResponse response= httpClient.execute(post);
            //返回json格式
            if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
                String res= EntityUtils.toString(response.getEntity(),"UTF-8");
                return res;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void tset(String username,String password){
        JSONObject object=new JSONObject();
        object.put("userName",username);
        object.put("password",password);
        try{
            String response=doPost("https://api.dtuip.com/qy/user/login.html",object);
            System.out.println(response);
            ArrayList<String> id=filter(response);
            test2(id.get(0),"",id.get(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void test2(String userApikey,String deviceNo,String flagCode){
        JSONObject object=new JSONObject();
        object.put("userApiKey",userApikey);
        object.put("deviceNo",deviceNo);
        object.put("flagCode",flagCode);
        try{
            String response=doPost("https://api.dtuip.com/qy/device/queryDevMoniData.html",object);

            System.out.println(response);
            filter2(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getToken() {
        // 授予形式
        String grant_type = "client_credential";
        //应用ID
        String appid = "wx3c6dcd27e8515677";
        //密钥
        String secret = "a5066fca3b242a01be22f97cfe89ca29";
        // 接口地址拼接参数
        String getTokenApi = "https://api.weixin.qq.com/cgi-bin/token?grant_type=" + grant_type + "&appid=" + appid
                + "&secret=" + secret;
        String tokenJsonStr = doGetPost(getTokenApi, "GET", null);
        JSONObject tokenJson = JSONObject.parseObject(tokenJsonStr);
        String token = tokenJson.get("access_token").toString();
        System.out.println("获取到的TOKEN : " + token);
        return token;
    }
    /***
     * 发送消息
     *
     * @param token
     */
    public static void SendWeChatMsg(String token,String sensorName,String isLine,String updateDateTime,String value) {
        // 接口地址
        String sendMsgApi = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="+token;
        //openId
        String toUser = "o9YEK69-1hBUw1SzsGG0JNpVL9BA";
        //消息模板ID
        String template_id = "853ml7BOSDAEGJAXSvw0qSRfKLLwEHx0re1Lzk6DpVA";
        //整体参数map
        Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
        //消息主题显示相关map
        Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
        Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
        Map<String, Object> valueMap1 = new LinkedHashMap<String, Object>();
        Map<String, Object> valueMap2 = new LinkedHashMap<String, Object>();
        Map<String, Object> valueMap3 = new LinkedHashMap<String, Object>();
        Map<String, Object> valueMap4 = new LinkedHashMap<String, Object>();

        //根据自己的模板定义内容和颜色
        valueMap.put("value",sensorName);
        dataMap.put("first",valueMap);
        valueMap1.put("value",value);
        dataMap.put("keyword1",valueMap1);
        valueMap2.put("value",isLine);
        dataMap.put("keyword2",valueMap2);
        valueMap3.put("value","");
        dataMap.put("keyword3",valueMap3);
        valueMap4.put("value",updateDateTime);
        dataMap.put("remark",valueMap4);
        paramMap.put("touser", toUser);
        paramMap.put("template_id", template_id);
        paramMap.put("data", dataMap);
        System.out.println(doGetPost(sendMsgApi,"POST",paramMap));
    }
    /**
     * 调用接口 post
     * @param apiPath
     */
    public static String doGetPost(String apiPath, String type, Map<String, Object> paramMap){
        OutputStreamWriter out = null;
        InputStream is = null;
        String result = null;
        try{
            URL url = new URL(apiPath);// 创建连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod(type) ; // 设置请求方式
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            connection.connect();
            if(type.equals("POST")){
                out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
                JSONObject jsonObject=new JSONObject(paramMap);
                out.append(jsonObject.toString());
                System.out.println(jsonObject);
                out.flush();
                out.close();
            }
            // 读取响应
            is = connection.getInputStream();
            int length = (int) connection.getContentLength();// 获取长度
            if (length != -1) {
                byte[] data = new byte[length];
                byte[] temp = new byte[512];
                int readLen = 0;
                int destPos = 0;
                while ((readLen = is.read(temp)) > 0) {
                    System.arraycopy(temp, 0, data, destPos, readLen);
                    destPos += readLen;
                }
                result = new String(data, "UTF-8"); // utf-8编码
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  result;
    }




    public static void main(String[] args) {

        tset("18170994373","allinpay01");
    }
}
