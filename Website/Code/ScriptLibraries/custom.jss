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

	var redirectURL = "";

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
	dBar.info("+++++++++++setScopeLanguage++++++++++++++++++++")
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
//		println("browserLocale: "+browserLocale);
		dBar.info("browserLocale = "+ browserLocale)
		var languageLocal = new String("americas-en");
		//var languageLocal = new String("eme-intl");
	 
		switch (browserLocale) {
			case "en_us":  //11-04-2016
				dBar.info("United States")
				LanguageLocal = "americas-en";
				sessionScope.BrowserL = "USA";
				break;
			case "es_ca":  //11-04-2016
				dBar.info("Canada")
				sessionScope.BrowserL = "USA"
				break;
			case "es_bo":  //11-04-2016
				dBar.info("Bolivia")
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
				break;
			case "es_cl":  //11-04-2016
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_co":  //11-04-2016
				dBar.info("Columbia")
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_cr":  //11-04-2016
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_do":  //11-04-2016
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_ec":  //11-04-2016
				dBar.info("Ecuador")
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_sv":  //11-04-2016
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_gt":  //11-04-2016
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_hn":  //11-04-2016
				dBar.info("Honduras")
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			//case "es_mx":  //11-04-2016
			case "es_ni":  //11-04-2016
				dBar.info("Nicuarda")
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_pa":  //11-04-2016
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_py":  //11-04-2016
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_pe":  //11-04-2016
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_uy":  //11-04-2016
				dBar.info("Uraguay")
				sessionScope.BrowserL = "Central and South America"
					languageLocal = "americas-es"; 
					break;
			case "es_ve":  //11-04-2016
				dBar.info("Venezuela")
				sessionScope.BrowserL = "Central and South America"
				languageLocal = "americas-es"; 
				break;
				break
			case "es_mx":  //07-05-2017
				sessionScope.BrowserL = "Mexico"
				dBar.info("Mexico")
				languageLocal = "americas-es";
				break;
			case "de":
			case "de_de":  //11-04-2016
			case "de_at":  //11-04-2016
				languageLocal = "eme-de";	
				sessionScope.BrowserL = "GERMANY"
				break;
			case "en_gb":  //11-04-2016
			case "en_ie":  // English (Ireland)   //11-04-2016
			case "ga_ie":     // Irish  //11-04-2016
			case "ga":     // Irish
			case "gd":  // Scots Gaelic
				languageLocal = "eme-en";
				sessionScope.BrowserL = "UK"
				break;
			case "es":
			case "ca_es":  // Catalan  //11-04-2016
			case "es_es":   // Spain  //11-04-2016
				languageLocal = "eme-es";	
				sessionScope.BrowserL = "SPAIN"
				break;
			case "fi":
			case "fi_fi":  // Finland  //11-04-2016
				languageLocal = "eme-fi";		
				break;
			
			case "en_ca":  //9-28-2017 - DSR
				languageLocal = "americas-ca"; 
				sessionScope.BrowserL = "CANADA"
				break;
				
			case "fr":
			case "fr_ca":  
			case "fr_fr":  // France  //11-04-2016
			case "fr_be":  // Belgium  //11-04-2016
				languageLocal = "eme-fr";		
				sessionScope.BrowserL = "FRANCE"
				break;
			case "it":
			case "it_it":  //11-04-2016
				languageLocal = "eme-it";		
				sessionScope.BrowserL = "ITALY"
				break;
			case "nl":
			case "nl_nl":  //11-04-2016
			case "nl_be":  //11-04-2016
				languageLocal = "eme-nl";	
				sessionScope.BrowserL = ""
				break;
			case "sv":
			case "sv_se": // Sweden  //11-04-2016
				languageLocal = "eme-se";		
				sessionScope.BrowserL = ""
					sessionScope.BrowserL = ""
				break;
			case "en_au":
			case "ja":
			case "ja_jp":
			case "ja_jp_jp":
			case "zh":
			case "zh_hk":
			case "zh_cn":
			case "zh_sg":
			case "zh_tw":
				languageLocal = "international";
				break;
			default:
				//dBar.info("case default");
				languageLocal = "international";	
		};
		
		//dBar.info("languageLocal = " + languageLocal);
			
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
			} else if(languageLocal== "international"){
				sessionScope.languageP = "americas-en";
				sessionScope.languageURL = langURL;
			} else {
				// should never get here
				sessionScope.LanguageP = "americas-en";
				sessionScope.LanguageURL = "americas/en/" ;			
		};
		//code below added to separate Eurpoean countries for cookie consent.  The result is used
		//for loading ccEuropeanCookieConsent custum control.
		var pos = sessionScope.LanguageP.indexOf("eme")
		if(pos > -1){
			sessionScope.Region = "Europe"
		}else{
			sessionScope.Region = "Americas"
		}
		dBar.info("sessionScope.LanguageP = " + sessionScope.LanguageP);
		dBar.info("sessionScope.LanguageURL = " + sessionScope.LanguageURL);

		//println("=+=+=+=+=+=+=");
	//};
	//dBar.info("SKIPPED function,  sessionScope.LanguageP = " + sessionScope.LanguageP);
	dBar.info("sessionScope.LanguageURL = " + sessionScope.LanguageURL);
}


