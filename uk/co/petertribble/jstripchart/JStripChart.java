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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * A panel that shows a graphical strip chart.
 */
public final class JStripChart extends JPanel {

    private static final long serialVersionUID = 1L;

    // these are imported statically by the other classes in this package
    public static final int DEFAULT_WIDTH = 80;
    public static final int DEFAULT_HEIGHT = 80;
    public static final int STYLE_LINE = 0;
    public static final int STYLE_SOLID = 1;

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
    private double[] values;

    // vertical range (always measured from zero)
    private double dmax;

    // background and foreground colours
    private Color bgcolor;
    private Color fgcolor;

    /**
     * Create an empty strip chart.
     */
    public JStripChart() {
	this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Create an empty strip chart of the specified size.
     *
     * @param width The desired width of the strip chart
     * @param height The desired height of the strip chart
     */
    public JStripChart(int width, int height) {
	this(width, height, Color.BLUE, Color.RED);
    }

    /**
     * Create an empty strip chart of the specified colours.
     *
     * @param bgcolor The Color to be used for the chart background
     * @param fgcolor The Color to be used for the chart foreground
     */
    public JStripChart(Color bgcolor, Color fgcolor) {
	this(DEFAULT_WIDTH, DEFAULT_HEIGHT, bgcolor, fgcolor);
    }

    /**
     * Create an empty strip chart of the specified size and colours.
     *
     * @param width The desired width of the strip chart
     * @param height The desired height of the strip chart
     * @param bgcolor The Color to be used for the chart background
     * @param fgcolor The Color to be used for the chart foreground
     */
    public JStripChart(int width, int height, Color bgcolor,
			Color fgcolor) {
	this.bgcolor = bgcolor;
	this.fgcolor = fgcolor;
	setMinimumSize(new Dimension(width, height));
	setPreferredSize(new Dimension(width, height));
	nsize = width;
	values = new double[nsize];
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
     * Add a data point to the strip chart.
     *
     * @param i the data point to add
     */
    public void add(int i) {
	add((double) i);
    }

    /**
     * Add a data point to the strip chart.
     *
     * @param l the data point to add
     */
    public void add(long l) {
	add((double) l);
    }

    /**
     * Add a data point to the strip chart.
     *
     * @param d the data point to add
     */
    public void add(double d) {
	if (autoscale && dmax < d * 1.1) {
	    // fudge a little extra for rounding
	    dmax = d * 1.10001;
	}
	ncur++;
	if (ncur == nsize) {
	    // wrap back to the beginning
	    ncur = 0;
	}
	values[ncur] = d;
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
	double dx = w / ((double) nsize);

	g2.setPaint(fgcolor);
	/*
	 * increment x, use current values as y
	 * start at the right, work our way back through the array
	 * from the current position
	 */
	for (int i = ncur; i >= 0; i--) {
	    x -= dx;
	    double hh = h * values[i] / dmax;
	    double dh = (style == STYLE_LINE) ? dx : hh;
	    g2.fill(new Rectangle2D.Double(x, h - hh, dx, dh));
	}
	/*
	 * Now go from the end of the array back to the current position
	 */
	for (int i = nsize - 1; i > ncur; i--) {
	    x -= dx;
	    double hh = h * values[i] / dmax;
	    double dh = (style == STYLE_LINE) ? dx : hh;
	    g2.fill(new Rectangle2D.Double(x, h - hh, dx, dh));
	}
    }
}
