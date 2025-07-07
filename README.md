StarCommunity

StarCommunity 是一个基于 Spring Boot + Kotlin + Jetpack Compose 构建的开源社区应用，支持用户发帖、评论、点赞、头像展示等功能，适用于移动端社区类应用开发与学习。


---

✨ 项目特性

👤 用户登陆/注册

📍 发布、查看、评论帖子

👍 点赞功能

📷 头像加载、版区分类

📊 操作日志 + 系统时间统一管理



---

🤖 技术栈

后端

Spring Boot 3.x

Spring Web / JPA / Validation

PostgreSQL 数据库

Hibernate ORM

RESTful API

Gradle Kotlin DSL


前端 (Android App)

Kotlin + Jetpack Compose

ViewModel + StateFlow

Retrofit2 + Gson + Coil

Material 3 Design



---

♻ 环境需求

组件	版本

JDK	17+
Gradle	8.x+
Kotlin	1.9+
Android SDK	33+
PostgreSQL	14+



---

🚀 构建 & 运行

后端 Spring Boot

# 基于 Gradle 构建
cd StarCommunity-main
./gradlew build

# 运行
java -jar build/libs/your-backend.jar

前端 Android App

在 Android Studio 中打开 StarCommunity-main/app/ ，选择设备运行即可。


---

📂 结构概览

StarCommunity/
  └─ StarCommunity-main/
      ├─ app/              # Android 前端源码
      ├─ build.gradle.kts # 项目构建文件
      ├─ settings.gradle.kts
      └─ src/...          # 后端 Spring Boot 源码


---

📅 未来计划

[ ] 支持 Markdown 帖子内容

[ ] 支持多媒体图片上传

[ ] 用户级别/类型管理

[ ] 推荐帖子算法优化



---


---

✉ 联系方式

如果想了解更多，或者希望参与开发，请联系:

Email: 528771345@qq.com


---

📄 License

MIT License
