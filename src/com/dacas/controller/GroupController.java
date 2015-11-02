package com.dacas.controller;

import java.util.LinkedList;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.database.Cursor;

import com.dacas.localdb.DBManager;
import com.dacas.model.ContactGroupModel;

public class GroupController {
	public static String tableName = "groups";
	public static String USER_PRORITY = "3";
	
	Context context;
	DBManager manager;
	public GroupController(Context context) {
		this.context = context;
		this.manager = new DBManager(context);
	}
	/**
	 * ��ȡ�����SQL���
	 * @return SQL���
	 */
	public static String getGroupSQLString(){
		return "create table "+tableName+"(id integer primary key autoincrement,"
				+ "name varchar,"
				+ "priority integer)";//�趨���ȼ�
	}
	/**
	 * Ĭ�ϰ���ȫ����ϵ�ˡ�������ϵ�ˡ�δ������ϵ������
	 * @return
	 */
	public static String initGroup(){
		//Ĭ�ϰ���δ������ϵ�˺ͳ�����ϵ��
		//��δ������ϵ�˵����ȼ���Ϊ���1
		//������ϵ�˴�֮
		//�û��Զ�����ϵ�˷����ٴ�֮
		return "insert into "+tableName+"(id,name,priority) values (NULL,'ȫ����ϵ��',0),(NULL,'δ������ϵ��',1),(NULL,'������ϵ��',2)";
	}
	public long addNewGroup(String group){
		List<String> names = new LinkedList<String>();
		names.add("name");
		names.add("priority");
		List<String> values = new LinkedList<String>();
		values.add(group);
		values.add(USER_PRORITY);//Ĭ���û�����Ϊ3
		
		return manager.insertIntoTable(tableName,names, values);
	}
	
	public List<ContactGroupModel> getAllGroups(){
		Cursor cursor = manager.selectAllFromTable(tableName, null, null);
		List<ContactGroupModel> res = restoreObjectFromCursor(cursor);
		cursor.close();
		return res;
	}
	
	public List<ContactGroupModel> restoreObjectFromCursor(Cursor cursor){
		List<ContactGroupModel> list = new LinkedList<ContactGroupModel>();
		if(cursor == null)
			return list;
		while(cursor.moveToNext()){
			ContactGroupModel groupModel = new ContactGroupModel();
			groupModel.id = cursor.getInt(cursor.getColumnIndex("id"));
			groupModel.name = cursor.getString(cursor.getColumnIndex("name"));
			groupModel.priority = cursor.getInt(cursor.getColumnIndex("priority"));
			
			list.add(groupModel);
		}
		return list;
		
	}
	/**
	 * ����group��name
	 * @param group
	 * @return
	 */
	public boolean updateGroupName(ContactGroupModel group){
		List<String> names = new LinkedList<String>();
		List<String> values = new LinkedList<String>();
		
		names.add("name");
		values.add(group.name);
		int rows = manager.updateTable(tableName, names, values, String.valueOf(group.id));
		if(rows != 1)
			return false;
		return true;
	}
	/**
	 * ɾ�����飬�Լ��÷��������е�Ԫ��
	 * @param group
	 * @return
	 */
	public boolean deleteGroup(ContactGroupModel group){
		List<String> names = new LinkedList<String>();
		names.add("id");
		List<String> values = new LinkedList<String>();
		values.add(String.valueOf(group.id));
		
		int nums = manager.deleteFromTable(tableName, names, values);
		if(nums == 1){//ɾ���ɹ�
			//ɾ��������
			names.clear();
			values.clear();
			names.add("groupid");
			values.add(String.valueOf(group.id));
			nums = manager.deleteFromTable(ContactGroupController.tableName, names, values);
			if(nums != -1)
				return true;
			return false;
		}else {
			return false;
		}
	}
}
