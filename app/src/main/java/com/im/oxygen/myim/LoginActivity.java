package com.im.oxygen.myim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LoginActivity extends ActionBarActivity {


    @InjectView(R.id.username)
    EditText mUsername;
    @InjectView(R.id.password)
    EditText mPassword;
    @InjectView(R.id.login)
    Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        if (!sharedPreferences.getString("username","").equals("")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        }

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EMChatManager.getInstance().login(mUsername.getText().toString(),
                        mPassword.getText().toString(), new EMCallBack() {
                            @Override
                            public void onSuccess() {
                                SharedPreferences sharedPreferences =
                                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", mUsername.getText().toString());
                                editor.putString("password", mPassword.getText().toString());
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                LoginActivity.this.finish();
                            }

                            @Override
                            public void onError(int i, String s) {

                            }

                            @Override
                            public void onProgress(int i, String s) {

                            }
                        });
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
