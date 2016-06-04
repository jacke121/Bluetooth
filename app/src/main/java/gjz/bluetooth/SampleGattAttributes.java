package gjz.bluetooth;

import java.util.HashMap;
import java.util.UUID;

public class SampleGattAttributes
{
  public static final UUID CHR_AIR_PATCH;
  public static final UUID CHR_CONNECTION_PARAMETER;
  public static final UUID CHR_ISSC_MP;
  public static final UUID CHR_ISSC_TRANS_RX;
  public static final UUID CHR_ISSC_TRANS_TX;
  public static final UUID CHR_TI_TRANS_RX;
  public static final UUID CHR_TI_TRANS_TX;
  public static String CLIENT_CHARACTERISTIC_CONFIG;
  public static String HEART_RATE_MEASUREMENT;
  public static final UUID SERVICE_ISSC_PROPRIETARY;
  public static final UUID SERVICE_TI_PROPRIETARY;
  public static final String S_CHR_ISSC_TRANS_RX = "49535343-8841-43F4-A8D4-ECBE34729BB3";
  public static final String S_CHR_ISSC_TRANS_TX = "49535343-1E4D-4BD9-BA61-23C647249616";
  public static final String S_CHR_TI_TRANS_RX = "0000fff1-0000-1000-8000-00805f9b34fb";
  public static final String S_CHR_TI_TRANS_TX = "0000fff4-0000-1000-8000-00805f9b34fb";
  public static final String S_SERVICE_ISSC_PROPRIETARY = "49535343-FE7D-4AE5-8FA9-9FAFD205E455";
  public static final String S_SERVICE_TI_PROPRIETARY = "0000fff0-0000-1000-8000-00805f9b34fb";
  private static HashMap<String, String> attributes = new HashMap();
  
  static
  {
    HEART_RATE_MEASUREMENT = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    SERVICE_ISSC_PROPRIETARY = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");
    CHR_CONNECTION_PARAMETER = UUID.fromString("49535343-6DAA-4D02-ABF6-19569ACA69FE");
    CHR_AIR_PATCH = UUID.fromString("49535343-ACA3-481C-91EC-D85E28A60318");
    CHR_ISSC_TRANS_TX = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");
    CHR_ISSC_TRANS_RX = UUID.fromString("49535343-8841-43F4-A8D4-ECBE34729BB3");
    CHR_ISSC_MP = UUID.fromString("49535343-ACA3-481C-91EC-D85E28A60318");
    SERVICE_TI_PROPRIETARY = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    CHR_TI_TRANS_TX = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
    CHR_TI_TRANS_RX = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    attributes.put("00001800-0000-1000-8000-00805f9b34fb", "0x1800");
    attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
    attributes.put("49535343-fe7d-4ae5-8fa9-9fafd205e455", "Data Service");
    attributes.put("49535343-1e4d-4bd9-ba61-23c647249616", "ISSC_RX");
    attributes.put("49535343-8841-43f4-a8d4-ecbe34729bb3", "ISSC_TX");
  }
  
  public static String lookup(String paramString1, String paramString2)
  {
    paramString1 = (String)attributes.get(paramString1);
    if (paramString1 == null) {
      return paramString2;
    }
    return paramString1;
  }
}
