package com.cascorp;
  
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.FacesContext;

import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import com.cascorp.JDESupport.JDEData;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.DominoUtils;

public class  OrderStatusBean implements Serializable{
	//used in the OrderStatus to do the searching of orders
	private static final long serialVersionUID = 1L;
	private static boolean showDBar = true;
	String userName;
	String region;
	String acctNumber;
	boolean inOrderStatusGroup;
	String companyName;
	String phoneNbr;
	//11-30-18 DSR - Added support for usertype & firstname for portals
	String userType;
	String firstName;
	String lastName;
	
	JDEData compInfo;
	ArrayList<JDESupport.orderData> orders;
	private String address1;
	private String address2;
	private String address3;
	private String city;
	private String zip;
	
	
	private String countryCode;
	private String state;
	//these fields are bound to the search criteria
	private String shipToNumberSelection;
	private String sortSelection;
	private String dateSelection;
	private String searchSelection;
	private String searchByList;
	private String searchInput;
	private String acctNbrInput;  // DSR - 12-10-18 to support search on cust account number
	
	public String getShipToNumberSelection() {
		return shipToNumberSelection;
	}
	public void setShipToNumberSelection(String shipToNumberSelection) {
		this.shipToNumberSelection = shipToNumberSelection;
	}
	public String getSortSelection() {
		return sortSelection;
	}
	public void setSortSelection(String sortSelection) {
		this.sortSelection = sortSelection;
	}
	public String getSearchSelection() {
		return searchSelection;
	}
	public void setSearchSelection(String searchSelection) {
		this.searchSelection = searchSelection;
	}
	public String getSearchByList() {
		return searchByList;
	}
	public void setSearchByList(String searchByList) {
		this.searchByList = searchByList;
	}
	public String getSearchInput() {
		return searchInput;
	}
	public void setSearchInput(String searchInput) {
		this.searchInput = searchInput;
	}
	//DSR - 12-10-18 added to support search by cust acct nbr
	public String getAcctNbrInput() {
		return acctNbrInput;
	}
	public void setAcctNbrInput(String acctNbrInput) {
		this.acctNbrInput = acctNbrInput;
	}

