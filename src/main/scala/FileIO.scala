import scala.io.Source

object FileIO {
  // Pure function to read subscriptions from a JSON file
  def readSubscriptions(): List[String] = {
    List(
      "http://localhost:8123/r/scala/.json?count=10",
      "http://localhost:8123/r/programming/.json?count=10"
    )
  }

  // Pure function to download JSON feed from a URL
  def downloadFeed(url: String): String = {
    val source = Source.fromURL(url)
    source.mkString
  }
}
