/*
 * Copyright (C) 2015 Advanced Card Systems Ltd. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Advanced
 * Card Systems Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ACS.
 */

package com.acs.btdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

/**
 * The <code>VersionInfoDialogFragment</code> class shows the version
 * information.
 * 
 * @author Godfrey Chung
 * @version 1.0, 28 May 2015
 */
public class VersionInfoDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        PackageInfo packageInfo = null;
        String version = null;

        try {

            /* Get the version name. */
            packageInfo = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(), 0);
            version = getString(R.string.version) + " "
                    + packageInfo.versionName;

        } catch (NameNotFoundException e) {

            version = getString(R.string.unknown_version);
        }

        builder.setTitle(R.string.about)
                .setMessage(version)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        });

        return builder.create();
    }
}
