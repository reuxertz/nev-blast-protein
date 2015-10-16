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
 * @Grapher.java This class is the object that handles the drawing of the
 * graphs. This is done by creating a series of points and colors from each of
 * the sequenceHits (the sequence hits are scaled to ensure they fit smoothly on
 * the graph) The program builds 2 AWT mouse classes. It builds
 * MyScatterSelector which is a custom built AWTScatterMouseSelector to select
 * points and uses AWTDualModeMouseSelector which attaches MyScatterSelector.
 *
 */
package group4.nevblast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import javax.swing.JTextPane;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.selection.AWTAbstractMouseSelector;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory.Toolkit;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.selectable.SelectableScatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;

public class Grapher extends AbstractAnalysis {

    ArrayList<SequenceHit> data; //3d  
    ArrayList<SequenceHit> data2D; //2d
    SelectableScatter scatter; //3d
    SelectableScatter scatter2dA; //2d
    SelectableScatter scatter2dB; //2d
    JTextPane outputWindow;
    String outputHeader;
    ResultsTable tableView;
    String chartType;
    SelectableScatter otherScatter1;
    SelectableScatter otherScatter2;
    boolean needColors;
    Color[] definedColors;
    boolean isGraphScaled;

    Grapher(ArrayList<SequenceHit> toGraph, JTextPane output, String outputHead, String chartType) throws IOException {
        data = toGraph;
        outputWindow = output;
        outputHeader = outputHead;
        this.chartType = chartType;
        needColors = true;
        //  init();
    }

    Grapher(ArrayList<SequenceHit> toGraph, JTextPane output, String outputHead, String chartType, Color[] cols) throws IOException {
        data = toGraph;
        outputWindow = output;
        outputHeader = outputHead;
        this.chartType = chartType;
        definedColors = cols;
        needColors = false;
        //  init();
    }

