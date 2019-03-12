package com.cascorp;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import javax.faces.context.FacesContext;

import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.rest.api.Client;
import com.rest.api.Response;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.Session;
import lotus.domino.View;

public class Eloqua {
	private static boolean showDBar = true;

	private static String appendFieldData(String fieldID, String fieldValue) {
		try {
			String returnJSON = "{\"type\": \"FieldValue\",";
			returnJSON = returnJSON.concat("\"id\": \"");
			returnJSON = returnJSON.concat(fieldID);
			returnJSON = returnJSON.concat("\",\"value\": \"");
			returnJSON = returnJSON.concat(fieldValue);
			returnJSON = returnJSON.concat("\"}");
			debugMsg(returnJSON);
			return returnJSON;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@SuppressWarnings("unchecked")
	private static String getSubmissionData(Document doc, Vector vector) {
		try {
			debugMsg("getSubmissionData started!");
			String body = "{\"fieldValues\": [";
			Iterator itr = vector.iterator();

			while (itr.hasNext()) {
				String thisValue = "";
				String arr[] = itr.next().toString().split("~");
				// debugMsg(arr[0]);
				if (Objects.equals(arr[0], new String("@Created"))) {
					thisValue = doc.getCreated().toString();
					body = body.concat(appendFieldData(arr[1], thisValue));
					if (itr.hasNext()) {
						body = body.concat(",");
					}
				} else if (doc.hasItem(arr[0])) {
					Item item = doc.getFirstItem(arr[0]);
					switch (item.getType()) {
					case Item.NUMBERS:
						thisValue = Double.toString(item.getValueDouble());
						break;
					case Item.DATETIMES:
						thisValue = item.getDateTimeValue().getDateOnly().toString();
						break;
					case Item.TEXT:
						thisValue = item.getText();
						break;
					default:
						thisValue = item.getText();
						break;
					}
					// body = body.concat(appendFieldData(arr[1],
					// doc.getItemValueString(arr[0])));
					body = body.concat(appendFieldData(arr[1], thisValue));
					if (itr.hasNext()) {
						body = body.concat(",");
					}
				}
				// debugMsg(thisValue);
			}
			body = body.concat("]}");
			return body;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static void submitToEloqua(Document submitDoc) {

		try {
			debugMsg("Submit TO Eloqua starting!");
			// Session session = NotesFactory.createSession();
			Session session = ExtLibUtil.getCurrentSession();
			// Get document used for passing data
			Database db = session.getCurrentDatabase();
			debugMsg("Get View");
			View setupView = db.getView("Eloqua Setup");
			debugMsg("Get Setup Doc for " + submitDoc.getItemValue("Form"));
			Document setupDoc = setupView.getDocumentByKey(submitDoc.getItemValue("Form"));
			debugMsg("Found setup doc!");

			// (Your code goes here)
			String site = setupDoc.getItemValueString("CompanyName"); // "CascadeCorporation";
			String user = setupDoc.getItemValueString("Username"); // "Daryl.Rochette";
			String password = setupDoc.getItemValueString("Password"); // "abcDEF123";
			String baseUrl = setupDoc.getItemValueString("BaseURL"); // "https://secure.p03.eloqua.com/API/REST/2.0";
			String formID = setupDoc.getItemValueString("FormID");
			Vector vector = setupDoc.getItemValue("fieldMap");
			debugMsg(vector.toString());

			Client client = new Client(site + "\\" + user, password, baseUrl);
			debugMsg(client.toString());

			// Response response = client.get("/data/contact/2");

			// String body = "{\"fieldValues\": [{\"type\":
			// \"FieldValue\",\"id\": \"1022\",\"value\":
			// \"Fredrick\"},{\"type\": \"FieldValue\",\"id\":
			// \"1023\",\"value\": \"Smithson\"},{\"type\":
			// \"FieldValue\",\"id\": \"1024\",\"value\":
			// \"fred.smith@fedex.com\"},{\"type\": \"FieldValue\",\"id\":
			// \"1026\",\"value\": \"99999\"},{\"type\": \"FieldValue\",\"id\":
			// \"1266\",\"value\": \"Recycling Industry\"},{\"type\":
			// \"FieldValue\",\"id\": \"1267\",\"value\": \"This is a question I
			// guess\"}]}";
			// String body = "{\"fieldValues\": [{\"type\":
			// \"FieldValue\",\"id\": \"1022\",\"value\":
			// \"Fredrick\"},{\"type\": \"FieldValue\",\"id\":
			// \"1023\",\"value\": \"Smithson\"},{\"type\":
			// \"FieldValue\",\"id\": \"1024\",\"value\":
			// \"fred.smith@fedex.com\"}]}";
			String body = getSubmissionData(submitDoc, vector);
			debugMsg(body);
			Response response2 = client.post("/data/form/" + formID, body);

			debugMsg(response2.exception);
			debugMsg(response2.errorStream);
			debugMsg(response2.exception);
			debugMsg("Java agent Completed!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void debugMsg(String msg) {
		Map viewscope = (Map) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "viewScope");
		if (viewscope.containsKey("debug")) {
			if (((String) viewscope.get("debug")).equalsIgnoreCase("true")) {
				DebugToolbarBean.get().info(msg);
			}
		} else if (showDBar) {
			DebugToolbarBean.get().info(msg);
		}
	}
}
