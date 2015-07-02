/**
 * This file is part of the Squashtest platform.
 * Copyright (C) 2010 - 2015 Henix, henix.fr
 * <p/>
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p/>
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * this software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.infolist;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.FieldComparator;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Locale;

/**
 * FIXME NdGRF I added required methods when upgrading dependencies. As there is no documentation (comments or tests),
 * the original author shall check and fix.
 */
public class InfoListItemComparator extends FieldComparator<String> {

    private String[] values;
    private String[] currentReaderValues;
    private final String field;
    private String bottom;
    private String i18nRoot;
    private MessageSource source;
    private Locale locale;
    private String top;

    InfoListItemComparator(int numHits, String field, String i18nRoot, MessageSource source, Locale locale) {
        values = new String[numHits];
        this.field = field;
        this.i18nRoot = i18nRoot;
        this.source = source;
        this.locale = locale;
    }

    @Override
    public int compare(int slot1, int slot2) {
        final String val1 = values[slot1];
        final String val2 = values[slot2];

        int result = 0;
        if (val1 == null) {
            if (val2 != null) {
                result = -1;
            }
        } else if (val2 == null) {
            result = 1;
        } else {
            String internationalizedVal1 = source.getMessage(i18nRoot + val1, null, val1, locale);
            String internationalizedVal2 = source.getMessage(i18nRoot + val2, null, val2, locale);
            result = internationalizedVal1.compareTo(internationalizedVal2);
        }
        return result;
    }

    @Override
    public int compareBottom(int doc) {
        return compareValueToDoc(bottom, doc);
    }

    private int compareValueToDoc(String val1, int doc) {
        final String val2 = currentReaderValues[doc];

        int result = 0;
        if (val1 == null) {
            if (val2 != null) {
                result = -1;
            }
        } else if (val2 == null) {
            result = 1;
        } else {
            String internationalizedVal2 = source.getMessage(i18nRoot + val2, null, val2, locale);
            result = val1.compareTo(internationalizedVal2);
        }

        return result;
    }

    @Override
    public int compareTop(int doc) throws IOException {
        // implementation according to what is specified in javadoc
        return compareValueToDoc(top, doc);
    }

    @Override
    public void copy(int slot, int doc) {
        values[slot] = currentReaderValues[doc];
    }

    @Override
    public FieldComparator<String> setNextReader(AtomicReaderContext context) throws IOException {
        // FIXME 90% sure its broken but I'm clueless about what to do
//        currentReaderValues = FieldCache.DEFAULT.getStrings(reader, field);
        return this;
    }

    @Override
    public void setBottom(final int bottom) {

        this.bottom = values[bottom];
    }

    @Override
    public void setTopValue(String top) {
        // FIXME blind-guess implementation
        this.top = top;
    }

    @Override
    public String value(int slot) {

        return values[slot];
    }
}
