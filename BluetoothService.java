package com.em.btest;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;


/**
 * 蓝牙服务
 */
public class BluetoothService implements Runnable {
    private static final UUID SERVER_UUID = new UUID("0000110100001000800000805F9B34FB", false);

    private boolean isListening;
    private StreamConnectionNotifier streamConnectionNotifier;

    public BluetoothService() {
        isListening = true;
    }

    public void init() {
        try {
            //设置蓝牙状态为可检测
            LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
            streamConnectionNotifier = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + SERVER_UUID.toString());

            ServiceRecord serviceRecord = LocalDevice.getLocalDevice().getRecord(streamConnectionNotifier);
            // 获得ServiceRecord
            // 设置 ServiceRecord ServiceAvailability (0x0008) 属性的值
            // 表明该服务为可用
            // 0xFF 表示完全可用状态
            // 这个操作是可选的
            serviceRecord.setAttributeValue(0x0008, new DataElement(DataElement.U_INT_1, 0xFF));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startListening() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (isListening) {
            StreamConnection streamConnection;
            try {
                streamConnection = streamConnectionNotifier.acceptAndOpen();
                new ClientThread(streamConnection).start();

 
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
	/**
	 * 开启一个线程专门从与客户端蓝牙设备中读取文件数据，并把文件数据存储到本地。
	 * 
	 * @author fly
	 *
	 */
	private class ClientThread extends Thread {
		private StreamConnection mStreamConnection = null;

		public ClientThread(StreamConnection sc) {
			mStreamConnection = sc;
		}

		@Override
		public void run() {
			try {
				BufferedInputStream bis = new BufferedInputStream(mStreamConnection.openInputStream());

				// 本地创建一个video1.mp4文件接收来自于手机客户端发来的图片文件数据。
				FileOutputStream fos = new FileOutputStream("video1.mp4");

				int c = 0;
				byte[] buffer = new byte[1024];

				System.out.println("开始读视频...");
				while (true) {
					c = bis.read(buffer);
					if (c == -1) {
						System.out.println("读取视频结束");
						break;
					} else {
						fos.write(buffer, 0, c);
					}
				}

				fos.flush();

				fos.close();
				bis.close();
				mStreamConnection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}


