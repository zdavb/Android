package com.dacas.loginHelper;

public class LoginOffline {
	boolean result = false;
	LoginOffline(String name,String passwd){
		result |= searchInLocalDB(name, passwd);
		if(name.equals("admin") && passwd.equals("123456"))
			result|=true;
	}
	private boolean searchInLocalDB(String name,String passwd){
		return false;
	}
	public boolean getResult(){
		return result;
	}
}
