package java.net

import java.io.IOException
import java.io.Serializable
import java.io.UnsupportedEncodingException
import java.util.StringTokenizer

object URI {
  val unreserved: String = "_-!.~\'()*" //$NON-NLS-1$

  val punct: String = ",;:$&+=" //$NON-NLS-1$

  val reserved: String = punct + "?/[]@" //$NON-NLS-1$

  val someLegal: String = unreserved + punct

  val queryLegal: String = unreserved + reserved + "\\\"" //$NON-NLS-1$

  val allLegal: String = unreserved + reserved

  /**
   * Parses the given argument {@code uri} and creates an appropriate URI
   * instance.
   *
   * @param uri
   *            the string which has to be parsed to create the URI instance.
   * @return the created instance representing the given URI.
   */
  def create(uri: String): URI = {
    var result: URI = null
    result = new URI(uri)
    result
  }

}

final class URI extends Comparable[URI] with Serializable {

  import URI._

  private val serialVersionUID = -6052424284110960213l

  private var string: String = _

  @transient private var scheme: String             = _
  @transient private var schemespecificpart: String = _
  @transient private var authority: String          = _
  @transient private var userinfo: String           = _
  @transient private var host: String               = _
  @transient private var port: Int                  = -1
  @transient private var path: String               = _
  @transient private var query: String              = _
  @transient private var fragment: String           = _
  @transient private var opaque: Boolean            = _
  @transient private var absolute: Boolean          = _
  @transient private var serverAuthority: Boolean   = false
  @transient private var hash: Int                  = -1

  /**
   * Creates a new URI instance according to the given string {@code uri}.
   *
   * @param uri
   * the textual URI representation to be parsed into a URI object.
   * @throws URISyntaxException
   * if the given string { @code uri} doesn't fit to the
   *                             specification RFC2396 or could not be parsed correctly.
   */
  /**
   * Creates a new URI instance according to the given string {@code uri}.
   *
   * @param uri
   * the textual URI representation to be parsed into a URI object.
   * @throws URISyntaxException
   * if the given string { @code uri} doesn't fit to the
   *                             specification RFC2396 or could not be parsed correctly.
   */
  def this(uri: String) = {
    this()
    new Helper().parseURI(uri, false)
  }

  /**
   * Creates a new URI instance using the given arguments. This constructor
   * first creates a temporary URI string from the given components. This
   * string will be parsed later on to create the URI instance.
   * <p>
   * {@code [scheme:]scheme-specific-part[#fragment]}
   *
   * @param scheme
   * the scheme part of the URI.
   * @param ssp
   * the scheme-specific-part of the URI.
   * @param frag
   * the fragment part of the URI.
   * @throws URISyntaxException
   * if the temporary created string doesn't fit to the
   * specification RFC2396 or could not be parsed correctly.
   */
  def this(scheme: String, ssp: String, frag: String) = {
    this()
    val uri: StringBuilder = new StringBuilder()
    if (scheme != null) {
      uri.append(scheme)
      uri.append(':')
    }
    if (ssp != null) {
      uri.append(quoteComponent(ssp, allLegal))
    }
    if (frag != null) {
      uri.append('#')
      uri.append(quoteComponent(frag, allLegal))
    }
    new Helper().parseURI(uri.toString, false)
  }

  /**
   * Creates a new URI instance using the given arguments. This constructor
   * first creates a temporary URI string from the given components. This
   * string will be parsed later on to create the URI instance.
   * <p>
   * {@code [scheme:][user-info@]host[:port][path][?query][#fragment]}
   *
   * @param scheme
   *            the scheme part of the URI.
   * @param userinfo
   *            the user information of the URI for authentication and
   *            authorization.
   * @param host
   *            the host name of the URI.
   * @param port
   *            the port number of the URI.
   * @param path
   *            the path to the resource on the host.
   * @param query
   *            the query part of the URI to specify parameters for the
   *            resource.
   * @param fragment
   *            the fragment part of the URI.
   * @throws URISyntaxException
   *             if the temporary created string doesn't fit to the
   *             specification RFC2396 or could not be parsed correctly.
   */
  def this(scheme: String,
           userinfo: String,
           host: String,
           port: Int,
           path: String,
           query: String,
           fragment: String) = {
    this()
    var hostVar   = host
    var earlyStop = false
    if (scheme == null && userinfo == null && host == null && path == null &&
        query == null &&
        fragment == null) {
      this.path = ""
      earlyStop = true
    }
    if (!earlyStop) {
      if (scheme != null && path != null && path.length > 0 && path.charAt(0) != '/') {
        throw new URISyntaxException(path, "Relative path")
      }
      val uri: StringBuilder = new StringBuilder()
      if (scheme != null) {
        uri.append(scheme)
        uri.append(':')
      }
      if (userinfo != null || host != null || port != -1) {
        uri.append("//")
      }
      if (userinfo != null) {
        uri.append(quoteComponent(userinfo, someLegal))
        uri.append('@')
      }
      if (host != null) {
        if (host.indexOf(':') != -1 && host.indexOf(']') == -1 && host.indexOf(
              '[') == -1) {
          hostVar = "[" + host + "]"
        }
        uri.append(hostVar)
      }
      if (port != -1) {
        uri.append(':')
        uri.append(port)
      }
      if (path != null) {
        uri.append(quoteComponent(path, "/@" + someLegal))
      }
      if (query != null) {
        uri.append('?')
        uri.append(quoteComponent(query, allLegal))
      }
      if (fragment != null) {
        uri.append('#')
        uri.append(quoteComponent(fragment, allLegal))
      }
      new Helper().parseURI(uri.toString, true)
    }
  }

