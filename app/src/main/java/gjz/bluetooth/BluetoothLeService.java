package gjz.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class BluetoothLeService
  extends Service
{
  public static final String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
  public static final String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
  public static final String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
  public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
  public static final String ACTION_RSSI_AVAILABLE = "com.example.bluetooth.le.ACTION_RSSI_AVAILABLE";
  public static final String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
  public static final String EXTRA_RSSI = "com.example.bluetooth.le.EXTRA_RSSI";
  private static final int STATE_CONNECTED = 2;
  private static final int STATE_CONNECTING = 1;
  private static final int STATE_DISCONNECTED = 0;
  private static final String TAG = BluetoothLeService.class.getSimpleName();
  public static final UUID UUID_ISSC_DATA_SERVICE = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");
  public static final int WRITE_TYPE_DEFAULT = 2;
  public static final int WRITE_TYPE_NO_RESPONSE = 1;
  private final IBinder mBinder = new LocalBinder();
  private BluetoothAdapter mBluetoothAdapter;
  public String mBluetoothDeviceAddress;
  public BluetoothGatt mBluetoothGatt;
  private BluetoothManager mBluetoothManager;
  private int mConnectionState = 0;
  private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
  {
    public void onCharacteristicChanged(BluetoothGatt paramAnonymousBluetoothGatt, BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic)
    {
      BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE", paramAnonymousBluetoothGattCharacteristic);
    }
    
    public void onCharacteristicRead(BluetoothGatt paramAnonymousBluetoothGatt, BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic, int paramAnonymousInt)
    {
      if (paramAnonymousInt == 0) {
        BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE", paramAnonymousBluetoothGattCharacteristic);
      }
    }
    
    public void onConnectionStateChange(BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt1, int paramAnonymousInt2)
    {
      if (paramAnonymousInt2 == 2)
      {
        BluetoothLeService.this.mConnectionState = 2;
        BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_GATT_CONNECTED");
        Log.i(BluetoothLeService.TAG, "Connected to GATT server.");
        Log.i(BluetoothLeService.TAG, "Attempting to start service discovery:" + BluetoothLeService.this.mBluetoothGatt.discoverServices());
      }
      while (paramAnonymousInt2 != 0) {
        return;
      }
      BluetoothLeService.this.mConnectionState = 0;
      Log.i(BluetoothLeService.TAG, "Disconnected from GATT server.");
      BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_GATT_DISCONNECTED");
    }
    
    public void onReadRemoteRssi(BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt1, int paramAnonymousInt2)
    {
      if (paramAnonymousInt2 == 0) {
        BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_RSSI_AVAILABLE", paramAnonymousInt1);
      }
    }
    
    public void onServicesDiscovered(BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt)
    {
      if (paramAnonymousInt == 0)
      {
        BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED");
        return;
      }
      Log.w(BluetoothLeService.TAG, "onServicesDiscovered received: " + paramAnonymousInt);
    }
  };
  private Log myLog;
  
  private void broadcastUpdate(String paramString)
  {
    sendBroadcast(new Intent(paramString));
  }
  
  private void broadcastUpdate(String paramString, int paramInt)
  {
    Intent   intent = new Intent(paramString);
    intent.putExtra("com.example.bluetooth.le.EXTRA_RSSI", paramInt);
   // sendBroadcast(paramString);
  }
  
  private void broadcastUpdate(String strparamString, BluetoothGattCharacteristic paramBluetoothGattCharacteristic)
  {
    Intent    paramString = new Intent(strparamString);
    int i;
    if (UUID_ISSC_DATA_SERVICE.equals(paramBluetoothGattCharacteristic.getUuid())) {
      if ((paramBluetoothGattCharacteristic.getProperties() & 0x1) != 0)
      {
        i = 18;
        Log.d(TAG, "Heart rate format UINT16.");
        i = paramBluetoothGattCharacteristic.getIntValue(i, 1).intValue();
        Log.d(TAG, String.format("Received heart rate: %d", new Object[] { Integer.valueOf(i) }));
        paramString.putExtra("com.example.bluetooth.le.EXTRA_DATA", String.valueOf(i));
      }
    }
//    for (;;)
//    {
//      sendBroadcast(paramString);
//      return;
////     int i = 17;
//      Log.d(TAG, "Heart rate format UINT8.");
//      break;
////      paramBluetoothGattCharacteristic = paramBluetoothGattCharacteristic.getValue();
////      if ((paramBluetoothGattCharacteristic != null) && (paramBluetoothGattCharacteristic.length > 0))
////      {
////        new StringBuilder(paramBluetoothGattCharacteristic.length);
////        paramString.putExtra("com.example.bluetooth.le.EXTRA_DATA", paramBluetoothGattCharacteristic);
////      }
//    }
  }
  
  public void close()
  {
    if (this.mBluetoothGatt == null) {
      return;
    }
    this.mBluetoothGatt.close();
    this.mBluetoothGatt = null;
  }
  
  public boolean connect(String paramString)
  {
    if ((this.mBluetoothAdapter == null) || (paramString == null))
    {
      Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
      return false;
    }
    if ((this.mBluetoothDeviceAddress != null) && (paramString.equals(this.mBluetoothDeviceAddress)) && (this.mBluetoothGatt != null))
    {
      Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
      if (this.mBluetoothGatt.connect())
      {
        this.mConnectionState = 1;
        return true;
      }
      return false;
    }
    BluetoothDevice localBluetoothDevice = this.mBluetoothAdapter.getRemoteDevice(paramString);
    if (localBluetoothDevice == null)
    {
      Log.w(TAG, "Device not found.  Unable to connect.");
      return false;
    }
    this.mBluetoothGatt = localBluetoothDevice.connectGatt(this, false, this.mGattCallback);
    Log.e(TAG, "Trying to create a new connection.");
    this.mBluetoothDeviceAddress = paramString;
    this.mConnectionState = 1;
    return true;
  }
  
  public void disconnect()
  {
    if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null))
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }
    this.mBluetoothGatt.disconnect();
  }
  
  public List<BluetoothGattService> getSupportedGattServices()
  {
    if (this.mBluetoothGatt == null) {
      return null;
    }
    return this.mBluetoothGatt.getServices();
  }
  
  public boolean initialize()
  {
    if (this.mBluetoothManager == null)
    {
      this.mBluetoothManager = ((BluetoothManager)getSystemService("bluetooth"));
      if (this.mBluetoothManager == null)
      {
        Log.e(TAG, "Unable to initialize BluetoothManager.");
        return false;
      }
    }
    this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
    if (this.mBluetoothAdapter == null)
    {
      Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
      return false;
    }
    return true;
  }
  
  public IBinder onBind(Intent paramIntent)
  {
    return this.mBinder;
  }
  
  public boolean onUnbind(Intent paramIntent)
  {
    close();
    return super.onUnbind(paramIntent);
  }
  
  public void readCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic)
  {
    if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null))
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }
    this.mBluetoothGatt.readCharacteristic(paramBluetoothGattCharacteristic);
  }
  
  public boolean reconnect(String paramString)
  {
    if ((this.mBluetoothAdapter == null) || (paramString == null)) {
      Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
    }
    BluetoothDevice localBluetoothDevice;
    do
    {
//      return false;
      localBluetoothDevice = this.mBluetoothAdapter.getRemoteDevice(paramString);
      if (localBluetoothDevice == null)
      {
        Log.e(TAG, "Device not found.  Unable to connect.");
        return false;
      }
      if ((this.mBluetoothDeviceAddress == null) || (!paramString.equals(this.mBluetoothDeviceAddress)) || (this.mBluetoothGatt == null)) {
        break;
      }
      Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
    } while (!this.mBluetoothGatt.connect());
    this.mConnectionState = 1;
//    return true;
    this.mBluetoothGatt = localBluetoothDevice.connectGatt(this, false, this.mGattCallback);
    Log.d(TAG, "Trying to create a new connection.");
    this.mBluetoothDeviceAddress = paramString;
    this.mConnectionState = 1;
    return true;
  }
  
  public void setCharacteristicNotification(BluetoothGattCharacteristic paramBluetoothGattCharacteristic, boolean paramBoolean)
  {
    if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null)) {
      Log.w(TAG, "BluetoothAdapter not initialized");
    }
    do
    {
//      return;
      Log.e(TAG, "启动数据传输！");
      this.mBluetoothGatt.setCharacteristicNotification(paramBluetoothGattCharacteristic, paramBoolean);
    } while ((!SampleGattAttributes.CHR_ISSC_TRANS_TX.equals(paramBluetoothGattCharacteristic.getUuid())) && (!SampleGattAttributes.CHR_TI_TRANS_TX.equals(paramBluetoothGattCharacteristic.getUuid())));
  //  paramBluetoothGattCharacteristic = paramBluetoothGattCharacteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
    paramBluetoothGattCharacteristic.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
  //  this.mBluetoothGatt.writeDescriptor(paramBluetoothGattCharacteristic);
    Log.e(TAG, "开始数据传输！");
  }
  
  public void writeCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic)
  {
    if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null))
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }
    this.mBluetoothGatt.writeCharacteristic(paramBluetoothGattCharacteristic);
  }
  
  public class LocalBinder
    extends Binder
  {
    public LocalBinder() {}
    
    public BluetoothLeService getService()
    {
      return BluetoothLeService.this;
    }
  }
}
