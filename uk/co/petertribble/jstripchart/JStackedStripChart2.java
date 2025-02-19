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
import java.awt.*;
import java.awt.geom.Rectangle2D;
import static uk.co.petertribble.jstripchart.JStripChart.DEFAULT_WIDTH;
import static uk.co.petertribble.jstripchart.JStripChart.DEFAULT_HEIGHT;
import static uk.co.petertribble.jstripchart.JStripChart.STYLE_LINE;

/**
 * A panel that shows a graphical strip chart graphing 2 values.
 */
public class JStackedStripChart2 extends JPanel {

    private static final long serialVersionUID = 1L;

    // the current style
    private int style;

    // how many points we save
    private int nsize;
    private int ncur;
    // whether we autoscale
    private boolean autoscale = true;

    /*
     * We work with doubles, which is what Rectangle2D wants. We can display
     * long or int, but they're cast to double internally.
     */
    private double[] values1;
    private double[] values2;

    // vertical range (always measured from zero)
    private double dmax;

    // background and foreground colours
    private Color bgcolor;
    private Color fgcolor1;
    private Color fgcolor2;

    /**
     * Create an empty strip chart.
     */
    public JStackedStripChart2() {
	this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Create an empty strip chart of the specified size.
     *
     * @param width The desired width of the strip chart
     * @param height The desired height of the strip chart
     */
    public JStackedStripChart2(int width, int height) {
	this(width, height, Color.BLUE, Color.RED, Color.YELLOW);
    }

    /**
     * Create an empty strip chart of the specified colours.
     *
     * @param bgcolor The Color to be used for the chart background
     * @param fgcolor1 The Color to be used for the first data series
     * @param fgcolor2 The Color to be used for the second data series
     */
    public JStackedStripChart2(Color bgcolor, Color fgcolor1, Color fgcolor2) {
	this(DEFAULT_WIDTH, DEFAULT_HEIGHT, bgcolor, fgcolor1, fgcolor2);
    }

    /**
     * Create an empty strip chart of the specified size and colours.
     *
     * @param width The desired width of the strip chart
     * @param height The desired height of the strip chart
     * @param bgcolor The Color to be used for the chart background
     * @param fgcolor1 The Color to be used for the first data series
     * @param fgcolor2 The Color to be used for the second data series
     */
    public JStackedStripChart2(int width, int height, Color bgcolor,
			Color fgcolor1, Color fgcolor2) {
	this.bgcolor = bgcolor;
	this.fgcolor1 = fgcolor1;
	this.fgcolor2 = fgcolor2;
	setMinimumSize(new Dimension(width, height));
	setPreferredSize(new Dimension(width, height));
	nsize = width;
	values1 = new double[nsize];
	values2 = new double[nsize];
	dmax = 1.0;
    }

    /**
     * Set the line style to be used.
     *
     * @param style The line style to be used
     */
    public void setStyle(int style) {
	this.style = style;
    }

    /**
     * Set the maximum scale. Also forces the vertical scale to be fixed
     * rather than dynamically adjusting to the data.
     *
     * @param imax The required maximum value to be shown
     */
    public void setMax(int imax) {
	setMax((double) imax);
    }

    /**
     * Set the maximum scale. Also forces the vertical scale to be fixed
     * rather than dynamically adjusting to the data.
     *
     * @param lmax The required maximum value to be shown
     */
    public void setMax(long lmax) {
	setMax((double) lmax);
    }

    /**
     * Set the maximum scale. Also forces the vertical scale to be fixed
     * rather than dynamically adjusting to the data.
     *
     * @param dmax The required maximum value to be shown
     */
    public void setMax(double dmax) {
	this.dmax = dmax;
	autoscale = false;
    }

    /**
     * Add data to the strip chart.
     *
     * @param i1 the first data point to add
     * @param i2 the second data point to add
     */
    public void add(int i1, int i2) {
	add((double) i1, (double) i2);
    }

    /**
     * Add data to the strip chart.
     *
     * @param l1 the first data point to add
     * @param l2 the second data point to add
     */
    public void add(long l1, long l2) {
	add((double) l1, (double) l2);
    }

    /**
     * Add data to the strip chart.
     *
     * @param d1 the first data point to add
     * @param d2 the second data point to add
     */
    public void add(double d1, double d2) {
	if (autoscale && dmax < d1*1.1) {
	    // fudge a little extra for rounding
	    dmax = d1*1.10001;
	}
	if (autoscale && dmax < d2*1.1) {
	    // fudge a little extra for rounding
	    dmax = d2*1.10001;
	}
	ncur ++;
	if (ncur == nsize) {
	    // wrap back to the beginning
	    ncur = 0;
	}
	values1[ncur] = d1;
	values2[ncur] = d2;
	repaint();
    }

    @Override
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	Dimension d = getSize();

	double h = d.height;
	double w = d.width;
	g2.setPaint(bgcolor);
	g2.fill(new Rectangle2D.Double(0.0d, 0.0d, w, h));

	double x = w;
	double dx = w/((double) nsize);

	// first paint the 2nd data point in the background
	g2.setPaint(fgcolor2);
	/*
	 * increment x, use current values as y
	 * start at the right, work our way back through the array
	 * from the current position
	 */
	for (int i = ncur; i >= 0; i--) {
	    x -= dx;
	    double hh2 = h*values2[i]/dmax;
	    double hh1 = h*values1[i]/dmax;
	    double dh = (style == STYLE_LINE) ? dx : hh2;
	    g2.fill(new Rectangle2D.Double(x, h-(hh1+hh2), dx, dh));
	}
	/*
	 * Now go from the end of the array back to the current position
	 */
	for (int i = nsize-1; i > ncur; i--) {
	    x -= dx;
	    double hh2 = h*values2[i]/dmax;
	    double hh1 = h*values1[i]/dmax;
	    double dh = (style == STYLE_LINE) ? dx : hh2;
	    g2.fill(new Rectangle2D.Double(x, h-(hh1+hh2), dx, dh));
	}

	// now paint the 1st data point in the foreground
	g2.setPaint(fgcolor1);
	x = w;
	for (int i = ncur; i >= 0; i--) {
	    x -= dx;
	    double hh = h*values1[i]/dmax;
	    double dh = (style == STYLE_LINE) ? dx : hh;
	    g2.fill(new Rectangle2D.Double(x, h-hh, dx, dh));
	}
	for (int i = nsize-1; i > ncur; i--) {
	    x -= dx;
	    double hh = h*values1[i]/dmax;
	    double dh = (style == STYLE_LINE) ? dx : hh;
	    g2.fill(new Rectangle2D.Double(x, h-hh, dx, dh));
	}
    }
}
