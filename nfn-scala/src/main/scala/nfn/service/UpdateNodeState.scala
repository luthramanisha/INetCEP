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
import scala.collection.mutable.ListBuffer
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


class UpdateNodeState() extends NFNService {
  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {

    //Very important to remember: Content stored here through NFN Calls is ONLY available to this node.
    def updateNodeState(nodeID: String, content: String, timeOfUpdate: String): String = {
      var now = Calendar.getInstance()
      var name = s"/${nodeID}/" + now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE);

      var nameOfContentWithoutPrefixToAdd = CCNName(new String(name).split("/").tail: _*)
      ccnApi ! NFNApi.AddToLocalCache(Content(nameOfContentWithoutPrefixToAdd, content.getBytes, MetaInfo.empty), prependLocalPrefix = false)

      var now1 = Calendar.getInstance()
      var name1 = s"/${nodeID}/" + now.get(Calendar.HOUR_OF_DAY) + (now.get(Calendar.MINUTE) + 1).toString();
      var nameOfContentWithoutPrefixToAdd1 = CCNName(new String(name1).split("/").tail: _*)
      ccnApi ! NFNApi.AddToLocalCache(Content(nameOfContentWithoutPrefixToAdd1, content.getBytes, MetaInfo.empty), prependLocalPrefix = false)

      var now2 = Calendar.getInstance()
      var name2 = s"/${nodeID}/" + now.get(Calendar.HOUR_OF_DAY) + (now.get(Calendar.MINUTE) + 2).toString();
      var nameOfContentWithoutPrefixToAdd2 = CCNName(new String(name2).split("/").tail: _*)
      ccnApi ! NFNApi.AddToLocalCache(Content(nameOfContentWithoutPrefixToAdd2, content.getBytes, MetaInfo.empty), prependLocalPrefix = false)

      return name;
    }

    NFNStringValue(
    args match {
      case Seq(nodeID: NFNStringValue, content: NFNStringValue, timeOfUpdate:NFNStringValue) => updateNodeState(nodeID.str, content.str, timeOfUpdate.str)
      case _ =>
        throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
    }
    )
  }
}

