/*
 * Copyright (c) 2015 Lunci Hua
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package org.lunci.dumbthing.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.squareup.timessquare.CalendarPickerView;

import org.lunci.dumbthing.R;
import org.lunci.dumbthing.util.DateTimeHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lunci on 2/3/2015.
 */
public class CalendarDialog extends DialogFragment {
    public static final String EXTRA_DATES="extra_dates";

    public interface CalendarDialogCallbacks{
        void onDateSelected(Date date);
    }

    private static final CalendarDialogCallbacks DummyCallbacks=new CalendarDialogCallbacks() {
        @Override
        public void onDateSelected(Date date) {

        }
    };

    public static CalendarDialog newInstance(ArrayList<String> availableDates){
        final CalendarDialog dialog=new CalendarDialog();
        final Bundle bundle=new Bundle();
        bundle.putStringArrayList(EXTRA_DATES, availableDates);
        dialog.setArguments(bundle);
        return dialog;
    }

    private ArrayList<Date> mDates;
    private CalendarDialogCallbacks mCallbacks=DummyCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            final List<String> dateString=getArguments().getStringArrayList(EXTRA_DATES);
            mDates=new ArrayList<>(dateString.size());
            try {
                for (String s : dateString) {
                    mDates.add(DateTimeHelper.getDateTimeFromString(s, DateTimeHelper.DISPLAYFORMAT));
                }
            }catch (ParseException ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.calendar_layout, container);
        CalendarPickerView calendar = (CalendarPickerView) view.findViewById(R.id.calendar);
        if(mDates!=null && mDates.size()>0) {
            final Date minDate=mDates.get(0);
            final Date maxDate=new Date(System.currentTimeMillis()+86400000);//add one more day
            calendar.init(minDate, maxDate);
            calendar.highlightDates(mDates);
            calendar.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
                @Override
                public void onDateSelected(Date date) {
                    mCallbacks.onDateSelected(date);
                    dismiss();
                }

                @Override
                public void onDateUnselected(Date date) {

                }
            });
        }else{
            this.dismiss();
        }
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.dumb_thing_calendar);
        return dialog;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity instanceof CalendarDialogCallbacks){
            mCallbacks=(CalendarDialogCallbacks)activity;
        }
    }

    @Override
    public void onDetach(){
        mCallbacks=DummyCallbacks;
        super.onDetach();
    }

    public void setCallbacks(CalendarDialogCallbacks callbacks){
        mCallbacks=callbacks;
    }
}
