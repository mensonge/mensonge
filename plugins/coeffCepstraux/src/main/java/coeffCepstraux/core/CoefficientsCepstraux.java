package coeffCepstraux.core;

import java.io.File;
import java.util.List;

import javax.swing.SwingUtilities;

import mensonge.core.IExtraction;
import mensonge.core.plugins.Plugin;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import coeffCepstraux.userinterface.DrawXYGraph;

public class CoefficientsCepstraux implements Plugin
{
	private boolean isActive = false;

	private void drawGraph()
	{
		XYSeries series = new XYSeries("Canard !");
		final double phaseMultiplier = 2 * Math.PI * 5 / 500;

		for (int i = 0; i < 500; i++)
		{
			final double cycleX = i * phaseMultiplier;
			final double sineResult = Math.sin(cycleX);
			series.add(cycleX, sineResult);
		}
		final XYDataset xyDataset = new XYSeriesCollection(series);
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{

				DrawXYGraph graph = new DrawXYGraph("Canard ?", "Super graph", xyDataset);
				graph.display();
			}
		});
	}

	@Override
	public void lancer(IExtraction extraction, List<File> listeFichiersSelectionnes)
	{
		this.isActive = true;
		this.drawGraph();
		this.isActive = false;
	}

	@Override
	public void stopper()
	{
		this.isActive = false;
	}

	@Override
	public String getNom()
	{
		return "Coefficients cepstraux";
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}
}
