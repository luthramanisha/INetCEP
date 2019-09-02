package nfn.service
/**
  * Created by Ali on 06.02.18.
  */
import akka.actor.ActorRef
import ccn.packet.CCNName
import scala.io.Source
import scala.util.control._
import ccn.packet.{CCNName, Content, MetaInfo, NFNInterest}
import nfn.NFNApi
import nfn.tools.Networking._
import java.io._
import nfn.service._
import config.StaticConfig

class SetData() extends NFNService {

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {

    val sacepicnEnv = StaticConfig.systemPath

    def setData(name: NFNStringValue, data: String): String = {
      var objectname = s"$sacepicnEnv/nodeData/" + name.str.replace("/","-");

      val file = new File(objectname)
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(data)
      bw.close()

      return "Object hard set on Node - Ok!";
    }

    NFNStringValue(
    args match {
      case Seq(name: NFNStringValue, data: NFNStringValue, ts: NFNStringValue) => setData(name, data.str)
      case Seq(name: NFNStringValue, data: NFNIntValue, ts: NFNStringValue) => setData(name, data.toString)
      case _ =>
        throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
    })
  }
}

