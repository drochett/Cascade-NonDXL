package com.cascorp;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;

import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.relational.util.JdbcUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class JDESupport implements Serializable {

	private static final long serialVersionUID = 1L;
	private transient Connection EUconnection;
	private transient Connection AMconnection;

	public class JDEData {

		private static final long serialVersionUID = 1L;
		boolean isValid = false;

		public boolean isValid() {
			return isValid;
		}

		public void setValid(boolean isValid) {
			this.isValid = isValid;
		}

		String acctNumber;
		String custName; // abalph
		String acctType; // abat1(americas) or abac06(eme)
		String custLocation; // alctr
		String custZip; // aladdz
		String address1; // aladd1
		String address2; // aladd2
		String address3; // aladd3
		String custCity; // alcty1;
		String custState; // aladds
		String custCountryCode; // alctr
		String custAC;
		String custPhone;
		String region;
		String shipDate;

		public String getAcctNumber() {
			return acctNumber;
		}

		public void setAcctNumber(String acctNumber) {
			this.acctNumber = acctNumber;
		}

		public String getCustName() {
			return custName;
		}

		public void setCustName(String custName) {
			this.custName = custName;
		}

		public String getAcctType() {
			return acctType;
		}

		public void setAcctType(String acctType) {
			this.acctType = acctType;
		}

		public String getCustLocation() {
			return custLocation;
		}

		public void setCustLocation(String custLocation) {
			this.custLocation = custLocation;
		}

		public String getShipDate() {
			return shipDate;
		}

		public void setShipDate(String shipDate) {
			this.shipDate = shipDate;
		}

		public String getCustZip() {
			return custZip;
		}

		public void setCustZip(String custZip) {
			this.custZip = custZip;
		}

		public String getAddress1() {
			return address1;
		}

		public void setAddress1(String address1) {
			this.address1 = address1;
		}

		public String getAddress2() {
			return address2;
		}

		public void setAddress2(String address2) {
			this.address2 = address2;
		}

		public String getAddress3() {
			return address3;
		}

		public void setAddress3(String address3) {
			this.address3 = address3;
		}

		public String getCustCity() {
			return custCity;
		}

		public void setCustCity(String custCity) {
			this.custCity = custCity;
		}

		public String getCustState() {
			return custState;
		}

		public void setCustState(String custState) {
			this.custState = custState;
		}

		public String getCustCountryCode() {
			return custCountryCode;
		}

		public String getCustAC() {
			return custAC;
		}

		// added below code to get Area Code and Pn=hone numbers of customer
		public void setCustAC(String custAC) {
			this.custAC = custAC;
		}

		public String getCustPhone() {
			return custPhone;
		}

		public void setCustPhone(String custPhone) {
			this.custPhone = custPhone;
		}

		public void setCustCountryCode(String custCountryCode) {
			this.custCountryCode = custCountryCode;
		}
		// end of code added above for area code and phone number

		public void setRegion(String region) {
			this.region = region;
		}

		public String getRegion() {
			return region;
		}

		// code added 04/19/2017- MDZ - MCR WSCR-AKHTEC - New Page and Web
		// Form for Dealers Only (Not public or OEMs)
		// to fix accessType for eme to come from abac06 instead of abat1 field
		public JDEData getJDEAcctType(String acctNum, String region) {
			JDEData rtnData = new JDEData();
			rtnData.setValid(false);
			Connection conn = null;
			ResultSet rs = null;
			PreparedStatement st = null;
			String acctType = "";
			String schema;
			schema = getSchema(region);
			DebugToolbarBean.get().info("getJDEAcctType(region) =>>>" + region + "<<<");
			DebugToolbarBean.get().info("getJDEAcctType(schema) =>>>" + schema + "<<<");
			try {
				conn = getConnection(region);
				DebugToolbarBean.get().info("getJDEAcctType>>RETURN FROM GET CONNECTION 01 - " + conn.getCatalog());
				// fix code added 04/19/2017- MDZ - MCR WSCR-AKHTEC - New Page
				// and
				// Web Form for Dealers Only (Not public or OEMs)
				// to fix accessType for eme to come from abac06 instead of
				// abat1
				// field
				String sqlString;
				if (schema == "Schema-EME") {
					sqlString = new String("SELECT abac06 FROM " + schema + ".f0111 where aban8 = " + acctNum);
				} else {
					sqlString = new String("SELECT abat1  FROM " + schema + ".f0111 WHERE aban8=" + acctNum);
				}
				DebugToolbarBean.get().info("getJDEAcctType(" + acctNum + ", " + region + "):  " + sqlString);
				st = conn.prepareStatement(sqlString);
				st.setString(1, new String(acctNum));
				boolean res = st.execute();
				if (res) {
					rs = st.getResultSet();
					if (rs.isBeforeFirst()) {
						rs.next();
						rtnData.setValid(true);
						if (schema == "Schema-EME") {
							acctType = rs.getString("abac06");
						} else {
							acctType = rs.getString("abat1");
						}
					}
					rtnData.setAcctType(StringUtil.trim(acctType));
					DebugToolbarBean.get().info("getJDEAcctType>>value for AcctType is:" + rtnData.getAcctType());
				} else {
					DebugToolbarBean.get().info("res is false");
				}

			} catch (SQLException se) {

				DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
				DebugToolbarBean.get().error(se);
				rtnData.setValid(false);
			} catch (Exception e) {
				DebugToolbarBean.get().info("error in exception");
				DebugToolbarBean.get().error(e);
				rtnData.setValid(false);
			} finally {
				try { // close resources
					DebugToolbarBean.get().info("Closing connections");
					if (rs != null) {
						rs.close();
					}
					if (st != null) {
						st.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
					throw new RuntimeException(e);
				}
			}
			return rtnData;
		}

		// end of fix code added 04/19/2017 - MDZ
	}

	public class orderData implements Serializable {

		private static final long serialVersionUID = 1L;
		String orderNumber; // this is the document id a.shdoco fld2
		String purchaseOrderNumber; // po number a.shvr01 fld
		String shipTo; // ship to number b.sdshan fld3
		String docType; // doc type a.shdcto fld1
		String custName; // d.wwmlnm
		String carrier; // a.shrout fld6
		String curCode; // a.shcrcd fld5 currencyCode
		Double orderTotal; // this is computed
		boolean showTotalPrice; // this is used to determine if total should be
		// shown

		// these fields are for displaying concatenated fields on the order
		// status lines
		String dispOrderNumber; // orderNumber + " " + docType
		String dispShipTo; // shipTo + "-" + custName
		// this holds the line items for the order
		ArrayList<lineItem> orderItems = new ArrayList<lineItem>();

		public Double getOrderTotal() {
			return orderTotal;
		}

		public void setOrderTotal(Double orderTotal) {
			this.orderTotal = orderTotal;
		}

		public String getPurchaseOrderNumber() {
			return purchaseOrderNumber;
		}

		public void setPurchaseOrderNumber(String purchaseOrderNumber) {
			this.purchaseOrderNumber = purchaseOrderNumber;
		}

		public String getShipTo() {
			return shipTo;
		}

		public void setShipTo(String shipTo) {
			this.shipTo = shipTo;
		}

		public String getDocType() {
			return docType;
		}

		public void setDocType(String docType) {
			this.docType = docType;
		}

		public String getCustName() {
			return custName;
		}

		public void setCustName(String custName) {
			this.custName = custName;
		}

		public String getCarrier() {
			return carrier;
		}

		public void setCarrier(String carrier) {
			this.carrier = carrier;
		}

		public String getCurCode() {
			return curCode;
		}

		public void setCurCode(String curCode) {
			this.curCode = curCode;
		}

		public String getDispOrderNumber() {
			return dispOrderNumber;
		}

		public void setDispOrderNumber(String dispOrderNumber) {
			this.dispOrderNumber = dispOrderNumber;
		}

		public String getDispShipTo() {
			return dispShipTo;
		}

		public void setDispShipTo(String dispShipTo) {
			this.dispShipTo = dispShipTo;
		}

		public ArrayList<lineItem> getOrderItems() {
			return orderItems;
		}

		public void setOrderItems(ArrayList<lineItem> orderItems) {
			this.orderItems = orderItems;
		}

		public boolean isShowTotalPrice() {
			return showTotalPrice;
		}

		public void setShowTotalPrice(boolean showTotalPrice) {
			this.showTotalPrice = showTotalPrice;
		}

		public String getOrderNumber() {
			return orderNumber;
		}

		public void setOrderNumber(String orderNumber) {
			this.orderNumber = orderNumber;
		}

		public class lineItem implements Serializable {

			private static final long serialVersionUID = 1L;
			// these are related to the columns that need to be displayed

			Integer quantity;
			String item;
			String description;
			String serialNumber;
			Double extPrice;
			Date orderDate;
			Date targetShipDate;
			Date actualShipDate;
			String carrierTerms;
			boolean showPrice;
			boolean showQty;
			boolean showOrderDate;
			boolean showTargetShipDate;
			boolean showActualShipDate;

			public String getItem() {
				return item;
			}

			public void setItem(String item) {
				this.item = item;
			}

			public String getDescription() {
				return description;
			}

			public void setDescription(String description) {
				this.description = description;
			}

			public String getSerialNumber() {
				return serialNumber;
			}

			public void setSerialNumber(String serialNumber) {
				this.serialNumber = serialNumber;
			}

			public Double getExtPrice() {
				return extPrice;
			}

			public void setExtPrice(Double extPrice) {
				this.extPrice = extPrice;
			}

			public Date getOrderDate() {
				return orderDate;
			}

			public void setOrderDate(Date orderDate) {
				this.orderDate = orderDate;
			}

			public Date getTargetShipDate() {
				return targetShipDate;
			}

			public void setTargetShipDate(Date targetShipDate) {
				this.targetShipDate = targetShipDate;
			}

			public Date getActualShipDate() {
				return actualShipDate;
			}

			public void setActualShipDate(Date actualShipDate) {
				this.actualShipDate = actualShipDate;
			}

			public String getCarrierTerms() {
				return carrierTerms;
			}

			public void setCarrierTerms(String carrierTerms) {
				this.carrierTerms = carrierTerms;
			}

			public Integer getQuantity() {
				return quantity;
			}

			public void setQuantity(Integer quantity) {
				this.quantity = quantity;
			}

			public boolean isShowPrice() {
				return showPrice;
			}

			public void setShowPrice(boolean showPrice) {
				this.showPrice = showPrice;
			}

			public boolean isShowQty() {
				return showQty;
			}

			public void setShowQty(boolean showQty) {
				this.showQty = showQty;
			}

			public boolean isShowOrderDate() {
				return showOrderDate;
			}

			public void setShowOrderDate(boolean showOrderDate) {
				this.showOrderDate = showOrderDate;
			}

			public boolean isShowTargetShipDate() {
				return showTargetShipDate;
			}

			public void setShowTargetShipDate(boolean showTargetShipDate) {
				this.showTargetShipDate = showTargetShipDate;
			}

			public boolean isShowActualShipDate() {
				return showActualShipDate;
			}

			public void setShowActualShipDate(boolean showActualShipDate) {
				this.showActualShipDate = showActualShipDate;
			}

		}

		public void addLineItem(Integer quantity, String item, String description, String serialNumber, Double extPrice,
				Date orderDate, Date targetShipDate, Date actualShipDate, String carrierTerms, boolean showPrice,
				boolean showQty) {

			lineItem lineItem = new lineItem();

			lineItem.quantity = quantity;
			lineItem.item = item;
			lineItem.description = description;
			lineItem.serialNumber = serialNumber;

			lineItem.extPrice = extPrice;
			lineItem.orderDate = orderDate;
			lineItem.targetShipDate = targetShipDate;
			lineItem.actualShipDate = actualShipDate;
			lineItem.carrierTerms = carrierTerms;
			// set boolean items to control rendered
			lineItem.showQty = showQty;
			if (actualShipDate == null) {
				lineItem.showActualShipDate = false;
			} else {
				lineItem.showActualShipDate = true;
			}
			if (orderDate == null) {
				lineItem.showOrderDate = false;
			} else {
				lineItem.showOrderDate = true;
			}
			if (targetShipDate == null) {
				lineItem.showTargetShipDate = false;
			} else {
				lineItem.showTargetShipDate = true;
			}

			lineItem.showPrice = showPrice;
			// add the price to the accumulated total
			if (showPrice) {
				// if the price should be shown add to total
				this.orderTotal = this.orderTotal + extPrice;
			}
			orderItems.add(lineItem);
		}

		public void addLineItem(String partNumber, String description, Double extPrice, boolean showPrice) {
			lineItem lineItem = new lineItem();

			lineItem.showPrice = showPrice;

			lineItem.description = description;

			lineItem.extPrice = extPrice;

			// add the price to the accumulated total
			this.orderTotal = this.orderTotal + extPrice;
			orderItems.add(lineItem);
		}

		public orderData(String orderNumber, String purchaseOrderNumber, String shipTo, String docType, String custName,
				String carrier, String curCode) {

			this.orderNumber = orderNumber;
			this.purchaseOrderNumber = purchaseOrderNumber;
			this.shipTo = shipTo;
			this.docType = docType;
			this.custName = custName;
			this.carrier = carrier;
			this.curCode = curCode;
			this.orderTotal = new Double(0);
			this.dispOrderNumber = orderNumber + "-" + docType;
			this.dispShipTo = shipTo + "-" + custName;
		}

		public void addSerialNumber(String serialNumber) {
			lineItem lineItem = new lineItem();
			String emptyString = new String("");
			lineItem.quantity = new Integer(0);
			lineItem.item = emptyString;
			lineItem.description = emptyString;
			lineItem.serialNumber = serialNumber;
			lineItem.extPrice = new Double(0);
			lineItem.orderDate = null;
			lineItem.targetShipDate = null;
			lineItem.actualShipDate = null;
			lineItem.carrierTerms = emptyString;
			// set boolean items to control rendereds
			lineItem.showQty = false;
			lineItem.showActualShipDate = false;
			lineItem.showOrderDate = false;
			lineItem.showTargetShipDate = false;
			lineItem.showPrice = false;
			// add to orderItems
			orderItems.add(lineItem);
		}

	}

	public ArrayList<orderData> setSampleOrder() {
		ArrayList<orderData> rtnArrayList = new ArrayList<orderData>();
		orderData testData = new orderData("ord1", "PO111", "shipto1", "dt1", "Howard", "UPS", "USD");
		testData.addLineItem(new Integer(23), "1331", "desc1", "serial1", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);
		testData.addLineItem(new Integer(23), "A332", "desc2", "serial2", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);
		testData.addLineItem(new Integer(23), "A333", "desc3", "serial3", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);

		rtnArrayList.add(testData);
		testData = new orderData("ord2", "PO222", "shipto2", "dt2", "Paul", "FEDX", "EUR");
		testData.addLineItem(new Integer(23), "A331", "desc1", "serial1", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);
		testData.addLineItem(new Integer(23), "A332", "desc2", "serial2", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);
		testData.addLineItem(new Integer(23), "A333", "desc3", "serial3", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);

		rtnArrayList.add(testData);
		return rtnArrayList;
	}

	public ArrayList<orderData> setSampleOrder2() {
		ArrayList<orderData> rtnArrayList = new ArrayList<orderData>();
		orderData testData = new orderData("ord3", "PO111", "shipto1", "dt1", "Howard", "UPS", "USD");
		testData.addLineItem(new Integer(23), "A331", "desc1", "serial1", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);
		testData.addLineItem(new Integer(23), "A332", "desc2", "serial2", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);
		testData.addLineItem(new Integer(23), "A333", "desc3", "serial3", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);

		rtnArrayList.add(testData);
		testData = new orderData("ord4", "PO222", "shipto2", "dt2", "Paul", "FEDX", "EUR");
		testData.addLineItem(new Integer(23), "A431", "desc1", "serial1", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);
		testData.addLineItem(new Integer(23), "A432", "desc2", "serial2", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);
		testData.addLineItem(new Integer(23), "A433", "desc3", "serial3", new Double(235.00), new Date(), new Date(),
				new Date(), "UPS", true, true);

		rtnArrayList.add(testData);
		return rtnArrayList;
	}

	public ArrayList<orderData> setSampleQuery(String region, String custNumber, String shipTo, Integer daysToSearch) {
		ArrayList<orderData> rtnArrayList = new ArrayList<orderData>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String previousOrderNumber = new String("");
		String prevLineId = new String("");
		orderData orderData = null;
		String carrierConfigValue;
		String freightTermsConfigValue;
		String dispItem;
		if (region.equalsIgnoreCase("americas")) {
			// set config lookup values for americas
			carrierConfigValue = new String("Carrier-americas");
			freightTermsConfigValue = new String("FreightTerms-americas");
		} else {
			carrierConfigValue = new String("Carrier-eme");
			freightTermsConfigValue = new String("FreightTerms-eme");
		}
		boolean showPrice;
		boolean showTotalPrice = false;
		String carrierInfo;
		try {
			conn = getConnection(region);
			// st = getPOSearchPS(conn,"eme", "1933","IOR15-8340", "OrderDate");
			st = getAllSearchPS(conn, region, custNumber, "OrderDate", shipTo, daysToSearch);
			boolean res = st.execute();
			DebugToolbarBean.get().info("res is " + res);
			if (res) {
				rs = st.getResultSet();
				while (rs.next()) {
					// get the order level items
					String poNumber = StringUtil.trim(rs.getString("shvr01"));
					String docType = StringUtil.trim(rs.getString("shdcto"));
					String orderNumber = StringUtil.trim(rs.getString("shdoco"));
					String shipToRtn = StringUtil.trim(rs.getString("sdshan"));
					String custName = StringUtil.trim(rs.getString("wwmlnm"));
					String curCode = StringUtil.trim(rs.getString("shcrcd"));
					String carrier = StringUtil.trim(rs.getString("shrout"));

					// get the line item items
					String itemNumber = StringUtil.trim(rs.getString("sdlitm"));
					String description = StringUtil.trim(rs.getString("sddsc1"));
					Double price = rs.getDouble("sdaexp") / 100;
					String lineType = StringUtil.trim(rs.getString("sdlnty")); // line
					// type

					String lastStatus = StringUtil.trim(rs.getString("sdlttr"));
					String nextStatus = StringUtil.trim(rs.getString("sdnxtr"));
					String billToCustNumber = StringUtil.trim(rs.getString("sdan8"));

					Double qtyDouble = rs.getDouble("sduorg") / 10; // divide by
					// 10
					Integer quantity = new Integer(qtyDouble.intValue()); // convert
					// to
					// Integer
					String clipperShip = StringUtil.trim(rs.getString("sdstop"));

					String lineID = StringUtil.trim(rs.getString("sdlnid"));
					String serialNumber = StringUtil.trim(rs.getString("swsrl1"));
					// String fld22 = StringUtil.trim(rs.getString("sdurab"));
					// //this is always 0 per Mike, so not used
					Double fld21Number;
					if (region.equalsIgnoreCase("americas")) {
						fld21Number = rs.getDouble("kocsid");
					} else {
						fld21Number = rs.getDouble("kzcsid");
					}
					String fld21 = new Integer(fld21Number.intValue()).toString();
					DebugToolbarBean.get().info("fld21 is " + fld21);

					// get date fields
					Date orderDate = convertJDEDate(StringUtil.trim(rs.getString("sdtrdj")));
					// DebugToolbarBean.get().info("orderDate is " +
					// orderDate.toString());
					Date targetShipDate = convertJDEDate(StringUtil.trim(rs.getString("sddrqj")));
					// DebugToolbarBean.get().info("targetShipDate is " +
					// targetShipDate.toString());
					String actualShipDateString = StringUtil.trim(rs.getString("sdaddj"));
					DebugToolbarBean.get().info("String value of actualShip Date is " + actualShipDateString
							+ " converted date is " + convertJDEDate(actualShipDateString));
					Date actualShipDate = convertJDEDate(actualShipDateString);
					DebugToolbarBean.get().info("Clipper ship is " + clipperShip);
					DebugToolbarBean.get().info("carrier is " + carrier);
					// NOTE, the dates return "0" from the JDE system, the
					// convertJDEDate then returns null
					// Be sure to test any dates for null before using!!!
					/*
					 * if (actualShipDate == null){
					 * DebugToolbarBean.get().info("actualShipDate is null"); }
					 * else { DebugToolbarBean.get().info("actualShipDate is " +
					 * actualShipDate.toString()); }
					 */

					DebugToolbarBean.get().info("doc type is " + docType);
					// if previous number is empty create a new order
					if (StringUtil.isEmpty(previousOrderNumber)) {
						// create a new order
						orderData = new orderData(orderNumber, poNumber, shipToRtn, docType, custName, carrier,
								curCode);

					} else if (!StringUtil.equals(previousOrderNumber, orderNumber)) {
						// we got a new order, so save the current one to
						// rtnArrayList
						// write out if the total should be saved
						DebugToolbarBean.get().info("showTotalPrice is " + showTotalPrice);
						orderData.setShowTotalPrice(showTotalPrice);
						rtnArrayList.add(orderData);
						// then create a new order
						orderData = new orderData(orderNumber, poNumber, shipToRtn, docType, custName, carrier,
								curCode);
						// reset showTotalPrice for the next order
						showTotalPrice = false;
					}
					// figure out to show prices for this item
					if (billToCustNumber.equalsIgnoreCase(custNumber)) {
						// showPrice used when adding item to orderData
						showPrice = true;
						// flip on the showTotalPrice set in orderData before
						// writing out order
						showTotalPrice = true;

					} else {
						DebugToolbarBean.get().info("HIDE Price-BillTo is " + billToCustNumber);
						showPrice = false;

					}

					// process the rest of the line
					if (prevLineId.equals(orderNumber.concat(lineID))) {
						// we did this item already so it must be a serial
						// number
						orderData.addSerialNumber(serialNumber);
						DebugToolbarBean.get().info("Serial number is " + serialNumber);
					} else {
						if (lineType.equalsIgnoreCase("TS") || lineType.equalsIgnoreCase("F")) {
							// show partNumber, description, price, actual
							// shipdate,
							// show clipper ship if not blank, otherwise show
							// Carrier
							DebugToolbarBean.get().info("TS or F line");
							if (StringUtil.isNotEmpty(clipperShip)) {
								carrierInfo = lookupConfigValue(clipperShip, carrierConfigValue);
							} else {
								carrierInfo = lookupConfigValue(carrier, freightTermsConfigValue);
							}
							DebugToolbarBean.get().info("carrier value is " + carrierInfo);
							// create line item
							orderData.addLineItem(0, itemNumber, description, "", price, null, null, actualShipDate,
									carrierInfo, showPrice, false);
						} else if ((lastStatus.equals("520") && nextStatus.equals("524")) || lineType.equals("Z")) {
							// If Acknowledgements haven't run yet (status of
							// 520/524), do not show the target ship date.
							// Never show the target ship date for Z line types.
							DebugToolbarBean.get().info("520 AND 524 line or Z");
							if (StringUtil.isNotEmpty(clipperShip)) {
								carrierInfo = lookupConfigValue(clipperShip, carrierConfigValue);
							} else {
								carrierInfo = lookupConfigValue(carrier, freightTermsConfigValue);
							}
							// figure out the item number
							if (lineType.equals("WC")) {
								dispItem = itemNumber + "-" + fld21;
							} else {
								dispItem = itemNumber;
							}

							// create line item
							orderData.addLineItem(quantity, dispItem, description, "", price, orderDate, null,
									actualShipDate, carrierInfo, showPrice, true);
						} else {
							// item shipped, show all fields
							DebugToolbarBean.get().info("Shipped item");
							if (StringUtil.isNotEmpty(clipperShip)) {
								carrierInfo = lookupConfigValue(clipperShip, carrierConfigValue);
							} else {
								carrierInfo = lookupConfigValue(carrier, freightTermsConfigValue);
							}
							// figure out the item number
							if (lineType.equals("WC")) {
								dispItem = itemNumber + "-" + fld21;
							} else {
								dispItem = itemNumber;
							}

							// create line item
							orderData.addLineItem(quantity, dispItem, description, serialNumber, price, orderDate,
									targetShipDate, actualShipDate, carrierInfo, showPrice, true);
						}

					}
					// save previous order Number
					previousOrderNumber = orderNumber;
					// save the line id
					prevLineId = orderNumber.concat(lineID);
					// MOVE ONTO NEXT LINE IN RESULT SET
				}
			}
		} catch (Exception e) {
			DebugToolbarBean.get().error(e);
			// throw new RuntimeException(e);
		}

		return rtnArrayList;
	}

	public ArrayList<orderData> performQuery(String region, String custNumber, String searchSelection, String shipTo,
			Integer daysToSearch, String sortSelection, String searchBySelection, String searchInput,
			String searchType) {
		/*
		 * Used in OrderStatusBean to peform orders query
		 */
		// if bynumber use the search by PO or sales order number
		// setup shipTo, if All use an empty string
		if (shipTo.equals("All")) {
			// reset to default empty value for query
			shipTo = new String("");
		}
		DebugToolbarBean.get().info("sort selection is " + sortSelection);

		ArrayList<orderData> rtnArrayList = new ArrayList<orderData>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String previousOrderNumber = new String("");
		String prevLineId = new String("");
		orderData orderData = null;
		String carrierConfigValue;
		String freightTermsConfigValue;
		String dispItem;
		boolean foundData = false;
		if (region.equalsIgnoreCase("americas")) {
			// set config lookup values for americas
			carrierConfigValue = new String("Carrier-americas");
			freightTermsConfigValue = new String("FreightTerms-americas");
		} else {
			carrierConfigValue = new String("Carrier-eme");
			freightTermsConfigValue = new String("FreightTerms-eme");
		}
		boolean showPrice;
		boolean showTotalPrice = false;
		String carrierInfo;
		try {
			conn = getConnection(region);
			// figure out the type of search to do
			if (searchType.equals("bynumber")) {

				// see if by PO or by sales order number
				if (searchBySelection.equalsIgnoreCase("PONumber")) {
					// search by PO number, sort by OrderDate
					DebugToolbarBean.get().info("PO Search input is " + searchInput + " cust Number is " + custNumber);
					st = getPOSearchPS(conn, region, custNumber, searchInput, "OrderDate");
				} else {
					// sort by sales order number, sort by OrderDate
					DebugToolbarBean.get()
							.info("SALES NUMBER Search input is " + searchInput + " cust Number is " + custNumber);
					st = getSOSearchPS(conn, region, custNumber, searchInput, "OrderDate");
				}

			} else {
				if (searchSelection.equalsIgnoreCase("Open")) {
					// search selection will be Open or All
					st = getOpenSearchPS(conn, region, custNumber, sortSelection, shipTo, daysToSearch);
				} else {
					// default to All
					st = getAllSearchPS(conn, region, custNumber, sortSelection, shipTo, daysToSearch);
				}
			}

			boolean res = st.execute();
			DebugToolbarBean.get().info("res is " + res);
			if (res) {
				rs = st.getResultSet();
				while (rs.next()) {

					// get the order level items
					String poNumber = StringUtil.trim(rs.getString("shvr01"));
					String docType = StringUtil.trim(rs.getString("shdcto"));
					String orderNumber = StringUtil.trim(rs.getString("shdoco"));
					String shipToRtn = StringUtil.trim(rs.getString("sdshan"));
					String custName = StringUtil.trim(rs.getString("wwmlnm"));
					String curCode = StringUtil.trim(rs.getString("shcrcd"));
					String carrier = StringUtil.trim(rs.getString("shrout"));

					// get the line item items
					String itemNumber = StringUtil.trim(rs.getString("sdlitm"));
					String description = StringUtil.trim(rs.getString("sddsc1"));
					Double price = rs.getDouble("sdaexp") / 100;
					String lineType = StringUtil.trim(rs.getString("sdlnty")); // line
					// type

					String lastStatus = StringUtil.trim(rs.getString("sdlttr"));
					String nextStatus = StringUtil.trim(rs.getString("sdnxtr"));
					String billToCustNumber = StringUtil.trim(rs.getString("sdan8"));

					Double qtyDouble = rs.getDouble("sduorg") / 10; // divide by
					// 10
					Integer quantity = new Integer(qtyDouble.intValue()); // convert
					// to
					// Integer
					String clipperShip = StringUtil.trim(rs.getString("sdstop"));

					String lineID = StringUtil.trim(rs.getString("sdlnid"));
					String serialNumber = StringUtil.trim(rs.getString("swsrl1"));
					// String fld22 = StringUtil.trim(rs.getString("sdurab"));
					// //this is always 0 per Mike, so not used
					Double fld21Number;
					if (region.equalsIgnoreCase("americas")) {
						fld21Number = rs.getDouble("kocsid");
					} else {
						fld21Number = rs.getDouble("kzcsid");
					}
					String fld21 = new Integer(fld21Number.intValue()).toString();
					DebugToolbarBean.get().info("fld21 is " + fld21);

					// get date fields
					Date orderDate = convertJDEDate(StringUtil.trim(rs.getString("sdtrdj")));
					// DebugToolbarBean.get().info("orderDate is " +
					// orderDate.toString());
					Date targetShipDate = convertJDEDate(StringUtil.trim(rs.getString("sddrqj")));
					// DebugToolbarBean.get().info("targetShipDate is " +
					// targetShipDate.toString());
					String actualShipDateString = StringUtil.trim(rs.getString("sdaddj"));
					DebugToolbarBean.get().info("String value of actualShip Date is " + actualShipDateString
							+ " converted date is " + convertJDEDate(actualShipDateString));
					Date actualShipDate = convertJDEDate(actualShipDateString);
					DebugToolbarBean.get().info("Clipper ship is " + clipperShip);
					DebugToolbarBean.get().info("carrier is " + carrier);
					// NOTE, the dates return "0" from the JDE system, the
					// convertJDEDate then returns null
					// Be sure to test any dates for null before using!!!
					/*
					 * if (actualShipDate == null){
					 * DebugToolbarBean.get().info("actualShipDate is null"); }
					 * else { DebugToolbarBean.get().info("actualShipDate is " +
					 * actualShipDate.toString()); }
					 */

					// if previous number is empty create a new order
					if (StringUtil.isEmpty(previousOrderNumber)) {
						// create a new order
						DebugToolbarBean.get().info("Creating new order for " + orderNumber + " po is " + poNumber);
						orderData = new orderData(orderNumber, poNumber, shipToRtn, docType, custName, carrier,
								curCode);
						// indicate we found some data
						foundData = true;

					} else if (!StringUtil.equals(previousOrderNumber, orderNumber)) {
						// we got a new order, so save the current one to
						// rtnArrayList
						// write out if the total should be saved
						DebugToolbarBean.get().info("showTotalPrice is " + showTotalPrice);
						orderData.setShowTotalPrice(showTotalPrice);
						rtnArrayList.add(orderData);
						// then create a new order
						orderData = new orderData(orderNumber, poNumber, shipToRtn, docType, custName, carrier,
								curCode);
						// reset showTotalPrice for the next order
						showTotalPrice = false;
					}
					// figure out to show prices for this item
					if (billToCustNumber.equalsIgnoreCase(custNumber)) {
						// showPrice used when adding item to orderData
						showPrice = true;
						// flip on the showTotalPrice set in orderData before
						// writing out order
						showTotalPrice = true;

					} else {
						DebugToolbarBean.get().info("HIDE Price-BillTo is " + billToCustNumber);
						showPrice = false;

					}

					// process the rest of the line
					if (prevLineId.equals(orderNumber.concat(lineID))) {
						// we did this item already so it must be a serial
						// number
						orderData.addSerialNumber(serialNumber);
						DebugToolbarBean.get().info("Serial number is " + serialNumber);
					} else {
						if (lineType.equalsIgnoreCase("TS") || lineType.equalsIgnoreCase("F")) {
							// show partNumber, description, price, actual
							// shipdate,
							// show clipper ship if not blank, otherwise show
							// Carrier
							DebugToolbarBean.get().info("TS or F line");
							if (StringUtil.isNotEmpty(clipperShip)) {
								carrierInfo = lookupConfigValue(clipperShip, carrierConfigValue);
							} else {
								carrierInfo = lookupConfigValue(carrier, freightTermsConfigValue);
							}
							DebugToolbarBean.get().info("carrier value is " + carrierInfo);

							// create line item
							orderData.addLineItem(0, itemNumber, description, "", price, null, null, actualShipDate,
									carrierInfo, showPrice, false);
						} else if ((lastStatus.equals("520") && nextStatus.equals("524")) || lineType.equals("Z")) {
							// If Acknowledgements haven't run yet (status of
							// 520/524), do not show the target ship date.
							// Never show the target ship date for Z line types.
							DebugToolbarBean.get().info("520 AND 524 line or Z");
							if (StringUtil.isNotEmpty(clipperShip)) {
								carrierInfo = lookupConfigValue(clipperShip, carrierConfigValue);
							} else {
								carrierInfo = lookupConfigValue(carrier, freightTermsConfigValue);
							}
							// figure out the item number
							if (lineType.equals("WC")) {
								dispItem = itemNumber + "-" + fld21;
							} else {
								dispItem = itemNumber;
							}

							// create line item
							orderData.addLineItem(quantity, dispItem, description, "", price, orderDate, null,
									actualShipDate, carrierInfo, showPrice, true);
						} else {
							// item shipped, show all fields
							DebugToolbarBean.get().info("Shipped item");
							if (StringUtil.isNotEmpty(clipperShip)) {
								carrierInfo = lookupConfigValue(clipperShip, carrierConfigValue);
							} else {
								carrierInfo = lookupConfigValue(carrier, freightTermsConfigValue);
							}
							// figure out the item number
							if (lineType.equals("WC")) {
								dispItem = itemNumber + "-" + fld21;
							} else {
								dispItem = itemNumber;
							}

							// create line item

							orderData.addLineItem(quantity, dispItem, description, serialNumber, price, orderDate,
									targetShipDate, actualShipDate, carrierInfo, showPrice, true);
						}

					}
					// save previous order Number
					previousOrderNumber = orderNumber;
					// save the line id
					prevLineId = orderNumber.concat(lineID);
					// MOVE ONTO NEXT LINE IN RESULT SET
				}
				// have to handle saving the orderData for the last row, only if
				// some results were found
				if (foundData) {
					orderData.setShowTotalPrice(showTotalPrice);
					rtnArrayList.add(orderData);
				}
			}
		} catch (Exception e) {
			DebugToolbarBean.get().error(e);
			// throw new RuntimeException(e);
		}

		return rtnArrayList;
	}

	public ArrayList<String> getShipToInformation(String custNumber, String region, Integer daysToSearch) {
		// this gets the ship to info to use when selecting which ship to
		// address to select in the query
		ArrayList<String> rtnArrayList = new ArrayList<String>();
		String searchDate = adjustDate(daysToSearch);
		String rtnLine = new String("");
		DebugToolbarBean.get().info("Search for ship to after " + searchDate);
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String sqlString = "SELECT distinct(a.shshan), d.wwmlnm, c.alcty1, c.aladds " + "FROM  " + getSchema(region)
				+ ".F4201 a INNER JOIN  " + getSchema(region) + ".F0111 " + "d ON a.shshan = d.wwan8 " + "INNER JOIN  "
				+ getSchema(region) + ".F0116 c ON a.shshan = c.alan8 " + "WHERE (a.shan8 = ? OR a.shshan = ?) "
				+ "AND a.shdcto IN (" + getDocType(region) + ") AND a.shtrdj > " + searchDate + " AND d.wwidln = 0 "
				+ "UNION SELECT distinct(a.shshan), d.wwmlnm, c.alcty1, c.aladds " + "FROM  " + getSchema(region)
				+ ".F42019 a INNER JOIN  " + getSchema(region) + ".F0111 " + "d ON a.shshan = d.wwan8 " + "INNER JOIN  "
				+ getSchema(region) + ".F0116 c ON a.shshan = c.alan8 " + "WHERE (a.shan8 = ? OR a.shshan = ?) "
				+ "AND a.shdcto IN (" + getDocType(region) + ") AND a.shtrdj > " + searchDate + " AND d.wwidln = 0";
		try {
			conn = getConnection(region);
			st = conn.prepareStatement(sqlString);
			// have four customer numbers
			st.setString(1, new String(custNumber));
			st.setString(2, new String(custNumber));
			st.setString(3, new String(custNumber));
			st.setString(4, new String(custNumber));
			boolean res = st.execute();
			DebugToolbarBean.get().info("res is " + res);
			if (res) {
				rs = st.getResultSet();
				while (rs.next()) {
					String shshan = StringUtil.trim(rs.getString("shshan"));
					String shipToDesc = StringUtil.trim(rs.getString("wwmlnm"));
					String city = StringUtil.trim(rs.getString("alcty1"));
					String state = StringUtil.trim(rs.getString("aladds"));
					// build string for each value
					rtnLine = shshan + " - " + shipToDesc + "  " + city;
					if (StringUtil.equalsIgnoreCase(region, "americas")) {
						// if americas show the state, otherwise don't
						rtnLine = rtnLine.concat(", " + state);
					}
					// add alias
					rtnLine = rtnLine.concat("|" + shshan);
					DebugToolbarBean.get().info("rtnLine: " + rtnLine);
					rtnArrayList.add(rtnLine);
				}
			}
		} catch (Exception e) {
			DebugToolbarBean.get().info("error in getShipToInformation" + e.getMessage());
			DebugToolbarBean.get().error(e);

		}
		return rtnArrayList;
	}

	public boolean testConnection(String acctNum, String region) {
		boolean rtn = false;

		if (getAcctDetails(acctNum, region).isValid) {
			rtn = true;
		}

		return rtn;

	}

	public JDEData getAcctDetails(String acctNum, String region) {
		// for a given account number and a region, return all the details for
		// that company
		// used the inner class JDEData to populate the info. from JDE
		// used in the registration process, see RegistrationBean
		DebugToolbarBean.get().info("{getAccctDetails} =>>> ENTERING <<<");
		JDEData rtnData = new JDEData();
		rtnData.setValid(false);
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String custName;
		String acctType;
		String custLocation;
		String custZip;
		String address1;
		String address2;
		String address3;
		String custCity;
		String custState;
		String custCountryCode;
		String schema;
		schema = getSchema(region);
		DebugToolbarBean.get().info("getAccctDetails(region) =>>>" + region + "<<<");
		DebugToolbarBean.get().info("getAccctDetails(schema) =>>>" + schema + "<<<");

		try {
			conn = getConnection(region);
			DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 01 - " + conn.getCatalog());
			// fix code added 04/19/2017- MDZ - MCR WSCR-AKHTEC - New Page and
			// Web Form for Dealers Only (Not public or OEMs)
			// to fix accessType for eme to come from abac06 instead of abat1
			// field
			String sqlString;
			// end of fix code added 04/19/2017 - MDZ
			if (region.equalsIgnoreCase("americas")) {
				sqlString = new String(
						"SELECT a.wwmlnm,a.wwan8,b.abalph,b.abat1,c.aladd1,c.aladd2,c.aladd3,c.alcty1,c.aladds,c.aladdz,c.alctr FROM "
								+ schema + ".f0111 as a INNER JOIN " + schema
								+ ".f0101 as b ON a.wwan8 = b.aban8 INNER JOIN " + schema
								+ ".f0116 as c ON a.wwan8 = c.alan8 WHERE a.wwan8= ?  AND a.wwidln=0");
			} else {
				sqlString = new String(
						"SELECT a.wwmlnm,a.wwan8,b.abalph,b.abac06,c.aladd1,c.aladd2,c.aladd3,c.alcty1,c.aladds,c.aladdz,c.alctr FROM "
								+ schema + ".f0111 as a INNER JOIN " + schema
								+ ".f0101 as b ON a.wwan8 = b.aban8 INNER JOIN " + schema
								+ ".f0116 as c ON a.wwan8 = c.alan8  WHERE a.wwan8= ? AND a.wwidln=0");
			}
			DebugToolbarBean.get().info("getAccctDetails(" + acctNum + "," + region + ") sqlString:  " + sqlString);

			st = conn.prepareStatement(sqlString);
			/*
			 * DebugToolbarBean.get().info( "getAccctDetails(" + acctNum + "," +
			 * region + ") - conn done with sqlString");
			 */
			st.setString(1, new String(acctNum));
			boolean res = st.execute();
			/*
			 * DebugToolbarBean.get().info( "getAccctDetails(" + acctNum + "," +
			 * region + ") - done with st.execute");
			 */
			if (res) {
				rs = st.getResultSet();
				if (rs.isBeforeFirst()) {
					rs.next();
					rtnData.setValid(true);
					custName = rs.getString("abalph");
					// fix code added 04/19/2017- MDZ - MCR WSCR-AKHTEC - New
					// Page and Web Form for Dealers Only (Not public or OEMs)
					// to fix accessType for eme to come from abac06 instead of
					// abat1 field
					if (region.equalsIgnoreCase("americas")) {
						DebugToolbarBean.get().info("running Schema-Americas code ");
						acctType = rs.getString("abat1");
					} else {
						DebugToolbarBean.get().info("running Schema-EME code ");
						acctType = rs.getString("abac06");
					}
					// end of fix code added 04/19/2017 - MDZ
					custLocation = rs.getString("alctr");
					custZip = rs.getString("aladdz");
					address1 = rs.getString("aladd1");
					address2 = rs.getString("aladd2");
					address3 = rs.getString("aladd3");
					custCity = rs.getString("alcty1");
					custState = rs.getString("aladds");
					custCountryCode = rs.getString("alctr");

					rtnData.setCustName(custName);
					rtnData.setAcctType(StringUtil.trim(acctType));
					rtnData.setCustLocation(StringUtil.trim(custLocation));
					rtnData.setCustZip(StringUtil.trim(custZip));
					rtnData.setAddress1(StringUtil.trim(address1));
					rtnData.setAddress2(StringUtil.trim(address2));
					rtnData.setAddress3(StringUtil.trim(address3));
					rtnData.setCustCity(StringUtil.trim(custCity));
					rtnData.setCustState(StringUtil.trim(custState));
					rtnData.setCustCountryCode(StringUtil.trim(custCountryCode));
					rtnData.setRegion(region);
					DebugToolbarBean.get()
							.info("values are:" + rtnData.getCustName() + ":[" + rtnData.getAcctType() + "]"
									+ rtnData.getCustLocation() + rtnData.getAddress1() + rtnData.getAddress2()
									+ rtnData.getAddress3() + rtnData.getCustCity() + rtnData.getCustState()
									+ rtnData.getCustZip() + "  CUST COUNTRY CODE  " + rtnData.getCustCountryCode());

				} else {
					DebugToolbarBean.get().info("Value not found");
				}
			} else {
				DebugToolbarBean.get().info("res is false");
			}
		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception:  " + se.getMessage());
			DebugToolbarBean.get().error(se);
			rtnData.setValid(false);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);
			rtnData.setValid(false);

		} finally {
			try { // close resources
				DebugToolbarBean.get().info("Closing connections");
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
				throw new RuntimeException(e);
			}
		}

		return rtnData;

	}

	private Connection getConnection(String region) throws SQLException {
		if (region.equalsIgnoreCase("americas")) {
			DebugToolbarBean.get().info("use americas server");
			return getAMConnection();
		} else {
			DebugToolbarBean.get().info("use eme server");
			return getEUConnection();
		}
	}

	private Connection getAMConnection() throws SQLException {
		DebugToolbarBean.get().info("A - entered getAMConnection");
		if (AMconnection == null) {
			DebugToolbarBean.get().info("B - AMConnection==null");
			AMconnection = JdbcUtil.getConnection(FacesContext.getCurrentInstance(), "db2");
		} else {
			DebugToolbarBean.get().info("C - AMConnetion not null");
		}
		return AMconnection;
	}

	private Connection getEUConnection() throws SQLException {

		if (EUconnection == null) {
			// DebugToolbarBean.get().info("getting eme server");
			EUconnection = JdbcUtil.getConnection(FacesContext.getCurrentInstance(), "db2-eme");
			// DebugToolbarBean.get().info("catalog is " +
			// EUconnection.getCatalog());
		}
		return EUconnection;
	}

	private String getSchema(String region) {
		ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(),
				"configBean");
		String valueName;
		if (region.equalsIgnoreCase("americas")) {
			valueName = "Schema-Americas";
		} else {
			valueName = "Schema-EME";
		}
		String schema = (String) configBean.getValue(valueName);
		return schema;
	}

	public String getCustomerCurrency(String acctNum, String region) {
		// this is used in the parts order to display the currency for the user
		String rtn = new String("");
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			// sql to get the currency
			String sql = "SELECT a5crcd from " + getSchema(region) + ".f0301 where a5an8 = '" + acctNum + "'";
			DebugToolbarBean.get().info("getCustomerCurrency(" + acctNum + ", " + region + "):  " + sql);
			conn = getConnection(region);
			DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 02 - " + conn.getCatalog());
			st = conn.prepareStatement(sql);
			DebugToolbarBean.get().info("getCustCur: st.setString is next");
			DebugToolbarBean.get().info("getCustCur: acctNum=" + acctNum);
			// st.setString(1, new String(acctNum));
			DebugToolbarBean.get().info("getSuctCur: about to esecute");
			boolean res = st.execute();
			if (res) {
				rs = st.getResultSet();
				if (rs.isBeforeFirst()) {
					rs.next();
					rtn = rs.getString("a5crcd");
					DebugToolbarBean.get().info("Customer currency is " + rtn);
				}

				else {
					DebugToolbarBean.get().info("Value not found");
				}
			} else {
				DebugToolbarBean.get().info("res is false");
			}
		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
			DebugToolbarBean.get().error(se);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);

		} finally {
			try { // close resources
				DebugToolbarBean.get().info("Closing connections");
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
				throw new RuntimeException(e);
			}
		}

		return rtn;
	}

	// added 06/23/2017 mdz = get itm number fron crossover table f4104
	// @SuppressWarnings("unchecked")
	public Map<String, Object> getForkCrossOverInformation(String forkNum, String acctNum, String region) {
		// this gets the fork number and returns a part object with
		// information about that part
		HashMap<String, Object> rtnMap = new HashMap<String, Object>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		// String cascadeNbr = "";
		// String rtnAcctNum;
		String jdeDate = getJDEDate(new Date());
		String sql = "";
		DebugToolbarBean.get().info("getForkCrossOverInformation( ):  1st ? = " + forkNum);
		DebugToolbarBean.get().info("getForkCrossOverInformation( ):  2nd ? = " + acctNum);
		boolean res;
		// get the schema from the config docs
		String schema = getSchema(region);
		int count = 0;
		rs = null;
		boolean goodPart = false;
		DebugToolbarBean.get().info("0.goodPart = " + goodPart);
		try {

			sql = "SELECT DISTINCT ivlitm, ivan8 from " + schema + ".f4104 where trim(ivcitm) = '" + forkNum
					+ "' AND ivan8 = '" + acctNum + "' AND ivexdj > " + jdeDate + " AND iveftj <= " + jdeDate
					+ " AND ivxrt = 'C'";

			DebugToolbarBean.get().info("getForkCrossOverInformation( ):  " + sql);

			conn = getConnection(region);
			DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 03 - " + conn.getCatalog());
			st = conn.prepareStatement(sql);
			res = st.execute();
			DebugToolbarBean.get().info("getForkCrossOverInformation( ):  Executed SQL");

			if (res) {
				rs = st.getResultSet();
				// next set of lines for finding multiple values
				while (rs.next()) {
					count = count + 1;
				}
				DebugToolbarBean.get().info("getForkCrossOverInformation( ):  final count is " + count);
				// rerun the sql command to get fresh set of values
				res = st.execute();
				rs = st.getResultSet();

				if (rs.isBeforeFirst()) {
					rs.next();
					rtnMap.put("cascorpNum", rs.getString("ivlitm").trim());

				} else {
					rtnMap.put("status", "badpartnumber");
					DebugToolbarBean.get().info("getForkCrossOverInformation( ):  Fork Number = " + forkNum);
					DebugToolbarBean.get().info("getForkCrossOver( ):  Account Number = " + acctNum);
					DebugToolbarBean.get().info("getForkCrossOver( ):  Region = " + region);
					DebugToolbarBean.get().info("Value not found");
					rtnMap.put("cascorpNum", forkNum);
				}
			}
		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);

		} finally {
			try { // close resources
				DebugToolbarBean.get().info("Closing connections");
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
				throw new RuntimeException(e);
			}
		}

		return rtnMap;

	}

	// added 05/04/2018 mdz = get itm number from crossover table f4104
	// @SuppressWarnings("unchecked")
	public Map<String, Object> getPartCrossOverInformation(String partNum, String region) {
		// this gets the fork number and returns a part object with
		// information about that part
		HashMap<String, Object> rtnMap = new HashMap<String, Object>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		// String cascadeNbr = "";
		// String rtnAcctNum;
		String jdeDate = getJDEDate(new Date());
		String sql = "";
		DebugToolbarBean.get().info("getPartCrossOverInformation( ):  1st ? = " + partNum);
		boolean res;
		// get the schema from the config docs
		String schema = getSchema(region);
		int count = 0;
		rs = null;
		boolean goodPart = false;
		DebugToolbarBean.get().info("0.goodPart = " + goodPart);
		try {
			sql = "SELECT DISTINCT ivcitm from " + schema + ".f4104 where trim(ivlitm) = '" + partNum
					+ "' AND ivexdj > " + jdeDate + " AND iveftj <= " + jdeDate + " AND ivxrt = 'R'";

			DebugToolbarBean.get().info("getPartCrossOverInformation( ):  " + sql);
			conn = getConnection(region);
			DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 03 - " + conn.getCatalog());
			st = conn.prepareStatement(sql);
			res = st.execute();
			DebugToolbarBean.get().info("getPartCrossOverInformation( ):  Executed SQL");
			if (res) {
				rs = st.getResultSet();
				// next set of lines for finding multiple values
				while (rs.next()) {
					count = count + 1;
				}
				DebugToolbarBean.get().info("getPartCrossOverInformation( ):  final count is " + count);
				// rerun the sql command to get fresh set of values
				res = st.execute();
				rs = st.getResultSet();
				if (rs.isBeforeFirst()) {
					rs.next();
					rtnMap.put("cascorpNum", rs.getString("ivcitm").trim());
				} else {
					rtnMap.put("status", "badpartnumber");
					DebugToolbarBean.get().info("getPartCrossOverInformation( ):  Part Number = " + partNum);
					DebugToolbarBean.get().info("getPartCrossOver( ):  Region = " + region);
					DebugToolbarBean.get().info("Value not found");
					rtnMap.put("cascorpNum", partNum);
				}
			}
		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);
		} finally {
			try { // close resources
				DebugToolbarBean.get().info("Closing connections");
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
				throw new RuntimeException(e);
			}
		}
		return rtnMap;
	}

	public boolean checkForkInformation(String partNum, String region) {
		// this code is intended to find a good Cascade part number if an array
		// of part numbers
		// are returned from the crossover table
		DebugToolbarBean.get().info("checkForkInformation( ):  partNum-" + partNum);
		boolean rtn = false;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;

		// get the schema from the config docs
		String schema = getSchema(region);
		String sql = "SELECT DISTINCT bpitm  FROM " + schema + ".f4106 where bpitm = ?";
		DebugToolbarBean.get().info("checkForkInformation( ):  " + sql);
		try {
			conn = getConnection(region);
			DebugToolbarBean.get().info("checkForkInformation - " + conn.getCatalog());
			st = conn.prepareStatement(sql);
			st.setString(1, partNum);
			boolean res = st.execute();
			DebugToolbarBean.get().info("checkForkInformation 1. res = " + res);
			if (res) {
				rs = st.getResultSet();
				if (rs.isBeforeFirst()) {
					rs.next();
					DebugToolbarBean.get().info("checkForkInformation: rsGetString: " + rs.getString("ivlitm").trim());
				}

				else {

					DebugToolbarBean.get().info("Value not found for " + partNum);
					rtn = false;
				}
				DebugToolbarBean.get().info("checkForkInformation: rsGetString: rtn=  " + rtn);
				// rtn = true;
			}

		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
			DebugToolbarBean.get().error(se);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);
		} finally {
			try { // close resources
				DebugToolbarBean.get().info("Closing connections");
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
				throw new RuntimeException(e);
			}
		}

		return rtn;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getForkInformation(String partNum, String acctNum, String region) {
		// this gets the part information and returns a part object with
		// information about that part
		HashMap<String, Object> rtnMap = new HashMap<String, Object>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String BranchPlantList = new String("");
		String valueName;
		String jdeDate = getJDEDate(new Date());
		String customerCurrency;
		DebugToolbarBean.get().info("getForkInfo: acctNum=" + acctNum);
		try {
			// get BranchPlantList
			ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(),
					"configBean");
			DebugToolbarBean.get().info("region is " + region);
			if (region.equalsIgnoreCase("americas")) {

				valueName = "BranchPlantList-Americas";
			} else {
				valueName = "BranchPlantList-EME";
			}
			Object BPObj = configBean.getValue(valueName);
			DebugToolbarBean.get().info("object type is " + BPObj.getClass().toString());
			if (BPObj.getClass().toString().indexOf("String") >= 0) {
				// the config value is a String
				BranchPlantList = "'" + (String) BPObj + "'";
			} else {
				ArrayList BPArray = (ArrayList) BPObj;
				BranchPlantList = BPArray.toString().replace("[", "'").replace("]", "'").replace(", ", "','");
			}
			DebugToolbarBean.get().info("BPL is " + BranchPlantList);

			// get the customer's currency
			if (region.equalsIgnoreCase("americas")) {
				String cCodeList;
				// get the customer's currency code from JDE
				DebugToolbarBean.get().info("getForkInfo: 2nd time acctNum=" + acctNum);
				String tmpCurrency = getCustomerCurrency(acctNum, region);
				// get the valid currency code(s) for Americas
				Object CCObj = configBean.getValue("CurrencyCodes-Americas");

				if (CCObj.getClass().toString().indexOf("String") >= 0) {
					// the value is a string
					cCodeList = "'" + (String) CCObj + "'";
					DebugToolbarBean.get().info("Valid codes from CCObj = " + cCodeList);
				} else {
					// the value is an array
					ArrayList CCArray = (ArrayList) CCObj;
					cCodeList = CCArray.toString().replace("[", "'").replace("]", "'").replace(", ", "','");
				}
				// if the customer's currency code is in the valid currency code
				// list
				// set up the regex pattern and matcher
				Pattern p = Pattern.compile(tmpCurrency);
				Matcher m = p.matcher(cCodeList);
				// check for a match
				if (m.find()) {
					DebugToolbarBean.get().info("Found a match for " + tmpCurrency);
					// The customer's currency code is in the list so use it
					customerCurrency = tmpCurrency;
				} else {
					DebugToolbarBean.get().info("Did not find a match for " + tmpCurrency);
					// The customer's currency is not in the list so get the
					// default currency code for Americas from the ConfigBean
					Object DCObj = configBean.getValue("DefaultCurrency-Americas");
					if (DCObj.getClass().toString().indexOf("String") >= 0) {
						// the value is a string
						customerCurrency = (String) DCObj;
						DebugToolbarBean.get().info("Assigned to default currency: " + customerCurrency);
					} else {
						// the value is an array - this should never happen as
						// there should only be one default value
						DebugToolbarBean.get().info("More than one value is set for DefaultCurrency-Americas.");
						customerCurrency = "";
					}

				}

			} else {
				// if the region is not Americas, just get the currency code
				customerCurrency = getCustomerCurrency(acctNum, region);
			}
			DebugToolbarBean.get().info("final customerCurrency is " + customerCurrency);

			// get the schema from the config docs
			String schema = getSchema(region);

			// sql to get the fork information
			String sql = "SELECT DISTINCT a.imlitm, a.imdsc1, b.bpuprc FROM " + schema + ".f4101 a INNER JOIN " + schema
					+ ".f4106 b ON a.imlitm = b.bplitm INNER JOIN " + schema + ".f4102 c ON a.imlitm = c.iblitm "
					+ "WHERE b.bpan8 = '" + acctNum + "' AND c.ibsrp1 = 'FKG' AND b.bpcrcd = '" + customerCurrency
					+ "' AND b.bpexdj > " + jdeDate + " AND b.bpeftj <= " + jdeDate + " and imlitm = '" + partNum
					+ "' AND trim(b.bpmcu)=''";

			// sql to get the fork information
			String sql2 = "SELECT DISTINCT a.imlitm, a.imdsc1, b.bpuprc FROM " + schema + ".f4101 a INNER JOIN "
					+ schema + ".f4106 b ON a.imlitm = b.bplitm INNER JOIN " + schema
					+ ".f4102 c ON a.imlitm = c.iblitm " + "WHERE b.bpan8 = 0 AND c.ibsrp1 = 'FKG' AND b.bpcrcd = '"
					+ customerCurrency + "' AND b.bpexdj > " + jdeDate + " AND b.bpeftj <= " + jdeDate
					+ " and imlitm = '" + partNum + "' AND trim(b.bpmcu)=''";
			DebugToolbarBean.get().info("getForkInformation( ) sql:  " + sql);

			conn = getConnection(region);
			DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 03 - " + conn.getCatalog());
			st = conn.prepareStatement(sql);
			// st.setString(1, partNum);
			boolean res = st.execute();
			if (res) {
				rs = st.getResultSet();
				if (rs.isBeforeFirst()) {
					rs.next();
					rtnMap.put("description", rs.getString("imdsc1").trim());
					DebugToolbarBean.get().info("description is  " + rtnMap.get("description"));
					rtnMap.put("price", rs.getDouble("bpuprc") / 10000);
					DebugToolbarBean.get().info("price is  " + rtnMap.get("price"));
					rtnMap.put("itemnumber", rs.getString("imlitm").trim());
					DebugToolbarBean.get().info("item number is  " + rtnMap.get("itemnumber"));
					rtnMap.put("status", "good");

				}

				else {
					DebugToolbarBean.get().info("getForkInformation( ) sql2:  " + sql2);
					st = conn.prepareStatement(sql2);
					// st.setString(1, partNum);
					res = st.execute();
					if (res) {
						rs = st.getResultSet();
						if (rs.isBeforeFirst()) {
							rs.next();
							rtnMap.put("description", rs.getString("imdsc1").trim());
							DebugToolbarBean.get().info("description is  " + rtnMap.get("description"));
							rtnMap.put("price", rs.getDouble("bpuprc") / 10000);
							DebugToolbarBean.get().info("price is  " + rtnMap.get("price"));
							rtnMap.put("itemnumber", rs.getString("imlitm").trim());
							DebugToolbarBean.get().info("item number is  " + rtnMap.get("itemnumber"));
							rtnMap.put("status", "good");

						}

					} else {
						rtnMap.put("status", "badpartnumber");
						DebugToolbarBean.get().info("getForkInformation( ):  Fork Number = " + partNum);
						DebugToolbarBean.get().info("getForkInformation( ):  Account Number = " + acctNum);
						DebugToolbarBean.get().info("getForkInformation( ):  Region = " + region);
						DebugToolbarBean.get().info("Value not found");
					}
				}
			} else {
				rtnMap.put("status", "badpartnumber");
				DebugToolbarBean.get().info("res is false");
			}
		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
			DebugToolbarBean.get().error(se);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);

		} finally {
			try { // close resources
				DebugToolbarBean.get().info("Closing connections");
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
				throw new RuntimeException(e);
			}
		}

		return rtnMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getPartInformation(String partNum, String acctNum, String region) {
		// this gets the part information and returns a part object with
		// information about that part

		// this is used in the parts order to display the currency for the user
		HashMap<String, Object> rtnMap = new HashMap<String, Object>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String BranchPlantList = new String("");
		String valueName;
		String jdeDate = getJDEDate(new Date());
		String customerCurrency;
		try {

			// get BranchPlantList
			ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(),
					"configBean");
			DebugToolbarBean.get().info("getPartInformation() - region is " + region);

			String tmpCurrency = getCustomerCurrency(acctNum, region);
			DebugToolbarBean.get().info("tmpCurrency is [" + tmpCurrency + "]");
			if (region.equalsIgnoreCase("americas")) {
				// added 11/02/2017 so proper branch can be accessed if Candaian
				// user - mdz
				if (tmpCurrency.equalsIgnoreCase("CAD")) {
					valueName = "BranchPlantList-Canada";
					DebugToolbarBean.get().info("tmpCurrency is [" + tmpCurrency + "]"
							+ " thus valueName should be BranchPlantList-Canada");
				} else {
					valueName = "BranchPlantList-Americas";
					DebugToolbarBean.get().info("tmpCurrency is [" + tmpCurrency + "]"
							+ " thus valueName should be BranchPlantList-Americas");
				}
			} else {
				valueName = "BranchPlantList-EME";
			}
			DebugToolbarBean.get().info("valueName is " + valueName);
			Object BPObj = configBean.getValue(valueName);
			DebugToolbarBean.get().info("object type is " + BPObj.getClass().toString());
			if (BPObj.getClass().toString().indexOf("String") >= 0) {
				// the config value is a String
				BranchPlantList = "'" + (String) BPObj + "'";
			} else {
				ArrayList BPArray = (ArrayList) BPObj;
				BranchPlantList = BPArray.toString().replace("[", "'").replace("]", "'").replace(", ", "','");
			}
			DebugToolbarBean.get().info("BPL is " + BranchPlantList);

			// get the customer's currency
			if (region.equalsIgnoreCase("americas")) {
				String cCodeList;
				// get the customer's currency code from JDE
				tmpCurrency = getCustomerCurrency(acctNum, region);
				// get the valid currency code(s) for Americas
				Object CCObj = configBean.getValue("CurrencyCodes-Americas");

				if (CCObj.getClass().toString().indexOf("String") >= 0) {
					// the value is a string
					cCodeList = "'" + (String) CCObj + "'";
					DebugToolbarBean.get().info("Valid codes from CCObj = " + cCodeList);
				} else {
					// the value is an array
					ArrayList CCArray = (ArrayList) CCObj;
					cCodeList = CCArray.toString().replace("[", "'").replace("]", "'").replace(", ", "','");
				}
				// if the customer's currency code is in the valid currency code
				// list
				// set up the regex pattern and matcher
				Pattern p = Pattern.compile(tmpCurrency);
				Matcher m = p.matcher(cCodeList);
				// check for a match
				if (m.find()) {
					DebugToolbarBean.get().info("getPartInformation() - Found a match for " + tmpCurrency);
					// The customer's currency code is in the list so use it
					customerCurrency = tmpCurrency;
				} else {
					DebugToolbarBean.get().info("getPartInformation() - Did not find a match for " + tmpCurrency);
					// The customer's currency is not in the list so get the
					// default currency code for Americas from the ConfigBean
					Object DCObj = configBean.getValue("DefaultCurrency-Americas");
					if (DCObj.getClass().toString().indexOf("String") >= 0) {
						// the value is a string
						customerCurrency = (String) DCObj;
						DebugToolbarBean.get().info("Assigned to default currency: " + customerCurrency);
					} else {
						// the value is an array - this should never happen as
						// there should only be one default value
						DebugToolbarBean.get().info("More than one value is set for DefaultCurrency-Americas.");
						customerCurrency = "";
					}

				}

			} else {
				// if the region is not Americas, just get the currency code
				customerCurrency = getCustomerCurrency(acctNum, region);
			}
			DebugToolbarBean.get().info("final customerCurrency is " + customerCurrency);

			// get the schema from the config docs
			String schema = getSchema(region);

			// sql to get the part information
			String sql = "SELECT DISTINCT a.imlitm, a.imdsc1, b.bpuprc FROM " + schema + ".f4101 a INNER JOIN " + schema
					+ ".f4106 b ON a.imlitm = b.bplitm INNER JOIN " + schema
					+ ".f4102 c ON a.imlitm = c.iblitm AND b.bpmcu = c.ibmcu "
					+ "WHERE b.bpan8 = 0 AND trim(b.bpmcu) in (" + BranchPlantList + ") AND " + "b.bpcrcd = '"
					+ customerCurrency + "'" + " AND c.ibstkt  != 'O' AND b.bpexdj > " + jdeDate + " AND b.bpeftj <= "
					+ jdeDate + " AND a.imlitm = ?";
			DebugToolbarBean.get().info("getPartInformation()( ):  " + sql);

			conn = getConnection(region);
			DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 03 - " + conn.getCatalog());
			st = conn.prepareStatement(sql);
			st.setString(1, partNum);
			boolean res = st.execute();
			if (res) {
				rs = st.getResultSet();
				if (rs.isBeforeFirst()) {
					rs.next();
					rtnMap.put("description", rs.getString("imdsc1").trim());
					DebugToolbarBean.get().info("getPartInformation() - description is  " + rtnMap.get("description"));
					rtnMap.put("price", rs.getDouble("bpuprc") / 10000);
					DebugToolbarBean.get().info("price is  " + rtnMap.get("price"));
					rtnMap.put("itemnumber", rs.getString("imlitm").trim());
					DebugToolbarBean.get().info("getPartInformation() - item number is  " + rtnMap.get("itemnumber"));
					rtnMap.put("status", "good");

				}

				else {
					rtnMap.put("status", "badpartnumber");
					DebugToolbarBean.get()
							.info("2. getPartInformation() - rtnMap status=" + rtnMap.get("status") + ".");
					DebugToolbarBean.get().info("getPartInformation():  Part Number = " + partNum);
					DebugToolbarBean.get().info("getPartInformation():  Account Number = " + acctNum);
					DebugToolbarBean.get().info("getPartInformation():  Region = " + region);
					DebugToolbarBean.get().info("Value not found");
				}
			} else {
				rtnMap.put("status", "badpartnumber");
				DebugToolbarBean.get().info("getPartInformation() - res is false");
			}
		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
			DebugToolbarBean.get().error(se);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);

		} finally {
			try { // close resources
				DebugToolbarBean.get().info("Closing connections");
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
				throw new RuntimeException(e);
			}
		}

		DebugToolbarBean.get().info("getPartInformation() - final rtnMap status=" + rtnMap.get("status") + ".");
		return rtnMap;
	}

	private Date convertJDEDate(String jdeDate) {
		Date rtn = null;
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
		calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
		calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
		calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
		if (jdeDate.matches("[0-9]+")) {
			if (!jdeDate.equals("0")) {
				// it has to be all numeric characters
				// get century
				String century = jdeDate.substring(0, 1);
				// DebugToolbarBean.get().info("century is " + century);
				// convert to Integer and add 19
				Integer centInt = 19 + new Integer(century);
				// convert back to String, should be the two left digits of year
				// DebugToolbarBean.get().info("centInt is " + centInt);
				// DebugToolbarBean.get().info("jdeDate1,3 - " +
				// jdeDate.substring(1, 3));
				century = new String(centInt.toString());
				String year = century + jdeDate.substring(1, 3);
				// DebugToolbarBean.get().info("year is " + year);
				// convert year to integer
				Integer yearInt = new Integer(year);
				calendar.set(Calendar.YEAR, yearInt);
				// now set the day of the year
				String day = jdeDate.substring(3);
				// DebugToolbarBean.get().info("day is " + day);
				// convert to Integer
				Integer dayOfYear = new Integer(day);
				calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
				rtn = calendar.getTime();

			}
		}
		return rtn;
	}

	private String getJDEDate(Date javaDate) {
		// this function takes in a Java date and returns a JDE date
		String rtn = new String("");

		SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy");
		SimpleDateFormat curYearDaySdf = new SimpleDateFormat("yyDDD");
		String Year = yearSdf.format(javaDate);
		// DebugToolbarBean.get().info("Year is " + Year);
		// DebugToolbarBean.get().info("Year two: " + Year.substring(0, 2));
		Integer centYear = new Integer(Year.substring(0, 2)) - 19;
		// DebugToolbarBean.get().info("Year two: " + centYear);
		// get the current year as two digits and the day in the year
		String curYearDay = curYearDaySdf.format(javaDate);
		// DebugToolbarBean.get().info("year and day:" + curYearDay);
		rtn = centYear.toString().concat(curYearDay);
		// DebugToolbarBean.get().info("return is " + rtn);
		return rtn;
	}

	private String sqlSelect(String region) {
		String fieldName;
		if (StringUtil.equalsIgnoreCase(region, "americas")) {
			fieldName = "kocsid";
		} else {
			fieldName = "kzcsid";
		}

		String sql = "SELECT a.shdoco, a.shdcto, a.shvr01, a.shrout, a.shcrcd, b.sdan8, "
				+ "b.sdshan, b.sddrqj, b.sdtrdj, b.sdaddj, b.sdlnid, b.sdlitm, b.sddsc1, "
				+ " b.sduorg, b.sdurab, b.sdaexp, b.sdlnty, b.sdnxtr, b.sdlttr, b.sdstop, " + "d.wwmlnm, f.swsrl1, g."
				+ fieldName + " ";
		return sql;
	}

	private String sqlOrdOpen(String region) {
		// SQLOrdOpen
		String sql = "FROM " + getSchema(region) + ".F4201 a INNER JOIN " + getSchema(region)
				+ ".F4211 b ON a.shdoco = b.sddoco " + "AND a.shdcto = b.sddcto INNER JOIN " + getSchema(region)
				+ ".F0111 d ON b.sdshan = d.wwan8 " + "AND d.wwidln =0 ";
		return sql;
	}

	private String sqlPOSearch() {
		// question mark is PO Number
		String sql = " AND b.sdvr01= ? ";
		return sql;
	}

	private String sqlOrdHistory(String region) {
		String sql = "FROM " + getSchema(region) + ".F42019 a INNER JOIN " + getSchema(region) + ".F42119 b "
				+ "ON a.shdoco = b.sddoco AND a.shdcto = b.sddcto	INNER JOIN " + getSchema(region) + ".F0111 d "
				+ "ON b.sdshan = d.wwan8 AND d.wwidln = 0 ";
		return sql;
	}

	private String sqlDuration(Integer daysToSearch) {
		/* SQLDuration */
		String searchDate = adjustDate(daysToSearch);
		String sql = "AND  b.sdtrdj > " + searchDate + " ";
		return sql;
	}

	private String sqlShipTo() {
		/* SQLShipTo */
		String sql = "AND b.sdshan =  ? "; /*
											 * only include if a ship to was
											 * selected
											 */
		return sql;
	}

	private String sqlOrdOpenHdr(String region) {
		String sql = "FROM " + getSchema(region) + ".F4201 a INNER JOIN " + getSchema(region) + ".F42119 b "
				+ "ON a.shdoco = b.sddoco AND a.shdcto = b.sddcto	INNER JOIN " + getSchema(region) + ".F0111 d "
				+ "ON b.sdshan = d.wwan8 AND d.wwidln = 0 ";
		return sql;
	}

	private String sqlStdSearch(String region) {
		// both ? are customer numbers
		// return F32943 for americas and F3294 for eme
		String tableName;
		String fieldName1;
		String fieldName2;
		String fieldName3;
		String fieldName4;
		// for the tableName return F32943 for americas and F3294 for eme
		// the field names are different too
		if (StringUtil.equalsIgnoreCase(region, "americas")) {
			tableName = "F32943";
			fieldName1 = "kokcoo";
			fieldName2 = "kodoco";
			fieldName3 = "kodcto";
			fieldName4 = "kolnid";
		} else {
			tableName = "F3294";
			fieldName1 = "kzkcoo";
			fieldName2 = "kzdoco";
			fieldName3 = "kzdcto";
			fieldName4 = "kzlnid";
		}
		String sql = "LEFT OUTER JOIN " + getSchema(region) + ".F4220 f ON (a.shdoco = f.swdoco "
				+ "AND a.shdcto = f.swdcto AND b.sditm = f.switm AND b.sdlnid = f.swlnid) " + " LEFT OUTER JOIN "
				+ getSchema(region) + "." + tableName + " g ON b.sdkcoo = g." + fieldName1 + " AND b.sddoco = g."
				+ fieldName2 + " AND b.sddcto = g." + fieldName3 + " AND b.sdlnid = g." + fieldName4
				+ " WHERE (a.shan8 = ? OR a.shshan = ?) " + "AND a.shdcto IN(" + getDocType(region)
				+ ") AND b.sdlnty in(" + getLineType(region) + ") "
				+ "AND d.wwidln = 0 AND NOT (b.sdlttr = '980' AND b.sdnxtr = '999')";
		return sql;
	}

	private String sqlSort(String sortBy) {
		String sql;
		if (StringUtil.equalsIgnoreCase(sortBy, "OrderDate")) {
			sql = " ORDER BY sdtrdj desc, shdoco";
		} else if (StringUtil.equalsIgnoreCase(sortBy, "CascadeOrder")) {
			sql = " ORDER BY shdoco";
		} else if (StringUtil.equalsIgnoreCase(sortBy, "ShipToNo")) {
			sql = " ORDER BY sdshan, shdoco";
		} else if (StringUtil.equalsIgnoreCase(sortBy, "ShipToName")) {
			sql = " ORDER BY wwmlnm, sdshan, shdoco";
		} else if (StringUtil.equalsIgnoreCase(sortBy, "PurchaseOrder")) {
			sql = " ORDER BY shvr01, shdoco";
		} else {
			sql = " ORDER BY sdtrdj desc, shdoco";
		}
		return sql;
	}

	private String sqlSOSearch() {
		// ? is the sales order number
		String sql = "AND b.sddoco= ?";
		return sql;
	}

	private PreparedStatement getPOSearchPS(Connection conn, String region, String custNumber, String PONumber,
			String sortBy) {
		// build the sql to search by PO Number
		// this needs
		// two cust numbers
		// one po number
		// two cust numbers
		// one po number
		// two cust numbers
		// one po number
		PreparedStatement st = null;
		try {
			String sqlString = sqlSelect(region) + sqlOrdOpen(region) + sqlStdSearch(region) + sqlPOSearch()
					+ " UNION ALL " + sqlSelect(region) + sqlOrdHistory(region) + sqlStdSearch(region) + sqlPOSearch()
					+ " UNION ALL " + sqlSelect(region) + sqlOrdOpenHdr(region) + sqlStdSearch(region) + sqlPOSearch()
					+ sqlSort(sortBy);
			DebugToolbarBean.get().info("sql is " + sqlString);
			st = conn.prepareStatement(sqlString);
			st.setString(1, new String(custNumber));
			st.setString(2, new String(custNumber));
			st.setString(3, new String(PONumber));
			st.setString(4, new String(custNumber));
			st.setString(5, new String(custNumber));
			st.setString(6, new String(PONumber));
			st.setString(7, new String(custNumber));
			st.setString(8, new String(custNumber));
			st.setString(9, new String(PONumber));

		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
			DebugToolbarBean.get().error(se);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);

		}

		return st;
	}

	private PreparedStatement getSOSearchPS(Connection conn, String region, String custNumber, String SONumber,
			String sortBy) {
		// build the sql that searches by Sales Order Number
		// this needs
		// two cust numbers
		// one po number
		// two cust numbers
		// one po number
		// two cust numbers
		// one po number
		PreparedStatement st = null;
		try {
			String sqlString = sqlSelect(region) + sqlOrdOpen(region) + sqlStdSearch(region) + sqlSOSearch()
					+ " UNION ALL " + sqlSelect(region) + sqlOrdHistory(region) + sqlStdSearch(region) + sqlSOSearch()
					+ " UNION ALL " + sqlSelect(region) + sqlOrdOpenHdr(region) + sqlStdSearch(region) + sqlSOSearch()
					+ sqlSort(sortBy);
			DebugToolbarBean.get().info("sql is " + sqlString);
			st = conn.prepareStatement(sqlString);
			st.setString(1, new String(custNumber));
			st.setString(2, new String(custNumber));
			st.setString(3, new String(SONumber));
			st.setString(4, new String(custNumber));
			st.setString(5, new String(custNumber));
			st.setString(6, new String(SONumber));
			st.setString(7, new String(custNumber));
			st.setString(8, new String(custNumber));
			st.setString(9, new String(SONumber));

		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
			DebugToolbarBean.get().error(se);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);

		}

		return st;
	}

	private PreparedStatement getAllSearchPS(Connection conn, String region, String custNumber, String sortBy,
			String shipTo, Integer daysToSearch) {
		// build the sql to search for all orders by customer
		// this needs
		// two cust numbers
		// one po number
		// two cust numbers
		// one po number
		// two cust numbers
		// one po number
		PreparedStatement st = null;
		try {

			String shipToSql = new String("");
			if (StringUtil.isNotEmpty(shipTo)) {
				// if the shipTo value is not empty than add that into the query
				shipToSql = sqlShipTo();
			}
			String sqlString = sqlSelect(region) + sqlOrdOpen(region) + sqlStdSearch(region) + sqlDuration(daysToSearch)
					+ shipToSql + " UNION ALL " + sqlSelect(region) + sqlOrdHistory(region) + sqlStdSearch(region)
					+ sqlDuration(daysToSearch) + shipToSql + " UNION ALL " + sqlSelect(region) + sqlOrdOpenHdr(region)
					+ sqlStdSearch(region) + sqlDuration(daysToSearch) + shipToSql + sqlSort(sortBy);
			DebugToolbarBean.get().info("sql is " + sqlString);
			st = conn.prepareStatement(sqlString);

			// add the shipTo parameter if not empty
			if (StringUtil.isNotEmpty(shipTo)) {
				st.setString(1, custNumber);
				st.setString(2, custNumber);
				st.setString(3, shipTo);
				st.setString(4, custNumber);
				st.setString(5, custNumber);
				st.setString(6, shipTo);
				st.setString(7, custNumber);
				st.setString(8, custNumber);
				st.setString(9, shipTo);
			} else {
				// there are six parameters, all custNumber
				st.setString(1, custNumber);
				st.setString(2, custNumber);
				st.setString(3, custNumber);
				st.setString(4, custNumber);
				st.setString(5, custNumber);
				st.setString(6, custNumber);
			}

		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
			DebugToolbarBean.get().error(se);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);

		}

		return st;
	}

	private PreparedStatement getOpenSearchPS(Connection conn, String region, String custNumber, String sortBy,
			String shipTo, Integer daysToSearch) {
		// build the sql to search for all orders by customer
		// this needs
		// two cust numbers
		// one po number
		// two cust numbers
		// one po number
		// two cust numbers
		// one po number
		PreparedStatement st = null;
		try {

			String shipToSql = new String("");
			if (StringUtil.isNotEmpty(shipTo)) {
				// if the shipTo value is not empty than add that into the query
				shipToSql = sqlShipTo();
			}
			String sqlString = sqlSelect(region) + sqlOrdOpen(region) + sqlStdSearch(region) + sqlDuration(daysToSearch)
					+ shipToSql + sqlSort(sortBy);
			DebugToolbarBean.get().info("sql is " + sqlString);
			st = conn.prepareStatement(sqlString);

			// add the shipTo parameter if not empty
			if (StringUtil.isNotEmpty(shipTo)) {
				st.setString(1, custNumber);
				st.setString(2, custNumber);
				st.setString(3, shipTo);
			} else {
				// there are six parameters, all custNumber
				st.setString(1, custNumber);
				st.setString(2, custNumber);
			}

		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
			DebugToolbarBean.get().error(se);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);

		}

		return st;
	}

	@SuppressWarnings("unchecked")
	private String getDocType(String region) {
		// get Document Type for the region
		String rtn = new String("");
		String valueName;
		ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(),
				"configBean");
		if (region.equalsIgnoreCase("americas")) {
			valueName = "OrderStatusDocType-Americas";
		} else {
			valueName = "OrderStatusDocType-EME";
		}
		Object BPObj = configBean.getValue(valueName);
		// DebugToolbarBean.get().info("object type is " +
		// BPObj.getClass().toString());
		if (BPObj.getClass().toString().indexOf("String") >= 0) {
			// the config value is a String
			rtn = "'" + (String) BPObj + "'";
		} else {
			ArrayList BPArray = (ArrayList) BPObj;
			rtn = BPArray.toString().replace("[", "'").replace("]", "'").replace(", ", "','");
		}
		return rtn;
	}

	@SuppressWarnings("unchecked")
	private String getLineType(String region) {
		// get Document Type for the region
		String rtn = new String("");
		String valueName;
		ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(),
				"configBean");
		if (region.equalsIgnoreCase("americas")) {
			valueName = "OrderStatusLineType-Americas";
		} else {
			valueName = "OrderStatusLineType-EME";
		}
		Object BPObj = configBean.getValue(valueName);
		// DebugToolbarBean.get().info("object type is " +
		// BPObj.getClass().toString());
		if (BPObj.getClass().toString().indexOf("String") >= 0) {
			// the config value is a String
			rtn = "'" + (String) BPObj + "'";
		} else {
			ArrayList BPArray = (ArrayList) BPObj;
			rtn = BPArray.toString().replace("[", "'").replace("]", "'").replace(", ", "','");
		}
		return rtn;
	}

	private String adjustDate(Integer daysToSearch) {
		// this will adjust the current date by the daysToSearch and return it
		// in JDE format
		String rtnString;
		Calendar calendar = Calendar.getInstance(); // this would default to now
		calendar.add(Calendar.DAY_OF_YEAR, -daysToSearch);
		Date searchDate = calendar.getTime();
		// call function to get JDE Date
		rtnString = getJDEDate(searchDate);
		return rtnString;
	}

	@SuppressWarnings("unchecked")
	public String lookupConfigValue(String lookupValue, String configName) {
		// lookupValue is the value stored in config doc
		// returns the alias string (to the right of the vertical bar |)
		String rtn;
		LinkedHashMap<String, String> lookupMap = new LinkedHashMap();

		ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(),
				"configBean");

		ArrayList<String> carrierArray = (ArrayList<String>) configBean1.getValue(configName);

		String key = new String();
		String value = new String();
		// load up a map
		if (carrierArray.size() > 0) {
			for (String member : carrierArray) {
				value = StringUtil.trim(member.substring(0, member.indexOf("|")));
				key = StringUtil.trim(member.substring(member.lastIndexOf('|') + 1));
				// DebugToolbarBean.get().info("key is " + key);
				// DebugToolbarBean.get().info("alias is " + value);
				lookupMap.put(key, value);
			}
		}
		DebugToolbarBean.get().info("lookup value is " + lookupValue);
		if (lookupMap.containsKey(lookupValue)) {
			rtn = lookupMap.get(lookupValue);
			DebugToolbarBean.get().info("key found" + rtn);
		} else {
			rtn = new String("");
		}

		return rtn;
	}

	public void testPOSearch() {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			conn = getConnection("americas");
			// st = getPOSearchPS(conn,"eme", "1933","IOR15-8340", "OrderDate");
			st = getPOSearchPS(conn, "americas", "223400", "P0624A", "OrderDate");
			boolean res = st.execute();
			DebugToolbarBean.get().info("res is " + res);
			if (res) {
				rs = st.getResultSet();
				while (rs.next()) {
					String sddsc1 = rs.getString("sddsc1");
					Double sduorg = rs.getDouble("sduorg");
					Double sdaexp = rs.getDouble("sdaexp");
					DebugToolbarBean.get().info("sddsc1 :" + sddsc1);
					DebugToolbarBean.get().info("sduorg: " + sduorg);
					DebugToolbarBean.get().info("sdaexp: " + sdaexp);
				}
			}
		} catch (Exception e) {
			DebugToolbarBean.get().info("ERROR: " + e.toString());
		}

	}

	public void testSOSearch(String region, String custNumber, String SONumber) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			conn = getConnection(region);
			// st = getPOSearchPS(conn,"eme", "1933","IOR15-8340", "OrderDate");
			st = getSOSearchPS(conn, region, custNumber, SONumber, "OrderDate");
			boolean res = st.execute();
			DebugToolbarBean.get().info("res is " + res);
			if (res) {
				rs = st.getResultSet();
				while (rs.next()) {
					String sddsc1 = rs.getString("sddsc1");
					Double sduorg = rs.getDouble("sduorg");
					Double sdaexp = rs.getDouble("sdaexp");
					DebugToolbarBean.get().info("sddsc1 :" + sddsc1 + " sduorg: " + sduorg + " sdaexp: " + sdaexp);
				}
			}
		} catch (Exception e) {
			DebugToolbarBean.get().info("ERROR: " + e.toString());
		}

	}

	public void testSearchAll(String region, String custNumber, String shipTo, Integer daysToSearch) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			conn = getConnection(region);
			// st = getPOSearchPS(conn,"eme", "1933","IOR15-8340", "OrderDate");
			st = getAllSearchPS(conn, region, custNumber, "OrderDate", shipTo, daysToSearch);
			boolean res = st.execute();
			DebugToolbarBean.get().info("res is " + res);
			if (res) {
				rs = st.getResultSet();
				while (rs.next()) {
					String sddsc1 = rs.getString("sddsc1");
					Double sduorg = rs.getDouble("sduorg");
					Double sdaexp = rs.getDouble("sdaexp");
					String sonumber = rs.getString("shdoco");
					DebugToolbarBean.get().info(
							"sddsc1 :" + sddsc1 + " sduorg: " + sduorg + " sdaexp: " + sdaexp + " shdoco:" + sonumber);
				}
			}
		} catch (Exception e) {
			DebugToolbarBean.get().info("ERROR: " + e.toString());
		}

	}

	public void testSearchOpen(String region, String custNumber, String shipTo, Integer daysToSearch) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			conn = getConnection(region);
			// st = getPOSearchPS(conn,"eme", "1933","IOR15-8340", "OrderDate");
			st = getAllSearchPS(conn, region, custNumber, "OrderDate", shipTo, daysToSearch);
			boolean res = st.execute();
			DebugToolbarBean.get().info("res is " + res);
			if (res) {
				rs = st.getResultSet();
				while (rs.next()) {
					String sddsc1 = rs.getString("sddsc1");
					Double sduorg = rs.getDouble("sduorg");
					Double sdaexp = rs.getDouble("sdaexp");
					String sonumber = rs.getString("shdoco");
					DebugToolbarBean.get().info(
							"sddsc1 :" + sddsc1 + " sduorg: " + sduorg + " sdaexp: " + sdaexp + " shdoco:" + sonumber);
				}
			}
		} catch (Exception e) {
			DebugToolbarBean.get().info("ERROR: " + e.toString());
		}

	}

	public void testJDEDate() {
		DebugToolbarBean.get().info("115140: " + convertJDEDate("115140"));
		DebugToolbarBean.get().info("today in JDE time is  " + getJDEDate(new Date()) + " and converted back is "
				+ convertJDEDate(getJDEDate(new Date())));
	}

	public boolean testJDE(String region) {
		boolean rtn = false;

		Connection conn = null;
		try {
			DebugToolbarBean.get().info("starting test connection in testJDE");
			conn = getConnection(region);

			DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 04 - testJDE" + conn.getCatalog());

			DebugToolbarBean.get().info("Setting rtn to true in testJDE");
			rtn = true;
		} catch (SQLException se) {

			DebugToolbarBean.get().info("SQL exception in testJDE:JDESupport: " + se.getMessage());
			DebugToolbarBean.get().error(se);

		} catch (Exception e) {
			DebugToolbarBean.get().info("Exception in testJDE:JDESupport: " + e.getMessage());
			DebugToolbarBean.get().error(e);

		} finally {
			try { // close resources
				DebugToolbarBean.get().info("Closing connections - test JDE");

				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION - testJDE:JDESupport" + e.toString());
				throw new RuntimeException(e);
			}
		}
		DebugToolbarBean.get().info("ending testJDE:JDESupport = " + rtn);
		return rtn;
	}

	public JDESupport() {

	}

	public Map<String, String> getMHPdata(String mhpItem, String mhpRev, String region, String jdbcTable) {
		// this gets the part information and returns a part object with
		// information about that part
		Map<String, String> rtnMap = new HashMap<String, String>();

		// DebugToolbarBean.get().info("Entering JDESupport:getMHPdata");
		// this is used in the parts order to display the currency for the
		// user
		// HashMap<String, Object> rtnMap = new HashMap<String, Object>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;

		try {
			// DebugToolbarBean.get().info("region is " + region);
			// DebugToolbarBean.get().info("jdbcTable is " + jdbcTable);
			String sql;

			// sql to get the mhp information

			// >>> sql call for table F5530013 <<< //
			sql = "Select pdlitm, pdelbn, pdedmv, pdedma from " + jdbcTable + " where pdlitm = '" + mhpItem
					+ "' and pdmerl = '" + mhpRev
					+ "' and  (pdelbn = 'ADDITIONAL INFORMATION' or pdelbn='ATTACHMENT MASS-KG' or pdelbn = 'ATTACHMENT MASS-LB'"
					+ " or pdelbn = 'POSITION 1 HORIZONTAL CG-IN' or pdelbn = 'POSITION 1 HORIZONTAL CG-MM'"
					+ " or pdelbn = 'POSITION 1 CAPACITY BETWEEN ARMS-KG' or pdelbn = 'POSITION 1 CAPACITY BETWEEN ARMS-LB'"
					+ " or pdelbn = 'POSITION 1 CAPACITY ON ARMS-KG' or pdelbn = 'POSITION 1 CAPACITY ON ARMS-LB'"
					+ " or pdelbn = 'POSITION 1 LOST LOAD-MM' or pdelbn = 'POSITION 1 LOST LOAD-IN' or pdelbn = 'POSITION 1 VERTICAL CG-MM'"
					+ " or pdelbn = 'POSITION 1 VERTICAL CG-IN' or pdelbn = 'POSITION 1 LOAD CENTER-MM' or pdelbn = 'POSITION 1 LOAD CENTER-IN'"
					+ " or pdelbn = 'MINIMUM OPERATING FLOW-LPM' or pdelbn = 'MINIMUM OPERATING FLOW-GPM' or pdelbn = 'MAXIMUM OPERATING PRESSURE-PSI'"
					+ " or pdelbn = 'MAXIMUM OPERATING PRESSURE-BAR' or pdelbn = 'RECOMMENDED OPERATING FLOW-GPM'"
					+ " or pdelbn = 'RECOMMENDED OPERATING FLOW-LPM' or pdelbn = 'POSITION 2 HORIZONTAL CG-MM'"
					+ " or pdelbn = 'POSITION 2 HORIZONTAL CG-IN' or pdelbn = 'MAXIMUM OPERATING FLOW-LPM' or pdelbn = 'MAXIMUM OPERATING FLOW-GPM'"
					+ " or pdelbn = 'POSTION 1 VERTICAL CG-MM' or pdelbn = 'POSTION 1 VERITICAL CG-IN'"
					+ " or pdelbn = 'POSITION 1 LATERAL CG-MM' or pdelbn = 'POSITION 1 LATERAL CG-IN'"
					+ " or pdelbn = 'POSITION 3 HORIZONTAL CG-MM' or pdelbn = 'POSITION 3 HORIZONTAL CG-IN'"
					+ " or pdelbn = 'POSITION 3 VERTICAL CG-MM' or pdelbn = 'POSITION 3 VERTICAL CG-IN'"
					+ " or pdelbn = 'POSITION 3 LATERAL CG-MM' or pdelbn = 'POSITION 3 LATERAL CG-IN'"
					+ " or pdelbn = 'POSITION 2 VERTICAL CG-MM' or pdelbn = 'POSITION 2 VERTICAL CG-IN'"
					+ " or pdelbn = 'POSITION 2 LATERAL CG-MM' or pdelbn = 'POSITION 2 LATERAL CG-IN'"
					+ " or pdelbn = 'POSITION 4 HORIZONTAL CG-MM' or pdelbn = 'POSITION 4 HORIZONTAL CG-IN'"
					+ " or pdelbn = 'POSITION 4 VERTICAL CG-MM' or pdelbn = 'POSITION 4 VERTICAL CG-IN'"
					+ " or pdelbn = 'POSITION 4 LATERAL CG-MM' or pdelbn = 'POSITION 4 LATERAL CG-IN'"
					+ " or pdelbn = 'POSITION 2 LOST LOAD-MM' or pdelbn = 'POSITION 2 LOST LOAD-IN'"
					+ " or pdelbn = 'POSITION 2 CAPACITY ON ARMS-KG' or pdelbn = 'POSITION 2 CAPACITY ON ARMS-LB'"
					+ " or pdelbn = 'POSITION 2 CAPACITY BETWEEN ARMS-KG' or pdelbn = 'POSITION 2 CAPACITY BETWEEN ARMS-LB'"
					+ " or pdelbn = 'DESIGNED OPERATING PRESSURE-BAR' or pdelbn = 'DESIGNED OPERATING PRESSURE-PSI'"
					+ " or pdelbn = 'POSITION 2 LOAD CENTER-MM' or pdelbn = 'POSITION 2 LOAD CENTER-IN')";

			DebugToolbarBean.get().info(sql);
			conn = getConnection(region);
			// DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 03 - " +
			// conn.getCatalog());
			st = conn.prepareStatement(sql);
			// DebugToolbarBean.get().info("executing st");
			boolean res = st.execute();
			// DebugToolbarBean.get().info("done executing st");
			if (res) {
				// DebugToolbarBean.get().info("jde:res exists");
				rs = st.getResultSet();
				// >>> sql return from table F5530013 <<< //
				while (rs.next()) {
					// DebugToolbarBean.get().info("in rs.next");
					String pdlbn = rs.getString("pdelbn").trim(); // F550003
																	// table
					pdlbn = pdlbn.replaceAll("\\s+", "");
					if (rs.getString("pdedma").trim().equals("") || rs.getString("pdedma").trim().equals(0)) {
						// DebugToolbarBean.get().info(pdlbn + " is getting
						// pdedmv");
						rtnMap.put(pdlbn, rs.getString("pdedmv").trim());
					} else {
						// DebugToolbarBean.get().info(pdlbn + "is getting
						// pdedma");
						rtnMap.put(pdlbn, rs.getString("pdedma").trim());
					}
				}
			} else {
				rtnMap.put("status", "Model Number " + mhpItem + " with Revision Number " + mhpRev
						+ " currently is not in our database.  Please contact Cascade Customer Service at 800 CASCADE.");
				// DebugToolbarBean.get().info("res is false");
			}
		} catch (SQLException se) {
			DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
			DebugToolbarBean.get().error(se);
		} catch (Exception e) {
			DebugToolbarBean.get().info("!!!error in exception!!!");
			DebugToolbarBean.get().error(e);
		} finally {
			try { // close resources
					// DebugToolbarBean.get().info("Closing connections");
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
				throw new RuntimeException(e);
			}
		}
		// DebugToolbarBean.get().info("JDESupport:getMHPdata - returning
		// hashmap");
		// print out results
		// DebugToolbarBean.get().info("Map Size: " + rtnMap.size());
		if (rtnMap.size() == 0) {
			rtnMap.put("status", "DATA UNAVAILABLE AT THIS TIME, PLEASE CONTACT CASCADE");
		}
		for (Object mapVal : rtnMap.entrySet()) {
			// DebugToolbarBean.get().info(mapVal);
		}
		// DebugToolbarBean.get().info("Map Size: " + rtnMap.size());
		return rtnMap;
	}

	public String getModelNbrData(String serial, String region, String jdbcTable) {
		DebugToolbarBean.get().info("Entering JDESupport:getModelNbrData");
		String ModelNbr = "";

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			DebugToolbarBean.get().info("region is " + region);
			DebugToolbarBean.get().info("jdbcTable is " + jdbcTable);
			String sql;
			sql = "Select SWLITM from " + jdbcTable + " where SWSRL1 = '" + serial + "'";
			DebugToolbarBean.get().info(sql);
			conn = getConnection(region);
			DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 03 - " + conn.getCatalog());
			st = conn.prepareStatement(sql);
			DebugToolbarBean.get().info("executing st");
			boolean res = st.execute();
			DebugToolbarBean.get().info("done executing st");
			if (res) {
				DebugToolbarBean.get().info("jde:ModelNbrData exists");
				rs = st.getResultSet();
				while (rs.next()) {
					if (rs.getString("SWLITM").trim().equals("")) {
						ModelNbr = "";
					} else {
						ModelNbr = rs.getString("SWLITM").trim();
					}
				}

			} else {
				// do something - no result set
				DebugToolbarBean.get().info("No result set returned");
			}
		} catch (SQLException se) {
			DebugToolbarBean.get().info("error in sqlexception" + se.getMessage());
			DebugToolbarBean.get().error(se);
		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);
		}
		DebugToolbarBean.get().info("returning model nbr");
		DebugToolbarBean.get().info("Model Nbr = " + ModelNbr);
		return ModelNbr;
	}

	public JDEData getShipAcctNbr(String serial, String region) {
		// for a given serial number and region of a unit, this function
		// returns the acount number that unit was originally shipped to.
		DebugToolbarBean.get().info("{getShipAcctNumber} =>>> ENTERING <<<");
		JDEData rtnData = new JDEData();
		rtnData.setValid(false);
		// String accountNumber;
		String acctNumber;
		String shipDate;
		ResultSet rs = null;
		PreparedStatement st = null;
		Connection conn = null;
		String schema;
		schema = getSchema(region);
		DebugToolbarBean.get().info("getShipAcctNbr(region) =>>>" + region + "<<<");
		DebugToolbarBean.get().info("getShipAcctNbr(schema) =>>>" + schema + "<<<");
		try {
			conn = getConnection(region);
			DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 01 - " + conn.getCatalog());
			String sqlString;
			sqlString = new String(
					"Select swdoco, swdcto, swshan, swlitm, swlnid,(DATE(digits(DECIMAL(swshpj+1900000,7,0)))) AS ShipDate  From  "
							+ schema + ".f4220 where swsrl1='" + serial + "'");
			DebugToolbarBean.get().info("getShipAcctNbr sqlString:  " + sqlString);
			st = conn.prepareStatement(sqlString);
			boolean res = st.execute();
			if (res) {
				DebugToolbarBean.get().info("1");
				rs = st.getResultSet();
				// while (rs.next()) {
				if (rs.isBeforeFirst()) {
					rs.next();
					rtnData.setValid(true);
					DebugToolbarBean.get().info("2");
					acctNumber = rs.getString("swshan");
					DebugToolbarBean.get().info("3");
					shipDate = rs.getString("ShipDate");
					DebugToolbarBean.get().info("4");
					rtnData.setAcctNumber(StringUtil.trim(acctNumber));
					DebugToolbarBean.get().info("5");
					rtnData.setShipDate(StringUtil.trim(shipDate));
					DebugToolbarBean.get().info("6");
					DebugToolbarBean.get()
							.info("values are:" + rtnData.getAcctNumber() + ">>>" + rtnData.getShipDate());
				} else {
					DebugToolbarBean.get().info("Value not found");
				}
				DebugToolbarBean.get().info("7");
				// }
			} else {
				DebugToolbarBean.get().info("res is false");
			}

		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception:  " + se.getMessage());
			DebugToolbarBean.get().error(se);
			// rtnData.setValid(false);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);
			// rtnData.setValid(false);
		} finally {
			try { // close resources
				DebugToolbarBean.get().info("Closing connections");
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
				throw new RuntimeException(e);
			}
		} // DebugToolbarBean.get().info("Map Size: " + rtnMap.size());
		return rtnData;
	}

	public JDEData getShipAcctNbr_OLD(String serial, String region) {
		// for a given serial number and region of a unit, this function
		// returns the acount number that unit was originally shipped to.
		DebugToolbarBean.get().info("{getShipAcctNumber} =>>> ENTERING <<<");
		JDEData rtnData = new JDEData();
		rtnData.setValid(false);
		String accountNumber;
		String acctNumber;
		String shipDate;
		ResultSet rs = null;
		PreparedStatement st = null;
		Connection conn = null;
		String schema;
		schema = getSchema(region);
		DebugToolbarBean.get().info("getShipAcctNbr(region) =>>>" + region + "<<<");
		DebugToolbarBean.get().info("getShipAcctNbr(schema) =>>>" + schema + "<<<");
		try {
			conn = getConnection(region);
			DebugToolbarBean.get().info("RETURN FROM GET CONNECTION 01 - " + conn.getCatalog());
			String sqlString;
			sqlString = new String("SELECT swshan,(DATE(digits(DECIMAL(swshpj+1900000,7,0)))) AS ShipDate FROM "
					+ schema + ".f4220 where swsrl1='" + serial + "'");
			DebugToolbarBean.get().info("getShipAcctNbr sqlString:  " + sqlString);
			st = conn.prepareStatement(sqlString);
			boolean res = st.execute();
			if (res) {
				DebugToolbarBean.get().info("1");
				rs = st.getResultSet();
				// while (rs.next()) {
				if (rs.isBeforeFirst()) {
					rs.next();
					rtnData.setValid(true);
					DebugToolbarBean.get().info("2");
					acctNumber = rs.getString("swshan");
					DebugToolbarBean.get().info("3");
					shipDate = rs.getString("ShipDate");
					DebugToolbarBean.get().info("4");
					rtnData.setAcctNumber(StringUtil.trim(acctNumber));
					DebugToolbarBean.get().info("5");
					rtnData.setShipDate(StringUtil.trim(shipDate));
					DebugToolbarBean.get().info("6");
					DebugToolbarBean.get()
							.info("values are:" + rtnData.getAcctNumber() + ">>>" + rtnData.getShipDate());
				} else {
					DebugToolbarBean.get().info("Value not found");
				}
				DebugToolbarBean.get().info("7");
				// }
			} else {
				DebugToolbarBean.get().info("res is false");
			}

		} catch (SQLException se) {

			DebugToolbarBean.get().info("error in sqlexception:  " + se.getMessage());
			DebugToolbarBean.get().error(se);
			// rtnData.setValid(false);

		} catch (Exception e) {
			DebugToolbarBean.get().info("error in exception");
			DebugToolbarBean.get().error(e);
			// rtnData.setValid(false);
		} finally {
			try { // close resources
				DebugToolbarBean.get().info("Closing connections");
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				DebugToolbarBean.get().info("FATAL EXCEPTION " + e.toString());
				throw new RuntimeException(e);
			}
		} // DebugToolbarBean.get().info("Map Size: " + rtnMap.size());
		return rtnData;
	}
}
