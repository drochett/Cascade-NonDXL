package com.cascorp;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.FacesContext;

import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Directory;
import lotus.domino.DirectoryNavigator;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import com.cascorp.JDESupport.JDEData;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.DominoUtils;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import com.ibm.xsp.model.domino.wrapped.DominoRichTextItem;

public class RegistrationUtilities {
	/*
	 * This is used during Registration to perform validation, interface with
	 * the address book and other tasks like put users in groups It also sends
	 * out the various emails used in the registration process
	 */
	private static boolean showDBar = true;

	private static class FieldData {
		String nabDocName;
		String labelName;
		String newValue;

		public FieldData() {

		}
	}
	public static String getLeftStr(String fullString, String searchString) {
		String retString = (fullString.indexOf(searchString) > -1 )?fullString.substring(0, fullString.indexOf(searchString)): fullString;	
		return retString;
	}
	
	public static String getRightStr(String fullString, String searchString) {
		String retString = (fullString.indexOf(searchString) > -1 )?fullString.substring(fullString.indexOf(searchString)+2,fullString.length()): fullString;	
		return retString;
	}

	public static String getRightBackStr(String fullString, String searchString) {
		String retString = (fullString.lastIndexOf(searchString) > -1 )?fullString.substring(fullString.lastIndexOf(searchString)+2,fullString.length()): fullString;	
		return retString;
	}
	
	public static String getDisplayValue(String fullList, String storedValue) {
		return getRightBackStr(getLeftStr(fullList,"|" + storedValue), ", ");
	}
	public static String getEloquaLang(String langList, String selValue) {
		String xLang = getDisplayValue(langList,selValue);
		if (xLang.equals("Spanish (Americas)")) {
			return "Spanish - Americas";
		} else if (xLang.equals("Spanish (Spain)")) {
			return "Spanish - Spain";
		} else if (xLang.indexOf("English")==0) {
			return "English";
		} else if (xLang.indexOf("French")==0) {
			return "French";
		} else {
			return xLang;
		}
	}
	public static String getEloquaCountry(String countryList, String selValue) {
		String xCountry = getDisplayValue(countryList,selValue);
		if (xCountry.indexOf("Korea")==0) {
			return "Korea, South";
		} else {
			return xCountry;
		}
	}
	public static String getHTMLfromRT(final Document doc,
			final String richTextItemName) throws NotesException {
		// this is code that converts a richtext item to HTML
		// standard code from OpenNTF snippets, web search, etc....
		DominoDocument wrappedDoc = null;

		Database db = doc.getParentDatabase();

		// disable MIME to RichText conversion
		db.getParent().setConvertMIME(false);

		// wrap the lotus.domino.Document as a lotus.domino.DominoDocument
		// see
		// http://public.dhe.ibm.com/software/dw/lotus/Domino-Designer/JavaDocs/DesignerAPIs/com/ibm/xsp/model/domino/wrapped/DominoDocument.html
		wrappedDoc = DominoDocument.wrap(doc.getParentDatabase().getFilePath(),
				doc, null, null, false, null, null);

		// see
		// http://public.dhe.ibm.com/software/dw/lotus/Domino-Designer/JavaDocs/DesignerAPIs/com/ibm/xsp/model/domino/wrapped/DominoRichTextItem.html
		DominoRichTextItem drti = null;

		Item itemRT = doc.getFirstItem(richTextItemName);

		if (null != itemRT) {

			if (itemRT.getType() == Item.RICHTEXT) {

				// create a DominoRichTextItem from the RichTextItem
				RichTextItem rt = (RichTextItem) itemRT;
				drti = new DominoRichTextItem(wrappedDoc, rt);

			} else if (itemRT.getType() == Item.MIME_PART) {

				// create a DominoRichTextItem from the Rich Text item that
				// contains MIME
				MIMEEntity rtAsMime = doc.getMIMEEntity(richTextItemName);
				drti = new DominoRichTextItem(wrappedDoc, rtAsMime,
						richTextItemName);

			}
		}

		return drti.getHTML();

	}

	@SuppressWarnings("unchecked")
	public static String activateRegistration(String lang, String unid,
			Session session) {
		/*
		 * When the user activates their registration than this routine
		 * completes it...the user is created in the Domino Directory and the
		 * registration db is updated. The unid is the doc id of the
		 * registration document (reg db)
		 */
		String rtn = new String("");
		DebugToolbarBean.get().info("Starting" + unid);
		ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		String regDbname = (String) configBean1.getValue("RegistrationDbPath");
		debugMsg(regDbname);
		String nabDbname = (String) configBean1
				.getValue("UserDominoDirectoryPath");
		debugMsg(nabDbname);
		Map viewScope = FacesContext.getCurrentInstance().getViewRoot()
				.getViewMap();
		try {
			Database regDb = session.getDatabase(session.getServerName(),
					regDbname, false);
			if (regDb != null) {
				Document regDoc = regDb.getDocumentByUNID(unid);
				if (regDoc.isValid()) {
					// got a good document
					debugMsg("The document is valid:  " + regDoc.getCreated());
					String userName = regDoc.getItemValueString("fullname");
					String regType = regDoc.getItemValueString("regtype");
					debugMsg("user name in activate is " + userName);
					viewScope.put("userName", userName);
					// check address book again for the user
					if (checkAddressBook(userName)) {
						// ok to setup
						Database nabDb = session.getDatabase(session
								.getServerName(), nabDbname, false);
						if (nabDb != null) {
							// create NAB person doc and add to groups

							if (createPersonInNab(regDoc, nabDb)) {
								// add to groups
								// add all users to the external group
								String allUsersGroupName = (String) configBean1
										.getValue("GroupNameExternalUsers");
								boolean groupCheck = false;
								groupCheck = addUserToMultiGroup(userName,
										allUsersGroupName, nabDb, session);
								debugMsg("activateRegistration - done with checkGroup");
								if (groupCheck) {
									debugMsg("Added " + userName
											+ " to group: " + allUsersGroupName);
								} else {
									debugMsg("FAILED adding " + userName
											+ " to group: " + allUsersGroupName);
								}
								
								if (regType=="oem") {
									String oemAppendGroupName = findOEMGroupName(regDoc
											.getItemValueString("AccountNumber"));
									debugMsg("oemAppendGroupName is "
											+ oemAppendGroupName);
									if (StringUtil.isNotEmpty(oemAppendGroupName)) {
										// setup OEM group if the account number is
										// listed as an OEM
										debugMsg("Starting OEM group stuff");
										String oemPrefix = (String) configBean1
												.getValue("OEMGroupPrefix");
										String fulloemGroupName = oemPrefix
												.concat(oemAppendGroupName);
										debugMsg("full oemGroupName is "
												+ fulloemGroupName);
										groupCheck = addUserToGroup(userName,
												fulloemGroupName, nabDb, session);
										if (groupCheck) {
											debugMsg("Added " + userName
													+ " to group: "
													+ fulloemGroupName);
										} else {
											debugMsg("FAILED adding " + userName
													+ " to group: "
													+ fulloemGroupName);
										}
										// add user to all OEM group
	
										String oemGroupName = (String) configBean1
												.getValue("OEMGroupName");
										groupCheck = addUserToGroup(userName,
												oemGroupName, nabDb, session);
										if (groupCheck) {
											debugMsg("Added " + userName
													+ " to group: " + oemGroupName);
										} else {
											debugMsg("FAILED adding " + userName
													+ " to group: " + oemGroupName);
										}
									}
									// added 08-05-16 mdz - need to make sure
									// account is not in OEMGeneralDeny list
									// debugMsg("Starting new code added 08-05-16 mdz");
									String oemAppendDenyName = findOEMDenyName(regDoc
											.getItemValueString("AccountNumber"));
									debugMsg("oemAppendDenyName is "
											+ oemAppendDenyName);
									if (StringUtil.isNotEmpty(oemAppendDenyName)) {
										// setup OEM group if the account number is
										// listed as an OEM
										debugMsg("Starting OEM group stuff");
										String oemPrefix = (String) configBean1
												.getValue("OEMGroupPrefix");
										String fulloemDenyName = oemPrefix
												.concat(oemAppendDenyName);
										debugMsg("fulloemDenyName is "
												+ fulloemDenyName);
										groupCheck = addUserToGroup(userName,
												fulloemDenyName, nabDb, session);
										if (groupCheck) {
											debugMsg("Added " + userName
													+ " to group: "
													+ fulloemDenyName);
										} else {
											debugMsg("FAILED adding " + userName
													+ " to group: "
													+ fulloemDenyName);
										}
										// add user to all OEM group
	
										String oemDenyName = (String) configBean1
												.getValue("OEMGroupPrefix")
												+ oemAppendDenyName;
										groupCheck = addUserToGroup(userName,
												oemDenyName, nabDb, session);
										if (groupCheck) {
											debugMsg("Added " + userName
													+ " to group: " + oemDenyName);
										} else {
											debugMsg("FAILED adding " + userName
													+ " to group: " + oemDenyName);
										}
									}
									// end of code added on 08/05-16 mdz
	
									// added 05-15-17 mdz - MCR WSCR-AKHTEC New Page
									// and Web Form for Dealers Only (Not public or
									// OEMs)
									// If AcccessType is "D" add the user to the
									// Cascorp-External-Dealers group
									String dealerGroupName = (String) configBean1
											.getValue("DealerGroupName");
									String accessType = regDoc
											.getItemValueString("AccessType");
									debugMsg("full dealerGroupName is "
											+ dealerGroupName);
									debugMsg("regDoc.AccessType = [" + accessType
											+ "]");
									if (StringUtil.isNotEmpty(dealerGroupName)
											&& (accessType.equals("D") || accessType
													.equals("B"))) {
										debugMsg("Starting dealer group stuff");
										debugMsg("AccessType is D");
										groupCheck = addUserToMultiGroup(userName,
												dealerGroupName, nabDb, session);
										if (groupCheck) {
											debugMsg("Added " + userName
													+ " to group: "
													+ dealerGroupName);
										} else {
											debugMsg("FAILED adding " + userName
													+ " to group: "
													+ dealerGroupName);
										}
									} else {
										debugMsg("AccessType actually is |"
												+ accessType + "|");
									}
									// end of code added 05-15-17 mdz
	
									// 11/09/2017 - mdz WSCR-ASES6S - Vendors / DXF
									// Files - add code to put vendors in proper DXL
									// group
									String dxfAppendGroupName = findDXFGroupName(regDoc
											.getItemValueString("AccountNumber"));
									debugMsg("dfxAppendGroupName is "
											+ dxfAppendGroupName);
									if (StringUtil.isNotEmpty(dxfAppendGroupName)) {
										// setup DXF group if the account number is
										// listed as an DXF
										debugMsg("Starting DXF group stuff");
										String dxfPrefix = (String) configBean1
												.getValue("DXFGroupPrefix");
										String fulldxfGroupName = dxfPrefix
												.concat(dxfAppendGroupName);
										debugMsg("full dxfGroupName is "
												+ fulldxfGroupName);
										groupCheck = addUserToGroup(userName,
												fulldxfGroupName, nabDb, session);
										if (groupCheck) {
											debugMsg("Added " + userName
													+ " to group: "
													+ fulldxfGroupName);
										} else {
											debugMsg("FAILED adding " + userName
													+ " to group: "
													+ fulldxfGroupName);
										}
										// add user to all OEM group
	
										String dxfGroupName = (String) configBean1
												.getValue("DXFGroupName");
										groupCheck = addUserToGroup(userName,
												dxfGroupName, nabDb, session);
										if (groupCheck) {
											debugMsg("Added " + userName
													+ " to group: " + dxfGroupName);
										} else {
											debugMsg("FAILED adding " + userName
													+ " to group: " + dxfGroupName);
										}
									}
									// WSCR-ASES6S - Vendors / DXF File end of code
									// added 11/09/2017 mdz
									
								// DSR - 12-10-18 - Added support for End User Registration
								} else {
									// Do End User Stuff
								}
								// DSR - 12-10-18 <eoc>
								
								
								// all done
								// update the views in the nab
								refreshLookupViews(userName, nabDb, session);
								debugMsg("Refreshed views for " + userName);
								// remove password information from regdoc
								regDoc.replaceItemValue("NewPassword", "");
								regDoc.replaceItemValue("NewPasswordConfirm",
										"");
								regDoc.replaceItemValue("Status", "Processed");
								regDoc.save(false, false);
								// set the destination url to go to, this is the
								// page the
								// user might have been on when they started to
								// register
								String DestURL = regDoc
										.getItemValueString("DestinationURL");
								debugMsg("dest url is " + DestURL);
								if (StringUtil.isNotEmpty(DestURL)) {
									viewScope.put("originalPage", DestURL);
								}
								// Send activated email msg
								sendRegAfterActivateEmail(regDoc
										.getItemValueString("firstname"),
										userName,
										regDoc.getItemValueString("LanguageP"),
										session);

								// return the success string to the XPage
								rtn = "success";
								// WSCR-B4BSU9 - Web Content Change - Email
								// notice for new Accounts 10-05-2018 mdz
								sendRegNotifyEmail(regDb, regDoc);
							} else {
								// person could not be created
								rtn = "failure-baddoc";
								debugMsg("createPersonInNab failed");

							}

						} else {
							// nabDB can not be opened
							rtn = "failure-baddoc";
							debugMsg("NAB db not open");
							throw new RuntimeException(
									"Error in activateRegistration: NAB DB Not Open");

						}
					} else {
						// user already is in address book
						rtn = "failure-dupemail";
					}

				} else {
					rtn = "failure-baddoc";
					debugMsg("URL is bad");
				}

			} else {
				rtn = "failure-baddoc";
				debugMsg("Reg db not open");
				throw new RuntimeException(
						"Error in activateRegistration: Reg DB Not Open");
			}
		} catch (Exception e) {
			if (StringUtil.lastIndexOfIgnoreCase(e.toString(),
					"Invalid universal id") >= 0) {
				rtn = "failure-baddoc";
				debugMsg("error in activateRegistration: Bad UNID");

			} else {
				// some other exception
				rtn = "failure-baddoc";
				debugMsg("error in activateRegistration:Error is "
						+ e.toString());
			}
		}
		return rtn;
	}

