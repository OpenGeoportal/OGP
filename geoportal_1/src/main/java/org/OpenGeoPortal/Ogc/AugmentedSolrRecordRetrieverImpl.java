package org.OpenGeoPortal.Ogc;

import java.util.Map;

import org.OpenGeoPortal.Solr.SolrRecord;
import org.OpenGeoPortal.Utilities.ParseJSONSolrLocationField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class AugmentedSolrRecordRetrieverImpl implements AugmentedSolrRecordRetriever {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final int WMS_ATTEMPTS = 3;
	private final int DATA_ATTEMPTS = 3;
	private final int PAUSE = 200;//milliseconds

	@Autowired
	@Qualifier("ogcInfoRequester.wms")
	private OgcInfoRequester wmsRequester;
	@Autowired
	@Qualifier("ogcInfoRequester.wfs")
	private OgcInfoRequester wfsRequester;
	@Autowired
	@Qualifier("ogcInfoRequester.wcs_1_1_1")
	private OgcInfoRequester wcsRequester;
	
	@Override
	public Map<String,String> getWmsInfo(String layerId) throws Exception{
		return this.getWmsPlusSolrInfo(layerId).getWmsResponseMap();
	}
	
	@Override
	public AugmentedSolrRecord getWmsPlusSolrInfo(String layerId) throws Exception{
		AugmentedSolrRecord asr = getInfoAttempt(wmsRequester, WMS_ATTEMPTS, layerId);
		return asr;
	}
	
	@Override
	public Map<String,String> getOgcDataInfo(String layerId) throws Exception {
		return getOgcAugmentedSolrRecord(layerId).getDataResponseMap();
	}
	
	@Override
	public AugmentedSolrRecord getOgcAugmentedSolrRecord(String layerId) throws Exception {

		AugmentedSolrRecord asr = getWmsPlusSolrInfo(layerId);
		String type = asr.getWmsResponseMap().get("owsType");
		//String qualName = wmsInfo.getWmsResponseMap().get("qualifiedName");
		String owsUrl = asr.getWmsResponseMap().get("owsUrl");
		Thread.sleep(PAUSE);

		AugmentedSolrRecord dataInfo = null;

		if (type.equalsIgnoreCase("wfs")){
			try{
				ParseJSONSolrLocationField.getWfsUrl(asr.getSolrRecord().getLocation());
				dataInfo = getInfoAttempt(wfsRequester, DATA_ATTEMPTS, asr.getSolrRecord());
				asr.setDataResponseMap(dataInfo.getDataResponseMap());
				
			} catch (Exception e){
				logger.info("trying retrieved URL: " + owsUrl);
				try {
					dataInfo = getInfoAttempt(wfsRequester, DATA_ATTEMPTS, asr.getSolrRecord(), owsUrl);
					asr.setDataResponseMap(dataInfo.getDataResponseMap());
				} catch (Exception e1){
					return asr;
				}
			}
		} else if (type.equalsIgnoreCase("wcs")){
			try{
				ParseJSONSolrLocationField.getWcsUrl(asr.getSolrRecord().getLocation());

				dataInfo = getInfoAttempt(wcsRequester, DATA_ATTEMPTS, asr.getSolrRecord());
				asr.setDataResponseMap(dataInfo.getDataResponseMap());
			} catch (Exception e){
				try {
					dataInfo = getInfoAttempt(wcsRequester, DATA_ATTEMPTS, asr.getSolrRecord(), owsUrl);
					asr.setDataResponseMap(dataInfo.getDataResponseMap());
				} catch (Exception e1){
					return asr;
				}
			}
		}

		return asr;
	}
	
	
	private AugmentedSolrRecord getInfoAttempt(OgcInfoRequester requester, int numAttempts, String layerId) throws Exception{
		AugmentedSolrRecord asr = null;

		for (int i = 0; i < numAttempts; i++ ){
			logger.info("Attempt " + (i + 1));

			try{
				asr = requester.getOgcAugment(layerId);
				if (asr == null){
					continue;
				} else {
					return asr;
				}
			} catch (Exception e){
				//just continue
			}
			Thread.sleep(PAUSE * (i + 1));
		}

		if (asr == null){
			throw new Exception("Error reaching the OGC server.");
		} else {
			return asr;
		}
	}
	
	private AugmentedSolrRecord getInfoAttempt(OgcInfoRequester requester, int numAttempts, SolrRecord solrRecord) throws Exception{
		AugmentedSolrRecord asr = null;
		for (int i = 0; i < numAttempts; i++ ){
			logger.info("Attempt " + (i + 1));
			try{
				asr = requester.getOgcAugment(solrRecord);
				if (asr == null){
					continue;
				} else {
					return asr;
				}
			} catch (Exception e){
				
			}
			Thread.sleep(PAUSE * (i + 1));

		}
		if (asr == null){
			throw new Exception("Error reaching the OGC server.");
		} else {
			return asr;
		}
	}
	
	private AugmentedSolrRecord getInfoAttempt(OgcInfoRequester requester, int numAttempts, SolrRecord solrRecord, String url) throws Exception{
		AugmentedSolrRecord asr = null;
		for (int i = 0; i < numAttempts; i++ ){
			logger.info("Attempt " + (i + 1));

			try{
				asr = requester.getOgcAugment(solrRecord, url);
				if (asr == null){
					continue;
				} else {
					return asr;
				}
			} catch (Exception e){
				
			}
			Thread.sleep(PAUSE * (i + 1));

		}
		if (asr == null){
			throw new Exception("Error reaching the OGC server.");
		} else {
			return asr;
		}
	}
}
