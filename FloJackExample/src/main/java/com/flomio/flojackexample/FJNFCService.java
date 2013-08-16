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
import android.media.MediaRecorder;
import android.os.Build;
import android.util.FloatMath;
import android.util.Log;

import java.nio.ShortBuffer;

public class FJNFCService extends IntentService {
    // Basic Audio system constants
    private final static int RATE = 44100;                     // rate at which samples are collected
    private final static int SAMPLES = 512;                    // number of samples to process at one time

    private final static int ZERO_TO_ONE_THRESHOLD = 0;        // threshold used to detect start bit
    private final static int SAMPLESPERBIT = 13;               // (44100 / HIGHFREQ)  // how many samples per UART bit
    private final static int SHORT =  (SAMPLESPERBIT/2 + SAMPLESPERBIT/4);
    private final static int LONG =  (SAMPLESPERBIT + SAMPLESPERBIT/2);
    private final static float HIGHFREQ = 3392;                // baud rate. best to take a divisible number for 44.1kS/s
    private final static float LOWFREQ = (HIGHFREQ / 2);
    private final static int NUMSTOPBITS = 20;                 // number of stop bits to send before sending next value.
    private final static int NUMSYNCBITS = 4;                  // number of ones to send before sending first value.
    private final static int SAMPLE_NOISE_CEILING = 10000;    // keeping running average and filter out noisy values around 0
    private final static int SAMPLE_NOISE_FLOOR = -10000;     // keeping running average and filter out noisy values around 0
    private final static double MESSAGE_SYNC_TIMEOUT = 0.500;  // seconds

    // Message Length Boundaries
    private final static int MIN_MESSAGE_LENGTH = 3;   //TODO: change to 4
    private final static int MAX_MESSAGE_LENGTH = 255;
    private final static int CORRECT_CRC_VALUE = 0;

    // Audio Unit attributes
    private AudioRecord remoteInputUnit;
    private AudioTrack remoteOutputUnit;
    private long processingTime = 0;
    private byte logicOne, logicZero;
    private int outputAmplitude;

    // Audio Unit attributes shared across classes
    private ShortBuffer inAudioData;
    private ShortBuffer outAudioData;
    //private short[] inAudioData;
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
        super(FJNFCService.class.getSimpleName());
        Log.d(LOG_TAG, "constructor called.");

        // Logic high/low varies based on host device
        logicOne = getDeviceLogicOneValue();
        logicZero = getDeviceLogicZeroValue();
        muteEnabled = false;
        currentlySendingMessage = false; // TODO: toggle flag in send(byte) queuing system
        byteForTX = (byte) 0xAA;  // TODO: put send(byte) queuing system in place (use Handler?)
        outputAmplitude = (1<<24);  // TODO: create accessor for setting normal and high amplitude
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(LOG_TAG, "onStart called");

        // a frame is composed of one audio sample. Stereo can have 2 channels
        int playBufferSize = AudioTrack.getMinBufferSize(RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (SAMPLES > playBufferSize) {
            Log.e(LOG_TAG, "your SAMPLES size is too large for the playing audio buffer.");
            return;
        }
        remoteOutputUnit = new AudioTrack(AudioManager.STREAM_DTMF, RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, playBufferSize,
                AudioTrack.MODE_STREAM);
        if (remoteOutputUnit.getState() != AudioTrack.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "can't initialize AudioTrack");  // TODO needs some permission?
            return;
        }

        // Set Number of frames to be 4 times larger than the minimum audio capture size.  This
        // is to allow for small chunks of the input samples to be processed while the others
        // continue to be created.  This approach allows for the FJNFCService thread to not
        // hog all the processing time and make the UI Threads unresponsive.
        int recBufferSize = 4 * AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (SAMPLES > recBufferSize) {
            Log.e(LOG_TAG, "your SAMPLES size is too large for the recorded audio buffer.");
            return;
        }