	public synchronized static boolean checkAddressBook(String email) {
		// pass in email of prospective user
		// returns true if user was not found and ok to register them
		debugMsg("Starting checkAddressBook");

		boolean rtn = false;
		try {
			Session s = ExtLibUtil.getCurrentSessionAsSigner();
			// FacesContext context = FacesContext.getCurrentInstance();
			// Session s = (Session)
			// context.getApplication().getVariableResolver().resolveVariable(context,
			// "sessionAsSigner");

			ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String nabDbname = (String) configBean1
					.getValue("UserDominoDirectoryPath");
			// String nabDbname = new
			// String("Development\\External\\Cascade\\cascweb3final.nsf");
			// String nabDbname = getConfigValue("UserDominoDirectoryPath", s);
			debugMsg("nab db is " + nabDbname);
			if (s == null) {
				debugMsg("s is not valid");
			} else if (s.isValid() == false) {
				debugMsg("s is null");
			} else {
				debugMsg("s is good");
			}
			debugMsg("Session server is " + s.getServerName());

			Database nabDb = null;
			// String server = ExtLibUtil.getCurrentDatabase().getServer();
			nabDb = s.getDatabase(s.getServerName(), nabDbname, false);
			// debugMsg("is recycled is "+ isRecycled(nabDb, true));
			// debugMsg("try to get db" + nabDb.isOpen());
			if (nabDb != null) {
				debugMsg("Database is open and title is " + nabDb.getTitle());
				// get the view
				View nabVw = nabDb.getView("($LDAPCN)");
				debugMsg("view" + nabVw.getName());
				if (nabVw != null) {
					ViewEntryCollection colPeople = nabVw.getAllEntriesByKey(
							email, true);
					debugMsg("count is:" + colPeople.getCount());
					if (colPeople.getCount() > 0) {
						// if user was found return false
						debugMsg("User was found");
					} else {
						// user was NOT found, return true, ok to register
						debugMsg("User was NOT found");
						rtn = true;
					}
					// done with the view and database
					// nabVw.recycle();
					// nabDb.recycle();
				} else {
					debugMsg("nab view not open");
				}

			} else {
				rtn = false;
				debugMsg("Nab db not open");
				throw new RuntimeException(
						"Error in checkAddressBook: Nab DB Not Open");
			}
		} catch (Exception e) {
			rtn = false;
			debugMsg("error in checkAddressBook:dBar says " + e.toString());
			e.printStackTrace();
			throw new RuntimeException("Error in checkAddressBook: "
					+ e.toString());

		}
		return rtn;
	}

	public synchronized static boolean addUserToMultiGroup(String name,
			String group, Database nabDb, Session session) {
		// this adds the user to the group where the group has subgroups
		boolean saveGroupMainDoc = false;
		// boolean saveGroupSubDoc = false;
		boolean savedSubGroup = false;
		String subGroup = "";
		// boolean newGroup = false;
		boolean result = false;
		Document groupMainDoc;
		Item groupMainMembers;
		Item groupSubMembers;
		// Integer groupNum = 0;
		Document groupSubDoc;
		debugMsg("Starting addUserToMultiGroup");

		try {
			if (nabDb.isOpen()) {
				if (isPersonInGroup(nabDb, group, name, session)) {
					// already there
					debugMsg("Person is in group - addUserToMultiGroup method");
					result = true;
				} else {
					View groupVw = nabDb.getView("Groups");
					if (groupVw != null) {
						groupMainDoc = groupVw.getDocumentByKey(group, true);
						if (groupMainDoc == null) {
							// if main group doc does not exist create it,
							// not saved yet
							debugMsg("Creating main group doc");
							groupMainDoc = createGroupDoc(nabDb, group, group
									.concat(" 1"));
							// groupMainDoc.replaceItemValue("ListDescription",
							// "Do NOT edit this group manually, it is maintained by an agent.");
							subGroup = group.concat(" 1");
							// mark MainDoc to be saved
							saveGroupMainDoc = true;
						} else {
							debugMsg("Found main group");
						}
						// check for groupMainMembers and if null, create
						// empty item
						groupMainMembers = groupMainDoc.getFirstItem("members");
						if (groupMainMembers == null) {
							debugMsg("Main group members was null");
							// create the item
							groupMainDoc.replaceItemValue("Members", group
									.concat(" 1"));
							subGroup = group.concat(" 1");
							groupMainMembers = groupMainDoc
									.getFirstItem("members");
							saveGroupMainDoc = true;
						}
						// loop through groupMainMembers
						debugMsg("value is "
								+ groupMainMembers.getValueString());
						if (groupMainMembers.getValueString().isEmpty()) {
							// the member in the Main Group was blank, add
							// group 1
							debugMsg("Main Is empty");
							groupMainDoc.replaceItemValue("Members", group
									.concat(" 1"));
							subGroup = group.concat(" 1");
							groupMainMembers = groupMainDoc
									.getFirstItem("members");
							saveGroupMainDoc = true;

						}
						debugMsg("size is "
								+ groupMainMembers.getValues().size());
						if (subGroup != "") {
							// a subGroup has to be created and populated
							// and then we are done
							// create group doc and add user to it
							groupSubDoc = createGroupDoc(nabDb, subGroup, name);
							groupSubDoc.computeWithForm(false, false);
							result = groupSubDoc.save(true, true);
							debugMsg("saving groupSubDoc " + subGroup
									+ " result is " + result);
						} else {
							Object[] members;
							members = groupMainMembers.getValues().toArray();
							// go through array in reverse
							int i = members.length - 1;
							// save last entry in case a new group has to be
							// created
							int maxsize = members.length;
							debugMsg("last entry is " + maxsize);
							while (savedSubGroup == false && i >= 0) {

								// loop through each doc in reverse and find
								// first one with room
								debugMsg("sub group value is " + members[i]);
								// groupVw.refresh();
								groupSubDoc = groupVw.getDocumentByKey(
										members[i], true);
								if (groupSubDoc != null) {
									debugMsg("group found:"
											+ groupSubDoc
													.getItemValueString("ListName"));
									groupSubMembers = groupSubDoc
											.getFirstItem("members");
									debugMsg("length of field:"
											+ groupSubMembers.getValueLength());
									if (groupSubMembers.getValueLength() < 10000) {
										debugMsg("There is room - adding name to list: "
												+ name);
										groupSubMembers.appendToTextList(name);
										groupSubDoc.computeWithForm(false,
												false);
										savedSubGroup = groupSubDoc.save(true,
												true);
										result = true;
									}
								} else {
									// the subgroup was in Main but does not
									// exist, create and add user
									groupSubDoc = createGroupDoc(nabDb,
											members[i].toString(), name);
									groupSubDoc.computeWithForm(false, false);
									savedSubGroup = groupSubDoc
											.save(true, true);
									result = true;
								}
								i = i - 1;
								groupSubDoc.recycle();
							}
							if (savedSubGroup == false) {
								// check to see if a subGroup Doc was saved,
								// if not create a new one
								debugMsg("creating new sub group:"
										+ (maxsize + 1));
								groupSubDoc = createGroupDoc(nabDb, group
										.concat(" " + (maxsize + 1)), name);
								groupSubDoc.computeWithForm(false, false);
								result = groupSubDoc.save(true, true);
								// update main group doc
								groupMainMembers.appendToTextList(group
										.concat(" " + (maxsize + 1)));
								saveGroupMainDoc = true;
							}
						}
						if (saveGroupMainDoc) {
							groupMainDoc.computeWithForm(false, false);
							result = groupMainDoc.save(true, true);

						}
						groupMainDoc.recycle();
					}
				}

			}
			debugMsg("Ending addUserToMultiGroup");

		} catch (Exception e) {
			debugMsg("Error in AddUserToMultiGroup: " + e.toString());
			throw new RuntimeException("Error in AddUserToMultiGroup: "
					+ e.toString());

		}
		return result;
	}

	public synchronized static boolean addUserToGroup(String name,
			String group, Database dbNab, Session session) {
		// adds user to group, no subgroups in this case!
		// dbNab is the NAB database object
		debugMsg("Starting-addUserToGroup");
		Document groupDoc;
		Item groupMembers;
		boolean res = true;
		try {
			if (!isPersonInGroup(dbNab, group, name, session)) {
				// only add the user if they are not in the group already
				if (dbNab.isOpen()) {
					debugMsg("addUserToGroup - got database");
					View groupVw = dbNab.getView("Groups");
					if (groupVw != null) {
						debugMsg("addUserToGroup - Got view getting " + group
								+ " groupDoc");

						groupDoc = groupVw.getDocumentByKey(group, true);
						if (groupDoc == null) {
							// create new groupDoc and add user
							debugMsg("addUserToGroup - create new group");
							groupDoc = createGroupDoc(dbNab, group);
						}
						if (groupDoc.isValid()) {
							debugMsg("addUserToGroup - groupDoc.isValid");
							// add user to group
							groupMembers = groupDoc.getFirstItem("members");
							groupMembers.appendToTextList(name);
							// computewithform to clean it up and set flags on
							// fields
							groupDoc.computeWithForm(false, false);
							debugMsg("addUserToGroup - goupDoc " + group
									+ ".computingWithForm");
							groupDoc.save();
							groupDoc.recycle();
							groupVw.recycle();

						} else {
							// throw error, bad group doc
							res = false;
							throw new RuntimeException(
									"addUserToGroup:Group Doc is not valid");
						}
					} else {
						res = false;
						throw new RuntimeException(
								"addUserToGroup:Groups View is not valid");
					}
				}
			} else {
				// already in the group
				res = true;
				debugMsg("addUserToGroup - User " + name
						+ " is already in the group " + group);
			}
		} catch (Exception e) {
			res = false;
			debugMsg("Error in AddUserToGroup: " + e.toString());
			throw new RuntimeException("error in addUserToGroup: "
					+ e.toString());
		}
		return res;
	}

