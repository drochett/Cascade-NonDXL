 package com.cascorp;

import java.io.Serializable;
import java.util.Arrays;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.List;
import javax.faces.context.FacesContext;

import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.DataObject;
import com.ibm.xsp.model.domino.DominoUtils;
 
public class LangBean implements Serializable, DataObject {
	
	//This bean is used to supply translated labels
	
	//turn on to show DBar messages or set viewScope="true" on XPage
	private boolean showDBar = true;
	private static final long serialVersionUID = 1L;

	private HashMap<Object, Object> langMap = new HashMap<Object, Object>();
	
	@SuppressWarnings("unchecked")
	private String getLangType() {
		//this gets the language for the user
		Map sessionscope = (Map) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "sessionScope");
		if (((String) sessionscope.get("LanguageP") == "") || ((String) sessionscope.get("LanguageP") == null)){
			// assume English
			return "americas-en";
		} else {
			return (String) sessionscope.get("LanguageP");
		}
	}

	public Class<String> getType(Object key) {
		return String.class;
	}

	@SuppressWarnings("unchecked")
	public Object getValue(Object key) {
		//System.out.println("starting getValue");
		//debugMsg("starting getValue");
		String langType = getLangType();
		//debugMsg("lang type is " + langType);
		//System.out.println("lang type is " + langType);
		if(!langMap.containsKey(key)) {
			 throw new RuntimeException("Argument not found in Language Bean:" + key); 
		 } else {
			 //
			 //debugMsg("key is " + key);
			 Map<String, Object> itemMap = (Map<String, Object>) langMap.get(key);
			 //debugMsg("value is "+ itemMap.get(langType));
			 return itemMap.get(langType);
		 }
	}

	@SuppressWarnings("unchecked")
	public String getLangString(String varName, String lang) {
		
		//this gets the language label for the user based on the user's language
		String rtnString = new String("");
		//check to see if item is in the label map
		if (!langMap.containsKey(varName)) {
			throw new RuntimeException(
					"getLangString: variable not found in Language Bean");
		} else {
			//get the map of languages for the requested item
			Map<String, String> itemMap = (Map<String, String>) langMap
					.get(varName);
			//check that the requested lang is in the map for that item
			if (itemMap.containsKey(lang)) {
					//return requested language string
					rtnString = itemMap.get(lang);
			} else {
				throw new RuntimeException(
				"getLangString: language for requested string not found in Language Bean. Name is " + varName + " and language is " + lang );
			}
		}
		return rtnString;
	}
	
	public boolean isReadOnly(Object arg0) {
		return true;
	}

	public void setValue(Object arg0, Object arg1) {
		throw new UnsupportedOperationException();

	}

	public LangBean() {
		 //System.out.println("Starting LangBean");
		//constructor used for the langBean
		
		//debugMsg("Starting lang bean init");
		Database dbName = DominoUtils.getCurrentDatabase();
		String viewName = "ActiveLabelValues";
		initBean(dbName, viewName);
	}
	private HashMap<String, Integer> getSupportedLanguages(){
		HashMap<String, Integer> languages = new HashMap<String, Integer>();
		//load supported languages names and view column numbers
		languages.put(new String("americas-en"), new Integer(2));
		languages.put(new String("eme-de"), new Integer(3));
		languages.put(new String("eme-en"), new Integer(4));
		languages.put(new String("eme-es"), new Integer(5));
		languages.put(new String("eme-fi"), new Integer(6));
		languages.put(new String("eme-fr"), new Integer(7));
		languages.put(new String("eme-it"), new Integer(8));
		languages.put(new String("eme-nl"), new Integer(9));
		languages.put(new String("eme-se"), new Integer(10));
		languages.put(new String("americas-es"), new Integer(11)); //added by mdz 07/07/2017
		languages.put(new String("americas-ca"), new Integer(12)); //added by dsr 9/27/17
		languages.put(new String("americas-fr"), new Integer(13)); //added by dsr 9/27/17
		return languages;
	}
	
	
	
	public void resetConfig() {
		// System.out.println("Starting resetConfig");
		// use current database
		Database dbName = DominoUtils.getCurrentDatabase();
		// setup values will be read from view named below
		String viewName = "ActiveLabelValues";
		// setup values
		initBean(dbName, viewName);
	}

	@SuppressWarnings("unchecked")
	private void initBean(Database dbName, String viewName) {
		 //System.out.println("Starting initBean");
		 //System.out.println("viewName = " + viewName);
		
		//This loads up the label translations
		ViewEntry tempEntry;
		ViewEntry entry;
		String isMultValue;
		String langName;
		HashMap<String, String> itemMap;
		HashMap<String, List> multiCol;
		
		try {
			View setupVw = dbName.getView(viewName);
			if (setupVw != null) {
				ViewEntryCollection vwEntryCol = setupVw.getAllEntries();
				entry = vwEntryCol.getFirstEntry();
				if (entry != null) {
					while (entry != null) {
						// loop through view to load up all entries
						Vector values = entry.getColumnValues();
						//System.out.println("Key is " + values.elementAt(0));
						isMultValue = (String)values.elementAt(1);
						//System.out.println("is mult value:" + isMultValue);
						if (StringUtil.equalsIgnoreCase(isMultValue, new String("Yes"))){
							//get values from document since they are multi value
							//System.out.println("multi value part for " + (String) values.elementAt(0));
							Document langDoc = entry.getDocument();
							//System.out.println("key from doc" + langDoc.getItemValueString("key"));
							//init multiCol
							multiCol = new HashMap<String, List>();
							
							for (String langObj : getSupportedLanguages().keySet()){
								langName = langObj.toString();
								String fieldName = "val_" + StringUtil.replace(langName, "-", "_");
								//System.out.println("fieldName is "+ fieldName);
								Vector v = langDoc.getItemValue( fieldName);
								//System.out.println("size is " + v.size());
								//System.out.println("storing " + langName + " value is "+ Arrays.asList(v.toArray()));
								multiCol.put(langName, Arrays.asList(v.toArray()));
							}
							//store in bean
							langMap.put((String) values.elementAt(0), multiCol);
							
						} else {
							//System.out.println("Starting the regular lang part");
						//init itemMap
						itemMap = new HashMap<String, String>();
						
						//loop through all languages for this row
						
						for (Map.Entry<String, Integer> langEntry : getSupportedLanguages().entrySet()){
							//System.out.println("getting " + langEntry.getKey());
							//System.out.println( "value is " + values.elementAt(0));
							if (values.elementAt(langEntry.getValue()).getClass().equals(String.class )){
								//test if a String before storing
								itemMap.put(langEntry.getKey(), (String) values.elementAt(langEntry.getValue()));
							} else {
								//send a message to the console
								System.out.println("ERROR in langBean: key " + values.elementAt(0) + " is not a String");
							}
						}
						//store in Bean
						langMap.put(values.elementAt(0), itemMap);
						}
						
						// get next view entry, recycle in the process
						tempEntry = vwEntryCol.getNextEntry(entry);
						entry.recycle();
						entry = tempEntry;
					} // end while loop
				} else {
					// no documents were found
					throw new RuntimeException("No Language Documents found in LangBean");
				}
			} else {
				// the setup view name was not found in the current database
				throw new RuntimeException("ActiveLabelValues View not found (from LangBean)");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
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
