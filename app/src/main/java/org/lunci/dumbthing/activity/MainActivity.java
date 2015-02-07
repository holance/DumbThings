package org.lunci.dumbthing.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;
import org.lunci.dumbthing.dataModel.GlobalMessages;
import org.lunci.dumbthing.dialog.LinkShareDialog;
import org.lunci.dumbthing.fragment.FunctionFragment;
import org.lunci.dumbthing.fragment.ItemListFragment;
import org.lunci.dumbthing.fragment.MainDisplayFragment;
import org.lunci.dumbthing.fragment.MainFragment;
import org.lunci.dumbthing.service.AsyncMessage;
import org.lunci.dumbthing.util.AutoShareManager;
import org.lunci.dumbthing.util.Utils;

import de.greenrobot.event.EventBus;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private EventBus mEventBus = EventBus.getDefault();
    private AutoShareManager mAutoShareManager;
    private Handler mHandler = new Handler(new android.os.Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean succ=false;
            switch (msg.what){
                case GlobalMessages.MESSAGE_SHOW_ALL_DUMBS:
                    Log.i(TAG, "on handle MESSAGE_SHOW_ALL_DUMBS");
                    final Fragment mDumbListFragment=ItemListFragment.newInstance();
                    FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.frameLayout_main, mDumbListFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            
                    final Fragment mFunctionFragment= FunctionFragment.newInstance();
                    transaction.replace(R.id.frameLayout_info, mFunctionFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    
                    transaction.addToBackStack(null).commit();
                    succ=true;
                    break;
            }
            return succ;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            final Fragment mFragment = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, mFragment).commit();
            final Fragment mInfoFragment = MainDisplayFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_info, mInfoFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().commit();
        }
        mAutoShareManager=new AutoShareManager(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Utils.startSettingActivity(this);
            return true;
        }else if(id==R.id.action_about){
            final Intent intent=new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }else if(id==R.id.action_link){
            LinkShareDialog dialog=new LinkShareDialog();
            dialog.show(getSupportFragmentManager(), LinkShareDialog.class.getSimpleName());
        }
        return super.onOptionsItemSelected(item);
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

    public void onEventAsync(AsyncMessage msg) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onEventAsync, what=" + msg.getMessage().what);
        }
        mEventBus.post(msg.getMessage());
        msg.dispose();
    }
    
    public void onEventMainThread(Message msg){
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onEventMainThread, what=" + msg.what);
        }
        mHandler.dispatchMessage(msg);
    }

    public void onEventMainThread(GlobalMessages.PostContent content){
      //  mAutoShareManager.publishStoryOnFacebook(content.Content);
        mAutoShareManager.publishStoryOnTwitter(content.Content);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        final Fragment linkShareFragment= getSupportFragmentManager().findFragmentByTag(LinkShareDialog.class.getSimpleName());
        if(linkShareFragment!=null){
            linkShareFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
