JStripChart is a small set of Java classes producing simple strip
charts. I wrote this for JKstat, but it's standalone and designed to be
generally useful.

The files here are licensed under CDDL, version 1.0, see the file
LICENSES/CDDL-1.0.txt.

Usage is fairly simple, first:

import uk.co.petertribble.jstripchart.*;

And then create a strip chart which can be:

JStripChart - just one data point
JStripChart2 - 2 data points
JStackedStripChart2 - 2 data points, stacked

or a sparkline chart with

JSparkChart - just one data point

You can specify the size with

JStripChart jsc = new JStripChart(200, 40);

this is used both to set the preferred and minimum sizes (which your
layout manager can completely ignore), and the first determines the
number of data points to show. If at its preferred size, then, each
data point is 1 pixel wide.

You can specify the Colors with, for example

JStackedStripChart2 jsc =
	new JStackedStripChart2(Color.BLUE, Color.GREEN, Color.YELLOW);

The first color is the background, then the data colors.


By default, you get lines. To turn the charts solid,

jsc.setStyle(JStripChart.STYLE_SOLID);

(the constants are all defined in the JStripChart class).


By default, the graph autoscales to the incoming data. To turn this
off, manually set the maximum range (the bottom of the scale is always
zero), with:

jsc.setMax(1.0d);


Once you've got a chart, it's just a JPanel so you can add it to your
application as normal.


To show data, simply add some numbers:

jsc.add(x1);

or for the ones displaying 2 charts

jsc.add(x1, x2);

each time you add, it goes on the right and everything else gets pushed
along.


To get the chart moving, create a Timer loop and add the data in that.
