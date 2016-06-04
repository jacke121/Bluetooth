package gjz.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class ReceiveDatas extends Thread {
    // 变量 略过
    BluetoothSocket mmSocket;
    InputStream mmInStream;

    // 构造方法
    public ReceiveDatas(BluetoothSocket socket) {

        this.mmSocket = socket;
        InputStream tempIn = null;

        // 获取输入流 
        try {
            tempIn = socket.getInputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block 
        }
        mmInStream = tempIn;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];// 缓冲数据流 
        int bytes;// 返回读取到的数据 
        // 监听输入流 
        while (true) {
            try {
                bytes = mmInStream.read(buffer);
                // 此处处理数据……

            } catch (IOException e) {
                try {
                    if (mmInStream != null) {
                        mmInStream.close();
                    }
                    Log.i("info", "异常");
                    break;
                } catch (IOException e1) {
                    // TODO Auto-generated catch block 
                    e1.printStackTrace();
                }
            }
            try {
                Thread.sleep(50);// 延迟 
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block 
                e.printStackTrace();
            }
        }
    }
}