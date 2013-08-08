if (typeof org == 'undefined'){ 
	org = {};
} else if (typeof org != "object"){
	throw new Error("org already exists and is not an object");
}

// Repeat the creation and type-checking code for the next level
if (typeof org.OpenGeoPortal == 'undefined'){
	org.OpenGeoPortal = {};
} else if (typeof org.OpenGeoPortal != "object"){
    throw new Error("org.OpenGeoPortal already exists and is not an object");
}

org.OpenGeoPortal.unGeoreferenced = {

	baseURL: null,
	layerState: null,
	index: null,
	layers: null,
	myLocation: null,
	CQL: null,
	coda: null,
	layerId: null,
	workspaceName: null,


	test_message: null,


	init: function(layerState, layerID, index, loc, workspaceName, collectionId) {

//    	    alert("org.OpenGeoPortal.unGeoreferenced.init called");

	    var that = this;
		
//            alert("After ajax call");
//	    alert("Passed layerID: " + layerID);  


	    this.layerState = layerState;
  	    this.layerId = layerID;
	    this.index = index;
    	    this.myLocation = loc;
    	    this.workspaceName = workspaceName;
    	    this.baseURL = loc.imageCollection.url;
    	    this.layers = workspaceName + ":" + collectionId;
    	    this.CQL = "PATH='" + loc.imageCollection.path + "'";
    	    this.coda = 'srs=EPSG:404000&format=image/jpeg';
    	    this.test_message = "set in init";

//    	    alert("layerId: " + this.layerId);
//    	    alert("baseURL: " + this.baseURL);


  	},

	getValues: function() { 
	    return this.collectValues.call(this); 
	},

	test: function() { 
	    return this.test_message;
        },


	collectValues: function() {

/*
	    alert("Message from collectValues: " + this.test_message);
	
	    alert("baseURL in collectValues: " + this.baseURL);
	    alert("layers in collectValues: " + this.layers);
	    alert("CQL in collectValues: " + this.CQL);
	    alert("coda in collectValues: " + this.coda);
*/


	    return { layerState: this.layerState,
		     layerId: this.layerId,
		     index: this.index,
	             location: this.myLocation,
		     baseURL: this.baseURL,
	             layers: this.layers,
	             CQL: this.CQL,
	             coda: this.coda,
		     collectionurl: this.myLocation.imageCollection.collectionurl 
	           };
       },
	
       previewUG: function() {


//         var imageURL = this.constructNGURL(location, workspaceName, collectionId);

   	    var noid = this.layerId;
            if(noid.substr("/") != -1) { noid = noid.substr(noid.indexOf("/")); }
    	    var frameName = "IFrame_" + noid;



/*
    alert("image_points.maxx: " + this.image_points.maxx); 
    alert("baseURL: " + org.OpenGeoPortal.unGeoreferenced.baseURL);
    alert("layers: " + org.OpenGeoPortal.unGeoreferenced.layers); 
    alert("CQL: " + org.OpenGeoPortal.unGeoreferenced.CQL); 
    alert("coda: " + org.OpenGeoPortal.unGeoreferenced.coda); 
    alert("collectionurl: " + org.OpenGeoPortal.unGeoreferenced.myLocation.imageCollection.collectionurl); 
*/

            var w = window.open("resources/frameContent.html", 
			        "_blank", 
				"width=800,height=800,status=yes,resizable=yes");

/*
            alert("Calling test");
            w.test();
            var ret = function() { w.close(); }
*/
            return w;

  }
};


/*

http://gis.lib.berkeley.edu:8080/geoserver/wms?version=1.1.0&request=GetMap&layers=UCB:images&CQL_FILTER=PATH=%27furtwangler/17076013_03_028a.tif%27&bbox=0.0,-8566.0,6215.0,0.0&width=6215&height=8566&srs=EPSG:404000&format=image/jpeg

This works better.
*/


/*

        return location.imageCollection.url + 
	"?version=1.1.0&request=GetMap&layers=" + 
	workSpaceName + ":" + collectionId +  
	"&CQL_FILTER=PATH=%27" + 
	location.imageCollection.path + 
	"%27&styles=&bbox=" + 
	image_size.minx + ", " + 
	image_size.miny + ", " + 
	image_size.maxx + ", " +
	image_size.maxy + 
	"&width=" + maxx +
	"&height=" + abs(image_size.miny) +
	"&srs=EPSG:404000&format=image/jpeg";
    },


	    '<iframe  id="' + frameName + '"  frameborder="0"  vspace="0" ' +
 	    'hspace="0"  marginwidth="2"  marginheight="2" width="700"  ' +
	    'height="600"  src="' + imageURL + '"></iframe>

*/

