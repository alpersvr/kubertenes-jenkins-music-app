pipeline {
    agent any // Jenkins'in bu pipeline'ı herhangi bir uygun agent üzerinde çalıştırmasını sağlar

    environment {
        // Jenkins'te tanımladığınız Docker Hub credentials'ın ID'si
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials' // BU ALANI DEĞİŞTİRİN
        // Docker Hub kullanıcı adınız ve imaj adınız
        DOCKER_IMAGE_NAME      = 'alpersever/kubertenes-jenkins-music-app' // BU ALANI DEĞİŞTİRİN
        KUBERNETES_YAML_PATH   = 'kubernetes' // Kubernetes YAML dosyalarınızın bulunduğu klasör
    }

    stages {
        stage('Checkout: Kodu Çek') { // Aşama 1: Projeyi lokal bilgisayara klonla [cite: 8]
            steps {
                // Github repository URL'nizi ve branch'inizi buraya yazın
                git branch: 'main', url: 'https://github.com/alpersvr/kubertenes-jenkins-music-app.git' // BU ALANI DEĞİŞTİRİN
            }
        }

        stage('Build: Projeyi Derle') { // Aşama 2: Projeyi derle ve JAR oluştur [cite: 9]
            steps {
                script {
                    // Maven projesi için (pom.xml varsa)
                    sh './mvnw clean package -DskipTests'
                    // Gradle projesi için (build.gradle varsa)
                    // sh './gradlew build -x test'
                }
            }
        }

        stage('Build Docker Image: Docker İmajı Oluştur') { // Aşama 3: Docker imajı oluştur [cite: 10]
            steps {
                script {
                    // Dockerfile dosyanızın projenin kök dizininde olduğunu varsayıyoruz
                    // BUILD_NUMBER Jenkins tarafından sağlanan bir ortam değişkenidir ve imajı etiketlemek için kullanılır
                    def customImage = docker.build("${env.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}", ".")
                    env.IMAGE_TAG = env.BUILD_NUMBER // Tag'i daha sonra kullanmak üzere sakla
                }
            }
        }

        stage('Login to Docker Hub: Docker Huba Giriş Yap') { // Aşama 4: Dockerhub'a giriş yap
            steps {
                script {
                    // Docker Hub registry ve Jenkins'te tanımlı credentials'ı kullanarak giriş yap
                    docker.withRegistry('https://registry.hub.docker.com', DOCKERHUB_CREDENTIALS_ID) {
                        echo "Docker Hub'a başarıyla giriş yapıldı."
                    }
                }
            }
        }

        stage('Push Docker Image: İmajı Docker Huba Yükle') { // Aşama 5: İmajı huba push et [cite: 11]
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', DOCKERHUB_CREDENTIALS_ID) {
                        // Oluşturulan ve etiketlenen imajı push et
                        docker.image("${env.DOCKER_IMAGE_NAME}:${env.IMAGE_TAG}").push()
                        // Opsiyonel: Aynı imajı 'latest' olarak da etiketleyip push edebilirsiniz
                        // docker.image("${env.DOCKER_IMAGE_NAME}:${env.IMAGE_TAG}").push('latest')
                        echo "İmaj ${env.DOCKER_IMAGE_NAME}:${env.IMAGE_TAG} Docker Hub'a yüklendi."
                    }
                }
            }
        }

        stage('Deploy to Kubernetes: Uygulamayı K8s e Dağıt') { // Aşama 6: K8s deployment dosyasını çalıştır [cite: 12]
            steps {
                script {
                    // Jenkins sunucusunun Minikube ile aynı makinede olduğunu ve kubectl'in ayarlı olduğunu varsayıyoruz
                    // deployment.yaml dosyasındaki imaj adının Docker Hub'a yüklediğiniz imajla eşleştiğinden emin olun
                    // (Örn: image: SİZİN_DOCKERHUB_KULLANICI_ADINIZ/SİZİN_İMAJ_ADINIZ:TAG)
                    // İmaj etiketini dinamik olarak değiştirmek daha gelişmiş bir yöntemdir,
                    // şimdilik deployment.yaml'de BUILD_NUMBER ile etiketlenmiş imajı kullanacağınızı varsayalım
                    // VEYA deployment.yaml'de 'latest' etiketini kullanabilirsiniz (push aşamasında latest tag'ini de push ettiyseniz)

                    // ÖNEMLİ: deployment.yaml dosyanızda image alanını güncelleyin!
                    // Örneğin: image: alialpersever/simple-app:${env.IMAGE_TAG} veya image: alialpersever/simple-app:latest

                    sh "kubectl apply -f ${env.KUBERNETES_YAML_PATH}/deployment.yaml"
                    // Deployment'ın durumunu kontrol et (deployment adını kendi YAML dosyanızdaki adla değiştirin)
                    sh "kubectl rollout status deployment/music-app-deployment --timeout=2m"
                }
            }
        }

        stage('Expose Service on Kubernetes: Servisi K8s de Aktif Et') { // Aşama 7: K8s service dosyasını çalıştır [cite: 12]
            steps {
                script {
                    sh "kubectl apply -f ${env.KUBERNETES_YAML_PATH}/service.yaml"
                    // Opsiyonel: Servis bilgilerini göster (servis adını kendi YAML dosyanızdaki adla değiştirin)
                    sh "kubectl get svc/music-app-service"
                    echo "Uygulamaya erişmek için 'minikube service music-app-service --url' komutunu kullanabilirsiniz."
                }
            }
        }
    }

    post { // Pipeline tamamlandıktan sonra çalışacak adımlar
        always {
            echo 'Pipeline tamamlandı.'
            // cleanWs() // Opsiyonel: Çalışma alanını temizle
        }
        success {
            echo 'Pipeline başarıyla tamamlandı!'
        }
        failure {
            echo 'Pipeline başarısız oldu!'
        }
    }
}