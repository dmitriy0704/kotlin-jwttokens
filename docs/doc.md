# Заметки по проекту

## JWT

# JWT.builder()

Что делает каждый метод в цепочке при генерации JWT
токена в Kotlin/Spring Boot с использованием `io.jsonwebtoken` (JJWT):

```kotlin
val token = Jwts.builder()
    .claims()
    .subject("user123")
    .issuedAt(Date())
    .expiration(Date(System.currentTimeMillis() + 1000 * 60 * 60))
    .signWith(secretKey)
    .compact()
```

🔎 **Что тут происходит:**

1. **`Jwts.builder()`**
   Создаёт новый объект **JWT Builder**. Это конструктор, с помощью которого
   можно "собрать" токен шаг за шагом.

---

2. **`.claims()`**
   Открывает возможность добавлять в токен **claims** (полезную нагрузку, "тело"
   токена).
   Claims — это ключ-значение пары, например:

    * стандартные (`sub`, `iat`, `exp`, `iss`, `aud`)
    * кастомные (например `role: "ADMIN"`, `userId: 42`).

---

3. **`.subject("user123")`**
   Устанавливает **`sub` (subject claim)** — идентификатор субъекта токена.
   Обычно это:

    * `username`
    * `email`
    * `userId`

   👉 Этот параметр часто используется для идентификации пользователя при
   валидации токена.

---

4. **`.issuedAt(Date())`**
   Устанавливает **`iat` (issued at)** — время выпуска токена.
   Используется для проверки "свежести" токена и может пригодиться при
   logout/refresh механизмах.

---

5. **`.expiration(Date(...))`**
   Устанавливает **`exp` (expiration time)** — время истечения токена.
   После этого момента токен будет считаться **недействительным**.
   Обычно выставляют что-то вроде `15 минут` или `1 час`.

---

6. **`.signWith(secretKey)`**
   Подписывает токен с помощью **секретного ключа** (для HMAC) или **приватного
   ключа** (для RSA/ECDSA).
   Без подписи токен не будет защищён от подделки.

   🔑 Здесь и важен твой секретный ключ (`HS256` требует минимум 256-битный
   ключ).

---

7. **`.compact()`**
   Завершает построение токена и возвращает его в виде **строки** (три части,
   разделённые точками: Header.Payload.Signature).

   Пример:

   ```
   eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNjk2MjM4MDAwLCJleHAiOjE2OTYyNDE2MDB9.DfX...
   ```

---

📌 В итоге:

* `subject` = кто пользователь
* `issuedAt` = когда выдан
* `expiration` = до какого времени действует
* `signWith` = защита подписью

---

# Зачем нужен бин userDetailsService

Очень хороший вопрос 👌

Ты заметил, что в конфиге часто создают бин `UserDetailsService`.
Давай разберёмся **зачем он нужен**, если у нас уже есть
`CustomUserDetailsService`.

---

### 🚩 1. Почему это работает без бина?

Если твой `CustomUserDetailsService` помечен как `@Service`, Spring
автоматически зарегистрирует его как бин.
В простом случае этого достаточно: Security найдёт его и будет использовать.

---

### 🚩 2. Когда нужен явный бин?

Иногда ты явно пишешь в `@Configuration`:

```kotlin
@Configuration
class SecurityConfig {

    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService =
        CustomUserDetailsService(userRepository)
}
```

Это может быть полезно в нескольких случаях:

1. **Явная интеграция с `AuthenticationManager`**
   Когда ты сам строишь `AuthenticationManager` через
   `AuthenticationManagerBuilder` или `AuthenticationConfiguration`, Spring
   Security ожидает бин `UserDetailsService`.
   Если он есть — он подхватывается автоматически.

2. **Тесты**
   В тестах удобно замокать `UserDetailsService` или подменить на другой бин (
   например, InMemory). Если у тебя бин определён явно, ты легко его
   переопределишь.

