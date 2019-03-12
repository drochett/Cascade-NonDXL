package com.cascorp;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.FacesContext;

import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;


import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.DominoUtils;
public class SearchLiteratureBean  implements Serializable{
	/*this bean is used to search service literature
	It is used on the serviceliterature XPage
	
	The various input fields that allow the user to enter the search criteria are bound to the
	fields on this bean
	
	There are three types of searches (which correspond to the three search methods
	on the XPage).
	
	The processSearchx methods do these searches.
	getSearchResults returns the List of searchResults to the repeat.
	
	
	*/
	
	
	private static final long serialVersionUID = 1L;
	private static boolean showDBar = true;
	private String searchPhrase;
	//holds the search results, the results are ViewItem class
	List<ViewItem> searchResults;
	
	ConfigBean configBean;
	//Variables to bind to on XPage
	String searchLanguage;
	String sortOrder;
	
	//for keyword Search
	String searchInputKeyword;
	String selKeywordFilter;
	//for part manual
	String productFamilyParts;
	String searchInputParts;
	
	//for other documentation
	String productFamilyOther;
	String docTypeOther;
	
	Integer returnedResultsDownloads = new Integer(0);
	Integer maxResults;
	String totalResultsDisplay;
	String maxResultsDisplay;
	
	private Integer maxResultsPerPage;
	
	public String getSelKeywordFilter() {
		return selKeywordFilter;
	}
	public void setSelKeywordFilter(String selKeywordFilter) {
		this.selKeywordFilter = selKeywordFilter;
	}
	public String getSearchInputKeyword() {
		return searchInputKeyword;
	}
	public void setSearchInputKeyword(String searchInputKeyword) {
		this.searchInputKeyword = searchInputKeyword;
	}
	public String getProductFamilyParts() {
		//debugMsg("getProductFamilyParts: " + productFamilyParts);
		return productFamilyParts;
	}
	public void setProductFamilyParts(String productFamilyParts) {
		//debugMsg("setProductFamilyParts: " + productFamilyParts);
		this.productFamilyParts = productFamilyParts;
	}
	public String getSearchInputParts() {
		return searchInputParts;
	}
	public void setSearchInputParts(String searchInputParts) {
		this.searchInputParts = searchInputParts;
	}
	public String getProductFamilyOther() {
		return productFamilyOther;
	}
	public void setProductFamilyOther(String productFamilyOther) {
		this.productFamilyOther = productFamilyOther;
	}
	public String getDocTypeOther() {
		return docTypeOther;
	}
	public void setDocTypeOther(String docTypeOther) {
		this.docTypeOther = docTypeOther;
	}
	public String getSearchLanguage() {
		return searchLanguage;
	}
	public void setSearchLanguage(String searchLanguage) {
		this.searchLanguage = searchLanguage;
	}
	
	public String getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	
	
	
	
	public Integer getReturnedResultsDownloads() {
		//this is the total of how many results were found
		return returnedResultsDownloads;
	}
	public void setReturnedResultsDownloads(Integer returnedResultsDownloads) {
		this.returnedResultsDownloads = returnedResultsDownloads;
	}
	
	
	
	public String getTotalResultsDisplay() {
		//gets how many were found and outputs as a String
		Integer foundResults =  this.returnedResultsDownloads;
		String numberAsString = String.format ("%d", foundResults);
		return numberAsString;
	}
	
	public Integer getTotalResults(){
		return this.returnedResultsDownloads;
	}
	
	
	public String getMaxResultsDisplay() {
		//add together the max from downloads and home
		Integer totalMaxResults = this.maxResults;
		String numberAsString = String.format ("%d", totalMaxResults);
		return numberAsString;
		
	}
	public Integer getMaxResults(){
		return this.maxResults;
	}
	
	
	public List<ViewItem> getSearchResults() {
		debugMsg("starting getSearchResults and count is " + searchResults.size());
		return searchResults;
	}
	public class ViewItem implements Serializable {
		/*this is what gets returned in the List of searchResults
		The controls in the repeat use these methods
		note that the setters are never used!
		*/
		
