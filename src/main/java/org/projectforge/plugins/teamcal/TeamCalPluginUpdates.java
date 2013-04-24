/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.admin.UpdateEntry;
import org.projectforge.admin.UpdateEntryImpl;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateRunningStatus;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.core.ConfigurationDao;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.Table;
import org.projectforge.database.TableAttribute;
import org.projectforge.database.TableAttributeType;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.event.TeamEventAttendeeDO;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.registry.Registry;
import org.projectforge.web.admin.SystemUpdatePage;
import org.projectforge.web.core.ConfigurationEditPage;
import org.projectforge.web.wicket.AbstractEditPage;

/**
 * Contains the initial data-base set-up script and later all update scripts if any data-base schema updates are required by any later
 * release of this to-do plugin.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TeamCalPluginUpdates
{
  static DatabaseUpdateDao dao;

  @SuppressWarnings("serial")
  public static UpdateEntry getInitializationUpdateEntry()
  {
    return new UpdateEntryImpl(TeamCalPlugin.ID, "1.0.0", "2013-04-03", "Adds tables T_PLUGIN_CALENDAR_* and parameter CALENDAR_DOMAIN.") {

      final Table calendarTable = new Table(TeamCalDO.class);

      final Table eventTable = new Table(TeamEventDO.class);

      final Table attendeeTable = new Table(TeamEventAttendeeDO.class);

      final String[] calendarAttributes = { "owner", "fullAccessGroupIds", "fullAccessUserIds", "readonlyAccessGroupIds",
          "readonlyAccessUserIds", "minimalAccessGroupIds", "minimalAccessUserIds", "description", "title", "abo", "aboCalendarBinary", "aboHash", "aboUrl"};

      final String[] eventAttributes = { "subject", "location", "allDay", "calendar", "startDate", "endDate", "note", "organizer",
          "recurrenceRule", "recurrenceExDate", "recurrenceDate", "recurrenceReferenceId", "recurrenceUntil", "externalUid",
          "reminderDuration", "reminderDurationUnit", "reminderActionType"};

      final String[] attendeeAttributes = { "id", "url", "userId", "loginToken", "status", "comment"};

      {
        calendarTable.addDefaultBaseDOAttributes().addAttributes(calendarAttributes);
        eventTable.addDefaultBaseDOAttributes().addAttributes(eventAttributes);
        attendeeTable.addAttributes(attendeeAttributes);
      }

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        // Does the data-base table already exist?
        if (dao.doesExist(calendarTable, eventTable, attendeeTable) == true //
            && dao.doesTableAttributesExist(calendarTable, calendarAttributes) == true //
            && dao.doesTableAttributesExist(eventTable, eventAttributes) == true //
            && dao.doesTableAttributesExist(attendeeTable, attendeeAttributes) == true //
            && dao.doesTableAttributeExist(attendeeTable.getName(), "team_event_fk") == true //
            && Configuration.getInstance().isCalendarDomainValid() == true) {
          return UpdatePreCheckStatus.ALREADY_UPDATED;
        } else {
          return UpdatePreCheckStatus.OK;
        }
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        // Create initial data-base table:
        if (dao.doesExist(calendarTable) == false) {
          dao.createTable(calendarTable);
        } else if (dao.doesTableAttributesExist(calendarTable, calendarAttributes) == false) {
          dao.addTableAttributes(calendarTable, calendarTable.getAttributes());
        }
        if (dao.doesExist(eventTable) == false) {
          dao.createTable(eventTable);
        }
        if (dao.doesTableAttributesExist(eventTable, eventAttributes) == false) {
          dao.addTableAttributes(eventTable, eventTable.getAttributes());
        }
        if (dao.doesExist(attendeeTable) == false) {
          dao.createTable(attendeeTable);
        }
        if (dao.doesTableAttributesExist(attendeeTable, attendeeAttributes) == false) {
          dao.addTableAttributes(attendeeTable, attendeeTable.getAttributes());
        }
        if (dao.doesTableAttributeExist(attendeeTable.getName(), "team_event_fk") == false) {
          final TableAttribute attr = new TableAttribute("team_event_fk", TableAttributeType.INT).setForeignTable(TeamEventDO.class)
              .setForeignAttribute("pk");
          dao.addTableAttributes(attendeeTable, attr);
        }
        dao.createMissingIndices();

        return UpdateRunningStatus.DONE;
      }

      /**
       * @see org.projectforge.admin.UpdateEntry#afterUpdate()
       */
      @Override
      public void afterUpdate()
      {
        if (Configuration.getInstance().isCalendarDomainValid() == false) {
          // Force to edit configuration value 'calendar domain'.
          final ConfigurationDao configurationDao = Registry.instance().getDao(ConfigurationDao.class);
          configurationDao.checkAndUpdateDatabaseEntries();
          final ConfigurationDO configurationDO = configurationDao.getEntry(ConfigurationParam.CALENDAR_DOMAIN);
          final ConfigurationEditPage configurationEditPage = new ConfigurationEditPage(new PageParameters().add(AbstractEditPage.PARAMETER_KEY_ID, configurationDO.getId()));
          configurationEditPage.setReturnToPage(new SystemUpdatePage(new PageParameters()));
          throw new RestartResponseException(configurationEditPage);
        }
      }
    };
  }
}
