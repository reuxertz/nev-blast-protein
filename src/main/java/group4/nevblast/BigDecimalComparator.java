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

import java.math.BigDecimal;
import java.util.Comparator;

/**
*
* A {@link BigDecimal} {@link Comparator} based on {@link BigDecimal#compareTo(BigDecimal)}.<br>
* Is useful if ones wants to use BigDecimal assertions based on {@link BigDecimal#compareTo(BigDecimal)} instead of
* {@link BigDecimal#equals(Object)} method.
*
* @author Joel Costigliola
*/
public class BigDecimalComparator implements Comparator<String> {

  /**
* an instance of {@link BigDecimalComparator}.
*/
  public static final BigDecimalComparator BIG_DECIMAL_COMPARATOR = new BigDecimalComparator();

  @Override
  public int compare(String bigDecimal1, String bigDecimal2) {
      BigDecimal b1 = new BigDecimal(bigDecimal1);
      BigDecimal b2 = new BigDecimal(bigDecimal2);
    return b1.compareTo(b2);
  }
}
