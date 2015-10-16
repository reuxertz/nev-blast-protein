/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package group4.nevblast;

import java.awt.Color;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 *
 * @author ziggy
 */
public class EntrezQuerySelectUI extends BasicComboBoxUI {

    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton() {
                    @Override
                    public int getWidth() {
                        return 0;
                    }
                };
        
        
        button.setBackground(Color.WHITE);
//        button.setVisible(false);
        return button;
//        return super.createArrowButton(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ComboBoxEditor createEditor() {
        return super.createEditor(); //To change body of generated methods, choose Tools | Templates.
    }

    
    @Override
    public void configureArrowButton() {
        super.configureArrowButton(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
