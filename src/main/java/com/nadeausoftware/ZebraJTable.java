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
 * This class is copyright Dr David R. Nadeau, Nadeau Software Consulting.
 */
// CHECKSTYLE:OFF
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
     * @param rowData
     * @param columnNames
     */
    public ZebraJTable(java.util.Vector<?> rowData,
            java.util.Vector<?> columnNames) {
        super(rowData, columnNames);
    }

    /**
     * Add stripes between cells and behind non-opaque cells.
     * {@inheritDoc}
     */
    @Override
    public void paintComponent(java.awt.Graphics g) {
        if (!(drawStripes = isOpaque())) {
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
            javax.swing.table.TableCellRenderer renderer,
            int row,
            int col) {
        final java.awt.Component c = super.prepareRenderer(renderer, row, col);
        if (drawStripes && !isCellSelected(row, col))
            c.setBackground(rowColors[row & 1]);
        return c;
    }

    /**
     * Add background stripes behind edited cells.
     * {@inheritDoc}
     */
    @Override
    public java.awt.Component prepareEditor(
            javax.swing.table.TableCellEditor editor,
            int row,
            int col) {
        final java.awt.Component c = super.prepareEditor(editor, row, col);
        if (drawStripes && !isCellSelected(row, col))
            c.setBackground(rowColors[row & 1]);
        return c;
    }

    /**
     * Force the table to fill the viewport's height.
     * {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        final java.awt.Component p = getParent();
        if (!(p instanceof javax.swing.JViewport))
            return false;
        return ((javax.swing.JViewport) p).getHeight() > getPreferredSize().height;
    }

    /**
     * Compute zebra background stripe colors.
     */
    private void updateZebraColors() {
        if ((rowColors[0] = getBackground()) == null) {
            rowColors[0] = rowColors[1] = java.awt.Color.white;
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
// CHECKSTYLE: ON