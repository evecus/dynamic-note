# ===== 项目混淆规则 =====
# 保留 Kotlin 元数据，Compose / kotlinx.serialization 等框架依赖它做反射
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions, EnclosingMethod
-keep class kotlin.Metadata { *; }

# Room 实体 / DAO / 数据库及其生成实现：保留字段与构造函数，避免反射访问失败
-keep class com.note.dynamic.data.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**

# Compose runtime 通过反射查找组合，保留相关注解已被上面覆盖
-dontwarn androidx.compose.**

#协程
-dontwarn kotlinx.coroutines.**
