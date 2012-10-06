package org.rest.client.storage.websql;

import java.util.ArrayList;
import java.util.Date;

import org.rest.client.storage.store.objects.RequestObject;

import com.google.code.gwt.database.client.service.ListCallback;
import com.google.code.gwt.database.client.service.RowIdListCallback;
import com.google.code.gwt.database.client.service.ScalarCallback;
import com.google.code.gwt.database.client.service.Select;
import com.google.code.gwt.database.client.service.Update;
import com.google.code.gwt.database.client.service.VoidCallback;

/**
 * New version of request data store. This store stores all requests data either
 * saved as project endpoint or regular request.
 * 
 * @author Paweł Psztyć
 * 
 */
public interface RequestDataService extends AppDatabase {

	/**
	 * Create database table.
	 * 
	 * @param callback
	 *            callback function
	 */
	@Update("CREATE TABLE IF NOT EXISTS request_data ("
			+ "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
			+ "project INTEGER DEFAULT 0, " 
			+ "name TEXT NOT NULL, "
			+ "url TEXT NOT NULL, " 
			+ "method TEXT NOT NULL, "
			+ "encoding TEXT NULL, " 
			+ "headers TEXT NULL, " 
			+ "payload TEXT NULL, "
			+ "skipProtocol INTEGER DEFAULT 0, "
			+ "skipServer INTEGER DEFAULT 0, "
			+ "skipParams INTEGER DEFAULT 0, "
			+ "skipHistory INTEGER DEFAULT 0, "
			+ "skipMethod INTEGER DEFAULT 0, "
			+ "skipPayload INTEGER DEFAULT 0, "
			+ "skipHeaders INTEGER DEFAULT 0, "
			+ "skipPath INTEGER DEFAULT 0, " 
			+ "time INTEGER)")
	void initTable(VoidCallback callback);

	@Update("INSERT INTO request_data (project, name, url, method, encoding, headers, "
			+ "payload, skipProtocol, skipServer, skipParams, skipHistory, skipMethod, "
			+ "skipPayload, skipHeaders, skipPath, time) VALUES ("
			+ "{data.getProject()},{data.getName()},{data.getURL()},{data.getMethod()},"
			+ "{data.getEncoding()},{data.getHeaders()},{data.getPayload()},"
			+ "{data.getSkipProtocol()},{data.getSkipServer()},{data.getSkipParams()},"
			+ "{data.getSkipHistory()},{data.getSkipMethod()},{data.getSkipPayload()},"
			+ "{data.getSkipHeaders()},{data.getSkipPath()},{dt.getTime()})")
	void insertData(RequestObject data, Date dt, RowIdListCallback callback);
	
	@Select("SELECT * FROM request_data WHERE project={projectId} ORDER BY time ASC")
	void getProjectRequests(int projectId, ListCallback<RequestObject> calloback);
	
	@Select("SELECT * FROM request_data WHERE project={projectId} ORDER BY time ASC LIMIT 1")
	void getProjectDefaultRequests(int projectId, ListCallback<RequestObject> calloback);
	
	@Select("SELECT * FROM request_data WHERE ID={id}")
	void getRequest(int id, ListCallback<RequestObject> calloback);
	
	@Select("SELECT COUNT(*) FROM request_data WHERE project={projectId}")
	void getEndpointsCount(int projectId, ScalarCallback<Integer> callback);
	
	@Select("SELECT * FROM request_data WHERE project=0 ORDER BY time ASC LIMIT {limit} OFFSET {offset}")
	void getSavedRequests(int limit, int offset, ListCallback<RequestObject> calloback);
	
	/**
	 * @param callback
	 */
	@Select("SELECT * FROM request_data ORDER BY name")
	void getAllData(ListCallback<RequestObject> callback);
	
	@Update(sql = "INSERT INTO request_data (project, name, url, method, encoding, headers, "
			+ "payload, skipProtocol, skipServer, skipParams, skipHistory, skipMethod, "
			+ "skipPayload, skipHeaders, skipPath, time) VALUES ("
			+ "{_.getProject()},{_.getName()},{_.getURL()},{_.getMethod()},"
			+ "{_.getEncoding()},{_.getHeaders()},{_.getPayload()},"
			+ "{_.getSkipProtocol()},{_.getSkipServer()},{_.getSkipParams()},"
			+ "{_.getSkipHistory()},{_.getSkipMethod()},{_.getSkipPayload()},"
			+ "{_.getSkipHeaders()},{_.getSkipPath()},{dt.getTime()})", foreach = "data")
	void insertImportData(ArrayList<RequestObject> data, Date dt, RowIdListCallback callback);
	
	@Update(sql = "INSERT INTO request_data (project, name, url, method, encoding, headers, "
			+ "payload, skipProtocol, skipServer, skipParams, skipHistory, skipMethod, "
			+ "skipPayload, skipHeaders, skipPath, time) VALUES ("
			+ "{_.getProject()},{_.getName()},{_.getURL()},{_.getMethod()},"
			+ "{_.getEncoding()},{_.getHeaders()},{_.getPayload()},"
			+ "{_.getSkipProtocol()},{_.getSkipServer()},{_.getSkipParams()},"
			+ "{_.getSkipHistory()},{_.getSkipMethod()},{_.getSkipPayload()},"
			+ "{_.getSkipHeaders()},{_.getSkipPath()},{_.getTime()})", foreach = "data")
	void insertFileImportData(ArrayList<RequestObject> data, RowIdListCallback callback);
}