	public static boolean isPersonInGroup(Database nabDb, String groupName,
			String member, Session s) {
		/*
		 * check if a person is in the named group nabDb is the Domino Directory
		 * groupName is the name of the group to check member is the name to
		 * check
		 */
		boolean found = false;
		ViewEntry vwEntry;
		ViewEntry entryTemp;
		String foundGroup;

		try {
			View serverAccessVw = nabDb.getView("($ServerAccess)");
			if (serverAccessVw != null) {
				ViewEntryCollection col = serverAccessVw.getAllEntriesByKey(
						member, true);
				if (col.getCount() > 0) {
					// Person is in a group, check to see if right group
					vwEntry = col.getFirstEntry();
					while (vwEntry != null && found == false) {
						// loop through all entries to see if person is in group
						foundGroup = (String) vwEntry.getColumnValues().get(1);
						debugMsg("isPersonInGroup - Checking: " + foundGroup);
						if (foundGroup.toLowerCase().contains(
								groupName.toLowerCase())) {
							// found group
							found = true;
							debugMsg("Person is in group - isPersonInGroup "
									+ foundGroup);
						}

						entryTemp = col.getNextEntry(vwEntry);
						vwEntry.recycle();
						vwEntry = entryTemp;
						//	
					}
				} else {
					// Person not found in view, so not in any groups
					debugMsg("Not found in the group");

					found = false;
				}

			}

		} catch (Exception e) {
			debugMsg("Error in isPersonInGroup method" + e.toString());
			found = false;
			throw new RuntimeException("Error in isPersonInGroup method"
					+ e.toString());

		}
		debugMsg("isPersonInGroup - Returning " + found);
		return found;
	}

	public synchronized static void refreshLookupViews(String userName,
			Database nabDb, Session session) {
		// this routine will try to update the views so the user can login
		// quicker
		// session must be sessionAsSigner
		DirectoryNavigator dirNav;
		try {
			if (nabDb != null) {
				// Get NAB database
				View nabVw;

				// refresh the same views they did in the old system
				ArrayList<String> vwNames = new ArrayList<String>();
				vwNames.add("($LDAPCN)");
				vwNames.add("($Users)");
				vwNames.add("($ServerAccess)");
				for (String vwName : vwNames) {
					nabVw = nabDb.getView(vwName);
					nabVw.refresh();
					nabVw.recycle();
					debugMsg("refreshing view:" + vwName);
				}
				Directory dirObj = session.getDirectory(null);

				dirNav = dirObj.lookupNames("($LDAPCN)", userName, "FirstName");
				debugMsg("name located" + dirNav.isNameLocated());
				dirObj.freeLookupBuffer();
				dirNav.recycle();
				dirNav = dirObj.lookupNames("($Users)", userName, "ListName");
				dirNav.findFirstName();
				// debugMsg(dirNav.isNameLocated());
				dirObj.freeLookupBuffer();
				dirNav.recycle();
				dirObj.recycle();

			} else {
				throw new RuntimeException(
						"refreshLookupViews:NAB Database not valid");
			}
		} catch (Exception e) {
			DebugToolbarBean.get().error(
					"Error in RefreshLookupViews: " + e.toString());
		}
	}

	private static Document createGroupDoc(Database db, String groupName) {
		// subroutine to create new group doc
		debugMsg("Creating group doc");
		Document groupDoc = null;
		try {
			debugMsg("Creating new document:" + groupName);
			groupDoc = db.createDocument();
			groupDoc.replaceItemValue("Form", "Group");
			groupDoc.replaceItemValue("ListName", groupName);
			groupDoc.replaceItemValue("Members", "");
			groupDoc.replaceItemValue("GroupType", "2");
			groupDoc
					.replaceItemValue("ListDescription",
							"Do NOT edit this group manually, it is maintained by an agent.");

		} catch (Exception e) {
			debugMsg("Error in createGroupDoc");
			throw new RuntimeException("Error in createGroupDoc" + e.toString());
		}
		return groupDoc;

	}

	private static Document createGroupDoc(Database db, String groupName,
			String name) {
		// subroutine to create new group doc
		debugMsg("Creating group doc-adds user at same time");
		Document groupDoc = null;
		try {
			debugMsg("Creating new group doc-createGroupDoc" + groupName);
			groupDoc = db.createDocument();
			groupDoc.replaceItemValue("Form", "Group");
			groupDoc.replaceItemValue("ListName", groupName);
			groupDoc.replaceItemValue("Members", name);
			groupDoc.replaceItemValue("GroupType", "0");
			groupDoc
					.replaceItemValue("ListDescription",
							"Do NOT edit this group manually, it is maintained by an agent.");

		} catch (Exception e) {
			debugMsg("Error in createGroupDoc(adds user at same time)");
			throw new RuntimeException("Error in createGroupDoc" + e.toString());
		}
		return groupDoc;

	}

	public synchronized static boolean sendRegAfterActivateEmail(
			String firstName, String userName, String lang, Session session) {
		// this is the routine that sends out an email to the user after their
		// account has been created in the Domino Directory
		// Session MUST be set to SessionAsASigner!!!
		boolean res = false;
		try {
			// check language and default to americas-en if needed
			if (StringUtil.isEmpty(lang)) {
				lang = "americas-en";
			}
			// get language specific strings for email
			LangBean langBean = (LangBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "langBean");
			String greetingStr = langBean.getLangString(
					"AfterActivateEmail-Greeting", lang);
			String subjectStr = langBean.getLangString(
					"AfterActivateEmail-Subject", lang);
			String firstLine_1Str = langBean.getLangString(
					"AfterActivateEmail-1stline-1", lang);
			String firstLine_2Str = langBean.getLangString(
					"AfterActivateEmail-1stline-2", lang);

			String closingStr = langBean.getLangString(
					"AfterActivateEmail-closing", lang);
			// test output
			debugMsg("Found lang value:" + greetingStr);
			// get config values
			ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String siteName = configBean.getValue("SiteName").toString();
			String returnAddress = configBean.getValue("EmailReturnAddress")
					.toString();

			String hostName = configBean.getValue("HostURL").toString();
			String regDbName = configBean.getValue("RegistrationDbPath")
					.toString();

			// test output
			debugMsg("Found SiteName:" + siteName);

			// create new document for email (not to be saved)

			Database regDb = session.getDatabase(session.getServerName(),
					regDbName);
			if (regDb.isOpen()) {
				// create new temp doc for email
				Document mailDoc = regDb.createDocument();
				debugMsg("mailDoc" + mailDoc.getCreated());

				Item sendTo = mailDoc.replaceItemValue("SendTo", userName);
				sendTo.setSummary(true);
				mailDoc
						.replaceItemValue("Subject", siteName + " "
								+ subjectStr);
				mailDoc.replaceItemValue("Principal", returnAddress);
				mailDoc.replaceItemValue("Form", "Memo");
				mailDoc.replaceItemValue("from", returnAddress);
				mailDoc.replaceItemValue("inetfrom", returnAddress);

				RichTextItem bodyField = mailDoc.createRichTextItem("Body");
				bodyField.appendText(greetingStr + " " + firstName + ":");
				bodyField.addNewLine(2);
				bodyField.appendText(firstLine_1Str + " " + siteName + " "
						+ firstLine_2Str + ".");
				bodyField.addNewLine(2);
				bodyField.appendText(closingStr + ".");
				bodyField.addNewLine(2);
				bodyField.appendText(siteName);
				bodyField.addNewLine(1);
				bodyField.appendText(hostName);
				mailDoc.send(false);
				res = true;
			} else {
				debugMsg("Error in sendRegAfterActivateEmail:Database Not Valid");
				throw new RuntimeException(
						"sendPwAfterActivateEmail: RegDb is not open");
			}
		} catch (Exception e) {
			debugMsg("Error in sendRegAfterActivateEmail" + e.toString());
			e.printStackTrace();
		}

		return res;
	}

	public synchronized static boolean sendafterPwResetEmail(String userName,
			String lang, Session session) {
		// this sends out an email after the user resets their password
		// Session MUST be set to SessionAsASigner!!!
		boolean res = false;
		String firstName;
		try {
			// check language and default to americas-en if needed
			if (StringUtil.isEmpty(lang)) {
				lang = "americas-en";
			}
			// get language specific strings for email
			LangBean langBean = (LangBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "langBean");

			Document personDoc = getPersonDoc(userName, session);
			firstName = personDoc.getItemValueString("firstname");
			String greetingStr = langBean.getLangString(
					"AfterPWChangeEmail-Greeting", lang);
			String subjectStr = langBean.getLangString(
					"AfterPWChangeEmail-Subject", lang);
			String firstLine_1Str = langBean.getLangString(
					"AfterPWChangeEmail-1stline-1", lang);
			String firstLine_2Str = langBean.getLangString(
					"AfterPWChangeEmail-1stline-2", lang);

			String closingStr = langBean.getLangString(
					"AfterPWChangeEmail-closing", lang);
			// test output
			debugMsg("Found lang value:" + greetingStr);
			// get config values
			ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String siteName = configBean.getValue("SiteName").toString();
			String returnAddress = configBean.getValue("EmailReturnAddress")
					.toString();

			String hostName = configBean.getValue("HostURL").toString();
			String regDbName = configBean.getValue("RegistrationDbPath")
					.toString();

			// test output
			debugMsg("Found SiteName:" + siteName);

			// create new document for email (not to be saved)

			Database regDb = session.getDatabase(session.getServerName(),
					regDbName);
			if (regDb.isOpen()) {
				// create new temp doc for email
				Document mailDoc = regDb.createDocument();
				debugMsg("mailDoc" + mailDoc.getCreated());

				Item sendTo = mailDoc.replaceItemValue("SendTo", userName);
				sendTo.setSummary(true);
				mailDoc
						.replaceItemValue("Subject", siteName + " "
								+ subjectStr);
				mailDoc.replaceItemValue("Principal", returnAddress);
				mailDoc.replaceItemValue("Form", "Memo");
				mailDoc.replaceItemValue("from", returnAddress);
				mailDoc.replaceItemValue("inetfrom", returnAddress);

				RichTextItem bodyField = mailDoc.createRichTextItem("Body");
				bodyField.appendText(greetingStr + " " + firstName + ":");
				bodyField.addNewLine(2);
				bodyField.appendText(firstLine_1Str + " " + siteName + " "
						+ firstLine_2Str + ".");
				bodyField.addNewLine(2);
				bodyField.appendText(closingStr + ".");
				bodyField.addNewLine(2);
				bodyField.appendText(siteName);
				bodyField.addNewLine(1);
				bodyField.appendText(hostName);
				mailDoc.send(false);
				res = true;
			} else {
				debugMsg("Error in afterPWResetEmail:Database Not Valid");
				throw new RuntimeException(
						"Error in afterPWResetEmail: RegDb is not open");
			}
		} catch (Exception e) {
			debugMsg("Error in afterPWResetEmail" + e.toString());
			e.printStackTrace();
		}

		return res;
	}

	public static void testAfterUpdateEmail(Session session) {
		// this is used to test sending emails to shortcut the process
		// only used in dev/testing
		String firstName = new String("Howard");
		String userName = new String("howardg@tlcc.com");
		String lang = new String("eme-en");
		ArrayList<FieldData> changedFields = new ArrayList<FieldData>();
		FieldData fd = new FieldData();
		fd.labelName = "LabelPassword";
		fd.newValue = "my password";
		changedFields.add(fd);
		fd = new FieldData();
		fd.labelName = "LabelFirstName";
		fd.newValue = "new first name";
		changedFields.add(fd);
		fd = new FieldData();
		fd.labelName = "LabelLastName";
		fd.newValue = "new last name";
		changedFields.add(fd);
		fd = new FieldData();
		fd.labelName = "LabelPrintOptOut";
		fd.newValue = "";
		changedFields.add(fd);

		sendafterRegUpdateEmail(firstName, userName, lang, changedFields,
				session);
	}

