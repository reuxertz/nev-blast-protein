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
 * @MyScatterSelector.java This custom mouse selector class was built for our
 * need. Currently it only allows for selection box selecting This custom mouse
 * selector was built to allow the text to be displayed on a window. This is
 * currently working.
 *
 *
 */
package group4.nevblast;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JTextPane;
import org.jzy3d.chart.controllers.mouse.selection.AWTScatterMouseSelector;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.IntegerCoord2d;
import org.jzy3d.plot3d.primitives.selectable.SelectableScatter;
import org.jzy3d.plot3d.rendering.scene.Scene;
import org.jzy3d.plot3d.rendering.view.View;

/**
 * @author Matthew Zygowicz - Ziggy
 */
public class MyScatterSelector extends AWTScatterMouseSelector {

    float prevX;
    float prevY;
    String outputMessage;
    JTextPane outputWindow;
    ArrayList<SequenceHit> data;
    String outputHeader;
    ResultsTable tableView;
    SelectableScatter otherScatter1;
    SelectableScatter otherScatter2;
    boolean disabled;

    MyScatterSelector(SelectableScatter scat, JTextPane output, ArrayList<SequenceHit> blastData, String outputHead,ResultsTable rt,SelectableScatter s1,SelectableScatter s2) {
        super(scat);
        outputWindow = output;
        outputMessage = "";
        outputHeader = outputHead;
        data = blastData;
        tableView = rt;
        otherScatter1 = s1;
        otherScatter2 = s2;
        disabled = false;
    }

    protected void processSelection(Scene scene, View view, int width, int height) {
        scatter.resetHighlighting();
        otherScatter1.resetHighlighting();
        otherScatter2.resetHighlighting();
        tableView.table.clearSelection();
        view.project();
        Coord3d[] projection = scatter.getProjection();
        Coord3d[] dat = scatter.getData();
                
        for (int i = 0; i < projection.length; i++) {
            if(!disabled)
                if (matchRectangleSelection(in, out, projection[i], width, height)) {
                    scatter.setHighlighted(i, true);
                    otherScatter1.setHighlighted(i, true);
                    otherScatter2.setHighlighted(i, true);
                    outputMessage += "<font color=\"" + data.get(i).getHexColor() + "\">";
                    outputMessage += "Accession Number: " + data.get(i).getAccession() + "<br>";
                    outputMessage += "Hit Sequence: " + data.get(i).getHitSequence() + "<br>";
                    outputMessage += "Hit From: " + data.get(i).getHitFrom() + "<br>";
                    outputMessage += "Hit To: " + data.get(i).getHitTo() + "<br>";
                    outputMessage += "eValue: " + data.get(i).geteValue() + "<br><br>";
                    outputMessage += "Sequence 1 Match: " + data.get(i).getSigAMatch() + "<br>";
                    outputMessage += "Sequence 1 Score: " + data.get(i).getScoreA() + "<br>";
                    outputMessage += "Sequence 2 Match: " + data.get(i).getSigBMatch() + "<br>";
                    outputMessage += "Sequence 2 Score: " + data.get(i).getScoreB()+ "<br><br><br>";
                    outputMessage += "</font>";
                    int ndx= tableView.table.convertRowIndexToModel(i);
                    tableView.table.addRowSelectionInterval(ndx, ndx);
                }
        } 
        
        outputWindow.setText("<html>" + outputHeader + outputMessage + "</html>");
        outputMessage = "";


    }
    
    @Override
    protected void drawSelection(Graphics2D g2d, int width, int height) {
            this.width = width;
            this.height = height;

            if (dragging && !disabled)
                    drawRectangle(g2d, in, out);
    }
    
    public void setDisabled(boolean status){
        disabled = status;
    }
    public void mouseClicked(MouseEvent e) {    
     
        scatter.resetHighlighting();
        tableView.table.clearSelection();
  
        Coord3d[] projection = scatter.getProjection();
        Coord3d[] dat = scatter.getData();
        IntegerCoord2d tempIn = new IntegerCoord2d();
        IntegerCoord2d tempOut = new IntegerCoord2d();
        tempIn.x = e.getX() - 4;
        tempIn.y = e.getY() - 4;
        tempOut.x = e.getX() + 4;
        tempOut.y = e.getY() + 4;
        
        for (int i = 0; i < projection.length; i++) {
            if (matchRectangleSelection(tempIn, tempOut, projection[i], width, height)) {
                scatter.setHighlighted(i, true);
                outputMessage += "<font color=\"" + data.get(i).getHexColor() + "\">";
                outputMessage += "Accession Number: " + data.get(i).getAccession() + "<br>";
                outputMessage += "Hit Sequence: " + data.get(i).getHitSequence() + "<br>";
                outputMessage += "Hit From: " + data.get(i).getHitFrom() + "<br>";
                outputMessage += "Hit To: " + data.get(i).getHitTo() + "<br>";
                outputMessage += "eValue: " + data.get(i).geteValue() + "<br><br>";
                outputMessage += "Sequence 1 Match: " + data.get(i).getSigAMatch() + "<br>";
                outputMessage += "Sequence 1 Score: " + data.get(i).getScoreA() + "<br>";
                outputMessage += "Sequence 2 Match: " + data.get(i).getSigBMatch() + "<br>";
                outputMessage += "Sequence 2 Score: " + data.get(i).getScoreB()+ "<br><br><br>";
                outputMessage += "</font>";
                int ndx= tableView.table.convertRowIndexToModel(i);
                tableView.table.addRowSelectionInterval(ndx, ndx);
                //tableView.table.addRowSelectionInterval(i, i);
                //tableView.table.set
            }
        } 

        outputWindow.setText("<html>" + outputHeader + outputMessage + "</html>");
        outputMessage = "";
     }
     
}
