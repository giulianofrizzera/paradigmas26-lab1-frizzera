import FileIO.Subscription
import FileIO.Post
object Main {
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"

    val subscriptions: List[Subscription] = FileIO.readSubscriptions("subscriptions.json").getOrElse(List())

    val allPosts = subscriptions.map { case (name, url) => 
      println(s"Fetching posts from: $name ($url)") 
      
      val postsOpt = for { 
        jsonContent <- FileIO.downloadFeed(url) 
        posts <- FileIO.parsePosts(name, jsonContent)
      } yield posts
      val posts = postsOpt.getOrElse(List())
      (name, url, posts)
    }

    val output = allPosts.map { case (name, url, posts) =>
      val allText = posts  
        .map { case (_, title, selftext, _, _, _) => s"$title $selftext" }
        .mkString(" ")

      val topWords = FileIO.wordcount(allText)
        .toList
        .sortBy { case (_, frecuencia) => -frecuencia}
        .take(5)

      val topPosts = posts.take(5)

      val scoreTotal = FileIO.totalScore(posts)
      println(s"suma total de puntajes: $scoreTotal")

      println("\nPalabras: ")
      topWords.foreach { case (palabra, apariciones) =>
        print(s"  - $palabra: $apariciones")
      }

      println("\n5 Primeros posts:")
      topPosts.foreach { case (subreddit, title, _, date, score, url) =>
        val scoreVal = score.getOrElse(0)
        println(s"  * [$date] ($subreddit) (Score: $score) $title\n   URL: $url")
      }
      println("=" * 50 )
    }
  }
}
