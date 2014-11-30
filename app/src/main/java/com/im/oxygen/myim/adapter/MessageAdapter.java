package com.im.oxygen.myim.adapter;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.im.oxygen.myim.ChatActivity;
import com.im.oxygen.myim.R;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MessageAdapter extends BaseAdapter {

    private Context mContext;
    private String mUsername;
    private Activity mActivity;
    private LayoutInflater inflater;
    MediaPlayer mediaPlayer;


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
    public int getItemViewType(int position) {
        Log.d("message adapter","get item view type position:" + position);
        EMMessage message = emConversation.getMessage(position);
        if (EMMessage.Direct.RECEIVE == message.direct) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final EMMessage message = emConversation.getMessage(i);
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
        if (message.getType() == EMMessage.Type.TXT) {
            String messageText = ((TextMessageBody) message.getBody()).getMessage();
            Log.d("message adapter",messageText + " from " + message.getFrom() +
                    " : " + message.direct.name());

            viewHolder.mMessage.setText(messageText);
            viewHolder.mUsername.setText(message.getFrom());
        } else if (message.getType() == EMMessage.Type.VOICE) {
            VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
            final String filePath = voiceBody.getLocalUrl();
            viewHolder.mMessage.setText("语音！！！");
            viewHolder.mMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(new File(filePath).exists())) {
                        return;
                    }
                    ((ChatActivity) mActivity).playMsgId = message.getMsgId();
                    AudioManager audioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);

                    mediaPlayer = new MediaPlayer();
                    if (EMChatManager.getInstance().getChatOptions().getUseSpeaker()) {
                        audioManager.setMode(AudioManager.MODE_NORMAL);
                        audioManager.setSpeakerphoneOn(true);
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                    } else {
                        audioManager.setSpeakerphoneOn(false);// 关闭扬声器
                        // 把声音设定成Earpiece（听筒）出来，设定为正在通话中
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                    }
                    try {
                        mediaPlayer.setDataSource(filePath);
                        mediaPlayer.prepare();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                // TODO Auto-generated method stub
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }

                        });
                        mediaPlayer.start();

                    } catch (Exception e) {
                    }
                }
            });

        }


        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.message)
        TextView mMessage;
        @InjectView(R.id.username)
        TextView mUsername;
        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
