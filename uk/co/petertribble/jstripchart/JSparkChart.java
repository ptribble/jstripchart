/*
 * SPDX-License-Identifier: CDDL-1.0
 *
 * CDDL HEADER START
 *
 * This file and its contents are supplied under the terms of the
 * Common Development and Distribution License ("CDDL"), version 1.0.
 * You may only use this file in accordance with the terms of version
 * 1.0 of the CDDL.
 *
 * A full copy of the text of the CDDL should have accompanied this
 * source. A copy of the CDDL is also available via the Internet at
 * http://www.illumos.org/license/CDDL.
 *
 * CDDL HEADER END
 *
 * Copyright 2025 Peter Tribble
 *
 */

package uk.co.petertribble.jstripchart;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;
import static uk.co.petertribble.jstripchart.JStripChart.DEFAULT_WIDTH;
import static uk.co.petertribble.jstripchart.JStripChart.DEFAULT_HEIGHT;

/**
 * A panel that shows a sparkline chart.
 */
public final class JSparkChart extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * How many points we save.
     */
    private int nsize;
    /**
     * The current position of the data.
     */
    private int ncur = -1;
    /**
     * The index of the largest element.
     */
    private int nmax;
    /**
     * Whether we autoscale.
     */
    private boolean autoscale = true;
    /**
     * Whether we have wrapped.
     */
    private boolean wrapped;

    /**
     * The values to display.
     *
     * We work with doubles, which is what Rectangle2D wants. We can display
     * long or int, but they're cast to double internally.
     */
    private double[] values;

    /**
     * Vertical range (always measured from zero).
     */
    private double dmax;

    /**
     * The background color.
     */
    private Color bgcolor;
    /**
     * The foreground color.
     */
    private Color fgcolor;
    /**
     * The Stroke used to draw the sparkline.
     */
    private transient BasicStroke stroke;

    /**
     * Create an empty sparkline chart.
     */
    public JSparkChart() {
	this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Create an empty sparkline chart of the specified size.
     *
     * @param width The desired width of the sparkline chart
     * @param height The desired height of the sparkline chart
     */
    public JSparkChart(final int width, final int height) {
	this(width, height, Color.BLUE, Color.RED);
    }

    /**
     * Create an empty sparkline chart of the specified colours.
     *
     * @param nbcolor The Color to be used for the chart background
     * @param nfcolor The Color to be used for the chart foreground
     */
    public JSparkChart(final Color nbcolor, final Color nfcolor) {
	this(DEFAULT_WIDTH, DEFAULT_HEIGHT, nbcolor, nfcolor);
    }

    /**
     * Create an empty sparkline chart of the specified size and colours.
     *
     * @param width The desired width of the sparkline chart
     * @param height The desired height of the sparkline chart
     * @param nbcolor The Color to be used for the chart background
     * @param nfcolor The Color to be used for the chart foreground
     */
    public JSparkChart(final int width, final int height, final Color nbcolor,
			final Color nfcolor) {
	bgcolor = nbcolor;
	fgcolor = nfcolor;
	setMinimumSize(new Dimension(width, height));
	setPreferredSize(new Dimension(width, height));
	nsize = width;
	values = new double[nsize];
	dmax = 1.0;
	stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
    }

    /**
     * Set the maximum scale. Also forces the vertical scale to be fixed
     * rather than dynamically adjusting to the data.
     *
     * @param imax The required maximum value to be shown
     */
    public void setMax(final int imax) {
	setMax((double) imax);
    }

    /**
     * Set the maximum scale. Also forces the vertical scale to be fixed
     * rather than dynamically adjusting to the data.
     *
     * @param lmax The required maximum value to be shown
     */
    public void setMax(final long lmax) {
	setMax((double) lmax);
    }

    /**
     * Set the maximum scale. Also forces the vertical scale to be fixed
     * rather than dynamically adjusting to the data.
     *
     * @param ndmax The required maximum value to be shown
     */
    public void setMax(final double ndmax) {
	dmax = ndmax;
	autoscale = false;
    }

    /**
     * Add a data point to the strip chart.
     *
     * @param i the data point to add
     */
    public void add(final int i) {
	add((double) i);
    }

    /**
     * Add a data point to the strip chart.
     *
     * @param l the data point to add
     */
    public void add(final long l) {
	add((double) l);
    }

    /**
     * Add a data point to the strip chart. Resets the scale if necessary.
     *
     * @param d the data point to add
     */
    public void add(final double d) {
	ncur++;
	if (ncur == nsize) {
	    // wrap back to the beginning
	    ncur = 0;
	    wrapped = true;
	}
	values[ncur] = d;
	if (autoscale) {
	    if (dmax < d * 1.1) {
		// fudge a little extra for rounding
		dmax = d * 1.10001;
		nmax = ncur;
	    } else if (nmax == ncur) {
		// we replace the old maximum, so recalculate
		resetMax();
	    }
	}
	repaint();
    }

    private void resetMax() {
	dmax = values[0];
	nmax = 0;
	for (int i = 1; i < nsize; i++) {
	    if (values[i] > dmax) {
		dmax = values[i];
		nmax = i;
	    }
	}
	dmax *= 1.10001;
	/*
	 * Ugh. This avoids setting dmax to zero.
	 */
	dmax += 0.00001;
    }

    @Override
    public void paint(final Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	Dimension d = getSize();

	double h = d.height;
	double hmax = h - 1.0d;
	double hrange = h - 2.0d;
	double w = d.width;
	g2.setPaint(bgcolor);
	g2.fill(new Rectangle2D.Double(0.0d, 0.0d, w, h));

	double x = w;
	double dx = w / ((double) nsize);

	g2.setPaint(fgcolor);
	g2.setStroke(stroke);

	// FIXME allow a little border

	GeneralPath path = new GeneralPath();
	/*
	 * increment x, use current values as y
	 * start at the right, work our way back through the array
	 * from the current position
	 */
	path.moveTo((float) x, (float) (hmax - hrange * values[ncur] / dmax));
	for (int i = ncur - 1; i >= 0; i--) {
	    x -= dx;
	    double hh = hmax - hrange * values[i] / dmax;
	    path.lineTo((float) x, (float) hh);
	}
	/*
	 * Now go from the end of the array back to the current position, but
	 * only if the data has wrapped around.
	 */
	if (wrapped) {
	    for (int i = nsize - 1; i > ncur; i--) {
		x -= dx;
		double hh = hmax - hrange * values[i] / dmax;
		path.lineTo((float) x, (float) hh);
	    }
	}
	g2.draw(path);
    }
}
