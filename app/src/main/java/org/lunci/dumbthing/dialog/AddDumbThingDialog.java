/*
 * Copyright 2015 Lunci Hua
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lunci.dumbthing.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.lunci.dumbthing.R;

/**
 * Created by Lunci on 2/2/2015.
 */
public class AddDumbThingDialog extends DialogFragment{
    public interface AddDumbThingDialogCallbacks{
        void onConfirmed(String text);
        void onCanceled();
    }
    private static final String EXTRA_CONTENT="extra_content";
    private static final AddDumbThingDialogCallbacks DummyCallbacks=new AddDumbThingDialogCallbacks() {
        @Override
        public void onConfirmed(String text) {

        }

        @Override
        public void onCanceled() {

        }
    };
    private AddDumbThingDialogCallbacks mCallbacks=DummyCallbacks;
    private EditText mEditText;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_add_dumb_thing, container);
        mEditText=(EditText)view.findViewById(R.id.editText_content);
        if(savedInstanceState!=null){
            mEditText.setText(savedInstanceState.getString(EXTRA_CONTENT));
        }
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(EditorInfo.IME_ACTION_DONE==actionId){
                    mCallbacks.onConfirmed(mEditText.getText().toString());
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        view.findViewById(R.id.button_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.onConfirmed(mEditText.getText().toString());
                dismiss();
            }
        });
        view.findViewById(R.id.button_negative).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mCallbacks.onCanceled();
                dismiss();
            }
        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Dialog dialog=super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getResources().getString(R.string.add_dumb_thing));
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outBundle){
        super.onSaveInstanceState(outBundle);
        outBundle.putString(EXTRA_CONTENT, mEditText.getText().toString());
    }

    public void setCallbacks(AddDumbThingDialogCallbacks callbacks){
        mCallbacks=callbacks;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity instanceof AddDumbThingDialogCallbacks){
            mCallbacks=(AddDumbThingDialogCallbacks)activity;
        }
    }

    @Override
    public void onDetach(){
        mCallbacks=DummyCallbacks;
        super.onDetach();
    }
}
