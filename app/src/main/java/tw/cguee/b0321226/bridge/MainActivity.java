package tw.cguee.b0321226.bridge;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnWifiUdp, btnWifiTcp, btnBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("程序", "進入主畫面");

        btnWifiUdp = findViewById(R.id.btnWifiUdp);
        btnWifiTcp = findViewById(R.id.btnWifiTcp);
        btnBluetooth = findViewById(R.id.btnBluetooth);

        btnWifiUdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent itWifiUdp = new Intent(MainActivity.this, WifiUdp.class);
                startActivity(itWifiUdp);
                finish();
            }
        });

        btnWifiTcp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent itWifiTcp = new Intent(MainActivity.this, WifiTcp.class);
                startActivity(itWifiTcp);
                finish();
            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "尚未啟用", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
    }
}