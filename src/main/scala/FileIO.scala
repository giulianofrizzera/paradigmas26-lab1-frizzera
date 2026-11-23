import scala.io.Source
import org.json4s._
import org.json4s.jackson.JsonMethods._
import java.net.{HttpURLConnection, URI}

object FileIO {
  type Subscription = (String, String) // (subredditName, url)
  type Post = (String, String, String, String) // (subreddit, title, selftext,)

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

  def parsePosts(subreddit: String, jsonString: String): List[Post] = {
    val json = parse(jsonString)
    val children = (json \ "data" \ "children").children

    children.map { item => 
      val data = item \ "data"
      val title = (data \ "title").extract[String]
      val selftext = (data \ "selftext").extract[String]
      val createdUtc = (data \ "created_utc").extract[Double].toLong
      val date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss") .format(new java.util.Date(createdUtc * 1000L))
      (subreddit, title, selftext, date)
    }.filter { case (_, title, selftext,_) =>
      selftext.trim.nonEmpty && title.trim.nonEmpty
    }
  }
}
