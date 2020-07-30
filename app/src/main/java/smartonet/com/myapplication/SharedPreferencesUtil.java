package smartonet.com.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by hanzh on 2018/3/20.
 * key为设备code的为最近一次与设备通信的ack
 * key为设备code+new的为新申请的ACK
 * key为设备code+token的为最近一次与设备通信的token
 */

public class SharedPreferencesUtil {

    /**
     * 获取本地xml中指定键的值
     * @param context
     * @param key
     * @return
     */
    public static String getString(Context context, String key){
        SharedPreferences pref = context.getSharedPreferences("SmartoNet",MODE_PRIVATE);
        return pref.getString(key,"");
    }

    /**
     * 保存键值对
     * @param context
     * @param key
     * @return
     */
    public static void setString(Context context, String key, String value){
        SharedPreferences pref = context.getSharedPreferences("SmartoNet",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key,value);
        editor.commit();
    }

    /**
     * 删除指定的键值对
     * @param context
     * @param key
     */
    public static void deleteString(Context context, String key){
        SharedPreferences pref = context.getSharedPreferences("SmartoNet",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.commit();
    }
}