/* PDN July 7, 2015
 * Sets the viewScope.unid property from the URL for the current page
 */
function setScpUnid () {
	var url = facesContext.getExternalContext().getRequestPathInfo();
	dBar.info("=======setScopeUnid==========");
	dBar.info(">>>>>>>>>>>>>>> url: " + url + " <<<<<<<<<<<<<<");
	//print(">>>>>>>>>>>>>>> url: " + url + " <<<<<<<<<<<<<<");  removed 01/18/2018 mdz
	
	var url1 = @Right(url, "/");
	dBar.info("url1: " + url1);
	var country = @Left(url1, "/");
	dBar.info("region: " + country);	
	//get what is to the right of the region component
	var url2 = @Right(url1, "/");
	dBar.info("url2: " + url2);
	var lang = @Left(url2, "/");
	dBar.info("lang: " + lang);	
	//get desired product
	var url3 = @Right(url2, "/");
	dBar.info("url3: " + url3);
	var product = "";
	if (url3.indexOf("/") > 0){
		product = @Left(url3, "/");
	} else if (url3.indexOf("?") > 0){
		product = @Left(url3, "?");
	} else {
		product = url3;
	}
	dBar.info("product: " + product);
		
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
		dBar.info("*** function setOrderProcessingRoles ()  ***");
		//dBar.info("website.nsf userBean.accessRoles= "+userBean.accessRoles);
		
		var db:NotesDatabase = session.getDatabase(session.getServerName(), configBean.getValue("OrdersDbPath"));
		//print("db="+db.title);
		var roles = db.queryAccessRoles(session.getEffectiveUserName());
		dBar.info( roles );
		dBar.info("roles.length= " + roles.length );
		dBar.info("session.getUserName()= " + session.getUserName() );
		dBar.info("session.getEffectiveUserName()= " + session.getEffectiveUserName() );
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
		if (@Contains(roles,"EditForks")) sessionScope.roles.isEditForks = true;
		if (@Contains(roles,"EditProdReg")) sessionScope.roles.isEditProdReg = true;
		if (@Contains(roles,"EditWarranty")) sessionScope.roles.isEditWarranty = true;
		if (@Contains(roles,"EditDealerClaim")) sessionScope.roles.isEditDealerClaim = true;
		if (@Contains(roles,"EditWebquote")) sessionScope.roles.isEditWebquote = true;
		if (@Contains(roles,"EditInternal")) sessionScope.roles.isEditInternal = true;
		if (@Contains(roles,"EditForkquote")) sessionScope.roles.isEditForkquote = true;
		if (@Contains(roles,"EditFeedback")) sessionScope.roles.isEditFeedback = true;
		if (@Contains(roles,"EditPromotional")) sessionScope.roles.isEditPromotional = true;
		/* 03-03-2017 MDZ
		 * check the hoime database ans see if the user has
		 * the role of [Dealer].  This is used
		 *  to restrict who can view Dealer web pages *  
		 *  */
		//print("Now getting role for dealer");
		var db:NotesDatabase = session.getDatabase(session.getServerName(), configBean.getValue("HomeDbPath"));
		var roles = db.queryAccessRoles(session.getEffectiveUserName());
		var internal:boolean = @Contains(roles,"Dealer") ;
		if (internal) {
		    sessionScope.isDealerRole = true ;
		    //dBar.info("sessionScope.isEditRole = " + sessionScope.isEditRole );
		} else {
			sessionScope.isDealerRole = false;
		}
	    dBar.info("sessionScope.isEditRole = " + sessionScope.isEditRole );
		if (@Contains(roles,"Dealer")) sessionScope.roles.isDealer = true;
		//WSCR-ASES6S - Vendors / DXF Files put true/false in sessionScope if the user is vendor or no
		//make call to JDE (AS400) via JDESupport and get accessType
		//dBar.info("isVendor = [" + sessionScope.get("notVendor")+"]")
		var caswebDB:NotesDatabase = session.getDatabase(session.getServerName(), configBean.getValue("CaswebDBPath"));
		if (caswebDB != null){
			//caswebDB is open - get view
			//dBar.info("[function setOrderProcessingRoles]" + caswebDB.getTitle()+" is Open")
			var vu:NotesView = caswebDB.getView("($LDAPCN)")
			if (vu != null){
				//view is open - get document
				//dBar.info("[function setOrderProcessingRoles]" + vu.getName()+" is Open")
				var pDoc:NotesDocument = vu.getDocumentByKey(session.getEffectiveUserName())
				if(pDoc !=null){
					//person document is open - get values nedded
					//dBar.info("[function setOrderProcessingRoles]" + "Got person Doc for "+session.getEffectiveUserName());
					//we will be getting account number from this document
					var accountType = pDoc.getItemValueString("AccessType")
					//dBar.info("[function setOrderProcessingRoles]" + "acctType is "+ accountType)
					
					//DSR 1-30-19 Added support for determining if this is an EndUser
					sessionScope.isEndUser = false;
					sessionScope.isVendor = false;
					if(accountType =="V"){
						sessionScope.isVendor = true;
					} else if (accountType =="END") {
						sessionScope.isEndUser = true;
					}
				}else{
					//person doc not opened =- exit
					//dBar.info("[function setOrderProcessingRoles]" + "No person doc for " + session.getEffectiveUserName() );
				}
			}else{
				//view is not openb exit
				//dBar.info("[function setOrderProcessingRoles]" + "vu not opened")
			}
		}else{
			//caswebDB is not open - exit
			//dBar.info("[function setOrderProcessingRoles]" + "caswebDB not opened")
		}
		//WSCR-ASES6S - Vendors / DXF File end of code added
	}catch(e){
	}
	dBar.info("*** end function setOrderProcessingRoles ()  ***");
}

