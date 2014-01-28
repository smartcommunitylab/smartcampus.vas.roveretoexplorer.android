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
package eu.iescities.pilot.rovereto.roveretoexplorer.custom.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

import eu.iescities.pilot.rovereto.roveretoexplorer.custom.CategoryHelper;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.DTParamsHelper;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.BaseDTObject;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.CommunityData;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ExplorerObject;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ObjectFilter;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.Review;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ReviewObject;
import eu.iescities.pilot.rovereto.roveretoexplorer.fragments.search.WhenForSearch;
import eu.iescities.pilot.rovereto.roveretoexplorer.fragments.search.WhereForSearch;
import eu.iescities.pilot.rovereto.roveretoexplorer.map.MapManager;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.LocationHelper;
import eu.trentorise.smartcampus.android.common.navigation.NavigationHelper;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SuggestionHelper;
import eu.trentorise.smartcampus.network.JsonUtils;
import eu.trentorise.smartcampus.network.RemoteConnector;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.BasicObject;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.StorageConfigurationException;
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
import eu.trentorise.smartcampus.storage.sync.Utils;

public class DTHelper {

	/**
	 * 
	 */

	public static final String EVENTS = "events";
	public static final String EVENTS_P = "events/%s";
	public static final String RATE = "objects/%s/rate";
	public static final String ATTEND = "social/attend";
	public static final String NOT_ATTEND = "social/attend";
	public static final String FOLLOW = "objects/%s/follow";
	public static final String UNFOLLOW = "objects/%s/unfollow";
	public static final String SYNC_SERVICE = "sync";
	public static final String SERVICE = "/roveretoexplorer";

	private static final String SERVICE_ESPLORAROVERETO = "roveretoexplorer";
	public static final int SYNC_REQUIRED = 2;
	public static final int SYNC_NOT_REQUIRED = 0;
	public static final int SYNC_REQUIRED_FIRST_TIME = 3;
	public static final int SYNC_ONGOING = 1;
	private static final int CURR_DB = 4;

	// tutorial's stuff

	private static final String TUT_PREFS = "dt_tut_prefs";
	private static final String TOUR_PREFS = "dt_wantTour";
	private static final String FIRST_LAUNCH_PREFS = "dt_firstLaunch";
	private static SCAccessProvider accessProvider = null;
	private static String serviceUrl;
	// private static TerritoryService tService;
	//
	// public static TerritoryService gettService() {
	// return tService;
	// }

	private static DTHelper instance = null;

	// private static SCAccessProvider accessProvider = new
	// EmbeddedSCAccessProvider();

	// private SyncManager mSyncManager;
	private static Context mContext;
	private StorageConfiguration config = null;
	// private SyncStorageConfiguration config = null;
	private DTSyncStorage storage = null;
	// private static RemoteStorage remoteStorage = null;

	// private ProtocolCarrier mProtocolCarrier = null;

	private static LocationHelper mLocationHelper;

	private boolean syncInProgress = false;
	private FragmentActivity rootActivity = null;

	// static BasicProfile bp = null;

	// private String myToken = null;
	// private UserProfile userProfile = null;

	public static void init(final Context mContext) {
		if (instance == null)
			instance = new DTHelper(mContext);

		// serviceUrl = getAppUrl() + SERVICE_ESPLORAROVERETO;
		serviceUrl = getAppUrl();
		if (!serviceUrl.endsWith("/")) {
			serviceUrl += '/';
		}

		// new AsyncTask<Void, Void, BasicProfile>() {
		// @Override
		// protected BasicProfile doInBackground(Void... params) {
		// try {
		// String token =
		// SCAccessProvider.getInstance(mContext).readToken(mContext);
		// BasicProfileService service = new BasicProfileService(getAppUrl() +
		// "aac");
		// bp = service.getBasicProfile(token);
		// return bp;
		// } catch (Exception e) {
		// e.printStackTrace();
		// return null;
		// }
		// }
		// }.execute();
	}

	private static String getAppUrl() {
		String returnAppUrl = "";
		try {
			returnAppUrl = GlobalConfig.getAppUrl(mContext);
			if (!returnAppUrl.endsWith("/"))
				returnAppUrl = returnAppUrl.concat("/");
		} catch (Exception e) { // protocolexception
			e.printStackTrace();
		}
		return returnAppUrl;
	}

	public static SCAccessProvider getAccessProvider() {
		if (accessProvider == null)
			accessProvider = SCAccessProvider.getInstance(mContext);
		return accessProvider;
	}

	public static String getAuthToken() {
		String mToken = null;
		try {
			mToken = getAccessProvider().readToken(mContext);
		} catch (AACException e) {
			Log.e(DTHelper.class.getName(), "No token data");
		}
		return mToken;
	}

	// public static String getAuthToken() {
	// try {
	// return SCAccessProvider.getInstance(mContext).readToken(mContext);
	// }
	// catch (AACException e) {
	// return null;
	// }
	// }

	// public static String getUserId() {
	// // UserData data = getAccessProvider().readUserData(instance.mContext,
	// // null);
	// if (bp != null) {
	// return bp.getUserId();
	// } else
	// getUserProfile();
	// return null;
	// }

	private static void getUserProfile() {

	}

	private static DTHelper getInstance() throws DataException {
		if (instance == null)
			throw new DataException("DTHelper is not initialized");
		return instance;
	}

	protected DTHelper(Context mContext) {
		super();

		DTHelper.mContext = mContext;
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO) {
			RemoteConnector.setClientType(RemoteConnector.CLIENT_TYPE.CLIENT_WILDCARD);
		}
		accessProvider = SCAccessProvider.getInstance(mContext);
		DTParamsHelper.init(mContext);
		MapManager.initWithParam();
		// this.mSyncManager = new SyncManager(mContext,
		// DTSyncStorageService.class);
		config = new DTStorageConfiguration();