    /*
     * Scale SigScore sigscore/max = <= 1
     */
    public void init() throws IOException {

        int size = data.size();
        float x;
        float y;
        float z;
        float a;

        //boolean predefinedColors = tr;
        //BigDecimalUtils
        if (needColors) {
            //  predefinedColors = true;
            definedColors = new Color[size];
        }

        String finalFileNameOne = BlastQuery.queryNameCompute;
        Coord3d[] points = new Coord3d[size]; //holds 3d plotting data
        Coord3d[] points2dA = new Coord3d[size]; //holds 2d plotting data
        Coord3d[] points2dB = new Coord3d[size]; //holds 2d plotting data
        Color[] colors = new Color[size]; //would make a rainbow if we wanted it to BUT WE DON'T 
        FileWriter fw;
        fw = new FileWriter(new File(finalFileNameOne));

        BufferedWriter writer = new BufferedWriter(fw);
        writer.write("Gene Product, Accession Number, eValue, Signature Score 1, Signature Score 2, Signature Score 1 Match, Signature Score 2 Match, Hit Sequence\n");
        writer.flush();
        //double[] scales = constructScales(data);
        ArrayList<SequenceHit> d = data;
        //double scales[] = constructScales(d);

        // Scale the values to 0-100 and add it to the set of points.
        for (int i = 0; i < points.length; i++) {
//            //get xyz and assign them to the SequenceHits
//            x = Float.valueOf(
//                    d.get(i).getScoreA()) * (float) scales[0];
//            y = Float.valueOf(
//                    d.get(i).getScoreB()) * (float) scales[1];
//            z = Float.valueOf(
//                    d.get(i).geteValue()) * (float) scales[2];
            if (isGraphScaled) {
                x = (float) d.get(i).getNormalizedScoreA();
                y = (float) d.get(i).getNormalizedScoreB();
                z = getEV(new BigDecimal(d.get(i).geteValue()));
            } else {
                x = Float.valueOf(
                        d.get(i).getScoreA());
                y = Float.valueOf(
                        d.get(i).getScoreB());
                z = Float.valueOf(
                        d.get(i).geteValue());
            }

            data.get(i).setX_3d(x);
            data.get(i).setY_3d(y);
            data.get(i).setZ_3d(z);

//            String hitSequenceNoHyphens = "";
//            
//            for (int k = 0; k < data.get(i).getHitSequence().length(); k++) {
//                if (data.get(i).getHitSequence().charAt(i) == '-') {
//                } else {
//                    hitSequenceNoHyphens += hitSequenceNoHyphens + data.get(i).getHitSequence().charAt(i);
//                }
//            }

            points[i] = new Coord3d(x, y, z);
            points2dA[i] = new Coord3d(x, z, 0); //The Z score is ignored in the 2d graph 
            points2dB[i] = new Coord3d(y, z, 0); //The Z score is ignored in the 2d graph 
            if (!needColors) {
                colors[i] = definedColors[i];
            } else {
                colors[i] = Color.random();
                definedColors[i] = colors[i];
            }
            data.get(i).setColor(colors[i]);

            writer.write("\"" + data.get(i).getHitDef() + "\",\"" + data.get(i).getAccession() + "\",\"" + z + "\",\"" + x + "\",\"" + y + "\",\"" + d.get(i).getSigAMatch() + "\",\"" + d.get(i).getSigBMatch() + "\",\"" + data.get(i).getHitSequence().replaceAll("-", "") + "\"\n");   //write file
            writer.flush();
        }

        writer.close();
        AWTChartComponentFactory s = new AWTChartComponentFactory();
        //  Toolkit.awt;

        scatter = new SelectableScatter(points, colors);
        scatter2dA = new SelectableScatter(points2dA, colors);
        scatter2dB = new SelectableScatter(points2dB, colors);

        scatter.setWidth(10);
        scatter2dA.setWidth(5);
        scatter2dB.setWidth(5);

        scatter.setHighlightColor(Color.BLUE);
        scatter2dA.setHighlightColor(Color.BLUE);
        scatter2dB.setHighlightColor(Color.BLUE);
        /// scatter2d.s
        //     scatter.
        chart = s.newChart(Quality.Advanced, Toolkit.awt);

        /*
         * Sets up the properties for each chart type (labeling each axis)
         */
        if (chartType.equals("3dChart")) {
            chart.getScene().add(scatter);
            chart.getAxeLayout().setXAxeLabel("Signature Score 1");
            chart.getAxeLayout().setYAxeLabel("Signature Score 2");
            chart.getAxeLayout().setZAxeLabel("eValue");
            //   chart.setViewMode(ViewPositionMode.TOP);
            //       chart.setAxeDisplayed(true);
            chart.getAxeLayout().setFaceDisplayed(true);
        } else if (chartType.equals("2dChartA")) {
            chart.getAxeLayout().setXAxeLabel("Signature Score 1");
            chart.getAxeLayout().setYAxeLabel("eValue");
            chart.getScene().add(scatter2dA);
            chart.setViewMode(ViewPositionMode.TOP);

        } else {//else 2dChartn
            chart.getAxeLayout().setXAxeLabel("Signature Score 2");
            chart.getAxeLayout().setYAxeLabel("eValue");
            chart.getScene().add(scatter2dB);
            chart.setViewMode(ViewPositionMode.TOP);
        }
    }

    /*
     * Handles scaling options in the following two methods
     */
    public boolean isIsGraphScaled() {
        return isGraphScaled;
    }

    public void setIsGraphScaled(boolean isGraphScaled) {
        this.isGraphScaled = isGraphScaled;
    }

    public void attachResultTable(ResultsTable rt) {
        tableView = rt;
    }

    public void attachMouse() {
        AWTAbstractMouseSelector mouseSelection;

        if (chartType.equals("3dChart")) {
            mouseSelection = new MyScatterSelector(scatter, outputWindow, data, outputHeader, tableView, otherScatter1, otherScatter2);
            MyDualModeMouse mouse = new MyDualModeMouse(chart, mouseSelection, chartType);
            mouse.build(chart, mouseSelection);
            mouse.buildMessageRenderer();

        } else if (chartType.equals("2dChartA")) {
            mouseSelection = new MyScatterSelector(scatter2dA, outputWindow, data, outputHeader, tableView, otherScatter1, otherScatter2);
            mouseSelection.register(chart);

        } else {
            mouseSelection = new MyScatterSelector(scatter2dB, outputWindow, data, outputHeader, tableView, otherScatter1, otherScatter2);
            mouseSelection.register(chart);
        }
    }

