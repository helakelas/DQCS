/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.monitor.wizard.referencedata;

import org.datacleaner.monitor.wizard.Wizard;

/**
 * A pluggable component (plug-in / extension) which provides a wizard in the
 * webapp for creating reference data in the DC monitor configuration.
 * 可插拔组件（插件/扩展），它在webapp中提供了一个向导，用于在DC监视器配置中创建参考数据。
 */
public interface ReferenceDataWizard extends Wizard<ReferenceDataWizardContext, ReferenceDataWizardSession> {
}
