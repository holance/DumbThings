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
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;
import org.lunci.dumbthing.adapter.DumbItemListAdapter;
import org.lunci.dumbthing.dataModel.DumbModel;
import org.lunci.dumbthing.dataModel.GlobalMessages;
import org.lunci.dumbthing.dialog.CalendarDialog;
import org.lunci.dumbthing.dialog.EditDumbThingDialog;
import org.lunci.dumbthing.service.DataServiceMessages;
import org.lunci.dumbthing.util.DateTimeHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class ItemListFragment extends ServiceFragmentBase implements AbsListView.OnItemClickListener,
        DumbItemListAdapter.DumbItemListAdapterCallbacks {
    private static final String TAG = ItemListFragment.class.getSimpleName();
    private static final String EXTRA_ITEMS = "extra_items";
    
    private boolean mStarted = false;
    private ListView mListView;
    private HashMap<Long, Integer> mItemIdTopMap=new HashMap<>();
    private int MOVE_DURATION=300;

    private DumbItemListAdapter mAdapter;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean succ = false;
            switch (msg.what) {
                case DataServiceMessages.Service_Get_All_Item_DESC_Finished:
                    if (msg.obj instanceof List && mAdapter != null) {
                        try {
                            if (BuildConfig.DEBUG) {
                                Log.i(TAG, "on handler Service_Get_All_Item_ASC_Finished");
                            }
                            final List<DumbModel> models = (List<DumbModel>) msg.obj;
                            mAdapter.clear();
                            mAdapter.addAll(models);
                            succ = true;
                        } catch (ClassCastException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
                case DataServiceMessages.Service_Remove_Item_Finished:
                    getEventBus().postSticky(new GlobalMessages.UpdateMainDisplayFragment());
                    getEventBus().postSticky(new GlobalMessages.UpdateMainFragment());
                    succ=true;
                    break;
                case DataServiceMessages.Service_Modify_Item_Finished:
                    getEventBus().postSticky(new GlobalMessages.UpdateMainDisplayFragment());
                    succ=true;
                    break;
            }
            return succ;
        }
    });

    public ItemListFragment() {
    }

    // TODO: Rename and change types of parameters
    public static ItemListFragment newInstance() {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new DumbItemListAdapter(getActivity(), this);
        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(DumbModel.class.getClassLoader());
            final ArrayList<Parcelable> list = savedInstanceState.getParcelableArrayList(EXTRA_ITEMS);
            for (Parcelable p : list) {
                mAdapter.add((DumbModel) p);
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        if (!mStarted) {
            sendMessageToService(Message.obtain(null, DataServiceMessages.Service_Get_All_Items_DESC));
            mStarted = true;
        }
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dumbmodel_list, container, false);

        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        final ArrayList<DumbModel> models = new ArrayList<>(mAdapter.getCount());
        for (int i = 0; i < mAdapter.getCount(); ++i) {
            models.add(mAdapter.getItem(i));
        }
        bundle.putParcelableArrayList(EXTRA_ITEMS, models);
    }

    public void findItemByDate(Date date) {
        try {
            final String dateString = DateTimeHelper.getStringFromDate(date, DateTimeHelper.DISPLAYFORMAT);
            Log.i(TAG, "findItemByDate, date=" + dateString);
            for (int i = 0; i < mAdapter.getCount(); ++i) {
                if (mAdapter.getItem(i).getCreatedAt().regionMatches(0, dateString, 0, 8)) {
                    mListView.smoothScrollToPositionFromTop(i, 20, 400);
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "Found item at index:" + i);
                    }
                    break;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onItemClick, position=" + position);
        }
        final EditDumbThingDialog dialog=EditDumbThingDialog.newInstance(mAdapter.getItem(position), position);
        dialog.show(getFragmentManager(), EditDumbThingDialog.class.getSimpleName());
    }
    
    public void onEventMainThread(EditDumbThingDialog.EditDumbThingDialogCallbacks callbacks){
        mAdapter.getItem(callbacks.getPosition()).setContent(callbacks.getUpdatedValues().getAsString(DumbModel.Content_Field));
        final Bundle bundle=new Bundle();
        bundle.putLong(DataServiceMessages.EXTRA_ID, callbacks.getId());
        bundle.putParcelable(DataServiceMessages.EXTRA_CONTENTVALUES, callbacks.getUpdatedValues());
        sendMessageToService(Message.obtain(null, DataServiceMessages.Service_Modify_Item, -1, -1, bundle));
    }

    public void onEventMainThread(CalendarDialog.CalendarDialogCallbacks callbacks){
        if(callbacks.getSelectedDate()!=null)
            findItemByDate(callbacks.getSelectedDate());
    }

    @Override
    public void onItemEdit(final int position, final View view) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onItemEdit, position=" + position);
        }
    }

    @Override
    public void onItemDelete(final int position, final View view) {

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onItemDelete, position=" + position);
        }
        mListView.setEnabled(false);
        final DumbModel item = mAdapter.getItem(position);
        sendMessageToService(Message.obtain(null, DataServiceMessages.Service_Remove_Item, -1, -1, item));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.closeAllItems();
                animateRemoval(mListView, view, item);
            }
        }, 300);
    }

    private void animateRemoval(final ListView listview, View viewToRemove, DumbModel item) {
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = mAdapter.getItemId(position);
                mItemIdTopMap.put(itemId, child.getTop());
            }
        }
        // Delete the item from the adapter
        mAdapter.remove(item);

        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listview.getFirstVisiblePosition();
                for (int i = 0; i < listview.getChildCount(); ++i) {
                    final View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = mAdapter.getItemId(position);
                    Integer startTop = mItemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if (firstAnimation) {
                                child.animate().withEndAction(new Runnable() {
                                    public void run() {
                                        mListView.setEnabled(true);
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    } else {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                public void run() {
                                    mListView.setEnabled(true);
                                }
                            });
                            firstAnimation = false;
                        }
                    }
                }
                mItemIdTopMap.clear();
                return true;
            }
        });
    }
}
