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
 * @NevBlastGui.java This class is the main brain behind NEVBLAST, this is the
 * program that calls all of the moving pieces. When the Submit Button is
 * clicked the method btnSubmitAction is called which then feeds all the input
 * to the method sanitizeInput to be sanitized. Inside sanitizeInput the
 * signature sequences are verified and created (see section for logic). After
 * sanitizeInput it is returned to btnSubmitAction where it calls the class
 * BlastQuery to query BLAST. The output returned from BlastQuery is then send
 * to the method CreateOutput. Here the final screen is prepared, by creating
 * the three windows: ResultsTable,Graph,TextPane.
 *
 * The menu system is built inside the constructor by calling makeMenu
 */

package group4.nevblast;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.io.FastaReaderHelper;

public class NevBlastGui extends MasterProgram {

    //declares instance variables
    public static String queryNameFinal;
    public int width;
    public int height;
    public static ArrayList<String> numberSig1 = new ArrayList<String>();
    public static ArrayList<String> numberSig2 = new ArrayList<String>();
    public boolean isGraphScaled;
    public EventList taxonomy = new BasicEventList();
    AutoCompleteSupport autoComplete;
    public int clickCheck = 0;
    public int checkInput = 0;

    /**
     * Creates new form NevBlastGui - defaults filled in
     */
    public NevBlastGui() throws IOException {
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent evt) {
                destroyGui();
            }
        });
        
        count++;
        makeMenu();
        initComponents();
        //adds the auto-complete functionality to the entrez query text field
        taxonomy.add("");

        
        autoComplete = AutoCompleteSupport.install(txt_entrezQuery, taxonomy);
        txt_entrezQuery.setEditable(true);
        txt_entrezQuery.setSelectedIndex(0);
