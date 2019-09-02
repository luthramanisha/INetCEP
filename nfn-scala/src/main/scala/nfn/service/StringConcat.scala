package nfn.service

import akka.actor.ActorRef
import ccn.packet.CCNName

class StringConcat extends NFNService {
  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {
    NFNStringValue(args.map({
      case NFNContentObjectValue(name, data) => new String(data)
      case NFNStringValue(s) => s
      case NFNIntValue(i) => i.toString
      case _ =>
        throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
    }).mkString(" "))
  }
}
