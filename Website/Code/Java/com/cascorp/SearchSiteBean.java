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


import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.DominoUtils;
public class SearchSiteBean implements Serializable {
	//this code searches the "site" for requested documents
	//the SiteSearch page will set the requested search term (setSearchPhrase)
	//then the page calls doSearch, which does the searching
	//the results are put in the searchResults and returned in getSearchResults
	
	
	private static final long serialVersionUID = 1L;
	private static boolean showDBar = true;
	String searchPhrase;
	List<ViewItem> searchResults;
	
	ConfigBean configBean;
	
	Integer returnedResultsHome = new Integer(0);
	Integer returnedResultsDownloads = new Integer(0);
	// DSR 1-30-19 Added blog to search
	Integer returnedResultsBlog = new Integer(0);
	Integer maxResults;
	String totalResultsDisplay;
	String maxResultsDisplay;
	
	private Integer maxResultsPerPage;
	
	
	public Integer getReturnedResultsHome() {
		return returnedResultsHome;
	}
	public void setReturnedResultsHome(Integer returnedResultsHome) {
		this.returnedResultsHome = returnedResultsHome;
	}
	public Integer getReturnedResultsDownloads() {
		return returnedResultsDownloads;
	}
	public void setReturnedResultsDownloads(Integer returnedResultsDownloads) {
		this.returnedResultsDownloads = returnedResultsDownloads;
	}
	
	//DSR 1-30-19 Added blog to search
	public Integer getReturnedResultsBlog() {
		return returnedResultsBlog;
	}
	public void setReturnedResultsBlog(Integer returnedResultsBlog) {
		this.returnedResultsBlog = returnedResultsBlog;
	}

	public String getSearchPhrase() {
		debugMsg("getting searchphrase " + searchPhrase);
		return searchPhrase;
	}
	public void setSearchPhrase(String searchPhrase) {
		//the SiteSearch page will set the requested search term
		debugMsg("setting searchphrase " + searchPhrase);
		this.searchPhrase = searchPhrase;
	}
	
	
	public String getTotalResultsDisplay() {
		//adds the two result values together and outputs as a String
		Integer foundResults = this.returnedResultsHome + this.returnedResultsDownloads + this.returnedResultsBlog;
		String numberAsString = String.format ("%d", foundResults);
		return numberAsString;
	}
	
	public String getMaxResultsDisplay() {
		//add together the max from downloads and home
		Integer totalMaxResults = this.maxResults * 2;
		String numberAsString = String.format ("%d", totalMaxResults);
		return numberAsString;
		
	}
	
	
	public List<ViewItem> getSearchResults() {
		debugMsg("starting getSearchResults and count is " + searchResults.size());
		return searchResults;
	}
	public class ViewItem implements Serializable {
		//each found search item is put in a ViewItem class
		//the repeat repeats over a list of ViewItems
		private static final long serialVersionUID = 1L;
		String url;
		Integer fTSearchScore;
		String subject;
		Date lastModified;
		String iconURL;
		String language;
		boolean isHomeDoc;
		String mediaType;
		
		
		
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

		public ViewItem(Integer FTSearchScore, String subject, String url, String iconURL,String language,  Date lastMod, boolean isHomeDoc, String mediaType) {
			this.fTSearchScore = FTSearchScore;
			this.subject = subject;
			this.url = url;
			this.iconURL = iconURL;
			this.lastModified = lastMod;
			this.language = language;
			this.isHomeDoc = isHomeDoc;
			this.mediaType = mediaType;
		}
		
	
	}
	
	public static  Comparator<ViewItem> COMPARE_BY_SCORE2	= new Comparator<ViewItem>() {
		//not currently used, can be used to compare by FTScore and then by subject if the same
        public int compare(ViewItem one, ViewItem other) {
        	if (other.fTSearchScore != one.fTSearchScore){
        		return other.fTSearchScore.compareTo(one.fTSearchScore);
        	} else {
        		//if they are the same compare by subject
        		return other.subject.compareTo(one.subject);
        	}
            
        }
};
	
	
	public static  Comparator<ViewItem> COMPARE_BY_SCORE	= new Comparator<ViewItem>() {
        public int compare(ViewItem one, ViewItem other) {
            return other.fTSearchScore.compareTo(one.fTSearchScore);
        }
};
public static  Comparator<ViewItem> COMPARE_BY_NAME	= new Comparator<ViewItem>() {
	//not currently used, but could be used to sort by subject
    public int compare(ViewItem one, ViewItem other) {
        return one.subject.compareTo(other.subject);
    }
};
	
