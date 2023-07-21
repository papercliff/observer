# Observer

Observer is a social media and news management tool written in Clojure. It helps to interact with multiple platforms,
generates hourly and daily trending topics and posts them to different social media platforms, also it commits to a
Github repository for news. It creates a word association graph and finds cliques of associated words that are being
used in combination frequently.

## Important namespaces

* `observer.apis.papercliff`: This is the main module where data about word associations are fetched, processed and the
cliques are identified.
* `observer.attempt`: This namespace is responsible for managing retry attempts with sleep intervals when a call to an
API fails.
* `observer.image`: This namespace is responsible for generating the screenshot of daily activity, saving it, and
posting it on various social media platforms. It also commits this image to a Github repository.
* `observer.text`: This namespace generates text posts that are posted to various social media platforms and Github.
The post includes the keywords identified from the word association graph and a link to a search query with those
keywords.

The project interacts with multiple platforms like Facebook, Github, LinkedIn, Mastodon, Reddit, Tumblr, and Twitter.
You need to have the respective API keys set up in your environment variables to successfully interact with these
platforms.
