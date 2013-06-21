package org.OpenGeoPortal.Download.Methods;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.OpenGeoPortal.Download.Types.BoundingBox;
import org.OpenGeoPortal.Download.Types.LayerRequest;
import org.OpenGeoPortal.Ogc.OgcInfoRequest;
import org.OpenGeoPortal.Ogc.Wfs.WfsGetFeature;
import org.OpenGeoPortal.Solr.SolrRecord;
import org.codehaus.jackson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class WfsDownloadMethod extends AbstractDownloadMethod implements PerLayerDownloadMethod {	
	private static final Boolean INCLUDES_METADATA = false;
	private static final String METHOD = "POST";
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	@Qualifier("ogcInfoRequest.wfs")
	private OgcInfoRequest ogcInfoRequest;
	
	@Override
	public String getMethod(){
		return METHOD;
	}
	
	@Override
	public Set<String> getExpectedContentType(){
		Set<String> expectedContentType = new HashSet<String>();
		expectedContentType.add("application/zip");
		return expectedContentType;
	}
	
	public String createDownloadRequest() throws Exception {
		//--generate POST message
		//info needed: geometry column, bbox coords, epsg code, workspace & layername
	 	//all client bboxes should be passed as lat-lon coords.  we will need to get the appropriate epsg code for the layer 
	 	//in order to return the file in original projection to the user (will also need to transform the bbox)
		String layerName = this.currentLayer.getLayerNameNS();
		SolrRecord layerInfo = this.currentLayer.getLayerInfo();
		BoundingBox nativeBounds = new BoundingBox(layerInfo.getMinX(), layerInfo.getMinY(), layerInfo.getMaxX(), layerInfo.getMaxY());
		BoundingBox bounds = nativeBounds.getIntersection(this.currentLayer.getRequestedBounds());

		String workSpace = layerInfo.getWorkspaceName();
		Map<String, String> describeLayerInfo = getWfsDescribeLayerInfo();
		String geometryColumn = describeLayerInfo.get("geometryColumn");
		String nameSpace = describeLayerInfo.get("nameSpace");
		int epsgCode = 4326;//we are filtering the bounds based on WGS84
		
		String bboxFilter = "";
		if (!nativeBounds.isEquivalent(bounds)){

  			bboxFilter += WfsGetFeature.getBboxFilter(bounds, geometryColumn, epsgCode);
		}
		
		//really, we should check the get caps doc to see if this is a viable option...probably this should be done before/at the download prompt
		String outputFormat = "shape-zip";
		
		return WfsGetFeature.createWfsGetFeatureRequest(layerName, workSpace, nameSpace, outputFormat, bboxFilter);

	}
	 
	@Override
	public List<String> getUrls(LayerRequest layer) throws MalformedURLException, JsonParseException{
		String url = layer.getWfsUrl();
		this.checkUrl(url);
		return urlToUrls(url);
	}
	
	 Map<String, String> getWfsDescribeLayerInfo() throws Exception {
		String layerName = this.currentLayer.getLayerNameNS();
	 	String describeFeatureRequest = ogcInfoRequest.createRequest(layerName);
	 	String method = ogcInfoRequest.getMethod();
	 	String url = this.getUrl(this.currentLayer);
		InputStream inputStream = this.httpRequester.sendRequest(url, describeFeatureRequest, method);
		String contentType = this.httpRequester.getContentType();

		if (!contentType.contains("xml")){
			throw new Exception("Expecting an XML response; instead, got content type '" + contentType + "'");
		}
		
		return ogcInfoRequest.parseResponse(inputStream);
	 }


	@Override
	public Boolean includesMetadata() {
		return INCLUDES_METADATA;
	}

}
