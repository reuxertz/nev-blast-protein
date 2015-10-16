/*
 * 
 This file is part of NEVBLAST.

 NEVBLAST is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation.

 NEVBLAST is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with NEVBLAST.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

/**
 * @BlastQuery.java This class takes the userinput and performs a BLAST query.
 * Once the Blast Object is returned from BLAST it is then parsed and an
 * ArrayList of SequenceHit objects are created. These objects hold all the
 * information that blast has passed it. It is here that the BLAST perform the
 * signature score calculation.
 *
 */

package group4.nevblast;

import BLAST.BlastOutput;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.biojava3.core.sequence.io.util.IOUtils;
import org.biojava3.ws.alignment.qblast.*;
import static org.biojava3.ws.alignment.qblast.BlastAlignmentParameterEnum.ALIGNMENTS;
import static org.biojava3.ws.alignment.qblast.BlastAlignmentParameterEnum.ENTREZ_QUERY;
import static org.biojava3.ws.alignment.qblast.BlastAlignmentParameterEnum.MAX_NUM_SEQ;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class BlastQuery {

    /*
     * instance variables
     */
    public String status = "";
    private final String queryFld_;
    private final Signature sigA_;
    private final Signature sigB_;
    private double sigScoreA_;
    private double sigScoreB_;
    private final String fastaSequence_;
    private final BigDecimal EValue_;
    private final String numberOfResults_;
    private final String sugSigs_;
    private final String fastaHeader_;
    private final MySubstitutionMatrixHelper blosum;
    private int normalizedBlosumScoreA;
    private int normalizedBlosumScoreB;
    private String blastProgram;
    private String blastDatabase;
    private String entrezQuery;
    public static String queryNameCompute = "198749586";

    /*
     * Create a stream to hold the output This is the constructor to create a
     * BLAST Query Each signature is an arraylist of AminoAcid/LineNumber
     * combinations. The complete arraylist makes up the signature
     */
    public BlastQuery(String queryFld, Signature sigA, Signature sigB, String fSequence, String fHeader, BigDecimal EVal, String nResults, String sugSigs, String matrixType, int[][] userMatrix, String program, String database, String entrezQueryParam) {
        //constructor specific variables 
        queryFld_ = queryFld;
        sigA_ = sigA;
        sigB_ = sigB;
        fastaSequence_ = fSequence;
        fastaHeader_ = fHeader;
        EValue_ = EVal;
        numberOfResults_ = nResults;                                             //nResults = Number of Results
        sugSigs_ = sugSigs;
        blosum = new MySubstitutionMatrixHelper(matrixType, userMatrix);
        blastProgram = program;
        blastDatabase = database;
        entrezQuery = entrezQueryParam;

        //get the best possible score
        for (int i = 0; i < sigA_.size(); i++) {
            normalizedBlosumScoreA += blosum.getCharScore(sigA_.get(i).getAminoAcid(), sigA_.get(i).getAminoAcid());
        }
        for (int i = 0; i < sigB_.size(); i++) {
            normalizedBlosumScoreB += blosum.getCharScore(sigB_.get(i).getAminoAcid(), sigB_.get(i).getAminoAcid());
        }
        
        /*
         * TEST Normalization
         */
        //System.out.println("Normalized A: " + normalizedBlosumScoreA);
        //System.out.println("Normalized B: " + normalizedBlosumScoreB);
    }

    /*
     * -- Getters --
     */
    public String getQueryFld() {
        return queryFld_;
    }

    public Signature getSigA() {
        return sigA_;
    }

    public Signature getSigB() {
        return sigB_;
    }

    public String getFasta() {
        return fastaSequence_;
    }

    public BigDecimal getEVal() {
        return EValue_;
    }

    public int getNumberOfResults() {
        return Integer.valueOf(numberOfResults_);
    }

    public String getSugSigs() {
        return sugSigs_;
    }

    //ArrayList to hold signature information
    public ArrayList<SequenceHit> toBlast() throws Exception {
        ArrayList<SequenceHit> toGraph = new ArrayList<SequenceHit>();
        BlastOutput BO;
        int numberOfResults = getNumberOfResults();

        //NCBIQ query call
        NCBIQBlastService service = new NCBIQBlastService();

        // set alignment options
        NCBIQBlastAlignmentProperties props = new NCBIQBlastAlignmentProperties();

        if (blastProgram == "blastp") {
            props.setBlastProgram(BlastProgramEnum.blastp);
        } else if (blastProgram == "blastn") {
            props.setBlastProgram(BlastProgramEnum.blastn);
        } else if (blastProgram == "blastx") {
            props.setBlastProgram(BlastProgramEnum.blastx);
        } else if (blastProgram == "megablast") {
            props.setBlastProgram(BlastProgramEnum.megablast);
        } else if (blastProgram == "tblastn") {
            props.setBlastProgram(BlastProgramEnum.tblastn);
        } else if (blastProgram == "tblastx") {
            props.setBlastProgram(BlastProgramEnum.tblastx);
        }

        props.setBlastDatabase(blastDatabase);
        //get blast DB from http://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Web&PAGE_TYPE=BlastDocs&DOC_TYPE=ProgSelectionGuide

        //only set entrezQuery if it is there!
        if (this.entrezQuery.trim().length() > 0) {
            props.setAlignmentOption(ENTREZ_QUERY, this.entrezQuery.trim() + "[Organism]");
        }
        
        props.setAlignmentOption(ALIGNMENTS, String.valueOf(numberOfResults));
        props.setAlignmentOption(MAX_NUM_SEQ, String.valueOf(numberOfResults));
        //props.setBlastWordSize(3);

        // set output options
        NCBIQBlastOutputProperties outputProps = new NCBIQBlastOutputProperties();

        outputProps.setOutputOption(BlastOutputParameterEnum.ALIGNMENTS, String.valueOf(numberOfResults));
        outputProps.setOutputOption(BlastOutputParameterEnum.DESCRIPTIONS, String.valueOf(numberOfResults));

        String rid = null;          // blast request ID
        FileWriter writer = null;
        BufferedReader reader = null;
        
        try {        
            // send blast request and save request id
            rid = service.sendAlignmentRequest(fastaSequence_, props);
            
            //This is just to display where goes the RID after you have submitted a new BLAST
            System.out.println("The newly submitted BLAST had a RID: " + rid);
            System.out.println(service.getRemoteBlastInfo());

            // wait until results become available. Alternatively, one can do other computations/send other alignment requests
            while (!service.isReady(rid)) {
                status = "Waiting for results. Sleeping for 5 seconds";
                System.out.println(status);
                Thread.sleep(5000);
                //reset status
                status = ""; 
            }

            //read results when they are ready
            InputStream in = service.getAlignmentResults(rid, outputProps);

            /*
             * The following code does the majority of the work in terms of
             * doing calulations and extracting output values.
             */
            BO = BlastQuery.catchBLASTOutput(in);

            for (int i = 0; i < BO.getBlastOutputIterations().getIteration().size(); i++) {
                for (int k = 0; k < BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().size(); k++) {
                    String accession = BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitAccession();                  
                    //need to iterate HSPS
                    for (int j = 0; j < BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().size(); j++) {

                        BigDecimal tempEvalue = new BigDecimal(BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspEvalue());
                        if (tempEvalue.compareTo(EValue_) <= 0) {
                            String hitSeq = BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspHseq();
                            String hitFrom = BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspHitFrom();

                            String hitTo = BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspHitTo();

                            String queryFrom = BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspQueryFrom();
                            int queryFromInt = Integer.parseInt(queryFrom);
                            ArrayList<String> numberSigOne = NevBlastGui.numberSig1;
                            ArrayList<String> numberSigTwo = NevBlastGui.numberSig2;
                            String sigAMatchNew = "";
                            String sigBMatchNew = "";

                            /*
                             * BEGIN GETTING SIGA SCORE
                             */
                            int scorea = 0;
                            for (int p = 0; p < sigA_.size(); p++) {
                                if (sigA_.get(p).getLineNumber() <= Integer.valueOf(BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspQueryTo())) {
                                    if (sigA_.get(p).getLineNumber() >= Integer.valueOf(BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspQueryFrom())) {
                                        //Number is valid!
                                        //position = lineNumber of search - line number query begins at
                                        int position = sigA_.get(p).getLineNumber() - Integer.valueOf(BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspQueryFrom());

                                        String qHit = BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspQseq();
                                        int originalPosition = position;
                                        for (int y = 0; y < originalPosition; y++) {
                                            if (qHit.charAt(y) == '-') {
                                                position++;
                                            }
                                        }
                                        
                                        sigAMatchNew += BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspHseq().charAt(position);
                                        scorea += blosum.getCharScore(sigA_.get(p).getAminoAcid(), BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspHseq().charAt(position));
                                    }//end if greater then or equal to queryFrom
                                    else {
                                        //outside of hit range (too low)
                                        sigAMatchNew += "-";
                                    }
                                }//end if less then the queryTo
                                else {
                                    //outside of hit range (too high)
                                    sigAMatchNew += "-";
                                }
                            }//end for p - sigA

                            /*
                             * BEGIN GETTINGS SIGB SCORE
                             */
                            int scoreb = 0;
                            for (int q = 0; q < sigB_.size(); q++) {
                                if (sigB_.get(q).getLineNumber() <= Integer.valueOf(BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspQueryTo())) {
                                    if (sigB_.get(q).getLineNumber() >= Integer.valueOf(BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspQueryFrom())) {
                                        //Number is valid!
                                        //position = lineNumber of search - line number query begins at
                                        int position = sigB_.get(q).getLineNumber() - Integer.valueOf(BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspQueryFrom());
                                        String qHit = BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspQseq();
                                        int originalPosition = position;
                                        for (int y = 0; y < originalPosition; y++) {
                                            if (qHit.charAt(y) == '-') {
                                                position++;
                                            }
                                        }

                                        sigBMatchNew += BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspHseq().charAt(position);
                                        scoreb += blosum.getCharScore(sigB_.get(q).getAminoAcid(), BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitHsps().getHsp().get(j).getHspHseq().charAt(position));

                                    }//end if greater then or equal to queryFrom
                                    else {
                                        //outside of hit range (too low)
                                        sigBMatchNew += "-";
                                    }
                                }//end if less then the queryTo
                                else {
                                    //outside of hit range (too high)
                                    sigBMatchNew += "-";
                                }
                            }//end for q - sigB

                            String hitDef = BO.getBlastOutputIterations().getIteration().get(i).getIterationHits().getHit().get(k).getHitDef();
                            SequenceHit temp = new SequenceHit(accession, hitSeq, hitFrom, hitTo, tempEvalue.toString(), Double.toString(scorea), Double.toString(scoreb), sigAMatchNew, sigBMatchNew, hitDef);
                            temp.setNormalizedScoreA((double) scorea / (double) normalizedBlosumScoreA);
                            temp.setNormalizedScoreB((double) scoreb / (double) normalizedBlosumScoreB);
                            toGraph.add(temp);
                        }
                    }//end j - iterate HSPS
                }//end for k
            }  //end for i        

            //figure out the output file name
            String queryNameFile = NevBlastGui.queryNameFinal;
            queryNameCompute = queryNameFile + ".csv";
            File file = new File(queryNameFile + ".csv");
            while (file.exists() == true) {
                
                //if the ouput filename already exists, ask the user if he or
                //she wants to override it.
                int fileOption = JOptionPane.showConfirmDialog(null, "The file you are trying to create already exists. Would you like to replace the existing file?", "Duplicate File Found", JOptionPane.YES_NO_OPTION);
                if (fileOption == JOptionPane.YES_OPTION) {
                    queryNameCompute = queryNameFile + ".csv";
                    break;  //break loop decision is final
                } else if (fileOption == JOptionPane.NO_OPTION) {
                    String fileTemp = JOptionPane.showInputDialog(null, "Choose a new file name for your output.");
                    if (fileTemp.endsWith(".csv")) {
                        queryNameCompute = fileTemp;
                    } else {
                        queryNameCompute = fileTemp + ".csv";
                    }
                } else {
                    JOptionPane.showMessageDialog(new JFrame(), "The data will be stored in a file named 'Output.csv'.");
                    queryNameCompute = "Output.csv";
                    break; //break loop decision is final
                }
                file = new File(queryNameCompute);
            }
            //we only had one BLAST query, so we are expecting only one iteration, that's why .get(o).
            //same thing about the .getHit().get(0) - we are asking for the best Hit, which is first on the list
            //etc, just use the BO getters
            //---------------MyCode brakes the BioJava example here -----------------------\\
        } catch (Exception e) {

            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("blastquery exception");
            SequenceHit errorSequence = new SequenceHit();
            errorSequence.setError(e.getMessage());
            toGraph.clear();
            toGraph.add(errorSequence);
            return toGraph;

        } finally {
            // clean up
            IOUtils.close(writer);
            IOUtils.close(reader);

            service.sendDeleteRequest(rid);
            return toGraph;
        }

    }//end toBlast

    private static BlastOutput catchBLASTOutput(InputStream in) throws Exception {
        //JAXBContext jc = JAXBContext.newInstance(BlastOutput.class);
        //Unmarshaller u = jc.createUnmarshaller();
        //return (BlastOutput) u.unmarshal(in);
        JAXBContext context = JAXBContext.newInstance(BlastOutput.class);
        Unmarshaller u = context.createUnmarshaller();

        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setValidating(true); // Not required for JAXB/XInclude

        XMLReader xr = (XMLReader) spf.newSAXParser().getXMLReader();
        SAXSource source = new SAXSource(xr, new InputSource(in));

        return (BlastOutput) u.unmarshal(source);
    }
}//end BlastQuery Class

