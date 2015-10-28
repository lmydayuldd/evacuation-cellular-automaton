/* zet evacuation tool copyright (c) 2007-15 zet evacuation team
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
package org.zet.cellularautomaton;

/**
 * Describes the cause of death if an individual dies.
 */
public enum DeathCause {

    /**
     * If no exit is reachable. Happens if a person is surrounded by barriers.
     */
    ExitUnreachable,
    /**
     * If the {@code Individual} is inside the building when the maximum evacuation time is over.
     */
    NotEnoughTime;
}