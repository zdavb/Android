package com.dacas.localdb;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager{
	SQLiteDatabase database;
	LocalDBHelper helper;
	String TAG  = getClass().getSimpleName();
	
	public DBManager(Context context) {
		helper = new LocalDBHelper(context);
		database = helper.getWritableDatabase();
	}
	/**
	 * 添加一条新的记录
	 * @param tableName
	 * @param names
	 * @param values
	 * @return 插入ID号
	 */
	public long insertIntoTable(String tableName,List<String> names,List<String> values){
		ContentValues value = new ContentValues();
		if(names.size()== 0 || names.size() != values.size())
			return -1;
		//init values
		for(int i = 0;i<names.size();i++){
			value.put(names.get(i), values.get(i));
		}
		return database.insert(tableName, null, value);
	}
	/**
	 * 删除全部数据
	 * @param tableName
	 * @return 删除的条数
	 */
	public int deleteAll(String tableName){
		return database.delete(tableName, null, null);
	}
	/**
	 * 按条件删除数据
	 * @param tableName
	 * @param names
	 * @param values
	 * @return 成功返回删除条数；否则返回-1
	 */
	public int deleteFromTable(String tableName,List<String> names,List<String> values){
		StringBuilder builder = new StringBuilder();
		if(names.size()==0 || names.size() != values.size())
			return -1;
		
		builder.append(names.get(0)+"=?");
		for(int i = 1;i<names.size();i++){
			builder.append(" AND ");
			builder.append(names.get(i)+"=?");
		}
		Log.d(TAG, "deleteFromTable:"+builder.toString());
		String[] valuesStrings = new String[values.size()];
		for(int i = 0;i<values.size();i++)
			valuesStrings[i] = values.get(i);
		
		return database.delete(tableName, builder.toString(), valuesStrings);
	}
	/**
	 * 删除表中全部数据
	 * @param tableName
	 * @return
	 */
	public int deleteAllFromTable(String tableName){
		return database.delete(tableName, null, null);
	}
	/**
	 * 
	 * @param tableName
	 * @param names
	 * @param values
	 * @param id
	 * @return 影响的条数
	 */
	public int updateTable(String tableName,List<String> names,List<String> values,String id){
		ContentValues value = new ContentValues();
		if(names == null || names.size() == 0 || names.size() != values.size())
			return -1;
		int size = names.size();
		for(int i = 0;i<size;i++){
			value.put(names.get(i), values.get(i));
		}
		return database.update(tableName, value, "id=?", new String[]{id});
	}
	/**
	 * 从表中查询数据，返回所有列
	 * @param tableName
	 * @param names
	 * @param values
	 * @param groupby
	 * @param orderby
	 * @return cursor
	 */
	public Cursor selectFromTable(String tableName,List<String> names,List<String> values,String groupby,String orderby){
		StringBuilder builder = new StringBuilder();
		int size = names.size();
		if(names == null || size == 0 || size != values.size())
			return null;
		builder.append(names.get(0)+"= ?");
		for(int i = 1;i<size;i++){
			builder.append(" AND "+names.get(i)+"=?");
		}
		String[] strings = null;
		size = values.size();
		if(size>0){
			strings = new String[values.size()];
			for(int i = 0;i<size;i++){
				strings[i] = values.get(i);
			}
		}
		return database.query(tableName,null,builder.toString(),strings,groupby,null,orderby);
	}
	/**
	 * 查询表中所有行和列
	 * @param tableName
	 * @param selections
	 * @param orderby
	 * @return cursor
	 */
	public Cursor selectAllFromTable(String tableName,List<String> selections,String orderby){
		//return database.rawQuery("select * from contact", null);
		if(selections == null || selections.size() == 0)
			return database.query(tableName, null, null, null, null, null, orderby);
		String[] strings = null;
		if(selections!=null){
			int size = selections.size();
			strings = new String[size];
			for(int i = 0;i<size;i++)
				strings[i] = selections.get(i);
		}
		return database.query(tableName, strings, null, null, null, null, orderby);
	}
}
