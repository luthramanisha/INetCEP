package nfn.service

import config.StaticConfig
import org.junit.Test

class GoldStandards {
  val sacepicnEnv = StaticConfig.systemPath
  val gps1SensorPath = "gps1"
  val gps2SensorPath = "gps2"
  val plug1SensorPath = "plug0"
  val plug2SensorPath = "plug1"
  val victimsSensorPath = "victims"
  val survivorsSensorPath = "survivors"

  @Test
  def q1NewGoldStandard() = {

    val path1 = victimsSensorPath
    var a = 0
    val executionTimes = 30
    System.out.println("Q1 New Gold Standard: ")
    for (a <- 1 to executionTimes) {
      val window1 = new Window()
      System.out.println("Query Result: ")
      System.out.println(window1.readRelativeTimedSensor(path1, 4, "S", "debugTest"))
    }
  }

  @Test
   def q1GoldStandard()={
    val window = new Window()
    val path = gps1SensorPath
    var a = 0
    val executionTimes = 30
    System.out.println("Q1 Gold Standard: ")
    for (a <- 1 to executionTimes){
      System.out.println("Query Result: ")
      System.out.println(window.readRelativeTimedSensor(path,4,"S","debugTest"))
    }
  }

  @Test
  def q2NewGoldStandard() = {

    val filter = new Filter()
    val path1 = victimsSensorPath
    var a = 0
    val executionTimes = 30
    var arguments1 = filter.parseFilterArguments("3=M&4>30")
    System.out.println("Q2 New Gold Standard: ")
    for (a <- 1 to executionTimes) {
      val window1 = new Window()
      System.out.println("Query Result: ")
      val res = filter.filter("name", window1.readRelativeTimedSensor(path1, 4, "S", "debugTest"), arguments1(0), arguments1(1), "/").stripSuffix("\n")
      if(res == "")
        System.out.println("No Results!")
      else
        System.out.println(res)
    }
  }

  @Test
  def q2GoldStandard() = {
    val window = new Window()
    val filter = new Filter()
    val path = gps1SensorPath
    var a = 0
    val executionTimes = 30
    var arguments = filter.parseFilterArguments("3>50")
    System.out.println("Q2 Gold Standard: ")
    for (a <- 1 to executionTimes) {
      System.out.println("Query Result: ")
      val res = filter.filter("name", window.readRelativeTimedSensor(path, 4, "S", "debugTest"), arguments(0), arguments(1), ";").stripSuffix(("\n"))
      if(res == "")
        System.out.println("No Results!")
      else
        System.out.println(res)
    }
  }

  @Test
  def q3NewGoldStandard() = {

    val join = new Join()
    val filter = new Filter()
    val path1 = victimsSensorPath
    val path2 = survivorsSensorPath
    var a = 0
    val executionTimes = 30
    var arguments1 = filter.parseFilterArguments("3=f&4>25")
    var arguments2 = filter.parseFilterArguments("3=M&4>30")
    System.out.println("Q3 New Gold Standard: ")
    for (a <- 1 to executionTimes) {
      val window1 = new Window()
      val window2 = new Window()
      System.out.println("Query Result: ")
      val res = join.joinStreams(filter.filter("name", window1.readRelativeTimedSensor(path1, 4, "S", "debugTest"), arguments2(0), arguments2(1), "/").stripSuffix(("\n")),filter.filter("name", window2.readRelativeTimedSensor(path2, 4, "S", "debugTest"), arguments1(0), arguments1(1), "/").stripSuffix(("\n"))).stripSuffix("\n")
      if(res == "")
        System.out.println("No Results!")
      else
        System.out.println(res)
    }
  }

