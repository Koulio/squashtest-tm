/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class should be used to obtain locks on a given entity. An entity is defined with its class ans its primary key.
 *
 * @author Gregory Fouquet
 * @since 1.11.6
 */
public final class EntityLockManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityLockManager.class);

	/**
	 * "static" class -> private constructor
	 */
	private EntityLockManager() {
		super();
	}

	private static class EntityRef {
		private final Class type;
		private final Serializable id;

		@Override
		public String toString() {
			return "EntityRef{" +
				"type=" + type +
				", id=" + id +
				'}';
		}

		private EntityRef(@NotNull Class type, Serializable id) {
			this.type = type;
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			EntityRef entityRef = (EntityRef) o;

			if (!type.equals(entityRef.type)) return false;
			return id.equals(entityRef.id);

		}

		@Override
		public int hashCode() {
			int result = type.hashCode();
			result = 31 * result + id.hashCode();
			return result;
		}
	}

	private static final Map<EntityRef, WeakReference<ReentrantLock>> locks = new ConcurrentHashMap<EntityRef, WeakReference<ReentrantLock>>();

	/**
	 * Gets a lock for the given entity.
	 *
	 * @param type
	 * @param id
	 * @return
	 */
	public static synchronized ReentrantLock getLock(Class type, Serializable id) {
		EntityRef ref = new EntityRef(type, id);
		WeakReference<ReentrantLock> wr = locks.get(ref);
		ReentrantLock lock;

		if (wr == null) {
			lock = createLock(ref);
		} else {
			LOGGER.trace("Retrieved lock for entity {}", ref);
			lock = wr.get();

			if (lock == null) {
				LOGGER.trace("Previous lock was GC'd");
				lock = createLock(ref);
			}
		}

		LOGGER.debug("Gotten lock for {}", ref);
		return lock;
	}

	private static ReentrantLock createLock(EntityRef ref) {
		ReentrantLock lock;
		LOGGER.trace("Creating new weak reference and lock for entity {}", ref);
		lock = new ReentrantLock();
		locks.put(ref, new WeakReference<ReentrantLock>(lock));
		return lock;
	}

	/**
	 * Batch-lock entities of a given type
	 * @param type
	 * @param ids
	 * @return
	 */
	public static synchronized Collection<Lock> lock(Class<?> type, Collection<? extends Serializable> ids) {
		LOGGER.trace("Batch locking entities of type {} and ids {}", type, ids);
		ArrayList<Lock> locks = new ArrayList<Lock>(ids.size());

		for (Serializable id : ids) {
			Lock lock = getLock(type, id);
			lock.lock();
			locks.add(lock);
		}

		return locks;
	}

	/**
	 * Releases the given locks
	 * @param locks Non-null collection of non-null locks
	 */
	public static void release(Collection<Lock> locks) {
		LOGGER.trace("Batch unlocking entities");
		for (Lock lock : locks) {
			if (lock != null) {
				lock.unlock();
			}
		}
	}

}