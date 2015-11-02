package com.dacas.loginHelper;

public class LoginInfo {
	public static int USER_LOGIN = 1;
	public static int DEVICE_LOGIN = 2;
	
	boolean result = false;
	public LoginInfo(int type,String name,String passwd){
		if(type == USER_LOGIN){//�û���¼
			//���ߵ�¼
			LoginOnline loginOnline = new LoginOnline(name,passwd);
			result |= loginOnline.getResult();
			LoginOffline loginOffline = new LoginOffline(name,passwd);
			result |= loginOffline.getResult();
		}else if(type == DEVICE_LOGIN){//�豸��¼
		}
	}
	public boolean getResult() {
		return result;
	}
}
