/*******************************************************************************
 * Copyright 2012 Paweł Psztyć
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.rest.client;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rest.client.event.ApplicationReadyEvent;
import org.rest.client.event.NewProjectAvailableEvent;
import org.rest.client.event.SavedRequestEvent;
import org.rest.client.gdrive.DriveAuth;
import org.rest.client.gdrive.DriveCall;
import org.rest.client.gdrive.DriveFileItem;
import org.rest.client.mvp.AppActivityMapper;
import org.rest.client.mvp.AppPlaceHistoryMapper;
import org.rest.client.place.ImportExportPlace;
import org.rest.client.place.RequestPlace;
import org.rest.client.request.FilesObject;
import org.rest.client.request.HttpMethodOptions;
import org.rest.client.request.RequestHeadersParser;
import org.rest.client.storage.StoreResultCallback;
import org.rest.client.storage.store.LocalStore;
import org.rest.client.storage.store.ProjectStoreWebSql;
import org.rest.client.storage.store.RequestDataStoreWebSql;
import org.rest.client.storage.store.objects.ProjectObject;
import org.rest.client.storage.store.objects.RequestObject;
import org.rest.client.task.CreateMenuTask;
import org.rest.client.task.FirstRunTask;
import org.rest.client.task.InitializeAppHandlersTask;
import org.rest.client.task.InitializeDatabaseTask;
import org.rest.client.task.TasksLoader;
import org.rest.client.ui.RequestView;
import org.rest.client.util.UUID;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.xhr2.client.RequestHeader;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class RestClient implements EntryPoint {
	
	private static boolean initializing = true;
	public static boolean isInitializing(){ return initializing; }
	
	private Place defaultPlace = new RequestPlace(null);
	private SimplePanel appWidget = new SimplePanel();
	private static final Logger log = Logger.getLogger(RestClient.class
			.getName());
	private final static ClientFactory clientFactory = GWT
			.create(ClientFactory.class);

	public final static ClientFactory getClientFactory() {
		return clientFactory;
	}
	
	private static int currentOpenedProject = -1;
	private static int previousOpenedProject = -1;
	private static String applicationUserId = null;
	/**
	 * True if Save request dialog is opened.
	 */
	public static boolean isSaveDialogEnabled = false;
	/**
	 * @return current opened project ID or -1 if none
	 */
	public final static int getOpenedProject(){
		return currentOpenedProject;
	}
	/**
	 * @return previously opened project ID or -1 if none
	 */
	public final static int getPreviousProject(){
		return previousOpenedProject;
	}
	/**
	 * @param project current opened project ID or -1 if none
	 */
	public final static void setOpenedProject(int project){
		currentOpenedProject = project;
	}
	/**
	 * 
	 * @param project previously opened project ID or -1 if none
	 */
	public final static void setPreviousProject(int project){
		previousOpenedProject = project;
	}
	
	
	public static String getApplicationUserId() {
		return applicationUserId;
	}
	public static void setApplicationUserId(String appUserId) {
		applicationUserId = appUserId;
	}
	
	public void onModuleLoad() {
		//Log.debug("AAAAAAAAAAAAAAAAAAAAAAAA");
		
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException(Throwable e) {
				log.log(Level.SEVERE, e.getMessage(), e);
				Log.error("Application error", e);
			}
		});
		
		Logger.getLogger("").addHandler(clientFactory.getErrorDialogView().getHandler());
		Logger.getLogger("").setLevel(Level.WARNING);
		
		setSyncData();
		
		final EventBus eventBus = clientFactory.getEventBus();
		final PlaceController placeController = clientFactory.getPlaceController();
		// Start ActivityManager for the main widget with our ActivityMapper
		ActivityMapper activityMapper = new AppActivityMapper(clientFactory);
		ActivityManager activityManager = new ActivityManager(activityMapper,
				eventBus);
		appWidget.getElement().getStyle().setPaddingBottom(20, Unit.PX);
		activityManager.setDisplay(appWidget);

		// Start PlaceHistoryHandler with our PlaceHistoryMapper
		AppPlaceHistoryMapper historyMapper = GWT
				.create(AppPlaceHistoryMapper.class);
		final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(
				historyMapper);
		historyHandler.register(placeController, eventBus, defaultPlace);
		
		//
		// Start up application. 
		//
		
		TasksLoader.addTask(new InitializeDatabaseTask());
		TasksLoader.addTask(new FirstRunTask());
		TasksLoader.addTask(new InitializeAppHandlersTask());
		TasksLoader.addTask(new CreateMenuTask());
		
		TasksLoader.runTasks(new Callback<Void, Void>() {

			@Override
			public void onFailure(Void reason) {
				Log.error("Initialize error...");
			}

			@Override
			public void onSuccess(Void result) {
				RootPanel.get("appContainer").add(appWidget);
				
				
				//
				// Special Tokens
				// import/{UID} import data from Applications server
				// TODO: to be removed on December 1st, 2012
				//
				if (Window.Location.getHash().startsWith("#import/")) {
					String importUID = Window.Location.getHash().substring(8);
					placeController.goTo(ImportExportPlace.fromServerImport(importUID));
				} else {
					historyHandler.handleCurrentHistory();
				}
				fixChromeLayout();
				eventBus.fireEvent(new ApplicationReadyEvent());
				initializing = false;
			}
		});
	}
	
	
	
	
	/**
	 * 
	 * @return true if debug output is enabled, false otherwise
	 */
	public static boolean isDebug() {
		return SyncAdapter.isDebug();
	}
	
	public static void setDebug(boolean debug){
		SyncAdapter.setDebug(debug);
	}
	
	
	/**
	 * @return true if history feature output is enabled, false otherwise
	 */
	public static boolean isHistoryEabled(){
		return SyncAdapter.isHistory();
	}
	
	public static void setHistoryEnabled(boolean historyEnabled){
		SyncAdapter.setHistory(historyEnabled);
	}

	/**
	 * Sets synch data from chrome.storage.sync API
	 */
	private static void setSyncData(){
		final Storage store = Storage.getLocalStorageIfSupported();
		//first, restore local value, for quick access
		String debugValue = store.getItem(LocalStore.DEBUG_KEY);
		String historyValue = store.getItem(LocalStore.HISTORY_KEY);
		String notificationsValue = store.getItem(LocalStore.NOTIFICATIONS_ENABLED_KEY);
		String magicVarsValue = store.getItem(LocalStore.MAGIC_VARS_ENABLED_KEY);
		if(debugValue != null && debugValue.equals("true")){
			SyncAdapter.setDebug(true);
		} else {
			SyncAdapter.setDebug(false);
		}
		if(historyValue == null || historyValue.equals("true")){
			SyncAdapter.setHistory(true);
		} else {
			SyncAdapter.setHistory(false);
		}
		if(notificationsValue != null && notificationsValue.equals("true")){
			SyncAdapter.setNotifications(true);
		} else {
			SyncAdapter.setNotifications(false);
		}
		if(magicVarsValue != null && magicVarsValue.equals("true")){
			SyncAdapter.setMagicVars(true);
		} else {
			SyncAdapter.setMagicVars(false);
		}
		SyncAdapter.sync();
		SyncAdapter.observe();
	}
	
	
	/**
	 * Get request data from current form. If current view is not request view
	 * it will get data from storage, from latest request.
	 * 
	 * @param callback
	 */
	public static void collectRequestData(
			final Callback<RequestObject, Throwable> callback) {
		
		if (History.getToken().isEmpty() || History.getToken().startsWith("RequestPlace")) {
			RequestView requestView = RestClient.getClientFactory()
					.getRequestView();
			
			RequestObject rp = RequestObject.createRequest(); 
			rp.setFiles(requestView.getFiles());
			rp.setEncoding(requestView.getEncoding());
			rp.setHeaders(requestView.getHeaders());
			rp.setMethod(requestView.getMethod());
			rp.setPayload(requestView.getPayload());
			rp.setURL(requestView.getUrl());
			rp.setName(requestView.getRequestName());
			
			callback.onSuccess(parseRequestParameters(rp));
		} else {
			RequestObject
					.restoreLatest(new Callback<RequestObject, Throwable>() {
						@Override
						public void onSuccess(RequestObject result) {
							callback.onSuccess(parseRequestParameters(result));
						}

						@Override
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
						}
					});
		}
	}
	
	/**
	 * Prepare request data.
	 * It set (if request include payload) content-type header according to user preferences
	 * @param rp
	 * @return
	 */
	private static RequestObject parseRequestParameters(RequestObject rp){
		String headers = rp.getHeaders();
		String method = rp.getMethod();
		String encoding = rp.getEncoding();
		boolean hasPayload = HttpMethodOptions.hasBody(method);
		
		ArrayList<FilesObject> files = null;
		if(hasPayload){
			//handle content-type header for request with payload. 
			//It can be either set via headers form or select input with predefined options.
			ArrayList<RequestHeader> headersList = RequestHeadersParser.stringToHeaders(headers);
			if (headers == null) {
				headersList = new ArrayList<RequestHeader>();
			}
			int i = 0;
			for(RequestHeader item : headersList){
				String key = item.getName();
				if(key.toLowerCase().equals("content-type")){
					encoding = item.getValue();
					if(RestClient.isDebug()){
						Log.debug("Found Content-Type header in headers list. Overwrite content-type value to " + encoding);
					}
					//Temporarily remove it from headers list
					headersList.remove(i);
					break;
				}
				i++;
			}
			files = rp.getFiles();
			if(files != null && files.size() > 0){
				encoding = null;
			}
			if(encoding != null){
				headersList.add(new RequestHeader("Content-Type", encoding));
			}
			headers = RequestHeadersParser.headersListToString(headersList);
			
		}
		
		RequestObject requestObject = RequestObject.createRequest();
		requestObject.setHeaders(headers);
		requestObject.setMethod(method);
		String url = rp.getURL();
		if(url.startsWith("/") && !GWT.isProdMode()){
			//
			// DEV mode.
			//
			url = "http://127.0.0.1:8888"+url;
		}
		
		requestObject.setURL(url);
		if(hasPayload){
			requestObject.setPayload(rp.getPayload());
			requestObject.setEncoding(null);
			requestObject.setFiles(files);
		}
		return requestObject;
	}
	
	
	
	
	
	
	
	/**
	 * Save to Store Request form data. This method is used when creating new project.
	 * @param obj Object data to save
	 * @param newProjectName Project name to save
	 * @param callback
	 */
	public static void saveRequestData(final RequestObject obj, final String newProjectName, final Callback<RequestObject, Throwable> callback){
		final ProjectStoreWebSql store = clientFactory.getProjectsStore();
		ProjectObject project = ProjectObject.create();
		project.setName(newProjectName);
		store.put(project, null, new StoreResultCallback<Integer>() {

			@Override
			public void onSuccess(Integer result) {
				obj.setProject(result.intValue());
				clientFactory.getEventBus().fireEvent(new NewProjectAvailableEvent(result.intValue()));
				saveRequestData(obj, callback);
			}

			@Override
			public void onError(Throwable e) {
				if(RestClient.isDebug()){
					Log.error("Unable to save project data. Can't save project to store.", e);
				}
				callback.onFailure(e);
			}
		});
	}
	/**
	 * Save new request data.
	 * @param obj
	 * @param callback
	 */
	public static void saveRequestData(final RequestObject obj, final Callback<RequestObject, Throwable> callback){
		final RequestDataStoreWebSql store = clientFactory.getRequestDataStore();
		Integer updateId = null;
		if(obj.getId() > 0){
			updateId = obj.getId();
		}
		store.put(obj, updateId, new StoreResultCallback<Integer>() {
			
			@Override
			public void onSuccess(Integer result) {
				obj.setId(result.intValue());
				clientFactory.getEventBus().fireEvent(new SavedRequestEvent(obj));
				callback.onSuccess(obj);
			}
			
			@Override
			public void onError(Throwable e) {
				if(RestClient.isDebug()){
					Log.error("Unable to save request data. Can't save request to store.", e);
				}
				callback.onFailure(e);
			}
		});
	}
	/**
	 * Save the request to Google Drive.
	 * 
	 * @param obj The request to save. If request object contains gDriveId it will overwrite existing file instead of creating new.
	 * @param callback
	 */
	public static void saveRequestToGDrive(RequestObject obj, Callback<DriveFileItem, Throwable> callback){
		saveRequestToGDrive(obj, null, callback);
	}
	/**
	 * Save request object in Google Drive.
	 * @param obj The request to save
	 * @param parentId If not null (create action from Drive UI) will not show folder selector.
	 * @param callback
	 */
	public static void saveRequestToGDrive(final RequestObject obj, final String parentId, final Callback<DriveFileItem, Throwable> callback){
		DriveCall.hasSession(new DriveCall.SessionHandler() {
			@Override
			public void onResult(DriveAuth result) {
				if(result == null){
					//no logged in user
					DriveCall.auth(new DriveCall.SessionHandler() {
						@Override
						public void onResult(DriveAuth result) {
							if(result == null){
								callback.onFailure(new Throwable("Authorization required."));
								return;
							}
							doSaveRequestToGDrive(obj, callback, result.getAccessToken(),parentId);
						}
					}, false);
					return;
				}
				doSaveRequestToGDrive(obj, callback, result.getAccessToken(),parentId);
			}
		});
	}
	
	private static void doSaveRequestToGDrive(final RequestObject obj, 
			final Callback<DriveFileItem, Throwable> callback, String accessToken, 
			final String folderId){
		
		if(obj.getGDriveId() != null){
			if(RestClient.isDebug()){
				Log.debug("Updating Google Drive™ item");
			}
			DriveCall.updateFile(obj.getGDriveId(), obj, new DriveCall.FileUploadHandler() {
				@Override
				public void onLoad(DriveFileItem response) {
					clientFactory.getEventBus().fireEvent(new SavedRequestEvent(obj));
					callback.onSuccess(response);
				}
				
				@Override
				public void onError(JavaScriptException exc) {
					callback.onFailure(exc);
				}
			});
			return;
		}
		
		if(folderId != null){
			DriveCall.insertNewFile(folderId, obj.getName(), obj, new DriveCall.FileUploadHandler() {
				@Override
				public void onLoad(DriveFileItem response) {
					callback.onSuccess(response);
				}
				
				@Override
				public void onError(JavaScriptException exc) {
					callback.onFailure(exc);
				}
			});
			return;
		}
		
		DriveCall.showGoogleForlderPickerDialog(accessToken, new DriveCall.SelectFolderHandler() {
			
			@Override
			public void onSelect(String folderId) {
				if(folderId == null || folderId.isEmpty()){
					callback.onSuccess(null);
					return;
				}
				Storage storage = Storage.getLocalStorageIfSupported();
				storage.setItem(LocalStore.LATEST_GDRIVE_FOLDER, folderId);
				
				
				DriveCall.insertNewFile(folderId, obj.getName(), obj, new DriveCall.FileUploadHandler() {
					@Override
					public void onLoad(DriveFileItem response) {
						callback.onSuccess(response);
					}
					
					@Override
					public void onError(JavaScriptException exc) {
						callback.onFailure(exc);
					}
				});
			}
			
			@Override
			public void onCancel() {
				callback.onSuccess(null);
			}
		});
	}
	
	
	
	
	
	
	/**
	 * Get application unique ID (36 characters).
	 * 
	 * @return generated RFC4122 UUID
	 */
	public static String getAppId() {
		Storage storage = Storage.getLocalStorageIfSupported();
		String uuid = storage.getItem("aapi");
		if (uuid == null || uuid.equals("")) {
			uuid = UUID.uuid();
			storage.setItem("aapi", uuid);
		}
		return uuid;
	}
	
	
	/**
	 * Sometimes chrome freeze after layout change via javascript (not sure if is it).
	 * Use this after operations when it's happen.
	 */
	public static final native void fixChromeLayout() /*-{
		$doc.body.style.display = 'none';
		$wnd.setTimeout(function() {
			$doc.body.style.removeProperty('display');
		}, 15);
	}-*/;
}
