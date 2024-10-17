/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

package uk.co.petertribble.jstripchart;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;
import static uk.co.petertribble.jstripchart.JStripChart.DEFAULT_WIDTH;
import static uk.co.petertribble.jstripchart.JStripChart.DEFAULT_HEIGHT;

/**
 * A panel that shows a sparkline chart.
 */
public class JSparkChart extends JPanel {

    private static final long serialVersionUID = 1L;

    // how many points we save
    private int nsize;
    // the current position of the data
    private int ncur = -1;
    // the index of the largest element
    private int nmax;
    // whether we autoscale
    private boolean autoscale = true;
    // whether we have wrapped
    private boolean wrapped;

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
    private BasicStroke stroke;

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
    public JSparkChart(int width, int height) {
	this(width, height, Color.BLUE, Color.RED);
    }

    /**
     * Create an empty sparkline chart of the specified colours.
     *
     * @param bgcolor The Color to be used for the chart background
     * @param fgcolor The Color to be used for the chart foreground
     */
    public JSparkChart(Color bgcolor, Color fgcolor) {
	this(DEFAULT_WIDTH, DEFAULT_HEIGHT, bgcolor, fgcolor);
    }

    /**
     * Create an empty sparkline chart of the specified size and colours.
     *
     * @param width The desired width of the sparkline chart
     * @param height The desired height of the sparkline chart
     * @param bgcolor The Color to be used for the chart background
     * @param fgcolor The Color to be used for the chart foreground
     */
    public JSparkChart(int width, int height, Color bgcolor,
			Color fgcolor) {
	this.bgcolor = bgcolor;
	this.fgcolor = fgcolor;
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
     * Add a data point to the strip chart. Resets the scale if necessary.
     *
     * @param d the data point to add
     */
    public void add(double d) {
	ncur++;
	if (ncur == nsize) {
	    // wrap back to the beginning
	    ncur = 0;
	    wrapped = true;
	}
	values[ncur] = d;
	if (autoscale) {
	    if (dmax < d*1.1) {
		// fudge a little extra for rounding
		dmax = d*1.10001;
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
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	Dimension d = getSize();

	double h = d.height;
	double hmax = h - 1.0d;
	double hrange = h - 2.0d;
	double w = d.width;
	g2.setPaint(bgcolor);
	g2.fill(new Rectangle2D.Double(0.0d, 0.0d, w, h));

	double x = w;
	double dx = w/((double) nsize);

	g2.setPaint(fgcolor);
	g2.setStroke(stroke);

	// FIXME allow a little border

	GeneralPath path = new GeneralPath();
	/*
	 * increment x, use current values as y
	 * start at the right, work our way back through the array
	 * from the current position
	 */
	path.moveTo((float) x, (float) (hmax - hrange*values[ncur]/dmax));
	for (int i = ncur-1; i >= 0; i--) {
	    x -= dx;
	    double hh = hmax - hrange*values[i]/dmax;
	    path.lineTo((float) x, (float) hh);
	}
	/*
	 * Now go from the end of the array back to the current position, but
	 * only if the data has wrapped around.
	 */
	if (wrapped) {
	    for (int i = nsize-1; i > ncur; i--) {
		x -= dx;
		double hh = hmax - hrange*values[i]/dmax;
		path.lineTo((float) x, (float) hh);
	    }
	}
	g2.draw(path);
    }
}
