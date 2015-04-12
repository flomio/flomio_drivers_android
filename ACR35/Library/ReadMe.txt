ACS Audio Jack Android Library
Advanced Card Systems Ltd.



Introduction
------------

This library provides classes and interfaces for communicating with ACS audio
jack readers.

This library is based on Project HiJack. This project is to hijack power and
bandwidth from the mobile phone's audio interface and create a cubic-inch
peripheral sensor ecosystem for the mobile phone. See Project HiJack [1] for
more information.

To install the library to your development environment, see the section
"Installation".

[1] http://web.eecs.umich.edu/~prabal/projects/hijack/



Release Notes
-------------

Version:      1.0.0
Release Date: 27/2/2015

This library may or may not work with your Android device.

System Requirements

- Android 2.0 (Eclair) or above.

Development Environment

- ADT r23.0.2 or above. See Android Developers [1] for more information.

[1] http://developer.android.com/

Supported Readers

- ACR31
- ACR32
- ACR35



Installation
------------

1. To try the demo project, select Import from File menu on Eclipse. Choose
   "AudioJackDemo" to import to your workspace.

2. You can also run the demo project without Eclipse installed. Copy
   "AudioJackDemo.apk" to your Android device. Launch the installation by
   clicking the file icon on any File Explorer application.

3. To use the class library to your project, copy "acsaj-x.y.z.jar" to the
   "libs" folder of your project.



History
-------

Library

v1.0.0 (27/2/2015)
- Adjust the reset frequency.
- Target to Android 5.0.1 platform.
- Update the documentation.
- Add the following constants to Result class:
  ERROR_PICC_NO_CARD
- Fix the timeout problem for the first command after resetting the reader.

v1.0.0 Preview 15 (17/11/2014)
- Add the following constructors to AudioJackReader class:
  AudioJackReader(AudioManager, boolean)
- Add the following methods to AudioJackReader class:
  isMute()
  setMute()
- Add the following methods to Track1Data class:
  getTrack1String()
- Add the following methods to Track2Data class:
  getTrack2String()
- Trim the firmware version in AudioJackReader class.

v1.0.0 Preview 14 (15/10/2014)
- Use the received message length to return the firmware version in
  AudioJackReader class.
- Fix the compatibility with HTC devices.
- Synchronize start() and stop() method of AudioJackReader class.
- Return false if the reader is stopped in sendFrame() method of AudioJackReader
  class.
- Add the following classes:
  ReaderNotStartedException
- Throw ReaderNotStartedException in power(), setProtocol(), transmit() and
  control() method of AudioJackReader class.
- Add the following methods to AudioJackReader class:
  updateCardState()
- Rename getState() to getCardState() method of AudioJackReader class.
- Update the documentation.

v1.0.0 Preview 13 (4/9/2014)
- Add the following interfaces to AudioJackReader class:
  OnTrackDataNotificationListener
- Add the following methods to AudioJackReader class:
  setOnTrackDataNotificationListener()
- Target to Android 4.4W platform.

v1.0.0 Preview 12 (15/5/2014)
- Remove the limitation of command length in transmit() and control() method of
  AudioJackReader class.

v1.0.0 Preview 11 (28/3/2014)
- Correct the documentation for CardTimeoutException in transmit() method of
  AudioJackReader class.
- Handle IOCTL_CCID_XFR_BLOCK in control() method of AudioJackReader class.

v1.0.0 Preview 10 (21/3/2014)
- Change the TrackData class to abstract class.
- Add the following classes:
  AesTrackData
  DukptTrackData
  DukptReceiver
- Add the following methods to Track1Data class:
  Track1Data(String)
  fromString()
- Add the following methods to Track2Data class:
  Track2Data(String)
  fromString()
- Add the following constants to AudioJackReader class:
  TRACK_DATA_OPTION_ENCRYPTED_TRACK1
  TRACK_DATA_OPTION_ENCRYPTED_TRACK2
  TRACK_DATA_OPTION_MASKED_TRACK1
  TRACK_DATA_OPTION_MASKED_TRACK2
- Add the following methods to AudioJackReader class:
  getTrackDataOption()
  setTrackDataOption()
  setOnTrackDataOptionAvailableListener()
- Update the package documentation.

v1.0.0 Preview 9 (3/3/2014)
- Improve the sending speed.

