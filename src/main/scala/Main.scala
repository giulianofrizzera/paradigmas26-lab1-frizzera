import FileIO.Subscription
object Main {
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"

    val subscriptions: List[Subscription] = FileIO.readSubscriptions("subscriptions.json")

    val allPosts: List[(String, String)] = subscriptions.map { case (name, url) => 
      println(s"Fetching posts from: $name ($url)") 
      val posts = FileIO.downloadFeed(url) 
      (name, posts) 
    }

    val output = allPosts
      .map { case (name, posts) => Formatters.formatSubscription(name, posts) }
      .mkString("\n")

    println(output)
  }
}
