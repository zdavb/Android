package com.dacas.model;

//��ϵ��Model
public class ContactModel {
	public int id;//ID
	public String name;//����
	public String office;//��λ
	public String phone;//�绰
	public String agency;//·��
	public String department;//�籨��
	
	@Override
	public String toString() {
		return "id="+id+"&name="+name+"&office="+office+"&phone="+phone+"&agency="+agency+"&department="+department;
	}
}
