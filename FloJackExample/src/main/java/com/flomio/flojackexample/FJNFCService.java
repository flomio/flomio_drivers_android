/** Copyright (C) 2009 by Aleksey Surkov.
 **
 **  Modified by Richard Grundy on 8/6/13.
 **  Flomio, Inc.
 */

package com.flomio.flojackexample;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Build;
import android.os.Process;
import android.util.FloatMath;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class FJNFCService extends IntentService {
    private final static int ZERO_TO_ONE_THRESHOLD = 0;        // threshold used to detect start bit
    private final static int SAMPLESPERBIT = 13;               // (44100 / HIGHFREQ)  // how many samples per UART bit
    private final static int SHORT =  (SAMPLESPERBIT/2 + SAMPLESPERBIT/4);
    private final static int LONG =  (SAMPLESPERBIT + SAMPLESPERBIT/2);
    private final static float HIGHFREQ = 3392;                  // baud rate. best to take a divisible number for 44.1kS/s
    private final static float LOWFREQ = (HIGHFREQ / 2);
    private final static int NUMSTOPBITS = 20;                 // number of stop bits to send before sending next value.
    private final static int NUMSYNCBITS = 4;                  // number of ones to send before sending first value.
    private final static int SAMPLE_NOISE_CEILING = 100000;    // keeping running average and filter out noisy values around 0
    private final static int SAMPLE_NOISE_FLOOR = -100000;     // keeping running average and filter out noisy values around 0
//    private final static double MESSAGE_SYNC_TIMEOUT = 0.500;  // seconds
    private final static int RATE = 44100;

    // Audio Unit attributes
    private AudioRecord remoteInputUnit;
    private AudioTrack remoteOutputUnit;
    private float hwSampleRate;
    private long processingTime = 0;
    private byte logicOne, logicZero;
    private int outputAmplitude;

    // Audio Unit attributes shared across classes
    private int inNumberFrames = 0; // a frame is composed of one mic audio sample. Stereo mics can have 2 channels
    private int outNumberFrames = 0; // a frame is composed of one audio sample. Stereo can have 2 channels
    private ByteBuffer inAudioData;
    private ByteBuffer outAudioData;
    private long timeTracker;

    // NFC Service state variables
    private byte byteForTX;
    private boolean byteQueuedForTX;
    private boolean currentlySendingMessage;
    private boolean muteEnabled;

    // Message handling variables
    private double lastByteReceivedAtTime;
    private byte messageCRC;
    private int[] messageReceiveBuffer;
    private int messageLength;
    private boolean messageValid;

    private enum uart_state {
        STARTBIT,               //(0)
        SAMEBIT,                //(1)
        NEXTBIT,                //(2)
        STOPBIT,                //(3)
        STARTBIT_FALL,          //(4)
        DECODE_BYTE_SAMPLE      //(5)
    }

    private String LOG_TAG = "FJNFCService";

    public FJNFCService() {
        super("FJNFCService");
        Log.d(LOG_TAG, "constructor called.");

        // Logic high/low varies based on host device
        logicOne = getDeviceLogicOneValue();
        logicZero = getDeviceLogicZeroValue();
        muteEnabled = false;
        outputAmplitude = (1<<24);  // TODO: create accessor for setting normal and high amplitude
    }

    @Override
    public void onStart(Intent intent,  int startId) {
        super.onStart(intent, startId);
        Log.d(LOG_TAG, "onStart called");
        new FJNFCListener().start();
    }

    @Override
    public void onHandleIntent(Intent intent) {
        try{
            Log.d(LOG_TAG,"handle intent called");

        }catch(Exception e){

        }
    }

    public class FJNFCListener extends Thread {

        public void run() {
            Log.i(LOG_TAG, "starting thread to capture samples");
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            outNumberFrames = AudioTrack.getMinBufferSize(RATE, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            remoteOutputUnit = new AudioTrack(AudioManager.STREAM_DTMF, RATE,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, outNumberFrames,
                    AudioTrack.MODE_STREAM);
            if (remoteOutputUnit.getState() != AudioTrack.STATE_INITIALIZED) {
                Log.e(LOG_TAG, "can't initialize AudioTrack");  // TODO needs some permissions?
                return;
            }
            remoteOutputUnit.setPositionNotificationPeriod(outNumberFrames);
            remoteOutputUnit.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onPeriodicNotification(AudioTrack track) {
                    Log.d(LOG_TAG, "notified by AudioTrack subsystem");

                    floJackAUOutputCallback(FJNFCService.this.outAudioData.asIntBuffer());
                    processingTime = (System.currentTimeMillis() - FJNFCService.this.timeTracker);
                }

                @Override
                public void onMarkerReached(AudioTrack track) {
                    Log.d(LOG_TAG, "AudioTrack notification marker reached");
                }
            });

            inNumberFrames = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            remoteInputUnit = new AudioRecord(AudioSource.MIC, RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, inNumberFrames);
            if (remoteInputUnit.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(LOG_TAG, "can't initialize AudioRecord");  // Needed <uses-permission android:name="android.permission.RECORD_AUDIO"/>
                return;
            }
            remoteInputUnit.setPositionNotificationPeriod(inNumberFrames);
            timeTracker = System.currentTimeMillis();
            remoteInputUnit.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
                @Override
                public void onPeriodicNotification(AudioRecord recorder) {
                    Log.d(LOG_TAG, "notified by AudioRecord subsystem");

//                    if (inAudioData.length != inNumberFrames){
//                        inAudioData = new int[inNumberFrames];
//                    }
//                    for (int i  = 0; i < FJNFCService.this.inNumberFrames; i++){
//                        FJNFCService.this.inAudioData[i] = (int) (inAudioData[i]/64.0);
//                    }
                    floJackAUInputCallback(FJNFCService.this.inAudioData.asIntBuffer());
                    processingTime = (System.currentTimeMillis()-FJNFCService.this.timeTracker);
                }

                @Override
                public void onMarkerReached(AudioRecord recorder) {
                    Log.d(LOG_TAG, "AudioRecord notification marker reached");
                }
            });


            remoteInputUnit.startRecording();
            remoteOutputUnit.play();
            inAudioData.allocate(inNumberFrames);
            while (!Thread.interrupted()) {
                inNumberFrames = 0; // zero out the number of frames to adjust the buffer captured to size
                timeTracker = System.currentTimeMillis();
                if (outNumberFrames != remoteOutputUnit.write(outAudioData.array(), 0, outNumberFrames)) {
                    Log.e(LOG_TAG, "Write: bad value given");
                    break;
                }
                timeTracker = System.currentTimeMillis();
                if (inNumberFrames != remoteInputUnit.read(inAudioData, inNumberFrames)) {
                    Log.e(LOG_TAG, "Read: bad value given");
                    break;
                }
                try {
                    Thread.sleep(1000);
                    Log.d(LOG_TAG,"in progress");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            remoteOutputUnit.stop();
            remoteInputUnit.stop();
            remoteOutputUnit.release();
            remoteInputUnit.release();
        }
    }

    private void floJackAUInputCallback(IntBuffer inData) {
        // TX vars
        uart_state decoderState = uart_state.STARTBIT;
        int lastSample = 0;
        int lastPhase2 = 0;
        int phase2 = 0;
        int sample = 0;

        // UART decoding
        int bitNum = 0;
        boolean parityGood = false;
        byte parityRx = 0;
        byte uartByte = 0;
        float sample_avg_low = 0;
        float sample_avg_high = 0;

        /************************************
         * UART Decoding
         ************************************/
        for (int frameIndex = 0; frameIndex<inNumberFrames; frameIndex++) {
            float raw_sample = inData.get(frameIndex);
            Log.d(LOG_TAG, String.format("%8ld, %8.0f\n", phase2, raw_sample));

            if (decoderState == uart_state.DECODE_BYTE_SAMPLE)
                Log.d(LOG_TAG, String.format("%8ld, %8.0f, %d\n", phase2, raw_sample, frameIndex));

            phase2 += 1;
            if (raw_sample < ZERO_TO_ONE_THRESHOLD) {
                sample = logicZero;
                sample_avg_low = (sample_avg_low + raw_sample)/2;
            }
            else {
                sample = logicOne;
                sample_avg_high = (sample_avg_high + raw_sample)/2;
            }

            if ((sample != lastSample) && (sample_avg_high > SAMPLE_NOISE_CEILING) && (sample_avg_low < SAMPLE_NOISE_FLOOR)) {
                // we have a transition
                int diff = phase2 - lastPhase2;
                switch (decoderState) {
                    case STARTBIT:
                        if (lastSample == 0 && sample == 1) {
                            // low->high transition. Now wait for a long period
                            decoderState = uart_state.DECODE_BYTE_SAMPLE;
                        }
                        break;
                    case STARTBIT_FALL:
                        if ((SHORT < diff) && (diff < LONG)) {
                            // looks like we got a 1->0 transition.
                            Log.d(LOG_TAG, String.format("Received a valid StartBit \n"));
                            decoderState = uart_state.DECODE_BYTE_SAMPLE;
                            bitNum = 0;
                            parityRx = 0;
                            uartByte = 0;
                        }
                        else {
                            // looks like we didn't
                            decoderState = uart_state.DECODE_BYTE_SAMPLE;
                        }
                        break;
                    case DECODE_BYTE_SAMPLE:
                        if ((SHORT < diff) && (diff < LONG)) {
                            // We have a valid sample.
                            if (bitNum < 8) {
                                // Sample is part of the byte
                                //Log.d(LOG_TAG, String.format("Bit %d value %ld diff %ld parity %d\n", bitNum, sample, diff, parityRx & 0x01));
                                uartByte = (byte)((uartByte >> 1) | (sample << 7));
                                bitNum += 1;
                                parityRx += sample;
                            }
                            else if (bitNum == 8) {
                                // Sample is a parity bit
                                if(sample != (parityRx & 0x01)) {
                                    Log.d(LOG_TAG, String.format(" -- parity %ld,  UartByte 0x%x\n", sample, uartByte));
                                    //decoderState = STARTBIT;
                                    parityGood = false;
                                    bitNum += 1;
                                }
                                else {
                                    Log.d(LOG_TAG, String.format(" ++ UartByte: 0x%x\n", uartByte));
                                    //Log.d(LOG_TAG, String.format(" +++ good parity: %ld \n", sample));
                                    parityGood = true;
                                    bitNum += 1;
                                }
                            }
                            else {
                                // Sample is stop bit
                                if (sample == 1) {
                                    // Valid byte
                                    //Log.d(LOG_TAG, String.format(" +++ stop bit: %ld \n", sample));
//                                if(frameIndex > (80))
//                                {
//                                    //Log.d(LOG_TAG, String.format("%f\n", raw_sample));
//                                    for(int k=frameIndex-80; k<256; k++)
//                                    {
//                                        Log.i(LOG_TAG,String.format("%d\n", inData[k]));
//                                    }
//                                    Log.i(LOG_TAG,String.format("%f\n", raw_sample));
//                                }
                                }
                                else {
                                    // Invalid byte
                                    Log.d(LOG_TAG, String.format(" -- StopBit: %ld UartByte 0x%x\n", sample, uartByte));
                                    parityGood = false;
                                }

                                // Send byte to message handler
                                handleReceivedByte(uartByte, parityGood, System.currentTimeMillis());
                                decoderState = uart_state.STARTBIT;
                            }
                        }
                        else if (diff > LONG) {
                            Log.d(LOG_TAG, String.format("Diff too long %ld\n", diff));
                            decoderState = uart_state.STARTBIT;
                        }
                        else {
                            // don't update the phase as we have to look for the next transition
                            lastSample = sample;
                            continue;
                        }
                        break;
                    default:
                        break;
                }
                lastPhase2 = phase2;
            }
            lastSample = sample;
        } //end: for(int j = 0; j < inNumberFrames; j++)

        return;
    }

    private void floJackAUOutputCallback(IntBuffer outData) {
        // TX vars
        short parityTx = 0;
        int phase = 0;

        // UART encode
        boolean comm_sync_in_progress = false;
        byte currentBit = 1;
        uart_state encoderState = uart_state.NEXTBIT;
        int nextPhaseEnc = SAMPLESPERBIT;
        int phaseEnc = 0;
        byte uartByteTx = 0x0;
        int uartBitTx = 0;
        int uartSyncBitTx = 0;
        float[] uartBitEnc = new float[SAMPLESPERBIT];


        if (muteEnabled == false) {
            /*******************************
             * Generate 22kHz Tone
             *******************************/
            double waves;
            for (int j = 0; j < inNumberFrames; j++) {
                waves = 0;
                waves += FloatMath.sin((float)Math.PI * phase+0.5f); // nfcService should be 22.050kHz
                waves *= outputAmplitude; // <--------- make sure to divide by how many waves you're stacking

                outData.put(j,(int)waves);
                phase++;
            }

            /*******************************
             * UART Encoding
             *******************************/
            for(int j = 0; j<inNumberFrames && currentlySendingMessage == true; j++) {
                if (phaseEnc >= nextPhaseEnc) {
                    if(byteQueuedForTX == true && uartBitTx >= NUMSTOPBITS && comm_sync_in_progress == false) {
                        comm_sync_in_progress = true;
                        encoderState = uart_state.NEXTBIT;
                        uartSyncBitTx = 0;
                    }
                    else if (byteQueuedForTX == true && uartBitTx >= NUMSTOPBITS && uartSyncBitTx >= NUMSYNCBITS) {
                        encoderState = uart_state.STARTBIT;
                    }
                    else {
                        encoderState = uart_state.NEXTBIT;
                    }
                } //end: if (phaseEnc >= nextPhaseEnc)

                switch (encoderState) {
                    case STARTBIT:
                    {
                        uartByteTx = byteForTX;
                        byteQueuedForTX = false;

                        Log.d(LOG_TAG, String.format("uartByteTx: 0x%x\n", uartByteTx));

                        uartBitTx = 0;
                        parityTx = 0;

                        encoderState = uart_state.NEXTBIT;
                        // break;   // fall through intentionally
                    }
                    case NEXTBIT:
                    {
                        byte nextBit;
                        if (uartBitTx == 0) {
                            // start bit
                            nextBit = 0;
                        } else {
                            if (uartBitTx == 9) {
                                // parity bit
                                nextBit = (byte)(parityTx & 0x01);
                            }
                            else if (uartBitTx >= 10) {
                                // stop bit
                                nextBit = 1;
                            }
                            else {
                                nextBit = (byte)((uartByteTx >> (uartBitTx - 1)) & 0x01);
                                parityTx += nextBit;
                            }
                        }
                        if (nextBit == currentBit) {
                            if (nextBit == 0) {
                                for (int p = 0; p<SAMPLESPERBIT; p++) {
                                    uartBitEnc[p] = -FloatMath.sin((float)Math.PI * 2.0f / hwSampleRate * HIGHFREQ * (p+1));
                                }
                            }
                            else {
                                for (int p = 0; p<SAMPLESPERBIT; p++) {
                                    uartBitEnc[p] = FloatMath.sin((float)Math.PI * 2.0f / hwSampleRate * HIGHFREQ * (p + 1));
                                }
                            }
                        } else {
                            if (nextBit == 0) {
                                for (int p = 0; p<SAMPLESPERBIT; p++) {
                                    uartBitEnc[p] = FloatMath.sin((float)Math.PI * 2.0f / hwSampleRate * LOWFREQ * (p + 1));
                                }
                            } else {
                                for (int p = 0; p<SAMPLESPERBIT; p++) {
                                    uartBitEnc[p] = -FloatMath.sin((float)Math.PI * 2.0f / hwSampleRate * LOWFREQ * (p + 1));
                                }
                            }
                        }
                        currentBit = nextBit;
                        uartBitTx++;
                        encoderState = uart_state.SAMEBIT;
                        phaseEnc = 0;
                        nextPhaseEnc = SAMPLESPERBIT;

                        break;
                    }
                    default:
                        break;
                } //end: switch(state)
                outData.put(j, (int)(uartBitEnc[phaseEnc%SAMPLESPERBIT] * outputAmplitude));
                phaseEnc++;
            } //end: for(int j = 0; j< inNumberFrames; j++)
            // copy data into left channel
            if((uartBitTx<=NUMSTOPBITS || uartSyncBitTx<=NUMSYNCBITS) && currentlySendingMessage == true) {
                // TODO not sure if need to create worker thread and render audio per http://stackoverflow.com/questions/10158409/android-audio-streaming-sine-tone-generator-odd-behaviour
                uartSyncBitTx++;
            }
            else {
                comm_sync_in_progress = false;
                // TODO Need to implement this SilenceData function
                //SilenceData(outData);
            }
        }
    }
    private void handleReceivedByte(byte myByte, boolean parityGood, double timestamp){

    }
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

    /**
     Get the Logic One value based on device type.

     @return byte    1 or 0 indicating logical one for this device
     */
    public byte getDeviceLogicOneValue() {
        // Get the device model number from uname
        String machineName = Build.MODEL;

        // Default value (should work on most devices)
        byte logicOneValue = 1;

        // Device exceptions
        if (machineName.equalsIgnoreCase("Samsung Galaxy Note10.1")) {
            logicOneValue = 0;
        }

        return logicOneValue;

    }

    /**
     Get the logical zero value based on device type.

     @return byte    1 or 0 indicating logical one for this device
     */

    public byte getDeviceLogicZeroValue() {
        // Return inverse of LogicOne value
        if (this.getDeviceLogicOneValue() == 1)
            return 0;
        else
            return 1;
    }
}
