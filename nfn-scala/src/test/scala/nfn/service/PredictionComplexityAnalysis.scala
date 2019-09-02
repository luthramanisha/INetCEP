package nfn.service

import java.io.FileNotFoundException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import org.junit.Test
import config.StaticConfig
import nfn.tools.Helpers

import scala.io.{BufferedSource, Source}

class PredictionComplexityAnalysis {
  val sacepicnEnv = StaticConfig.systemPath
  val join = new Join()
  val DateTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
  val tuples = 10800

  def handleFileNotFoundException(fileNotFoundException: FileNotFoundException, nodeName: String): Unit = {
    LogMessage(nodeName, fileNotFoundException.toString)
  }
  def sortByDateTime(input: StringBuilder) = {
    val linelist = input.toString().split("\n").toList.sortWith(_.split(",")(1).toInt < _.split(",")(1).toInt)
    val output = new StringBuilder()
    for (line <- linelist) {
      output.append(line.toString + "\n")
    }
    output
  }

  def joinStrings(left:String, right:String) ={
    val buf = new StringBuilder
    buf.append(Helpers.trimData(left)).append(Helpers.trimData(right))
    sortByDateTime(buf).toString()
  }

  def readFile(path:String) ={
    var bufferedSource1: BufferedSource = null
    try {
      bufferedSource1 = Source.fromFile(s"$sacepicnEnv/sensors/ComplexityTests/" + path)
      //System.out.println(bufferedSource1.length)
      //System.out.println(bufferedSource1.getLines().length)
    }
    catch {
      case fileNotFoundException: FileNotFoundException => handleFileNotFoundException(fileNotFoundException, "Debug")
      case _: Throwable => LogMessage("Debug", "Got some Kind of Exception")
    }

    var output = new StringBuilder()

    val lineList1 = bufferedSource1.getLines.toList
    //System.out.println(bufferedSource1.getLines().length)
    var lineNumber = 1
    for (line <- lineList1){
      if(lineNumber<tuples)
        output.append(line.toString + "\n")
    }
    bufferedSource1.close()
    output.toString()
  }

  def joinFiles(file1:String, file2:String) ={
    var output = new StringBuilder()
    output.append(readFile(file1)).append(readFile(file2))
    output = sortByDateTime(output)
    output
  }

  @Test
  def prediction1Analysis={
    val buf = new StringBuilder
    val threshold = 60
    for (i <- 1 to threshold) {
      val prediction = new Prediction1()
      val granularityInSeconds = 2 * 60
      val historyGranularity = Math.round(86400 / granularityInSeconds)
      //val lines = joinFiles("plug0", "plug1").toString().stripSuffix("\n").split("\n").toList
      val lines = joinStrings(joinStrings(readFile("plug0"),readFile("plug1")),joinStrings(readFile("plug2"),readFile("plug3"))).stripSuffix("\n").split("\n").toList
      //val lines = readFile("plug0").stripSuffix("\n").split("\n").toList
      val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
      val startTime: LocalTime = LocalTime.now()
      val output = prediction.predict(lines, 40, 18, 20, granularityInSeconds, historyArray)
      val endTime: LocalTime = LocalTime.now()
      val timeDifference = (endTime.toNanoOfDay - startTime.toNanoOfDay)/ 1000000
      buf.append(i.toString+ "," + tuples*2 +"," + "2" +","+timeDifference.toString + "\n")
      //System.out.println(output)
      //System.out.println("Prediction1 lines: "+output.split("\n").length)
    }
    System.out.println("Prediction 1 Execution Times:")
    System.out.println(buf.toString())
  }

/*@Test
def prediction2Analysis={
  val buf = new StringBuilder
  val threshold = 60
  for (i <- 1 to threshold){
    val prediction = new Prediction2()
    val granularityInSeconds = 2*60
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    var historyArray1 = Array.ofDim[Double](historyGranularity)
    var historyArray2 = Array.ofDim[Double](historyGranularity)
    var historyArray3 = Array.ofDim[Double](historyGranularity)
    var historyArray4 = Array.ofDim[Double](historyGranularity)
    val lines1=readFile("plug0").stripSuffix("\n").split("\n").toList
    val lines2=readFile("plug1").stripSuffix("\n").split("\n").toList
    val lines3=readFile("plug2").stripSuffix("\n").split("\n").toList
    val lines4=readFile("plug3").stripSuffix("\n").split("\n").toList
    val startTime1: LocalTime = LocalTime.now()
    val out1 = prediction.predict(lines1,granularityInSeconds,historyArray1)
    val endTime1: LocalTime = LocalTime.now()
    val timeDifference1 = (endTime1.toNanoOfDay - startTime1.toNanoOfDay) / 1000000

    val startTime2: LocalTime = LocalTime.now()
    val out2 = prediction.predict(lines2,granularityInSeconds,historyArray2)
    val endTime2: LocalTime = LocalTime.now()
    val timeDifference2 = (endTime2.toNanoOfDay - startTime2.toNanoOfDay) / 1000000

    val startTime3: LocalTime = LocalTime.now()
    val out3 = prediction.predict(lines3,granularityInSeconds,historyArray3)
    val endTime3: LocalTime = LocalTime.now()
    val timeDifference3 = (endTime3.toNanoOfDay - startTime3.toNanoOfDay) / 1000000

    val startTime4: LocalTime = LocalTime.now()
    val out4 = prediction.predict(lines4,granularityInSeconds,historyArray4)
    val endTime4: LocalTime = LocalTime.now()
    val timeDifference4 = (endTime4.toNanoOfDay - startTime4.toNanoOfDay) / 1000000
    //val output = (joinStrings(out1,out2))

    val timeDifference = List(timeDifference1,timeDifference2,timeDifference3,timeDifference4).max
    /*var timeDifference = 0.0
    if(timeDifference1 >= timeDifference2)
      timeDifference = timeDifference1
    else
      timeDifference = timeDifference2*/

    buf.append(i.toString+ "," +tuples*2 +","+ "2" +","+timeDifference.toString + "\n")
    //System.out.println(output)
    //System.out.println("Prediction2 lines: "+output.split("\n").length)
  }
  System.out.println("Prediction 2 Execution Times:")
  System.out.println(buf.toString())

}*/

}
