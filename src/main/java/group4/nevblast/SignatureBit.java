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
 * @SignatureBit.java
 * This class holds the SignatureBit data of the signature sequence.  The data
 * consists of the letter (Amino Acid) we are expected to find as well as position
 * we expect to find the number at
 */

package group4.nevblast;

/**
 *
 * @author Matthew Zygowcz - Ziggy
 */
public class SignatureBit {
    int lineNumber;
    char aminoAcid;

    public SignatureBit(int lineNumber, char aminoAcid) {
        this.lineNumber = lineNumber;
        this.aminoAcid = aminoAcid;
    }
    public SignatureBit(){
        //default constructor
    }

    @Override
    public String toString() {
        return "SignatureBit{" + "lineNumber=" + lineNumber + ", aminoAcid=" + aminoAcid + '}';
    }
    
    //----------------------------------------
    //Getters and setters
    //----------------------------------------
    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public char getAminoAcid() {
        return aminoAcid;
    }

    public void setAminoAcid(char aminoAcid) {
        this.aminoAcid = aminoAcid;
    }
    
}
