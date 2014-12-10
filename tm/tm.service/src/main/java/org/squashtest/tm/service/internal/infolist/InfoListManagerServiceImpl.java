/**
 * This file is part of the Squashtest platform. Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * this software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.infolist;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.service.infolist.InfoListManagerService;
import org.squashtest.tm.service.internal.repository.InfoListDao;
import org.squashtest.tm.service.internal.repository.InfoListItemDao;

@Transactional
@Service("squashtest.tm.service.InfoListManagerService")
public class InfoListManagerServiceImpl implements InfoListManagerService {

	@Inject
	private InfoListDao infoListDao;

	@Inject
	private InfoListItemDao infoListItemDao;

	@Override
	public InfoList findById(Long id) {
		return infoListDao.findById(id);
	}

	@Override
	public InfoList findByCode(String code) {
		return infoListDao.findByCode(code);
	}

	@Override
	public void changeDescription(long infoListId, String newDescription) {

		InfoList infoList = findById(infoListId);
		infoList.setDescription(newDescription);

	}

	@Override
	public void changeLabel(long infoListId, String newLabel) {
		InfoList infoList = findById(infoListId);
		infoList.setLabel(newLabel);

	}

	@Override
	public void changeCode(long infoListId, String newCode) {
		InfoList infoList = findById(infoListId);
		infoList.setCode(newCode);

	}

	@Override
	public void upgradeVersion(InfoList infoList) {
		int currVersion = infoList.getVersion();
		infoList.setVersion(++currVersion);
	}

	@Override
	public void changeItemsPositions(long infoListId, int newIndex, List<Long> itemsIds) {
		InfoList infoList = findById(infoListId);

		List<InfoListItem> items = infoListItemDao.findAllByIds(itemsIds);
		for (InfoListItem item : items) {
			infoList.removeItem(item);
		}
		infoList.addItems(newIndex, items);
	}

	@Override
	public boolean isUsedByOneOrMoreProject(long infoListId) {
	
		return 	infoListDao.isUsedByOneOrMoreProject(infoListId);
	}

}
