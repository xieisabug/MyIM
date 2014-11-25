package com.im.oxygen.myim;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class AddContactActivity extends ActionBarActivity implements View.OnClickListener{

    @InjectView(R.id.keyword)
    EditText mKeyword;
    @InjectView(R.id.add)
    Button mAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        ButterKnife.inject(this);

        mAdd.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_contact, menu);
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
            case R.id.add:
                try {
                    EMContactManager.getInstance().addContact(mKeyword.getText().toString(), "加我加我～");
                    Toast.makeText(AddContactActivity.this, "添加成功，请等待对方同意", Toast.LENGTH_SHORT).show();
                    this.finish();
                } catch (EaseMobException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
