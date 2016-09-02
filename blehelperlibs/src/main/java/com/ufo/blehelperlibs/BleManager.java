package com.ufo.blehelperlibs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.ufo.blehelperlibs.callback.OnConnectCallback;
import com.ufo.blehelperlibs.callback.OnNotificationCallback;
import com.ufo.blehelperlibs.callback.OnReceiverCallback;
import com.ufo.blehelperlibs.callback.OnScanCallback;
import com.ufo.blehelperlibs.callback.OnWriteCallback;
import com.ufo.blehelperlibs.request.NotificationRequestQueue;
import com.ufo.blehelperlibs.request.ReceiverRequestQueue;
import com.ufo.blehelperlibs.request.WriterRequestQueue;
import com.ufo.blehelperlibs.utils.Md5Utils;
import com.ufo.blehelperlibs.utils.PrintUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Created by sz on 2016/8/31.
 * 描述:这是一个封装了对蓝牙Ble通信的一个管理类,此类为单例,可能过{@link BleManager#getInstance}
 * 获取该类的对象,然后对设备进行如:扫描{@link BleManager#scanBleDevices(int, OnScanCallback)};
 * 连接设备{@link BleManager#connection(int, String, OnConnectCallback)};断开连接{@link BleManager#disConnection()};
 * 设置通知{@link BleManager#notification(String, String, String, boolean, OnNotificationCallback)};
 * 写操作{@link BleManager#writer(String, String, String, OnWriteCallback)};
 * 接收设备以通知形式发过来的数据{@link BleManager#receiver(String, String, OnReceiverCallback)}等等
 */
public final class BleManager {
    private static final String TAG = "BleManager";
    /**
     * 默认扫描蓝牙设备的持续时间为5秒
     */
    private static final int SCAN_TIME = 1000 * 5;
    /**
     * 默认连接超时时间为6秒
     */
    private static final int CONNECTION_TIME_OUT = 1000 * 6;
    /**
     * 当前设备mac地址
     */
    private String mDevicesAddress;
    /**
     * 用户发起的连接是否已响应
     */
    private boolean isConnectResponse = false;
    private static BleManager sBleManager;
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private boolean isInit = false;
    private BluetoothAdapter mAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private BleGattCallback mGattCallback;
    private OnConnectCallback mOnConnectCallback;
    private BluetoothGatt mBluetoothGatt;
    private HashMap<String, Map<String, BluetoothGattCharacteristic>> servicesMap = new HashMap<>();
    private NotificationRequestQueue mNotificationRequestQueue = new NotificationRequestQueue();
    private WriterRequestQueue mWriterRequestQueue = new WriterRequestQueue();
    private ReceiverRequestQueue mReceiverRequestQueue = new ReceiverRequestQueue();


    private BleManager(Context context) {
        this.mContext = context;
        init();
    }

    /**
     * 调用此静态方法获取当前类的实例对象
     *
     * @param context 上下文
     * @return
     */
    public synchronized static BleManager getInstance(Context context) {
        if (null == sBleManager) {
            sBleManager = new BleManager(context);
        }
        return sBleManager;
    }

    /**
     * 初始化.获取完本类的实例后要调此方法进行初始化.
     */
    private void init() {
        if (null == mBluetoothManager) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (null == mBluetoothManager) {
                Log.d(TAG, "BluetoothManager init fail");
                isInit = false;
                return;
            }
        }

        mAdapter = mBluetoothManager.getAdapter();
        if (null == mAdapter) {
            Log.d(TAG, "BluetoothAdapter init fail");
            isInit = false;
            return;
        }

        mGattCallback = new BleGattCallback();
        isInit = true;

    }

    /**
     * 当前蓝牙是否打开
     *
     * @return 是返回true否则false
     */
    public boolean isEnable() {
        if (null != mAdapter) {
            return mAdapter.isEnabled();
        }
        return false;
    }

    /**
     * 返回GATT连接对象,可用它来进行扩展对和实现更多的对设备的操作
     * 功能,
     *
     * @return {@link BleManager#mBluetoothGatt}
     */
    public BluetoothGatt getBluetoothGatt() {
        if (null != mBluetoothGatt) {
            return mBluetoothGatt;
        }
        return null;
    }

    /**
     * 扫描设备,当传入的time值为0以下时默认扫描时间为5秒,{@link OnScanCallback}
     * 不能为空
     *
     * @param time           扫描的持续时间
     * @param onScanCallback 回调接口
     */
    public void scanBleDevices(int time, final OnScanCallback onScanCallback) {
        if (null == onScanCallback) {
            throw new IllegalArgumentException("OnScanCallback Can not null");
        }

        if (!isEnable()) {
            onScanCallback.onFailed(OnScanCallback.FAILED_BLUETOOTH_DISABLE);
            return;
        }

        final BleDeviceScanCallback bleDeviceScanCallback = new BleDeviceScanCallback(onScanCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //time后停止扫描
                mAdapter.stopLeScan(bleDeviceScanCallback);
                onScanCallback.onSuccess();
            }
        }, time <= 0 ? SCAN_TIME : time);

        mAdapter.startLeScan(bleDeviceScanCallback);
    }


    /**
     * ble蓝牙设备扫描回调
     */
    private class BleDeviceScanCallback implements BluetoothAdapter.LeScanCallback {
        private OnScanCallback mOnScanCallback;

        public BleDeviceScanCallback(OnScanCallback onScanCallback) {
            this.mOnScanCallback = onScanCallback;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (null != mOnScanCallback) {
                mOnScanCallback.onScanning(device, rssi, scanRecord);
            }
        }
    }

    /**
     * 连接
     *
     * @param connectionTimeOut 连接超时时间,默认是6秒.当赋值为0或更小值时用默认值
     * @param devicesAddress    要连接的设备的地址,
     * @param connectCallback   连接结果加调
     */
    public void connection(int connectionTimeOut, String devicesAddress, OnConnectCallback connectCallback) {
        if (TextUtils.isEmpty(devicesAddress)) {
            throw new IllegalArgumentException("device Address Can not null");
        }
        if (null == connectCallback) {
            throw new IllegalArgumentException("connectCallback can not null");
        }

        if (null == mAdapter) {
            connectCallback.onFailed(OnConnectCallback.UNKNOWN_ERROR);
            return;
        }

        if (!isEnable()) {
            connectCallback.onFailed(OnConnectCallback.FAILED_BLUETOOTH_DISABLE);
            return;
        }


        this.mOnConnectCallback = connectCallback;
        BluetoothDevice remoteDevice = mAdapter.getRemoteDevice(devicesAddress);
        if (null == remoteDevice) {
            Log.d(TAG, "RemoteDevice is null");
            mOnConnectCallback.onFailed(OnConnectCallback.INVALID_REMOTE_DEVICES);
            return;
        }
        if (null != mBluetoothGatt) {
            mBluetoothGatt.close();
        }

        mBluetoothGatt = remoteDevice.connectGatt(mContext, false, mGattCallback);
        mDevicesAddress = devicesAddress;
        Log.d(TAG, "连接远程蓝牙设备中...");
        delayConnectResponse(connectionTimeOut);

    }

    /**
     * 断开连接
     */
    public void disConnection() {
        if (null == mAdapter || null == mBluetoothGatt) {
            Log.d(TAG, "disconnection error maybe no init");
            return;
        }
        mBluetoothGatt.disconnect();
        Log.d(TAG, "disConnection Ble ");
    }

    /**
     * 复位
     */
    private void reset() {
        isConnectResponse = false;
        servicesMap.clear();
    }

    /**
     * 蓝牙GATT连接及操作事件回调
     */
    private class BleGattCallback extends BluetoothGattCallback {

        //连接成功,或是断开连接
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (mOnConnectCallback != null) {
                isConnectResponse = true;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mBluetoothGatt.discoverServices();
                    mOnConnectCallback.onSuccess(gatt);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mOnConnectCallback.onFailed(OnConnectCallback.CONNECTION_TIME_OUT);
                    reset();
                }
            }
        }

        //服务被发现了
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, gatt.getServices().size() + "");
            getDeviceServices();
        }

        //特征被写了
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "特征被写了:");
            String characterUUID = characteristic.getUuid().toString();
            String serviceUUID = characteristic.getService().getUuid().toString();
            String key = Md5Utils.MD5(serviceUUID.concat(characterUUID));
            OnWriteCallback callback = mWriterRequestQueue.get(key);
            if (null != callback) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    callback.onSuccess();
                } else {
                    callback.onFailed(OnWriteCallback.FAILED_OPERATION);
                }
                mWriterRequestQueue.removeRequest(key);
            }
        }

        //特征变化
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String characterUUID = characteristic.getUuid().toString();
            String serviceUUID = characteristic.getService().getUuid().toString();
            String key = Md5Utils.MD5(serviceUUID.concat(characterUUID));
            OnReceiverCallback callback = mReceiverRequestQueue.get(key);
            if (null != callback) {
                callback.onSuccess(characteristic.getValue());
            }

        }

        //描述符被写了
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "描述符被写了:" + descriptor.getValue().length);
            String descriptorUUID = descriptor.getUuid().toString();
            String characterUUID = descriptor.getCharacteristic().getUuid().toString();
            String serviceUUID = descriptor.getCharacteristic().getService().getUuid().toString();
            String key = Md5Utils.MD5(serviceUUID.concat(characterUUID).concat(descriptorUUID));
            OnNotificationCallback callback = mNotificationRequestQueue.get(key);
            if (null != callback) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    callback.onSuccess();
                } else {
                    callback.onFailed(OnNotificationCallback.FAILED_OPERATION);
                }
                mNotificationRequestQueue.removeRequest(key);
            }
        }

        //描述符被读了
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "描述符被读了");
        }
    }

    /**
     * 如果连接connectionTimeOut时间后还没有响应,手动关掉连接.
     *
     * @param connectionTimeOut
     */
    private void delayConnectResponse(int connectionTimeOut) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                responseConnectionFail();
            }
        }, connectionTimeOut <= 0 ? CONNECTION_TIME_OUT : connectionTimeOut);
    }

    /**
     * 响应连接失败
     */
    private synchronized void responseConnectionFail() {
        // TODO: 2016/8/31 待测,如果当前是在连接中,然后调用了,上面这行代码.不知道BluetoothGattCallback
        // TODO: 2016/8/31 onConnectionStateChange是否会被回调,如果不会应该要再执行下面这行代码.
        if (!isConnectResponse) {
            mBluetoothGatt.disconnect();
            mOnConnectCallback.onFailed(OnConnectCallback.CONNECTION_TIME_OUT);
            isConnectResponse = false;
        }
    }

    /**
     * 遍历服务里的所有,然后存到 {@code servicesMap}里
     */
    private void getDeviceServices() {
        if (null != mBluetoothGatt) {
            List<BluetoothGattService> services = mBluetoothGatt.getServices();

            int serviceSize = services.size();
            for (int i = 0; i < serviceSize; i++) {
                HashMap<String, BluetoothGattCharacteristic> charMap = new HashMap<>();
                BluetoothGattService bluetoothGattService = services.get(i);
                String serviceUuid = bluetoothGattService.getUuid().toString();
                List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                int characteristicSize = characteristics.size();
                for (int j = 0; j < characteristicSize; j++) {
                    charMap.put(characteristics.get(j).getUuid().toString(), characteristics.get(j));
                }
                servicesMap.put(serviceUuid, charMap);
            }
            PrintUtils.printCharacteristic(servicesMap);
        }
    }


    /**
     * 通知设置
     *
     * @param serviceUUID    服务UUID
     * @param characterUUID  特征UUID
     * @param descriptorUUID 描述符UUID
     * @param enable         true为开启通知false为关闭通知
     */
    public void notification(String serviceUUID, String characterUUID,
                             String descriptorUUID, boolean enable, OnNotificationCallback callback) {
        if (null == callback) {
            throw new IllegalArgumentException("OnNotificationCallback can not null");
        }
        if (TextUtils.isEmpty(serviceUUID) || TextUtils.isEmpty(characterUUID) || TextUtils.isEmpty(descriptorUUID)) {
            throw new IllegalArgumentException("ServiceUUID and CharacterUUID and descriptorUUID can not null");
        }
        if (!isEnable()) {
            callback.onFailed(OnNotificationCallback.FAILED_BLUETOOTH_DISABLE);
            return;
        }
        /**添加进队列 key是serviceUUID + characterUUID + descriptorUUID的md5 值*/
        String requestKey = Md5Utils.MD5(serviceUUID.concat(characterUUID).concat(descriptorUUID));
        mNotificationRequestQueue.set(requestKey, callback);

        //找特征
        BluetoothGattCharacteristic gattCharacteristic = getBluetoothGattCharacteristic(serviceUUID, characterUUID);
        if (null == gattCharacteristic) {
            callback.onFailed(OnNotificationCallback.FAILED_INVALID_CHARACTER);
            mNotificationRequestQueue.removeRequest(requestKey);
            return;
        }

        //找描述符
        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(UUID.fromString(descriptorUUID));
        if (null == descriptor) {
            callback.onFailed(OnNotificationCallback.FAILED_INVALID_DESCRIPTOR);
            mNotificationRequestQueue.removeRequest(requestKey);
            return;
        }

        //设置通知
        mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, enable);
        if (enable) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        boolean notifyResult = mBluetoothGatt.writeDescriptor(descriptor);
        if (!notifyResult) {
            callback.onFailed(OnNotificationCallback.FAILED_OPERATION);
            mNotificationRequestQueue.removeRequest(requestKey);
        }
        Log.d(TAG, " 通知设置结果:" + notifyResult);

    }

    /**
     * 写操作
     *
     * @param serviceUUID   服务的UUID
     * @param characterUUID 特征的UUID
     * @param data          要写的数据
     * @param callback      回调结果
     */
    public void writer(String serviceUUID, String characterUUID, String data, OnWriteCallback callback) {
        if (null == callback) {
            throw new IllegalArgumentException("OnWriteCallback can not null");
        }
        if (TextUtils.isEmpty(serviceUUID) || TextUtils.isEmpty(characterUUID)) {
            throw new IllegalArgumentException("ServiceUUID and CharacterUUID  can not null");
        }
        if (TextUtils.isEmpty(data)) {
            throw new IllegalArgumentException("data can not null");
        }
        if (!isEnable()) {
            callback.onFailed(OnWriteCallback.FAILED_BLUETOOTH_DISABLE);
            return;
        }

        /**添加进队列 key是serviceUUID + characterUUID 的md5 值*/
        String requestKey = Md5Utils.MD5(serviceUUID.concat(characterUUID));
        mWriterRequestQueue.set(requestKey, callback);

        //找特征
        BluetoothGattCharacteristic gattCharacteristic = getBluetoothGattCharacteristic(serviceUUID, characterUUID);
        if (null == gattCharacteristic) {
            callback.onFailed(OnWriteCallback.FAILED_INVALID_CHARACTER);
            mWriterRequestQueue.removeRequest(requestKey);
            return;
        }

        //写
        if (null == mBluetoothGatt) {
            callback.onFailed(OnWriteCallback.FAILED_OPERATION);
            return;
        }

        gattCharacteristic.setValue(data);
        boolean writeResult = mBluetoothGatt.writeCharacteristic(gattCharacteristic);
        if (!writeResult) {
            callback.onFailed(OnWriteCallback.FAILED_OPERATION);
            mWriterRequestQueue.removeRequest(requestKey);
        }
        Log.d(TAG, " 写操作结果:" + writeResult);
    }

    /**
     * 接收通知数据
     *
     * @param serviceUUID   服务UUID
     * @param characterUUID 特征UUID
     * @param callback      回调接口
     */
    public void receiver(String serviceUUID, String characterUUID, OnReceiverCallback callback) {
        if (TextUtils.isEmpty(serviceUUID) || TextUtils.isEmpty(characterUUID)) {
            throw new IllegalArgumentException("ServiceUUID and CharacterUUID  can not null");
        }
        if (null == callback) {
            throw new IllegalArgumentException("OnReceiverCallback can not null");
        }

        if (!isEnable()) {
            callback.onFailed(OnReceiverCallback.FAILED_BLUETOOTH_DISABLE);
            return;
        }
        //加入队列
        String requestKey = Md5Utils.MD5(serviceUUID.concat(characterUUID));
        mReceiverRequestQueue.set(requestKey, callback);
    }

    /**
     * 不接收数据,与{@code receiver}相反,从队列中移除监听
     *
     * @param serviceUUID   服务UUID
     * @param characterUUID 特征UUID
     */
    public void unReceiver(String serviceUUID, String characterUUID) {
        if (TextUtils.isEmpty(serviceUUID) || TextUtils.isEmpty(characterUUID)) {
            throw new IllegalArgumentException("ServiceUUID and CharacterUUID  can not null");
        }
        //移出队列
        String requestKey = Md5Utils.MD5(serviceUUID.concat(characterUUID));
        mReceiverRequestQueue.removeRequest(requestKey);
    }

    /**
     * 根据服务UUID和特征UUID,获取一个特征{@link BluetoothGattCharacteristic}
     *
     * @param serviceUUID   服务UUID
     * @param characterUUID 特征UUID
     */
    public BluetoothGattCharacteristic getBluetoothGattCharacteristic(String serviceUUID, String characterUUID) {
        if (TextUtils.isEmpty(serviceUUID) || TextUtils.isEmpty(characterUUID)) {
            throw new IllegalArgumentException("ServiceUUID and CharacterUUID  can not null");
        }
        if (!isEnable()) {
            throw new IllegalArgumentException(" Bluetooth is no enable please call BluetoothAdapter.enable()");
        }
        if (null == mBluetoothGatt) {
            return null;
        }

        //找服务
        Map<String, BluetoothGattCharacteristic> bluetoothGattCharacteristicMap = servicesMap.get(serviceUUID);
        if (null == bluetoothGattCharacteristicMap) {
            return null;
        }

        //找特征
        Set<Map.Entry<String, BluetoothGattCharacteristic>> entries = bluetoothGattCharacteristicMap.entrySet();
        BluetoothGattCharacteristic gattCharacteristic = null;
        for (Map.Entry<String, BluetoothGattCharacteristic> entry : entries) {
            if (characterUUID.equals(entry.getKey())) {
                gattCharacteristic = entry.getValue();
                break;
            }
        }

        return gattCharacteristic;

    }


}
