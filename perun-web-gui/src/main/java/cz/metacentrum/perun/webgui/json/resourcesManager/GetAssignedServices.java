package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Ajax query to get assigned services for specified resource
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class GetAssignedServices implements JsonCallback, JsonCallbackTable<Service> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url for services
	private final String JSON_URL = "resourcesManager/getAssignedServices";
	// Data provider and tables
	private ListDataProvider<Service> dataProvider = new ListDataProvider<Service>();
	private PerunTable<Service> table;
	private ArrayList<Service> list = new ArrayList<Service>();
	// Selection model
	final MultiSelectionModel<Service> selectionModel = new MultiSelectionModel<Service>(new GeneralKeyProvider<Service>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Service, String> tableFieldUpdater;
	// resource ID
	private int resourceId;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;

	/**
	 * Creates a new ajax query
	 *
	 * @param resourceId ID of resource to get services for
	 */
	public GetAssignedServices(int resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * Creates a new ajax query
	 *
	 * @param resourceId ID of resource to get services for
	 * @param events external events for this query
	 */
	public GetAssignedServices(int resourceId, JsonCallbackEvents events) {
		this.events = events;
		this.resourceId = resourceId;
	}

	/**
	 * Returns table with assigned services on resource with custom onClick
	 * 
	 * @param fu custom onClick action
	 * @return table widget
	 */
	public CellTable<Service> getTable(FieldUpdater<Service, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table with assigned services on resource
	 * 
	 * @return table widget
	 */
	public CellTable<Service> getTable(){

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Service>(list);

		// Cell table
		table = new PerunTable<Service>(list);
		
		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Service> columnSortHandler = new ListHandler<Service>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);
		
		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Service> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		
		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();			
		}
		
		table.addIdColumn("Service Id", tableFieldUpdater, 110);
		
		table.addNameColumn(tableFieldUpdater);
		
		return table;

	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData(){
		final String param = "resource=" + this.resourceId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

    /**
     * Sorts table by objects Name
     */
    public void sortTable() {
        list = new TableSorter<Service>().sortByName(getList());
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Add object as new row to table
     *
     * @param object Service to be added as new row
     */
    public void addToTable(Service object) {
        list.add(object);
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Removes object as row from table
     *
     * @param object Service to be removed as row
     */
    public void removeFromTable(Service object) {
        list.remove(object);
        selectionModel.getSelectedSet().remove(object);
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Clear all table content
     */
    public void clearTable(){
        loaderImage.loadingStart();
        list.clear();
        selectionModel.clear();
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Clears list of selected items
     */
    public void clearTableSelectedSet(){
        selectionModel.clear();
    }

    /**
     * Return selected items from list
     *
     * @return return list of checked items
     */
    public ArrayList<Service> getTableSelectedList(){
        return JsonUtils.setToList(selectionModel.getSelectedSet());
    }

    /**
     * Called, when an error occurs
     */
    public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Error while loading assigned services.");
        loaderImage.loadingError(error);
        events.onError(error);
    }

    /**
     * Called, when loading starts
     */
    public void onLoadingStart() {
        session.getUiElements().setLogText("Loading assigned services started.");
        events.onLoadingStart();
    }

    /**
     * Called, when operation finishes successfully.
     */
    public void onFinished(JavaScriptObject jso) {
        setList(JsonUtils.<Service>jsoAsList(jso));
        sortTable();
        session.getUiElements().setLogText("Assigned services loaded: " + list.size());
        events.onFinished(jso);
        loaderImage.loadingFinished();
    }

    public void insertToTable(int index, Service object) {
        list.add(index, object);
        dataProvider.flush();
        dataProvider.refresh();
    }

    public void setEditable(boolean editable) {
        // TODO Auto-generated method stub
    }

    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    public void setList(ArrayList<Service> list) {
        clearTable();
        this.list.addAll(list);
        dataProvider.flush();
        dataProvider.refresh();
    }

    public ArrayList<Service> getList() {
        return this.list;
    }

}