v1.0.0 Preview 8 (4/2/2014)
- Add the following constants to AudioJackReader class:
  CARD_POWER_DOWN
  CARD_COLD_RESET
  CARD_WARM_RESET
  PROTOCOL_UNDEFINED
  PROTOCOL_T0
  PROTOCOL_T1
  PROTOCOL_RAW
  PROTOCOL_TX
  PROTOCOL_DEFAULT
  PROTOCOL_OPTIMAL
  CARD_UNKNOWN
  CARD_ABSENT
  CARD_PRESENT
  CARD_SWALLOWED
  CARD_POWERED
  CARD_NEGOTIABLE
  CARD_SPECIFIC
  IOCTL_CCID_ESCAPE
- Add the following methods to AudioJackReader class:
  power()
  setProtocol()
  transmit()
  control()
  getAtr()
  getState()
  getProtocol()
- Add the following exception classes:
  CardTimeoutException
  CommunicationErrorException
  CommunicationTimeoutException
  InvalidDeviceStateException
  ProtocolMismatchException
  ReaderException
  RemovedCardException
  RequestQueueFullException
  UnresponsiveCardException
  UnsupportedCardException

v1.0.0 Preview 7 (16/12/2013)
- Rename the project to AudioJack.
- Rename the package to com.acs.audiojack.
- Rename ACR31Reader class to AudioJackReader class.
- Add the following constants to AudioJackReader class:
  PICC_CARD_TYPE_ISO14443_TYPE_A
  PICC_CARD_TYPE_ISO14443_TYPE_B
  PICC_CARD_TYPE_FELICA_212KBPS
  PICC_CARD_TYPE_FELICA_424KBPS
  PICC_CARD_TYPE_AUTO_RATS
- Add the following methods to AudioJackReader class:
  setOnPiccAtrAvailableListener()
  setOnPiccResponseApduAvailableListener()
  piccPowerOn()
  piccTransmit(int, byte[])
  piccTransmit(int, byte[], int, int)
  piccPowerOff()
  setPiccRfConfig()

v1.0.0 Preview 6 (27/11/2013)
- Target to Android 4.4 platform.
- Limit the received message length to 300.
- Add the noise filter.
- Optimize the encoder performance.
- Add TRACK_ERROR_PARITY constant and getKeySerialNumber() method to TrackData
  class.
- Add the following interfaces to ACR31Reader class:
  OnAuthCompleteListener
  OnCustomIdAvailableListener
  OnDeviceIdAvailableListener
  OnDukptOptionAvailableListener
- Add the following methods to ACR31Reader class:
  setOnAuthCompleteListener()
  setOnCustomIdAvailableListener()
  setOnDeviceIdAvailableListener()
  setOnDukptOptionAvailableListener()
  reset(OnResetCompleteListener)
  authenticate(byte[])
  authenticate(byte[], OnAuthCompleteListener)
  getCustomId()
  setCustomId()
  getDeviceId()
  setMasterKey()
  setAesKey()
  getDukptOption()
  setDukptOption()
  initializeDukpt()
- Add the following constants to ACR31Reader class:
  AUTH_ERROR_SUCCESS
  AUTH_ERROR_FAILURE
  AUTH_ERROR_TIMEOUT
- Add the following constants to Result class:
  ERROR_DUKPT_OPERATION_CEASED
  ERROR_DUKPT_DATA_CORRUPTED
  ERROR_FLASH_DATA_CORRUPTED
  ERROR_VERIFICATION_FAILED

v1.0.0 Preview 5 (27/8/2013)
- Target to Android 4.3 platform.
- Add getJis2Data() method to Track1Data class.

v1.0.0 Preview 4 (11/7/2013)
- Fix a bug that timeout calculation includes the delay from reading audio data.
- Reduce the audio record buffer size in order to improve the response time.

v1.0.0 Preview 3 (9/7/2013)
- Handle normal and inverted audio inputs.
- Clear the pending requests during reset.
- Improve the data parsing in fromByteArray() method from Track1Data and
  Track2Data classes.

v1.0.0 Preview 2 (28/6/2013)
- Remove BATTERY_STATUS_* and TRACK_ERROR_* constants from ACR31Reader class.
- Remove wakeUp(), isSleepMode() and setSleepMode() methods from ACR31Reader
  class.
- Remove OnBatteryStatusAvailableListener class from ACR31Reader class.
- Rename send() to sendCommand() method from ACR31Reader class.
- Modify the parameters in OnFirmwareVersionAvailableListener,
  OnTrackDataAvailableListener and OnRawDataAvailableListener classes from
  ACR31Reader class.
