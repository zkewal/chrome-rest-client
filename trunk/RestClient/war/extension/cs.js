
//window.addEventListener('ARC:READY',function(e){
//	console.log('ARC:READY');
//},false);

window.addEventListener("message", receiveMessage, false);

function receiveMessage(e){
	if(e.origin != location.origin){ return; };
	var data;
	try{
		data = JSON.parse(e.data);
	} catch(e){ return; }
	
	if(!(data && data.source && data.source == "dev:gwt")) return;
	//console.log('receiveMessage (ext)', data.data);
	
	if(data.payload){
		switch(data.payload){
			case 'setEnvironment':
			chrome.extension.sendMessage(data, function(response) {});
			break;
			case "requestBegin": 
				chrome.extension.sendMessage(data, function(response) {
					window.postMessage({"source":"dev:cs", "payload":"requestBegin"}, location.href);
				});
			break;
			case "getRequestData":
			case "getExternalData":
				chrome.extension.sendMessage(data, function(response) {
					if(response.payload&&response.data){
						data.payload = response.payload;
						response = response.data;
					}
					window.postMessage({"source":"dev:cs", "payload":data.payload, "data": response}, location.href);
				});
			break;
			case "copyToClipboard":
				chrome.extension.sendMessage(data, function(response) {});
				break;
			default:
				chrome.extension.sendMessage(data, function(response) {
					if(typeof response.payload != 'undefined'){
						response = response.data;
					}
					window.postMessage({"source":"dev:cs", "payload":data.payload, "data": response}, location.href);
				});
				break;
		}
	}
}