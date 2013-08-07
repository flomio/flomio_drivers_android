package com.flomio.flojackexample;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;

/**
 * Created by grundyoso on 8/6/13.
 */

public class FJNFCService {
    // Sample rate calculation
    private static final int fc = 1200;
    private static final int df = 100;
    private static final double T = (1/df);
    private int N;

    private static final int ZERO_TO_ONE_THRESHOLD = 0;        // threshold used to detect start bit

    private static final int SAMPLESPERBIT = 13;               // (44100 / HIGHFREQ)  // how many samples per UART bit
    private static final int SHORT =  (SAMPLESPERBIT/2 + SAMPLESPERBIT/4);
    private static final int LONG =  (SAMPLESPERBIT + SAMPLESPERBIT/2);

    private static final int HIGHFREQ = 3392;                  // baud rate. best to take a divisible number for 44.1kS/s
    private static final int LOWFREQ = (HIGHFREQ / 2);

    private static final int NUMSTOPBITS = 20;                 // number of stop bits to send before sending next value.
    private static final int NUMSYNCBITS = 4;                  // number of ones to send before sending first value.

    private static final int SAMPLE_NOISE_CEILING = 100000;    // keeping running average and filter out noisy values around 0
    private static final int SAMPLE_NOISE_FLOOR = -100000;     // keeping running average and filter out noisy values around 0

    private static final double MESSAGE_SYNC_TIMEOUT = 0.500;  // seconds

    public FJNFCService() {
    }

    private enum uart_state {
        STARTBIT,               //(0)
        SAMEBIT,                //(1)
        NEXTBIT,                //(2)
        STOPBIT,                //(3)
        STARTBIT_FALL,          //(4)
        DECODE_BYTE_SAMPLE      //(5)
    }

    //id <FJNFCServiceDelegate>	 _delegate;
    //dispatch_queue_t             _backgroundQueue;

    // Audio Unit attributes
    //AURenderCallbackStruct		 _audioUnitRenderCallback;
    private double              hwSampleRate;
    private int 				maxFPS;
    //DCRejectionFilter			*remoteIODCFilter;
    //AudioUnit					 _remoteIOUnit;
    //CAStreamBasicDescription	 _remoteIOOutputFormat;
    private byte                 ignoreRouteChangeCount;

    // NFC Service state variables
    private byte                byteForTX;
    private boolean				byteQueuedForTX;
    private boolean             currentlySendingMessage;
    private boolean             muteEnabled;

    // Logic Values
    private byte                logicOne;
    private byte                logicZero;

    // Message handling variables
    private double              lastByteReceivedAtTime;
    private byte                messageCRC;
    private ByteBuffer          messageReceiveBuffer;
    private int                 messageLength;
    private boolean             messageValid;


