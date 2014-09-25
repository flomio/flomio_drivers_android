/*
 * Copyright (C) 2013 Advanced Card Systems Ltd. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Advanced
 * Card Systems Ltd. ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ACS.
 */

package com.acs.audiojackdemo;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.acs.audiojack.AesTrackData;
import com.acs.audiojack.AudioJackReader;
import com.acs.audiojack.DukptReceiver;
import com.acs.audiojack.DukptTrackData;
import com.acs.audiojack.Result;
import com.acs.audiojack.Status;
import com.acs.audiojack.Track1Data;
import com.acs.audiojack.Track2Data;
import com.acs.audiojack.TrackData;

/**
 * The <code>MainActivity</code> class is a preference activity and it
 * demonstrates the functionality of ACS audio jack reader.
 * 
 * @author Godfrey Chung
 * @version 1.0, 21 Feb 2013
 */
public class MainActivity extends PreferenceActivity {

    public static final String DEFAULT_MASTER_KEY_STRING = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
    public static final String DEFAULT_AES_KEY_STRING = "4E 61 74 68 61 6E 2E 4C 69 20 54 65 64 64 79 20";
    public static final String DEFAULT_IKSN_STRING = "FF FF 98 76 54 32 10 E0 00 00";
    public static final String DEFAULT_IPEK_STRING = "6A C2 92 FA A1 31 5B 4D 85 8A B3 A3 D7 D5 93 3A";

    private class OnGetFirmwareVersionPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Get the firmware version. */
                mFirmwareVersionReady = false;
                mResultReady = false;
                if (!reader.getFirmwareVersion()) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the firmware version. */
                    showFirmwareVersion();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Clear the firmware version. */
            mAboutReaderFirmwareVersionPreference.setSummary("");

            /* Show the progress. */
            mProgress.setMessage("Getting the firmware version...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnGetStatusPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Get the status. */
                mStatusReady = false;
                mResultReady = false;
                if (!reader.getStatus()) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the status. */
                    showStatus();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Clear the status. */
            mAboutReaderBatteryLevelPreference.setSummary("");
            mAboutReaderSleepTimeoutPreference.setSummary("");

            /* Show the progress. */
            mProgress.setMessage("Getting the status...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnSetSleepTimeoutPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Set the sleep timeout. */
                mResultReady = false;
                if (!mReader.setSleepTimeout(mSleepTimeout)) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the result. */
                    if (showResult()) {

                        /* Get the status. */
                        mStatusReady = false;
                        mResultReady = false;
                        if (!reader.getStatus()) {

                            /* Show the request queue error. */
                            showRequestQueueError();

                        } else {

                            /* Show the status. */
                            showStatus();
                        }
                    }
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        private class OnOkClickListener implements
                DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                String text = mSleepTimeoutEditText.getText().toString();

                try {
                    mSleepTimeout = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    mSleepTimeout = 0;
                }

                /* Show the progress. */
                mProgress.setMessage("Setting the sleep timeout...");
                mProgress.show();

                /* Reset the reader. */
                mReader.reset(new OnResetCompleteListener());

                dialog.dismiss();
            }
        }

