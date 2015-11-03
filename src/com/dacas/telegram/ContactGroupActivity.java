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
	
	EditText newGroupText;//�½�����
	EditText changeGroupNameText;//�ı�����
	
	Dialog newGroupDialog;//�½������dialog
	Dialog deleteGroupDialog;//ɾ�������dialog
	Dialog changeGroupNameDialog;//���������Ƶ�dialog
	Dialog chooseOperationOnGroupDialog;//ѡ��������
	Dialog chooseOperationOnChildDialog;//ѡ���Ԫ�ز���
	Dialog moveGroupDialog;//ѡ���ƶ�����
	Dialog copyGroupDialog;//ѡ���Ƶ���
	
	ContactController contactController;
	GroupController groupController;
	ContactGroupController contactGroupController;
	
	ExpandableListView view;
	ExpandableAdapter adapter;
	
	ContactGroupModel groupOfLongClick;
	ContactModel contactOfLongClick;
	List<ContactGroupModel> chooseGroupModels;//ѡ���ĸ�����
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
			return parentData.get(groupPosition).contacts.size();//��Ԫ�صĸ���
		}
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView view = null;
			ContactModel childModel = parentData.get(groupPosition).contacts.get(childPosition);
			
			if(convertView != null){
				view = (TextView)convertView;
				view.setText(childModel.agency+" "+childModel.name);//��λ+����
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
		
		//�Զ��������
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.expandable_listview_group);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.activity_title_bar);
		
		//��ȡ��ҳ��Ŀռ�
		TextView title_info = (TextView)findViewById(R.id.title_info);
		TextView title_content = (TextView)findViewById(R.id.title_content);
		TextView title_back = (TextView)findViewById(R.id.title_back);
		title_info.setText("�½�����");
		title_content.setText("��ϵ�˷���");
		title_info.setVisibility(View.VISIBLE);
		
		//�����Ӧ�¼�
		title_info.setOnClickListener(this);//�½�����
		title_back.setOnClickListener(this);//���ؼ�
		
		//��ʼ��Adapter����
		initGroupData();
		
		view = (ExpandableListView)findViewById(R.id.main_group_listview);
		adapter = new ExpandableAdapter();
		view.setAdapter(adapter);
		view.setOnChildClickListener(new OnChildClickListener() {//������Ԫ�ص���¼�
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
		view.setOnItemLongClickListener(new OnItemLongClickListener() {//����Ԫ�س�������¼�
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				//��ȡ����������Ԫ�ػ�����Ԫ��
				int itemType = ExpandableListView.getPackedPositionType(id);
				if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP){//��������
					Builder groupDialogBuilder = new AlertDialog.Builder(ContactGroupActivity.this);
					groupDialogBuilder.setTitle("ѡ�����");
					groupDialogBuilder.setItems(new String[]{"ɾ������","��������"}, ContactGroupActivity.this);
					
					groupOfLongClick = (ContactGroupModel)parent.getItemAtPosition(position);
					chooseOperationOnGroupDialog = groupDialogBuilder.show();
				}else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD){//�����ӽڵ�
					Builder childDialogBuilder = new AlertDialog.Builder(ContactGroupActivity.this);
					childDialogBuilder.setTitle("ѡ�����");
					childDialogBuilder.setItems(new String[]{"ɾ��","�ƶ�","����"}, ContactGroupActivity.this);

					int groupPos = (Integer)view.getTag();
					groupOfLongClick = parentData.get(groupPos);//get tag ��ʾ��ȡ��ǰview�ĸ�Ԫ�ص�λ��
					contactOfLongClick = (ContactModel)parent.getItemAtPosition(position);
					chooseOperationOnChildDialog = childDialogBuilder.show();
				}
				return true;
			}
		});
	}
	/**
	 * ��ʼ�����鼰��ϵ����Ϣ
	 */
	private void initGroupData(){
		parentData = groupController.getAllGroups();//��ȡȫ������Ϣ
		contacts = contactController.getAllContacts();//��ȡȫ����ϵ����Ϣ
		groupNameSet = new HashSet<String>();
		
		Map<Integer, ContactModel> contactMap = new HashMap<Integer, ContactModel>();
		
		int size = contacts.size();
		for(int i = 0;i<size;i++){
			ContactModel model = contacts.get(i);
			contactMap.put(model.id, model);
		}//����ϵ�˽ṹ��Ϊhash�ṹ
		
		for(int i = 0;i<parentData.size();i++){
			int groupId = parentData.get(i).id;
			groupNameSet.add(parentData.get(i).name);
			
			List<Integer> contactIds = contactGroupController.getContacts(groupId);//���������������ϵ�˵�ID
			List<ContactModel> contactList = new LinkedList<ContactModel>();
			//������ã�Ҳ���ж�����û�ָ��ͬһ��Ԫ�أ���������������������
			for(int j = 0;j<contactIds.size();j++)
				contactList.add(contactMap.get(contactIds.get(j)));
			
			parentData.get(i).contacts = contactList;
		}
	}
	//����view�ĵ���¼�
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_back:
			finish();
			break;
		case R.id.title_info:
			//�½�һ���Ի����½����顿
			Builder builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.dialog_message_view, null);
			builder.setView(layout);
			builder.setTitle("������");
			builder.setNegativeButton("ȡ��", this);
			builder.setPositiveButton("ȷ��", this);
			
			newGroupText = (EditText)layout.findViewById(R.id.dialog_edittext);
			newGroupDialog = builder.show();
			break;
		default:
			break;
		}
	}
	//����dialog�ĵ���¼�
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(dialog.equals(newGroupDialog) && which == -1){//�½��������
			String groupName = newGroupText.getText().toString();
			if(groupNameSet.contains(groupName)){
				Toast.makeText(ContactGroupActivity.this, "�÷������Ѿ����ڣ������", Toast.LENGTH_SHORT).show();
				return;
			}
			groupNameSet.add(groupName);
			
			ContactGroupModel group = new ContactGroupModel();
			group.name = groupName;
			group.priority = Integer.parseInt(GroupController.USER_PRORITY);//�����û����ȼ�
			group.contacts = new LinkedList<ContactModel>();
			
			long rowId = groupController.addNewGroup(groupName);
			if(rowId == -1){//����ʧ��
				Toast.makeText(ContactGroupActivity.this, "�������ʧ��", Toast.LENGTH_SHORT).show();
			}else{
				group.id = (int)rowId;
				//��ӵ���list
				parentData.add(group);
				Toast.makeText(ContactGroupActivity.this, "�������ɹ�", Toast.LENGTH_SHORT).show();
			}
		}
		else if(dialog.equals(deleteGroupDialog) && which == -1){//ɾ���������
			parentData.remove(groupOfLongClick);
			boolean result = groupController.deleteGroup(groupOfLongClick);
			if(result){
				adapter.notifyDataSetChanged();
				Toast.makeText(ContactGroupActivity.this, "ɾ���ɹ�", Toast.LENGTH_SHORT).show();
			}
			else
				Toast.makeText(ContactGroupActivity.this, "ɾ��ʧ��", Toast.LENGTH_SHORT).show();
		}else if(dialog.equals(changeGroupNameDialog) && which == -1){//���·������
			String newGroupName = changeGroupNameText.getText().toString();
			groupOfLongClick.name = newGroupName;
			//���µ����ݿ�
			boolean result = groupController.updateGroupName(groupOfLongClick);
			if(result)
				Toast.makeText(ContactGroupActivity.this, "���³ɹ�", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(ContactGroupActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
		}
		else if(dialog.equals(chooseOperationOnGroupDialog)){
			switch (which) {
			case 0://ɾ������
				AlertDialog.Builder deleteTmpDialog = new AlertDialog.Builder(this);
				deleteTmpDialog.setTitle("ע��");
				deleteTmpDialog.setMessage("��ȷ��Ҫɾ�������Լ��������г�Ա��");
				deleteTmpDialog.setPositiveButton("ȷ��", this);
				deleteTmpDialog.setNegativeButton("ȡ��", null);
				deleteGroupDialog = deleteTmpDialog.show();
				break;
			case 1://����������
				AlertDialog.Builder changeNameTmpDilaog = new AlertDialog.Builder(this);
				changeNameTmpDilaog.setTitle("�������µ�����");
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout view = (LinearLayout)inflater.inflate(R.layout.dialog_message_view, null);
				
				changeGroupNameText = (EditText)view.findViewById(R.id.dialog_edittext);
				changeNameTmpDilaog.setView(view);
				changeNameTmpDilaog.setPositiveButton("ȷ��", this);
				changeNameTmpDilaog.setNegativeButton("ȡ��", null);
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
					groupOfLongClick.contacts.remove(contactOfLongClick);//ɾ������ĳһ����Ա
					//�����ݿ���ɾ��
					int count = contactGroupController.delete(groupOfLongClick.id,contactOfLongClick.id);
					if(count == 1){
						Toast.makeText(ContactGroupActivity.this, "ɾ���ɹ�", Toast.LENGTH_SHORT).show();
						adapter.notifyDataSetChanged();
					}else{
						Toast.makeText(ContactGroupActivity.this, "ɾ��ʧ��", Toast.LENGTH_SHORT).show();
					}
					break;
				case 1://�ƶ�
					moveGroupDialog = showGroupDialog();
					break;
				case 2://����
					copyGroupDialog = showGroupDialog();
					break;
				default:
					break;
			}
		}else if(dialog.equals(moveGroupDialog)){
			ContactGroupModel targetGroup = chooseGroupModels.get(which);
			ContactGroupModel sourceGroup = groupOfLongClick;
			
			if(targetGroup.contacts.contains(contactOfLongClick)){
				Toast.makeText(ContactGroupActivity.this, "�÷������Ѿ���������ϵ��", Toast.LENGTH_SHORT).show();
				return;
			}
			sourceGroup.contacts.remove(contactOfLongClick);
			targetGroup.contacts.add(contactOfLongClick);
			int res = contactGroupController.delete(sourceGroup.id, contactOfLongClick.id);
			
			if(res != 1){
				Toast.makeText(ContactGroupActivity.this, "�ƶ�������ʧ��", Toast.LENGTH_SHORT).show();
			}else{
				if(!contactGroupController.insert(targetGroup.id,contactOfLongClick.id))
					Toast.makeText(ContactGroupActivity.this, "�ƶ�������ʧ��", Toast.LENGTH_SHORT).show();
				else {
					Toast.makeText(ContactGroupActivity.this, "�ƶ��ɹ�", Toast.LENGTH_SHORT).show();
					adapter.notifyDataSetChanged();
				}
			}
			
		}else if(dialog.equals(copyGroupDialog)){
			ContactGroupModel targetGroup = chooseGroupModels.get(which);
			ContactGroupModel sourceGroup = groupOfLongClick;
			
			if(targetGroup.contacts.contains(contactOfLongClick)){
				Toast.makeText(ContactGroupActivity.this, "�÷������Ѿ���������ϵ��", Toast.LENGTH_SHORT).show();
				return;
			}
			targetGroup.contacts.add(contactOfLongClick);
			if(!contactGroupController.insert(targetGroup.id,contactOfLongClick.id))
				Toast.makeText(ContactGroupActivity.this, "���Ƶ�����ʧ��", Toast.LENGTH_SHORT).show();
			else {
				Toast.makeText(ContactGroupActivity.this, "���Ƴɹ�", Toast.LENGTH_SHORT).show();
				adapter.notifyDataSetChanged();
			}
		}
	}
	//��ʾ�����dialog
	public Dialog showGroupDialog(){
		Builder builder = new AlertDialog.Builder(ContactGroupActivity.this);
		builder.setTitle("ѡ�����");
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
