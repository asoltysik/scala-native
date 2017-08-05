package java.nio.channels.spi

import java.nio.channels._
import java.net.ProtocolFamily

private[spi] class SelectorProviderImpl extends SelectorProvider {

  def openDatagramChannel: DatagramChannel                         = ???
  def openDatagramChannel(family: ProtocolFamily): DatagramChannel = ???
  def openPipe: Pipe                                               = ???
  def openSelector: AbstractSelector                               = ???
  def openServerSocketChannel: ServerSocketChannel                 = ???
  def openSocketChannel: SocketChannel                             = ???

}
