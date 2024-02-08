package com.hex;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serial;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MainTable {

    private enum ToolTipMode {SELECT_ONE, SELECT_TWO, SELECT_FOUR, SELECT_EIGHT}

    private enum ShiftMode {SHIFT, NO_SHIFT}

    private JTable table;
    private JTable headerTable;
    private JPanel toolTipModePanel;
    private JPanel shiftModePanel;
    private ToolTipMode curToolTipMode;
    private ShiftMode curShiftMode;
    private int[] copyStart = null;
    private int[] copyEnd = null;

    public JTable getTable() {
        return table;
    }

    public void setTable(JTable table) {
        this.table = table;
    }

    public JTable getHeaderTable() {
        return headerTable;
    }

    public void setHeaderTable(JTable headerTable) {
        this.headerTable = headerTable;
    }

    public JPanel getToolTipModePanel() {
        return toolTipModePanel;
    }

    public void setToolTipModePanel(JPanel toolTipModePanel) {
        this.toolTipModePanel = toolTipModePanel;
    }

    public JPanel getShiftModePanel() {
        return shiftModePanel;
    }

    public void setShiftModePanel(JPanel shiftModePanel) {
        this.shiftModePanel = shiftModePanel;
    }

    public ToolTipMode getCurToolTipMode() {
        return curToolTipMode;
    }

    public void setCurToolTipMode(ToolTipMode curToolTipMode) {
        this.curToolTipMode = curToolTipMode;
    }

    public ShiftMode getCurShiftMode() {
        return curShiftMode;
    }

    public void setCurShiftMode(ShiftMode curShiftMode) {
        this.curShiftMode = curShiftMode;
    }

    public int[] getCopyStart() {
        return copyStart;
    }

    public void setCopyStart(int[] copyStart) {
        this.copyStart = copyStart;
    }

    public int[] getCopyEnd() {
        return copyEnd;
    }

    public void setCopyEnd(int[] copyEnd) {
        this.copyEnd = copyEnd;
    }

    private final RandomAccessFile raf;
    private final int n;

    public RandomAccessFile getRaf() {
        return raf;
    }

    public int getN() {
        return n;
    }

    static class CustomTableModel extends AbstractTableModel {

        private final RandomAccessFile raf;
        private final int n;

        public RandomAccessFile getRaf() {
            return raf;
        }

        public int getN() {
            return n;
        }

        public CustomTableModel(RandomAccessFile raf, int n) {
            this.raf = raf;
            this.n = n;
        }

        public int getColumnCount() {
            return n;
        }

        public int getRowCount() {
            try {
                return (int) (raf.length()/n);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String getColumnName(int col) {
            return Integer.toString(col);
        }

        public String getValueAt(int row, int col) {
            try {
                raf.seek((long) row *n + col);
            } catch (IOException e) {
                return "";
            }
            try {
                return String.format("%02X", raf.read());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            try {
                raf.seek((long) row*n + col);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                raf.write(Byte.parseByte((String) value));
            } catch (Throwable ignored) {
            }
            fireTableCellUpdated(row, col);
        }
    }

    public  MainTable(RandomAccessFile raf, int n){
        curToolTipMode = ToolTipMode.SELECT_ONE;
        curShiftMode = ShiftMode.SHIFT;
        this.raf = raf;
        this.n = n;
        CustomTableModel model = new CustomTableModel(raf, n);
        model.fireTableDataChanged();
        table = new JTable(model) {
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);
                try {
                    raf.seek((long) rowIndex*n + realColumnIndex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                if (rowIndex < 0) return "00";
                byte[] bytes;
                switch (curToolTipMode) {
                    case SELECT_ONE -> {
                        try {
                            tip = String.valueOf(raf.read());
                        } catch (Throwable ignored) {}
                    }
                    case SELECT_TWO -> {
                        bytes = new byte[2];
                        try {
                            raf.read(bytes);
                        } catch (Throwable ignored) {}
                        int iVal = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
                        tip = String.valueOf(iVal);
                    }
                    case SELECT_FOUR -> {
                        bytes = new byte[4];
                        try {
                            raf.read(bytes);
                        } catch (Throwable ignored) {}
                        float fVal = ByteBuffer.wrap(bytes).getFloat();
                        tip = String.valueOf(fVal);
                    }
                    case SELECT_EIGHT -> {
                        bytes = new byte[8];
                        try {
                            raf.read(bytes);
                        } catch (Throwable ignored) {}
                        double dVal = ByteBuffer.wrap(bytes).getDouble();
                        tip = String.valueOf(dVal);
                    }
                }
                return tip;
            }
        };
        table.setCellSelectionEnabled(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        createPopupMenu();
        createTipModePanel();
        createShiftModePanel();
        createHeaderTable();
    }

    private void createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem deleteJMI = new JMenuItem("Delete");
        deleteJMI.addActionListener(e -> {
            int rowStart = table.getSelectedRow();
            if (rowStart < 0) return;
            int rowEnd = table.getSelectionModel().getMaxSelectionIndex();
            int colStart = table.convertColumnIndexToModel(
                    table.getSelectedColumn());
            int colEnd = table.convertColumnIndexToModel(
                    table.getColumnModel().getSelectionModel().getMaxSelectionIndex());
            switch (curShiftMode) {
                case NO_SHIFT -> {
                    for (int row = rowStart; row <= rowEnd; row++) {
                        for (int col = colStart; col <= colEnd; col++) {
                            table.setValueAt(0, row, col);
                            try {
                                raf.seek((long) row*n + col);
                                raf.write((byte) 0);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }
                case SHIFT -> {
//                    for (int row = rowStart; row <= rowEnd; row++) {
//                        for (int col = colStart; col <= colEnd; col++) {
//                            try {
//                                data.get(row).remove(colStart);
//                                hexEditor.setData(data);
//                            }
//                            catch (Throwable ignored) {
//                            }
//                        }
//                    }
                }
            }
            table.repaint();
        });
        popupMenu.add(deleteJMI);

        JMenuItem copyJMI = new JMenuItem("Copy");
        copyJMI.addActionListener(e -> {
            int rowStart = table.getSelectedRow();
            if (rowStart < 0) return;
            int rowEnd = table.getSelectionModel().getMaxSelectionIndex();
            int colStart = table.convertColumnIndexToModel(
                    table.getSelectedColumn());
            int colEnd = table.convertColumnIndexToModel(
                    table.getColumnModel().getSelectionModel().getMaxSelectionIndex());
            copyStart = new int[]{rowStart, colStart};
            copyEnd = new int[]{rowEnd, colEnd};
            table.repaint();
        });
        popupMenu.add(copyJMI);

        JMenuItem pasteJMI = new JMenuItem("Paste");
        pasteJMI.addActionListener(e -> {

            int row = table.getSelectedRow();
            if (row < 0 || copyStart == null) return;
            int col = table.convertColumnIndexToModel(table.getSelectedColumn());

            switch (curShiftMode) {
                case NO_SHIFT -> {
                    int width = copyEnd[1] - copyStart[1] + 1;
                    for (int crow = copyStart[0]; crow <= copyEnd[0]; crow ++){
                        byte[] copyBuffer = new byte[width];

                        try {
                            raf.seek((long) crow*n + copyStart[1]);
                            raf.read(copyBuffer);
                            raf.seek((long) row*n + col);
                            raf.write(copyBuffer);
                            for (int i = col; i < col + width; i ++){
                                table.setValueAt(copyBuffer[i - col], row, i);
                            }
                            row += 1;
                            if (row >= table.getRowCount()){
                                break;
                            }
                        }

                        catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                case SHIFT -> {
//                    for (byte b : copyBuffer) {
//                        try {
//                            Utils.insertOne(hexEditor, b, row, col++);
//                        } catch (IOException ex) {
//                            throw new RuntimeException(ex);
//                        }
//                    }
                }
            }
            table.repaint();
        });
        popupMenu.add(pasteJMI);
        table.setComponentPopupMenu(popupMenu);
    }
    private  void createTipModePanel() {
        toolTipModePanel = new JPanel(new FlowLayout());
        final JCheckBox toolTipCB1 = new JCheckBox("ToolTipMode: BYTE");
        final JCheckBox toolTipCB2 = new JCheckBox("ToolTipMode: INT");
        final JCheckBox toolTipCB4 = new JCheckBox("ToolTipMode: FLOAT");
        final JCheckBox toolTipCB8 = new JCheckBox("ToolTipMode: DOUBLE");

        toolTipCB1.setSelected(true);

        toolTipCB1.addActionListener(e -> {
            if (toolTipCB1.isSelected()) {
                curToolTipMode = ToolTipMode.SELECT_ONE;
                toolTipCB2.setSelected(false);
                toolTipCB4.setSelected(false);
                toolTipCB8.setSelected(false);
            }
        });

        toolTipCB2.addActionListener(e -> {
            if (toolTipCB2.isSelected()) {
                curToolTipMode = ToolTipMode.SELECT_TWO;
                toolTipCB1.setSelected(false);
                toolTipCB4.setSelected(false);
                toolTipCB8.setSelected(false);
            }
        });

        toolTipCB4.addActionListener(e -> {
            if (toolTipCB4.isSelected()) {
                curToolTipMode = ToolTipMode.SELECT_FOUR;
                toolTipCB1.setSelected(false);
                toolTipCB2.setSelected(false);
                toolTipCB8.setSelected(false);
            }
        });

        toolTipCB8.addActionListener(e -> {
            if (toolTipCB8.isSelected()) {
                curToolTipMode = ToolTipMode.SELECT_EIGHT;
                toolTipCB1.setSelected(false);
                toolTipCB2.setSelected(false);
                toolTipCB4.setSelected(false);
            }
        });

        toolTipModePanel.add(toolTipCB1);
        toolTipModePanel.add(toolTipCB2);
        toolTipModePanel.add(toolTipCB4);
        toolTipModePanel.add(toolTipCB8);
    }

    private  void createShiftModePanel() {
        shiftModePanel = new JPanel(new FlowLayout());
        final JCheckBox shiftCBShift = new JCheckBox("ShiftMode: SHIFT");
        final JCheckBox shiftCBNoShift = new JCheckBox("ShiftMode: NO SHIFT");

        shiftCBShift.setSelected(true);

        shiftCBShift.addActionListener(e -> {
            if (shiftCBShift.isSelected()) {
                curShiftMode = ShiftMode.SHIFT;
                shiftCBNoShift.setSelected(false);
            }
        });

        shiftCBNoShift.addActionListener(e -> {
            if (shiftCBNoShift.isSelected()) {
                curShiftMode = ShiftMode.NO_SHIFT;
                shiftCBShift.setSelected(false);
            }
        });

        shiftModePanel.add(shiftCBShift);
        shiftModePanel.add(shiftCBNoShift);
    }



    private  void createHeaderTable() {
        final AbstractTableModel model = new AbstractTableModel() {

            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getValueAt(int row, int column) {
                return table.convertRowIndexToModel(row);
            }

            @Override
            public int getRowCount() {
                return table.getRowCount();
            }
        };

        headerTable = new JTable(model);
        headerTable.setShowGrid(false);
        headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        headerTable.setPreferredScrollableViewportSize(new Dimension(80, 0));
        headerTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        headerTable.getColumnModel().getColumn(0).setCellRenderer((x, value, isSelected, hasFocus, row, column) -> {

            boolean selected = table.getSelectionModel().isSelectedIndex(row);
            Component component = table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table, value, false, false, -1, -2);
            ((JLabel) component).setHorizontalAlignment(JLabel.CENTER);
            if (selected) {
                component.setFont(component.getFont().deriveFont(Font.BOLD));
            } else {
                component.setFont(component.getFont().deriveFont(Font.PLAIN));
            }
            return component;
        });

        final RowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        table.getRowSorter().addRowSorterListener(e -> model.fireTableDataChanged());
        table.getSelectionModel().addListSelectionListener(e -> model.fireTableRowsUpdated(0, model.getRowCount() - 1));

    }
}
