package nfn.service

/**
  * Created by Ali on 06.02.18.
  */
import nfn.NFNApi
import nfn.service._
import nfn.tools.Networking._

import akka.actor.ActorRef
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

class ExecuteQuery() extends NFNService {
  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {

    def processQuery(parameterCount: String, node: String, service: String, parameters: String): String = {
      val paramsList = parameters.toString().split(' ')
        .toSeq
        .map(_.trim)
        .filter(_ != "").filter(_ != " ");

      var paramString = "";
      for (p <- paramsList) {
        //Handling named-interest:
        if(p.contains("[") && p.contains("]")){
          paramString += "\'" + p.replace("[", "").replace("]","") + "\' ";
        }
        else {
          //For all other parameters, we will enclose it:
          paramString += "\'" + p + "\' ";
        }
      }

      paramString = paramString.stripSuffix(" ").trim;
      return new String(fetchContent(
        NFNInterest(s"(call " + parameterCount + " /node/" + node + "/nfn_service_" + service + " " + paramString + ")"),
        ccnApi,
        30 seconds).get.data)
    }


    NFNStringValue(
      args match {
        //case NFNStringValue(s) => NFNDataValue(fetchContentAndKeepAlive(NFNInterest(s"(call 2 /node/nodeF/nfn_service_DelayedWordCount '${s}')"), ccnApi, 3 seconds).get.data)
        //case NFNIntValue(s)    => NFNDataValue(fetchContentAndKeepAlive(NFNInterest(s"(call 2 /node/nodeF/nfn_service_DelayedWordCount ${s})"), ccnApi, 3 seconds).get.data)
        case Seq(parameterCount: NFNStringValue, node: NFNStringValue, service: NFNStringValue, parameters: NFNStringValue) => processQuery(parameterCount.str, node.str, service.str, parameters.str)
        case Seq(parameterCount: NFNStringValue, node: NFNStringValue, service: NFNStringValue, parameters: NFNContentObjectValue) => processQuery(parameterCount.str, node.str, service.str, new String(parameters.data))
        //case _ => NFNIntValue(0)
        case _ =>
          throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
      }
    )
  }
}

