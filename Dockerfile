FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/market-analysis.jar /market-analysis/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/market-analysis/app.jar"]
