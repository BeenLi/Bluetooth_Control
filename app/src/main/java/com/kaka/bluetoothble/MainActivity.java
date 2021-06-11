package com.kaka.bluetoothble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.kaka.bluetoothble.adapter.BleAdapter;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "ble_tag";
  private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
  ProgressBar pbSearchBle;
  Button ivSearchBtn;
  //    TextView tvSerBleStatus;
  TextView tvSerBindStatus;
  ListView bleListView;
  private RelativeLayout operaView;
  private ImageView ivStop;
  private ImageView ivForward;
  private ImageView ivBackward;
  private ImageView ivLeft;
  private ImageView ivRight;
  private TextView car_status;
  //    private Button btnWrite;
//    private Button btnRead;
//    private EditText etWriteContent;
//    private TextView tvResponse;
  private List<BluetoothDevice> device_list;
  private List<Integer> mRssis;
  private BleAdapter mAdapter;
  private BluetoothAdapter BluetoothAdapter;
  private BluetoothManager mBluetoothManager;
  private boolean isScaning = false;
  private boolean isConnecting = false;
  private boolean isConnected = false;
  private BluetoothGatt mBluetoothGatt;

  //服务和特征值
  private UUID write_UUID_service;
  private UUID write_UUID_chara;
  private UUID read_UUID_service;
  private UUID read_UUID_chara;
  private UUID notify_UUID_service;
  private UUID notify_UUID_chara;
  private UUID indicate_UUID_service;
  private UUID indicate_UUID_chara;
  private String hex = "7B46363941373237323532443741397D";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search_device);   // 设置布局为activity_search_device
    initView();
    initData();
    // 调用系统服务---获取蓝牙管理器
    mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
    BluetoothAdapter = mBluetoothManager.getAdapter();
    // 如果没有开启蓝牙, 则提醒用户打开
    if (BluetoothAdapter == null || !BluetoothAdapter.isEnabled()) {
      Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(intent, ENABLE_BLUETOOTH_REQUEST_CODE);
    }

  }

  //    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        ;
//    }
//    @Overide
  public void onClick(View view) {
    int id = view.getId();
    if (id == R.id.stop) {
      Log.i(TAG, "停止");
      writeData("00");
    } else if (id == R.id.forward) {
      Log.i(TAG, "前进");
      writeData("01");
    } else if (id == R.id.backward) {
      Log.i(TAG, "后退");
      writeData("02");
    } else if (id == R.id.left) {
      Log.i(TAG, "向左");
      writeData("03");
    } else if (id == R.id.right) {
      Log.i(TAG, "向右");
      writeData("04");
    }
  }

  private void initData() {
    device_list = new ArrayList<>();
    mRssis = new ArrayList<>();
    mAdapter = new BleAdapter(MainActivity.this, device_list, mRssis);
    bleListView.setAdapter(mAdapter);       // adapter里面存访listview的数据
    mAdapter.notifyDataSetChanged();        // 数据改动提醒
  }

  private void initView() {
    pbSearchBle = findViewById(R.id.ProgressBar);      // 进度条
    ivSearchBtn = findViewById(R.id.Btn_scan);             // image_view蓝牙图片(点击搜索)
    tvSerBindStatus = findViewById(R.id.Tv_Conn_Status);      // 蓝牙状态(未绑定,已连接,搜索已结束)
//        tvSerBleStatus=findViewById(R.id.tv_ser_ble_status);        // 停止搜索(没有显示)
    bleListView = findViewById(R.id.ListView_Devices);               // 显示搜索到的蓝牙条目
    operaView = findViewById(R.id.Relative_Control);                    // 连接后进行操作的View(线性布局)

    // 小车控制部分
    ivBackward = findViewById(R.id.backward);
    ivForward = findViewById(R.id.forward);
    ivLeft = findViewById(R.id.left);
    ivRight = findViewById(R.id.right);
    ivStop = findViewById(R.id.stop);

    car_status = findViewById(R.id.status_value);
//        btnWrite=findViewById(R.id.btnWrite);                       // 写按钮
//        btnRead=findViewById(R.id.btnRead);                         // 读按钮
//        etWriteContent=findViewById(R.id.et_write);                 // edit_text传递消息文本框
//        tvResponse=findViewById(R.id.tv_response);                  // 设备返回:

//        btnRead.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                readData();
//            }
//        });

//        btnWrite.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //执行写入操作
//                writeData();
//            }
//        });

    // 点击中间的蓝牙图标,进行搜索
    ivSearchBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isConnected) { // 断开连接逻辑(手动断开前将小车控制为停止状态)

          mBluetoothGatt.disconnect();
          isConnected = false;
          tvSerBindStatus.setText("未连接");
          ivSearchBtn.setText("开始扫描");
          bleListView.setVisibility(View.VISIBLE);
          operaView.setVisibility(View.GONE);
        } else {
          Button a = (Button) v;
          if (isScaning) {         // 停止扫描
            pbSearchBle.setVisibility(View.GONE);
            tvSerBindStatus.setText("停止搜索");
            a.setText("开始扫描");
            stopScanDevice();
          } else {                  // 开始扫描
            checkPermissions();
            a.setText("停止扫描");
          }
        }
      }
    });
    bleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        isConnecting = true;
        BluetoothDevice bluetoothDevice = device_list.get(position);
        //连接设备
        tvSerBindStatus.setText("连接中");
        pbSearchBle.setVisibility(View.VISIBLE);
        // 在API23之后,加入了连接方式(transport_LE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this,
              true, BLEGattCallback, TRANSPORT_LE);
        } else {
          mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this,
              true, BLEGattCallback);
        }
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            // 3s后如果还没连接上,给用户提示, 结束连接
            if (!isConnected) {
              mBluetoothGatt.disconnect();
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  isConnecting = false;
                  tvSerBindStatus.setText("连接超时");
                  if (!isScaning) {
                    pbSearchBle.setVisibility(View.GONE);
                  }
                }
              });
            }
          }
        }, 3000);

      }
    });


  }