    public void attachScatters(SelectableScatter scat1, SelectableScatter scat2) {
        otherScatter1 = scat1;
        otherScatter2 = scat2;
    }

    public Chart getChart() {
        return chart;
    }

    public Color[] getDefinedColors() {
        return definedColors;
    }

    public float getEV(BigDecimal evalue) {
        float ret = 0;
        BigDecimal big = log10(evalue, 201);
        ret = Math.abs(big.floatValue());
        return ret;
    }

    //http://everything2.com/index.pl?node_id=946812        
    public BigDecimal log10(BigDecimal b, int dp) {
        final int NUM_OF_DIGITS = dp + 2; // need to add one to get the right number of dp
        //  and then add one again to get the next number
        //  so I can round it correctly.

        MathContext mc = new MathContext(NUM_OF_DIGITS, RoundingMode.HALF_EVEN);

        //special conditions:
        // log(-x) -> exception
        // log(1) == 0 exactly;
        // log of a number lessthan one = -log(1/x)

        if (b.signum() <= 0) {
            return new BigDecimal(-200);
        } else if (b.compareTo(BigDecimal.ONE) == 0) {
            return BigDecimal.ZERO;
        } else if (b.compareTo(BigDecimal.ONE) < 0) {
            return (log10((BigDecimal.ONE).divide(b, mc), dp)).negate();
        }

        StringBuffer sb = new StringBuffer();
        //number of digits on the left of the decimal point
        int leftDigits = b.precision() - b.scale();

        //so, the first digits of the log10 are:
        sb.append(leftDigits - 1).append(".");

        //this is the algorithm outlined in the webpage
        int n = 0;
        while (n < NUM_OF_DIGITS) {
            b = (b.movePointLeft(leftDigits - 1)).pow(10, mc);
            leftDigits = b.precision() - b.scale();
            sb.append(leftDigits - 1);
            n++;
        }

        BigDecimal ans = new BigDecimal(sb.toString());
        //Round the number to the correct number of decimal places.
        ans = ans.round(new MathContext(ans.precision() - ans.scale() + dp, RoundingMode.HALF_EVEN));
        return ans;
    }

    //this function scales all of the points to make for easier plotting
    public double[] constructScales(ArrayList<SequenceHit> s) {
        // Check each index of Sequence and update the x, y, and z Max as needed.
        double xMax = 0.0f, yMax = 0.0f, zMax = 0.0f;
        for (int i = 0; i < s.size(); i++) {
            if (xMax < Double.valueOf(
                    s.get(i).getScoreA())) {
                xMax = Double.valueOf(
                        s.get(i).getScoreA());
            }
            if (yMax < Double.valueOf(
                    s.get(i).getScoreB())) {
                yMax = Double.valueOf(
                        s.get(i).getScoreB());
            }
            if (zMax < Double.valueOf(
                    s.get(i).geteValue())) {
                zMax = Double.valueOf(
                        s.get(i).geteValue());
            }
        }

        // Check each index of Sequence and update the x, y, and z Min as needed.
        double xMin = xMax, yMin = yMax, zMin = zMax;
        for (int i = 0; i < s.size(); i++) {
            if (xMin > Double.valueOf(
                    s.get(i).getScoreA())) {
                xMin = (float) Float.valueOf(
                        s.get(i).getScoreA());
            }
            if (yMin > Double.valueOf(
                    s.get(i).getScoreB())) {
                yMin = (float) Float.valueOf(
                        s.get(i).getScoreB());
            }
            if (zMin > Double.valueOf(
                    s.get(i).geteValue())) {
                zMin = (float) Float.valueOf(
                        s.get(i).geteValue());
            }
        }
        
        //OldRange = (OldMax - OldMin)  
        //NewRange = (NewMax - NewMin)  
        //NewValue = (((OldValue - OldMin) * NewRange) / OldRange) + NewMin
        // Return an array of doubles containing values which will scale all x,y, and z values to values of 0 - 100.
        return new double[]{(100.0f / (xMax - xMin)), (100.0f / (yMax - yMin)), (100.0f / (zMax - zMin))};
    }
}