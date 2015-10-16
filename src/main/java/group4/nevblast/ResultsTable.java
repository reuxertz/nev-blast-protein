/*
 * 
 This file is part of NEVBLAST.

 NEVBLAST is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 NEVBLAST is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with NEVBLAST.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
/**
 * @ResultsTable.java This object simply create a table view of the ArrayList of
 * SequenceHits. The Class itself is an extended version of JPanel
 */
package group4.nevblast;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jzy3d.plot3d.primitives.selectable.SelectableScatter;

/**
 * @author Matthew Zygowicz - Ziggy
 */
public class ResultsTable extends JPanel {

    private final boolean DEBUG = true;
    public JTable table;
    private final JScrollPane scrollPane;
    private static DefaultTableModel tMod;
    ArrayList<SequenceHit> blastData;
    JTextPane outputWindow;
    String outputHeader;
    SelectableScatter scatter3d;
    SelectableScatter scatter2dA;
    SelectableScatter scatter2dB;
    /*
     * Sets up the default table view. The chart class will modify this view
     * after the first attempt to select / deselect points.
     */

    public ResultsTable(ArrayList<SequenceHit> returnedBLAST, int myLength, JTextPane outputW, String outputHead, SelectableScatter scat3d, SelectableScatter scat2dA, SelectableScatter scat2dB) {
        super(new GridLayout(1, 0));
        //prepare variable for text outputwindow
        blastData = returnedBLAST;
        outputWindow = outputW;
        outputHeader = outputHead;
        scatter3d = scat3d;
        scatter2dA = scat2dA;
        scatter2dB = scat2dB;

        String[] columnNames = {"Accession", "EValue",
            "Signature 1 Match", "Signature Score 1", "Signature 2 Match", "Signature 2 Score", "Sequence"};
        Object[][] tableData = new Object[myLength][7];
        for (int i = 0; i < myLength; i++) {
            tableData[i][0] = returnedBLAST.get(i).getAccession();
            //	tableData[i][1] = returnedBLAST[i].getName();
            tableData[i][1] = returnedBLAST.get(i).eValue;
            tableData[i][2] = returnedBLAST.get(i).getSigAMatch();
            tableData[i][3] = returnedBLAST.get(i).getScoreA();
            tableData[i][4] = returnedBLAST.get(i).getSigBMatch();
            tableData[i][5] = returnedBLAST.get(i).getScoreB();
            tableData[i][6] = returnedBLAST.get(i).getHitSequence();
        }
        tMod = new DefaultTableModel(tableData, columnNames);
        table = new JTable(tMod);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
       // table.setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
        sorter.setComparator(1, new BigDecimalComparator());
        sorter.setComparator(2, new DoubleComparator());
        sorter.setComparator(3, new DoubleComparator());
        table.setRowSorter(sorter);
        
        if (DEBUG) {
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    printDebugData(table);
                    //table.getS
                   
                    //int row = table.rowAtPoint(new Point(e.getX(), e.getY()));
                    //int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
                    int rowClicked = table.getSelectedRow();
                    int row = table.convertRowIndexToModel(rowClicked);
                    int col = table.getSelectedColumn();
                    if(col == 0){
                        String url = (String) table.getModel().getValueAt(row, col);
                            // Create Desktop object
                        Desktop d=Desktop.getDesktop();
                        try {
                            // Browse a URL, say google.com
                            d.browse(new URI("http://www.ncbi.nlm.nih.gov/protein/" + url));
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(ResultsTable.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Hyperlinks are not supported in your OS");
                            JOptionPane.showMessageDialog(null, "Hyperlinks are not supported in your OS");
                        } catch (IOException ex) {
                            Logger.getLogger(ResultsTable.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Hyperlinks are not supported in your OS");
                            JOptionPane.showMessageDialog(null, "Hyperlinks are not supported in your OS");
                        }
                    }
                }
            });
        }
        
        // Create the scroll pane and add the table to it.
        scrollPane = new JScrollPane(table);

        // Add the scroll pane to this panel.
        add(scrollPane);
        table.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {

        @Override
        public Component getTableCellRendererComponent(JTable table, final Object value, boolean arg2,
                boolean arg3, int arg4, int arg5) {
            final JLabel lab = new JLabel("<html><a href=\" http://www.ncbi.nlm.nih.gov/protein/" + value + "\">" + value + "</a>");
            return lab;
        }
    });
    }

    /*
     * This method allows the user to quickly change the contents of the table
     * contained in this class and update it with a new set of Sequences.
     */
    public void updateTable(ArrayList<SequenceHit> BLAST) {
        String[] columnNames = {"Accession", "EValue",
            "Signature 1 Match", "Signature Score 1", "Signature 2 Match", "Signature 2 Score", "Sequence"};
       
        table.setModel(new DefaultTableModel(columnNames, BLAST.size()));
        if (BLAST.size() > 0) {
            tMod = (DefaultTableModel) table.getModel(); // Give the table the
            // new model.
            for (int i = 0; i < BLAST.size(); i++) {
                // Add values to the table.
                tMod.setValueAt(BLAST.get(i).getAccession(), i, 0);
                //tMod.setValueAt(BLAST[i].getName(), i, 1);
                tMod.setValueAt(BLAST.get(i).geteValue(), i, 1);
                tMod.setValueAt(BLAST.get(i).getSigAMatch(), i, 2);
                tMod.setValueAt(BLAST.get(i).getScoreA(), i, 3);
                tMod.setValueAt(BLAST.get(i).getSigBMatch(), i, 5);
                tMod.setValueAt(BLAST.get(i).getScoreB(), i, 6);
                tMod.setValueAt(BLAST.get(i).getHitSequence(), i, 7);
            }
        }
    }

    private void printDebugData(JTable table) {
        javax.swing.table.TableModel model = table.getModel();

        int[] rows = table.getSelectedRows();
        String outputMessage = "";
        // if(scatter.)
        scatter3d.resetHighlighting();
        scatter2dA.resetHighlighting();
        scatter2dB.resetHighlighting();
        for (int i = 0; i < rows.length; i++) {
            int current = table.convertRowIndexToModel(rows[i]);
            scatter3d.setHighlighted(current, true);
            scatter2dA.setHighlighted(current, true);
            scatter2dB.setHighlighted(current, true);
            //     System.out.println(model.getValueAt(rows[i], 0));//get the accession number
            outputMessage += "<font color=\"" + blastData.get(current).getHexColor() + "\">";
            outputMessage += "Accession Number: " + blastData.get(current).getAccession() + "<br>";
            outputMessage += "Hit Sequence: " + blastData.get(current).getHitSequence() + "<br>";
            outputMessage += "Hit From: " + blastData.get(current).getHitFrom() + "<br>";
            outputMessage += "Hit To: " + blastData.get(current).getHitTo() + "<br>";
            outputMessage += "eValue: " + blastData.get(current).geteValue() + "<br><br>";
            outputMessage += "Sequence Score 1: " + blastData.get(current).getScoreA() + "<br>";
            outputMessage += "Sequence Score 2: " + blastData.get(current).getScoreB() + "<br><br><br>";
            outputMessage += "</font>";
        }
        outputWindow.setText("<html>" + outputHeader + outputMessage + "</html>");
        outputMessage = "";

    }
}
