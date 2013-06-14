package org.OpenGeoPortal.Ogc;

import java.util.Map;

public interface AugmentedSolrRecordRetriever {

	Map<String, String> getWmsInfo(String layerId) throws Exception;

	AugmentedSolrRecord getWmsPlusSolrInfo(String layerId) throws Exception;

	Map<String, String> getOgcDataInfo(String layerId) throws Exception;

	AugmentedSolrRecord getOgcAugmentedSolrRecord(String layerId)
			throws Exception;

}
