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

package org.lunci.dumbthing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.lunci.dumbthing.R;
import org.lunci.dumbthing.dataModel.DumbModel;

/**
 * Created by Lunci Hua on 2/2/2015.
 */
public class DumbItemSimpleAdapter extends ArrayAdapter<DumbModel> {
    private static final String TAG=DumbItemSimpleAdapter.class.getSimpleName();
    private int mLayoutId;
    public DumbItemSimpleAdapter(Context context, int layoutId){
        super(context, -1);
        mLayoutId=layoutId;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            final LayoutInflater mInflater=(LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=mInflater.inflate(mLayoutId, parent, false);
            final ViewHolder holder=new ViewHolder();
            convertView.setTag(holder);
            final TextView mDataTime=(TextView)convertView.findViewById(R.id.textView_datetime);
            final TextView mContent=(TextView)convertView.findViewById(R.id.textView_content);
            holder.mContent=mContent;
            holder.mDate=mDataTime;
        }
        if(convertView.getTag() instanceof ViewHolder){
            final DumbModel model=getItem(position);
            final ViewHolder holder=(ViewHolder)convertView.getTag();
            holder.mDate.setText(model.getCreatedAt());
            holder.mContent.setText(model.getContent());
        }
        return convertView;
    }

    private final class ViewHolder{
        public TextView mDate;
        public TextView mContent;
    }
}
