package tw.cguee.b0321226.bridge;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class WifiUdpService extends Service {
    public String serverIp;
    public int txPort, rxPort;

    Thread thread;
    Handler mHandler;

    public InetAddress serverAddress, receiveIp;
    DatagramSocket txSocket = null, rxSocket = null;
    DatagramPacket txPacket, rxPacket;
    public byte[] txBuf, rxBuf;
    public String rxMessage;

    public WifiUdpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("程序", "進入WiFi-UDP Service");

        serverIp = intent.getStringExtra("serverIp");
        txPort = intent.getIntExtra("txPort", 0);
        rxPort = intent.getIntExtra("rxPort", 0);

        Log.i("程序", "收到連結IP: " + serverIp);
        Log.i("程序", "收到傳送Port: " + txPort);
        Log.i("程序", "收到接收Port: " + rxPort);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ProtoPieUtils.PROTOPIE_RECEIVE_ACTION);
        filter.addAction("wifi.udp.arduino.to.protopie");
        registerReceiver(broadcastReceiver, filter);

        try {
            serverAddress = InetAddress.getByName(serverIp);
            txSocket = new DatagramSocket(txPort);
            rxSocket = new DatagramSocket(rxPort);
        } catch (UnknownHostException e) {
            Log.e("錯誤", "連結IP錯誤");
        } catch (SocketException e) {
            Log.e("錯誤", "Port錯誤");
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        Intent itSendProtopie = new Intent();
                        itSendProtopie.putExtra("rxMessage", rxMessage);
                        itSendProtopie.setAction("wifi.udp.arduino.to.protopie");
                        sendBroadcast(itSendProtopie);

                        WifiUdp.sendToConsole(WifiUdpService.this, "Arduino傳送給ProtoPie: " + rxMessage);

                        Log.i("程序", "傳送給ProtoPie訊息: " + rxMessage);
                        break;
                    default:
                        break;
                }
            }
        };

        thread = new Thread(new Runnable() {
            public void run() {
                Log.i("程序", "啟動接收Thread");

                while (true) {
                    rxBuf = new byte[1];
                    rxPacket = new DatagramPacket(rxBuf, rxBuf.length, serverAddress, rxPort);
                    try {
                        rxSocket.receive(rxPacket);
                    } catch (IOException e) {
                        Log.e("錯誤", "接收Socket失敗");
                    }

                    rxMessage = new String(rxPacket.getData());
                    receiveIp = rxPacket.getAddress();

                    Log.i("程序", "接收自Arduino訊息: " + rxMessage);
                    Log.i("程序", "來自IP: " + receiveIp);

                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                }
            }
        });

        thread.start();

        Vibrator vb = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vb.vibrate(300);

        Toast.makeText(WifiUdpService.this, "啟動完成", Toast.LENGTH_SHORT).show();

        WifiUdp.sendToConsole(WifiUdpService.this, "啟動完成");

        Log.i("程序", "完成啟動WiFi-UDP Service");

        return super.onStartCommand(intent, flags, startId);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("程序", "Broadcast接收自: " + intent.getAction());

            if (intent.getAction().equals("wifi.udp.arduino.to.protopie")) {
                String sendProtopie = intent.getStringExtra("rxMessage");

                ProtoPieUtils.sendToProtoPie(WifiUdpService.this, sendProtopie);
            } else if (intent.getAction().equals(ProtoPieUtils.PROTOPIE_RECEIVE_ACTION)) {
                String sendArduino = intent.getStringExtra("messageId");

                Log.i("程序", "接收自ProtoPie訊息: " + sendArduino);

                txBuf = sendArduino.getBytes();
                txPacket = new DatagramPacket(txBuf, txBuf.length, serverAddress, txPort);
                try {
                    txSocket.send(txPacket);

                    WifiUdp.sendToConsole(WifiUdpService.this, "ProtoPie傳送給Arduino: " + sendArduino);

                    Log.i("程序", "傳送給Arduino訊息: " + sendArduino);
                } catch (IOException e) {
                    Log.e("錯誤", "傳送Socket失敗");
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        thread.interrupt();

        Log.i("程序", "關閉接收Thread");
    }
}