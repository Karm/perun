package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.json.registrarManager.CopyMails;
import cz.metacentrum.perun.webgui.json.vosManager.GetVos;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Tab which allow you to copy application mails from VO to VO or from Group to Group
 * !!! USE AS INNER TAB ONLY !!!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class CopyMailsTabItem implements TabItem {

    /**
     * Perun web session
     */
    private PerunWebSession session = PerunWebSession.getInstance();

    /**
     * Content widget - should be simple panel
     */
    private SimplePanel contentWidget = new SimplePanel();

    /**
     * Title widget
     */
    private Label titleWidget = new Label("Copy mails");

    /**
     * Entity ID to set
     */
    private int voId = 0;
    private int groupId = 0;

    /**
     * Creates a tab instance
     *
     * @param voId
     * @param groupId
     */
    public CopyMailsTabItem(int voId, int groupId){
        this.voId = voId;
        this.groupId = groupId;
    }

    public boolean isPrepared(){
        return true;
    }

    public Widget draw() {

        final FlexTable content = new FlexTable();
        content.setStyleName("inputFormFlexTable");

        // boxes
        final ListBoxWithObjects<VirtualOrganization> vosBox = new ListBoxWithObjects<VirtualOrganization>();
        final ListBoxWithObjects<Group> groupsBox = new ListBoxWithObjects<Group>();

        final CustomButton save;

        final TabItem tab = this;

        VerticalPanel vp = new VerticalPanel();
        TabMenu menu = new TabMenu();

        if (groupId == 0) {

            titleWidget.setText("Copy mails from VO");

            save = TabMenu.getPredefinedButton(ButtonType.OK, ButtonTranslation.INSTANCE.copyMailsFromVo());

            // get them
            GetVos vos = new GetVos(new JsonCallbackEvents(){
                @Override
                public void onFinished(JavaScriptObject jso) {
                    vosBox.clear();
                    ArrayList<VirtualOrganization> vos = JsonUtils.jsoAsList(jso);
                    vos = new TableSorter<VirtualOrganization>().sortByName(vos);
                    vosBox.addAllItems(vos);
                    if (vosBox.getAllObjects().size() > 0) {
                        save.setEnabled(true);
                    }
                }
                @Override
                public void onError(PerunError error) {
                    vosBox.addItem("Error while loading");
                    save.setEnabled(false);
                }
                @Override
                public void onLoadingStart(){
                    vosBox.addItem("Loading...");
                    save.setEnabled(false);
                }
            });
            vos.retrieveData();
            content.setHTML(0, 0, "Source VO:");
            content.getFlexCellFormatter().setStyleName(0, 0, "itemName");
            content.setWidget(0, 1, vosBox);

            save.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    CopyMails request = new CopyMails(PerunEntity.VIRTUAL_ORGANIZATION, vosBox.getSelectedObject().getId(), voId, JsonCallbackEvents.closeTabDisableButtonEvents(save, tab));
                    request.copyMails();
                }
            });

            menu.addWidget(save);

        } else {

            titleWidget.setText("Copy mails from group");

            save = TabMenu.getPredefinedButton(ButtonType.OK, ButtonTranslation.INSTANCE.copyMailsFromGroup());

            // get them
            GetAllGroups getGroups = new GetAllGroups(voId, new JsonCallbackEvents(){
                @Override
                public void onFinished(JavaScriptObject jso) {
                    groupsBox.clear();
                    ArrayList<Group> groups = JsonUtils.jsoAsList(jso);
                    groups = new TableSorter<Group>().sortByName(groups);
                    groupsBox.addAllItems(groups);
                    if (groupsBox.getAllObjects().size() > 0) {
                        save.setEnabled(true);
                    }
                }
                @Override
                public void onError(PerunError error) {
                    groupsBox.addItem("Error while loading");
                    save.setEnabled(false);
                }
                @Override
                public void onLoadingStart(){
                    groupsBox.addItem("Loading...");
                    save.setEnabled(false);
                }
            });
            getGroups.retrieveData();
            content.setHTML(0, 0, "Source group:");
            content.getFlexCellFormatter().setStyleName(0, 0, "itemName");
            content.setWidget(0, 1, groupsBox);

            save.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    CopyMails request = new CopyMails(PerunEntity.GROUP, groupsBox.getSelectedObject().getId(), groupId, JsonCallbackEvents.closeTabDisableButtonEvents(save, tab));
                    request.copyMails();
                }
            });

            menu.addWidget(save);

        }

        content.setHTML(1, 0, "All mail definitions will be added to yours.");
        content.getFlexCellFormatter().setStyleName(1, 0, "inputFormInlineComment");
        content.getFlexCellFormatter().setColSpan(1, 0, 2);

        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

        vp.add(content);
        vp.add(menu);
        vp.setCellHeight(menu, "30px");
        vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

        this.contentWidget.setWidget(vp);

        return getWidget();
    }

    public Widget getWidget() {
        return this.contentWidget;
    }

    public Widget getTitle() {
        return this.titleWidget;
    }

    public ImageResource getIcon() {
        return SmallIcons.INSTANCE.addIcon();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 6786786;
        return result;
    }

    /**
     * @param obj
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CopyMailsTabItem create = (CopyMailsTabItem) obj;
        if (voId != create.voId){
            return false;
        }

        return true;
    }

    public boolean multipleInstancesEnabled() {
        return false;
    }

    public void open()
    {
        // no open for inner tab
    }

    public boolean isAuthorized() {

        if (session.isVoAdmin(voId)) {
            return true;
        } else {
            return false;
        }
    }

}