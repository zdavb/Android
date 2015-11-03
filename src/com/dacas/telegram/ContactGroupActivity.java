package com.dacas.telegram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.R.integer;
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
	List<ContactModel> contacts;
	Set<String> groupNameSet;
	
	EditText newGroupText;//新建分组
	EditText changeGroupNameText;//改变组名
	
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
	
	ContactGroupModel groupOfLongClick;
	ContactModel contactOfLongClick;
	List<ContactGroupModel> chooseGroupModels;//选择哪个分组
	class ExpandableAdapter extends BaseExpandableListAdapter{
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return parentData.get(groupPosition).contacts.get(childPosition);
		}
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return parentData.get(groupPosition).contacts.get(childPosition).id;
		}
		@Override
		public int getChildrenCount(int groupPosition) {
			return parentData.get(groupPosition).contacts.size();//子元素的个数
		}
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView view = null;
			ContactModel childModel = parentData.get(groupPosition).contacts.get(childPosition);
			
			if(convertView != null){
				view = (TextView)convertView;
				view.setText(childModel.agency+" "+childModel.name);//单位+姓名
				view.setTag(groupPosition);
			}else{
				view = createView(childModel.agency+" "+childModel.name);
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
			return parentData.get(groupPosition).id;
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
		groupController = new GroupController(this);
		contactGroupController = new ContactGroupController(this);
		contactController = new ContactController(this);
		
		//自定义标题栏
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.expandable_listview_group);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.activity_title_bar);
		
		//获取主页面的空间
		TextView title_info = (TextView)findViewById(R.id.title_info);
		TextView title_content = (TextView)findViewById(R.id.title_content);
		TextView title_back = (TextView)findViewById(R.id.title_back);
		title_info.setText("新建分组");
		title_content.setText("联系人分组");
		title_info.setVisibility(View.VISIBLE);
		
		//添加响应事件
		title_info.setOnClickListener(this);//新建分组
		title_back.setOnClickListener(this);//返回键
		
		//初始化Adapter数据
		initGroupData();
		
		view = (ExpandableListView)findViewById(R.id.main_group_listview);
		adapter = new ExpandableAdapter();
		view.setAdapter(adapter);
		view.setOnChildClickListener(new OnChildClickListener() {//设置子元素点击事件
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				ContactModel contact = parentData.get(groupPosition).contacts.get(childPosition);
				Intent intent = new Intent(ContactGroupActivity.this,ContactDetail.class);
				intent.putExtra("content", contact);
				startActivity(intent);
				return true;
			}
		});
		view.setOnItemLongClickListener(new OnItemLongClickListener() {//设置元素长按点击事件
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				//获取长按的是子元素还是组元素
				int itemType = ExpandableListView.getPackedPositionType(id);
				if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP){//长按组名
					Builder groupDialogBuilder = new AlertDialog.Builder(ContactGroupActivity.this);
					groupDialogBuilder.setTitle("选择操作");
					groupDialogBuilder.setItems(new String[]{"删除分组","更改组名"}, ContactGroupActivity.this);
					
					groupOfLongClick = (ContactGroupModel)parent.getItemAtPosition(position);
					chooseOperationOnGroupDialog = groupDialogBuilder.show();
				}else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD){//长按子节点
					Builder childDialogBuilder = new AlertDialog.Builder(ContactGroupActivity.this);
					childDialogBuilder.setTitle("选择操作");
					childDialogBuilder.setItems(new String[]{"删除","移动","复制"}, ContactGroupActivity.this);

					int groupPos = (Integer)view.getTag();
					groupOfLongClick = parentData.get(groupPos);//get tag 表示获取当前view的父元素的位置
					contactOfLongClick = (ContactModel)parent.getItemAtPosition(position);
					chooseOperationOnChildDialog = childDialogBuilder.show();
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
		contacts = contactController.getAllContacts();//获取全部联系人信息
		groupNameSet = new HashSet<String>();
		
		Map<Integer, ContactModel> contactMap = new HashMap<Integer, ContactModel>();
		
		int size = contacts.size();
		for(int i = 0;i<size;i++){
			ContactModel model = contacts.get(i);
			contactMap.put(model.id, model);
		}//将联系人结构化为hash结构
		
		for(int i = 0;i<parentData.size();i++){
			int groupId = parentData.get(i).id;
			groupNameSet.add(parentData.get(i).name);
			
			List<Integer> contactIds = contactGroupController.getContacts(groupId);//获得了组内所有联系人的ID
			List<ContactModel> contactList = new LinkedList<ContactModel>();
			//添加引用，也许有多个引用会指向同一个元素，但是它们是满足条件的
			for(int j = 0;j<contactIds.size();j++)
				contactList.add(contactMap.get(contactIds.get(j)));
			
			parentData.get(i).contacts = contactList;
		}
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
			
			newGroupText = (EditText)layout.findViewById(R.id.dialog_edittext);
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
			String groupName = newGroupText.getText().toString();
			if(groupNameSet.contains(groupName)){
				Toast.makeText(ContactGroupActivity.this, "该分组名已经存在，请更换", Toast.LENGTH_SHORT).show();
				return;
			}
			groupNameSet.add(groupName);
			
			ContactGroupModel group = new ContactGroupModel();
			group.name = groupName;
			group.priority = Integer.parseInt(GroupController.USER_PRORITY);//设置用户优先级
			group.contacts = new LinkedList<ContactModel>();
			
			long rowId = groupController.addNewGroup(groupName);
			if(rowId == -1){//插入失败
				Toast.makeText(ContactGroupActivity.this, "添加新组失败", Toast.LENGTH_SHORT).show();
			}else{
				group.id = (int)rowId;
				//添加到组list
				parentData.add(group);
				Toast.makeText(ContactGroupActivity.this, "添加新组成功", Toast.LENGTH_SHORT).show();
			}
		}
		else if(dialog.equals(deleteGroupDialog) && which == -1){//删除分组操作
			parentData.remove(groupOfLongClick);
			boolean result = groupController.deleteGroup(groupOfLongClick);
			if(result){
				adapter.notifyDataSetChanged();
				Toast.makeText(ContactGroupActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
			}
			else
				Toast.makeText(ContactGroupActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
		}else if(dialog.equals(changeGroupNameDialog) && which == -1){//更新分组操作
			String newGroupName = changeGroupNameText.getText().toString();
			groupOfLongClick.name = newGroupName;
			//更新到数据库
			boolean result = groupController.updateGroupName(groupOfLongClick);
			if(result)
				Toast.makeText(ContactGroupActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(ContactGroupActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
		}
		else if(dialog.equals(chooseOperationOnGroupDialog)){
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
			Log.d("chooseOperation", contactOfLongClick.toString());
			switch (which) {
				case 0:
					Log.d("contact group", groupOfLongClick.toString());
					groupOfLongClick.contacts.remove(contactOfLongClick);//删除组中某一个成员
					//从数据库中删除
					int count = contactGroupController.delete(groupOfLongClick.id,contactOfLongClick.id);
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
			ContactGroupModel targetGroup = chooseGroupModels.get(which);
			ContactGroupModel sourceGroup = groupOfLongClick;
			
			if(targetGroup.contacts.contains(contactOfLongClick)){
				Toast.makeText(ContactGroupActivity.this, "该分组中已经包含该联系人", Toast.LENGTH_SHORT).show();
				return;
			}
			sourceGroup.contacts.remove(contactOfLongClick);
			targetGroup.contacts.add(contactOfLongClick);
			int res = contactGroupController.delete(sourceGroup.id, contactOfLongClick.id);
			
			if(res != 1){
				Toast.makeText(ContactGroupActivity.this, "移动到分组失败", Toast.LENGTH_SHORT).show();
			}else{
				if(!contactGroupController.insert(targetGroup.id,contactOfLongClick.id))
					Toast.makeText(ContactGroupActivity.this, "移动到分组失败", Toast.LENGTH_SHORT).show();
				else {
					Toast.makeText(ContactGroupActivity.this, "移动成功", Toast.LENGTH_SHORT).show();
					adapter.notifyDataSetChanged();
				}
			}
			
		}else if(dialog.equals(copyGroupDialog)){
			ContactGroupModel targetGroup = chooseGroupModels.get(which);
			ContactGroupModel sourceGroup = groupOfLongClick;
			
			if(targetGroup.contacts.contains(contactOfLongClick)){
				Toast.makeText(ContactGroupActivity.this, "该分组中已经包含该联系人", Toast.LENGTH_SHORT).show();
				return;
			}
			targetGroup.contacts.add(contactOfLongClick);
			if(!contactGroupController.insert(targetGroup.id,contactOfLongClick.id))
				Toast.makeText(ContactGroupActivity.this, "复制到分组失败", Toast.LENGTH_SHORT).show();
			else {
				Toast.makeText(ContactGroupActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
				adapter.notifyDataSetChanged();
			}
		}
	}
	//显示分组的dialog
	public Dialog showGroupDialog(){
		Builder builder = new AlertDialog.Builder(ContactGroupActivity.this);
		builder.setTitle("选择分组");
		int size = parentData.size();
		chooseGroupModels = new LinkedList<ContactGroupModel>();
		String[] groupNames = new String[size-1];
		int index = 0;
		
		for(int i = 0;i<size;i++){
			if(parentData.get(i) != groupOfLongClick){
				chooseGroupModels.add(parentData.get(i));
				groupNames[index++] = parentData.get(i).name;
			}
		}
		builder.setItems(groupNames, this);
		return builder.show();
	}
}