//        txt_entrezQuery.removeAllItems();
        txt_entrezQuery.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
        
            //implements the auto-complete functionality 
            @Override
            public void keyReleased(KeyEvent event) {

                if (((JTextComponent) ((JComboBox) ((Component) event.getSource()).getParent()).getEditor().getEditorComponent()).getText().length() == 3) {
                    try {
                        EventList tax = getTaxonomy(((JTextComponent) ((JComboBox) ((Component) event.getSource()).getParent()).getEditor().getEditorComponent()).getText());
                        for (Object t : taxonomy) {
                            taxonomy.remove(t);
                        }
                        for (Object t : tax) {
                            taxonomy.add(t);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(NevBlastGui.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        
        //The following code makes the EntrezQuery select box look like a normal text box
        txt_entrezQuery.setUI(new EntrezQuerySelectUI());
        txt_entrezQuery.remove(txt_entrezQuery.getComponent(0));
        
        //initial entered values and states of the GUI components
        jLabel10.setVisible(false);
        txt_eValue.setText(".1");
        txt_fastaSequence.setText(">gi|19918470|gb|AAM07687.1| efflux system transcriptional regulator, ArsR family [Methanosarcina acetivorans C2A]\n"
                + "MQEKCDRVNPEQIENLLQKVPDPEYITRMSAVFQALQSDTRLKILFLLRQKEMCVCELEQALEVTQSAVS\n"
                + "HGLRTLRQLDLVRVRREGKFTVYYIADEHVRTLIEMCLEHVEEKI");
        txt_signature1.setText("S 67 S 70 H 71 L 76 Y 93");
        txt_signature2.setText("C 5 C 54 C56");
        txt_numberOfResults.setText("1000");
        txt_queryName.setText("Test Query");

        submitButton.setOpaque(false);
        submitButton.setContentAreaFilled(false);
        submitButton.setBorderPainted(false);
        submitButton.setText("");

        clearButton.setOpaque(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setBorderPainted(false);
        clearButton.setText("");

        backgroundLabel.setSize(getWidth(), getHeight());
        clearClicked.setVisible(false);
        submitClicked.setVisible(false);
        
        //txt_entrezQuery.setVisible(false);
        //lbl_entrezQuery.setVisible(false);
    }

    //method to retrieve the taxonomy file and fill an EventList with its contents
    private EventList getTaxonomy(String search) throws FileNotFoundException, IOException {
        File file = new File("alphaTax.txt");

        BufferedReader br = new BufferedReader(new FileReader(file));
        EventList tax = new BasicEventList();

        String line;
        boolean found = false;
        boolean globalFound = false;
        while ((line = br.readLine()) != null) {
            if (line.startsWith(search)) {

                tax.add(line);

                found = true;
                globalFound = true;
            } else {
                found = false;
            }
            if (globalFound && !found) {
                break;
            }

        }
        br.close();
        return tax;
    }

    /*
     * method to add the menubar functionality to the GUI
     */
    private void makeMenu() {
        JMenuBar menubar = new JMenuBar();
        JMenu file = makeMenuFile();
        JMenu matrix = makeMenuMatrix();
        JMenu database = makeMenuDatabase();
        JMenu program = makeMenuProgram();
        JMenu graph = makeMenuGraph();

        menubar.add(file);
        menubar.add(matrix);
        //menubar.add(program);
        menubar.add(database);
        menubar.add(graph);

        setJMenuBar(menubar);

        //setdefaults for menus
        globalMatrix = "blosum62";
        globalBlastDatabase = "nr";
        globalBlastProgram = "blastp";
        isGraphScaled = false;
    }

    /*
     * sets the default value types for the BLAST query
     */
    private JMenu makeMenuProgram() {
        final JMenu program = new JMenu("BLAST Program");
        //set default
        final JCheckBoxMenuItem cbMenuItem1;
        cbMenuItem1 = new JCheckBoxMenuItem("blastp");
        cbMenuItem1.setMnemonic(KeyEvent.VK_C);
        cbMenuItem1.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastProgram = "blastp";
                for (int i = 0; i < program.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) program.getItem(i)).setSelected(false);
                }
                cbMenuItem1.setSelected(true);
            }
        });
        cbMenuItem1.setSelected(true);

        final JCheckBoxMenuItem cbMenuItem2;
        cbMenuItem2 = new JCheckBoxMenuItem("blastn");
        cbMenuItem2.setMnemonic(KeyEvent.VK_C);
        cbMenuItem2.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastProgram = "blastn";
                for (int i = 0; i < program.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) program.getItem(i)).setSelected(false);
                }
                cbMenuItem2.setSelected(true);
            }
        });

        final JCheckBoxMenuItem cbMenuItem3;
        cbMenuItem3 = new JCheckBoxMenuItem("blastx");
        cbMenuItem3.setMnemonic(KeyEvent.VK_C);
        cbMenuItem3.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastProgram = "blastx";
                for (int i = 0; i < program.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) program.getItem(i)).setSelected(false);
                }
                cbMenuItem3.setSelected(true);
            }
        });

        final JCheckBoxMenuItem cbMenuItem4;
        cbMenuItem4 = new JCheckBoxMenuItem("megablast");
        cbMenuItem4.setMnemonic(KeyEvent.VK_C);
        cbMenuItem4.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastProgram = "megablast";
                for (int i = 0; i < program.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) program.getItem(i)).setSelected(false);
                }
                cbMenuItem4.setSelected(true);
            }
        });

        final JCheckBoxMenuItem cbMenuItem5;
        cbMenuItem5 = new JCheckBoxMenuItem("tblastn");
        cbMenuItem5.setMnemonic(KeyEvent.VK_C);
        cbMenuItem5.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastProgram = "tblastn";
                for (int i = 0; i < program.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) program.getItem(i)).setSelected(false);
                }
                cbMenuItem5.setSelected(true);
            }
        });

        final JCheckBoxMenuItem cbMenuItem6;
        cbMenuItem6 = new JCheckBoxMenuItem("tblastx");
        cbMenuItem6.setMnemonic(KeyEvent.VK_C);
        cbMenuItem6.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastProgram = "tblastx";
                for (int i = 0; i < program.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) program.getItem(i)).setSelected(false);
                }
                cbMenuItem6.setSelected(true);
            }
        });

        program.add(cbMenuItem1);
        program.add(cbMenuItem2);
        program.add(cbMenuItem3);
        program.add(cbMenuItem4);
        program.add(cbMenuItem5);
        program.add(cbMenuItem6);
        return program;

    }

    /*
     * Creates the menu option for raw/scaled graphing
     */
    private JMenu makeMenuGraph() {
        final JMenu graph = new JMenu("Graphing Options");
        //set default
        final JCheckBoxMenuItem cbMenuItem1;
        cbMenuItem1 = new JCheckBoxMenuItem("Raw Graph Points");
        cbMenuItem1.setMnemonic(KeyEvent.VK_C);
        cbMenuItem1.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                isGraphScaled = false;
                for (int i = 0; i < graph.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) graph.getItem(i)).setSelected(false);
                }
                cbMenuItem1.setSelected(true);
            }
        });
        cbMenuItem1.setSelected(true);

        final JCheckBoxMenuItem cbMenuItem2;
        cbMenuItem2 = new JCheckBoxMenuItem("Scale Graph Points");
        cbMenuItem2.setMnemonic(KeyEvent.VK_C);
        cbMenuItem2.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                isGraphScaled = true;
                for (int i = 0; i < graph.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) graph.getItem(i)).setSelected(false);
                }
                cbMenuItem2.setSelected(true);
            }
        });
        
        graph.add(cbMenuItem1);
        graph.add(cbMenuItem2);
        return graph;
    }

    /*
     * Creates the menubar functionality that allows a user to choose from
     * multiple databases.
     */
    private JMenu makeMenuDatabase() {
        final JMenu database = new JMenu("BLAST Database");
        //set default
        final JCheckBoxMenuItem cbMenuItem1;
        cbMenuItem1 = new JCheckBoxMenuItem("nr");
        cbMenuItem1.setMnemonic(KeyEvent.VK_C);
        cbMenuItem1.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastDatabase = "nr";
                for (int i = 0; i < database.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) database.getItem(i)).setSelected(false);
                }
                cbMenuItem1.setSelected(true);
            }
        });
        cbMenuItem1.setSelected(true);

        //swissprot database
        final JCheckBoxMenuItem cbMenuItem2;
        cbMenuItem2 = new JCheckBoxMenuItem("swissprot");
        cbMenuItem2.setMnemonic(KeyEvent.VK_C);
        cbMenuItem2.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastDatabase = "swissprot";
                for (int i = 0; i < database.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) database.getItem(i)).setSelected(false);
                }
                cbMenuItem2.setSelected(true);
            }
        });

        //est database
        final JCheckBoxMenuItem cbMenuItem3;
        cbMenuItem3 = new JCheckBoxMenuItem("est");
        cbMenuItem3.setMnemonic(KeyEvent.VK_C);
        cbMenuItem3.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastDatabase = "est";
                for (int i = 0; i < database.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) database.getItem(i)).setSelected(false);
                }
                cbMenuItem3.setSelected(true);
            }
        });

        //pdb database
        final JCheckBoxMenuItem cbMenuItem4;
        cbMenuItem4 = new JCheckBoxMenuItem("pdb");
        cbMenuItem4.setMnemonic(KeyEvent.VK_C);
        cbMenuItem4.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastDatabase = "pdb";
                for (int i = 0; i < database.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) database.getItem(i)).setSelected(false);
                }
                cbMenuItem4.setSelected(true);
            }
        });

        //month database
        final JCheckBoxMenuItem cbMenuItem5;
        cbMenuItem5 = new JCheckBoxMenuItem("month");
        cbMenuItem5.setMnemonic(KeyEvent.VK_C);
        cbMenuItem5.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalBlastDatabase = "month";
                for (int i = 0; i < database.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) database.getItem(i)).setSelected(false);
                }
                cbMenuItem5.setSelected(true);
            }
        });

