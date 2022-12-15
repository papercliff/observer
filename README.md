# observer

This script (`observer.core/-main`) runs every hour, identifies new stories, and
posts them on [Twitter](https://twitter.com/papercliff_api)
and [Mastodon](https://newsie.social/@papercliff).

To be executed successfully, the following environment variables need to be set:
* `X_RAPIDAPI_KEY`
* `TWITTER_API_KEY`
* `TWITTER_API_KEY_SECRET`
* `TWITTER_API_ACCESS_TOKEN`
* `TWITTER_API_ACCESS_TOKEN_SECRET`
* `MASTODON_ACCESS_TOKEN`
