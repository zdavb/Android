package com.dacas.model;

import java.util.List;

public class ContactGroupModel {
	public int id;//id
	public String name;//组名
	public int priority;
	public List<ContactModel> contacts;//所包含的联系人
	
	public String toString(){
		return "id="+id+"&name="+name+"&priority="+priority;
	}
}
