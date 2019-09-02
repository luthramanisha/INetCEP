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
import java.nio.file.{Paths, Files}
import config.StaticConfig

class GetData() extends NFNService {

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue= {

    val sacepicnEnv = StaticConfig.systemPath

    def getData(name: NFNStringValue): String = {
      var objectname = s"$sacepicnEnv/nodeData/" + name.str.replace("/", "-");
      var output: String = ""
      if (Files.exists(Paths.get(objectname))) {
        val bufferedSource = Source.fromFile(objectname)
        bufferedSource
          .getLines
          .foreach { line: String =>
            output += "#" + line.toString() + "\n";
          }
        bufferedSource.close
      }

      if (output != "")
        output = output.stripSuffix("\n").stripMargin('#');
      else
        output += "No Results!"

      return output;
    }

    NFNStringValue(
      args match {
        case Seq(name: NFNStringValue) => getData(name)
        case _ =>
          throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
      })
  }
}

