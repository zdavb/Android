package com.dacas.telegram;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dacas.controller.ContactController;
import com.dacas.controller.ContactGroupController;
import com.dacas.controller.GroupController;
import com.dacas.localdb.DBManager;
import com.dacas.model.ContactGroupModel;
import com.dacas.model.ContactModel;

public class ContactGroupActivity extends Activity implements OnClickListener,DialogInterface.OnClickListener{
	List<ContactGroupModel> parentData;
	Map<Integer, List<ContactModel>> sonsData;
	List<ContactModel> contacts;
	
	EditText text;
	EditText changeGroupNameText;
	int GroupPositionForOperation;//待操作的组位置；删除分组||更改组名称
	int ChildPositionForOperation;//待操作的元素位置 删除||移动||复制
	
	Dialog newGroupDialog;//新建分组的dialog
	Dialog deleteGroupDialog;//删除分组的dialog
	Dialog changeGroupNameDialog;//更改组名称的dialog
	Dialog chooseOperationOnGroupDialog;//选择对组操作
	Dialog chooseOperationOnChildDialog;//选择对元素操作
	Dialog moveGroupDialog;//选择移动到组
	Dialog copyGroupDialog;//选择复制到组
	
	ContactController contactController;
	GroupController groupController;
	ContactGroupController contactGroupController;
	
	ExpandableListView view;
	ExpandableAdapter adapter;
	
