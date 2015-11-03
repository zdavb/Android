package com.dacas.controller;

import java.util.LinkedList;
import java.util.List;

import com.dacas.localdb.DBManager;
import com.dacas.model.ContactModel;

import android.content.Context;
import android.database.Cursor;

public class ContactGroupController {
	
	public static String tableName = "contactgroup";
	public Context context;
	public DBManager manager;
	/**
	 * 获取创建关联表的SQL语句
	 * @return
	 */
	public static String getGroupAndContactSQLString(){
		return "create table "+tableName+"(id integer primary key autoincrement,contactid integer,groupid integer)";
	}
	
	/**
	 * 将所有联系人都插入到全部联系人列表以及未分组列表
	 * @param cursor:传入联系人列表中的全部联系人
	 * @return SQL语句
	 */
	public static String initContactGroup(Cursor cursor){
		if(cursor==null)
			return "";
		
		StringBuilder builder = new StringBuilder();
		builder.append("insert into "+tableName+"(id,contactid,groupid) values");
		
		while(cursor.moveToNext()){
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			builder.append("(NULL,"+id+",1),");//全部联系人
			builder.append("(NULL,"+id+",2),");//未分组联系人
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}
	public ContactGroupController(Context context){
		this.context = context;
		this.manager = DBManager.getInstance(context);
	}
	/**
	 * 获取每个分组所包含的用户ID
	 * @param context
	 * @param groupId
	 * @return
	 */
	public List<Integer> getContacts(int groupId){
		List<Integer> contactIds;
		
		List<String> names = new LinkedList<String>();
		List<String> values = new LinkedList<String>();
		names.add("groupid");
		values.add(String.valueOf(groupId));
		
		Cursor cursor = manager.selectFromTable(tableName, names, values, null, "id");
		contactIds = restoreObjectFromCursor(cursor);
		cursor.close();
		return contactIds;
	}
	
	public int delete(int groupId,int contactId){
		List<String> names = new LinkedList<String>();
		names.add("groupid");
		names.add("contactid");
		List<String> values = new LinkedList<String>();
		values.add(String.valueOf(groupId));
		values.add(String.valueOf(contactId));
		
		return manager.deleteFromTable(tableName, names, values);
	}

	public boolean insert(int groupId,int contactId){
		List<String> names = new LinkedList<String>();
		List<String> values = new LinkedList<String>();
		
		names.add("groupid");
		names.add("contactid");
		values.add(String.valueOf(groupId));
		values.add(String.valueOf(contactId));
		
		long rowId =  manager.insertIntoTable(tableName, names, values);
		if(rowId == -1)
			return false;
		return true;
	}
	public List<Integer> restoreObjectFromCursor(Cursor cursor){
		List<Integer> res = new LinkedList<Integer>();
		if(cursor == null)
			return res;
		
		while(cursor.moveToNext()){
			int id = cursor.getInt(cursor.getColumnIndex("contactid"));
			res.add(id);
		}
		return res;
	}
}
