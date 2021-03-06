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

import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;
import org.lunci.dumbthing.dataModel.DumbModel;
import org.lunci.dumbthing.dataModel.GlobalMessages;
import org.lunci.dumbthing.dialog.AddDumbThingDialog;
import org.lunci.dumbthing.preference.PreferencesTracker;
import org.lunci.dumbthing.service.DataServiceMessages;
import org.lunci.dumbthing.ui.RippleTextView;
import org.lunci.dumbthing.util.Utils;


public class MainFragment extends ServiceFragmentBase {
    private static String TAG = MainFragment.class.getSimpleName();
    private static String EXTRA_DUMB_COUNT = "extra_dumb_count";
    private boolean mStarted = false;
    private long mDumbCount = 0;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean succ = false;
            switch (msg.what) {
                case DataServiceMessages.Service_Get_Item_Count_Finished:
                    if (msg.obj != null) {
                        final long count = (long) msg.obj;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "on handling message: Service_Get_Item_Count_Finished, count=" + count);
                        }
                        if (mDumbCount != count) {
                            mDumbCount = count;
                            mViewHolder.getRippleTextView().setTextRipple(String.valueOf(count));
                            succ = true;
                        }
                    } else {
                        Log.e(TAG, "Service_Get_Item_Count_Finished, does not return count.");
                    }
                    break;
                case DataServiceMessages.Service_Add_Item_Finished:
                    if (PreferencesTracker.getInstance().isEnableAutoShare()) {
                        if (msg.obj instanceof DumbModel) {
                            final DumbModel model = (DumbModel) msg.obj;
                            if (model.getContent() != null && !model.getContent().isEmpty()) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(PreferencesTracker.getInstance().isAutoSharingAccountExists())
                                        {
                                            Utils.autoShareText(getActivity(), model.getContent());
                                        }
                                        else
                                        {
                                            Utils.shareText(getActivity(), model.getContent());
                                        }
                                    }
                                });
                                succ = true;
                            }
                        }
                    }
                    break;
            }
            return succ;
        }
    });
    private ViewHolder mViewHolder = new ViewHolder();

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        if (savedInstanceState != null) {
            mDumbCount = savedInstanceState.getLong(EXTRA_DUMB_COUNT);
        } else {

        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        if (!mStarted) {
            update();
            mStarted = true;
        }
    }

    private void update() {
        sendMessageToService(Message.obtain(null, DataServiceMessages.Service_Get_Item_Count_Sticky));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_DUMB_COUNT, mDumbCount);
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public void onResume() {
        super.onResume();
        getEventBus().unregister(this);
        getEventBus().registerSticky(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mViewHolder.setRippleTextView((RippleTextView) rootView.findViewById(R.id.textView_counter));
        mViewHolder.setAddDumbButton(rootView.findViewById(R.id.textView_add_dumb));

        try {
            mViewHolder.setup();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return rootView;
    }

    public void onEventMainThread(GlobalMessages.UpdateMainFragment update) {
        update();
        getEventBus().removeStickyEvent(update);
    }

    @SuppressWarnings("unused")
    private class ViewHolder {
        private static final int DELAY = 400;
        private RippleTextView mRippleCounter;
        private View mAddDumbButton;

        public ViewHolder() {
        }

        public RippleTextView getRippleTextView() {
            return mRippleCounter;
        }

        public void setRippleTextView(RippleTextView rippleTextView) {
            this.mRippleCounter = rippleTextView;
        }

        public View getAddDumbButton() {
            return mAddDumbButton;
        }

        public void setAddDumbButton(View addBumButton) {
            this.mAddDumbButton = addBumButton;
        }

        public void setup() throws NullPointerException {
            mRippleCounter.setText(String.valueOf(mDumbCount));
            mAddDumbButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAddDumbButton.setEnabled(false);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final AddDumbThingDialog dialog = new AddDumbThingDialog();
                            dialog.show(getFragmentManager(), AddDumbThingDialog.class.getSimpleName());
                            mAddDumbButton.setEnabled(true);
                        }
                    }, DELAY);
                }
            });

            mRippleCounter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "send message to show list");
                    mRippleCounter.setEnabled(false);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getEventBus().post(Message.obtain(null, GlobalMessages.MESSAGE_SHOW_ALL_DUMBS));
                            mRippleCounter.setEnabled(true);
                        }
                    }, DELAY);
                }
            });
        }
    }

    public void onEventMainThread(AddDumbThingDialog.AddDumbThingDialogCallback callback){
        if (callback.getText() == null || callback.getText().isEmpty()) {
            return;
        }
        ++mDumbCount;
        mViewHolder.getRippleTextView().setTextRipple(String.valueOf(mDumbCount));
        final DumbModel model = new DumbModel();
        model.setContent(callback.getText());
        final Message msg = Message.obtain(null, DataServiceMessages.Service_Add_Item, -1, -1, model);
        sendMessageToService(msg);
    }
}
