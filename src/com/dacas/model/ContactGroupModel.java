package com.dacas.model;

import java.util.List;

public class ContactGroupModel {
	public int id;//id
	public String name;//����
	public int priority;
	public List<ContactModel> contacts;//����������ϵ��
	
	public String toString(){
		return "id="+id+"&name="+name+"&priority="+priority;
	}
}
