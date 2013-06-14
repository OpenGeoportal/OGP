package org.OpenGeoPortal.Solr;

import java.io.IOException;
import java.util.Iterator;

import org.OpenGeoPortal.Download.Config.ConfigRetriever;
import org.OpenGeoPortal.Utilities.ParseJSONSolrLocationField;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;

public class OgpConfigRetriever extends ConfigRetriever implements
		SearchConfigRetriever {

	private @Value("${ogp.proxyToWMS}") String proxyToWMS;
	private @Value("${ogp.proxyToWFS}") String proxyToWFS;
	private @Value("${ogp.proxyToWCS}") String proxyToWCS;

	
	public String getSearchUrl() throws IOException {
		this.readConfigFile();
		JsonNode jsonObject = this.configContents.path("search");
		return jsonObject.path("serviceAddress").getTextValue();
	}

	public String getSearchPort() throws IOException {
		this.readConfigFile();
		JsonNode jsonObject = this.configContents.path("search");
		int searchPort = jsonObject.path("servicePort").asInt();
		return Integer.toString(searchPort);
	}

	public String getSearchType() throws IOException {
		this.readConfigFile();
		JsonNode jsonObject = this.configContents.path("search");
		return jsonObject.path("serviceType").getTextValue();
	}

	public String getHome() throws IOException {
		this.readConfigFile();
		return this.configContents.path("homeInstitution").getTextValue();
	}

	public Boolean isOgp() {
		return true;
	}

	public String getArbitrary(String configKey) throws Exception {
		/*
		 * this only works for strings in the top level. should eventually be
		 * more generic
		 */
		this.readConfigFile();
		return this.configContents.path(configKey).getTextValue();
	}

	private String getProxy(String institution, String accessLevel, String serviceKey)
			throws IOException {
		this.readConfigFile();
		institution = institution.trim();
		accessLevel = accessLevel.trim();
		JsonNode allInstitutionsObject = this.configContents.path("institutions");
		JsonNode institutionObject = allInstitutionsObject.path(institution);
		if (institutionObject.has("proxy")) {
			JsonNode proxyObject = institutionObject.path("proxy");
			ArrayNode accessNode = (ArrayNode) proxyObject.path("accessLevel");
			Iterator<JsonNode> accessIterator = accessNode.getElements();
			while (accessIterator.hasNext()){
				String currentValue = accessIterator.next().getTextValue();
				if (currentValue.equalsIgnoreCase(accessLevel)){
					return proxyObject.path(serviceKey).getTextValue();
				}
			}
			return null;
		
		} else {
			return null;
		}
	}

	@Override
	public String getWmsUrl(SolrRecord solrRecord) throws IOException{
		String institution = solrRecord.getInstitution();
		String access = solrRecord.getAccess();
		
		if (hasWmsProxy(institution, access)){
			return getWmsProxy(institution, access);
		} else {
			return ParseJSONSolrLocationField.getWmsUrl(solrRecord.getLocation());
		}
	}
	
	@Override
	public String getWfsUrl(SolrRecord solrRecord) throws IOException{
		String institution = solrRecord.getInstitution();
		String access = solrRecord.getAccess();
		
		if (hasWfsProxy(institution, access)){
			return getWfsProxy(institution, access);
		} else {
			return ParseJSONSolrLocationField.getWfsUrl(solrRecord.getLocation());
		}
	}
	
	@Override
	public String getWmsProxy(String institution, String accessLevel)
			throws IOException {
		return getProxy(institution, accessLevel, "wms");
	}
	
	@Override
	public String getWfsProxy(String institution, String accessLevel)
			throws IOException {
		return getProxy(institution, accessLevel, "wfs");
	}

	@Override
	public String getWmsProxyInternal(String institution, String accessLevel)
			throws Exception {

		return this.proxyToWMS;
	}

	@Override
	public String getWfsProxyInternal(String institution, String accessLevel)
			throws IOException {
		return this.proxyToWFS;
	}

	@Override
	public boolean hasWmsProxy(String institution, String access) {
		return hasProxy(institution, access, "wms");
	}

	private boolean hasProxy(String institution, String access, String ogcProtocol){
		try {
			if (this.getProxy(institution, access, ogcProtocol) != null){
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean hasWfsProxy(String institution, String access) {
		return hasProxy(institution, access, "wfs");
	}

	@Override
	public String getWcsProxy(String institution, String accessLevel)
			throws IOException {
		return getProxy(institution, accessLevel, "wcs");

	}

	@Override
	public String getWcsProxyInternal(String institution, String accessLevel)
			throws IOException {
		
		return this.proxyToWCS;
	}

	@Override
	public boolean hasWcsProxy(String institution, String access) {
		return hasProxy(institution, access, "wcs");
	}

	@Override
	public String getWcsUrl(SolrRecord solrRecord) throws IOException {
		String institution = solrRecord.getInstitution();
		String access = solrRecord.getAccess();
		
		if (hasWfsProxy(institution, access)){
			return getWcsProxy(institution, access);
		} else {
			return ParseJSONSolrLocationField.getWcsUrl(solrRecord.getLocation());
		}
	}
}
