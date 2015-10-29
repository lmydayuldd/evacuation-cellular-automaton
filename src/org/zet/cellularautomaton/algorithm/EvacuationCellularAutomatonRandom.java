/* zet evacuation tool copyright (c) 2007-14 zet evacuation team
 *
 * This program is free software; you can redistribute it and/or
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.zet.cellularautomaton.algorithm;

import org.zetool.rndutils.RandomUtils;
import org.zet.cellularautomaton.Individual;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EvacuationCellularAutomatonRandom extends EvacuationCellularAutomatonAlgorithm {

    @Override
    public List<Individual> getIndividuals() {
        Individual[] indArray = getProblem().getCa().getIndividuals().toArray(new Individual[0]);
        // permute
        for (int i = indArray.length - 1; i >= 0; i--) {
            int randomNumber = (RandomUtils.getInstance()).getRandomGenerator().nextInt(i + 1);
            Individual t = indArray[i];	// Save position i
            indArray[i] = indArray[randomNumber];	// Store randomNumber at i
            indArray[randomNumber] = t;	// Set Individual from i to randomNumber
        }
        return Collections.unmodifiableList(Arrays.asList(indArray));
    }

    @Override
    public String toString() {
        return "EvacuationCellularAutomatonRandom";
    }

}
