# Как работает компас Military PDA

## 🧭 Архитектура системы компаса

### Разделение ответственности

Система состоит из трех компонентов:

1. **Military PDA (предмет)** - физический предмет в игре
2. **WrbPlayerData (capability)** - данные игрока, хранимые на сервере
3. **HudOverlay (клиент)** - отображение компаса на экране

## 🔄 Как это работает

### 1. Включение/выключение компаса

Когда игрок использует КПК (ПКМ):

```java
// В MilitaryPdaItem.java
public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (!level.isClientSide) {
        player.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY).ifPresent(data -> {
            // Переключаем состояние в capability игрока
            boolean newState = !data.isCompassActive();
            data.setCompassActive(newState);
            
            // Синхронизируем с клиентом
            WrbNetworking.sendToClient(new SyncWrbDataPacket(data), serverPlayer);
        });
    }
}
```

**Важно:** Состояние компаса хранится в `WrbPlayerData` (capability игрока), а НЕ в NBT тегах КПК!

### 2. Отображение компаса

На клиенте `HudOverlay` проверяет два условия:

```java
// В HudOverlay.java
boolean compassActive = pl.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY)
        .map(WrbPlayerData::isCompassActive)
        .orElse(false);

boolean hasPda = pl.getInventory().contains(ModItems.MILITARY_PDA.get().getDefaultInstance())
        || pl.getOffhandItem().is(ModItems.MILITARY_PDA.get());

if (!compassActive || !hasPda) return;
```

**Условия для отображения компаса:**
1. ✅ Компас активирован (через capability)
2. ✅ У игрока есть КПК в инвентаре или в руке

### 3. Синхронизация

Состояние синхронизируется через пакет:

```java
WrbNetworking.sendToClient(new SyncWrbDataPacket(data), serverPlayer);
```

Этот пакет обновляет capability на клиенте, и `HudOverlay` видит новое состояние.

## 📦 Роль NBT тегов

NBT теги КПК используются **только для метаданных**:

| NBT Тег | Назначение |
|---------|------------|
| `Enabled` | Отображение состояния в tooltip |
| `Owner` | Показать владельца в tooltip |
| `OwnerUUID` | Отладка и идентификация |
| `UsageCount` | Статистика использования |
| `CreatedTime` | Возраст КПК для отладки |

**NBT теги НЕ влияют на работу компаса!**

## ⚙️ Почему такая архитектура?

### Преимущества разделения:

1. **Состояние привязано к игроку, а не к предмету**
   - Компас остается активным, даже если КПК в другой руке
   - Компас работает, если КПК в инвентаре
   - Состояние не теряется при смене КПК

2. **Один источник истины**
   - `WrbPlayerData.isCompassActive()` - единственный источник состояния
   - Нет конфликтов между разными экземплярами КПК
   - Проще синхронизировать клиент-сервер

3. **NBT теги для метаданных**
   - Хранят информацию о конкретном экземпляре КПК
   - Полезны для отладки и статистики
   - Не влияют на функциональность

## 🐛 Решение проблемы "компас не отображается"

### Проблема была НЕ в NBT тегах

Когда пользователь сообщил "компас не отображается", проблема была не связана с добавлением NBT тегов. 

**Возможные причины:**
1. ❌ КПК не был включен (нужно нажать ПКМ)
2. ❌ КПК не в инвентаре
3. ❌ Десинхронизация клиент-сервер
4. ❌ Проблема с capability

### Как проверить работу:

1. **Убедитесь, что КПК в инвентаре:**
   ```java
   boolean hasPda = player.getInventory().contains(ModItems.MILITARY_PDA.get().getDefaultInstance());
   ```

2. **Убедитесь, что компас включен:**
   ```java
   boolean active = player.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY)
           .map(WrbPlayerData::isCompassActive)
           .orElse(false);
   ```

3. **Проверьте синхронизацию:**
   - После использования КПК должен прийти `SyncWrbDataPacket`
   - Capability на клиенте должна обновиться

## 🔍 Отладка

### Если компас не работает:

1. **Проверьте capability на сервере:**
   ```java
   player.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY).ifPresent(data -> {
       System.out.println("Compass active: " + data.isCompassActive());
   });
   ```

2. **Проверьте capability на клиенте:**
   ```java
   Minecraft.getInstance().player.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY).ifPresent(data -> {
       System.out.println("Client compass active: " + data.isCompassActive());
   });
   ```

3. **Проверьте наличие КПК:**
   ```java
   System.out.println("Has PDA: " + player.getInventory().contains(ModItems.MILITARY_PDA.get().getDefaultInstance()));
   ```

## ✅ Что было исправлено

### Изменения:
- ❌ Удален NBT тег `LastUsed` (не нужен)
- ✅ Сохранен механизм работы компаса через capability
- ✅ NBT теги используются только для метаданных
- ✅ Обновлена документация

### Компас работает, потому что:
1. ✅ `MilitaryPdaItem.use()` корректно обновляет `data.setCompassActive()`
2. ✅ Состояние синхронизируется с клиентом через `SyncWrbDataPacket`
3. ✅ `HudOverlay` проверяет правильную capability
4. ✅ NBT теги не мешают работе системы

## 📝 Резюме

- **Компас работает через WrbPlayerData capability**
- **NBT теги КПК - только для метаданных**
- **Состояние компаса привязано к игроку, а не к предмету**
- **Синхронизация клиент-сервер через пакеты**

Если компас не отображается, проблема НЕ в NBT тегах, а в:
- Capability не инициализирована
- Пакет синхронизации не отправлен
- КПК не в инвентаре
- Компас не включен (нужно использовать КПК)