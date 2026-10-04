# Reglas para la build de release (minify desactivado por defecto).
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.barriored.app.**$$serializer { *; }
-keepclassmembers class com.barriored.app.** { *** Companion; }
