package com.im.oxygen.myim;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.VoiceRecorder;
import com.im.oxygen.myim.adapter.MessageAdapter;

import java.io.File;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ChatActivity extends ActionBarActivity implements View.OnClickListener {

    private static final int MESSAGE_REFRESH = 1;
    public static final int CHAT = 1;
    public static final int GROUP_CHAT = 2;
    @InjectView(R.id.text)
    EditText mText;
    @InjectView(R.id.send)
    Button mSend;
    @InjectView(R.id.chat_list)
    ListView mChatList;
    @InjectView(R.id.expand)
    Button mExpand;
    @InjectView(R.id.voice)
    Button mVoice;
    @InjectView(R.id.get_name_card)
    Button mSendNameCard;
    @InjectView(R.id.button_container)
    LinearLayout mButtonContainer;

    boolean recordFlag;
    VoiceRecorder voiceRecorder;

    MessageAdapter mMessageAdapter;
    NewMessageBroadcastReceiver msgReceiver;
    CmdMessageBroadcastReceiver cmdMessageBroadcastReceiver;

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
    public String playMsgId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.inject(this);

        msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        intentFilter.setPriority(3);
        registerReceiver(msgReceiver, intentFilter);

        cmdMessageBroadcastReceiver = new CmdMessageBroadcastReceiver();
        IntentFilter cmdFilter = new IntentFilter(EMChatManager.getInstance().getCmdMessageBroadcastAction());
        intentFilter.setPriority(3);
        registerReceiver(cmdMessageBroadcastReceiver, cmdFilter);

        chatType = getIntent().getIntExtra("chatType", CHAT);
        if (chatType == CHAT) {
            username = getIntent().getStringExtra("username");
            getSupportActionBar().setTitle(username);
        } else if (chatType == GROUP_CHAT) {
            username = getIntent().getStringExtra("groupId");
            group = EMGroupManager.getInstance().getGroup(username);
            getSupportActionBar().setTitle(group.getGroupName());
            //群聊不能要求发送名片
            mSendNameCard.setVisibility(View.GONE);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMessageAdapter = new MessageAdapter(this, username);

        mChatList.setAdapter(mMessageAdapter);
        mChatList.setSelection(mChatList.getCount() - 1);

        mSend.setOnClickListener(this);
        mExpand.setOnClickListener(this);
        mVoice.setOnClickListener(this);
        mSendNameCard.setOnClickListener(this);

        recordFlag = false;
        voiceRecorder = new VoiceRecorder(new Handler(){});
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
            case R.id.expand:
                if (mButtonContainer.getVisibility() == View.VISIBLE) {
                    mButtonContainer.setVisibility(View.GONE);
                } else if (mButtonContainer.getVisibility() == View.GONE) {
                    mButtonContainer.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.voice:
                if (!recordFlag) {
                    try {
                        voiceRecorder.startRecording(null, username, getApplicationContext());
                        recordFlag = !recordFlag;
                    } catch (Exception e) {
                        Toast.makeText(ChatActivity.this, "发送失败，请检测服务器是否连接", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    try {
                        int length = voiceRecorder.stopRecoding();
                        if (length > 0) {
                            sendVoice(voiceRecorder.getVoiceFilePath(), voiceRecorder.getVoiceFileName(username),
                                    Integer.toString(length), false);
                        } else if (length == EMError.INVALID_FILE) {
                            Toast.makeText(getApplicationContext(), "无录音权限", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "录音时间太短", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ChatActivity.this, "发送失败，请检测服务器是否连接", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.get_name_card:
                getNameCard();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(msgReceiver);
    }

    /**
     * 发送语音
     */
    private void sendVoice(String filePath,String fileName, String length, boolean some) {
        if (!(new File(filePath).exists())) {
            return;
        }
        try {
            final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.VOICE);
            // 如果是群聊，设置chattype,默认是单聊
            if (chatType == GROUP_CHAT)
                message.setChatType(EMMessage.ChatType.GroupChat);
            message.setReceipt(username);
            int len = Integer.parseInt(length);
            VoiceMessageBody body = new VoiceMessageBody(new File(filePath), len);
            message.addBody(body);
            EMChatManager.getInstance().sendMessage(message);

            mMessageHandler.sendEmptyMessage(MESSAGE_REFRESH);
            mChatList.setSelection(mChatList.getCount() - 1);
            setResult(RESULT_OK);
            // send file
            // sendVoiceSub(filePath, fileName, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getNameCard(){
        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.CMD);
        message.setReceipt(username);
        CmdMessageBody cmdMessageBody = new CmdMessageBody("name_card");
        message.addBody(cmdMessageBody);
        try {
            EMChatManager.getInstance().sendMessage(message);
            mMessageHandler.sendEmptyMessage(MESSAGE_REFRESH);
            mChatList.setSelection(mChatList.getCount() - 1);
        } catch (EaseMobException e) {
            e.printStackTrace();
        }

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

    private class CmdMessageBroadcastReceiver extends BroadcastReceiver {
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
            EMMessage message = intent.getParcelableExtra("message");
            if (!username.equals(username)) {
                // 消息不是发给当前会话，return
                return;
            }

            CmdMessageBody cmdMessageBody = (CmdMessageBody) message.getBody();
            if (cmdMessageBody.action.equals("name_card")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder
                        .setTitle("对方希望您介绍自己")
                        .setMessage("对方希望您介绍自己，发送卡片给对方")
                        .setPositiveButton("发送", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences sharedPreferences =
                                        PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                                String myName = sharedPreferences.getString("username","");
                                EMMessage sendMessage = EMMessage.createSendMessage(EMMessage.Type.TXT);
                                sendMessage.setReceipt(username);
                                TextMessageBody textMessageBody = new TextMessageBody("您好，我是 " + myName);
                                sendMessage.addBody(textMessageBody);

                                try {
                                    EMChatManager.getInstance().sendMessage(sendMessage);
                                } catch (EaseMobException e) {
                                    e.printStackTrace();
                                }
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
            Log.d("ChatActivity", "new message id:" + msgId + " from:" + msgFrom + " type:" + msgType);

//            Log.d("ChatActivity", "message content:" + ((TextMessageBody) message.getBody()).getMessage());
            mMessageHandler.sendEmptyMessage(MESSAGE_REFRESH);
        }
    }
}
