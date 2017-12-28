package tw.cguee.b0321226.bridge;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class WifiTcpService extends Service {
    public String serverHttp;

    public WifiTcpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("程序", "進入WiFi-TCP Service");

        serverHttp = intent.getStringExtra("serverHttp");

        Log.i("程序", "收到連結HTTP: " + serverHttp);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ProtoPieUtils.PROTOPIE_RECEIVE_ACTION);
        registerReceiver(broadcastReceiver, filter);

        Vibrator vb = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vb.vibrate(300);

        Toast.makeText(WifiTcpService.this, "啟動完成", Toast.LENGTH_SHORT).show();

        WifiTcp.sendToConsole(WifiTcpService.this, "啟動完成");

        Log.i("程序", "完成啟動WiFi-Tcp Service");

        return super.onStartCommand(intent, flags, startId);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("程序", "Broadcast接收自: " + intent.getAction());

            if (intent.getAction().equals(ProtoPieUtils.PROTOPIE_RECEIVE_ACTION)) {
                String sendArduino = intent.getStringExtra("messageId");

                Log.i("程序", "接收自ProtoPie訊息: " + sendArduino);

                WifiTcp.sendToConsole(WifiTcpService.this, sendArduino);
            }
        }
    };
}