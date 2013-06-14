package org.OpenGeoPortal.Ogc;

import java.io.InputStream;
import java.util.Map;

public interface OgcInfoRequest {

	String createRequest(String layerName);

	Map<String, String> parseResponse(InputStream inputStream) throws Exception;

	String getMethod();

	String getOgcProtocol();

	String getVersion();

}
