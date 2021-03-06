package zio.json

import java.util.Arrays
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.TimeUnit

import zio.json
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import io.circe
import zio.json.GoogleMapsAPIBenchmarks._
import zio.json.TestUtils._
import zio.json.data.googlemaps._
import org.openjdk.jmh.annotations._
import play.api.libs.{ json => Play }

import scala.util.Try

// To enable the yourkit agent enable a profiling mode, e.g.:
//
// set neoJmhYourkit in Jmh := Seq("sampling")
// set neoJmhYourkit in Jmh := Seq("allocsampled")
//
// more options at https://www.yourkit.com/docs/java/help/startup_options.jsp
//
// When profiling only run one longer test at a time, e.g.
//
// jmh:run -i 1 -wi 0 -r60 GoogleMaps.*decodeZioSuccess1
//
// and look for the generated snapshot in YourKit (ignore the rest)
//
// Also try the async profiler, e.g.
//
//  jmh:run -i 1 -wi 0 -r60 -prof jmh.extras.Async GoogleMaps.*encodeMagnolia
//  jmh:run -i 1 -wi 0 -r60 -prof jmh.extras.Async:event=alloc GoogleMaps.*encodeMagnolia
//
// which may require kernel permissions:
//
//   echo 1 | sudo tee /proc/sys/kernel/perf_event_paranoid
//   echo 0 | sudo tee /proc/sys/kernel/kptr_restrict
//
// and needs these projects installed, with these variables:
//
// export ASYNC_PROFILER_DIR=$HOME/Projects/async-profiler
// export FLAME_GRAPH_DIR=$HOME/Projects/FlameGraph
//
// http://malaw.ski/2017/12/10/automatic-flamegraph-generation-from-jmh-benchmarks-using-sbt-jmh-extras-plain-java-too/
// (note you need to type `make` in the async-profiler directory)
//
// to use allocation profiling, you need debugging symbols in your jvm. e.g. use
// the Zulu Java distribution.

@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
//, jvmArgs=Array("-XX:-OmitStackTraceInFastThrow"))
//, jvmArgs=Array("-XX:-StackTraceInThrowable"))
class GoogleMapsAPIBenchmarks {
  var jsonString, jsonStringCompact, jsonStringErr, jsonStringErrParse, jsonStringErrNumber: String  = _
  var jsonStringAttack0, jsonStringAttack1, jsonStringAttack2, jsonStringAttack3: String             = _
  var jsonChars, jsonCharsCompact, jsonCharsErr, jsonCharsErrParse, jsonCharsErrNumber: CharSequence = _
  var jsonCharsAttack0, jsonCharsAttack1, jsonCharsAttack2, jsonCharsAttack3: CharSequence           = _

