package com.dacas.localdb;

import com.dacas.controller.ContactController;
import com.dacas.controller.ContactGroupController;
import com.dacas.controller.GroupController;
import com.dacas.model.ContactModel;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalDBHelper extends SQLiteOpenHelper{
	private static String DB_NAME = "Telegram.db";
	private static int DB_VERSION = 1;
	private String TAG;
	public LocalDBHelper(Context context) {
		super(context,DB_NAME,null,DB_VERSION);
		TAG = getClass().getSimpleName();
	}
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate");
		String SQL  = ContactController.getSQLString();
		Log.d("创建联系人表", SQL);
		db.execSQL(SQL);
		SQL = GroupController.getGroupSQLString();
		Log.d("创建分组表", SQL);
		db.execSQL(SQL);
		SQL = ContactGroupController.getGroupAndContactSQLString();
		Log.d("创建分组与联系人关联表", SQL);
		db.execSQL(SQL);
		
		SQL = ContactController.insertTestDataToDB();
		Log.d("联系人表测试数据", SQL);
		db.execSQL(SQL);
		
		SQL = GroupController.initGroup();
		Log.d("分组列表测试数据", SQL);
		db.execSQL(SQL);
		
		Cursor cursor = db.rawQuery("select id from "+ContactController.tableName, null);
		SQL = ContactGroupController.initContactGroup(cursor);
		cursor.close();
		Log.d("关联表测试数据",SQL);
		db.execSQL(SQL);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
