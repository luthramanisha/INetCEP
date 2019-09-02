package nfn.service

/**
  * Created by Ali on 06.02.18.
  */
import nfn.NFNApi
import nfn.service._
import nfn.tools.Networking._
import akka.actor.ActorRef
import akka.event.Logging

import scala.io.Source
import scala.util.control._
import scala.collection.mutable
import scala.List
import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.Vector
import scala.util.control.Exception._

//Added for contentfetch
import lambdacalculus.parser.ast.{Constant, Str}
import scala.concurrent.duration._
import scala.language.postfixOps
import java.util.Calendar
import java.time
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import ccn.packet.{CCNName, Content, MetaInfo, NFNInterest, Interest}

class GetContent() extends NFNService {
  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {

    def getValue(name: String): String = {
      val nameOfContentWithoutPrefixToAdd = CCNName(new String(name).split("/").tail: _*)
      var intermediateResult = ""
      try{
        intermediateResult = new String(fetchContentRepeatedly(Interest(nameOfContentWithoutPrefixToAdd), ccnApi, 15 seconds).get.data)
      }
      catch {
        case e : NoSuchElementException => intermediateResult = "Timeout"
      }

      // Extended for redirect by Johannes
      /*LogMessage("GetContent",s"trying to look into the result:"+intermediateResult)
      if(intermediateResult.contains("redirect")){
        LogMessage("GetContent",s"result contains redirect, trying to resolve it")
        var str = intermediateResult.replace("\n", "").trim
        val rname = CCNName(str.splitAt(9)._2.split("/").toList.tail.map(_.replace("%2F", "/").replace("%2f", "/")), None)
        val interest = new Interest(rname)
        intermediateResult = new String(fetchContent(interest,ccnApi,30 seconds).get.data)
      }*/

      intermediateResult
    }

    NFNStringValue(
    args match {
      case Seq(name: NFNStringValue) => getValue(name.str)
      case _ =>
        throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
    }
    )
  }
}

