function populateFavorites(){
	
	try{
		if (getFavorites().length == 0){
			var newfavorites = new Array();
	        var vw:NotesView = database.getView("Documents");
			var dc:NotesDocumentCollection = vw.getAllDocumentsByKey("All~Documents");
			var doc:NotesDocument = dc.getFirstDocument();
			for (var i=0; i<2; i++){
				if (doc != null){
					newfavorites.push(doc.getUniversalID());
					doc = dc.getNextDocument();
				}
			}
			dc = vw.getAllDocumentsByKey("All~Images");
			doc = dc.getNthDocument(4);
			newfavorites.push(doc.getUniversalID());
			dc = vw.getAllDocumentsByKey("All~Presentations");
			doc = dc.getNthDocument(3);
			newfavorites.push(doc.getUniversalID());
			dc = vw.getAllDocumentsByKey("All~Spreadsheets");
			doc = dc.getNthDocument(2);
			newfavorites.push(doc.getUniversalID());
		
			setFavorites(newfavorites);
		}
	}catch(e){
	}
}


/* PDN Oct 1, 2015
 * Set fields when order docs are processed
 * 
 */
function orderSetProcessed(){
	try{

		
	}catch(e){
	}
}

/* PDN Aug 10, 2015
 * This function to auto-redirect to other Cascade sites is not used
 * See setScpLanguage where unsupported browser locales are redirected to the International page
 */
function isRedirectSite (browserLocale:String) {
	//dBar.info("In isRedirectSite() where browserLocale : "+ browserLocale);
	println("=+=+=+=+=+=");
	var redirectURL = "";
	println("browserLocale: "+browserLocale);
	switch (browserLocale) {	
		case "pt_BR":
			redirectURL = "http://www.cascadedobrasil.com.br"; 
			break;
		case "ja":
		case "ja_JP":
		case "ja_JP_JP":
			redirectURL = "http://www.cascadejapan.com/"; 
			break;
		case "zh":
		case "zh_HK":
		case "zh_CN":
		case "zh_SG":
		case "zh_TW":
			redirectURL = "http://www.cascorp.com.cn/"; 
			break;
		default:
			redirectURL = "http://www.cascorp.com/international";	
	};
	
	//dBar.info("after switch redirectURL = " + redirectURL);
	
	if (redirectURL != "") {
		//dBar.info("about to redirectURL = " + redirectURL);
		facesContext.getExternalContext().redirect(redirectURL);
	};

	println("=+=+=+=+=+=");

}

/* PDN July 7, 2015
 * Sets the sessionScope.LanguageURL and sessionScope.LanguageP properties
 * for the current user session
 * 
 * July 13, 2015 added additional Locales to browserLocale switch
 *  
 * Aug 08, 2015 added the continent - Used to filter view data
 */
