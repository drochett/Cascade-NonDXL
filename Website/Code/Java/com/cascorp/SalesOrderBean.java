package com.cascorp;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.FacesContext; //import javax.faces.context.ExternalContext;

import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.DominoUtils;

public class SalesOrderBean implements Serializable {
	/*
	 * This is used in the ordering process to get the list of what the user can
	 * orderIt is then used to store all the user entered informationThe final
	 * step will create a new order in the orders db
	 */
	private static final long serialVersionUID = 1L;

	private static boolean showDBar = true;

	static String OrdersDbName;
	// these are used for non-parts orders
	LinkedHashMap<String, ArrayList<SalesOrderItem>> forkkits;
	LinkedHashMap<String, ArrayList<SalesOrderItem>> drivertrainingvideos;
	LinkedHashMap<String, ArrayList<SalesOrderItem>> salesliterature;
	LinkedHashMap<String, ArrayList<SalesOrderItem>> salesvideos;
	LinkedHashMap<String, ArrayList<SalesOrderItem>> pricelists;
	LinkedHashMap<String, ArrayList<SalesOrderItem>> dealerclaims;
	// 06/05/2018 MCR WSCR-AZBRBS - Add Fork Positioner / Sideshifter Order -mdz
	LinkedHashMap<String, ArrayList<PromoOrderItem>> promoitems;
	// this is used for parts orders, the key is the part number, value is
	// SalesOrderItem
	LinkedHashMap<String, SalesOrderItem> partsItems = new LinkedHashMap<String, SalesOrderItem>();
	// 6/20/17 DSR - Forks Order Item
	LinkedHashMap<String, SalesOrderItem> forksItems = new LinkedHashMap<String, SalesOrderItem>();

	String promoCode;
	Address shipAddress = new Address();
	Address billAddress = new Address();
	String billingAddressSameAsShip;
	String shipInstructions;
	String orderEmail;
	Date orderDate;
	Double grandTotalPrice; // save price to display at end
	String DealerPONumber;
	String EndUserPONumber;
	String region;
	String accountNumber;
	String countryCode;
	String custCurrency;
	String urgentCheckBox;
	DealerClaim data = new DealerClaim();

	public class SalesOrderItem implements Serializable {
		// used to return a salesOrderItem to the repeat
		private static final long serialVersionUID = 1L;
		Double orderQty;
		String orderQtyString;
		Integer maxQty;
		String description;
		boolean specMsgDisp;
		String specialMsgDesc;
		Double price;
		String partNumber;

		public Double getOrderQty() {
			return this.orderQty;
		}

		public void setOrderQty(Double orderQty) {
			debugMsg("Starting setOrderQty: value is " + orderQty);
			this.orderQty = orderQty;
		}

		public String getOrderQtyString() {
			return orderQtyString;
		}

		public void setOrderQtyString(String orderQtyString) {

			this.orderQtyString = orderQtyString.toString();
			this.orderQty = new Double(orderQtyString);
			debugMsg("Value for " + this.partNumber + " is " + this.orderQty);
		}

		public Integer getMaxQty() {
			return maxQty;
		}

		public void setMaxQty(Integer maxQty) {
			this.maxQty = maxQty;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public boolean isSpecMsgDisp() {
			return specMsgDisp;
		}

		public void setSpecMsgDisp(boolean specMsgDisp) {
			this.specMsgDisp = specMsgDisp;
		}

		public String getSpecialMsgDesc() {
			return specialMsgDesc;
		}

		public void setSpecialMsgDesc(String specialMsgDesc) {
			this.specialMsgDesc = specialMsgDesc;
		}

		public Double getPrice() {
			return price;
		}

		public void setPrice(Double price) {
			this.price = price;
		}

		public String getPartNumber() {
			return partNumber;
		}

		public void setPartNumber(String partNumber) {
			this.partNumber = partNumber;
		}

		public SalesOrderItem(String partNum, String description, Double price,
				Double maxQty, String hasSpecDesc, String specialDesc) {
			this.orderQty = new Double(0);
			this.orderQtyString = new String("0");
			this.maxQty = maxQty.intValue();
			this.price = price;
			this.description = description;
			this.specialMsgDesc = specialDesc;
			this.partNumber = partNum;
			if (StringUtil.equals(hasSpecDesc, "Y")) {
				this.specMsgDisp = true;
			} else {
				this.specMsgDisp = false;
			}
		}

		public SalesOrderItem(String partNum, String description,
				Double orderQty, Double price, Double maxQty,
				String hasSpecDesc, String specialDesc) {
			this.orderQty = new Double(0);
			this.orderQtyString = new String("0");
			this.maxQty = maxQty.intValue();
			this.price = price;
			this.orderQty = orderQty;
			this.description = description;
			this.specialMsgDesc = specialDesc;
			this.partNumber = partNum;
			if (StringUtil.equals(hasSpecDesc, "Y")) {
				this.specMsgDisp = true;
			} else {
				this.specMsgDisp = false;
			}
		}

		public SalesOrderItem(String partNum, String description,
				Double orderQty, Double price) {
			this.orderQty = orderQty;

			this.price = price;
			this.description = description;

			this.partNumber = partNum;
		}
	}

	public Address getShipAddress() {
		return shipAddress;
	}

	public void setShipAddress(Address shipAddress) {
		this.shipAddress = shipAddress;
	}

	public Address getBillAddress() {
		return billAddress;
	}

	public void setBillAddress(Address billAddress) {
		this.billAddress = billAddress;
	}

	public String getShipInstructions() {
		return shipInstructions;
	}

	public void setShipInstructions(String shipInstructions) {
		this.shipInstructions = shipInstructions;
	}

	public String getBillingAddressSameAsShip() {
		return billingAddressSameAsShip;
	}

