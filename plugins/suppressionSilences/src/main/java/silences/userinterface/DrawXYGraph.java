package silences.userinterface;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;

public class DrawXYGraph extends JFrame
{
	private static final long serialVersionUID = 1L;
	private XYPlot plot;
	private int index;

	public DrawXYGraph(String applicationTitle, String chartTitle, XYDataset xyDataset, String xLabel, String yLabel)
	{
		super(applicationTitle);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, xLabel, yLabel, xyDataset,
				PlotOrientation.VERTICAL, true, false, false);
		this.index = 1;
		this.plot = (XYPlot) chart.getPlot();
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(500, 270));
		chartPanel.getChartRenderingInfo().setEntityCollection(null);
		this.setContentPane(chartPanel);
	}

	public DrawXYGraph(String applicationTitle, String chartTitle, String xLabel, String yLabel)
	{
		super(applicationTitle);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, xLabel, yLabel, null,
				PlotOrientation.VERTICAL, true, false, false);
		this.plot = chart.getXYPlot();
		this.index = 0;
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(500, 270));
		chartPanel.getChartRenderingInfo().setEntityCollection(null);
		this.setContentPane(chartPanel);
	}

	public void addDataset(XYDataset xyDataset)
	{
		this.plot.setDataset(index, xyDataset);
		this.plot.setRenderer(index, new StandardXYItemRenderer());
		index++;
	}

	public void display()
	{
		this.pack();
		this.setVisible(true);
	}
}