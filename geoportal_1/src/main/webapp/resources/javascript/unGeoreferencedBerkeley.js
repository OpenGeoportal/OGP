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

http://linuxdev.lib.berkeley.edu:8080/geoserver/UCB/wms?service=WMS&version=1.1.0&request=GetMap&layers=UCB:images&CQL_FILTER=PATH=%27furtwangler_sid/17076013_01_001a_s.sid%27&styles=&bbox=0.0,-65536.0,65536.0,0.0&width=512&height=512&srs=EPSG:404000&format=application/openlayers

	Here is what we get:
	fileName: 17076013_07_072a.tif
	location:
{"imageCollection": {"path": "furtwangler/17076013_07_072a.tif", "url": "http://linuxdev.lib.berkeley.edu:8080/geoserver/UCB/wms", collectionurl: "http://www.lib.berkeley.edu/EART/mapviewer/collections/histoposf/"}}

So:
*/

