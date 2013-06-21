package org.OpenGeoPortal.Metadata;

import java.util.List;
import java.util.Set;

import org.OpenGeoPortal.Solr.SearchConfigRetriever;
import org.OpenGeoPortal.Solr.SolrClient;
import org.OpenGeoPortal.Solr.SolrRecord;
import org.OpenGeoPortal.Utilities.ParseJSONSolrLocationField;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.codehaus.jackson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrLayerInfoRetriever implements LayerInfoRetriever{
	private SearchConfigRetriever searchConfigRetriever;
	private SolrClient solrClient;
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void setSolrClient(SolrClient solrClient) {
		this.solrClient = solrClient;
	}

	public void setSearchConfigRetriever(SearchConfigRetriever searchConfigRetriever) throws Exception{
		this.searchConfigRetriever = searchConfigRetriever;
	}
	
	public List<SolrRecord> fetchAllLayerInfo(Set<String> layerIds) throws SolrServerException {
		SolrServer server = solrClient.getSolrServer();
		String query = "";
		for (String layerId : layerIds){
			logger.debug(layerId);
			query += "LayerId:" + ClientUtils.escapeQueryChars(layerId.trim());
			query += " OR ";
		}
		if (query.length() > 0){
			query = query.substring(0, query.lastIndexOf(" OR "));
		}
        /*ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", query);

            QueryResponse response = server.query(params);*/
		logger.debug("Solr query terms: " + query);
	    SolrQuery queryObj = new SolrQuery();
	    queryObj.setQuery(query);
	    QueryResponse response = null;
	    try {
	    	response = server.query(queryObj);
	    } catch (Exception e){
	    	logger.error(e.getMessage());
	    }
	    //logger.info(response.getResults().get(0).getFieldValue("Name").toString());
	    List<SolrRecord> results = response.getBeans(SolrRecord.class);

		return results;
	}

	/*@Override
	public String getWMSUrl(SolrRecord solrRecord) throws JsonParseException {
		if (hasProxy(solrRecord)){
			String institution = solrRecord.getInstitution();//layerInfo.get("Institution");
			String accessLevel = solrRecord.getAccess();//layerInfo.get("Access")
			try {
				String proxyUrl = this.searchConfigRetriever.getWmsProxy(institution, accessLevel);
				logger.info("Has proxy url: " + proxyUrl);
				return proxyUrl;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Error getting proxy");
				e.printStackTrace();
				return null;
			}
		} else {
			return ParseJSONSolrLocationField.getWmsUrl(solrRecord.getLocation());
		}
	}*/

	
	@Override
	public SolrRecord getAllLayerInfo(String layerId) throws SolrServerException{
		String query = "LayerId:" + layerId.trim();
	    SolrQuery queryObj = new SolrQuery();
	    queryObj.setQuery( query );
		List<SolrRecord> results = solrClient.getSolrServer().query(queryObj).getBeans(SolrRecord.class);
		return results.get(0);
	}

	/*@Override
	public String getWFSUrl(SolrRecord solrRecord) throws JsonParseException {
		logger.info("Has proxy url: " + Boolean.toString(hasProxy(solrRecord)));
		if (hasProxy(solrRecord)){
			String institution = solrRecord.getInstitution();//layerInfo.get("Institution");
			String accessLevel = solrRecord.getAccess();//layerInfo.get("Access")
			try {
				return this.searchConfigRetriever.getWfsProxy(institution, accessLevel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Error getting proxy");
				e.printStackTrace();
				return null;
			}
		} else {
			return ParseJSONSolrLocationField.getWfsUrl(solrRecord.getLocation());
		}
	}*/

}
