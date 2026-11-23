import FileIO.Subscription
import FileIO.Post
object Main {
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"

    val subscriptions: List[Subscription] = FileIO.readSubscriptions("subscriptions.json").getOrElse(List())

    val allPosts: List[Post] = subscriptions.flatMap { case (name, url) => 
      println(s"Fetching posts from: $name ($url)") 
      
      val postsOpt = for { 
        jsonContent <- FileIO.downloadFeed(url) 
        posts <- FileIO.parsePosts(name, jsonContent)
      } yield posts
      postsOpt.getOrElse(List())
    }

    println(s"Total posts descargados: ${allPosts.length}")
    val output = allPosts
      .map { case (subreddit, title, selftext, date) => Formatters.formatPost(subreddit, title, selftext, date) }
      .mkString("\n")

    println(output)
  }
}
