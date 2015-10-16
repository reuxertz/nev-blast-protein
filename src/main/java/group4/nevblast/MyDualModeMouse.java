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
 * @MyDualModeMouse.java This custom mouse mode switcher was created to fit the
 * special needs of the nevblast application. It uses the MyScatterSelector and
 * MyCameraMouse. In addition to simply using those classes it passes them
 * custom variables, and has the ability to disable the selector.
 *
 *
 */
package group4.nevblast;

import java.awt.Graphics;
import java.awt.event.KeyListener;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.AWTDualModeMouseSelector;
import org.jzy3d.chart.controllers.mouse.selection.AWTAbstractMouseSelector;
import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
import org.jzy3d.plot3d.rendering.view.Renderer2d;

/**
 *
 * @author Matthew Zygowicz - ziggy
 */
public class MyDualModeMouse extends AWTDualModeMouseSelector {

    String chartType;
    boolean disabled;
    boolean disabledSelect;

    public MyDualModeMouse(Chart chart, AWTAbstractMouseSelector alternativeMouse, String chartType) {
        super(chart, alternativeMouse);
        this.chartType = chartType;
        disabled = false;
        disabledSelect = false;

    }

    @Override
    public Chart build(final Chart chart, AWTAbstractMouseSelector alternativeMouse) {
        this.chart = chart;
        this.mouseSelection = alternativeMouse;

        // Create and add controllers
        threadCamera = new CameraThreadController(chart);
        mouseCamera = new MyCameraMouse(chart);
        mouseCamera.addSlaveThreadController(threadCamera);
        chart.getCanvas().addKeyController(buildToggleKeyListener(chart));
        releaseCam(); // default mode is selection

        messageRenderer = buildMessageRenderer();
        getAWTChart(chart).addRenderer(messageRenderer);

        return chart;
    }

    private AWTChart getAWTChart(final Chart chart) {
        return (AWTChart) chart;
    }

    @Override
    protected void useCam() {
        mouseSelection.unregister();
        chart.addController(mouseCamera);
    }

    @Override
    protected void releaseCam() {
        chart.removeController(mouseCamera);
        mouseSelection.register(chart);
    }

    public void disableSelection() {
        ((MyScatterSelector) mouseSelection).setDisabled(true);
    }

    public void enableSelection() {
        ((MyScatterSelector) mouseSelection).setDisabled(false);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String newMessage) {
        message = newMessage;
    }

    public AWTAbstractMouseSelector getMouseSelect() {
        return mouseSelection;
    }

    public void setMouseSelect(AWTAbstractMouseSelector select) {
        mouseSelection = select;
    }

    @Override
    public KeyListener buildToggleKeyListener(final Chart chart) {

        MyKeyListener keyListener = new MyKeyListener(chart, this);
        return keyListener;

    }

    @Override
    public Renderer2d buildMessageRenderer() {
        return new Renderer2d() {
            public void paint(Graphics g) {
                if (displayMessage && message != null) {
                    g.setColor(java.awt.Color.RED);
                    g.drawString(message, 10, 30);
                }
            }
        };

    }

}
