OGP
===

This is the active, community repository for the portal (including client JavaScript and server-side authentication and download Java code).

Installation
Installation of the OpenGeoportal v1.2.  

Requirements
The OpenGeoportal WAR file, a Solr 4.x WAR file, an OGP Solr schema, and Tomcat 6.x
A note on OS's:  Our development and production servers at Tufts both run Red Hat linux and have apache 2.x running in front.  However, I routinely run the OpenGeoportal code on MacOS 10.8 for my development work.  I don't know how the code runs under Windows or in other containers.

Generating a WAR file
Currently the only way to get a WAR file for the OpenGeoportal is to clone the project from github and generate one from a Maven build.  See the development docs for more information on how to do this.  Once you have a WAR file, it should run in Tomcat as is.  I tend to manually extract it in place with jar -xvf rather than letting Tomcat autodeploy.

A Note on Tomcat Configuration
This is going to be system dependent, and likely, if you have sysadmins, they will have a preferred way of doing this.  My personal preference is to have separate Tomcat instances for Solr and the OpenGeoportal, and in fact, there is no necessity for them to even run on the same server.  
One thing you should do: add mime-type application/json, text/json to your ‘server.xml’ Otherwise, you may get odd warning messages from your javascript console when loading ogpConfig.json.
See the development docs for details on running on your local machine with Eclipse.