	public synchronized static boolean sendafterRegUpdateEmail(
			String firstName, String userName, String lang,
			ArrayList<FieldData> changedFields, Session session) {
		// sends out email to user after they have changed their reg.
		// information
		// this shows them what fields they have changed

		// Session MUST be set to SessionAsASigner!!!
		boolean res = false;
		String label;
		String value;
		try {
			// check language and default to americas-en if needed
			if (StringUtil.isEmpty(lang)) {
				lang = "americas-en";
			}
			// get language specific strings for email
			LangBean langBean = (LangBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "langBean");

			Document personDoc = getPersonDoc(userName, session);
			firstName = personDoc.getItemValueString("firstname");
			String greetingStr = langBean.getLangString(
					"AfterUpdateEmail-Greeting", lang);
			String subjectStr = langBean.getLangString(
					"AfterUpdateEmail-Subject", lang);
			String firstLine_1Str = langBean.getLangString(
					"AfterUpdateEmail-1stline-1", lang);
			String firstLine_2Str = langBean.getLangString(
					"AfterUpdateEmail-1stline-2", lang);
			String fieldUpdatedLine = langBean.getLangString(
					"AfterUpdateEmailFieldsChanged", lang);
			String closingStr = langBean.getLangString(
					"AfterUpdateEmail-closing", lang);
			// test output
			debugMsg("Found lang value:" + greetingStr);
			// get config values
			ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String siteName = configBean.getValue("SiteName").toString();
			String returnAddress = configBean.getValue("EmailReturnAddress")
					.toString();

			String hostName = configBean.getValue("HostURL").toString();
			String regDbName = configBean.getValue("RegistrationDbPath")
					.toString();
			String webSiteUrl = configBean.getValue("WebsiteURL").toString();
			String updateLine = langBean.getLangString(
					"AfterUpdateEmail-UpdateLine", lang);

			// test output
			debugMsg("Found SiteName:" + siteName);

			// create new document for email (not to be saved)

			Database regDb = session.getDatabase(session.getServerName(),
					regDbName);
			if (regDb.isOpen()) {
				// create new temp doc for email
				Document mailDoc = regDb.createDocument();
				debugMsg("mailDoc" + mailDoc.getCreated());

				Item sendTo = mailDoc.replaceItemValue("SendTo", userName);
				sendTo.setSummary(true);
				mailDoc
						.replaceItemValue("Subject", siteName + " "
								+ subjectStr);
				mailDoc.replaceItemValue("Principal", returnAddress);
				mailDoc.replaceItemValue("Form", "Memo");
				mailDoc.replaceItemValue("from", returnAddress);
				mailDoc.replaceItemValue("inetfrom", returnAddress);

				RichTextItem bodyField = mailDoc.createRichTextItem("Body");
				bodyField.appendText(greetingStr + " " + firstName + ":");
				bodyField.addNewLine(2);
				bodyField.appendText(firstLine_1Str + " " + siteName + " "
						+ firstLine_2Str + ".");
				bodyField.addNewLine(2);
				bodyField.appendText(fieldUpdatedLine.concat(":"));
				bodyField.addNewLine(1);
				// loop through the changed fields, but don't show the password!
				for (FieldData cf : changedFields) {
					// lookup label
					label = langBean.getLangString(cf.labelName, lang);
					value = cf.newValue;
					if (label.indexOf("|") > 0) {
						bodyField.appendText(label.substring(0, label
								.lastIndexOf("|")));
					} else {
						bodyField.appendText(label);
					}
					bodyField.appendText(":  ");
					if (!StringUtil.equals(cf.labelName, new String(
							"LabelPassword"))) {
						// don't send out the new password in an email
						// remove any characters from the vertical bar to the
						// right
						bodyField.appendText(value);
					} else {
						// for the password just show the label noted below
						bodyField.appendText(langBean.getLangString(
								"AfterUpdateEmailPwChanged", lang));
					}
					bodyField.addNewLine(1);
				}
				bodyField.addNewLine(1);
				bodyField.appendText(updateLine.concat(":"));
				bodyField.addNewLine(1);
				bodyField.appendText(hostName + webSiteUrl
						+ "/updateRegistration.xsp");
				bodyField.addNewLine(2);
				bodyField.appendText(closingStr + ".");
				bodyField.addNewLine(2);
				bodyField.appendText(siteName);
				bodyField.addNewLine(1);
				bodyField.appendText(hostName);
				mailDoc.send(false);
				res = true;
			} else {
				debugMsg("Error in sendafterRegUpdateEmail:Database Not Valid");
				throw new RuntimeException(
						"Error in afterPWResetEmail: RegDb is not open");
			}
		} catch (Exception e) {
			debugMsg("Error in sendafterRegUpdateEmail" + e.toString());
			e.printStackTrace();
		}

		return res;
	}


	public synchronized static boolean sendRegActivateEmail(String unid,
			String firstName, String userName, String lang, Session session) {
		// This method sends out the email after the user completes the
		// ccPhase2NewRegistration

		// Session MUST be set to SessionAsASigner!!!
		boolean res = false;
		try {
			// check language and default to americas-en if needed
			if (StringUtil.isEmpty(lang)) {
				lang = "americas-en";
			}
			// get language specific strings for email
			LangBean langBean = (LangBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "langBean");
			String greetingStr = langBean.getLangString("NewRegEmail-Greeting",
					lang);
			String subjectStr = langBean.getLangString("NewRegEmail-subject",
					lang);
			String firstLine_1Str = langBean.getLangString(
					"NewRegEmail-1stline-1", lang);
			String firstLine_2Str = langBean.getLangString(
					"NewRegEmail-1stline-2", lang);
			String secondLineStr = langBean.getLangString(
					"NewRegEmail-2ndline", lang);
			String thirdLineStr = langBean.getLangString("NewRegEmail-3rdline",
					lang);
			String closingStr = langBean.getLangString("NewRegEmail-closing",
					lang);
			// test output
			debugMsg("Found lang value:" + greetingStr);
			// get config values
			ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String siteName = configBean.getValue("SiteName").toString();
			String returnAddress = configBean.getValue("EmailReturnAddress")
					.toString();
			String regHomePage = configBean.getValue("RegistrationHomeXPage")
					.toString();
			String hostName = configBean.getValue("HostURL").toString();
			String regDbName = configBean.getValue("RegistrationDbPath")
					.toString();
			String activationXPageName = configBean.getValue("ActivationXPage")
					.toString();
			String webSiteUrl = configBean.getValue("WebsiteURL").toString();
			// test output
			debugMsg("Found SiteName:" + siteName);

			// create new document for email (not to be saved)

			Database regDb = session.getDatabase(session.getServerName(),
					regDbName);
			if (regDb.isOpen()) {
				// create new temp doc for email
				Document mailDoc = regDb.createDocument();
				debugMsg("mailDoc" + mailDoc.getCreated());

				Item sendTo = mailDoc.replaceItemValue("SendTo", userName);
				sendTo.setSummary(true);
				mailDoc
						.replaceItemValue("Subject", siteName + " "
								+ subjectStr);
				mailDoc.replaceItemValue("Principal", returnAddress);
				mailDoc.replaceItemValue("Form", "Memo");
				mailDoc.replaceItemValue("from", returnAddress);
				mailDoc.replaceItemValue("inetfrom", returnAddress);

				RichTextItem bodyField = mailDoc.createRichTextItem("Body");
				bodyField.appendText(greetingStr + " " + firstName + ":");
				bodyField.addNewLine(2);
				bodyField.appendText(firstLine_1Str + " " + siteName + " "
						+ firstLine_2Str + ".");
				bodyField.addNewLine(1);
				bodyField.appendText(hostName + webSiteUrl + "/"
						+ activationXPageName + "?OpenXPage&unid=" + unid);
				bodyField.addNewLine(2);
				bodyField.appendText(secondLineStr + ": " + userName + ".");
				bodyField.addNewLine(2);
				bodyField.appendText(thirdLineStr + ":");
				bodyField.addNewLine(1);
				bodyField.appendText(hostName + webSiteUrl + "/" + regHomePage);
				bodyField.addNewLine(2);
				bodyField.appendText(closingStr);

				mailDoc.send(false);
				res = true;

			} else {
				debugMsg("Error in sendRegActivateEmail:Database Not Valid");
				throw new RuntimeException(
						"Error in sendRegActivateEmail: RegDb is not open");
			}
		} catch (Exception e) {
			debugMsg("Error in sendRegActivateEmail" + e.toString());
			e.printStackTrace();
		}

		return res;
	}

	// WSCR-B4BSU9 - Web Content Change - Email notice for new Accounts
	// 10-05-2018 mdz
	// add new function to send email to webmaster when a new registration is
	// submitted
	@SuppressWarnings("unchecked")
	public synchronized static boolean sendRegNotifyEmail(Database regDb,
			Document regDoc) {
		boolean res = true;
		try {
			if (regDb.isOpen()) {
				// get config values
				ConfigBean configBean = (ConfigBean) ExtLibUtil
						.resolveVariable(FacesContext.getCurrentInstance(),
								"configBean");
				ArrayList<String> validNotifyCodes = (ArrayList<String>) configBean
						.getValue("ValidNotifyCodes");
				debugMsg("ValidNotifyCodes>>> " + validNotifyCodes.toString());
				String accessType = regDoc.getItemValueString("accessType");
				if (validNotifyCodes.contains(accessType)) {
					String userName = regDoc.getItemValueString("fullName");
					String lastName = regDoc.getItemValueString("lastName");
					String firstName = regDoc.getItemValueString("firstName");
					String acctNbr = regDoc.getItemValueString("accountNumber");
					String companyName = regDoc.getItemValueString("cName");
					String returnAddress = configBean.getValue(
							"EmailReturnAddress").toString();
					debugMsg("userName:  " + userName);
					debugMsg("accessType:  " + accessType);
					debugMsg("firstName:  " + firstName);
					debugMsg("lastName:  " + lastName);
					debugMsg("acctNbr:  " + acctNbr);
					debugMsg("companyName:  " + companyName);
					String msg = firstName + " " + lastName
							+ " just registered as " + userName
							+ ".  AccountNbr: " + acctNbr + " | CompanyName: "
							+ companyName + " | AccessType: " + accessType;
					debugMsg(msg);
					// create new temp doc for email
					Document mailDoc = regDb.createDocument();
					debugMsg("mailDoc" + mailDoc.getCreated());
					Item sendTo = mailDoc.replaceItemValue("SendTo",
							"webmaster@cascorp.com");
					// Item sendTo = mailDoc.replaceItemValue("SendTo",
					// "michael.zens@cascorp.com");
					sendTo.setSummary(true);
					mailDoc.replaceItemValue("Subject",
							"New Website Registrant");
					mailDoc.replaceItemValue("Principal", returnAddress);
					mailDoc.replaceItemValue("Form", "Memo");
					mailDoc.replaceItemValue("from", returnAddress);
					mailDoc.replaceItemValue("inetfrom", returnAddress);

					RichTextItem bodyField = mailDoc.createRichTextItem("Body");
					bodyField.appendText(msg);
					mailDoc.send(false);
				}
			}
		} catch (Exception e) {
			debugMsg("Error in sendRegNotifyEmail" + e.toString());
			e.printStackTrace();
		}
		return res;
	}

	// END OF CODE ADDED FOR WSCR-B4BSU9 - Web Content Change - Email notice for
	// new Accounts - mdz

