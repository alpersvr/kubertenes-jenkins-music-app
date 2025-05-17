# Kullanılacak temel Java imajı (uygulamanızın Java sürümüne uygun bir imaj seçin)
# Örneğin, Java 17 için:
    FROM openjdk:17-jdk-slim

    # JAR dosyasının kopyalanacağı argüman
    ARG JAR_FILE=target/*.jar
    
    # JAR dosyasını container içine kopyala
    COPY ${JAR_FILE} app.jar
    
    # Uygulamanın çalışacağı port (Spring Boot varsayılanı 8080)
    EXPOSE 8081
    
    # Uygulamayı çalıştırma komutu
    ENTRYPOINT ["java","-jar","/app.jar"]