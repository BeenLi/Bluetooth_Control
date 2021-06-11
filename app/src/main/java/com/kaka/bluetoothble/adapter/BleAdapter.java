package com.kaka.bluetoothble.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kaka.bluetoothble.R;

import java.util.List;

public class BleAdapter extends BaseAdapter {

  private Context mContext;               // 一个抽象类, 可以访问应用特定的资源和类
  private List<BluetoothDevice> mBluetoothDevices;
  private List<Integer> mRssis;

  public BleAdapter(Context mContext, List<BluetoothDevice> bluetoothDevices, List<Integer> rssis) {
    this.mContext = mContext;
    this.mBluetoothDevices = bluetoothDevices;
    this.mRssis = rssis;
  }

  @Override
  public int getCount() {
    return mBluetoothDevices.size();
  }

  @Override
  public Object getItem(int position) {
    return mBluetoothDevices.get(position);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView = LayoutInflater.from(mContext)
          .inflate(R.layout.bluetooth_devices_list_item, null);
      viewHolder = new ViewHolder(convertView);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    BluetoothDevice device = (BluetoothDevice) getItem(position);
    viewHolder.name.setText(device.getName());
    viewHolder.introduce.setText(device.getAddress());
    viewHolder.tvRssi.setText(mRssis.get(position) + "");
    return convertView;
  }


  class ViewHolder {

    public TextView name;           // 蓝牙名称
    public TextView introduce;      // 蓝牙mac 地址
    public TextView tvRssi;         // 蓝牙信号强度

    public ViewHolder(View view) {
      name = view.findViewById(R.id.name);
      introduce = view.findViewById(R.id.introduce);
      tvRssi = view.findViewById(R.id.rssi);
    }
  }
}
