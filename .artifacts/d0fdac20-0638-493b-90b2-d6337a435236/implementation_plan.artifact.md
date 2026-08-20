# План внедрения WebView + OTA Updates

Перевод приложения на гибридную модель: основной интерфейс работает через WebView (локально), но может автоматически обновляться через GitHub без переустановки APK.

## User Review Required

> [!IMPORTANT]
> Для работы обновлений вам нужно будет создать репозиторий на GitHub и выложить туда файлы из папки `web`.
> Я настрою приложение на проверку файла `version.json` в вашем репозитории.

## Proposed Changes

### Assets & Web Content

#### [NEW] [index.html](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/assets/index.html)
Копия текущего `web/index.html` для обеспечения автономной работы при первом запуске.

#### [NEW] [style.css](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/assets/style.css)
Копия стилей для WebView.

### Android Code

#### [MODIFY] [MainActivity.kt](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/java/com/example/myapplication/MainActivity.kt)
- Удаление всего Compose UI.
- Инициализация `WebView` на весь экран.
- Настройка `WebSettings` (JavaScript, LocalStorage, Caching).
- Интеграция с `UpdateManager` для выбора источника загрузки (Assets или Internal Storage).

#### [NEW] [UpdateManager.kt](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/java/com/example/myapplication/UpdateManager.kt)
Логика "Over-The-Air" обновлений:
1. Проверка `version.json` на GitHub в фоновом потоке.
2. Сравнение версий.
3. Скачивание новых `index.html` и `style.css` во внутреннюю память телефона (`/data/user/0/.../files/www/`).
4. Обновление флага текущей версии.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/AndroidManifest.xml)
- Проверка разрешений на интернет.
- Включение `hardwareAccelerated` для плавной работы WebView.

## Verification Plan

### Manual Verification
1. Запустить приложение без интернета — должен открыться локальный интерфейс из Assets.
2. Включить интернет.
3. Я создам тестовый сценарий (имитация GitHub ответа), чтобы проверить, что файлы скачиваются и подменяются при следующем запуске.
4. Проверить работу всех функций (Supabase через JS должен работать так же, как в браузере).
