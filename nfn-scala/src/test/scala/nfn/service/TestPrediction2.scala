package nfn.service

import config.StaticConfig
import nfn.tools.Helpers
import org.junit.Assert.assertEquals
import org.junit.Test

import scala.io.Source

class TestPrediction2 {
  val sacepicnEnv = StaticConfig.systemPath
  val line1 = "6977,1377986405,0,1,0,0,0"
  val line2 = "22:18:38.841/Building A/O/L"
  val line3 = "6977,1378033205,0,1,0,0,0"

  val path1 = "buildings"
  val path2 = "plug0"
  /*@Test def getDelimiterFromPathTest ={
    val prediction = new Prediction2()
    val delimiter1 = Helpers.getDelimiterFromPath(path2)
    val delimiter2 = Helpers.getDelimiterFromPath(path1)
    assertEquals(",",delimiter1)
    assertEquals("/",delimiter2)
  }

  @Test def getDelimiterFromLineTest ={
    val prediction = new Prediction2()
    val delimiter1 = Helpers.getDelimiterFromLine(line1)
    val delimiter2 = Helpers.getDelimiterFromLine(line2)
    assertEquals(",",delimiter1)
    assertEquals("/",delimiter2)
  }


  @Test def getDatePositionTest = {
    val prediction = new Prediction2()
    val delimiter1 = Helpers.getDelimiterFromLine(line1)
    val delimiter2 = Helpers.getDelimiterFromLine(line2)
    val datePosition1 = Helpers.getDatePosition(delimiter1)
    val datePosition2 = Helpers.getDatePosition(delimiter2)
    assertEquals(1,datePosition1)
    assertEquals(0,datePosition2)
  }

  @Test def getValuePositionTest ={
    val prediction = new Prediction2()
    val delimiter1 = Helpers.getDelimiterFromLine(line1)
    val delimiter2 = Helpers.getDelimiterFromLine(line2)
    val valPosition1 = Helpers.getValuePosition(delimiter1)
    val valPosition2 = Helpers.getValuePosition(delimiter2)
    assertEquals(2,valPosition1)
    assertEquals(1,valPosition2)
  }

  @Test def parseTimeTest ={

    val prediction = new Prediction2()
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
  @Test def predictTestNoHistory ={
    val prediction = new Prediction2()
    val granularityInSeconds = 2*60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val plug0Path = "plug0"
    val bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + plug0Path)
    val lines = (for (line <- bufferedSource.getLines()) yield line).toList
    var historyArray = Array.ofDim[Double](historyGranularity)
    val expectedOutput = "16:24:02,84,0,0,0,20.968734996993092\n16:36:01,85,0,0,0,20.77757746252103\n"
    val output = prediction.predict(lines,historyGranularity,historyArray)
    System.out.println(output)
    assertEquals(expectedOutput,output)
  }

  @Test def predictTestWithHistory ={
    val prediction = new Prediction2()
    val granularityInSeconds = 2*60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val plug0Path = "plug0"
    val bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + plug0Path)
    val lines = (for (line <- bufferedSource.getLines()) yield line).toList
    var historyArray = Array.ofDim[Double](historyGranularity)
    historyArray(84) = 99.9
    historyArray(85) = 50.1
    val expectedOutput = "16:24:02,84,0,0,0,60.43436749849655\n16:36:01,85,0,0,0,35.438788731260516\n"
    val output = prediction.predict(lines,historyGranularity,historyArray)
    System.out.println(output)
    assertEquals(expectedOutput,output)
  }*/
}
