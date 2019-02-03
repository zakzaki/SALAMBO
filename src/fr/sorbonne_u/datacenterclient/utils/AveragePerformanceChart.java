package fr.sorbonne_u.datacenterclient.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

/**
 * Chart used to trace Application Average Performance
 */
public class AveragePerformanceChart extends ApplicationFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static int positionX = 0 ;
	public static int positionY = 0 ;

	/** The time series data. */
	private TimeSeries series;

	/** The most recent value added. */
	private double lastValue = 100.0;

	/** Timer to refresh graph after every 1/4th of a second */
	private Timer timer = new Timer(250, this);

	/**
	 * Constructs a new dynamic chart application.
	 *
	 * @param title
	 *            the frame title.
	 */
	public AveragePerformanceChart(final String title) {

		super(title);
		this.series = new TimeSeries("Average", Millisecond.class);

		final TimeSeriesCollection dataset = new TimeSeriesCollection(this.series);
		final JFreeChart chart = createChart(dataset);

		timer.setInitialDelay(20);

		// Sets background color of chart
		chart.setBackgroundPaint(Color.WHITE);

		// Created JPanel to show graph on screen
		final JPanel content = new JPanel(new BorderLayout());

		// Created Chartpanel for chart area
		final ChartPanel chartPanel = new ChartPanel(chart);

		// Added chartpanel to main panel
		content.add(chartPanel);

		// Sets the size of whole window (JPanel)
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));

		// Puts the whole content on a Frame
		setContentPane(content);

		timer.start();

	}

	/**
	 * Creates a sample chart.
	 *
	 * @param dataset
	 *            the dataset.
	 *
	 * @return A sample chart.
	 */
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart result = ChartFactory.createTimeSeriesChart("Average Application Performance", "Time", "Value",
				dataset, true, true, false);

		final XYPlot plot = result.getXYPlot();

		plot.setBackgroundPaint(new Color(0xffffe0));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.lightGray);

		ValueAxis xaxis = plot.getDomainAxis();
		xaxis.setAutoRange(true);

		// Domain axis would show data of 60 seconds for a time
		xaxis.setFixedAutoRange(60000.0); // 60 seconds
		xaxis.setVerticalTickLabels(true);

		ValueAxis yaxis = plot.getRangeAxis();
		yaxis.setRange(0.0, 60.0);

		return result;
	}

	/**
	 * Trace the average of Application Performance .
	 *
	 * @param average
	 */
	public void traceAverage(double average) {
		this.series.add(new Millisecond(), average);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

}