function setScpLanguage () {
	//PDN 12/03/15 DO NOT test for LanguageP to skip function as breaks selected langauge
	//if (sessionScope.LanguageP == "" || sessionScope.LanguageP == null) {
	/* 11/04/2016 changes made - changes are marked with //11-04-2016 after code
	 * made browserLocale all lower case
	 * 
	*/
	//println("=+=+=+=+=+=+=");
		dBar.info("session.getServerName = " + session.getServerName());
		var languageDefault = "eme-en";
		var languageSel = new String("initialze as String object then set to null string");
		var languageSel = "";
		
		if (cookie.containsKey("languagePref")) {
			languageSel = cookie.get("languagePref").getValue();
			dBar.info("languageSel = " + languageSel);
		};
		var browserLocale = context.getLocaleString().toLowerCase(); //11-04-2016
		dBar.info("***** context.getLocaleString() : "+ browserLocale);
		//println("browserLocale: "+browserLocale);
		
		//var languageLocal = new String("americas-en");
		var languageLocal = new String("eme-intl");
	 
		switch (browserLocale) {
			case "en_us":  //11-04-2016
			case "en_ca":  //11-04-2016
			case "fr_ca":  //11-04-2016
			case "es_ca":  //11-04-2016
			case "es_bo":  //11-04-2016
			case "es_cl":  //11-04-2016
			case "es_co":  //11-04-2016
			case "es_cr":  //11-04-2016
			case "es_do":  //11-04-2016
			case "es_ec":  //11-04-2016
			case "es_sv":  //11-04-2016
			case "es_gt":  //11-04-2016
			case "es_hn":  //11-04-2016
			case "es_mx":  //11-04-2016
			case "es_ni":  //11-04-2016
			case "es_pa":  //11-04-2016
			case "es_py":  //11-04-2016
			case "es_pe":  //11-04-2016
			case "es_uy":  //11-04-2016
			case "es_ve":  //11-04-2016
				languageLocal = "americas-en"; 
				break;
			case "de":
			case "de_de":  //11-04-2016
			case "de_at":  //11-04-2016
				languageLocal = "eme-de";	
				break;
			case "en_gb":  //11-04-2016
			case "en_ie":  // English (Ireland)   //11-04-2016
			case "ga_ie":     // Irish  //11-04-2016
			case "ga":     // Irish
			case "gd":  // Scots Gaelic
				languageLocal = "eme-en";
				break;
			case "es":
			case "ca_es":  // Catalan  //11-04-2016
			case "es_es":   // Spain  //11-04-2016
				languageLocal = "eme-es";	
				break;
			case "fi":
			case "fi_fi":  // Finland  //11-04-2016
				languageLocal = "eme-fi";		
				break;
			case "fr":
			case "fr_fr":  // France  //11-04-2016
			case "fr_be":  // Belgium  //11-04-2016
				languageLocal = "eme-fr";		
				break;
			case "it":
			case "it_it":  //11-04-2016
				languageLocal = "eme-it";		
				break;
			case "nl":
			case "nl_nl":  //11-04-2016
			case "nl_be":  //11-04-2016
				languageLocal = "eme-nl";	
				break;
			case "sv":
			case "sv_se": // Sweden  //11-04-2016
				languageLocal = "eme-se";		
				break;
			default:
				//dBar.info("case default");
				languageLocal = "eme-intl";	
		};
		
		dBar.info("languageLocal = " + languageLocal);
			
		var langURL = languageLocal.replace("-","/") + "/" ;
		dBar.info("langURL = " + langURL);
	
		if (!!languageSel) {
				sessionScope.LanguageP = languageSel ;
				sessionScope.LanguageURL = languageSel.replace("-","/") + "/" ;
				sessionScope.isBrowserLocaleSupported = true ;	
			} else if (languageLocal != "eme-intl"){
				sessionScope.LanguageP = languageLocal ;
				sessionScope.LanguageURL = langURL ;
				sessionScope.isBrowserLocaleSupported = true ;	
			} else if (languageLocal == "eme-intl"){
				sessionScope.LanguageP = "eme-en";
				sessionScope.LanguageURL = "eme/en/" ;	
				sessionScope.isBrowserLocaleSupported = false ;	
				return facesContext.getExternalContext().redirect("/international");
			} else {
				// should never get here
				sessionScope.LanguageP = "americas-en";
				sessionScope.LanguageURL = "americas/en/" ;			
		};
	
		dBar.info("sessionScope.LanguageP = " + sessionScope.LanguageP);
		dBar.info("sessionScope.LanguageURL = " + sessionScope.LanguageURL);

		//println("=+=+=+=+=+=+=");
	//};
	//dBar.info("SKIPPED function,  sessionScope.LanguageP = " + sessionScope.LanguageP);
	//dBar.info("sessionScope.LanguageURL = " + sessionScope.LanguageURL);
}


/* PDN July 7, 2015
 * Sets the viewScope.unid property from the URL for the current page
 */
function setScpUnid () {
	var url = facesContext.getExternalContext().getRequestPathInfo();
	//dBar.info("=================")
	//dBar.info("url: " + url);
	var url1 = @Right(url, "/");
	//dBar.info("url1: " + url1);
	var country = @Left(url1, "/");
	//dBar.info("region: " + country);	
	//get what is to the right of the region component
	var url2 = @Right(url1, "/");
	//dBar.info("url2: " + url2);
	var lang = @Left(url2, "/");
	//dBar.info("lang: " + lang);	
	//get desired product
	var url3 = @Right(url2, "/");
	//dBar.info("url3: " + url3);
	var product = "";
	if (url3.indexOf("/") > 0){
		product = @Left(url3, "/");
	} else if (url3.indexOf("?") > 0){
		product = @Left(url3, "?");
	} else {
		product = url3;
	}
	//dBar.info("product: " + product);
		
	//get docs database and the document
	var docDb:NotesDatabase = sessionAsSigner.getDatabase(session.getServerName(), configBean.getValue("HomeDbPath"));
	var vW:NotesView = docDb.getView("Links2");
	var unid = "";
	var docCol:NotesViewEntryCollection = vW.getAllEntriesByKey(country+"-"+lang+"-" + product, true);
	if (docCol.getCount() == 1){
		//that is good, only found one
		unid = docCol.getFirstEntry().getUniversalID();
	}
	if (unid != "" ){
		viewScope.unid = unid;
	} else {
		viewScope.unid = "";
		//redirect to an error page since the document was not found
	}
	
}


/* PDN Oct 1, 2015
 * Set fields when order docs are processed
 * 
 */
