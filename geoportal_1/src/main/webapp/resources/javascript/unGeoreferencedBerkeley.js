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

/**
 * LayerTable constructor
 * this object defines the behavior of the search results table, as well as the saved layers table
 * 
 *  @param userDiv  the id of the div element to place the table in
 *  @param tableName the id of the actual table element
 */
org.OpenGeoPortal.unGeoreferenced = {

    constructUGURL: function(fileName, location) {

        return location.imageCollection[0].url + "?service=WMS&version=1.1.0&request=GetMap&layers=" + location.imageCollection[0].collection + "&CQL_FILTER=PATH=%27" + location.imageCollection[0].path + "/" + fileName + "%27&styles=&bbox=0.0,-65536.0,65536.0,0.0&width=512&height=512&srs=EPSG:404000&format=application/openlayers";
    },

    previewNG: function(layerID, fileName, location) {

	var imageURL = this.constructNGURL(fileName, location);
	var frameName = "IFrame_" + fileName;


	var shutDownScript = '<script type="text/javascript" src="javascript/jquery/js/jquery-1.6.4.min.js"></script> ';

	shutDownScript += '<script type="text/javascript"> ';
	shutDownScript += 'function shutdown() {';
	shutDownScript += 'opener.org.OpenGeoPortal.layerState.setState(' + layerID + ', {"preview":"off"});';
	shutDownScript += 'opener.org.OpenGeoPortal.previewWindows.deleteLayer(' + layerID + ');'
	shutDownScript += ' }';
	shutDownScript += ' window.onbeforeunload = shutdown;' ;

	shutDownScript += '</script>';



	var frameContent = '<html><head>' +
	    ' <script src="javascript/openlayers/OpenLayers-2.13.1/OpenLayers.debug.js"></script> ' + 
	    ' <script type="text/javascript"> ' +
	    ' 
	    shutDownScript + 
	    '</head><body><table><tr><td>' +
	    '<iframe  id="' + frameName + '"  frameborder="0"  vspace="0" ' +
 	    'hspace="0"  marginwidth="2"  marginheight="2" width="700"  ' +
	    'height="600"  src="' + imageURL + '"></iframe></td></tr>' +
	    '<tr><td>You are previewing an ungeoreferenced image, which cannot be displayed on the map.<br /> Move the image into the center of the window before zooming.</td></tr><tr><td>To see this image in its collection, see <a href="' + 
location.imageCollection[0].collectionurl + '">' + 
location.imageCollection[0].collectionurl + 
'</td></tr></table><button onClick="shutdown();">Shutdown</button></body></html>';


	var w = window.open("", "_blank", "width=800,height=800,status=yes,resizable=yes");

	w.document.write(frameContent);

	var ret = function() { w.close(); }
	return ret;

    }

};


/* Non-georeferenced handling */
/*
	Here is what we want:
http://linuxdev.lib.berkeley.edu:8080/geoserver/UCB/wms?service=WMS&version=1.1.0&request=GetMap&layers=UCB:images&CQL_FILTER=PATH=%27furtwangler_sid/17076013_01_001a_s.sid%27&styles=&bbox=0.0,-65536.0,65536.0,0.0&width=512&height=512&srs=EPSG:404000&format=application/openlayers

	Here is what we get:
	fileName: 17076013_07_072a.tif
	location:
{"imageCollection": {"collection": "UCB:images", "path": "furtwangler", "url": "http://linuxdev.lib.berkeley.edu:8080/geoserver/UCB/wms", collectionurl: "http://www.lib.berkeley.edu/EART/mapviewer/collections/histoposf/"}}

So:
*/

