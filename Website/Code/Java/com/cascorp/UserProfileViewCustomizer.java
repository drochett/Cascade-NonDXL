package com.cascorp;

import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.openntf.xsp.debugtoolbar.beans.DebugToolbarBean;

import com.ibm.xsp.component.UICommandEx2;
import com.ibm.xsp.component.UIEventHandler;
import com.ibm.xsp.component.xp.XspEventHandler;
import com.ibm.xsp.extlib.component.dynamicview.UIDynamicViewPanel;
import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.extlib.component.dynamicview.DominoDynamicColumnBuilder;
import com.ibm.xsp.extlib.component.dynamicview.DominoDynamicColumnBuilder.DominoViewCustomizer;
import com.ibm.xsp.extlib.component.dynamicview.UIDynamicViewPanel.DynamicColumn;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign.ColumnDef;



public class UserProfileViewCustomizer extends DominoViewCustomizer {
	// this is used to customize the DynamicViewPanels
	@SuppressWarnings("unchecked")
	@Override
	public void afterCreateColumn(FacesContext context, int index,
			ColumnDef colDef, IControl column) {
		com.ibm.xsp.component.xp.XspEventHandler event = null;
		// Get a map of the session variables to read the view session scope variable
		Map svals = context.getExternalContext().getSessionMap();
		// Create a variable for the current component
		UIComponent columnComponent = column.getComponent();
		// Create a reference to the column and set the links to open in read mode
		DynamicColumn dynamicColumn = (DynamicColumn) columnComponent;
		// To have every view open the selected documents in read mode add the following
		dynamicColumn.setOpenDocAsReadonly(true);
		DebugToolbarBean.get().info("column " + dynamicColumn.getColumnName());
		/*
		 * If all you need to do is have the views open in read mode instead of
		 * edit mode then the above code is all you need. If you want to
		 * customize the view columns the the follow code can be used as an
		 * example.
		 */
		// Set column properties for specific views.
		if (svals.containsValue("attachmentsbyowner")) {
			
			// Hide the first column in this view
			if (dynamicColumn.getColumnName().equalsIgnoreCase("$1")) {
				//dynamicColumn.setRendered(false);
				dynamicColumn.setDisplayAs("hidden");
				DebugToolbarBean.get().info("hiding " + dynamicColumn.getColumnName());
	            //String type = dynamicColumn.getChildren().get(0).getClass().toString();
	           // DebugToolbarBean.get().info("type is " + type);
	            event = null; //(XspEventHandler) dynamicColumn.getChildren().get(0);
			}
			if (dynamicColumn.getColumnName().equalsIgnoreCase("icon")) {
				String url = "";
				// dynamicColumn.setIconSrc(dynamicColumn.);
				dynamicColumn.setDisplayAs("text");
				dynamicColumn.setRendered(true);
			}
			if (dynamicColumn.getColumnName().equalsIgnoreCase(
					"attachmentidentifier")) {
				// make it a link
				dynamicColumn.setDisplayAs("link");
				DebugToolbarBean.get().info(
						"making " + dynamicColumn.getColumnName()+ " column a link");
				dynamicColumn.setOnclick("Attachment");
				if (event != null){
		              dynamicColumn.getChildren().add(event);
		              DebugToolbarBean.get().info("adding event");
		          } else {
		              DebugToolbarBean.get().info("event is null");
		          }
				
			}
			if (dynamicColumn.getColumnName().equalsIgnoreCase("edit")) {
				// make it a link
				 dynamicColumn.setDisplayAs("text");
				// dynamicColumn.
				DebugToolbarBean.get().info("making " + dynamicColumn.getColumnName()+ " column a XXX");
				dynamicColumn.setRendered(true);
			}
		}
		// }

		super.afterCreateColumn(context, index, colDef, column);
	  }
}