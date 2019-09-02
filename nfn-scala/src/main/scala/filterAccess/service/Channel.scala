package filterAccess.service

import nfn.service._

/**
 * Created by Claudio Marxer <marxer@claudio.li>
 *
 * Superclass for channels (content channel, permission channel, key channel as well as variations and proxies)
 *
 */
abstract class Channel extends NFNService {


  /** public key (identity) */
  protected var publicKey:String = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJpoF3jlUz9OOFgvEtraFMuaOuA211Ck3UHuHToMys65tT7PqvY87VNdOflJN1oTqqIuy3b8Hn4r45duJFc9N+MCAwEAAQ=="

  /** corresponding private key */
  protected var privateKey:String = "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAmmgXeOVTP044WC8S2toUy5o64DbXUKTdQe4dOgzKzrm1Ps+q9jztU105+Uk3WhOqoi7Ldvwefivjl24kVz034wIDAQABAkAecJbwBoW63TjOablV29htqyIgQa+A/n+AF+k7IHp69mDE7CtlikW4bDQXsaPVw1Sp18UhnZUJgfEFCjGPmimBAiEA/YcXjwvgAL/bfvsOwMWg44LwjY4g/WXdVHxLp4VXnksCIQCb6Y2e+P4RdOAdgvMP3+riIBs7B2U4u0eIyR6NbaRtyQIgMBu2aLqEIyBE8m+JeSMHSKTMKNBTikIOIb4ETSGMYskCIDQzy8Y5ih/gKRXYfXeIOoXByDxIapzHH9lttXwXBOH5AiBLTG6tCPaSz3DdslndvdK6dfy8Beg0iV1QdiqyAYe/fQ=="

  // TODO
  // Setting publicKey via setPublicKey(...) causes problems
  // Why? It seems that nfn-scala re-instantiates services.
  // Workaround: Hard-code this in here..

  /**
   * Set publicKey (public key) of this service.
   *
   * @param   id    Public Key
   */
  def setPublicKey(id: String): Unit = {
    publicKey = id
  }

  /**
   * Get publicKey (public key) of this service.
   *
   * @return  Public Key
   */
  def getPublicKey: String = publicKey

  /**
   * Set private key corresponding to public key (publicKey).
   *
   * @param   id    Public Key
   */
  def setPrivateKey(id: String): Unit = {
    privateKey = id
  }

  /**
   * Get private key corresponding to public key (publicKey).
   *
   * @return  Private Key
   */
  def getPrivateKey: String = privateKey

}
