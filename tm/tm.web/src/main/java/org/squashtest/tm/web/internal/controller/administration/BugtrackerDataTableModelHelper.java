/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.administration;

import java.util.Locale;

import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

public class BugtrackerDataTableModelHelper extends DataTableModelBuilder<BugTracker> {

	private InternationalizationHelper messageSource;

	private Locale locale;

	public BugtrackerDataTableModelHelper(InternationalizationHelper messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public Object[] buildItemData(BugTracker item) {

		String isIframeFriendlyStringValue = "";

		if (item.isIframeFriendly()) {
			isIframeFriendlyStringValue = messageSource.internationalize("squashtm.truefalse.true", locale);
		} else {
			isIframeFriendlyStringValue = messageSource.internationalize("squashtm.truefalse.false", locale);
		}

		return new Object[] { item.getId(), getCurrentIndex(), item.getName(), item.getKind(), item.getUrl(),
				isIframeFriendlyStringValue };
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public InternationalizationHelper getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(InternationalizationHelper messageSource) {
		this.messageSource = messageSource;
	}
}
