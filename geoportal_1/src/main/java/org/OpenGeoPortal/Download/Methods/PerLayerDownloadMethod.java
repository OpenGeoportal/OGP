package org.OpenGeoPortal.Download.Methods;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Future;

import org.OpenGeoPortal.Download.Types.LayerRequest;

public interface PerLayerDownloadMethod {

	Future<Set<File>> download(LayerRequest currentLayer) throws Exception;
	Boolean includesMetadata();
	Boolean hasRequiredInfo(LayerRequest layer);

}
