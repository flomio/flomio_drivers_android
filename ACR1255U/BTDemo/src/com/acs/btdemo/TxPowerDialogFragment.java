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
import android.os.Bundle;

/**
 * The <code>TxPowerDialogFragment</code> class shows a list of Tx power values.
 * 
 * @author Godfrey Chung
 * @version 1.0, 27 May 2015
 */
public class TxPowerDialogFragment extends DialogFragment {

    /**
     * The <code>TxPowerDialogListener</code> Interface defines a callback to be
     * invoked.
     */
    public interface TxPowerDialogListener {

        /**
         * Called when the item is clicked.
         * 
         * @param dialog
         *            the dialog fragment.
         * @param which
         *            the item.
         */
        public void onDialogItemClick(DialogFragment dialog, int which);
    }

    /** Listener. */
    private TxPowerDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (TxPowerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + "must implement TxPowerDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.set_tx_power).setItems(
                R.array.tx_power_values, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogItemClick(TxPowerDialogFragment.this,
                                which);
                    }
                });

        return builder.create();
    }
}
