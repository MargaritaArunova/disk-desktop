FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Копируем Gradle wrapper и основные файлы сборки
COPY gradlew gradlew.bat build.gradle.kts ./
COPY gradle gradle

RUN chmod +x gradlew

# Предварительно прогреваем кеш зависимостей
RUN ./gradlew --no-daemon --version

# Копируем исходный код
COPY src src

# Сборка fat/regular jar
RUN ./gradlew --no-daemon clean build

FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Копируем собранный jar
COPY --from=build /app/build/libs/*.jar app.jar

# Переменная окружения для базового URL backend-а (пока используется только в коде, но может быть интегрирована)
ENV BACKEND_BASE_URL=http://host.docker.internal:8080/api

CMD ["java", "-jar", "app.jar"]