//        JCheckBoxMenuItem cbMenuItem6;
//        cbMenuItem6 = new JCheckBoxMenuItem("month.nt");
//        cbMenuItem6.setMnemonic(KeyEvent.VK_C);
//        cbMenuItem6.addActionListener(new ActionListener() {
//            public void actionPerformed(final ActionEvent event) {
//                globalBlastDatabase = "month.nt";
//            }
//        });

        database.add(cbMenuItem1);
        database.add(cbMenuItem2);
        database.add(cbMenuItem3);
        database.add(cbMenuItem4);
        database.add(cbMenuItem5);

        return database;
    }

    /*
     * Creates the menubar functionality that allows a user to choose from
     * multiple matrices.
     */
    private JMenu makeMenuMatrix() {
        final JMenu matrix = new JMenu("Matrix");

        //Blosum-30 matrix
        final JCheckBoxMenuItem cbMenuItem1;
        cbMenuItem1 = new JCheckBoxMenuItem("Blosum-30");
        cbMenuItem1.setMnemonic(KeyEvent.VK_C);
        cbMenuItem1.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum30";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem1.setSelected(true);
            }
        });

        //Blosum 35 matrix
        final JCheckBoxMenuItem cbMenuItem2;
        cbMenuItem2 = new JCheckBoxMenuItem("Blosum-35");
        cbMenuItem2.setMnemonic(KeyEvent.VK_C);
        cbMenuItem2.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum35";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem2.setSelected(true);
            }
        });

        //Blosum-40 matrix
        final JCheckBoxMenuItem cbMenuItem3;
        cbMenuItem3 = new JCheckBoxMenuItem("Blosum-40");
        cbMenuItem3.setMnemonic(KeyEvent.VK_C);
        cbMenuItem3.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum40";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem3.setSelected(true);
            }
        });

        //Blosum-45 matrix
        final JCheckBoxMenuItem cbMenuItem4;
        cbMenuItem4 = new JCheckBoxMenuItem("Blosum-45");
        cbMenuItem4.setMnemonic(KeyEvent.VK_C);
        cbMenuItem4.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum45";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem4.setSelected(true);
            }
        });

        //Blosum-50 matrix
        final JCheckBoxMenuItem cbMenuItem5;
        cbMenuItem5 = new JCheckBoxMenuItem("Blosum-50");
        cbMenuItem5.setMnemonic(KeyEvent.VK_C);
        cbMenuItem5.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum50";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem5.setSelected(true);
            }
        });

        //Blosum-55 matrix
        final JCheckBoxMenuItem cbMenuItem6;
        cbMenuItem6 = new JCheckBoxMenuItem("Blosum-55");
        cbMenuItem6.setMnemonic(KeyEvent.VK_C);
        cbMenuItem6.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum55";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem6.setSelected(true);
            }
        });

        //Blosum-60 matrix
        final JCheckBoxMenuItem cbMenuItem7;
        cbMenuItem7 = new JCheckBoxMenuItem("Blosum-60");
        cbMenuItem7.setMnemonic(KeyEvent.VK_C);
        cbMenuItem7.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum60";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem7.setSelected(true);
            }
        });

        //Blosum-62
        final JCheckBoxMenuItem cbMenuItem8;
        cbMenuItem8 = new JCheckBoxMenuItem("Blosum-62");
        cbMenuItem8.setMnemonic(KeyEvent.VK_C);
        cbMenuItem8.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum62";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem8.setSelected(true);
            }
        });

        //Blosum-65 matrix
        final JCheckBoxMenuItem cbMenuItem9;
        cbMenuItem9 = new JCheckBoxMenuItem("Blosum-65");
        cbMenuItem9.setMnemonic(KeyEvent.VK_C);
        cbMenuItem9.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum65";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem9.setSelected(true);
            }
        });

        //Blosum-70 matrix
        final JCheckBoxMenuItem cbMenuItem10;
        cbMenuItem10 = new JCheckBoxMenuItem("Blosum-70");
        cbMenuItem10.setMnemonic(KeyEvent.VK_C);
        cbMenuItem10.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum70";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem10.setSelected(true);
            }
        });

        //Blosum-75 matrix
        final JCheckBoxMenuItem cbMenuItem11;
        cbMenuItem11 = new JCheckBoxMenuItem("Blosum-75");
        cbMenuItem11.setMnemonic(KeyEvent.VK_C);
        cbMenuItem11.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum75";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem11.setSelected(true);
            }
        });

        //Blosum-80 matrix
        final JCheckBoxMenuItem cbMenuItem12;
        cbMenuItem12 = new JCheckBoxMenuItem("Blosum-80");
        cbMenuItem12.setMnemonic(KeyEvent.VK_C);
        cbMenuItem12.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum80";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem12.setSelected(true);
            }
        });

        //Blosum-85 matrix
        final JCheckBoxMenuItem cbMenuItem13;
        cbMenuItem13 = new JCheckBoxMenuItem("Blosum-85");
        cbMenuItem13.setMnemonic(KeyEvent.VK_C);
        cbMenuItem13.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum85";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem13.setSelected(true);
            }
        });

        //Blosum-90 matrix
        final JCheckBoxMenuItem cbMenuItem14;
        cbMenuItem14 = new JCheckBoxMenuItem("Blosum-90");
        cbMenuItem14.setMnemonic(KeyEvent.VK_C);
        cbMenuItem14.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum90";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem14.setSelected(true);
            }
        });

        //Blosum-95 matrix
        final JCheckBoxMenuItem cbMenuItem15;
        cbMenuItem15 = new JCheckBoxMenuItem("Blosum-95");
        cbMenuItem15.setMnemonic(KeyEvent.VK_C);
        cbMenuItem15.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum95";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem15.setSelected(true);
            }
        });

        //Blosum-100 matrix
        final JCheckBoxMenuItem cbMenuItem16;
        cbMenuItem16 = new JCheckBoxMenuItem("Blosum-100");
        cbMenuItem16.setMnemonic(KeyEvent.VK_C);
        cbMenuItem16.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "blosum100";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem16.setSelected(true);
            }
        });

        //Gonnet-250 matrix
        final JCheckBoxMenuItem cbMenuItem17;
        cbMenuItem17 = new JCheckBoxMenuItem("Gonnet-250");
        cbMenuItem17.setMnemonic(KeyEvent.VK_C);
        cbMenuItem17.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "gonnet250";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem17.setSelected(true);
            }
        });

        //Pam-250 matrix
        final JCheckBoxMenuItem cbMenuItem18;
        cbMenuItem18 = new JCheckBoxMenuItem("Pam-250");
        cbMenuItem18.setMnemonic(KeyEvent.VK_C);
        cbMenuItem18.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "pam250";
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem18.setSelected(true);
            }
        });

        //User-Entered Matrix (Custom Matrix)
        final JCheckBoxMenuItem cbMenuItem19;
        cbMenuItem19 = new JCheckBoxMenuItem("User-Entered Matrix");
        cbMenuItem19.setMnemonic(KeyEvent.VK_C);
        cbMenuItem19.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                globalMatrix = "UserDefined";
                createUserMatrix();
                for (int i = 0; i < matrix.getItemCount(); i++) {
                    ((JCheckBoxMenuItem) matrix.getItem(i)).setSelected(false);
                }
                cbMenuItem19.setSelected(true);

            }
        });

        //blosum 62 is default
        cbMenuItem8.setSelected(true);

        matrix.add(cbMenuItem1);
        matrix.add(cbMenuItem2);
        matrix.add(cbMenuItem3);
        matrix.add(cbMenuItem4);
        matrix.add(cbMenuItem5);
        matrix.add(cbMenuItem6);
        matrix.add(cbMenuItem7);
        matrix.add(cbMenuItem8);
        matrix.add(cbMenuItem9);
        matrix.add(cbMenuItem10);
        matrix.add(cbMenuItem11);
        matrix.add(cbMenuItem12);
        matrix.add(cbMenuItem13);
        matrix.add(cbMenuItem14);
        matrix.add(cbMenuItem15);
        matrix.add(cbMenuItem16);
        matrix.add(cbMenuItem17);
        matrix.add(cbMenuItem18);
        matrix.add(cbMenuItem19);

        return matrix;
    }

    /*
     * Creates the menubar functionality that allows a user to choose from many
     * 'File' options.
     */
    private JMenu makeMenuFile() {
        JMenu file = new JMenu("File");

        file.setMnemonic(KeyEvent.VK_F);//alt F shortcut

        //creates a new instance of the GUI so multiple runs can occur concurrently
        JMenuItem eMenuItem = new JMenuItem("New Instance");
        eMenuItem.setToolTipText("Create a new instance of NEVBLAST");
        eMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        try {
                            new NevBlastGui().setVisible(true);
                        } catch (IOException ex) {
                            Logger.getLogger(NevBlastGui.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
        });

        //restarts the current instance of the NEVBLAST GUI
        JMenuItem eMenuItem1 = new JMenuItem("Restart");
        eMenuItem1.setToolTipText("Close current instance and open new");
        eMenuItem1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                //a.awt.EventQueue.
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        try {
                            new NevBlastGui().setVisible(true);
                        } catch (IOException ex) {
                            Logger.getLogger(NevBlastGui.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                destroyGui();
            }
        });

        //exits the current instance of the NEVBLAST GUI
        JMenuItem eMenuItem2 = new JMenuItem("Exit");
        eMenuItem2.setToolTipText("Exit this instance of NEVBLAST");
        eMenuItem2.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {

                destroyGui();
            }
        });

        //exits ALL open instances of the NEVBLAST GUI
        JMenuItem eMenuItem3 = new JMenuItem("Exit All");
        eMenuItem3.setToolTipText("Exit all instances of NEVBLAST");
        eMenuItem3.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {

                System.exit(0);
            }
        });
        file.add(eMenuItem);
        file.add(eMenuItem1);
        file.add(eMenuItem2);
        file.add(eMenuItem3);
        return file;
    }

    /*
     * Ends the operations of the current GUI by closing the interface and
     * exiting the program.
     */
    public void destroyGui() {
//        WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
//        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
        this.dispose();
        this.setVisible(false);
        count--;
        if (count == 0) {

            System.exit(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButton1 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        submitButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        queryNameLabel = new javax.swing.JLabel();
        fastaSequenceLabel = new javax.swing.JLabel();
        signature1Label = new javax.swing.JLabel();
        signature2Label = new javax.swing.JLabel();
        eValueLabel = new javax.swing.JLabel();
        lbl_entrezQuery = new javax.swing.JLabel();
        numOfResultsLabel = new javax.swing.JLabel();
        newClearButton = new javax.swing.JLabel();
        newSubmitButton = new javax.swing.JLabel();
        clearClicked = new javax.swing.JLabel();
        submitClicked = new javax.swing.JLabel();
        txt_entrezQuery = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        txt_fastaSequence = new javax.swing.JTextArea();
        txt_signature1 = new javax.swing.JTextField();
        txt_signature2 = new javax.swing.JTextField();
        txt_eValue = new javax.swing.JTextField();
        txt_numberOfResults = new javax.swing.JTextField();
        txt_queryName = new javax.swing.JTextField();
        logoLabel = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        jButton1.setText("jButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 0));
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setMinimumSize(new java.awt.Dimension(800, 600));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group4/nevblast/colorsplash.gif"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(-50, 0, 0, 0);
        getContentPane().add(jLabel10, gridBagConstraints);

        submitButton.setText("Submit");
        submitButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 0, 204), new java.awt.Color(0, 51, 255)));
        submitButton.setMaximumSize(new java.awt.Dimension(77, 50));
        submitButton.setMinimumSize(new java.awt.Dimension(77, 50));
        submitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                submitButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                submitButtonMouseReleased(evt);
            }
        });
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.insets = new java.awt.Insets(12, -87, 0, 0);
        getContentPane().add(submitButton, gridBagConstraints);

        clearButton.setText("Clear");
        clearButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 0, 204), new java.awt.Color(0, 51, 255)));
        clearButton.setMaximumSize(new java.awt.Dimension(77, 50));
        clearButton.setMinimumSize(new java.awt.Dimension(77, 50));
        clearButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                clearButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                clearButtonMouseReleased(evt);
            }
        });
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.insets = new java.awt.Insets(12, 97, 0, 0);
        getContentPane().add(clearButton, gridBagConstraints);

        queryNameLabel.setFont(new java.awt.Font("Times New Roman", 1, 11)); // NOI18N
        queryNameLabel.setForeground(new java.awt.Color(255, 255, 255));
        queryNameLabel.setText("Query Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(50, 36, 0, 0);
        getContentPane().add(queryNameLabel, gridBagConstraints);

        fastaSequenceLabel.setFont(new java.awt.Font("Times New Roman", 1, 11)); // NOI18N
        fastaSequenceLabel.setForeground(new java.awt.Color(255, 255, 255));
        fastaSequenceLabel.setText("FASTA Sequence:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 36, 0, 0);
        getContentPane().add(fastaSequenceLabel, gridBagConstraints);

        signature1Label.setFont(new java.awt.Font("Times New Roman", 1, 11)); // NOI18N
        signature1Label.setForeground(new java.awt.Color(255, 255, 255));
        signature1Label.setText("Signature 1:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 36, 0, 0);
        getContentPane().add(signature1Label, gridBagConstraints);

        signature2Label.setFont(new java.awt.Font("Times New Roman", 1, 11)); // NOI18N
        signature2Label.setForeground(new java.awt.Color(255, 255, 255));
        signature2Label.setText("Signature 2:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 36, 0, 0);
        getContentPane().add(signature2Label, gridBagConstraints);

        eValueLabel.setFont(new java.awt.Font("Times New Roman", 1, 11)); // NOI18N
        eValueLabel.setForeground(new java.awt.Color(255, 255, 255));
        eValueLabel.setText("EValue:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 36, 0, 0);
        getContentPane().add(eValueLabel, gridBagConstraints);

        lbl_entrezQuery.setFont(new java.awt.Font("Times New Roman", 1, 11)); // NOI18N
        lbl_entrezQuery.setForeground(new java.awt.Color(255, 255, 255));
        lbl_entrezQuery.setText("ENTREZ_QUERY:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 36, 0, 0);
        getContentPane().add(lbl_entrezQuery, gridBagConstraints);

        numOfResultsLabel.setFont(new java.awt.Font("Times New Roman", 1, 11)); // NOI18N
        numOfResultsLabel.setForeground(new java.awt.Color(255, 255, 255));
        numOfResultsLabel.setText("Number Of Results:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 36, 0, 0);
        getContentPane().add(numOfResultsLabel, gridBagConstraints);

        newClearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group4/nevblast/Clear Button.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(-15, 100, 0, 0);
        getContentPane().add(newClearButton, gridBagConstraints);

        newSubmitButton.setForeground(new java.awt.Color(0, 102, 255));
        newSubmitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group4/nevblast/Submit Button.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(-42, -85, 0, 0);
        getContentPane().add(newSubmitButton, gridBagConstraints);

        clearClicked.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group4/nevblast/clearclicked.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 100, -60, 0);
        getContentPane().add(clearClicked, gridBagConstraints);

        submitClicked.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group4/nevblast/submitclicked.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(0, -85, -60, 0);
        getContentPane().add(submitClicked, gridBagConstraints);

        txt_entrezQuery.setForeground(new java.awt.Color(255, 255, 255));
        txt_entrezQuery.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        txt_entrezQuery.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 0, 204), new java.awt.Color(0, 51, 255)));
        txt_entrezQuery.setMaximumSize(new java.awt.Dimension(4, 22));
        txt_entrezQuery.setMinimumSize(new java.awt.Dimension(4, 22));
        txt_entrezQuery.setPreferredSize(new java.awt.Dimension(4, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.ipadx = 246;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 14, 0, 0);
        getContentPane().add(txt_entrezQuery, gridBagConstraints);

        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 0, 204), new java.awt.Color(0, 51, 255)));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(160, 98));

        txt_fastaSequence.setColumns(20);
        txt_fastaSequence.setRows(5);
        txt_fastaSequence.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane1.setViewportView(txt_fastaSequence);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 13, 0, 65);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        txt_signature1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 0, 204), new java.awt.Color(0, 51, 255)));
        txt_signature1.setMaximumSize(new java.awt.Dimension(4, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 246;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 14, 0, 0);
        getContentPane().add(txt_signature1, gridBagConstraints);

        txt_signature2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 0, 204), new java.awt.Color(0, 51, 255)));
        txt_signature2.setMaximumSize(new java.awt.Dimension(4, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 246;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 14, 0, 0);
        getContentPane().add(txt_signature2, gridBagConstraints);

        txt_eValue.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 0, 204), new java.awt.Color(0, 51, 255)));
        txt_eValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_eValueActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 246;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 14, 0, 0);
        getContentPane().add(txt_eValue, gridBagConstraints);

        txt_numberOfResults.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 0, 204), new java.awt.Color(0, 51, 255)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 246;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 14, 0, 0);
        getContentPane().add(txt_numberOfResults, gridBagConstraints);

        txt_queryName.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 0, 204), new java.awt.Color(0, 51, 255)));
        txt_queryName.setCaretColor(new java.awt.Color(255, 255, 255));
        txt_queryName.setMaximumSize(new java.awt.Dimension(4, 18));
        txt_queryName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_queryNameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 246;
        gridBagConstraints.ipady = -1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(50, 14, 0, 0);
        getContentPane().add(txt_queryName, gridBagConstraints);

        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group4/nevblast/blast mini logo.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipady = 56;
        gridBagConstraints.insets = new java.awt.Insets(-15, 15, 150, 0);
        getContentPane().add(logoLabel, gridBagConstraints);

        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group4/nevblast/newback.jpg"))); // NOI18N
        backgroundLabel.setMaximumSize(new java.awt.Dimension(999999999, 999999999));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.gridheight = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.1;
        getContentPane().add(backgroundLabel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txt_queryNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_queryNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_queryNameActionPerformed

    private void txt_eValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_eValueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_eValueActionPerformed

    /*
     * Handling procedure for the submit button
     */
    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        //sanitize the input
        sanitizeInput();
        //error handling if incorrect input is entered
        if (txt_queryName.getText().equals("")) {
            JOptionPane.showMessageDialog(rootPane, "Please enter a name for the query. (Query Name).");
        } else if (txt_fastaSequence.getText().equals("")) {
            JOptionPane.showMessageDialog(rootPane, "Please enter a properly formatted FASTA sequence (FASTA Sequence)");
        } else if (checkInput == 1) {
            checkInput = 0;
        } else if (txt_signature1.getText().equals("") && txt_signature2.getText().equals("")) {
            JOptionPane.showMessageDialog(rootPane, "Please enter one or two signature sequences (Signature 1/ Signature 2).");
        } else if (txt_eValue.getText().equals("")) {
            JOptionPane.showMessageDialog(rootPane, "Please enter an 'EValue' for the query (EValue).");
        } else if (txt_numberOfResults.getText().equals("")) {
            JOptionPane.showMessageDialog(rootPane, "Please enter the amount of results that should be returned (Number of Results).");
        } //if input is entered correctly...
        else {
            getContentPane().validate();
            getContentPane().repaint();
            jLabel10.setVisible(true);
            //instance variable for deciphering the Excel document name (located in the Grapher class)
            queryNameFinal = txt_queryName.getText();
            //disable editing on the input fields and send the user input to the SubmitHelper
            clearButton.setEnabled(false);
            submitButton.setEnabled(false);
            (new SubmitHelper()).execute();
            txt_queryName.setEditable(false);
            txt_fastaSequence.setEditable(false);
            txt_signature1.setEditable(false);
            txt_signature2.setEditable(false);
            txt_eValue.setEditable(false);
            txt_numberOfResults.setEditable(false);
            txt_entrezQuery.setEditable(false);
            clickCheck++;
        }
    }//GEN-LAST:event_submitButtonActionPerformed

    /*
     * Handling procedure for the clear button
     */
    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed

        //clears all of the input fields
        txt_eValue.setText(null);
        txt_fastaSequence.setText(null);
        txt_signature1.setText(null);
        txt_signature2.setText(null);
        txt_numberOfResults.setText(null);
        txt_queryName.setText(null);

    }//GEN-LAST:event_clearButtonActionPerformed

    /*
     * Changes the clear button image when pressed..
     */
    private void clearButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearButtonMousePressed
        if (clickCheck == 0) {
            newClearButton.setVisible(false);
            clearClicked.setVisible(true);
        }
    }//GEN-LAST:event_clearButtonMousePressed

    /*
     * Changes the clear button image when released..
     */
    private void clearButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearButtonMouseReleased
        clearClicked.setVisible(false);
        newClearButton.setVisible(true);

    }//GEN-LAST:event_clearButtonMouseReleased

    /*
     * Changes the submit button image when pressed..
     */
    private void submitButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_submitButtonMousePressed
        if (clickCheck == 0) {
            newSubmitButton.setVisible(false);
            submitClicked.setVisible(true);
        }
    }//GEN-LAST:event_submitButtonMousePressed

    /*
     * Changes the submit button image when released..
     */
    private void submitButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_submitButtonMouseReleased
        submitClicked.setVisible(false);
        newSubmitButton.setVisible(true);

    }//GEN-LAST:event_submitButtonMouseReleased

    /*
     * Method for creating/setting up a User-Entered Matrix
     */
    private void createUserMatrix() {
        final JFrame f = new JFrame("User-Entered Matrix");
        // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = f.getContentPane();

        MySubstitutionMatrixHelper defaultMatrix = new MySubstitutionMatrixHelper();

        Object columns[] = {"A", "R", "N", "D", "C", "Q", "E", "G", "H", "I", "L", "K", "M", "F", "P", "S", "T", "W", "Y", "V", "B", "Z", "X", "*"};

        final JTable userMatrixTable = new JTable(defaultMatrix.defaultMatrix, columns);

        JScrollPane scrollPane = new JScrollPane(userMatrixTable);
        RowNumberTable rowTable = new RowNumberTable(userMatrixTable);
        scrollPane.setRowHeaderView(rowTable);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,
                rowTable.getTableHeader());

        JButton matrixSubmit = new JButton();
        matrixSubmit.setText("Submit");
        matrixSubmit.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                f.setVisible(false);
                submitUserMatrix(userMatrixTable);
            }
        });
        