- Add Result, Status and TrackData classes.
- Add createFrame(), sendFrame(), getStatus(), setSleepTimeout() and reset()
  methods to ACR31Reader class.
- Add OnResetCompleteListener, OnResultAvailableListener and
  OnStatusAvailableListener classes to ACR31Reader class.
- Add AudioManager reference in ACR31Reader constructor in order to solo the
  music stream.

v1.0.0 Preview 1 (25/3/2013)
- New release.



Demo

v1.0.0 (27/2/2015)
- Target to Android 5.0.1 platform.
- Handle ERROR_PICC_NO_CARD constant of Result class in toErrorCodeString()
  method of MainActivity class.
- Update android:versionName to 1.0.0.
- Remove android:showAsAction from res/menu/main.xml.
- Replace the strings from res/values/strings.xml in showVersionInfo() method of
  MainActivity class.

v1.0.0 Preview 12 (17/11/2014)
- Mute the audio output if the reader is unplugged.
- Update the version name to "1.0.0 (12)".

v1.0.0 Preview 11 (15/10/2014)
- Add the following methods to MainActivity class:
  toCardStateString()
- Add "Update card state" to ICC subscreen.
- Show the card state after doing power action, setting the protocol and
  transmitting the APDU.
- Add About menu item and show the version information.
- Update the version name to "1.0.0 (11)".

v1.0.0 Preview 10 (4/9/2014)
- Show each track error and continue to show the track data.
- Show the progress of processing the track data.
- Target to Android 4.4W platform.

v1.0.0 Preview 9 (28/3/2014)
- Show the track data if key is null.

v1.0.0 Preview 8 (21/3/2014)
- Add track data setup.
- Show the track data using DUKPT.

v1.0.0 Preview 7 (4/2/2014)
- Add ICC.
- Check the reset volume in Power off, Transmit and Set RF configuration
  preference of PICC subsreen.
- Check the reset volume in Sleep preference.
- Set the reader to sleep before reset in setting custom ID, master key,
  AES key, DUKPT option and DUKPT initialization.
- Add "Use default key" to Cryptographic keys subscreen.
- Add "Use default IKSN & IPEK" to DUKPT setup subscreen.

v1.0.0 Preview 6 (16/12/2013)
- Rename the project to AudioJackDemo.
- Rename the package to com.acs.audiojackdemo.
- Add PICC.
- Change the app name to "AJ Demo".
- Change the wait timeout to 10 seconds.

v1.0.0 Preview 5 (27/11/2013)
- Target to Android 4.4 platform.
- Add About reader, Reader ID, Cryptographic keys and DUKPT setup.
- Rearrange the layout.

v1.0.0 Preview 4 (27/8/2013)
- Target to Android 4.3 platform.
- Fix a bug that swipe count and battery status are not updated if the track
  data contains error.
- Show the JIS2 data.
- Remove the previous fix. Change the theme to non-light in styles.xml to fix
  the problem of black background in sub-screen of PreferenceScreen on Android
  3.x or earlier.
- Fix a bug that the swipe count is reset on next swipe after the user interface
  is rotated.

v1.0.0 Preview 3 (9/7/2013)
- Fix the problem of black background in sub-screen of PreferenceScreen on
  Android 3.x or earlier.
- Check the maximum volume before resetting the reader.

v1.0.0 Preview 2 (28/6/2013)
- Fix the compilation errors with the updated library.
- Add reset, status and swipe count preferences.
- Set the sleep timeout of reader.
- Improve the error handling of track data.

v1.0.0 Preview 1 (25/3/2013)
- New release.



File Contents
-------------

API Documentation:  AudioJack\doc
Sample Application: AudioJackDemo
Android Package:    AudioJackDemo.apk
Class Library:      AudioJackDemo\libs\acsaj-1.0.0.jar



Support
-------

In case of problem, please contact ACS through:

Web Site: http://www.acs.com.hk/
E-mail: info@acs.com.hk
Tel: +852 2796 7873
Fax: +852 2796 1286



-------------------------------------------------------------------------------
Copyright (c) 2013-2015, Advanced Card Systems Ltd.
Copyright (c) 2011, CSE Division, EECS Department, University of Michigan.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

    Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

    Neither the name of the University of Michigan nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (
INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Android is a trademark of Google Inc.
