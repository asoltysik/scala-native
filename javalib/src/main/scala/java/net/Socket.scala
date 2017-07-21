package java.net

import java.io.{InputStream, OutputStream, IOException, Closeable}

class Socket protected (impl: SocketImpl,
                        private var addr: InetAddress,
                        private var port: Int,
                        private var localAddr: InetAddress,
                        private var localPort: Int,
                        streaming: Boolean,
                        shouldStartup: Boolean)
    extends Closeable {

  private var created        = false
  private var bound          = false
  private var connected      = false
  private var closed         = false
  private var inputShutdown  = false
  private var outputShutdown = false

  if (shouldStartup) {
    startup(addr, port)
  }

  private def startup(dstAddr: InetAddress, dstPort: Int, timeout: Int = 0) = {
    if (dstPort < 0 || dstPort > 65535)
      throw new IllegalArgumentException(
        "Socket port must be between 0 and 65535")

    impl.create(streaming)
    val created = true
    try {
      bound = true
      impl.connect(new InetSocketAddress(dstAddr, dstPort), timeout)
      connected = true
    } catch {
      case e: IOException => {
        impl.close
        throw e
      }
    }
  }

  def this() =
    this(new PlainSocketImpl(), null, -1, null, 0, true, false)

  def this(impl: SocketImpl) =
    this(impl, null, -1, null, 0, true, false)

  def this(address: InetAddress, port: Int) =
    this(new PlainSocketImpl(), address, port, null, 0, true, true)

  def this(address: InetAddress,
           port: Int,
           localAddr: InetAddress,
           localPort: Int) =
    this(new PlainSocketImpl(),
         address,
         port,
         localAddr,
         localPort,
         true,
         true)

  def this(host: String, port: Int) =
    this(new PlainSocketImpl(),
         InetAddress.getByName(host),
         port,
         null,
         0,
         true,
         true)

  def this(host: String, port: Int, localAddr: InetAddress, localPort: Int) =
    this(new PlainSocketImpl(),
         InetAddress.getByName(host),
         port,
         localAddr,
         localPort,
         true,
         true)

  def this(host: InetAddress, port: Int, stream: Boolean) =
    this(new PlainSocketImpl(), host, port, null, 0, stream, true)

  def this(host: String, port: Int, stream: Boolean) =
    this(new PlainSocketImpl(),
         InetAddress.getByName(host),
         port,
         null,
         0,
         true,
         true)

  private def checkClosedAndCreate: Unit = {
    if (closed) {
      throw new SocketException("Socket is closed")
    }

    if (!created) {
      impl.create(true)
      created = true
    }
  }

  // def this(proxy: Proxy)

  // def bind(bindpoint: SocketAddress): Unit

  def connect(endpoint: SocketAddress): Unit = connect(endpoint, 0)

  def connect(endpoint: SocketAddress, timeout: Int): Unit = {
    if (!endpoint.isInstanceOf[InetSocketAddress] || endpoint == null) {
      throw new IllegalArgumentException(
        "Invalid address argument to connect - " +
          "either of unsupported SocketAddress subclass or null")
    }
    val inetAddr = endpoint.asInstanceOf[InetSocketAddress]
    this.addr = inetAddr.getAddress
    this.port = inetAddr.getPort
    startup(addr, port, timeout)
  }

  // def getChannel: SocketChannel

  def getInetAddress: InetAddress  = addr
  def getLocalAddress: InetAddress = localAddr
  def getLocalPort: Int            = localPort
  def getPort: Int                 = port

  def getRemoteSocketAddress: SocketAddress = {
    if (!connected) null
    else new InetSocketAddress(addr, port)
  }

  def getLocalSocketAddress: SocketAddress = {
    if (!bound) null
    else new InetSocketAddress(localAddr, localPort)
  }

  def getKeepAlive: Boolean = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.SO_KEEPALIVE).asInstanceOf[Boolean]
  }

  def getOOBInline: Boolean = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.SO_OOBINLINE).asInstanceOf[Boolean]
  }

  def getReceiveBufferSize: Int = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.SO_RCVBUF).asInstanceOf[Int]
  }

  def getReuseAddress: Boolean = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.SO_REUSEADDR).asInstanceOf[Boolean]
  }

  def getSendBufferSize: Int = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.SO_SNDBUF).asInstanceOf[Int]
  }

  def getSoLinger: Int = {
    checkClosedAndCreate
    val value = impl.getOption(SocketOptions.SO_LINGER).asInstanceOf[Int]
    if (value == 0) -1 else value
  }

  def getSoTimeout: Int = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.SO_TIMEOUT).asInstanceOf[Int]
  }

  def getTcpNoDelay: Boolean = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.TCP_NODELAY).asInstanceOf[Boolean]
  }

  def getTrafficClass: Int = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.IP_TOS).asInstanceOf[Int]
  }

  def isBound: Boolean          = bound
  def isClosed: Boolean         = closed
  def isConnected: Boolean      = connected
  def isInputShutdown: Boolean  = inputShutdown
  def isOutputShutdown: Boolean = outputShutdown

  // def sendUrgentData(data: Int): Unit

  def setKeepAlive(on: Boolean): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.SO_KEEPALIVE, Boolean.box(on))
  }

  def setOOBInline(on: Boolean): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.SO_OOBINLINE, Boolean.box(on))
  }

  def setReceiveBufferSize(size: Int): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size))
  }

  def setReuseAddress(on: Boolean): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.SO_REUSEADDR, Boolean.box(on))
  }

  def setSendBufferSize(size: Int): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(size))
  }

  def setSoLinger(on: Boolean, linger: Int): Unit = {
    checkClosedAndCreate
    if (on && linger < 0) {
      throw new IllegalArgumentException("Linger is negative")
    }

    val value = if (on) {
      if (linger > 65535) 65535 else linger
    } else {
      -1
    }

    impl.setOption(SocketOptions.SO_LINGER, Integer.valueOf(value))
  }

  def setSoTimeout(timeout: Int): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout))
  }

  def setTcpNoDelay(on: Boolean): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.TCP_NODELAY, Boolean.box(on))
  }

  def setTrafficClass(tc: Int): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.IP_TOS, Integer.valueOf(tc))
  }

  def shutdownInput: Unit = {
    impl.shutdownInput
    inputShutdown = true
  }

  def shutdownOutput: Unit = {
    impl.shutdownOutput
    outputShutdown = true
  }

  override def close: Unit = impl.close

  // def setPerformancePreferences(connectionTime: Int, latency: Int, bandwith: Int): Unit

  override def toString: String = {
    if (connected)
      impl.toString
    else
      "Socket[unconnected]"
  }

  def getOutputStream: OutputStream = impl.getOutputStream

  def getInputStream: InputStream = impl.getInputStream

}

object Socket {
  // def setSocketImplFactory(fac: SocketImplFactory): Unit
}
