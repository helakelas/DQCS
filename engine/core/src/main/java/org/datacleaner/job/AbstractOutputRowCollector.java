/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.job;

import org.apache.metamodel.data.Row;
import org.datacleaner.api.OutputRowCollector;

/**
 * Convenient abstract implementation of most methods in the
 * {@link OutputRowCollector} interface.
 * {@link OutputRowCollector}接口中大多数方法的便捷抽象实现。
 */
public abstract class AbstractOutputRowCollector implements OutputRowCollector {

    @Override
    public void putRow(final Row row) {
        putValues(row.getValues());
    }

}