		private static final long serialVersionUID = 1L;
		String url;
		Integer fTSearchScore;
		String subject;
		Date lastModified;
		String iconURL;
		String language;
		boolean isHomeDoc;
		String mediaType;
		String productFamily;
		
		
		public String getProductFamily() {
			return productFamily;
		}
		public void setProductFamily(String productFamily) {
			this.productFamily = productFamily;
		}
		public String getMediaType() {
			return mediaType;
		}
		public void setMediaType(String mediaType) {
			this.mediaType = mediaType;
		}
		public boolean isHomeDoc() {
			return isHomeDoc;
		}
		public void setHomeDoc(boolean isHomeDoc) {
			this.isHomeDoc = isHomeDoc;
		}
		public String getLanguage() {
			return language;
		}
		public void setLanguage(String language) {
			this.language = language;
		}
		public Date getLastModified() {
			return lastModified;
		}
		public void setLastModified(Date lastModified) {
			this.lastModified = lastModified;
		}
		public String getIconURL() {
			return iconURL;
		}
		public void setIconURL(String iconURL) {
			this.iconURL = iconURL;
		}
		public Integer getFTSearchScore() {
			return fTSearchScore;
		}
		public void setFTSearchScore(Integer searchScore) {
			fTSearchScore = searchScore;
		}

	
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		
		public String getSubject() {
			return subject;
		}
		public void setSubject(String subject) {
			this.subject = subject;
		}

		public ViewItem(Integer FTSearchScore, String subject, String url, String iconURL,String language,  Date lastMod, boolean isHomeDoc, String mediaType, String productFamily) {
			//quick one line constructor to build a new ViewItem
			this.fTSearchScore = FTSearchScore;
			this.subject = subject;
			this.url = url;
			this.iconURL = iconURL;
			this.lastModified = lastMod;
			this.language = language;
			this.isHomeDoc = isHomeDoc;
			this.mediaType = mediaType;
			this.productFamily = productFamily;
		}
		
	
	}
	
	public static  Comparator<ViewItem> COMPARE_BY_SCORE2	= new Comparator<ViewItem>() {
		//this sorts by FT Score first and then by subject if the FTScores are the same!
        public int compare(ViewItem one, ViewItem other) {
        	if (other.fTSearchScore != one.fTSearchScore){
        		return other.fTSearchScore.compareTo(one.fTSearchScore);
        	} else {
        		//if they are the same compare by subject
        		return one.subject.compareTo(other.subject);
        	}
            
        }
};
public static  Comparator<ViewItem> COMPARE_BY_DATE	= new Comparator<ViewItem>() {
	//this sorts based on the date last modified, if the same, compare by subject
    public int compare(ViewItem one, ViewItem other) {
    	if (other.lastModified != one.lastModified){
    		return other.lastModified.compareTo(one.lastModified);
    	} else {
    		//if they are the same compare by subject
    		return one.subject.compareTo(other.subject);
    	}
        
    }
};
	
	public static  Comparator<ViewItem> COMPARE_BY_SCORE	= new Comparator<ViewItem>() {
		//this is not used
        public int compare(ViewItem one, ViewItem other) {
            return other.fTSearchScore.compareTo(one.fTSearchScore);
        }
};
public static  Comparator<ViewItem> COMPARE_BY_NAME	= new Comparator<ViewItem>() {
	//this sorts the results by the subject
    public int compare(ViewItem one, ViewItem other) {
        return one.subject.compareTo(other.subject);
    }
};
	
	public void sortSearchResultsRelevance(){
		//sort the results by the FT Score
		Collections.sort(searchResults, COMPARE_BY_SCORE2);
	}
	

