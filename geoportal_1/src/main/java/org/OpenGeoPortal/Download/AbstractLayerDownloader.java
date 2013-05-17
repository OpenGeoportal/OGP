package org.OpenGeoPortal.Download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for downloading layers
 * @author chris
 *
 */
//the layer downloader should handle all the errors thrown by the download method,
//and take care of layer status
abstract class AbstractLayerDownloader implements LayerDownloader {
	private String responseFileType;
	private String responseFileName;
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public String getResponseFileType() {
		return this.responseFileType;
	}
	
	public String getResponseFileName() {
		return this.responseFileName;
	}
	
	public void setResponseFileName(String responseFileName) {
		this.responseFileName = responseFileName;
	}
	
	abstract public void downloadLayers(MethodLevelDownloadRequest request) throws Exception;
	
}
