package com.im.oxygen.myim.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.im.oxygen.myim.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MessageAdapter extends BaseAdapter {

    private Context mContext;
    private String mUsername;
    private Activity mActivity;
    private LayoutInflater inflater;

    private EMConversation emConversation;

    public MessageAdapter(Context context, String username) {
        this.mContext = context;
        this.mActivity = (Activity) context;
        this.inflater = LayoutInflater.from(context);
        this.mUsername = username;

        this.emConversation = EMChatManager.getInstance().getConversation(username);

    }

    public void refresh(){
        this.emConversation = EMChatManager.getInstance().getConversation(mUsername);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return emConversation.getMsgCount();
    }

    @Override
    public Object getItem(int i) {
        return emConversation.getMessage(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        EMMessage message = emConversation.getMessage(i);
        ViewHolder viewHolder;
        if (view == null) {
            if (message.direct == EMMessage.Direct.RECEIVE) {
                view = inflater.inflate(R.layout.receive_message, null);
            } else {
                view = inflater.inflate(R.layout.send_message, null);
            }
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.mMessage.setText(((TextMessageBody) message.getBody()).getMessage());

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.message)
        TextView mMessage;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
