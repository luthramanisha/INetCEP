package nfn.tools

import java.util

import org.junit.Test
import nfn.service.{Prediction1, Window}
import org.junit.Assert._

class TestMathHelper {
  val path = "combindedPlugData"
  val window = new Window()
  val prediction1 = new Prediction1()

  @Test
  def testAllEqual = {

    val startTime = Helpers.parseTime("16:22:00.000", "")
    val endTime = Helpers.parseTime("16:27:00.000", "")
    val relevantData = window.readBoundSensor(path, startTime, endTime, "debugTest").split("\n").toList.sortWith(_.split(",")(1).toInt < _.split(",")(1).toInt)
    val granularity = "2m"
    val granularityInSeconds = 2 * 60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
    val prediction1_output = prediction1.predict(relevantData, 40, 18, 20, granularityInSeconds, historyArray)
    val actualOutput = MathHelper.getPrecisionRecallAccuracyFMeasure(startTime, endTime, granularity, prediction1_output.split("\n").toList)
    System.out.println(prediction1_output)
    assertTrue(util.Arrays.equals(actualOutput.toArray,Seq(1.0,1.0,1.0,1.0).toArray))
  }
  @Test
  def testOneTupleMore={
    val startTime = Helpers.parseTime("16:22:00.000", "")
    val endTime = Helpers.parseTime("16:27:00.000", "")
    val relevantData = window.readBoundSensor(path, startTime, endTime, "debugTest").split("\n").toList.sortWith(_.split(",")(1).toInt < _.split(",")(1).toInt)
    val granularity = "2m"
    val granularityInSeconds = 2 * 60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
    var prediction1_output = prediction1.predict(relevantData, 40, 18, 20, granularityInSeconds, historyArray)
    prediction1_output = prediction1_output + s"16:28:01.000,495,0,40.12993368107301"
    val actualOutput = MathHelper.getPrecisionRecallAccuracyFMeasure(startTime, endTime, granularity, prediction1_output.split("\n").toList)
    assertTrue(util.Arrays.equals(actualOutput.toArray,Seq(0.8,1.0,0.8,0.888888888888889).toArray))
  }
  @Test
  def testOneTupleMissing={
    val startTime = Helpers.parseTime("16:22:00.000", "")
    val endTime = Helpers.parseTime("16:27:00.000", "")
    val relevantData = window.readBoundSensor(path, startTime, endTime, "debugTest").split("\n").toList.sortWith(_.split(",")(1).toInt < _.split(",")(1).toInt)
    val granularity = "2m"
    val granularityInSeconds = 2 * 60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
    var prediction1_output = prediction1.predict(relevantData, 40, 18, 20, granularityInSeconds, historyArray)
    val actualOutput = MathHelper.getPrecisionRecallAccuracyFMeasure(startTime, endTime, granularity, prediction1_output.split("\n").toList.dropRight(1))
    assertTrue(util.Arrays.equals(actualOutput.toArray,Seq(1.0,0.75,0.75,0.8571428571428571).toArray))
  }
  @Test
  def testOneWrongTuple={
    val startTime = Helpers.parseTime("16:22:00.000", "")
    val endTime = Helpers.parseTime("16:27:00.000", "")
    val relevantData = window.readBoundSensor(path, startTime, endTime, "debugTest").split("\n").toList.sortWith(_.split(",")(1).toInt < _.split(",")(1).toInt)
    val granularity = "2m"
    val granularityInSeconds = 2 * 60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
    var prediction1_output = prediction1.predict(relevantData, 40, 18, 20, granularityInSeconds, historyArray)
    //System.out.println(prediction1_output)
    val output = prediction1_output.split("\n").toList.dropRight(1)
    val newOutput =  s"16:28:01.000,495,0,40.12993368107301" :: output
    val actualOutput = MathHelper.getPrecisionRecallAccuracyFMeasure(startTime, endTime, granularity, newOutput)
    //System.out.println(actualOutput)
    assertTrue(util.Arrays.equals(actualOutput.toArray,Seq(0.75, 0.75, 0.6, 0.75).toArray))
  }
}
