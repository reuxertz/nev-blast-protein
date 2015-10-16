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

package group4.nevblast;

import java.lang.Double;
import java.util.Comparator;

public class DoubleComparator implements Comparator<String> {

  public static final DoubleComparator DOUBLE_COMPARATOR = new DoubleComparator();

    public int compare(String t, String t1) {
        Double d1 = Double.valueOf(t);
        double d2 = Double.valueOf(t1);
        return d1.compareTo(d2);
    }
}

