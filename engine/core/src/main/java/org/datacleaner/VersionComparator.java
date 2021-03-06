/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner;

import java.util.Comparator;

/**
 * Compares the versions of DataCleaner to determine the latest.
 * 比较DataCleaner的版本以确定最新版本。
 *
 * Versions are Strings in format: X.Y.Z or X.Y.Z-SNAPSHOT. The comparator
 * expects only correct inputs, the values should be validated before passing it
 * to the comparator otherwise it will crash.
 * 版本是字符串，格式为：X.Y.Z或X.Y.Z-SNAPSHOT。
 * 比较器仅期望正确的输入，应在将值传递给比较器之前验证值否则它将崩溃。
 */
public class VersionComparator implements Comparator<String> {

    public boolean isComparable(String str) {
        return str.matches("\\d+\\.\\d+(\\.\\d+)?(\\-[A-Z0-9]+)?");
    }
    
    @Override
    public int compare(final String o1, final String o2) {
        final String[] o1Split = o1.split("\\.");
        final String[] o2Split = o2.split("\\.");

        for (int i = 0; i < Math.min(o1Split.length, o2Split.length); i++) {
            int specialEndCounter = 0;
            final Integer o1Part;
            if (o1Split[i].contains("-")) {
                specialEndCounter++;
                o1Part = Integer.parseInt(o1Split[i].substring(0, o1Split[i].lastIndexOf("-")));
            } else {
                o1Part = Integer.parseInt(o1Split[i]);
            }
            final Integer o2Part;
            if (o2Split[i].contains("-")) {
                specialEndCounter++;
                o2Part = Integer.parseInt(o2Split[i].substring(0, o2Split[i].lastIndexOf("-")));
            } else {
                o2Part = Integer.parseInt(o2Split[i]);
            }

            final int compareTo = o1Part.compareTo(o2Part);
            if (compareTo == 0) {
                // check if there was one SNAPSHOT/RC1/beta and one release - release is
                // ofc newer despite the same number
                if (specialEndCounter == 1) {
                    if (o1Split[i].contains("-")) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (specialEndCounter == 2) {
                    final String endO1 = o1Split[i].substring(o1Split[i].lastIndexOf("-") + 1, o1Split[i].length());
                    final String endO2 = o2Split[i].substring(o2Split[i].lastIndexOf("-") + 1, o2Split[i].length());
                    // SNAPSHOT/RC1/beta lexicographically sort
                    return endO1.toLowerCase().compareTo(endO2.toLowerCase());
                } else {
                    // check another part
                    continue;
                }
            } else {
                return compareTo;
            }
        }

        final Integer o1SplitLength = (Integer) o1Split.length;
        final Integer o2SplitLength = (Integer) o2Split.length;
        return o1SplitLength.compareTo(o2SplitLength);
    }

}
