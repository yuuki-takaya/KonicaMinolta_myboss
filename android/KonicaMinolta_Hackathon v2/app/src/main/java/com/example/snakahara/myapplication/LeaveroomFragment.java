package com.example.snakahara.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by TSUKASA on 16/06/20.
 */
public class LeaveroomFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Dialog dialog = builder.setTitle("出席管理システム").setMessage("退室しました！").setPositiveButton("OK",null).create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }
}