//        JButton matrixClear = new JButton();
//        matrixClear.setText("Clear");
//        matrixClear.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                clearTableData(userMatrixTable);             
//            }
//        });
//        

        content.add(scrollPane, BorderLayout.CENTER);

        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent evt) {
                submitUserMatrix(userMatrixTable);
            }
        });

        content.add(matrixSubmit, BorderLayout.SOUTH);
        //content.add(matrixClear, BorderLayout.AFTER_LINE_ENDS);
        f.setSize(800, 600);
        f.setVisible(true);
    }

    /*
     * Method to submit a User-Entered Matrix
     */
    private void submitUserMatrix(JTable userMatrixTable) {
        Object[][] A = getTableData(userMatrixTable);

        userMatrix = new int[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                userMatrix[i][j] = Integer.valueOf(A[i][j].toString());
            }
        }//end i      
    }

    /*
     * Method to make a 2D array representation of a User-Entered Matrix
     */
    public static Object[][] getTableData(JTable table) {
        //  DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        int nRow = table.getRowCount(), nCol = table.getColumnCount();
        Object[][] tableData = new Object[nRow][nCol];
        for (int i = 0; i < nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                tableData[i][j] = table.getValueAt(i, j);
            }
        }
        return tableData;
    }

    /*
     * Method to clear the User-Entered Matrix
     */
    public static void clearTableData(JTable table) {
        //  DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        int nRow = table.getRowCount(), nCol = table.getColumnCount();
        for (int i = 0; i < nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                table.setValueAt("0", i, j);
            }
        }
    }

    /*
     * Method that checks to see that input has been entered CORRECTLY (does not
     * handle if no input is entered) --> that's handled when input is submitted
     */
    private void sanitizeInput() {
        //set all varaibles
        /**
         * BEGIN SIGNATURE RETRIEVAL
         *
         * Technique used: hard-code valid numbers and letters allowed iterate
         * through each sequence separating the numbers from the letters put
         * each combination into its own bin if the bins are mismatched push an
         * error onto the stack if an invalid character is entered keep track of
         * it and continue to the next char
         *
         * Each sequence will be placed into an arrayList of combinations
         *
         */
        sig1 = new Signature();     //data format to be used
        sig2 = new Signature();     //data format to be used
        String validNumbers = "1234567890";
        String validLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ*-";
        String currentNumber = "";
        ArrayList<String> letterSig2 = new ArrayList<String>();
        ArrayList<String> letterSig1 = new ArrayList<String>();
        ArrayList<String> tempNumberSig1 = new ArrayList();
        for (int i = 0; i < numberSig1.size(); i++) {
            tempNumberSig1.add(numberSig1.get(i));
        }

        ArrayList<String> tempNumberSig2 = new ArrayList();
        for (int i = 0; i < numberSig2.size(); i++) {
            tempNumberSig1.add(numberSig2.get(i));
        }
        Signature tempSig1 = sig1;
        Signature tempSig2 = sig2;
        String tempSignatureErrorChars = signatureErrorChars;
        String signatureOneCopy = signature1;
        String signatureTwoCopy = signature2;
        signature1 = txt_signature1.getText();
        for (int i = 0; i < signature1.length(); i++) {
            if (validNumbers.contains(Character.toString(Character.toUpperCase(signature1.charAt(i))))) {
                //valid number
                currentNumber = currentNumber + Character.toString(Character.toUpperCase(signature1.charAt(i)));
            } else if (validLetters.contains(Character.toString(Character.toUpperCase(signature1.charAt(i))))) {
                //valid letter
                letterSig1.add(Character.toString(Character.toUpperCase(signature1.charAt(i))));
                if (currentNumber.length() > 0) {
                    numberSig1.add(currentNumber);
                    currentNumber = "";
                }//end if we have a currentNumber
            } else if (signature1.charAt(i) == ' ') {
                continue;
            } else {
                signatureErrorChars = signatureErrorChars + Character.toString(Character.toUpperCase(signature1.charAt(i)));
            }

        }//end for
        if (currentNumber.length() > 0) {
            numberSig1.add(currentNumber);
            currentNumber = "";
        }//end if we have a currentNumber

        if (letterSig1.size() == numberSig1.size()) {             //if valid match rebuild signature
            for (int k = 0; k < letterSig1.size(); k++) {
                SignatureBit sigPair = new SignatureBit();
                sigPair.setLineNumber(Integer.valueOf(numberSig1.get(k)));
                sigPair.setAminoAcid(letterSig1.get(k).charAt(0));

                sig1.addSignatureBit(sigPair);
            }//end k
        }//end if valid
        else {
            JOptionPane.showMessageDialog(rootPane, "Invalid Signature 1, Letters/Numbers Mismatch");
            checkInput = 1;
        }//end else

        signature2 = txt_signature2.getText();

        for (int i = 0; i < signature2.length(); i++) {
            if (validNumbers.contains(Character.toString(Character.toUpperCase(signature2.charAt(i))))) {
                //valid number
                currentNumber = currentNumber + Character.toString(Character.toUpperCase(signature2.charAt(i)));
            } else if (validLetters.contains(Character.toString(Character.toUpperCase(signature2.charAt(i))))) {
                //valid letter
                letterSig2.add(Character.toString(Character.toUpperCase(signature2.charAt(i))));
                if (currentNumber.length() > 0) {
                    numberSig2.add(currentNumber);
                    currentNumber = "";
                }//end if we have a currentNumber
            } else if (signature2.charAt(i) == ' ') {
                continue;
            } else {
                signatureErrorChars = signatureErrorChars + Character.toString(Character.toUpperCase(signature2.charAt(i)));
            }

        }//end for
        if (currentNumber.length() > 0) {
            numberSig2.add(currentNumber);
            currentNumber = "";
        }//end if we have a currentNumber

        if (letterSig2.size() == numberSig2.size()) {             //if valid match rebuild signature
            for (int k = 0; k < letterSig2.size(); k++) {
                SignatureBit sigPair = new SignatureBit();
                sigPair.setLineNumber(Integer.valueOf(numberSig2.get(k)));
                sigPair.setAminoAcid(letterSig2.get(k).charAt(0));

                sig2.addSignatureBit(sigPair);
            }//end k
        }//end if valid
        else {
            JOptionPane.showMessageDialog(rootPane, "Invalid Signature 2, Letters/Numbers Mismatch");
            checkInput = 1;
        }//end else

        /**
         * END SIGNATURE RETRIEVAL
         */
        eValue = txt_eValue.getText();

        if (isScientificNotation(eValue)) {
            eValueDecimal = new BigDecimal(eValue);
            if (eValueDecimal.doubleValue() <= 0) {
                JOptionPane.showMessageDialog(rootPane, "Incorrect EValue: You must enter an eValue that is greater than 0.");
                checkInput = 1;
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Invalid EValue, please use scientific format(7.001e-2) or long decimal(0.07001).");

        }

        fastaSequence = txt_fastaSequence.getText();
        numberOfResults = txt_numberOfResults.getText();

        try {
            int numResultsInt = Integer.parseInt(numberOfResults);
            if (numResultsInt <= 0) {
                JOptionPane.showMessageDialog(rootPane, "Incorrect number of results: The total number of results must be greater than 0.");
                checkInput = 1;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(rootPane, "Incorrect number of results: decimal values are not allowed, please enter an integer.");
            checkInput = 1;
            double numResultsDouble = Double.parseDouble(numberOfResults);
            if (numResultsDouble <= 0) {
                JOptionPane.showMessageDialog(rootPane, "Incorrect number of results: The total number of results must be greater than 0.");
                checkInput = 1;
            }
        }

        queryName = txt_queryName.getText();
        numberSig1 = tempNumberSig1;
        numberSig2 = tempNumberSig2;
        signatureErrorChars = tempSignatureErrorChars;
        sig1 = tempSig1;
        sig2 = tempSig2;
        currentNumber = "";
        //signature1 = signatureOneCopy;
        //signature2 = signatureTwoCopy;
    }//end sanitize input

    /*
     * Method to check if a String is valid scientific notation using BigDecimal
     */
    boolean isScientificNotation(String numberString) {
        // Validate number
        try {
            new BigDecimal(numberString);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    /**
     * This method returns the max signature scores of the given sequenceList.
     * @param toGraph
     * @return a size 2 array list.  The [0] item will equal the MaxSigA score and the [1] item will match the maxSigB score.
     */
    public ArrayList<Double> getMaxSigScores(ArrayList<SequenceHit> toGraph){
        ArrayList<Double> ret = new ArrayList<Double>();
        double maxA = 0;
        double maxB = 0;
        double tempA = 0;
        double tempB = 0;
        for(SequenceHit seq : toGraph){
            tempA = Double.parseDouble(seq.getScoreA());
            tempB = Double.parseDouble(seq.getScoreB());
            if( tempA > maxA)
                maxA = tempA;
            if( tempB > maxB)
                maxB = tempB;
        }
        
        ret.add(maxA);
        ret.add(maxB);
        return ret;
    }

    /*
     * Handles the sizing and location of each graphed output window
     */
    public void createOutput(ArrayList<SequenceHit> toGraph) throws IOException, SecurityException, UnsatisfiedLinkError, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        //if graph is contains only one element and its error code is not empty
        if (toGraph.size() == 1 && !toGraph.get(0).getError().isEmpty()) {
            String error = "Blast reported that no results back, please see error message\n\n\n";
            JOptionPane.showMessageDialog(new JFrame(), toGraph.get(0).getError());
            jLabel10.setVisible(false);
            return;
        }
//        setLibraryPath("");
        desktopOutput = new JDesktopPane();
        setExtendedState(MAXIMIZED_BOTH);
        // desktopOutput.
        String outputWindowHeader;
        ArrayList maxScores = getMaxSigScores(toGraph);
        outputWindowHeader = "Fasta Header: " + fastaHead + "<br>";
        outputWindowHeader += "Fasta Sequence: " + fastaSequence + "<br>";
        outputWindowHeader += "Signature 1: " + signature1 + "<br>";
        outputWindowHeader += "Max Signature 1: " + maxScores.get(0) + "<br>";
        outputWindowHeader += "Signature 2: " + signature2 + "<br>";
        outputWindowHeader += "Max Signature 2: " + maxScores.get(1) + "<br><br><br>";
        JTextPane outputWindow = new JTextPane();

        outputWindow.setSize(width, height);
        outputWindow.setContentType("text/html");
        outputWindow.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        JScrollPane scrollPane = new JScrollPane(outputWindow);

        Grapher chart3d = new Grapher(toGraph, outputWindow, outputWindowHeader, "3dChart");
        chart3d.setIsGraphScaled(isGraphScaled);
        chart3d.init();
        Grapher chart2dA = new Grapher(toGraph, outputWindow, outputWindowHeader, "2dChartA", chart3d.getDefinedColors());
        chart2dA.setIsGraphScaled(isGraphScaled);
        chart2dA.init();
        Grapher chart2dB = new Grapher(toGraph, outputWindow, outputWindowHeader, "2dChartB", chart3d.getDefinedColors());
        chart2dB.setIsGraphScaled(isGraphScaled);
        chart2dB.init();

        ResultsTable newContentPane = new ResultsTable(toGraph, toGraph.size(), outputWindow, outputWindowHeader, chart3d.scatter, chart2dA.scatter2dA, chart2dB.scatter2dB);       //only works for 3d
        chart3d.attachResultTable(newContentPane);
        chart2dA.attachResultTable(newContentPane);
        chart2dB.attachResultTable(newContentPane);

        chart3d.attachScatters(chart2dA.scatter2dA, chart2dB.scatter2dB);
        chart2dA.attachScatters(chart3d.scatter, chart2dB.scatter2dB);
        chart2dB.attachScatters(chart2dA.scatter2dA, chart3d.scatter);

        chart3d.attachMouse();
        chart2dA.attachMouse();
        chart2dB.attachMouse();

        width = this.getWidth();
        height = this.getHeight();

        //sets the location and size of each output window 
        createFrame("Result 3d", chart3d, width / 4, 0, (width / 2), (height / 3) * 2);
        createFrame("Result 2dA", chart2dA, 0, 0, (width / 4), height / 3);
        createFrame("Result 2dB", chart2dB, 0, height / 3, (width / 4), height / 3);
        createFrame("Text Output", scrollPane);
        createFrame("Results Table", newContentPane);

        setContentPane(desktopOutput);
        desktopOutput.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

    }

    /*
     * This is a helper method for setting up the 2D/3D graph sizes and
     * locations
     */
    protected void createFrame(String windowName, Grapher chart, int xpos, int ypos, int sizeW, int sizeH) {
        MyInternalFrame frame = new MyInternalFrame(windowName);
        frame.setVisible(true);

        // Set up the default size of the chart and add it to the frame.
        Component jComp = (java.awt.Component) chart.getChart().getCanvas();
        Dimension dim = new Dimension(sizeW, sizeH);
        jComp.setMaximumSize(dim);
        jComp.setPreferredSize(dim);
        jComp.setSize(dim);
        jComp.setMinimumSize(dim);
        frame.add(jComp);

        frame.setSize(sizeW, sizeH);
        frame.setLocation(xpos, ypos);

        desktopOutput.add(frame);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    /**
     * This version creates the results table at the bottom of the screen
     *
     * @param windowName
     * @param resultsTable
     */
    protected void createFrame(String windowName, ResultsTable resultsTable) {
        MyInternalFrame frame = new MyInternalFrame(windowName);
        frame.setVisible(true);
        resultsTable.setOpaque(true);

        frame.add(resultsTable);

        frame.setSize(width, height / 3);
        frame.setLocation(0, ((height / 3) * 2) - 10);
        desktopOutput.add(frame);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    /**
     * This version creates the results text frame on the right
     *
     * @param windowName
     * @param resultsText
     */
    protected void createFrame(String windowName, JScrollPane resultsText) {
        MyInternalFrame frame = new MyInternalFrame(windowName);
        frame.setVisible(true);
        // resultsText.setOpaque(true);
        frame.add(resultsText);

        frame.setSize(width / 4, (height / 3) * 2);
        frame.setLocation((width / 4) * 3, 0);

        desktopOutput.add(frame);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    /*
     * This is a helper class for taking the user input in the GUI and parsing
     * it through the database. It will also send the query results to a Grapher
     * class which will show the output in a clear, presentable way.
     */
    class SubmitHelper extends SwingWorker<String, Object> {

        @Override
        public String doInBackground() {
            //prints any additional errors not found in the sanitization method
            errors = new ArrayList<String>();
            InputStream stream = null;

            if (errors.size() > 0) {
                JOptionPane.showMessageDialog(new JFrame(), errors.toString().substring(1, errors.toString().length() - 1));
                jLabel10.setVisible(false);
                return "";
            }//if errors stop
            try {
                //we must parse fastaSequence
                String fasta = fastaSequence;
                stream = new ByteArrayInputStream(fasta.getBytes("UTF-8"));
                try {
                    LinkedHashMap<String, ProteinSequence> map = FastaReaderHelper.readFastaProteinSequence(stream);
                    //iterate through FASTA sequences
                    for (Entry<String, ProteinSequence> entry : map.entrySet()) {
                        String fastaHeader = entry.getValue().getOriginalHeader();
                        System.out.println(entry.getValue().getDescription());
                        fastaSequence = entry.getValue().getSequenceAsString();

                        fasta = fasta.replaceAll("(\\r|\\n)", "");
                        System.out.println(fastaHeader);
                        //   fastaHeader = fasta.substring(0, fasta.length() - fastaSequence.length());
                        System.out.println(fastaHeader);
                        System.out.println("Fasta Header: " + fastaHeader + "\nFasta sequence: " + fastaSequence);

                        fastaHead = fastaHeader;

                        entrezQuery = txt_entrezQuery.getSelectedItem().toString();
                        //create BlastQuery Object
                        BlastQuery blastQuery = new BlastQuery(queryName, sig1,
                                sig2, fastaSequence, fastaHeader, eValueDecimal, numberOfResults, "", globalMatrix, userMatrix, globalBlastProgram, globalBlastDatabase, entrezQuery);

                        createOutput(blastQuery.toBlast());
                        //    }

                    }//end foreach fasta
                } catch (Exception ex) {
                    Logger.getLogger(NevBlastGui.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("exception ex");
                    JOptionPane.showMessageDialog(new JFrame(), "An unexpected exception arose, please restart program.");
                    jLabel10.setVisible(false);

                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(NevBlastGui.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(new JFrame(), "An unsupported encoding exception arose, please restart program.");
                jLabel10.setVisible(false);

            } finally {
                try {
                    stream.close();
                } catch (IOException ex) {
                    Logger.getLogger(NevBlastGui.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(new JFrame(), "An unexpected exception arose, please restart program.");
                    jLabel10.setVisible(false);

                }
            }//end finally
            return "";
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JButton clearButton;
    private javax.swing.JLabel clearClicked;
    private javax.swing.JLabel eValueLabel;
    private javax.swing.JLabel fastaSequenceLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_entrezQuery;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel newClearButton;
    private javax.swing.JLabel newSubmitButton;
    private javax.swing.JLabel numOfResultsLabel;
    private javax.swing.JLabel queryNameLabel;
    private javax.swing.JLabel signature1Label;
    private javax.swing.JLabel signature2Label;
    private javax.swing.JButton submitButton;
    private javax.swing.JLabel submitClicked;
    private javax.swing.JTextField txt_eValue;
    private javax.swing.JComboBox txt_entrezQuery;
    private javax.swing.JTextArea txt_fastaSequence;
    private javax.swing.JTextField txt_numberOfResults;
    private javax.swing.JTextField txt_queryName;
    private javax.swing.JTextField txt_signature1;
    private javax.swing.JTextField txt_signature2;
    // End of variables declaration//GEN-END:variables
    private String eValue;                  //this holds the raw string
    private BigDecimal eValueDecimal;       //the value to be passed
    private String fastaSequence;
    private String fastaHead;
    private String numberOfResults;
    private String queryName;
    private String signature1;              //This holds the raw string
    private String signature2;              //This holds the raw string
    private ArrayList<String> errors;
    private Signature sig1;     //data format to be used
    private Signature sig2;     //data format to be used
    private String signatureErrorChars;
    private JDesktopPane desktopOutput;
    private String globalMatrix;
    private String globalBlastDatabase;
    private String globalBlastProgram;
    private int[][] userMatrix;
    private String entrezQuery;
}
