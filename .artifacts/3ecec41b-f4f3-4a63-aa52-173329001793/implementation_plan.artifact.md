# План реализации оффлайн-режима и синхронизации данных

Добавление возможности работы приложения без сети с последующей автоматической синхронизацией данных с Supabase при восстановлении подключения.

## Proposed Changes

### 1. Конфигурация зависимостей
Добавление библиотек Room (для локального хранения) и WorkManager (для фоновой синхронизации).

#### [MODIFY] [libs.versions.toml](file:///C:/Users/nuram/projects/newKAssa-main/gradle/libs.versions.toml)
#### [MODIFY] [build.gradle.kts](file:///C:/Users/nuram/projects/newKAssa-main/app/build.gradle.kts)

### 2. Мониторинг сетевого статуса
Создание утилиты для отслеживания состояния интернета в реальном времени.

#### [NEW] [NetworkObserver.kt](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/java/com/example/myapplication/utils/NetworkObserver.kt)

### 3. Локальное хранилище (Room)
Создание структуры для хранения "отложенных" операций.

#### [NEW] [PendingAction.kt](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/java/com/example/myapplication/models/PendingAction.kt)
#### [NEW] [AppDatabase.kt](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/java/com/example/myapplication/data/AppDatabase.kt)

### 4. Фоновая синхронизация (WorkManager)
Реализация воркера, который будет отправлять накопленные данные при появлении сети.

#### [NEW] [SyncWorker.kt](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/java/com/example/myapplication/data/SyncWorker.kt)

### 5. Обновление бизнес-логики (ViewModel)
Интеграция логики проверки сети и сохранения отложенных действий в `ClubViewModel`.

#### [MODIFY] [ClubViewModel.kt](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/java/com/example/myapplication/viewmodel/ClubViewModel.kt)

### 6. UI Уведомления
Добавление индикатора оффлайн-статуса в пользовательский интерфейс.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/nuram/projects/newKAssa-main/app/src/main/java/com/example/myapplication/MainActivity.kt)

## Verification Plan

### Automated Tests
- Проверка сохранения `PendingAction` в базу данных Room при отсутствии сети.
- Проверка срабатывания `WorkManager` при восстановлении соединения.

### Manual Verification
1. Отключить интернет на устройстве.
2. Совершить операцию (например, запуск сессии или покупка в баре).
3. Убедиться, что появилось уведомление "Вы вне сети, данные будут синхронизированы позже".
4. Включить интернет.
5. Проверить в админ-панели Supabase, что данные появились через несколько секунд.
