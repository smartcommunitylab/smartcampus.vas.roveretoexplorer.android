package eu.iescities.pilot.rovereto.roveretoexplorer.fragments.event.dasapere;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import eu.iescities.pilot.rovereto.roveretoexplorer.R;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.AbstractAsyncTaskProcessor;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.Utils;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.Constants;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.DTHelper;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ExplorerObject;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ToKnow;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class Fragment_EvDetail_DaSapere extends ListFragment {

	private static final List<String> CUSTOM_TOKNOW_FIELDS = Arrays.asList(
			Constants.CUSTOM_TOKNOW_PLACE_TYPE, Constants.CUSTOM_TOKNOW_ACCESS,
			Constants.CUSTOM_TOKNOW_CHANCE,
			Constants.CUSTOM_TOKNOW_LANGUAGE_MAIN,
			Constants.CUSTOM_TOKNOW_CLOTHING, Constants.CUSTOM_TOKNOW_TO_BRING);

	protected Context mContext;
	protected String mEventId;
	protected ExplorerObject mEvent = null;
	private EventDetailToKnowAdapter adapter;

	public static Fragment_EvDetail_DaSapere newInstance(String event_id) {
		Fragment_EvDetail_DaSapere f = new Fragment_EvDetail_DaSapere();
		Bundle b = new Bundle();
		b.putString(Utils.ARG_EVENT_ID, event_id);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Log.d("FRAGMENT LC", "Fragment_evDetail_DaSapere --> onAttach");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.detail_edit_da_sapere_menu, menu);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("FRAGMENT LC", "Fragment_evDetail_Info --> onCreate");

		this.mContext = this.getActivity();

		if (savedInstanceState == null) {
			Log.d("SCROLLTABS", "onCreate FIRST TIME");
			if (getArguments() != null) {
				mEventId = getArguments().getString(Utils.ARG_EVENT_ID);
				mEvent = DTHelper.findEventById(mEventId);
			} else {
				Log.d("SCROLLTABS", "onCreate SUBSEQUENT TIME");
			}
		}
		setHasOptionsMenu(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		// Log.d("FRAGMENT LC", "Fragment_evDetail_DaSapere --> onCreateView");
		return inflater.inflate(R.layout.frag_ev_detail_dasapere, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Log.d("FRAGMENT LC",
		// "Fragment_evDetail_DaSapere --> onActivityCreated");

		mEvent = getEvent();

		// adapter = new EventDetailToKnowAdapter(getActivity(),
		// R.layout.event_toknow_row_item, getTag(), mEventId);
		adapter = new EventDetailToKnowAdapter(getActivity(),
				R.layout.event_info_child_item, getTag(), mEvent);
		getListView().setDividerHeight(0);


		getListView().setDivider(null);
		getListView().setDivider(
				getResources().getDrawable(R.color.transparent));
		setListAdapter(adapter);

		// List<ToKnow> toKnowList =
		// Utils.toKnowMapToList(getToKnowEventData());

		List<ToKnow> toKnowList = Utils.toKnowMapToList(getToKnowEventData());
		LinearLayout emptyDaSapere= (LinearLayout) getActivity().findViewById(R.id.empty_da_sapere);

		if (toKnowList.size()!=0)
		{
		adapter.addAll(toKnowList);
		adapter.notifyDataSetChanged();
		emptyDaSapere.setVisibility(View.GONE);
		}
		else {
			//no information string
			emptyDaSapere.setVisibility(View.VISIBLE);
			TextView message = (TextView) getActivity().findViewById(R.id.empty_add_info);  
			message.setText(Html.fromHtml(getString(R.string.empty_add_informations),new ImageGetter(),null));
		}


	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		super.onPrepareOptionsMenu(menu);
		menu.clear();
		getActivity().getMenuInflater().inflate(R.menu.detail_edit_da_sapere_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		 if (item.getItemId() == R.id.edit) {
			// call fragment edit with the event for parameter
			FragmentTransaction fragmentTransaction = getActivity()
					.getSupportFragmentManager().beginTransaction();
			Fragment_EvDetail_DaSapere_edit fragment = new Fragment_EvDetail_DaSapere_edit();

			Bundle args = new Bundle();

			args.putString(Utils.ARG_EVENT_ID, mEvent.getId());
			fragment.setArguments(args);

			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.content_frame, fragment,
					"event_edit");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
		return true;

	}

	private Map<String, List<String>> getToKnowEventData() {

		if (mEvent.getCustomData() == null) {
			mEvent.setCustomData(new HashMap<String, Object>());
		}

		Map<String, List<String>> toKnowMap = Utils
				.getCustomToKnowDataFromEvent(mEvent);

		if (toKnowMap == null) {

			Map<String, Object> customData = mEvent.getCustomData();

			customData.put(Constants.CUSTOM_TOKNOW,
					new LinkedHashMap<String, List<String>>());
			mEvent.setCustomData(customData);
			toKnowMap = (Map<String, List<String>>) mEvent.getCustomData().get(
					Constants.CUSTOM_TOKNOW);

		}

		if (toKnowMap.isEmpty()) {

			Log.i("DASAPERE", "DaSapere--> toKnowMap EMPTY");

			try {

				List<ToKnow> toKnowList = new ArrayList<ToKnow>();
				for (String field : CUSTOM_TOKNOW_FIELDS) {
					toKnowList.add(ToKnow.newCustomDataAttributeField(field,
							false, 3));
				}

				Map<String, Object> customData = new HashMap<String, Object>();
				toKnowMap = Utils.toKnowListToMap(toKnowList);
				customData.put(Constants.CUSTOM_TOKNOW, toKnowMap);
				mEvent.setCustomData(customData);

				// persistence
				new SCAsyncTask<ExplorerObject, Void, Boolean>(getActivity(),
						new UpdateEventProcessor(getActivity()))
						.execute(mEvent);
			} catch (Exception e) {
				Log.e(getClass().getName(),
						e.getMessage() != null ? e.getMessage() : "");
			}
		}

		return toKnowMap;
	}

	@Override
	public void onStart() {
		super.onStart();
		// Log.d("FRAGMENT LC", "Fragment_evDetail_DaSapere --> onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		// Log.d("FRAGMENT LC", "Fragment_evDetail_DaSapere --> onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		// Log.d("FRAGMENT LC", "Fragment_evDetail_DaSapere --> onPause");

	}

	@Override
	public void onStop() {
		super.onStop();
		// Log.d("FRAGMENT LC", "Fragment_evDetail_DaSapere --> onStop");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Log.d("FRAGMENT LC",
		// "Fragment_evDetail_DaSapere --> onSaveInstanceState");

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// Log.d("FRAGMENT LC", "Fragment_evDetail_DaSapere --> onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Log.d("FRAGMENT LC", "Fragment_evDetail_DaSapere --> onDestroy");

	}

	@Override
	public void onDetach() {
		super.onDetach();
		// Log.d("FRAGMENT LC", "Fragment_evDetail_DaSapere --> onDetach");
	}

	private ExplorerObject getEvent() {
		if (mEventId == null) {
			mEventId = getArguments().getString(Utils.ARG_EVENT_ID);
		}
		mEvent = DTHelper.findEventById(mEventId);
		return mEvent;
	}

	private class UpdateEventProcessor extends
			AbstractAsyncTaskProcessor<ExplorerObject, Boolean> {

		public UpdateEventProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(ExplorerObject... params)
				throws SecurityException, Exception {
			// to be enabled when the connection with the server is ok
			return DTHelper.saveEvent(params[0], mContext);
		}

		@Override
		public void handleResult(Boolean result) {
			if (getActivity() != null) {
				// getActivity().getSupportFragmentManager().popBackStack();

				// if (result) {
				// Toast.makeText(getActivity(), R.string.event_create_success,
				// Toast.LENGTH_SHORT).show();
				// } else {
				// Toast.makeText(getActivity(), R.string.update_success,
				// Toast.LENGTH_SHORT).show();
				// }
			}
		}
	}
	public class ImageGetter implements Html.ImageGetter {

	    public Drawable getDrawable(String source) {
	        int id;

	        if (source.equals("empty_info")) {
	            id = R.drawable.empty_info;
	        }

	        else {
	            return null;
	        }

	        Drawable d = getResources().getDrawable(id);
	        d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
	        return d;
	    }
	};
}
