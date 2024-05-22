package com.fang.myapplication;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "AIS-RAOP-Main";

    private AirPlayServer mAirPlayServer;
    private RaopServer mRaopServer;
    private DNSNotify mDNSNotify;

    private SurfaceView mSurfaceView;
    private Button mBtnControl;
    private TextView mTxtDevice;
    private boolean mIsStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSystemService(Context.NSD_SERVICE);

        mBtnControl = findViewById(R.id.btn_control);
        mTxtDevice = findViewById(R.id.txt_device);
        mSurfaceView = findViewById(R.id.surface);

        mBtnControl.setOnClickListener(this);

        mAirPlayServer = new AirPlayServer();
        mRaopServer = new RaopServer(mSurfaceView);
        mDNSNotify = new DNSNotify();

        // Set focus initially to the control button
        mBtnControl.requestFocus();

        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] mediaCodecInfos = mediaCodecList.getCodecInfos();
        for (MediaCodecInfo codecInfo : mediaCodecInfos) {
            if (!codecInfo.isEncoder()) {
                Log.d(TAG, "codec= " + codecInfo.getName() +
                        "\nis_encoder=" + codecInfo.isEncoder() +
                        "\nis_vendor=" + codecInfo.isVendor() +
                        "\nhw_acc=" + codecInfo.isHardwareAccelerated() +
                        "\nsw_acc=" + codecInfo.isSoftwareOnly());
                String[] types = codecInfo.getSupportedTypes();
                Log.d(TAG, "supported codec = " + String.join(", ", types));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_control) {
            if (!mIsStart) {
                startServer();
                mTxtDevice.setText("Device name: " + mDNSNotify.getDeviceName());
            } else {
                stopServer();
                mTxtDevice.setText("have not started");
            }
            mIsStart = !mIsStart;
            mBtnControl.setText(mIsStart ? "End" : "Start");
        }
    }

    private void startServer() {
        mDNSNotify.changeDeviceName();
        mAirPlayServer.startServer();
        int airplayPort = mAirPlayServer.getPort();
        if (airplayPort == 0) {
            Toast.makeText(this.getApplicationContext(), "Start the AirPlay service failed", Toast.LENGTH_SHORT).show();
        } else {
            mDNSNotify.registerAirplay(airplayPort);
        }
        mRaopServer.startServer();
        int raopPort = mRaopServer.getPort();
        if (raopPort == 0) {
            Toast.makeText(this.getApplicationContext(), "Start the RAOP service failed", Toast.LENGTH_SHORT).show();
        } else {
            mDNSNotify.registerRaop(raopPort);
        }
        Log.d(TAG, "airplayPort = " + airplayPort + ", raopPort = " + raopPort);
    }

    private void stopServer() {
        mDNSNotify.stop();
        mAirPlayServer.stopServer();
        mRaopServer.stopServer();
    }
}
