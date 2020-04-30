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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.DataSetTableModel;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.ResourceDatastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.widgets.LoadingIcon;
import org.datacleaner.widgets.ResourceSelector;
import org.datacleaner.widgets.ResourceTypePresenter;
import org.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.JXStatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for {@link Resource}-based datastores such as {@link CsvDatastore}
 * and {@link JsonDatastore} etc.
 * 基于{@link Resource}的数据存储区的超类，例如{@link CsvDatastore}和{@link JsonDatastore}等。
 *
 * @param <D>
 *            the type of datastore
 */
public abstract class AbstractResourceBasedDatastoreDialog<D extends ResourceDatastore>
        extends AbstractDatastoreDialog<D> {

    /**
     * Amount of bytes to read for autodetection of encoding, separator and
     * quotes
     */
    protected static final int SAMPLE_BUFFER_SIZE = 128 * 1024;
    private static final long serialVersionUID = 1L;
    /**
     * Max amount of columns to display in the preview table
     */
    private static final int PREVIEW_COLUMNS = 10;
    private static final int PREVIEW_ROWS = 7;
    private static final Logger logger = LoggerFactory.getLogger(AbstractResourceBasedDatastoreDialog.class);
    private final ResourceSelector _resourceSelector;
    private final DCPanel _previewTablePanel;
    private final DCTable _previewTable;
    private final LoadingIcon _loadingIcon;

    protected AbstractResourceBasedDatastoreDialog(final D originalDatastore,
            final MutableDatastoreCatalog mutableDatastoreCatalog, final WindowContext windowContext,
            final DataCleanerConfiguration configuration, final UserPreferences userPreferences) {
        super(originalDatastore, mutableDatastoreCatalog, windowContext, userPreferences);
        _statusLabel.setText("Please select source");
        _resourceSelector = new ResourceSelector(configuration, getUserPreferences(), true);

        if (originalDatastore != null) {
            if (originalDatastore instanceof ResourceDatastore) {
                final ResourceDatastore resourceDatastore = (ResourceDatastore) originalDatastore;
                final Resource resource = resourceDatastore.getResource();
                _resourceSelector.setResource(resource);
            }
        }

        initializeFileFilters(_resourceSelector);

        _resourceSelector.addListener(new ResourceTypePresenter.Listener() {

            @Override
            public void onPathEntered(final ResourceTypePresenter<?> presenter, final String path) {
                onSelected(null);
            }

            @Override
            public void onResourceSelected(final ResourceTypePresenter<?> presenter, final Resource resource) {
                if (resource != null) {
                    if (StringUtils.isNullOrEmpty(_datastoreNameTextField.getText())) {
                        _datastoreNameTextField.setText(resource.getName());
                    }
                }

                validateAndUpdate();

                onSelected(resource);
            }
        });

        if (isPreviewTableEnabled()) {
            _previewTable = new DCTable(new DefaultTableModel(7, 10));
            _previewTablePanel = _previewTable.toPanel(false);
            _loadingIcon = new LoadingIcon();
            _loadingIcon.setVisible(false);
            _loadingIcon.setPreferredSize(_previewTablePanel.getPreferredSize());
        } else {
            _previewTable = null;
            _previewTablePanel = null;
            _loadingIcon = null;
        }
    }

    /**
     * Can be overridden by subclasses in order to react to resource selection
     * events.
     *
     * @param resource
     */
    protected void onSelected(final Resource resource) {
    }

    protected abstract D createDatastore(String name, Resource resource);

    protected abstract void initializeFileFilters(ResourceSelector resourceSelector);

    @Override
    protected final void validateAndUpdate() {
        final boolean valid = validateForm();
        setSaveButtonEnabled(valid);
        if (valid) {
            updatePreviewTable();
        } else {
            hidePreviewTable();
        }
    }

    protected boolean validateForm() {
        final Resource resource = _resourceSelector.getResource();
        if (resource == null) {
            setStatusError("Please enter or select a source");
            return false;
        }

        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            setStatusError("Please enter a datastore name");
            return false;
        }

        setStatusValid();
        return true;
    }

    protected boolean isPreviewTableEnabled() {
        return false;
    }

    @Override
    protected int getDialogWidth() {
        if (isPreviewTableEnabled()) {
            return WidgetUtils.DIALOG_WIDTH_WIDE;
        }
        return super.getDialogWidth();
    }

    public Resource getResource() {
        return _resourceSelector.getResource();
    }

    @Override
    protected final D createDatastore() {
        return createDatastore(getDatastoreName(), getResource());
    }

    protected List<Entry<String, JComponent>> getFormElements() {
        final List<Entry<String, JComponent>> res = super.getFormElements();
        res.add(new ImmutableEntry<>("Source", _resourceSelector));
        return res;
    }

    public String getDatastoreName() {
        return _datastoreNameTextField.getText();
    }

    private void updatePreviewTable() {
        if (!isPreviewTableEnabled()) {
            return;
        }

        // show loading indicator
        setSaveButtonEnabled(false);
        _previewTablePanel.setVisible(false);
        _loadingIcon.setVisible(true);

        // read file in background, it may take time if eg. it's located on a
        // network drive
        new SwingWorker<DataSet, Void>() {

            @Override
            protected DataSet doInBackground() throws Exception {
                return getPreviewData(getResource());
            }

            @Override
            protected void done() {
                try {
                    final DataSet dataSet = get();
                    if (dataSet == null) {
                        hidePreviewTable();
                    } else {
                        final TableModel tableModel = new DataSetTableModel(dataSet);
                        _previewTable.setModel(tableModel);
                    }
                } catch (final Throwable e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Error creating preview data: " + e.getMessage(), e);
                    }

                    hidePreviewTable();
                    setStatusError(e);
                }

                // show table
                _previewTablePanel.setVisible(true);
                _loadingIcon.setVisible(false);
                setSaveButtonEnabled(true);
            }
        }.execute();
    }

    private void hidePreviewTable() {
        final DefaultTableModel dummyTableModel = new DefaultTableModel(PREVIEW_ROWS, PREVIEW_COLUMNS);
        _previewTable.setModel(dummyTableModel);
    }

    private DataSet getPreviewData(final Resource resource) {
        if (!isPreviewDataAvailable()) {
            logger.info("Not displaying preview table because isPreviewDataAvailable() returned false");
            return null;
        }

        logger.info("Attempting to fetch preview data from resource: {}", resource);

        final D datastore = getPreviewDatastore(resource);
        try (DatastoreConnection con = datastore.openConnection()) {
            final DataContext dc = con.getDataContext();
            final Table table = getPreviewTable(dc);
            if (table == null) {
                logger.info("Not displaying preview because getPreviewTable(..) returned null");
                return null;
            }

            List<Column> columns = table.getColumns();
            if (columns.size() > getPreviewColumns()) {
                // include max 10 columns
                columns = columns.stream().limit(getPreviewColumns()).collect(Collectors.toList());
            }
            final Query q = dc.query().from(table).select(columns).toQuery();
            q.setMaxRows(PREVIEW_ROWS);

            return dc.executeQuery(q);
        }
    }

    @Override
    protected final JComponent getDialogContent() {
        final DCPanel formPanel = new DCPanel();
        final GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[] { 120, 450 };
        formPanel.setLayout(layout);

        final List<Entry<String, JComponent>> formElements = getFormElements();
        // temporary variable to make it easier to refactor the layout
        int row = 0;
        for (final Entry<String, JComponent> entry : formElements) {
            final String key = entry.getKey();
            if (StringUtils.isNullOrEmpty(key)) {
                WidgetUtils.addToGridBag(entry.getValue(), formPanel, 0, row, 2, 1);
            } else {
                WidgetUtils.addToGridBag(DCLabel.bright(key + ":"), formPanel, 0, row);
                WidgetUtils.addToGridBag(entry.getValue(), formPanel, 1, row);
            }
            row++;
        }

        if (isPreviewTableEnabled()) {
            WidgetUtils.addToGridBag(_loadingIcon, formPanel, 0, row, 2, 1);
            row++;
        }

        final DCPanel centerPanel = new DCPanel();
        centerPanel.setLayout(new GridBagLayout());
        WidgetUtils.addToGridBag(formPanel, centerPanel, 0, 0, 1, 1, GridBagConstraints.NORTH, 4, 1.0, 0,
                GridBagConstraints.BOTH);

        if (isPreviewTableEnabled()) {
            WidgetUtils.addToGridBag(_previewTablePanel, centerPanel, 0, 1, 1, 1, GridBagConstraints.NORTH, 4, 0.1, 1.0,
                    GridBagConstraints.BOTH);
        }
        WidgetUtils.addToGridBag(getButtonPanel(), centerPanel, 0, 2, 1, 1, GridBagConstraints.SOUTH, 4, 0, 0.1);

        centerPanel.setBorder(WidgetUtils.BORDER_TOP_PADDING);

        final JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);

        _outerPanel.setLayout(new BorderLayout());
        _outerPanel.add(centerPanel, BorderLayout.CENTER);
        _outerPanel.add(statusBar, BorderLayout.SOUTH);

        final String descriptionText = getDescriptionText();
        if (descriptionText != null) {
            final DescriptionLabel descriptionLabel = new DescriptionLabel();
            descriptionLabel.setText(descriptionText);
            _outerPanel.add(descriptionLabel, BorderLayout.NORTH);
        }

        validateAndUpdate();

        return _outerPanel;
    }

    protected boolean isPreviewDataAvailable() {
        return true;
    }

    protected Table getPreviewTable(final DataContext dc) {
        final Schema schema = dc.getDefaultSchema();
        if (schema.getTableCount() == 0) {
            return null;
        }
        return schema.getTable(0);
    }

    protected int getPreviewColumns() {
        return PREVIEW_COLUMNS;
    }

    protected D getPreviewDatastore(final Resource resource) {
        return createDatastore("Preview", resource);
    }

    protected byte[] getSampleBuffer() {
        byte[] bytes = new byte[SAMPLE_BUFFER_SIZE];

        final Resource resource = getResource();
        final InputStream in = resource.read();
        try {
            final int bufferSize = in.read(bytes, 0, SAMPLE_BUFFER_SIZE);
            if (bufferSize != -1 && bufferSize != SAMPLE_BUFFER_SIZE) {
                bytes = Arrays.copyOf(bytes, bufferSize);
            }
            return bytes;
        } catch (final IOException e) {
            logger.error("IOException occurred while reading sample buffer", e);
            return new byte[0];
        } finally {
            FileHelper.safeClose(in);
        }
    }

    protected char[] readSampleBuffer(final byte[] bytes, final String charSet) {
        char[] buffer = new char[bytes.length];
        Reader reader = null;
        try {
            reader = new InputStreamReader(new ByteArrayInputStream(bytes), charSet);

            // read a sample of the file to auto-detect quotes and separators
            final int bufferSize = reader.read(buffer);
            if (bufferSize != -1) {
                buffer = Arrays.copyOf(buffer, bufferSize);
            }
        } catch (final Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Error reading from source: " + e.getMessage(), e);
            }
            setStatusError("Error reading from source: " + e.getMessage());
            return new char[0];
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException ioe) {
                    logger.debug("Could not close reader", ioe);
                }
            }
        }
        return buffer;
    }

    protected DCTable getPreviewTable() {
        return _previewTable;
    }
}
