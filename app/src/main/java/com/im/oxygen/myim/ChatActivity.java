package com.im.oxygen.myim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.im.oxygen.myim.adapter.MessageAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ChatActivity extends ActionBarActivity implements View.OnClickListener{

    private static final int MESSAGE_REFRESH = 1;
    public static final int CHAT = 1;
    public static final int GROUP_CHAT = 2;
    @InjectView(R.id.text)
    EditText mText;
    @InjectView(R.id.send)
    Button mSend;
    @InjectView(R.id.chat_list)
    ListView mChatList;

    MessageAdapter mMessageAdapter;
    NewMessageBroadcastReceiver msgReceiver;

    String username;
    EMGroup group;
    int chatType;

    Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_REFRESH:
                    mMessageAdapter.refresh();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.inject(this);

        msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        intentFilter.setPriority(3);
        registerReceiver(msgReceiver, intentFilter);

        chatType = getIntent().getIntExtra("chatType", CHAT);
        if (chatType == CHAT) {
            username = getIntent().getStringExtra("username");
            getSupportActionBar().setTitle(username);
        } else if (chatType == GROUP_CHAT) {
            username = getIntent().getStringExtra("groupId");
            group = EMGroupManager.getInstance().getGroup(username);
            getSupportActionBar().setTitle(group.getGroupName());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMessageAdapter = new MessageAdapter(this, username);

        mChatList.setAdapter(mMessageAdapter);
        mChatList.setSelection(mChatList.getCount() - 1);
        mSend.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send:
                String text = mText.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                    message.setReceipt(username);
                    TextMessageBody textMessageBody = new TextMessageBody(text);
                    message.addBody(textMessageBody);
                    if (chatType == GROUP_CHAT) {
                        message.setChatType(EMMessage.ChatType.GroupChat);
                    }
                    try {
                        EMChatManager.getInstance().sendMessage(message);
                        mText.setText("");
                        mMessageHandler.sendEmptyMessage(MESSAGE_REFRESH);
                    } catch (EaseMobException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(msgReceiver);
    }

    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 记得把广播给终结掉
            abortBroadcast();
            //消息id
            String msgId = intent.getStringExtra("msgid");
            //发消息的人的username(userid)
            String msgFrom = intent.getStringExtra("from");
            //消息类型，文本，图片，语音消息等,这里返回的值为msg.type.ordinal()。
            //所以消息type实际为是enum类型
            int msgType = intent.getIntExtra("type", 0);
            //更方便的方法是通过msgId直接获取整个message
            EMMessage message = EMChatManager.getInstance().getMessage(msgId);
            // 如果是群聊消息，获取到group id
            if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                username = message.getTo();
            }
            if (!username.equals(username)) {
                // 消息不是发给当前会话，return
                return;
            }
            Log.d("ChatActivity", "new message id:" + msgId + " from:" + msgFrom + " type:" + msgType);

            Log.d("ChatActivity", "message content:" + ((TextMessageBody) message.getBody()).getMessage());
            mMessageHandler.sendEmptyMessage(MESSAGE_REFRESH);
        }
    }
}
