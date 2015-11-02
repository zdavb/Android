package com.dacas.telegram;

import com.dacas.loginHelper.LoginInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener{
	EditText username;
	EditText passwd;
	
	private static String TAG;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		TAG = getClass().getSimpleName();
		
		Button loginBtn = (Button)findViewById(R.id.loginbtn);
		username = (EditText)findViewById(R.id.username);
		passwd = (EditText)findViewById(R.id.passwd);
		loginBtn.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		String name = username.getText().toString();
		String pass = passwd.getText().toString();
		
		if(name.length() == 0 || pass.length() == 0){
			Toast.makeText(LoginActivity.this, "用户名或密码为空，请检查后重试", Toast.LENGTH_SHORT).show();
			return;
		}
		if(v.getId() == R.id.loginbtn){
			LoginInfo info = new LoginInfo(LoginInfo.USER_LOGIN,name,pass);
			if(!info.getResult()){
				Toast.makeText(LoginActivity.this, "登录失败，请检查后重试", Toast.LENGTH_SHORT).show();
				return;
			}
			//启动新的activity
			Toast.makeText(LoginActivity.this, "登录成功，将启动新的activity", Toast.LENGTH_SHORT).show();
			//Intent intent = new Intent(LoginActivity.this,ContactActivity.class);
			Intent intent = new Intent(LoginActivity.this,ContactGroupActivity.class);
			startActivity(intent);
		}
	}
}