	synchronized public static boolean createPersonInNab(Document doc,
			Database dbNab) {
		// creates person doc in NAB
		// doc is the registration document
		// dbNav is the name and address book, must be gotten via
		// sessionAsSigner!!!
		boolean res = true;
		try {
			if (doc.isValid()) {
				if (dbNab.isOpen()) {
					Document docPerson = dbNab.createDocument();

					docPerson.replaceItemValue("form", "Person");
					docPerson.replaceItemValue("Type", "Person");
					docPerson.replaceItemValue("LastName", doc
							.getItemValueString("LastName"));
					docPerson.replaceItemValue("FirstName", doc
							.getItemValueString("FirstName"));
					String fullname = doc.getItemValueString("FullName");
					docPerson.replaceItemValue("FullName", fullname);

					docPerson.replaceItemValue("InternetAddress", fullname);

					if (StringUtil.indexOfIgnoreCase(fullname, "cascorp.com") >= 0) {
						docPerson.replaceItemValue("MailDomain", "EAGLE");
					}

					// Only way to get username with @ to show up in ($LDAPCN)
					// view
					docPerson.replaceItemValue("CommonName", doc
							.getItemValue("FullName"));

					docPerson.replaceItemValue("ChallengeQuestion", doc
							.getItemValueString("ChallengeQuestion"));
					docPerson.replaceItemValue("ChallengeAnswer", doc
							.getItemValueString("ChallengeAnswer"));
					docPerson.replaceItemValue("HTTPPassword", doc
							.getItemValueString("NewPassword"));
					docPerson.replaceItemValue("MktgOptOut", doc
							.getItemValueString("MktgOptOut"));
					docPerson.replaceItemValue("EmployeeID", doc
							.getItemValue("accountnumber"));
					docPerson.replaceItemValue("Location", doc
							.getItemValueString("region"));

					// company data
					docPerson.replaceItemValue("CompanyName", doc
							.getItemValueString("cName"));
					docPerson.replaceItemValue("JobTitle", doc
							.getItemValueString("JobTitle"));
					docPerson.replaceItemValue("JobFunctions", doc
							.getItemValueString("JobFunctions"));
					docPerson.replaceItemValue("OfficeFaxPhoneNumber", doc
							.getItemValueString("WorkFax"));
					docPerson.replaceItemValue("CellPhoneNumber", doc
							.getItemValueString("CellPhone"));

					// personal data
					docPerson.replaceItemValue("StreetAddress", doc
							.getItemValueString("address1"));
					docPerson.replaceItemValue("StreetAddress2", doc
							.getItemValueString("address2"));
					docPerson.replaceItemValue("StreetAddress3", doc
							.getItemValueString("address3"));
					docPerson.replaceItemValue("City", doc
							.getItemValueString("City"));
					docPerson.replaceItemValue("State", doc
							.getItemValueString("State"));
					docPerson.replaceItemValue("Country", doc
							.getItemValueString("Country"));
					docPerson.replaceItemValue("Zip", doc
							.getItemValueString("Zip"));
					docPerson.replaceItemValue("OfficePhoneNumber", doc
							.getItemValue("WorkPhone"));

					// office data From JDE
					docPerson.replaceItemValue("OfficeStreetAddress", doc
							.getItemValueString("cAddress"));
					docPerson.replaceItemValue("OfficeStreetAddress2", doc
							.getItemValueString("cAddress2"));
					docPerson.replaceItemValue("OfficeStreetAddress3", doc
							.getItemValueString("cAddress3"));
					docPerson.replaceItemValue("OfficeCity", doc
							.getItemValueString("cCity"));
					docPerson.replaceItemValue("OfficeState", doc
							.getItemValueString("cState"));
					docPerson.replaceItemValue("OfficeZip", doc
							.getItemValueString("cZip"));
					docPerson.replaceItemValue("OfficeCountry", doc
							.getItemValueString("cCountry"));
					docPerson.replaceItemValue("AccessType", doc
							.getItemValueString("AccessType")); // added
					// 04/04/2017
					// (WSCR-ALLTK4)
					// to record
					// accountType
					// in person
					// document

					// Other Info
					docPerson.replaceItemValue("PreferredLanguage", doc
							.getItemValueString("PreferredLanguage"));
					docPerson.replaceItemValue("MHP", doc
							.getItemValueString("MHP"));
					docPerson.replaceItemValue("ALC", doc
							.getItemValueString("ALC"));
					docPerson.replaceItemValue("Forks", doc
							.getItemValueString("Forks"));
					docPerson.replaceItemValue("CEP", doc
							.getItemValueString("CEP"));
					docPerson.replaceItemValue("TimeZone", doc
							.getItemValueString("TimeZone"));
					docPerson.replaceItemValue("MailLiterature", doc
							.getItemValueString("MailLiterature"));
					docPerson.replaceItemValue("AcceptResponsibility", doc
							.getItemValueString("AcceptResponsibility"));

					// 2-21-19 DSR Added support for user types of End user and OEM
					docPerson.replaceItemValue("AccessType", doc
							.getItemValueString("regtype").toUpperCase());
					
					// Update all form formulas
					docPerson.computeWithForm(false, false);
					res = docPerson.save(false, true);
				} else {
					res = false;
					throw new RuntimeException(
							"Error in createPersonInNab: Database not open");
				}
			} else {
				res = false;
				throw new RuntimeException(
						"Error in createPersonInNab: Document is Not Valid");
			}
		} catch (Exception e) {
			res = false;
			throw new RuntimeException("Error in createPersonInNab: "
					+ e.getMessage());
		}

		return res;
	}

	@SuppressWarnings("unchecked")
	public static String findOEMGroupName(String acctNumber) {
		// gets the OEMgroup from the config values
		String rtn = new String("");
		ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		ArrayList<String> OEMAcctArray = (ArrayList<String>) configBean1
				.getValue("OEMAccountNumbers");
		HashMap<String, String> OEMAcctMap = new HashMap<String, String>();
		String key = new String();
		String groupName = new String();
		// load up a map
		debugMsg("OEMAcctArray.size():  " + OEMAcctArray.size());
		debugMsg("^^^key|groupName^^^");
		if (OEMAcctArray.size() > 0) {
			for (String member : OEMAcctArray) {
				key = StringUtil.trim(member.substring(0, member.indexOf("|")));
				groupName = StringUtil.trim(member.substring(member
						.lastIndexOf('|') + 1));
				debugMsg(key + "|" + groupName);
				// debugMsg("alias is " + groupName);
				OEMAcctMap.put(key, groupName);
			}
		}
		if (OEMAcctMap.containsKey(acctNumber)) {
			rtn = OEMAcctMap.get(acctNumber);
		}
		debugMsg("found value: " + rtn);
		return rtn;
	}

	// added 08/05/2016 mdz - check for group that is not in OEMAccountNumbers
	@SuppressWarnings("unchecked")
	public static String findOEMDenyName(String acctNumber) {
		String rtn = new String("");
		// debugMsg("entering findOEMDenyName");
		ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		ArrayList<String> OEMDenyAcctArray = (ArrayList<String>) configBean1
				.getValue("OEMGeneralDeny");
		HashMap<String, String> OEMAcctMap = new HashMap<String, String>();
		String key = new String();
		String groupName = new String();
		// load up a map
		if (OEMDenyAcctArray.size() > 0) {
			for (String member : OEMDenyAcctArray) {
				key = StringUtil.trim(member.substring(0, member.indexOf("|")));
				groupName = StringUtil.trim(member.substring(member
						.lastIndexOf('|') + 1));
				debugMsg("key is " + key);
				debugMsg("alias is " + groupName);
				OEMAcctMap.put(key, groupName);
			}
		}
		if (OEMAcctMap.containsKey(acctNumber)) {
			rtn = OEMAcctMap.get(acctNumber);
		}
		debugMsg("found value: " + rtn);
		return rtn;
	}

	// end of code added 08-05-16 mdz
	public static Map<String, String> findUserName(String username,
			Session session) {
		// this is used to find the user's name and then load up the challenges
		// used to reset the password, see the passwordReset XPage
		debugMsg("Starting findUserName");
		Map<String, String> userInfo = new HashMap<String, String>();
		try {
			Document personDoc = getPersonDoc(username, session);
			// Document personDoc = null;
			if (personDoc != null) {
				userInfo.put("status", "usernamefound");
				userInfo.put("userName", username);
				userInfo.put("firstName", personDoc
						.getItemValueString("firstname"));
				userInfo.put("lastName", personDoc
						.getItemValueString("lastname"));
				userInfo.put("challengeQuestion", personDoc
						.getItemValueString("ChallengeQuestion"));
				userInfo.put("challengeAnswer", personDoc
						.getItemValueString("ChallengeAnswer"));
			} else {
				// username not found
				userInfo.put("status", "usernamenotfound");
			}

		} catch (Exception e) {
			debugMsg("error in findUserName:dBar says " + e.toString());

			// throw new RuntimeException("Error in findUserName: " +
			// e.toString());
		}

		return userInfo;
	}

	private static Document getPersonDoc(String username, Session session) {
		Document personDoc = null;
		// System.out.println("entering getPersonDoc - username:  " + username);
		try {

			ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String nabDbname = (String) configBean1
					.getValue("UserDominoDirectoryPath");
			Database nabDb = null;
			nabDb = session.getDatabase(session.getServerName(), nabDbname,
					false);
			if (nabDb != null) {
				// System.out.println("Database is open and title is " +
				// nabDb.getTitle());
				debugMsg("Database is open and title is " + nabDb.getTitle());
				// get the view
				View nabVw = nabDb.getView("($LDAPCN)");
				debugMsg("view" + nabVw.getName());
				if (nabVw != null) {
					personDoc = nabVw.getDocumentByKey(username, true);
					if (personDoc != null) {
						// System.out.println("Person doc found for " +
						// username);
						debugMsg("Found person doc for " + username);
					} else {
						// person not found
						System.out.println("Person doc not found for "
								+ username);
						debugMsg("Person doc not found for " + username);
						personDoc = null;
					}
				}
			}
		} catch (Exception e) {
			personDoc = null;
			System.out.println("error in getPersonDoc:dBar says "
					+ e.toString());
			debugMsg("error in getPersonDoc:dBar says " + e.toString());

			// throw new RuntimeException("Error in getPersonDoc: " +
			// e.toString());
		}
		return personDoc;
	}

	public static String changePassword(String username, String newPassword,
			Session session) {
		// from the passwordReset XPage if the user passes the challenges they
		// can enter
		// a new password. This does the updating
		String rtn = "";
		debugMsg("Starting changePassword");
		debugMsg("New password is " + newPassword);
		try {
			// get the NAB DB
			ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String nabDbname = (String) configBean1
					.getValue("UserDominoDirectoryPath");
			debugMsg(nabDbname);
			Database nabDb = session.getDatabase(session.getServerName(),
					nabDbname, false);
			if (nabDb != null) {
				// get the person doc from the name and address book
				Document personDoc = getPersonDoc(username, session);
				if (personDoc.isValid()) {
					// got a valid personDoc
					debugMsg("person doc is " + personDoc.getCreated());
					personDoc.replaceItemValue("HTTPPassword", newPassword);
					DateTime dt = session.createDateTime("Today");
					dt.setNow();
					personDoc.replaceItemValue("HTTPPasswordChangeDate", dt);
					// refresh personDoc fields
					personDoc.computeWithForm(false, false);
					// save personDoc
					boolean saved = personDoc.save(false, false);
					if (saved) {
						// refresh the views so the user can login quicker
						refreshLookupViews(username, nabDb, session);
						// return string to indicate all is ok
						rtn = "passwordchanged";
					}
				} else {
					debugMsg("Person doc is not valid");
					rtn = "";
				}
			} else {
				// nab db is bad
				debugMsg("NAB DB is null");
				rtn = "";
			}

		} catch (Exception e) {
			rtn = "badpersondoc";
			debugMsg("error in changePassword:dBar says " + e.toString());
		}

		return rtn;
	}

