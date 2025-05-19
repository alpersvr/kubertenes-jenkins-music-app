pipeline {
    agent any // Jenkins'in bu pipeline'ı herhangi bir uygun agent üzerinde çalıştırmasını sağlar
    tools {
        // Manage Jenkins -> Tools altında Docker için verdiğiniz ismi buraya yazın
        dockerTool 'MyDocker' // Örneğin, 'MyDocker' olarak adlandırdıysanız
    }
    environment {
        // Jenkins'te tanımladığınız Docker Hub credentials'ın ID'si
        DOCKERHUB_CREDENTIALS_ID = 'dockerID' // BU ALANI DEĞİŞTİRİN
        // Docker Hub kullanıcı adınız ve imaj adınız
        DOCKER_IMAGE_NAME      = 'alpersever/kubertenes-jenkins-music-app' // BU ALANI DEĞİŞTİRİN
        KUBERNETES_YAML_PATH   = 'kubernetes' // Kubernetes YAML dosyalarınızın bulunduğu klasör
    }

    stages {
       /* stage('Checkout: Kodu Çek') { // Aşama 1: Projeyi lokal bilgisayara klonla [cite: 8]
            steps {
                // Github repository URL'nizi ve branch'inizi buraya yazın
                git branch: 'main', url: 'https://github.com/alpersvr/kubertenes-jenkins-music-app.git' // BU ALANI DEĞİŞTİRİN
            }
        }*/

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
                    // 'MyDocker', Manage Jenkins -> Tools altında Docker için verdiğiniz isim olmalı.
                    def dockerInstallationPath = tool('MyDocker') 

                    // Docker aracının yolunu mevcut PATH'in başına ekleyerek yeni bir PATH oluşturuyoruz.
                    withEnv(["PATH+Docker=${dockerInstallationPath}"]) {
                        echo "Jenkinsfile icinde guncellenmis PATH: ${env.PATH}" // Hata ayıklama için PATH'i yazdır
                        
                        // Dockerfile dosyanızın projenin kök dizininde olduğunu varsayıyoruz
                        // BUILD_NUMBER Jenkins tarafından sağlanan bir ortam değişkenidir ve imajı etiketlemek için kullanılır
                        def customImage = docker.build("${env.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}", ".")
                        env.IMAGE_TAG = env.BUILD_NUMBER // Tag'i daha sonra kullanmak üzere sakla
                    }
                }
            }
        }

        stage('Login to Docker Hub: Docker Huba Giriş Yap') {
            steps {
                script {
                    def dockerExecutable = "${tool('MyDocker')}/docker" 
                    echo "Docker Executable Path: ${dockerExecutable}"

                    withCredentials([usernamePassword(credentialsId: DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        echo "Attempting login for user: ${DOCKER_USERNAME}"
                        sh """
                            echo "\$DOCKER_PASSWORD" | '${dockerExecutable}' login -u "\$DOCKER_USERNAME" --password-stdin https://registry.hub.docker.com
                        """
                        // Login başarılı olduktan sonra, aynı oturumla bir pull denemesi yapalım.
                        // Eğer DOCKER_IMAGE_NAME daha önce hiç push edilmediyse bu satır hata verebilir.
                        // Bu durumda, Docker Hub'da var olan KENDİ PUBLIC imajlarınızdan birini kullanın
                        // veya bu pull testini atlayın.
                        // Şimdilik, bir önceki build'den kalan imajı çekmeyi deniyoruz (eğer varsa).
                        echo "Login sonrası ${env.DOCKER_IMAGE_NAME}:latest imajını çekme denemesi..."
                        // Pull komutunu try-catch içine alabiliriz, böylece imaj yoksa pipeline durmaz.
                        try {
                            sh "'${dockerExecutable}' pull '${env.DOCKER_IMAGE_NAME}:latest'"
                            echo "Test pull işlemi BAŞARILI. Login geçerli görünüyor."
                        } catch (Exception e) {
                            echo "UYARI: Test pull işlemi sırasında bir sorun oluştu veya imaj bulunamadı: ${e.getMessage()}"
                            echo "Login işlemi tamamlandı, ancak pull testi yapılamadı/başarısız oldu. Push aşamasına devam edilecek."
                        }
                    }
                    echo "Docker Hub'a giriş ve test pull denemesi tamamlandı." 
                }
            }
        }

        stage('Push Docker Image: İmajı Docker Huba Yükle') {
            steps {
                script {
                    def dockerExecutable = "${tool('MyDocker')}/docker" // Docker çalıştırılabilir dosyasının tam yolu
                    
                    // 1. Mevcut BUILD_NUMBER etiketli imajı push et
                    sh "'${dockerExecutable}' push '${env.DOCKER_IMAGE_NAME}:${env.IMAGE_TAG}'"
                    echo "İmaj ${env.DOCKER_IMAGE_NAME}:${env.IMAGE_TAG} Docker Hub'a push edildi."

                    // 2. Aynı imajı 'latest' olarak etiketle
                    sh "'${dockerExecutable}' tag '${env.DOCKER_IMAGE_NAME}:${env.IMAGE_TAG}' '${env.DOCKER_IMAGE_NAME}:latest'"
                    echo "İmaj ${env.DOCKER_IMAGE_NAME}:${env.IMAGE_TAG}, ayrıca ${env.DOCKER_IMAGE_NAME}:latest olarak etiketlendi."

                    // 3. 'latest' etiketli imajı push et
                    sh "'${dockerExecutable}' push '${env.DOCKER_IMAGE_NAME}:latest'"
                    echo "İmaj ${env.DOCKER_IMAGE_NAME}:latest Docker Hub'a push edildi."
                    
                    echo "Tüm imaj push işlemleri tamamlandı."
                }
            }
        }

        stage('Deploy to Kubernetes: Uygulamayı K8s e Dağıt') {
            steps {
                script {
                    echo "Mevcut çalışma dizini: ${pwd()}"
                    echo "kubernetes klasörünün içeriği:"
                    sh "ls -la kubernetes" // kubernetes klasörünün içeriğini listele

                    echo "deployment.yaml dosyasının varlığı ve içeriği:"
                    sh "cat kubernetes/deployment.yaml" // deployment.yaml dosyasının içeriğini göster (hata ayıklama için)

                    sh "kubectl apply -f ${env.KUBERNETES_YAML_PATH}/deployment.yaml"
                    sh "kubectl rollout status deployment/music-app-deployment --timeout=2m"
                }
            }
        }

        stage('Expose Service on Kubernetes: Servisi K8s de Aktif Et') {
            steps {
                script {
                    echo "kubernetes klasörünün içeriği (servis öncesi):"
                    sh "ls -la kubernetes"

                    echo "service.yaml dosyasının varlığı ve içeriği:"
                    sh "cat kubernetes/service.yaml" // service.yaml dosyasının içeriğini göster

                    sh "kubectl apply -f ${env.KUBERNETES_YAML_PATH}/service.yaml"
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
