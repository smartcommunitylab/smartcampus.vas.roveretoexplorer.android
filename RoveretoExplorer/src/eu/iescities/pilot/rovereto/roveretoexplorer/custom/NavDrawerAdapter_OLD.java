package eu.iescities.pilot.rovereto.roveretoexplorer.custom;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import eu.iescities.pilot.rovereto.roveretoexplorer.R;

public class NavDrawerAdapter_OLD extends BaseExpandableListAdapter {

	private Activity context;
	private Map<String, List<DrawerItemOld>> elements;
	private List<String> headers;

	public NavDrawerAdapter_OLD(Activity context, List<String> headers,
			Map<String, List<DrawerItemOld>> elements) {
		this.context = context;
		this.elements = elements;
		this.headers = headers;
	}

	public Object getChild(int groupPosition, int childPosition) {
		return elements.get(headers.get(groupPosition)).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.drawer_element_row, parent,
					false);
		}
		TextView tv = (TextView) convertView
				.findViewById(R.id.drawer_list_textview);
		DrawerItemOld item = (DrawerItemOld) getChild(groupPosition, childPosition);
		if (item.icon != null){
			tv.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, null,
					null);
		}
		tv.setText(item.text);
		return convertView;
	}

	public int getChildrenCount(int groupPosition) {
		return elements.get(headers.get(groupPosition)).size();
	}

	public Object getGroup(int groupPosition) {
		return headers.get(groupPosition);
	}

	public int getGroupCount() {
		return headers.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.drawer_header_row, parent,
					false);
		}
		TextView tv = (TextView) convertView
				.findViewById(R.id.drawer_list_textview);
		tv.setText((String) getGroup(groupPosition));
		if (isExpanded) {
			convertView.setBackgroundResource(R.drawable.selected_drawer_header_bg);
		} else {
			convertView.setBackgroundResource(R.drawable.drawer_header_bg);

		}
		return convertView;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}