/*

    var windowScript = '<script type="text/javascript"> ';
    windowScript += 'var map; var image; ';
    windowScript += 'OpenLayers.IMAGE_RELOAD_ATTEMPTS = 5; ';

// make OL compute scale according to WMS spec
    windowScript += 'OpenLayers.DOTS_PER_INCH = 25.4 / 0.28; ';
    windowScript += 'function UGOLinit(image_size, baseURL, layers, CQL, coda) { ';
    windowScript += 'var bounds = new OpenLayers.Bounds(0, image_size.miny, image_size.maxx, 0); ';
    windowScript += 'var options = { controls: [], maxExtent: bounds, maxResolution: 32, numZoomLevels: 6, projection: "EPSG:404000", units: "m", allowOverlays: true }; ';
    windowScript += 'map = new OpenLayers.Map("map", options); ';
    windowScript += 'alert("In UGOLinit, image_size: " + image_size); alert("In UGOLinit, baseURL: " + baseURL); alert("In UGOLinit, layers: " + layers); alert("In UGOLinit, CQL: " + CQL); alert("In UGOLinit, coda: " + coda);';
    windowScript += 'image = new OpenLayers.Layer.WMS( "UCB:images - Tiled", baseURL, {CQL_FILTER: CQL, LAYERS: layers, STYLES: "", format: "image/jpeg", palette: "safe", tiled: true, tilesOrigin : map.maxExtent.left + ", " + map.maxExtent.bottom, version: "1.1.0", bbox: image_size.minx + ", " + image_size.miny + ", " + image_size.maxx + ", " + image_size.maxy, width: image_size.maxx, height: Math.abs(image_size.miny), srs: "EPSG:404000"}, { buffer: 0, displayOutsideMaxExtent: true, isBaseLayer: true } );';
   windowScript += 'map.addLayers(image); map.zoomToMaxExtent(); } </script>';


    var shutDownScript = '<script type="text/javascript"> ';
    shutDownScript += 'function shutdown() {';
    shutDownScript += 'opener.org.OpenGeoPortal.layerState.setState(' + this.layerID + ', {"preview":"off"});';
    shutDownScript += 'opener.org.OpenGeoPortal.previewWindows.deleteLayer(' + this.layerID + ');'
    shutDownScript += ' }';
    shutDownScript += ' window.onbeforeunload = shutdown;';
    shutDownScript += '</script>';

    var frameContent = '<html><head>' +
	               '<script type="text/javascript" src="javascript/jquery/js/jquery-1.6.4.min.js"></script> ' +
	               ' <script type="text/javascript" src="javascript/openlayers/OpenLayers-2.13.1/OpenLayers.debug.js"></script> ' + 
	               windowScript + shutDownScript + 
		       '<script type="text/javascript">$jQuery.ready(UGOLinit(this.image_size, this.baseURL, this.layers, this.CQL, this.coda);</script> ' +
	               '</head>' + 
	               '<body>' +
	               '<table><tr><td><div id="map"></div></td></tr>' +
	               '<tr><td>You are previewing an ungeoreferenced image, which cannot be displayed on the map.<br /> Move the image into the center of the window before zooming.</td></tr><tr><td>To see this image in its collection, see <a href="' + 
                       this.location.imageCollection.collectionurl + '">' + 
                       this.location.imageCollection.collectionurl + 
                       '</td></tr></table><button onClick="shutdown();">Shutdown</button></body></html>';


*/

/* Non-georeferenced handling */
/*
	Here is what we want:
http://linuxdev.lib.berkeley.edu:8080/geoserver/UCB/wms?service=WMS&version=1.1.0&request=GetMap&layers=UCB:images&CQL_FILTER=PATH=%27furtwangler_sid/17076013_01_001a_s.sid%27&styles=&bbox=0.0,-65536.0,65536.0,0.0&width=512&height=512&srs=EPSG:404000&format=application/openlayers

	Here is what we get:
	fileName: 17076013_07_072a.tif
	location:
{"imageCollection": {"path": "furtwangler/17076013_07_072a.tif", "url": "http://linuxdev.lib.berkeley.edu:8080/geoserver/UCB/wms", collectionurl: "http://www.lib.berkeley.edu/EART/mapviewer/collections/histoposf/"}}

So:
*/

