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
package org.datacleaner.beans.script;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Provided;
import org.datacleaner.api.StringProperty;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ScriptingCategory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * A transformer that uses userwritten JavaScript to generate a transformer
 * object
 * 使用用户编写的JavaScript生成一个Translator对象的转换器
 */
@Named("JavaScriptAdvancedTransformer.name")
@Description("JavaScriptAdvancedTransformer.Description")
@Categorized(ScriptingCategory.class)
@Concurrent(false)
public class JavaScriptAdvancedTransformer implements Transformer {

    @Inject
    @Configured
    InputColumn<?>[] columns;

    @Inject
    @Configured
    Class<?>[] returnTypes = new Class[] { String.class, Object.class };

    @Inject
    @Configured
    @StringProperty(multiline = true, mimeType = { "text/javascript", "application/x-javascript" })
    // @formatter:off
    // @checkstyle:off
    String sourceCode =
              "var transformerObj = {\n"
            + "    initialize: function () {\n"
            + "        logger.info('Initializing advanced JavaScript transformer...');\n"
            + "    },\n"
            + "\n"
            + "    transform: function (columns, values, outputCollector) {\n"
            + "        logger.debug('transform({},{},{}) invoked', columns, values, outputCollector);\n"
            + "        for (var i = 0; i < columns.length; i++) {\n"
            + "            outputCollector.putValues(columns[i], values[i])\n"
            + "        }\n"
            + "    },\n"
            + "\n"
            + "    close: function () {\n"
            + "        logger.info('Closing advanced JavaScript transformer...');\n"
            + "    }\n"
            + "}";
    // @formatter:on
    // @checkstyle:on

    @Inject
    @Provided
    OutputRowCollector rowCollector;

    private ContextFactory _contextFactory;
    private Script _script;
    private ScriptableObject _sharedScope;
    private NativeObject _transformerObj;

    private Function _initializeFunction;
    private Function _transformFunction;
    private Function _closeFunction;

    @Override
    public OutputColumns getOutputColumns() {
        final String[] names = new String[returnTypes.length];
        final Class<?>[] types = new Class[returnTypes.length];
        for (int i = 0; i < returnTypes.length; i++) {
            names[i] = "JavaScript output " + (i + 1);
            types[i] = returnTypes[i];
        }
        return new OutputColumns(names, types);
    }

    @Initialize
    public void init() {
        _contextFactory = new ContextFactory();

        final Context context = _contextFactory.enterContext();
        try {
            _script = context.compileString(sourceCode, this.getClass().getSimpleName(), 1, null);
            _sharedScope = context.initStandardObjects();

            JavaScriptUtils.addToScope(_sharedScope, new JavaScriptLogger(), "logger", "log");
            JavaScriptUtils.addToScope(_sharedScope, System.out, "out");

            _script.exec(context, _sharedScope);
            _transformerObj = (NativeObject) _sharedScope.get("transformerObj");
            if (_transformerObj == null) {
                throw new IllegalStateException("Required JS object 'transformerObj' not found!");
            }

            _initializeFunction = (Function) _transformerObj.get("initialize");
            _transformFunction = (Function) _transformerObj.get("transform");
            _closeFunction = (Function) _transformerObj.get("close");

            _initializeFunction.call(context, _sharedScope, _sharedScope, new Object[0]);
        } finally {
            Context.exit();
        }
    }

    @Close
    public void close() {
        final Context context = _contextFactory.enterContext();
        try {
            _closeFunction.call(context, _sharedScope, _sharedScope, new Object[0]);
        } finally {
            Context.exit();
        }
    }

    @Override
    public Object[] transform(final InputRow inputRow) {
        final Context context = _contextFactory.enterContext();
        try {
            final String[] columnNames = new String[columns.length];
            final Object[] values = new Object[columns.length];
            for (int i = 0; i < columns.length; i++) {
                columnNames[i] = columns[i].getName();
                values[i] = inputRow.getValue(columns[i]);
            }
            final Object[] args = { columnNames, values, rowCollector };
            _transformFunction.call(context, _sharedScope, _sharedScope, args);
            return null;
        } finally {
            Context.exit();
        }
    }
}
