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

import android.app.Dialog;
import android.content.ContentValues;
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
import org.lunci.dumbthing.dataModel.DumbModel;

import de.greenrobot.event.EventBus;

/**
 * Created by Lunci on 2/2/2015.
 */
public class EditDumbThingDialog extends DialogFragment{
    public static class EditDumbThingDialogCallbacks{
        private final long mId;
        private final ContentValues mUpdatedValues;
        private final int mPosition;
        public EditDumbThingDialogCallbacks(long id, ContentValues updatedValues, int position){
            mId=id;
            mUpdatedValues=updatedValues;
            mPosition=position;
            
        }
        
        public long getId(){
            return mId;
            
        }
        
        public ContentValues getUpdatedValues(){
            return mUpdatedValues;
            
        }
        
        public int getPosition(){
            return mPosition;
            
        }
    }
    private static final String EXTRA_POSITION="extra_item_position";
    private static final String EXTRA_ITEM="extra_item";
    private static final String EXTRA_CONTENT="extra_content";

    private EditText mEditText;
    private DumbModel mModel;
    private int mPosition;
    
    public static EditDumbThingDialog newInstance(DumbModel item, int position){
        final Bundle bundle=new Bundle();
        bundle.putInt(EXTRA_POSITION, position);
        bundle.putParcelable(EXTRA_ITEM, item);
        final EditDumbThingDialog dialog= new EditDumbThingDialog();       
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            mModel=getArguments().getParcelable(EXTRA_ITEM);
            mPosition=getArguments().getInt(EXTRA_POSITION);
        }
        if(savedInstanceState!=null){
            mModel=savedInstanceState.getParcelable(EXTRA_ITEM);
            mPosition=savedInstanceState.getInt(EXTRA_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_edit_dumb_thing, container);
        mEditText=(EditText)view.findViewById(R.id.editText_content);
        if(mModel!=null){
            mEditText.setText(mModel.getContent());
        }
        if(savedInstanceState!=null){
            mEditText.setText(savedInstanceState.getString(EXTRA_CONTENT));
        }
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(EditorInfo.IME_ACTION_DONE==actionId){
                    final ContentValues updatedValues=new ContentValues();
                    updatedValues.put(DumbModel.Content_Field, mEditText.getText().toString());
                    EventBus.getDefault().post(new EditDumbThingDialogCallbacks(mModel.getId(), updatedValues, mPosition));
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        view.findViewById(R.id.button_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ContentValues updatedValues=new ContentValues();
                updatedValues.put(DumbModel.Content_Field, mEditText.getText().toString());
                EventBus.getDefault().post(new EditDumbThingDialogCallbacks(mModel.getId(), updatedValues, mPosition));
                dismiss();
            }
        });
        view.findViewById(R.id.button_negative).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
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
        outBundle.putParcelable(EXTRA_ITEM, mModel);
        outBundle.putInt(EXTRA_POSITION, mPosition);
    }
}
