package com.example.embedded_app;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "AiotMqtt";
    /* 设备三元组信息 */
    final private String PRODUCTKEY = "a11xsrWmW14";
    final private String DEVICENAME = "97522292";
    final private String DEVICESECRET = "kYLB2MaR";

    /* 自动Topic, 用于上报消息 */
    final private String PUB_TOPIC = "97522292/angles";
    final private String SUB_TOPIC = "97522292/newone2";

    /* 阿里云Mqtt服务器域名 */
    final String host = "tcp://45.149.77.235:1883";
    private String clientId;
    private String userName;
    private String passWord;

    MqttAndroidClient mqttAndroidClient;


    RecyclerView mRecyclerView;
    StarAdapter mStarAdapter;
    ArrayList<Star> mStarsList;

    public static EditText motor1;
    public static EditText motor2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AiotMqttOption aiotMqttOption = new AiotMqttOption().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET);
        if (aiotMqttOption == null) {
            Log.e(TAG, "device info error");
        } else {
            clientId = aiotMqttOption.getClientId();
            userName = aiotMqttOption.getUsername();
            passWord = aiotMqttOption.getPassword();
        }

        /* 创建MqttConnectOptions对象并配置username和password */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName("97522292");
        mqttConnectOptions.setPassword("kYLB2MaR".toCharArray());


        /* 创建MqttAndroidClient对象, 并设置回调接口 */
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "msg delivered");
            }
        });

        /* Mqtt建连 */
        try {
            mqttAndroidClient.connect(mqttConnectOptions,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "connect succeed");

                    subscribeTopic(SUB_TOPIC);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "connect failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }



        mStarsList = new ArrayList<>();
        mRecyclerView = findViewById( R.id.StarRecyclerView);
        mStarAdapter = new StarAdapter(mStarsList, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mStarAdapter);

        motor1 = findViewById(R.id.editText1);
        motor2 = findViewById(R.id.editText2);

        AssetManager am = this.getAssets();
        try {
            InputStream is = am.open("hygfull.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null){
                try {
                    Log.i("DDIIMMOOO", line);
                    String[] splited = line.split(",");
                    int starid = Integer.parseInt(splited[0]);
                    float ra = Float.parseFloat(splited[1]);
                    float dec = Float.parseFloat(splited[2]);
                    String spectrum = splited[3];
                    Star s = new Star(starid, ra, dec, spectrum);
                    mStarsList.add(s);
                    mStarAdapter.notifyDataSetChanged();
                } catch (Exception e){}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        AndroidNetworking.initialize(getApplicationContext());

        ((Button) findViewById(R.id.send_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float a1 = Float.parseFloat(motor1.getText().toString());
                float a2 = Float.parseFloat(motor2.getText().toString());

                int val1 = Math.round(a1);
                int val2 = Math.round(a2);

                try {
                    String jsonString = new JSONObject()
                            .put("motor1", val1+"")
                            .put("motor2", val2+"")
                            .toString();
                    publishMessage(jsonString);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                AndroidNetworking.get("http://192.168.4.1/get")
                        .addQueryParameter("motor1", val1+"")
                        .addQueryParameter("motor2", val2+"")
                        .build()
                        .getAsString(new StringRequestListener() {
                            @Override
                            public void onResponse(String response) {
                                String tmp = "Sent successful === > motor 1 : " + val1 + " motor2 : " + val2;
                                Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(ANError anError) {
                                Toast.makeText(getApplicationContext(), anError + "", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
    public static void SetTextBoxes(float s1, float s2){
        motor1.setText(s1+"");
        motor2.setText(s2+"");

        double todayJulian = toJulian();

        double[] res = raDecToAltAz(s1,s2,38.2464000,274.236400,todayJulian);
        motor1.setText(res[0]+"");
        motor2.setText(res[1]+"");
    }
    public static double toJulian() {
        Calendar today = Calendar.getInstance();
        int[] ymd = new int[]{today.get(Calendar.YEAR), today.get(Calendar.MONTH)+1,
                today.get(Calendar.DATE)};
        int JGREG= 15 + 31*(10+12*1582);
        double HALFSECOND = 0.5;

        int year=ymd[0];
        int month=ymd[1]; // jan=1, feb=2,...
        int day=ymd[2];
        int julianYear = year;
        if (year < 0) julianYear++;
        int julianMonth = month;
        if (month > 2) {
            julianMonth++;
        }
        else {
            julianYear--;
            julianMonth += 13;
        }

        double julian = (java.lang.Math.floor(365.25 * julianYear)
                + java.lang.Math.floor(30.6001*julianMonth) + day + 1720995.0);
        if (day + 31 * (month + 12 * year) >= JGREG) {
            // change over to Gregorian calendar
            int ja = (int)(0.01 * julianYear);
            julian += 2 - ja + (0.25 * ja);
        }
        return java.lang.Math.floor(julian);
    }

    public static double[] raDecToAltAz(float ra, float dec, double lat, double lon, double jd_ut){
        double gmst=greenwichMeanSiderealTime(jd_ut);
        double localSiderealTime=(gmst+lon)/(2*Math.PI);


        double H=(localSiderealTime - ra);
        if(H<0){H+=2*Math.PI;}
        if(H>Math.PI){H=H-2*Math.PI;}

        double az = (Math.atan2(Math.sin(H), Math.cos(H)*Math.sin(lat) - Math.tan(dec)*Math.cos(lat)));
        double a = (Math.asin(Math.sin(lat)*Math.sin(dec) + Math.cos(lat)*Math.cos(dec)*Math.cos(H)));
        az-=Math.PI;

        if(az<0){az+=2*Math.PI;}

        a*=45;
        az*=45;
        if(a<0)
            a*=-1;
        if(az<0)
            az*=-1;

        a=a%180;
        az=az%180;

        double[] res = new double[2];

        res[0] = az;
        res[1] = a;
        return res;
    }
    public static double greenwichMeanSiderealTime(double jd){
        //"Expressions for IAU 2000 precession quantities" N. Capitaine1,P.T.Wallace2, and J. Chapront
        double t = ((jd - 2451545.0)) / 36525.0;

        double gmst=earthRotationAngle(jd)+(0.014506 + 4612.156534*t + 1.3915817*t*t - 0.00000044 *t*t*t - 0.000029956*t*t*t*t - 0.0000000368*t*t*t*t*t)/60.0/60.0*Math.PI/180.0;  //eq 42
        gmst/=2*Math.PI;
        if(gmst<0) gmst+=2*Math.PI;
        return gmst;
    }

    public static double earthRotationAngle(double jd){
        //IERS Technical Note No. 32

        double t = jd- 2451545.0;
        double f = jd/1.0;

        double theta = 2*Math.PI * (f + 0.7790572732640 + 0.00273781191135448 * t); //eq 14
        theta%=2*Math.PI;
        if(theta<0)
            theta+=2*Math.PI;

        return theta;

    }
    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "subscribed succeed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String payload) {
        try {
            if (mqttAndroidClient.isConnected() == false) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(PUB_TOPIC, message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "publish succeed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }


}