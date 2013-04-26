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
package org.squashtest.tm.web.internal.controller.attachment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.service.foundation.collection.CollectionSorting;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;
import org.squashtest.tm.web.internal.model.datatable.DataTableCollectionSorting;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

@Controller
@RequestMapping("/attach-list/{attachListId}/attachments")
public class AttachmentManagerController {
	
	private static final String NAME = "name";

	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentManagerController.class);
	
	private AttachmentManagerService attachmentManagerService;
	
	private static final String ATTACH_LIST_ID = "attachListId";
	

	@Inject
	private MessageSource messageSource;
	

	private final DatatableMapper attachmentMapper = new NameBasedMapper()
														 .mapAttribute(Attachment.class, "id", Long.class, "item-id")
														 .mapAttribute(Attachment.class, NAME, String.class, "hyphenated-name")
														 .mapAttribute(Attachment.class, "size", Long.class, "size")
														 .mapAttribute(Attachment.class, "addedOn", Date.class, "added-on");

	@ServiceReference
	public void setAttachmentManagerService(AttachmentManagerService attachmentManagerService) {
		this.attachmentManagerService = attachmentManagerService;
	}

	
	/* ********************** data display *********************************** */
	
	@RequestMapping(value="/manager", method=RequestMethod.GET)
	public ModelAndView showAttachmentManager(@PathVariable(ATTACH_LIST_ID) long attachListId, @RequestParam("workspace") String workspace){

		ModelAndView mav = new ModelAndView("page/attachments/attachment-manager");
		mav.addObject("workspace",workspace);
		mav.addObject(ATTACH_LIST_ID, attachListId);

		return mav;

	}

	@RequestMapping(value="/details", method=RequestMethod.GET)
	public @ResponseBody DataTableModel displayAttachmentDetails(@PathVariable(ATTACH_LIST_ID) long attachListId, final DataTableDrawParameters params,
			final Locale locale){
		CollectionSorting filter = createPaging(params, attachmentMapper);
		FilteredCollectionHolder<List<Attachment>> attachList = attachmentManagerService.findFilteredAttachmentForList(attachListId, filter);

		
		DataTableModelHelper helper = new  AttachmentsTableModelHelper(messageSource);
		return helper.buildDataModel(attachList, filter.getFirstItemIndex()+1, params.getsEcho());

	}
	
	
	/* ******************************* delete *********************************** */
	
	@RequestMapping(value="/{attachmentIds}", method=RequestMethod.DELETE)
	@ResponseBody
	public void removeAttachment(@PathVariable long attachListId, @PathVariable("attachmentIds") List<Long> ids){
		attachmentManagerService.removeListOfAttachments(attachListId, ids);
	}

	/* ******************************* modify *********************************** */
	
	
	@RequestMapping(value="/{attachmentId}/name",method = RequestMethod.POST, params = { NAME })
	@ResponseBody
	public Object renameAttachment(HttpServletResponse response, @PathVariable long attachmentId, @RequestParam(NAME) String newName) {

		attachmentManagerService.renameAttachment(attachmentId, newName);
		LOGGER.info("AttachmentController : renaming attachment " + attachmentId + " as " + newName);
		return new RenameModel(newName);

	}

	
	/* ******************************* private stuffs ***************************** */

	
	private static class AttachmentsTableModelHelper extends DataTableModelHelper<Attachment>{

		private MessageSource messageSource;
		private Locale locale;
		private static final int INT_MAX_FILENAME_LENGTH = 50;
		
		public AttachmentsTableModelHelper(MessageSource messageSource){
			this.messageSource = messageSource;
			this.locale = LocaleContextHolder.getLocale();
		}
		
		@Override
		protected Map<Object, Object> buildItemData(Attachment item) {
			
			Map<Object, Object> result = new HashMap<Object, Object>();
			
			result.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			result.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			result.put(NAME, item.getName());
			result.put("hyphenated-name", hyphenateFilename(item.getName()));
			result.put("size",item.getFormattedSize(locale));
			result.put("added-on",localizedDate(item.getAddedOn(),locale));
			result.put(DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY, null);
			
			return result;
		}
		
		private String localizedDate(Date date, Locale locale){
			String format = messageSource.getMessage("squashtm.dateformat", null, locale);

			return new SimpleDateFormat(format).format(date);

		}
		
		private String hyphenateFilename(String longName){
			String newName = longName;
			if (longName.length() > INT_MAX_FILENAME_LENGTH){
				newName = longName.substring(0, (INT_MAX_FILENAME_LENGTH-3))+"...";
			}
			return newName;
		}

		
	}
	
	private CollectionSorting createPaging(final DataTableDrawParameters params, final DatatableMapper mapper) {
		return new DataTableCollectionSorting(params, mapper);
	}


}