  @Setup
  def setup(): Unit = {
    //Distance Matrix API call for top-10 by population cities in US:
    //https://maps.googleapis.com/maps/api/distancematrix/json?origins=New+York|Los+Angeles|Chicago|Houston|Phoenix+AZ|Philadelphia|San+Antonio|San+Diego|Dallas|San+Jose&destinations=New+York|Los+Angeles|Chicago|Houston|Phoenix+AZ|Philadelphia|San+Antonio|San+Diego|Dallas|San+Jose
    jsonString = getResourceAsString("google_maps_api_response.json")
    jsonChars = asChars(jsonString)
    jsonStringCompact = getResourceAsString(
      "google_maps_api_compact_response.json"
    )
    jsonCharsCompact = asChars(jsonStringCompact)
    jsonStringErr = getResourceAsString("google_maps_api_error_response.json")
    jsonCharsErr = asChars(jsonStringErr)

    // jmh:run GoogleMaps.*ErrorParse
    jsonStringErrParse = getResourceAsString("google_maps_api_error_parse.json")
    jsonCharsErrParse = asChars(jsonStringErr)
    jsonStringErrNumber = getResourceAsString(
      "google_maps_api_error_number.json"
    )
    jsonCharsErrNumber = asChars(jsonStringErr)

    jsonStringAttack0 = getResourceAsString("google_maps_api_attack0.json")
    jsonCharsAttack0 = asChars(jsonStringAttack0)
    jsonStringAttack1 = getResourceAsString("google_maps_api_attack1.json")
    jsonCharsAttack1 = asChars(jsonStringAttack1)
    jsonStringAttack2 = getResourceAsString("google_maps_api_attack2.json")
    jsonCharsAttack2 = asChars(jsonStringAttack2)
    jsonStringAttack3 = getResourceAsString("google_maps_api_attack3.json")
    jsonCharsAttack3 = asChars(jsonStringAttack3)

    assert(decodeCirceSuccess1() == decodeZioSuccess1())
    assert(decodeCirceSuccess2() == decodeZioSuccess2())
    assert(decodeCirceSuccess1() == decodePlaySuccess1())
    assert(decodeCirceSuccess2() == decodePlaySuccess2())

    assert(decodeCirceSuccess1() == decodeCirceAttack0())
    assert(decodeCirceSuccess1() == decodeZioAttack0())
    assert(decodeCirceSuccess1() == decodePlayAttack0())

    assert(decodeCirceSuccess1() == decodeCirceAttack1())
    assert(decodeCirceSuccess1() == decodeZioAttack1())
    assert(decodeCirceSuccess1() == decodePlayAttack1())

    assert(decodeCirceSuccess1() == decodeCirceAttack2())
    assert(decodeCirceSuccess1() == decodeZioAttack2())
    assert(decodeCirceSuccess1() == decodePlayAttack2())
  }

  // @Benchmark
  // def decodeJsoniterSuccess1(): Either[String, DistanceMatrix] =
  //   Try(readFromArray(jsonString.getBytes(UTF_8)))
  //     .fold(t => Left(t.toString), Right.apply)

  // @Benchmark
  // def decodeJsoniterSuccess2(): Either[String, DistanceMatrix] =
  //   Try(readFromArray(jsonStringCompact.getBytes(UTF_8)))
  //     .fold(t => Left(t.toString), Right.apply)

  // @Benchmark
  // def decodeJsoniterError(): Either[String, DistanceMatrix] =
  //   Try(readFromArray(jsonStringErr.getBytes(UTF_8)))
  //     .fold(t => Left(t.toString), Right.apply)

  // @Benchmark
  // def decodeJsoniterAttack1(): Either[String, DistanceMatrix] =
  //   Try(readFromArray(jsonStringAttack1.getBytes(UTF_8)))
  //     .fold(t => Left(t.toString), Right.apply)

  // @Benchmark
  // def decodeJsoniterAttack2(): Either[String, DistanceMatrix] =
  //   Try(readFromArray(jsonStringAttack2.getBytes(UTF_8)))
  //     .fold(t => Left(t.toString), Right.apply)

  // @Benchmark
  // def decodeJsoniterAttack3(): Either[String, DistanceMatrix] =
  //   Try(readFromArray(jsonStringAttack3.getBytes(UTF_8)))
  //     .fold(t => Left(t.toString), Right.apply)

  @Benchmark
  def decodeCirceSuccess1(): Either[circe.Error, DistanceMatrix] =
    circe.parser.decode[DistanceMatrix](jsonString)

  @Benchmark
  def decodeCirceSuccess2(): Either[circe.Error, DistanceMatrix] =
    circe.parser.decode[DistanceMatrix](jsonStringCompact)

  // @Benchmark
  // def decodeCirceError(): Either[circe.Error, DistanceMatrix] =
  //   circe.parser.decode[DistanceMatrix](jsonStringErr)

  @Benchmark
  def decodeCirceErrorParse(): Either[circe.Error, DistanceMatrix] =
    circe.parser.decode[DistanceMatrix](jsonStringErrParse)

  @Benchmark
  def decodeCirceErrorNumber(): Either[circe.Error, DistanceMatrix] =
    circe.parser.decode[DistanceMatrix](jsonStringErrNumber)

  @Benchmark
  def decodeCirceAttack0(): Either[circe.Error, DistanceMatrix] =
    circe.parser.decode[DistanceMatrix](jsonStringAttack0)

