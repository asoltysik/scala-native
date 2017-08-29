package java.nio.channels

import java.io.IOException

class AcceptPendingException extends IllegalStateException

class AlreadyBoundException extends IllegalStateException

class AlreadyConnectedException extends IllegalStateException

class ClosedChannelException extends IOException("Channel closed")

class AsynchronousCloseException extends ClosedChannelException

class CancelledKeyException extends IllegalStateException

class ClosedByInterruptException extends AsynchronousCloseException

class ClosedSelectorException extends IllegalStateException

class ConnectionPendingException extends IllegalStateException

class FileLockInterruptionException extends IOException

class IllegalBlockingModeException extends IllegalStateException

class IllegalChannelGroupException extends IllegalArgumentException

class IllegalSelectorException extends IllegalArgumentException

class InteruptedByTimeoutException extends IOException

class NoConnectionPendingException extends IllegalStateException

class NonReadableChannelException extends IllegalStateException

class NonWritableChannelException extends IllegalStateException

class NotYetBoundException extends IllegalStateException

class NotYetConnectedException extends IllegalStateException

class OverlappingFileLockException extends IllegalStateException

class ReadPendingException extends IllegalStateException

class ShutdownChannelGroupException extends IllegalStateException

class UnresolvedAddressException extends IllegalArgumentException

class WritePendingException extends IllegalStateException

class UnsupportedAddressTypeException extends IllegalArgumentException
