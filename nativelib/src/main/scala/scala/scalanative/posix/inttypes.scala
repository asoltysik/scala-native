package scala.scalanative
package posix

import native.{CUnsignedChar, CUnsignedInt, CUnsignedShort}

object inttypes {

  type uint8_t  = CUnsignedChar
  type uint16_t = CUnsignedShort
  type uint32_t = CUnsignedInt

}
