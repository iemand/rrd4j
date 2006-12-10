/* ============================================================
 * Rrd4j : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.rrd4j.org
 * Project Lead:  Mathias Bogaert (m.bogaert@memenco.com)
 *
 * (C) Copyright 2003-2006, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic
 *
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.rrd4j.core;

import java.io.IOException;

/**
 * Backend to be used to store all RRD bytes in memory.
 */
public class RrdMemoryBackend extends RrdBackend {
    private byte[] buffer = new byte[0];

    protected RrdMemoryBackend(String path) {
        super(path);
    }

    protected synchronized void write(long offset, byte[] b) throws IOException {
        int pos = (int) offset;
        for (byte aB : b) {
            buffer[pos++] = aB;
        }
    }

    protected synchronized void read(long offset, byte[] b) throws IOException {
        int pos = (int) offset;
        if (pos + b.length <= buffer.length) {
            for (int i = 0; i < b.length; i++) {
                b[i] = buffer[pos++];
            }
        }
        else {
            throw new IOException("Not enough bytes available in memory " + getPath());
        }
    }

    /**
     * Returns the number of RRD bytes held in memory.
     *
     * @return Number of all RRD bytes.
     */
    public long getLength() {
        return buffer.length;
    }

    /**
     * Reserves a memory section as a RRD storage.
     *
     * @param newLength Number of bytes held in memory.
     *
     * @throws IOException Thrown in case of I/O error.
     */
    protected void setLength(long newLength) throws IOException {
        if (newLength > Integer.MAX_VALUE) {
            throw new IOException("Cannot create this big memory backed RRD");
        }
        buffer = new byte[(int) newLength];
    }

    /**
     * This method is required by the base class definition, but it does not
     * releases any memory resources at all.
     */
    public void close() {
        // NOP
    }

    /**
     * This method is overriden to disable high-level caching in frontend Rrd4j classes.
     * @return Always returns <code>false</code>. There is no need to cache anything in high-level classes
     * since all RRD bytes are already in memory.
     */
    protected boolean isCachingAllowed() {
        return false;
	}
}