	int groupPositionOfchildItemOnLongClick;
	class ExpandableAdapter extends BaseExpandableListAdapter{
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			int groupId = parentData.get(groupPosition).id;
			return sonsData.get(groupId).get(childPosition);
		}
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}
		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			int groupId = parentData.get(groupPosition).id;
			return sonsData.get(groupId).size();
		}
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView view = null;
			int groupId = parentData.get(groupPosition).id;
			ContactModel model = sonsData.get(groupId).get(childPosition);
			if(convertView != null){
				view = (TextView)convertView;
				view.setText(model.agency+" "+model.name);//单位+姓名
				view.setTag(groupPosition);
			}else{
				view = createView(model.agency+" "+model.name);
				view.setTag(groupPosition);
			}
			return view;
		}
		@Override
		public Object getGroup(int groupPosition) {
			return parentData.get(groupPosition);
		}
		@Override
		public int getGroupCount() {
			return parentData.size();
		}
		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView view = null;
			if(convertView != null){
				view = (TextView)convertView;
				view.setText(parentData.get(groupPosition).name);
			}else{
				view = createView(parentData.get(groupPosition).name);
			}
			return view;
		}
		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}
		private TextView createView(String text){
			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,80);
			TextView view = new TextView(ContactGroupActivity.this);
			view.setLayoutParams(layoutParams);
			view.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			view.setPadding(80, 0, 0, 0);
			view.setText(text);
			return view;
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		groupController = new GroupController(ContactGroupActivity.this);
		contactGroupController = new ContactGroupController(ContactGroupActivity.this);
		contactController = new ContactController(ContactGroupActivity.this);
		
		//自定义标题栏
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.expandable_listview_group);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.activity_title_bar);
		TextView title_info = (TextView)findViewById(R.id.title_info);
		TextView title_content = (TextView)findViewById(R.id.title_content);
		TextView back = (TextView)findViewById(R.id.title_back);
		title_info.setText("新建分组");
		title_content.setText("联系人分组");
		title_info.setVisibility(View.VISIBLE);
		
		//添加响应事件
		title_info.setOnClickListener(this);//新建分组
		back.setOnClickListener(this);//返回键
		
		//初始化Adapter数据
		initGroupData();
		
		view = (ExpandableListView)findViewById(R.id.main_group_listview);
		adapter = new ExpandableAdapter();
		view.setAdapter(adapter);
		view.setOnChildClickListener(new OnChildClickListener() {//设置子元素点击事件
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				int groupId = parentData.get(groupPosition).id;
				ContactModel model = sonsData.get(groupId).get(childPosition);
				Intent intent = new Intent(ContactGroupActivity.this,ContactDetail.class);
				intent.putExtra("id", model.id);
				intent.putExtra("name", model.name);
				intent.putExtra("phone", model.phone);
				intent.putExtra("office", model.office);
				intent.putExtra("agency", model.agency);
				intent.putExtra("department", model.department);
				startActivity(intent);
				return false;
			}
		});
		view.setOnItemLongClickListener(new OnItemLongClickListener() {//设置元素长按点击事件
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				int itemType = ExpandableListView.getPackedPositionType(id);
				if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP){//长按组名
					Builder groupTmpBuilder = new AlertDialog.Builder(ContactGroupActivity.this);
					groupTmpBuilder.setTitle("选择操作");
					groupTmpBuilder.setItems(new String[]{"删除分组","更改组名"}, ContactGroupActivity.this);
					
					GroupPositionForOperation = position;
					chooseOperationOnGroupDialog = groupTmpBuilder.show();
				}else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD){//长按子节点
					Builder childTmpBuilder = new AlertDialog.Builder(ContactGroupActivity.this);
					childTmpBuilder.setTitle("选择操作");
					childTmpBuilder.setItems(new String[]{"删除","移动","复制"}, ContactGroupActivity.this);

					groupPositionOfchildItemOnLongClick = (Integer)view.getTag();

					ChildPositionForOperation = position;
					chooseOperationOnChildDialog = childTmpBuilder.show();
				}
				return true;
			}
		});
	}
	/**
	 * 初始化分组及联系人信息
	 */
	private void initGroupData(){
		parentData = groupController.getAllGroups();//获取全部组信息
		sonsData = new HashMap<Integer, List<ContactModel>>();
		contacts = contactController.getAllContacts();//获取全部联系人信息
		Map<Integer, ContactModel> contactMap = new HashMap<Integer, ContactModel>();
		
		int size = contacts.size();
		for(int i = 0;i<size;i++){
			ContactModel model = contacts.get(i);
			contactMap.put(model.id, model);
		}//将联系人结构化为hash结构
		
		for(int i = 0;i<parentData.size();i++){
			int groupId = parentData.get(i).id;
			List<Integer> contactId = contactGroupController.getContacts(groupId);//获得了组内所有联系人的ID
			List<ContactModel> contactList = new LinkedList<ContactModel>();
			//添加引用，也许有多个引用会指向同一个元素，但是它们是满足条件的
			for(int j = 0;j<contactId.size();j++)
				contactList.add(contactMap.get(contactId.get(j)));
			
			parentData.get(i).contacts = contactList;
			sonsData.put(groupId, contactList);
		}
	}
	private List<ContactGroupModel> getData(){
		List<ContactGroupModel> res = new LinkedList<ContactGroupModel>();
		DBManager manager = new DBManager(this);
		Cursor cursor = manager.selectAllFromTable(GroupController.tableName, null, null);
		if(cursor == null)
			return res;
		
		while(cursor.moveToNext()){
			ContactGroupModel model = new ContactGroupModel();
			model.id = cursor.getInt(cursor.getColumnIndex("id"));
			model.name = cursor.getString(cursor.getColumnIndex("name"));
			model.priority = cursor.getInt(cursor.getColumnIndex("priority"));
			
			res.add(model);
		}
		return res;
	}
	
	//处理view的点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_back:
			finish();
			break;
		case R.id.title_info:
			//新建一个对话框【新建分组】
			Builder builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.dialog_message_view, null);
			builder.setView(layout);
			builder.setTitle("请输入");
			builder.setNegativeButton("取消", this);
			builder.setPositiveButton("确定", this);
			
			text = (EditText)layout.findViewById(R.id.dialog_edittext);
			newGroupDialog = builder.show();
			break;
		default:
			break;
		}
	}
	//处理dialog的点击事件
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(dialog.equals(newGroupDialog) && which == -1){//新建分组操作
			String groupName = text.getText().toString();
			ContactGroupModel group = new ContactGroupModel();
			group.name = groupName;
			group.priority = Integer.parseInt(GroupController.USER_PRORITY);//设置用户优先级
			
			long rowId = groupController.addNewGroup(groupName);
			if(rowId == -1){//插入失败
				Toast.makeText(ContactGroupActivity.this, "添加新组失败", Toast.LENGTH_SHORT).show();
			}else{
				group.id = (int)rowId;
				//添加到组list
				parentData.add(group);
				sonsData.put(group.id, new LinkedList<ContactModel>());
				Toast.makeText(ContactGroupActivity.this, "添加新组成功", Toast.LENGTH_SHORT).show();
			}
		}
		else if(dialog.equals(deleteGroupDialog) && which == -1){//删除分组操作
			ContactGroupModel groupModel = (ContactGroupModel)view.getItemAtPosition(GroupPositionForOperation);
			//find group position and remove it
			for(int i = 0;i<parentData.size();i++){
				ContactGroupModel tmpModel = parentData.get(i);
				if(tmpModel.id == groupModel.id)
					parentData.remove(i);
			}
			sonsData.remove(groupModel.id);//删除相关分组
			
			boolean result = groupController.deleteGroup(groupModel);
			adapter.notifyDataSetChanged();
			if(result)
				Toast.makeText(ContactGroupActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(ContactGroupActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
		}else if(dialog.equals(changeGroupNameDialog) && which == -1){//更新分组操作
			String newGroupName = changeGroupNameText.getText().toString();
			ContactGroupModel groupModel = (ContactGroupModel)view.getItemAtPosition(GroupPositionForOperation);
			groupModel.name = newGroupName;
			//更新到数据库
			boolean result = groupController.updateGroupName(groupModel);
			if(result)
				Toast.makeText(ContactGroupActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(ContactGroupActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
		}
		else if(dialog.equals(chooseOperationOnGroupDialog)){
//			Toast.makeText(ContactGroupActivity.this, String.valueOf(which), Toast.LENGTH_SHORT).show();
			switch (which) {
			case 0://删除分组
				AlertDialog.Builder deleteTmpDialog = new AlertDialog.Builder(this);
				deleteTmpDialog.setTitle("注意");
				deleteTmpDialog.setMessage("您确定要删除分组以及组内所有成员吗？");
				deleteTmpDialog.setPositiveButton("确定", this);
				deleteTmpDialog.setNegativeButton("取消", null);
				deleteGroupDialog = deleteTmpDialog.show();
				break;
			case 1://更改组名称
				AlertDialog.Builder changeNameTmpDilaog = new AlertDialog.Builder(this);
				changeNameTmpDilaog.setTitle("请输入新的名称");
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout view = (LinearLayout)inflater.inflate(R.layout.dialog_message_view, null);
				
				changeGroupNameText = (EditText)view.findViewById(R.id.dialog_edittext);
				changeNameTmpDilaog.setView(view);
				changeNameTmpDilaog.setPositiveButton("确定", this);
				changeNameTmpDilaog.setNegativeButton("取消", null);
				changeGroupNameDialog = changeNameTmpDilaog.show();
				break;
			default:
				break;
			}
		}else if(dialog.equals(chooseOperationOnChildDialog)){
			ContactModel model = (ContactModel)view.getItemAtPosition(ChildPositionForOperation);
			Log.d("chooseOperation", model.toString());
			//Toast.makeText(ContactGroupActivity.this, "position:"+ChildPositionForOperation+"which:"+which, Toast.LENGTH_SHORT).show();
			switch (which) {
				case 0://删除
					List<ContactModel> list = sonsData.get(groupPositionOfchildItemOnLongClick);
					list.remove(model);
					//从数据库中删除
					int count = contactGroupController.delete(groupPositionOfchildItemOnLongClick,model.id);
					if(count == 1){
						Toast.makeText(ContactGroupActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
						adapter.notifyDataSetChanged();
					}else{
						Toast.makeText(ContactGroupActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
					}
					break;
				case 1://移动
					moveGroupDialog = showGroupDialog();
					break;
				case 2://复制
					copyGroupDialog = showGroupDialog();
					break;
				default:
					break;
			}
		}else if(dialog.equals(moveGroupDialog)){
			Toast.makeText(ContactGroupActivity.this, "移动到分组"+String.valueOf(which), Toast.LENGTH_SHORT).show();
		}else if(dialog.equals(copyGroupDialog)){
			Toast.makeText(ContactGroupActivity.this, "复制到分组"+String.valueOf(which), Toast.LENGTH_SHORT).show();
		}
	}
	//显示分组的dialog
	public Dialog showGroupDialog(){
		Builder builder = new AlertDialog.Builder(ContactGroupActivity.this);
		builder.setTitle("选择分组");
		int size = parentData.size();
		String[] names = new String[size-1];
		int index = 0;
		for(int i = 0;i<size;i++){
			ContactGroupModel groupModel = parentData.get(i);
			if(i != groupPositionOfchildItemOnLongClick){
				names[index++] = groupModel.name;
			}
		}
		builder.setItems(names, this);
		return builder.show();
	}
}
