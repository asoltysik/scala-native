package java.net

object SocketSuite extends tests.Suite {

  val s = new Socket()

  test("keepAlive") {
    val prevValue = s.getKeepAlive
    s.setKeepAlive(!prevValue)
    assertEquals(s.getKeepAlive, !prevValue)
  }

  test("reuseAddr") {
    val prevValue = s.getReuseAddress
    s.setReuseAddress(!prevValue)
    assertEquals(s.getReuseAddress, !prevValue)
  }

  test("OOBInline") {
    val prevValue = s.getOOBInline
    s.setOOBInline(!prevValue)
    assertEquals(s.getOOBInline, !prevValue)
  }

  test("tcpNoDelay") {
    val prevValue = s.getTcpNoDelay
    s.setTcpNoDelay(!prevValue)
    assertEquals(s.getTcpNoDelay, !prevValue)
  }

  /*test("soLinger") {
    val prevValue = s.getSoLinger
    s.setSoLinger(prevValue + 100)
    assertEquals(s.getSoLinger, prevValue + 100)
  }*/

  test("soTimeout") {
    val prevValue = s.getSoTimeout
    s.setSoTimeout(prevValue + 100)
    assertEquals(s.getSoTimeout, prevValue + 100)
  }

  test("receiveBufferSize") {
    val prevValue = s.getReceiveBufferSize
    s.setReceiveBufferSize(prevValue + 100)
    assertEquals(s.getReceiveBufferSize, (prevValue + 100) * 2)
  }

  test("sendBufferSize") {
    val prevValue = s.getSendBufferSize
    s.setSendBufferSize(prevValue + 100)
    assertEquals(s.getSendBufferSize, (prevValue + 100) * 2)
  }

  test("trafficClass") {
    s.setTrafficClass(0x28)
    assertEquals(s.getTrafficClass, 0x28)
  }

  test("connect with timeout") {
    val sock = new Socket()
    assertThrows[SocketTimeoutException] {
      sock.connect(new InetSocketAddress("123.123.123.123", 12341), 200)
    }
  }

}
