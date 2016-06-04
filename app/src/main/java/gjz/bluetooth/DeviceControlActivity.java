package gjz.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

public class DeviceControlActivity
  extends Activity
{
  public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
  public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
  private static final int PAYLOAD_MAX = 20;
  private static final String Paired_Devices_SDCARD_DIR = "/sdcard/PairedDevices";
  private static final String TAG = DeviceControlActivity.class.getSimpleName();
  private int connectOK = 0;
  private String filenameTemp;
  private BluetoothAdapter mBluetoothAdapter;
  private BluetoothLeService mBluetoothLeService;
  private Button mConnect;
  private boolean mConnected = false;
  private TextView mDataField;
  private String mDeviceAddress;
  private String mDeviceName;
  private int mDevicesCount;
  HashMap mDevicesHashMap = new HashMap();
  String paramAnonymousContextstr;
  private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
  {
    public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
    {
      paramAnonymousContextstr = paramAnonymousIntent.getAction();
      if ("com.example.bluetooth.le.ACTION_GATT_CONNECTED".equals(paramAnonymousContext))
      {
        DeviceControlActivity.this.mConnected = true;
        DeviceControlActivity.this.displayConnectState(1);
        if (DeviceControlActivity.this.pairOK == 1)
        {
          DeviceControlActivity.this.pairOK = 0;
          DeviceControlActivity.this.write("abcd03fd7802");
        }
        DeviceControlActivity.this.invalidateOptionsMenu();
      }
      do
      {
//        return;
        if ("com.example.bluetooth.le.ACTION_GATT_DISCONNECTED".equals(paramAnonymousContext))
        {
          DeviceControlActivity.this.mConnected = false;
          DeviceControlActivity.this.connectOK = 0;
          DeviceControlActivity.this.pairOK = 0;
          DeviceControlActivity.this.displayConnectState(0);
          DeviceControlActivity.this.invalidateOptionsMenu();
          DeviceControlActivity.this.clearUI();
          return;
        }
        if ("com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED".equals(paramAnonymousContext))
        {
          DeviceControlActivity.this.displayGattServices(DeviceControlActivity.this.mBluetoothLeService.getSupportedGattServices());
          return;
        }
        if ("com.example.bluetooth.le.ACTION_DATA_AVAILABLE".equals(paramAnonymousContext))
        {
          DeviceControlActivity.this.displayData(paramAnonymousIntent.getStringExtra("com.example.bluetooth.le.EXTRA_DATA"));
          return;
        }
      } while (!"com.example.bluetooth.le.ACTION_RSSI_AVAILABLE".equals(paramAnonymousContext));
      DeviceControlActivity.this.displayRssi(paramAnonymousIntent.getIntExtra("com.example.bluetooth.le.EXTRA_RSSI", 0));
    }
  };
  private EditText mInput;
  private TextView mRssi;
  private Button mSend;
  private final ServiceConnection mServiceConnection = new ServiceConnection()
  {
    public void onServiceConnected(ComponentName paramAnonymousComponentName, IBinder paramAnonymousIBinder)
    {
      DeviceControlActivity.this.mBluetoothLeService = ((BluetoothLeService.LocalBinder)paramAnonymousIBinder).getService();
      if (!DeviceControlActivity.this.mBluetoothLeService.initialize())
      {
        Log.e(DeviceControlActivity.TAG, "Unable to initialize Bluetooth");
        DeviceControlActivity.this.finish();
      }
      DeviceControlActivity.this.mBluetoothLeService.connect(DeviceControlActivity.this.mDeviceAddress);
    }
    
    public void onServiceDisconnected(ComponentName paramAnonymousComponentName)
    {
      DeviceControlActivity.this.mBluetoothLeService = null;
    }
  };
  private BluetoothGattCharacteristic mTransRx;
  private BluetoothGattCharacteristic mTransTx;
  private Log myLog;
  private int pairFlag;
  private int pairOK = 0;
  private boolean serviceBindFlag = false;
  
  private void clearUI()
  {
    this.mDataField.setText(" ");
  }
  
  private void displayConnectState(int paramInt)
  {
    if (paramInt == 1)
    {
      this.mConnect.setText("Connected");
      return;
    }
    this.mConnect.setText("Connect");
  }
  
  private void displayData(String paramString)
  {
    if (paramString != null)
    {
      this.mDataField.setText(paramString);
      if (this.pairFlag == 1)
      {
        Log.e("TAG", "记录配对");
        this.pairFlag = 0;
        this.mDevicesCount += 1;
      //  writefile(this.filenameTemp);
      }
    }
  }
  
  private void displayGattServices(List<BluetoothGattService> paramList)
  {
    if (paramList == null) {}
    BluetoothGattService localBluetoothGattService;
    String str;
    do
    {
      Iterator it = paramList.iterator();
      while (!it.hasNext()) {
        it = paramList.iterator();
      }
      localBluetoothGattService = (BluetoothGattService)it.next();
      str = localBluetoothGattService.getUuid().toString();
      if (str.equalsIgnoreCase("49535343-FE7D-4AE5-8FA9-9FAFD205E455"))
      {
        Log.e(TAG, "发现了ISSC的数据传输Service!");
        this.mTransTx = localBluetoothGattService.getCharacteristic(SampleGattAttributes.CHR_ISSC_TRANS_TX);
        this.mTransRx = localBluetoothGattService.getCharacteristic(SampleGattAttributes.CHR_ISSC_TRANS_RX);
        this.mBluetoothLeService.setCharacteristicNotification(this.mTransTx, true);
        Log.e(TAG, "开始数据传输！");
        this.mTransRx.setValue("abcd04430101c0");
        this.mBluetoothLeService.writeCharacteristic(this.mTransRx);
        return;
      }
    } while (!str.equalsIgnoreCase("0000fff0-0000-1000-8000-00805f9b34fb"));
    Log.e(TAG, "发现了TI的数据传输Service!");
    this.mTransTx = localBluetoothGattService.getCharacteristic(SampleGattAttributes.CHR_TI_TRANS_TX);
    this.mTransRx = localBluetoothGattService.getCharacteristic(SampleGattAttributes.CHR_TI_TRANS_RX);
    this.mBluetoothLeService.setCharacteristicNotification(this.mTransTx, true);
    Log.e(TAG, "开始数据传输！");
    this.mTransRx.setValue("abcd04430101c0");
    this.mBluetoothLeService.writeCharacteristic(this.mTransRx);
  }
  
  private void displayRssi(int paramInt)
  {
    String str = paramInt + " ";
    this.mRssi.setText(str);
  }
  
  private static IntentFilter makeGattUpdateIntentFilter()
  {
    IntentFilter localIntentFilter = new IntentFilter();
    localIntentFilter.addAction("com.example.bluetooth.le.ACTION_GATT_CONNECTED");
    localIntentFilter.addAction("com.example.bluetooth.le.ACTION_GATT_DISCONNECTED");
    localIntentFilter.addAction("com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED");
    localIntentFilter.addAction("com.example.bluetooth.le.ACTION_DATA_AVAILABLE");
    localIntentFilter.addAction("com.example.bluetooth.le.ACTION_RSSI_AVAILABLE");
    return localIntentFilter;
  }
  
  private void scanForConnect()
  {
  //  startActivityForResult(new Intent(this, DeviceScanActivity.class), 0);
  }
  
  private void viewFile(String paramString)
  {
    this.mDevicesCount = 0;
    int j = 0;
   File  paramfile = new File(paramString);
    if (paramfile.canRead()) {
      if (paramfile.isDirectory()) {
      return;
      }
    }
    for (;;)
    {
      char[] arrayOfChar;
      String str;
      int k;
      int i;
      try
      {
        BufferedReader localBufferedReader = new BufferedReader(new FileReader(paramString));
        paramString = localBufferedReader.readLine();
        if (paramString == null)
        {
          localBufferedReader.close();
          return;
        }
        StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ",");
        i = j;
        j = i;
        if (!localStringTokenizer.hasMoreTokens()) {
          continue;
        }
        arrayOfChar = localStringTokenizer.nextToken().toCharArray();
        str = "";
        j = 0;
        k = i;
        i = k;
        if (j >= arrayOfChar.length) {
          continue;
        }
        if (arrayOfChar[j] != '\t') {
          break ;
        }
        i = k;
        paramString = str;
        if (str.isEmpty()) {
          break ;
        }
        i = k + 1;
        if (i <= 1) {
          break ;
        }
        this.mDevicesCount += 1;
//        paramString = this.mDevicesCount;
        this.mDevicesHashMap.put(paramString, str);
      }
      catch (FileNotFoundException e)
      {
        e.printStackTrace();
        return;
      }
      catch (IOException e)
      {

        e.printStackTrace();
        return;
      }
//      paramString = arrayOfChar[j];
      paramString = str + paramString;
//      int i = k;
//      break label292;
      new AlertDialog.Builder(this).setTitle("Message").setMessage("權限不足~").setPositiveButton("ok", new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {}
      }).show();
      do
      {
        j += 1;
        k = i;
        str = paramString;
        if ((arrayOfChar[j] != '\r') || (arrayOfChar[j] != '\n')) {
          break;
        }
        i = k;
        paramString = str;
      } while (arrayOfChar[j] == '\t');
    }
  }
  
  private void write(CharSequence paramCharSequence)
  {
    paramCharSequence = new String(paramCharSequence.toString());
    int k = paramCharSequence.length() / 2;
    int j = 0;
    try
    {
      ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(k);
      int i = 0;
      for (;;)
      {
        if (i >= k)
        {
          write(localByteArrayOutputStream.toByteArray());
          return;
        }
        String str = (String) paramCharSequence.subSequence(j, j + 2);
        Log.e(TAG, "要发送的数据是：" + str);
        localByteArrayOutputStream.write(Integer.parseInt(str, 16));
        j += 2;
        i += 1;
      }
    }
    catch (Exception e)
    {
      Log.e(TAG, "发送数据异常啦！");
    }
  }
  
  private void write(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte.length > 0)
    {
      this.mTransRx.setValue(paramArrayOfByte);
      this.mBluetoothLeService.writeCharacteristic(this.mTransRx);
    }
  }
  
  void DialogChangePairMode(CharSequence paramCharSequence1, CharSequence paramCharSequence2)
  {
    final EditText localEditText = new EditText(this);
    localEditText.setFocusable(true);
    AlertDialog localAlertDialog = new AlertDialog.Builder(this).create();
    localAlertDialog.setTitle(paramCharSequence1);
    localAlertDialog.setMessage(paramCharSequence2);
    localAlertDialog.setView(localEditText);
    localAlertDialog.setButton(-2, "取消", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        DeviceControlActivity.this.mBluetoothLeService.disconnect();
      }
    });
    localAlertDialog.setButton(-1, "修改配对密码", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
//        paramAnonymousDialogInterface = localEditText.getText().toString();
//        DeviceControlActivity.this.write(paramAnonymousDialogInterface);
      }
    });
    localAlertDialog.show();
  }
  
  void DialogConfirmShow(CharSequence paramCharSequence1, CharSequence paramCharSequence2)
  {
    AlertDialog localAlertDialog = new AlertDialog.Builder(this).create();
    localAlertDialog.setTitle(paramCharSequence1);
    localAlertDialog.setMessage(paramCharSequence2);
    localAlertDialog.setButton(-1, "确定", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {}
    });
    localAlertDialog.show();
  }
  
  void DialogPairMode(CharSequence paramCharSequence1, CharSequence paramCharSequence2)
  {
    final EditText localEditText = new EditText(this);
    localEditText.setFocusable(true);
    AlertDialog localAlertDialog = new AlertDialog.Builder(this).create();
    localAlertDialog.setTitle(paramCharSequence1);
    localAlertDialog.setMessage(paramCharSequence2);
    localAlertDialog.setView(localEditText);
    localAlertDialog.setButton(-2, "取消", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        DeviceControlActivity.this.mBluetoothLeService.disconnect();
      }
    });
    localAlertDialog.setButton(-1, "配对", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
//        paramAnonymousDialogInterface = localEditText.getText().toString();
//        DeviceControlActivity.this.write(paramAnonymousDialogInterface);
      }
    });
    localAlertDialog.show();
  }
  
  void DialogSwitchPairMode(CharSequence paramCharSequence1, CharSequence paramCharSequence2)
  {
    AlertDialog localAlertDialog = new AlertDialog.Builder(this).create();
    localAlertDialog.setTitle(paramCharSequence1);
    localAlertDialog.setMessage(paramCharSequence2);
    localAlertDialog.setButton(-2, "取消", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        DeviceControlActivity.this.mBluetoothLeService.disconnect();
      }
    });
    localAlertDialog.setButton(-1, "配对", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        DeviceControlActivity.this.DialogPairMode("提示：", "请输入配对密码！");
      }
    });
    localAlertDialog.setButton(-3, "修改配对密码", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        DeviceControlActivity.this.DialogChangePairMode("提示：", "请输入旧的配对密码！");
      }
    });
    localAlertDialog.show();
  }
  
  boolean isPaired(String paramString)
  {
    int i=0;
    if (this.mDevicesCount > 0) {
      i = 1;
    }
    for (;;)
    {
      if (i > this.mDevicesCount) {
        return false;
      }
      String str = i+"";
      if (((String)this.mDevicesHashMap.get(str)).equals(paramString)) {
        return true;
      }
      i += 1;
    }
  }
  
  protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
  {
    switch (paramInt2)
    {
    }
    do
    {
      this.mDeviceName = paramIntent.getStringExtra("DEVICE_NAME");
      this.mDeviceAddress = paramIntent.getStringExtra("DEVICE_ADDRESS");
    } while (this.mDeviceAddress == null);
    bindService(new Intent(this, BluetoothLeService.class), this.mServiceConnection, 1);
    this.serviceBindFlag = true;
    if (!paramIntent.getBooleanExtra("Paired", false))
    {
      Log.e("TAG", "NotPaired");
      this.pairFlag = 1;
      paramInt1 = this.mDevicesCount;
    //  paramIntent = paramInt1 + 1;
      this.mDevicesHashMap.put(paramIntent, this.mDeviceAddress);
      return;
    }
    this.pairOK = 1;
  }
  
  public void onClickConnect(View paramView)
  {
    if (!this.mConnected)
    {
      scanForConnect();
      return;
    }
    this.mBluetoothLeService.disconnect();
  }
  
  public void onClickSend(View paramView)
  {
    if (this.mConnected) {
      write(this.mInput.getText());
    }
  }
  
  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2130903051);
    this.mDataField = ((TextView)findViewById(2131165238));
    this.mRssi = ((TextView)findViewById(2131165242));
    this.mConnect = ((Button)findViewById(2131165243));
    this.mSend = ((Button)findViewById(2131165239));
    this.mInput = ((EditText)findViewById(2131165240));
    this.pairFlag = 0;
    this.mDevicesCount = 0;
  //  paramBundle = new File("/sdcard/PairedDevices");
