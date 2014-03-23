/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

/**
 * The controller of the list page. Most functionality such as search etc. is done by the super class.
 * @author Werner Feder (werner.feder@t-online.de)
 */
@ListPage(editPage = TrainingEditPage.class)
public class TrainingListPage extends AbstractListPage<TrainingListForm, TrainingDao, TrainingDO> implements
IListPageColumnsCreator<TrainingDO>
{

  private static final long serialVersionUID = -6016271633747836684L;

  public static final String I18N_KEY_PREFIX = "plugins.skillmatrix";

  @SpringBean(name = "trainingDao")
  private TrainingDao trainingDao;

  public TrainingListPage(final PageParameters parameters)
  {
    super(parameters, I18N_KEY_PREFIX);
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  @SuppressWarnings("serial")
  @Override
  public List<IColumn<TrainingDO, String>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<TrainingDO, String>> columns = new ArrayList<IColumn<TrainingDO, String>>();
    final CellItemListener<TrainingDO> cellItemListener = new CellItemListener<TrainingDO>() {
      public void populateItem(final Item<ICellPopulator<TrainingDO>> item, final String componentId, final IModel<TrainingDO> rowModel)
      {
        final TrainingDO trainingDO = rowModel.getObject();
        appendCssClasses(item, trainingDO.getId(), trainingDO.isDeleted());
      }
    };

    columns.add(new CellItemListenerPropertyColumn<TrainingDO>(getString("plugins.skillmatrix.skillrating.skill"), getSortable(
        "skill.title", sortable), "skill.title", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TrainingDO>(getString("plugins.skillmatrix.skilltraining.training"), getSortable("title",
        sortable), "title", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TrainingDO>(TrainingDO.class, getSortable("description", sortable), "description",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TrainingDO>(TrainingDO.class, getSortable("startDate", sortable), "startDate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TrainingDO>(TrainingDO.class, getSortable("endDate", sortable), "endDate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TrainingDO>(TrainingDO.class, getSortable("rating", sortable), "rating",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TrainingDO>(TrainingDO.class, getSortable("certificate", sortable), "certificate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TrainingDO>(TrainingDO.class, getSortable("created", sortable), "created",
        cellItemListener) {
      @SuppressWarnings({ "unchecked", "rawtypes"})
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final TrainingDO trainingDo = (TrainingDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, TrainingEditPage.class, trainingDo.getId(), returnToPage,
            DateTimeFormatter.instance().getFormattedDateTime(trainingDo.getCreated())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<TrainingDO>(TrainingDO.class, getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));

    return columns;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#init()
   */
  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "lastUpdate", SortOrder.DESCENDING);
    form.add(dataTable);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#getBaseDao()
   */
  @Override
  protected TrainingDao getBaseDao()
  {
    return trainingDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#newListForm(org.projectforge.web.wicket.AbstractListPage)
   */
  @Override
  protected TrainingListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new TrainingListForm(this);
  }

  /*
   * @see org.projectforge.web.wicket.AbstractListPage#select(java.lang.String, java.lang.Object)
   */
  @Override
  public void select(final String property, final Object selectedValue)
  {
    if ("skillId".equals(property) == true) {
      form.getSearchFilter().setSkillId((Integer) selectedValue);
      refresh();
    } else if ("trainingId".equals(property) == true) {
      form.getSearchFilter().setTrainingId((Integer) selectedValue);
      refresh();
    } else
      super.select(property, selectedValue);
  }

  /**
   * 
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  @Override
  public void unselect(final String property)
  {
    if ("skillId".equals(property) == true) {
      form.getSearchFilter().setSkillId(null);
      refresh();
    } else if ("trainingId".equals(property) == true) {
      form.getSearchFilter().setTrainingId(null);
      refresh();
    }
    super.unselect(property);
  }

}
