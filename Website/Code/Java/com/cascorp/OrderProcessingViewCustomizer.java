package com.cascorp;
//import java.util.Map;



import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import com.ibm.xsp.extlib.component.dynamicview.UIDynamicViewPanel;



import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.extlib.component.dynamicview.DominoDynamicColumnBuilder.DominoViewCustomizer;
import com.ibm.xsp.extlib.component.dynamicview.UIDynamicViewPanel.DynamicColumn;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign.ColumnDef;
 
public class OrderProcessingViewCustomizer  extends DominoViewCustomizer {
	//this is used to customize the DynamicViewPanels used in the Order Processing views
	
	//@SuppressWarnings("unchecked")
	  @Override
	  public void afterCreateColumn(FacesContext context, int index,
	      ColumnDef colDef, IControl column) {
	      //Get a map of the session variables to read the view session scope variable
	      //Map svals = context.getExternalContext().getSessionMap();
	      //Create a variable for the current component
	      UIComponent columnComponent = column.getComponent();
	      //Create a reference to the column and set the links to open in read mode
	      DynamicColumn dynamicColumn = (DynamicColumn) columnComponent;
	      //To have every view open the selected documents in read mode add the following
	      dynamicColumn.setOpenDocAsReadonly(true); 
	      DebugToolbarBean.get().info("name of column " + dynamicColumn.getColumnName());
	      /*
	       * If all you need to do is have the views open in read mode instead of
	       * edit mode then the above code is all you need.
	       * If you want to customize the view columns the the follow code can be
	       * used as an example.     
	      */
	      
	      if (dynamicColumn.getColumnName().equalsIgnoreCase("OrderDate")){
	    	  //make it a link
	    	  dynamicColumn.setDisplayAs("link");
	    	  DebugToolbarBean.get().info("make OrderDate a link");
	      }
	      
	    
	      //Set column properties for specific views.
	     
	      //if (svals.containsValue("processforkkit")) {
	         
	        //Hide the first column in this view
	        if(dynamicColumn.getColumnName().equalsIgnoreCase("$2")){
	          dynamicColumn.setRendered(false);
	        
	        }
	      if (colDef.isCategorized() ){
	    	  //set the expand and collapse images if the column is categorized
	    	  DebugToolbarBean.get().info("column is categorized: " + dynamicColumn.getColumnName());
	    	//  ExtendedColumnDef extColDef = (ExtendedColumnDef) colDef;
	    	 // DebugToolbarBean.get().info("twistie image: " + extColDef.twistieImage);
	    	  UIDynamicViewPanel.DynamicColumn col = (UIDynamicViewPanel.DynamicColumn)column.getComponent();
	    	 col.setExpandedImage("expand.png");
	    	 col.setCollapsedImage("collapse.png");
	    	  DebugToolbarBean.get().info("collapsed image: " + col.getCollapsedImage());
	    	  DebugToolbarBean.get().info("expanded image: " + col.getExpandedImage());
	    	  DebugToolbarBean.get().info("style class: " + col.getStyleClass());
	    	  col.setStyleClass("category_col");
	      }
	      //}
	 
	     
	     
	    super.afterCreateColumn(context, index, colDef, column);
	  }
}
