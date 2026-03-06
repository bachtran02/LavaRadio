package dev.bachtran.lavaradio.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {

    // 400 Bad Request
    @ExceptionHandler(
        IdentifierIsNotUrlException::class,
        InvalidLoopModeException::class,
        InvalidSeekException::class,
        InvalidQueueIndexException::class,
        MoveItemUnmatchedException::class,
        InvalidSourceException::class,
        StreamAlreadyActiveException::class,
        StreamInactiveException::class
    )
    fun handleBadRequest(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Bad request"))
    }

    // 404 Not Found
    @ExceptionHandler(
        StreamNotFoundException::class,
        NoResultsFoundException::class,
    )
    fun handleNotFound(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Not found"))
    }

    // 403 Forbidden
    @ExceptionHandler(NoStreamPermissionException::class)
    fun handleForbidden(ex: NoStreamPermissionException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(ex.message ?: "Forbidden"))
    }

    // Catch-all
    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("Something went wrong"))
    }
}