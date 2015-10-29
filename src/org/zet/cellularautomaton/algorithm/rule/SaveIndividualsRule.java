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
package org.zet.cellularautomaton.algorithm.rule;

import org.zetool.common.util.IOTools;

public class SaveIndividualsRule extends AbstractSaveRule {
	static String nord = IOTools.getNextFreeNumberedFilename( "./", "results_nord", 3 );
	static String south = IOTools.getNextFreeNumberedFilename( "./", "results_süd", 3 );
	// muss VOR der EvacuateIndividualsRule aufgerufen werden!
	public SaveIndividualsRule() {
	}

	@Override
	protected void onExecute( org.zet.cellularautomaton.EvacCell cell ) {
		org.zet.cellularautomaton.Individual savedIndividual = cell.getIndividual();
		if( !(savedIndividual.isSafe()) ) {
//		try {
//			//System.out.println( savedIndividual.getStaticPotential().getName() );
//			// Write to file
//			File f = null;
////			if( savedIndividual.getStaticPotential().getName().equals( "Nordausgang" ) )
////				f = new File( "./" + nord + ".txt" );
////			else
////				f = new File( "./" + south + ".txt" );
////			FileWriter w = new FileWriter( f, true );
////			Double d = savedIndividual.getStepEndTime() * esp.getCa().getSecondsPerStep();
////			Double d2 = esp.getCa().getTimeStep() * esp.getCa().getSecondsPerStep();
////			w.append( Double.toString(  d  ) + '\n' );
////			w.close();
//		} catch( IOException ex ) {
//
//		}

			//esp.getCa().decreaseNrOfLivingAndNotSafeIndividuals();
			//savedIndividual.setSafe();
			esp.getCa().setIndividualSave( savedIndividual );
			savedIndividual.setPanic( 0 );
			//savedIndividual.setSafetyTime(esp.getCa().getTimeStep());

			if( cell instanceof org.zet.cellularautomaton.SaveCell ) {
				org.zet.cellularautomaton.StaticPotential correspondingExitPotential = ((org.zet.cellularautomaton.SaveCell) cell).getExitPotential();
				if( correspondingExitPotential == null ) {
					savedIndividual.setStaticPotential( esp.getCa().getPotentialManager().getSafePotential() );
				//throw new IllegalStateException("An individual is in a save area with no exit");
				} else {
					if( savedIndividual.getStaticPotential() != correspondingExitPotential ) {
						esp.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addChangedPotentialToStatistic( savedIndividual, esp.getCa().getTimeStep() );
						savedIndividual.setStaticPotential( correspondingExitPotential );
					}
					esp.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addExitToStatistic( savedIndividual, correspondingExitPotential );
				}
			}

			esp.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addSafeIndividualToStatistic( savedIndividual );

		}
	// else: nothing!
	}

	@Override
	public boolean executableOn( org.zet.cellularautomaton.EvacCell cell ) {
		// Regel NUR anwendbar, wenn auf der Zelle ein Individuum steht
		// und die Zelle eine Exit- Savecell oder  ist
		return (cell.getIndividual() != null) && ((cell instanceof org.zet.cellularautomaton.ExitCell) || (cell instanceof org.zet.cellularautomaton.SaveCell));
	}
}

