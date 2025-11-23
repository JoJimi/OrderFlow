dependencies {
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.example.user.UserServiceApplication")
}