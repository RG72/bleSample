package ru.tinytalk.ble18;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import ru.tinytalk.ble18.bleData;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import java.net.URISyntaxException;

public class MainActivity extends ActionBarActivity {
    private String serverAddress="http://217.116.59.46:3008";
    private BluetoothAdapter mBluetoothAdapter;
    private final String TAG = "Ble18";
    private boolean scaning = false;
    private Button startScan;
    private Button stopScan;
    private TextView textView;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gson = new Gson();

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        textView = (TextView) findViewById(R.id.textView);
        startScan = (Button) findViewById(R.id.startScan);
        stopScan = (Button) findViewById(R.id.stopScan);
        startScan.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ensures Bluetooth is available on the device and it is enabled. If not,
                // displays a dialog requesting user permission to enable Bluetooth.
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    //TODO process a answer
                    startActivityForResult(enableBtIntent, 1);
                    return;
                }

                mBluetoothAdapter.startLeScan(mLeScanCallback);
                changeState(true);
                mSocket.connect();
            }
        });

        stopScan.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Stop");
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                changeState(false);
                mSocket.disconnect();
            }
        });

        changeState(scaning);
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            bleData bData = new bleData();

            bData.mac=device.getAddress();
            bData.scanRecord=scanRecord;
            bData.rssi=rssi;

            mSocket.emit("bleData",gson.toJson(bData));

            String bleText="Mac:" + bData.mac + " rssi:"+String.valueOf(rssi);
            Log.i(TAG, "Found " + bleText);
            textView.setText(bleText);

            // scanRecord struct at
            // https://support.kontakt.io/hc/en-gb/articles/201492492-iBeacon-advertising-packet-structure
        }
    };

    private Socket mSocket;
    {
        try {

            mSocket = IO.socket(serverAddress);
        } catch (URISyntaxException e) {}
    }


    private void changeState(boolean newState){
        scaning=newState;
        startScan.setEnabled(!scaning);
        stopScan.setEnabled(scaning);
        //textView.setText((scaning) ? "Scaning ..." : "Press start for begin" );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