	public static String changePasswordChallenge(String username,
			String newPassword, String newQuestion, String newAnswer,
			Session session) {
		// from the passwordReset XPage if the user passes the challenges they
		// can enter
		// a new password. This does the updating
		String rtn = "";
		debugMsg("Starting changePassword");
		debugMsg("New password is " + newPassword);
		try {
			// get the NAB DB
			ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String nabDbname = (String) configBean1
					.getValue("UserDominoDirectoryPath");
			debugMsg(nabDbname);
			Database nabDb = session.getDatabase(session.getServerName(),
					nabDbname, false);
			if (nabDb != null) {
				// get the person doc from the name and address book
				Document personDoc = getPersonDoc(username, session);
				if (personDoc.isValid()) {
					// got a valid personDoc
					debugMsg("person doc is " + personDoc.getCreated());
					personDoc.replaceItemValue("HTTPPassword", newPassword);
					personDoc
							.replaceItemValue("ChallengeQuestion", newQuestion);
					personDoc.replaceItemValue("ChallengeAnswer", newAnswer);
					personDoc.replaceItemValue("HTTPPasswordForceChange", "");
					DateTime dt = session.createDateTime("Today");
					dt.setNow();
					personDoc.replaceItemValue("HTTPPasswordChangeDate", dt);
					// refresh personDoc fields
					personDoc.computeWithForm(false, false);
					// save personDoc
					boolean saved = personDoc.save(false, false);
					if (saved) {
						// refresh the views so the user can login quicker
						refreshLookupViews(username, nabDb, session);
						// return string to indicate all is ok
						rtn = "passwordchanged";
					}
				} else {
					debugMsg("Person doc is not valid");
					rtn = "";
				}
			} else {
				// nab db is bad
				debugMsg("NAB DB is null");
				rtn = "";
			}

		} catch (Exception e) {
			rtn = "badpersondoc";
			debugMsg("error in changePassword:dBar says " + e.toString());
		}

		return rtn;
	}

	protected static Document getCurrentDocument() throws NotesException {
		DominoDocument dominoDocument = (DominoDocument) ExtLibUtil
				.resolveVariable(FacesContext.getCurrentInstance(),
						"currentDocument");
		if (dominoDocument != null) {
			return dominoDocument.getDocument(true);
		}
		return null;
	}

	protected static DominoDocument getCurrentXSPDocument()
			throws NotesException {
		DominoDocument dominoDocument = (DominoDocument) ExtLibUtil
				.resolveVariable(FacesContext.getCurrentInstance(), "regDoc");
		if (dominoDocument != null) {
			return dominoDocument;
		}
		return null;
	}

	private static LinkedHashMap<String, FieldData> getFieldData() {
		// the key is the name of the field in the regDoc
		// the FieldData map holds the name of the field in the nabDoc and the
		// label name
		// from the setup view
		LinkedHashMap<String, FieldData> rtnMap = new LinkedHashMap<String, FieldData>();
		// setup the map for each set of fields
		rtnMap.put("email", generateFieldData("fullname", "LabelUserName"));
		rtnMap.put("FirstName",
				generateFieldData("FirstName", "LabelFirstName"));
		rtnMap.put("LastName", generateFieldData("LastName", "LabelLastName"));
		rtnMap.put("JobTitle", generateFieldData("JobTitle", "LabelJobTitle"));
		rtnMap.put("JobFunctions", generateFieldData("JobFunctions",
				"LabelJobFunction"));
		rtnMap.put("WorkPhone", generateFieldData("OfficePhoneNumber",
				"LabelWorkPhone"));
		rtnMap.put("WorkFax", generateFieldData("OfficeFaxPhoneNumber",
				"LabelWorkFax"));
		rtnMap.put("CellPhone", generateFieldData("CellPhoneNumber",
				"LabelCellPhone"));
		rtnMap.put("address1", generateFieldData("StreetAddress",
				"LabelStreetAddress"));
		rtnMap.put("address2", generateFieldData("StreetAddress2",
				"LabelAddressCont"));
		rtnMap.put("address3", generateFieldData("StreetAddress3",
				"LabelAddressCont"));
		rtnMap.put("City", generateFieldData("City", "labelCity"));
		rtnMap.put("State", generateFieldData("State", "labelState"));
		rtnMap.put("Country", generateFieldData("Country", "labelCountry"));
		rtnMap.put("Zip", generateFieldData("Zip", "labelPostalCode"));
		rtnMap.put("PreferredLanguage", generateFieldData("preferredLanguage",
				"LabelPreferredLang"));
		rtnMap.put("MHP", generateFieldData("MHP", "ProdMHP"));
		rtnMap.put("ALC", generateFieldData("ALC", "ProdALC"));
		rtnMap.put("Forks", generateFieldData("Forks", "ProdForks"));
		rtnMap.put("CEP", generateFieldData("CEP", "ProdCEP"));
		rtnMap.put("TimeZone", generateFieldData("TimeZone", "LabelTimeZone"));
		rtnMap.put("MailLiterature", generateFieldData("MailLiterature",
				"LabelPrintOptOut"));
		rtnMap.put("AcceptResponsibility", generateFieldData(
				"AcceptResponsibility", "LabelAcceptResponsibility"));
		rtnMap.put("NewPassword", generateFieldData("HTTPPassword",
				"LabelPassword"));
		rtnMap.put("ChallengeQuestion", generateFieldData("ChallengeQuestion",
				"LabelQuestion"));
		rtnMap.put("ChallengeAnswer", generateFieldData("ChallengeAnswer",
				"LabelAnswer"));
		rtnMap.put("MktgOptOut", generateFieldData("MktgOptOut",
				"LabelMktgOptOut"));

		return rtnMap;
	}

	private static FieldData generateFieldData(String nabDocName,
			String labelName) {
		FieldData fd = new FieldData();
		fd.labelName = labelName;
		fd.nabDocName = nabDocName;
		return fd;
	}

