/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.iescities.pilot.rovereto.roveretoexplorer.fragments.event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import eu.iescities.pilot.rovereto.roveretoexplorer.R;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.EventObject;


//in Fragment_EvDetail_Info
public class EventDetailInfoAdapter extends BaseExpandableListAdapter {

	private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private static final DateFormat extDateFormat = new SimpleDateFormat("EEEEEE dd/MM/yyyy");
	private Context context;
	private int elementSelected = -1;
	private boolean postProcAndHeader = true;

	//for expandable list 
	private Map<String, List<String>> attrValuesCollections;
	private List<String> attrNameGroupList;
	private int layoutResourceId;


	private EventObject event = null;
	//private EventPlaceholder eventPlaceHolder = null;
	private TextView txtView = null;
	
	private View row = null;
	


	public EventDetailInfoAdapter(Context context, int layoutResourceId, List<String> events_attr_names,
			Map<String, List<String>> eventAttrValuesCollections) {
		this.context = context;
		this.attrValuesCollections = eventAttrValuesCollections;
		this.attrNameGroupList = events_attr_names;
		this.layoutResourceId = layoutResourceId;

	}
	
	

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		final String eventAttributeValue = (String) getChild(groupPosition, childPosition);

		row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
			//eventPlaceHolder = new EventPlaceholder();
			txtView = (TextView) row.findViewById(R.id.event_info_attribute_values);
			//eventPlaceHolder.title.setTag(eventPlaceHolder.title); 
			//eventPlaceHolder.location = (TextView) row.findViewById(R.id.event_placeholder_loc);
			//e.hour = (TextView) row.findViewById(R.id.event_placeholder_hour);
			//eventPlaceHolder.icon = (ImageView) row.findViewById(R.id.event_placeholder_photo);
			//eventPlaceHolder.attendees = (TextView) row.findViewById(R.id.event_placeholder_participants);
			//eventPlaceHolder.rating = (RatingBar) row.findViewById(R.id.rating_bar);
			//e.dateSeparator = (TextView) row.findViewById(R.id.date_separator);
			row.setTag(txtView);
		} else
		{
			txtView = (TextView) row.getTag();
		}

		//eventPlaceHolder.event = event;

		//****  EVENT INFO   ***** //
		//Log.i("SCROLLTABS", "ATTR: " + txtView.getText() + "!!");
		//Log.i("EVENT", "title: " + e.event.getTitle() + "!!");
		//Log.i("EVENT", "rating: " + e.event.getCommunityData().getAverageRating() + "!!");
		//Log.i("EVENT", "participants: " + e.event.getAttendees() + "!!");
		//Log.i("EVENT", "location: " + (String) e.event.getCustomData().get("where_name") + "!!");
		//Log.i("EVENT", "when: " + e.event.eventDatesString() + "!!");
		//Log.i("EVENT", "image: " + e.event.getCustomData().get("event_img").toString() + "!!");

		txtView.setText(eventAttributeValue);

		return row;
	}


	/*public int getElementSelected() {
		return elementSelected;
	}

	public void setElementSelected(int elementSelected) {
		this.elementSelected = elementSelected;
	}*/


	// Methods needed for the Expandable adapter
	public Object getChild(int groupPosition, int childPosition) {
		return attrValuesCollections.get(attrNameGroupList.get(groupPosition)).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}




	public int getChildrenCount(int groupPosition) {
		return attrValuesCollections.get(attrNameGroupList.get(groupPosition)).size();
	}

	public Object getGroup(int groupPosition) {
		return attrNameGroupList.get(groupPosition);
	}

	public int getGroupCount() {
		return attrNameGroupList.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		String dateLabel = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.event_info_group_item,
					null);
		}

		convertView.setBackgroundResource(getBackgroundColor(groupPosition));

		TextView item = (TextView) convertView.findViewById(R.id.event_info_attribute_names);
		item.setTypeface(null, Typeface.BOLD);
		item.setText(dateLabel);

		return convertView;

	}

	public int getBackgroundColor(int groupPosition) {

		return R.color.orange_rovereto;

	}


	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}



}