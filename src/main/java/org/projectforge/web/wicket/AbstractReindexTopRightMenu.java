/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.wicket;

import java.io.Serializable;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

/**
 * Helper for creating re-index menu items in the top right drop down menu.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class AbstractReindexTopRightMenu implements Serializable
{
  private static final long serialVersionUID = 2959661327690147049L;

  protected abstract void rebuildDatabaseIndex(boolean onlyNewest);

  protected abstract String getString(final String i18nKey);

  @SuppressWarnings("serial")
  protected AbstractReindexTopRightMenu(final AbstractSecuredPage page, final boolean enableFullReindex)
  {
    WebMarkupContainer item = new WebMarkupContainer(page.getNewDropDownMenuChildId());
    page.addDropDownMenuEntry(item);
    Link<String> link = new Link<String>("menuEntry") {
      @Override
      public void onClick()
      {
        rebuildDatabaseIndex(true);
      }
    };
    WicketUtils.addTooltip(link, getString("menu.reindexNewestDatabaseEntries.tooltip"));
    link.add(new Label("label", getString("menu.reindexNewestDatabaseEntries")).setRenderBodyOnly(true));
    item.add(link);
    if (enableFullReindex == true) {
      item = new WebMarkupContainer(page.getNewDropDownMenuChildId());
      page.addDropDownMenuEntry(item);
      link = new Link<String>("menuEntry") {
        @Override
        public void onClick()
        {
          rebuildDatabaseIndex(false);
        }
      };
      WicketUtils.addTooltip(link, getString("menu.reindexAllDatabaseEntries.tooltip"));
      link.add(new Label("label", getString("menu.reindexAllDatabaseEntries")).setRenderBodyOnly(true));
      item.add(link);
    }
  }
}
