package nfn.service
import config.StaticConfig
import org.junit.Assert._
import org.junit.Test

import scala.io.Source

class TestHeatmap {
  val sacepicnEnv = StaticConfig.systemPath
  @Test def heatmapPrinterTest = {
    val heatmap = new Heatmap()
    val map = Array.ofDim[Int](20,10)
    map(0)(0) = 1
    map(0)(9) = 2
    map(19)(0) = 3
    map(19)(9) = 4
    map(5)(5) = 5
    //heatmap.heatmapPrinter(map)
    //System.out.println(heatmap.heatmapPrinter(map))
  }

  @Test def generateHeatmapTest={
    val minLat = "51.7832946777344"
    val maxLat = "51.8207664489746"
    val minLong = "8.7262659072876"
    val maxLong = "8.8215389251709"

    //minLat = "51.81837844848633"
    //minLong = "8.727282524108887"
    val granularity = "0.0015"
    val plug0Path = "gps2"
    val plug3Path = "gps3"
    val bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + plug0Path)
    val lines = (for (line <- bufferedSource.getLines()) yield line).toList
    val heatmap = new Heatmap()
    System.out.println("Heatmap:")
    System.out.print(heatmap.generateIntermediateHeatmap(heatmap.generateHeatmap(lines,granularity.toDouble,minLong.toDouble,maxLong.toDouble,minLat.toDouble,maxLat.toDouble)))
  }
}
