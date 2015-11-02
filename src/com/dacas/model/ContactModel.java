package com.dacas.model;

//联系人Model
public class ContactModel {
	public int id;//ID
	public String name;//姓名
	public String office;//单位
	public String phone;//电话
	public String agency;//路局
	public String department;//电报所
	
	@Override
	public String toString() {
		return "id="+id+"&name="+name+"&office="+office+"&phone="+phone+"&agency="+agency+"&department="+department;
	}
}
