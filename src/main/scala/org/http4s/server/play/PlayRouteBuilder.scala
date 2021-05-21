package org.http4s.server.play

import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fs2.interop.reactivestreams._
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Header, Headers, HttpApp, Method, Request, Response, Uri}
import play.api.libs.streams._
import play.api.http.{HttpChunk, HttpEntity}
import play.api.http.HttpEntity.{Chunked, Streamed, Strict}
import play.api.routing.Router.Routes
import play.api.mvc._
import cats.implicits._
import cats.effect._
import org.reactivestreams.Publisher

import scala.concurrent.ExecutionContext

object PlayRouteBuilder {
  private val akkaHttpSetsSeparately: Set[CaseInsensitiveString] =
    Set("Content-Type", "Content-Length", "Transfer-Encoding").map(CaseInsensitiveString.apply)

  private def requestHeaderToRequest[F[_]: ConcurrentEffect](
      requestHeader: RequestHeader,
      method: Method,
      publisher: Publisher[ByteString]
  ): Request[F] =
    Request[F](
      method = method,
      uri = Uri(path = requestHeader.uri),
      headers =
        Headers.apply(requestHeader.headers.toMap.toList.flatMap { case (headerName, values) =>
          values.map { value =>
            Header(headerName, value)
          }
        }),
      body = publisher
        .toStream[F]()
        .flatMap[F, Byte](bs => fs2.Stream.chunk[fs2.Pure, Byte](fs2.Chunk.bytes(bs.toArray)))
    )

  private def responseBuilder[F[_]](response: Response[F])(body: HttpEntity) =
    Result(
      header = ResponseHeader(
        status = response.status.code,
        headers = response.headers.toList.collect {
          case header if !akkaHttpSetsSeparately.contains(header.name) =>
            header.parsed.name.value -> header.parsed.value
        }.toMap
      ),
      body = body
    )

  private def convertResponseToResultStreamed[F[_]: ConcurrentEffect](
      response: Response[F]
  ): F[Result] =
    ConcurrentEffect[F]
      .delay(
        Streamed(
          data = Source.fromPublisher(
            response.body.chunks.map(chunk => ByteString(chunk.toArray)).toUnicastPublisher()
          ),
          contentLength = response.contentLength,
          contentType = response.contentType.map(_.value)
        )
      )
      .map(responseBuilder(response))

  private def convertResponseToResultChunked[F[_]: ConcurrentEffect](
      response: Response[F]
  ): F[Result] =
    ConcurrentEffect[F]
      .delay(
        Chunked(
          chunks = Source.fromPublisher(
            response.body.chunks
              .map(chunk => HttpChunk.Chunk(ByteString(chunk.toArray)))
              .toUnicastPublisher()
          ),
          contentType = response.contentType.map(_.value)
        )
      )
      .map(responseBuilder(response))

  private def convertResponseToResultStrict[F[_]: ConcurrentEffect](
      response: Response[F]
  ): F[Result] =
    response.body.chunks
      .map(chunk => ByteString(chunk.toArray))
      .reduce(_ ++ _)
      .compile
      .lastOrError
      .map(data =>
        Strict(
          data = data,
          contentType = response.contentType.map(_.value)
        )
      )
      .map(responseBuilder(response))

  private object MethodMatches {
    def unapply(requestHeader: RequestHeader): Option[Method] =
      Method.fromString(requestHeader.method).toOption
  }

  def httpAppToRoutes[F[_]: ConcurrentEffect](
      httpApp: HttpApp[F],
      executionContext: ExecutionContext
  ): Routes = { case MethodMatches(method) =>
    EssentialAction { requestHeader =>
      Accumulator(
        Sink.asPublisher[ByteString](fanout = false).mapMaterializedValue { publisher =>
          IO.suspend(
            ConcurrentEffect.toIOFromRunCancelable(
              Async.shift[F](executionContext) *> httpApp(
                requestHeaderToRequest[F](requestHeader, method, publisher)
              )
                .flatMap(convertResponseToResultChunked[F])
            )
          ).unsafeToFuture()
        }
      )
    }
  }
}
