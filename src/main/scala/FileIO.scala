import scala.io.Source
import org.json4s._
import org.json4s.jackson.JsonMethods._

object FileIO {
  type Subscription = (String, String) // (subredditName, url)
  implicit val formats: Formats = DefaultFormats 
  
  // Pure function to read subscriptions from a JSON file
  def readSubscriptions(filePath: String = "subscriptions.json"): List[Subscription] = {
    val source = Source.fromFile(filePath)
    try {
      val jsonString = source.mkString
      val json = parse(jsonString)

      json.children.map { item => 
        val name = (item \ "name").extract[String]
        val url = (item \ "url").extract[String]
        (name,url)
      }
    } finally {
      source.close()
    }
  }

  // Pure function to download JSON feed from a URL
  def downloadFeed(url: String): String = {
    val source = Source.fromURL(url)
    source.mkString
  }
}
