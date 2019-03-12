package com.cascorp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import lotus.domino.Database;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntryCollection;
import lotus.domino.Document;
import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.DominoDocumentData;
import com.ibm.xsp.model.domino.DominoUtils;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

//public class utilities implements Serializable {
public class utilities {
	// private static final long serialVersionUID = 1L;
	String rightData;
	String leftData;
	private static boolean showDBar = true;
	Collection<String> errorMessages;

	// private Object attachmentIdentifier;

	public void setLeftData(String leftData) {
		this.leftData = leftData;
	}

	public Collection<String> getErrorMessages() {
		return errorMessages;
	}

	public String getLeftDataInfo(String data) {
		String parser = "^";
		// System.out.println(parser);
		// System.out.println(data);
		Integer indexOf = data.indexOf(parser);
		// System.out.println(indexOf);
		// System.out.println(data.substring(0,indexOf));
		// leftData =
		// data.substring(data.indexOf(parser),data.indexOf(parser)+data.length());
		// System.out.println(data.substring(data.indexOf(parser),data.indexOf(parser)+data.length()));

		return data.substring(0, indexOf);

	}

	public String getRightDataInfo(String data) {
		String parser = "^";
		// System.out.println("["+parser+"]");
		// System.out.println(data);
		Integer indexOf = data.indexOf(parser);
		// System.out.println(indexOf);
		// rightData =
		// data.substring(data.indexOf(parser),data.indexOf(parser)+data.length());
		// System.out.println(data.substring(data.indexOf(parser),data.indexOf(parser)+data.length()));
		// System.out.println(data.substring(indexOf + 1));

		return data.substring(indexOf + 1);
	}

	public boolean checkAttachmentName(String attachmentName, String serialNbr) {
		// when a user registers a new attachment, this will check if
		// userName+attachmentName as a combo is already
		// being used and then it checks if userName+serial as a combo is
		// already being used
		debugMsg("Starting checkAttachmentName");
		debugMsg("ATTACHMENT NAME >>>>>> " + attachmentName);
		debugMsg("SERIAL NBR >>>>>> " + serialNbr);
		boolean rtn = false;
		errorMessages = new ArrayList<String>();
		try {
			Session s = ExtLibUtil.getCurrentSessionAsSigner();
			// FacesContext context = FacesContext.getCurrentInstance();
			ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String upDbname = (String) configBean1
					.getValue("UserprofilesDbPath");
			// String checked = "";
			Database upDb = null;
			upDb = s.getDatabase(s.getServerName(), upDbname, false);

			if (upDb != null) {
				debugMsg("Database is open and title is " + upDb.getTitle());
				View upVw = upDb.getView("attachmentsbyownerflat");
				debugMsg("view" + upVw.getName());
				if (upVw != null) {
					ViewEntryCollection colAttachments = upVw
							.getAllEntriesByKey(attachmentName, true);
					// debugMsg("NAME CHECK count is:" +
					// we are not checking for double attachment names if we do
					// then activate code below
					if (attachmentName != "notAvaiable") {

					}
					debugMsg("CHECKING SERIAL NUMBER:  " + serialNbr);
					upVw = upDb.getView("serialbyownerflat");
					colAttachments = upVw.getAllEntriesByKey(serialNbr, true);
					debugMsg("SERIAL CHECK count is:"
							+ colAttachments.getCount());
					if (colAttachments.getCount() > 0) {
						// if serial number was found for user return false
						debugMsg("Serial already being used");
						errorMessages.add("serialUsed");
						debugMsg("serial");
						rtn = false;
					}
					debugMsg("Error Message Array Size is "
							+ errorMessages.size());
					if (errorMessages.size() > 0) {
						rtn = false;
					} else {
						rtn = true;
					}
				}
			}
		} catch (Exception e) {
			debugMsg("ERROR in checkAttachmentName:  " + e.toString());
		}
		return rtn;
	}

	@SuppressWarnings("unchecked")
	private static void debugMsg(String msg) {
		Map viewscope = (Map) ExtLibUtil.resolveVariable(FacesContext
				.getCurrentInstance(), "viewScope");
		if (viewscope.containsKey("debug")) {
			if (((String) viewscope.get("debug")).equalsIgnoreCase("true")) {
				DebugToolbarBean.get().info(msg);
			}
		} else if (showDBar) {
			DebugToolbarBean.get().info(msg);
		}
	}
}
