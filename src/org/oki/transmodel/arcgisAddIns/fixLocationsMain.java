package org.oki.transmodel.arcgisAddIns;

import java.io.IOException;

import com.esri.arcgis.addins.desktop.Button;
import com.esri.arcgis.arcmapui.IMxDocument;
import com.esri.arcgis.carto.FeatureLayer;
import com.esri.arcgis.carto.IFeatureSelection;
import com.esri.arcgis.carto.IMap;
import com.esri.arcgis.carto.Map;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.geodatabase.IEnumIDs;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.ISelectionSet;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public class fixLocationsMain extends Button{
	private IApplication app;
	@Override
	public void onClick() throws IOException, AutomationException {
		try{
			// Get the document, map in the document, and the number of selected items
			IMxDocument mxDoc = new com.esri.arcgis.arcmapui.IMxDocumentProxy (app.getDocument());
			IMap focusMap = mxDoc.getFocusMap();
			int selectedCount=focusMap.getSelectionCount();
			if(selectedCount>0){
				// Find the correct layer... it is "CountStations"
				for(int x=0;x<focusMap.getLayerCount();x++){
					if(focusMap.getLayer(x).getName().equals("CountStations")){
						// Get the correct later as a featurelayer, then the selectionset of features on that layer
						FeatureLayer layer=(FeatureLayer) focusMap.getLayer(x);
						IFeatureSelection featSel=layer;
						ISelectionSet selSet=featSel.getSelectionSet();
						// Get the table and the X, Y, and shape fields - X and Y are filled for every record, but shape is not
						ITable table=selSet.getTarget();
						int idxShapeField=table.findField("SHAPE");
						int idxXField=table.findField("X");
						int idxYField=table.findField("Y");
						// Enumerate the selected objects
						IEnumIDs selIds=selSet.getIDs();
						int iId=selIds.next();
						// Going through selected objects
						while(iId>0){
							IRow row=table.getRow(iId);
							Point currentPoint=(Point)row.getValue(idxShapeField);
							// Got the point (above), now make sure it is null before continuing
							if(currentPoint==null){
								// Create a new point and store it
								Point newPoint=new Point();
								newPoint.setX((Double) row.getValue(idxXField));
								newPoint.setY((Double) row.getValue(idxYField));
								row.setValue(idxShapeField, newPoint);
								row.store();
							}
							iId=selIds.next();
						}
						
						// Refresh... gives a good indicator that everything is done.
						Map focusMap2=(Map)focusMap;
						focusMap2.refresh();
					}
				}
			}
		}catch (Exception ex){
			System.out.println(ex.getMessage());
		}
	}
	@Override
	public void init(IApplication app) throws IOException, AutomationException {
		this.app = app;
		super.init(app);
	}
}