function setOrderProcessingRoles () {
	try{

		//dBar.info("*** function setOrderProcessingRoles ()  ***");
		//dBar.info("website.nsf userBean.accessRoles= "+userBean.accessRoles);

		var db:NotesDatabase = session.getDatabase(session.getServerName(), configBean.getValue("OrdersDbPath"));
		var roles = db.queryAccessRoles(session.getEffectiveUserName());
		//dBar.info( roles );
		//dBar.info("roles.length= " + roles.length );
		//dBar.info("session.getUserName()= " + session.getUserName() );
		//dBar.info("session.getEffectiveUserName()= " + session.getEffectiveUserName() );
		sessionScope.isEditRole = false ;
		var internal:boolean = @Contains(@LowerCase(roles),"edit") ;
		if (internal) {
		    sessionScope.isEditRole = true ;
		    //dBar.info("sessionScope.isEditRole = " + sessionScope.isEditRole );
		}

		sessionScope.roles = new java.util.HashMap();
		if (@Contains(roles,"EditAllDocs")) sessionScope.roles.isEditAllDocs = true;
		if (@Contains(roles,"EditPreferences")) sessionScope.roles.isEditPreferences = true;
		if (@Contains(roles,"EditSalesLit")) sessionScope.roles.isEditSalesLit = true;
		if (@Contains(roles,"EditSalesVideo")) sessionScope.roles.isEditSalesVideo = true;
		if (@Contains(roles,"EditTrainVideo")) sessionScope.roles.isEditTrainVideo = true;
		if (@Contains(roles,"EditParts")) sessionScope.roles.isEditParts = true;
		if (@Contains(roles,"EditProdReg")) sessionScope.roles.isEditProdReg = true;
		if (@Contains(roles,"EditWarranty")) sessionScope.roles.isEditWarranty = true;
		if (@Contains(roles,"EditWebquote")) sessionScope.roles.isEditWebquote = true;
		if (@Contains(roles,"EditInternal")) sessionScope.roles.isEditInternal = true;
		if (@Contains(roles,"EditForkquote")) sessionScope.roles.isEditForkquote = true;
		if (@Contains(roles,"EditFeedback")) sessionScope.roles.isEditFeedback = true;
		
	}catch(e){
	}
}
function getExpandLevel(){
	var first = document1.getItemValueString("Continent") + "#" + sessionScope.LanguageP + "#" + document1.getItemValueString("Department");
		dBar.info("first=" + first);
		if (first=="Americas#americas-en#Remanufacturing"){
			dBar.info("expandLevel=0")
			viewScope.expandLevel = "0"
		} else {
			dBar.info("expandLevel=1")
			viewScope.expandLevel = "1"
		}
}

function doTestContext() {
	//following code added for testing only
	facesContext
//		var url = facesContext.getExternalContext().getRequestPathInfo();
		var url = facesContext.getExternalContext().getRequestContextPath()
		dBar.info("=================")
		dBar.info("(getRequestContextPath): " + url);
		var url = facesContext.getExternalContext().getRequestPathInfo()
		dBar.info("(getRequestPathInfo): " + url);
		var url = facesContext.getExternalContext().getRequestServletPath()
		dBar.info("(getRequestServletPath): " + url);
		var externalContext=facesContext.getExternalContext();
		var servletRequest=externalContext.getRequest();
		var queryString=servletRequest.getQueryString(); 
		dBar.info("queryString (servletRequest.getQueryString()): " + queryString);
		var getQueryString = facesContext.getExternalContext().getRequest().getQueryString();
		dBar.info("getQueryString: " + getQueryString);
		var tagname = context.getUrlParameter("documentID");
		dBar.info("tagname (getUrlParameter(documentID)): " + tagname);
		var getUrl = context.getUrl();
		dBar.info("getUrl:  " + getUrl);
		sessionScope.url = getUrl;
		var getLocale = context.getLocale();
		dBar.info("getLocale: " + getLocale);
		var host = context.getUrl().toString().split(facesContext.getExternalContext().getRequest().getRequestURI())[0];
		dBar.info("host: " + host)
		
		dBar.info("=================")
	// end of code added for testing
}
function getOrderData(){
	/*=========================================================
	05/24/2016 - mdz
	we need to get the document UNID and make it a scope variable.
	Stole some of this code from PDN's setScopeUnid function
	*/
	var queryString = facesContext.getExternalContext().getRequest().getQueryString()
	var host = context.getUrl().toString().split(facesContext.getExternalContext().getRequest().getRequestURI())[0];
	var dbPath = facesContext.getExternalContext().getRequestContextPath();
	dBar.info("host:  " + host);
	dBar.info("dbPath:  " + dbPath);
	sessionScope.host = host
	sessionScope.dbPath = dbPath
// TODO - based on orderType - set the proper print page
	viewScope.queryString = queryString;	
	var unid = context.getUrlParameter("documentID");
	var docDb:NotesDatabase = sessionAsSigner.getDatabase(session.getServerName(), configBean.getValue("OrdersDbPath"));
	var doc1 = docDb.getDocumentByUNID(unid)
	var orderType =  doc1.getItemValueString("OrderType");
	viewScope.orderType = orderType;
	var orderStatus =  doc1.getItemValueString("OrderStatus");
	viewScope.orderStatus = orderStatus;
	viewScope.printPage = "ProcessParts_Print.xsp"

/*
	if (unid != "" ){
		viewScope.unid = unid;
	} else {
		viewScope.unid = "Unable to get";
		//redirect to an error page since the document was not found
	}
*/
}