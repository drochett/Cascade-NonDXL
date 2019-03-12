package com.cascorp;
 
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.faces.context.FacesContext;

import lotus.domino.Database;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntryCollection;

import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import com.cascorp.JDESupport.JDEData;
import com.cascorp.RegistrationUtilities;  //WSCR-ASES6S - Vendors / DXF Files
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class RegistrationBean implements Serializable{
	/*
	 * This bean is used when registering a new person
	 * The controls on the ccNewRegistration are bound to the Person inner class
	 * 
	 * This bean is session Scope and the information is then used on the Phase2NewRegistration.xsp
	 * which is where the user enters more information about themselves
	 */
	
	
	private static final long serialVersionUID = 1L;
	
	//a person object holds information about the person being registered
	Person person;
	Collection<String> errorMessages ;
	//The JDEInfo has information about a person's company from JDE, see that class
	JDESupport.JDEData JDEInfo;

	public Collection<String> getErrorMessages() {
		return errorMessages;
	}

	public Person getPerson() {
		return person;
	}


	public void setPerson(Person person) {
		this.person = person;
	}
	public void invalidatePerson(){
		//create a new person object, wiping out what was already entered or looked up
		this.person = new Person();
		
	}

	public class Person implements Serializable {
		/*
		 * This class holds all the information about a person as they enter it
		 */
		private static final long serialVersionUID = 1L;
		String acctNumber;
		String acctType; //added 04/04/2017 mdz - need to capture acctType
		String regType; //added 12-7-18 dsr - end user reg
		String firstName; //added 12-7-18 dsr - end user reg
		String lastName; //added 12-7-18 dsr - end user reg
		String email;
		String region;
		String zipCode;
		String country;
		String urlToGoTo;
		String companyName;
		String companyAddress1;
		String companyAddress2;
		String companyAddress3;
		String companyZip;
		String companyCity;
		String companyState;
		String companyCountry;
		
		public String getCompanyAddress(){
			//build one big string for the company address
			String rtn = "";
			if (StringUtil.isNotEmpty(this.companyAddress3) & StringUtil.isNotEmpty(this.companyAddress2)){
				rtn = this.companyAddress1 + ", " + this.companyAddress2 + ", " + this.companyAddress3;
			}
			else if ( StringUtil.isNotEmpty(this.companyAddress2)){
				rtn = this.companyAddress1 + ", " + this.companyAddress2;
			} else {
				rtn = this.companyAddress1;
			}	
		return rtn;
		}
		public String[] getCompanyAddressArray(){
			//return the company address as an Array of Strings
			ArrayList<String> rtnAddress = new ArrayList<String>();
			rtnAddress.add( this.getCompanyAddress1());
			if (StringUtil.isNotEmpty(this.getCompanyAddress2())){
				rtnAddress.add(this.getCompanyAddress2());
				if (StringUtil.isNotEmpty(this.getCompanyAddress3())){
					rtnAddress.add(this.getCompanyAddress3());
				}
			}
			return rtnAddress.toArray(new String[rtnAddress.size()]);
		}

		public String getCompanyName() {
			return companyName;
		}
		public void setCompanyName(String companyName) {
			this.companyName = companyName;
		}
		public String getCompanyAddress1() {
			return companyAddress1;
		}
		public void setCompanyAddress1(String companyAdress1) {
			this.companyAddress1 = companyAdress1;
		}
		public String getCompanyAddress2() {
			return companyAddress2;
		}
		public void setCompanyAddress2(String companyAddress2) {
			this.companyAddress2 = companyAddress2;
		}
		public String getCompanyAddress3() {
			return companyAddress3;
		}
		public void setCompanyAddress3(String companyAddress3) {
			this.companyAddress3 = companyAddress3;
		}
		public String getCompanyZip() {
			return companyZip;
		}
		public void setCompanyZip(String companyZip) {
			this.companyZip = companyZip;
		}
		public String getCompanyCity() {
			return companyCity;
		}
		public void setCompanyCity(String companyCity) {
			this.companyCity = companyCity;
		}
		public String getCompanyState() {
			return companyState;
		}
		public void setCompanyState(String companyState) {
			this.companyState = companyState;
		}
		public String getCompanyCountry() {
			return companyCountry;
		}
		public void setCompanyCountry(String companyCountry) {
			this.companyCountry = companyCountry;
		}
		public String getUrlToGoTo() {
			return urlToGoTo;
		}
		public void setUrlToGoTo(String urlToGoTo) {
			this.urlToGoTo = urlToGoTo;
		}
		public String getCountry() {
			return country;
		}
		public void setCountry(String country) {
			this.country = country;
		}
		boolean validated;
		boolean validAcctNumber;
		public boolean isValidAcctNumber() {
			return validAcctNumber;
		}
		public void setValidAcctNumber(boolean validAcctNumber) {
			this.validAcctNumber = validAcctNumber;
		}
		boolean showCountry;
		boolean showZip;
		public boolean isShowCountry() {
			return showCountry;
		}
		public void setShowCountry(boolean showCountry) {
			this.showCountry = showCountry;
		}
		public boolean isShowZip() {
			return showZip;
		}
		public void setShowZip(boolean showZip) {
			this.showZip = showZip;
		}
		public boolean isValidated() {
			return validated;
		}
		public void setValidated(boolean validated) {
			this.validated = validated;
		}
		public String getAcctNumber() {
			return acctNumber;
		}
		public void setAcctNumber(String acctNumber) {
			this.acctNumber = acctNumber;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		
		// added 12/7/18 - DSR - end user reg
		public String getRegType() {
			return regType;
		}
		public void setRegType(String regType) {
			this.regType = regType;
		}
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
		// end 12-7-18 add dsr
		
		public String getRegion() {
			return region;
		}
		public void setRegion(String region) {
			this.region = region;
		}
		public String getZipCode() {
			return zipCode;//.toUpperCase();
		}
		public void setZipCode(String zipCode) {
			this.zipCode = zipCode.toUpperCase();
		}
		public Person(){
			//init Person
			this.setValidated(false);
			this.setShowZip(false);
			this.setShowZip(false);
			this.setValidAcctNumber(false);
			this.setUrlToGoTo("");
			this.setRegType("oem");  //added 12-7-18 dsr: end user reg
		}
		
		//++++++++++ following added 04/04/2017 by mdz for acctTypeInfo ++++++++++++
		public String getAcctType() {
			return  acctType;
		}
		public void setAcctType(String acctType) {
			this.acctType = acctType;
		}
		//++++++++++++done with addition 04/04/2017 mdz +++++++++++++
		
	}
	
	private void showCountry(){
		person.setShowCountry(true);
		person.setShowZip(false);
	}
	private void showZip(){
		person.setShowCountry(false);
		person.setShowZip(true);
	}
	
	public void resetNewRegistration(){
		person.setCountry("");
		person.setZipCode("");
		person.setValidAcctNumber(false);
		person.setShowCountry(false);
		person.setShowZip(false);
	}
	
	public boolean lookupAccountInfo(){
		//this method will validate the user's account number for the selected region
		boolean rtn = false;
		
		//all interaction with JDE is down with JDESupport
		JDESupport jde = new JDESupport();
		//this will get all the company information
		JDEInfo = jde.getAcctDetails(person.getAcctNumber(), person.getRegion());
		if (JDEInfo.isValid()){
			if (checkABAT1code(JDEInfo)){
				DebugToolbarBean.get().info("is valid and city is " + JDEInfo.getCustCity());
				person.setValidAcctNumber(true);
				if (StringUtil.isNotEmpty(JDEInfo.getCustZip())){
					DebugToolbarBean.get().info("returned customer sip " + JDEInfo.getCustCity());
					showZip();
				} else {
					showCountry();
				}
					rtn = true;
			} else {
				//ABAT1 code is not valid
				DebugToolbarBean.get().info("checkABAT1code(JDEInfo) returned false");
			}
			} 
	
		return rtn;
	}

	@SuppressWarnings("unchecked")
	private boolean checkABAT1code(JDEData JDEinfo) {
		//validate the allowed codes match what was set in the config docs
		//can this account use the system?
		ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "configBean");
		ArrayList<String> validAmericasCodes = (ArrayList<String>) configBean.getValue("ValidABAT1-AM");
		ArrayList<String> validEmeCodes = (ArrayList<String>) configBean.getValue("ValidABAT1-EME");
		ArrayList<String> validAmericasCountries = (ArrayList<String>) configBean.getValue("ValidCountryCodes-AM");
		ArrayList<String> validEmeCountries = (ArrayList<String>) configBean.getValue("ValidCountryCodes-EME");
		DebugToolbarBean.get().info("checkABAT1code>>Americas codes are " + validAmericasCodes.toString());
		DebugToolbarBean.get().info("checkABAT1code>>EME codes are " + validEmeCodes.toString());
		DebugToolbarBean.get().info("checkABAT1code>>Valid Americas country codes are " + validAmericasCountries);
		DebugToolbarBean.get().info("checkABAT1code>>Valid EME country codes are " + validEmeCountries);
		boolean rtn = false;
		if (JDEinfo.getRegion().equalsIgnoreCase("americas")){
			DebugToolbarBean.get().info("Searching for americas codes");
			DebugToolbarBean.get().info("checkABAT1code>>Account Type is:" + JDEinfo.getAcctType()+ ":");
			DebugToolbarBean.get().info("checkABAT1code>>Country Code is:" + JDEinfo.getCustCountryCode() + ":");
			if (validAmericasCodes.contains(JDEinfo.getAcctType()) && validAmericasCountries.contains(JDEInfo.getCustCountryCode())) {
				//the americas ABAT1 code is found, return true
				DebugToolbarBean.get().info("checkABAT1code>>Americas code is valid");
				rtn = true;
			} 		
		} else {
			//check EME codes
			if (validEmeCodes.contains(JDEinfo.getAcctType())  && validEmeCountries.contains(JDEinfo.getCustCountryCode())) {
				//the eme ABAT1 code is found, return true
				rtn = true;
			} 			
		}
		
		
		return rtn;
	}


	public boolean validatePhase1(Person req) {
		DebugToolbarBean.get().info("Starting validatePhase1");
		//this is called from ccNewRegistration to validate the user's info
		// check to see if the four values are not empty
		boolean rtn = true;
		errorMessages = new ArrayList<String>();
		try {
			
			if (req.getEmail().isEmpty()) {
				rtn = false;
				errorMessages.add("emailmissing");
				DebugToolbarBean.get().info("email");
			}
			
			if (req.getRegType()=="oem") {			
				if (req.getAcctNumber().isEmpty()) {
					rtn = false;
					errorMessages.add("accountmissing");
					DebugToolbarBean.get().info("account missing");
				}
				if (req.getRegion().isEmpty()) {
					rtn = false;
					errorMessages.add("regionmissing");
					DebugToolbarBean.get().info("region");
				}
				if (req.isShowZip() && req.getZipCode().isEmpty()) {
					rtn = false;
					errorMessages.add("zipcodemissing");
					DebugToolbarBean.get().info("zip");
				}
				
				if (req.isShowCountry() && req.getCountry().isEmpty()) {
					rtn = false;
					errorMessages.add("countrymissing");
					DebugToolbarBean.get().info("country");
				}
			}
			
			//see if the email is already registered
			boolean check = RegistrationUtilities.checkAddressBook(req.getEmail());
			
			DebugToolbarBean.get().info("call2:" + check);
			if (check == false) {
				// duplicate email
				DebugToolbarBean.get().info("checkAddressBook returned false");
				rtn = false;
				errorMessages.add("duplicateemail");
			} else {
				DebugToolbarBean.get().info("checkAddressBook returned true");
			}
			//make sure account was not denied!
			boolean checkDenied = checkDeniedAccounts(person.getAcctNumber());
			if (checkDenied == false){
				//not allowed
				DebugToolbarBean.get().info("checkDeniedAccounts returned false");
				rtn = false;
				errorMessages.add("deniedaccount");
				
			} else {
				DebugToolbarBean.get().info("checkDeniedAccount returned true");
			}
			
			if (req.getRegType()=="oem") {
				//WSCR-ASES6S - Vendors / DXF Files - check if they are an allowable vendor
				DebugToolbarBean.get().info("Starting checking Vendor code.");
				boolean vendorAllowed = false; //assume not allowed
				DebugToolbarBean.get().info("1. [validatePhase1]");
				JDESupport jde = new JDESupport();
				DebugToolbarBean.get().info("2.  [validatePhase1]");
				//this will get all the company information
				JDEInfo = jde.getAcctDetails(person.getAcctNumber(), person.getRegion());
				DebugToolbarBean.get().info("3. Checking Vendor code.");
				String accountType = jde.getAcctDetails(person.acctNumber,person.region).acctType;
				DebugToolbarBean.get().info("New registration account type is:  "+accountType);
				if(accountType.equalsIgnoreCase("V")) {
					//check if it is an allowable VendorAccount
					DebugToolbarBean.get().info("checkVendorAllowed returned false");
					vendorAllowed = RegistrationUtilities.findVendorAccount(person.acctNumber)  ; 
					if(vendorAllowed==false){
						rtn = false;
						DebugToolbarBean.get().info("3. [validatePhase1] vendorAllowed = false");
						errorMessages.add("deniedaccount");
					} else{
						DebugToolbarBean.get().info("checkVendorAllowed returned true");
					}
				} else {
					//DebugToolbarBean.get().info("4. Checking Vendor code.");
				}
				DebugToolbarBean.get().info("4.  [validatePhase1]");
				
				//WSCR-ASES6S - Vendors / DXF File end of code added
				if (JDEInfo.getCustZip().isEmpty()){
					//compare country codes the user entered to what is on the account
					//only used if the zip code was not entered (non-US)
					if (!StringUtil.equals(JDEInfo.getCustCountryCode(), req.getCountry())){
						//does not match so throw error
						DebugToolbarBean.get().info("country code does not agree with entered value");
						rtn = false;
						errorMessages.add("countrycodemismatch");
					}
				} else {
					//compare zip codes for the accounts (US accounts)
					if (!StringUtil.equals(JDEInfo.getCustZip(), req.getZipCode().toUpperCase())){
						//does not match so throw error
						DebugToolbarBean.get().info("zip code does not agree with entered value");
						rtn = false;
						errorMessages.add("zipcodemismatch");
					}
				}
			}
			
			// passed initial validation
			req.setValidated(rtn);
		} catch (Exception e) {
			rtn = false;
			DebugToolbarBean.get().info("Error in validatePhase1" + e.toString());
			DebugToolbarBean.get().error(e);
			e.printStackTrace();
		}
		/*
		 * String valueExpr =
		 * "#{javascript: getComponent('inputText1').getClientId(facesContext);}"
		 * ; FacesContext fc = FacesContext.getCurrentInstance();
		 * ExpressionEvaluatorImpl evaluator = new ExpressionEvaluatorImpl( fc
		 * ); ValueBinding vb = evaluator.createValueBinding( fc.getViewRoot(),
		 * valueExpr, null, null); String vreslt = (String) vb.getValue(fc);
		 * DebugToolbarBean.get().info("test" + vreslt);
		 * FacesContext.getCurrentInstance().addMessage(vreslt, new
		 * javax.faces.application.FacesMessage( "BAD" ));
		 */
		return rtn;
	}
	
	/*boolean checkAddressBook(String email){
		//pass in email of prospective user
		//returns true if user was not found and ok to register them
		DebugToolbarBean.get().info("Starting");
		
		boolean rtn = false;
		try {
		Session s = ExtLibUtil.getCurrentSessionAsSigner();
		//	FacesContext context = FacesContext.getCurrentInstance();
		//	Session s = (Session) context.getApplication().getVariableResolver().resolveVariable(context, "sessionAsSigner");
		
			
		
		ConfigBean configBean1 = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "configBean");
		String nabDbname = (String) configBean1.getValue("UserDominoDirectoryPath");
		//String nabDbname = new String("Development\\External\\Cascade\\cascweb3final.nsf");
		//String nabDbname = getConfigValue("UserDominoDirectoryPath", s);
		DebugToolbarBean.get().info("nab db is " + nabDbname);
		if (s == null){
			DebugToolbarBean.get().info("s is not valid");
		} else if (s.isValid() == false){
			DebugToolbarBean.get().info("s is null");
		} else{
			DebugToolbarBean.get().info("s is good");
		}
		DebugToolbarBean.get().info("Session server is " + s.getServerName());
			
		
		Database nabDb = null;
		//String server = ExtLibUtil.getCurrentDatabase().getServer();
		nabDb = s.getDatabase(s.getServerName(), nabDbname , false);
		//DebugToolbarBean.get().info("is recycled is "+ isRecycled(nabDb, true));
		//DebugToolbarBean.get().info("try to get db" + nabDb.isOpen()); 
		if (nabDb!=null){
			DebugToolbarBean.get().info("Database is open and title is " + nabDb.getTitle());
			//get the view
			View nabVw = nabDb.getView("($LDAPCN)");
			DebugToolbarBean.get().info("view" + nabVw.getName());
			if (nabVw != null){
				ViewEntryCollection colPeople = nabVw.getAllEntriesByKey(email);
				DebugToolbarBean.get().info("count is:" + colPeople.getCount());
				if (colPeople.getCount() > 0) {
					//if user was found return false
					DebugToolbarBean.get().info("User was found");
				} else{
					//user was NOT found, return true, ok to register
					DebugToolbarBean.get().info("User was NOT found");
					rtn = true;
				}
				//done with the view and database
				//nabVw.recycle();
				//nabDb.recycle();
			} else {
				DebugToolbarBean.get().info("nab view not open");
			}
			
		} else {
			rtn = false;
			DebugToolbarBean.get().info("Nab db not open");
			throw new RuntimeException(	"Error in checkAddressBook: Nab DB Not Open");
		}
		} catch (Exception e) {
			rtn = false;
			DebugToolbarBean.get().info("error in checkAddressBook:dBar says " + e.toString() );
			e.printStackTrace();
			throw new RuntimeException(	"Error in checkAddressBook: " + e.toString());
			
		}
		return rtn;
	}
	
	*/
	
	boolean checkDeniedAccounts(String account){
		//return true if they NOT are in the denied list, false if they are
		boolean rtn = false;
		try {
			Session s = ExtLibUtil.getCurrentSessionAsSigner();
			ConfigBean configBean = (ConfigBean) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "configBean");
			String regDbname = (String) configBean.getValue("RegistrationDbPath");
			
			
			DebugToolbarBean.get().info("reg db is " + regDbname);
			Database regDb ;
			
			//String server = ExtLibUtil.getCurrentDatabase().getServer();
			regDb = s.getDatabase(s.getServerName(), regDbname , false);
			if (regDb!=null){
				DebugToolbarBean.get().info("Database is open and title is " + regDb.getTitle());
			
			View regVw = regDb.getView("DeniedByAcctNumView");
			if (regVw != null){
				ViewEntryCollection colAcct = regVw.getAllEntriesByKey(account, true);
				DebugToolbarBean.get().info("count is:" + colAcct.getCount());
				if (colAcct.getCount()==0){
					DebugToolbarBean.get().info("Account NOT denied");
					rtn = true;
				} else {
					//is denied
					
					DebugToolbarBean.get().info("Account is denied");
				}
			} else {
				DebugToolbarBean.get().info("DeniedAccountview not open");
				throw new RuntimeException(	"Error in checkAddressBook: Denied Account View Not Open");
			}
			
		} else {
			
			DebugToolbarBean.get().info("Reg db not open");
			throw new RuntimeException(	"Error in checkAddressBook: Registration DB Not Open");
		}
			
		} catch (Exception e) {
			rtn = false;
			DebugToolbarBean.get().info("error in checkDeniedAccounts:dBar says " + e.toString() );
			e.printStackTrace();
			throw new RuntimeException(	"Error in checkDeniedAccounts: " + e.toString());
			
		}
			
		
		return rtn;
	}
	
	public boolean submitPhase1() {
		//this is what runs when the user submits the first registration panel (ccNewRegistration)
		//it returns false if validation failed
		DebugToolbarBean.get().info("starting submitPhase1");
		try {
			if (validatePhase1(person)) {
				DebugToolbarBean.get().info("validate returns true");
				//set company information in person bean
			  if (person.regType=="oem") {
				 person.setCompanyAddress1(JDEInfo.getAddress1());
				 person.setCompanyAddress2(JDEInfo.getAddress2());
				 person.setCompanyAddress3(JDEInfo.getAddress3());
				 person.setCompanyName(JDEInfo.getCustName());
				 person.setCompanyCity(JDEInfo.getCustCity());
				 person.setCompanyState(JDEInfo.getCustState());
				 person.setCompanyZip(JDEInfo.getCustZip());
				 person.setCompanyCountry(JDEInfo.getCustCountryCode());
				 person.setAcctType(JDEInfo.getAcctType());  //added 04/04/2017 mdz - need acctType info
				 DebugToolbarBean.get().info("person.acctType>>" + person.acctType);
			  }
			  return true;
			} else {
				DebugToolbarBean.get().info("validate returns false");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
public RegistrationBean() {
	DebugToolbarBean.get().info("Init Registration Bean");
	//System.out.println("Init Registration Bean");
	person = new Person();
}
	

	

}