  /**
   * Creates a new URI instance using the given arguments. This constructor
   * first creates a temporary URI string from the given components. This
   * string will be parsed later on to create the URI instance.
   * <p>
   * {@code [scheme:]host[path][#fragment]}
   *
   * @param scheme
   *            the scheme part of the URI.
   * @param host
   *            the host name of the URI.
   * @param path
   *            the path to the resource on the host.
   * @param fragment
   *            the fragment part of the URI.
   * @throws URISyntaxException
   *             if the temporary created string doesn't fit to the
   *             specification RFC2396 or could not be parsed correctly.
   */
  def this(scheme: String, host: String, path: String, fragment: String) =
    this(scheme, null, host, -1, path, null, fragment)

  /**
   * Creates a new URI instance using the given arguments. This constructor
   * first creates a temporary URI string from the given components. This
   * string will be parsed later on to create the URI instance.
   * <p>
   * {@code [scheme:][//authority][path][?query][#fragment]}
   *
   * @param scheme
   *            the scheme part of the URI.
   * @param authority
   *            the authority part of the URI.
   * @param path
   *            the path to the resource on the host.
   * @param query
   *            the query part of the URI to specify parameters for the
   *            resource.
   * @param fragment
   *            the fragment part of the URI.
   * @throws URISyntaxException
   *             if the temporary created string doesn't fit to the
   *             specification RFC2396 or could not be parsed correctly.
   */
  def this(scheme: String,
           authority: String,
           path: String,
           query: String,
           fragment: String) = {
    this()
    if (scheme != null && path != null && path.length > 0 && path.charAt(0) != '/') {
      throw new URISyntaxException(path, "Relative path")
    }
    val uri: StringBuilder = new StringBuilder()
    if (scheme != null) {
      uri.append(scheme)
      uri.append(':')
    }
    if (authority != null) {
      uri.append("//")
      uri.append(quoteComponent(authority, "@[]" + someLegal))
    }
    if (path != null) {
      uri.append(quoteComponent(path, "/@" + someLegal))
    }
    if (query != null) {
      uri.append('?')
      uri.append(quoteComponent(query, allLegal))
    }
    if (fragment != null) {
      uri.append('#')
      uri.append(quoteComponent(fragment, allLegal))
    }
    new Helper().parseURI(uri.toString, false)
  }

  private class Helper {

    @throws(classOf[URISyntaxException])
    def parseURI(uri: String, forceServer: Boolean): Unit = {
      var temp: String = uri
      string = uri
      var index: Int  = 0
      var index1: Int = 0
      var index2: Int = 0
      var index3: Int = 0

      index = temp.indexOf('#')
      if (index != -1) {
        fragment = temp.substring(index + 1)
        validateFragment(uri, fragment, index + 1)
        temp = temp.substring(0, index)
      }
      index = temp.indexOf(':')
      index1 = index
      index2 = temp.indexOf('/')
      index3 = temp.indexOf('?')
      if (index != -1 && (index2 >= index || index2 == -1) && (index3 >= index || index3 == -1)) {
        absolute = true
        scheme = temp.substring(0, index)
        if (scheme.length == 0) {
          throw new URISyntaxException(uri, "Scheme expected", index)
        }
        validateScheme(uri, scheme, 0)
        schemespecificpart = temp.substring(index + 1)
        if (schemespecificpart.length == 0) {
          throw new URISyntaxException(uri,
                                       "Scheme-specific part expected",
                                       index + 1)
        }
      } else {
        absolute = false
        schemespecificpart = temp
      }

      if (scheme == null || schemespecificpart.length > 0 && schemespecificpart
            .charAt(0) == '/') {
        opaque = false

        temp = schemespecificpart
        index = temp.indexOf('?')
        if (index != -1) {
          query = temp.substring(index + 1)
          temp = temp.substring(0, index)
          validateQuery(uri, query, index2 + 1 + index)
        }

        if (temp.startsWith("//")) {
          index = temp.indexOf('/', 2)
          if (index != -1) {
            authority = temp.substring(2, index)
            path = temp.substring(index)
          } else {
            authority = temp.substring(2)
            if (authority.length() == 0 && query == null
                && fragment == null) {
              throw new URISyntaxException(uri,
                                           "Authority expected",
                                           uri.length())
            }

            path = "";
          }

          if (authority.length() == 0) {
            authority = null
          } else {
            validateAuthority(uri, authority, index1 + 3)
          }
        } else {
          path = temp
        }

        var pathIndex: Int = 0
        if (index2 > -1) {
          pathIndex += index2
        }
        if (index > -1) {
          pathIndex += index
        }
        validatePath(uri, path, pathIndex)
      } else { // if not hierarchical, URI is opaque
        opaque = true
        validateSsp(uri, schemespecificpart, index2 + 2 + index)
      }

      parseAuthority(forceServer)
    }