3. **Гибкость**
   Иногда ты хочешь **подменить источник пользователей**:

    * в dev окружении использовать InMemory пользователей
    * в prod — `CustomUserDetailsService` с базой.
      Это проще делать через конфиг с `@Bean`, чем напрямую `@Service`.

---

### 🚩 3. Когда достаточно `@Service`?

Если у тебя **один источник пользователей (например, база через JPA)**, и ты *
*не настраиваешь вручную `AuthenticationManager`**, тогда достаточно:

```kotlin
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService { ... }
```

Spring Security сам подхватит этот бин.

---

✅ **Итог**
`@Bean userDetailsService(...)` нужен, когда ты хочешь **явно контролировать,
какой `UserDetailsService` используется**, особенно если ты настраиваешь
`AuthenticationManager` вручную, работаешь с несколькими источниками
пользователей или пишешь тесты.
В простом проекте достаточно `@Service`.

---

# AuthenticationManager

Отличный вопрос 👍
`AuthenticationManager` — это центральный компонент Spring Security, который
отвечает за сам **процесс аутентификации** (проверку логина/пароля, токена и
т.д.).

---

## 🔎 Что это такое

* Интерфейс `AuthenticationManager` имеет всего один метод:

```kotlin
interface AuthenticationManager {
    fun authenticate(authentication: Authentication): Authentication
}
```

* На вход он получает объект `Authentication` (например,
  `UsernamePasswordAuthenticationToken` с логином и паролем).
* На выход он возвращает **аутентифицированный** объект `Authentication`, в
  котором уже есть данные о пользователе и его ролях.
* Если проверка не удалась → кидается `AuthenticationException`.

## 🛠 Как работает под капотом

1. Внутри `AuthenticationManager` обычно используется **цепочка провайдеров** (
   `AuthenticationProvider`), каждый из которых умеет проверять свой способ
   логина:

    * `DaoAuthenticationProvider` — проверяет логин/пароль через
      `UserDetailsService`
    * `JwtAuthenticationProvider` — проверяет токены
    * `LdapAuthenticationProvider` — LDAP
    * и т.д.

2. Когда ты вызываешь `authenticationManager.authenticate(...)`, он перебирает
   провайдеры и отдаёт задачу тому, кто умеет её обработать.

---

## 📌 Зачем его строить вручную

В Spring Boot 2 раньше можно было просто инжектить `AuthenticationManager`.
В Spring Security 6 чаще делают так:

```kotlin
@Configuration
class SecurityConfig(
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder
) {

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }
}
```

или более явно:

```kotlin
@Bean
fun authenticationManager(): AuthenticationManager {
    val provider = DaoAuthenticationProvider()
    provider.setUserDetailsService(userDetailsService)
    provider.setPasswordEncoder(passwordEncoder)
    return ProviderManager(provider)
}
```

---

## 📍 Где он используется

1. **При логине**
   Когда пользователь отправляет `/login` с логином и паролем, твой контроллер
   вызывает `authenticationManager.authenticate(...)`.

    * Если успех → возвращаешь JWT.
    * Если ошибка → `BadCredentialsException`.

   ```kotlin
   val auth = authenticationManager.authenticate(
       UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
   )
   ```

2. **При JWT-фильтре**
   Можно также вызывать `authenticationManager` для проверки токена (если у тебя
   кастомный `AuthenticationProvider`).

---

✅ **Итог**
`AuthenticationManager` — это движок, который **проверяет подлинность
пользователя**.
Обычно он строится на основе `UserDetailsService + PasswordEncoder`, но ты
можешь расширить его провайдерами (например, для JWT или LDAP).
Строить его самому нужно, если ты хочешь **полный контроль** над процессом
аутентификации (например, кастомный логин-контроллер).


---

# AuthenticationProvider

Очень правильный вопрос 🙌
Давай разберём по кусочкам, что это такое и зачем в конфиге нужен такой бин.

---

## 📌 `AuthenticationProvider` в Spring Security

* `AuthenticationProvider` — это **компонент, который реально выполняет проверку
  данных пользователя** (логин/пароль, токен и т.д.).
