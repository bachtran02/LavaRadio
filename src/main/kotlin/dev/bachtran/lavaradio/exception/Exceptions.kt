package dev.bachtran.lavaradio.exception

class IdentifierIsNotUrlException(identifier: String)
    : RuntimeException("Identifier is not a valid URL: $identifier")

class InvalidLoopModeException(mode: String)
    : RuntimeException("Invalid loop mode: $mode")

class InvalidSeekException(position: Long)
    : RuntimeException("Current track is stream or invalid position: $position")

class InvalidQueueIndexException
    : RuntimeException("Invalid queue index")

class MoveItemUnmatchedException
    : RuntimeException("Move item is inconsistent")

// --- Search exceptions ---

class NoResultsFoundException(query: String)
    : RuntimeException("No results found for URL: $query")

class InvalidSourceException(source: String)
    : RuntimeException("Unknown source: $source")

// --- Stream exceptions ---

class StreamAlreadyActiveException(streamId: String)
    : RuntimeException("Stream with ID $streamId is already active")

class StreamInactiveException(streamId: String)
    : RuntimeException("Stream with ID $streamId is inactive")

class StreamNotFoundException(streamId: String)
    : RuntimeException("Stream with ID $streamId not found")

class NoStreamPermissionException(streamId: String, userId: String)
    : RuntimeException("User $userId does not have permission to access stream $streamId")