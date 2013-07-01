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
package org.squashtest.tm.web.internal.controller.audittrail;

import java.lang.reflect.Field;
import java.util.Locale;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.context.MessageSource;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.event.RequirementAuditEventVisitor;
import org.squashtest.tm.domain.event.RequirementCreation;
import org.squashtest.tm.domain.event.RequirementLargePropertyChange;
import org.squashtest.tm.domain.event.RequirementPropertyChange;
import org.squashtest.tm.domain.event.RequirementVersionModification;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

/**
 * Builder for datatable model showing {@link RequirementAuditEvent} objects. Not threadsafe, should be discarded after
 * use.
 * 
 * @author Gregory Fouquet
 * 
 */
public class RequirementAuditEventTableModelBuilder extends DataTableModelBuilder<RequirementAuditEvent> implements
		RequirementAuditEventVisitor {
	/**
	 * The locale to use to format the labels.
	 */
	private final Locale locale;
	/**
	 * The source for localized label messages.
	 */
	private final InternationalizationHelper i18nHelper;

	/**
	 * Data for the item currently build.
	 */
	private Object[] currentItemData;

	/**
	 * @param locale
	 */
	public RequirementAuditEventTableModelBuilder(@NotNull Locale locale, @NotNull InternationalizationHelper messageSource) {
		super();
		this.i18nHelper = messageSource;
		this.locale = locale;
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder#buildItemData(java.lang.Object)
	 */
	@Override
	protected Object[] buildItemData(RequirementAuditEvent item) {
		item.accept(this);

		return currentItemData;
	}

	/**
	 * @see org.squashtest.tm.tm.domain.event.RequirementAuditEventVisitor#visit(org.squashtest.tm.tm.domain.event.RequirementCreation)
	 */
	@Override
	public void visit(RequirementCreation event) {
		String message = i18nHelper.getMessage("label.Creation", null, locale);
		populateCurrentItemData(message, "creation", event);

	}

	/**
	 * @see org.squashtest.tm.tm.domain.event.RequirementAuditEventVisitor#visit(org.squashtest.tm.tm.domain.event.RequirementPropertyChange)
	 */
	@Override
	public void visit(RequirementPropertyChange event) {
		Object[] args = buildMessageArgs(event);

		String message = i18nHelper.getMessage(buildPropertyChangeMessageKey(event), args, locale);
		populateCurrentItemData(message, "simple-prop", event);
	}

	private String buildPropertyChangeMessageKey(RequirementVersionModification event) {
		return "message.property-change." + event.getPropertyName() + ".label";
	}

	private Object[] buildMessageArgs(RequirementPropertyChange event) {
		if (propertyIsEnumeratedAndInternationalizable(event)) {
			return buildMessageArgsForI18nableEnumProperty(event);
		}

		return buildMessageArgsForStringProperty(event);
	}

	private boolean propertyIsEnumeratedAndInternationalizable(RequirementPropertyChange event) {
		Field field = ReflectionUtils.findField(RequirementVersion.class, event.getPropertyName());
		Class<?> fieldType = field.getType();

		return Enum.class.isAssignableFrom(fieldType) && Internationalizable.class.isAssignableFrom(fieldType);
	}

	private Object[] buildMessageArgsForStringProperty(RequirementPropertyChange event) {
		return new Object[] { event.getOldValue(), event.getNewValue() };
	}

	private Object[] buildMessageArgsForI18nableEnumProperty(RequirementPropertyChange event) {
		Field enumField = ReflectionUtils.findField(RequirementVersion.class, event.getPropertyName());
		Class<?> enumType = enumField.getType();

		String oldValueLabel = retrieveEnumI18ndLabel(enumType, event.getOldValue());
		String newValueLabel = retrieveEnumI18ndLabel(enumType, event.getNewValue());

		return new Object[] { oldValueLabel, newValueLabel };
	}

	@SuppressWarnings("rawtypes")
	private String retrieveEnumI18ndLabel(Class enumType, String stringValue) {
		Internationalizable enumValue = Enum.valueOf(enumType, stringValue);
		return i18nHelper.getMessage(enumValue.getI18nKey(), null, locale);
	}

	/**
	 * @see org.squashtest.tm.tm.domain.event.RequirementAuditEventVisitor#visit(org.squashtest.tm.tm.domain.event.RequirementLargePropertyChange)
	 */
	@Override
	public void visit(RequirementLargePropertyChange event) {
		String message = i18nHelper.getMessage(buildPropertyChangeMessageKey(event), null, locale);
		populateCurrentItemData(message, "fat-prop", event);

	}

	private void populateCurrentItemData(String message, String eventType, RequirementAuditEvent event) {
		String formattedDate = DateFormatUtils.format(event.getDate(), "dd/MM/yyyy HH'h'mm");
		String escapedAuthor = HtmlUtils.htmlEscape(event.getAuthor());
		String escapedMessage = HtmlUtils.htmlEscape(message);

		currentItemData = new Object[] { formattedDate, escapedAuthor, escapedMessage, eventType, String.valueOf(event.getId()) };
	}
}
