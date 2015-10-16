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
 * @Signature.java
 * This class holds all of the signatureBits and its only purpose is a way of 
 * organizing all of the data.
 */

package group4.nevblast;

import java.util.ArrayList;

/**
 *
 * @author Matthew Zygowicz - ziggy
 */
public class Signature {
    ArrayList<SignatureBit> data= new ArrayList<SignatureBit>();

    public Signature() {
        data = new ArrayList<SignatureBit>();
    }

    public void addSignatureBit(SignatureBit sigbit){
        data.add(sigbit);
    }

    
    public int size(){
        
        return data.size();
    }


    public SignatureBit get(int index){
        return data.get(index);
    }
    @Override
    public String toString() {
        return "Signature{" + "data=" + data + '}';
    }
    
    
    //----------------------------------------
    //Getters and setters
    //----------------------------------------
    public ArrayList<SignatureBit> getData() {
        return data;
    }

    public void setData(ArrayList<SignatureBit> data) {
        this.data = data;
    }
    
}
