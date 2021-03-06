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
package org.datacleaner.widgets;

import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;

import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.action.OpenBrowserAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HTML viewer.
 * 简单的HTML查看器。
 */
public class DCHtmlBox extends JEditorPane {
    private static final Logger logger = LoggerFactory.getLogger(DCHtmlBox.class);
    private static final long serialVersionUID = 1L;
    private static final String HTML_START_TAG = "<html>";
    private static final String HTML_END_TAG = "</html>";
    private static final String CONTENT_TYPE_HTML = "text/html";

    public DCHtmlBox(final String text) {
        super();

        setEditorKit(JEditorPane.createEditorKitForContentType(CONTENT_TYPE_HTML));
        setEditable(false);
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        setFont(WidgetUtils.FONT_NORMAL);
        addHyperlinkListener(hyperlinkEvent -> {
            if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    (new OpenBrowserAction(hyperlinkEvent.getURL())).actionPerformed(null);
                } catch (final URISyntaxException e) {
                    logger.warn("Link can not be opened. " + e.getMessage());
                }
            }
        });

        if (text != null) {
            setText(text);
        }
    }

    public void setText(String text) {
        if (text.startsWith(HTML_START_TAG) && text.endsWith(HTML_END_TAG)) {
            text = text.substring(HTML_START_TAG.length(), text.length() - HTML_END_TAG.length());
        }

        super.setText(HTML_START_TAG + getTableHtml(text) + HTML_END_TAG);
    }

    private String getTableHtml(final String content) {
        return "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td>" + content + "</td></tr></table>";
    }
}
