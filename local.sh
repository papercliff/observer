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



ACCOUNT_ID=$(aws sts get-caller-identity --query 'Account' --output text) && \
REGION=$(aws configure get region) && \
ECR_REPOSITORY_URL="$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/observer" && \
LATEST_TAG=$(aws ecr describe-images --repository-name observer --query "sort_by(imageDetails,&imagePushedAt)[-1].imageTags[0]" --output text) && \
cp deployments/observer-text.yaml deployments/observer-text.yaml.bak && \
cp deployments/observer-image.yaml deployments/observer-image.yaml.bak && \
sed -i "s|{{ ECR_REPOSITORY_URL }}|$ECR_REPOSITORY_URL|g" deployments/observer-text.yaml && \
sed -i "s|{{ TAG }}|$LATEST_TAG|g" deployments/observer-text.yaml && \
sed -i "s|{{ ECR_REPOSITORY_URL }}|$ECR_REPOSITORY_URL|g" deployments/observer-image.yaml && \
sed -i "s|{{ TAG }}|$LATEST_TAG|g" deployments/observer-image.yaml && \
sed -i "s|{{ SSL_CERT_ARN }}|$SSL_CERT_ARN|g" deployments/observer-image.yaml && \
kubectl apply -f deployments/observer-text.yaml && \
kubectl apply -f deployments/observer-image.yaml && \
mv deployments/observer-text.yaml.bak deployments/observer-text.yaml && \
mv deployments/observer-image.yaml.bak deployments/observer-image.yaml



kubectl create job --from=cronjob/observer-text-cronjob observer-text-manual-job
kubectl create job --from=cronjob/observer-image-cronjob observer-image-manual-job