    @throws(classOf[URISyntaxException])
    def validateScheme(uri: String, scheme: String, index: Int): Unit = {
      val ch: Char = scheme.charAt(0)
      if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))) {
        throw new URISyntaxException(uri, "Illegal character in scheme", 0)
      }

      try {
        URIEncoderDecoder.validateSimple(scheme, "+-.");
      } catch {
        case e: URISyntaxException =>
          throw new URISyntaxException(uri,
                                       "Illegal character in scheme",
                                       index
                                         + e.getIndex)
      }
    }

    @throws(classOf[URISyntaxException])
    def validateSsp(uri: String, ssp: String, index: Int): Unit = {
      try {
        URIEncoderDecoder.validate(ssp, allLegal)
      } catch {
        case e: URISyntaxException =>
          throw new URISyntaxException(
            uri,
            e.getReason + " in scheme specific part",
            index + e.getIndex)
      }
    }

    @throws(classOf[URISyntaxException])
    def validateAuthority(uri: String, authority: String, index: Int): Unit = {
      try {
        URIEncoderDecoder.validate(authority, "@[]" + someLegal)
      } catch {
        case e: URISyntaxException =>
          throw new URISyntaxException(uri,
                                       e.getReason + " in authority",
                                       index + e.getIndex)
      }
    }

    @throws(classOf[URISyntaxException])
    def validatePath(uri: String, path: String, index: Int): Unit = {
      try {
        URIEncoderDecoder.validate(path, "/@" + someLegal)
      } catch {
        case e: URISyntaxException =>
          throw new URISyntaxException(uri,
                                       e.getReason + " in path",
                                       index + e.getIndex)
      }
    }

    @throws(classOf[URISyntaxException])
    def validateQuery(uri: String, query: String, index: Int): Unit = {
      try {
        URIEncoderDecoder.validate(query, queryLegal)
      } catch {
        case e: URISyntaxException =>
          throw new URISyntaxException(uri,
                                       e.getReason + " in query",
                                       index + e.getIndex)
      }
    }

    @throws(classOf[URISyntaxException])
    def validateFragment(uri: String, fragment: String, index: Int): Unit = {
      try {
        URIEncoderDecoder.validate(fragment, allLegal)
      } catch {
        case e: URISyntaxException =>
          throw new URISyntaxException(uri,
                                       e.getReason + " in fragment",
                                       index + e.getIndex)
      }
    }

    @throws(classOf[URISyntaxException])
    def parseAuthority(forceServer: Boolean): Unit = {
      if (authority == null) {
        return
      }
      var temp: String         = null
      var tempUserinfo: String = null
      var tempHost: String     = null
      var index: Int           = 0
      var hostindex: Int       = 0
      var tempPort: Int        = -1
      temp = authority
      index = temp.indexOf('@')
      if (index != -1) {
        tempUserinfo = temp.substring(0, index)
        validateUserinfo(authority, tempUserinfo, 0)
        temp = temp.substring(index + 1)
        hostindex = index + 1
      }
      index = temp.lastIndexOf(':')
      val endindex: Int = temp.indexOf(']')
      if (index != -1 && endindex < index) {
        tempHost = temp.substring(0, index)
        if (index < (temp.length - 1)) {
          try {
            tempPort = java.lang.Integer.parseInt(temp.substring(index + 1))
            if (tempPort < 0) {
              if (forceServer) {
                throw new URISyntaxException(authority,
                                             "Invalid port number",
                                             hostindex + index + 1)
              }
              return
            }
          } catch {
            case e: NumberFormatException => {
              if (forceServer) {
                throw new URISyntaxException(authority,
                                             "Invalid port number",
                                             hostindex + index + 1)
              }
              return
            }

          }
        }
      } else {
        tempHost = temp
      }
      if (tempHost == "") {
        if (forceServer) {
          throw new URISyntaxException(authority, "Expected host", hostindex)
        }
        return
      }
      if (!isValidHost(forceServer, tempHost)) {
        return
      }

      userinfo = tempUserinfo
      host = tempHost
      port = tempPort
      serverAuthority = true
    }

    @throws(classOf[URISyntaxException])
    def validateUserinfo(uri: String, userinfo: String, index: Int): Unit = {
      for (i <- 0 until userinfo.length) {
        val ch: Char = userinfo.charAt(i)
        if (ch == ']' || ch == '[') {
          throw new URISyntaxException(uri,
                                       "Illegal character in userinfo",
                                       index + i)
        }
      }
    }

    def isValidHost(forceServer: Boolean, host: String): Boolean = {
      if (host.charAt(0) == '[') {
        if (host.charAt(host.length - 1) != ']') {
          throw new URISyntaxException(
            host,
            "Expected a closing square bracket for ipv6 address",
            0)
        }
        if (!isValidIP6Address(host)) {
          throw new URISyntaxException(host, "Malformed ipv6 address")
        }
        return true
      }
      if (host.indexOf('[') != -1 || host.indexOf(']') != -1) {
        throw new URISyntaxException(host, "Illegal character in host name", 0)
      }
      val index: Int = host.lastIndexOf('.')
      if (index < 0 || index == host.length - 1 ||
          !java.lang.Character.isDigit(host.charAt(index + 1))) {
        if (isValidDomainName(host)) {
          return true
        }
        if (forceServer) {
          throw new URISyntaxException(host,
                                       "Illegal character in host name",
                                       0)
        }
        return false
      }
      if (isValidIPv4Address(host)) {
        return true
      }
      if (forceServer) {
        throw new URISyntaxException(host, "Malformed ipv4 address", 0)
      }
      false
    }

    def isValidDomainName(host: String): Boolean = {
      try {
        URIEncoderDecoder.validateSimple(host, "-.")
      } catch {
        case e: URISyntaxException => return false
      }
      var label: String       = null
      val st: StringTokenizer = new StringTokenizer(host, ".")
      while (st.hasMoreTokens) {
        label = st.nextToken()
        if (label.startsWith("-") || label.endsWith("-")) {
          return false
        }
      }
      if (label != host) {
        val ch: Char = label.charAt(0)
        if (ch >= '0' && ch <= '9') {
          return false
        }
      }
      true
    }

    def isValidIPv4Address(host: String): Boolean = {
      var index: Int  = 0
      var index2: Int = 0
      try {
        var num: Int = 0
        index = host.indexOf('.')
        num = java.lang.Integer.parseInt(host.substring(0, index))
        if (num < 0 || num > 255) {
          return false
        }
        index2 = host.indexOf('.', index + 1)
        num = java.lang.Integer.parseInt(host.substring(index + 1, index2))
        if (num < 0 || num > 255) {
          return false
        }
        index = host.indexOf('.', index2 + 1)
        num = java.lang.Integer.parseInt(host.substring(index2 + 1, index))
        if (num < 0 || num > 255) {
          return false
        }
        num = java.lang.Integer.parseInt(host.substring(index + 1))
        if (num < 0 || num > 255) {
          return false
        }
      } catch {
        case e: Exception => false

      }
      true
    }

    def isValidIP6Address(ipAddress: String): Boolean = {
      val length: Int          = ipAddress.length
      var doubleColon: Boolean = false
      var numberOfColons: Int  = 0
      var numberOfPeriods: Int = 0
      var word: String         = ""
      var c: Char              = 0
      var prevChar: Char       = 0
      var offset: Int          = 0
      if (length < 2) {
        return false
      }
      for (i <- 0 until length) {
        prevChar = c
        c = ipAddress.charAt(i)
        c match {
          case '[' =>
            if (i != 0) {
              return false
            }
            if (ipAddress.charAt(length - 1) != ']') {
              return false
            }
            if ((ipAddress.charAt(1) == ':') && (ipAddress.charAt(2) != ':')) {
              return false
            }
            offset = 1
            if (length < 4) {
              return false
            }
          case ']' =>
            if (i != length - 1) {
              return false
            }
            if (ipAddress.charAt(0) != '[') {
              return false
            }
          case '.' => { numberOfPeriods += 1; numberOfPeriods - 1 }
          if (numberOfPeriods > 3) {
            return false
          }
          if (!isValidIP4Word(word)) {
            return false
          }
          if (numberOfColons != 6 && !doubleColon) {
            return false
          }
          if (numberOfColons == 7 && ipAddress.charAt(0 + offset) != ':' &&
              ipAddress.charAt(1 + offset) != ':') {
            return false
          }
          word = ""
        case ':' => { numberOfColons += 1; numberOfColons - 1 }
          if (numberOfColons > 7) {
            return false
          }
          if (numberOfPeriods > 0) {
            return false
          }
          if (prevChar == ':') {
            if (doubleColon) {
              return false
            }
            doubleColon = true
          }
          word = ""
        case _ =>
            if (word.length > 3) {
              return false
            }
            if (!isValidHexChar(c)) {
              return false
            }
            word += c

        }
      }
      if (numberOfPeriods > 0) {
        if (numberOfPeriods != 3 || !isValidIP4Word(word)) {
          return false
        }
      } else {
        if (numberOfColons != 7 && !doubleColon) {
          return false
        }
        if (word == "" && ipAddress.charAt(length - 1 - offset) != ':' &&
            ipAddress.charAt(length - 2 - offset) != ':') {
          return false
        }
      }
      true
    }

    def isValidIP4Word(word: String): Boolean = {
      var c: Char = 0
      if (word.length < 1 || word.length > 3) {
        return false
      }
      for (i <- 0 until word.length) {
        c = word.charAt(i)
        if (!(c >= '0' && c <= '9')) {
          return false
        }
      }
      if (java.lang.Integer.parseInt(word) > 255) {
        return false
      }
      true
    }

    def isValidHexChar(c: Char): Boolean =
      (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')

  }

  private def quoteComponent(component: String, legalset: String): String = {
    try {
      URIEncoderDecoder.quoteIllegal(component, legalset);
    } catch {
      case e: UnsupportedOperationException =>
        throw new RuntimeException(e.toString)
    }
  }

  def compareTo(uri: URI): Int = {
    var ret: Int = 0
    if (scheme == null && uri.scheme != null) {
      -1
    } else if (scheme != null && uri.scheme == null) {
      1
    } else if (scheme != null && uri.scheme != null) {
      ret = scheme.compareToIgnoreCase(uri.scheme)
      if (ret != 0) {
        return ret
      }
    }
    if (!opaque && uri.opaque) {
      -1
    } else if (opaque && !uri.opaque) {
      1
    } else if (opaque && uri.opaque) {
      ret = schemespecificpart.compareTo(uri.schemespecificpart)
      if (ret != 0) {
        return ret
      }
    } else {
      if (authority != null && uri.authority == null) {
        1
      } else if (authority == null && uri.authority != null) {
        -1
      } else if (authority != null && uri.authority != null) {
        if (host != null && uri.host != null) {
          if (userinfo != null && uri.userinfo == null) {
            1
          } else if (userinfo == null && uri.userinfo != null) {
            -1
          } else if (userinfo != null && uri.userinfo != null) {
            ret = userinfo.compareTo(uri.userinfo)
            if (ret != 0) {
              return ret
            }
          }
          ret = host.compareToIgnoreCase(uri.host)
          if (ret != 0) {
            return ret
          }
          if (port != uri.port) {
            port - uri.port
          }
        } else {
          ret = authority.compareTo(uri.authority)
          if (ret != 0) {
            return ret
          }
        }
      }
      ret = path.compareTo(uri.path)
      if (ret != 0) {
        return ret
      }
      if (query != null && uri.query == null) {
        1
      } else if (query == null && uri.query != null) {
        -1
      } else if (query != null && uri.query != null) {
        ret = query.compareTo(uri.query)
        if (ret != 0) {
          return ret
        }
      }
    }
    if (fragment != null && uri.fragment == null) {
      1
    } else if (fragment == null && uri.fragment != null) {
      -1
    } else if (fragment != null && uri.fragment != null) {
      ret = fragment.compareTo(uri.fragment)
      if (ret != 0) {
        return ret
      }
    }
    0
  }

  private def duplicate(): URI = {
    val clone: URI = new URI()
    clone.absolute = absolute
    clone.authority = authority
    clone.fragment = fragment
    clone.host = host
    clone.opaque = opaque
    clone.path = path
    clone.port = port
    clone.query = query
    clone.scheme = scheme
    clone.schemespecificpart = schemespecificpart
    clone.userinfo = userinfo
    clone.serverAuthority = serverAuthority
    clone
  }

  /*
   * Takes a string that may contain hex sequences like %F1 or %2b and
   * converts the hex values following the '%' to lowercase
   */

  private def convertHexToLowerCase(s: String): String = {
    val result: StringBuilder = new StringBuilder("")
    if (s.indexOf('%') == -1) {
      s
    }
    var previndex: Int = 0
    var index: Int     = s.indexOf('%', previndex)
    while (index != -1) {
      result.append(s.substring(previndex, index + 1))
      result.append(s.substring(index + 1, index + 3).toLowerCase())
      index += 3
      previndex = index
      index = s.indexOf('%', previndex)
    }
    result.toString
  }

  /*
   * Takes two strings that may contain hex sequences like %F1 or %2b and
   * compares them, ignoring case for the hex values. Hex values must always
   * occur in pairs as above
   */

  private def equalsHexCaseInsensitive(first: String,
                                       second: String): Boolean = {
    if (first.indexOf('%') != second.indexOf('%')) {
      first == second
    }
    var previndex: Int = 0
    var index: Int     = first.indexOf('%', previndex)
    while (index != -1 && second.indexOf('%', previndex) == index) {
      var `match`: Boolean = first.substring(previndex, index) == second
        .substring(previndex, index)
      if (!`match`) {
        return false
      }
      `match` = first
        .substring(index + 1, index + 3)
        .equalsIgnoreCase(second.substring(index + 1, index + 3))
      if (!`match`) {
        return false
      }
      index += 3
      previndex = index
      index = first.indexOf('%', previndex)
    }
    first.substring(previndex) == second.substring(previndex)
  }

  /**
   * Compares this URI instance with the given argument {@code o} and
   * determines if both are equal. Two URI instances are equal if all single
   * parts are identical in their meaning.
   *
   * @param o
   *            the URI this instance has to be compared with.
   * @return {@code true} if both URI instances point to the same resource,
   *         {@code false} otherwise.
   */
  override def equals(o: Any): Boolean = {
    if (!(o.isInstanceOf[URI])) {
      return false
    }
    val uri: URI = o.asInstanceOf[URI]
    if (uri.fragment == null && fragment != null || uri.fragment != null && fragment == null) {
      return false
    } else if (uri.fragment != null && fragment != null) {
      if (!equalsHexCaseInsensitive(uri.fragment, fragment)) {
        return false
      }
    }
    if (uri.scheme == null && scheme != null || uri.scheme != null && scheme == null) {
      return false
    } else if (uri.scheme != null && scheme != null) {
      if (!uri.scheme.equalsIgnoreCase(scheme)) {
        return false
      }
    }
    if (uri.opaque && opaque) {
      equalsHexCaseInsensitive(uri.schemespecificpart, schemespecificpart)
    } else if (!uri.opaque && !opaque) {
      if (!equalsHexCaseInsensitive(path, uri.path)) {
        return false
      }
      if (uri.query != null && query == null || uri.query == null && query != null) {
        return false
      } else if (uri.query != null && query != null) {
        if (!equalsHexCaseInsensitive(uri.query, query)) {
          return false
        }
      }
      if (uri.authority != null && authority == null || uri.authority == null && authority != null) {
        return false
      } else if (uri.authority != null && authority != null) {
        if (uri.host != null && host == null || uri.host == null && host != null) {
          return false
        } else if (uri.host == null && host == null) {
          // both are registry based, so compare the whole authority
          equalsHexCaseInsensitive(uri.authority, authority)
        } else {
          // uri.host != null && host != null, so server-based
          if (!host.equalsIgnoreCase(uri.host)) {
            return false
          }
          if (port != uri.port) {
            return false
          }
          if (uri.userinfo != null && userinfo == null || uri.userinfo == null && userinfo != null) {
            return false
          } else if (uri.userinfo != null && userinfo != null) {
            return equalsHexCaseInsensitive(userinfo, uri.userinfo)
          } else {
            return true
          }
        }
      } else {
        // no authority
        return true
      }
    } else {
      // one is opaque, the other hierarchical
      return false
    }
  }

  /**
   * Gets the decoded authority part of this URI.
   *
   * @return the decoded authority part or {@code null} if undefined.
   */
  def getAuthority(): String = decode(authority)

  /**
   * Gets the decoded fragment part of this URI.
   *
   * @return the decoded fragment part or {@code null} if undefined.
   */
  def getFragment(): String = decode(fragment)

  /**
   * Gets the host part of this URI.
   *
   * @return the host part or {@code null} if undefined.
   */
  def getHost(): String = host

  /**
   * Gets the decoded path part of this URI.
   *
   * @return the decoded path part or {@code null} if undefined.
   */
  def getPath(): String = decode(path)

  /**
   * Gets the port number of this URI.
   *
   * @return the port number or {@code -1} if undefined.
   */
  def getPort(): Int = port

  /**
   * Gets the decoded query part of this URI.
   *
   * @return the decoded query part or {@code null} if undefined.
   */
  def getQuery(): String = decode(query)

  /**
   * Gets the authority part of this URI in raw form.
   *
   * @return the encoded authority part or {@code null} if undefined.
   */
  def getRawAuthority(): String = authority

  /**
   * Gets the fragment part of this URI in raw form.
   *
   * @return the encoded fragment part or {@code null} if undefined.
   */
  def getRawFragment(): String = fragment

  /**
   * Gets the path part of this URI in raw form.
   *
   * @return the encoded path part or {@code null} if undefined.
   */
  def getRawPath(): String = path

  /**
   * Gets the query part of this URI in raw form.
   *
   * @return the encoded query part or {@code null} if undefined.
   */
  def getRawQuery(): String = query

  /**
   * Gets the scheme-specific part of this URI in raw form.
   *
   * @return the encoded scheme-specific part or {@code null} if undefined.
   */
  def getRawSchemeSpecificPart(): String = schemespecificpart

  /**
   * Gets the user-info part of this URI in raw form.
   *
   * @return the encoded user-info part or {@code null} if undefined.
   */
  def getRawUserInfo(): String = userinfo

  /**
   * Gets the scheme part of this URI.
   *
   * @return the scheme part or {@code null} if undefined.
   */
  def getScheme(): String = scheme

  /**
   * Gets the decoded scheme-specific part of this URI.
   *
   * @return the decoded scheme-specific part or {@code null} if undefined.
   */
  def getSchemeSpecificPart(): String = decode(schemespecificpart)

  /**
   * Gets the decoded user-info part of this URI.
   *
   * @return the decoded user-info part or {@code null} if undefined.
   */
  def getUserInfo(): String = decode(userinfo)

  /**
   * Gets the hashcode value of this URI instance.
   *
   * @return the appropriate hashcode value.
   */
  override def hashCode(): Int = {
    if (hash == -1) {
      hash = getHashString.hashCode
    }
    hash
  }

  /**
   * Indicates whether this URI is absolute, which means that a scheme part is
   * defined in this URI.
   *
   * @return {@code true} if this URI is absolute, {@code false} otherwise.
   */
  def isAbsolute(): Boolean = absolute

  /**
   * Indicates whether this URI is opaque or not. An opaque URI is absolute
   * and has a scheme-specific part which does not start with a slash
   * character. All parts except scheme, scheme-specific and fragment are
   * undefined.
   *
   * @return {@code true} if the URI is opaque, {@code false} otherwise.
   */
  def isOpaque(): Boolean = opaque

  private def normalize(path: String): String = {
    // count the number of '/'s, to determine number of segments
    var index = -1
    index = path.indexOf('/', index + 1)
    val pathlen: Int = path.length
    var size: Int    = 0
    if (pathlen > 0 && path.charAt(0) != '/') {
      size += 1
    }
    while (index != -1) {
      if (index + 1 < pathlen && path.charAt(index + 1) != '/') {
        size += 1
      }
      index = path.indexOf('/', index + 1)
    }

    val seglist: Array[String]  = Array.ofDim[String](size)
    val include: Array[Boolean] = Array.ofDim[Boolean](size)
    // break the path into segments and store in the list
    var current: Int = 0
    var index2: Int  = path.indexOf('/', index + 1)
    index = if (pathlen > 0 && path.charAt(0) == '/') 1 else 0
    while (index2 != -1) {
      seglist({ current += 1; current - 1 }) = path.substring(index, index2)
      index = index2 + 1
      index2 = path.indexOf('/', index + 1)
    }
    // and there are no more segments
    if (current < size) {
      seglist(current) = path.substring(index)
    }

    for (i <- 0 until size) {
      include(i) = true
      if (seglist(i) == "..") {
        var remove = i - 1
        while (remove > -1 && !include(remove)) {
          remove -= 1
        }
        if (remove > -1 && seglist(remove) != "..") {
          include(remove) = false
          include(i) = false
        }
      } else if (seglist(i) == ".") {
        include(i) = false
      }
    }

    val newpath: StringBuilder = new StringBuilder()
    if (path.startsWith("/")) {
      newpath.append('/')
    }
    for (i <- seglist.indices if include(i)) {
      newpath.append(seglist(i))
      newpath.append('/')
    }
    if (!path.endsWith("/") && seglist.length > 0 &&
        include(seglist.length - 1)) {
      newpath.deleteCharAt(newpath.length - 1)
    }
    var result: String = newpath.toString
    // prepend "./" to normalize
    index = result.indexOf(':')
    index2 = result.indexOf('/')
    if (index != -1 && (index < index2 || index2 == -1)) {
      newpath.insert(0, "./")
      result = newpath.toString
    }
    result
  }

  /**
   * Normalizes the path part of this URI.
   *
   * @return an URI object which represents this instance with a normalized
   *         path.
   */
  def normalize(): URI = {
    if (opaque) {
      return this
    }
    val normalizedPath: String = normalize(path)
    // if the path is already normalized, return this
    if (path == normalizedPath) {
      return this
    }
    // since the path of the normalized URI is different from this URI.
    val result: URI = duplicate()
    result.path = normalizedPath
    result.setSchemeSpecificPart()
    result
  }

  /**
   * Tries to parse the authority component of this URI to divide it into the
   * host, port, and user-info. If this URI is already determined as a
   * ServerAuthority this instance will be returned without changes.
   *
   * @return this instance with the components of the parsed server authority.
   * @throws URISyntaxException
   *             if the authority part could not be parsed as a server-based
   *             authority.
   */
  @throws(classOf[URISyntaxException])
  def parseServerAuthority(): URI = {
    if (!serverAuthority) {
      new Helper().parseAuthority(true)
    }
    this
  }

  /**
   * Makes the given URI {@code relative} to a relative URI against the URI
   * represented by this instance.
   *
   * @param relative
   *            the URI which has to be relativized against this URI.
   * @return the relative URI.
   */
  def relativize(relative: URI): URI = {
    if (relative.opaque || opaque) {
      return relative
    }
    if (if (scheme == null) relative.scheme != null
        else scheme != relative.scheme) {
      return relative
    }
    if (if (authority == null) relative.authority != null
        else authority != relative.authority) {
      return relative
    }
    // normalize both paths
    var thisPath: String     = normalize(path)
    val relativePath: String = normalize(relative.path)
    /*
     * if the paths aren't equal, then we need to determine if this URI's
     * path is a parent path (begins with) the relative URI's path
     */

    if (thisPath != relativePath) {
      // if this URI's path doesn't end in a '/', add one
      if (!thisPath.endsWith("/")) {
        thisPath = thisPath + '/'
      }
      /*
       * if the relative URI's path doesn't start with this URI's path,
       * then just return the relative URI; the URIs have nothing in
       * common
       */

      if (!relativePath.startsWith(thisPath)) {
        return relative
      }
    }
    val result: URI = new URI()
    result.fragment = relative.fragment
    result.query = relative.query
    // the result URI is the remainder of the relative URI's path
    result.path = relativePath.substring(thisPath.length)
    result.setSchemeSpecificPart()
    result
  }

  def resolve(relative: URI): URI = {
    if (relative.absolute || opaque) {
      return relative
    }

    var result: URI = null
    if (relative.path.==("") && relative.scheme == null &&
        relative.authority == null &&
        relative.query == null &&
        relative.fragment != null) {
      result = duplicate()
      result.fragment = relative.fragment
      return result
    }

    if (relative.authority != null) {
      result = relative.duplicate()
      result.scheme = scheme
      result.absolute = absolute
    } else {
      result = duplicate()
      result.fragment = relative.fragment
      result.query = relative.query
      if (relative.path.startsWith("/")) {
        result.path = relative.path
      } else {
        val endindex = path.lastIndexOf('/') + 1
        result.path = normalize(path.substring(0, endindex) + relative.path)
      }
      result.setSchemeSpecificPart()
    }
    result
  }

  private def setSchemeSpecificPart() = {
    val ssp = new StringBuilder()
    if (authority != null) {
      ssp.append("//" + authority)
    }
    if (path != null) {
      ssp.append(path)
    }
    if (query != null) {
      ssp.append("?" + query)
    }
    schemespecificpart = ssp.toString()
    string = null
  }

  def resolve(relative: String): URI = {
    resolve(URI.create(relative))
  }

  private def encodeOthers(s: String): String = {
    try {
      URIEncoderDecoder.encodeOthers(s)
    } catch {
      case e: UnsupportedEncodingException =>
        throw new RuntimeException(e.toString)
    }
  }

  private def decode(s: String): String = {
    if (s == null) return s

    try {
      return URIEncoderDecoder.decode(s)
    } catch {
      case e: UnsupportedEncodingException =>
        throw new RuntimeException(e.toString)
    }
  }

  def toASCIIString: String = {
    encodeOthers(toString)
  }

  override def toString: String = {
    if (string == null) {
      val result: StringBuilder = new StringBuilder()
      if (scheme != null) {
        result.append(scheme)
        result.append(':')
      }
      if (opaque) {
        result.append(schemespecificpart)
      } else {
        if (authority != null) {
          result.append("//")
          result.append(authority)
        }
        if (path != null) {
          result.append(path)
        }
        if (query != null) {
          result.append('?')
          result.append(query)
        }
      }
      if (fragment != null) {
        result.append('#')
        result.append(fragment)
      }
      string = result.toString
    }
    string
  }

  private def getHashString(): String = {
    val result: StringBuilder = new StringBuilder()
    if (scheme != null) {
      result.append(scheme.toLowerCase())
      result.append(':')
    }
    if (opaque) {
      result.append(schemespecificpart)
    } else {
      if (authority != null) {
        result.append("//")
        if (host == null) {
          result.append(authority)
        } else {
          if (userinfo != null) {
            result.append(userinfo + "@")
          }
          result.append(host.toLowerCase())
          if (port != -1) {
            result.append(":" + port)
          }
        }
      }
      if (path != null) {
        result.append(path)
      }
      if (query != null) {
        result.append('?')
        result.append(query)
      }
    }
    if (fragment != null) {
      result.append('#')
      result.append(fragment)
    }
    convertHexToLowerCase(result.toString)
  }

  def toURL() = throw new NotImplementedError()

}