        private class OnCancelClickListener implements
                DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }

        private EditText mSleepTimeoutEditText;
        private int mSleepTimeout;

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            mSleepTimeoutEditText = new EditText(mContext);
            mSleepTimeoutEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

            builder.setMessage(R.string.dialog_message_sleep_timeout)
                    .setTitle(R.string.pref_title_sleep_timeout)
                    .setView(mSleepTimeoutEditText)
                    .setPositiveButton(R.string.ok, new OnOkClickListener())
                    .setNegativeButton(R.string.cancel,
                            new OnCancelClickListener());

            builder.show();
            return true;
        }
    }

    private class OnGetCustomIdPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Get the custom ID. */
                mCustomIdReady = false;
                mResultReady = false;
                if (!reader.getCustomId()) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the custom ID. */
                    showCustomId();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Clear the custom ID. */
            mReaderIdCustomIdPreference.setSummary("");

            /* Show the progress. */
            mProgress.setMessage("Getting the custom ID...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnSetCustomIdPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Authenticate the reader. */
                reader.authenticate(mMasterKey, new OnAuthCompleteListener());
            }
        }

        private class OnResetCompleteListener2 implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Get the custom ID. */
                mCustomIdReady = false;
                mResultReady = false;
                if (!reader.getCustomId()) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the custom ID. */
                    showCustomId();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        private class OnAuthCompleteListener implements
                AudioJackReader.OnAuthCompleteListener {

            @Override
            public void onAuthComplete(AudioJackReader reader, int errorCode) {

                boolean dismissed = true;

                if (errorCode == AudioJackReader.AUTH_ERROR_SUCCESS) {

                    byte[] buffer = null;
                    byte[] customId = new byte[10];

                    try {
                        buffer = mCustomIdString.getBytes("US-ASCII");
                    } catch (UnsupportedEncodingException e) {
                        buffer = null;
                    }

                    if (buffer != null) {
                        System.arraycopy(buffer, 0, customId, 0,
                                (buffer.length > 10) ? 10 : buffer.length);
                    }

                    /* Set the custom ID. */
                    mResultReady = false;
                    if (!reader.setCustomId(customId)) {

                        /* Show the request queue error. */
                        showRequestQueueError();

                    } else {

                        /* Show the result. */
                        if (showResult()) {

                            dismissed = false;

                            /* Set the reader to sleep. */
                            mResultReady = false;
                            if (!mReader.sleep()) {

                                /* Show the request queue error. */
                                showRequestQueueError();

                            } else {

                                /* Show the result. */
                                showResult();
                            }

                            /* Reset the reader to take effect. */
                            reader.reset(new OnResetCompleteListener2());
                        }
                    }

                } else if (errorCode == AudioJackReader.AUTH_ERROR_TIMEOUT) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication timeout. */
                            Toast.makeText(mContext,
                                    "The authentication timed out.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });

                } else {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication failure. */
                            Toast.makeText(mContext,
                                    "The authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });
                }

                /* Hide the progress. */
                if (dismissed) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mProgress.dismiss();
                        };
                    });
                }
            }
        }

        private class OnOkClickListener implements
                DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                /* Get the custom ID string. */
                mCustomIdString = mCustomIdEditText.getText().toString();

                /* Show the progress. */
                mProgress.setMessage("Setting the custom ID...");
                mProgress.show();

                /* Reset the reader. */
                mReader.reset(new OnResetCompleteListener());

                dialog.dismiss();
            }
        }

        private class OnCancelClickListener implements
                DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }

        private EditText mCustomIdEditText;
        private String mCustomIdString;

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            mCustomIdEditText = new EditText(mContext);

            builder.setMessage(R.string.dialog_message_custom_id)
                    .setTitle(R.string.pref_title_custom_id)
                    .setView(mCustomIdEditText)
                    .setPositiveButton(R.string.ok, new OnOkClickListener())
                    .setNegativeButton(R.string.cancel,
                            new OnCancelClickListener());

            builder.show();
            return true;
        }
    }

    private class OnGetDeviceIdPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Get the device ID. */
                mDeviceIdReady = false;
                mResultReady = false;
                if (!reader.getDeviceId()) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the device ID. */
                    showDeviceId();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Clear the device ID. */
            mReaderIdDeviceIdPreference.setSummary("");

            /* Show the progress. */
            mProgress.setMessage("Getting the device ID...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnNewMasterKeyPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference newMasterKeyPref = (EditTextPreference) preference;
            String newMasterKeyString = (String) newValue;
            byte[] newMasterKey = new byte[16];

            if (toByteArray(newMasterKeyString, newMasterKey) != 16) {

                showMessageDialog(R.string.error,
                        R.string.message_new_master_key_error_length);

            } else {

                System.arraycopy(newMasterKey, 0, mNewMasterKey, 0,
                        newMasterKey.length);
                newMasterKeyString = toHexString(mNewMasterKey);
                newMasterKeyPref.setText(newMasterKeyString);
                newMasterKeyPref.setSummary(newMasterKeyString);
            }

            return true;
        }
    }

    private class OnMasterKeyPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference masterKeyPref = (EditTextPreference) preference;
            String masterKeyString = (String) newValue;
            byte[] masterKey = new byte[16];

            if (toByteArray(masterKeyString, masterKey) != 16) {

                showMessageDialog(R.string.error,
                        R.string.message_master_key_error_length);

            } else {

                System.arraycopy(masterKey, 0, mMasterKey, 0, masterKey.length);
                masterKeyString = toHexString(mMasterKey);
                masterKeyPref.setText(masterKeyString);
                masterKeyPref.setSummary(masterKeyString);
            }

            return true;
        }
    }

    private class OnSetMasterKeyPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Authenticate the reader. */
                reader.authenticate(mMasterKey, new OnAuthCompleteListener());
            }
        }

        private class OnResetCompleteListener2 implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        private class OnAuthCompleteListener implements
                AudioJackReader.OnAuthCompleteListener {

            @Override
            public void onAuthComplete(AudioJackReader reader, int errorCode) {

                boolean dismissed = true;

                if (errorCode == AudioJackReader.AUTH_ERROR_SUCCESS) {

                    /* Set the master key. */
                    mResultReady = false;
                    if (!reader.setMasterKey(mNewMasterKey)) {

                        /* Show the request queue error. */
                        showRequestQueueError();

                    } else {

                        /* Show the result. */
                        if (showResult()) {

                            dismissed = false;

                            /* Set the reader to sleep. */
                            mResultReady = false;
                            if (!mReader.sleep()) {

                                /* Show the request queue error. */
                                showRequestQueueError();

                            } else {

                                /* Show the result. */
                                showResult();
                            }

                            /* Reset the reader to take effect. */
                            reader.reset(new OnResetCompleteListener2());
                        }
                    }

                } else if (errorCode == AudioJackReader.AUTH_ERROR_TIMEOUT) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication timeout. */
                            Toast.makeText(mContext,
                                    "The authentication timed out.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });

                } else {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication failure. */
                            Toast.makeText(mContext,
                                    "The authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });
                }

                /* Hide the progress. */
                if (dismissed) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mProgress.dismiss();
                        };
                    });
                }
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Setting the master key...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnAesKeyPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference aesKeyPref = (EditTextPreference) preference;
            String aesKeyString = (String) newValue;
            byte[] aesKey = new byte[16];

            if (toByteArray(aesKeyString, aesKey) != 16) {

                showMessageDialog(R.string.error,
                        R.string.message_aes_key_error_length);

            } else {

                System.arraycopy(aesKey, 0, mAesKey, 0, aesKey.length);
                aesKeyString = toHexString(mAesKey);
                aesKeyPref.setText(aesKeyString);
                aesKeyPref.setSummary(aesKeyString);
            }

            return true;
        }
    }

    private class OnSetAesKeyPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Authenticate the reader. */
                reader.authenticate(mMasterKey, new OnAuthCompleteListener());
            }
        }

        private class OnResetCompleteListener2 implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        private class OnAuthCompleteListener implements
                AudioJackReader.OnAuthCompleteListener {

            @Override
            public void onAuthComplete(AudioJackReader reader, int errorCode) {

                boolean dismissed = true;

                if (errorCode == AudioJackReader.AUTH_ERROR_SUCCESS) {

                    /* Set the AES key. */
                    mResultReady = false;
                    if (!reader.setAesKey(mAesKey)) {

                        /* Show the request queue error. */
                        showRequestQueueError();

                    } else {

                        /* Show the result. */
                        if (showResult()) {

                            dismissed = false;

                            /* Set the reader to sleep. */
                            mResultReady = false;
                            if (!mReader.sleep()) {

                                /* Show the request queue error. */
                                showRequestQueueError();

                            } else {

                                /* Show the result. */
                                showResult();
                            }

                            /* Reset the reader to take effect. */
                            reader.reset(new OnResetCompleteListener2());
                        }
                    }

                } else if (errorCode == AudioJackReader.AUTH_ERROR_TIMEOUT) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication timeout. */
                            Toast.makeText(mContext,
                                    "The authentication timed out.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });

                } else {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication failure. */
                            Toast.makeText(mContext,
                                    "The authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });
                }

                /* Hide the progress. */
                if (dismissed) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mProgress.dismiss();
                        };
                    });
                }
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Setting the AES key...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnUseDefaultKeyPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {

            toByteArray(DEFAULT_MASTER_KEY_STRING, mNewMasterKey);
            mCryptographicKeysNewMasterKeyPreference
                    .setText(DEFAULT_MASTER_KEY_STRING);
            mCryptographicKeysNewMasterKeyPreference
                    .setSummary(DEFAULT_MASTER_KEY_STRING);

            toByteArray(DEFAULT_MASTER_KEY_STRING, mMasterKey);
            mCryptographicKeysMasterKeyPreference
                    .setText(DEFAULT_MASTER_KEY_STRING);
            mCryptographicKeysMasterKeyPreference
                    .setSummary(DEFAULT_MASTER_KEY_STRING);

            toByteArray(DEFAULT_AES_KEY_STRING, mAesKey);
            mCryptographicKeysAesKeyPreference.setText(DEFAULT_AES_KEY_STRING);
            mCryptographicKeysAesKeyPreference
                    .setSummary(DEFAULT_AES_KEY_STRING);

            return true;
        }
    }

    private class OnGetDukptOptionPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Get the DUKPT option. */
                mDukptOptionReady = false;
                mResultReady = false;
                if (!reader.getDukptOption()) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the DUKPT option. */
                    showDukptOption();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Getting the DUKPT option...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnSetDukptOptionPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Authenticate the reader. */
                reader.authenticate(mMasterKey, new OnAuthCompleteListener());
            }
        }

        private class OnResetCompleteListener2 implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Get the DUKPT option. */
                mDukptOptionReady = false;
                mResultReady = false;
                if (!reader.getDukptOption()) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the DUKPT option. */
                    showDukptOption();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        private class OnAuthCompleteListener implements
                AudioJackReader.OnAuthCompleteListener {

            @Override
            public void onAuthComplete(AudioJackReader reader, int errorCode) {

                boolean dismissed = true;

                if (errorCode == AudioJackReader.AUTH_ERROR_SUCCESS) {

                    /* Set the DUKPT option. */
                    mResultReady = false;
                    if (!reader.setDukptOption(mDukptSetupDukptPreference
                            .isChecked())) {

                        /* Show the request queue error. */
                        showRequestQueueError();

                    } else {

                        /* Show the result. */
                        if (showResult()) {

                            dismissed = false;

                            /* Set the reader to sleep. */
                            mResultReady = false;
                            if (!mReader.sleep()) {

                                /* Show the request queue error. */
                                showRequestQueueError();

                            } else {

                                /* Show the result. */
                                showResult();
                            }

                            /* Reset the reader to take effect. */
                            reader.reset(new OnResetCompleteListener2());
                        }
                    }

                } else if (errorCode == AudioJackReader.AUTH_ERROR_TIMEOUT) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication timeout. */
                            Toast.makeText(mContext,
                                    "The authentication timed out.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });

                } else {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication failure. */
                            Toast.makeText(mContext,
                                    "The authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });
                }

                /* Hide the progress. */
                if (dismissed) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mProgress.dismiss();
                        };
                    });
                }
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Setting the DUKPT option...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnIksnPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference iksnPref = (EditTextPreference) preference;
            String iksnString = (String) newValue;
            byte[] iksn = new byte[10];

            if (toByteArray(iksnString, iksn) != 10) {

                showMessageDialog(R.string.error,
                        R.string.message_iksn_error_length);

            } else {

                System.arraycopy(iksn, 0, mIksn, 0, iksn.length);
                iksnString = toHexString(mIksn);
                iksnPref.setText(iksnString);
                iksnPref.setSummary(iksnString);

                /* Set the key serial number. */
                mDukptReceiver.setKeySerialNumber(mIksn);

                /* Load the initial key. */
                mDukptReceiver.loadInitialKey(mIpek);
            }

            return true;
        }
    }

    private class OnIpekPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference ipekPref = (EditTextPreference) preference;
            String ipekString = (String) newValue;
            byte[] ipek = new byte[16];

            if (toByteArray(ipekString, ipek) != 16) {

                showMessageDialog(R.string.error,
                        R.string.message_ipek_error_length);

            } else {

                System.arraycopy(ipek, 0, mIpek, 0, ipek.length);
                ipekString = toHexString(mIpek);
                ipekPref.setText(ipekString);
                ipekPref.setSummary(ipekString);

                /* Set the key serial number. */
                mDukptReceiver.setKeySerialNumber(mIksn);

                /* Load the initial key. */
                mDukptReceiver.loadInitialKey(mIpek);
            }

            return true;
        }
    }

    private class OnInitializeDukptPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Authenticate the reader. */
                reader.authenticate(mMasterKey, new OnAuthCompleteListener());
            }
        }

        private class OnResetCompleteListener2 implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        private class OnAuthCompleteListener implements
                AudioJackReader.OnAuthCompleteListener {

            @Override
            public void onAuthComplete(AudioJackReader reader, int errorCode) {

                boolean dismissed = true;

                if (errorCode == AudioJackReader.AUTH_ERROR_SUCCESS) {

                    /* Set the DUKPT option. */
                    mResultReady = false;
                    if (!reader.initializeDukpt(mIksn, mIpek)) {

                        /* Show the request queue error. */
                        showRequestQueueError();

                    } else {

                        /* Show the result. */
                        if (showResult()) {

                            dismissed = false;

                            /* Set the reader to sleep. */
                            mResultReady = false;
                            if (!mReader.sleep()) {

                                /* Show the request queue error. */
                                showRequestQueueError();

                            } else {

                                /* Show the result. */
                                showResult();
                            }

                            /* Reset the reader to take effect. */
                            reader.reset(new OnResetCompleteListener2());
                        }
                    }

                } else if (errorCode == AudioJackReader.AUTH_ERROR_TIMEOUT) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication timeout. */
                            Toast.makeText(mContext,
                                    "The authentication timed out.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });

                } else {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication failure. */
                            Toast.makeText(mContext,
                                    "The authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });
                }

                /* Hide the progress. */
                if (dismissed) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mProgress.dismiss();
                        };
                    });
                }
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Initializing the DUKPT...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnUseDefaultIksnIpekPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {

            toByteArray(DEFAULT_IKSN_STRING, mIksn);
            mDukptSetupIksnPreference.setText(DEFAULT_IKSN_STRING);
            mDukptSetupIksnPreference.setSummary(DEFAULT_IKSN_STRING);

            toByteArray(DEFAULT_IPEK_STRING, mIpek);
            mDukptSetupIpekPreference.setText(DEFAULT_IPEK_STRING);
            mDukptSetupIpekPreference.setSummary(DEFAULT_IPEK_STRING);

            /* Set the key serial number. */
            mDukptReceiver.setKeySerialNumber(mIksn);

            /* Load the initial key. */
            mDukptReceiver.loadInitialKey(mIpek);

            return true;
        }
    }

    private class OnGetTrackDataOptionPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Getting the track data option...");
            mProgress.show();

            new Thread(new Runnable() {

                @Override
                public void run() {

                    /* Get the track data option. */
                    mTrackDataOptionReady = false;
                    mResultReady = false;
                    if (!mReader.getTrackDataOption()) {

                        /* Show the request queue error. */
                        showRequestQueueError();

                    } else {

                        /* Show the track data option. */
                        showTrackDataOption();
                    }

                    /* Hide the progress. */
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mProgress.dismiss();
                        };
                    });
                }
            }).start();

            return true;
        }
    }

    private class OnSetTrackDataOptionPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Authenticate the reader. */
                reader.authenticate(mMasterKey, new OnAuthCompleteListener());
            }
        }

        private class OnResetCompleteListener2 implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Get the track data option. */
                mTrackDataOptionReady = false;
                mResultReady = false;
                if (!mReader.getTrackDataOption()) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the track data option. */
                    showTrackDataOption();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        private class OnAuthCompleteListener implements
                AudioJackReader.OnAuthCompleteListener {

            @Override
            public void onAuthComplete(AudioJackReader reader, int errorCode) {

                boolean dismissed = true;

                if (errorCode == AudioJackReader.AUTH_ERROR_SUCCESS) {

                    int option = 0;

                    if (mTrackDataSetupEncryptedTrack1Preference.isChecked()) {
                        option |= AudioJackReader.TRACK_DATA_OPTION_ENCRYPTED_TRACK1;
                    }

                    if (mTrackDataSetupEncryptedTrack2Preference.isChecked()) {
                        option |= AudioJackReader.TRACK_DATA_OPTION_ENCRYPTED_TRACK2;
                    }

                    if (mTrackDataSetupMaskedTrack1Preference.isChecked()) {
                        option |= AudioJackReader.TRACK_DATA_OPTION_MASKED_TRACK1;
                    }

                    if (mTrackDataSetupMaskedTrack2Preference.isChecked()) {
                        option |= AudioJackReader.TRACK_DATA_OPTION_MASKED_TRACK2;
                    }

                    /* Set the track data option. */
                    mResultReady = false;
                    if (!reader.setTrackDataOption(option)) {

                        /* Show the request queue error. */
                        showRequestQueueError();

                    } else {

                        /* Show the result. */
                        if (showResult()) {

                            dismissed = false;

                            /* Set the reader to sleep. */
                            mResultReady = false;
                            if (!mReader.sleep()) {

                                /* Show the request queue error. */
                                showRequestQueueError();

                            } else {

                                /* Show the result. */
                                showResult();
                            }

                            /* Reset the reader to take effect. */
                            reader.reset(new OnResetCompleteListener2());
                        }
                    }

                } else if (errorCode == AudioJackReader.AUTH_ERROR_TIMEOUT) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication timeout. */
                            Toast.makeText(mContext,
                                    "The authentication timed out.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });

                } else {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Show the authentication failure. */
                            Toast.makeText(mContext,
                                    "The authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        };
                    });
                }

                /* Hide the progress. */
                if (dismissed) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mProgress.dismiss();
                        };
                    });
                }
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Setting the track data option...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnIccPowerActionPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            ListPreference iccPowerActionPref = (ListPreference) preference;
            String iccPowerActionString = (String) newValue;

            mIccPowerAction = iccPowerActionPref
                    .findIndexOfValue(iccPowerActionString);
            iccPowerActionPref.setSummary(iccPowerActionString);

            return true;
        }
    }

    private class OnIccWaitTimeoutPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference iccWaitTimeoutPref = (EditTextPreference) preference;
            String iccWaitTimeoutString = (String) newValue;

            try {
                mIccWaitTimeout = Integer.parseInt(iccWaitTimeoutString);
            } catch (NumberFormatException e) {
                mIccWaitTimeout = 10000;
            }

            iccWaitTimeoutString = Integer.toString(mIccWaitTimeout);
            iccWaitTimeoutPref.setText(iccWaitTimeoutString);
            iccWaitTimeoutPref.setSummary(iccWaitTimeoutString + " ms");

            return true;
        }
    }

    private class OnIccPowerPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private byte[] mAtr;

        @Override
        public boolean onPreferenceClick(Preference preference) {

            String[] messages = { "Powering down the ICC...",
                    "Resetting the ICC (cold reset)...",
                    "Resetting the ICC (warm reset)..." };

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Clear the ATR. */
            mAtr = null;
            mIccAtrPreference.setSummary("");

            /* Show the progress. */
            mProgress.setMessage(messages[mIccPowerAction]);
            mProgress.show();

            new Thread(new Runnable() {

                @Override
                public void run() {

                    /* Do the power action. */
                    try {
                        mAtr = mReader.power(0, mIccPowerAction,
                                mIccWaitTimeout);
                    } catch (Exception e) {
                        showException(e);
                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Hide the progress. */
                            mProgress.dismiss();

                            /* Show the ATR. */
                            mIccAtrPreference.setSummary(toHexString(mAtr));
                        };
                    });
                }
            }).start();

            return true;
        }
    }

    private class OnIccSetProtocolPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        int mActiveProtocol;

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Clear the active protocol. */
            mActiveProtocol = 0;
            mIccActiveProtocolPreference.setSummary("");

            /* Show the progress. */
            mProgress.setMessage("Setting the protocol...");
            mProgress.show();

            new Thread(new Runnable() {

                @Override
                public void run() {

                    int preferredProtocols = 0;

                    if (mIccT0Preference.isChecked()) {
                        preferredProtocols |= AudioJackReader.PROTOCOL_T0;
                    }

                    if (mIccT1Preference.isChecked()) {
                        preferredProtocols |= AudioJackReader.PROTOCOL_T1;
                    }

                    /* Set the protocol. */
                    try {
                        mActiveProtocol = mReader.setProtocol(0,
                                preferredProtocols, mIccWaitTimeout);
                    } catch (Exception e) {
                        showException(e);
                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Hide the progress. */
                            mProgress.dismiss();

                            /* Show the active protocol. */
                            switch (mActiveProtocol) {

                            case AudioJackReader.PROTOCOL_T0:
                                mIccActiveProtocolPreference.setSummary("T=0");
                                break;

                            case AudioJackReader.PROTOCOL_T1:
                                mIccActiveProtocolPreference.setSummary("T=1");
                                break;

                            default:
                                mIccActiveProtocolPreference
                                        .setSummary("Unknown");
                                break;
                            }
                        };
                    });
                }
            }).start();

            return true;
        }
    }

    private class OnIccCommandApduPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference iccCommandApduPref = (EditTextPreference) preference;
            String iccCommandApduString = (String) newValue;

            if (!iccCommandApduString.equals("")) {
                mIccCommandApdu = toByteArray(iccCommandApduString);
            }

            iccCommandApduString = toHexString(mIccCommandApdu);
            iccCommandApduPref.setText(iccCommandApduString);
            iccCommandApduPref.setSummary(iccCommandApduString);

            return true;
        }
    }

    private class OnIccTransmitPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private byte[] mResponseApdu;

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Clear the response APDU. */
            mResponseApdu = null;
            mIccResponseApduPreference.setSummary("");

            /* Show the progress. */
            mProgress.setMessage("Transmitting the command APDU...");
            mProgress.show();

            new Thread(new Runnable() {

                @Override
                public void run() {

                    /* Transmit the APDU. */
                    try {
                        mResponseApdu = mReader.transmit(0, mIccCommandApdu,
                                mIccWaitTimeout);
                    } catch (Exception e) {
                        showException(e);
                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Hide the progress. */
                            mProgress.dismiss();

                            /* Show the response APDU. */
                            mIccResponseApduPreference
                                    .setSummary(toHexString(mResponseApdu));
                        }
                    });
                }
            }).start();

            return true;
        }
    }

    private class OnIccControlCodePreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference iccControlCodePref = (EditTextPreference) preference;
            String iccControlCodeString = (String) newValue;

            try {
                mIccControlCode = Integer.parseInt(iccControlCodeString);
            } catch (NumberFormatException e) {
                mIccControlCode = AudioJackReader.IOCTL_CCID_ESCAPE;
            }

            iccControlCodeString = Integer.toString(mIccControlCode);
            iccControlCodePref.setText(iccControlCodeString);
            iccControlCodePref.setSummary(iccControlCodeString);

            return true;
        }
    }

    private class OnIccControlCommandPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference iccControlCommandPref = (EditTextPreference) preference;
            String iccControlCommandString = (String) newValue;

            if (!iccControlCommandString.equals("")) {
                mIccControlCommand = toByteArray(iccControlCommandString);
            }

            iccControlCommandString = toHexString(mIccControlCommand);
            iccControlCommandPref.setText(iccControlCommandString);
            iccControlCommandPref.setSummary(iccControlCommandString);

            return true;
        }
    }

    private class OnIccControlPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private byte[] mControlResponse;

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Clear the control response. */
            mControlResponse = null;
            mIccControlResponsePreference.setSummary("");

            /* Show the progress. */
            mProgress.setMessage("Transmitting the control command...");
            mProgress.show();

            new Thread(new Runnable() {

                @Override
                public void run() {

                    /* Transmit the control command. */
                    try {
                        mControlResponse = mReader.control(0, mIccControlCode,
                                mIccControlCommand, mIccWaitTimeout);
                    } catch (Exception e) {
                        showException(e);
                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            /* Hide the progress. */
                            mProgress.dismiss();

                            /* Show the control response. */
                            mIccControlResponsePreference
                                    .setSummary(toHexString(mControlResponse));
                        }
                    });
                }
            }).start();

            return true;
        }
    }

    private class OnPiccTimeoutPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference piccTimeoutPref = (EditTextPreference) preference;
            String piccTimeoutString = (String) newValue;

            try {
                mPiccTimeout = Integer.parseInt(piccTimeoutString);
            } catch (NumberFormatException e) {
                mPiccTimeout = 1;
            }

            piccTimeoutString = Integer.toString(mPiccTimeout);
            piccTimeoutPref.setText(piccTimeoutString);
            piccTimeoutPref.setSummary(piccTimeoutString + " secs");

            return true;
        }
    }

    private class OnPiccCardTypePreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference piccCardTypePref = (EditTextPreference) preference;
            String piccCardTypeString = (String) newValue;
            byte[] cardType = new byte[1];

            toByteArray(piccCardTypeString, cardType);
            mPiccCardType = cardType[0] & 0xFF;
            if (mPiccCardType == 0) {
                mPiccCardType = 0x8F;
            }

            piccCardTypeString = toHexString(mPiccCardType);
            piccCardTypePref.setText(piccCardTypeString);
            piccCardTypePref.setSummary(piccCardTypeString);

            return true;
        }
    }

    private class OnPiccPowerOnPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class PowerOn implements Runnable {

            @Override
            public void run() {

                /* Power on the PICC. */
                mPiccAtrReady = false;
                mResultReady = false;
                if (!mReader.piccPowerOn(mPiccTimeout, mPiccCardType)) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the PICC ATR. */
                    showPiccAtr();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Clear the ATR. */
            mPiccAtrPreference.setSummary("");

            /* Show the progress. */
            mProgress.setMessage("Powering on the PICC...");
            mProgress.show();

            new Thread(new PowerOn()).start();
            return true;
        }
    }

    private class OnPiccPowerOffPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class PowerOff implements Runnable {

            @Override
            public void run() {

                /* Power off the PICC. */
                mResultReady = false;
                if (!mReader.piccPowerOff()) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the result. */
                    showResult();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Powering off the PICC...");
            mProgress.show();

            new Thread(new PowerOff()).start();
            return true;
        }
    }

    private class OnPiccCommandApduPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference piccCommandApduPref = (EditTextPreference) preference;
            String piccCommandApduString = (String) newValue;

            if (!piccCommandApduString.equals("")) {
                mPiccCommandApdu = toByteArray(piccCommandApduString);
            }

            piccCommandApduString = toHexString(mPiccCommandApdu);
            piccCommandApduPref.setText(piccCommandApduString);
            piccCommandApduPref.setSummary(piccCommandApduString);

            return true;
        }
    }

    private class OnPiccTransmitPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class Transmit implements Runnable {

            @Override
            public void run() {

                /* Transmit the command APDU. */
                mPiccResponseApduReady = false;
                mResultReady = false;
                if (!mReader.piccTransmit(mPiccTimeout, mPiccCommandApdu)) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the PICC response APDU. */
                    showPiccResponseApdu();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Clear the response APDU. */
            mPiccResponseApduPreference.setSummary("");

            /* Show the progress. */
            mProgress.setMessage("Transmitting the command APDU...");
            mProgress.show();

            new Thread(new Transmit()).start();
            return true;
        }
    }

    private class OnPiccRfConfigPreferenceChangeListener implements
            Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            EditTextPreference piccRfConfigPref = (EditTextPreference) preference;
            String piccRfConfigString = (String) newValue;
            byte[] piccRfConfig = new byte[19];

            if (toByteArray(piccRfConfigString, piccRfConfig) != 19) {

                showMessageDialog(R.string.error,
                        R.string.message_picc_rf_config_error_length);

            } else {

                System.arraycopy(piccRfConfig, 0, mPiccRfConfig, 0,
                        piccRfConfig.length);
                piccRfConfigString = toHexString(mPiccRfConfig);
                piccRfConfigPref.setText(piccRfConfigString);
                piccRfConfigPref.setSummary(piccRfConfigString);
            }

            return true;
        }
    }

    private class OnPiccSetRfConfigPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class SetRfConfig implements Runnable {

            @Override
            public void run() {

                /* Set the PICC RF configuration. */
                mResultReady = false;
                if (!mReader.setPiccRfConfig(mPiccRfConfig)) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the result. */
                    showResult();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Setting the RF configuration...");
            mProgress.show();

            new Thread(new SetRfConfig()).start();
            return true;
        }
    }

    private class OnResetPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnResetCompleteListener implements
                AudioJackReader.OnResetCompleteListener {

            @Override
            public void onResetComplete(AudioJackReader reader) {

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Resetting the reader...");
            mProgress.show();

            /* Reset the reader. */
            mReader.reset(new OnResetCompleteListener());

            return true;
        }
    }

    private class OnSleepPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class Sleep implements Runnable {

            @Override
            public void run() {

                /* Set the reader to sleep. */
                mResultReady = false;
                if (!mReader.sleep()) {

                    /* Show the request queue error. */
                    showRequestQueueError();

                } else {

                    /* Show the result. */
                    showResult();
                }

                /* Hide the progress. */
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgress.dismiss();
                    };
                });
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            /* Check the reset volume. */
            if (!checkResetVolume()) {
                return true;
            }

            /* Show the progress. */
            mProgress.setMessage("Setting the reader to sleep...");
            mProgress.show();

            new Thread(new Sleep()).start();
            return true;
        }
    }

    private class OnDataReceivedPreferenceClickListener implements
            Preference.OnPreferenceClickListener {

        private class OnOkClickListener implements
                DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            CharSequence summary = preference.getSummary();

            if ((summary != null) && (summary.length() > 0)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setMessage(summary)
                        .setTitle(R.string.pref_title_data_received)
                        .setPositiveButton(R.string.ok, new OnOkClickListener());
                builder.show();
            }

            return true;
        }
    }

    private class OnResultAvailableListener implements
            AudioJackReader.OnResultAvailableListener {

        @Override
        public void onResultAvailable(AudioJackReader reader, Result result) {

            synchronized (mResponseEvent) {

                /* Store the result. */
                mResult = result;

                /* Trigger the response event. */
                mResultReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }

    private class OnFirmwareVersionAvailableListener implements
            AudioJackReader.OnFirmwareVersionAvailableListener {

        @Override
        public void onFirmwareVersionAvailable(AudioJackReader reader,
                String firmwareVersion) {

            synchronized (mResponseEvent) {

                /* Store the firmware version. */
                mFirmwareVersion = firmwareVersion;

                /* Trigger the response event. */
                mFirmwareVersionReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }

    private class OnStatusAvailableListener implements
            AudioJackReader.OnStatusAvailableListener {

        @Override
        public void onStatusAvailable(AudioJackReader reader, Status status) {

            synchronized (mResponseEvent) {

                /* Store the status. */
                mStatus = status;

                /* Trigger the response event. */
                mStatusReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }

    private class OnTrackDataAvailableListener implements
            AudioJackReader.OnTrackDataAvailableListener {

        private Track1Data mTrack1Data;
        private Track2Data mTrack2Data;
        private Track1Data mTrack1MaskedData;
        private Track2Data mTrack2MaskedData;
        private String mTrack1MacString;
        private String mTrack2MacString;
        private String mBatteryStatusString;
        private String mKeySerialNumberString;

        @Override
        public void onTrackDataAvailable(AudioJackReader reader,
                TrackData trackData) {

            mTrack1Data = new Track1Data();
            mTrack2Data = new Track2Data();
            mTrack1MaskedData = new Track1Data();
            mTrack2MaskedData = new Track2Data();
            mTrack1MacString = "";
            mTrack2MacString = "";
            mBatteryStatusString = toBatteryStatusString(trackData
                    .getBatteryStatus());
            mKeySerialNumberString = "";

            /* Show the track error. */
            if ((trackData.getTrack1ErrorCode() != 0)
                    || (trackData.getTrack2ErrorCode() != 0)) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showMessageDialog(R.string.error,
                                R.string.message_track_data_error_corrupted);
                    }
                });

                /* Show the track data. */
                showTrackData();
                return;
            }

            /* Show the track data. */
            if (trackData instanceof AesTrackData) {
                showAesTrackData((AesTrackData) trackData);
            } else if (trackData instanceof DukptTrackData) {
                showDukptTrackData((DukptTrackData) trackData);
            }
        }

        /**
         * Shows the AES track data.
         * 
         * @param trackData
         *            the AES track data.
         */
        private void showAesTrackData(AesTrackData trackData) {

            byte[] decryptedTrackData = null;

            /* Decrypt the track data. */
            try {

                decryptedTrackData = aesDecrypt(mAesKey,
                        trackData.getTrackData());

            } catch (GeneralSecurityException e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showMessageDialog(R.string.error,
                                R.string.message_track_data_error_decrypted);
                    }
                });

                /* Show the track data. */
                showTrackData();
                return;
            }

            /* Verify the track data. */
            if (!mReader.verifyData(decryptedTrackData)) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showMessageDialog(R.string.error,
                                R.string.message_track_data_error_checksum);
                    }
                });

                /* Show the track data. */
                showTrackData();
                return;
            }

            /* Decode the track data. */
            mTrack1Data.fromByteArray(decryptedTrackData, 0,
                    trackData.getTrack1Length());
            mTrack2Data.fromByteArray(decryptedTrackData, 79,
                    trackData.getTrack2Length());

            /* Show the track data. */
            showTrackData();
        }

        /**
         * Shows the DUKPT track data.
         * 
         * @param trackData
         *            the DUKPT track data.
         */
        private void showDukptTrackData(DukptTrackData trackData) {

            int ec = 0;
            int ec2 = 0;
            byte[] track1Data = null;
            byte[] track2Data = null;
            String track1DataString = null;
            String track2DataString = null;
            byte[] key = null;
            byte[] dek = null;
            byte[] macKey = null;
            byte[] dek3des = null;

            mKeySerialNumberString = toHexString(trackData.getKeySerialNumber());
            mTrack1MacString = toHexString(trackData.getTrack1Mac());
            mTrack2MacString = toHexString(trackData.getTrack2Mac());
            mTrack1MaskedData.fromString(trackData.getTrack1MaskedData());
            mTrack2MaskedData.fromString(trackData.getTrack2MaskedData());

            /* Compare the key serial number. */
            if (!DukptReceiver.compareKeySerialNumber(mIksn,
                    trackData.getKeySerialNumber())) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showMessageDialog(R.string.error,
                                R.string.message_track_data_error_ksn);
                    }
                });

                /* Show the track data. */
                showTrackData();
                return;
            }

            /* Get the encryption counter from KSN. */
            ec = DukptReceiver.getEncryptionCounter(trackData
                    .getKeySerialNumber());

            /* Get the encryption counter from DUKPT receiver. */
            ec2 = mDukptReceiver.getEncryptionCounter();

            /*
             * Load the initial key if the encryption counter from KSN is less
             * than the encryption counter from DUKPT receiver.
             */
            if (ec < ec2) {

                mDukptReceiver.loadInitialKey(mIpek);
                ec2 = mDukptReceiver.getEncryptionCounter();
            }

            /*
             * Synchronize the key if the encryption counter from KSN is greater
             * than the encryption counter from DUKPT receiver.
             */
            while (ec > ec2) {

                mDukptReceiver.getKey();
                ec2 = mDukptReceiver.getEncryptionCounter();
            }

            if (ec != ec2) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showMessageDialog(R.string.error,
                                R.string.message_track_data_error_ec);
                    }
                });

                /* Show the track data. */
                showTrackData();
                return;
            }

            key = mDukptReceiver.getKey();
            if (key == null) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        Toast.makeText(
                                mContext,
                                "The maximum encryption count had been reached.",
                                Toast.LENGTH_LONG).show();
                    }
                });

                /* Show the track data. */
                showTrackData();
                return;
            }

            dek = DukptReceiver.generateDataEncryptionRequestKey(key);
            macKey = DukptReceiver.generateMacRequestKey(key);
            dek3des = new byte[24];

            /* Generate 3DES key (K1 = K3) */
            System.arraycopy(dek, 0, dek3des, 0, dek.length);
            System.arraycopy(dek, 0, dek3des, 16, 8);

            try {

                if (trackData.getTrack1Data() != null) {

                    /* Decrypt the track 1 data. */
                    track1Data = tripleDesDecrypt(dek3des,
                            trackData.getTrack1Data());

                    /* Generate the MAC for track 1 data. */
                    mTrack1MacString += " ("
                            + toHexString(DukptReceiver.generateMac(macKey,
                                    track1Data)) + ")";

                    /* Get the track 1 data as string. */
                    track1DataString = new String(track1Data, 1,
                            trackData.getTrack1Length(), "US-ASCII");

                    /* Divide the track 1 data into fields. */
                    mTrack1Data.fromString(track1DataString);
                }

                if (trackData.getTrack2Data() != null) {

                    /* Decrypt the track 2 data. */
                    track2Data = tripleDesDecrypt(dek3des,
                            trackData.getTrack2Data());

                    /* Generate the MAC for track 2 data. */
                    mTrack2MacString += " ("
                            + toHexString(DukptReceiver.generateMac(macKey,
                                    track2Data)) + ")";

                    /* Get the track 2 data as string. */
                    track2DataString = new String(track2Data, 1,
                            trackData.getTrack2Length(), "US-ASCII");

                    /* Divide the track 2 data into fields. */
                    mTrack2Data.fromString(track2DataString);
                }

            } catch (GeneralSecurityException e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showMessageDialog(R.string.error,
                                R.string.message_track_data_error_decrypted);
                    }
                });

            } catch (UnsupportedEncodingException e) {
            }

            /* Show the track data. */
            showTrackData();
        }

        /**
         * Shows the track data.
         */
        private void showTrackData() {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    /* Increment the swipe count. */
                    mSwipeCount++;

                    mTrackDataSwipeCountPreference.setSummary(Integer
                            .toString(mSwipeCount));
                    mTrackDataBatteryStatusPreference
                            .setSummary(mBatteryStatusString);
                    mTrackDataKeySerialNumberPreference
                            .setSummary(mKeySerialNumberString);
                    mTrackDataTrack1MacPreference.setSummary(mTrack1MacString);
                    mTrackDataTrack2MacPreference.setSummary(mTrack2MacString);

                    mTrack1Jis2DataPreference.setSummary(mTrack1Data
                            .getJis2Data());
                    mTrack1PrimaryAccountNumberPreference
                            .setSummary(concatString(
                                    mTrack1Data.getPrimaryAccountNumber(),
                                    mTrack1MaskedData.getPrimaryAccountNumber()));
                    mTrack1NamePreference
                            .setSummary(concatString(mTrack1Data.getName(),
                                    mTrack1MaskedData.getName()));
                    mTrack1ExpirationDatePreference.setSummary(concatString(
                            mTrack1Data.getExpirationDate(),
                            mTrack1MaskedData.getExpirationDate()));
                    mTrack1ServiceCodePreference.setSummary(concatString(
                            mTrack1Data.getServiceCode(),
                            mTrack1MaskedData.getServiceCode()));
                    mTrack1DiscretionaryDataPreference.setSummary(concatString(
                            mTrack1Data.getDiscretionaryData(),
                            mTrack1MaskedData.getDiscretionaryData()));

                    mTrack2PrimaryAccountNumberPreference
                            .setSummary(concatString(
                                    mTrack2Data.getPrimaryAccountNumber(),
                                    mTrack2MaskedData.getPrimaryAccountNumber()));
                    mTrack2ExpirationDatePreference.setSummary(concatString(
                            mTrack2Data.getExpirationDate(),
                            mTrack2MaskedData.getExpirationDate()));
                    mTrack2ServiceCodePreference.setSummary(concatString(
                            mTrack2Data.getServiceCode(),
                            mTrack2MaskedData.getServiceCode()));
                    mTrack2DiscretionaryDataPreference.setSummary(concatString(
                            mTrack2Data.getDiscretionaryData(),
                            mTrack2MaskedData.getDiscretionaryData()));
                }
            });
        }

        /**
         * Concatenates two strings with carriage return.
         * 
         * @param string1
         *            the first string.
         * @param string2
         *            the second string.
         * @return the combined string.
         */
        private String concatString(String string1, String string2) {

            String ret = string1;

            if ((string1.length() > 0) && (string2.length() > 0)) {
                ret += "\n";
            }

            ret += string2;

            return ret;
        }
    }

    private class OnRawDataAvailableListener implements
            AudioJackReader.OnRawDataAvailableListener {

        private String mHexString;

        @Override
        public void onRawDataAvailable(AudioJackReader reader, byte[] rawData) {

            mHexString = toHexString(rawData)
                    + (reader.verifyData(rawData) ? " (Checksum OK)"
                            : " (Checksum Error)");

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mDeviceDataReceivedPreference.setSummary(mHexString);
                }
            });
        }
    }

    private class OnCustomIdAvailableListener implements
            AudioJackReader.OnCustomIdAvailableListener {

        @Override
        public void onCustomIdAvailable(AudioJackReader reader, byte[] customId) {

            synchronized (mResponseEvent) {

                /* Store the custom ID. */
                mCustomId = new byte[customId.length];
                System.arraycopy(customId, 0, mCustomId, 0, customId.length);

                /* Trigger the response event. */
                mCustomIdReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }

    private class OnDeviceIdAvailableListener implements
            AudioJackReader.OnDeviceIdAvailableListener {

        @Override
        public void onDeviceIdAvailable(AudioJackReader reader, byte[] deviceId) {

            synchronized (mResponseEvent) {

                /* Store the custom ID. */
                mDeviceId = new byte[deviceId.length];
                System.arraycopy(deviceId, 0, mDeviceId, 0, deviceId.length);

                /* Trigger the response event. */
                mDeviceIdReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }

    private class OnDukptOptionAvailableListener implements
            AudioJackReader.OnDukptOptionAvailableListener {

        @Override
        public void onDukptOptionAvailable(AudioJackReader reader,
                boolean enabled) {

            synchronized (mResponseEvent) {

                /* Store the DUKPT option. */
                mDukptOption = enabled;

                /* Trigger the response event. */
                mDukptOptionReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }

    private class OnTrackDataOptionAvailableListener implements
            AudioJackReader.OnTrackDataOptionAvailableListener {

        @Override
        public void onTrackDataOptionAvailable(AudioJackReader reader,
                int option) {

            synchronized (mResponseEvent) {

                /* Store the track data option. */
                mTrackDataOption = option;

                /* Trigger the response event. */
                mTrackDataOptionReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }

    private class OnPiccAtrAvailableListener implements
            AudioJackReader.OnPiccAtrAvailableListener {

        @Override
        public void onPiccAtrAvailable(AudioJackReader reader, byte[] atr) {

            synchronized (mResponseEvent) {

                /* Store the PICC ATR. */
                mPiccAtr = new byte[atr.length];
                System.arraycopy(atr, 0, mPiccAtr, 0, atr.length);

                /* Trigger the response event. */
                mPiccAtrReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }

    private class OnPiccResponseApduAvailableListener implements
            AudioJackReader.OnPiccResponseApduAvailableListener {

        @Override
        public void onPiccResponseApduAvailable(AudioJackReader reader,
                byte[] responseApdu) {

            synchronized (mResponseEvent) {

                /* Store the PICC response APDU. */
                mPiccResponseApdu = new byte[responseApdu.length];
                System.arraycopy(responseApdu, 0, mPiccResponseApdu, 0,
                        responseApdu.length);

                /* Trigger the response event. */
                mPiccResponseApduReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }

    private Preference mAboutReaderFirmwareVersionPreference;
    private Preference mAboutReaderGetFirmwareVersionPreference;
    private Preference mAboutReaderBatteryLevelPreference;
    private Preference mAboutReaderSleepTimeoutPreference;
    private Preference mAboutReaderGetStatusPreference;
    private Preference mAboutReaderSetSleepTimeoutPreference;

    private Preference mReaderIdCustomIdPreference;
    private Preference mReaderIdGetCustomIdPreference;
    private Preference mReaderIdSetCustomIdPreference;
    private Preference mReaderIdDeviceIdPreference;
    private Preference mReaderIdGetDeviceIdPreference;

    private EditTextPreference mCryptographicKeysNewMasterKeyPreference;
    private EditTextPreference mCryptographicKeysMasterKeyPreference;
    private Preference mCryptographicKeysSetMasterKeyPreference;
    private EditTextPreference mCryptographicKeysAesKeyPreference;
    private Preference mCryptographicKeysSetAesKeyPreference;
    private Preference mCryptographicKeysUseDefaultKeyPreference;

    private CheckBoxPreference mDukptSetupDukptPreference;
    private Preference mDukptSetupGetDukptOptionPreference;
    private Preference mDukptSetupSetDukptOptionPreference;
    private EditTextPreference mDukptSetupIksnPreference;
    private EditTextPreference mDukptSetupIpekPreference;
    private Preference mDukptSetupInitializeDukptPreference;
    private Preference mDukptSetupUseDefaultIksnIpekPreference;

    private CheckBoxPreference mTrackDataSetupEncryptedTrack1Preference;
    private CheckBoxPreference mTrackDataSetupEncryptedTrack2Preference;
    private CheckBoxPreference mTrackDataSetupMaskedTrack1Preference;
    private CheckBoxPreference mTrackDataSetupMaskedTrack2Preference;
    private Preference mTrackDataSetupResetPreference;
    private Preference mTrackDataSetupGetTrackDataOptionPreference;
    private Preference mTrackDataSetupSetTrackDataOptionPreference;

    private Preference mIccResetPreference;
    private ListPreference mIccPowerActionPreference;
    private EditTextPreference mIccWaitTimeoutPreference;
    private Preference mIccAtrPreference;
    private Preference mIccPowerPreference;
    private CheckBoxPreference mIccT0Preference;
    private CheckBoxPreference mIccT1Preference;
    private Preference mIccActiveProtocolPreference;
    private Preference mIccSetProtocolPreference;
    private EditTextPreference mIccCommandApduPreference;
    private Preference mIccResponseApduPreference;
    private Preference mIccTransmitPreference;
    private EditTextPreference mIccControlCodePreference;
    private EditTextPreference mIccControlCommandPreference;
    private Preference mIccControlResponsePreference;
    private Preference mIccControlPreference;

    private Preference mPiccAtrPreference;
    private EditTextPreference mPiccTimeoutPreference;
    private EditTextPreference mPiccCardTypePreference;
    private Preference mPiccResetPreference;
    private Preference mPiccPowerOnPreference;
    private Preference mPiccPowerOffPreference;
    private EditTextPreference mPiccCommandApduPreference;
    private Preference mPiccResponseApduPreference;
    private Preference mPiccTransmitPreference;
    private EditTextPreference mPiccRfConfigPreference;
    private Preference mPiccSetRfConfigPreference;

    private Preference mDeviceResetPreference;
    private Preference mDeviceSleepPreference;
    private Preference mDeviceDataReceivedPreference;

    private Preference mTrackDataSwipeCountPreference;
    private Preference mTrackDataBatteryStatusPreference;
    private Preference mTrackDataKeySerialNumberPreference;
    private Preference mTrackDataTrack1MacPreference;
    private Preference mTrackDataTrack2MacPreference;

    private Preference mTrack1Jis2DataPreference;
    private Preference mTrack1PrimaryAccountNumberPreference;
    private Preference mTrack1NamePreference;
    private Preference mTrack1ExpirationDatePreference;
    private Preference mTrack1ServiceCodePreference;
    private Preference mTrack1DiscretionaryDataPreference;

    private Preference mTrack2PrimaryAccountNumberPreference;
    private Preference mTrack2ExpirationDatePreference;
    private Preference mTrack2ServiceCodePreference;
    private Preference mTrack2DiscretionaryDataPreference;

    private AudioManager mAudioManager;
    private AudioJackReader mReader;
    private DukptReceiver mDukptReceiver = new DukptReceiver();
    private Context mContext = this;
    private int mSwipeCount;

    private ProgressDialog mProgress;
    private Object mResponseEvent = new Object();

    private boolean mFirmwareVersionReady;
    private String mFirmwareVersion;

    private boolean mResultReady;
    private Result mResult;

    private boolean mStatusReady;
    private Status mStatus;

    private boolean mCustomIdReady;
    private byte[] mCustomId;

    private boolean mDeviceIdReady;
    private byte[] mDeviceId;

    private boolean mDukptOptionReady;
    private boolean mDukptOption;

    private boolean mTrackDataOptionReady;
    private int mTrackDataOption;

    private boolean mPiccAtrReady;
    private byte[] mPiccAtr;

    private boolean mPiccResponseApduReady;
    private byte[] mPiccResponseApdu;

    private byte[] mMasterKey = new byte[16];
    private byte[] mNewMasterKey = new byte[16];
    private byte[] mAesKey = new byte[16];
    private byte[] mIksn = new byte[10];
    private byte[] mIpek = new byte[16];
    private int mPiccTimeout;
    private int mPiccCardType;
    private byte[] mPiccCommandApdu;
    private byte[] mPiccRfConfig = new byte[19];
    private int mIccPowerAction;
    private int mIccWaitTimeout;
    private byte[] mIccCommandApdu;
    private int mIccControlCode;
    private byte[] mIccControlCommand;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        addPreferencesFromResource(R.xml.preferences);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mReader = new AudioJackReader(mAudioManager);

        mAboutReaderFirmwareVersionPreference = findPreference("about_reader_firmware_version");
        mAboutReaderGetFirmwareVersionPreference = findPreference("about_reader_get_firmware_version");
        mAboutReaderBatteryLevelPreference = findPreference("about_reader_battery_level");
        mAboutReaderSleepTimeoutPreference = findPreference("about_reader_sleep_timeout");
        mAboutReaderGetStatusPreference = findPreference("about_reader_get_status");
        mAboutReaderSetSleepTimeoutPreference = findPreference("about_reader_set_sleep_timeout");

        mReaderIdCustomIdPreference = findPreference("reader_id_custom_id");
        mReaderIdGetCustomIdPreference = findPreference("reader_id_get_custom_id");
        mReaderIdSetCustomIdPreference = findPreference("reader_id_set_custom_id");
        mReaderIdDeviceIdPreference = findPreference("reader_id_device_id");
        mReaderIdGetDeviceIdPreference = findPreference("reader_id_get_device_id");

        mCryptographicKeysNewMasterKeyPreference = (EditTextPreference) findPreference("cryptographic_keys_new_master_key");
        mCryptographicKeysMasterKeyPreference = (EditTextPreference) findPreference("cryptographic_keys_master_key");
        mCryptographicKeysSetMasterKeyPreference = findPreference("cryptographic_keys_set_master_key");
        mCryptographicKeysAesKeyPreference = (EditTextPreference) findPreference("cryptographic_keys_aes_key");
        mCryptographicKeysSetAesKeyPreference = findPreference("cryptographic_keys_set_aes_key");
        mCryptographicKeysUseDefaultKeyPreference = findPreference("cryptographic_keys_use_default_key");

        mDukptSetupDukptPreference = (CheckBoxPreference) findPreference("dukpt_setup_dukpt");
        mDukptSetupGetDukptOptionPreference = findPreference("dukpt_setup_get_dukpt_option");
        mDukptSetupSetDukptOptionPreference = findPreference("dukpt_setup_set_dukpt_option");
        mDukptSetupIksnPreference = (EditTextPreference) findPreference("dukpt_setup_iksn");
        mDukptSetupIpekPreference = (EditTextPreference) findPreference("dukpt_setup_ipek");
        mDukptSetupInitializeDukptPreference = findPreference("dukpt_setup_initialize_dukpt");
        mDukptSetupUseDefaultIksnIpekPreference = findPreference("dukpt_setup_use_default_iksn_ipek");

        mTrackDataSetupEncryptedTrack1Preference = (CheckBoxPreference) findPreference("track_data_setup_encrypted_track1");
        mTrackDataSetupEncryptedTrack2Preference = (CheckBoxPreference) findPreference("track_data_setup_encrypted_track2");
        mTrackDataSetupMaskedTrack1Preference = (CheckBoxPreference) findPreference("track_data_setup_masked_track1");
        mTrackDataSetupMaskedTrack2Preference = (CheckBoxPreference) findPreference("track_data_setup_masked_track2");
        mTrackDataSetupResetPreference = findPreference("track_data_setup_reset");
        mTrackDataSetupGetTrackDataOptionPreference = findPreference("track_data_setup_get_track_data_option");
        mTrackDataSetupSetTrackDataOptionPreference = findPreference("track_data_setup_set_track_data_option");

        mIccResetPreference = findPreference("icc_reset");
        mIccPowerActionPreference = (ListPreference) findPreference("icc_power_action");
        mIccWaitTimeoutPreference = (EditTextPreference) findPreference("icc_wait_timeout");
        mIccAtrPreference = findPreference("icc_atr");
        mIccPowerPreference = findPreference("icc_power");
        mIccT0Preference = (CheckBoxPreference) findPreference("icc_t0");
        mIccT1Preference = (CheckBoxPreference) findPreference("icc_t1");
        mIccActiveProtocolPreference = findPreference("icc_active_protocol");
        mIccSetProtocolPreference = findPreference("icc_set_protocol");
        mIccCommandApduPreference = (EditTextPreference) findPreference("icc_command_apdu");
        mIccResponseApduPreference = findPreference("icc_response_apdu");
        mIccTransmitPreference = findPreference("icc_transmit");
        mIccControlCodePreference = (EditTextPreference) findPreference("icc_control_code");
        mIccControlCommandPreference = (EditTextPreference) findPreference("icc_control_command");
        mIccControlResponsePreference = findPreference("icc_control_response");
        mIccControlPreference = findPreference("icc_control");

        mPiccAtrPreference = findPreference("picc_atr");
        mPiccTimeoutPreference = (EditTextPreference) findPreference("picc_timeout");
        mPiccCardTypePreference = (EditTextPreference) findPreference("picc_card_type");
        mPiccResetPreference = findPreference("picc_reset");
        mPiccPowerOnPreference = findPreference("picc_power_on");
        mPiccPowerOffPreference = findPreference("picc_power_off");
        mPiccCommandApduPreference = (EditTextPreference) findPreference("picc_command_apdu");
        mPiccResponseApduPreference = findPreference("picc_response_apdu");
        mPiccTransmitPreference = findPreference("picc_transmit");
        mPiccRfConfigPreference = (EditTextPreference) findPreference("picc_rf_config");
        mPiccSetRfConfigPreference = findPreference("picc_set_rf_config");

        mDeviceResetPreference = findPreference("device_reset");
        mDeviceSleepPreference = findPreference("device_sleep");
        mDeviceDataReceivedPreference = findPreference("device_data_received");

        mTrackDataSwipeCountPreference = findPreference("track_data_swipe_count");
        mTrackDataBatteryStatusPreference = findPreference("track_data_battery_status");
        mTrackDataKeySerialNumberPreference = findPreference("track_data_key_serial_number");
        mTrackDataTrack1MacPreference = findPreference("track_data_track1_mac");
        mTrackDataTrack2MacPreference = findPreference("track_data_track2_mac");

        mTrack1Jis2DataPreference = findPreference("track1_jis2_data");
        mTrack1PrimaryAccountNumberPreference = findPreference("track1_primary_account_number");
        mTrack1NamePreference = findPreference("track1_name");
        mTrack1ExpirationDatePreference = findPreference("track1_expiration_date");
        mTrack1ServiceCodePreference = findPreference("track1_service_code");
        mTrack1DiscretionaryDataPreference = findPreference("track1_discretionary_data");

        mTrack2PrimaryAccountNumberPreference = findPreference("track2_primary_account_number");
        mTrack2ExpirationDatePreference = findPreference("track2_expiration_date");
        mTrack2ServiceCodePreference = findPreference("track2_service_code");
        mTrack2DiscretionaryDataPreference = findPreference("track2_discretionary_data");

        /* Load the new master key. */
        String newMasterKeyString = mCryptographicKeysNewMasterKeyPreference
                .getText();
        if ((newMasterKeyString == null) || newMasterKeyString.equals("")
                || (toByteArray(newMasterKeyString, mNewMasterKey) != 16)) {

            newMasterKeyString = DEFAULT_MASTER_KEY_STRING;
            toByteArray(newMasterKeyString, mNewMasterKey);
        }
        newMasterKeyString = toHexString(mNewMasterKey);
        mCryptographicKeysNewMasterKeyPreference.setText(newMasterKeyString);
        mCryptographicKeysNewMasterKeyPreference.setSummary(newMasterKeyString);

        /* Load the master key. */
        String masterKeyString = mCryptographicKeysMasterKeyPreference
                .getText();
        if ((masterKeyString == null) || masterKeyString.equals("")
                || (toByteArray(masterKeyString, mMasterKey) != 16)) {

            masterKeyString = DEFAULT_MASTER_KEY_STRING;
            toByteArray(masterKeyString, mMasterKey);
        }
        masterKeyString = toHexString(mMasterKey);
        mCryptographicKeysMasterKeyPreference.setText(masterKeyString);
        mCryptographicKeysMasterKeyPreference.setSummary(masterKeyString);

        /* Load the AES key. */
        String aesKeyString = mCryptographicKeysAesKeyPreference.getText();
        if ((aesKeyString == null) || aesKeyString.equals("")
                || (toByteArray(aesKeyString, mAesKey) != 16)) {

            aesKeyString = DEFAULT_AES_KEY_STRING;
            toByteArray(aesKeyString, mAesKey);
        }
        aesKeyString = toHexString(mAesKey);
        mCryptographicKeysAesKeyPreference.setText(aesKeyString);
        mCryptographicKeysAesKeyPreference.setSummary(aesKeyString);

        /* Load the IKSN. */
        String iksnString = mDukptSetupIksnPreference.getText();
        if ((iksnString == null) || iksnString.equals("")
                || (toByteArray(iksnString, mIksn) != 10)) {

            iksnString = DEFAULT_IKSN_STRING;
            toByteArray(iksnString, mIksn);
        }
        iksnString = toHexString(mIksn);
        mDukptSetupIksnPreference.setText(iksnString);
        mDukptSetupIksnPreference.setSummary(iksnString);

        /* Load the IPEK. */
        String ipekString = mDukptSetupIpekPreference.getText();
        if ((ipekString == null) || ipekString.equals("")
                || (toByteArray(ipekString, mIpek) != 16)) {

            ipekString = DEFAULT_IPEK_STRING;
            toByteArray(ipekString, mIpek);
        }
        ipekString = toHexString(mIpek);
        mDukptSetupIpekPreference.setText(ipekString);
        mDukptSetupIpekPreference.setSummary(ipekString);

        /* Load the ICC power action. */
        String iccPowerActionString = mIccPowerActionPreference.getValue();
        if (iccPowerActionString == null) {

            iccPowerActionString = (String) mIccPowerActionPreference
                    .getEntryValues()[2];
            mIccPowerActionPreference.setValueIndex(2);
        }
        mIccPowerAction = mIccPowerActionPreference
                .findIndexOfValue(iccPowerActionString);
        mIccPowerActionPreference.setSummary(iccPowerActionString);

        /* Load the ICC wait timeout. */
        String iccWaitTimeoutString = mIccWaitTimeoutPreference.getText();
        if ((iccWaitTimeoutString == null) || iccWaitTimeoutString.equals("")) {
            iccWaitTimeoutString = "10000";
        }
        try {
            mIccWaitTimeout = Integer.parseInt(iccWaitTimeoutString);
        } catch (NumberFormatException e) {
            mIccWaitTimeout = 10000;
        }
        iccWaitTimeoutString = Integer.toString(mIccWaitTimeout);
        mIccWaitTimeoutPreference.setText(iccWaitTimeoutString);
        mIccWaitTimeoutPreference.setSummary(iccWaitTimeoutString + " ms");

        /* Load the ICC command APDU. */
        String iccCommandApduString = mIccCommandApduPreference.getText();
        if ((iccCommandApduString == null) || (iccCommandApduString.equals(""))) {
            iccCommandApduString = "00 84 00 00 08";
        }
        mIccCommandApdu = toByteArray(iccCommandApduString);
        iccCommandApduString = toHexString(mIccCommandApdu);
        mIccCommandApduPreference.setText(iccCommandApduString);
        mIccCommandApduPreference.setSummary(iccCommandApduString);

        /* Load the ICC control code. */
        String iccControlCodeString = mIccControlCodePreference.getText();
        if ((iccControlCodeString == null) || iccControlCodeString.equals("")) {
            iccControlCodeString = Integer
                    .toString(AudioJackReader.IOCTL_CCID_ESCAPE);
        }
        try {
            mIccControlCode = Integer.parseInt(iccControlCodeString);
        } catch (NumberFormatException e) {
            mIccControlCode = AudioJackReader.IOCTL_CCID_ESCAPE;
        }
        iccControlCodeString = Integer.toString(mIccControlCode);
        mIccControlCodePreference.setText(iccControlCodeString);
        mIccControlCodePreference.setSummary(iccControlCodeString);

        /* Load the ICC control command. */
        String iccControlCommandString = mIccControlCommandPreference.getText();
        if ((iccControlCommandString == null)
                || (iccControlCommandString.equals(""))) {
            iccControlCommandString = "E0 00 00 18 00";
        }
        mIccControlCommand = toByteArray(iccControlCommandString);
        iccControlCommandString = toHexString(mIccControlCommand);
        mIccControlCommandPreference.setText(iccControlCommandString);
        mIccControlCommandPreference.setSummary(iccControlCommandString);

        /* Load the PICC timeout. */
        String piccTimeoutString = mPiccTimeoutPreference.getText();
        if ((piccTimeoutString == null) || piccTimeoutString.equals("")) {
            piccTimeoutString = "1";
        }
        try {
            mPiccTimeout = Integer.parseInt(piccTimeoutString);
        } catch (NumberFormatException e) {
            mPiccTimeout = 1;
        }
        piccTimeoutString = Integer.toString(mPiccTimeout);
        mPiccTimeoutPreference.setText(piccTimeoutString);
        mPiccTimeoutPreference.setSummary(piccTimeoutString + " secs");

        /* Load the PICC card type. */
        String piccCardTypeString = mPiccCardTypePreference.getText();
        if ((piccCardTypeString == null) || piccCardTypeString.equals("")) {
            piccCardTypeString = "8F";
        }
        byte[] cardType = new byte[1];
        toByteArray(piccCardTypeString, cardType);
        mPiccCardType = cardType[0] & 0xFF;
        piccCardTypeString = toHexString(mPiccCardType);
        mPiccCardTypePreference.setText(piccCardTypeString);
        mPiccCardTypePreference.setSummary(piccCardTypeString);

        /* Load the PICC command APDU. */
        String piccCommandApduString = mPiccCommandApduPreference.getText();
        if ((piccCommandApduString == null)
                || (piccCommandApduString.equals(""))) {
            piccCommandApduString = "00 84 00 00 08";
        }
        mPiccCommandApdu = toByteArray(piccCommandApduString);
        piccCommandApduString = toHexString(mPiccCommandApdu);
        mPiccCommandApduPreference.setText(piccCommandApduString);
        mPiccCommandApduPreference.setSummary(piccCommandApduString);

        /* Load the PICC RF configuration. */
        String piccRfConfigString = mPiccRfConfigPreference.getText();
        if ((piccRfConfigString == null) || piccRfConfigString.equals("")
                || (toByteArray(piccRfConfigString, mPiccRfConfig) != 19)) {

            piccRfConfigString = "07 85 85 85 85 85 85 85 85 69 69 69 69 69 69 69 69 3F 3F";
            toByteArray(piccRfConfigString, mPiccRfConfig);
        }
        piccRfConfigString = toHexString(mPiccRfConfig);
        mPiccRfConfigPreference.setText(piccRfConfigString);
        mPiccRfConfigPreference.setSummary(piccRfConfigString);

        if (savedInstanceState != null) {

            /* Restore the contents. */
            mSwipeCount = savedInstanceState.getInt("mSwipeCount");

            mAboutReaderFirmwareVersionPreference.setSummary(savedInstanceState
                    .getCharSequence("about_reader_firmware_version"));
            mAboutReaderBatteryLevelPreference.setSummary(savedInstanceState
                    .getCharSequence("about_reader_battery_level"));
            mAboutReaderSleepTimeoutPreference.setSummary(savedInstanceState
                    .getCharSequence("about_reader_sleep_timeout"));

            mReaderIdCustomIdPreference.setSummary(savedInstanceState
                    .getCharSequence("reader_id_custom_id"));
            mReaderIdDeviceIdPreference.setSummary(savedInstanceState
                    .getCharSequence("reader_id_device_id"));

            mIccAtrPreference.setSummary(savedInstanceState
                    .getCharSequence("icc_atr"));
            mIccActiveProtocolPreference.setSummary(savedInstanceState
                    .getCharSequence("icc_active_protocol"));
            mIccResponseApduPreference.setSummary(savedInstanceState
                    .getCharSequence("icc_response_apdu"));
            mIccControlResponsePreference.setSummary(savedInstanceState
                    .getCharSequence("icc_control_response"));

            mPiccAtrPreference.setSummary(savedInstanceState
                    .getCharSequence("picc_atr"));
            mPiccResponseApduPreference.setSummary(savedInstanceState
                    .getCharSequence("picc_response_apdu"));

            mDeviceDataReceivedPreference.setSummary(savedInstanceState
                    .getCharSequence("device_data_received"));

            mTrackDataSwipeCountPreference.setSummary(savedInstanceState
                    .getCharSequence("track_data_swipe_count"));
            mTrackDataBatteryStatusPreference.setSummary(savedInstanceState
                    .getCharSequence("track_data_battery_status"));
            mTrackDataKeySerialNumberPreference.setSummary(savedInstanceState
                    .getCharSequence("track_data_key_serial_number"));
            mTrackDataTrack1MacPreference.setSummary(savedInstanceState
                    .getCharSequence("track_data_track1_mac"));
            mTrackDataTrack2MacPreference.setSummary(savedInstanceState
                    .getCharSequence("track_data_track2_mac"));

            mTrack1Jis2DataPreference.setSummary(savedInstanceState
                    .getCharSequence("track1_jis2_data"));
            mTrack1PrimaryAccountNumberPreference.setSummary(savedInstanceState
                    .getCharSequence("track1_primary_account_number"));
            mTrack1NamePreference.setSummary(savedInstanceState
                    .getCharSequence("track1_name"));
            mTrack1ExpirationDatePreference.setSummary(savedInstanceState
                    .getCharSequence("track1_expiration_date"));
            mTrack1ServiceCodePreference.setSummary(savedInstanceState
                    .getCharSequence("track1_service_code"));
            mTrack1DiscretionaryDataPreference.setSummary(savedInstanceState
                    .getCharSequence("track1_discretionary_data"));

            mTrack2PrimaryAccountNumberPreference.setSummary(savedInstanceState
                    .getCharSequence("track2_primary_account_number"));
            mTrack2ExpirationDatePreference.setSummary(savedInstanceState
                    .getCharSequence("track2_expiration_date"));
            mTrack2ServiceCodePreference.setSummary(savedInstanceState
                    .getCharSequence("track2_service_code"));
            mTrack2DiscretionaryDataPreference.setSummary(savedInstanceState
                    .getCharSequence("track2_discretionary_data"));
        }

        /* Initialize the progress dialog */
        mProgress = new ProgressDialog(mContext);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        /* Set the "Get firmware version" preference click callback. */
        mAboutReaderGetFirmwareVersionPreference
                .setOnPreferenceClickListener(new OnGetFirmwareVersionPreferenceClickListener());

        /* Set the "Get status" preference click callback. */
        mAboutReaderGetStatusPreference
                .setOnPreferenceClickListener(new OnGetStatusPreferenceClickListener());

        /* Set the "Set sleep timeout" preference click callback. */
        mAboutReaderSetSleepTimeoutPreference
                .setOnPreferenceClickListener(new OnSetSleepTimeoutPreferenceClickListener());

        /* Set the "Get custom ID" preference click callback. */
        mReaderIdGetCustomIdPreference
                .setOnPreferenceClickListener(new OnGetCustomIdPreferenceClickListener());

        /* Set the "Set custom ID" preference click callback. */
        mReaderIdSetCustomIdPreference
                .setOnPreferenceClickListener(new OnSetCustomIdPreferenceClickListener());

        /* Set the "Get device ID" preference click callback. */
        mReaderIdGetDeviceIdPreference
                .setOnPreferenceClickListener(new OnGetDeviceIdPreferenceClickListener());

        /* Set the new master key preference change callback. */
        mCryptographicKeysNewMasterKeyPreference
                .setOnPreferenceChangeListener(new OnNewMasterKeyPreferenceChangeListener());

        /* Set the master key preference change callback. */
        mCryptographicKeysMasterKeyPreference
                .setOnPreferenceChangeListener(new OnMasterKeyPreferenceChangeListener());

        /* Set the "Set master key" preference click callback. */
        mCryptographicKeysSetMasterKeyPreference
                .setOnPreferenceClickListener(new OnSetMasterKeyPreferenceClickListener());

        /* Set the AES key preference change callback. */
        mCryptographicKeysAesKeyPreference
                .setOnPreferenceChangeListener(new OnAesKeyPreferenceChangeListener());

        /* Set the "Set AES key" preference click callback. */
        mCryptographicKeysSetAesKeyPreference
                .setOnPreferenceClickListener(new OnSetAesKeyPreferenceClickListener());

        /* Set the "Use default key" preference click callback. */
        mCryptographicKeysUseDefaultKeyPreference
                .setOnPreferenceClickListener(new OnUseDefaultKeyPreferenceClickListener());

        /* Set the "Get DUKPT option" preference click callback. */
        mDukptSetupGetDukptOptionPreference
                .setOnPreferenceClickListener(new OnGetDukptOptionPreferenceClickListener());

        /* Set the "Set DUKPT option" preference click callback. */
        mDukptSetupSetDukptOptionPreference
                .setOnPreferenceClickListener(new OnSetDukptOptionPreferenceClickListener());

        /* Set the IKSN preference change callback. */
        mDukptSetupIksnPreference
                .setOnPreferenceChangeListener(new OnIksnPreferenceChangeListener());

        /* Set the IPEK preference change callback. */
        mDukptSetupIpekPreference
                .setOnPreferenceChangeListener(new OnIpekPreferenceChangeListener());

        /* Set the "Initialize DUKPT" preference click callback. */
        mDukptSetupInitializeDukptPreference
                .setOnPreferenceClickListener(new OnInitializeDukptPreferenceClickListener());

        /* Set the "Use default IKSN & IPEK" preference click callback. */
        mDukptSetupUseDefaultIksnIpekPreference
                .setOnPreferenceClickListener(new OnUseDefaultIksnIpekPreferenceClickListener());

        /* Set the Track data setup "Reset" preference click callback. */
        mTrackDataSetupResetPreference
                .setOnPreferenceClickListener(new OnResetPreferenceClickListener());

        /* Set the "Get track data option" preference click callback. */
        mTrackDataSetupGetTrackDataOptionPreference
                .setOnPreferenceClickListener(new OnGetTrackDataOptionPreferenceClickListener());

        /* Set the "Set track data option" preference click callback. */
        mTrackDataSetupSetTrackDataOptionPreference
                .setOnPreferenceClickListener(new OnSetTrackDataOptionPreferenceClickListener());

        /* Set the ICC "Reset" preference click callback. */
        mIccResetPreference
                .setOnPreferenceClickListener(new OnResetPreferenceClickListener());

        /* Set the ICC "Power action" preference change callback. */
        mIccPowerActionPreference
                .setOnPreferenceChangeListener(new OnIccPowerActionPreferenceChangeListener());

        /* Set the ICC "Wait timeout" preference change callback. */
        mIccWaitTimeoutPreference
                .setOnPreferenceChangeListener(new OnIccWaitTimeoutPreferenceChangeListener());

        /* Set the ICC "Power" preference click callback. */
        mIccPowerPreference
                .setOnPreferenceClickListener(new OnIccPowerPreferenceClickListener());

        /* Set the ICC "Set protocol" preference click callback. */
        mIccSetProtocolPreference
                .setOnPreferenceClickListener(new OnIccSetProtocolPreferenceClickListener());

        /* Set the ICC command APDU preference change callback. */
        mIccCommandApduPreference
                .setOnPreferenceChangeListener(new OnIccCommandApduPreferenceChangeListener());

        /* Set the ICC "Transmit" preference click callback. */
        mIccTransmitPreference
                .setOnPreferenceClickListener(new OnIccTransmitPreferenceClickListener());

        /* Set the ICC "Control code" preference change callback. */
        mIccControlCodePreference
                .setOnPreferenceChangeListener(new OnIccControlCodePreferenceChangeListener());

        /* Set the ICC "Control command" preference change callback. */
        mIccControlCommandPreference
                .setOnPreferenceChangeListener(new OnIccControlCommandPreferenceChangeListener());

        /* Set the ICC "Control" preference click callback. */
        mIccControlPreference
                .setOnPreferenceClickListener(new OnIccControlPreferenceClickListener());

        /* Set the PICC timeout preference change callback. */
        mPiccTimeoutPreference
                .setOnPreferenceChangeListener(new OnPiccTimeoutPreferenceChangeListener());

        /* Set the PICC card type preference change callback. */
        mPiccCardTypePreference
                .setOnPreferenceChangeListener(new OnPiccCardTypePreferenceChangeListener());

        /* Set the PICC "Reset" preference click callback. */
        mPiccResetPreference
                .setOnPreferenceClickListener(new OnResetPreferenceClickListener());

        /* Set the PICC "Power ON" preference click callback. */
        mPiccPowerOnPreference
                .setOnPreferenceClickListener(new OnPiccPowerOnPreferenceClickListener());

        /* Set the PICC "Power OFF" preference click callback. */
        mPiccPowerOffPreference
                .setOnPreferenceClickListener(new OnPiccPowerOffPreferenceClickListener());

        /* Set the PICC command APDU preference change callback. */
        mPiccCommandApduPreference
                .setOnPreferenceChangeListener(new OnPiccCommandApduPreferenceChangeListener());

        /* Set the PICC "Transmit" preference click callback. */
        mPiccTransmitPreference
                .setOnPreferenceClickListener(new OnPiccTransmitPreferenceClickListener());

        /* Set the PICC "RF configuration" preference change callback. */
        mPiccRfConfigPreference
                .setOnPreferenceChangeListener(new OnPiccRfConfigPreferenceChangeListener());

        /* Set the PICC "Set RF configuration" preference click callback. */
        mPiccSetRfConfigPreference
                .setOnPreferenceClickListener(new OnPiccSetRfConfigPreferenceClickListener());

        /* Set the reset preference click callback. */
        mDeviceResetPreference
                .setOnPreferenceClickListener(new OnResetPreferenceClickListener());

        /* Set the sleep preference click callback. */
        mDeviceSleepPreference
                .setOnPreferenceClickListener(new OnSleepPreferenceClickListener());

        /* Set the data received preference click callback. */
        mDeviceDataReceivedPreference
                .setOnPreferenceClickListener(new OnDataReceivedPreferenceClickListener());

        /* Set the result callback. */
        mReader.setOnResultAvailableListener(new OnResultAvailableListener());

        /* Set the firmware version callback. */
        mReader.setOnFirmwareVersionAvailableListener(new OnFirmwareVersionAvailableListener());

        /* Set the status callback. */
        mReader.setOnStatusAvailableListener(new OnStatusAvailableListener());

        /* Set the track data callback. */
        mReader.setOnTrackDataAvailableListener(new OnTrackDataAvailableListener());

        /* Set the raw data callback. */
        mReader.setOnRawDataAvailableListener(new OnRawDataAvailableListener());

        /* Set the custom ID callback. */
        mReader.setOnCustomIdAvailableListener(new OnCustomIdAvailableListener());

        /* Set the device ID callback. */
        mReader.setOnDeviceIdAvailableListener(new OnDeviceIdAvailableListener());

        /* Set the DUKPT option callback. */
        mReader.setOnDukptOptionAvailableListener(new OnDukptOptionAvailableListener());

        /* Set the track data option callback. */
        mReader.setOnTrackDataOptionAvailableListener(new OnTrackDataOptionAvailableListener());

        /* Set the PICC ATR callback. */
        mReader.setOnPiccAtrAvailableListener(new OnPiccAtrAvailableListener());

        /* Set the PICC response APDU callback. */
        mReader.setOnPiccResponseApduAvailableListener(new OnPiccResponseApduAvailableListener());

        /* Set the key serial number. */
        mDukptReceiver.setKeySerialNumber(mIksn);

        /* Load the initial key. */
        mDukptReceiver.loadInitialKey(mIpek);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mReader.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReader.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mProgress.dismiss();
        mReader.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProgress.dismiss();
        mReader.stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /* Save the contents. */
        outState.putInt("mSwipeCount", mSwipeCount);

        outState.putCharSequence("about_reader_firmware_version",
                mAboutReaderFirmwareVersionPreference.getSummary());
        outState.putCharSequence("about_reader_battery_level",
                mAboutReaderBatteryLevelPreference.getSummary());
        outState.putCharSequence("about_reader_sleep_timeout",
                mAboutReaderSleepTimeoutPreference.getSummary());

        outState.putCharSequence("reader_id_custom_id",
                mReaderIdCustomIdPreference.getSummary());
        outState.putCharSequence("reader_id_device_id",
                mReaderIdDeviceIdPreference.getSummary());

        outState.putCharSequence("icc_atr", mIccAtrPreference.getSummary());
        outState.putCharSequence("icc_active_protocol",
                mIccActiveProtocolPreference.getSummary());
        outState.putCharSequence("icc_response_apdu",
                mIccResponseApduPreference.getSummary());
        outState.putCharSequence("icc_control_response",
                mIccControlResponsePreference.getSummary());

        outState.putCharSequence("picc_atr", mPiccAtrPreference.getSummary());
        outState.putCharSequence("picc_response_apdu",
                mPiccResponseApduPreference.getSummary());

        outState.putCharSequence("device_data_received",
                mDeviceDataReceivedPreference.getSummary());

        outState.putCharSequence("track_data_swipe_count",
                mTrackDataSwipeCountPreference.getSummary());
        outState.putCharSequence("track_data_battery_status",
                mTrackDataBatteryStatusPreference.getSummary());
        outState.putCharSequence("track_data_key_serial_number",
                mTrackDataKeySerialNumberPreference.getSummary());
        outState.putCharSequence("track_data_track1_mac",
                mTrackDataTrack1MacPreference.getSummary());
        outState.putCharSequence("track_data_track2_mac",
                mTrackDataTrack2MacPreference.getSummary());

        outState.putCharSequence("track1_jis2_data",
                mTrack1Jis2DataPreference.getSummary());
        outState.putCharSequence("track1_primary_account_number",
                mTrack1PrimaryAccountNumberPreference.getSummary());
        outState.putCharSequence("track1_name",
                mTrack1NamePreference.getSummary());
        outState.putCharSequence("track1_expiration_date",
                mTrack1ExpirationDatePreference.getSummary());
        outState.putCharSequence("track1_service_code",
                mTrack1ServiceCodePreference.getSummary());
        outState.putCharSequence("track1_discretionary_data",
                mTrack1DiscretionaryDataPreference.getSummary());

        outState.putCharSequence("track2_primary_account_number",
                mTrack2PrimaryAccountNumberPreference.getSummary());
        outState.putCharSequence("track2_expiration_date",
                mTrack2ExpirationDatePreference.getSummary());
        outState.putCharSequence("track2_service_code",
                mTrack2ServiceCodePreference.getSummary());
        outState.putCharSequence("track2_discretionary_data",
                mTrack2DiscretionaryDataPreference.getSummary());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean ret = true;

        switch (item.getItemId()) {

        case R.id.action_clear:
            clearData();
            break;

        default:
            ret = super.onOptionsItemSelected(item);
            break;
        }

        return ret;
    }

    /**
     * Converts the battery level to string.
     * 
     * @param batteryLevel
     *            the battery level.
     * @return the battery level string.
     */
    private String toBatteryLevelString(int batteryLevel) {

        String batteryLevelString = null;

        switch (batteryLevel) {
        case 0:
            batteryLevelString = ">= 3.00V";
            break;
        case 1:
            batteryLevelString = "2.90V - 2.99V";
            break;
        case 2:
            batteryLevelString = "2.80V - 2.89V";
            break;
        case 3:
            batteryLevelString = "2.70V - 2.79V";
            break;
        case 4:
            batteryLevelString = "2.60V - 2.69V";
            break;
        case 5:
            batteryLevelString = "2.50V - 2.59V";
            break;
        case 6:
            batteryLevelString = "2.40V - 2.49V";
            break;
        case 7:
            batteryLevelString = "2.30V - 2.39V";
            break;
        case 8:
            batteryLevelString = "< 2.30V";
            break;
        default:
            batteryLevelString = "Unknown";
            break;
        }

        return batteryLevelString;
    }

    /**
     * Converts the error code to string.
     * 
     * @param errorCode
     *            the error code.
     * @return the error code string.
     */
    private String toErrorCodeString(int errorCode) {

        String errorCodeString = null;

        switch (errorCode) {
        case Result.ERROR_SUCCESS:
            errorCodeString = "The operation completed successfully.";
            break;
        case Result.ERROR_INVALID_COMMAND:
            errorCodeString = "The command is invalid.";
            break;
        case Result.ERROR_INVALID_PARAMETER:
            errorCodeString = "The parameter is invalid.";
            break;
        case Result.ERROR_INVALID_CHECKSUM:
            errorCodeString = "The checksum is invalid.";
            break;
        case Result.ERROR_INVALID_START_BYTE:
            errorCodeString = "The start byte is invalid.";
            break;
        case Result.ERROR_UNKNOWN:
            errorCodeString = "The error is unknown.";
            break;
        case Result.ERROR_DUKPT_OPERATION_CEASED:
            errorCodeString = "The DUKPT operation is ceased.";
            break;
        case Result.ERROR_DUKPT_DATA_CORRUPTED:
            errorCodeString = "The DUKPT data is corrupted.";
            break;
        case Result.ERROR_FLASH_DATA_CORRUPTED:
            errorCodeString = "The flash data is corrupted.";
            break;
        case Result.ERROR_VERIFICATION_FAILED:
            errorCodeString = "The verification is failed.";
            break;
        default:
            errorCodeString = "Error communicating with reader.";
            break;
        }

        return errorCodeString;
    }

    /**
     * Converts the battery status to string.
     * 
     * @param batteryStatus
     *            the battery status.
     * @return the battery status string.
     */
    private String toBatteryStatusString(int batteryStatus) {

        String batteryStatusString = null;

        switch (batteryStatus) {

        case TrackData.BATTERY_STATUS_LOW:
            batteryStatusString = "Low";
            break;

        case TrackData.BATTERY_STATUS_FULL:
            batteryStatusString = "Full";
            break;

        default:
            batteryStatusString = "Unknown";
            break;
        }

        return batteryStatusString;
    }

    /**
     * Converts the byte array to HEX string.
     * 
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    private String toHexString(byte[] buffer) {

        String bufferString = "";

        if (buffer != null) {

            for (int i = 0; i < buffer.length; i++) {

                String hexChar = Integer.toHexString(buffer[i] & 0xFF);
                if (hexChar.length() == 1) {
                    hexChar = "0" + hexChar;
                }

                bufferString += hexChar.toUpperCase(Locale.US) + " ";
            }
        }

        return bufferString;
    }

    /**
     * Converts the integer to HEX string.
     * 
     * @param i
     *            the integer.
     * @return the HEX string.
     */
    private String toHexString(int i) {

        String hexString = Integer.toHexString(i);

        if (hexString.length() % 2 == 1) {
            hexString = "0" + hexString;
        }

        return hexString.toUpperCase(Locale.US);
    }

    /**
     * Converts the HEX string to byte array.
     * 
     * @param hexString
     *            the HEX string.
     * @return the number of bytes.
     */
    private int toByteArray(String hexString, byte[] byteArray) {

        char c = 0;
        boolean first = true;
        int length = 0;
        int value = 0;
        int i = 0;

        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if ((c >= '0') && (c <= '9')) {
                value = c - '0';
            } else if ((c >= 'A') && (c <= 'F')) {
                value = c - 'A' + 10;
            } else if ((c >= 'a') && (c <= 'f')) {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[length] = (byte) (value << 4);

                } else {

                    byteArray[length] |= value;
                    length++;
                }

                first = !first;
            }

            if (length >= byteArray.length) {
                break;
            }
        }

        return length;
    }

    /**
     * Converts the HEX string to byte array.
     * 
     * @param hexString
     *            the HEX string.
     * @return the byte array.
     */
    private byte[] toByteArray(String hexString) {

        byte[] byteArray = null;
        int count = 0;
        char c = 0;
        int i = 0;

        boolean first = true;
        int length = 0;
        int value = 0;

        // Count number of hex characters
        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[length] = (byte) (value << 4);

                } else {

                    byteArray[length] |= value;
                    length++;
                }

                first = !first;
            }
        }

        return byteArray;
    }

    /**
     * Decrypts the data using AES.
     * 
     * @param key
     *            the key.
     * @param input
     *            the input buffer.
     * @return the output buffer.
     * @throws GeneralSecurityException
     *             if there is an error in the decryption process.
     */
    private byte[] aesDecrypt(byte key[], byte[] input)
            throws GeneralSecurityException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        return cipher.doFinal(input);
    }

    /**
     * Decrypts the data using Triple DES.
     * 
     * @param key
     *            the key.
     * @param input
     *            the input buffer.
     * @return the output buffer.
     * @throws GeneralSecurityException
     *             if there is an error in the decryption process.
     */
    private byte[] tripleDesDecrypt(byte[] key, byte[] input)
            throws GeneralSecurityException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DESede");
        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[8]);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipher.doFinal(input);
    }

    /**
     * Clears the data.
     */
    private void clearData() {

        /* Reset the swipe count. */
        mSwipeCount = 0;

        mAboutReaderFirmwareVersionPreference.setSummary("");
        mAboutReaderBatteryLevelPreference.setSummary("");
        mAboutReaderSleepTimeoutPreference.setSummary("");

        mReaderIdCustomIdPreference.setSummary("");
        mReaderIdDeviceIdPreference.setSummary("");

        mIccAtrPreference.setSummary("");
        mIccActiveProtocolPreference.setSummary("");
        mIccResponseApduPreference.setSummary("");
        mIccControlResponsePreference.setSummary("");

        mPiccAtrPreference.setSummary("");
        mPiccResponseApduPreference.setSummary("");

        mDeviceDataReceivedPreference.setSummary("");

        mTrackDataSwipeCountPreference.setSummary("");
        mTrackDataBatteryStatusPreference.setSummary("");
        mTrackDataKeySerialNumberPreference.setSummary("");
        mTrackDataTrack1MacPreference.setSummary("");
        mTrackDataTrack2MacPreference.setSummary("");

        mTrack1Jis2DataPreference.setSummary("");
        mTrack1PrimaryAccountNumberPreference.setSummary("");
        mTrack1NamePreference.setSummary("");
        mTrack1ExpirationDatePreference.setSummary("");
        mTrack1ServiceCodePreference.setSummary("");
        mTrack1DiscretionaryDataPreference.setSummary("");

        mTrack2PrimaryAccountNumberPreference.setSummary("");
        mTrack2ExpirationDatePreference.setSummary("");
        mTrack2ServiceCodePreference.setSummary("");
        mTrack2DiscretionaryDataPreference.setSummary("");
    }

    /**
     * Shows the message dialog.
     * 
     * @param titleId
     *            the title ID.
     * @param messageId
     *            the message ID.
     */
    private void showMessageDialog(int titleId, int messageId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage(messageId)
                .setTitle(titleId)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        });

        builder.show();
    }

    /**
     * Checks the reset volume.
     * 
     * @return true if current volume is equal to maximum volume.
     */
    private boolean checkResetVolume() {

        boolean ret = true;

        int currentVolume = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);

        int maxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        if (currentVolume < maxVolume) {

            showMessageDialog(R.string.info, R.string.message_reset_info_volume);
            ret = false;
        }

        return ret;
    }

    /**
     * Shows the firmware version.
     */
    private void showFirmwareVersion() {

        synchronized (mResponseEvent) {

            /* Wait for the firmware version. */
            while (!mFirmwareVersionReady && !mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            if (mFirmwareVersionReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the firmware version. */
                        mAboutReaderFirmwareVersionPreference
                                .setSummary(mFirmwareVersion);
                    }
                });

            } else if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the result. */
                        Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        Toast.makeText(mContext, "The operation timed out.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            mFirmwareVersionReady = false;
            mResultReady = false;
        }
    }

    /**
     * Shows the request queue error.
     */
    private void showRequestQueueError() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                /* Show the request queue error. */
                Toast.makeText(mContext, "The request cannot be queued.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Shows the status.
     */
    private void showStatus() {

        synchronized (mResponseEvent) {

            /* Wait for the status. */
            while (!mStatusReady && !mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            if (mStatusReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the status. */
                        mAboutReaderBatteryLevelPreference
                                .setSummary(toBatteryLevelString(mStatus
                                        .getBatteryLevel()));
                        mAboutReaderSleepTimeoutPreference.setSummary(mStatus
                                .getSleepTimeout() + " secs");
                    }
                });

            } else if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the result. */
                        Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        Toast.makeText(mContext, "The operation timed out.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            mStatusReady = false;
            mResultReady = false;
        }
    }

    /**
     * Shows the result.
     * 
     * @return true if the result is ready.
     */
    private boolean showResult() {

        boolean ret = false;

        synchronized (mResponseEvent) {

            /* Wait for the result. */
            while (!mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            ret = mResultReady;

            if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the result. */
                        Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        Toast.makeText(mContext, "The operation timed out.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            mResultReady = false;
        }

        return ret;
    }

    /**
     * Shows the custom ID.
     */
    private void showCustomId() {

        synchronized (mResponseEvent) {

            /* Wait for the custom ID. */
            while (!mCustomIdReady && !mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            if (mCustomIdReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        String customIdString = null;

                        try {
                            customIdString = new String(mCustomId, "US-ASCII");
                        } catch (UnsupportedEncodingException e) {
                        }

                        /* Show the custom ID. */
                        mReaderIdCustomIdPreference.setSummary(customIdString);
                    }
                });

            } else if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the result. */
                        Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        Toast.makeText(mContext, "The operation timed out.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            mCustomIdReady = false;
            mResultReady = false;
        }
    }

    /**
     * Shows the device ID.
     */
    private void showDeviceId() {

        synchronized (mResponseEvent) {

            /* Wait for the device ID. */
            while (!mDeviceIdReady && !mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            if (mDeviceIdReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the device ID. */
                        mReaderIdDeviceIdPreference
                                .setSummary(toHexString(mDeviceId));
                    }
                });

            } else if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the result. */
                        Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        Toast.makeText(mContext, "The operation timed out.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            mDeviceIdReady = false;
            mResultReady = false;
        }
    }

    /**
     * Shows the DUKPT option.
     */
    private void showDukptOption() {

        synchronized (mResponseEvent) {

            /* Wait for the DUKPT option. */
            while (!mDukptOptionReady && !mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            if (mDukptOptionReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the DUKPT option. */
                        mDukptSetupDukptPreference.setChecked(mDukptOption);
                    }
                });

            } else if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the result. */
                        Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        Toast.makeText(mContext, "The operation timed out.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            mDukptOptionReady = false;
            mResultReady = false;
        }
    }

    /**
     * Shows the track data option.
     */
    private void showTrackDataOption() {

        synchronized (mResponseEvent) {

            /* Wait for the track data option. */
            while (!mTrackDataOptionReady && !mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            if (mTrackDataOptionReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the track data option. */
                        mTrackDataSetupEncryptedTrack1Preference
                                .setChecked((mTrackDataOption & AudioJackReader.TRACK_DATA_OPTION_ENCRYPTED_TRACK1) != 0);
                        mTrackDataSetupEncryptedTrack2Preference
                                .setChecked((mTrackDataOption & AudioJackReader.TRACK_DATA_OPTION_ENCRYPTED_TRACK2) != 0);
                        mTrackDataSetupMaskedTrack1Preference
                                .setChecked((mTrackDataOption & AudioJackReader.TRACK_DATA_OPTION_MASKED_TRACK1) != 0);
                        mTrackDataSetupMaskedTrack2Preference
                                .setChecked((mTrackDataOption & AudioJackReader.TRACK_DATA_OPTION_MASKED_TRACK2) != 0);
                    }
                });

            } else if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the result. */
                        Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        Toast.makeText(mContext, "The operation timed out.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            mTrackDataOptionReady = false;
            mResultReady = false;
        }
    }

    /**
     * Shows the PICC ATR.
     */
    private void showPiccAtr() {

        synchronized (mResponseEvent) {

            /* Wait for the PICC ATR. */
            while (!mPiccAtrReady && !mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            if (mPiccAtrReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the PICC ATR. */
                        mPiccAtrPreference.setSummary(toHexString(mPiccAtr));
                    }
                });

            } else if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the result. */
                        Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        Toast.makeText(mContext, "The operation timed out.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            mPiccAtrReady = false;
            mResultReady = false;
        }
    }

    /**
     * Shows the PICC response APDU.
     */
    private void showPiccResponseApdu() {

        synchronized (mResponseEvent) {

            /* Wait for the PICC response APDU. */
            while (!mPiccResponseApduReady && !mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            if (mPiccResponseApduReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the PICC response APDU. */
                        mPiccResponseApduPreference
                                .setSummary(toHexString(mPiccResponseApdu));
                    }
                });

            } else if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the result. */
                        Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        Toast.makeText(mContext, "The operation timed out.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            mPiccResponseApduReady = false;
            mResultReady = false;
        }
    }

    /**
     * Shows the exception message.
     * 
     * @param e
     *            the exception.
     */
    private void showException(Exception e) {

        final String message = (e.getMessage() == null) ? e.toString() : e
                .getMessage();

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                /* Show the exception message. */
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
