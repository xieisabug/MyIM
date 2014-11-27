package com.im.oxygen.myim;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class CreateGroupActivity extends ActionBarActivity {

    @InjectView(R.id.name)
    EditText mName;
    @InjectView(R.id.desc)
    EditText mDesc;
    @InjectView(R.id.submit)
    Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.inject(this);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EMGroup emGroup = EMGroupManager.getInstance().createPublicGroup(mName.getText().toString(),
                            mDesc.getText().toString(), new String[]{}, false);
                    if (emGroup == null) {
                        Toast.makeText(CreateGroupActivity.this, "创建群组失败", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CreateGroupActivity.this, "创建成功", Toast.LENGTH_SHORT).show();
                        CreateGroupActivity.this.finish();
                    }
                } catch (EaseMobException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
