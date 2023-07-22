docker build -t observer .
docker run --entrypoint lein --name observer-text-job observer:latest run -m observer.text
docker run --entrypoint lein --name observer-image-job observer:latest run -m observer.image
kubectl create secret generic facebook --from-env-file=deployments/secrets/facebook.txt
kubectl create secret generic github --from-env-file=deployments/secrets/github.txt
kubectl create secret generic imgur --from-env-file=deployments/secrets/imgur.txt
kubectl create secret generic linkedin --from-env-file=deployments/secrets/linkedin.txt
kubectl create secret generic mastodon --from-env-file=deployments/secrets/mastodon.txt
kubectl create secret generic papercliff-core --from-env-file=deployments/secrets/papercliff-core.txt
kubectl create secret generic rapidapi --from-env-file=deployments/secrets/rapidapi.txt
kubectl create secret generic reddit --from-env-file=deployments/secrets/reddit.txt
kubectl create secret generic tumblr --from-env-file=deployments/secrets/tumblr.txt
kubectl create secret generic twitter --from-env-file=deployments/secrets/twitter.txt
kubectl apply -f deployments/observer-text.yaml
kubectl apply -f deployments/observer-image.yaml
kubectl create job --from=cronjob/observer-text-cronjob observer-text-manual-job
kubectl create job --from=cronjob/observer-image-cronjob observer-image-manual-job
