package org.zet.cellularautomaton.potential;

import org.zet.cellularautomaton.EvacCellInterface;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class FakePotential implements Potential {

    @Override
    public int getPotential(EvacCellInterface cell) {
        return 1;
    }

    @Override
    public double getPotentialDouble(EvacCellInterface cell) {
        return 1.0;
    }

    @Override
    public int getMaxPotential() {
        return 1;
    }

    @Override
    public boolean hasValidPotential(EvacCellInterface cell) {
        return true;
    }
    
}
