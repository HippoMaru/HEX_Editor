package hex.gui;

import hex.HEXEditor;
import hex.Utils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Serial;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.OptionalInt;

public class MainTable {

    private enum ToolTipMode {SELECT_ONE, SELECT_TWO, SELECT_FOUR, SELECT_EIGHT}
    private enum ShiftMode {SHIFT, NO_SHIFT}
    private final JTable table;
    private final JTable headerTable;
    private final JPopupMenu popupMenu;
    private final JPanel toolTipModePanel;
    private final JPanel shiftModePanel;
    private final ArrayList<ArrayList<Byte>> data;
    private ToolTipMode curToolTipMode;
    private ShiftMode curShiftMode;
    private byte[] copyBuffer;
    public MainTable(ArrayList<ArrayList<Byte>> data){
        this.data = data;
        copyBuffer = new byte[]{};
        curToolTipMode = ToolTipMode.SELECT_ONE;
        curShiftMode = ShiftMode.SHIFT;

        table = createTable();
        popupMenu = createPopupMenu();
        toolTipModePanel = createTipModePanel();
        shiftModePanel = createShiftModePanel();
        headerTable = createHeaderTable();
    }

    static class CustomTableModel extends AbstractTableModel {

        private final ArrayList<ArrayList<Byte>> data;

        public CustomTableModel(ArrayList<ArrayList<Byte>> data){
            this.data = data;
        }

        public int getColumnCount() {
            OptionalInt result = data.stream().mapToInt(ArrayList::size).max();
            return result.isPresent() ? result.getAsInt() : 0;
        }

        public int getRowCount() {
            return data.size();
        }

        public String getColumnName(int col) {
            return Integer.toString(col);
        }

        public String getValueAt(int row, int col) {
            if (data.get(row).size() <= col) return "00";
            return String.format("%02X", data.get(row).get(col));
        }

        public boolean isCellEditable(int row, int col)
        { return col != 0; }
        public void setValueAt(Object value, int row, int col) {
            while (data.get(row).size() <= col){
                data.get(row).add((byte) 0);
            }
            try {data.get(row).set(col, Byte.valueOf((String) value));}
            catch (Throwable ignored){}
            fireTableCellUpdated(row, col);
        }
    }

    private JTable createTable(){
        JTable table = new JTable(new CustomTableModel(data)){
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);

                if (rowIndex < 0) return "00";
                byte[] bytes;
                switch (curToolTipMode) {
                    case SELECT_ONE -> {
                        if (data.get(rowIndex).size() <= colIndex) tip = "00";
                        else tip = String.valueOf(data.get(rowIndex).get(realColumnIndex));
                    }
                    case SELECT_TWO -> {
                        bytes = new byte[2];
                        for (int i = 0; i < 2; i++) {
                            if (realColumnIndex + i < data.get(rowIndex).size())
                                bytes[i] = data.get(rowIndex).get(realColumnIndex + i);
                        }
                        int iVal = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
                        tip = String.valueOf(iVal);
                    }
                    case SELECT_FOUR -> {
                        bytes = new byte[4];
                        for (int i = 0; i < 4; i++) {
                            if (realColumnIndex + i < data.get(rowIndex).size())
                                bytes[i] = data.get(rowIndex).get(realColumnIndex + i);
                        }
                        float fVal = ByteBuffer.wrap(bytes).getFloat();
                        tip = String.valueOf(fVal);
                    }
                    case SELECT_EIGHT -> {
                        bytes = new byte[8];
                        for (int i = 0; i < 8; i++) {
                            if (realColumnIndex + i < data.get(rowIndex).size())
                                bytes[i] = data.get(rowIndex).get(realColumnIndex + i);
                        }
                        double dVal = ByteBuffer.wrap(bytes).getDouble();
                        tip = String.valueOf(dVal);
                    }
                }
                return tip;
            }
        };
        table.setCellSelectionEnabled(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        return table;
    }
    public JTable getTable() {
        return table;
    }

    private JPopupMenu createPopupMenu(){
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
            switch (curShiftMode){
                case NO_SHIFT -> {
                    for (int row=rowStart; row<=rowEnd; row++) {
                        for (int col=colStart; col<=colEnd; col++) {
                            try {
                                Utils.deleteOne(row, col, data);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }
                case SHIFT -> {
                    for (int row=rowStart; row<=rowEnd; row++) {
                        for (int col=colStart; col<=colEnd; col++) {
                            try {
                                data.get(row).remove(colStart);
                            }
                            catch (Throwable ignored){}
                        }
                    }
                }
            }
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
            copyBuffer = new byte[(rowEnd - rowStart + 1) * (colEnd - colStart + 1)];
            for (int row=rowStart; row<=rowEnd; row++) {
                for (int col=colStart; col<=colEnd; col++) {
                    try {
                        copyBuffer[(row-rowStart)*(colEnd-colStart + 1) + col-colStart] = data.get(row).get(col);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
        popupMenu.add(copyJMI);

        JMenuItem pasteJMI = new JMenuItem("Paste");
        pasteJMI.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || copyBuffer.length == 0) return;
            int col = table.convertColumnIndexToModel(table.getSelectedColumn());
            switch (curShiftMode){
                case NO_SHIFT -> {
                    for (byte b : copyBuffer) {
                        if(data.get(row).size() <= col){data.get(row).add(b);}
                        else{data.get(row).set(col, b);}
                        col++;
                    }
                }
                case SHIFT -> {
                    for (byte b : copyBuffer) {
                        try {
                            Utils.insertOne(b, row, col++, data);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        });
        popupMenu.add(pasteJMI);
        table.setComponentPopupMenu(popupMenu);
        return popupMenu;
    }

    public JPopupMenu getPopupMenu(){ return popupMenu; }

    private JPanel createTipModePanel(){
        JPanel toolTipModePanel = new JPanel(new FlowLayout());
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

        return toolTipModePanel;
    }

    public JPanel getToolTipModePanel() {
        return toolTipModePanel;
    }

    private JPanel createShiftModePanel(){
        JPanel shiftModePanel = new JPanel(new FlowLayout());
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

        return shiftModePanel;
    }

    public JPanel getShiftModePanel(){
        return shiftModePanel;
    }

    private JTable createHeaderTable(){
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

        JTable headerTable = new JTable(model);
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

        return headerTable;
    }

    public JTable getHeaderTable() {
        return headerTable;
    }
}
