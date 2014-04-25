/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.fibu;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.RecentQueue;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.ProjektFormatter;
import org.projectforge.web.user.UserPreferencesHelper;
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel;

/**
 * This panel shows the actual customer.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class NewProjektSelectPanel extends AbstractSelectPanel<ProjektDO> implements ComponentWrapperPanel
{

  private static final long serialVersionUID = -7461448790487855518L;

  private static final String USER_PREF_KEY_RECENT_PROJECTS = "ProjectSelectPanel:recentProjects";

  private boolean defaultFormProcessing = false;

  @SpringBean(name = "projektFormatter")
  private ProjektFormatter projektFormatter;

  @SpringBean(name = "projektDao")
  private ProjektDao projektDao;

  private RecentQueue<String> recentProjects;

  private final PFAutoCompleteTextField<ProjektDO> projectTextField;

  // Only used for detecting changes:
  private ProjektDO currentProject;

  /**
   * @param id
   * @param model
   * @param caller
   * @param selectProperty
   */
  public NewProjektSelectPanel(final String id, final IModel<ProjektDO> model, final ISelectCallerPage caller, final String selectProperty)
  {
    this(id, model, null, caller, selectProperty);
  }

  /**
   * @param id
   * @param model
   * @param caller
   * @param selectProperty
   */
  @SuppressWarnings("serial")
  public NewProjektSelectPanel(final String id, final IModel<ProjektDO> model, final String label, final ISelectCallerPage caller,
      final String selectProperty)
  {
    super(id, model, caller, selectProperty);
    projectTextField = new PFAutoCompleteTextField<ProjektDO>("projectField", getModel()) {
      @Override
      protected List<ProjektDO> getChoices(final String input)
      {
        final BaseSearchFilter filter = new BaseSearchFilter();
        filter.setSearchFields("id", "name", "identifier", "nummer");
        filter.setSearchString(input);
        final List<ProjektDO> list = projektDao.getList(filter);
        return list;
      }

      @Override
      protected List<String> getRecentUserInputs()
      {
        return getRecentProjects().getRecents();
      }

      @Override
      protected String formatLabel(final ProjektDO project)
      {
        if (project == null) {
          return "";
        }
        return projektFormatter.format(project, false);
      }

      @Override
      protected String formatValue(final ProjektDO project)
      {
        if (project == null) {
          return "";
        }
        return projektFormatter.format(project, false);
      }

      @Override
      protected void convertInput()
      {
        final ProjektDO project = getConverter(getType()).convertToObject(getInput(), getLocale());
        setConvertedInput(project);
        if (project != null && (currentProject == null || project.getId() != currentProject.getId())) {
          getRecentProjects().append(projektFormatter.format(project, false));
        }
        currentProject = project;
      }

      /**
       * @see org.apache.wicket.Component#getConverter(java.lang.Class)
       */

      @SuppressWarnings({ "unchecked", "rawtypes"})
      @Override
      public <C> IConverter<C>  getConverter(final Class<C> type)
      {
        return new IConverter() {
          @Override
          public Object convertToObject(final String value, final Locale locale)
          {
            if (StringUtils.isEmpty(value) == true) {
              getModel().setObject(null);
              return null;
            }
            final int ind = value.indexOf(": ");
            final String projectname = ind >= 0 ? value.substring(ind + 2, value.length()) : value;
            final ProjektDO project = null; //projektDao.getById(projectname);
            if (project == null) {
              error(getString("panel.error.projectNotFound"));
            }
            getModel().setObject(project);
            return project;
          }

          @Override
          public String convertToString(final Object value, final Locale locale)
          {
            if (value == null) {
              return "";
            }
            final ProjektDO project = (ProjektDO) value;
            return formatLabel(project);
          }

        };
      }
    };
    currentProject = getModelObject();
    projectTextField.enableTooltips().withLabelValue(true).withMatchContains(true).withMinChars(2).withAutoSubmit(false); //.withWidth(400);
  }

  /**
   * Should be called before init() method. If true, then the validation will be done after submitting.
   * @param defaultFormProcessing
   */
  public void setDefaultFormProcessing(final boolean defaultFormProcessing)
  {
    this.defaultFormProcessing = defaultFormProcessing;
  }

  @Override
  public NewProjektSelectPanel init()
  {
    super.init();
    add(projectTextField);
    return this;
  }

  public NewProjektSelectPanel withAutoSubmit(final boolean autoSubmit)
  {
    projectTextField.withAutoSubmit(autoSubmit);
    return this;
  }

  @Override
  public Component getWrappedComponent()
  {
    return projectTextField;
  }

  @Override
  protected void convertInput()
  {
    setConvertedInput(getModelObject());
  }

  @SuppressWarnings("unchecked")
  private RecentQueue<String> getRecentProjects()
  {
    if (this.recentProjects == null) {
      this.recentProjects = (RecentQueue<String>) UserPreferencesHelper.getEntry(USER_PREF_KEY_RECENT_PROJECTS);
    }
    if (this.recentProjects == null) {
      this.recentProjects = new RecentQueue<String>();
      UserPreferencesHelper.putEntry(USER_PREF_KEY_RECENT_PROJECTS, this.recentProjects, true);
    }
    return this.recentProjects;
  }

  private String formatCustomer(final ProjektDO customer)
  {
    if (customer == null) {
      return "";
    }
    return projektFormatter.format(customer, false);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel#getComponentOutputId()
   */
  @Override
  public String getComponentOutputId()
  {
    projectTextField.setOutputMarkupId(true);
    return projectTextField.getMarkupId();
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel#getFormComponent()
   */
  @Override
  public FormComponent< ? > getFormComponent()
  {
    return projectTextField;
  }

  /**
   * @return
   */
  public String getProjectTextInput()
  {
    if (projectTextField != null) {
      return projectTextField.getRawInput();
    }
    return null;
  }
}