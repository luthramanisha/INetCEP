package myutil

import java.time.LocalTime
import java.time.Duration
import java.lang.Object

/**
 * Created by basil on 16/09/14.
 */
object FormattedOutput {

  def byteArrayToHex(bytes: Array[Byte]): String = bytes.map{ b => String.format("%02X", new java.lang.Integer(b & 0xff)) }.mkString("'", " ", "'")

  //Updated by Ali: 11.08.2018
  def parseDouble(s: String) = try { (s.toDouble) } catch { case _ => 0.0 }

  def parseLong(s: String):Long = try { s.toLong } catch {case _ => 0}

  def round(value: Either[Double, Float], places: Int) = {
    if (places < 0) 0.0
    else {
      val factor = Math.pow(10, places)
      value match {
        case Left(d) => (Math.round(d * factor) / factor)
        case Right(f) => (Math.round(f * factor) / factor)
      }
    }
  }

  def roundLong(value: Either[Int, Long], places: Int) = {
    if (places < 0) 0.0
    else {
      val factor = Math.pow(10, places)
      value match {
        case Left(i) => (Math.round(i * factor) / factor)
        case Right(l) => (Math.round(l * factor) / factor)
      }
    }
  }

  def round(value: Double): Double = round(Left(value), 0)
  def round(value: Double, places: Int): Double = round(Left(value), places)

  def round(value: Float): Double = round(Right(value), 0)
  def round(value: Float, places: Int): Double = round(Right(value), places)

  def round(value: Int): Double = roundLong(Left(value), 0)
  def round(value: Int, places: Int): Double = roundLong(Left(value), places)

  def round(value: Long): Double = roundLong(Right(value), 0)
  def round(value: Long, places: Int): Double = roundLong(Right(value), places)

  def toInt(s: String): Int = {
    try {
      s.toInt
    } catch {
      case e: Exception => 0
    }
  }

  //Separating past and future time retreival in two separate functions. We can still make one function for this but keeping them separate for now.
  def getPastTime(currentTime: LocalTime, timePeriod: Long, unit: String):LocalTime = {
    if(unit.toUpperCase() == "S"){

      return currentTime.minusSeconds(timePeriod)
    }
    if(unit.toUpperCase() == "M") {
      return currentTime.minusMinutes(timePeriod)
    }
    if(unit.toUpperCase() == "H") {
      return currentTime.minusHours(timePeriod)
    }
    //Default case: seconds (if the option is not correctly stated in the query)
    return currentTime.minusMinutes(timePeriod)
  }
  def getFutureTime(currentTime: LocalTime, timePeriod: Long, unit: String):LocalTime = {
    if(unit.toUpperCase() == "S")
      return currentTime.plusSeconds(timePeriod)
    if(unit.toUpperCase() == "M")
      return currentTime.plusMinutes(timePeriod)
    if(unit.toUpperCase() == "H")
      return currentTime.plusHours(timePeriod)

    //Default case: seconds (if the option is not correctly stated in the query)
    return currentTime.plusSeconds(timePeriod)
  }
  //Updated by Ali: 11.08.2018
}

