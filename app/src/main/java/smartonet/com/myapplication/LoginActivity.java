package smartonet.com.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;
import static org.apache.http.params.CoreConnectionPNames.CONNECTION_TIMEOUT;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    // UI references.
    private AutoCompleteTextView mEmailView,showtext;
    private View mProgressView;
    private View mLoginFormView;
    public static final String TAG = "test";
    MqttClient mClient, mClient2;
    Handler mHandler;
    MqttConnectOptions mOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        showtext = (AutoCompleteTextView) findViewById(R.id.showtext);
        Button connection = (Button) findViewById(R.id.email_sign_in_button);
        Button send = (Button) findViewById(R.id.send);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mOptions = initMqttConnectionOptions();
        connection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //serverURL是你的消息服务器的地址，clientid是单独的唯一的标识，让服务器能定位到你，mqttcallback消息接收回调方法，后边的数组是你要订阅的主题列表
                mClient = initClient("serverURL", "id（唯一）", mqttCallback, mOptions, new String[]{"HelloWorld"});
                mClient2=initClient("serverURL","id（唯一）",mqttCallback,mOptions,new String[]{"HelloWorld"});
            }
        });
        send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("asd", ":asd22222222222222222");
                sendMessage(mClient2,mEmailView.getText().toString(),1,false,"HelloWorld");
            }
        });
        mHandler = new MyHandler(LoginActivity.this,showtext);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * 初始化mqq连接设置
     * @return
     */
    private MqttConnectOptions initMqttConnectionOptions() {
        MqttConnectOptions mOptions = new MqttConnectOptions();
        mOptions.setAutomaticReconnect(false);//断开后，是否自动连接
        mOptions.setCleanSession(true);//是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
        mOptions.setConnectionTimeout(60);//设置超时时间，单位为秒
        mOptions.setUserName("admin");//设置用户名。跟Client ID不同。用户名可以看做权限等级
        mOptions.setPassword("admin".toCharArray());//设置登录密码
        mOptions.setKeepAliveInterval(60);//心跳时间，单位为秒。即多长时间确认一次Client端是否在线
        mOptions.setMaxInflight(10);//允许同时发送几条消息（未收到broker确认信息）
        mOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);//选择MQTT版本
        return mOptions;
    }

    /**
     * 回调方法
     */
    MqttCallbackExtended mqttCallback = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Log.i(TAG, "connect Complete" + Thread.currentThread().getId());
            Log.w("这么长时间了可算折腾好了","，鼓励一下自己");
        }

        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG, "connection Lost ");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

//            if (topic.equalsIgnoreCase("subscribe topic1")) { //判断是哪个服务发送过来的数据
                Log.w("接到消息了","接到消息了");
                Log.i(TAG, "messageArrived: " + new String(message.getPayload()));
                Message msg = new Message();
                Bundle bindle = new Bundle();
                bindle.putString("Content", new String(message.getPayload()));
                msg.what = 1;
                msg.setData(bindle);
                mHandler.sendMessage(msg);
            }
//        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(TAG, "delivery Complete ");//即服务器成功delivery消息
        }
    };

    /**
     * 初始化客户端连接
     * @param serverURI
     * @param clientId
     * @param callback
     * @param options
     * @param subscribeTopics
     * @return
     */
    private MqttClient initClient(String serverURI, String clientId, MqttCallback callback, MqttConnectOptions options, String[] subscribeTopics) {
        MqttClient client = null;
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient(serverURI, clientId, persistence);
            client.setCallback(callback);//设置回调函数
            client.connect(options);//连接broker
            client.subscribe(subscribeTopics);//设置监听的topic
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return client;
    }

    /**
     * 发送消息
     */
    private void sendMessage(MqttClient mClient,String content,int qos,boolean retained,String topic) {
        MqttMessage msg = new MqttMessage();
        String msgStr = content;
        msg.setPayload(msgStr.getBytes());//设置消息内容
        msg.setQos(qos);//设置消息发送质量，可为0,1,2.
        msg.setRetained(retained);//服务器是否保存最后一条消息，若保存，client再次上线时，将再次受到上次发送的最后一条消息。
        try {
            mClient.publish(topic, msg);//设置消息的topic，并发送
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 确保类内部的handler不含有对外部类的隐式引用 。
     */
    static class MyHandler extends Handler {
        private Context mContext;
        AutoCompleteTextView showtext;
        MyHandler(Context mContext,AutoCompleteTextView showtext) {
            this.mContext = mContext;
            this.showtext=showtext;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showtext.setText(msg.getData().getString("Content"));
        }
    }
}

