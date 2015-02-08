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

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.implments.SwipeItemAdapterMangerImpl;
import com.daimajia.swipe.interfaces.SwipeAdapterInterface;
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface;
import com.daimajia.swipe.util.Attributes;

import org.lunci.dumbthing.R;
import org.lunci.dumbthing.dataModel.DumbModel;
import org.lunci.dumbthing.util.Utils;

import java.util.List;

/**
 * Created by Lunci Hua on 2/2/2015.
 */
public class DumbItemListAdapter extends ArrayAdapter<DumbModel> implements SwipeItemMangerInterface,SwipeAdapterInterface {
    private static final String TAG=DumbItemListAdapter.class.getSimpleName();
    private SwipeItemAdapterMangerImpl mItemManger = new SwipeItemAdapterMangerImpl(this);
    public static interface DumbItemListAdapterCallbacks{
        void onItemEdit(int position, View view);
        void onItemDelete(int position, View view);
    }

    private static final DumbItemListAdapterCallbacks DummyCallbacks=new DumbItemListAdapterCallbacks(){

        @Override
        public void onItemEdit(int position, View view) {

        }

        @Override
        public void onItemDelete(int position, View view) {

        }
    };

    private DumbItemListAdapterCallbacks mCallbacks=DummyCallbacks;

    public DumbItemListAdapter(Context context){
        super(context, -1);
        mItemManger.setMode(Attributes.Mode.Single);
    }

    public DumbItemListAdapter(Context context, DumbItemListAdapterCallbacks callbacks){
        this(context);
        mCallbacks=callbacks;
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
            convertView=mInflater.inflate(R.layout.dumb_item_list_simple, parent, false);
            mItemManger.initialize(convertView, position);
            final ViewHolder holder=new ViewHolder();
            convertView.setTag(holder);
            final TextView mDataTime=(TextView)convertView.findViewById(R.id.textView_datetime);
            final TextView mContent=(TextView)convertView.findViewById(R.id.textView_content);
            holder.mContent=mContent;
            holder.mDate=mDataTime;
            holder.mRoot=convertView;
            final View buttonEdit=convertView.findViewById(R.id.imageButton_edit);
            final View buttonDelete=convertView.findViewById(R.id.imageButton_delete);
            final View buttonShare=convertView.findViewById(R.id.imageView_share);
            final View buttonAutoShare=convertView.findViewById(R.id.imageView_auto_share);
            holder.mDeleteButton=buttonDelete;
            holder.mEditButton=buttonEdit;
            holder.mShareButton=buttonShare;
            holder.mAutoShareButton=buttonAutoShare;
            final SwipeLayout swipe=(SwipeLayout)convertView;
            swipe.setDragEdge(SwipeLayout.DragEdge.Left);
            swipe.setShowMode(SwipeLayout.ShowMode.PullOut);
            swipe.setSwipeEnabled(true);
            try {
                holder.setup();
            }catch (NullPointerException ex){
                ex.printStackTrace();
            }
        }
        mItemManger.updateConvertView(convertView, position);
        if(convertView.getTag() instanceof ViewHolder){
            final DumbModel model=getItem(position);
            final ViewHolder holder=(ViewHolder)convertView.getTag();
            holder.mDate.setText(model.getCreatedAt());
            holder.mContent.setText(model.getContent());
            holder.mCurrentIndex=position;
        }
        return convertView;
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipeLayout_root;
    }

    @Override
    public void openItem(int position) {
        mItemManger.openItem(position);
    }

    @Override
    public void closeItem(int position) {
        mItemManger.closeItem(position);
    }

    @Override
    public void closeAllExcept(SwipeLayout layout) {
        mItemManger.closeAllExcept(layout);
    }

    @Override
    public void closeAllItems() {
        mItemManger.closeAllItems();
    }

    @Override
    public List<Integer> getOpenItems() {
        return mItemManger.getOpenItems();
    }

    @Override
    public List<SwipeLayout> getOpenLayouts() {
        return mItemManger.getOpenLayouts();
    }

    @Override
    public void removeShownLayouts(SwipeLayout layout) {
        mItemManger.removeShownLayouts(layout);
    }

    @Override
    public boolean isOpen(int position) {
        return mItemManger.isOpen(position);
    }

    @Override
    public Attributes.Mode getMode() {
        return mItemManger.getMode();
    }

    @Override
    public void setMode(Attributes.Mode mode) {
        mItemManger.setMode(mode);
    }

    public void setCallbacks(DumbItemListAdapterCallbacks callbacks){
        mCallbacks=callbacks;
    }

    private final class ViewHolder{
        public TextView mDate;
        public TextView mContent;
        public View mDeleteButton;
        public View mEditButton;
        public View mShareButton;
        public View mAutoShareButton;
        public View mRoot;
        public View mSlideIndicator;
        public int mCurrentIndex;

        public void setup()throws NullPointerException{
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItemManger.getOpenItems().size()==1) {
                        final int pos=mItemManger.getOpenItems().get(0);
                        mCallbacks.onItemDelete(pos, mRoot);
                    }
                }
            });

            mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItemManger.getOpenItems().size()==1) {
                        final int pos=mItemManger.getOpenItems().get(0);
                        mCallbacks.onItemEdit(pos, mRoot);
                    }
                }
            });

            mShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItemManger.getOpenItems().size()==1) {
                        final int pos=mItemManger.getOpenItems().get(0);
                        Utils.shareText(getContext(), getItem(pos).getContent());
                    }                    
                }
            });
            
            mAutoShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItemManger.getOpenItems().size()==1) {
                        final int pos=mItemManger.getOpenItems().get(0);
                        Utils.autoShareText(getContext(), getItem(pos).getContent());
                    }
                }
            });
        }
    }
}
