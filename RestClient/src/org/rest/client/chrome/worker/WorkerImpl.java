package org.rest.client.chrome.worker;

import com.google.gwt.core.client.JavaScriptObject;

class WorkerImpl extends JavaScriptObject {
	
	protected WorkerImpl() {}
	
	static final native WorkerImpl get(String script) /*-{
		var worker = new Worker(script); 
		worker.onerror = $entry(function(e){
			console.error('WorkerError::',e);
		});
		return worker;
	}-*/;
	
	
	/**
	 * Starts the worker.
	 * @param message Message sent to worker.
	 */
	final native void postMessage(String message) /*-{
		this.postMessage(message);
	}-*/;
	
	
	final native void onMessage(WorkerMessageHandler handler) /*-{
		this.addEventListener('message', $entry(function(e) {
		  console.log('Worker said: ', e.data);
		  handler.@org.rest.client.chrome.worker.WorkerMessageHandler::onMessage(Ljava/lang/String;)(e.data);
		}), false);
	}-*/;
}
