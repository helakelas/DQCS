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
package org.datacleaner.result;

import org.datacleaner.api.AnalyzerResult;


/**
 * Result producers produce AnalyzerResults. Typically they are used to
 * represent command-objects that produce a drill-to-detail/exploration style
 * result in a crosstab, chart or similar.
 * 结果产生器产生AnalyzerResults。通常，
 * 它们用于表示产生钻取到详细信息/探索样式的命令对象产生交叉表，图表或类似内容。
 *
 * ResultProducers are not required to be Serializable, but if they are they
 * will be saved with the rest of the AnalyzerResult that contain them, if that
 * is persisted.
 * ResultProducers不需要是可序列化的，但是如果它们是，
 * 则将与包含它们的其余AnalyzerResult保存在一起，如果仍然存在。
 */
public interface ResultProducer {

    AnalyzerResult getResult();
}
