package org.OpenGeoPortal.Ogc;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.OpenGeoPortal.Metadata.LayerInfoRetriever;
import org.OpenGeoPortal.Solr.SearchConfigRetriever;
import org.OpenGeoPortal.Solr.SolrRecord;
import org.OpenGeoPortal.Utilities.OgpUtils;
import org.OpenGeoPortal.Utilities.Http.HttpRequester;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OgcInfoRequesterImpl implements OgcInfoRequester {
	private HttpRequester httpRequester;
	private OgcInfoRequest ogcInfoRequest;
	private LayerInfoRetriever layerInfoRetriever;
	private SearchConfigRetriever searchConfigRetriever;
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public HttpRequester getHttpRequester() {
		return httpRequester;
	}

	public void setHttpRequester(HttpRequester httpRequester) {
		this.httpRequester = httpRequester;
	}

	public OgcInfoRequest getOgcInfoRequest() {
		return ogcInfoRequest;
	}

	public void setOgcInfoRequest(OgcInfoRequest ogcInfoRequest) {
		this.ogcInfoRequest = ogcInfoRequest;
	}

	public LayerInfoRetriever getLayerInfoRetriever() {
		return layerInfoRetriever;
	}

	public void setLayerInfoRetriever(LayerInfoRetriever layerInfoRetriever) {
		this.layerInfoRetriever = layerInfoRetriever;
	}

	public SearchConfigRetriever getSearchConfigRetriever() {
		return searchConfigRetriever;
	}

	public void setSearchConfigRetriever(SearchConfigRetriever searchConfigRetriever) {
		this.searchConfigRetriever = searchConfigRetriever;
	}


	private Map<String,String> handleResponse(String contentType, InputStream inputStream) throws Exception{
		logger.info(contentType);
		Boolean contentMatch = contentType.toLowerCase().contains("xml");
		if (!contentMatch){
			logger.error("Unexpected content type: " + contentType);
			//If there is a mismatch with the expected content, but the response is text, we want to at least log the response
			if (contentType.toLowerCase().contains("text")||contentType.toLowerCase().contains("html")||contentType.toLowerCase().contains("xml")){
				logger.error("Returned text: " + IOUtils.toString(inputStream));
			} 
		
			throw new Exception("Unexpected content type");

		} else {
			try{
				return ogcInfoRequest.parseResponse(inputStream);
			} catch (Exception e){
				e.printStackTrace();
				throw new Exception("Could not parse response");
			}
		}
	}
		
	public Map<String,String> getResponseMap(SolrRecord solrRecord, String owsUrl) throws Exception {
		
		String layerName = OgpUtils.getLayerNameNS(solrRecord.getWorkspaceName(), solrRecord.getName());
		
		String request = ogcInfoRequest.createRequest(layerName);
		String method = ogcInfoRequest.getMethod();
		logger.info(owsUrl);
		logger.info(request);
		logger.info(method);
		InputStream is = httpRequester.sendRequest(owsUrl, request, method);
		int status = httpRequester.getStatus();
		if (status == 200){
			String contentType = httpRequester.getContentType().toLowerCase();
			return handleResponse(contentType, is);
		} else {
			return handleError(status);
		}
		
	}
	

	public Map<String,String> getResponseMap(SolrRecord solrRecord) throws Exception {
		
		String layerName = OgpUtils.getLayerNameNS(solrRecord.getWorkspaceName(), solrRecord.getName());
		
		String request = ogcInfoRequest.createRequest(layerName);
		String method = ogcInfoRequest.getMethod();
		String protocol = ogcInfoRequest.getOgcProtocol();
		String url = "";
		if (protocol.equalsIgnoreCase("wms")){
			url = searchConfigRetriever.getWmsUrl(solrRecord); 
		} else if (protocol.equalsIgnoreCase("wfs")){
			url = searchConfigRetriever.getWfsUrl(solrRecord);
		} else if (protocol.equalsIgnoreCase("wcs")){
			url = searchConfigRetriever.getWcsUrl(solrRecord);
		}
		
		InputStream is = httpRequester.sendRequest(url, request, method);
		int status = httpRequester.getStatus();
		if (status == 200){
			String contentType = httpRequester.getContentType().toLowerCase();
			return handleResponse(contentType, is);
		} else {
			return handleError(status);
		}
		
	}
	

	private Map<String, String> handleError(int status) {
		logger.error("Returned error status code: " + Integer.toString(status));
		Map<String,String> errorMap = new HashMap<String,String>();
		errorMap.put("errorStatus", Integer.toString(status));
		return errorMap;
	}

	public Map<String,String> getResponseMap(String layerId) throws Exception {
		SolrRecord solrRecord = layerInfoRetriever.getAllLayerInfo(layerId);
		
		return getResponseMap(solrRecord);
		
	}

	@Override
	public AugmentedSolrRecord getOgcAugment(String layerId) throws Exception {
		SolrRecord solrRecord = layerInfoRetriever.getAllLayerInfo(layerId);

		return getOgcAugment(solrRecord);
	}

	@Override
	public AugmentedSolrRecord getOgcAugment(String layerId, String owsUrl)
			throws Exception {
		SolrRecord solrRecord = layerInfoRetriever.getAllLayerInfo(layerId);
		return getOgcAugment(solrRecord, owsUrl);
	}
	
	@Override
	public AugmentedSolrRecord getOgcAugment(SolrRecord solrRecord, String owsUrl)
			throws Exception {
		AugmentedSolrRecord asr = new AugmentedSolrRecord();
		asr.setSolrRecord(solrRecord);
		Map<String,String> rmap = getResponseMap(solrRecord, owsUrl);
		if (rmap.containsKey("owsUrl")){
			asr.setWmsResponseMap(rmap);
		} else {
			asr.setDataResponseMap(rmap);
		}
		return asr;
	}

	@Override
	public AugmentedSolrRecord getOgcAugment(SolrRecord solrRecord)
			throws Exception {
		AugmentedSolrRecord asr = new AugmentedSolrRecord();
		asr.setSolrRecord(solrRecord);
		Map<String,String> rmap = getResponseMap(solrRecord);
		if (rmap.containsKey("owsUrl")){
			asr.setWmsResponseMap(rmap);
		} else {
			asr.setDataResponseMap(rmap);
		}
		return asr;
	}
	

}
