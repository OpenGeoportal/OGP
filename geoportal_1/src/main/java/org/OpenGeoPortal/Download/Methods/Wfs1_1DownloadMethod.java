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
import org.OpenGeoPortal.Solr.SolrRecord;
import org.codehaus.jackson.JsonParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class Wfs1_1DownloadMethod extends AbstractDownloadMethod implements PerLayerDownloadMethod {	
	private static final Boolean INCLUDES_METADATA = false;
	private static final String METHOD = "POST";

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
		int epsgCode = 4326;//we are filtering the bounds based on WGS84
		String geometryColumn = describeLayerInfo.get("geometryColumn");
		String nameSpace = describeLayerInfo.get("nameSpace");
		String bboxFilter = "";
		if (!nativeBounds.isEquivalent(bounds)){
  			bboxFilter += "<ogc:Filter>"
      		+		"<ogc:BBOX>"
        	+			"<ogc:PropertyName>" + geometryColumn + "</ogc:PropertyName>"
        	+			bounds.generateGMLEnvelope(epsgCode)
        	+		"</ogc:BBOX>"
      		+	"</ogc:Filter>";
		}
		// TODO should be xml
		String getFeatureRequest = "<wfs:GetFeature service=\"WFS\" version=\"1.1.0\""
			+ " outputFormat=\"shape-zip\""
			+ " xmlns:" + workSpace + "=\"" + nameSpace + "\""
  			+ " xmlns:wfs=\"http://www.opengis.net/wfs\""
  			+ " xmlns:ogc=\"http://www.opengis.net/ogc\""
  			+ " xmlns:gml=\"http://www.opengis.net/gml\""
  			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
  			+ " xsi:schemaLocation=\"http://www.opengis.net/wfs"
            + " http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\">"
  			+ "<wfs:Query typeName=\"" + layerName + "\">"
  			+ bboxFilter
  			+ "</wfs:Query>"
			+ "</wfs:GetFeature>";

    	return getFeatureRequest;
	}
	
	@Override
	public List<String> getUrls(LayerRequest layer) throws MalformedURLException, JsonParseException{
		String url = layer.getWfsUrl();
		this.checkUrl(url);
		return urlToUrls(url);
	}
	
	 Map<String, String> getWfsDescribeLayerInfo()
	 	throws Exception {

		String layerName = this.currentLayer.getLayerNameNS();

		InputStream inputStream = this.httpRequester.sendRequest(this.getUrl(this.currentLayer), ogcInfoRequest.createRequest(layerName), ogcInfoRequest.getMethod());
		System.out.println(this.httpRequester.getContentType());//check content type before doing any parsing of xml?

		return ogcInfoRequest.parseResponse(inputStream);
	 }


	@Override
	public Boolean includesMetadata() {
		return INCLUDES_METADATA;
	}

}