        remoteInputUnit = new AudioRecord(MediaRecorder.AudioSource.MIC, RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recBufferSize);
        if (remoteInputUnit.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "can't initialize AudioRecord");  // Needed <uses-permission android:name="android.permission.RECORD_AUDIO"/>
            return;
        }

        remoteInputUnit.startRecording();
        remoteOutputUnit.play();
        new FJNFCListener().start();
    }

    @Override
    public void onHandleIntent(Intent intent) {
        try{
            Log.d(LOG_TAG,"handle intent called");
        }catch(Exception e){
            // intentionally left empty
        }
    }

    public class FJNFCListener extends Thread {

        FJNFCListener() {
            inAudioData = ShortBuffer.allocate(SAMPLES);
//            outAudioData = ByteBuffer.allocate(SAMPLES);
//            inAudioData = new short[SAMPLES];
        }

        public void run() {
            Log.i(LOG_TAG, "starting thread to capture samples");

            // Make this thread the highest priority in order to capture a continuous stream of data
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            while (!Thread.interrupted()) {
                int offset = 0;
//                timeTracker = System.currentTimeMillis();
//                if (remoteOutputUnit.write(outAudioData.array(), 0, SAMPLES) < SAMPLES) {
//                    Log.e(LOG_TAG, "we didn't write enough samples to playback audio buffer.");
//                    break;
//                }
//                floJackAUOutputCallback(outAudioData);
//                processingTime = (System.currentTimeMillis() - timeTracker);
//                Log.d(LOG_TAG, String.format("played samples in %dms", processingTime));

                timeTracker = System.currentTimeMillis();
                if (remoteInputUnit.read(inAudioData.array(), 0, SAMPLES) < SAMPLES) {
                    Log.e(LOG_TAG, "we didn't read enough samples from recorded audio buffer.");
                    break;
                }
                floJackAUInputCallback(inAudioData);
                processingTime = (System.currentTimeMillis()-timeTracker);
                Log.d(LOG_TAG, String.format("processed samples in %dms", processingTime));
            }
            remoteOutputUnit.stop();     // stop playing date out
            remoteInputUnit.stop();      // stop sampling data in
            remoteOutputUnit.release();  // let go of playBuffer
            remoteInputUnit.release();   // let go of recBuffer
        }
    }

    private void floJackAUInputCallback(ShortBuffer inData) {
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
        for (int frameIndex = 0; frameIndex<inData.capacity(); frameIndex++) {
            float raw_sample = inData.get(frameIndex);
            Log.d(LOG_TAG, String.format("%8d, %8.0f %s\n", phase2, raw_sample,
                    (decoderState==uart_state.DECODE_BYTE_SAMPLE)?"Decode"+frameIndex:""));

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
                        Intent i = new Intent();
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setAction("com.restock.serialmagic.gears.action.SCAN");
                        i.putExtra("scan","01020304050607");
                        startActivity(i);

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
                                    Log.d(LOG_TAG, String.format(" -- parity %d,  UartByte 0x%x\n", sample, uartByte));
                                    //decoderState = STARTBIT;
                                    parityGood = false;
                                    bitNum += 1;
                                }
                                else {
                                    Log.d(LOG_TAG, String.format(" ++ UartByte: 0x%x\n", uartByte));
                                    //Log.d(LOG_TAG, String.format(" +++ good parity: %d \n", sample));
                                    parityGood = true;
                                    bitNum += 1;
                                }
                            }
                            else {
                                // Sample is stop bit
                                if (sample == 1) {
                                    // Valid byte
                                    //Log.d(LOG_TAG, String.format(" +++ stop bit: %d \n", sample));
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
                                    Log.d(LOG_TAG, String.format(" -- StopBit: %d UartByte 0x%x\n", sample, uartByte));
                                    parityGood = false;
                                }

                                // Send byte to message handler
                                handleReceivedByte(uartByte, parityGood, System.currentTimeMillis());
                                decoderState = uart_state.STARTBIT;
                            }
                        }
                        else if (diff > LONG) {
                            Log.d(LOG_TAG, String.format("Diff too long %d\n", diff));
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

    private void floJackAUOutputCallback(ShortBuffer outData) {
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
            for (int j = 0; j < outData.capacity(); j++) {
                waves = 0;
                waves += FloatMath.sin((float)Math.PI * phase+0.5f); // nfcService should be 22.050kHz
                waves *= outputAmplitude; // <--------- make sure to divide by how many waves you're stacking

                outData.put(j,(short)waves);
                phase++;
            }

            /*******************************
             * UART Encoding
             *******************************/
            for(int j = 0; j<outData.capacity() && currentlySendingMessage == true; j++) {
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
                                    uartBitEnc[p] = -FloatMath.sin((float)Math.PI * 2.0f / RATE * HIGHFREQ * (p+1));
                                }
                            }
                            else {
                                for (int p = 0; p<SAMPLESPERBIT; p++) {
                                    uartBitEnc[p] = FloatMath.sin((float)Math.PI * 2.0f / RATE * HIGHFREQ * (p + 1));
                                }
                            }
                        } else {
                            if (nextBit == 0) {
                                for (int p = 0; p<SAMPLESPERBIT; p++) {
                                    uartBitEnc[p] = FloatMath.sin((float)Math.PI * 2.0f / RATE * LOWFREQ * (p + 1));
                                }
                            } else {
                                for (int p = 0; p<SAMPLESPERBIT; p++) {
                                    uartBitEnc[p] = -FloatMath.sin((float)Math.PI * 2.0f / RATE * LOWFREQ * (p + 1));
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
                outData.put(j, (short)(uartBitEnc[phaseEnc%SAMPLESPERBIT] * outputAmplitude));
                phaseEnc++;
            } //end: for(int j = 0; j< outData.capacity(); j++)
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

    private void handleReceivedByte(byte myByte, boolean parityGood, long timestamp) {
//        /*
//         *  ERROR CHECKING
//         */
//        // Before anything else carry out error handling
//        if (!parityGood) {
//            // last byte was corrupted, dump this entire message
//            Log.d(LOG_TAG," --- Parity Bad: dumping message.");
//            markCurrentMessageCorruptAndClearBufferAtTime(timestamp);
//            return;
//        }
//        else if (!messageValid & !(timestamp - lastByteReceivedAtTime >= MESSAGE_SYNC_TIMEOUT)) {
//            // byte is ok but we're still receiving a corrupt message, dump it.
//            Log.d(LOG_TAG,String.format(" --- Message Invalid: dumping message (timeout: %f)", (timestamp - lastByteReceivedAtTime)));
//            markCurrentMessageCorruptAndClearBufferAtTime(timestamp);
//            return;
//        }
//        else if (timestamp - lastByteReceivedAtTime >= MESSAGE_SYNC_TIMEOUT) {
//            // sweet! timeout has passed, let's get cranking on this valid message
//            if (messageReceiveBuffer.length > 0) {
//                Log.d(LOG_TAG,String.format("Timeout reached. Dumping previous buffer. \n_messageReceiveBuffer:%@ \n_messageReceiveBuffer.length:%d", messageReceiveBuffer.toString(), messageReceiveBuffer.length));
//
////                if([_delegate respondsToSelector:@selector(nfcService: didHaveError:)]) {
////                    dispatch_async(_backgroundQueue, ^(void) {
////                            [_delegate nfcService:self didHaveError:FLOMIO_STATUS_MESSAGE_CORRUPT_ERROR];
////                    });
////                }
//            }
//
//            Log.d(LOG_TAG,String.format(" ++ Message Valid: byte is part of a new message (timeout: %f)", (timestamp - lastByteReceivedAtTime)));
//            markCurrentMessageValidAtTime(timestamp);
//            clearMessageBuffer();
//        }
//
//        /*
//         *  BUFFER BUILDER
//         */
//        markCurrentMessageValidAtTime(timestamp);
////        messageReceiveBuffer(myByte, 1);
//        messageCRC ^= myByte;
//
//        // Have we received the message length yet ?
//        if (messageReceiveBuffer.length == 2) {
//            byte length = 0;
////            messageReceiveBuffer(length);
////            range:NSMakeRange(FLOJACK_MESSAGE_LENGTH_POSITION,
////                    FLOJACK_MESSAGE_LENGTH_POSITION)];
//            messageLength = length;
//            if (messageLength < MIN_MESSAGE_LENGTH || messageLength > MAX_MESSAGE_LENGTH)
//            {
//                Log.d(LOG_TAG,String.format("Invalid message length, ingoring current message."));
//                markCurrentMessageCorruptAndClearBufferAtTime(timestamp);
//            }
//        }
//
//        // Is the message complete?
//        if (messageReceiveBuffer.length == messageLength
//                && messageReceiveBuffer.length > MIN_MESSAGE_LENGTH) {
//            // Check CRC
//            if (messageCRC == CORRECT_CRC_VALUE) {
//                // Well formed message received, pass it to the delegate
//                Log.d(LOG_TAG,String.format("FJNFCService: Complete message, send to delegate."));
//
////                if([_delegate respondsToSelector:@selector(nfcService: didReceiveMessage:)]) {
////                    NSData *dataCopy = [[NSData alloc] initWithData:_messageReceiveBuffer];
////                    dispatch_async(_backgroundQueue, ^(void) {
////                            [_delegate nfcService:self didReceiveMessage:dataCopy];
////                    });
////                }
//
//                markCurrentMessageValidAtTime(timestamp);
//                clearMessageBuffer();
//            }
//            else {
//                //TODO: plumb this through to delegate
//                Log.d(LOG_TAG,String.format("Bad CRC, ingoring current message."));
//                markCurrentMessageCorruptAndClearBufferAtTime(timestamp);
//            }
//        }
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

    /**
     Mark the current message corrupt and clear the receive buffer.

     @param timestamp       Time when message was marked valid and buffer cleared
     @return void
     */
    public void markCurrentMessageCorruptAndClearBufferAtTime(long timestamp) {
        markCurrentMessageCorruptAtTime(timestamp);
        clearMessageBuffer();
    }

    /**
     Mark the current message invalid and timestamp.
     The message receive buffer will be flushed after transmission
     completes.

     @param timestamp       Time when message was marked corrupt
     @return void
     */
    public void markCurrentMessageCorruptAtTime(long timestamp) {
        lastByteReceivedAtTime = timestamp;
        messageValid = false;
    }

    /**
     Mark the current message valid and capture the timestamp.

     @param timestamp       Time when message was marked valid
     @return void
     */
    public void markCurrentMessageValidAtTime(long timestamp) {
        lastByteReceivedAtTime = timestamp;
        messageValid = true;
    }

    public void clearMessageBuffer() {
//        messageReceiveBuffer = 0;
        messageLength  = MAX_MESSAGE_LENGTH;
        messageCRC = 0;
    }


}

