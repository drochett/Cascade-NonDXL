package com.cascorp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import com.ibm.xsp.model.DataObject;
import com.ibm.xsp.model.domino.DominoUtils;
      
public class ConfigBean implements Serializable, DataObject {
   
	/**
This Bean is used to read in values from a view in the database and setup a HashMap that
is accessed via EL or Java. It will read in all the documents from the view.
	 **/
	private static final long serialVersionUID = 1L;
	private HashMap<Object, Object> configMap = new HashMap<Object, Object>(); 
	public Class<String> getType(Object arg0) {
		return String.class;
	}

	public Object getValue(Object key) {
		//get the value, throws an exception if the key was not found
		 if(!configMap.containsKey(key)) {
			 throw new RuntimeException("Argument not found in configBean: " + key); 
		 } 
		 return configMap.get(key);
	}

	public boolean isReadOnly(Object arg0) {
		return true;
	}

	public void setValue(Object key, Object value) {
		 throw new UnsupportedOperationException();
	}
	public ConfigBean() {
		//initialize the values from the view named below
		//System.out.println("Starting ConfigBean");
		Database dbName = DominoUtils.getCurrentDatabase();
		String viewName = "ActiveSetupValues";
		//all the work happens here
		initBean(dbName, viewName);
	}
	public void resetConfig(){
		//this method is used to reload all the config values from the view
		//System.out.println("Starting resetConfig");
		//use current database
		Database dbName = DominoUtils.getCurrentDatabase();
		//setup values will be read from view named below
		String viewName = "ActiveSetupValues";
		//setup values
		initBean(dbName, viewName);
	}
	
	@SuppressWarnings("unchecked")
	private void initBean(Database dbName, String viewName) {
		//initializes the config values from the view
		//System.out.println("Starting initBean");
		ViewEntry tempEntry;
		ViewEntry entry;
		List<String> list;
		try {
			View setupVw = dbName.getView(viewName);
			if (setupVw != null) {
				ViewEntryCollection vwEntryCol = setupVw.getAllEntries();
				entry = vwEntryCol.getFirstEntry();
				Document configDoc;
				if (entry != null) {
					while (entry != null) {
						configDoc = entry.getDocument();
						//Vector values = entry.getColumnValues();
						Vector v = configDoc.getItemValue("value");
						//System.out.println("Loading: " + values.elementAt(0) + " value is " + values.elementAt(1));
						if (v.isEmpty() == false ){
							if (v.size()== 1){
								configMap.put(configDoc.getItemValueString("key"), configDoc.getItemValueString("value"));	
								//System.out.println(configDoc.getItemValueString("value"));
							} else {
								list = new ArrayList(Arrays.asList(v.toArray()) );
								configMap.put(configDoc.getItemValueString("key"), list);
								//System.out.println("Storing array " + configDoc.getItemValueString("key"));
							}
						}
						// get next view entry, recycle in the process
						tempEntry = vwEntryCol.getNextEntry(entry);
						entry.recycle();
						entry = tempEntry;
					} //end while loop
				} else {
					//no documents were found
					throw new RuntimeException("No Setup Documents found in ActiveSetupValues");
				}
			} else {
				//the setup view name was not found in the current database
				throw new RuntimeException("ActiveSetupValues View not found in ConfigBean");
			}
  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
