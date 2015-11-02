package com.dacas.telegram;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ContactDetail extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listview_contact_detail);
		Bundle bundle = getIntent().getExtras();
		
		TextView name = (TextView)findViewById(R.id.contact_detail_name);
		TextView office = (TextView)findViewById(R.id.contact_detail_office);
		TextView phone = (TextView)findViewById(R.id.contact_detail_phone);
		TextView agency = (TextView)findViewById(R.id.contact_detail_agency);
		TextView department = (TextView)findViewById(R.id.contact_detail_department);
		
		name.setText(bundle.getString("name"));
		office.setText(bundle.getString("office"));
		phone.setText(bundle.getString("phone"));
		agency.setText(bundle.getString("agency"));
		department.setText(bundle.getString("department"));
	}
}