* У `AuthenticationManager` может быть несколько провайдеров, он просто
  перебирает их и смотрит, кто сможет обработать запрос.

---

## 📌 `DaoAuthenticationProvider`

Это стандартная реализация `AuthenticationProvider`, которая:

1. Берёт логин из запроса (`UsernamePasswordAuthenticationToken`).
2. Через `UserDetailsService` ищет пользователя в базе.
3. Сравнивает пароли с помощью `PasswordEncoder`.
4. Если всё ок → возвращает аутентифицированного `Authentication` с ролями.

---

## 📌 Что делает твой бин

```kotlin
@Bean
fun authenticationProvider(repository: UserRepository): AuthenticationProvider =
    DaoAuthenticationProvider().also {
        it.setUserDetailsService(userDetailsService(repository)) // как искать пользователей
        it.setPasswordEncoder(encoder()) // как проверять пароль
    }
```

🔎 Здесь:

* `DaoAuthenticationProvider()` — создаём провайдер, который будет работать
  через БД.
* `setUserDetailsService(...)` — указываем, как искать пользователя (через твой
  `CustomUserDetailsService`).
* `setPasswordEncoder(...)` — указываем, каким образом сравнивать пароли (
  например, `BCryptPasswordEncoder`).

Таким образом, ты говоришь Spring Security:
👉 "Проверяй логины через мою БД (`UserRepository`) и сравнивай пароли по моему
правилу (`encoder()`)".

---

## 📌 Зачем явно создавать бин?

1. Чтобы `AuthenticationManager` смог использовать его (он подхватит все
   `AuthenticationProvider` из контекста).
2. Чтобы явно указать, **какой способ аутентификации** применяется (например,
   только через БД).
3. Чтобы легко тестировать / подменять провайдер (например, на in-memory в dev).

---

✅ **Итог**
Этот бин создаёт `DaoAuthenticationProvider`, который подключает твою базу (
`UserDetailsService`) и твой `PasswordEncoder`.
Он нужен, чтобы Spring Security знал **как именно аутентифицировать
пользователей** при логине.

---

# DaoAuthenticationProvider deprecated

Да, ты всё правильно заметил 👌
В **Spring Security 6+** многие вещи, к которым мы привыкли, вроде
`DaoAuthenticationProvider` и ручного создания `AuthenticationManager`, стали
подсвечиваться как `@Deprecated`.

---

### 🚩 Почему так?

Spring Security упростил конфигурацию:

* Теперь не нужно руками создавать `DaoAuthenticationProvider` или даже
  `AuthenticationManager`.
* Достаточно зарегистрировать **`UserDetailsService`** и **`PasswordEncoder`**
  как бины, а фреймворк сам соберёт провайдер и менеджер внутри.

---

### 📌 Пример нового стиля (Spring Boot 3 / Security 6)

```kotlin
@Configuration
class SecurityConfig(
    private val userDetailsService: UserDetailsService
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/auth/**").permitAll()
                auth.anyRequest().authenticated()
            }
            .httpBasic { } // или formLogin / JWT фильтр

        return http.build()
    }

    // 🔑 Главное — просто регистрируем userDetailsService
    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService =
        CustomUserDetailsService(userRepository)
}
```

---

### 📍 Как теперь работает аутентификация?

* Spring видит твой бин `UserDetailsService` + `PasswordEncoder`.
* Автоматически создаёт `DaoAuthenticationProvider` (хоть он и deprecated, но
  под капотом его пока используют).
* Автоматически регистрирует `AuthenticationManager`.

👉 То есть тебе больше не нужно писать:

```kotlin
@Bean
fun authenticationProvider(...): AuthenticationProvider = ...
```

---

### ✅ Итог

* `DaoAuthenticationProvider` подсвечен `deprecated`, потому что **его вручную
  создавать больше не обязательно**.
* Теперь достаточно зарегистрировать:

    * `UserDetailsService` (например, твой `CustomUserDetailsService`)
    * `PasswordEncoder` (например, `BCryptPasswordEncoder`)
* Spring Security соберёт всё остальное автоматически.

---
