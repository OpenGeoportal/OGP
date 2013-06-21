package org.OpenGeoPortal.Proxy.Controllers;

import java.util.Map;

import org.OpenGeoPortal.Metadata.LayerInfoRetriever;
import org.OpenGeoPortal.Ogc.AugmentedSolrRecord;
import org.OpenGeoPortal.Ogc.AugmentedSolrRecordRetriever;
import org.OpenGeoPortal.Solr.SolrRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/info")
public class GetOgcInfoController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	AugmentedSolrRecordRetriever augmentedSolrRecordRetriever;

	@Autowired
	LayerInfoRetriever layerInfoRetriever;
	
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	public @ResponseBody AugmentedSolrRecord getLayerInfo(@RequestParam("OGPID") String layerId) throws Exception {
		return augmentedSolrRecordRetriever.getOgcAugmentedSolrRecord(layerId);

	}
	
	@RequestMapping(value="wmsInfo", method=RequestMethod.GET, produces="application/json")
	public @ResponseBody Map<String,String> wmsInfo(@RequestParam("OGPID") String layerId) throws Exception {
		return augmentedSolrRecordRetriever.getWmsInfo(layerId);
	}
	
	@RequestMapping(value="ogcData", method=RequestMethod.GET, produces="application/json")
	public @ResponseBody Map<String,String> ogcDataInfo(@RequestParam("OGPID") String layerId) throws Exception {
		return augmentedSolrRecordRetriever.getOgcDataInfo(layerId);
	}
	
	@RequestMapping(value="ogp", method=RequestMethod.GET, produces="application/json")
	public @ResponseBody SolrRecord ogcSolrInfo(@RequestParam("OGPID") String layerId, @RequestParam(value="full", defaultValue="false") Boolean includeMetadata) throws Exception {
		SolrRecord record = layerInfoRetriever.getAllLayerInfo(layerId);
		if (includeMetadata){
			return record;
		} else {
			record.setFgdcText(null);
			
			return record;
		}
	}

}
