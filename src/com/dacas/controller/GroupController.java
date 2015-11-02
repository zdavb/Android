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
	 * 获取建组表SQL语句
	 * @return SQL语句
	 */
	public static String getGroupSQLString(){
		return "create table "+tableName+"(id integer primary key autoincrement,"
				+ "name varchar,"
				+ "priority integer)";//设定优先级
	}
	/**
	 * 默认包含全部联系人、常用联系人、未分组联系人两种
	 * @return
	 */
	public static String initGroup(){
		//默认包含未分组联系人和常用联系人
		//将未分组联系人的优先级定为最高1
		//常用联系人次之
		//用户自定义联系人分组再次之
		return "insert into "+tableName+"(id,name,priority) values (NULL,'全部联系人',0),(NULL,'未分组联系人',1),(NULL,'常用联系人',2)";
	}
	public long addNewGroup(String group){
		List<String> names = new LinkedList<String>();
		names.add("name");
		names.add("priority");
		List<String> values = new LinkedList<String>();
		values.add(group);
		values.add(USER_PRORITY);//默认用户分组为3
		
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
	 * 更新group的name
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
	 * 删除分组，以及该分组下所有的元素
	 * @param group
	 * @return
	 */
	public boolean deleteGroup(ContactGroupModel group){
		List<String> names = new LinkedList<String>();
		names.add("id");
		List<String> values = new LinkedList<String>();
		values.add(String.valueOf(group.id));
		
		int nums = manager.deleteFromTable(tableName, names, values);
		if(nums == 1){//删除成功
			//删除关联表
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
