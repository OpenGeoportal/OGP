package org.OpenGeoPortal.Ogc;

import java.util.Map;

import org.OpenGeoPortal.Solr.SolrRecord;

public class AugmentedSolrRecord {
	Map<String,String> wmsResponseMap;
	Map<String,String> dataResponseMap;
	SolrRecord solrRecord;
	
	public Map<String, String> getWmsResponseMap() {
		return wmsResponseMap;
	}
	public void setWmsResponseMap(Map<String, String> wmsResponseMap) {
		this.wmsResponseMap = wmsResponseMap;
	}
	public Map<String, String> getDataResponseMap() {
		return dataResponseMap;
	}
	public void setDataResponseMap(Map<String, String> dataResponseMap) {
		this.dataResponseMap = dataResponseMap;
	}
	public SolrRecord getSolrRecord() {
		return solrRecord;
	}
	public void setSolrRecord(SolrRecord solrRecord) {
		this.solrRecord = solrRecord;
	}

}
