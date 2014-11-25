package com.im.oxygen.myim;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMNotifier;
import com.easemob.exceptions.EaseMobException;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    String TAG = "MainActivity";

    @InjectView(R.id.contact_list)
    ListView mContactList;
    @InjectView(R.id.add)
    Button mAdd;

    List<String> usernames;
    private ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        // ** 第一次登录或者之前logout后，加载所有本地群和回话
        // ** manually load all local groups and
        // conversations in case we are auto login
        EMGroupManager.getInstance().loadAllGroups();
        EMChatManager.getInstance().loadAllConversations();
        // demo中简单的处理成每次登陆都去获取好友username，开发者自己根据情况而定
        try {
            getContactList();
            for (String username : usernames) {
                Log.d("MainActivity", username);
            }
            contactAdapter = new ContactAdapter(usernames);
            mContactList.setAdapter(contactAdapter);
            mContactList.setOnItemClickListener(this);
        } catch (EaseMobException e) {
            e.printStackTrace();
        }

        EMContactManager.getInstance().setContactListener(new MyContactListener());

        mAdd.setOnClickListener(this);
        EMChat.getInstance().setAppInited();
    }

    private void getContactList() throws EaseMobException {
        usernames = EMContactManager.getInstance().getContactUserNames();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("username", this.usernames.get(i));
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add:
                Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
                startActivity(intent);
                break;
        }
    }

    class MyContactListener implements EMContactListener {
        @Override
        public void onContactAdded(List<String> strings) {
            Log.d(TAG, "added");
            try {
                getContactList();
                contactAdapter.notifyDataSetChanged();
            } catch (EaseMobException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onContactDeleted(List<String> strings) {

        }

        @Override
        public void onContactInvited(String s, String s2) {
            Log.d(TAG, "invited");
            Log.d(TAG, "S:"+s + " ; s2:" + s2);
            EMNotifier.getInstance(getApplicationContext()).notifyOnNewMsg();
        }

        @Override
        public void onContactAgreed(String s) {
            Log.d(TAG, "added");
            try {
                getContactList();
                contactAdapter.notifyDataSetChanged();
            } catch (EaseMobException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onContactRefused(String s) {

        }
    }

    class ContactAdapter extends BaseAdapter {

        private List<String> contactList;

        public ContactAdapter(List<String> contactList) {
            this.contactList = contactList;
        }

        @Override
        public int getCount() {
            return this.contactList.size();
        }

        @Override
        public Object getItem(int i) {
            return this.contactList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = View.inflate(MainActivity.this, R.layout.row_contact, null);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.mUsername.setText(this.contactList.get(i));

            return view;
        }

        class ViewHolder {
            @InjectView(R.id.username)
            TextView mUsername;

            ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }
    }
}