    public void initialize() {
        N = (int)(T * hwSampleRate);
        int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        //recorder.setPositionNotificationPeriod(duration * sampleRate); //TODO, replace duration and sampleRate with final static above
        recorder.startRecording();
        recorder.stop();
        recorder.release();

//        // Setup Grand Central Dispatch queue (thread pool)
//        _backgroundQueue = dispatch_queue_create("com.flomio.flojack", NULL);
//        dispatch_retain(_backgroundQueue);
//
//        // Register an input callback function with an audio unit.
//        _audioUnitRenderCallback.inputProc = floJackAURenderCallback;
//        _audioUnitRenderCallback.inputProcRefCon = (__bridge_retained void*) self;
//
//        // Initialize receive handler buffer
//        _messageReceiveBuffer = [[NSMutableData alloc] initWithCapacity:MAX_MESSAGE_LENGTH];
//        _messageLength = MAX_MESSAGE_LENGTH;
//        _messageCRC = 0;
//
//        // Init state flags
//        _currentlySendingMessage = FALSE;
//        _muteEnabled = FALSE;
//        _byteQueuedForTX = FALSE;
//
//        _messageTXLock = dispatch_semaphore_create(1);
//        _ignoreRouteChangeCount = 0;
//
//        // Assume non EU device
//        [self setOutputAmplitudeNormal];
//
//        try {
//            // Logic high/low varies based on host device
//            _logicOne = [FJNFCService getDeviceLogicOneValue];
//            _logicZero = [FJNFCService getDeviceLogicZeroValue];
//
//            static dispatch_once_t onceToken;
//            dispatch_once(&onceToken, ^{
//                XThrowIfError(AudioSessionInitialize(NULL, NULL, floJackAudioSessionInterruptionListener, (__bridge_retained void*) self), "couldn't initialize audio session");
//
//
//                // Initialize and configure the audio session
//                XThrowIfError(AudioSessionSetActive(true), "couldn't set audio session active\n");
//
//                UInt32 audioCategory = kAudioSessionCategory_PlayAndRecord;
//                XThrowIfError(AudioSessionSetProperty(kAudioSessionProperty_AudioCategory, sizeof(audioCategory), &audioCategory), "couldn't set audio category");
//                XThrowIfError(AudioSessionAddPropertyListener(kAudioSessionProperty_AudioRouteChange, floJackAudioSessionPropertyListener, (__bridge_retained void*) self), "couldn't set property listener");
//
//                Float32 preferredBufferSize = .005;
//                XThrowIfError(AudioSessionSetProperty(kAudioSessionProperty_PreferredHardwareIOBufferDuration, sizeof(preferredBufferSize), &preferredBufferSize), "couldn't set i/o buffer duration");
//
//                UInt32 size = sizeof(_hwSampleRate);
//                XThrowIfError(AudioSessionGetProperty(kAudioSessionProperty_CurrentHardwareSampleRate, &size, &_hwSampleRate), "couldn't get hw sample rate");
//
//                XThrowIfError(AudioSessionAddPropertyListener(kAudioSessionProperty_CurrentHardwareOutputVolume, floJackAudioSessionPropertyListener, (__bridge_retained void*) self), "couldn't set property listener");
//
//                XThrowIfError(SetupRemoteIO(_remoteIOUnit, _audioUnitRenderCallback, _remoteIOOutputFormat), "couldn't setup remote i/o unit");
//
//                _remoteIODCFilter = new DCRejectionFilter[_remoteIOOutputFormat.NumberChannels()];
//
//                size = sizeof(_maxFPS);
//                XThrowIfError(AudioUnitGetProperty(_remoteIOUnit, kAudioUnitProperty_MaximumFramesPerSlice, kAudioUnitScope_Global, 0, &_maxFPS, &size), "couldn't get the remote I/O unit's max frames per slice");
//
//                XThrowIfError(AudioOutputUnitStart(_remoteIOUnit), "couldn't start remote i/o unit");
//
//                size = sizeof(_remoteIOOutputFormat);
//                XThrowIfError(AudioUnitGetProperty(_remoteIOUnit, kAudioUnitProperty_StreamFormat, kAudioUnitScope_Output, 1, &_remoteIOOutputFormat, &size), "couldn't get the remote I/O unit's output client format");
//            });
//        }
//        catch (CAXException &e) {
//            char buf[256];
//            fprintf(stderr, "Error: %s (%s)\n", e.mOperation, e.FormatError(buf));
//            if (_remoteIODCFilter) delete[] _remoteIODCFilter;
//        }
//        catch (...) {
//            fprintf(stderr, "An unknown error occurred\n");
//            if (_remoteIODCFilter) delete[] _remoteIODCFilter;
//        }
    }
    private void floJackAURenderCallback(int        inRefCon,
                                         int        ioActionFlags,
                                         double 	inTimeStamp,
                                         int 		inBusNumber,
                                         int 		inNumberFrames,
                                         ByteBuffer ioData) {


//        FJNFCService *self = (__bridge FJNFCService *)inRefCon;
//        OSStatus ossError = AudioUnitRender(self->_remoteIOUnit, ioActionFlags, inTimeStamp, 1, inNumberFrames, ioData);
//
//        if (ossError) {
//            printf("AudioUnitRender Error:%d\n", (int)ossError);
//            return ossError;
//        }
//        else if (!self.floJackConnected) {
//            SilenceData(ioData);
//            return ossError;
//        }
//
//        // TX vars
//        static int byteCounter = 1;
//        static int decoderState = STARTBIT;
//        static SInt32 lastSample = 0;
//        static UInt32 lastPhase2 = 0;
//        static UInt8 parityTx = 0;
//        static UInt32 phase = 0;
//        static UInt32 phase2 = 0;
//        static SInt32 sample = 0;
//
//        // UART decoding
//        static int bitNum = 0;
//        static BOOL parityGood = 0;
//        static uint8_t uartByte = 0;
//
//        // UART encode
//        static BOOL comm_sync_in_progress = FALSE;
//        static uint8_t currentBit = 1;
//        static uint8_t encoderState = NEXTBIT;
//        static uint32_t nextPhaseEnc = SAMPLESPERBIT;
//        static UInt8 parityRx = 0;
//        static uint32_t phaseEnc = 0;
//        static float sample_avg_low = 0;
//        static float sample_avg_high = 0;
//        static uint8_t uartByteTx = 0x0;
//        static uint32_t uartBitTx = 0;
//        static float uartBitEnc[SAMPLESPERBIT];
//        static uint32_t uartSyncBitTx = 0;
//
//        // Audio Channels
//        SInt32* left_audio_channel = (SInt32*)(ioData->mBuffers[0].mData);
//
//
//        /************************************
//         * UART Decoding
//         ************************************/
//        for(int frameIndex = 0; frameIndex<inNumberFrames; frameIndex++) {
//            float raw_sample = left_audio_channel[frameIndex];
//            //left_audio_channel[frameIndex] = 0;
//            LogWaveform(@"%8ld, %8.0f\n", phase2, raw_sample);
//
//            if(decoderState == DECODE_BYTE_SAMPLE )
//                LogDecoderVerbose(@"%8ld, %8.0f, %d\n", phase2, raw_sample, frameIndex);
//
//            phase2 += 1;
//            if (raw_sample < ZERO_TO_ONE_THRESHOLD) {
//                sample = self->_logicZero;
//                sample_avg_low = (sample_avg_low + raw_sample)/2;
//            }
//            else {
//                sample = self->_logicOne;
//                sample_avg_high = (sample_avg_high + raw_sample)/2;
//            }
//
//            if ((sample != lastSample) && (sample_avg_high > SAMPLE_NOISE_CEILING) && (sample_avg_low < SAMPLE_NOISE_FLOOR)) {
//                // we have a transition
//                SInt32 diff = phase2 - lastPhase2;
//                switch (decoderState) {
//                    case STARTBIT:
//                        if (lastSample == 0 && sample == 1) {
//                            // low->high transition. Now wait for a long period
//                            decoderState = STARTBIT_FALL;
//                        }
//                        break;
//                    case STARTBIT_FALL:
//                        if ((SHORT < diff) && (diff < LONG)) {
//                            // looks like we got a 1->0 transition.
//                            LogDecoder(@"Received a valid StartBit \n");
//                            decoderState = DECODE_BYTE_SAMPLE;
//                            bitNum = 0;
//                            parityRx = 0;
//                            uartByte = 0;
//                        }
//                        else {
//                            // looks like we didn't
//                            decoderState = STARTBIT;
//                        }
//                        break;
//                    case DECODE_BYTE_SAMPLE:
//                        if ((SHORT < diff) && (diff < LONG)) {
//                            // We have a valid sample.
//                            if (bitNum < 8) {
//                                // Sample is part of the byte
//                                //LogDecoder(@"Bit %d value %ld diff %ld parity %d\n", bitNum, sample, diff, parityRx & 0x01);
//                                uartByte = ((uartByte >> 1) + (sample << 7));
//                                bitNum += 1;
//                                parityRx += sample;
//                            }
//                            else if (bitNum == 8) {
//                                // Sample is a parity bit
//                                if(sample != (parityRx & 0x01)) {
//                                    LogError(@" -- parity %ld,  UartByte 0x%x\n", sample, uartByte);
//                                    //decoderState = STARTBIT;
//                                    parityGood = false;
//                                    bitNum += 1;
//                                }
//                                else {
//                                    LogTrace(@" ++ UartByte: 0x%x\n", uartByte);
//                                    //LogTrace(@" +++ good parity: %ld \n", sample);
//                                    parityGood = true;
//                                    bitNum += 1;
//                                }
//                            }
//                            else {
//                                // Sample is stop bit
//                                if (sample == 1) {
//                                    // Valid byte
//                                    //LogTrace(@" +++ stop bit: %ld \n", sample);
//        //                                if(frameIndex > (80))
//        //                                {
//        //                                    //LogError(@"%f\n", raw_sample);
//        //                                    for(int k=frameIndex-80; k<256; k++)
//        //                                    {
//        //                                        NSLog(@"%d\n", left_audio_channel[k]);
//        //                                    }
//        //                                    NSLog(@"%f\n", raw_sample);
//        //                                }
//                                }
//                                else {
//                                    // Invalid byte
//                                    LogError(@" -- StopBit: %ld UartByte 0x%x\n", sample, uartByte);
//                                    parityGood = false;
//                                }
//
//                                // Send bye to message handler
//                                //NSAutoreleasePool *autoreleasepool = [[NSAutoreleasePool alloc] init];
//                                @autoreleasepool {
//                                    [self handleReceivedByte:uartByte withParity:parityGood atTimestamp:CACurrentMediaTime()];
//                                }
//                                //[autoreleasepool release];
//
//                                decoderState = STARTBIT;
//                            }
//                        }
//                        else if (diff > LONG) {
//                            LogDecoder(@"Diff too long %ld\n", diff);
//                            decoderState = STARTBIT;
//                        }
//                        else {
//                            // don't update the phase as we have to look for the next transition
//                            lastSample = sample;
//                            continue;
//                        }
//
//                        break;
//                    default:
//                        break;
//                }
//                lastPhase2 = phase2;
//            }
//            lastSample = sample;
//        } //end: for(int j = 0; j < inNumberFrames; j++)
//
//        if (self->_muteEnabled == NO) {
//            // Prepare the sine wave
//            SInt32 values[inNumberFrames];
//
//            /*******************************
//             * Generate 22kHz Tone
//             *******************************/
//            double waves;
//            for(int j = 0; j < inNumberFrames; j++) {
//                waves = 0;
//                waves += sin(M_PI * phase+0.5); // nfcService should be 22.050kHz
//                waves *= (self->_outputAmplitude); // <--------- make sure to divide by how many waves you're stacking
//
//                values[j] = (SInt32)waves;
//                phase++;
//            }
//
//            /*******************************
//             * UART Encoding
//             *******************************/
//            for(int j = 0; j<inNumberFrames && self->_currentlySendingMessage == TRUE; j++) {
//                if (phaseEnc >= nextPhaseEnc) {
//                    if(self->_byteQueuedForTX == TRUE && uartBitTx >= NUMSTOPBITS && comm_sync_in_progress == FALSE) {
//                        comm_sync_in_progress = TRUE;
//                        encoderState = NEXTBIT;
//                        uartSyncBitTx = 0;
//                    }
//                    else if (self->_byteQueuedForTX == TRUE && uartBitTx >= NUMSTOPBITS && uartSyncBitTx >= NUMSYNCBITS) {
//                        encoderState = STARTBIT;
//                    }
//                    else {
//                        encoderState = NEXTBIT;
//                    }
//                } //end: if (phaseEnc >= nextPhaseEnc)
//
//                switch (encoderState) {
//                    case STARTBIT:
//                    {
//                        uartByteTx = self->_byteForTX;
//                        self->_byteQueuedForTX = FALSE;
//
//                        LogTrace(@"uartByteTx: 0x%x\n", uartByteTx);
//
//                        byteCounter += 1;
//                        uartBitTx = 0;
//                        parityTx = 0;
//
//                        encoderState = NEXTBIT;
//                        // break;   // fall through intentionally
//                    }
//                    case NEXTBIT:
//                    {
//                        uint8_t nextBit;
//                        if (uartBitTx == 0) {
//                            // start bit
//                            nextBit = 0;
//                        } else {
//                            if (uartBitTx == 9) {
//                                // parity bit
//                                nextBit = parityTx & 0x01;
//                            }
//                            else if (uartBitTx >= 10) {
//                                // stop bit
//                                nextBit = 1;
//                            }
//                            else {
//                                nextBit = (uartByteTx >> (uartBitTx - 1)) & 0x01;
//                                parityTx += nextBit;
//                            }
//                        }
//                        if (nextBit == currentBit) {
//                            if (nextBit == 0) {
//                                for( uint8_t p = 0; p<SAMPLESPERBIT; p++) {
//                                    uartBitEnc[p] = -sin(M_PI * 2.0f / self->_hwSampleRate * HIGHFREQ * (p+1));
//                                }
//                            }
//                            else {
//                                for( uint8_t p = 0; p<SAMPLESPERBIT; p++) {
//                                    uartBitEnc[p] = sin(M_PI * 2.0f / self->_hwSampleRate * HIGHFREQ * (p+1));
//                                }
//                            }
//                        } else {
//                            if (nextBit == 0) {
//                                for( uint8_t p = 0; p<SAMPLESPERBIT; p++) {
//                                    uartBitEnc[p] = sin(M_PI * 2.0f / self->_hwSampleRate * LOWFREQ * (p+1));
//                                }
//                            } else {
//                                for( uint8_t p = 0; p<SAMPLESPERBIT; p++) {
//                                    uartBitEnc[p] = -sin(M_PI * 2.0f / self->_hwSampleRate * LOWFREQ * (p+1));
//                                }
//                            }
//                        }
//                        currentBit = nextBit;
//                        uartBitTx++;
//                        encoderState = SAMEBIT;
//                        phaseEnc = 0;
//                        nextPhaseEnc = SAMPLESPERBIT;
//
//                        break;
//                    }
//                    default:
//                        break;
//                } //end: switch(state)
//                values[j] = (SInt32)(uartBitEnc[phaseEnc%SAMPLESPERBIT] * self->_outputAmplitude);
//                phaseEnc++;
//            } //end: for(int j = 0; j< inNumberFrames; j++)
//            // copy data into left channel
//            if((uartBitTx<=NUMSTOPBITS || uartSyncBitTx<=NUMSYNCBITS) && self->_currentlySendingMessage == TRUE) {
//                memcpy(ioData->mBuffers[0].mData, values, ioData->mBuffers[0].mDataByteSize);
//                uartSyncBitTx++;
//            }
//            else {
//                comm_sync_in_progress = FALSE;
//                SilenceData(ioData);
//            }
//        }
//
//
//        return ossError;
    }
    private void handleReceivedByte(byte myByte, boolean parityGood, double timestamp){

    }
    private void sendFloJackConnectedStatusToDelegate() {

    }
    private void clearMessageBuffer() {

    }
    private void checkIfVolumeLevelMaxAndNotifyDelegate() {

    }
    private void disableDeviceSpeakerPlayback() {

    }
    private void enableDeviceSpeakerPlayback() {

    }
    private void sendByteToHost(byte theByte) {

    }
    private void sendMessageDataToHost(ByteBuffer messageData) {

    }
    private void setOutputAmplitudeHigh() {

    }
    private void setOutputAmplitudeNormal() {

    }
    public byte getDeviceInterByteDelay() {

    }
    public byte getDeviceLogicOneValue() {

    }
    public byte getDeviceLogicZeroValue() {

    }
}