	public void sortResultsByName(){
		//sort the search results by name
		Collections.sort(searchResults, COMPARE_BY_NAME);
	}
	public void sortResultsByDate(){
		//sort the search results by date
		Collections.sort(searchResults, COMPARE_BY_DATE);
	}
	public void reorderResults(){
		//called when the user changes the sort order to return the new search results sorted
		if  (this.sortOrder.equalsIgnoreCase("Subject")) {
			sortResultsByName();
		} else if (this.sortOrder.equalsIgnoreCase("Date")) {
			sortResultsByDate();
			
		} else {
			//sort the results by relevance
			sortSearchResultsRelevance();
		}
		
	}
	
	
	public void processSearch1(){
		//this builds the FT Search string to search in the Downloads
		//It is used on the first tab, Search Parts Manuals
		debugMsg("product Family is " + this.productFamilyParts);
		debugMsg("input is " + this.searchInputParts);
		//reset other searches
		this.searchInputKeyword = new String("");
		this.docTypeOther = new String("");
		this.productFamilyOther = new String("");
		//build language part
		String langSearch = new String("");
		if (StringUtil.isNotEmpty(this.searchLanguage)){
			//add the language to the search
			langSearch = new String(" AND ").concat("FIELD Language=").concat(this.searchLanguage);
		}
		//build Product Family Search
		String prodFamilySearch = new String("");
		if (StringUtil.isNotEmpty(this.productFamilyParts)){
			prodFamilySearch = new String("FIELD ProductFamily =").concat(this.productFamilyParts);
			debugMsg("prodFamilySearch is " + prodFamilySearch);
		}
		//add search for model
		// AND (FIELD ItemNumber = "28g" OR FIELD Description = "28g")
		String modelSearch = new String("");
		if (StringUtil.isNotEmpty(this.searchInputParts)){
			//build search for model
			modelSearch = new String("(FIELD ItemNumber = \"");
			modelSearch = modelSearch.concat(this.searchInputParts).concat("\" OR FIELD Description = \"").concat(this.searchInputParts).concat("\")");
			debugMsg("modelSearch is " + modelSearch);
		} else {
			debugMsg("searchInputParts is Empty");
		}
		
		//only the parts manuals and suggested parts - add to all searches
		String onlyPartsManuals = new String(" AND (FIELD MediaType=\"Parts Manual\" OR FIELD MediaType =\"Recommended Spare Parts\")");
		// AND FIELD ProductFamily = "Forks" AND (FIELD MediaType = "Parts Manual" OR FIELD MediaType = "Recommended Spare Parts")
		
		//build searchphrase
		
		if (StringUtil.isNotEmpty(prodFamilySearch) && StringUtil.isNotEmpty(modelSearch)){
			//combine all
		this.searchPhrase = prodFamilySearch.concat(" AND ").concat(modelSearch).concat(langSearch).concat(onlyPartsManuals);
		} else if (StringUtil.isNotEmpty(prodFamilySearch)){
			//only product Family
			this.searchPhrase = prodFamilySearch.concat(langSearch).concat(onlyPartsManuals);
		} else if (StringUtil.isNotEmpty(modelSearch)){
			//only model search
			this.searchPhrase = modelSearch.concat(langSearch).concat(onlyPartsManuals);
		} else {
			//nothing specified show all using language and only Parts manuals
			this.searchPhrase = new String("(FIELD MediaType=\"Parts Manual\" OR FIELD MediaType =\"Recommended Spare Parts\")").concat(langSearch);
			
		}
		
		//start the search
		debugMsg("Search phrase is " + this.searchPhrase);
		doSearch();
		
	}
	