//    private void readData() {
//        BluetoothGattCharacteristic characteristic=mBluetoothGatt.getService(read_UUID_service)
//                .getCharacteristic(read_UUID_chara);
//        mBluetoothGatt.readCharacteristic(characteristic);
//    }


  /**
   * 开始扫描 5秒后自动停止
   */
  private void scanDevice() {
    tvSerBindStatus.setText("正在搜索");
    isScaning = true;
    pbSearchBle.setVisibility(View.VISIBLE);
    BluetoothAdapter.startLeScan(scanBLECallback);
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        //结束扫描
        BluetoothAdapter.stopLeScan(scanBLECallback);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            pbSearchBle.setVisibility(View.GONE);
            if (!isConnected) {                  // 防止连接成功后覆盖
              ivSearchBtn.setText("开始扫描");
              tvSerBindStatus.setText("未连接");
            }
            isScaning = false;
          }
        });
      }
    }, 5000); // 5s
  }

  /**
   * 停止扫描
   */
  private void stopScanDevice() {
    isScaning = false;
    pbSearchBle.setVisibility(View.GONE);
    BluetoothAdapter.stopLeScan(scanBLECallback);
  }


  // 用于发送LE扫描结果的回调接口
  BluetoothAdapter.LeScanCallback scanBLECallback = new BluetoothAdapter.LeScanCallback() {
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
      Log.e(TAG, "run: scanning...");
      if (!device_list.contains(device)) {
        device_list.add(device);
        mRssis.add(rssi);
        mAdapter.notifyDataSetChanged();
      }
    }
  };

  private BluetoothGattCallback BLEGattCallback = new BluetoothGattCallback() {
    /**
     * 断开或连接 状态发生变化时调用
     * */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
      super.onConnectionStateChange(gatt, status, newState);
      Log.e(TAG, "onConnectionStateChange()");
      if (status == BluetoothGatt.GATT_SUCCESS) {
        //连接成功
        if (newState == BluetoothGatt.STATE_CONNECTED) {
          Log.e(TAG, "连接成功");
          //发现服务
          isConnected = true;
          gatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
          mBluetoothGatt.close();
          Log.e(TAG, "断开连接");
        }
      } else {
        //连接失败
        Log.e(TAG, "失败==" + status);
        mBluetoothGatt.close();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            tvSerBindStatus.setText("连接失败");
          }
        });
      }
    }

    /**
     * 发现服务（真正建立连接）
     * */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      super.onServicesDiscovered(gatt, status);
      //直到这里才是真正建立了可通信的连接
      isConnecting = false;
      Log.e(TAG, "onServicesDiscovered()---建立连接");
      //获取初始化服务和特征值
      initServiceAndChara();
      //订阅通知(很重要,从BLE从设备发回来的数据用订阅查收)
      mBluetoothGatt.setCharacteristicNotification(mBluetoothGatt
          .getService(notify_UUID_service).getCharacteristic(notify_UUID_chara), true);

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          bleListView.setVisibility(View.GONE);
          operaView.setVisibility(View.VISIBLE);
          tvSerBindStatus.setText("已连接");
          pbSearchBle.setVisibility(View.GONE);
          ivSearchBtn.setText("断开连接");        // 提供断开连接的按钮
        }
      });
    }

    /**
     * 读操作的回调
     * */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
        int status) {
      super.onCharacteristicRead(gatt, characteristic, status);
      Log.e(TAG, "onCharacteristicRead()");
    }

    /**
     * 写操作的回调
     * */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
        BluetoothGattCharacteristic characteristic, int status) {
      super.onCharacteristicWrite(gatt, characteristic, status);

      Log.e(TAG, "onCharacteristicWrite()  status=" + status + ",value=" + HexUtil
          .encodeHexStr(characteristic.getValue()));
    }

    /**
     * 接收已经连接的BLE device的通知notifications(BLE设备发回来的数据)
     * */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
        BluetoothGattCharacteristic characteristic) {
      super.onCharacteristicChanged(gatt, characteristic);
      String return_hex = onebyte2hex(characteristic.getValue()[0]);
      String return_status;
      switch (return_hex) {
        case "00": {
          return_status = "停止";
          break;
        }
        case "01": {
          return_status = "前进";
          break;
        }
        case "02": {
          return_status = "后退";
          break;
        }
        case "03": {
          return_status = "向左";
          break;
        }
        case "04": {
          return_status = "向右";
          break;
        }
        case "05": {
          return_status = "危险";
          break;
        }
        default: {
          return_status = "未知";
        }
      }

      Log.e(TAG, "onCharacteristicChanged()" + "return_hex = " + return_hex + "  return_origin = "
          + characteristic.getStringValue(0));
      String finalReturn_status = return_status;
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          car_status.setText(return_status);
        }
      });

    }
  };

  /**
   * 检查权限
   */
  private void checkPermissions() {
    RxPermissions rxPermissions = new RxPermissions(MainActivity.this);
    rxPermissions.request(android.Manifest.permission.ACCESS_FINE_LOCATION)
        .subscribe(new io.reactivex.functions.Consumer<Boolean>() {
          @Override
          public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) {
              // 用户已经同意该权限
              scanDevice();
            } else {
              // 用户拒绝了该权限，并且选中『不再询问』
              ToastUtils.showLong("用户开启权限后才能使用");
            }
          }
        });
  }


  // 初始化service 和 characteristic
  private void initServiceAndChara() {
    List<BluetoothGattService> bluetoothGattServices = mBluetoothGatt.getServices();
    // 遍历services
    for (BluetoothGattService bluetoothGattService : bluetoothGattServices) {
      List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
      // 遍历 characteristics
      for (BluetoothGattCharacteristic characteristic : characteristics) {
        int charaProp = characteristic.getProperties();
        // read_characterisctic(独热编码)
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
          read_UUID_chara = characteristic.getUuid();
          read_UUID_service = bluetoothGattService.getUuid();
          Log.e(TAG, "read_chara=" + read_UUID_chara + "----read_service=" + read_UUID_service);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
          write_UUID_chara = characteristic.getUuid();
          write_UUID_service = bluetoothGattService.getUuid();
          Log.e(TAG, "write_chara=" + write_UUID_chara + "----write_service=" + write_UUID_service);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
          write_UUID_chara = characteristic.getUuid();
          write_UUID_service = bluetoothGattService.getUuid();
          Log.e(TAG, "write_chara=" + write_UUID_chara + "----write_service=" + write_UUID_service);

        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
          notify_UUID_chara = characteristic.getUuid();
          notify_UUID_service = bluetoothGattService.getUuid();
          Log.e(TAG,
              "notify_chara=" + notify_UUID_chara + "----notify_service=" + notify_UUID_service);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
          indicate_UUID_chara = characteristic.getUuid();
          indicate_UUID_service = bluetoothGattService.getUuid();
          Log.e(TAG, "indicate_chara=" + indicate_UUID_chara + "----indicate_service="
              + indicate_UUID_service);

        }
      }
    }
  }

