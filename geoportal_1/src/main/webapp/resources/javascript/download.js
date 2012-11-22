
if (typeof org == 'undefined'){ 
	org = {};
} else if (typeof org != "object"){
	throw new Error("org already exists and is not an object");
}

// Repeat the creation and type-checking code for the next level
if (typeof org.OpenGeoPortal == 'undefined'){
	org.OpenGeoPortal = {};
} else if (typeof org.OpenGeoPortal != "object"){
    throw new Error("org.OpenGeoPortal already exists and is not an object");
}


org.OpenGeoPortal.Downloader = function(){
	jQuery("body").append('<div id="requestTickerContainer" class="raised"></div>');
	var that = this;
	this.requestQueue = {
			"pollId": "",
			"isPollRunning": false,
			"tickerId": "",
			"processingIndicatorId": "",
			"isTickerRunning": false,
			"requests": {
				"pending":{
					"layers":[],
					"images":[]
				},
				"complete":{
					"layers":[],
					"images":[]
				},
				"failed":{
					"layers":[],
					"images":[]
				}
			}
	};
	
	var INTERVAL_MS = 3000;

	 var removeRequest = function (requestId, srcArray){
		var requestQueueObj;
		for (var i in srcArray){
			var currentRequest = srcArray[i];
			if (currentRequest.requestId == requestId){
				return srcArray.splice(i, 1)[0];
			}
		}
		return false;
	};
		
	this.removePendingLayerRequest = function(requestId){
		return removeRequest(requestId, this.getLayerRequests());
	};
	
	this.removePendingImageRequest = function(requestId){
		return removeRequest(requestId, this.getImageRequests());
	};
	
	this.getLayerRequests = function(){
		return that.requestQueue.requests.pending.layers;
	};
	
	this.getCompleteLayerRequests = function(){
		return that.requestQueue.requests.complete.layers;
	};
	
	this.getFailedLayerRequests = function(){
		return that.requestQueue.requests.failed.layers;
	};
	
	var getNewRequest = function(requestId, requestObj){
		var request = {};
		request.requestId = requestId;
		request.status = {};
		request.params = requestObj;
		return request;
	};
	
	var addLayerRequest = function(requestId, requestObj){
		that.requestQueue.requests.pending.layers.push(getNewRequest(requestId, requestObj));
	};
	
	var addImageRequest = function(requestId, requestObj){
		that.requestQueue.requests.pending.images.push(getNewRequest(requestId, requestObj));
	};
	
	this.registerLayerRequest = function(requestId, requestObj){
		addLayerRequest(requestId, requestObj);
		if (!that.requestQueue.isPollRunning){
			this.startPoll();
		}
	};
	
	this.addLayerToComplete = function(requestObj){
		that.requestQueue.requests.complete.layers.push(requestObj);
	};
	
	this.addImageToComplete = function(requestStatus){
		that.requestQueue.requests.complete.images.push(requestStatus);
	};
	
	this.addLayerToFailed = function(requestObj){
		that.requestQueue.requests.failed.layers.push(requestObj);
	};
	
	this.addImageToFailed = function(requestStatus){
		that.requestQueue.requests.failed.images.push(requestStatus);
	};
	
	this.layerRequestToComplete = function(requestStatus){
		var requestQueueObj = this.removePendingLayerRequest(requestStatus.requestId);
		var newObj = {};
		jQuery.extend(true, newObj, requestQueueObj, requestStatus);
		this.addLayerToComplete(newObj);
	};
	
	this.imageRequestToComplete = function(requestStatus){
		var requestQueueObj = this.removePendingImageRequest(requestStatus.requestId);
		var newObj = {};
		jQuery.extend(true, newObj, requestQueueObj, requestStatus);
		this.addImageToComplete(newObj);
	};
	
	this.layerRequestToFailed = function(requestStatus){
		var requestQueueObj = this.removePendingLayerRequest(requestStatus.requestId);
		var newObj = {};
		jQuery.extend(true, newObj, requestQueueObj, requestStatus);
		this.addLayerToFailed(newObj);
	};
	
	this.imageRequestToFailed = function(requestStatus){
		var requestQueueObj = this.removePendingImageRequest(requestStatus.requestId);
		var newObj = {};
		jQuery.extend(true, newObj, requestQueueObj, requestStatus);
		this.addImageToFailed(newObj);
	};
	
	this.getImageRequests = function(){
		return that.requestQueue.requests.pending.images;
	};
	
	this.getCompleteImageRequests = function(){
		return that.requestQueue.requests.complete.images;
	};
	
	this.getFailedImageRequests = function(){
		return that.requestQueue.requests.failed.images;
	};
	
	this.registerImageRequest = function(requestId, requestObj){
		addImageRequest(requestId, requestObj);
		if (!that.requestQueue.isPollRunning){
			this.startPoll();
		}
	};
	
	this.getRequestById = function(id){
		var requests = this.getLayerRequests().concat(this.getImageRequests());
		for (var i in requests){
			var currentRequest = requests[i];
			if (currentRequest.requestId == id){
				currentRequest.queue = "pending";
				return currentRequest;
			}
		}
		var requests = this.getCompleteLayerRequests().concat(this.getCompleteImageRequests());
		for (var i in requests){
			var currentRequest = requests[i];
			if (currentRequest.requestId == id){
				currentRequest.queue = "complete";
				return currentRequest;
			}
		}
		var requests = this.getFailedLayerRequests().concat(this.getFailedImageRequests());
		for (var i in requests){
			var currentRequest = requests[i];
			if (currentRequest.requestId == id){
				currentRequest.queue = "failed";
				return currentRequest;
			}
		}
		
	};
	
	this.requestsToFailedById = function(ids){
		for (var i in ids){
			var requestId = ids[i];
			var requestQueueObj = this.removePendingLayerRequest(requestId);
			if (requestQueueObj){
				this.addLayerToFailed(requestQueueObj);
				continue;
			} else {
				var requestQueueObj = this.removePendingImageRequest(requestId);
				if (requestQueueObj){
					this.addImageToFailed(requestQueueObj);
				} else {
					//there's a problem
				}
			}
		}
	};
	
	this.requestToFailedByStatus = function(requestStatus){
		var requestId = requestStatus.requestId;
		var requestQueueObj = this.removePendingLayerRequest(requestId);
		if (requestQueueObj){
			var newObj = {};
			jQuery.extend(true, newObj, requestQueueObj, requestStatus);
			this.addLayerToFailed(newObj);
			return;
		} else {
			var requestQueueObj = this.removePendingImageRequest(requestId);
			if (requestQueueObj){
				var newObj = {};
				jQuery.extend(true, newObj, requestQueueObj, requestStatus);
				this.addImageToFailed(newObj);
			} else {
				//there's a problem
			}
		}
	};
	
	this.firePoll = function(){
		var t=setTimeout('org.OpenGeoPortal.downloadQueue.pollRequestStatus()', INTERVAL_MS);
		this.requestQueue.pollId = t;
		this.requestQueue.isPollRunning = true;
		this.setTickerText();
	};
	
	this.startPoll = function(){
		if (!that.requestQueue.isPollRunning){
			that.startTicker();
			that.firePoll();
		} else {
			//poll is already running
		}
	};
	
	this.startTicker = function(){
		//show ticker (a div with transparent black background, fixed to bottom of screen, loader
		//put a counter in a closure to iterate over the array
		if (jQuery("#requestTicker").length == 0){
			jQuery("#requestTickerContainer").html('<div id="processingIndicator"></div><div id="requestTicker"></div></div>');
		} else {
		}
		
		jQuery("#requestTickerContainer").fadeIn();

		//jQuery("#requestTicker").text(this.getTickerText());

		this.requestQueue.processingIndicatorId = org.OpenGeoPortal.Utility.indicatorAnimationStart("processingIndicator");
	};
	
	this.stopTicker = function(){
		try {
			var intervalId = this.requestQueue.tickerId;
			clearInterval(intervalId);
		} catch (e) {}
		//hide ticker
		jQuery("#requestTickerContainer").fadeOut();
		clearInterval(this.requestQueue.processingIndicatorId);
	};
	
	this.setTickerText = function(){
		var pending = this.requestQueue.requests.pending;
		var imageRequests = pending.images.length; 
		var layerRequests = pending.layers.length;
		var totalRequests = imageRequests + layerRequests;
		//console.log(imageRequests + " " + layerRequests + " " + totalRequests);
		var tickerText = "Processing ";

		if (totalRequests > 1){
			tickerText += totalRequests;
			tickerText += " Requests";
			//var that = this;
			//this.requestQueue.tickerId = setInterval(function(){ that.tick () }, 3000);
		} else {
			tickerText += "Request";
		}

		jQuery("#requestTicker").text(tickerText);
		//jQuery("#requestTickerContainer").width(jQuery("#requestTicker").width());
	};
	
	/*this.tick = function(){
		jQuery('#requestTicker').slideUp( function () { jQuery('#requestTicker').slideDown(); });
	}*/

	this.stopPoll = function(){
		this.stopTicker();
		if (this.requestQueue.isPollRunning){
			var t= this.requestQueue.pollId;
			clearTimeout(t);
			this.requestQueue.isPollRunning = false;
		} else {
			//poll is not running
		}
	};
	
	var handleStatusResponse = function(data){
		var statuses = data.requestStatus;
		//console.log(statuses);
		var pendingCounter = 0;
		for (var i in statuses){
			var currentStatus = statuses[i].status;
			//console.log(currentStatus);
			//should be a clause for each possible status message
			if ((currentStatus == "COMPLETE_SUCCEEDED")||
					(currentStatus == "COMPLETE_PARTIAL")){
				//get the download
				handleDownload(statuses[i]);
				if (currentStatus == "COMPLETE_PARTIAL"){
					//should be a note to the user for partial success
				}
			} else if (currentStatus == "PROCESSING"){
				pendingCounter++;
			} else if (currentStatus == "COMPLETE_FAILED"){
				that.requestToFailedByStatus(statuses[i]);
				//should be a note to user that the download failed
			}
		}
		
		if (pendingCounter > 0){
			//console.log("should fire poll");
			that.firePoll();
		} else {
			that.stopPoll();
		}
	};
	
	var handleDownload = function(statusObj){
		var url;
		var currentRequestId;
		if (statusObj.type == "layer"){
			currentRequestId = statusObj.requestId;
			that.layerRequestToComplete(statusObj);
			url = "getDownload?requestId=" + currentRequestId;
		} else if (statusObj.type == "image"){
			currentRequestId = statusObj.requestId;
			that.imageRequestToComplete(statusObj);
			url = "getImage?requestId=" + currentRequestId;
		}
		jQuery('body').append('<iframe id="' + currentRequestId + '" class="download" src="' + url + '"></iframe>');
		//jQuery("#" + currentRequestId).load(function(){jQuery(this).remove()});

	};
	
	var getLayerRequestIds = function(){
		var requestIdObjs = that.getLayerRequests();
		var requestIds = [];
		for (var i in requestIdObjs){
			requestIds.push(requestIdObjs[i].requestId);
		}
		return requestIds;
	};
	
	var getImageRequestIds = function(){
		var requestIdObjs = that.getImageRequests();
		var requestIds = [];
		for (var i in requestIdObjs){
			requestIds.push(requestIdObjs[i].requestId);
		}
		return requestIds;
	};
	
	this.pollRequestStatus = function(){
		var ids = getLayerRequestIds().concat(getImageRequestIds());
		var that = this;
		//console.log(getLayerRequestIds());
		//console.log(getImageRequestIds());
		var successFunction = function(data){
			that.requestQueue.isPollRunning = false;
			//parse this data, update request queue
			handleStatusResponse(data);
			//fire a LayerDownload completion event
			jQuery(document).trigger("requestStatus.success", data);
		};
		
		var failureFunction = function(){
			that.requestQueue.isPollRunning = false;
			//fire a LayerDownload request failed event
			that.requestsToFailedById(ids);
			jQuery(document).trigger("requestStatus.failure", ids);
		};
		var path = "requestStatus";
		if (ids.length == 0){
			failureFunction();
			return;
		}
		var params = {
				  url: path + "?requestIds=" + ids.join(","),
				  dataType: "json",
				  success: successFunction,
				  error: failureFunction
			};
		
			jQuery.ajax(params);
	};
	
	this.createErrorMessageObj = function(statusObj){
		var requestId = statusObj.requestId;
		var statusMessage = statusObj.status;
		var requestObj = this.getRequestById(requestId);
		var layers = [];
		var layerIds = [];
		var layersParam = requestObj.layers;
		for (var i in layersParam){
			var arrLayer = layersParam[i].split("=");
			var layerObj = {"layerId": arrLayer[0], "format": arrLayer[1]};
			layerIds.push(arrLayer[0]);
			layers.push(layerObj);
		}
		//get some info from solr about the layer
        var solr = new org.OpenGeoPortal.Solr();
    	var query = solr.getInfoFromLayerIdQuery(layerIds);
    	solr.sendToSolr(query, this.errorInfoSuccess, this.errorInfoError);
    	//create message box here, but keep it hidden until solr callback
	};
	
	this.errorInfoSuccess = function(data){
		
	};
};