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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.lunci.dumbthing.service.DataService;

import de.greenrobot.event.EventBus;

/**
 * Created by Lunci on 2/2/2015.
 */
public abstract class ServiceFragmentBase extends Fragment implements ServiceConnection {
    private static String TAG=ServiceFragmentBase.class.getSimpleName();
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceMessenger = new Messenger(service);
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceMessenger = null;
    }

    private Messenger mServiceMessenger;

    private boolean mBound = false;

    private EventBus mEventBus=EventBus.getDefault();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventBus.register(this);
    }

    @Override
    public void onPause() {
        mEventBus.unregister(this);
        super.onPause();
    }

    @SuppressWarnings("unused")
    protected boolean sendMessageToService(Message msg) {
        if (mServiceMessenger == null)
            return false;
        msg.replyTo = mServiceMessenger;
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!mBound) {
            Intent intent = new Intent(activity, DataService.class);
            activity.getApplicationContext().bindService(intent, this,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDetach() {
        if (mBound) {
            getActivity().getApplicationContext().unbindService(this);
            mBound = false;
        }
        super.onDetach();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!super.onOptionsItemSelected(item)) {
            return getActivity().onOptionsItemSelected(item);
        } else
            return true;
    }

    protected EventBus getEventBus(){
        return mEventBus;
    }
    
    public abstract Handler getHandler();

    @SuppressWarnings("unused")
    public void onEventMainThread(Message msg){
        getHandler().dispatchMessage(msg);
    }
}
