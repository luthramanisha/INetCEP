package nfn.tools

import java.io.FileNotFoundException
import java.time.LocalTime

import config.StaticConfig
import nfn.service.{LogMessage, Prediction1}

import scala.io.{BufferedSource, Source}

object MathHelper {
  val sacepicnEnv: String = StaticConfig.systemPath
  val path = "combindedPlugData"
  val prediction1 = new Prediction1()
  var truePositives: Double = 0
  var trueNegatives: Double = 0
  var falsePositives: Double = 0
  var falseNegatives: Double = 0
  var precision: Double = 0.0
  var recall: Double = 0.0
  var accuracy: Double = 0.0
  var fMeasure: Double = 0.0

  /**
    *
    * @param startTime   The start time of the values to consider
    * @param endTime     The end time of values to consider
    * @param granularity the granularity in seconds of the calculation
    * @param results     the actual simulation results
    * @return Sequence of four values: precision, recall, accuracy and F-Measure
    */
  def getPrecisionRecallAccuracyFMeasure(startTime: LocalTime, endTime: LocalTime, granularity: String, results: List[String]) = {
    val granularityInSeconds = granularity.takeRight(1).toLowerCase() match {
      case "s" => granularity.dropRight(1).toInt
      case "m" => granularity.dropRight(1).toInt * 60
      case "h" => granularity.dropRight(1).toInt * 60 * 60
      case _ => 0
    }
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    val historyArray = Array.ofDim[Double](historyGranularity, 40, 18, 20)
    val data = getRelevantTuples(readFile, startTime, endTime)
    val relevantTuples = prediction1.predict(data, 40, 18, 20, granularityInSeconds, historyArray).split("\n").toList
    getTruePositives(relevantTuples, results)
    getTrueNegatives()
    getFalseNegatives(relevantTuples, results)
    getFalsePositives(relevantTuples, results)
    calculatePrecision()
    calculateRecall()
    calculateAccuracy()
    calculateFMeasure()
    Seq(precision, recall, accuracy, fMeasure)
  }

  /**
    * A true positive is a tuple that is in the expected Result set and in the actual result set.
    * All values have to match
    *
    * @param relevantTuples the expected list of Tuples
    * @param results        the actual list of resulting Tuples
    */
  def getTruePositives(relevantTuples: List[String], results: List[String]) = {
    var count = 0
    for (line <- results) {
      if (relevantTuples.contains(line)) {
        count = count + 1
      }
    }
    truePositives = count
  }

  /**
    * We never get True negatives, they can be omitted
    *
    * @return alsways 0
    */
  def getTrueNegatives() = {
    0
  }

  /**
    * A False Positive is either a tuple that is in the result set but not in the expected Result set or
    * if the predicted values of two corresponding tuples do not match
    *
    * @param relevantTuples the expected list of Tuples
    * @param results        the actual list of resulting Tuples
    */
  def getFalsePositives(relevantTuples: List[String], results: List[String]) = {
    var count = 0
    for (line <- results) {
      if (!relevantTuples.contains(line)) {
        count = count + 1
      }
    }
    for (line <- results) {
      val datepart1: LocalTime = Helpers.parseTime(line.split(",")(0), "")
      for (line2 <- relevantTuples) {
        val datepart2: LocalTime = Helpers.parseTime(line2.split(",")(0), "")
        if (datepart1.equals(datepart2)) {

          val line1split = line.split(",")
          val line2split = line2.split(",")
          if (line1split.size > 5) {
            if (line1split(2) == line2split(2))
              if (line1split(3) == line2split(3))
                if (line1split(4) == line2split(4))
                  if (line != line2)
                    count = count + 1
          }
          else {
            if (line1split.size < 5 && line2split.size < 5)
              if (line1split(2) == line2split(2))
                if (line != line2)
                  count = count + 1
          }

        }

      }
    }
    falsePositives = count
  }

  /**
    * A false negative is a tuple that is in the expected result set but not in the actual result set
    *
    * @param relevantTuples the expected list of Tuples
    * @param results        the actual list of resulting Tuples
    */
  def getFalseNegatives(relevantTuples: List[String], results: List[String]) = {
    var count = 0
    for (line <- relevantTuples) {
      if (!results.contains(line)) {
        count = count + 1
      }
    }
    falseNegatives = count
  }

  /**
    * Calculation of Precision
    */
  def calculatePrecision() = {
    precision = truePositives / (truePositives + falsePositives)
  }

  /**
    * Calculation of Recall
    */
  def calculateRecall() = {
    recall = truePositives / (truePositives + falseNegatives)
  }

  /**
    * Calculation of Accuracy
    */
  def calculateAccuracy() = {
    accuracy = (truePositives + trueNegatives) / (truePositives + trueNegatives + falsePositives + falseNegatives)
  }

  /**
    * Calculation of F-Measure
    */
  def calculateFMeasure() = {
    fMeasure = 2 * ((precision * recall) / (precision + recall))
  }

  /**
    * Reads a file
    *
    * @return the read file line by line as a list of Strings
    */
  def readFile() = {
    var bufferedSource: BufferedSource = null
    try {
      bufferedSource = Source.fromFile(s"$sacepicnEnv/evalData/" + path)
    }
    catch {
      case fileNotFoundException: FileNotFoundException => handleFileNotFoundException(fileNotFoundException, "Exception")
      case _: Throwable => LogMessage("Exception", "Got some Kind of Exception")
    }
    bufferedSource.getLines().toList

  }

  def handleFileNotFoundException(fileNotFoundException: FileNotFoundException, nodeName: String): Unit = {
    LogMessage(nodeName, fileNotFoundException.toString)
  }

  /**
    * Returns the tuples that we consider for the evaluation
    *
    * @param lines   a list of Strings (tuples), each tuple is a line
    * @param minTime the lower time bound of tuples to consider
    * @param maxTime the upper time bound of tuples to consider
    * @return A sorted list of Strings (tuples) by time, where each tuple is in the given time window
    */
  def getRelevantTuples(lines: List[String], minTime: LocalTime, maxTime: LocalTime) = {
    var output = ""
    for (line <- lines) {
      val datepart = line.split(",")(1)
      val timestamp = Helpers.parseTime(datepart, ",")
      if ((timestamp.isAfter(minTime) || timestamp.equals(minTime)) && (timestamp.isBefore(maxTime) || timestamp.equals(maxTime))) {
        output = output + line + "\n"
      }
    }
    output.split("\n").toList.sortWith(_.split(",")(1).toInt < _.split(",")(1).toInt)
  }
}
