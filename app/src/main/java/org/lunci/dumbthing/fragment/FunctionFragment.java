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

package org.lunci.dumbthing.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.lunci.dumbthing.R;
import org.lunci.dumbthing.dialog.CalendarDialog;
import org.lunci.dumbthing.service.DataServiceMessages;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FunctionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FunctionFragment extends ServiceFragmentBase {

    // TODO: Rename and change types and number of parameters
    public static FunctionFragment newInstance() {
        FunctionFragment fragment = new FunctionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private Handler mHandler=new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            boolean succ=false;
            switch (msg.what){
                case DataServiceMessages.Service_Get_All_Dates_ASC_Finished:
                    if(msg.obj instanceof ArrayList) {
                        try{
                            final ArrayList<String> dates=(ArrayList<String>)msg.obj;
                            final CalendarDialog calendarDialog = CalendarDialog.newInstance(dates);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    calendarDialog.show(getFragmentManager(), CalendarDialog.class.getSimpleName());
                                }
                            }, 200);
                            succ = true;
                        }catch (ClassCastException ex){
                            ex.printStackTrace();
                        }
                    }
                    break;
            }
            return succ;
        }
    });

    private ViewHolder mViewHolder=new ViewHolder();

    public FunctionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView= inflater.inflate(R.layout.fragment_functions, container, false);
        final View calendarButton=rootView.findViewById(R.id.imageView_calendar);
        mViewHolder.setCalendarButton(calendarButton);

        mViewHolder.setup();
        return rootView;
    }

    private final class ViewHolder{
        public View getCalendarButton() {
            return mCalendarButton;
        }

        public void setCalendarButton(View calendarButton) {
            this.mCalendarButton = calendarButton;
        }

        private View mCalendarButton;

        public void setup() throws NullPointerException{
            mCalendarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageToService(Message.obtain(null, DataServiceMessages.Service_Get_All_Dates_ASC));
                }
            });
        }
    }
}
