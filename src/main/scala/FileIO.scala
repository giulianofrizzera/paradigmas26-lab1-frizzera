import scala.io.Source
import scala.util.Try
import org.json4s._
import org.json4s.jackson.JsonMethods._
import java.net.{HttpURLConnection, URI}

object FileIO {
  type Subscription = (String, String) // (subredditName, url)
  type Post = (String, String, String, String) // (subreddit, title, selftext,)

  implicit val formats: Formats = DefaultFormats 

  // Pure function to read subscriptions from a JSON file
  def readSubscriptions(filePath: String = "subscriptions.json"): Option[List[Subscription]] = {
    Try {
      val source = Source.fromFile(filePath)
      val jsonString = source.mkString
      source.close()
      val json = parse(jsonString)

      json.children.map { item => 
        val name = (item \ "name").extract[String]
        val url = (item \ "url").extract[String]
        (name,url)
      }
    }.toOption
  }

  // Pure function to download JSON feed from a URL
  def downloadFeed(url: String): Option[String] = {
    Try{
      val source = Source.fromURL(url)
      source.mkString
    }.toOption
  }

  def parsePosts(subreddit: String, jsonString: String): Option[List[Post]] = {
    Try {
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
    }.toOption
  }
}
