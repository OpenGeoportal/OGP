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
import org.OpenGeoPortal.Ogc.Wcs.WcsGetCoverage1_1_1;
import org.OpenGeoPortal.Solr.SolrRecord;
import org.OpenGeoPortal.Utilities.OgpUtils;
import org.codehaus.jackson.JsonParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class Wcs1_1_1DownloadMethod extends AbstractDownloadMethod implements PerLayerDownloadMethod {	
	private static final Boolean INCLUDES_METADATA = false;
	private static final String METHOD = "POST";

	@Autowired
	@Qualifier("ogcInfoRequest.wcs_1_1_1")
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
    	//all client bboxes should be passed as lat-lon coords.  we will need to get the appropriate epsg code for the layer
		//in order to return the file in original projection to the user 

		SolrRecord layerInfo = this.currentLayer.getLayerInfo();
		BoundingBox nativeBounds = new BoundingBox(layerInfo.getMinX(), layerInfo.getMinY(), layerInfo.getMaxX(), layerInfo.getMaxY());
		BoundingBox bounds = nativeBounds.getIntersection(this.currentLayer.getRequestedBounds());
		String layerName = this.currentLayer.getLayerNameNS();

		Map<String, String> describeCoverageInfo = getWcsDescribeLayerInfo();
		int epsgCode = 4326;
		String outputFormat = "image/tiff;subtype=&quot;geotiff&quot;"; 
		String getCoverageRequest = WcsGetCoverage1_1_1.createWfsGetFeatureRequest(layerName, describeCoverageInfo, bounds, epsgCode, outputFormat);
		

		return getCoverageRequest;	 
	}
	
	@Override
	public List<String> getUrls(LayerRequest layer) throws MalformedURLException, JsonParseException{
		String url = layer.getWcsUrl();
		this.checkUrl(url);
		return urlToUrls(url);
	}
	
	private Map<String, String> getWcsDescribeLayerInfo() throws Exception {
		
			String layerName = this.currentLayer.getLayerNameNS();
			String describeCoverageRequest = ogcInfoRequest.createRequest(layerName);

			InputStream inputStream = this.httpRequester.sendRequest(OgpUtils.filterQueryString(this.getUrl(this.currentLayer)), describeCoverageRequest, ogcInfoRequest.getMethod());
			//parse the returned XML and return needed info as a map
			
			return ogcInfoRequest.parseResponse(inputStream);
		 }
	 
		@Override
		public Boolean includesMetadata() {
			return INCLUDES_METADATA;
		}


}
