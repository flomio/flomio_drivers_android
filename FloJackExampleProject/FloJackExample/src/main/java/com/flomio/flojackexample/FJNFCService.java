/** Copyright (C) 2009 by Aleksey Surkov.
 **
 **  Modified by Richard Grundy on 8/6/13.
 **  Flomio, Inc.
 */

package com.flomio.flojackexample;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.util.Log;

public class FJNFCService extends Thread {
//    private static final int ZERO_TO_ONE_THRESHOLD = 0;        // threshold used to detect start bit
//
//    private static final int SAMPLESPERBIT = 13;               // (44100 / HIGHFREQ)  // how many samples per UART bit
//    private static final int SHORT =  (SAMPLESPERBIT/2 + SAMPLESPERBIT/4);
//    private static final int LONG =  (SAMPLESPERBIT + SAMPLESPERBIT/2);
//
//    private static final int HIGHFREQ = 3392;                  // baud rate. best to take a divisible number for 44.1kS/s
//    private static final int LOWFREQ = (HIGHFREQ / 2);
//
//    private static final int NUMSTOPBITS = 20;                 // number of stop bits to send before sending next value.
//    private static final int NUMSYNCBITS = 4;                  // number of ones to send before sending first value.
//
//    private static final int SAMPLE_NOISE_CEILING = 100000;    // keeping running average and filter out noisy values around 0
//    private static final int SAMPLE_NOISE_FLOOR = -100000;     // keeping running average and filter out noisy values around 0
//
//    private static final double MESSAGE_SYNC_TIMEOUT = 0.500;  // seconds

    private String LOG_TAG = "FJNFCService";
    private FJNFCListener mListener;
    private AudioRecord remoteIOUnit;

    private final static int RATE = 44100;
    private final static int BUFFER_SIZE = 0x1000;
    private final static int BUFFER_SIZE_IN_MS = 3000;
    private final static int CHUNK_SIZE_IN_SAMPLES = 4096; // = 2 ^
    private final static int CHUNK_SIZE_IN_MS = 1000 * CHUNK_SIZE_IN_SAMPLES / RATE;
    private final static int BUFFER_SIZE_IN_BYTES = RATE * BUFFER_SIZE_IN_MS / 1000 * 2;
    private final static int CHUNK_SIZE_IN_BYTES = RATE * CHUNK_SIZE_IN_MS / 1000 * 2;

    private enum uart_state {
        STARTBIT,               //(0)
        SAMEBIT,                //(1)
        NEXTBIT,                //(2)
        STOPBIT,                //(3)
        STARTBIT_FALL,          //(4)
        DECODE_BYTE_SAMPLE      //(5)
    }

    public FJNFCService(FJNFCListener listener) {
        super();
        mListener = listener;
    }

    public interface FJNFCListener {
        public void onData(String parsedData);
        public void onError(String error);
    }

    private void floJackAURenderCallback() {
    }

    public void run() {
        Log.i(LOG_TAG, "Starting to capture samples");

        android.os.Process
                .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        int bufferSize = BUFFER_SIZE;
        final int minBufferSize = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (minBufferSize > bufferSize) {
            bufferSize = minBufferSize;
        }
        remoteIOUnit = new AudioRecord(AudioSource.MIC, RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        if (remoteIOUnit.getState() != AudioRecord.STATE_INITIALIZED) {
            mListener.onError("Can't initialize AudioRecord");  // Needed <uses-permission android:name="android.permission.RECORD_AUDIO"/>
            return;
        }

        remoteIOUnit.startRecording();
        while (!Thread.interrupted()) {
            short[] audioData = new short[BUFFER_SIZE_IN_BYTES / 2];
            remoteIOUnit.read(audioData, 0, CHUNK_SIZE_IN_BYTES / 2);
//            String parsedData = handleReceivedByte(audioData);
//            PostToUI(parsedData);
        }
        remoteIOUnit.release();
    }
//    private void handleReceivedByte(byte myByte, boolean parityGood, double timestamp){
//
//    }
//    private void sendFloJackConnectedStatusToDelegate() {
//
//    }
//    private void clearMessageBuffer() {
//
//    }
//    private void checkIfVolumeLevelMaxAndNotifyDelegate() {
//
//    }
//    private void disableDeviceSpeakerPlayback() {
//
//    }
//    private void enableDeviceSpeakerPlayback() {
//
//    }
//    private void sendByteToHost(byte theByte) {
//
//    }
//    private void sendMessageDataToHost(ByteBuffer messageData) {
//
//    }
//    private void setOutputAmplitudeHigh() {
//
//    }
//    private void setOutputAmplitudeNormal() {
//
//    }
//    public byte getDeviceInterByteDelay() {
//
//    }
//    public byte getDeviceLogicOneValue() {
//
//    }
//    public byte getDeviceLogicZeroValue() {
//
//    }
}
