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
package eu.iescities.pilot.rovereto.roveretoexplorer.map;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import eu.iescities.pilot.rovereto.roveretoexplorer.R;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.Utils;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.BaseDTObject;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ExplorerObject;
import eu.iescities.pilot.rovereto.roveretoexplorer.fragments.event.Fragment_EventDetails;

public class InfoDialog extends DialogFragment {
	public static final String PARAM = "DTO_OBJECT";
	private BaseDTObject data;

	public InfoDialog() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (this.data == null) {
			this.data = (BaseDTObject) getArguments().getSerializable(PARAM);
		}
		if (data instanceof ExplorerObject) {
			getDialog().setTitle(R.string.info_dialog_title_event);
			
		}
		View v =inflater.inflate(R.layout.mapdialog, container, false);

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		TextView msgTitle = (TextView) getDialog().findViewById(R.id.infodialog_title);
		TextView msgCat = (TextView) getDialog().findViewById(R.id.infodialog_category);
		TextView msgDate = (TextView) getDialog().findViewById(R.id.infodialog_date);
		int textViewId = getResources().getIdentifier("android:id/alertTitle", null, null);
//		TextView tv = (TextView) getDialog().findViewById(textViewId);
//		tv.setTextColor(getResources().getColor(android.R.color.black));
//		int dividerId = getResources().getIdentifier("android:id/titleDivider", null, null);
//		View divider = getDialog().findViewById(dividerId);
//		divider.setBackgroundColor(getResources().getColor(android.R.color.black));
		if (data instanceof ExplorerObject) {
			ExplorerObject event = (ExplorerObject) data;
			String strTitle = "";
			String strCat = "";
			String strDate = "";
			strTitle += "<h2>";
			strTitle += event.getTitle();
			strTitle += "</h2><br/><p>";
			if (event.getType() != null) {
				String categoryString=event.categoryString(getActivity());
				if (categoryString!=null) {

					strCat += "<p>" + categoryString +"</p><br/>";

				}
			}
			strDate += "<p>" + event.dateTimeString() + "</p>";

			String place = Utils.getEventShortAddress(event);
			if (place != null) {
				strDate += "<p>" + place + "</p>";
			}
			msgTitle.setText(Html.fromHtml(strTitle));
			msgCat.setText(Html.fromHtml(strCat));
			msgDate.setText(Html.fromHtml(strDate));
		}

		msgDate.setMovementMethod(new ScrollingMovementMethod());

		Button b = (Button) getDialog().findViewById(R.id.mapdialog_cancel);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});

		b = (Button) getDialog().findViewById(R.id.mapdialog_ok);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
				Bundle args = new Bundle();

				if (data instanceof ExplorerObject) {
					Fragment_EventDetails fragment = new Fragment_EventDetails();
					args.putString(Utils.ARG_EVENT_ID, (data.getId()));
					fragment.setArguments(args);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(R.id.content_frame, fragment, "me");
					fragmentTransaction.addToBackStack(fragment.getTag());
				}
				fragmentTransaction.commit();
				getDialog().dismiss();
			}
		});

	}
}
