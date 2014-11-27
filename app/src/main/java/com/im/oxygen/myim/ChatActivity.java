package com.im.oxygen.myim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.im.oxygen.myim.adapter.MessageAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ChatActivity extends ActionBarActivity implements View.OnClickListener{

    @InjectView(R.id.text)
    EditText mText;
    @InjectView(R.id.send)
    Button mSend;
    @InjectView(R.id.chat_list)
    ListView mChatList;

    MessageAdapter mMessageAdapter;

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.inject(this);

        NewMessageBroadcastReceiver msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        intentFilter.setPriority(3);
        registerReceiver(msgReceiver, intentFilter);
        username = getIntent().getStringExtra("username");
        getSupportActionBar().setTitle(username);

        mMessageAdapter = new MessageAdapter(this, username);

        mChatList.setAdapter(mMessageAdapter);
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
        if (id == R.id.action_settings) {
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
                    try {
                        EMChatManager.getInstance().sendMessage(message);
                        mText.setText("");
                    } catch (EaseMobException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //消息id
            String msgId = intent.getStringExtra("msgid");
            //发消息的人的username(userid)
            String msgFrom = intent.getStringExtra("from");
            //消息类型，文本，图片，语音消息等,这里返回的值为msg.type.ordinal()。
            //所以消息type实际为是enum类型
            int msgType = intent.getIntExtra("type", 0);
            Log.d("ChatActivity", "new message id:" + msgId + " from:" + msgFrom + " type:" + msgType);
            //更方便的方法是通过msgId直接获取整个message
            EMMessage message = EMChatManager.getInstance().getMessage(msgId);
            Log.d("ChatActivity", "message content:" + ((TextMessageBody) message.getBody()).getMessage());

            mMessageAdapter.notifyDataSetChanged();
        }
    }
}
