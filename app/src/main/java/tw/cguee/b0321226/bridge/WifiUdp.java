package tw.cguee.b0321226.bridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

public class WifiUdp extends AppCompatActivity implements View.OnClickListener {
    private EditText edtIp, edtTxPort, edtRxPort;
    private Button btnSet;
    private TextView txvIp, txvTxPort, txvRxPort, console;
    private List<String> consoleLines = new LinkedList<>();
    private StringBuilder consoleText = new StringBuilder();

    private static final String ACTION_CONSOLE = "Wifi.Udp.Console";

    public String serverIp;
    public int txPort, rxPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_udp);

        Log.i("程序", "進入WiFi-UDP");

        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_CONSOLE));

        edtIp = findViewById(R.id.edtIp);
        edtTxPort = findViewById(R.id.edtTxPort);
        edtRxPort = findViewById(R.id.edtRxPort);
        btnSet = findViewById(R.id.btnSet);
        txvIp = findViewById(R.id.txvIp);
        txvTxPort = findViewById(R.id.txvTxPort);
        txvRxPort = findViewById(R.id.txvRxPort);
        console = findViewById(R.id.console);

        console.setMovementMethod(new ScrollingMovementMethod());

        edtTxPort.setText("2390");
        edtRxPort.setText("2490");

        btnSet.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (edtIp.length() == 0) {
            txvIp.setText("連結IP: 請輸入連結IP");
        } else {
            serverIp = edtIp.getText().toString();
            txvIp.setText("連結IP: " + serverIp);
        }

        if (edtTxPort.length() == 0) {
            txvTxPort.setText("傳送Port: 請輸入傳送Port");
        } else {
            txPort = Integer.parseInt(edtTxPort.getText().toString());
            txvTxPort.setText("傳送Port: " + txPort);
        }

        if (edtRxPort.length() == 0) {
            txvRxPort.setText("接收Port: 請輸入接收Port");
        } else {
            rxPort = Integer.parseInt(edtRxPort.getText().toString());
            txvRxPort.setText("接收Port: " + rxPort);
        }

        if (edtIp.length() != 0 && edtTxPort.length() != 0 && edtRxPort.length() != 0) {
            Intent itWifiUdpService = new Intent(WifiUdp.this, WifiUdpService.class);
            itWifiUdpService.putExtra("serverIp", serverIp);
            itWifiUdpService.putExtra("txPort", txPort);
            itWifiUdpService.putExtra("rxPort", rxPort);
            startService(itWifiUdpService);
        } else {
            Toast.makeText(WifiUdp.this, "請先完成設定", Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendToConsole(Context context, String message) {
        Intent intent = new Intent(ACTION_CONSOLE);
        intent.putExtra("message", message);
        context.sendBroadcast(intent);
    }

    private void writeToConsole(String message) {
        consoleLines.add(message);

        while (consoleLines.size() > 10) {
            consoleLines.remove(0);
        }

        consoleText.delete(0, consoleText.length());

        for (int i = 0; i < consoleLines.size(); i++) {
            if (i > 0) {
                consoleText.append("\n");
            }
            consoleText.append(consoleLines.get(i));
        }

        console.setText(consoleText);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CONSOLE.equals(intent.getAction())) {
                String message = intent.getStringExtra("message");
                if (message != null) {
                    writeToConsole(message);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(WifiUdp.this, WifiUdpService.class));

        Log.i("程序", "關閉WiFi-UDP Service");
    }
}