//    private void addText(TextView textView, String content) {
//        textView.append(content);
//        textView.append("\n");
//        int offset = textView.getLineCount() * textView.getLineHeight();
//        if (offset > textView.getHeight()) {
//            textView.scrollTo(0, offset - textView.getHeight());
//        }
//    }

  private void writeData(String content) {
    BluetoothGattService service = mBluetoothGatt.getService(write_UUID_service);
    BluetoothGattCharacteristic charaWrite = service.getCharacteristic(write_UUID_chara);
    byte[] data;
//        if (!TextUtils.isEmpty(content)){
//            data=HexUtil.hexStringToBytes(content);
//        }else{
//            data=HexUtil.hexStringToBytes(hex);
//        }
    data = HexUtil.hexStringToBytes(content);
    if (data.length > 20) {//数据大于个字节 分批次写入
      Log.e(TAG, "writeData: length=" + data.length);
      int num = 0;
      if (data.length % 20 != 0) {
        num = data.length / 20 + 1;
      } else {
        num = data.length / 20;
      }
      for (int i = 0; i < num; i++) {
        byte[] tempArr;
        if (i == num - 1) {
          tempArr = new byte[data.length - i * 20];
          System.arraycopy(data, i * 20, tempArr, 0, data.length - i * 20);
        } else {
          tempArr = new byte[20];
          System.arraycopy(data, i * 20, tempArr, 0, 20);
        }
        charaWrite.setValue(tempArr);
        mBluetoothGatt.writeCharacteristic(charaWrite);
      }
    } else {
      charaWrite.setValue(data);
      mBluetoothGatt.writeCharacteristic(charaWrite);
    }
  }

  private static final String HEX = "0123456789abcdef";

  public static String bytes2hex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      // 取出这个字节的高4位，然后与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
      sb.append(HEX.charAt((b >> 4) & 0x0f));
      // 取出这个字节的低位，与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
      sb.append(HEX.charAt(b & 0x0f));
    }
    return sb.toString();
  }

  public static String onebyte2hex(byte bytes) {
    return String.valueOf(HEX.charAt((bytes >> 4) & 0x0f)) +
        HEX.charAt(bytes & 0x0f);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mBluetoothGatt.disconnect();
  }
}