function getExpandLevel(){
	var first = document1.getItemValueString("Continent") + "#" + sessionScope.LanguageP + "#" + document1.getItemValueString("Department");
		//dBar.info("first=" + first);
		if (first=="Americas#americas-en#Remanufacturing"){
			//dBar.info("expandLevel=0")
			viewScope.expandLevel = "0"
		} else {
			//dBar.info("expandLevel=1")
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
		var getRequestServletPath = facesContext.getExternalContext().getRequestServletPath();
		dBar.info("getRequestServletPath: "+ getRequestServletPath);
				
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
//	var doc1 = docDb.getDocumentByUNID(sesUnid)
	var orderType =  doc1.getItemValueString("OrderType");
	viewScope.orderType = orderType;
//	Next two lines added for URGENCY MCR - mdz -04/214/2018
	var urgency = doc1.getItemValueString("Urgent");
	viewScope.urgency = urgency;
//	END OF URGENCY CODE ADDED
	var orderStatus =  doc1.getItemValueString("OrderStatus");
	viewScope.orderStatus = orderStatus;
//	switch(docOrderType){
//		case "DealerClaim":
//			viewScope.printPage = "DealerClaim_Print";
//		default:
			viewScope.printPage = "ProcessParts_Print.xsp";	
//	}
}
	function getDealerClaimData(){
			/*=========================================================
			05/30/2017 - mdz
			we need to get the document UNID and make it a scope variable.
			*/
			var queryString = facesContext.getExternalContext().getRequest().getQueryString()
			var host = context.getUrl().toString().split(facesContext.getExternalContext().getRequest().getRequestURI())[0];
			var dbPath = facesContext.getExternalContext().getRequestContextPath();
			dBar.info("host:  " + host);
			dBar.info("dbPath:  " + dbPath);
			viewScope.printPage = "DealerClaim_Print.xsp"
			viewScope.orderType = "Dealer Claim";
			sessionScope.host = host;
			sessionScope.dbPath = dbPath;
			sessionScope.unid = context.getUrlParameter("documentID");
		// TODO - based on orderType - set the proper print page
			viewScope.queryString = queryString;	
			var unid = context.getUrlParameter("documentID");
			unid = sessionScope.unid;
			var docDb:NotesDatabase = sessionAsSigner.getDatabase(session.getServerName(), configBean.getValue("OrdersDbPath"));
			var doc1 = docDb.getDocumentByUNID(unid);
			var orderType =  doc1.getItemValueString("OrderType");
			viewScope.orderType = orderType;
			var orderStatus =  doc1.getItemValueString("OrderStatus");
			viewScope.orderStatus = orderStatus;
	
}
// the following function was added 06/29/2018 by mdz - MCR WSCR-AZWMDQ - Add blog that powers the news section.
function setOtherProcessingRoles () {

		try{
			dBar.info("+++ function setOtherProcessingRoles ()  +++");
			//dBar.info("website.nsf userBean.accessRoles= "+userBean.accessRoles);
			
			var db:NotesDatabase = session.getDatabase(session.getServerName(), configBean.getValue("BlogDbPath"));
			
			var rolesOther = db.queryAccessRoles(session.getEffectiveUserName());
			dBar.info( "Blog:  " + rolesOther );
			dBar.info("Blog rolesOther.length= " + rolesOther.length );
			dBar.info("Blog session.getUserName()= " + session.getUserName() );
			dBar.info("Blog session.getEffectiveUserName()= " + session.getEffectiveUserName() );
			sessionScope.isBlogAdminRole = false ;
			var internal:boolean = @Contains(@LowerCase(rolesOther),"admin") ;
			if (internal) {
			    sessionScope.isBlogAdminRole = true ;
			    sessionScope.isEditRole = true  //this is so appropriate user can see order processing section
			    dBar.info("Blog sessionScope.isBlogAdminRole = " + sessionScope.isBlogAdminRole );
			    dBar.info("Blog sessionScope.isEditRole = " + sessionScope.isEditRole );
			} else {
			    dBar.info("Blog sessionScope.isBlogAdminRole = " + sessionScope.isBlogAdminRole );
			    dBar.info("Blog sessionScope.isEditRole = " + sessionScope.isEditRole );
			}
			
			sessionScope.rolesOther = new java.util.HashMap();
			if (@Contains(rolesOther,"BlogAdmin")) sessionScope.rolesOther.isBlogAdmin = true;
		}catch(e){
	}
		dBar.info("+++ end function setOtherProcessingRoles ()  +++");
}
//end of function added 06/29/2018
// added 09/12/2018 to captuture acl roles of other db's  -  mdz
function setRoles(){
	try{
		dBar.info("+++ function setRoles ()  +++");
		//dBar.info("website.nsf userBean.accessRoles= "+userBean.accessRoles);
		
		var db:NotesDatabase = session.getDatabase(session.getServerName(), configBean.getValue("WebsiteDbPath"));
		var roles = db.queryAccessRoles(session.getEffectiveUserName());
		var debug:boolean = @Contains(@LowerCase(rolesOther),"Debug") ;
		if (debug){
			sessionScope.showDebug = new java.util.HashMap();
			sessionScope.showDebug = true;
		}else{
			sessionScope.showDebug = false;
		}
		
	}catch(e){	

}
}
//added 09/11/2018 to capture last login for user
function setLastLogin() {
	try{
		var user = session.getEffectiveUserName();
		sessionScope.userName = user;
		if (user != "Anonymous") {
			dBar.info("Capturing last login info")
			sessionScope.userLastLoginSet = true;
			var loginTime:NotesDateTime = session.createDateTime("Today 12")
			loginTime.setNow()
			if (sessionScope.userLastLogin == null){
				sessionScope.userLastLogin = loginTime.getLocalTime()
			}
		} else {
			sessionScope.userLastLoginSet = false;
		}
	}catch(e){  	}
	
}