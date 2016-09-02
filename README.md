# BleHelperLibs
一个封装蓝牙4.0的帮助库

  
###1.
  
         //监听设备以通知方式发过来的数据
        mBleManager = BleManager.getInstance(this);
        mBleManager.receiver("00001950-0000-1000-8000-00805f9b34fb",
                "00002a6c-0000-1000-8000-00805f9b34fb", new OnReceiverCallback() {
                    @Override
                    public void onSuccess(byte[] value) {
                        Log.d(TAG," 接收到的数据:" + new String(value));
                    }

                    @Override
                    public void onFailed(int state) {
                         Log.d(TAG," 接收数据失败" + state);
                    }
                });
                
###2.                
                
                 //扫描设备
            mBleManager.scanBleDevices(0, new OnScanCallback() {
                @Override
                public void onSuccess() {
                     Log.d(TAG," 扫描完成");
                }

                @Override
                public void onFailed(int fail) {
                     Log.d(TAG,"扫描失败" + fail);
                }

                @Override
                public void onScanning(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    //每扫描一个设备回调一次此方法
                     Log.d(TAG," 设备:" + device.getName() + "地址: " + device.getAddress());
                }
            });
            
###3.
            
            //连接设备
            mBleManager.connection(0, "CB:A5:AF:B8:AD:58", new OnConnectCallback() {
                @Override
                public void onSuccess(BluetoothGatt gatt) {
                    Log.d(TAG," 连接成功");
                }

                @Override
                public void onFailed(int state) {
                    Log.d(TAG," 连接断开: " + state);
                }
            });
            
###4.
           
            //设置通知,true为打开通知,false为关
            mBleManager.notification("00001950-0000-1000-8000-00805f9b34fb",
                    "00002a6c-0000-1000-8000-00805f9b34fb",
                    "00002902-0000-1000-8000-00805f9b34fb", true, new OnNotificationCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG," 通知设置成功");
                        }

                        @Override
                        public void onFailed(int state) {
                             Log.d(TAG," 通知设置失败代码为:" + state);
                        }
                    });
                    
###5.       
             
                     //写入数据
            mBleManager.writer("00001950-0000-1000-8000-00805f9b34fb",
                    "00002a6d-0000-1000-8000-00805f9b34fb",
                    "{S5:1}", new OnWriteCallback() {
                        @Override
                        public void onSuccess() {
                             Log.d(TAG," 写成功");
                        }

                        @Override
                        public void onFailed(int state) {
                            Log.d(TAG," 写失败:" + state);
                        }
                    });
###6.                   
                    
                     //移除接收数据
            mBleManager.unReceiver("00001950-0000-1000-8000-00805f9b34fb", "00002a6c-0000-1000-8000-00805f9b34fb");
###7.           
            
             //获取一个特征
             mBleManager.getBluetoothGattCharacteristic("00001950-0000-1000-8000-00805f9b34fb","00002a6c-0000-1000-8000-00805f9b34fb");
###8.          
             //断开连接
            mBleManager.disConnection();
            
            
