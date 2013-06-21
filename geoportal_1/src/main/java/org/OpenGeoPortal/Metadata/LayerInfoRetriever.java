package org.OpenGeoPortal.Metadata;

import java.util.List;
import java.util.Set;

import org.OpenGeoPortal.Solr.SolrRecord;

public interface LayerInfoRetriever {
	public List<SolrRecord> fetchAllLayerInfo(Set<String> layerIds) throws Exception;
	SolrRecord getAllLayerInfo(String layerId) throws Exception;

}