		// this.config = new SyncStorageConfiguration(sc,
		// GlobalConfig.getAppUrl(mContext), Constants.SYNC_SERVICE,
		// Constants.SYNC_INTERVAL);
		if (Utils.getDBVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) != CURR_DB) {
			Utils.writeObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME, 0);
		}
		this.storage = new DTSyncStorage(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME, CURR_DB,
				config);
		// this.mProtocolCarrier = new ProtocolCarrier(mContext,
		// DTParamsHelper.getAppToken());

		// LocationManager locationManager = (LocationManager)
		// mContext.getSystemService(Context.LOCATION_SERVICE);
		// locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
		// 0, 0, new DTLocationListener());
		// locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
		// 0, 0, new DTLocationListener());
		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// 0, 0, new DTLocationListener());
		setLocationHelper(new LocationHelper(mContext));
	}

	/**
	 * @return synchronization required status:
	 *         <ul>
	 *         <li>0 if no sync needed</li>
	 *         <li>1 if sync is ongoing</li>
	 *         <li>2 if sync is required</li>
	 *         <li>3 if sync is required first time</li>
	 *         </ul>
	 *         0 if no if the DB synchronization is required: the last
	 *         synchronization happened more than
	 *         {@link Constants#SYNC_INTERVAL} minutes ago or is ongoing.
	 * @throws DataException
	 * @throws NameNotFoundException
	 */
	public static int syncRequired() throws DataException, NameNotFoundException {
		if (getInstance().syncInProgress)
			return SYNC_ONGOING;
		long last = Utils.getLastObjectSyncTime(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME);
		if (System.currentTimeMillis() - last > Constants.SYNC_INTERVAL * 60 * 1000) {
			if (last > 0)
				return SYNC_REQUIRED;
			return SYNC_REQUIRED_FIRST_TIME;
		}
		return SYNC_NOT_REQUIRED;
	}

	/**
	 * Enable auot sync for the activity life-cycle
	 * 
	 * @throws NameNotFoundException
	 * @throws DataException
	 */
	public static void activateAutoSync() {
		try {
			String authority = Constants.getAuthority(mContext);
			Account account = new Account(eu.trentorise.smartcampus.ac.Constants.getAccountName(mContext),
					eu.trentorise.smartcampus.ac.Constants.getAccountType(mContext));

			ContentResolver.setIsSyncable(account, authority, 1);
			ContentResolver.setSyncAutomatically(account, authority, true);
			ContentResolver.addPeriodicSync(account, authority, new Bundle(), Constants.SYNC_INTERVAL * 60);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static FragmentActivity start(FragmentActivity activity) throws RemoteException, DataException,
			StorageConfigurationException, SecurityException, ConnectionException, ProtocolException,
			NameNotFoundException, AACException {
		getInstance().rootActivity = activity;
		try {
			if (getInstance().syncInProgress)
				return null;

			if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) <= 0) {
				Utils.writeObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME, 1L);
			}

			getInstance().syncInProgress = true;
			// TO DO
			getInstance().storage.synchronize(getAuthToken(), getAppUrl(), SYNC_SERVICE);
			// activateAutoSync();

			// getInstance().storage.synchronize(getAuthToken(),
			// GlobalConfig.getAppUrl(mContext),
			// Constants.SYNC_SERVICE);

		} finally {
			getInstance().syncInProgress = false;
		}
		return getInstance().rootActivity;
	}

	public static void synchronize() throws RemoteException, DataException, StorageConfigurationException,
			SecurityException, ConnectionException, ProtocolException, AACException {
		// TO DO
		getInstance().storage.synchronize(getAuthToken(), getAppUrl(), serviceUrl);
		// getInstance().storage.synchronize(getAuthToken(),
		// GlobalConfig.getAppUrl(mContext),
		// Constants.SYNC_SERVICE);
		// ContentResolver.requestSync(new
		// Account(eu.trentorise.smartcampus.ac.Constants.ACCOUNT_NAME,
		// eu.trentorise.smartcampus.ac.Constants.ACCOUNT_TYPE),
		// "eu.trentorise.smartcampus.dt", new Bundle());
	}

	public static void destroy() {
		// try {
		// String authority = Constants.getAuthority(mContext);
		// Account account = new Account(
		// eu.trentorise.smartcampus.ac.Constants.getAccountName(mContext),
		// eu.trentorise.smartcampus.ac.Constants.getAccountType(mContext));
		// ContentResolver.removePeriodicSync(account, authority, new Bundle());
		// ContentResolver.setSyncAutomatically(account, authority, false);
		// ContentResolver.setIsSyncable(account, authority, 0);
		// } catch (Exception e) {
		// Log.e(DTHelper.class.getName(), "Failed destroy: " + e.getMessage());
		// }
	}

	// public static Collection<POIObject> getAllPOI() throws DataException,
	// StorageConfigurationException, ConnectionException, ProtocolException,
	// SecurityException {
	// if (Utils.getObjectVersion(instance.mContext,
	// DTParamsHelper.getAppToken()) > 0) {
	// return getInstance().storage.getObjects(POIObject.class);
	// } else {
	// return Collections.emptyList();
	// }
	// }
	public static List<String> getAllPOITitles() {

		Cursor cursor = null;
		try {
			cursor = getInstance().storage.rawQuery("select title from pois", null);
			if (cursor != null) {
				List<String> result = new ArrayList<String>();
				cursor.moveToFirst();
				int i = 0;
				while (cursor.getPosition() < cursor.getCount()) {
					String v = cursor.getString(0);
					if (v != null && v.trim().length() > 0) {
						result.add(v.trim());
					}
					cursor.moveToNext();
					i++;
				}
				return result;
			}
		} catch (Exception e) {
			Log.e(DTHelper.class.getName(), "" + e.getMessage());
		} finally {
			try {
				getInstance().storage.cleanCursor(cursor);
			} catch (DataException e) {
			}
		}
		return Collections.emptyList();
	}

	/**
	 * return true in case of create and false in case of update
	 * 
	 * @param event
	 * @return
	 * @throws RemoteException
	 * @throws DataException
	 * @throws StorageConfigurationException
	 * @throws ConnectionException
	 * @throws ProtocolException
	 * @throws SecurityException
	 * @throws TerritoryServiceException
	 * @throws AACException
	 */
	public static Boolean saveEvent(ExplorerObject event) throws RemoteException, DataException,
			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException, AACException {
		Boolean result = null;
		if (event.getId() == null) {
			// TO DO
			event = createEvent(event, getAuthToken());
			result = true;
		} else {
			// TO DO
			event = updateEvent(event.getId(), event, getAuthToken());
			result = false;
		}
		// String requestService = null;
		// Method method = null;
		// Boolean result = null;
		// if (event.getId() == null) {
		// if (event.createdByUser())
		// requestService = Constants.SERVICE +
		// "/eu.trentorise.smartcampus.dt.model.UserExplorerObject";
		// else
		// throw new DataException("cannot create service object");
		// method = Method.POST;
		// result = true;
		// } else {
		// if (event.createdByUser())
		// requestService = Constants.SERVICE +
		// "/eu.trentorise.smartcampus.dt.model.UserExplorerObject/"
		// + event.getId();
		// else
		// requestService = Constants.SERVICE +
		// "/eu.trentorise.smartcampus.dt.model.ServiceExplorerObject/"
		// + event.getId();
		// method = Method.PUT;
		// result = false;
		// }
		// MessageRequest request = new
		// MessageRequest(GlobalConfig.getAppUrl(mContext),
		// requestService);
		// request.setMethod(method);
		// String json =
		// eu.trentorise.smartcampus.android.common.Utils.convertToJSON(event);
		// request.setBody(json);
		//
		// MessageResponse msg =
		// getInstance().mProtocolCarrier.invokeSync(request,
		// DTParamsHelper.getAppToken(),
		// getAuthToken());
		// // getRemote(instance.mContext, instance.token).create(poi);
		// ExplorerObject eventreturn =
		// eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(msg.getBody(),
		// ExplorerObject.class);
		synchronize();
		return result;
	}

	private static ExplorerObject updateEvent(String id, ExplorerObject event, String authToken) {
		if (event != null) {
			try {
				String json = RemoteConnector.putJSON(serviceUrl, String.format(EVENTS_P, id), JsonUtils.toJSON(event)
						.toString(), authToken);
				return JsonUtils.toObject(json, ExplorerObject.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static ExplorerObject createEvent(ExplorerObject event, String authToken) {
		if (event != null) {
			try {
				String json = RemoteConnector.postJSON(serviceUrl, EVENTS, JsonUtils.toJSON(event).toString(),
						authToken);
				return JsonUtils.toObject(json, ExplorerObject.class);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;

	}

	public static Collection<ExplorerObject> searchEventsByCategory(int position, int size, String text,
			String... inCategories) throws DataException, StorageConfigurationException, ConnectionException,
			ProtocolException, SecurityException, AACException {
		ArrayList<ExplorerObject> returnlist = new ArrayList<ExplorerObject>();

		if (inCategories == null || inCategories.length == 0)
			return Collections.emptyList();

		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));

		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			List<String> nonNullCategories = new ArrayList<String>();
			String where = "";
			for (int i = 0; i < categories.length; i++) {
				if (where.length() > 0)
					where += " or ";
				if (categories[i] != null) {
					nonNullCategories.add(categories[i]);
					where += " type = ?";
				} else {
					where += " type is null";
				}
			}
			if (where.length() > 0) {
				where = "(" + where + ")";
			}
			List<String> parameters = nonNullCategories;

			if (text != null) {
				where += "AND ( events MATCH ? ) AND fromTime > " + getCurrentDateTimeForSearching();
				parameters.add(text);
			}
			Collection<ExplorerObject> events = getInstance().storage.query(ExplorerObject.class, where,
					parameters.toArray(new String[parameters.size()]), position, size, "fromTime ASC");
			for (ExplorerObject event : events) {
				returnlist.add(event);
			}
			return returnlist;
		} else {
			for (int c = 0; c < categories.length; c++) {
				ObjectFilter filter = new ObjectFilter();
				filter.setTypes(Arrays.asList(categories));
				filter.setSkip(position);
				filter.setLimit(size);
				// TO DO
				returnlist.addAll(getEventsRemote(filter, getAuthToken()));
			}
			return returnlist;
		}
	}

	private static List<ExplorerObject> getEventsRemote(ObjectFilter filter, String authToken) {
		try {
			Map<String, Object> params = null;
			if (filter == null)
				params = Collections.<String, Object> emptyMap();
			else
				params = Collections.<String, Object> singletonMap("filter", JsonUtils.toJSON(filter));
			String json = RemoteConnector.getJSON(serviceUrl, EVENTS, authToken, params);
			return JsonUtils.toObjectList(json, ExplorerObject.class);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static long getCurrentDateTimeForSearching() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.add(Calendar.DATE, -1);
		return c.getTimeInMillis();
	}

	public static Collection<ExplorerObject> getEvents(int position, int size, String... inCategories)
			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException,
			SecurityException {

		if (inCategories == null || inCategories.length == 0)
			return Collections.emptyList();

		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));

		if (Utils.getObjectVersion(instance.mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			List<String> nonNullCategories = new ArrayList<String>();
			String where = "";
			for (int i = 0; i < categories.length; i++) {
				if (where.length() > 0)
					where += " or ";
				if (categories[i] != null) {
					nonNullCategories.add(categories[i]);
					where += " category = ?";
				} else {
					where += " type is null";
				}
			}
			if (where.length() > 0) {
				where = "(" + where + ")";
			}
			where += "AND fromTime > " + getCurrentDateTimeForSearching();
			return getInstance().storage.query(ExplorerObject.class, "", null, position, size, "fromTime ASC");
		} else {
			// ArrayList<ExplorerObject> result = new
			// ArrayList<ExplorerObject>();
			List<ExplorerObject> result = null;
			for (int c = 0; c < categories.length; c++) {
				ObjectFilter filter = new ObjectFilter();
				filter.setTypes(Arrays.asList(categories));
				filter.setSkip(position);
				filter.setLimit(size);
				// TO DO
				result = getEventsRemote(filter, getAuthToken());
				// returnlist.addAll(eu.trentorise.smartcampus.trentinofamiglia.custom.Utils.convertToLocalEvent(events));
			}
			return result;
		}
	}

	public static Collection<ExplorerObject> getEventsByCategories(int position, int size, String... inCategories)
			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException,
			SecurityException {

		if (inCategories == null || inCategories.length == 0)
			return Collections.emptyList();

		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));

		if (Utils.getObjectVersion(instance.mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			List<String> nonNullCategories = new ArrayList<String>();
			String where = "";
			for (int i = 0; i < categories.length; i++) {
				if (where.length() > 0)
					where += " or ";
				if (categories[i] != null) {
					nonNullCategories.add(categories[i]);
					where += " category = ?";
				} else {
					where += " category is null";
				}
			}
			if (where.length() > 0) {
				where = "(" + where + ")";
			}
			where += " AND fromTime > " + getCurrentDateTimeForSearching();
			return getInstance().storage.query(ExplorerObject.class, where,
					nonNullCategories.toArray(new String[nonNullCategories.size()]), position, size, "fromTime ASC");
		} else {
			// ArrayList<ExplorerObject> result = new
			// ArrayList<ExplorerObject>();
			List<ExplorerObject> result = null;
			for (int c = 0; c < categories.length; c++) {
				ObjectFilter filter = new ObjectFilter();
				filter.setTypes(Arrays.asList(categories));
				filter.setSkip(position);
				filter.setLimit(size);
				// TO DO
				result = getEventsRemote(filter, getAuthToken());
				// returnlist.addAll(eu.trentorise.smartcampus.trentinofamiglia.custom.Utils.convertToLocalEvent(events));
			}
			return result;
		}
	}

	// public static Collection<ExplorerObject> getEventsByCategories(int
	// position,
	// int size, String... inCategories)
	// throws DataException, StorageConfigurationException, ConnectionException,
	// ProtocolException,
	// SecurityException, AACException {
	// ArrayList<ExplorerObject> returnlist = new ArrayList<ExplorerObject>();
	//
	// if (inCategories == null || inCategories.length == 0)
	// return Collections.emptyList();
	//
	// String[] categories = CategoryHelper.getAllCategories(new
	// HashSet<String>(Arrays.asList(inCategories)));
	//
	// if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(),
	// Constants.SYNC_DB_NAME) > 0) {
	// List<String> nonNullCategories = new ArrayList<String>();
	// String where = "";
	// for (int i = 0; i < categories.length; i++) {
	// if (where.length() > 0)
	// where += " or ";
	// if (categories[i] != null) {
	// nonNullCategories.add(categories[i]);
	// where += " type = ?";
	// } else {
	// where += " type is null";
	// }
	// }
	// if (where.length() > 0) {
	// where = "(" + where + ")";
	// }
	// // if (where.length() > 0) where += " AND ";
	// // where += "fromTime > " + getCurrentDateTimeForSearching();
	// Collection<EventObjectForBean> events =
	// getInstance().storage.query(EventObjectForBean.class, where,
	// nonNullCategories.toArray(new String[nonNullCategories.size()]),
	// position, size, "fromTime ASC");
	// for (EventObjectForBean eventBean : events) {
	// LocalExplorerObject event = new LocalExplorerObject();
	// event.setEventFromEventObjectForBean(eventBean);
	// returnlist.add(event);
	// }
	// return returnlist;
	// } else {
	// for (int c = 0; c < categories.length; c++) {
	// ObjectFilter filter = new ObjectFilter();
	// filter.setTypes(Arrays.asList(categories));
	// filter.setSkip(position);
	// filter.setLimit(size);
	// List<ExplorerObject> events = tService.getEvents(filter, getAuthToken());
	// returnlist
	// .addAll(eu.iescities.pilot.rovereto.roveretoexplorer.custom.Utils.convertToLocalEvent(events));
	//
	// }
	// return returnlist;
	//
	// }
	// }

	// public static Collection<ExplorerObject> searchTodayEvents(int position,
	// int
	// size, String text) throws DataException,
	// StorageConfigurationException, ConnectionException, ProtocolException,
	// SecurityException, AACException {
	// ArrayList<ExplorerObject> returnlist = new ArrayList<ExplorerObject>();
	//
	// // Date now = new Date();
	// Calendar cal = Calendar.getInstance();
	// // cal.setTime(now);
	// cal.set(Calendar.HOUR_OF_DAY, 0);
	// cal.set(Calendar.MINUTE, 0);
	// cal.set(Calendar.SECOND, 0);
	// cal.set(Calendar.MILLISECOND, 0);
	//
	// cal.add(Calendar.DAY_OF_YEAR, 1);
	// Date tomorrow = cal.getTime();
	//
	// if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(),
	// Constants.SYNC_DB_NAME) > 0) {
	// Collection<EventObjectForBean> events =
	// getInstance().storage.query(EventObjectForBean.class, "( toTime > "
	// + getCurrentDateTimeForSearching() + " AND fromTime < " +
	// tomorrow.getTime() + " ) ", null,
	// position, size, "fromTime ASC");
	// /* convert from eventobj to localeventobj */
	// for (EventObjectForBean eventBean : events) {
	// ExplorerObject event = new ExplorerObject();
	// event.setEventFromEventObjectForBean(eventBean);
	// returnlist.add(event);
	// }
	// return returnlist;
	// } else {
	// ObjectFilter filter = new ObjectFilter();
	// Map<String, Object> criteria = new HashMap<String, Object>(1);
	// criteria.put("text", text);
	// filter.setCriteria(criteria);
	// filter.setSkip(position);
	// filter.setLimit(size);
	// List<ExplorerObject> events = tService.getEvents(filter, getAuthToken());
	// returnlist.addAll(eu.iescities.pilot.rovereto.roveretoexplorer.custom.Utils.convertToLocalEvent(events));
	// return returnlist;
	// }
	// }
	public static Collection<ExplorerObject> searchTodayEvents(int position, int size, String text)
			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException,
			SecurityException {
		// Date now = new Date();
		Calendar cal = Calendar.getInstance();
		// cal.setTime(now);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		cal.add(Calendar.DAY_OF_YEAR, 1);
		Date tomorrow = cal.getTime();

		if (Utils.getObjectVersion(instance.mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			return getInstance().storage.query(ExplorerObject.class, "( toTime > " + getCurrentDateTimeForSearching()
					+ " AND fromTime < " + tomorrow.getTime() + " ) ", null, position, size, "fromTime ASC");
		} else {
			ObjectFilter filter = new ObjectFilter();
			Map<String, Object> criteria = new HashMap<String, Object>(1);
			criteria.put("text", text);
			filter.setCriteria(criteria);
			filter.setSkip(position);
			filter.setLimit(size);
			// TO DO
			List<ExplorerObject> events = getEventsRemote(filter, getAuthToken());
			return events;
		}
	}

	public static Collection<ExplorerObject> getEventsByPOI(int position, int size, String poiId) throws DataException,
			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException, AACException {
		ArrayList<ExplorerObject> returnlist = new ArrayList<ExplorerObject>();

		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			Collection<ExplorerObject> events = getInstance().storage.query(ExplorerObject.class,
					"poiId = ? AND fromTime > " + getCurrentDateTimeForSearching(), new String[] { poiId }, position,
					size, "fromTime ASC");
			for (ExplorerObject event : events) {
				returnlist.add(event);
			}
			return returnlist;
		} else {
			ObjectFilter filter = new ObjectFilter();
			Map<String, Object> criteria = new HashMap<String, Object>(1);
			criteria.put("poiId", poiId);
			filter.setCriteria(criteria);
			filter.setSkip(position);
			filter.setLimit(size);
			// TO DO
			return getEventsRemote(filter, getAuthToken());

			// return Collections.emptyList();
		}
	}

	public static List<SemanticSuggestion> getSuggestions(CharSequence suggest) throws ConnectionException,
			ProtocolException, SecurityException, DataException, AACException {
		return SuggestionHelper.getSuggestions(suggest, mContext, GlobalConfig.getAppUrl(mContext), getAuthToken(),
				DTParamsHelper.getAppToken());
	}

	// private static RemoteStorage getRemote(Context mContext, String token)
	// throws ProtocolException, DataException {
	// if (remoteStorage == null) {
	// remoteStorage = new RemoteStorage(mContext,
	// DTParamsHelper.getAppToken());
	// }
	// remoteStorage.setConfig(token, GlobalConfig.getAppUrl(mContext),
	// Constants.SERVICE);
	// return remoteStorage;
	// }

	public static void endAppFailure(Activity activity, int id) {
		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
		activity.finish();
	}

	public static void showFailure(Activity activity, int id) {
		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
	}

	public static boolean deleteEvent(ExplorerObject ExplorerObject) throws DataException, ConnectionException,
			ProtocolException, SecurityException, RemoteException, StorageConfigurationException, AACException {
		if (ExplorerObject.getId() != null) {
			// TO DO
			deleteEvent(ExplorerObject.getId(), getAuthToken());
			synchronize();
			return true;
		}
		return false;
	}

	private static void deleteEvent(String id, String authToken) {
		if (id != null) {
			try {
				RemoteConnector.deleteJSON(serviceUrl, String.format(EVENTS_P, id), authToken);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// public static int rate(BaseDTObject event, int rating) throws
	// ConnectionException, ProtocolException,
	// SecurityException, DataException, RemoteException,
	// StorageConfigurationException, AACException {
	// int returnValue = tService.rate(event.getId(), rating, getAuthToken());
	// // MessageRequest request = new
	// // MessageRequest(GlobalConfig.getAppUrl(mContext),
	// // Constants.SERVICE
	// // + "/objects/" + event.getId() + "/rate");
	// // request.setMethod(Method.PUT);
	// // String query = "rating=" + rating;
	// // request.setQuery(query);
	// // String response = getInstance().mProtocolCarrier.invokeSync(request,
	// // DTParamsHelper.getAppToken(),
	// // getAuthToken()).getBody();
	// synchronize();
	// return returnValue;
	// }
	public static int rate(BaseDTObject event, int rating) throws ConnectionException, ProtocolException,
			SecurityException, DataException, RemoteException, StorageConfigurationException, AACException {
		// TO DO
		int returnValue = rate(event.getId(), rating, getAuthToken());
		synchronize();
		return returnValue;
	}

	private static int rate(String id, int rating, String authToken) {
		if (id != null) {
			try {
				Map<String, Object> params = Collections.<String, Object> singletonMap("rating", rating);
				String json = RemoteConnector.putJSON(serviceUrl, String.format(RATE, id), null, authToken, params);
				return Integer.parseInt(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 3;
	}

	public static CommunityData review(BaseDTObject object, Review review) throws ConnectionException,
			ProtocolException, SecurityException, DataException, RemoteException, StorageConfigurationException,
			AACException, java.lang.SecurityException, eu.trentorise.smartcampus.network.RemoteException {
		String string = RemoteConnector.postJSON(getAppUrl(),
				SERVICE_ESPLORAROVERETO + "/social/review/" + object.getId(), JsonUtils.toJSON(review), getAuthToken());
		synchronize();

		return JsonUtils.toObject(string, CommunityData.class);
	}

	public static List<Review> loadReviews(String id) throws java.lang.SecurityException,
			eu.trentorise.smartcampus.network.RemoteException {
		String string = RemoteConnector.getJSON(getAppUrl(), SERVICE_ESPLORAROVERETO + "/social/readReviews/" + id,
				getAuthToken());
		return JsonUtils.toObject(string, ReviewObject.class).getReviews();
	}

	// public static BaseDTObject follow(BaseDTObject object) throws
	// ConnectionException, ProtocolException,
	// SecurityException, DataException, RemoteException,
	// StorageConfigurationException, AACException {
	// BaseDTObject returnObj = null;
	//
	// // try {
	// // if (object instanceof ExplorerObject) {
	// // returnObj = DTHelper.followEvent(object.getId(), getAuthToken());
	// //
	// //
	// // }
	// // // catch (TerritoryServiceException e) {
	// // // e.printStackTrace();
	// // // }
	// MessageRequest request = new
	// MessageRequest(GlobalConfig.getAppUrl(mContext), Constants.SERVICE +
	// "/objects/"
	// + object.getId() + "/follow");
	// request.setMethod(Method.PUT);
	// String query = "idTopic=" + idTopic;
	// request.setQuery(query);
	// getInstance().mProtocolCarrier.invokeSync(request,
	// DTParamsHelper.getAppToken(), getAuthToken());
	// synchronize();
	// return returnObj;
	// }
	//
	// private static BaseDTObject followEvent(String id, String authToken) {
	// return null;
	// }
	//
	// public static void unfollow(BaseDTObject object) throws
	// ConnectionException, ProtocolException, SecurityException,
	// DataException, RemoteException, StorageConfigurationException,
	// AACException {
	// // try {
	// // if (object instanceof ExplorerObject) {
	// // DTHelper.unfollowEvent(object.getId(), getAuthToken());
	// //
	// // }
	// // }
	// // // catch (TerritoryServiceException e) {
	// // // e.printStackTrace();
	// // // }
	// MessageRequest request = new
	// MessageRequest(GlobalConfig.getAppUrl(mContext), Constants.SERVICE +
	// "/objects/"
	// + object.getId() + "/unfollow");
	// request.setMethod(Method.PUT);
	// getInstance().mProtocolCarrier.invokeSync(request,
	// DTParamsHelper.getAppToken(), getAuthToken());
	// synchronize();
	// }
	public static BaseDTObject follow(BaseDTObject event, String idTopic) throws ConnectionException,
			ProtocolException, SecurityException, DataException, RemoteException, StorageConfigurationException,
			AACException {
		// TO DO
		BaseDTObject returnObj = null;

		try {
			returnObj = followEvent(event.getId(), getAuthToken(), true);

		} catch (Exception e) {
			e.printStackTrace();
		}

		synchronize();
		return returnObj;
	}

	private static ExplorerObject followEvent(String id, String authToken, boolean add) {
		if (id != null) {
			try {

				String json = RemoteConnector
						.putJSON(serviceUrl, String.format(add ? FOLLOW : UNFOLLOW, id), authToken);
				return JsonUtils.toObject(json, ExplorerObject.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void unfollow(BaseDTObject event) throws ConnectionException, ProtocolException, SecurityException,
			DataException, RemoteException, StorageConfigurationException, AACException {
		// TO DO
		try {

			unfollowEvent(event.getId(), getAuthToken(), false);

		} catch (Exception e) {
			e.printStackTrace();
		}
		synchronize();
	}

	private static void unfollowEvent(String id, String authToken, boolean add) {
		if (id != null) {
			try {

				String json = RemoteConnector
						.putJSON(serviceUrl, String.format(add ? FOLLOW : UNFOLLOW, id), authToken);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// public static ExplorerObject attend(BaseDTObject event) throws
	// ConnectionException, ProtocolException,
	// SecurityException, DataException, RemoteException,
	// StorageConfigurationException, AACException {
	// LocalExplorerObject returnEvent = new LocalExplorerObject();
	// ExplorerObject newEvent = tService.myEvent(event.getId(), true,
	// getAuthToken());
	// EventObjectForBean newEventBean = new EventObjectForBean();
	// newEventBean.setObjectForBean(newEvent);
	// returnEvent.setEventFromEventObjectForBean(newEventBean);
	// synchronize();
	// return returnEvent;
	// }
	//
	// public static LocalExplorerObject notAttend(BaseDTObject event) throws
	// ConnectionException, ProtocolException,
	// SecurityException, DataException, RemoteException,
	// StorageConfigurationException, AACException {
	// LocalExplorerObject returnEvent = new LocalExplorerObject();
	// ExplorerObject newEvent = tService.myEvent(event.getId(), false,
	// getAuthToken());
	// EventObjectForBean newEventBean = new EventObjectForBean();
	// newEventBean.setObjectForBean(newEvent);
	// returnEvent.setEventFromEventObjectForBean(newEventBean);
	// synchronize();
	// return returnEvent;
	// }
	public static ExplorerObject attend(BaseDTObject event) throws ConnectionException, ProtocolException,
			SecurityException, DataException, RemoteException, StorageConfigurationException, AACException {
		// TO DO
		ExplorerObject newEvent = myEvent(event.getId(), true, getAuthToken());

		synchronize();
		return newEvent;
	}

	public static ExplorerObject notAttend(BaseDTObject event) throws ConnectionException, ProtocolException,
			SecurityException, DataException, RemoteException, StorageConfigurationException {
		// TO DO

		ExplorerObject newEvent = myEvent(event.getId(), false, getAuthToken());

		return newEvent;
	}

	private static ExplorerObject myEvent(String id, boolean add, String authToken) {
		if (id != null) {
			try {
				String json = postJSON(getAppUrl(), "social/attend/" + id+"/"+JsonUtils.toJSON(add),
						"", getAuthToken());

				return JsonUtils.toObject(json, ExplorerObject.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String postJSON(String host, String service, String body, String token) throws SecurityException,
			RemoteException {
		return postJSON(host, service, body, token, null);
	}

	public enum CLIENT_TYPE {
		CLIENT_NORMAL, CLIENT_WILDCARD, CLIENT_ACCEPTALL
	};

	private static CLIENT_TYPE clientType = CLIENT_TYPE.CLIENT_NORMAL;

	private static final String DEFAULT_CHARSET = "UTF-8";
	/** */
	protected static final String RH_ACCEPT = "Accept";
	/** */
	protected static final String RH_AUTH_TOKEN = "Authorization";
	public static int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;

	public static String postJSON(String host, String service, String body, String token, Map<String, Object> parameters)
			throws SecurityException, RemoteException {

		String queryString = generateQueryString(parameters);
		final HttpResponse resp;
		final HttpPost post = new HttpPost(normalizeURL(host + service) + queryString);

		post.setHeader(RH_ACCEPT, "application/json");
		post.setHeader(RH_AUTH_TOKEN, bearer(token));

		try {
			StringEntity input = new StringEntity(body, DEFAULT_CHARSET);
			input.setContentType("application/json");
			post.setEntity(input);

			resp = getHttpClient().execute(post);
			String response = EntityUtils.toString(resp.getEntity(), DEFAULT_CHARSET);
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return response;
			}
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN
					|| resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				throw new Exception();
			}

			String msg = "";
			try {
				msg = response.substring(response.indexOf("<h1>") + 4,
						response.indexOf("</h1>", response.indexOf("<h1>")));
			} catch (Exception e) {
				msg = resp.getStatusLine().toString();
			}
			throw new RemoteException(msg);
		} catch (Exception e) {

			e.printStackTrace();
			return "";

		}
	}

	protected static HttpClient getHttpClient() {
		HttpClient httpClient = null;
		switch (clientType) {
		case CLIENT_WILDCARD:
			httpClient = getWildcartHttpClient(null);
			break;
		case CLIENT_ACCEPTALL:
			httpClient = getAcceptAllHttpClient(null);
			break;
		default:
			httpClient = getDefaultHttpClient(null);
		}

		final HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
		HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
		ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
		return httpClient;
	}

	private static HttpClient getDefaultHttpClient(HttpParams inParams) {
		if (inParams != null) {
			return new DefaultHttpClient(inParams);
		} else {
			return new DefaultHttpClient();
		}
	}

	private static HttpClient getAcceptAllHttpClient(HttpParams inParams) {
		HttpClient client = null;

		HttpParams params = inParams != null ? inParams : new BasicHttpParams();

		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

			// IMPORTANT: use CustolSSLSocketFactory for 2.2
			SSLSocketFactory sslSocketFactory = new CustomSSLSocketFactory(trustStore);
			sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			registry.register(new Scheme("https", sslSocketFactory, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			client = new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			client = new DefaultHttpClient(params);
		}

		return client;
	}

	private static class CustomSSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public CustomSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
				UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	private static HttpClient getWildcartHttpClient(HttpParams inParams) {
		HttpClient client = null;

		HttpParams params = inParams != null ? inParams : new BasicHttpParams();

		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

			SSLSocketFactory sslSocketFactory = new CustomSSLSocketFactory(trustStore);
			final X509HostnameVerifier delegate = sslSocketFactory.getHostnameVerifier();
			if (!(delegate instanceof WildcardVerifier)) {
				sslSocketFactory.setHostnameVerifier(new WildcardVerifier(delegate));
			}
			registry.register(new Scheme("https", sslSocketFactory, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			client = new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			client = new DefaultHttpClient(params);
		}

		return client;
	}

	private static class WildcardVerifier extends AbstractVerifier {
		private final X509HostnameVerifier delegate;

		public WildcardVerifier(final X509HostnameVerifier delegate) {
			this.delegate = delegate;
		}

		@Override
		public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
			boolean ok = false;
			try {
				delegate.verify(host, cns, subjectAlts);
			} catch (SSLException e) {
				for (String cn : cns) {
					if (cn.startsWith("*.")) {
						try {
							delegate.verify(host, new String[] { cn.substring(2) }, subjectAlts);
							ok = true;
						} catch (Exception e1) {
							throw new SSLException(e1);
						}
					}
				}
				if (!ok) {
					throw e;
				}
			}
		}
	}

	private static String normalizeURL(String uriString) throws RemoteException {
		try {
			URL url = new URL(uriString);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), url.getRef());
			uriString = uri.toURL().toString();
		} catch (Exception e) {
			throw new RemoteException(e.getMessage());
		}
		return uriString;
	}

	protected static String bearer(String token) {
		return "Bearer " + token;
	}

	protected static String generateQueryString(Map<String, Object> parameters) {
		String queryString = "?";
		if (parameters != null) {
			for (String param : parameters.keySet()) {
				Object value = parameters.get(param);
				if (value == null) {
					if (queryString.length() > 1) {
						queryString += "&";
					}
					queryString += param + "=";
				} else if (value instanceof List) {
					for (Object v : ((List<?>) value)) {
						if (queryString.length() > 1) {
							queryString += "&";
						}
						queryString += param + "=" + encodeValue(v.toString());
					}
				} else {
					if (queryString.length() > 1) {
						queryString += "&";
					}
					queryString += param + "=" + encodeValue(value.toString());
				}

			}
		}
		return queryString.length() > 1 ? queryString : "";
	}

	protected static String encodeValue(String value) {
		try {
			return URLEncoder.encode(value, "utf8");
		} catch (UnsupportedEncodingException e) {
			return value;
		}
	}

	public static ExplorerObject findEventByEntityId(String entityId) throws DataException,
			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
		return findDTObjectByEntityId(ExplorerObject.class, entityId);
	}

	@SuppressWarnings("rawtypes")
	private static <T extends BaseDTObject> T findDTObjectByEntityId(Class<T> cls, String entityId)
			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException,
			SecurityException {
		T returnObject = null;
		String where = "entityId = '" + entityId + "'";
		Collection<T> coll = getInstance().storage.query(cls, where, null);
		if (coll != null && coll.size() == 1)
			returnObject = coll.iterator().next();
		if (returnObject == null)
			returnObject = findLocalDTOObjectByEntityId(cls, entityId);
		return returnObject;

	}

	@SuppressWarnings("rawtypes")
	private static <T extends BaseDTObject> T findLocalDTOObjectByEntityId(Class<T> cls, String entityId) {
		try {
			DTHelper.synchronize();
			T returnObject = null;
			String where = "entityId = '" + entityId + "'";
			Collection<T> coll = getInstance().storage.query(cls, where, null);
			if (coll != null && coll.size() == 1)
				returnObject = coll.iterator().next();
			return returnObject;
		} catch (Exception e) {
			return null;
		}

	}

	// public static Boolean saveStory(StoryObject storyObject) throws
	// RemoteException, DataException,
	// StorageConfigurationException, ConnectionException, ProtocolException,
	// SecurityException, AACException {
	// boolean returnvalue = true;
	// if (storyObject.getId() == null) {
	// tService.createStory(storyObject, getAuthToken());
	// returnvalue = true;
	// } else {
	// storyObject = tService.updateStory(storyObject.getId(), storyObject,
	// getAuthToken());
	// returnvalue = false;
	// }
	// // String requestService = null;
	// // Method method = null;
	// // Boolean result = null;
	// // if (storyObject.getId() == null) {
	// // // create
	// // requestService = Constants.SERVICE +
	// // "/eu.trentorise.smartcampus.dt.model.UserStoryObject";
	// // method = Method.POST;
	// // result = true;
	// // } else {
	// // // update
	// // requestService = Constants.SERVICE +
	// // "/eu.trentorise.smartcampus.dt.model.UserStoryObject/"
	// // + storyObject.getId();
	// // method = Method.PUT;
	// // result = false;
	// // }
	// // MessageRequest request = new
	// // MessageRequest(GlobalConfig.getAppUrl(mContext),
	// // requestService);
	// // request.setMethod(method);
	// // String json =
	// //
	// eu.trentorise.smartcampus.android.common.Utils.convertToJSON(storyObject);
	// // request.setBody(json);
	// //
	// // getInstance().mProtocolCarrier.invokeSync(request,
	// // DTParamsHelper.getAppToken(), getAuthToken());
	// // getRemote(mContext, instance.token).create(poi);
	// synchronize();
	// return returnvalue;
	// }

	// public static Collection<StoryObject> getStoryByCategory(int position,
	// int size, String... inCategories)
	// throws DataException, StorageConfigurationException, ConnectionException,
	// ProtocolException,
	// SecurityException, AACException {
	// ArrayList<StoryObject> returnlist = new ArrayList<StoryObject>();
	//
	// if (inCategories == null || inCategories.length == 0)
	// return Collections.emptyList();
	//
	// String[] categories = CategoryHelper.getAllCategories(new
	// HashSet<String>(Arrays.asList(inCategories)));
	//
	// if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(),
	// Constants.SYNC_DB_NAME) > 0) {
	// List<String> nonNullCategories = new ArrayList<String>();
	// String where = "";
	// for (int i = 0; i < categories.length; i++) {
	// if (where.length() > 0)
	// where += " or ";
	// if (categories[i] != null) {
	// nonNullCategories.add(categories[i]);
	// where += " type = ?";
	// } else {
	// where += " type is null";
	// }
	// }
	// if (where.length() > 0) {
	// where = "(" + where + ")";
	// }
	// Collection<StoryObjectForBean> stories =
	// getInstance().storage.query(StoryObjectForBean.class, where,
	// nonNullCategories.toArray(new String[nonNullCategories.size()]),
	// position, size, "title ASC");
	// for (StoryObjectForBean storyBean : stories) {
	// returnlist.add(storyBean.getObjectForBean());
	// }
	// return returnlist;
	//
	// } else {
	// for (int c = 0; c < categories.length; c++) {
	// ObjectFilter filter = new ObjectFilter();
	// filter.setTypes(Arrays.asList(categories));
	// filter.setSkip(position);
	// filter.setLimit(size);
	// returnlist.addAll(tService.getStories(filter, getAuthToken()));
	// }
	// return returnlist;
	// }
	// }

	// public static Collection<StoryObject> searchStories(int position, int
	// size, String text) throws DataException,
	// StorageConfigurationException, ConnectionException, ProtocolException,
	// SecurityException,
	// AACException {
	// ArrayList<StoryObject> returnlist = new ArrayList<StoryObject>();
	//
	// if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(),
	// Constants.SYNC_DB_NAME) > 0) {
	// if (text == null || text.trim().length() == 0) {
	// Collection<StoryObjectForBean> stories =
	// getInstance().storage.getObjects(StoryObjectForBean.class);
	// for (StoryObjectForBean storyBean : stories) {
	// returnlist.add(storyBean.getObjectForBean());
	// }
	// return returnlist;
	// }
	// Collection<StoryObjectForBean> stories =
	// getInstance().storage.query(StoryObjectForBean.class,
	// "stories MATCH ? ", new String[] { text }, position, size, "title ASC");
	// for (StoryObjectForBean storyBean : stories) {
	// returnlist.add(storyBean.getObjectForBean());
	// }
	// return returnlist;
	// } else {
	// ObjectFilter filter = new ObjectFilter();
	// Map<String, Object> criteria = new HashMap<String, Object>(1);
	// criteria.put("text", text);
	// filter.setCriteria(criteria);
	// filter.setSkip(position);
	// filter.setLimit(size);
	// return tService.getStories(filter, getAuthToken());
	// }
	// }

	// public static Boolean deleteStory(StoryObject storyObject) throws
	// DataException, ConnectionException,
	// ProtocolException, SecurityException, RemoteException,
	// StorageConfigurationException, AACException {
	// if (storyObject.getId() != null) {
	// tService.deleteStory(storyObject.getId(), getAuthToken());
	// synchronize();
	// return true;
	// }
	// return false;
	// }

	// public static StoryObjectForBean findStoryByEntityId(String storyId)
	// throws DataException,
	// StorageConfigurationException, ConnectionException, ProtocolException,
	// SecurityException {
	// return findDTObjectByEntityId(StoryObjectForBean.class, storyId);
	// }
	//
	// public static StoryObject findStoryById(String storyId) {
	// try {
	// StoryObjectForBean story = getInstance().storage.getObjectById(storyId,
	// StoryObjectForBean.class);
	// return story.getObjectForBean();
	// } catch (Exception e) {
	// return null;
	// }
	// }

	// try {
	// StoryObject story = tService.getStory(storyId, getAuthToken());
	// return story;
	// } catch (Exception e) {
	// return null;
	// }

	// public static ArrayList<POIObject> getPOIBySteps(List<StepObject> steps)
	// throws DataException,
	// StorageConfigurationException, ConnectionException, ProtocolException,
	// SecurityException {
	//
	// // usare findpoibyid nella lista steps
	//
	// ArrayList<POIObject> poiList = new ArrayList<POIObject>();
	// for (StepObject step : steps) {
	// POIObject poiStep = findPOIById(step.getPoiId());
	// poiList.add(poiStep);
	// }
	// return poiList;
	//
	// }
	//
	// public static StoryObject addToMyStories(BaseDTObject story) throws
	// ConnectionException, ProtocolException,
	// SecurityException, DataException, RemoteException,
	// StorageConfigurationException,
	// AACException {
	// // LocalExplorerObject returnEvent = new LocalExplorerObject();
	// // ExplorerObject newEvent = tService.myEvent(event.getId(), true,
	// // getAuthToken());
	// // EventObjectForBean newEventBean = new EventObjectForBean();
	// // newEventBean.setObjectForBean(newEvent);
	// // returnEvent.setEventFromEventObjectForBean(newEventBean);
	// // return returnEvent;
	//
	// StoryObject returnObject = tService.myStory(story.getId(), true,
	// getAuthToken());
	// synchronize();
	// return returnObject;
	// }
	//
	// public static StoryObject removeFromMyStories(BaseDTObject story) throws
	// ConnectionException, ProtocolException,
	// SecurityException, DataException, RemoteException,
	// StorageConfigurationException,
	// AACException {
	// StoryObject returnObject = tService.myStory(story.getId(), false,
	// getAuthToken());
	// synchronize();
	// return returnObject;
	// }
	//
	// public static Collection<StoryObject> getMyStories(int position, int
	// size) throws DataException,
	// StorageConfigurationException, ConnectionException, ProtocolException,
	// SecurityException,
	// AACException {
	// ArrayList<StoryObject> returnlist = new ArrayList<StoryObject>();
	//
	// if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(),
	// Constants.SYNC_DB_NAME) > 0) {
	// Collection<StoryObjectForBean> stories =
	// getInstance().storage.query(StoryObjectForBean.class,
	// "attending IS NOT NULL", null, position, size, "title ASC");
	// for (StoryObjectForBean storyBean : stories) {
	// returnlist.add(storyBean.getObjectForBean());
	// }
	// return returnlist;
	// } else {
	// ObjectFilter filter = new ObjectFilter();
	// filter.setMyObjects(true);
	// filter.setSkip(position);
	// filter.setLimit(size);
	// return tService.getStories(filter, getAuthToken());
	// }
	// }

	public static LocationHelper getLocationHelper() {
		return mLocationHelper;
	}

	public static void setLocationHelper(LocationHelper mLocationHelper) {
		DTHelper.mLocationHelper = mLocationHelper;
	}

	public class DTLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	public static DTSyncStorage getSyncStorage() throws DataException {
		return getInstance().storage;
	}

	public static boolean isOwnedObject(BaseDTObject obj) {
		// if (obj.getId() == null)
		// return true;
		// // UserData p = null;
		// // try {
		// // p = accessProvider.readUserData(mContext, null);
		// // } catch (DataException e) {
		// //
		// // }
		// if (bp != null)
		// return bp.getUserId().equals(obj.getCreatorId());
		// else
		// getUserProfile();
		return false;
	}

	// @SuppressWarnings("rawtypes")
	// public static <T extends GenericObjectForBean> Collection<T>
	// searchInGeneral(int position, int size, String what,
	// WhereForSearch distance, WhenForSearch when, boolean my, Class<T> cls,
	// SortedMap<String, Integer> sort,
	// String... inCategories) throws DataException,
	// StorageConfigurationException, ConnectionException,
	// ProtocolException, SecurityException {
	// /* calcola when */
	// String[] argsArray = null;
	// ArrayList<String> args = null;
	// if (distance != null) {
	// /* search online */
	// return getObjectsFromServer(position, size, what, distance, when, my,
	// cls, inCategories, sort);
	// } else {
	// /* search offline */
	//
	// // if (Utils.getObjectVersion(mContext,
	// // DTParamsHelper.getAppToken()) > 0) {
	//
	// /* if sync create the query */
	// String where = "";
	// if (inCategories[0] != null) {
	// args = new ArrayList<String>();
	// where = addCategoriesToWhere(where, inCategories, args);
	// }
	// if (what != null && what.length() > 0) {
	// where = addWhatToWhere(cls, where, what);
	// if (args == null)
	// args = new ArrayList<String>(Arrays.asList(what));
	// else
	// args.add(what);
	// }
	// if
	// (ExplorerObject.class.getCanonicalName().equals(cls.getCanonicalName()))
	// {
	// if (when != null)
	// where = addWhenToWhere(where, when.getFrom(), when.getTo());
	//
	// /* se sono con gli eventi setto la data a oggi */
	// else
	// where = addWhenToWhere(where, getCurrentDateTimeForSearching(), 0);
	// }
	// if (my)
	// where = addMyEventToWhere(where);
	// if (args != null)
	// argsArray = args.toArray(new String[args.size()]);
	// /*
	// * se evento metti in ordine di data ma se place metti in ordine
	// * alfabetico
	// */
	// if
	// (EventObjectForBean.class.getCanonicalName().equals(cls.getCanonicalName()))
	// {
	// return getInstance().storage.query(cls, where, argsArray, position, size,
	// "fromTime ASC");
	// } else {
	// return getInstance().storage.query(cls, where, argsArray, position, size,
	// "title ASC");
	// }
	// // } else {
	// // /* if not sync... (not used anymore) */
	// // ArrayList<T> result = new ArrayList<T>();
	// // for (String category : inCategories) {
	// // ObjectFilter filter = new ObjectFilter();
	// // if (what != null) {
	// // Map<String, Object> criteria = new HashMap<String, Object>(1);
	// // criteria.put("text", what);
	// // filter.setCriteria(criteria);
	// // }
	// // if (when != null) {
	// // filter.setFromTime(when.getFrom());
	// // filter.setToTime(when.getTo());
	// // }
	// // if (category != null) {
	// // Arrays.asList(categories);
	// // }
	// // if (my)
	// // filter.setMyObjects(true);
	// //
	// // filter.setSkip(position);
	// // filter.setLimit(size);
	// // result.addAll(getRemote(mContext,
	// // getAuthToken()).searchObjects(filter, cls));
	// // }
	// // return result;
	// // }
	//
	// }
	//
	// }
	public static <T extends BaseDTObject> Collection<T> searchInGeneral(int position, int size, String what,
			WhereForSearch distance, WhenForSearch when, boolean my, Class<T> cls, SortedMap<String, Integer> sort,
			String... inCategories) throws DataException, StorageConfigurationException, ConnectionException,
			ProtocolException, SecurityException {
		/* calcola when */
		String[] argsArray = null;
		ArrayList<String> args = null;

		if (distance != null) {
			/* search online */
			return getObjectsFromServer(position, size, what, distance, when, my, cls, inCategories, sort);
		} else {
			/* search offline */

			// if (Utils.getObjectVersion(instance.mContext,
			// DTParamsHelper.getAppToken()) > 0) {

			/* if sync create the query */
			String where = "";
			if (inCategories[0] != null) {
				args = new ArrayList<String>();
				where = addCategoriesToWhere(where, inCategories, args);
			}
			if (what != null && what.length() > 0) {
				where = addWhatToWhere(cls, where, what);
				if (args == null)
					args = new ArrayList<String>(Arrays.asList(what));
				else
					args.add(what);
			}
			if (ExplorerObject.class.getCanonicalName().equals(cls.getCanonicalName())) {
				if (when != null)
					where = addWhenToWhere(where, when.getFrom(), when.getTo());

				/* se sono con gli eventi setto la data a oggi */
				else
					where = addWhenToWhere(where, getCurrentDateTimeForSearching(), 0);
			}
			if (my)
				where = addMyEventToWhere(where);
			if (args != null)
				argsArray = args.toArray(new String[args.size()]);
			/*
			 * se evento metti in ordine di data ma se place metti in ordine
			 * alfabetico
			 */
			if (ExplorerObject.class.getCanonicalName().equals(cls.getCanonicalName())) {
				return getInstance().storage.query(cls, where, argsArray, position, size, "fromTime ASC");
			} else {
				return getInstance().storage.query(cls, where, argsArray, position, size, "title ASC");
			}

		}

	}

	@SuppressWarnings("unchecked")
	private static <T extends BasicObject> Collection<T> getObjectsFromServer(int position, int size, String what,
			WhereForSearch distance, WhenForSearch when, boolean myevent, Class<T> cls, String[] inCategories,
			SortedMap<String, Integer> sort) {
		try {

			ObjectFilter filter = new ObjectFilter();

			/* get position */
			// long currentDate = getCurrentDateTimeForSearching();
			if (when != null)
				filter.setFromTime(when.getFrom());
			if ((when != null) && (when.getTo() != 0))
				filter.setToTime(when.getTo());

			if (distance != null) {
				GeoPoint mypos = MapManager.requestMyLocation(getInstance().mContext);
				filter.setCenter(new double[] { (double) mypos.getLatitudeE6() / 1000000,
						(double) mypos.getLongitudeE6() / 1000000 });
				filter.setRadius(distance.getFilter());
			}
			if (what != null && what.length() > 0) {
				filter.setText(what);
			}
			if (inCategories[0] != null) {
				filter.setTypes(Arrays.asList(CategoryHelper.getAllCategories(new HashSet<String>(Arrays
						.asList(inCategories)))));
			}
			filter.setSkip(position);
			filter.setLimit(size);
			filter.setClassName(cls.getCanonicalName());
			if (sort != null)
				filter.setSort(sort);
			// TO DO
			Collection<T> result = new ArrayList<T>();
			Collection<ExplorerObject> events = null;
			events = getEventsRemote(filter, null);

			// for (ExplorerObject poi : events) {
			// ExplorerObject eventBean = new ExplorerObject();
			// eventBean.setObjectForBean(poi);
			// eventsbean.add(eventBean);
			// }
			// result = (Collection<T>) eventsbean;

			if (result != null) {
				synchronize();
			}
			return result;
			// List<T> returnevents =
			// eu.trentorise.smartcampus.android.common.Utils.convertJSONToObjects(eventsReturn,
			// cls);
			// return returnevents;

		} catch (Exception e) {
			return null;
		}
	}

	// private static <T extends BasicObject> Collection<T>
	// getObjectsFromServer(int position, int size, String what,
	// WhereForSearch distance, WhenForSearch when, boolean myevent, Class<?>
	// cls, String[] inCategories,
	// SortedMap<String, Integer> sort) {
	// try {
	//
	// ObjectFilter filter = new ObjectFilter();
	//
	// /* get position */
	// // long currentDate = getCurrentDateTimeForSearching();
	// if (when != null)
	// filter.setFromTime(when.getFrom());
	// if ((when != null) && (when.getTo() != 0))
	// filter.setToTime(when.getTo());
	//
	// GeoPoint mypos = MapManager.requestMyLocation(mContext);
	// if (distance != null) {
	// filter.setCenter(new double[] { (double) mypos.getLatitudeE6() / 1000000,
	// (double) mypos.getLongitudeE6() / 1000000 });
	// filter.setRadius(distance.getFilter());
	// }
	// if (what != null && what.length() > 0) {
	// filter.setText(what);
	// }
	// if (inCategories[0] != null) {
	// filter.setTypes(Arrays.asList(CategoryHelper.getAllCategories(new
	// HashSet<String>(Arrays
	// .asList(inCategories)))));
	// }
	// filter.setSkip(position);
	// filter.setLimit(size);
	// // filter.setClassName(cls.getCanonicalName());
	// if (sort != null)
	// filter.setSort(sort);
	// // Collection<T> result = getRemote(mContext,
	// // getAuthToken()).searchObjects(filter, cls);
	// Collection<T> result = new ArrayList<T>();
	//
	// if (cls == EventObjectForBean.class) {
	// Collection<ExplorerObject> events = null;
	// Collection<EventObjectForBean> eventsbean = new
	// ArrayList<EventObjectForBean>();
	// events = tService.getEvents(filter, null);
	//
	// for (ExplorerObject poi : events) {
	// EventObjectForBean eventBean = new EventObjectForBean();
	// eventBean.setObjectForBean(poi);
	// eventsbean.add(eventBean);
	// }
	// result = (Collection<T>) eventsbean;
	//
	// }
	//
	// if (result != null) {
	// synchronize();
	// }
	// return result;
	//
	// } catch (Exception e) {
	// return null;
	// }
	// }

	private static String addMyEventToWhere(String where) {
		String whereReturns = new String(" attending IS NOT NULL ");
		if (where.length() > 0) {
			return where += " and (" + whereReturns + ")";
		} else
			return where += whereReturns;
	}

	private static String addWhenToWhere(String where, long whenFrom, long whenTo) {
		String whereReturns = null;
		if ((whenTo != 0)) {
			whereReturns = new String("( fromTime > " + whenFrom + " AND fromTime < " + whenTo + " ) OR (  toTime < "
					+ whenTo + " AND toTime > " + whenFrom + " )");
			// whereReturns = " (  fromTime <= " + whenTo + " AND toTime >= " +
			// whenFrom + " )";+
		} else
			whereReturns = new String(" ( fromTime > " + whenFrom + "  ) OR ( toTime > " + whenFrom + " )");

		// whereReturns = " ( toTime >= " + whenFrom + " )";

		if (where.length() > 0) {
			return where += " and (" + whereReturns + ")";
		} else
			return "(" + whereReturns + ")";

	}

	@SuppressWarnings("rawtypes")
	// private static <T extends GenericObjectForBean> String
	// addWhatToWhere(Class<T> cls, String where, String what)
	// throws StorageConfigurationException, DataException {
	// String whereReturns = "";
	//
	// whereReturns = " " + getInstance().config.getTableName(cls) +
	// " MATCH ? ";
	// if (where.length() > 0) {
	// return where += " and (" + whereReturns + ")";
	// } else
	// return where += whereReturns;
	//
	// }
	private static <T extends BaseDTObject> String addWhatToWhere(Class<T> cls, String where, String what)
			throws StorageConfigurationException, DataException {
		String whereReturns = "";

		whereReturns = " " + getInstance().config.getTableName(cls) + " MATCH ? ";
		if (where.length() > 0) {
			return where += " and (" + whereReturns + ")";
		} else
			return where += whereReturns;

	}

	private static String addCategoriesToWhere(String where, String[] inCategories, List<String> nonNullCategories) {
		String whereReturns = new String();
		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));

		for (int i = 0; i < categories.length; i++) {
			if (whereReturns.length() > 0)
				whereReturns += " or ";
			if (categories[i] != null) {
				nonNullCategories.add(categories[i]);
				whereReturns += " category = ?";
			} else {
				whereReturns += " category is null";
			}
		}
		if (where.length() > 0) {
			return where += " and (" + whereReturns + ")";
		} else
			return where += "( " + whereReturns + " ) ";

	}

	public static boolean checkInternetConnection(Context context) {

		ConnectivityManager con_manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (con_manager.getActiveNetworkInfo() != null && con_manager.getActiveNetworkInfo().isAvailable()
				&& con_manager.getActiveNetworkInfo().isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	// public static ExplorerObject findEventById(String eventId) {
	// ExplorerObject returnEvent = new ExplorerObject();
	// try {
	// EventObjectForBean event = getInstance().storage.getObjectById(eventId,
	// EventObjectForBean.class);
	// returnEvent.setEventFromEventObjectForBean(event);
	// return returnEvent;
	// } catch (Exception e) {
	// return null;
	// }
	// }
	public static ExplorerObject findEventById(String eventId) {

		try {
			ExplorerObject event = getInstance().storage.getObjectById(eventId, ExplorerObject.class);
			return event;
		} catch (Exception e) {
			return null;
		}
	}

	public static SharedPreferences getTutorialPreferences(Context ctx) {
		SharedPreferences out = ctx.getSharedPreferences(TUT_PREFS, Context.MODE_PRIVATE);
		return out;
	}

	public static boolean isFirstLaunch(Context ctx) {
		return getTutorialPreferences(ctx).getBoolean(FIRST_LAUNCH_PREFS, true);
	}

	public static void disableFirstLaunch(Context ctx) {
		Editor edit = getTutorialPreferences(ctx).edit();
		edit.putBoolean(FIRST_LAUNCH_PREFS, false);
		edit.commit();
	}

	public static boolean wantTour(Context ctx) {
		return getTutorialPreferences(ctx).getBoolean(TOUR_PREFS, false);
	}

	public static void setWantTour(Context ctx, boolean want) {
		Editor edit = getTutorialPreferences(ctx).edit();
		edit.putBoolean(TOUR_PREFS, want);
		edit.commit();
	}

	public static void bringmethere(FragmentActivity activity, Address from, Address to) {
		Intent intent = activity.getPackageManager().getLaunchIntentForPackage(
				"eu.trentorise.smartcampus.viaggiatrento");
		if (intent == null) {
			intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=eu.trentorise.smartcampus.viaggiatrento"));
			activity.startActivity(intent);
		} else
			// startActivity(intent);
			NavigationHelper.bringMeThere(activity, from, to);

	}

}
