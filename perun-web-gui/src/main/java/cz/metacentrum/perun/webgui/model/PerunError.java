package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for PerunException object from Perun
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class PerunError extends JavaScriptObject {

	protected PerunError() { }

	public final native String getErrorId() /*-{
		if (typeof this.errorId === 'undefined' || !this.errorId) {
			return "";
		}
		return this.errorId;
	}-*/;

	public final native void setErrorId(String id) /*-{
		this.errorId = id;
	}-*/;

    /**
     * Return name of exception (this.getClass().getSimpleName())
     * which is equivalent to "beanName" in Perun Beans
     *
     * @return name of exception or empty string
     */
	public final native String getName() /*-{
		if (!this.name) {
            return "";
        }
    	return this.name;
	}-*/;

    /**
     * Return TYPE of exception (e.g. for CabinetException)
     *
     * @return type of exception or empty string
     */
    public final native String getType() /*-{
        if (!this.type) {
            return "";
        }
        return this.type;
    }-*/;

    /**
     * Return reason for "ExtendMembershipException"
     * @return reason why can't extend membership
     */
    public final native String getReason() /*-{
		if (!this.reason) {
			return "";
		}
    	return this.reason;
	}-*/;
	
	public final native void setType(String type) /*-{
		this.type = type;
	}-*/;
	
	public final native String getErrorInfo() /*-{
        if (typeof this.message === 'undefined' || !this.message) {
            return "";
        }
		return this.message;
    }-*/;
	
	public final native void setErrorInfo(String info) /*-{
		this.message = info;
	}-*/;

    /**
     * Get attribute object related to error message
     *
     * @return attribute object or null if not present
     */
    public final native Attribute getAttribute() /*-{
        if (!this.attribute) {
            return null;
        }
        return this.attribute;
    }-*/;

    /**
     * Get referenced attribute object related to error message
     *
     * @return attribute object or null if not present
     */
    public final native Attribute getReferenceAttribute() /*-{
        if (!this.referenceAttribute) {
            return null;
        }
        return this.referenceAttribute;
    }-*/;

    /**
     * Get referenced VO related to error message
     *
     * @return Vo object or null if not present
     */
    public final native VirtualOrganization getVo() /*-{
        if (!this.vo) {
            return null;
        }
        return this.vo;
    }-*/;

    /**
     * Get referenced Facility related to error message
     *
     * @return Facility object or null if not present
     */
    public final native Facility getFacility() /*-{
        if (!this.facility) {
            return null;
        }
        return this.facility;
    }-*/;

    /**
     * Get referenced Group related to error message
     *
     * @return Group object or null if not present
     */
    public final native Group getGroup() /*-{
        if (!this.group) {
            return null;
        }
        return this.group;
    }-*/;

    /**
     * Get referenced User  related to error message
     *
     * @return User object or null if not present
     */
    public final native User getUser() /*-{
        if (!this.user) {
            return null;
        }
        return this.user;
    }-*/;

    /**
     * Get holder of attribute related to error message
     *
     * @return GeneralObject object or null if not present
     */
    public final native GeneralObject getAttributeHolder() /*-{
        if (!this.attributeHolder) {
            return null;
        }
        return this.attributeHolder;
    }-*/;

    /**
     * Get secondary holder of attribute related to error message
     *
     * @return GeneralObject object or null if not present
     */
    public final native GeneralObject getAttributeHolderSecondary() /*-{
        if (!this.attributeHolderSecondary) {
            return null;
        }
        return this.attributeHolderSecondary;
    }-*/;

    /**
     * Get referenced Member related to error message
     *
     * @return Member object or null if not present
     */
    public final native Member getMember() /*-{
        if (!this.member) {
            return null;
        }
        return this.member;
    }-*/;

    /**
     * Get referenced login related to error message
     *
     * @return login string or null
     */
    public final native String getLogin() /*-{
        if (!this.login) {
            return null;
        }
        return this.login;
    }-*/;

    /**
     * Get referenced login-namespace related to error message
     *
     * @return login namespace string or null
     */
    public final native String getNamespace() /*-{
        if (!this.namespace) {
            return null;
        }
        return this.namespace;
    }-*/;

    /**
     * Get referenced destination related to error message
     *
     * @return destination object or null
     */
    public final native Destination getDestination() /*-{
        if (!this.destination) {
            return null;
        }
        return this.destination;
    }-*/;

    /**
     * Get referenced external source related to error message
     *
     * @return external source or null
     */
    public final native ExtSource getExtSource() /*-{
        if (!this.extSource) {
            return null;
        }
        return this.extSource;
    }-*/;

    /**
     * Get referenced service related to error message
     *
     * @return service or null
     */
    public final native Service getService() /*-{
        if (!this.service) {
            return null;
        }
        return this.service;
    }-*/;


    /**
	 * Returns Perun specific type of object
	 * 
	 * @return type of object
	 */
	public final String getObjectType() {
        if ("".equalsIgnoreCase(getErrorId()) && "".equalsIgnoreCase(getErrorInfo())) {
            return getNativeObjectType();
        } else {
            return "PerunError";
        }
    }

    /**
     * Get native type
     */
    public final native String getNativeObjectType() /*-{
        if (!this.beanName) {
            return "JavaScriptObject";
        } else {
            return this.beanName;
        }
    }-*/;
	
	/**
	 * Sets Perun specific type of object
	 * 
	 * @param type type of object
	 */
	public final native void setObjectType(String type) /*-{
		this.beanName = type;
	}-*/;
	
	/**
	 * Returns the status of this item in Perun system as String
	 * VALID, INVALID, SUSPENDED, EXPIRED, DISABLED
	 * 
	 * @return string which defines item status
	 */
	public final native String getStatus() /*-{
		return this.status;
	}-*/;
	
	/**
	 * Compares to another object
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(PerunError o)
	{
		return o.getErrorId() == this.getErrorId();		
	}
	
}