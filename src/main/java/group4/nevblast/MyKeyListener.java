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
 * @MyKeyListener.java This custom Key listener was created to be used inside
 * the MyDualModeMouse. This keylistener listens for key events every 100ms.
 * This is done to fix a OS problem that caused the 3d graph to lag. The problem
 * was that when the key was being held the os registered this as multiple key
 * presses every millisecond causing lag and very bad behavior.
 *
 *
 */
package group4.nevblast;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import javax.swing.Timer;
import org.jzy3d.chart.Chart;

/**
 *
 * @author Matthew Zygowicz - ziggy
 */
public class MyKeyListener implements KeyListener {

    public static String MESSAGE_SELECTION_MODE = "Current mouse mode: selection (hold 'c' to switch to camera mode)";
    public static String MESSAGE_ROTATION_MODE = "Current mouse mode: camera (release 'c' to switch to selection mode)";
    protected static boolean camera = false;
    protected static boolean holding = false;
    private boolean firstRun;
    private boolean initialized;
    /**
     * Stores currently pressed keys
     */
    HashSet<Character> pressedKeys = new HashSet<Character>();
    Chart chart;
    MyDualModeMouse mouse;

    MyKeyListener(Chart chart1, MyDualModeMouse mouse1) {
        this.chart = chart1;
        this.mouse = mouse1;
        firstRun = true;
        initialized = true;
        new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (!pressedKeys.isEmpty()) {
                    for (char c : pressedKeys) {

                        if (c == 'C' || c == 'c') {
                            holding = true;
                        }
                    }
                } else {
                    holding = false;
                }

                if (holding) {
                    if (firstRun) {
                        mouse.setMessage(MESSAGE_ROTATION_MODE);
                        mouse.useCam();
                        mouse.setMessage(MESSAGE_ROTATION_MODE);
                        mouse.disableSelection();
                    }
                    mouse.getMouseSelect().clearLastSelection();
                    holding = true;
                    firstRun = false;
                    initialized = true;

                } else {
                    if (initialized) {
                        mouse.releaseCam();
                        mouse.setMessage(MESSAGE_SELECTION_MODE);
                        mouse.enableSelection();

                    }
                    holding = false;
                    initialized = false;
                    firstRun = true;

                }
                chart.render(); // update message display
            }

        }).start();
    }

    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyChar());
    }

    public void keyTyped(KeyEvent e) {
        //  throw new UnsupportedOperationException("Not supported yet");
    }

    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyChar());
        //  throw new UnsupportedOperationException("Not supported yet");
    }

}
