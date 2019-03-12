package com.cascorp;

import java.io.Serializable; 
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;

import lotus.domino.Database;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.DominoUtils;
public class MenuBean implements Serializable {
	//this bean is used to build the menus the user sees based on the user's language
	//turn on to show DBar messages or set viewScope="true" on XPage
	private boolean showDBar = false;
	private static final long serialVersionUID = 1L;
	HashMap<String, Integer> langSupport = new HashMap<String, Integer>();
	@SuppressWarnings("unchecked")
	private String getLangType() {
		//figure out the language the user is using
		Map sessionscope = (Map) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "sessionScope");
		if (((String) sessionscope.get("LanguageP") == "") || ((String) sessionscope.get("LanguageP") == null)){
			// assume English
			return "eme-en";
		} else {
			return (String) sessionscope.get("LanguageP");
		}
	}
	
	//this is the parent for all the menus, the key is the language and the value is a collection of menus
	private HashMap<String, Collection<MainMenus>> menuMap = new HashMap<String, Collection<MainMenus>>();
	
	
	public Collection<SubMenus> getMenuChildren(String key){
		//gets all the submenus under a main menu item
		//returns only the submenus for the user's language
		String lang = getLangType();
		String fnct = "getMenuChildren - ";
		Collection<SubMenus> rtnSubMenu = new ArrayList<SubMenus>();
		Collection<MainMenus> mainMenuChoices = menuMap.get(lang);
		Iterator<MainMenus> parentIt = mainMenuChoices.iterator();
		debugMsg(fnct+"key= " + key);
		debugMsg(fnct+"lang= " + lang);
		for (Iterator<MainMenus> mainMenu = parentIt; mainMenu.hasNext();){
			MainMenus main = mainMenu.next();
			debugMsg("key of main is " + main.getKey());
			if (main.getKey().equalsIgnoreCase(key)){
				debugMsg("found desired key " + main.getKey());
				//this is the desired region
				rtnSubMenu.addAll(main.getSubMenus());
			} else {
				debugMsg("did not find desired key ");
			}
		}
		return rtnSubMenu;
	}
	
	
	
	public Collection<MainMenus> getMain() {
		//returns the main menus for a user's language
		debugMsg("starting getMain");
		String lang = getLangType();
		debugMsg("lang type is " + lang);
		return menuMap.get(lang);
	}
	
	public Collection<MainMenus> getByLang(String lang){
		//can be used to get the menus for any language (pass in the param)
		return menuMap.get(lang);
	}

	public class  Menu implements Serializable{
		//a menu class holds information about a menu item
		//key, value and isExternal means it is an external link
		//this is extended by other classes that use this base class
		private static final long serialVersionUID = 1L;
		String key;
		String value;
		boolean isExternal;
		
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public boolean isExternal() {
			return isExternal;
		}
		public void setExternal(boolean isExternal) {
			this.isExternal = isExternal;
		}
		Menu(){
			//empty constructor for base class
		}
		
	}
	
	public class SubMenus extends Menu{
		//this holds submenu items
		private static final long serialVersionUID = 1L;

		public SubMenus(String key, String value, boolean isExt) {
			setKey(key);
			setValue(value);
			setExternal(isExt);
		}
	}
	
	public class MainMenus extends Menu{
			//this hold mainmenu items, each one has a submenu collection
		private static final long serialVersionUID = 1L;
		Collection<SubMenus> SubMenus;
		public MainMenus(String key, String value, boolean isExt, Collection<SubMenus> subMenus) {
			setValue(value);
			setKey(key);
			setExternal(isExt);
			setSubMenus(subMenus);
		}
		public Collection<SubMenus> getSubMenus() {
			//get all the submenus for this main menu item
			return SubMenus;
		}
		public void setSubMenus(Collection<SubMenus> subMenus) {
			SubMenus = subMenus;
		}
		
	}
	
	
	public MenuBean() {
		// System.out.println("Starting MenuBean");
		//this constructor loads the menus, see initBean
		//langSupport simply defines the columns used in the view for each language
		
		langSupport = getSupportedLanguages();
		
		Database dbName = DominoUtils.getCurrentDatabase();
		String viewName = "Menus";
		initBean(dbName, viewName);
		//testBean();
	}
	
	
	/* Used to test without going to views
	    private void testBean(){
		SubMenus sm1 = new SubMenus("attachments", "attachment label", false);
		SubMenus sm2 = new SubMenus("forks", "fork label", false);
		ArrayList<SubMenus> sub1 = new ArrayList<SubMenus>();
		sub1.add(sm1);
		sub1.add(sm2);
		sm1 = new SubMenus("contactus", "Contact Us", false);
		sm2 = new SubMenus("http://www.tlcc.com", "TLCC", true);
		ArrayList<SubMenus> sub2 = new ArrayList<SubMenus>();
		sub2.add(sm1);
		sub2.add(sm2);
		
		MainMenus main = new MainMenus("products", "Product Label", false, sub1);
		parentMenus.add(main);
		main = new MainMenus("about", "About Label", false, sub2);
		parentMenus.add(main);
		
	}*/
	
	
	
	private void initBean (Database dbName, String viewName) {
		//this is used to load the menu values for each language
		ViewEntry entry;
		ViewEntry tempEntry;
		ViewEntry subEntry;
		ViewEntry tempSubEntry;
		String key;
		String value_americas_en = new String("");
		String value_americas_ca = new String("");
		String value_americas_fr = new String("");
		String value_americas_es = new String("");
		String value_eme_de = new String("");
		String value_eme_en = new String("");
		String value_eme_es = new String("");
		String value_eme_fi = new String("");
		String value_eme_fr = new String("");
		String value_eme_it = new String("");
		String value_eme_nl = new String("");
		String value_eme_se = new String("");
		String extURL;
		String subKey = new String("");
		//temporary placeholder for sub menu labels
		String subvalue_americas_en = new String("");
		String subvalue_americas_ca = new String("");
		String subvalue_americas_fr = new String("");
		String subvalue_americas_es = new String("");
		String subvalue_eme_de = new String("");
		String subvalue_eme_en = new String("");
		String subvalue_eme_es = new String("");
		String subvalue_eme_fi = new String("");
		String subvalue_eme_fr = new String("");
		String subvalue_eme_it = new String("");
		String subvalue_eme_nl = new String("");
		String subvalue_eme_se = new String("");
		String subExtURL;
		boolean isExternal = false;
		
		//hold submenus for each language
		Collection<SubMenus> subMenus_americas_en = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_americas_ca = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_americas_fr = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_americas_es = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_eme_de = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_eme_en = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_eme_es = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_eme_fi = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_eme_fr = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_eme_it = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_eme_nl = new ArrayList<SubMenus>();
		Collection<SubMenus> subMenus_eme_se = new ArrayList<SubMenus>();
		
		MainMenus mainEntry;
		SubMenus subMainEntry;
		
		//hold main menus for each language
		Collection<MainMenus> mainMenus_americas_en = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_americas_ca = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_americas_fr = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_americas_es = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_eme_de = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_eme_en = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_eme_es = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_eme_fi = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_eme_fr = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_eme_it = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_eme_nl = new ArrayList<MainMenus>();
		Collection<MainMenus> mainMenus_eme_se = new ArrayList<MainMenus>();
		try {
			View setupVw = dbName.getView(viewName);
			if (setupVw != null) {
				//get all main menu items, there has to be a Main category in the view!
				ViewEntryCollection MainCol = setupVw.getAllEntriesByKey("Main");
				debugMsg("count is " + MainCol.getCount());
				if (MainCol.getCount()>0 ){
					entry = MainCol.getFirstEntry();
					while (entry != null){
						//loop through main menus and get sub menus
						key = (String) entry.getColumnValues().elementAt(4);
						debugMsg("key is " + key);
						extURL = (String)entry.getColumnValues().elementAt(5);
						if (StringUtil.isNotEmpty(extURL)){
							key = extURL;
							isExternal = true;
						} 
						//load up value for each language
						//this would need to be extended for new language support
						value_americas_en = (String)entry.getColumnValues().elementAt(langSupport.get("americas-en"));
						value_americas_ca = (String)entry.getColumnValues().elementAt(langSupport.get("americas-ca"));
						value_americas_fr = (String)entry.getColumnValues().elementAt(langSupport.get("americas-fr"));
						value_americas_es = (String)entry.getColumnValues().elementAt(langSupport.get("americas-es"));
						value_eme_de = (String)entry.getColumnValues().elementAt(langSupport.get("eme-de"));
						value_eme_en = (String)entry.getColumnValues().elementAt(langSupport.get("eme-en"));
						debugMsg("main value eme-en is " + value_eme_en);
						value_eme_es = (String)entry.getColumnValues().elementAt(langSupport.get("eme-es"));
						value_eme_fi = (String)entry.getColumnValues().elementAt(langSupport.get("eme-fi"));
						value_eme_fr = (String)entry.getColumnValues().elementAt(langSupport.get("eme-fr"));
						value_eme_it = (String)entry.getColumnValues().elementAt(langSupport.get("eme-it"));
						value_eme_nl = (String)entry.getColumnValues().elementAt(langSupport.get("eme-nl"));
						value_eme_se = (String)entry.getColumnValues().elementAt(langSupport.get("eme-se"));
						//see if there are child menus for this main menu item
						ViewEntryCollection subCol = setupVw.getAllEntriesByKey(key);
						debugMsg("key is " + key + " and count is " + subCol.getCount());
						//clear out old submenu array lists
						subMenus_americas_en = new ArrayList<SubMenus>();
						subMenus_americas_ca = new ArrayList<SubMenus>();
						subMenus_americas_fr = new ArrayList<SubMenus>();
						subMenus_americas_es = new ArrayList<SubMenus>();
						subMenus_eme_de = new ArrayList<SubMenus>();
						subMenus_eme_en = new ArrayList<SubMenus>();
						subMenus_eme_es = new ArrayList<SubMenus>();
						subMenus_eme_fi = new ArrayList<SubMenus>();
						subMenus_eme_fr = new ArrayList<SubMenus>();
						subMenus_eme_it = new ArrayList<SubMenus>();
						subMenus_eme_nl = new ArrayList<SubMenus>();
						subMenus_eme_se = new ArrayList<SubMenus>();
						if (subCol.getCount()> 0){
							//found some sub menus
							
							subEntry = subCol.getFirstEntry();
							while (subEntry!= null){
								//get the key
								subKey = (String) subEntry.getColumnValues().elementAt(4);
								//get the url column
								subExtURL = (String)subEntry.getColumnValues().elementAt(5);
								//get all the labels
								subvalue_americas_en = (String)subEntry.getColumnValues().elementAt(langSupport.get("americas-en"));
								subvalue_americas_ca = (String)subEntry.getColumnValues().elementAt(langSupport.get("americas-ca"));
								subvalue_americas_fr = (String)subEntry.getColumnValues().elementAt(langSupport.get("americas-fr"));
								subvalue_americas_es = (String)subEntry.getColumnValues().elementAt(langSupport.get("americas-es"));
								subvalue_eme_de = (String)subEntry.getColumnValues().elementAt(langSupport.get("eme-de"));
								subvalue_eme_en = (String)subEntry.getColumnValues().elementAt(langSupport.get("eme-en"));
								subvalue_eme_es = (String)subEntry.getColumnValues().elementAt(langSupport.get("eme-es"));
								subvalue_eme_fi = (String)subEntry.getColumnValues().elementAt(langSupport.get("eme-fi"));
								subvalue_eme_fr = (String)subEntry.getColumnValues().elementAt(langSupport.get("eme-fr"));
								subvalue_eme_it = (String)subEntry.getColumnValues().elementAt(langSupport.get("eme-it"));
								subvalue_eme_nl = (String)subEntry.getColumnValues().elementAt(langSupport.get("eme-nl"));
								subvalue_eme_se = (String)subEntry.getColumnValues().elementAt(langSupport.get("eme-se"));

								//create a sub menu entry for each language
								//add each sub menu entry to the ArrayList (Collection) for each language
								//this is hard coded for the supported languages, add to this for more lang support!
								subMainEntry = createSubEntry(subKey, subvalue_americas_en, subExtURL);
								if (subMainEntry != null){
									subMenus_americas_en.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_americas_ca, subExtURL);
								if (subMainEntry != null){
									subMenus_americas_ca.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_americas_fr, subExtURL);
								if (subMainEntry != null){
									subMenus_americas_fr.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_americas_es, subExtURL);
								if (subMainEntry != null){
									subMenus_americas_es.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_eme_de, subExtURL);
								if (subMainEntry != null){
									subMenus_eme_de.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_eme_en, subExtURL);
								if (subMainEntry != null){
									subMenus_eme_en.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_eme_es, subExtURL);
								if (subMainEntry != null){
									subMenus_eme_es.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_eme_fi, subExtURL);
								if (subMainEntry != null){
									subMenus_eme_fi.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_eme_fr, subExtURL);
								if (subMainEntry != null){
									subMenus_eme_fr.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_eme_it, subExtURL);
								if (subMainEntry != null){
									subMenus_eme_it.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_eme_nl, subExtURL);
								if (subMainEntry != null){
									subMenus_eme_nl.add(subMainEntry);
								}
								subMainEntry = createSubEntry(subKey, subvalue_eme_se, subExtURL);
								if (subMainEntry != null){
									subMenus_eme_se.add(subMainEntry);
								}
								
								//get next entry after recycle
								tempSubEntry = subCol.getNextEntry(subEntry);
								subEntry.recycle();
								subEntry = tempSubEntry;
							}
						}
						
						
						//check to be sure the main menu item is not empty for that language
						//if not, then create a new MainMenu for that language and add to the ArrayList
						//of mainMenus for each supported language
						if (StringUtil.isNotEmpty(value_americas_en)){
							mainEntry = new MainMenus(key, value_americas_en,isExternal, subMenus_americas_en );
							mainMenus_americas_en.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_americas_ca)){
							mainEntry = new MainMenus(key, value_americas_ca,isExternal, subMenus_americas_ca );
							mainMenus_americas_ca.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_americas_fr)){
							mainEntry = new MainMenus(key, value_americas_fr,isExternal, subMenus_americas_fr );
							mainMenus_americas_fr.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_americas_es)){
							mainEntry = new MainMenus(key, value_americas_es,isExternal, subMenus_americas_es );
							mainMenus_americas_es.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_eme_de)){
							mainEntry = new MainMenus(key, value_eme_de,isExternal, subMenus_eme_de );
							mainMenus_eme_de.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_eme_en)){
							mainEntry = new MainMenus(key, value_eme_en,isExternal, subMenus_eme_en );
							debugMsg("key added to eme_en:" + key );
							mainMenus_eme_en.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_eme_es)){
							mainEntry = new MainMenus(key, value_eme_es,isExternal, subMenus_eme_es );
							mainMenus_eme_es.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_eme_fi)){
							mainEntry = new MainMenus(key, value_eme_fi ,isExternal, subMenus_eme_fi );
							mainMenus_eme_fi.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_eme_fr)){
							mainEntry = new MainMenus(key, value_eme_fr,isExternal, subMenus_eme_fr );
							mainMenus_eme_fr.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_eme_it)){
							mainEntry = new MainMenus(key, value_eme_it,isExternal, subMenus_eme_it );
							mainMenus_eme_it.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_eme_nl)){
							mainEntry = new MainMenus(key, value_eme_nl,isExternal, subMenus_eme_nl );
							mainMenus_eme_nl.add(mainEntry);
						}
						if (StringUtil.isNotEmpty(value_eme_se)){
							mainEntry = new MainMenus(key, value_eme_se,isExternal, subMenus_eme_se );
							mainMenus_eme_se.add(mainEntry);
						}
						//get next main menu entry after recycle
						tempEntry= MainCol.getNextEntry(entry);
						entry.recycle();
						entry = tempEntry;
					}
				//now add the mainMenu list for each lang. to the menuMap
				//add to language map
					debugMsg("eme=en count is :" + mainMenus_eme_en.size());
					menuMap.put("americas-en" ,mainMenus_americas_en);
					menuMap.put("americas-ca" ,mainMenus_americas_ca);
					menuMap.put("americas-fr" ,mainMenus_americas_fr);
					menuMap.put("americas-es" ,mainMenus_americas_es);
					menuMap.put("eme-de" ,mainMenus_eme_de);
					menuMap.put("eme-en" ,mainMenus_eme_en);
					menuMap.put("eme-es" ,mainMenus_eme_es);
					menuMap.put("eme-fi" ,mainMenus_eme_fi);
					menuMap.put("eme-fr" ,mainMenus_eme_fr);
					menuMap.put("eme-it" ,mainMenus_eme_it);
					menuMap.put("eme-nl" ,mainMenus_eme_nl);
					menuMap.put("eme-se" ,mainMenus_eme_se);
				}
				
			}
		

		} catch (Exception e) {
			debugMsg(" catch " + e.toString() );
			e.printStackTrace();
			throw new RuntimeException(
			"MenuBean: error in initBean method: " + e.toString());
		}
	}
	
	private SubMenus createSubEntry(String key, String value, String url){
		//return null if the label for this language was an empty string
		SubMenus subMenu = null;
		boolean subIsExt = false;
		if (StringUtil.isNotEmpty(value)){
			//if the label is empty skip this menu item
			if (StringUtil.isNotEmpty(url)){
				debugMsg("URL USED" + StringUtil.isSpace(url));
				key = url;
				subIsExt = true;
			}
			//create subMenu object
			subMenu = new SubMenus(key, value,subIsExt);
		}	
		return subMenu;
	}
		
		
	public void resetConfig() {
		//used from an XPage to reset the menuBean to read values from the db again
		// use current database
		Database dbName = DominoUtils.getCurrentDatabase();
		// setup values will be read from view named below
		String viewName = "Menus";
		// setup values
		initBean(dbName, viewName);
	}
	
	
	private HashMap<String, Integer> getSupportedLanguages(){
		HashMap<String, Integer> languages = new HashMap<String, Integer>();
		//load supported languages names and view column numbers
		//this would have to be extended for new languages and the view modified
		languages.put(new String("americas-en"), new Integer(6));
		languages.put(new String("eme-de"), new Integer(7));
		languages.put(new String("eme-en"), new Integer(8));
		languages.put(new String("eme-es"), new Integer(9));
		languages.put(new String("eme-fi"), new Integer(10));
		languages.put(new String("eme-fr"), new Integer(11));
		languages.put(new String("eme-it"), new Integer(12));
		languages.put(new String("eme-nl"), new Integer(13));
		languages.put(new String("eme-se"), new Integer(14));
		languages.put(new String("americas-es"), new Integer(15));
		languages.put(new String("americas-ca"), new Integer(16));
		languages.put(new String("americas-fr"), new Integer(17));
		return languages;
	}
	
	@SuppressWarnings("unchecked")
	private void debugMsg(String msg){
		Map viewscope = (Map) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "viewScope");
		if (viewscope.containsKey("debug")){
			if ( ((String) viewscope.get("debug")).equalsIgnoreCase("true")  ){
				DebugToolbarBean.get().info(msg);
			} 
		} else if (showDBar){
			DebugToolbarBean.get().info(msg);
		}
	}
	
}
