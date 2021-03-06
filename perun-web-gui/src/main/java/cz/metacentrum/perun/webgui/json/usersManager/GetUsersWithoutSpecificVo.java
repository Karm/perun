package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Searching for users which can be added as member to specific vo (are not members yet).
 * Search is base on name, login and mail.
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
@Deprecated
public class GetUsersWithoutSpecificVo implements JsonCallback, JsonCallbackTable<User> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "usersManager/getUsersWithoutSpecificVo";
	// Data provider
	private ListDataProvider<User> dataProvider = new ListDataProvider<User>();
	// table
	private PerunTable<User> table;
	// table data
	private ArrayList<User> list = new ArrayList<User>();
	// Selection model
	final MultiSelectionModel<User> selectionModel = new MultiSelectionModel<User>(new GeneralKeyProvider<User>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<User, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage(true, WidgetTranslation.INSTANCE.emptySearchForUsers());

	// parameters
	private String searchString;
	private int vo;
	// checkable
	private boolean checkable = true;

	/**
	 * Creates a new request
	 * @param vo
	 */
	public GetUsersWithoutSpecificVo(int vo) {
		this.vo = vo;
	}
	
	/**
	 * Creates a new request
	 * @param vo
	 * @param searchString
	 */
	public GetUsersWithoutSpecificVo(int vo, String searchString) {
		this.vo = vo;
		this.searchString = searchString;
	}

	/**
	 * Creates a new request with custom events
	 * @param events
	 * @param vo
	 * @param searchString
	 */
	public GetUsersWithoutSpecificVo(JsonCallbackEvents events, int vo, String searchString) {
		this.events = events;
		this.vo = vo;
		this.searchString = searchString;
	}

	/**
	 * Returns table of users
	 * @param
	 */
	public CellTable<User> getTable(FieldUpdater<User, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}
	
	/**
	 * Returns table of users.
	 * @return
	 */
	public CellTable<User> getTable(){ 
		// retrieve data
		retrieveData();
		return getEmptyTable();
		
	}

	/**
	 * Returns empty table definition
	 * @return
	 */
	public CellTable<User> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<User>(list);

		// Cell table
		table = new PerunTable<User>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<User> columnSortHandler = new ListHandler<User>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<User> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// columns
		if (checkable) {
			table.addCheckBoxColumn();	
		}
		table.addIdColumn("User ID", tableFieldUpdater);

		// NAME COLUMN
		Column<User, String> nameColumn = JsonUtils.addColumn(new JsonUtils.GetValue<User, String>() {
			public String getValue(User user) {
				return user.getFullNameWithTitles(); // display full name with titles
			}
		},tableFieldUpdater);

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new Comparator<User>() {
			public int compare(User o1, User o2) {
				return o1.getFullName().compareToIgnoreCase(o2.getFullName());  // sort by name without titles
			}
		});

		table.addColumn(nameColumn, "Name");

		return table;

	}

	/**
	 * Users search
	 * @param query
	 */
	public void searchFor(String query){
		this.searchString = query;
		clearTable();
		retrieveData();
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData()
	{
		if(this.searchString==null || this.searchString.isEmpty()) return;
		loaderImage.loadingStart();
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "searchString=" + this.searchString+"&vo="+vo, this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
        list = new TableSorter<User>().sortByName(getList());
        dataProvider.flush();
        dataProvider.refresh();
	}

    /**
     * Add object as new row to table
     *
     * @param object user to be added as new row
     */
    public void addToTable(User object) {
        list.add(object);
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Removes object as row from table
     *
     * @param object user to be removed as row
     */
    public void removeFromTable(User object) {
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
	public ArrayList<User> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while searching for: " + searchString);
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		events.onLoadingStart();
	}

    /**
     * Called, when operation finishes successfully.
     */
    public void onFinished(JavaScriptObject jso) {
        setList(JsonUtils.<User>jsoAsList(jso));
        sortTable();
        session.getUiElements().setLogText("Users loaded: " + list.size());
        events.onFinished(jso);
        loaderImage.loadingFinished();
    }

	public void insertToTable(int index, User object) {
        list.add(index, object);
        dataProvider.flush();
        dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable= checkable; 
	}

    public void setList(ArrayList<User> list) {
        clearTable();
        this.list.addAll(list);
        dataProvider.flush();
        dataProvider.refresh();
    }

    public ArrayList<User> getList() {
        return this.list;
    }

	public void setSelected(User user) {
		selectionModel.setSelected(user, true);
	}

	public void setEvents(JsonCallbackEvents event) {
		this.events = event;
	}

	public void setSearchString(String text) {
		this.searchString = text;
	}
	
}