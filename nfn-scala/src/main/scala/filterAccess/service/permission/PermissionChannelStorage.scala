package filterAccess.service.permission

import akka.actor.ActorRef
import filterAccess.crypto.Encryption._
import filterAccess.json.KeyChannelParser._
import filterAccess.persistency.{KeyPersistency, PermissionPersistency}
import filterAccess.tools.DataNaming

/**
 * Created by Claudio Marxer <marxer@claudio.li>
 *
 * This class is used to set up a service for the permission channel.
 *
 */
class PermissionChannelStorage extends PermissionChannel {

  /**
   *
   * This function is called by entry point of this service to handle the actual work.
   *
   * @param    rdn       Relative data name
   * @return             JSON Object
   */
  override def processPermissionChannel(rdn: String, ccnApi: ActorRef): Option[String] = {

    // Fetch permission data
    PermissionPersistency.getPersistentPermission(rdn) match {
      case Some(jsonPermission) => {
        // Fetch json object with symmetric key
        KeyPersistency.getPersistentKey(rdn) match {
          case Some(jsonSymKey) => {
            // Extract symmetric key
            // Note: AccessLevel "-1" specifies key to secure permission data
            val symKey = extractLevelKey(jsonSymKey, 0)
            // Encrypt permission data with symmetric key
            Some(symEncrypt(jsonPermission, symKey.get))
          }
          case _ => {
            // Could not fetch symmetric key from persistent storage
            None
          }
        }
      }
      case None => {
        // Could not fetch permission data from persistent storage
        None
      }

    }


  }

}
