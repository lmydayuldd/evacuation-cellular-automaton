package org.zet.cellularautomaton.algorithm.rule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jmock.AbstractExpectations.any;
import static org.jmock.AbstractExpectations.returnValue;
import static org.jmock.AbstractExpectations.same;
import static org.zet.cellularautomaton.algorithm.rule.RuleTestMatchers.executeableOn;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.zet.cellularautomaton.DeathCause;
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.EvacuationCellularAutomaton;
import org.zet.cellularautomaton.Individual;
import org.zet.cellularautomaton.Room;
import org.zet.cellularautomaton.RoomCell;
import org.zet.cellularautomaton.potential.StaticPotential;
import org.zet.cellularautomaton.statistic.CAStatisticWriter;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class TestInitialPotentialRandomRule {
    private final Mockery context = new Mockery();
    private InitialPotentialRandomRule rule;
    private EvacCell cell;
    private Individual i;
    private EvacuationCellularAutomaton eca;
    private EvacuationState es;
    private final static CAStatisticWriter statisticWriter = new CAStatisticWriter();

    @Before
    public void init() {
        rule = new InitialPotentialRandomRule();
        Room room = context.mock(Room.class);
        es = context.mock(EvacuationState.class);
        eca = new EvacuationCellularAutomaton();
        i = new Individual();
        context.checking(new Expectations() {
            {
                allowing(es).getCellularAutomaton();
                will(returnValue(eca));
                allowing(room).getID();
                will(returnValue(1));
                allowing(room).addIndividual(with(any(EvacCell.class)), with(i));
                allowing(room).removeIndividual(with(i));
                allowing(es).getStatisticWriter();
                will(returnValue(statisticWriter));
            }
        });
        cell = new RoomCell(1, 0, 0, room);
        i.setCell(cell);
        cell.getState().setIndividual(i);

        rule.setEvacuationSimulationProblem(es);
        eca.addIndividual(cell, i);
    }

    @Test
    public void testAppliccableIfNotEmpty() {
        cell = new RoomCell(0, 0);
        assertThat(rule, is(not(executeableOn(cell))));

        i = new Individual();
        cell.getState().setIndividual(i);
        assertThat(rule, is(executeableOn(cell)));
    }
    
    @Test
    public void testNotApplicableIfPotentialSet() {
        StaticPotential sp = new StaticPotential();
        i.setStaticPotential(sp);
        assertThat(rule, is(not(executeableOn(cell))));
    }
    
    @Test
    public void testDeadIfNoPotentials() {
        context.checking(new Expectations() {{
                exactly(1).of(es).setIndividualDead(with(i), with(DeathCause.EXIT_UNREACHABLE));
        }});
        rule.execute(cell);
    }
    
    @Test
    public void testDeadIfPotentialsBad() {
        StaticPotential sp = new StaticPotential();
        eca.addStaticPotential(sp);
        
        context.checking(new Expectations() {{
                exactly(1).of(es).setIndividualDead(with(i), with(DeathCause.EXIT_UNREACHABLE));
        }});
        rule.execute(cell);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testSinglePotentialTaken() {
        StaticPotential sp = new StaticPotential();
        sp.setPotential(cell, 1);

        eca.addStaticPotential(sp);
        
        rule.execute(cell);
        assertThat(i.isDead(), is(false));
        assertThat(i.getDeathCause(), is(nullValue()));
        assertThat(i.getStaticPotential(), is(same(sp)));
    }
    
    @Test
    public void testRandomPotentialTaken() {
        // Need to insert seed into rule to manipulate random decision
//        rule.execute(cell);
//        assertThat(i.isDead(), is(false));
//        assertThat(i.getDeathCause(), is(nullValue()));
//        assertThat(i.getStaticPotential(), is(same(targetPotential)));
    }
    
}
