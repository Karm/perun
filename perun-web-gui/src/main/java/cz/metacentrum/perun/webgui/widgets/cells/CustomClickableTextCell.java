package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Custom GWT cell, which is clickable and looks like an anchor.
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class CustomClickableTextCell extends ClickableTextCell
{
	private String style;   

	/**
	 * Creates a new Clickable text cell
	 */
	public CustomClickableTextCell()
	{
		super();
		style = "customClickableTextCell";
	}

	/**
	 * Renders the widget.
	 */
	@Override
	protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
		if (value != null) {
			sb.appendHtmlConstant("<div class=\"" + style + "\">");
			// sb.append(value);
			sb.appendEscapedLines(value.asString()); // for linebreaks (\n => </br>) in values 
			sb.appendHtmlConstant("</div>");
		}
	}
	
	/**
	 * Adds a class to the style
	 * @param style
	 */
	public void addStyleName(String style)
	{
		this.style += " " + style; 
	}


}