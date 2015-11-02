package com.dacas.telegram;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dacas.controller.ContactController;
import com.dacas.localdb.DBManager;
import com.dacas.model.ContactModel;
public class ContactActivity extends Activity implements OnItemClickListener{
	private String TAG = "contactActivity";
	List<ContactModel> contacts;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		contacts = getData();
		
		ListView view = new ListView(this);
		LayoutInflater lif = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View header = lif.inflate(R.layout.listview_header, null);
		
		view.addHeaderView(header);
		view.setAdapter(new SimpleAdapter(this, getShowData(), R.layout.listview_contact, new String[]{"name","office","agency"},
				new int[]{R.id.contactview_name,R.id.contactview_office,R.id.contactview_agency}));
		setContentView(view);
	
		view.setOnItemClickListener(this);
	}
	
	
	private List<Map<String, String>> getShowData() {
		int size = contacts.size();
		List<Map<String, String>> showData = new LinkedList<Map<String,String>>();
		for(int i = 0;i<size;i++){
			ContactModel model = contacts.get(i);
			Map<String, String> map = new HashMap<String, String>();
			map.put("name", model.name);
			map.put("office", model.office);
			map.put("agency", model.agency);
			showData.add(map);
		}
		return showData;
	}
	private List<ContactModel> getData(){
		ContactController controller = new ContactController(ContactActivity.this);
		return controller.getAllContacts();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(position <= 0)
			return;
		ContactModel model = contacts.get(position-1);
		Intent intent = new Intent(ContactActivity.this,ContactDetail.class);
		intent.putExtra("name", model.name);
		intent.putExtra("office", model.office);
		intent.putExtra("phone", model.phone);
		intent.putExtra("agency", model.agency);
		intent.putExtra("department", model.department);
		
		startActivity(intent);
	}
}