Configuration
Basic configuration of the OpenGeoportal can be done by editing the json file resources/ogpConfig.json.
{"config":{"homeInstitution": "Tufts", "institutionSpecificCss": "", 
        "institutionSpecificJavaScript": "", 
  "googleAnalyticsId": "YourAnalyticsId", 
	"googleAPIKey": "YourAPIKey",
Setting the Home Institution
Set the name of the Home Institution. This lets the portal code know if a particular layer is local to the institution running the layer. "homeInstitution" must be identical to the name defined in the Solr records for your institution. If you don't have any Solr records for your institution yet, you should still change this value.
institutionSpecificCss
This is a relative path to a css file that you can use to customize your instance of the OGP. It contains css rules that define logos, backgrounds and colors.  You can use institutionTufts.css as an example.
institutionSpecificJavaScript
If you develop custom code to override OGP methods or extend OGP objects, this is the place to put it. 
googleAnalyticsId
If you want to use Google Analytics for your OGP site, put in your analytics ID here. 
googleAPIKey
the google api key is still needed to use google’s link shortener api for “share link”
How to let the web client know about your data
Add an entry to ogpConfig.json under institutions. The Tufts entry looks like this: 
"institutions": {"Tufts": {"login": {"loginType": "form", "authenticationPage": "login"}, "proxy": {"id": "proxy.Restricted.Tufts", "accessLevel": ["restricted"], "wms": "restricted/wms", "wfs": "restricted/wfs"}, "graphics":{"sourceIcon":{"resourceLocation": "resources/media/src_tufts.png", "altDisplay": "Tufts", "tooltipText": "Tufts University"}}}, 

The institution name needs to match the institution name you used for homeInstitution. 
“login”:
“loginType” is either “form” or “iframe”.  If you’re using form based login, like ldap, use “form”, if your authentication system requires login to an external web page (CAS, shibboleth, etc.) choose “iframe”.  “authenticationPage” is the local login page.

“proxy”:
If you have non-public layers, this defines the terms for re-routing requests so that they can be resolved by OGP once the user has been logged in.  “id” is the name of the Spring Bean which forwards the requests.  “accessLevel” defines which layers are forwarded (must be a layer local to the defined “homeInstitution”) and “wms” and “wfs“ define the endpoints to which the layer requests are forwarded.  There is some special config required to use this “proxy”.
"sourceIcon" defines what icon appears in the search results and in the "Select Repositories" dropdown under Advanced Search. The icon should be the same size as the others (17px x 17px) to preserve formatting. Preferably, it should be in a similar style (grey-scale, similar shading) 

Configuring Download
Downloading and collating geospatial data can be complex as there are many possibilities for file formats and data types, as well the need to interface with existing systems. However, if you plan to use OGP's download methods, which currently uses OGC web services to retrieve data (assuming that you are running GeoServer? and have your layers configured), configuring download is relatively easy. 
1. Define entries in WEB-INF/download.xml: Using Tufts entries as a model, simply replace the string "Tufts" with your institution name. 
  <!-- Tufts -->
   <bean id="layerDownloader.vector.Tufts" class="org.OpenGeoPortal.Download.UnpackagedLayerDownloader">
                <property name="downloadMethod" ref="downloadMethod.wfs"/>
  </bean>
 
  <bean id="layerDownloader.kml.Tufts" class="org.OpenGeoPortal.Download.UnpackagedLayerDownloader">
                <property name="downloadMethod" ref="downloadMethod.kml"/>
  </bean>
  
  <bean id="layerDownloader.raster.Tufts" class="org.OpenGeoPortal.Download.UnpackagedLayerDownloader">
                <property name="downloadMethod" ref="downloadMethod.wcs"/>
  </bean>
2. Define entries in ogpDownloadConfig.json: Again, using the "Tufts" entry as a template: 
"Tufts":[{"classKey": "layerDownloader.kml.Tufts", "accessLevel":["public"],"dataType":["vector", "raster"], 
                "outputFormats":["kml", "kmz"], "params":{"serviceAddress": "http://geoserver01.uit.tufts.edu/wms/kml"}},
                {"classKey": "layerDownloader.vector.Tufts", "accessLevel":["public","restricted"],"dataType":["vector"], "outputFormats":["shp"], "params":{"serviceAddress": "http://geoserver01.uit.tufts.edu/wfs"}},
                {"classKey": "layerDownloader.raster.Tufts", "accessLevel":["public","restricted"],"dataType":["raster"],"outputFormats":["tif"], "params":{"serviceAddress": "http://geoserver01.uit.tufts.edu/wcs"}}],
Replace the string "Tufts" with your institution name and the "serviceAddress" values as appropriate. If you are running GeoServer?, the service endpoints will be ${geoserver address}/wms/kml (kml reflector) for kml download, ${geoserver address}/wfs for vector download, and ${geoserver address}/wcs for raster download. Using a different download method will almost definitely entail writing some custom code, which we would be happy to discuss. 
As our security model evolves, there could be changes here in the future. 
Run Portal
Start Tomcat once you are ready to run the portal. If you have defined your institution under ogpConfig.json, appropriate UI elements will be auto-generated. Simply go to the portal's URL.  Tufts production instance is http://geodata.tufts.edu. 
Setting up Solr
By default, the OpenGeoportal instance will search against the Tufts Solr instance.  If you want your own instance of Solr to search against, you will need to install Solr.
Solr 4.x is readily available at: http://lucene.apache.org/solr.  Follow the installation instructions available there.  The most important thing is to set “solr/home”.  The default core name doesn't matter much unless you intend to run multiple cores in this solr instance.
The Solr schema and current synonym files for v1.2 can be found at: https://github.com/OpenGeoportal/ogpSolrConfig.  We are currently using Solr 4.0 in production at Tufts.  Later versions should work fine, but it is advised to update the version of the Solr jars referenced in the OpenGeoportal pom.xml file to match the version you use before building your OGP war file.

Set the Solr Server URL

The Solr URL is set in resources/ogpConfig.json under "search". 
	"search":{"serviceType": "solr", "serviceAddress": "http://geodata.tufts.edu/solr/select"}, 
Until you change this, your search results come from the Solr instance running at Tufts.  
Sharding
The OpenGeoportal provides some support for searching multiple Solr instances. To get results from more then one Solr instance, provide a list of multiple URLs for "serviceAddress". The request will be sent to the first server in the list while a "shard" parameter specifies what servers to search against. Using a single, local Solr instance is quite fast; searches are on the order of 20 milliseconds. Using two Solr instances is substantially slower, but still fast. With one Solr at Tufts and one at Berkeley, searches average around 300 milliseconds. 
“search":{"serviceType": "solr", "serviceAddress": "http://geodata.tufts.edu/solr/select,http://gis.lib.berkeley.edu:8080/solr/select"},
Run Solr
With these elements in place, you can start Tomcat using bin/startup.sh. You should verify Solr is running by visiting its admin web page. This page is at {yourSolrUrl}/solr/admin . You can run a query to show all layers in the database by setting the Query String to ’* :*’. If you haven't ingested any data into Solr, the search returns no layers. 



Solr Ingest
Once you have a working portal instance, you will likely want to ingest your own records into Solr.
There is no hard and fast way for this to be done.  Ideally, you will have reasonably good FGDC CSDGM or ISO 199115/19139 XML metadata for your layers.  Given this, using the ogpIngest project (https://github.com/OpenGeoportal/ogpIngest) is a good way to proceed. 
The code there is designed to import metadata of those types, populate an object that represents the Solr schema from an XML document, then use Solr’s SolrJ library to ingest that data into your Solr instance.  There are options to require/validate certain pieces of data and to register a layer with a GeoServer instance via GeoServer’s REST API.
 Additionally, the code has utilities for getting Solr records from other OGP instances, deleting records, etc.  See the project itself for more details.
Solr is flexible and has a nice web API, so you can write reasonably simple scripts to ingest records from alternative data sources in your language of choice.  Better yet, extend the code at ogpIngest! 

