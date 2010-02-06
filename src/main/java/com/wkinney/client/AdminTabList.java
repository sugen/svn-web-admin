/**
 * 
 */
package com.wkinney.client;

import java.util.ArrayList;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.wkinney.client.AdminTab.AdminTabInfo;

/**
 * @author wkinney
 *
 */
public class AdminTabList extends Composite {

    private class MouseLink extends Hyperlink {

      private int index;

      public MouseLink(String name, int index) {
        super(name, name);
        this.index = index;
        sinkEvents(Event.MOUSEEVENTS);
      }

      public void onBrowserEvent(Event event) {
        switch (DOM.eventGetType(event)) {
          case Event.ONMOUSEOVER:
            mouseOver(index);
            break;

          case Event.ONMOUSEOUT:
            mouseOut(index);
            break;
        }

        super.onBrowserEvent(event);
      }
    }

    private HorizontalPanel list = new HorizontalPanel();
    private ArrayList adminTabs = new ArrayList();

    private int selectedAdminTab = -1;

    public AdminTabList(AdminTab.Images images) {
      initWidget(list);
      list.add(images.svnAdminLogo().createImage());
      setStyleName("ks-List");
    }

    public void addSink(final AdminTabInfo info) {
      String name = info.getName();
      int index = list.getWidgetCount() - 1;

      MouseLink link = new MouseLink(name, index);
      list.add(link);
      adminTabs.add(info);

      list.setCellVerticalAlignment(link, HorizontalPanel.ALIGN_BOTTOM);
      styleSink(index, false);
    }

    public AdminTabInfo find(String sinkName) {
      for (int i = 0; i < adminTabs.size(); ++i) {
          AdminTabInfo info = (AdminTabInfo) adminTabs.get(i);
        if (info.getName().equals(sinkName)) {
          return info;
        }
      }

      return null;
    }

    public void setSinkSelection(String name) {
      if (selectedAdminTab != -1) {
        styleSink(selectedAdminTab, false);
      }

      for (int i = 0; i < adminTabs.size(); ++i) {
        AdminTabInfo info = (AdminTabInfo) adminTabs.get(i);
        if (info.getName().equals(name)) {
          selectedAdminTab = i;
          styleSink(selectedAdminTab, true);
          return;
        }
      }
    }

    private void colorSink(int index, boolean on) {
      String color = "";
      if (on) {
        color = ((AdminTabInfo) adminTabs.get(index)).getColor();
      }

      Widget w = list.getWidget(index + 1);
      DOM.setStyleAttribute(w.getElement(), "backgroundColor", color);
    }

    private void mouseOut(int index) {
      if (index != selectedAdminTab) {
        colorSink(index, false);
      }
    }

    private void mouseOver(int index) {
      if (index != selectedAdminTab) {
        colorSink(index, true);
      }
    }

    private void styleSink(int index, boolean selected) {
      String style = (index == 0) ? "ks-FirstSinkItem" : "ks-SinkItem";
      if (selected) {
        style += "-selected";
      }

      Widget w = list.getWidget(index + 1);
      w.setStyleName(style);

      colorSink(index, selected);
    }
  }
