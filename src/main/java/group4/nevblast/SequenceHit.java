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
 * @SequenceHit.java This class holds the SequenceHit data-structure. The
 * purpose of this class is to neatly organize BlastSequenceHit data. When
 * constructed all but the color is set. Color is set by the Grapher object and
 * used to color text on output.
 *
 *
 */
package group4.nevblast;

import org.jzy3d.colors.Color;

public class SequenceHit {

    String accession;
    String hitSequence;
    String hitFrom;
    String hitTo;
    String sigAMatch;
    String sigBMatch;
    String eValue;
    String scoreA;
    String scoreB;
    double normalizedScoreA;
    double normalizedScoreB;
    String error;   //used to report errors from blast
    //the following is data used for graphing
    Color color;
    float x_3d;
    float y_3d;
    float z_3d;
    String hitDef;
    boolean isHighlighted;

    public SequenceHit(String accession, String hitSequence, String hitFrom, String hitTo, String eValue, String scoreA, String scoreB, String aMatch, String bMatch, String hitDef) {
        this.accession = accession;
        this.hitSequence = hitSequence;
        this.hitFrom = hitFrom;
        this.hitTo = hitTo;
        this.eValue = eValue;
        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.error = "";
        this.isHighlighted = false;
        this.sigAMatch = aMatch;
        this.sigBMatch = bMatch;
        this.hitDef = hitDef;
    }

    public SequenceHit() {
        //default constructor does nothing
        this.error = "";
        this.isHighlighted = false;
    }
    public String getHitDef(){
        if(this.hitDef.contains(">gi"))
              return this.hitDef.substring(0,this.hitDef.indexOf(">gi"));
        return this.hitDef;
    }
    public void setColor(Color color) {
        this.color = color;
    }

    public String getHexColor() {
        String hexa = "#";
        hexa += Integer.toHexString((int) (color.r * 255));
        hexa += Integer.toHexString((int) (color.g * 255));
        hexa += Integer.toHexString((int) (color.b * 255));
        return hexa;
    }

    //-----------------------------------
    //Getters and Setters
    //-----------------------------------
    public double getNormalizedScoreA() {
        return normalizedScoreA;
    }

    public void setNormalizedScoreA(double normalizedScoreA) {
        this.normalizedScoreA = normalizedScoreA;
    }

    public double getNormalizedScoreB() {
        return normalizedScoreB;
    }

    public void setNormalizedScoreB(double normalizedScoreB) {
        this.normalizedScoreB = normalizedScoreB;
    }

    public boolean isIsHighlighted() {
        return isHighlighted;
    }

    public void setIsHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }

    public float getX_3d() {
        return x_3d;
    }

    public void setX_3d(float x_3d) {
        this.x_3d = x_3d;
    }

    public float getY_3d() {
        return y_3d;
    }

    public void setY_3d(float y_3d) {
        this.y_3d = y_3d;
    }

    public float getZ_3d() {
        return z_3d;
    }

    public void setZ_3d(float z_3d) {
        this.z_3d = z_3d;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getHitSequence() {
        return hitSequence;
    }

    public void setHitSequence(String hitSequence) {
        this.hitSequence = hitSequence;
    }

    public String getHitFrom() {
        return hitFrom;
    }

    public void setHitFrom(String hitFrom) {
        this.hitFrom = hitFrom;
    }

    public String getHitTo() {
        return hitTo;
    }

    public void setHitTo(String hitTo) {
        this.hitTo = hitTo;
    }

    public String geteValue() {
        return eValue;
    }

    public void seteValue(String eValue) {
        this.eValue = eValue;
    }

    public String getScoreA() {
        return scoreA;
    }

    public void setScoreA(String scoreA) {
        this.scoreA = scoreA;
    }

    public String getScoreB() {
        return scoreB;
    }

    public void setScoreB(String scoreB) {
        this.scoreB = scoreB;
    }

    public String getSigAMatch() {
        if(sigAMatch.startsWith("-"))
            return " " + sigAMatch;
        return sigAMatch;
    }

    public void setSigAMatch(String sigAMatched) {
        this.sigAMatch = sigAMatched;
    }

    public String getSigBMatch() {
        if(sigBMatch.startsWith("-"))
            return " " + sigBMatch;
        return sigBMatch;
    }

    public void setSigBMatch(String sigBMatched) {
        this.sigAMatch = sigBMatched;
    }
}
