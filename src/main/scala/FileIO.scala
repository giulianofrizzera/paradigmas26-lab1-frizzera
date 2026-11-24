import scala.io.Source
import scala.util.Try
import org.json4s._
import org.json4s.jackson.JsonMethods._
import java.net.{HttpURLConnection, URI}

object FileIO {
  type Subscription = (String, String) // (subredditName, url)
  type Post = (String, String, String, String, Option[Int], String) // (subreddit, title, selftext,)

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
      val content = source.mkString
      source.close()
      content
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
        val score = (data \ "score").extractOpt[Int]
        val url = (data \ "url").extract[String]
        (subreddit, title, selftext, date, score, url)
      }.filter { case (_, title, selftext, _, _, _) =>
        selftext.trim.nonEmpty && title.trim.nonEmpty
      }
    }.toOption
  }

  def wordcount(subText: String): Map[String, Int] =  {
    val stopwords: Set[String] = Set(
      "the", "about", "above", "after", "again", "against", "all", "am", "an",
      "and", "any", "are", "aren't", "as", "at", "be", "because", "been",
      "before", "being", "below", "between", "both", "but", "by", "can't",
      "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't",
      "doing", "don't", "down", "during", "each", "few", "for", "from", "further",
      "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd",
      "he'll", "he's", "her", "here", "here's", "hers", "herself", "him",
      "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if",
      "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me",
      "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off",
      "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves",
      "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's",
      "should", "shouldn't", "so", "some", "such", "than", "that", "that's",
      "the", "their", "theirs", "them", "themselves", "then", "there", "there's",
      "these", "they", "they'd", "they'll", "re", "they've", "this", "those",
      "through", "to", "too", "under", "until", "up", "very", "was", "wasn't",
      "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what",
      "what's", "when", "when's", "where", "where's", "which", "while", "who",
      "who's", "whom", "why", "why's", "with", "won't", "would",
      "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours",
      "yourself", "yourselves"
    )

    subText
      .split("\\s+")
      .map(_.replaceAll("[^a-zA-Z]", ""))
      .filter{ p => 
        p.nonEmpty &&
        p.head.isUpper &&
        !stopwords.contains(p.toLowerCase)
      }
      .groupBy(identity)
      .map { case (palabra, lista) => (palabra, lista.size) }
  }

  def totalScore(posts: List[Post]): Int ={
    posts.foldLeft(0) { (acumulado, post) =>
      val (_, _, _, _, score, _) = post
      acumulado + score.getOrElse(0)
    }
  }
}
