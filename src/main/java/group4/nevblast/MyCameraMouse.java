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
@MyCameraMouse.java
    * The custom mouse class was created to better handle zooming in.

   
*/

package group4.nevblast;

import java.awt.event.MouseWheelEvent;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.camera.AWTCameraMouseController;
import org.jzy3d.maths.Scale;

/**
 *
 * @author matthew Zygowicz - ziggy
 */
public class MyCameraMouse extends AWTCameraMouseController{
    Chart myChart;
    Scale scale;

    MyCameraMouse(Chart chart){
     //   scale = 1;
        super(chart);
        myChart = chart;
        
    }
    
    //add buttons to perform zooms on different axis...currently only zooms y
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        stopThreadController();
//        if(e.getWheelRotation() > 0)
//            scale = scale + .01f;
//        else
//            scale = scale - .01f;
//        System.out.println("hello zoom!");
        float factor = 1 + (e.getWheelRotation() / 500.0f);
   //     System.out.println(factor);

//        myChart.getView().zoomZ(factor);
//        myChart.getView().zoomX(factor);
//        myChart.getView().zoomY(factor);
        myChart.getView().zoom(factor);
   
  //      System.out.println( myChart.getScale().toString() );
    }
    
    
    
}
