package com.vcreate.ecg.bluetooth.data;


import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class DataParser {
    public String TAG = "DataParser";

    //Buffer queue
    private LinkedBlockingQueue<Integer> bufferQueue = new LinkedBlockingQueue<Integer>(2024);
    private int[] PACKAGE_HEAD = new int[]{0x55, 0xaa};
    private final int PKG_ECG_WAVE = 0x01;
    private final int PKG_ECG_PARAMS = 0x02;
    private final int PKG_NIBP = 0x03;
    private final int PKG_SPO2_PARAMS = 0x04;
    private final int PKG_TEMP = 0x05;
    private final int PKG_SW_VER = 0xfc;
    private final int PKG_HW_VER = 0xfd;
    private final int PKG_SPO2_WAVE = 0xfe;

//    spo2 - start - 0x55 0xAA 0x04 0x03 0x01, 0xf7
//    nibp - start - 0x55 0xAA 0x04 0x02 0x01 0xf8
//    ecg  - start - 0x55 0xAA 0x04 0x01 0x01 0xF9
//    ecg  - stop  - 0x55 0xAA 0x04 0x01 0x00 0xF9
    public static byte[] CMD_START_NIBP = new byte[]{0x55, (byte) 0xaa, 0x04, 0x02, 0x01, (byte) 0xf8};
    public static byte[] CMD_STOP_NIBP = new byte[]{0x55, (byte) 0xaa, 0x04, 0x02, 0x00, (byte) 0xf9};
    public static byte[] CMD_START_ECG = new byte[]{0x55, (byte) 0xaa, 0x04, 0x01, 0x01, (byte) 0xf9};
    public static byte[] CMD_STOP_ECG = new byte[]{0x55, (byte) 0xaa, 0x04, 0x01, 0x00, (byte) 0xf9};

    public static byte[] CMD_START_SPO2 = new byte[]{0x55, (byte) 0xaa, 0x04, 0x03, 0x01, (byte) 0xf7};
    public static byte[] CMD_FW_VERSION = new byte[]{0x55, (byte) 0xaa, 0x04, (byte) 0xfc, 0x00, (byte) 0xff};
    public static byte[] CMD_HW_VERSION = new byte[]{0x55, (byte) 0xaa, 0x04, (byte) 0xfd, 0x00, (byte) 0xfe};

    //Parse Runnable
    private ParseRunnable mParseRunnable;
    private boolean isStop = true;
    private onPackageReceivedListener mListener;


    private OnEcgReceivedListener1 onEcgReceivedListener1;
    private OnEcgReceivedListener2 onEcgReceivedListener2;

    private OnVideoCallDataPackageReceiveListener onVideoCallDataPackageReceiveListener;

    private OnNibpReceivedListener onNibpReceivedListener;

    private OnSpo2ReceivedListener onSpo2ReceivedListener;

    private OnCheckupDataReceivedListener onCheckupDataReceivedListener;

    public DataParser(OnCheckupDataReceivedListener onCheckupDataReceivedListener) {
        this.onCheckupDataReceivedListener = onCheckupDataReceivedListener;
    }

    public DataParser(onPackageReceivedListener listener) {
        this.mListener = listener;
    }

    public DataParser(OnVideoCallDataPackageReceiveListener onVideoCallDataPackageReceiveListener) {
        this.onVideoCallDataPackageReceiveListener = onVideoCallDataPackageReceiveListener;
    }

    public DataParser(OnEcgReceivedListener1 onEcgReceivedListener1, OnEcgReceivedListener2 onEcgReceivedListener2) {
        this.onEcgReceivedListener1 = onEcgReceivedListener1;
        this.onEcgReceivedListener2 = onEcgReceivedListener2;
    }

    public DataParser(OnNibpReceivedListener onNibpReceivedListener) {
        this.onNibpReceivedListener = onNibpReceivedListener;
    }

    public DataParser(OnSpo2ReceivedListener onSpo2ReceivedListener) {
        this.onSpo2ReceivedListener = onSpo2ReceivedListener;
    }

    public void start() {
        Log.d(TAG, "Inside Data Parser Start Method");
        mParseRunnable = new ParseRunnable();
        new Thread(mParseRunnable).start();
    }

    public void stop() {
        isStop = true;
    }


    class ParseRunnable implements Runnable {
        int dat;
        int[] packageData;

        @Override
        public void run() {
            int count = 0;
            while (isStop) {
                dat = getData();
                Log.d(TAG, "Inside parse method" + dat);
                if (dat == PACKAGE_HEAD[0]) {
                    dat = getData();
                    if (dat == PACKAGE_HEAD[1]) {
                        int packageLen = getData();
                        packageData = new int[packageLen + PACKAGE_HEAD.length];

                        packageData[0] = PACKAGE_HEAD[0];
                        packageData[1] = PACKAGE_HEAD[1];

                        if (packageLen > 0) {
                            packageData[2] = packageLen;

                            for (int i = 3; i < packageData.length; i++) {
                                packageData[i] = getData();
                            }

                            if (CheckSum(packageData)) {
                                count++;
                                ParsePackage(packageData);
                            } else {
                                Log.d(TAG, "else of parse package");
                            }
                        } else {
                            Log.d(TAG, "Package length is 0, skipping data parsing");
                        }
                    } else {
                        Log.d(TAG, "data is not packgage head 1");
                    }
                } else {
                    Log.d(TAG, "data is not package head 0");
                }
            }

            Log.d("TestingSize", "Size of Recorded data of "+count);
        }
    }

    private void ParsePackage(int[] pkgData) {
        int pkgType = pkgData[3];
        Log.d(TAG, "Pkg Data: " + Arrays.toString(pkgData));

        switch (pkgType) {
            case PKG_ECG_WAVE:
                try {
                    if (onEcgReceivedListener1 != null && onEcgReceivedListener2 != null) {
                        onEcgReceivedListener1.onEcgWave1Received(pkgData[4]);
                        onEcgReceivedListener1.onEcgWave2Received(pkgData[5]);
                        onEcgReceivedListener1.onEcgWave3Received(pkgData[6]);

                        onEcgReceivedListener2.onEcgWave1Received(pkgData[4]);
                        onEcgReceivedListener2.onEcgWave2Received(pkgData[5]);
                        onEcgReceivedListener2.onEcgWave3Received(pkgData[6]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                 }
                break;

            case PKG_SPO2_WAVE:
                if (mListener != null) {
                    mListener.onSpO2WaveReceived(pkgData[4]);
                }
                if (onCheckupDataReceivedListener != null) {
                    onCheckupDataReceivedListener.onSpO2ReceivedWave(pkgData[4]);
                }
                break;
            case PKG_ECG_PARAMS:
                int status = pkgData[4];
                int heartRate = pkgData[5];
                int restRate = pkgData[6];
                int stLevel = pkgData[7];
                int arrCode = pkgData[8];
                ECG params = new ECG(heartRate, restRate, status, stLevel, arrCode);
                if (onEcgReceivedListener1 != null && onEcgReceivedListener2 != null) {
                    onEcgReceivedListener1.onEcgReceived(params);
                    onEcgReceivedListener2.onEcgReceived(params);
                }
                if (onVideoCallDataPackageReceiveListener != null) {
                    onVideoCallDataPackageReceiveListener.onEcgReceived(params);
                }
                break;
            case PKG_NIBP:
                NIBP params2 = new NIBP(pkgData[6], pkgData[7], pkgData[8], pkgData[5] * 2, pkgData[4]);
                if (mListener != null) {
                    mListener.onNIBPReceived(params2);
                }
                if (onVideoCallDataPackageReceiveListener != null) {
                    Log.d("DataParserTagNibp", "Sending data return to ui "+params2);
                    onVideoCallDataPackageReceiveListener.onNIBPReceived(params2);
                }

                if (onNibpReceivedListener != null) {
                    onNibpReceivedListener.onNIBPReceived(params2);
                }

                if (onCheckupDataReceivedListener != null) {
                    onCheckupDataReceivedListener.onNIBPReceived(params2);
                }
                break;
            case PKG_SPO2_PARAMS:
                SpO2 params3 = new SpO2(pkgData[5], pkgData[6], pkgData[4]);
                if (mListener != null) {
                    mListener.onSpO2Received(params3);
                }
                if (onVideoCallDataPackageReceiveListener != null) {
                    Log.d("DataParserTag", "Sending data return to ui "+params3);
                    onVideoCallDataPackageReceiveListener.onSpO2Received(params3);
                }

                if (onSpo2ReceivedListener !=  null) {
                    onSpo2ReceivedListener.onSpO2Received(params3);
                }

                if (onCheckupDataReceivedListener != null) {
                    onCheckupDataReceivedListener.onSpO2Received(params3);
                }
                break;
            case PKG_TEMP:
                Temp params4 = new Temp((pkgData[5] * 10 + pkgData[6]) / 10.0, pkgData[4]);
                if (mListener != null) {
                    mListener.onTempReceived(params4);
                }
                break;
            case PKG_SW_VER:
                StringBuilder sb = new StringBuilder();
                for (int i = 4; i < pkgData.length - 1; i++) {
                    sb.append((char) (pkgData[i] & 0xff));
                }
                if (mListener != null) {
                    mListener.onFirmwareReceived(sb.toString());
                }
                break;
            case PKG_HW_VER:
                StringBuilder sb1 = new StringBuilder();
                for (int i = 4; i < pkgData.length - 1; i++) {
                    sb1.append((char) (pkgData[i] & 0xff));
                }
                if (mListener != null) {
                    mListener.onHardwareReceived(sb1.toString());
                }
                break;
            default:
                Log.d(TAG, "Data irrelevant");
                break;
        }

    }

    public void add(byte[] dat) {
        for (byte b : dat) {
            try {
                bufferQueue.put(toUnsignedInt(b));
            } catch (InterruptedException e) {
                Log.d(TAG, "tag _debugging" + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private int getData() {
        int dat = 0;
        try {
            dat = bufferQueue.take();
        } catch (InterruptedException e) {
            Log.d(TAG, String.valueOf(e));
        }
        return dat;
    }

    private boolean CheckSum(int[] packageData) {
        Log.d(TAG, "Inside checksum ");
        int sum = 0;
        for (int i = 2; i < packageData.length - 1; i++) {
            sum += (packageData[i]);
        }

        if (((~sum) & 0xff) == (packageData[packageData.length - 1] & 0xff)) {
            return true;
        }

        return false;
    }

    private int toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }

    public interface onPackageReceivedListener {
        void onSpO2WaveReceived(int dat);

        void onSpO2Received(SpO2 spo2);

        void onTempReceived(Temp temp);

        void onNIBPReceived(NIBP nibp);

        void onFirmwareReceived(String str);

        void onHardwareReceived(String str);
    }


    public interface OnVideoCallDataPackageReceiveListener {
        void onSpO2Received(SpO2 spo2);
        void onNIBPReceived(NIBP nibp);

        void onEcgReceived(ECG ecg);
    }


    public interface OnEcgReceivedListener1 {
        void onEcgReceived(ECG ecg);
        void onEcgWave1Received(int dat);
        void onEcgWave2Received(int dat);
        void onEcgWave3Received(int dat);
    }


    public interface OnEcgReceivedListener2 {
        void onEcgReceived(ECG ecg);
        void onEcgWave1Received(int dat);
        void onEcgWave2Received(int dat);
        void onEcgWave3Received(int dat);
    }


    public interface OnNibpReceivedListener {
        void onNIBPReceived(NIBP nibp);
    }


    public interface OnCheckupDataReceivedListener {
        void onNIBPReceived(NIBP nibp);
        void onSpO2Received(SpO2 spo2);
        void onSpO2ReceivedWave(int data);
    }

    public interface OnSpo2ReceivedListener {
        void onSpO2Received(SpO2 spo2);
        void onSpo2WaveReceived(int data);
    }



}
