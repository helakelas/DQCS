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
package org.datacleaner.widgets.properties;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.util.Resource;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.ExtensionFilter;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.convert.ResourceConverter;
import org.datacleaner.widgets.AbstractResourceTextField;
import org.datacleaner.widgets.ResourceSelector;
import org.datacleaner.widgets.ResourceTypePresenter;

/**
 * Property widget for a single {@link Resource} field.
 * 单个{@link Resource}字段的属性窗口小部件。
 */
public final class SingleResourcePropertyWidget extends AbstractPropertyWidget<Resource> {

    private final ResourceSelector _resourceTextField;

    @Inject
    public SingleResourcePropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor,
            final ComponentBuilder componentBuilder, final UserPreferences userPreferences) {
        super(componentBuilder, propertyDescriptor);

        boolean openMode = true;
        String[] extensions = null;

        final FileProperty fileProperty = propertyDescriptor.getAnnotation(FileProperty.class);
        if (fileProperty != null) {
            openMode = fileProperty.accessMode() == FileAccessMode.OPEN;

            extensions = fileProperty.extension();
        }

        final ResourceConverter resourceConverter = getResourceConverter();
        _resourceTextField =
                new ResourceSelector(getAnalysisJobBuilder().getConfiguration(), resourceConverter, userPreferences,
                        openMode);

        if (extensions != null && extensions.length > 0) {
            final List<FileFilter> filters = new ArrayList<>(extensions.length);
            for (final String extension : extensions) {
                final String extensionWithDot;
                if (extension.startsWith(".")) {
                    extensionWithDot = extension;
                } else {
                    extensionWithDot = "." + extension;
                }
                final FileFilter filter = new ExtensionFilter(extension.toUpperCase() + " file", extensionWithDot);
                filters.add(filter);
                _resourceTextField.addChoosableFileFilter(filter);
            }
            if (filters.size() == 1) {
                _resourceTextField.setSelectedFileFilter(filters.get(0));
            } else {
                final FileFilter filter = FileFilters
                        .combined("All suggested file formats", filters.toArray(new FileFilter[filters.size()]));
                _resourceTextField.setSelectedFileFilter(filter);
            }
        } else {
            _resourceTextField.setSelectedFileFilter(FileFilters.ALL);
        }

        final Resource currentValue = getCurrentValue();
        if (currentValue != null) {
            _resourceTextField.setResource(currentValue);
        }

        _resourceTextField.addListener(new ResourceTypePresenter.Listener() {

            @Override
            public void onResourceSelected(final ResourceTypePresenter<?> presenter, final Resource resource) {
                fireValueChanged();
            }

            @Override
            public void onPathEntered(final ResourceTypePresenter<?> presenter, final String path) {
                fireValueChanged();
            }
        });

        add(_resourceTextField);
    }

    /**
     * Gets the resource converter to use - can be overridden by subclasses if
     * needed.
     *
     * @return
     */
    protected ResourceConverter getResourceConverter() {
        final DataCleanerConfiguration configuration = getAnalysisJobBuilder().getConfiguration();
        return new ResourceConverter(configuration);
    }

    @Override
    public boolean isSet() {
        return getValue() != null;
    }

    /**
     *
     * @return The text field used for the filename
     * @deprecated use {@link #getResourceTextField()} instead
     */
    @Deprecated
    public AbstractResourceTextField<?> getFilenameField() {
        final ResourceTypePresenter<?> presenter = _resourceTextField.getResourceTypePresenter("file");
        if (presenter instanceof AbstractResourceTextField) {
            return (AbstractResourceTextField<?>) presenter;
        }
        return null;
    }

    public ResourceSelector getResourceTextField() {
        return _resourceTextField;
    }

    @Override
    public Resource getValue() {
        return _resourceTextField.getResource();
    }

    @Override
    protected void setValue(final Resource value) {
        _resourceTextField.setResource(value);
    }
}
