---
typora-root-url: ./
---

# Bluetooth_Control

An android app that used to control the embedded car using Bluetooth low energy(BLE)

一个使用低功耗蓝牙去控制嵌入式小车的安卓软件

## 1、demo

![蓝牙连接成功后](/res/1.png)



demo视频：[/res/终极demo.mp4]https://github.com/BeenLi/Bluetooth_Control/blob/main/res/%E7%BB%88%E6%9E%81demo.mp4

## 2、UI草图

![主界面UI](/res/软件布局草图.png)

> 使用相对布局将整体分为三部分：扫描按钮，连接状态，蓝牙设备列表/控制按钮

![蓝牙设备列表样式](/res/list_view.png)

> 使用权重线性布局：首先分为左右两部分，然后将左边分为上下两部分。

## 3、软件逻辑

![整体逻辑](/res/软件整体逻辑.png)

> 分为三步走：申请权限、连接蓝牙、蓝牙通信

![软件架构](/res/软件逻辑.png)

![重要的API](/res/重要的API.png)

- 申请权限获得蓝牙句柄---btAdapter

- 扫描设备回调获得----bluetoothDevice

- 连接蓝牙设备获得----mBluetoothGatt

- 写数据/读数据 --- getServices/getCharateristic

  
