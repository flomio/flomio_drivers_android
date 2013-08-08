package com.flomio.flojackexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* Created by grundyoso on 8/6/13.
*/

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

    // Currently, only this combination of rate, encoding and channel mode
    // actually works.
    private final static int RATE = 8000;
    private final static int CHANNEL_MODE = AudioFormat.CHANNEL_IN_MONO;
    private final static int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private final static int BUFFER_SIZE_IN_MS = 3000;
    private final static int CHUNK_SIZE_IN_SAMPLES = 4096; // = 2 ^
    // CHUNK_SIZE_IN_SAMPLES_POW2
    private final static int CHUNK_SIZE_IN_MS = 1000 * CHUNK_SIZE_IN_SAMPLES
            / RATE;
    private final static int BUFFER_SIZE_IN_BYTES = RATE * BUFFER_SIZE_IN_MS
            / 1000 * 2;
    private final static int CHUNK_SIZE_IN_BYTES = RATE * CHUNK_SIZE_IN_MS
            / 1000 * 2;

    private final static int MIN_FREQUENCY = 49; // 49.0 HZ of G1 - lowest note
    // for crazy Russian choir.
    private final static int MAX_FREQUENCY = 1568; // 1567.98 HZ of G6 - highest
    // demanded note in the
    // classical repertoire

    private final static int DRAW_FREQUENCY_STEP = 5;

    public native void DoFFT(double[] data, int size); // an NDK library
    // 'fft-jni'

//    //id <FJNFCServiceDelegate>	 _delegate;
//    //dispatch_queue_t             _backgroundQueue;
//
//    // Audio Unit attributes
//    //AURenderCallbackStruct		 _audioUnitRenderCallback;
//    private double              hwSampleRate;
//    private int 				maxFPS;
//    //DCRejectionFilter			*remoteIODCFilter;
//    //AudioUnit					 _remoteIOUnit;
//    //CAStreamBasicDescription	 _remoteIOOutputFormat;
//    private byte                 ignoreRouteChangeCount;
//
//    // NFC Service state variables
//    private byte                byteForTX;
//    private boolean				byteQueuedForTX;
//    private boolean             currentlySendingMessage;
//    private boolean             muteEnabled;
//
//    // Logic Values
//    private byte                logicOne;
//    private byte                logicZero;
//
//    // Message handling variables
//    private double              lastByteReceivedAtTime;
//    private byte                messageCRC;
//    private ByteBuffer          messageReceiveBuffer;
//    private int                 messageLength;
//    private boolean             messageValid;

    private Activity hostActivity;
    private Handler hostProcessQueue;
    private AudioRecord remoteIOUnit;

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

    public void initialize() {
//        int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//        AudioRecord remoteIOUnit = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                44100, AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
//        remoteIOUnit.setPositionNotificationPeriod(SAMPLESPERBIT);
//        remoteIOUnit.startRecording();
//        remoteIOUnit.stop();
//        remoteIOUnit.release();
    }
    private void floJackAURenderCallback() {
    }

    public void run() {
        Log.i(LOG_TAG, "Starting to capture samples");

        android.os.Process
                .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        remoteIOUnit = new AudioRecord(MediaRecorder.AudioSource.MIC, RATE, CHANNEL_MODE,
                ENCODING, 6144);
        if (remoteIOUnit.getState() != AudioRecord.STATE_INITIALIZED) {
            mListener.onError("Can't initialize AudioRecord");
            return;
        }

        remoteIOUnit.startRecording();
        while (!Thread.interrupted()) {
            short[] audioData = new short[BUFFER_SIZE_IN_BYTES / 2];
            remoteIOUnit.read(audioData, 0, CHUNK_SIZE_IN_BYTES / 2);
//            String parsedData = handleReceivedByte(audioData);
//            PostToUI(parsedData);
        }
        remoteIOUnit.stop();
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
