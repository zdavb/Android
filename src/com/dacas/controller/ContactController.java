package com.dacas.controller;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.text.GetChars;
import android.util.Log;

import com.dacas.localdb.DBManager;
import com.dacas.model.ContactModel;

public class ContactController {
	public static String tableName = "contact";
	public Context context;
	DBManager manager;

	/**
	 * ��ȡ�������
	 * @return SQL
	 */
	public static String getSQLString(){
		String SQL  = "create table "+tableName+"("
				+ "id integer primary key autoincrement,"
				+ "name varchar,"
				+ "office varchar,"
				+ "phone varchar,"
				+ "agency varchar,"
				+ "department varchar)";
		return SQL;
	}
	/**
	 * �����������
	 * @return �����������SQL���
	 */
	public static String insertTestDataToDB(){
		ContactModel[] contacts = new ContactModel[10];
		//init contacts
		for(int i = 0;i<10;i++){
			contacts[i] = new ContactModel();
			contacts[i].id = i;
			contacts[i].name = "contact"+i;
			contacts[i].office = "contactOffice"+i;
			contacts[i].phone = "110"+i;
			contacts[i].agency = "agency"+i;
			contacts[i].department = "department"+i;
		}
		//contruct sql 
		StringBuilder builder = new StringBuilder();
		builder.append("insert into "+tableName+ " (id,name,office,phone,agency,department) values ");
		
		for(int i = 0;i<10;i++){
			String format = String.format("(NULL,'%s','%s','%s','%s','%s'),", contacts[i].name,contacts[i].office,contacts[i].phone,contacts[i].agency,contacts[i].department);
			builder.append(format);
		}
		builder.deleteCharAt(builder.length()-1);//ɾ�����һ��������
		return builder.toString();
	}
	
	public ContactController(Context context){
		this.context = context;
		this.manager = new DBManager(context);
	}
	/**
	 * ��ȡȫ����ϵ��
	 * @return
	 */
	public List<ContactModel> getAllContacts(){
		Cursor cursor = manager.selectAllFromTable(tableName, null, null);
		return restoreObjectFromCursor(cursor);
	}
	/**
	 * ����ѯ�ַ������ع�ΪcontactModel����
	 * @param cursor
	 * @return �����б�
	 */
	public  List<ContactModel> restoreObjectFromCursor(Cursor cursor){
		if(cursor == null){
			Log.e("ContactController=>restoreObjectFromCursor", "cursor null");
			return null;
		}
		List<ContactModel> res = new LinkedList<ContactModel>();
		String[] columns = cursor.getColumnNames();
		
		while(cursor.moveToNext()){
			ContactModel model = new ContactModel();
			for(int i = 0;i<columns.length;i++){
				String column = columns[i];
				if(column.equals("id"))
					model.id = cursor.getInt(i);
				else if(column.equals("name"))
					model.name = cursor.getString(i);
				else if(column.equals("office"))
					model.office = cursor.getString(i);
				else if(column.equals("phone"))
					model.phone = cursor.getString(i);
				else if(column.equals("agency"))
					model.agency = cursor.getString(i);
				else {
					model.department = cursor.getString(i);
				}
			}
			res.add(model);
		}
		return res;
	}
}