	public void processSearch2(){
		/* This is used to do the search for the Search Other Manuals and Guides tab
		 * This sets up the FT Search query
		 * 
		 */
		debugMsg("product Family is " + this.productFamilyOther);
		debugMsg("doctype is " + this.docTypeOther);
		//reset other searches
		this.searchInputKeyword = new String("");
		this.productFamilyParts = new String("");
		this.searchInputParts = new String("");
		
		
		//build language part if the user chose a language
		String langSearch = new String("");
		if (StringUtil.isNotEmpty(this.searchLanguage)){
			//add the language to the search
			langSearch = new String(" AND ").concat("FIELD Language=").concat(this.searchLanguage);
		}
		//build Product Family Search
		String prodFamilySearch = new String("");
		if (StringUtil.isNotEmpty(this.productFamilyOther)){
			prodFamilySearch = new String("FIELD ProductFamily =").concat(this.productFamilyOther);
			debugMsg("prodFamilySearch is " + prodFamilySearch);
		}
		//create doc types search string
		String docTypeSearch = new String("");
		if (StringUtil.isNotEmpty(this.docTypeOther)){
			/* OLD CODE
			 docTypeSearch = new String("FIELD MediaType=")
			 .concat(this.docTypeOther); }
			 */
			//replaced rem'd out code above with this code - mdz 08-25-16
			// docTypeOther: User Manual (Op, Inst, Maint, Parts) cases search error - romoved the parens part
			boolean userManual;
			userManual = false;
			debugMsg("docTypeOther=" + docTypeOther);
			if (docTypeOther.contains("User Manual")) {
				userManual = true;
			}
			debugMsg("userManual: " +userManual);
			if (userManual == false) {
				//debugMsg("entering if");
				docTypeSearch = new String("FIELD MediaType=")
				.concat(this.docTypeOther);
			} else {
				//debugMsg("entering else");
				docTypeSearch = new String("FIELD MediaType=User Manual");
				//debugMsg("docTypeSearch has changed");
			}
		}
		//debugMsg("docTypeSearch: " + docTypeSearch);
		// END OF CODE REPLACED 08/25/16 BY MDZ
		//add this to all queries
		String eliminatePartsManual = new String(" AND NOT FIELD MediaType = Parts Manual");
		
		//Build the final string
		if (StringUtil.isNotEmpty(prodFamilySearch) && StringUtil.isNotEmpty(docTypeSearch)){
			//combine both
			this.searchPhrase = prodFamilySearch.concat(" AND ").concat(docTypeSearch).concat(langSearch).concat(eliminatePartsManual);
		} else if (StringUtil.isNotEmpty(prodFamilySearch)){
			this.searchPhrase = prodFamilySearch.concat(langSearch).concat(eliminatePartsManual);
		} else if (StringUtil.isNotEmpty(docTypeSearch)){
				this.searchPhrase = docTypeSearch.concat(langSearch).concat(eliminatePartsManual);
		} else {
				//in this case just search on the language and no parts manuals
			this.searchPhrase = new String("NOT FIELD MediaType = Parts Manual").concat(langSearch);		
		}
		//start the search
		debugMsg("Search phrase is " + this.searchPhrase);
		doSearch();
		
		
	}
	
	
	public void processSearch3(){
		/*
		 * This is used for the Search using a keyword tab
		 * Build the FT Search string and then call doSearch...
		 */
		debugMsg("input is " + this.searchInputKeyword);
		debugMsg("language is " + this.searchLanguage);
		//reset other searches
		this.productFamilyParts = new String("");
		this.searchInputParts = new String("");
		this.docTypeOther = new String("");
		this.productFamilyOther = new String("");
		
		//setup filter for document type
		//create doc types search string
		String docTypeSearch = new String("");
		if (StringUtil.isNotEmpty(this.selKeywordFilter)){
			docTypeSearch = new String(" AND FIELD MediaType=").concat(this.selKeywordFilter);
		}
		
		
		//if language selected by user...
		debugMsg("searchLanguage is " + this.searchLanguage);
		if (StringUtil.isNotEmpty(this.searchLanguage)){
			//add the language to the search
			String langSearch = new String("FIELD Language=");
			this.searchPhrase = langSearch.concat(this.searchLanguage).concat(" AND " ).concat(this.searchInputKeyword).concat(docTypeSearch);
			
		} else {
			this.searchPhrase = this.searchInputKeyword.concat(docTypeSearch);
		}
		
		//start the search
		doSearch();
	}
	
	
	public void processSearch4(String lang){
		/*
		 * This is used for the Search using a keyword tab
		 * Build the FT Search string and then call doSearch...
		 */
		debugMsg("input is " + this.searchInputKeyword);
		debugMsg("language is " + lang);
		//reset other searches
		this.productFamilyParts = new String("");
		this.searchInputParts = new String("");
		this.docTypeOther = new String("");
		this.productFamilyOther = new String("");
		
		//setup filter for document type
		//create doc types search string
		String docTypeSearch = new String("");
		if (StringUtil.isNotEmpty(this.selKeywordFilter)){
			docTypeSearch = new String(" AND FIELD MediaType=").concat(this.selKeywordFilter);
		}
		
		
		//if language selected by user...
		if (StringUtil.isNotEmpty(lang)){
			//add the language to the search
			String langSearch = new String("FIELD Language=");
			this.searchPhrase = langSearch.concat(lang).concat(" AND " ).concat(this.searchInputKeyword).concat(docTypeSearch);
		} else {
			this.searchPhrase = this.searchInputKeyword.concat(docTypeSearch);
		}
		
		//start the search
		doSearch();
	}
	
	
	@SuppressWarnings("unchecked")
	private void doSearch() {
		//this does the actual searching, the searchPhrase is the FT Search to use
		//it searches in the Downloads database only
		String searchPhrase = this.searchPhrase;
		debugMsg("Search string is " + searchPhrase);
		String subject = new String("");
		String url = new String("");
		String iconUrl = new String("");
		String language = new String("");
		Integer score = new Integer(0);
		String mediaType = new String("");
		String productFamily = new String("");
		boolean isHomeDoc;
		int resultNum;
		Date modDate = null;
		DateTime NotesDate;
		// get the locations of the database to search in
		
		String downloadsDbPath = getConfigValueString("DownloadsDbPath");
		String downloadsURL = getConfigValueString("DownloadsURL");
		String HostURL = getConfigValueString("HostURL");
		debugMsg("config values are : " + ", "
				+ downloadsDbPath + ", " + downloadsURL + ", " + HostURL);

		// init results array
		this.searchResults = new ArrayList<ViewItem>();
		if (!searchPhrase.isEmpty()) {

			// variables for looping through views
			ViewEntry viewEntry;
			ViewEntry tempviewEntry;

			try {

				Session session = DominoUtils.getCurrentSession();
				
				// start search in downloads database
				Database downloadsDb = session.getDatabase(session
						.getServerName(), downloadsDbPath);
				if (downloadsDb.isOpen()) {
					// search in the home db
					View lookupVw = downloadsDb.getView("ServiceLiterature2");
					if (lookupVw != null) {
						if (downloadsDb.isFTIndexed()) {
							// do the search!!!

							resultNum = lookupVw.FTSearch(searchPhrase,
									maxResults);
							debugMsg("results are " + resultNum);
							this.returnedResultsDownloads = resultNum;
							ViewEntryCollection vwEntryCol = lookupVw
									.getAllEntries();
							viewEntry = vwEntryCol.getFirstEntry();
							if (resultNum > 0) {
								while (viewEntry != null) {
									// create a new ViewItem and add to
									// ArrayList
									Vector colValues = viewEntry
											.getColumnValues();
									
									subject = (String) colValues.elementAt(0);
									
									url = downloadsURL.concat((String) colValues
											.elementAt(8));
									url = HostURL.concat(url);
									
									productFamily = (String) colValues.elementAt(2);
							
									language = (String) colValues.elementAt(7);
									debugMsg("language is " + language);
									debugMsg("elementAt(6) " + colValues.elementAt(6));
									debugMsg("elementAt(0) " + colValues.elementAt(0));
									NotesDate = (DateTime) colValues
											.elementAt(6);
									modDate = NotesDate.toJavaDate();
									iconUrl = (String) colValues.elementAt(1);
									mediaType = (String) colValues.elementAt(3);
									score = viewEntry.getFTSearchScore();
									isHomeDoc = false;
									ViewItem vwItem = new ViewItem(score,
											subject, url, iconUrl, language,
											modDate, isHomeDoc, mediaType, productFamily);
									searchResults.add(vwItem);
									// get next document
									// recycle the vector per Paul Withers blog
									// since there was a DateTime column
									session.recycle(colValues);
									tempviewEntry = vwEntryCol
											.getNextEntry(viewEntry);
									viewEntry.recycle();
									viewEntry = tempviewEntry;
								}
							}

						} else {
							// database is not FT Indexed
							// view is not available
							debugMsg("Download Database is not FT Indexed");
							throw new RuntimeException(
									"Download Database is not FT Indexed");
						}
					} else {
						// view is not available
						debugMsg("ServicesLiteratureView2 in Download Database is not open");
						throw new RuntimeException(
								"ServicesLiteratureView2 in Download Database is not open");
					}

				} else {
					// downloads db is not open
					debugMsg("Downloads Database is not open: "
							+ downloadsDbPath);
					throw new RuntimeException(
							"Downloads Database is not open: "
									+ downloadsDbPath);
				}
			//Determine the search results sort order
			//one of three choices the user can select
			if  (this.sortOrder.equalsIgnoreCase("Subject")) {
				sortResultsByName();
			} else if (this.sortOrder.equalsIgnoreCase("Date")) {
				sortResultsByDate();
				
			} else {
				//sort the results by relevance as the default
				sortSearchResultsRelevance();
			}
				
				
			} catch (Exception e) {
				debugMsg("error : " + e.toString());
				DebugToolbarBean.get().error(e);
			}
		} else {
			// search phrase was empty
			debugMsg("Search Phrase was empty");
		}

	}
	