//    if (!paramBundle.exists()) {}
//    try
//    {
//      paramBundle.mkdirs();
//      label114:
//      this.filenameTemp = "/sdcard/PairedDevices/Devices.csv";
//      paramBundle = new File(this.filenameTemp);
//      if (!paramBundle.exists()) {}
//      try
//      {
//        paramBundle.createNewFile();
//        return;
//      }
//      catch (Exception paramBundle) {}
//      viewFile(this.filenameTemp);
//      return;
//    }
//    catch (Exception paramBundle)
//    {
//      break label114;
//    }
  }
  
  protected void onDestroy()
  {
    super.onDestroy();
    if (this.serviceBindFlag)
    {
      this.serviceBindFlag = false;
      unbindService(this.mServiceConnection);
      this.mBluetoothLeService = null;
    }
  }
  
  protected void onPause()
  {
    super.onPause();
    unregisterReceiver(this.mGattUpdateReceiver);
  }
  
  protected void onResume()
  {
    super.onResume();
    registerReceiver(this.mGattUpdateReceiver, makeGattUpdateIntentFilter());
    if (this.mBluetoothLeService != null)
    {
      boolean bool = this.mBluetoothLeService.connect(this.mDeviceAddress);
      Log.d(TAG, "Connect request result=" + bool);
    }
  }
  

}
