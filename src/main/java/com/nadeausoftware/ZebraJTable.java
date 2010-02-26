package com.nadeausoftware;

/**
 * A JTable that draws a zebra striped background.
 *
 * From http://nadeausoftware.com/articles/2008/01/
 * java_tip_how_add_zebra_background_stripes_jtable
 *
 * Included here with permission of the author - with slight
 * modifications to conform to my coding style; formatting, adding
 * final modifiers.
 *
 * It is licensed under the simplified BSD license:
 *
 * This class is Copyright (c) 2010, Dr. David R. Nadeau, Nadeau Software Consulting.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Nadeau Software Consulting nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
@SuppressWarnings("serial")
public class ZebraJTable extends javax.swing.JTable {
    private final java.awt.Color[] rowColors = new java.awt.Color[2];

    private boolean drawStripes = false;

    /**
     *
     */
    public ZebraJTable() {
    }

    /**
     * @param numRows the number of rows
     * @param numColumns the number of columns
     */
    public ZebraJTable(final int numRows, final int numColumns) {
        super(numRows, numColumns);
    }

    /**
     * @param rowData the row data
     * @param columnNames the names of the columns
     */
    public ZebraJTable(final Object[][] rowData, final Object[] columnNames) {
        super(rowData, columnNames);
    }

    /**
     * @param dataModel the data model
     */
    public ZebraJTable(final javax.swing.table.TableModel dataModel) {
        super(dataModel);
    }

    /**
     * @param dataModel the data model
     * @param columnModel the column model
     */
    public ZebraJTable(final javax.swing.table.TableModel dataModel,
            final javax.swing.table.TableColumnModel columnModel) {
        super(dataModel, columnModel);
    }

    /**
     * @param dataModel the data model
     * @param columnModel the column model
     * @param selectionModel the selection model
     */
    public ZebraJTable(final javax.swing.table.TableModel dataModel,
            final javax.swing.table.TableColumnModel columnModel,
            final javax.swing.ListSelectionModel selectionModel) {
        super(dataModel, columnModel, selectionModel);
    }

    /**
     * @param rowData the row data
     * @param columnNames the column names
     */
    public ZebraJTable(final java.util.Vector<?> rowData,
            final java.util.Vector<?> columnNames) {
        super(rowData, columnNames);
    }

    /**
     * Add stripes between cells and behind non-opaque cells.
     * {@inheritDoc}
     */
    @Override
    public void paintComponent(final java.awt.Graphics g) {
        drawStripes = isOpaque();
        if (!drawStripes) {
            super.paintComponent(g);
            return;
        }
        // Paint zebra background stripes
        updateZebraColors();
        final java.awt.Insets insets = getInsets();
        final int w = getWidth() - insets.left - insets.right;
        final int h = getHeight() - insets.top - insets.bottom;
        final int x = insets.left;
        int y = insets.top;
        int rowHeight = 16; // A default for empty tables
        final int nItems = getRowCount();
        for (int i = 0; i < nItems; i++, y += rowHeight) {
            rowHeight = getRowHeight(i);
            g.setColor(rowColors[i & 1]);
            g.fillRect(x, y, w, rowHeight);
        }
        // Use last row height for remainder of table area
        final int nRows = nItems + (insets.top + h - y) / rowHeight;
        for (int i = nItems; i < nRows; i++, y += rowHeight) {
            g.setColor(rowColors[i & 1]);
            g.fillRect(x, y, w, rowHeight);
        }
        final int remainder = insets.top + h - y;
        if (remainder > 0) {
            g.setColor(rowColors[nRows & 1]);
            g.fillRect(x, y, w, remainder);
        }
        // Paint component
        setOpaque(false);
        super.paintComponent(g);
        setOpaque(true);
    }

    /**
     * Add background stripes behind rendered cells.
     * {@inheritDoc}
     */
    @Override
    public java.awt.Component prepareRenderer(
            final javax.swing.table.TableCellRenderer renderer,
            final int row,
            final int col) {
        final java.awt.Component c = super.prepareRenderer(renderer, row, col);
        if (drawStripes && !isCellSelected(row, col)) {
            c.setBackground(rowColors[row & 1]);
        }
        return c;
    }

    /**
     * Add background stripes behind edited cells.
     * {@inheritDoc}
     */
    @Override
    public java.awt.Component prepareEditor(
            final javax.swing.table.TableCellEditor editor,
            final int row,
            final int col) {
        final java.awt.Component c = super.prepareEditor(editor, row, col);
        if (drawStripes && !isCellSelected(row, col)) {
            c.setBackground(rowColors[row & 1]);
        }
        return c;
    }

    /**
     * Force the table to fill the viewport's height.
     * {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        final java.awt.Component p = getParent();
        if (!(p instanceof javax.swing.JViewport)) {
            return false;
        }
        return ((javax.swing.JViewport) p).getHeight() > getPreferredSize().height;
    }

    /**
     * Compute zebra background stripe colors.
     */
    private void updateZebraColors() {
        rowColors[0] = getBackground();
        if (rowColors[0] == null) {
            rowColors[0] = java.awt.Color.white;
            rowColors[1] = java.awt.Color.white;
            return;
        }
        final java.awt.Color sel = getSelectionBackground();
        if (sel == null) {
            rowColors[1] = rowColors[0];
            return;
        }
        final float[] bgHSB = java.awt.Color.RGBtoHSB(rowColors[0].getRed(),
            rowColors[0].getGreen(), rowColors[0].getBlue(), null);
        final float[] selHSB = java.awt.Color.RGBtoHSB(sel.getRed(), sel
                .getGreen(), sel.getBlue(), null);
        rowColors[1] = java.awt.Color.getHSBColor(
            (selHSB[1] == 0.0 || selHSB[2] == 0.0) ? bgHSB[0] : selHSB[0], 0.1f
                    * selHSB[1] + 0.9f * bgHSB[1], bgHSB[2]
                    + ((bgHSB[2] < 0.5f) ? 0.05f : -0.05f));
    }
}
