package ru.kledrock.discord.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChartGenerator {
    public static File generateTpsChart(List<Double> tpsHistory) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < tpsHistory.size(); i++) {
            dataset.addValue(tpsHistory.get(i), "TPS", String.valueOf(i + 1));
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "TPS Monitor", "Time", "TPS", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        File chartFile = File.createTempFile("tps_chart", ".png");
        ChartUtils.saveChartAsPNG(chartFile, lineChart, 800, 400);
        return chartFile;
    }
}