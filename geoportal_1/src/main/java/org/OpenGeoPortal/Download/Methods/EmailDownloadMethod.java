package org.OpenGeoPortal.Download.Methods;

import java.util.List;
import java.util.concurrent.Future;

import org.OpenGeoPortal.Download.Types.LayerRequest;

public interface EmailDownloadMethod {

	Boolean includesMetadata();

	String createDownloadRequest();

	Future<Boolean> sendEmail(List<LayerRequest> layerList);

	Boolean hasRequiredInfo(LayerRequest layer);

}
