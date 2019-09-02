package ccn.ccnlite

import java.nio.ByteBuffer
import javax.xml.bind.DatatypeConverter

import ccn.packet._
import com.typesafe.scalalogging.slf4j.Logging
import org.xml.sax.SAXParseException

import scala.collection.immutable
import scala.util.{Failure, Success, Try}
import scala.xml._


object CCNLiteXmlParser extends Logging {

  def decodeBase64(data: String): Array[Byte] = DatatypeConverter.parseBase64Binary(data)
  def encodeBase64(bytes: Array[Byte]): String = DatatypeConverter.printBase64Binary(bytes)


  def parseCCNPacket(xmlString: String):Try[CCNPacket] = {

    val chunkMarker: Byte = 0x00
    val ndntlvMarkers: Set[Byte] = Set(chunkMarker)

    def unsignedByteToInt(b: Array[Byte]): Int = {
      val r = ByteBuffer.wrap(b.reverse.padTo(4, 0.toByte).reverse).getInt
      if(r < 0) throw new Exception("parsed a signed Integer, does not fit into a Integer")
      else r
    }

    def parseComponentsCCNB(elem: Elem):Seq[String] = {
      val components = elem \ "name" \ "component"

      components.map { parseDataCCNB }
    }

    def ndntlvParseName(elem: Elem): CCNName = {

      val components = elem \ "Name" \ "NameComponent" \ "data"

      val cmpsWithMarkers: List[Array[Byte]] = components.map({ cmp => decodeBase64(cmp.text) }).toList

      val (cmps, markedCmps) = cmpsWithMarkers.partition { cmp: Array[Byte] => cmp.nonEmpty && !ndntlvMarkers.contains(cmp(0)) }
      val chunkComp = markedCmps.find { cmp => cmp(0) == chunkMarker}
      val chunkNum = chunkComp map { cmp =>
        unsignedByteToInt(cmp.tail)
      }

      CCNName(cmps.map(new String(_)).toList, chunkNum)
    }
    def parseDataCCNB(elem: Node): String = {

      val data = elem \ "data"

      val nameSize = (data \ "@size").text.toInt
      val encoding = (data \ "@dt").text
      val nameData = data.text.trim

      encoding match {
        case "string" =>
          nameData
        case "binary.base64" =>
          new String(decodeBase64(nameData))
        case _ => throw new Exception(s"parseDataCCNB() does not support data of type: '$encoding'")
      }
    }

    def parseDataNDNTLV(elem: Node): Array[Byte] = {
      val datasNodes = elem \ "data"

      datasNodes.foldLeft(Array[Byte]()) { (curData: Array[Byte], data: Node) =>
        val nameSize = (data \ "@size").text.toInt
        val encoding = (data \ "@dt").text
        val nameData = data.text.trim

        val nextData = encoding match {
          case "string" =>
            nameData.getBytes
          case "binary.base64" =>
            decodeBase64(nameData)
          case _ => throw new Exception(s"parseDataNDNTLV() does not support data of type: '$encoding'")
        }

        curData ++ nextData
      }
    }

    def parseContentDataCCNB(elem: Elem): Array[Byte] = {
      val contents = elem \ "content"

      assert(contents.size == 1, "content should only contain one node with content")
      parseDataCCNB(contents.head).getBytes
    }
    def parseContentDataNDNTLV(elem: Elem): Array[Byte] = {
      val contents = elem \ "Content"

      assert(contents.size == 1, "content should only contain one node with content")
      parseDataNDNTLV(contents.head)
    }

    def ccntlvParseName(packet: Node): CCNName = {
      val nameComponents = packet \ "Name" \ "NameSegment" map { ns => new String(decodeBase64(ns.text.trim)) }
      val chunkNum:Option[Int] =
        (packet \ "Name" \ "Chunk").headOption map {
          c => unsignedByteToInt(decodeBase64(c.text.trim))
        }

      CCNName(nameComponents.toList, chunkNum)
    }

    def iottlvParseName(packet: Node): CCNName = { //TODO Chunk Numbers
      val nameComponents = packet \ "Name-ReqRepCtx" \ "PathName-NameCtx" \ "Component-PathNameCtx" map { ns => new String(decodeBase64(ns.text.trim)) }
      /*val chunkNum:Option[Int] =
        (packet \ "Name" \ "Chunk").headOption map {
          c => unsignedByteToInt(decodeBase64(c.text.trim))
        }*/

      CCNName(nameComponents.toList, None)
    }

    val cleanedXmlString = xmlString.trim.replace("&", "&amp;")
    val addToCacheAckStringBase64 = "MCvi6qVsYXN0AAGqfsXy+qVjY254APqFAPr1YWRkY2FjaGVvYmplY3QAAAGaAf0EygHVQ29udGVudCBzdWNjZXNzZnVsbHkgYWRkZWQ"
    val addToCacheAckStringBase642 ="8vqlY2NueAD6hQD69WFkZGNhY2hlb2JqZWN0AAABmgH9BMoB1UNvbnRlbnQgc3VjY2Vzc2Z1bGx5IGFkZGVk"
    val addToCacheFailedStringBase64 = "8vqlY2NueAD6hQD69WFkZGNhY2hlb2JqZWN0AAABmgHVBMoBrUZhaWxlZCB0byBhZGQgY29udGVud"
    if(cleanedXmlString.contains(addToCacheAckStringBase64) || cleanedXmlString.contains(addToCacheAckStringBase642)) {
      Try(AddToCacheAck(CCNName("/")))
    } else if (cleanedXmlString.contains(addToCacheFailedStringBase64)) {
      Try(AddToCacheNack(CCNName("/")))
    } else {

      val triedParsePacket =
      Try {
        scala.xml.XML.loadString(cleanedXmlString) match {
          // CCNB interest
          case interest @ <interest>{_*}</interest>=> {
            val nameComponents = parseComponentsCCNB(interest)
            Interest(nameComponents :_*)
          }
          // CCNB content
          case content @ <contentobj>{_*}</contentobj> => {
            val nameComponents = parseComponentsCCNB(content)
            val contentData = parseContentDataCCNB(content)
            if(contentData.startsWith(":NACK".getBytes)) {
              Nack(CCNName(nameComponents :_*))
            } else {
              Content(contentData, nameComponents :_*)
            }
          }

          // CCNTLV and NDNTLV both are pktdumped with <Interest><Name>
          // they differ by NameSegments and NameComponents
          case interest @ <Interest>{_*}</Interest> => {
            val nameNodes: NodeSeq = interest \ "Name"

            val nameSegments  = nameNodes \ "NameSegment"
            val name =
              if(nameSegments.nonEmpty) {
                ccntlvParseName(interest)
              }
              else {
                ndntlvParseName(interest)
              }
            Interest(name)
          }
            /* CCNTLV Content Object (chunked)
            <Object>
              <Name>
                <NameSegment size="4" dt="binary.base64">
                  Y2NueA==
                </NameSegment>
                <NameSegment size="7" dt="binary.base64">
                  Y2h1bmtlZA==
                </NameSegment>
                <Chunk size="1" dt="binary.base64">
                  Ag==
                </Chunk>
              </Name>
              <MetaData>
                <EndChunk size="1" dt="binary.base64">
                  Aw==
                </EndChunk>
              </MetaData>
              <Payload size="5" dt="binary.base64">
                dGNvbnQ=
              </Payload>
            </Object>
            */
          case content @ <Object>{_*}</Object> => {

            val name = ccntlvParseName(content)
            val contentData = content \ "Payload" map {d => decodeBase64(d.text.trim) } reduceLeft(_ ++ _)
            val lastChunkNum = (content \ "MetaData" \ "EndChunk").headOption map {
              c => unsignedByteToInt(decodeBase64(c.text.trim))
            }
            if(contentData.startsWith(":NACK".getBytes)) {
              Nack(name)
            } else {
              Content(name, contentData, MetaInfo(lastChunkNum))
            }
          }

          /* NDNTLV Content Object (chunked)

            <Data>
              <Name>
                <NameComponent>
                  <data size="3" dt="binary.base64">
                    bmRu
                  </data>
                </NameComponent>
                <NameComponent>
                  <data size="7" dt="binary.base64">
                    Y2h1bmtlZA==
                  </data>
                </NameComponent>
                <NameComponent>
                  <data size="2" dt="binary.base64">
                    AAI=
                  </data>
                </NameComponent>
              </Name>
              <MetaInfo>
                <FinalBlockId>
                  <data size="4" dt="binary.base64">
                    CAIAAw==
                  </data>
                </FinalBlockId>
              </MetaInfo>
              <Content>
                <data size="5" dt="binary.base64">
                  dGNvbnQ=
                </data>
              </Content>
              <SignatureInfo>
                <SignatureType>
                  <data size="1" dt="binary.base64">
                    AQ==
                  </data>
                </SignatureType>
                <KeyLocator>
                </KeyLocator>
              </SignatureInfo>
              <SignatureValue>
              </SignatureValue>
            </Data>
          */
          case content @ <Data>{_*}</Data> => {
            val name = ndntlvParseName(content)
            val contentData = parseContentDataNDNTLV(content)

            // parse last chunk num
            val lastChunkNum: Option[Int] = (content \ "MetaInfo" \ "FinalBlockId" \ "NameComponent").headOption flatMap { (lastChunkNumNode: Node) =>
              val lastChunkNumData = parseDataNDNTLV(lastChunkNumNode)
              if(lastChunkNumData.size == 0 || lastChunkNumData(0) != chunkMarker) {
                None
              } else {
                // TODO: this probably breaks for large numbers because it does ignore the NDNTLV encoding of includedNonNegInt
                Some(unsignedByteToInt(lastChunkNumData.tail))
              }
            }
            if(contentData.startsWith(":NACK".getBytes)) {
              Nack(name)
            } else {
              Content(name, contentData, MetaInfo(lastChunkNum))
            }
          }
          //IOTTLV Interest
          case interest @ <Request-toplevelCtx>{_*}</Request-toplevelCtx> => {
            val name = iottlvParseName(interest)
            Interest(name)
          }
          //IOTTLV Content
          case content @ <Object>{_*}</Object> => {

            val name = iottlvParseName(content)
            val contentData = content \ "Payload-ReqRepCtx" \ "Data-PayloadCtx" map {d => decodeBase64(d.text.trim) } reduceLeft(_ ++ _)
            /*val lastChunkNum = (content \ "MetaData" \ "EndChunk").headOption map {
              c => unsignedByteToInt(decodeBase64(c.text.trim))
            }*/
            if(contentData.startsWith(":NACK".getBytes)) {
              Nack(name)
            } else {
              Content(name, contentData, MetaInfo(None))
            }
          }
          case _ => throw new Exception("XML parser cannot parse:\n" + cleanedXmlString)
        }
      }
      // transform exception to print the original xml
      triedParsePacket.transform (
        s => Success(s),
        e => Failure( new Exception(s"Could not parse xml ('$cleanedXmlString')",e) )
      )
    }
  }
}