	public static void setRegistrationUpdateDoc(String username, Session session) {
		// this is used when the ccUpdateRegisration loads to get the
		// information from
		// the person document in the Domino Directory.
		// It will also get the latest company details from the JDE system for
		// the
		// company address, etc.
		try {
			String nabDocFieldName;
			// Document regDoc = getCurrentDocument();
			// use the current XSPDocument the user is viewing
			DominoDocument regDoc = getCurrentXSPDocument();
			Document personDoc = getPersonDoc(username, session);
			Map<String, FieldData> fieldData = RegistrationUtilities
					.getFieldData();
			for (String regDocName : fieldData.keySet()) {
				debugMsg("key is " + regDocName
						+ " and the value of nabDocName is "
						+ fieldData.get(regDocName).nabDocName);
				nabDocFieldName = fieldData.get(regDocName).nabDocName;
				regDoc.replaceItemValue(regDocName, personDoc
						.getItemValue(nabDocFieldName));
				debugMsg("value in person doc is "
						+ personDoc.getItemValue(nabDocFieldName));
			}
			// setup duplicate pw field
			regDoc.replaceItemValue("newPasswordConfirm", personDoc
					.getItemValue("HTTPPassword"));
			// set user email in orig email and fullname
			regDoc.replaceItemValue("origemail", username);
			// store the fullname
			regDoc.replaceItemValue("fullname", username);
			// set the account number
			regDoc.replaceItemValue("accountnumber", "EmployeeID");
			// set the region
			regDoc.replaceItemValue("region", "Location");
			// set the account number
			regDoc.replaceItemValue("AccountNumber", personDoc
					.getItemValue("EmployeeID"));
			// set the location
			regDoc.replaceItemValue("region", personDoc
					.getItemValue("Location"));

			// get JDE info for company name
			JDESupport jde = new JDESupport();
			debugMsg("Account number used in lookup is: "
					+ regDoc.getItemValueString("AccountNumber"));
			JDEData jdeData = jde.getAcctDetails(regDoc
					.getItemValueString("AccountNumber"), regDoc
					.getItemValueString("region"));
			String coName = jdeData.getCustName();
			debugMsg("Company name is " + coName);
			// set company information
			regDoc.replaceItemValue("cName", jdeData.getCustName());
			regDoc.replaceItemValue("cAddress", jdeData.getAddress1());
			regDoc.replaceItemValue("cAddress2", jdeData.getAddress2());
			regDoc.replaceItemValue("cAddress3", jdeData.getAddress3());
			regDoc.replaceItemValue("cCity", jdeData.getCustCity());
			regDoc.replaceItemValue("cState", jdeData.getCustState());
			regDoc.replaceItemValue("cZip", jdeData.getCustZip());
			regDoc.replaceItemValue("cCountry", jdeData.getCustCountryCode());
			// compute single address field for company address
			String compAddress = "";
			if (StringUtil.isNotEmpty(jdeData.getAddress3())
					& StringUtil.isNotEmpty(jdeData.getAddress2())) {
				compAddress = jdeData.getAddress1() + ", "
						+ jdeData.getAddress2() + ", " + jdeData.getAddress3();
			} else if (StringUtil.isNotEmpty(jdeData.getAddress2())) {
				compAddress = jdeData.getAddress1() + ", "
						+ jdeData.getAddress2();
			} else {
				compAddress = jdeData.getAddress1();

			}
			regDoc.replaceItemValue("CompanyAddressDisplay", compAddress);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String updateRegistration(String noteID, String lang,
			Session session) {

		/*
		 * This handles the updating when the user changes their registration
		 * details It figures out what fields changed so the user can be sent an
		 * email with the changed fields. The groups are updated in the email
		 * changes as well.
		 */

		boolean saved = false;
		String rtn = new String("");
		// this holds the labels of what was changed to display to the user
		FieldData changedData;
		ArrayList<FieldData> changedFields = new ArrayList<FieldData>();
		String nabDocFieldName;
		String regDocFieldValue;
		String nabDocFieldValue;
		String oldUserName = new String("");
		boolean pwChanged = false;
		boolean emailChanged = false;

		ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		String regDbname = (String) configBean1.getValue("RegistrationDbPath");

		try {
			Database regDb = session.getDatabase(session.getServerName(),
					regDbname, false);
			if (regDb != null) {
				// System.out.println("Handle on Registration DB");
				Document regDoc = regDb.getDocumentByID(noteID);
				// System.out.println("Tried to get Handle on Registration DOCUMENT");
				if (regDoc != null) {
					// System.out.println("Got Handle on Registration DOCUMENT");
					// get person document from NAB
					Document nabDoc = getPersonDoc(regDoc
							.getItemValueString("fullname"), session);
					if (nabDoc != null) {
						// loop thru fields and figure out what was changed
						LinkedHashMap<String, FieldData> fieldData = RegistrationUtilities
								.getFieldData();
						for (String regDocName : fieldData.keySet()) {
							debugMsg("key is " + regDocName
									+ " and the value of nabDocName is "
									+ fieldData.get(regDocName).nabDocName);

							nabDocFieldName = fieldData.get(regDocName).nabDocName;
							regDocFieldValue = regDoc
									.getItemValueString(regDocName);
							nabDocFieldValue = nabDoc
									.getItemValueString(nabDocFieldName);
							if (!StringUtil.equals(regDocFieldValue,
									nabDocFieldValue)) {
								// the value was changed
								// debugMsg("Value changed: nabDoc is "+
								// nabDocFieldValue + " and regDoc is "+
								// regDocFieldValue);
								// System.out.println("Value changed for "+
								// nabDocFieldName + ": nabDoc is "+
								// nabDocFieldValue + " and regDoc is "+
								// regDocFieldValue);
								if (StringUtil.equals(regDocName, "email")) {
									// note if email has changed
									debugMsg("Email has changed");
									emailChanged = true;
									// get the old user name to use when
									// changing in
									// groups and other docs
									oldUserName = regDoc
											.getItemValueString("origemail");
								}
								if (StringUtil
										.equals(regDocName, "NewPassword")) {
									// password has changed;
									// debugMsg("Password has changed");
									// System.out.println("NewPassword");
									// remove password information from regdoc -
									// added 12/06/2017 mdz
									regDoc.replaceItemValue("NewPassword", "");
									regDoc.replaceItemValue(
											"NewPasswordConfirm", "");
									regDoc.save();
									pwChanged = true;
								}
								// write to nabDoc
								nabDoc.replaceItemValue(nabDocFieldName,
										regDocFieldValue);
								// debugMsg("new value in nabDoc is "+
								// regDocFieldValue + " in "+ nabDocFieldName);
								// save label name and value
								changedData = new FieldData();
								changedData.labelName = fieldData
										.get(regDocName).labelName;
								// added to display Yes or No for certain fields
								// by
								// HG
								// on 1/27/16
								// get translation of Yes or No
								// get language specific strings for email
								LangBean langBean = (LangBean) ExtLibUtil
										.resolveVariable(FacesContext
												.getCurrentInstance(),
												"langBean");
								String yesStr = langBean.getLangString(
										"AfterUpdateEmail-Yes", lang);
								String noStr = langBean.getLangString(
										"AfterUpdateEmail-No", lang);
								if (regDocName.equalsIgnoreCase("MHP")
										|| regDocName.equalsIgnoreCase("ALC")
										|| regDocName.equalsIgnoreCase("Forks")
										|| regDocName.equalsIgnoreCase("CEP")) {
									if (StringUtil.isEmpty(regDocFieldValue)) {
										changedData.newValue = noStr;
									} else {
										changedData.newValue = yesStr;
									}

								} else if (regDocName
										.equalsIgnoreCase("MailLiterature")
										|| regDocName
												.equalsIgnoreCase("MktgOptOut")) {
									if (regDocFieldValue.equalsIgnoreCase("1")) {
										changedData.newValue = yesStr;
									} else {
										changedData.newValue = noStr;
									}
								} else {
									// just set the value the user entered
									changedData.newValue = regDocFieldValue;
								}
								// end of added code on 1/27/16
								changedFields.add(changedData);
							} // end if statement to check if values changed
						}// end for loop
						// check out changedFields - REMOVE LATER
						for (FieldData cf : changedFields) {
							debugMsg("changedFieldLabel is are " + cf.labelName
									+ " and the value is " + cf.newValue);
						}
						// check the company information and write out to
						// personDoc
						// no checking if changed in current system, so doing
						// the
						// same
						nabDoc.replaceItemValue("CompanyName", regDoc
								.getItemValueString("cName"));
						nabDoc.replaceItemValue("OfficeStreetAddress", regDoc
								.getItemValueString("cAddress"));
						nabDoc.replaceItemValue("OfficeStreetAddress2", regDoc
								.getItemValueString("cAddress2"));
						nabDoc.replaceItemValue("OfficeStreetAddress3", regDoc
								.getItemValueString("cAddress3"));
						nabDoc.replaceItemValue("OfficeCity", regDoc
								.getItemValueString("cCity"));
						nabDoc.replaceItemValue("OfficeState", regDoc
								.getItemValueString("cState"));
						nabDoc.replaceItemValue("OfficeZip", regDoc
								.getItemValueString("cZip"));
						nabDoc.replaceItemValue("OfficeCountry", regDoc
								.getItemValueString("cCountry"));

						// see if email has changed, if so do a lot more stuff
						if (emailChanged) {
							// fixup the other nabDoc fields
							String regNewEmail = regDoc
									.getItemValueString("email");
							nabDoc.replaceItemValue("FullName", regNewEmail);
							nabDoc.replaceItemValue("InternetAddress",
									regNewEmail);
							if (StringUtil.indexOfIgnoreCase(regNewEmail,
									"cascorp.com") >= 0) {
								nabDoc.replaceItemValue("MailDomain", "EAGLE");
							}
							// Only way to get username with @ to show up in
							// ($LDAPCN)
							// view
							nabDoc.replaceItemValue("CommonName", regNewEmail);
						}
						// computewithform for the personDoc
						nabDoc.computeWithForm(false, false);
						saved = nabDoc.save(false, true);
						if (saved) {
							// SEND OUT EMAIL
							sendafterRegUpdateEmail(regDoc
									.getItemValueString("firstname"), regDoc
									.getItemValueString("email"), lang,
									changedFields, session);
						}

						if (!saved) {
							// did not get saved
							debugMsg("updateRegistration: Error in saving nabDoc");
							throw new RuntimeException(
									"Error in updateRegistration-Error in saving nabDoc");
						} else if (emailChanged) {
							String newUserName = regDoc
									.getItemValueString("email");
							// if nabDoc was saved and email changed then fix
							// groups,
							// orders, profiles
							debugMsg("old user name: " + oldUserName
									+ " new username: " + newUserName);
							// change in groups
							updateUserInGroup(oldUserName, newUserName, nabDoc
									.getParentDatabase(), session);
							// change in orders
							renameInOrders(oldUserName, newUserName, session);
							// change in profiles
							renameInUserProfiles(oldUserName, newUserName,
									session);
							// update the views
							refreshLookupViews(regDoc
									.getItemValueString("email"), nabDoc
									.getParentDatabase(), session);
							rtn = "emailchanged";
						} else if (pwChanged) {
							rtn = "pwchanged";
						} else {
							// other fields changed, return
							rtn = "otherchanged";
						}
					} else {
						// person doc not found
						debugMsg("Person doc not found in updateRegistration");
						throw new RuntimeException(
								"Error in updateRegistration-nabDoc is null");
					}
				}
			} else {
				System.out.println("NO HANDLE ON REGDOC");
			}
		} catch (Exception e) {

		}
		return rtn;
	}

	@SuppressWarnings("unchecked")
	public static void updateUserInGroup(String oldName, String newName,
			Database dbNab, Session session) {
		// adds user to group
		// dbNab is the NAB database object
		debugMsg("Starting-addUserToGroup");
		Document groupDoc;
		Document tempDoc;

		try {
			// make sure nab index is updated
			dbNab.updateFTIndex(true);
			View view = dbNab.getView("Groups");
			view.FTSearch(oldName, 0);
			groupDoc = view.getFirstDocument();
			while (groupDoc != null) {
				debugMsg("Found group called "
						+ groupDoc.getItemValueString("ListName"));
				Vector<String> members = groupDoc.getItemValue("Members");

				Collections.replaceAll(members, oldName, newName);
				// replace the contents
				groupDoc.replaceItemValue("Members", members);
				debugMsg("saving group:"
						+ groupDoc.getItemValueString("ListName"));
				boolean saved = groupDoc.save(true);
				debugMsg("group doc save status:" + saved);
				tempDoc = view.getNextDocument(groupDoc);
				groupDoc.recycle();
				groupDoc = tempDoc;

			}
		} catch (Exception e) {
			debugMsg("Error in updateUserInGroup");
			throw new RuntimeException("Error in updateUserInGroup"
					+ e.toString());
		}
	}

	public static void renameInUserProfiles(String oldName, String newName,
			Session session) {
		// this updates the user names in the profiles if the user changes their
		// name
		try {
			ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String userProfileDbName = (String) configBean1
					.getValue("UserprofilesDbPath");
			Database dbUserProfiles = session.getDatabase(session
					.getServerName(), userProfileDbName);
			Document docTemp;
			View viewTemp = dbUserProfiles.getView("AddressesByOwner");
			DocumentCollection collTemp = viewTemp.getAllDocumentsByKey(
					oldName, true);
			Document docProfile = collTemp.getFirstDocument();
			while (docProfile != null) {
				docProfile.replaceItemValue("Owner", newName);
				docProfile.save(true);
				docTemp = collTemp.getNextDocument(docProfile);
				docProfile.recycle();
				docProfile = docTemp;
			}
		} catch (Exception e) {
			debugMsg("Error in renameInUserProfiles: " + e.toString());
			throw new RuntimeException("Error in renameInUserProfiles: "
					+ e.toString());
		}
	}

	public static void renameInOrders(String oldName, String newName,
			Session session) {
		// this updates the name in orders if they change their name
		try {
			View viewOrders;
			DocumentCollection collTemp;
			Document docOrder;
			Document docTemp;
			ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String userProfileDbName = (String) configBean1
					.getValue("OrdersDbPath");
			Database dbOrders = session.getDatabase(session.getServerName(),
					userProfileDbName);

			viewOrders = dbOrders.getView("OrdersByOwner");
			collTemp = viewOrders.getAllDocumentsByKey(oldName, true);

			docOrder = collTemp.getFirstDocument();
			while (docOrder != null) {
				docOrder.replaceItemValue("OrderOwner", newName);
				docOrder.save(true);
				docTemp = collTemp.getNextDocument(docOrder);
				docOrder.recycle();
				docOrder = docTemp;
			}
		} catch (Exception e) {
			debugMsg("Error in renameInOrders: " + e.toString());
			throw new RuntimeException("Error in renameInOrders: "
					+ e.toString());
		}
	}

	public static void flipEmailSubscribe(String username, Session session) {
		// this routine will set the value of the email subscription in the
		// personDoc
		// if set to 1 it is saying the person will NOT get emails
		// if set to empty ("") than they will get emails
		try {
			Document personDoc = getPersonDoc(username, session);
			String mktOptOut = personDoc.getItemValueString("MktgOptOut");
			debugMsg("mktOptOut is " + mktOptOut);
			if (StringUtil.equals(mktOptOut, "1")) {
				// set to ""
				personDoc.replaceItemValue("MktgOptOut", "");
			} else {
				personDoc.replaceItemValue("MktgOptOut", "1");
			}
			// save the person doc
			personDoc.save(true, false);
		} catch (Exception e) {
			debugMsg("flipEmailSubscribe: " + e.toString());
			throw new RuntimeException("Error in flipEmailSubscribe: "
					+ e.toString());
		}

	}

	public static boolean isPersonSubscribedToEmails(String username,
			Session session) {
		// this is used in the myprofile to return if the user is subscribed to
		// emails
		boolean rtn = true;
		try {
			Document personDoc = getPersonDoc(username, session);
			debugMsg("got personDoc for "
					+ personDoc.getItemValueString("fullname"));
			if (personDoc != null) {
				String mktOptOut = personDoc.getItemValueString("MktgOptOut");
				debugMsg("mktOptOut is " + mktOptOut);
				if (StringUtil.equals(mktOptOut, "1")) {
					rtn = false;
				}
			}
		} catch (Exception e) {
			debugMsg("Error in isPersonSubscribedToEmails: " + e.toString());
			throw new RuntimeException("Error in isPersonSubscribedToEmails: "
					+ e.toString());
		}
		return rtn;

	}

	public static Map<String, String> getAccountNumber(String username,
			Session session) {
		// this routine gets the account number from the person document for the
		// username parameter
		// It returns a Map containing the account number as well as the region
		
		Map<String, String> rtnMap = new HashMap<String, String>();
		 //System.out.println("getAccountNumber - username: " + username);
		try {
			// System.out.println("getAccountNumber - entering try");
			Document personDoc = getPersonDoc(username, session);
			// System.out.println("got personDoc for "+
			// personDoc.getItemValueString("fullname"));
			debugMsg("got personDoc for "
					+ personDoc.getItemValueString("fullname"));
			if (personDoc != null) {
				String acctNumber = personDoc.getItemValueString("EmployeeID");
				if (StringUtil.isNotEmpty(acctNumber)) {
					rtnMap.put("acctnumber", acctNumber);
				} else {
					rtnMap.put("acctnumber", "Not Found");
						
				}
				
				debugMsg("acctnumber is " + rtnMap.get("acctnumber"));
				rtnMap.put("region", personDoc.getItemValueString("Location"));
				rtnMap.put("firstName", personDoc.getItemValueString("FirstName"));
				rtnMap.put("lastName", personDoc.getItemValueString("lastName"));
				debugMsg("region is " + rtnMap.get("region"));
				rtnMap.put("countryCode", personDoc.getItemValueString("OfficeCountry"));
				debugMsg("countryCode is " + rtnMap.get("countryCode"));
// DSR 11-30-18 - Added to support My Cascade portal types
				rtnMap.put("userType", getUserType(rtnMap.get("region"), personDoc.getItemValueString("AccessType")));
				rtnMap.put("companyName", personDoc.getItemValueString("CompanyName"));
				rtnMap.put("Address1", personDoc.getItemValueString("StreetAddress"));
				rtnMap.put("Address2", personDoc.getItemValueString("StreetAddress2"));
				rtnMap.put("Address3", personDoc.getItemValueString("StreetAddress3"));
				rtnMap.put("CustCity", personDoc.getItemValueString("City"));
				rtnMap.put("CustZip", personDoc.getItemValueString("Zip"));
				rtnMap.put("CustState", personDoc.getItemValueString("State"));
				rtnMap.put("CustCountryCode", personDoc.getItemValueString("Country"));
				rtnMap.put("phoneNbr", personDoc.getItemValueString("OfficePhoneNumber"));
				debugMsg("Company Name is " + rtnMap.get("companyName"));
			}
		} catch (Exception e) {
			System.out.println("Error in getAccountNumber: " + e.toString());
			debugMsg("Error in getAccountNumber: " + e.toString());
			throw new RuntimeException("Error in getAccountNumber: "
					+ e.toString());
		}
		return rtnMap;
	}
	
	// DSR 11-30-18 - Added to support My Cascade portal types
	public static String getUserType(String region, String accessType) {
		// takes accesstype from address book which can be all over the place and maps to the user portal types
		// Portal types are end = end user, oem = dealer/oem, int = internal, ven = vendor
		try {
			Database thisDb = DominoUtils.getCurrentDatabase();
			View typeVw = thisDb.getView("LUPortalTypes");
			Document typeDoc = typeVw.getDocumentByKey(region + "~" + accessType);
			return typeDoc.getItemValueString("Type");
			/*
			if (accessType.equals("INT") | accessType.equals("B")) {
				return "INT";
			} else if (accessType.equals("OEM") | accessType.equals("DEA") | accessType.equals("O") | accessType.equals("EXD") | accessType.equals("D")) {
				return "OEM";			
			} else if (accessType.equals("VEN") | accessType.equals("V")) {
				return "VEN";			
			} else {				// If not found (or if value is END, it will default to END
				return "END";*/
		} catch (Exception e) {
			System.out.println("Error in getAccountNumber: " + e.toString());
			debugMsg("Error in getAccountNumber: " + e.toString());
			return "END";
		}
	}
	
	public static String getCompanyNameFromJDE(String acctNumber, String region) {
		// gets the company name from JDE for a given acctNumber
		String rtnString = new String("");
		if (!StringUtil.equalsIgnoreCase(acctNumber, "Not Found")) {
			// get JDE info for company name
			JDESupport jde = new JDESupport();
			debugMsg("Account number used in lookup is: " + acctNumber);
			JDEData jdeData = jde.getAcctDetails(acctNumber, region);
			rtnString = jdeData.getCustName();
			debugMsg("Company name is " + rtnString);
		} else {
			rtnString = "Not Found";
		}
		return rtnString;
	}

	public class regUsersMap {
		// this is used in the repeat in the showRegisteredUsers XPage
		String fullName;
		String email;
		boolean isOrderPerson;

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public boolean isOrderPerson() {
			return isOrderPerson;
		}

		public void setOrderPerson(boolean isOrderPerson) {
			this.isOrderPerson = isOrderPerson;
		}

		public regUsersMap(String fullName, String email, boolean isOrderPerson) {
			this.email = email;
			this.fullName = fullName;
			this.isOrderPerson = isOrderPerson;
		}
	}

	@SuppressWarnings("unchecked")
	public ArrayList<regUsersMap> getRegisteredUsers(String acctNumber,
			Session session) {
		// used in the showRegisteredUsers XPage
		// gets all the users for a given account
		// returns a list of regUsersMap
				
		ArrayList<regUsersMap> rtnArrayList = new ArrayList<regUsersMap>();
		Vector groupMembers = new Vector();
		
		if (acctNumber.equals("")|acctNumber.equals("Not Found")) { 
			return rtnArrayList;
		}
		
		try {
			ViewEntry entry;
			ViewEntry tempEntry;
			ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
					FacesContext.getCurrentInstance(), "configBean");
			String nabDbname = (String) configBean1
					.getValue("UserDominoDirectoryPath");
			Database nabDb = null;
			nabDb = session.getDatabase(session.getServerName(), nabDbname,
					false);
			if (nabDb != null) {
				debugMsg("Database is open and title is " + nabDb.getTitle());
				// get the views, this one is for searching for people by
				// account number
				View PeopleVw = nabDb.getView("($People)");
				// this view is for getting the groups needed to figure out the
				// order capability for each person
				View VIMGroups = nabDb.getView("($VIMGroups)");
				// get the group documents
				Document DocGroup = VIMGroups
						.getDocumentByKey("Cascorp-Order-Status");
				Document DocGroupA = VIMGroups
						.getDocumentByKey("Cascorp-Order-Status-Americas");
				Document DocGroupE = VIMGroups
						.getDocumentByKey("Cascorp-Order-Status-Europe");
				Document DocGroupT = VIMGroups
						.getDocumentByKey("Cascorp-Order-Status-Test");
				// add the members to one big Vector
				groupMembers.addAll(DocGroup.getItemValue("members"));
				groupMembers.addAll(DocGroupA.getItemValue("members"));
				groupMembers.addAll(DocGroupE.getItemValue("members"));
				groupMembers.addAll(DocGroupT.getItemValue("members"));
				// find all users with the same account number
				int numDocs = PeopleVw.FTSearch("[EmployeeID] CONTAINS "
						+ acctNumber, 0);
				if (numDocs > 0) {
					// found some documents
					debugMsg("Found same account number, count is " + numDocs);
					ViewEntryCollection viewEntryCol = PeopleVw.getAllEntries();
					entry = viewEntryCol.getFirstEntry();
					while (entry != null) {
						if (entry.isDocument()) {
							// copy over the information to a new Map and add to
							// the ArrayList
							Document personDoc = entry.getDocument();
							String name = personDoc
									.getItemValueString("firstname")
									+ " "
									+ personDoc.getItemValueString("lastname");
							String email = personDoc
									.getItemValueString("fullname");

							boolean isOrderStatus = false;
							if (groupMembers.contains(email)) {
								debugMsg("Found user in groups:" + email);
								isOrderStatus = true;
							}
							debugMsg("adding " + name);
							regUsersMap userMap = new regUsersMap(name, email,
									isOrderStatus);
							rtnArrayList.add(userMap);
						}
						// get next entry
						tempEntry = viewEntryCol.getNextEntry(entry);
						entry.recycle();
						entry = tempEntry;
					}
				}

			} else {
				// NAB DB is not open
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		return rtnArrayList;
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

	// WSCR-ASES6S - Vendors / DXF Files - added code to allow certain vendors
	// to register
	@SuppressWarnings("unchecked")
	public static boolean findVendorAccount(String acctNumber) {
		// gets the VendorAccounts from the config values
		debugMsg("1. [findVendorAccount] =>>> ENTERING <<<");
		boolean rtn = false; // assume false

		ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		debugMsg("2. [findVendorAccount] =>>> Getting ArrayList <<<");
		ArrayList<String> VendorAcctArray = (ArrayList<String>) configBean1
				.getValue("DXFAccountNumbers");
		debugMsg("3. [findVendorAccount] =>>> Getting HasMap <<<");
		HashMap<String, String> VendorAcctMap = new HashMap<String, String>();
		String key = new String();
		String groupName = new String();
		// load up a map
		debugMsg("DFXAcctArray.size():  " + VendorAcctArray.size());
		debugMsg("^^^key|groupName^^^");
		if (VendorAcctArray.size() > 0) {
			for (String member : VendorAcctArray) {
				key = StringUtil.trim(member.substring(0, member.indexOf("|")));
				groupName = StringUtil.trim(member.substring(member
						.lastIndexOf('|') + 1));
				debugMsg(key + "|" + groupName);
				// debugMsg("alias is " + groupName);
				VendorAcctMap.put(key, groupName);
			}
		}
		if (VendorAcctMap.containsKey(acctNumber)) {
			rtn = true;
		}
		debugMsg("10. [findVendorAccount] =>>> rtn: " + rtn);
		return rtn;
	}

	@SuppressWarnings("unchecked")
	public static String findDXFGroupName(String acctNumber) {
		// gets the OEMgroup from the config values
		String rtn = new String("");
		ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		ArrayList<String> DFXAcctArray = (ArrayList<String>) configBean1
				.getValue("DXFAccountNumbers");
		HashMap<String, String> DFXAcctMap = new HashMap<String, String>();
		String key = new String();
		String groupName = new String();
		// load up a map
		debugMsg("DFXAcctArray.size():  " + DFXAcctArray.size());
		debugMsg("^^^key|groupName^^^");
		if (DFXAcctArray.size() > 0) {
			for (String member : DFXAcctArray) {
				key = StringUtil.trim(member.substring(0, member.indexOf("|")));
				groupName = StringUtil.trim(member.substring(member
						.lastIndexOf('|') + 1));
				debugMsg(key + "|" + groupName);
				// debugMsg("alias is " + groupName);
				DFXAcctMap.put(key, groupName);
			}
		}
		if (DFXAcctMap.containsKey(acctNumber)) {
			rtn = DFXAcctMap.get(acctNumber);
		}
		debugMsg("found value: " + rtn);
		return rtn;
	}

	// WSCR-ASES6S - Vendors / DXF File end of code added

	public static String getHTMLfromRT2(final Document doc,
			final String richTextItemName, Session session) throws NotesException {
		// this is code that converts a richtext item to HTML
		// standard code from OpenNTF snippets, web search, etc....

		
		
		// need to get handle of work horse database - it holds the images to be displayed
		ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		String whDbname = (String) configBean1.getValue("WorkHorseDbPath");
		debugMsg(whDbname);
		Database whDb = session.getDatabase(session.getServerName(),
		whDbname, false);

		// disable MIME to RichText conversion
		whDb.getParent().setConvertMIME(false);

		// wrap the lotus.domino.Document as a lotus.domino.DominoDocument
		// see
		// http://public.dhe.ibm.com/software/dw/lotus/Domino-Designer/JavaDocs/DesignerAPIs/com/ibm/xsp/model/domino/wrapped/DominoDocument.html
		DominoDocument wrappedDoc = null;
		wrappedDoc = DominoDocument.wrap(doc.getParentDatabase().getFilePath(),
				doc, null, null, false, null, null);

		// see
		// http://public.dhe.ibm.com/software/dw/lotus/Domino-Designer/JavaDocs/DesignerAPIs/com/ibm/xsp/model/domino/wrapped/DominoRichTextItem.html
		DominoRichTextItem drti = null;

		Item itemRT = doc.getFirstItem(richTextItemName);

		if (null != itemRT) {

			if (itemRT.getType() == Item.RICHTEXT) {

				// create a DominoRichTextItem from the RichTextItem
				RichTextItem rt = (RichTextItem) itemRT;
				drti = new DominoRichTextItem(wrappedDoc, rt);

			} else if (itemRT.getType() == Item.MIME_PART) {

				// create a DominoRichTextItem from the Rich Text item that
				// contains MIME
				MIMEEntity rtAsMime = doc.getMIMEEntity(richTextItemName);
				drti = new DominoRichTextItem(wrappedDoc, rtAsMime,
						richTextItemName);

			}
		}

		return drti.getHTML();
	}
}