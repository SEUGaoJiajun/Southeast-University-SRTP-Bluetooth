package exp.com.bluetoothtest;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;


public class MainActivity extends AppCompatActivity {
    private static final String serverUUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final String SERVICE_ADDRESS = "30:24:32:61:7F:50";
    private static final String TAG = "Bluetooth";

    private boolean scanCompleted;
    private TextView text;
//    private Button btn_start;
    private Button take_video;
//    private File currentImageFile = null;
    private File currentVideoFile = null;
    private BluetoothDevice service;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();

        scanCompleted = false;

        text = (TextView) findViewById(R.id.tv_msg);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void bindViews() {
//        btn_start = (Button) findViewById(R.id.btn_start);
        take_video = (Button) findViewById(R.id.take_video);

//        btn_start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                File dir = new File(Environment.getExternalStorageDirectory(),"pictures");
//                if(dir.exists()){
//                    dir.mkdirs();
//                }
//                currentImageFile = new File(dir, "image1.jpg");
//                if(!currentImageFile.exists()){
//                    try {
//                        currentImageFile.createNewFile();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
//                startActivityForResult(it, Activity.DEFAULT_KEYS_DIALER);
//            }
//        });

        take_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File dir = new File(Environment.getExternalStorageDirectory(),"pictures");
                if(dir.exists()){
                    dir.mkdirs();
                }
                currentVideoFile = new File(dir,"video1.mp4");
                if(!currentVideoFile.exists()){
                    try {
                        currentVideoFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Intent it = new Intent();
                it.setAction("android.media.action.VIDEO_CAPTURE");
                it.addCategory("android.intent.category.DEFAULT");
                it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentVideoFile));
                startActivityForResult(it, Activity.DEFAULT_KEYS_DIALER);
            }
        });
    }


    public void onOpen(View v) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            showMessage("开启蓝牙");
        } else {
            showMessage("蓝牙已开启");
        }
    }

    public void onCheck(View v) {
        if (bluetoothAdapter != null) {
            //获得已配对设备列表
            Set<BluetoothDevice> mySet = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : mySet) {
                if (device.getAddress().equals(SERVICE_ADDRESS)) {
                    service = device;
                    break;
                }
            }
            if (service != null) {
                showMessage("服务已得到");
                scanCompleted = true;
            } else {
                showMessage("服务未得到，搜索中");
                bluetoothAdapter.startDiscovery();
            }
        } else {
            showMessage("请先开启蓝牙!");
        }
    }

    private BluetoothSocket bluetoothSocket;

    public void onConnect(View v) {
        if (service != null && scanCompleted) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bluetoothSocket = service.createRfcommSocketToServiceRecord(UUID.fromString(serverUUID));
                        bluetoothSocket.connect();
                        showMessage("成功连接");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

//    public void onSend(View v) {
//        if (service != null && scanCompleted) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    OutputStream outputStream;
//                    try {
//                        outputStream = bluetoothSocket.getOutputStream();
//                        outputStream.write("A message from android device".getBytes());
//                        showMessage("Successfully send message");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    InputStream inputStream;
//                    try {
//                        inputStream = bluetoothSocket.getInputStream();
//                        byte[] buffer = new byte[200];
//                        inputStream.read(buffer);
//
//                        showMessage("Concurrently receive message : " + new String(buffer));
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
//    }

//    public void onImageSend(View v) {
//        if (service != null && scanCompleted) {
////            new Thread(new ClientThread(service)).start();
//            new Thread(new ImgClientThread()).start();
//
//        }
//    }
//
//    /**
//     * 该线程往蓝牙服务器端发送文件数据。
//     */
//    private class ImgClientThread extends Thread {
////        private BluetoothDevice device;
////
////        public ClientThread(BluetoothDevice device) {
////            this.device = device;
////        }
//
//        @Override
//        public void run() {
//              try {
//               // 开始往服务器端发送数据。
////                Log.d(TAG, "开始往蓝牙服务器发送数据...");
//                showMessage("start to send picture");
////                  Toast.makeText(getApplicationContext(), "开始往蓝牙服务器发送数据...", Toast.LENGTH_SHORT).show();
//                  sendDataToServer(bluetoothSocket);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        private void sendDataToServer(BluetoothSocket socket) {
//            try {
//                FileInputStream fis = new FileInputStream(getImgFile());
//                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
//
//                byte[] buffer = new byte[1024];
//                int c;
//                while (true) {
//                    c = fis.read(buffer);
//                    if (c == -1) {
////                    Log.d(TAG, "读取结束");
//                        showMessage("sending is over");
////                        Toast.makeText(getApplicationContext(), "读取结束", Toast.LENGTH_SHORT).show();
//                        break;
//                    } else {
//                        bos.write(buffer, 0, c);
//                    }
//                }
//
//                bos.flush();
//
//                fis.close();
//                bos.close();
//
////              Log.d(TAG, "发送文件成功");
//                showMessage("successfully sent it");
////                Toast.makeText(getApplicationContext(), "发送文件成功", Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    private File getImgFile() {
//        File root = new File(Environment.getExternalStorageDirectory(),"pictures");
//        File file = new File(root, "image1.jpg");
//        return file;
//    }

    public void onVideoSend(View v) {
        if (service != null && scanCompleted) {
//            new Thread(new ClientThread(service)).start();
            new Thread(new VidClientThread()).start();

        }
    }

    /**
     * 该线程往蓝牙服务器端发送文件数据。
     */
    private class VidClientThread extends Thread {
//        private BluetoothDevice device;
//
//        public ClientThread(BluetoothDevice device) {
//            this.device = device;
//        }

        @Override
        public void run() {
            try {
                // 开始往服务器端发送数据。
//                Log.d(TAG, "开始往蓝牙服务器发送数据...");
                showMessage("开始上传编码视频");
//                  Toast.makeText(getApplicationContext(), "开始往蓝牙服务器发送数据...", Toast.LENGTH_SHORT).show();
                sendDataToServer(bluetoothSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendDataToServer(BluetoothSocket socket) {
            try {
                FileInputStream fis = new FileInputStream(getVidFile());
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

                byte[] buffer = new byte[1024];
                int c;
                while (true) {
                    c = fis.read(buffer);
                    if (c == -1) {
//                    Log.d(TAG, "读取结束");
                        showMessage("视频传送结束");
//                        Toast.makeText(getApplicationContext(), "读取结束", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        bos.write(buffer, 0, c);
                    }
                }

                bos.flush();

                fis.close();
                bos.close();

//              Log.d(TAG, "发送文件成功");
                showMessage("视频传送成功");
//                Toast.makeText(getApplicationContext(), "发送文件成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private File getVidFile() {
        File root = new File(Environment.getExternalStorageDirectory(),"pictures");
        File file = new File(root, "video1.mp4");
        return file;
    }

//    public void onDisconnect(View v) {
//        if (bluetoothSocket != null) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    OutputStream outputStream;
//                    try {
//                        outputStream = bluetoothSocket.getOutputStream();
//                        outputStream.write("EXIT_APP".getBytes());
//                        showMessage("Successfully send message");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
//    }

    private void showMessage(String messageText) {
        Message message = Message.obtain();
        message.obj = messageText;
        handler.sendMessage(message);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            text.append("\n" + msg.obj);
            return false;
        }
    });

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "Receive Broadcast : " + action);
            //找到设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.e(TAG, "Find device: [" + device.getName() + "," + device.getAddress() + ", "
                        + (device.getBondState() == BluetoothDevice.BOND_BONDED ? "bonded" : "default") + "]");
                if (device.getAddress().equals(SERVICE_ADDRESS)) {
                    service = device;
                    showMessage("Service found and bound");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                text.append("\n搜索完成");
                scanCompleted = true;
            }
        }
    };
}
