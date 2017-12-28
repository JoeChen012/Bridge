package tw.cguee.b0321226.bridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

public class WifiTcp extends AppCompatActivity implements View.OnClickListener {
    private EditText edtHttp;
    private Button btnSet;
    private TextView txvHttp, console;
    private List<String> consoleLines = new LinkedList<>();
    private StringBuilder consoleText = new StringBuilder();
    private WebView webView;

    private static final String ACTION_CONSOLE = "Wifi.Tcp.Console";

    public String serverHttp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_tcp);

        Log.i("程序", "進入WiFi-TCP");

        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_CONSOLE));

        edtHttp = findViewById(R.id.edtHttp);
        btnSet = findViewById(R.id.btnSet);
        txvHttp = findViewById(R.id.txvHttp);
        console = findViewById(R.id.console);

        console.setMovementMethod(new ScrollingMovementMethod());

        btnSet.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (edtHttp.length() == 0) {
            txvHttp.setText("連結HTTP: 請輸入連結HTTP");

            Toast.makeText(WifiTcp.this, "請先完成設定", Toast.LENGTH_SHORT).show();
        } else {
            serverHttp = edtHttp.getText().toString();
            txvHttp.setText("連結HTTP: " + serverHttp);

            Intent itWifiTcpService = new Intent(WifiTcp.this, WifiTcpService.class);
            itWifiTcpService.putExtra("serverHttp", serverHttp);
            startService(itWifiTcpService);
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
                if (message.equals("啟動完成")) {
                    writeToConsole(message);
                } else if (message != null) {
                    webView = findViewById(R.id.webView);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.setWebViewClient(new InsideWebViewClient());
                    webView.loadUrl("http://" + serverHttp + "/" + message);

                    writeToConsole("ProtoPie傳送給Arduino: " + message);

                    Log.i("程序", "傳送給Arduino訊息: " + message);
                }
            }
        }
    };

    class InsideWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(WifiTcp.this, WifiTcpService.class));

        Log.i("程序", "關閉WiFi-TCP Service");
    }
}