	public void sortSearchResults(){
		//this is the one that is current used, sorted by FTScore
		Collections.sort(searchResults, COMPARE_BY_SCORE);
	}
	

	public void sortSearchResults2(){
		//not currently used
		Collections.sort(searchResults, COMPARE_BY_SCORE2);
	}
	public void sortResultsByName(){
		//not currently used
		Collections.sort(searchResults, COMPARE_BY_NAME);
	}
	
	@SuppressWarnings("unchecked")
	public void doSearch() {
		//does all the work!
		String searchPhrase = this.searchPhrase;

		String subject = new String("");
		String url = new String("");
		String iconUrl = new String("");
		String language = new String("");
		Integer score = new Integer(0);
		String mediaType = new String("");
		boolean isHomeDoc;
		int resultNum;
		Date modDate = null;
		DateTime NotesDate;
		// get the locations of the two databases to search in
		String homeDbPath = getConfigValueString("HomeDbPath");
		String downloadsDbPath = getConfigValueString("DownloadsDbPath");
		String homeURL = getConfigValueString("HomeURL");
		String downloadsURL = getConfigValueString("DownloadsURL");
		String HostURL = getConfigValueString("HostURL");
		
		// DSR - 1-30-19 Added Blog to Search
		String blogDbPath = getConfigValueString("BlogDbPath");
		String blogURL = getConfigValueString("BlogURL");
		
		debugMsg("config values are : " + homeDbPath + ", " + homeURL + ", "
				+ downloadsDbPath + ", " + downloadsURL + ", "
				+ blogDbPath + ", " + blogURL + ", " + HostURL);

		// init results array
		this.searchResults = new ArrayList<ViewItem>();
		if (!searchPhrase.isEmpty()) {

			// variables for looping through views
			ViewEntry viewEntry;
			ViewEntry tempviewEntry;

			try {

				Session session = DominoUtils.getCurrentSession();
				Database homeDb = session.getDatabase(session.getServerName(),
						homeDbPath);
				if (homeDb.isOpen()) {
					// search in the home db
					View lookupVw = homeDb.getView("SiteSearchView");
					if (lookupVw != null) {
						if (homeDb.isFTIndexed()) {
							// only search if ft indexed
							resultNum = lookupVw.FTSearch(searchPhrase,
									maxResults);
							debugMsg("results are " + resultNum);
							this.returnedResultsHome = resultNum;
							ViewEntryCollection vwEntryCol = lookupVw
									.getAllEntries();
							viewEntry = vwEntryCol.getFirstEntry();
							if (resultNum > 0) {
								while (viewEntry != null) {
									// create a new ViewItem and add to
									// ArrayList
									Vector colValues = viewEntry
											.getColumnValues();
									url = HostURL.concat((String) colValues
											.elementAt(0));

									language = (String) colValues.elementAt(1);
									subject = (String) colValues.elementAt(2);
									NotesDate = (DateTime) colValues
											.elementAt(3);
									modDate = NotesDate.toJavaDate();
									score = viewEntry.getFTSearchScore();
									isHomeDoc = true;
									ViewItem vwItem = new ViewItem(score,
											subject, url, iconUrl, language,
											modDate, isHomeDoc, mediaType);
									searchResults.add(vwItem);
									// get next document
									// recycle the vector per Paul Withers blog
									// since there
									// was
									// a DateTime column
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
							debugMsg("Home Database is not FT Indexed");
							throw new RuntimeException(
									"Home Database is not FT Indexed");
						}
					} else {
						// view is not available
						debugMsg("SiteSearchView in Home Database is not open");
						throw new RuntimeException(
								"SiteSearchView in Home Database is not open");
					}
				} else {
					// home database is not open
					debugMsg("Home Database is not open: " + homeDbPath);
					throw new RuntimeException("Home Database is not open: "
							+ homeDbPath);
				}
				// start search in downloads database
				Database downloadsDb = session.getDatabase(session
						.getServerName(), downloadsDbPath);
				if (downloadsDb.isOpen()) {
					// search in the home db
					View lookupVw = downloadsDb.getView("SiteSearchView");
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
									url = downloadsURL
											.concat((String) colValues
													.elementAt(0));
									url = HostURL.concat(url);
									language = (String) colValues.elementAt(1);
									subject = (String) colValues.elementAt(2);
									NotesDate = (DateTime) colValues
											.elementAt(3);
									modDate = NotesDate.toJavaDate();
									iconUrl = "/".concat((String) colValues
											.elementAt(4));
									
									mediaType = (String) colValues.elementAt(5);
									score = viewEntry.getFTSearchScore();
									isHomeDoc = false;
									ViewItem vwItem = new ViewItem(score,
											subject, url, iconUrl, language,
											modDate, isHomeDoc, mediaType);
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
						debugMsg("SiteSearchView in Download Database is not open");
						throw new RuntimeException(
								"SiteSearchView in Download Database is not open");
					}

				} else {
					// downloads db is not open
					debugMsg("Downloads Database is not open: "
							+ downloadsDbPath);
					throw new RuntimeException(
							"Downloads Database is not open: "
									+ downloadsDbPath);
				}

				//DSR 1-30-19 Added Blog db to search
				Database blogDb = session.getDatabase(session
						.getServerName(), blogDbPath);
				if (blogDb.isOpen()) {
					// search in the home db
					View lookupVw = blogDb.getView("SiteSearchView");
					if (lookupVw != null) {
						if (blogDb.isFTIndexed()) {
							// do the search!!!

							resultNum = lookupVw.FTSearch(searchPhrase,
									maxResults);
							debugMsg("blog results are " + resultNum);
							this.returnedResultsBlog = resultNum;
							ViewEntryCollection vwEntryCol = lookupVw
									.getAllEntries();
							viewEntry = vwEntryCol.getFirstEntry();
							
							if (resultNum > 0) {
								while (viewEntry != null) {
									// create a new ViewItem and add to
									// ArrayList
									Vector colValues = viewEntry
											.getColumnValues();
									url = HostURL.concat((String) colValues
											.elementAt(0));

									language = (String) colValues.elementAt(1);
									subject = (String) colValues.elementAt(2);
									NotesDate = (DateTime) colValues
											.elementAt(3);
									modDate = NotesDate.toJavaDate();
									score = viewEntry.getFTSearchScore();
									isHomeDoc = true;
									ViewItem vwItem = new ViewItem(score,
											subject, url, iconUrl, language,
											modDate, isHomeDoc, mediaType);
									searchResults.add(vwItem);
									// get next document
									// recycle the vector per Paul Withers blog
									// since there
									// was
									// a DateTime column
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
							debugMsg("Blog Database is not FT Indexed");
							throw new RuntimeException(
									"Blog Database is not FT Indexed");
						}
					} else {
						// view is not available
						debugMsg("SiteSearchView in Blog Database is not open");
						throw new RuntimeException(
								"SiteSearchView in Blog Database is not open");
					}

				} else {
					// blog db is not open
					debugMsg("Blog Database is not open: "
							+ blogDbPath);
					throw new RuntimeException(
							"blog Database is not open: "
									+ blogDbPath);
				}

				
				
				// sort the results by relevance
				sortSearchResults();
				

			} catch (Exception e) {
				debugMsg("error : " + e.toString());
				DebugToolbarBean.get().error(e);
			}
		} else {
			// search phrase was empty
			debugMsg("Search Phrase was empty");
		}

	}
	
	private String getConfigValueString(String configName){
		if (configBean == null){
			//initialize configBean variable
			configBean = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "configBean");
		}
		String rtnValue = (String) configBean.getValue(configName);
		return rtnValue;
	}
	
	private Integer getConfigValueInteger(String configName){
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
	public SearchSiteBean() {
		//initialize the search results
		//this gets the max results per the config doc
		searchResults = new ArrayList<ViewItem>();
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
