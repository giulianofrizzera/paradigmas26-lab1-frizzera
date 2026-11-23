
object Formatters {

  // Pure function to format posts from a subscription
  def formatSubscription(url: String, posts: String): String = {
    val header = s"\n${"=" * 80}\nPosts from: $url \n${"=" * 80}"
    val formattedPosts = posts.take(80)
    header + "\n" + formattedPosts
  }

  def formatPost(subreddit: String, title: String, selftext: String, date: String): String = { 
    s"""-------------------------------------------------------------------------------- 
    Subreddit: $subreddit | Fecha: $date 
    Título: $title 
    -------------------------------------------------------------------------------- 
    $selftext 
    """ 
  } 
}
