package com.im.oxygen.myim;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class GroupChatActivity extends ActionBarActivity {

    @InjectView(R.id.expand)
    Button mExpand;
    @InjectView(R.id.text)
    EditText mText;
    @InjectView(R.id.send)
    Button mSend;
    @InjectView(R.id.input_container)
    LinearLayout mInputContainer;
    @InjectView(R.id.chat_list)
    ListView mChatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.send)
    void send(View view) {
        String text = mText.getText().toString();
        EMMessage emMessage = EMMessage.createSendMessage(EMMessage.Type.TXT);
        emMessage.setChatType(EMMessage.ChatType.GroupChat);
        emMessage.setReceipt("");
        TextMessageBody textMessageBody = new TextMessageBody(text);
        emMessage.addBody(textMessageBody);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_chat, menu);
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
}