	public void setDateSelection(String dateSelection) {
		this.dateSelection = dateSelection;
	}
	public String getDateSelection() {
		return dateSelection;
	}
	
	
	public JDEData getCompInfo() {
		return compInfo;
	}
	public void setCompInfo(JDEData compInfo) {
		this.compInfo = compInfo;
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
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public boolean hasResults() {
		//called to determine if there are results to show
		if (orders.isEmpty()){
			return false;
		} else {
			return true;
		}
}
	
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getPhoneNbr() {
		return phoneNbr;
	}
	public void setPhoneNbr(String phoneNbr) {
		this.phoneNbr = phoneNbr;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	//DSR - 11-30-18 - Added support for My Cascade
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	// eoc
	
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}

	public String getAcctNumber() {
		return acctNumber;
	}
	public void setAcctNumber(String acctNumber) {
		this.acctNumber = acctNumber;
	}

	public boolean isInOrderStatusGroup() {
		return inOrderStatusGroup;
	}
	public void setInOrderStatusGroup(boolean inOrderStatusGroup) {
		this.inOrderStatusGroup = inOrderStatusGroup;
	}

	/*public JDEData getCompanyDetails() {
		return companyDetails;
	}
	public void setCompanyDetails(JDEData companyDetails) {
		this.companyDetails = companyDetails;
	}*/

	@SuppressWarnings("unchecked")
	public boolean checkOrderStatusGroup() throws NotesException{
		//this determines if the user is in the group allowed to view the order status
		//it compares the group's the user is in to the allowed groups from the configBean
		boolean rtn = false;
		//get the allowed groups from the config doc
		ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(
				FacesContext.getCurrentInstance(), "configBean");
		String valueName = "OrderStatusGroups";
		Object GroupObj = configBean.getValue(valueName);
		ArrayList<String> allowedGroups = new ArrayList<String>();
		if (GroupObj.getClass().toString().indexOf("String") >= 0) {
			// the config value is a String
			allowedGroups.add(GroupObj.toString());
		} else {
			//allowed groups is an ArrayList 
			ArrayList GroupArray = (ArrayList) GroupObj;
			allowedGroups.addAll(GroupArray);
		}
		debugMsg("allowed groups are " + allowedGroups.toString());

		
		//get a list of all the user groups
		Session session = DominoUtils.getCurrentSession();
		//get a list of all the groups the user is in
		Vector groupVector = session.getUserGroupNameList();
		for (Object groupObj : groupVector){
			Name groupName = (Name) groupObj;
			//loop through allowable group names
			debugMsg("checking group: " + groupName.getCommon());
			for (String allowedGroupName : allowedGroups){
				if (StringUtil.equalsIgnoreCase(allowedGroupName, groupName.getCommon())){
					debugMsg("found user in " + allowedGroupName);
					//the user is in the group that is allowed
					rtn = true;
				}
			}
		}
		return rtn;
	}
	
	public String[] getCompanyAddressArray(){
		//this is used to display the address information
		ArrayList<String> rtnAddress = new ArrayList<String>();
		rtnAddress.add( this.address1);
		if (StringUtil.isNotEmpty(this.address2)){
			rtnAddress.add(this.address2);
			if (StringUtil.isNotEmpty(this.address3)){
				rtnAddress.add(this.address3);
			}
		}
		return rtnAddress.toArray(new String[rtnAddress.size()]);
	}
	
	 public OrderStatusBean() {
		 //get the user name
		 
		 try {
			this.userName = DominoUtils.getCurrentSession().getEffectiveUserName();
			//this.firstName = DominoUtils.getCurrentSession().getCommonUserName();
			Session sessionAsSigner = ExtLibUtil.getCurrentSessionAsSigner();
			if (!StringUtil.equalsIgnoreCase(this.userName,"Anonymous"));
			//if the user is not Anonymous than setup the user details
			//get the Person Document
			//System.out.println("UN: " + this.userName);
			Map<String,String> userInfo = RegistrationUtilities.getAccountNumber(this.userName, sessionAsSigner);
			this.region =  userInfo.get("region");
			this.acctNumber = userInfo.get("acctnumber");
			this.phoneNbr = userInfo.get("phoneNbr");
			
			//DSR - 11-30-18 - Added usertype & firstname support for portals
			this.userType = userInfo.get("userType");
			//this.userType = "OEM";
			this.firstName = userInfo.get("firstName");
			this.lastName = userInfo.get("lastName");
			
			//Find out if user is in the order status groups, get the list of groups from the setup doc
			this.inOrderStatusGroup = checkOrderStatusGroup();
			//get the company details
			
			if (this.acctNumber!="Not Found") {
				JDESupport jdeSupport = new JDESupport();
				JDEData companyDetails =jdeSupport.getAcctDetails(this.acctNumber, this.region);
			
				this.companyName = companyDetails.getCustName();
				this.address1 = companyDetails.getAddress1();
				this.address2 = companyDetails.getAddress2();
				this.address3 = companyDetails.getAddress3();
				this.city = companyDetails.getCustCity();
				this.zip = companyDetails.getCustZip();
				this.setState(companyDetails.getCustState());
				this.countryCode = companyDetails.getCustCountryCode();
			} else {
				this.companyName = userInfo.get("companyName");
				this.address1 = userInfo.get("Address1");
				this.address2 = userInfo.get("Address2");
				this.address3 = userInfo.get("Address3");
				this.city = userInfo.get("CustCity");
				this.zip = userInfo.get("CustZip");
				this.setState(userInfo.get("CustState"));
				this.countryCode = userInfo.get("CustCountryCode");
			}
			
			//init input values used on the page
			this.shipToNumberSelection = "All";
			this.sortSelection = "OrderDate";
			this.dateSelection = "30";
			this.searchSelection = "Open" ;
			this.searchByList = "PONumber";
			this.searchInput = new String("");
			this.acctNbrInput = new String("");
			
			//init orders
			orders = new  ArrayList<JDESupport.orderData>();
		
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	 
	 
	 
	 
	public void performQuery1(){
		//setup the variables to call the performQuery in JDESupport
		//this is used in Search By Range
		JDESupport jde = new JDESupport();
		Integer daysSearch;
		//convert days to integer
		try{
			daysSearch = Integer.parseInt(this.dateSelection);
		} catch (NumberFormatException nfe) {
			//default to 180 if a bad date
			DebugToolbarBean.get().info("Bad Number, setting to 180 days");
			daysSearch = 180;
		}
		DebugToolbarBean.get().info("Number is " + daysSearch);
		
		orders = jde.performQuery(this.region,this.acctNumber, this.searchSelection, 
				this.shipToNumberSelection, daysSearch,
				this.sortSelection,this.searchByList, this.searchInput , "range");	
	}
	public void performQuery2(){
		//setup the variables to call the performQuery in JDESupport
		//this is used in Search for a specific order
		JDESupport jde = new JDESupport();
		Integer daysSearch;
		//convert days to integer
		try{
			daysSearch = Integer.parseInt(this.dateSelection);
		} catch (NumberFormatException nfe) {
			//default to 180 if a bad date
			DebugToolbarBean.get().info("Bad Number, setting to 180 days");
			daysSearch = 180;
		}
		daysSearch = 800;
		DebugToolbarBean.get().info("Number is " + daysSearch);
		//"223400", "2219841")    this.searchSelection
		orders = jde.performQuery(this.region, this.acctNumber , this.searchSelection, 
				this.shipToNumberSelection, daysSearch,
				this.sortSelection,this.searchByList, this.searchInput , "bynumber");
		
	}
	
	public void performQueryTest(String region, String acctNum){
		//testing only
		JDESupport jde = new JDESupport();
		
		orders = jde.setSampleQuery(region, acctNum, "" , 180);
	}
	
	public void performQuery3(){
		//testing only
		JDESupport jde = new JDESupport();
		orders = jde.setSampleOrder2();
	}
	public void performQueryByAcctNbr(){
		//setup the variables to call the performQuery in JDESupport
		//this is used in Search By Range
		JDESupport jde = new JDESupport();
		Integer daysSearch;
		//convert days to integer
		try{
			daysSearch = Integer.parseInt(this.dateSelection);
		} catch (NumberFormatException nfe) {
			//default to 180 if a bad date
			DebugToolbarBean.get().info("Bad Number, setting to 180 days");
			daysSearch = 180;
		}
		DebugToolbarBean.get().info("Number is " + daysSearch);
		
		orders = jde.performQuery(this.region,this.acctNbrInput, this.searchSelection, 
				this.shipToNumberSelection, daysSearch,
				this.sortSelection,this.searchByList, this.searchInput , "range");	
		DebugToolbarBean.get().info("Account Number is " + this.acctNbrInput);
		
	}
	
	public ArrayList<JDESupport.orderData> getOrders() {
		//returns the orders found
		return orders;
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
	public void setState(String state) {
		this.state = state;
	}
	public String getState() {
		return state;
	}
	
}
