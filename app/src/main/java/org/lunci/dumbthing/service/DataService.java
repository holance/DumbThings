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

package org.lunci.dumbthing.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.contentProvider.ContentProvider;
import org.lunci.dumbthing.dataModel.DumbModel;
import org.lunci.dumbthing.util.DateTimeHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DataService extends Service {
    private static final String TAG=DataService.class.getSimpleName();
    
    private EventBus mEventBus = EventBus.getDefault();
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean succ=false;
            switch (msg.what) {
                case DataServiceMessages.Service_Get_Item_Count:
                    final long count1 = getItemCount();
                    postMessage(Message.obtain(null, DataServiceMessages.Service_Get_Item_Count_Finished, -1, -1, count1), false);
                    succ=true;
                    break;
                case DataServiceMessages.Service_Get_Item_Count_Sticky:
                    final long count2 = getItemCount();
                    postMessage(Message.obtain(null, DataServiceMessages.Service_Get_Item_Count_Finished, -1, -1, count2), true);
                    succ=true;
                    break;
                case DataServiceMessages.Service_Add_Item:
                    if (msg.obj instanceof DumbModel) {
                        final DumbModel item = (DumbModel) msg.obj;
                        addItem(item);
                        postMessage(Message.obtain(null, DataServiceMessages.Service_Add_Item_Finished, -1, -1, item), false);
                        succ=true;
                    }
                    break;
                case DataServiceMessages.Service_Modify_Item:
                    if(msg.obj instanceof Bundle){
                        final Bundle bundle=(Bundle)msg.obj;
                        final long id=bundle.getLong(DataServiceMessages.EXTRA_ID);
                        final ContentValues values=bundle.getParcelable(DataServiceMessages.EXTRA_CONTENTVALUES);
                        final int count=modifyItem(id, values);
                        DumbModel item=null;
                        if(count>0){
                            succ=true;
                            item=getItemById(id);
                        }else{
                            Log.w(TAG, "Service_Modify_Item failed on item id="+id);
                        }
                        postMessage(Message.obtain(null, DataServiceMessages.Service_Modify_Item_Finished, count, -1, item), false);
                    }
                    break;
                case DataServiceMessages.Service_Remove_Item:
                    if(msg.obj instanceof DumbModel) {
                        final DumbModel item=(DumbModel)msg.obj;
                        final int count=removeItem(item);
                        if(count>0){
                            succ=true;
                        }else{
                            Log.w(TAG, "Service_Remove_Item failed on item id="+item.getId());
                        }
                        postMessage(Message.obtain(null, DataServiceMessages.Service_Remove_Item_Finished, count, -1, item.getId()), false);
                    }
                    break;
                case DataServiceMessages.Service_Get_All_Items_DESC:
                    getAllItems(false);
                    succ=true;
                    break;
                case DataServiceMessages.Service_Get_All_Items_ASC:
                    getAllItems(true);
                    succ=true;
                    break;
                case DataServiceMessages.Service_Get_All_Dates_DESC:
                    getAllDates(true);
                    succ=true;
                    break;
                case DataServiceMessages.Service_Get_All_Dates_ASC:
                    getAllDates(true);
                    succ=true;
                    break;
            }
            return succ;
        }

        private long getItemCount() {
            if(BuildConfig.DEBUG){
                Log.i(TAG, "getItemCount");
            }
            int count = 0;
            try {
                final Cursor c = mProvider.getReadableDatabase().query(DumbModel.Table, new String[]{DumbModel.Id_Field}, null, null, null, null, null);
                try {
                    count = c.getCount();
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                } finally {
                    if (c != null)
                        c.close();
                }
            } catch (SQLiteException ex) {
                ex.printStackTrace();
            }
            if(BuildConfig.DEBUG){
                Log.i(TAG, "item count="+count);
            }
            return count;
        }

        private long addItem(final DumbModel item) {
            if(BuildConfig.DEBUG){
                Log.i(TAG, "add item, item content=" + item.getContent());
            }
            long id = -1;
            try {
                mProvider.getWritableDatabase().beginTransaction();
                try {
                    final ContentValues values = new ContentValues();
                    values.put(DumbModel.Content_Field, item.getContent());
                    values.put(DumbModel.MediaId_Field, item.getMediaId());
                    id = mProvider.getWritableDatabase().insert(DumbModel.Table, null, values);
                    mProvider.getWritableDatabase().setTransactionSuccessful();
                    item.setId(id);
                    final Cursor c=mProvider.getReadableDatabase().query(DumbModel.Table,
                            new String[]{DumbModel.CreatedAt_Field, DumbModel.ModifiedAt_Field}, DumbModel.Id_Field + "=" + String.valueOf(id), null, null, null, null);
                    try{
                        c.moveToFirst();
                        item.setCreatedAt(c.getString(0));
                        item.setModifiedAt(c.getString(1));
                    }catch (IndexOutOfBoundsException ex){
                        ex.printStackTrace();
                    }finally {
                        if(c!=null)
                            c.close();
                    }
                } catch (SQLiteException ex) {
                    ex.printStackTrace();
                } finally {
                    mProvider.getWritableDatabase().endTransaction();
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            return id;
        }

        private int removeItem(final DumbModel item) {
            if(BuildConfig.DEBUG){
                Log.i(TAG, "remove item, item content=" + item.getContent());
            }
            int count = -1;
            try {
                mProvider.getWritableDatabase().beginTransaction();
                try {
                    count = mProvider.getWritableDatabase().delete(DumbModel.Table, DumbModel.Id_Field+"="+String.valueOf(item.getId()), null);
                    mProvider.getWritableDatabase().setTransactionSuccessful();
                } catch (SQLiteException ex) {
                    ex.printStackTrace();
                } finally {
                    mProvider.getWritableDatabase().endTransaction();
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            return count;
        }

        private int modifyItem(final long id, final ContentValues values) {
            if(BuildConfig.DEBUG){
                Log.i(TAG, "modify item");
            }
            int count = -1;
            try {
                mProvider.getWritableDatabase().beginTransaction();
                try {
                    count = mProvider.getWritableDatabase().update(DumbModel.Table, values, DumbModel.Id_Field+"="+String.valueOf(id), null);
                    mProvider.getWritableDatabase().setTransactionSuccessful();
                } catch (SQLiteException ex) {
                    ex.printStackTrace();
                } finally {
                    mProvider.getWritableDatabase().endTransaction();
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            return count;
        }
        
        private DumbModel getItemById(long id){
            final DumbModel item=new DumbModel();
            item.setId(id);
            final Cursor c=mProvider.getReadableDatabase().query(DumbModel.Table,
                    new String[]{DumbModel.CreatedAt_Field, DumbModel.ModifiedAt_Field}, DumbModel.Id_Field + "=" + String.valueOf(id), null, null, null, null);
            try{
                c.moveToFirst();
                item.setCreatedAt(c.getString(0));
                item.setModifiedAt(c.getString(1));
            }catch (IndexOutOfBoundsException ex){
                ex.printStackTrace();
            }finally {
                if(c!=null)
                    c.close();
            }
            return item;
        }

        private void getAllDates(boolean order){
            try{
                String ord=" DESC";
                if(order){
                    ord=" ASC";
                }
                final Cursor c=mProvider.getReadableDatabase().query(true, DumbModel.Table,
                        new String[]{DumbModel.CreatedAt_Field},
                        null,null,null,null,DumbModel.CreatedAt_Field+ord, null);
                try {
                    final ArrayList<String> dates=new ArrayList<>(c.getCount());
                    if(c.getCount()>0) {
                        c.moveToFirst();
                        String last="";
                        do {
                            final String dateTime=DateTimeHelper
                                    .getLocalDateTimeFromString(c.getString(0),
                                            DateTimeHelper.DATABASEFORMAT, DateTimeHelper.DISPLAYFORMAT);
                            final String dateOnly=dateTime.substring(0, 8);
                            if(last.equals(dateOnly))
                                continue;
                            dates.add(dateTime);
                        } while (c.moveToNext());
                    }
                    if(order){
                        postMessage(Message.obtain(null, DataServiceMessages.Service_Get_All_Dates_ASC_Finished, -1, -1, dates), false);
                    }else {
                        postMessage(Message.obtain(null, DataServiceMessages.Service_Get_All_Dates_DESC_Finished, -1, -1, dates), false);
                    }
                }catch (IndexOutOfBoundsException ex){
                    ex.printStackTrace();
                }catch (SQLiteException ex){
                    ex.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                    c.close();
                }
            }catch (NullPointerException ex){
                ex.printStackTrace();
            }
        }

        private void getAllItems(boolean order){
            try{
                String ord=" DESC";
                if(order){
                    ord=" ASC";
                }
                final Cursor c=mProvider.getReadableDatabase().query(DumbModel.Table,
                        new String[]{DumbModel.Id_Field, DumbModel.Content_Field,
                                DumbModel.MediaId_Field, DumbModel.CreatedAt_Field, DumbModel.ModifiedAt_Field},
                        null,null,null,null,DumbModel.CreatedAt_Field+ord);
                try {
                    final List<DumbModel> models=new ArrayList<>();
                    if(c.getCount()>0) {
                        c.moveToFirst();
                        do {
                            final DumbModel model = new DumbModel(c.getLong(0));
                            model.setContent(c.getString(1));
                            model.setCreatedAt(DateTimeHelper
                                    .getLocalDateTimeFromString(c.getString(3),
                                            DateTimeHelper.DATABASEFORMAT, DateTimeHelper.DISPLAYFORMAT));
                            model.setMediaId(c.getLong(2));
                            model.setModifiedAt(DateTimeHelper
                                    .getLocalDateTimeFromString(c.getString(4),
                                            DateTimeHelper.DATABASEFORMAT, DateTimeHelper.DISPLAYFORMAT));
                            models.add(model);
                        } while (c.moveToNext());
                    }
                    if(order){
                        postMessage(Message.obtain(null, DataServiceMessages.Service_Get_All_Item_ASC_Finished, -1, -1, models), false);
                    }else {
                        postMessage(Message.obtain(null, DataServiceMessages.Service_Get_All_Item_DESC_Finished, -1, -1, models), false);
                    }
                }catch (IndexOutOfBoundsException ex){
                    ex.printStackTrace();
                }catch (SQLiteException ex){
                    ex.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                    c.close();
                }
            }catch (NullPointerException ex){
                ex.printStackTrace();
            }
        }
    });

    private Messenger mMessenger;
    private ContentProvider mProvider = ContentProvider.getInstance();

    public DataService() {
        mMessenger = new Messenger(mHandler);
    }

    private void postMessage(Message msg, boolean sticky) {
        if(sticky)
            mEventBus.postSticky(new AsyncMessage(msg));
        else
            mEventBus.post(new AsyncMessage(msg));
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mMessenger.getBinder();
    }
}
