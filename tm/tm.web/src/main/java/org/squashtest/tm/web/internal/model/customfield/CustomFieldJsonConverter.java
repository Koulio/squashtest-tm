/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.web.internal.model.customfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.CustomFieldValueOption;
import org.squashtest.tm.domain.customfield.MultiValuedCustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.customfield.TagsValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedMultiSelectField;

@Component
public class CustomFieldJsonConverter {

	@Inject
	private MessageSource messageSource;

	@Inject
	private CustomFieldModelFactory customFieldFactory;

	public CustomFieldJsonConverter() {
		super();
	}

	public CustomFieldJsonConverter(MessageSource messageSource) {
		super();
		this.messageSource = messageSource;
	}


	// ************ simple  jsonifications ********************

	public RenderingLocationModel toJson(RenderingLocation location) {

		RenderingLocationModel model = new RenderingLocationModel();

		model.setEnumName(location.toString());
		model.setFriendlyName(getMessage(location.getI18nKey()));

		return model;

	}

	public RenderingLocationModel[] toJson(Collection<RenderingLocation> values) {
		RenderingLocationModel[] modelArray = new RenderingLocationModel[values.size()];
		int i = 0;
		for (RenderingLocation location : values) {
			modelArray[i++] = toJson(location);
		}
		return modelArray;
	}


	public CustomFieldBindingModel toJson(CustomFieldBinding binding) {

		CustomFieldBindingModel bindingModel = new CustomFieldBindingModel();

		BindableEntityModel entityModel = toJson(binding.getBoundEntity());
		RenderingLocationModel[] locationArrayModel = toJson(binding.getRenderingLocations());
		CustomFieldModel<?> fieldModel = toJson(binding.getCustomField());

		bindingModel.setId(binding.getId());
		bindingModel.setProjectId(binding.getBoundProject().getId());
		bindingModel.setBoundEntity(entityModel);
		bindingModel.setCustomField(fieldModel);
		bindingModel.setRenderingLocations(locationArrayModel);
		bindingModel.setPosition(binding.getPosition());

		return bindingModel;

	}

	public BindableEntityModel toJson(BindableEntity entity) {

		BindableEntityModel model = new BindableEntityModel();

		model.setEnumName(entity.toString());
		model.setFriendlyName(getMessage(entity.getI18nKey()));

		return model;

	}


	// ************ custom field and custom field values jsonifications ***************

	public CustomFieldModel<?> toJson(CustomField field) {

		return customFieldFactory.createCustomFieldModel(field);

	}


	// because of Java spec 15.12 and because I don't want another Visitor bullshit
	// we're doing an ugly downcast for multi select fields when appropriate
	@SuppressWarnings("unchecked")
	public CustomFieldValueModel toJson(CustomFieldValue value) {

		// TODO expression below is either false or can be rewritten as an instanceof
		if (MultiValuedCustomFieldValue.class.isAssignableFrom(value.getClass())) {
			return toJson((TagsValue) value);
		} else {
			CustomFieldValueModel model = createStdCustomFieldValues(value);
			model.setValue(value.getValue());

			return model;
		}
	}

	public CustomFieldValueModel toJson(TagsValue value) {

		CustomFieldValueModel model = createStdCustomFieldValues(value);

		List<String> options = new ArrayList<>(value.getSelectedOptions().size());
		for (CustomFieldValueOption option : value.getSelectedOptions()) {
			options.add(option.getLabel());
		}
		model.setOptionValues(options);
		return model;
	}


	private <CF extends CustomFieldValue> CustomFieldValueModel createStdCustomFieldValues(CF value) {
		CustomFieldValueModel model = new CustomFieldValueModel();

		BindableEntityModel entityTypeModel = toJson(value.getBoundEntityType());
		CustomFieldBindingModel bindingModel = toJson(value.getBinding());

		model.setId(value.getId());
		model.setBoundEntityId(value.getBoundEntityId());
		model.setBoundEntityType(entityTypeModel);
		model.setBinding(bindingModel);

		return model;
	}


	// *********************** denormalized field values **************************


	public CustomFieldModel<?> toCustomFieldJsonModel(DenormalizedFieldValue field) {

		return customFieldFactory.createCustomFieldModel(field);

	}


	@SuppressWarnings("unchecked")
	public CustomFieldValueModel toJson(DenormalizedFieldValue value) {

		CustomFieldValueModel model = new CustomFieldValueModel();

		// pseudo custom field binding
		CustomFieldBindingModel bindingModel = new CustomFieldBindingModel();
		bindingModel.setPosition(value.getPosition());
		bindingModel.setRenderingLocations(toJson(value.getRenderingLocations()));

		// pseudo input type
		InputTypeModel inputTypeModel = new InputTypeModel();
		inputTypeModel.setEnumName(value.getInputType().name());
		inputTypeModel.setFriendlyName(value.getInputType().name());

		// pseudo custom field
		CustomFieldModel<?> customFieldModel = toCustomFieldJsonModel(value);

		// pseudo bindable entity
		BindableEntityModel bindableEntityModel = new BindableEntityModel();
		bindableEntityModel.setEnumName(value.getDenormalizedFieldHolderType().name());
		bindableEntityModel.setFriendlyName(value.getDenormalizedFieldHolderType().name());
		bindingModel.setBoundEntity(bindableEntityModel);


		// wire all the stuff
		model.setId(value.getId());
		model.setBoundEntityId(value.getDenormalizedFieldHolderId());
		model.setBinding(bindingModel);

		// the value depends on the actual subtype
		// TODO expression below is either false or can be rewritten as an instanceof
		if (DenormalizedMultiSelectField.class.isAssignableFrom(value.getClass())) {
			model.setOptionValues(((DenormalizedMultiSelectField) value).getValues());
		} else {
			model.setValue(value.getValue());
		}


		bindingModel.setCustomField(customFieldModel);


		return model;

	}


	// ***************** other things ******************************

	private String getMessage(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, locale);
	}

}