  @Test
  def q3GoldStandard() = {
    val window1 = new Window()
    val window2 = new Window()
    val join = new Join()
    val filter = new Filter()
    val path1 = gps1SensorPath
    val path2 = gps2SensorPath
    var a = 0
    val executionTimes = 30
    var arguments = filter.parseFilterArguments("3>50")
    System.out.println("Q3 Gold Standard: ")
    for (a <- 1 to executionTimes) {
      System.out.println("Query Result: ")
      System.out.println(join.joinStreams(filter.filter("name", window1.readRelativeTimedSensor(path1, 4, "S", "debugTest"), arguments(0), arguments(1), ";").stripSuffix(("\n")),filter.filter("name", window1.readRelativeTimedSensor(path2, 4, "S", "debugTest"), arguments(0), arguments(1), ";").stripSuffix(("\n"))).stripSuffix("\n"))
    }
  }

  @Test
  def q4GoldStandard()={

    val join = new Join()
    val path1 = plug1SensorPath
    val path2 = plug2SensorPath
    var a = 0
    val executionTimes = 120
    System.out.println("Q4 Gold Standard: ")
    for (a <- 1 to executionTimes){
      val window1 = new Window()
      val window2 = new Window()
      val prediction1 = new Prediction2
      val prediction2 = new Prediction2
      System.out.println("Query Result: ")
      var retVal = join.joinStreams(prediction1.predict(window1.readRelativeTimedSensor(path1,1,"M","debugTest").stripSuffix("\n").split("\n").toList,"30s").stripSuffix("\n"),prediction2.predict(window2.readRelativeTimedSensor(path2,1,"M","debugTest").stripSuffix("\n").split("\n").toList,"30s").stripSuffix("\n")).stripSuffix("\n")
      if(retVal != "")
        System.out.println(retVal.stripSuffix("\n"))
      else
        System.out.println("No Results!")
    }
  }

  @Test
  def q5GoldStandard()={

    val join = new Join()
    val filter = new Filter()
    val path1 = plug1SensorPath
    val path2 = plug2SensorPath
    var a = 0
    val executionTimes = 120
    var arguments = filter.parseFilterArguments("6>50")
    System.out.println("Q5 Gold Standard: ")
    for (a <- 1 to executionTimes){
      val window1 = new Window()
      val window2 = new Window()

      val prediction1 = new Prediction2
      val prediction2 = new Prediction2
      System.out.println("Query Result: ")
      var retVal = filter.filter("name",join.joinStreams(prediction1.predict(window1.readRelativeTimedSensor(path1,1,"M","debugTest").stripSuffix("\n").split("\n").toList,"30s").stripSuffix("\n"),prediction2.predict(window2.readRelativeTimedSensor(path2,1,"M","debugTest").stripSuffix("\n").split("\n").toList,"30s").stripSuffix("\n")).stripSuffix("\n"), arguments(0), arguments(1), ",").stripSuffix("\n").stripMargin('#')
      if(retVal != "")
        System.out.println(retVal)
      else
        System.out.println("No Results!")
    }
  }
  @Test
  def q6GoldStandard(): Unit ={

    val join = new Join()
    val heatmap = new Heatmap()
    val path1 = gps1SensorPath
    val path2 = gps2SensorPath
    val minLat = "51.7832946777344"
    val maxLat = "51.8207664489746"
    val minLong = "8.7262659072876"
    val maxLong = "8.8215389251709"
    val granularity = "0.0015"
    var a = 0
    val executionTimes = 120
    System.out.println("Q6 Gold Standard: ")
    for (a <- 1 to executionTimes){
      val window1 = new Window()
      val window2 = new Window()
      System.out.println("Query Result: ")
      val lines = join.joinStreams(window1.readRelativeTimedSensor(path1, 2, "S", "debugTest"),window2.readRelativeTimedSensor(path2, 2, "S", "debugTest")).split("\n").toList
      var retVal = heatmap.generateIntermediateHeatmap(heatmap.generateHeatmap(lines,granularity.toDouble,minLong.toDouble,maxLong.toDouble,minLat.toDouble,maxLat.toDouble))
      System.out.println(retVal)
    }
  }
}