	public String getSearchPhrase() {
		return searchPhrase;
	}
	public void setSearchPhrase(String searchPhrase) {
		this.searchPhrase = searchPhrase;
	}
	private String getConfigValueString(String configName){
		//this gets a config value when it should be a string
		if (configBean == null){
			//initialize configBean variable
			configBean = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "configBean");
		}
		String rtnValue = (String) configBean.getValue(configName);
		return rtnValue;
	}
	
	private Integer getConfigValueInteger(String configName){
		//this gets a config value when it is an Integer, converts the config value to
		//an Integer since they are always strings
		Integer rtnValue ;
		if (configBean == null){
			//initialize configBean variable
			configBean = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "configBean");
		}
		String rtnValueString = (String) configBean.getValue(configName); 
		//convert days to integer
		try{
			rtnValue = Integer.parseInt(rtnValueString);
		} catch (NumberFormatException nfe) {
			//default to 50 if a bad date
			debugMsg("Bad Number, setting to 50");
			rtnValue =  50;
		}
		return rtnValue;
	}
	
	//constructor
	public SearchLiteratureBean() {
		//initialize the search results
		searchResults = new ArrayList<ViewItem>();
		sortOrder = new String("Relevance");
	//set the max results value for each database
		this.maxResults = getConfigValueInteger("MaxSiteSearchResults");	
		this.setMaxResultsPerPage(getConfigValueInteger("MaxSiteSearchResultsPerPage"));
		debugMsg("max search is " + this.maxResults + " max per page is " + this.maxResultsPerPage);
	}
	

	@SuppressWarnings("unchecked")
	private static void debugMsg(String msg){
		
		Map viewscope = (Map) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "viewScope");
		if (viewscope.containsKey("debug")){
			if ( ((String) viewscope.get("debug")).equalsIgnoreCase("true")  ){
				DebugToolbarBean.get().info(msg);
			} 
		} else if (showDBar){
			DebugToolbarBean.get().info(msg);
		}
	}
	public void setMaxResultsPerPage(Integer maxResultsPerPage) {
		this.maxResultsPerPage = maxResultsPerPage;
	}
	public Integer getMaxResultsPerPage() {
		return maxResultsPerPage;
	}
	public boolean hasResults(){
		//called to determine if there are results to show
		debugMsg("Starting hasResults - " + !searchResults.isEmpty());
		return !searchResults.isEmpty();
	}
	
	
}