  @Benchmark
  def decodeCirceAttack1(): Either[circe.Error, DistanceMatrix] =
    circe.parser.decode[DistanceMatrix](jsonStringAttack1)

  @Benchmark
  def decodeCirceAttack2(): Either[circe.Error, DistanceMatrix] =
    circe.parser.decode[DistanceMatrix](jsonStringAttack2)

  @Benchmark
  def decodeCirceAttack3(): Either[circe.Error, DistanceMatrix] =
    circe.parser.decode[DistanceMatrix](jsonStringAttack3)

  def playDecode[A](
    str: String
  )(implicit R: Play.Reads[A]): Either[String, A] =
    Try(Play.Json.parse(str).as[A]).fold(
      // if we don't access the stacktrace then the JVM can optimise it away in
      // these tight loop perf tests, which would cover up a real bottleneck
      err => Left(Arrays.toString(err.getStackTrace().asInstanceOf[Array[Object]])),
      a => Right(a)
    )

  @Benchmark
  def decodePlaySuccess1(): Either[String, DistanceMatrix] =
    playDecode[DistanceMatrix](jsonString)

  @Benchmark
  def decodePlaySuccess2(): Either[String, DistanceMatrix] =
    playDecode[DistanceMatrix](jsonStringCompact)

  // @Benchmark
  // def decodePlayError(): Either[String, DistanceMatrix] =
  //   playDecode[DistanceMatrix](jsonStringErr)

  @Benchmark
  def decodePlayErrorParse(): Either[String, DistanceMatrix] =
    playDecode[DistanceMatrix](jsonStringErrParse)

  @Benchmark
  def decodePlayErrorNumber(): Either[String, DistanceMatrix] =
    playDecode[DistanceMatrix](jsonStringErrNumber)

  @Benchmark
  def decodePlayAttack0(): Either[String, DistanceMatrix] =
    playDecode[DistanceMatrix](jsonStringAttack0)

  @Benchmark
  def decodePlayAttack1(): Either[String, DistanceMatrix] =
    playDecode[DistanceMatrix](jsonStringAttack1)

  @Benchmark
  def decodePlayAttack2(): Either[String, DistanceMatrix] =
    playDecode[DistanceMatrix](jsonStringAttack2)

  @Benchmark
  def decodePlayAttack3(): Either[String, DistanceMatrix] =
    playDecode[DistanceMatrix](jsonStringAttack3)

  @Benchmark
  def decodeZioSuccess1(): Either[String, DistanceMatrix] =
    json.parser.decode[DistanceMatrix](jsonChars)

  @Benchmark
  def decodeZioSuccess2(): Either[String, DistanceMatrix] =
    json.parser.decode[DistanceMatrix](jsonCharsCompact)

  // @Benchmark
  // def decodeZioError(): Either[String, DistanceMatrix] =
  //   json.parser.decode[DistanceMatrix](jsonCharsErr)

  @Benchmark
  def decodeZioErrorParse(): Either[String, DistanceMatrix] =
    json.parser.decode[DistanceMatrix](jsonCharsErrParse)

  @Benchmark
  def decodeZioErrorNumber(): Either[String, DistanceMatrix] =
    json.parser.decode[DistanceMatrix](jsonCharsErrNumber)

  @Benchmark
  def decodeZioAttack0(): Either[String, DistanceMatrix] =
    json.parser.decode[DistanceMatrix](jsonCharsAttack0)

  @Benchmark
  def decodeZioAttack1(): Either[String, DistanceMatrix] =
    json.parser.decode[DistanceMatrix](jsonCharsAttack1)

  @Benchmark
  def decodeZioAttack2(): Either[String, DistanceMatrix] =
    json.parser.decode[DistanceMatrix](jsonCharsAttack2)

  @Benchmark
  def decodeZioAttack3(): Either[String, DistanceMatrix] =
    json.parser.decode[DistanceMatrix](jsonCharsAttack3)

}

object GoogleMapsAPIBenchmarks {
  implicit val codec: JsonValueCodec[DistanceMatrix] =
    JsonCodecMaker.make
}