	public void setBillingAddressSameAsShip(String billingAddressSameAsShip) {
		this.billingAddressSameAsShip = billingAddressSameAsShip;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public Double getGrandTotalPrice() {
		// if (ordertype = "promoitems") {
		// return
		// }
		return grandTotalPrice;
	}

	public void setGrandTotalPrice(Double grandTotalPrice) {
		this.grandTotalPrice = grandTotalPrice;
	}

	public String getDealerPONumber() {
		return DealerPONumber;
	}

	public void setDealerPONumber(String dealerPONumber) {
		DealerPONumber = dealerPONumber;
	}

	public String getEndUserPONumber() {
		return EndUserPONumber;
	}

	public void setEndUserPONumber(String endUserPONumber) {
		EndUserPONumber = endUserPONumber;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCustCurrency() {
		return custCurrency;
	}

	public void setCustCurrency(String custCurrency) {
		this.custCurrency = custCurrency;
	}

	// MCR WSCR-AXZRWL - Additional Parts Order Email Language 04/23/2018 MDZ
	public String getUrgentCheckBox() {
		return urgentCheckBox;
	}

	public void setUrgentCheckBox(String urgentCheckBox) {
		this.urgentCheckBox = urgentCheckBox;
		debugMsg("Urgent is " + urgentCheckBox);
	}

	// END OF MCR WSCR-AXZRWL - Additional Parts Order Email Language

	public Double getOrderTotal(String typeName) {
		Double rtnTotal = new Double(0);
		// This gets the ordered items
		ArrayList<SalesOrderItem> orderedItems = new ArrayList<SalesOrderItem>();
		if (StringUtil.equalsIgnoreCase(typeName, "forkkit")) {
			orderedItems = getOrderedItemsArrayList(forkkits);
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesliterature")) {
			orderedItems = getOrderedItemsArrayList(salesliterature);
		} else if (StringUtil
				.equalsIgnoreCase(typeName, "drivertrainingvideos")) {
			orderedItems = getOrderedItemsArrayList(drivertrainingvideos);
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesvideos")) {
			orderedItems = getOrderedItemsArrayList(salesvideos);
		} else if (StringUtil.equalsIgnoreCase(typeName, "parts")) {
			orderedItems = getOrderedParts();
		} else if (StringUtil.equalsIgnoreCase(typeName, "forks")) {
			orderedItems = getOrderedForks();
		} else if (StringUtil.equalsIgnoreCase(typeName, "pricelists")) {
			orderedItems = getOrderedItemsArrayList(pricelists);
		}
		// loop through ordered items and add up the price
		for (SalesOrderItem ordItem : orderedItems) {
			rtnTotal = rtnTotal + ordItem.getPrice() * ordItem.getOrderQty();
		}
		return rtnTotal;
	}

	@SuppressWarnings("unchecked")
	public boolean saveOrder(String orderType, Session session) {
		// this is used when the user places the order to create a doc in the
		// orders db
		boolean rtn = false;
		String ordLang = "americas-en";
		Item item;
		ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		String ordersDbname = (String) configBean.getValue("OrdersDbPath");
		Map sessionScope = (Map) ExtLibUtil.resolveVariable(FacesContext
				.getCurrentInstance(), "sessionScope");
		debugMsg("Profiles db is " + ordersDbname);
		String dispLang = (String) sessionScope.get("LanguageP");
		debugMsg("LanguageP is " + dispLang);
		// System.out.println("LanguageP is " + dispLang);

		// get the language and region based on the user's company
		// this will more accurately allow processing by proper Cascade person
		String region = (String) sessionScope.get("region");
		String cc = (String) sessionScope.get("countryCode");
		if (orderType.trim().equals("salesliterature")) {
			cc = "US";
		}
		debugMsg("cc = |" + cc + "|");
		debugMsg("region = |" + region + "|");
		// System.out.println("cc = |" + cc + "|");
		if (cc.trim().equals("US") || cc.trim().equals("GB")) {
			debugMsg("cc = US or GB");
			ordLang = region + "-en";

			// *** 9/27/17 - DSR addedd
		} else if (cc.trim().equals("CA")) {
			debugMsg("cc = CA [Canada]");
			ordLang = region + "-ca";
			// **
			// *** 01/31/18 - MDZ addedAll Central and South America Spanish
			// speaking countries
		} else if (cc.trim().equals("BO") || cc.trim().equals("CL")
				|| cc.trim().equals("CO") || cc.trim().equals("EC")
				|| cc.trim().equals("PY") || cc.trim().equals("PE")
				|| cc.trim().equals("UY") || cc.trim().equals("VE")
				|| cc.trim().equals("AR")) {
			debugMsg("South America Organization");
			ordLang = region + "-es";
		} else if (cc.trim().equals("CR") || cc.trim().equals("DO")
				|| cc.trim().equals("SV") || cc.trim().equals("GT")
				|| cc.trim().equals("HN") || cc.trim().equals("NI")
				|| cc.trim().equals("PA") || cc.trim().equals("MX")) {
			debugMsg("Central America Organization");
			ordLang = region + "-es";
		} else if (cc.trim().equals("BZ") || cc.trim().equals("GY")
				|| cc.trim().equals("SR")) {
			debugMsg("Somewhere South Of The Border Organization");
			ordLang = region + "-es";
			// ** END OF CODE ADDED 01/31/2018 - MDZ
		} else if (region.trim().equals("americas")) {
			ordLang = region + "-en";
		} else {
			debugMsg("cc = something");
			ordLang = region + "-" + cc.toLowerCase();
		}

		debugMsg("ordLang = " + ordLang);
		// System.out.println("ordLang = " + ordLang);
		// //////////////////////////////////////////////

		try {
			// System.out.println("Entering try");
			// get orders database
			Database ordersDb = session.getDatabase(session.getServerName(),
					ordersDbname, false);
			if (ordersDb.isOpen()) {
				// create new orders document
				Document doc = ordersDb.createDocument();
				// create vector to hold who is an editor
				Vector<String> editorArray = new Vector<String>();
				editorArray.add("[EditAllDocs]");
				debugMsg("1.0 = ordLang=" + ordLang);
				item = doc.replaceItemValue("AccessEditor", editorArray);
				item.setAuthors(true);
				String username = DominoUtils.getCurrentSession()
						.getEffectiveUserName();
				debugMsg("username=" + username);
				// System.out.println("username=  " + username);
				String reader;
				if (!username.equalsIgnoreCase("Anonymous")) {
					reader = username;
				} else {
					reader = "[EditAllDocs]";
				}
				debugMsg("reader=" + reader);
				// System.out.println("reader=  " + reader);
				item = doc.replaceItemValue("OrderOwner", reader);
				item.setReaders(true);
				// set order date
				DateTime dt = session.createDateTime("Today");

				dt.setNow();
				doc.replaceItemValue("OrderDate", dt);
				setOrderDate(dt.toJavaDate());
				// set order id
				String id = doc.getUniversalID();
				doc.replaceItemValue("OrderID", id);
				doc.replaceItemValue("OrderStatus", "New");
				// set the OrderForm field per the specs in the spreadsheet
				doc.replaceItemValue("OrderForm", orderType);
				// System.out.println("orderType is " + orderType);
				// set the ordertype field per the spreadsheet
				// update for new order types
				if (orderType.equals("forkkit")) {
					doc.replaceItemValue("ordertype", "Fork Inspection Kit");
				} else if (orderType.equals("parts")) {
					doc.replaceItemValue("ordertype", "Parts");
				} else if (orderType.equals("forks")) {
					doc.replaceItemValue("ordertype", "Forks");
				} else if (orderType.equals("pricelists")) {
					doc.replaceItemValue("ordertype", "Pricelists");
				} else if (orderType.equals("drivertrainingvideos")) {
					doc.replaceItemValue("ordertype", "Driver Training Videos");
				} else if (orderType.equals("salesliterature")) {
					doc.replaceItemValue("ordertype", "Sales Literature");
				} else if (orderType.equals("salesvideos")) {
					doc.replaceItemValue("ordertype", "Sales Video");
				} else if (orderType.equals("promoitems")) {
					doc.replaceItemValue("ordertype", "Promotional Order");
				}
				doc.replaceItemValue("countryCode", cc.trim());
				// doc.replaceItemValue("CustomerCurrency", "USD");
				// debugMsg("1.countryCode is "+ cc.trim());
				// debugMsg("2.region is " + region);
				// System.out.println("1.countryCode is "+ cc.trim());
				// System.out.println("2.region is " + region);
				/*
				 * Double code - commented out 02/01/2018 - MDZ if
				 * (cc.trim().equals("US") || cc.trim().equals("GB")) {
				 * debugMsg("cc = US or GB"); ordLang = region + "-en";
				 * 
				 * //*** 9/27/17 - DSR addedd }else if (cc.trim().equals("CA"))
				 * { debugMsg("cc = CA"); ordLang = "americas-ca"; //**
				 * 
				 * }else if (region.trim().equals("americas")){ ordLang = region
				 * + "-es"; } else { debugMsg("cc = something"); ordLang =
				 * region + "-" + cc.toLowerCase(); } END OF CODE COMMENTED OUT
				 * 02/01/2018 - MDZ
				 */
				debugMsg("oddLang=" + ordLang.toString());
				if (cc.trim().equals("US")) {
					doc.replaceItemValue("CustomerCurrency", "USD");
					// System.out.println("CountryCode="+cc.toLowerCase());
				} else if (region.toLowerCase().equals("americas")) {
					//System.out.println("region = " + region.toLowerCase());
					debugMsg("region = " + region.toLowerCase());

					// *** 9/27/17 - DSR added support for CAD
					if (cc.trim().equals("CA")) {
						doc.replaceItemValue("CustomerCurrency", "CAD");
					} else {
						doc.replaceItemValue("CustomerCurrency", "USD");
					}
				} else if (cc.trim().equals("GB")) {
					doc.replaceItemValue("CustomerCurrency", "GBP");
				} else if (cc.trim().equals("SE")) {
					doc.replaceItemValue("CustomerCurrency", "SEK");
				} else {
					doc.replaceItemValue("CustomerCurrency", "EUR");
				}
				doc.replaceItemValue("MsgLanguage", dispLang);
				doc.replaceItemValue("OrdLanguage", ordLang);
				// set AuthorAccessFulfillment
				String AuthorAccessFulfill = new String("");
				if (StringUtil.equalsIgnoreCase(orderType, "forkkit")) {
					// for forkkits, have to setup other types
					AuthorAccessFulfill = "[EditInternal]";
				} else if (StringUtil.equalsIgnoreCase(orderType, "parts")) {
					AuthorAccessFulfill = "[EditParts]";
				} else if (StringUtil.equalsIgnoreCase(orderType, "forks")) {
					AuthorAccessFulfill = "[EditForks]";
				} else if (StringUtil.equalsIgnoreCase(orderType, "pricelists")) {
					AuthorAccessFulfill = "[EditInternal]";
				} else if (StringUtil.equalsIgnoreCase(orderType,
						"drivertrainingvideos")) {
					AuthorAccessFulfill = "[EditTrainVideo]";
				} else if (StringUtil.equalsIgnoreCase(orderType,
						"salesliterature")) {
					AuthorAccessFulfill = "[EditInternal]";
				} else if (StringUtil
						.equalsIgnoreCase(orderType, "salesvideos")) {
					AuthorAccessFulfill = "[EditSalesVideo]";
				} else if (StringUtil.equalsIgnoreCase(orderType, "promoitems")) {
					AuthorAccessFulfill = "[EditParts]";
				}

				item = doc.replaceItemValue("AuthorAccessFulfillment",
						AuthorAccessFulfill);
				item.setAuthors(true);

				// copy over DealerPONumber and EndUserPONumber added by mdz
				// 05/13/2016
				if (StringUtil.equalsIgnoreCase(orderType, "parts")) {
					doc.replaceItemValue("PONumber", DealerPONumber);
					doc.replaceItemValue("CustomerPONumber", EndUserPONumber);
					doc.replaceItemValue("accountNbr", this.accountNumber);
				} else if (StringUtil.equalsIgnoreCase(orderType, "forks")) {
					doc.replaceItemValue("PONumber", DealerPONumber);
					doc.replaceItemValue("CustomerPONumber", EndUserPONumber);
					doc.replaceItemValue("accountNbr", this.accountNumber);
				} else if (StringUtil.equalsIgnoreCase(orderType, "promoitems")) {
					doc.replaceItemValue("PONumber", DealerPONumber);
					doc.replaceItemValue("accountNbr", this.accountNumber);
				}
				doc.replaceItemValue("ShippingStateZip", "");
				doc.replaceItemValue("BillingStateZip", "");
				// end of code added 05/13/2016 */

				// copy address info over to order
				// create fields on order document for order items
				Integer i = 0;

				if (StringUtil.equalsIgnoreCase(orderType, "promoitems")) {
					ArrayList<PromoOrderItem> orderedItems = getPromoItems(orderType);
					for (PromoOrderItem orderItem : orderedItems) {
						doc.replaceItemValue("price_" + i.toString(), orderItem
								.getPrice()
								* orderItem.getOrderQty());
						doc.replaceItemValue("Description_" + i.toString(),
								orderItem.getDescription());
						doc.replaceItemValue("ItemCount_" + i.toString(),
								orderItem.getOrderQty());
						i++;
					}
				} else {
					ArrayList<SalesOrderItem> orderedItems = getOrderedItems(orderType);
					for (SalesOrderItem orderItem : orderedItems) {
						// loop through and create the fields
						doc.replaceItemValue("price_" + i.toString(), orderItem
								.getPrice()
								* orderItem.getOrderQty());
						doc.replaceItemValue("OrderNumber_" + i.toString(),
								orderItem.getPartNumber());
						doc.replaceItemValue("Description_" + i.toString(),
								orderItem.getDescription());
						doc.replaceItemValue("ItemCount_" + i.toString(),
								orderItem.getOrderQty());
						i++;
					}
				}
				// save the shipping address
				doc.replaceItemValue("shippingcompany", shipAddress
						.getCompany());
				doc.replaceItemValue("shippingperson", shipAddress.getPerson());
				doc.replaceItemValue("shippingaddress", shipAddress
						.getAddress());
				doc.replaceItemValue("shippingaddress_2", shipAddress
						.getAddress_2());
				doc.replaceItemValue("shippingcity", shipAddress.getCity());
				doc.replaceItemValue("shippingstate", shipAddress.getState());
				doc.replaceItemValue("shippingcountry", shipAddress
						.getCountry());
				doc.replaceItemValue("shippingpostalcode", shipAddress
						.getPostalCode());
				doc.replaceItemValue("shippingemail", shipAddress.getEmail());
				doc.replaceItemValue("shippingfax", shipAddress.getFax());
				doc.replaceItemValue("shippingphone", shipAddress.getPhone());

				if (StringUtil.equals(billingAddressSameAsShip, "1")) {
					// save the billing address from the ship address
					doc.replaceItemValue("billingcompany", shipAddress
							.getCompany());
					doc.replaceItemValue("billingperson", shipAddress
							.getPerson());
					doc.replaceItemValue("billingaddress", shipAddress
							.getAddress());
					doc.replaceItemValue("billingaddress_2", shipAddress
							.getAddress_2());
					doc.replaceItemValue("billingcity", shipAddress.getCity());
					doc
							.replaceItemValue("billingstate", shipAddress
									.getState());
					doc.replaceItemValue("billingcountry", shipAddress
							.getCountry());
					doc.replaceItemValue("billingpostalcode", shipAddress
							.getPostalCode());
					doc
							.replaceItemValue("billingemail", shipAddress
									.getEmail());
					doc.replaceItemValue("billingfax", shipAddress.getFax());
					doc
							.replaceItemValue("billingphone", shipAddress
									.getPhone());
				} else {
					// save the billing address from the billAddress object
					doc.replaceItemValue("billingcompany", billAddress
							.getCompany());
					doc.replaceItemValue("billingperson", billAddress
							.getPerson());
					doc.replaceItemValue("billingaddress", billAddress
							.getAddress());
					doc.replaceItemValue("billingaddress_2", billAddress
							.getAddress_2());
					doc.replaceItemValue("billingcity", billAddress.getCity());
					doc
							.replaceItemValue("billingstate", billAddress
									.getState());
					doc.replaceItemValue("billingcountry", billAddress
							.getCountry());
					doc.replaceItemValue("billingpostalcode", billAddress
							.getPostalCode());
					doc
							.replaceItemValue("billingemail", billAddress
									.getEmail());
					doc.replaceItemValue("billingfax", billAddress.getFax());
					doc
							.replaceItemValue("billingphone", billAddress
									.getPhone());
				}
				// save the promoCode and shipping Instructions, orderEmail
				doc.replaceItemValue("orderEmail", orderEmail);
				doc.replaceItemValue("promocode", promoCode);
				doc.replaceItemValue("shippingInstructions", shipInstructions);
				// MCR WSCR-AXZRWL - Additional Parts Order Email Language
				// 04/23/2018 MDZ
				if (StringUtil.equalsIgnoreCase(orderType, "parts")) {
					doc.replaceItemValue("Urgent", urgentCheckBox);
					debugMsg("Value for urgent is " + this.urgentCheckBox);
				}
				// get the order total
				if (StringUtil.equalsIgnoreCase(orderType, "promoitems")) {
					doc.replaceItemValue("PriceTotal",
							getPromoOrderTotal(orderType));
				} else {
					doc
							.replaceItemValue("PriceTotal",
									getOrderTotal(orderType));
				}
				doc.replaceItemValue("form", dispLang.concat("-ordersummary"));

				// save document
				rtn = doc.save(true, true);

				docUnid(doc.getUniversalID()); // added 12/01/2016 by mdz to put
				// save the total price
				if (StringUtil.equalsIgnoreCase(orderType, "promoitems")) {
					grandTotalPrice = getPromoOrderTotal(orderType);
				} else {
					grandTotalPrice = getOrderTotal(orderType);
				}

			} else {
				// throw error
				debugMsg("SalesOrderBean.saveOrder: Orders Database not open");
				System.out
						.println("SalesOrderBean.saveOrder: Orders Database not open");
				new RuntimeException(
						"SalesOrderBean.saveOrder:Orders Database is not open.");
			}
		} catch (Exception e) {
			debugMsg("SalesOrderBean.saveOrder: " + e.toString());
			System.out.println("SalesOrderBean.saveOrder: " + e.toString());
			throw new RuntimeException("SalesOrderBean.saveOrder: "
					+ e.toString());
		}
		return rtn;
	}

	public void reinitOrder(String typeName) {
		// this zeros out the order so all the values are empty
		// save the total price
		grandTotalPrice = getOrderTotal(typeName);
		// empty out the maps
		forkkits = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
		drivertrainingvideos = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
		salesliterature = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
		salesvideos = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
		partsItems = new LinkedHashMap<String, SalesOrderItem>();
		forksItems = new LinkedHashMap<String, SalesOrderItem>();
		pricelists = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
	}

	public String getOrderEmail() {
		return orderEmail;
	}

	public void setOrderEmail(String orderEmail) {
		this.orderEmail = orderEmail;
	}

	public class Address implements Serializable {
		// this class is used to hold the address information
		// used for both shipping and billing
		private static final long serialVersionUID = 1L;
		String person;
		String company;
		String address;
		String address_2;
		String city;
		String state;
		String postalCode;
		String country;
		String email;
		String profileUNID; // holds the profile doc for address
		String phone;
		String fax;

		public String getprofileUNID() {
			return profileUNID;
		}

		public void setprofileUNID(String profileUNID) {
			this.profileUNID = profileUNID;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPerson() {
			return person;
		}

		public void setPerson(String person) {
			this.person = person;
		}

		public String getCompany() {
			return company;
		}

		public void setCompany(String company) {
			this.company = company;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getAddress_2() {
			return address_2;
		}

		public void setAddress_2(String address_2) {
			this.address_2 = address_2;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public String getPostalCode() {
			return postalCode;
		}

		public void setPostalCode(String postalCode) {
			this.postalCode = postalCode;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getFax() {
			return fax;
		}

		public void setFax(String fax) {
			this.fax = fax;
		}

		public Address() {
			// set the profile UNID so it is initially empty
			this.profileUNID = new String("");
		}

		public void setAddress(Session session) {
			// this gets an address from the profiles db
			debugMsg("starting setAddress and unid is " + this.profileUNID);
			if (StringUtil.isNotEmpty(this.profileUNID)) {
				// get the shipping document
				try {
					ConfigBean configBean = (ConfigBean) ExtLibUtil
							.resolveVariable(FacesContext.getCurrentInstance(),
									"configBean");
					String profilesDbname = (String) configBean
							.getValue("UserprofilesDbPath");
					debugMsg("Profiles db is " + profilesDbname);
					Database profilesDb = session.getDatabase(session
							.getServerName(), profilesDbname, false);
					if (profilesDb.isOpen()) {
						// get document by UNID
						Document profileDoc = profilesDb
								.getDocumentByUNID(this.profileUNID);
						if (profileDoc.isValid()) {
							// set the fields

							this.person = profileDoc
									.getItemValueString("person");
							this.company = profileDoc
									.getItemValueString("company");
							this.address = profileDoc
									.getItemValueString("address");
							this.address_2 = profileDoc
									.getItemValueString("address_2");
							this.city = profileDoc.getItemValueString("city");
							this.state = profileDoc.getItemValueString("state");
							this.country = profileDoc
									.getItemValueString("country");
							this.postalCode = profileDoc
									.getItemValueString("postalCode");
							this.email = profileDoc.getItemValueString("email");
							this.phone = profileDoc.getItemValueString("phone");
							this.fax = profileDoc.getItemValueString("fax");
						}
					}

				} catch (Exception e) {
					if (StringUtil.lastIndexOfIgnoreCase(e.toString(),
							"Invalid universal id") >= 0) {
						debugMsg("setShippngAddress: Bad UNID");

					} else {
						// some other exception
						debugMsg("setShippngAddress:Error is " + e.toString());
						throw new RuntimeException(
								"setShippngAddress:Error is " + e.toString());
					}
				}
			}
		}
	}

	public void resetBillAddress() {
		// zero out the billAddress
		billAddress = new Address();
	}

	public String getPromoCode() {
		return promoCode;
	}

	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	@SuppressWarnings("unchecked")
	private LinkedHashMap<String, ArrayList<SalesOrderItem>> loadSalesOrderDocuments(
			String salesType, Session session) {
		// this is used to get all the items the customer can order for a
		// non-parts order
		debugMsg("starting loadSalesOrderDocuments for " + salesType);
		LinkedHashMap<String, ArrayList<SalesOrderItem>> rtnMap = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
		String catName = new String("");
		Vector row;
		SalesOrderItem lineItem;

		String desc;
		String specMsg;
		ViewEntry tempEntry;
		ViewEntry vwEntry;
		String partNum;
		String hasSpecMsg;
		Double price;
		Double maxQty;
		// load up the sales order docs for this type of order
		try {
			// get the orders database
			Database orderDb = session.getDatabase(session.getServerName(),
					OrdersDbName, false);
			View lookupVw = orderDb.getView(salesType);
			ViewNavigator vwNav = lookupVw.createViewNav();

			vwEntry = vwNav.getFirst();
			while (vwEntry != null) {
				row = vwEntry.getColumnValues();

				if (vwEntry.isCategory()) {
					// set Category name
					catName = (String) row.elementAt(1);
					// create a new ArrayList
					ArrayList<SalesOrderItem> lineItemArray = new ArrayList<SalesOrderItem>();

					// create new Map entry
					debugMsg("Category Name is " + catName);
					rtnMap.put(catName, lineItemArray);

				} else if (vwEntry.isDocument()) {
					// the link is a document
					// get the values
					desc = (String) row.elementAt(4);
					specMsg = (String) row.elementAt(8);
					partNum = (String) row.elementAt(2);
					hasSpecMsg = (String) row.elementAt(7);
					price = (Double) row.elementAt(5);
					String rtnMaxQty = row.elementAt(6).toString();

					DecimalFormat decimalFormat = new DecimalFormat("#");

					// convert the max qty to a number, if it is a string use 99
					try {
						double number = decimalFormat.parse(rtnMaxQty)
								.doubleValue();

						maxQty = new Double(number);
					} catch (ParseException e) {

						maxQty = new Double(99);
					}

					/*
					 * if (typeRtn.equalsIgnoreCase("java.lang.String")){
					 * debugMsg("rtnMaxQty is a String"); //maxQty = new
					 * Double(0.0); maxQty = 99.0; } else {
					 * debugMsg("rtnMaxQty is a number "); maxQty = (Double)
					 * rtnMaxQty; }
					 */

					lineItem = new SalesOrderItem(partNum, desc, price, maxQty,
							hasSpecMsg, specMsg);
					// put in array
					rtnMap.get(catName).add(lineItem);
				}
				tempEntry = vwNav.getNext(vwEntry);
				vwEntry.recycle();
				vwEntry = tempEntry;
			}
		} catch (Exception e) {
			debugMsg("Error in loadSalesOrderDocuments for " + salesType
					+ " error is: " + e.toString());
			// throw new
			// RuntimeException("Error in loadSalesOrderDocuments for " +
			// salesType + " error is: " + e.toString());
		}
		debugMsg("size of rtnMap is " + rtnMap.size());
		return rtnMap;
	}

	public boolean isTypeEmpty(String typeName) {
		// returns false if the Map is populated for that type of order
		boolean rtn;
		if (StringUtil.equalsIgnoreCase(typeName, "forkkit")) {
			rtn = forkkits.isEmpty();
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesliterature")) {
			rtn = salesliterature.isEmpty();
		} else if (StringUtil
				.equalsIgnoreCase(typeName, "drivertrainingvideos")) {
			rtn = drivertrainingvideos.isEmpty();
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesvideos")) {
			rtn = salesvideos.isEmpty();
		} else if (StringUtil.equalsIgnoreCase(typeName, "pricelists")) {
			rtn = pricelists.isEmpty();
		} else if (StringUtil.equalsIgnoreCase(typeName, "promoitems")) {
			rtn = promoitems.isEmpty();
		} else {
			rtn = false;
			throw new RuntimeException(
					"Error in SalesOrderBean: isTypeEmpty was passed bad parameter");
		}
		return rtn;
	}

	public ArrayList<SalesOrderItem> getLineItems(String typeName,
			String catName) {
		// used in inner repeat to get the items that appear under each category
		// first parameter is the type, like forkkits
		// second parameter is the category for each type
		debugMsg("getting " + catName);
		ArrayList<SalesOrderItem> rtnLineItems = new ArrayList<SalesOrderItem>();
		// find out which type of sales order item Map to get
		if (StringUtil.equalsIgnoreCase(typeName, "forkkit")) {
			rtnLineItems = new ArrayList<SalesOrderItem>(forkkits.get(catName));
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesliterature")) {
			rtnLineItems = new ArrayList<SalesOrderItem>(salesliterature
					.get(catName));
		} else if (StringUtil
				.equalsIgnoreCase(typeName, "drivertrainingvideos")) {
			rtnLineItems = new ArrayList<SalesOrderItem>(drivertrainingvideos
					.get(catName));
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesvideos")) {
			rtnLineItems = new ArrayList<SalesOrderItem>(salesvideos
					.get(catName));
		} else if (StringUtil.equalsIgnoreCase(typeName, "pricelists")) {
			rtnLineItems = new ArrayList<SalesOrderItem>(pricelists
					.get(catName));
		}
		return rtnLineItems;
	}

	public ArrayList<String> getSalesOrderByType(String typeName) {
		// pass in a value corresponding to the string comparision below to get
		// the
		// categories used
		debugMsg("Getting SalesOrderByType for " + typeName);
		ArrayList<String> rtnArrayList = new ArrayList<String>();
		if (StringUtil.equalsIgnoreCase(typeName, "forkkit")) {
			debugMsg("getting forkkits count is " + forkkits.size());
			rtnArrayList = new ArrayList<String>(forkkits.keySet());
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesliterature")) {
			rtnArrayList = new ArrayList<String>(salesliterature.keySet());
		} else if (StringUtil
				.equalsIgnoreCase(typeName, "drivertrainingvideos")) {
			rtnArrayList = new ArrayList<String>(drivertrainingvideos.keySet());
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesvideos")) {
			rtnArrayList = new ArrayList<String>(salesvideos.keySet());
		} else if (StringUtil.equalsIgnoreCase(typeName, "pricelists")) {
			rtnArrayList = new ArrayList<String>(pricelists.keySet());
		}

		return rtnArrayList;
	}

	public ArrayList<SalesOrderItem> getOrderedItems(String typeName) {
		// This gets the ordered items
		ArrayList<SalesOrderItem> rtnArrayList = new ArrayList<SalesOrderItem>();
		if (StringUtil.equalsIgnoreCase(typeName, "forkkit")) {
			rtnArrayList = getOrderedItemsArrayList(forkkits);
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesliterature")) {
			rtnArrayList = getOrderedItemsArrayList(salesliterature);
		} else if (StringUtil
				.equalsIgnoreCase(typeName, "drivertrainingvideos")) {
			rtnArrayList = getOrderedItemsArrayList(drivertrainingvideos);
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesvideos")) {
			rtnArrayList = getOrderedItemsArrayList(salesvideos);
		} else if (StringUtil.equalsIgnoreCase(typeName, "parts")) {
			rtnArrayList = getOrderedParts();
		} else if (StringUtil.equalsIgnoreCase(typeName, "forks")) {
			rtnArrayList = getOrderedForks();
		} else if (StringUtil.equalsIgnoreCase(typeName, "pricelists")) {
			rtnArrayList = getOrderedItemsArrayList(pricelists);
		} else if (StringUtil.equalsIgnoreCase(typeName, "Dealer Claim")) {
			rtnArrayList = getOrderedItemsArrayList(dealerclaims);
		}
		return rtnArrayList;
	}

	private ArrayList<SalesOrderItem> getOrderedItemsArrayList(
			LinkedHashMap<String, ArrayList<SalesOrderItem>> itemMap) {
		ArrayList<SalesOrderItem> rtnArrayList = new ArrayList<SalesOrderItem>();
		// loop through each category
		for (ArrayList<SalesOrderItem> lineItemArray : itemMap.values()) {
			// now loop through each member of the arrray list
			for (SalesOrderItem lineItem : lineItemArray) {
				if (lineItem.getOrderQty() > 0) {
					// copy item to rtnArrayList
					rtnArrayList.add(lineItem);
				}
			}
		}
		return rtnArrayList;

	}

	public void initializeDocuments(String typeName, Session session) {
		// setup a new order, load up the stuff a user can order
		debugMsg("starting initializeDocuments for " + typeName);
		billAddress = new Address();
		shipAddress = new Address();
		promoCode = new String("");
		shipInstructions = new String("");
		orderEmail = new String("");
		billingAddressSameAsShip = new String("");
		try {
			if (StringUtil.equalsIgnoreCase(typeName, "forkkit")) {
				forkkits = loadSalesOrderDocuments("staticForkKit", session);
			} else if (StringUtil.equalsIgnoreCase(typeName, "salesliterature")) {
				salesliterature = loadSalesOrderDocuments(
						"staticsalesliterature", session);
			} else if (StringUtil.equalsIgnoreCase(typeName,
					"drivertrainingvideos")) {
				drivertrainingvideos = loadSalesOrderDocuments(
						"staticdrivertrainingvideos", session);
			} else if (StringUtil.equalsIgnoreCase(typeName, "salesvideos")) {
				salesvideos = loadSalesOrderDocuments("staticsalesvideos",
						session);
			} else if (StringUtil.equalsIgnoreCase(typeName, "pricelists")) {
				pricelists = loadSalesOrderDocuments("staticpricelists",
						session);
			} else if (StringUtil.equalsIgnoreCase(typeName, "promoitems")) {
				promoitems = loadPromoOrderDocuments("staticSpecPromo", session);
			} else {
				throw new RuntimeException(
						"Error in SalesOrderBean: initializeDocuments was passed bad parameter");
			}
		} catch (Exception e) {
			debugMsg("Error in initialize Documents for " + typeName
					+ " error is: " + e.toString());
		}
	}

	public SalesOrderBean() {
		// set location of Orders db

		ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		OrdersDbName = (String) configBean.getValue("OrdersDbPath");
		// initialize maps as empty
		forkkits = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
		drivertrainingvideos = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
		salesliterature = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
		salesvideos = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
		pricelists = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();

	}

	public boolean checkQtyInput(String typeName) {
		// validate qty input - returns true as long as one qty is greater than
		// 0
		boolean rtn = false;
		LinkedHashMap<String, ArrayList<SalesOrderItem>> itemMap = new LinkedHashMap<String, ArrayList<SalesOrderItem>>();
		// if a non-parts order than return to itemMap the correct map to use
		if (StringUtil.equalsIgnoreCase(typeName, "forkkit")) {
			itemMap = forkkits;
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesliterature")) {
			itemMap = salesliterature;
		} else if (StringUtil
				.equalsIgnoreCase(typeName, "drivertrainingvideos")) {
			itemMap = drivertrainingvideos;
		} else if (StringUtil.equalsIgnoreCase(typeName, "salesvideos")) {
			itemMap = salesvideos;
		} else if (StringUtil.equalsIgnoreCase(typeName, "pricelists")) {
			itemMap = pricelists;
		}
		// see if parts order, if so, do the first if
		if (StringUtil.equalsIgnoreCase(typeName, "parts")) {
			// if partsItem is empty than there is no parts on the order
			if (!partsItems.isEmpty()) {
				// loop through parts map to add up the quantities
				for (SalesOrderItem lineItem : partsItems.values()) {
					if (lineItem.getOrderQty() > 0) {
						// copy item to rtnArrayList
						rtn = true;
					}
				}
			}
			// 6/20/17 DSR - next check if forks order and do the needful
		} else if (StringUtil.equalsIgnoreCase(typeName, "forks")) {
			// if forksItem is empty than there are no forks on the order
			if (!forksItems.isEmpty()) {
				// loop through parts map to add up the quantities
				for (SalesOrderItem lineItem : forksItems.values()) {
					if (lineItem.getOrderQty() > 0) {
						// copy item to rtnArrayList
						rtn = true;
					}
				}
			}
		} else if (!itemMap.isEmpty()) {
			// must be a non-Parts/forks order
			// check the quantities
			for (ArrayList<SalesOrderItem> lineItemArray : itemMap.values()) {
				// now loop through each member of the array list
				for (SalesOrderItem lineItem : lineItemArray) {
					if (lineItem.getOrderQty() > 0) {
						// copy item to rtnArrayList
						rtn = true;
					}
				}
			}
		} else {
			// throw error
			debugMsg("SalesOrderBean:checkQtyInput got invalid parameter");
			throw new RuntimeException(
					"SalesOrderBean:checkQtyInput got invalid parameter");
		}

		return rtn;
	}

	public ArrayList<SalesOrderItem> getOrderedItemsFromDoc(String unid,
			Session session) {
		// pass in an existing order document
		// return an array of all the ordered items on that document
		ArrayList<SalesOrderItem> rtnItems = new ArrayList<SalesOrderItem>();
		ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		String ordersDbname = (String) configBean.getValue("OrdersDbPath");
		SalesOrderItem orderedItem;
		Double orderQty;
		Double price;
		String description;
		String partNum;
		debugMsg("Orders db is " + ordersDbname);
		try {
			Database ordersDb = session.getDatabase(session.getServerName(),
					ordersDbname, false);
			if (ordersDb.isOpen()) {
				// get document by UNID
				Document orderDoc = ordersDb.getDocumentByUNID(unid);
				if (orderDoc.isValid()) {
					// loop through the quantity and add to array if valid
					// assume no more than 200 items

					for (int i = 0; i < 200; i++) {
						// if the qty is greater than zero add to the rtnItems
						// array
						if (orderDoc.getItemValueDouble("ItemCount_"
								+ Integer.toString(i)) > 0) {
							debugMsg("found one at" + Integer.toString(i));
							orderQty = orderDoc.getItemValueDouble("ItemCount_"
									+ Integer.toString(i));
							debugMsg("Qty is " + orderQty);
							price = orderDoc.getItemValueDouble("Price_"
									+ Integer.toString(i));
							description = orderDoc
									.getItemValueString("Description_"
											+ Integer.toString(i));
							partNum = orderDoc
									.getItemValueString("OrderNumber_"
											+ Integer.toString(i));
							orderedItem = new SalesOrderItem(partNum,
									description, orderQty, price);
							rtnItems.add(orderedItem);
						}
					}
				}
			}
		} catch (Exception e) {
			if (StringUtil.lastIndexOfIgnoreCase(e.toString(),
					"Invalid universal id") >= 0) {
				debugMsg("getOrderedItemsFromDoc: Bad UNID");

			} else {
				// some other exception
				debugMsg("getOrderedItemsFromDoc:Error is " + e.toString());
				throw new RuntimeException("getOrderedItemsFromDoc:Error is "
						+ e.toString());
			}
		}
		return rtnItems;
	}

	public ArrayList<SalesOrderItem> getOrderedParts() {
		// this returns the list of ordered parts as an ArrayList
		ArrayList<SalesOrderItem> rtnItems = new ArrayList<SalesOrderItem>();
		for (SalesOrderItem partItem : partsItems.values()) {
			rtnItems.add(partItem);
		}
		return rtnItems;
	}

	// 6/20/17 DSR - add forks ordering
	public ArrayList<SalesOrderItem> getOrderedForks() {
		// this returns the list of ordered parts as an ArrayList
		ArrayList<SalesOrderItem> rtnItems = new ArrayList<SalesOrderItem>();
		for (SalesOrderItem partItem : forksItems.values()) {
			rtnItems.add(partItem);
		}
		return rtnItems;
	}

	public void removeForkItem(String forkNum) {
		// removes an ordered Part Item
		// used for ordering parts only
		if (forksItems.containsKey(forkNum)) {
			// if present, remove the item
			forksItems.remove(forkNum);
		}
	}

	public void removePartItem(String partNum) {
		// removes an ordered Part Item
		// used for ordering parts only
		if (partsItems.containsKey(partNum)) {
			// if present, remove the item
			partsItems.remove(partNum);
		}
	}

	@SuppressWarnings("unchecked")
	public String addForkItem(String partNumber, String origPN) {
		// this will get a new fork number and add to the order
		// only used when ordering forks
		// returns badforknumber if fork number is not found
		// returns duplicate if fork is already on order
		// returns added if fork was found and added to order
		String rtn = new String("");
		String itemNumber = new String("");
		JDESupport jde = new JDESupport();
		// print out keys
		for (String fork : forksItems.keySet()) {
			debugMsg("Forks key is \"" + fork + "\"");
		}
		String fixedPartNumber = partNumber.toUpperCase();
		Double maxQty = new Double(99); // this is the max number of items that
		// can be ordered
		// this calls out to JDE via the JDESupport to get the fork information

		// See the getForkInformation for what the rtnFork Map holds (copied
		// below)
		/*
		 * rtnMap is what gets returned... rtnMap.put("description",
		 * rs.getString("imdsc1").trim()); rtnMap.put("price",
		 * rs.getDouble("bpuprc") / 10000); rtnMap.put("itemnumber",
		 * rs.getString("imlitm").trim()); rtnMap.put("status", "good");
		 */

		Map rtnFork = jde.getForkInformation(fixedPartNumber,
				this.accountNumber, this.region);
		if (rtnFork.isEmpty()) {
			debugMsg("rtnFork is empty");
			rtn = "badforknumber";
		}
		if (forksItems.containsKey(fixedPartNumber)) {
			debugMsg("duplicate fork item on order");
			rtn = "duplicate";
		} else if (StringUtil.equalsIgnoreCase((String) rtnFork.get("status"),
				"good")) {
			// add fork
			debugMsg("found a good fork");
			itemNumber = (String) rtnFork.get("itemnumber");
			debugMsg("itemNumber: [" + itemNumber.trim() + "]");
			debugMsg("origPN:  [" + origPN.trim() + "]");
			if (itemNumber.trim().equals(origPN.trim())) {
				itemNumber = (String) rtnFork.get("itemnumber");
			} else {
				itemNumber = (String) origPN + " (" + rtnFork.get("itemnumber")
						+ ")";
			}
			String description = (String) rtnFork.get("description");
			debugMsg("itemNumber is \"" + itemNumber + "\"");

			Double price = (Double) rtnFork.get("price");
			SalesOrderItem newItem = new SalesOrderItem(itemNumber,
					description, new Double(1.0), price, maxQty, "", "");
			forksItems.put(itemNumber, newItem);
			rtn = "added";
		} else {
			rtn = "badforknumber";
		}

		return rtn;
	}

	@SuppressWarnings("unchecked")
	public String addPartItem(String partNumber, String origPN) {
		// this will get a new part number and add to the order
		// only used when ordering parts
		// returns badpartnumber if part number is not found
		// returns duplicate if part is already on order
		// returns added if part was found and added to order
		String rtn = new String("");
		String rtnCrossoverPart;
		JDESupport jde = new JDESupport();
		// print out keys
		for (String part : partsItems.keySet()) {
			debugMsg("Parts key is \"" + part + "\"");
		}
		/*
		 * if (Double.isNaN(quantity)) { quantity = 1000;
		 * debugMsg("Quantity is " + quantity); }else{ debugMsg("Quantity is: "
		 * + quantity); }
		 */
		String fixedPartNumber = partNumber.toUpperCase();
		Double maxQty = new Double(99); // this is the max number of items that
		// can be ordered
		// this calls out to JDE via the JDESupport to get the part information

		// See the getPartInformation for what the rtnPart Map holds (copied
		// below)
		/*
		 * rtnMap is what gets returned... rtnMap.put("description",
		 * rs.getString("imdsc1").trim()); rtnMap.put("price",
		 * rs.getDouble("bpuprc") / 10000); rtnMap.put("itemnumber",
		 * rs.getString("imlitm").trim()); rtnMap.put("status", "good");
		 */

		Map rtnPart = jde.getPartInformation(fixedPartNumber,
				this.accountNumber, this.region);
		// NEED TO CHECK CROSS REFERENCE TABLE - if it is a badpartNumber. Maybe
		// it is there MDZ MCR WSCR-AYESXF
		debugMsg("addPartItem() - rtnPart.staus = " + rtnPart.get("status"));
		if (rtnPart.get("status") == "badpartnumber") {
			debugMsg("addPartItem() - checking cross reference table");
			rtnCrossoverPart = getPartCrossoverNum(fixedPartNumber);
			debugMsg("addPartItem() - rtnCrossoverPart = [" + rtnCrossoverPart
					+ "]");
			if (rtnCrossoverPart.isEmpty()) {
				debugMsg("addPartItem() - 2.0. rtnCrossoverPart is empty");
				rtn = "badpartnumber";
			} else {
				debugMsg("addPartItem() - 3.0. rtnCrossoverPart returned ["
						+ rtnCrossoverPart + "]");
				partNumber = rtnCrossoverPart;
				fixedPartNumber = partNumber.toUpperCase();
				rtnPart = jde.getPartInformation(fixedPartNumber,
						this.accountNumber, this.region);
				debugMsg("addPartItem() - 3.0.1. rtnPart.staus = "
						+ rtnPart.get("status"));
			}
		}
		// END OF CHECKING CROSS REFERNCE CODE
		// if(rtnPart.get("status"))
		if (rtnPart.isEmpty()) {
			debugMsg("rtnPart is empty");
			rtn = "badpartnumber";
		}
		if (partsItems.containsKey(fixedPartNumber)) {
			debugMsg("duplicate part item on order");
			rtn = "duplicate";
		} else if (StringUtil.equalsIgnoreCase((String) rtnPart.get("status"),
				"good")) {
			// add part
			debugMsg("found a good part");
			String itemNumber = (String) rtnPart.get("itemnumber");
			String description = (String) rtnPart.get("description");
			debugMsg("itemNumber is \"" + itemNumber + "\"");
			debugMsg("origPN:  [" + origPN.trim() + "]");
			if (itemNumber.trim().equals(origPN.trim())) {
				itemNumber = (String) rtnPart.get("itemnumber");
			} else {
				itemNumber = (String) rtnPart.get("itemnumber")
						+ " (replaces PN " + origPN + ")";
			}

			Double price = (Double) rtnPart.get("price");
			SalesOrderItem newItem = new SalesOrderItem(itemNumber,
					description, new Double(1.0), price, maxQty, "", "");
			partsItems.put(itemNumber, newItem);
			rtn = "added";
		} else {
			rtn = "badpartnumber";
		}

		return rtn;
	}

	public String getForkCrossoverNum(String forkNum, String acctNum) {
		String ForkNumber = new String("");
		String custForkNumber = forkNum.toUpperCase();
		debugMsg("getCrossoverNum - forkNum: [" + ForkNumber + "]");
		String rtn = new String("");
		JDESupport jde = new JDESupport();
		Map rtnFork = jde.getForkCrossOverInformation(custForkNumber, acctNum,
				this.region);
		if (rtnFork.isEmpty()) {
			debugMsg("rtnFork is empty");
			rtn = forkNum;
		}
		debugMsg("getCrossoverNum - returnFork.get(cascorpNum): ["
				+ rtnFork.get("cascorpNum") + "]");
		ForkNumber = (String) rtnFork.get("cascorpNum");
		debugMsg("getCrossoverNum - forkNum: [" + ForkNumber + "]");

		rtn = ForkNumber;
		return rtn;
	}

	public String getPartCrossoverNum(String partNum) {
		String partNumber = new String("");
		debugMsg("getPartCrossoverNum - partNum: [" + partNum + "]");
		String rtn = new String("");
		JDESupport jde = new JDESupport();
		Map rtnPart = jde.getPartCrossOverInformation(partNum, this.region);
		if (rtnPart.isEmpty()) {
			debugMsg("rtnPart is empty");
			rtn = partNum;
		}
		debugMsg("getPartCrossoverNum - returnPart.get(cascorpNum): ["
				+ rtnPart.get("cascorpNum") + "]");
		partNumber = (String) rtnPart.get("cascorpNum");
		debugMsg("getCrossoverNum - Num: [" + partNumber + "]");

		rtn = partNumber;
		return rtn;
	}

	@SuppressWarnings("unchecked")
	public String addForkItemQty(String partNumber, String quantity,
			String origPN) {
		// this will get a new part number and add to the order
		// only used when ordering forks
		// returns badforknumber if fork number is not found
		// returns duplicate if fork is already on order
		// returns added if fork was found and added to order
		String rtn = new String("");
		String itemNumber = new String("");
		JDESupport jde = new JDESupport();
		// print out keys
		for (String fork : forksItems.keySet()) {
			debugMsg("Forks key is \"" + fork + "\"");
		}
		// added by mdz (09/13/2016) for quantity check and convert to integer
		// for use as qty
		if (isInteger(quantity)) {
			debugMsg("Quantity is " + quantity);
		} else {
			quantity = "1";
			debugMsg("Quantity defaulted to " + quantity);
		}
		double orderQty = new Double(quantity);
		// end of code added

		String fixedPartNumber = partNumber.toUpperCase();
		Double maxQty = new Double(99); // this is the max number of items that
		// can be ordered
		// this calls out to JDE via the JDESupport to get the fork information

		// See the getForkInformation for what the rtnFork Map holds (copied
		// below)
		/*
		 * rtnMap is what gets returned... rtnMap.put("description",
		 * rs.getString("imdsc1").trim()); rtnMap.put("price",
		 * rs.getDouble("bpuprc") / 10000); rtnMap.put("itemnumber",
		 * rs.getString("imlitm").trim()); rtnMap.put("status", "good");
		 */

		Map rtnFork = jde.getForkInformation(fixedPartNumber,
				this.accountNumber, this.region);
		if (rtnFork.isEmpty()) {
			debugMsg("rtnFork is empty");
			rtn = "badforknumber";
		}
		if (forksItems.containsKey(fixedPartNumber)) {
			debugMsg("duplicate fork item on order");
			rtn = "duplicate";
		} else if (StringUtil.equalsIgnoreCase((String) rtnFork.get("status"),
				"good")) {
			// add fork
			debugMsg("found a good fork");
			itemNumber = (String) rtnFork.get("itemnumber");
			if (itemNumber.trim().equals(origPN.trim())) {
				itemNumber = (String) rtnFork.get("itemnumber");
			} else {
				itemNumber = origPN + " (" + (String) rtnFork.get("itemnumber")
						+ ")";
			}
			debugMsg("Item #: " + itemNumber);
			String description = (String) rtnFork.get("description");
			debugMsg("itemNumber is \"" + itemNumber + "\"");

			Double price = (Double) rtnFork.get("price");
			SalesOrderItem newItem = new SalesOrderItem(itemNumber,
					description, orderQty, price, maxQty, "", "");
			forksItems.put(itemNumber, newItem);
			rtn = "added";
		} else {
			rtn = "badforknumber";
		}

		return rtn;
	}

	@SuppressWarnings("unchecked")
	public String addPartItemQty(String partNumber, String quantity) {
		// this will get a new part number and add to the order
		// only used when ordering parts
		// returns badpartnumber if part number is not found
		// returns duplicate if part is already on order
		// returns added if part was found and added to order
		String rtn = new String("");
		String origPN = partNumber;
		String rtnCrossoverPart;
		JDESupport jde = new JDESupport();
		// print out keys
		for (String part : partsItems.keySet()) {
			debugMsg("addPartItemQty() - Parts key is \"" + part + "\"");
		}
		// added by mdz (09/13/2016) for quantity check and convert to integer
		// for use as qty
		if (isInteger(quantity)) {
			debugMsg("Quantity is " + quantity);
		} else {
			quantity = "1";
			debugMsg("Quantity defaulted to " + quantity);
		}
		double orderQty = new Double(quantity);
		// end of code added

		String fixedPartNumber = partNumber.toUpperCase();
		Double maxQty = new Double(99); // this is the max number of items that
		// can be ordered
		// this calls out to JDE via the JDESupport to get the part information

		// See the getPartInformation for what the rtnPart Map holds (copied
		// below)
		/*
		 * rtnMap is what gets returned... rtnMap.put("description",
		 * rs.getString("imdsc1").trim()); rtnMap.put("price",
		 * rs.getDouble("bpuprc") / 10000); rtnMap.put("itemnumber",
		 * rs.getString("imlitm").trim()); rtnMap.put("status", "good");
		 */

		Map rtnPart = jde.getPartInformation(fixedPartNumber,
				this.accountNumber, this.region);
		debugMsg("addPartItemQty() - rtnPart.staus = " + rtnPart.get("status"));
		// NEED TO CHECK CROSS REFERENCE TABLE - maybe it is there MDZ - MCR
		// WSCR-AYESXF
		if (rtnPart.get("status") == "badpartnumber") {
			// NEED TO CHECK CROSS REFERENCE TABLE - maybe it is there
			debugMsg("addPartItemQty() - checking cross reference table");
			rtnCrossoverPart = getPartCrossoverNum(fixedPartNumber);
			debugMsg("addPartItemQty() - rtnCrossoverPart = ["
					+ rtnCrossoverPart + "]");
			if (rtnCrossoverPart.isEmpty()) {
				debugMsg("addPartItemQty() - 2.0. rtnCrossoverPart is empty");
				rtn = "badpartnumber";
			} else {
				debugMsg("addPartItemQty() - 3.0. rtnCrossoverPart returned ["
						+ rtnCrossoverPart + "]");
				partNumber = rtnCrossoverPart;
				fixedPartNumber = partNumber.toUpperCase();
				rtnPart = jde.getPartInformation(fixedPartNumber,
						this.accountNumber, this.region);
				debugMsg("addPartItemQty() - 3.0.1. rtnPart.staus = "
						+ rtnPart.get("status"));
			}
		}
		// END OF CHECKING CROSS REFERNCE CODE
		if (rtnPart.isEmpty()) {
			debugMsg("rtnPart is empty");
			rtn = "badpartnumber";
		}
		if (partsItems.containsKey(fixedPartNumber)) {
			debugMsg("addPartItemQty() - duplicate part item on order");
			rtn = "duplicate";
		} else if (StringUtil.equalsIgnoreCase((String) rtnPart.get("status"),
				"good")) {
			// add part
			debugMsg("addPartItemQty() - found a good part");
			String itemNumber = (String) rtnPart.get("itemnumber");
			String description = (String) rtnPart.get("description");
			debugMsg("addPartItemQty() - itemNumber is \"" + itemNumber + "\"");
			if (itemNumber.trim().equals(origPN.trim())) {
				itemNumber = (String) rtnPart.get("itemnumber");
			} else {
				itemNumber = (String) rtnPart.get("itemnumber")
						+ " (replaces PN " + origPN + ")";
			}
			Double price = (Double) rtnPart.get("price");
			SalesOrderItem newItem = new SalesOrderItem(itemNumber,
					description, orderQty, price, maxQty, "", "");
			partsItems.put(itemNumber, newItem);
			rtn = "added";
		}
		// WSCR-ASES6S - Vendors / DXF Files - add code to not allow vendors to
		// order
		else if (StringUtil.equalsIgnoreCase((String) rtnPart.get("status"),
				"accountbad")) {
			rtn = "accountbad";
		} // WSCR-ASES6S - Vendors / DXF File end of code added
		else {
			rtn = "badpartnumber";
		}

		return rtn;
	}

	// added by mdz to check if string is a number
	public boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String addTestPartItem(String partNumber) {
		// this is only for testing, can use only 123 and 789 for part numbers
		// this will get a new part number and add to the order
		// only used when ordering parts
		// returns badpartnumber if part number is not found
		// returns duplicate if part is already on order
		// returns added if part was found and added to order
		String rtn = new String("");
		if (partsItems.containsKey(partNumber)) {
			rtn = "duplicate";
		} else if (partNumber.equalsIgnoreCase("123")) {
			// add dummy part
			SalesOrderItem newItem = new SalesOrderItem(partNumber, "part 1",
					new Double(1.0), new Double(25.0), new Double(99), "", "");
			partsItems.put(partNumber, newItem);
			rtn = "added";
		} else if (partNumber.equalsIgnoreCase("789")) {
			SalesOrderItem newItem = new SalesOrderItem(partNumber, "part 2",
					new Double(1.0), new Double(25.0), new Double(99), "", "");
			partsItems.put(partNumber, newItem);
			rtn = "added";
		} else {
			rtn = "badpartnumber";
		}

		return rtn;
	}

	// following added 03/27/2017 for dealer claims MCR WSCR-AKHTEC ============
	@SuppressWarnings("unchecked")
	public boolean saveDealerClaim(String orderType, Session session) {
		// this is for dealer claims intended to capture address info and
		// pass it onto the domino document
		boolean rtn = false;
		// String ordLang;
		debugMsg("inside saveDealerClaim");
		Item item;
		ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		String ordersDbname = (String) configBean.getValue("OrdersDbPath");
		// FacesContext facesContext = FacesContext.getCurrentInstance();
		Map sessionScope = (Map) ExtLibUtil.resolveVariable(FacesContext
				.getCurrentInstance(), "sessionScope");
		debugMsg("Profiles db is " + ordersDbname);
		String dispLang = (String) sessionScope.get("LanguageP");
		debugMsg("LanguageP is " + dispLang);
		//System.out.println("LanguageP is " + dispLang);

		// get the language and region based on the user's company
		// this will more accurately allow processing by proper Cascade person
		String region = (String) sessionScope.get("region");
		String cc = (String) sessionScope.get("countryCode");
		debugMsg("cc = |" + cc + "|");
		debugMsg("region = |" + region + "|");
		//System.out.println("cc = |" + cc + "|");
		/*
		 * if (cc.trim().equals("US") || cc.trim().equals("GB")) {
		 * debugMsg("cc = US or GB"); System.out.println("cc = US or GB");
		 * ordLang = region + "-en"; } else { debugMsg("cc = something");
		 * System.out.println("cc = something"); ordLang = region + "-" +
		 * cc.toLowerCase(); } debugMsg("ordLang = " + ordLang);
		 * System.out.println("ordLang = " + ordLang);
		 */
		//System.out.println("now going to try");
		try {
			//System.out.println("Entering try");
			// get orders database
			Database ordersDb = session.getDatabase(session.getServerName(),
					ordersDbname, false);
			if (ordersDb.isOpen()) {
				// create new orders document
				Document doc = ordersDb.createDocument();
				// set the form for the document
				doc.replaceItemValue("Form", "DealerClaim");
				// create vector to hold who is an editor
				Vector<String> editorArray = new Vector<String>();
				editorArray.add("[EditAllDocs]");

				item = doc.replaceItemValue("AccessEditor", editorArray);
				item.setAuthors(true);
				String username = DominoUtils.getCurrentSession()
						.getEffectiveUserName();
				debugMsg("username=" + username);
				//System.out.println("username=  " + username);
				String reader;
				if (!username.equalsIgnoreCase("Anonymous")) {
					reader = username;
				} else {
					reader = "[EditAllDocs],[EditDealerClaim]";
				}
				//System.out.println("reader=  " + reader);
				item = doc.replaceItemValue("OrderOwner", reader);
				item.setReaders(true);
				// set order date
				DateTime dt = session.createDateTime("Today");
				dt.setNow();
				doc.replaceItemValue("OrderDate", dt);
				setOrderDate(dt.toJavaDate());
				// set order id
				String id = doc.getUniversalID();
				doc.replaceItemValue("OrderID", id);
				doc.replaceItemValue("OrderStatus", "New");
				// set the OrderForm field per the specs in the spreadsheet
				doc.replaceItemValue("OrderForm", "dealerclaims");
				//System.out.println("orderType is " + orderType);
				doc.replaceItemValue("ordertype", "Dealer Claim");
				doc.replaceItemValue("MsgLanguage", dispLang);
				// doc.replaceItemValue("OrdLanguage", ordLang);
				// set AuthorAccessFulfillment
				doc.replaceItemValue("AuthorAccessFulfillment",
						"[EditDealerClaim]");
				// set the shipping info
				doc.replaceItemValue("shippingcompany", shipAddress
						.getCompany());
				doc.replaceItemValue("shippingperson", shipAddress.getPerson());
				doc.replaceItemValue("shippingaddress", shipAddress
						.getAddress());
				doc.replaceItemValue("shippingaddress_2", shipAddress
						.getAddress_2());
				doc.replaceItemValue("shippingcity", shipAddress.getCity());
				doc.replaceItemValue("shippingstate", shipAddress.getState());
				doc.replaceItemValue("shippingcountry", shipAddress
						.getCountry());
				doc.replaceItemValue("shippingPostalcode", shipAddress
						.getPostalCode());
				doc.replaceItemValue("shippingEmail", shipAddress.getEmail());
				doc.replaceItemValue("orderEmail", shipAddress.getEmail());
				doc.replaceItemValue("shippingFax", shipAddress.getFax());
				doc.replaceItemValue("shippingPhone", shipAddress.getPhone());

				// save the billing address from the billAddress object
				doc
						.replaceItemValue("billingcompany", billAddress
								.getCompany());
				doc.replaceItemValue("billingperson", billAddress.getPerson());
				doc
						.replaceItemValue("billingaddress", billAddress
								.getAddress());
				doc.replaceItemValue("billingaddress_2", billAddress
						.getAddress_2());
				doc.replaceItemValue("billingcity", billAddress.getCity());
				doc.replaceItemValue("billingstate", billAddress.getState());
				doc
						.replaceItemValue("billingcountry", billAddress
								.getCountry());
				doc.replaceItemValue("billingpostalcode", billAddress
						.getPostalCode());
				if (sessionScope == null) {
					doc.replaceItemValue("serviceContract",
							"sessionScope is null");
				} else if (sessionScope.get("contract") == null) {
					doc.replaceItemValue("serviceContract",
							"sessionScope.contract is null");
				} else {
					doc.replaceItemValue("serviceContract", sessionScope
							.get("contract"));
				}
				// doc.replaceItemValue("serviceContract",
				// sessionScope.get("serviceContract"));
				doc.replaceItemValue("modelCatalog", sessionScope.get("model"));
				doc.replaceItemValue("serialNbr", sessionScope.get("serial"));
				doc
						.replaceItemValue("dateInstall", sessionScope
								.get("install"));
				doc.replaceItemValue("pressure", sessionScope.get("pressure"));
				doc.replaceItemValue("flow", sessionScope.get("flow"));
				doc.replaceItemValue("dateComplete", sessionScope
						.get("complete"));
				doc.replaceItemValue("HFC", sessionScope.get("hfc"));
				doc.replaceItemValue("Load", sessionScope.get("load"));
				doc.replaceItemValue("Digital", sessionScope.get("digital"));
				doc.replaceItemValue("truckModel", sessionScope
						.get("truckModel"));
				doc.replaceItemValue("truckSerial", sessionScope
						.get("truckSerial"));

				// save document
				rtn = doc.save(true, true);

				// xx

			} else {
				// throw error
				debugMsg("SalesOrderBean.saveDealerClaim: Orders Database not open");
				System.out
						.println("SalesOrderBean.saveDealerClaim: Orders Database not open");
				new RuntimeException(
						"SalesOrderBean.saveDealerClaim:Orders Database is not open.");
			}
		} catch (Exception e) {

			return false;
		}
		return rtn;
	}

	public class DealerClaim implements Serializable {
		// this class is used to hold the Dealer Claim information needed
		private static final long serialVersionUID = 1L;

		public DealerClaim() {

		}

		String serviceContract;
		String modelCatalog;
		String serialNbr;
		String dateInstall;
		String pressure;
		String flow;
		String dateComplete;
		String hfc;
		String load;
		String digital;
		String truckModel;
		String truckSerial;

		public String getServiceContract() {
			return serviceContract;
		}

		public void setServiceContract(String serviceContract) {
			this.serviceContract = serviceContract;
		}

		public String getmodelCatalog() {
			return modelCatalog;
		}

		public void setModelCatalog(String modelCatalog) {
			this.modelCatalog = modelCatalog;
		}

		public String getSerialNbr() {
			return serialNbr;
		}

		public void setSerialNbr(String serialNbr) {
			this.serialNbr = serialNbr;
		}

		public String getDateInstall() {
			return dateInstall;
		}

		public void SetDateInstall(String dateInstall) {
			this.dateInstall = dateInstall;
		}

		public String getPressure() {
			return pressure;
		}

		public void setPressure(String pressure) {
			this.pressure = pressure;
		}

		public String getFlow() {
			return flow;
		}

		public void setFlow(String flow) {
			this.flow = flow;
		}

		public String getDateComplete() {
			return dateComplete;
		}

		public void getDateComplete(String dateComplete) {
			this.dateComplete = dateComplete;
		}

		public String getHFC() {
			return hfc;
		}

		public void setHFC(String hfc) {
			this.hfc = hfc;
		}

		public String getLoad() {
			return load;
		}

		public void setLoad(String load) {
			this.load = load;
		}

		public String getDigital() {
			return digital;
		}

		public void setDigital(String digital) {
			this.digital = digital;
		}

		public String getTruckModel() {
			return truckModel;
		}

		public void setTruckModel(String truckModel) {
			this.truckModel = truckModel;
		}

		public String getTruckSerial() {
			return truckSerial;
		}

		public void setTruckSerial(String truckSerial) {
			this.truckSerial = truckSerial;
		}
	}

	// ============== End of MCR WSCR-AKHTEC saveDealerClaim ===================

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

	private static void docUnid(String unid) { // added 12/01/2016 by mdz - puts
		// unid if doc in viewScope
		try {
			// System.out.println("unid=" + unid);
			ExtLibUtil.getViewScope().put("unid", unid);
		} catch (Exception e) {
			System.out.println("docUnid function ERROR: " + e);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Added for MCR WSCR-AZBRBS - Add Fork Positioner / Sideshifter Order - mdz
	@SuppressWarnings("unchecked")
	private LinkedHashMap<String, ArrayList<PromoOrderItem>> loadPromoOrderDocuments(
			String salesType, Session session) {
		// this is used to get all the items the customer can order for a
		// a special promotion order used for the OrderPromoItems XPage
		debugMsg("starting loadPromoOrderDocuments for " + salesType);
		LinkedHashMap<String, ArrayList<PromoOrderItem>> rtnMap = new LinkedHashMap<String, ArrayList<PromoOrderItem>>();
		String catName = new String("");
		Vector row;
		PromoOrderItem lineItem;

		String desc;
		String specMsg;
		ViewEntry tempEntry;
		ViewEntry vwEntry;
		String partNum;
		String hasSpecMsg;
		Double price = 0.0;
		Double maxQty;
		String promoPN = "";
		Double promoItemPrice = 0.0;
		// load up the promo order docs for this type of order
		try {
			// get the orders database
			Database orderDb = session.getDatabase(session.getServerName(),
					OrdersDbName, false);
			View lookupVw = orderDb.getView(salesType);
			ViewNavigator vwNav = lookupVw.createViewNav();

			vwEntry = vwNav.getFirst();
			while (vwEntry != null) {
				row = vwEntry.getColumnValues();

				if (vwEntry.isCategory()) {
					// set Category name
					catName = (String) row.elementAt(1);
					// create a new ArrayList
					ArrayList<PromoOrderItem> lineItemArray = new ArrayList<PromoOrderItem>();

					// create new Map entry
					debugMsg("Promo category Name is " + catName);
					rtnMap.put(catName, lineItemArray);

				} else if (vwEntry.isDocument()) {
					debugMsg("vwEntry.isDocument()");
					// the link is a document
					// get the values
					String rtnMaxQty;
					debugMsg("salesType is " + salesType);
					desc = (String) row.elementAt(2) + "  -   "
							+ row.elementAt(4) + "  "
							+ (String) row.elementAt(8);
					// debugMsg("desc = " + desc);
					specMsg = (String) row.elementAt(11);
					// debugMsg("specMsg = " + specMsg);
					partNum = (String) row.elementAt(2);
					// debugMsg("partNum = " + partNum);
					// debugMsg("row10 = " + row.elementAt(10));
					// debugMsg("row5 = " + row.elementAt(5));
					hasSpecMsg = (String) row.elementAt(10);
					// debugMsg("hasSpecMsg = " + hasSpecMsg);
					price = (Double) row.elementAt(5);
					// debugMsg("price = " + price);
					rtnMaxQty = row.elementAt(6).toString();
					debugMsg("rtnMaxQry = " + rtnMaxQty);
					// promoPN = (String) row.elementAt(8);
					// debugMsg("promoPN = " + promoPN);
					// promoItemPrice = (Double) row.elementAt(10);
					// debugMsg("promoItemPrice = " + promoItemPrice);
					DecimalFormat decimalFormat = new DecimalFormat("#");

					// convert the max qty to a number, if it is a string use 99
					try {
						double number = decimalFormat.parse(rtnMaxQty)
								.doubleValue();
						maxQty = new Double(number);
					} catch (ParseException e) {

						maxQty = new Double(99);
					}

					lineItem = new PromoOrderItem(partNum, desc, price, maxQty,
							hasSpecMsg, specMsg, promoItemPrice, promoPN); // ,
					// promoPrice);
					// put in array
					rtnMap.get(catName).add(lineItem);
				}
				tempEntry = vwNav.getNext(vwEntry);
				vwEntry.recycle();
				vwEntry = tempEntry;
			}
		} catch (Exception e) {
			debugMsg("Error in loadPromoOrderDocuments for " + salesType
					+ " error is: " + e.toString());
		}
		debugMsg("size of rtnMap is " + rtnMap.size());
		return rtnMap;

	}

	public class PromoOrderItem implements Serializable {
		// used to return a PromoOrderItem to the repeat
		private static final long serialVersionUID = 1L;
		Double orderQty;
		String orderQtyString;
		Integer maxQty;
		String description;
		boolean specMsgDisp;
		String specialMsgDesc;
		Double price;
		String partNumber;
		String promoItemPN;
		Double promoItemPrice;

		public Double getOrderQty() {
			return this.orderQty;
		}

		public void setOrderQty(Double orderQty) {
			debugMsg("Starting setOrderQty: value is " + orderQty);
			this.orderQty = orderQty;
		}

		public String getOrderQtyString() {
			return orderQtyString;
		}

		public void setOrderQtyString(String orderQtyString) {

			this.orderQtyString = orderQtyString.toString();
			this.orderQty = new Double(orderQtyString);
			debugMsg("Value for " + this.partNumber + " is " + this.orderQty);
		}

		public Integer getMaxQty() {
			return maxQty;
		}

		public void setMaxQty(Integer maxQty) {
			this.maxQty = maxQty;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public boolean isSpecMsgDisp() {
			return specMsgDisp;
		}

		public void setSpecMsgDisp(boolean specMsgDisp) {
			this.specMsgDisp = specMsgDisp;
		}

		public String getSpecialMsgDesc() {
			return specialMsgDesc;
		}

		public void setSpecialMsgDesc(String specialMsgDesc) {
			this.specialMsgDesc = specialMsgDesc;
		}

		public Double getPrice() {
			return price;
		}

		public void setPrice(Double price) {
			this.price = price;
		}

		public String getPartNumber() {
			return partNumber;
		}

		public void setPartNumber(String partNumber) {
			this.partNumber = partNumber;
		}

		public Double getPromoItemPrice() {
			return promoItemPrice;
		}

		public void setPromoItemPrice(Double price) {
			this.promoItemPrice = promoItemPrice;
		}

		public void setPromoPrice(Double promoPrice) {
			this.promoItemPrice = price;
		}

		public String getPromoItemPN() {
			return promoItemPN;
		}

		public void setPromoItemPN() {
			this.promoItemPN = promoItemPN;
		}

		public PromoOrderItem(String partNum, String description, Double price,
				Double maxQty, String hasSpecDesc, String specialDesc,
				Double promoItemPrice, String promoItemPN) {
			this.orderQty = new Double(0);
			this.orderQtyString = new String("0");
			this.maxQty = maxQty.intValue();
			this.price = price;
			this.description = description;
			this.specialMsgDesc = specialDesc;
			this.partNumber = partNum;
			this.promoItemPrice = promoItemPrice;
			this.promoItemPN = promoItemPN;
			if (StringUtil.equals(hasSpecDesc, "Y")) {
				this.specMsgDisp = true;
			} else {
				this.specMsgDisp = false;
			}
		}
	}

	public ArrayList<PromoOrderItem> getPromoLineItems(String typeName,
			String catName) {
		// used in inner repeat to get the items that appear under each category
		// first parameter is the type, like forkkits
		// second parameter is the category for each type
		debugMsg("getting " + catName);
		ArrayList<PromoOrderItem> rtnLineItems = new ArrayList<PromoOrderItem>();
		// find out which type of sales order item Map to get
		if (StringUtil.equalsIgnoreCase(typeName, "promoitems")) {
			rtnLineItems = new ArrayList<PromoOrderItem>(promoitems
					.get(catName));
		}
		return rtnLineItems;
	}

	public ArrayList<String> getPromoOrderByType(String typeName) {
		// pass in a value corresponding to the string comparision below to get
		// the
		// categories used
		debugMsg("Getting SalesOrderByType for " + typeName);
		ArrayList<String> rtnArrayList = new ArrayList<String>();
		if (StringUtil.equalsIgnoreCase(typeName, "promoitems")) {
			rtnArrayList = new ArrayList<String>(promoitems.keySet());
		}
		return rtnArrayList;
	}

	public ArrayList<PromoOrderItem> getPromoItems(String typeName) {
		// This gets the ordered items
		ArrayList<PromoOrderItem> rtnArrayList = new ArrayList<PromoOrderItem>();
		if (StringUtil.equalsIgnoreCase(typeName, "promoitems")) {
			rtnArrayList = getPromoItemsArrayList(promoitems);
		}
		return rtnArrayList;
	}

	private ArrayList<PromoOrderItem> getPromoItemsArrayList(
			LinkedHashMap<String, ArrayList<PromoOrderItem>> itemMap) {
		ArrayList<PromoOrderItem> rtnArrayList = new ArrayList<PromoOrderItem>();
		// loop through each category
		for (ArrayList<PromoOrderItem> lineItemArray : itemMap.values()) {
			// now loop through each member of the arrray list
			for (PromoOrderItem lineItem : lineItemArray) {
				if (lineItem.getOrderQty() > 0) {
					// copy item to rtnArrayList
					rtnArrayList.add(lineItem);
				}
			}
		}
		return rtnArrayList;
	}

	public Double getPromoOrderTotal(String typeName) {
		Double rtnTotal = new Double(0);
		// This gets the ordered items
		ArrayList<PromoOrderItem> orderedItems = new ArrayList<PromoOrderItem>();
		if (StringUtil.equalsIgnoreCase(typeName, "promoitems")) {
			orderedItems = getPromoItemsArrayList(promoitems);
		}
		// loop through ordered items and add up the price
		for (PromoOrderItem ordItem : orderedItems) {
			rtnTotal = rtnTotal + ordItem.getPrice() * ordItem.getOrderQty();
		}
		return rtnTotal;
	}

	public boolean checkPromoQtyInput(String typeName) {
		// validate qty input - returns true as long as one qty is greater than
		// 0
		boolean rtn = false;
		LinkedHashMap<String, ArrayList<PromoOrderItem>> itemMap = new LinkedHashMap<String, ArrayList<PromoOrderItem>>();
		// if a non-parts order than return to itemMap the correct map to use
		if (StringUtil.equalsIgnoreCase(typeName, "promoitems")) {
			itemMap = promoitems;
		}
		if (!itemMap.isEmpty()) {
			// must be a non-Parts/forks order
			// check the quantities
			for (ArrayList<PromoOrderItem> lineItemArray : itemMap.values()) {
				// now loop through each member of the array list
				for (PromoOrderItem lineItem : lineItemArray) {
					if (lineItem.getOrderQty() > 0) {
						// copy item to rtnArrayList
						rtn = true;
					}
				}
			}
		} else {
			// throw error
			debugMsg("SalesOrderBean:checkPromoQtyInput got invalid parameter");
			throw new RuntimeException(
					"SalesOrderBean:checkPromoQtyInput got invalid parameter");
		}

		return rtn;
	}

	// END OF Added for MCR WSCR-AZBRBS - Add Fork Positioner / Sideshifter
	// Order
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
