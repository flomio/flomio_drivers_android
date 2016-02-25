ACS Bluetooth Android Library
Advanced Card Systems Ltd.



Introduction
------------

This library provides classes and interfaces for communicating with ACS
Bluetooth readers.

To install the library to your development environment, see the section
"Installation".



Release Notes
-------------

Version:      1.0.0 Preview 7
Release Date: 29/1/2016

This preview library is subject to change. It may or may not work with your
Android device.

The connection may be lost before the bonding stage. In order to use the
reader, you need to retry to connect it.

System Requirements

- Android 4.3 (Jelly Bean MR2) or above.

Development Environment

- ADT r23.0.2 or above. See Android Developers [1] for more information.

[1] http://developer.android.com/

Supported Readers

- ACR3901U-S1 (v1.04 or above)
- ACR1255U-J1 (v1.12 or above)



Installation
------------

1. To try the demo project, select Import from File menu on Eclipse. Choose
   "BTDemo" to import to your workspace.

2. You can also run the demo project without Eclipse installed. Copy
   "BTDemo.apk" to your Android device. Launch the installation by
   clicking the file icon on any File Explorer application.

3. To use the class library to your project, copy "acsbt-x.y.z.jar" to
   the "libs" folder of your project.



History
-------

Library

v1.0.0 Preview 7 (29/1/2016)
- Enable serial number string in getDeviceInfo() method of Acr1255uj1Reader
  class.

v1.0.0 Preview 6 (29/5/2015)
- Require ACR3901U-S1 v1.04 or above.
- Require ACR1255U-J1 v1.12 or above.
- Add the following methods to BluetoothReader:
  authenticate()
- Rename OnAuthenticationComplete() to onAuthenticationComplete() in
  OnAuthenticationCompleteListener.
- Rename OnResponseApduAvailable() to onResponseApduAvailable() in
  OnResponseApduAvailableListener.
- Rename onCardStatusResponseAvailable() to onCardStatusAvailable() in
  OnCardStatusAvailableListener.
- Remap the error codes in BluetoothReader.
- Update the documentation.
- Target to Android 5.1.1 platform.

v1.0.0 Preview 5 (31/12/2014)
- Fix the connection problem with ACR3901U-S1 firmware v0.04 (bonding disabled)
  by enabling notifications automatically.

v1.0.0 Preview 4 (24/10/2014)
- Add the following class:
  BluetoothReaderGattCallback
- Change API for establishing connection.
- Create GATT connection and callback in application.
- Update the documentation.

v1.0.0 Preview 3 (30/9/2014)
- Enable BluetoothReaderManager to connect with multiple readers.
- Update the documentation.

v1.0.0 Preview 2 (27/8/2014)
- Add the following classes:
  Acr3901us1Reader
  Acr1255uj1Reader
  BluetoothReaderManager
- Convert BluetoothReader to an abstract class.
- Add BluetoothReaderManager class to control the connection and detect the
  model of connected reader by comparing the included services uuid.
- Move the BluetoothGattCallback to BluetoothReaderManager.
- Change the get device information method to getDeviceInfo(int infoId).
- Support ACR1255U-J1 reader.
- Update the documentation.

v1.0.0 Preview 1 (19/6/2014)
- New release.



Demo

v1.0.0 Preview 6 (29/5/2015)
- Fix the bug that System ID is not correctly displayed.
- Rename com.btdemo to com.acs.btdemo.
- Update app_name to BTDemo in res/values/strings.xml.
- Rename onCardStatusResponseAvailable() to onCardStatusAvailable().
- Set Tx power for ACR1255U-J1.
- Add About menu item to show the version information.
- Add the following classes:
  TxPowerDialogFragment
  VersionInfoDialogFragment
- Update the version to 1.0.0 (6).
- Target to Android 5.1.1 platform.

v1.0.0 Preview 5 (31/12/2014)
- Change the version string to "1.0.0 (5)".

v1.0.0 Preview 4 (24/10/2014)
- Change API for establishing connection.
- Create GATT connection and callback in application.
- Control GATT connection by application.

v1.0.0 Preview 3 (30/9/2014)
- Add the following class:
  ReaderActivity
- Remove the following classes:
  Acr1255uj1Activity
  Acr3901us1Activity
- Replace Acr1255uj1Activity and Acr3901us1Activity with ReaderActivity.
- Disable the button if the function is not supported by reader.
- Use Toast to display pop up message.
- Synchronize scanLeDevice method.

v1.0.0 Preview 2 (27/8/2014)
- Detect the reader model using BluetoothReaderManager.
- Add the following class:
  Acr1255uj1Activity
- Enable/disable the polling for ACR1255U-J1.
- Show the battery level for ACR1255U-J1.
- Replace with getDeviceInfo method to read device information of reader.
- Move the set listener methods from onCreate to onReaderDetected.
- Add getEditTextinHexBytes method in Utils.

v1.0.0 Preview 1 (19/6/2014)
- New release.



File Contents
-------------

API Documentation:  ACSBluetooth\doc
Sample Application: BTDemo
Android Package:    BTDemo.apk
Class Library:      BTDemo\libs\acsbt-1.0.0preview7.jar



Support
-------

In case of problem, please contact ACS through:

Web Site: http://www.acs.com.hk/
E-mail: info@acs.com.hk
Tel: +852 2796 7873
Fax: +852 2796 1286



-------------------------------------------------------------------------------
Copyright (C) 2014-2016 Advanced Card Systems Ltd. All Rights Reserved.

No part of this reference manual may be reproduced or transmitted in any from
without the expressed, written permission of ACS.

Due to rapid change in technology, some of specifications mentioned in this
publication are subject to change without notice. Information furnished is
believed to be accurate and reliable. ACS assumes no responsibility for any
errors or omissions, which may appear in this document.

Android is a trademark of Google Inc.
