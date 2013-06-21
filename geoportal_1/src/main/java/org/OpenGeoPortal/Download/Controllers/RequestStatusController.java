package org.OpenGeoPortal.Download.Controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.OpenGeoPortal.Download.DownloadRequest;
import org.OpenGeoPortal.Download.RequestStatusManager;
import org.OpenGeoPortal.Export.GeoCommons.GeoCommonsExportRequest;
import org.OpenGeoPortal.Proxy.Controllers.ImageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/requestStatus")
public class RequestStatusController {

	@Autowired
	private RequestStatusManager requestStatusManager;
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private List<DownloadRequest> downloadRequests; 
	private List<ImageRequest> imageRequests;
	private List<GeoCommonsExportRequest> exportRequests;
	
	public enum StatusSummary {
		PROCESSING,
		READY_FOR_PACKAGING,
		COMPLETE_FAILED,
		COMPLETE_SUCCEEDED,
		COMPLETE_PARTIAL
	}
	
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	public @ResponseBody RequestStatus getDownloadStatus(@RequestParam("requestIds") String requestIds) throws IOException  {
		String[] requestIdsArr = requestIds.split(",");
		if (requestIdsArr.length == 0){
			throw new IOException("Request Ids required.");
		}
		this.downloadRequests = new ArrayList<DownloadRequest>();
		this.imageRequests = new ArrayList<ImageRequest>();
		this.exportRequests = new ArrayList<GeoCommonsExportRequest>();

		for (String requestId: requestIdsArr){
			try {
				DownloadRequest dlRequest = requestStatusManager.getDownloadRequest(UUID.fromString(requestId));
				if (dlRequest != null){
					this.downloadRequests.add(dlRequest);
				}
			} catch (Exception e){
				//e.printStackTrace();
			}
			try {
				ImageRequest imRequest = requestStatusManager.getImageRequest(UUID.fromString(requestId));
				if (imRequest != null){
					this.imageRequests.add(imRequest);
				}
			} catch (Exception e){
				//e.printStackTrace();
			}
			try {
				GeoCommonsExportRequest exRequest = requestStatusManager.getExportRequest(UUID.fromString(requestId));
				if (exRequest != null){
					this.exportRequests.add(exRequest);
				}
			} catch (Exception e){
				//e.printStackTrace();
			}
		}
		if ((this.downloadRequests.size() + this.imageRequests.size() + this.exportRequests.size()) > 0){
			return getRequestStatus();
		} else {
			logger.error("no requests found");
			//throw new IOException("No requests found.");
			//return an empty status object instead of just throwing an error
			return new RequestStatus();
		}
	}
	
	private RequestStatus getRequestStatus(){
		logger.debug("Creating RequestStatus object");
		RequestStatus requestStatus = new RequestStatus();
		//logger.info("download requests size: " + Integer.toString(downloadRequests.size()));
		for (DownloadRequest downloadRequest: downloadRequests){
			UUID requestId = downloadRequest.getRequestId();
			logger.debug("RequestId: " + requestId.toString());
			String type = "layer";
			if (downloadRequest.getEmailSent()){
				type = "email";
			}
			StatusSummary status = downloadRequest.getStatusSummary();
			logger.debug("Download status summary: " + status.toString());
			requestStatus.addRequestStatusElement(requestId, type, status);
		}

		for (ImageRequest imageRequest: imageRequests){
			UUID requestId = imageRequest.getRequestId();
			//logger.info("RequestId: " + requestId.toString());
			String type = "image";
			StatusSummary status = imageRequest.getStatusSummary();
			//logger.info("Image status summary: " + status.toString());
			requestStatus.addRequestStatusElement(requestId, type, status);
		}
		
		for (GeoCommonsExportRequest exportRequest: exportRequests){
			UUID requestId = exportRequest.getRequestId();
			//logger.info("RequestId: " + requestId.toString());
			String type = "export";
			StatusSummary status = exportRequest.getStatusSummary();
			//logger.info("Image status summary: " + status.toString());
			requestStatus.addRequestStatusElement(requestId, type, status);
		}

		//logger.info(Integer.toString(requestStatus.getRequestStatus().size()));
		return requestStatus;
	}
	

}
