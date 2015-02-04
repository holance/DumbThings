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

package org.lunci.dumbthing.fragment;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.ImageButton;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;
import org.lunci.dumbthing.adapter.DumbItemSimpleAdapter;
import org.lunci.dumbthing.dataModel.DumbModel;
import org.lunci.dumbthing.dataModel.GlobalMessages;
import org.lunci.dumbthing.service.DataServiceMessages;
import org.lunci.dumbthing.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lunci on 2/2/2015.
 */
public class MainDisplayFragment extends ServiceFragmentBase {
    private static final String TAG = MainDisplayFragment.class.getSimpleName();
    private static final String EXTRA_ITEMS="extra_items";
    private ViewHolder mViewHolder = new ViewHolder();
    private DumbItemSimpleAdapter mAdapter;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean succ = false;
            switch (msg.what) {
                case DataServiceMessages.Service_Get_All_Item_ASC_Finished:
                    if (msg.obj instanceof List && mAdapter != null) {
                        try {
                            if(BuildConfig.DEBUG){
                                Log.i(TAG, "on handler Service_Get_All_Item_ASC_Finished");
                            }
                            final List<DumbModel> models = (List<DumbModel>) msg.obj;
                            mAdapter.clear();
                            mAdapter.addAll(models);
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    mViewHolder.setDisplayedChild(models.size() - 1);
                                }
                            });
                            succ = true;
                        } catch (ClassCastException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
                case DataServiceMessages.Service_Add_Item_Finished:
                    if (msg.obj instanceof DumbModel) {
                        final DumbModel model = (DumbModel) msg.obj;
                        mAdapter.add(model);
                        mViewHolder.showNext();
                    }
                    break;
            }
            return succ;
        }
    });
    private GestureDetectorCompat mDetector;
    private boolean mStarted=false;

    public MainDisplayFragment() {

    }

    public static MainDisplayFragment newInstance() {
        return new MainDisplayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new DumbItemSimpleAdapter(getActivity(), R.layout.dumb_display_item_simple);
        mDetector = new GestureDetectorCompat(getActivity(), new GestureDetector.OnGestureListener() {
            private static final float ThresholdDistance = 50;
            private static final float ThresholdVelocity = 1000;

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(e1==null || e2==null)return false;
                final float distanceX = e1.getRawX() - e2.getRawX();
                if (distanceX < -ThresholdDistance && velocityX > ThresholdVelocity) {
                    return mViewHolder.showPrevious();
                } else if (distanceX > ThresholdDistance && velocityX < -ThresholdVelocity) {
                    return mViewHolder.showNext();
                }
                return true;
            }
        });
        if(savedInstanceState!=null){
            savedInstanceState.setClassLoader(DumbModel.class.getClassLoader());
            final ArrayList<Parcelable> list = savedInstanceState.getParcelableArrayList(EXTRA_ITEMS);
            for (Parcelable p : list) {
                mAdapter.add((DumbModel) p);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.navigation_panel, container, false);
        final AdapterViewFlipper flipper = (AdapterViewFlipper) rootView.findViewById(R.id.adapterViewFlipper_last_item);
        mViewHolder.setItemSwitcher(flipper);
        mViewHolder.setRootView(rootView);
        final ImageButton shareButton = (ImageButton) rootView.findViewById(R.id.textView_share);
        mViewHolder.setShareButton(shareButton);
        final View leftArrow = rootView.findViewById(R.id.imageView_left_arrow);
        final View rightArrow = rootView.findViewById(R.id.imageView_right_arrow);
        mViewHolder.setLeftArrow(leftArrow);
        mViewHolder.setRightArrow(rightArrow);
        mViewHolder.init();
        if(savedInstanceState!=null){
            mViewHolder.setDisplayedChild(mAdapter.getCount()-1);
        }
        return rootView;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        if(!mStarted) {
            update();
            mStarted=true;
        }
    }
    
    @Override
         public void onResume(){
        super.onResume();
        getEventBus().unregister(this);
        getEventBus().registerSticky(this);
    }
    
    private void update(){
        sendMessageToService(Message.obtain(null, DataServiceMessages.Service_Get_All_Items_ASC));
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        final ArrayList<DumbModel> models=new ArrayList<>(mAdapter.getCount());
        for(int i=0; i<mAdapter.getCount(); ++i) {
            models.add(mAdapter.getItem(i));
        }
        bundle.putParcelableArrayList(EXTRA_ITEMS, models);
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

    public void onEventMainThread(GlobalMessages.UpdateMainDisplayFragment update){
        update();
        getEventBus().removeStickyEvent(update);
    }

    private class ViewHolder {
        public boolean CycleView = false;
        private View mRootView;
        private AdapterViewFlipper mItemSwitcher;
        private View mLeftArrow;
        private View mRightArrow;
        private ImageButton mShareButton;

        public View getRootView() {
            return mRootView;
        }

        public void setRootView(View mRootView) {
            this.mRootView = mRootView;
        }

        public View getRightArrow() {
            return mRightArrow;
        }

        public void setRightArrow(View rightArrow) {
            this.mRightArrow = rightArrow;
        }

        public View getLeftArrow() {
            return mLeftArrow;
        }

        public void setLeftArrow(View leftArrow) {
            this.mLeftArrow = leftArrow;
        }

        public ImageButton getShareButton() {
            return mShareButton;
        }

        public void setShareButton(ImageButton shareButton) {
            this.mShareButton = shareButton;
        }

//        public AdapterViewFlipper getItemSwitcher() {
//            return mItemSwitcher;
//        }

        public void setItemSwitcher(AdapterViewFlipper switcher) {
            mItemSwitcher = switcher;
        }

        public boolean showNext() {
            if (!CycleView && mItemSwitcher.getDisplayedChild() >= mItemSwitcher.getCount() - 1) {
                return false;
            }
            mItemSwitcher.setInAnimation(getActivity(), R.anim.slide_right_in_obj);
            mItemSwitcher.setOutAnimation(getActivity(), R.anim.slide_left_out_obj);
            mItemSwitcher.showNext();
            onViewChanged(mItemSwitcher.getDisplayedChild());
            return true;
        }

        public boolean showPrevious() {
            if (!CycleView && mItemSwitcher.getDisplayedChild() == 0) {
                return false;
            }
            mItemSwitcher.setInAnimation(getActivity(), R.anim.slide_left_in_obj);
            mItemSwitcher.setOutAnimation(getActivity(), R.anim.slide_right_out_obj);
            mItemSwitcher.showPrevious();
            onViewChanged(mItemSwitcher.getDisplayedChild());
            return true;
        }

        public void setDisplayedChild(int position) {
            if (position == mItemSwitcher.getDisplayedChild() || position < 0 || position > mItemSwitcher.getCount() - 1) {
                return;
            } else if (position > mItemSwitcher.getDisplayedChild()) {
                mItemSwitcher.setInAnimation(getActivity(), R.anim.slide_right_in_obj);
                mItemSwitcher.setOutAnimation(getActivity(), R.anim.slide_left_out_obj);
            } else {
                mItemSwitcher.setInAnimation(getActivity(), R.anim.slide_left_in_obj);
                mItemSwitcher.setOutAnimation(getActivity(), R.anim.slide_right_out_obj);
            }
            mItemSwitcher.setDisplayedChild(position);
            onViewChanged(mItemSwitcher.getDisplayedChild());
        }

        public void init() throws NullPointerException {
            mItemSwitcher.setAdapter(mAdapter);
            if (mItemSwitcher != null) {
                mItemSwitcher.setFlipInterval(800);
                mItemSwitcher.setAnimateFirstView(false);
                mItemSwitcher.setInAnimation(getActivity(), R.anim.slide_right_in_obj);
                mItemSwitcher.setOutAnimation(getActivity(), R.anim.slide_left_out_obj);
            }

            mShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int index = mItemSwitcher.getDisplayedChild();
                    final DumbModel model = mAdapter.getItem(index);
                    mShareButton.setEnabled(false);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Utils.shareText(getActivity(), Utils.buildDumbContent(model.getContent()));
                                mShareButton.setEnabled(true);
                            } catch (IndexOutOfBoundsException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }, 300);
                }
            });

            mRootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mDetector.onTouchEvent(event);
                }
            });
            
            mLeftArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPrevious();
                }
            });
            
            mRightArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNext();
                }
            });
        }

        public void onViewChanged(int position) {
            if (position == 0) {
                mLeftArrow.setEnabled(false);
                mLeftArrow.animate().alpha(0f).setDuration(300);
            } else if (position >= mItemSwitcher.getCount() - 1) {
                mRightArrow.setEnabled(false);
                mRightArrow.animate().alpha(0f).setDuration(300);
            } else {
                if (!mLeftArrow.isEnabled()) {
                    mLeftArrow.setEnabled(true);
                    mLeftArrow.animate().alpha(1f).setDuration(300);
                }
                if (!mRightArrow.isEnabled()) {
                    mRightArrow.setEnabled(true);
                    mRightArrow.animate().alpha(1f).setDuration(300);
                }
            }
        }
    }
}
