package nfn.service
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import config.StaticConfig
import nfn.tools.Helpers
import org.junit.Assert._
import org.junit.Test

import scala.io.Source

class TestPrediction1 {
  val sacepicnEnv = StaticConfig.systemPath
  val line1 = "6977,1377986405,0,1,0,0,0"
  val line2 = "22:18:38.841/Building A/O/L"
  val line3 = "6977,1378033205,0,1,0,0,0"

  val path1 = "buildings"
  val path2 = "plug0"
  val lb = "16:22:00.000"
  val ub = "16:27:00.000"
  val DateTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
  val startTime = LocalTime.parse(lb, DateTimeFormat)
  val endTime = LocalTime.parse(ub, DateTimeFormat)
  val window = new Window()


  @Test def getDelimiterFromPathTest ={
    val prediction = new Prediction1()
    val delimiter1 = Helpers.getDelimiterFromPath(path2)
    val delimiter2 = Helpers.getDelimiterFromPath(path1)
    assertEquals(",",delimiter1)
    assertEquals("/",delimiter2)
  }

  @Test def getDelimiterFromLineTest ={
    val prediction = new Prediction1()
    val delimiter1 = Helpers.getDelimiterFromLine(line1)
    val delimiter2 = Helpers.getDelimiterFromLine(line2)
    assertEquals(",",delimiter1)
    assertEquals("/",delimiter2)
  }


  @Test def getDatePositionTest = {
    val prediction = new Prediction1()
    val delimiter1 = Helpers.getDelimiterFromLine(line1)
    val delimiter2 = Helpers.getDelimiterFromLine(line2)
    val datePosition1 = Helpers.getDatePosition(delimiter1)
    val datePosition2 = Helpers.getDatePosition(delimiter2)
    assertEquals(1,datePosition1)
    assertEquals(0,datePosition2)
  }

  @Test def getValuePositionTest ={
    val prediction = new Prediction1()
    val delimiter1 = Helpers.getDelimiterFromLine(line1)
    val delimiter2 = Helpers.getDelimiterFromLine(line2)
    val valPosition1 = Helpers.getValuePosition(delimiter1)
    val valPosition2 = Helpers.getValuePosition(delimiter2)
    assertEquals(2,valPosition1)
    assertEquals(1,valPosition2)
  }

 @Test def parseTimeTest ={

   val prediction = new Prediction1()
   val delimiter1 = Helpers.getDelimiterFromLine(line1)
   val delimiter2 = Helpers.getDelimiterFromLine(line2)
   val delimiter3 = Helpers.getDelimiterFromLine(line3)
   val datePosition1 = Helpers.getDatePosition(delimiter1)
   val datePosition2 = Helpers.getDatePosition(delimiter2)
   val datePosition3 = Helpers.getDatePosition(delimiter3)
   val timeVal1 = line1.split(delimiter1)(datePosition1)
   val timeVal2 = line2.split(delimiter2)(datePosition2)
   val timeVal3 = line3.split(delimiter3)(datePosition3)
   assertEquals("00:00:05",Helpers.parseTime(timeVal1,delimiter1).toString)
   assertEquals("22:18:38.841",Helpers.parseTime(timeVal2,delimiter2).toString)
   assertEquals("13:00:05",Helpers.parseTime(timeVal3,delimiter3).toString)

 }
  @Test def predict1TestNoHistory ={
    val prediction = new Prediction1()
    val granularityInSeconds = 2*60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val plugPath = "plug0"
    val bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + plugPath)
    val lines = window.readBoundSensor(plugPath,startTime,endTime,"debugTest").split("\n").toList.sortWith(_.split(",")(1).toInt<_.split(",")(1).toInt)
    val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
    val expectedOutput = "16:24:02.000,494,0,0,0,19.716909090909088\n16:24:02.000,494,0,19.716909090909088\n16:26:01.000,495,0,0,0,20.05081818181818\n16:26:01.000,495,0,20.05081818181818\n"
    val output = prediction.predict(lines,40,18,20,granularityInSeconds,historyArray)
    //System.out.println(output)
    assertEquals(expectedOutput,output)
  }

  @Test def predict1TestWithHistory ={
    val prediction = new Prediction1()
    val granularityInSeconds = 2*60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val plugPath = "plug0"
    val bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + plugPath)
    val lines = window.readBoundSensor(plugPath,startTime,endTime,"debugTest").split("\n").toList.sortWith(_.split(",")(1).toInt<_.split(",")(1).toInt)
    val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
    historyArray(84)(0)(0)(0) = 99.9
    historyArray(85)(0)(0)(0) = 50.1
    val expectedOutput = "16:24:02.000,494,0,0,0,19.716909090909088\n16:24:02.000,494,0,19.716909090909088\n16:26:01.000,495,0,0,0,20.05081818181818\n16:26:01.000,495,0,20.05081818181818\n"
    val output = prediction.predict(lines,40,18,20,granularityInSeconds,historyArray)
    //System.out.println(output)
    assertEquals(expectedOutput,output)
  }

  @Test def predict2TestNoHistory ={
    val prediction = new Prediction1()
    val granularityInSeconds = 2*60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val plugPath = "plug1"
    val bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + plugPath)
    val lines = window.readBoundSensor(plugPath,startTime,endTime,"debugTest").split("\n").toList.sortWith(_.split(",")(1).toInt<_.split(",")(1).toInt)
    val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
    val expectedOutput = "16:24:00.000,494,0,0,1,63.70662711864407\n16:24:00.000,494,0,63.70662711864407\n16:26:01.000,495,0,0,1,62.209049180327845\n16:26:01.000,495,0,62.209049180327845\n"
    val output = prediction.predict(lines,40,18,20,granularityInSeconds,historyArray)
    //System.out.println(output)
    assertEquals(expectedOutput,output)
  }

  @Test def predict2TestWithHistory ={
    val prediction = new Prediction1()
    val granularityInSeconds = 2*60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val plugPath = "plug1"
    val bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + plugPath)
    val lines = window.readBoundSensor(plugPath,startTime,endTime,"debugTest").split("\n").toList.sortWith(_.split(",")(1).toInt<_.split(",")(1).toInt)
    val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
    historyArray(84)(0)(0)(1) = 99.9
    historyArray(85)(0)(0)(1) = 50.1
    val expectedOutput = "16:24:00.000,494,0,0,1,63.70662711864407\n16:24:00.000,494,0,63.70662711864407\n16:26:01.000,495,0,0,1,62.209049180327845\n16:26:01.000,495,0,62.209049180327845\n"
    val output = prediction.predict(lines,40,18,20,granularityInSeconds,historyArray)
    //System.out.println(output)
    assertEquals(expectedOutput,output)
  }

  @Test def predict3TestNoHistory ={
    val prediction = new Prediction1()
    val granularityInSeconds = 2*60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val plugPath = "combindedPlugData"
    val lines = window.readBoundSensor(plugPath,startTime,endTime,"debugTest").split("\n").toList.sortWith(_.split(",")(1).toInt<_.split(",")(1).toInt)
    val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
    val expectedOutput = "16:24:00.000,494,0,0,0,19.716909090909088\n16:24:00.000,494,0,0,1,63.70662711864407\n16:24:00.000,494,0,41.71176810477658\n16:26:01.000,495,0,0,0,20.05081818181818\n16:26:01.000,495,0,0,1,62.209049180327845\n16:26:01.000,495,0,41.12993368107301\n"
    val output = prediction.predict(lines,40,18,20,granularityInSeconds,historyArray)
    //System.out.println(output)
    assertEquals(expectedOutput,output)
  }

}
